/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager.ServiceRegistered;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenActivated;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenDisactivated;
import org.openflexo.foundation.resource.FileIODelegate.FileHasBeenWrittenOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.WillDeleteFileOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.WillRenameFileOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.WillWriteFileOnDiskNotification;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;

/**
 * Default implementation for the {@link FlexoResourceCenterService} Manage the {@link UserResourceCenter} and the default
 * {@link DirectoryResourceCenter}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultResourceCenterService extends FlexoServiceImpl implements FlexoResourceCenterService {

	private static final ClassLoader cl = ClassLoader.getSystemClassLoader();

	/**
	 * Instantiate a new DefaultResourceCenterService with only the UserResourceCenter
	 * 
	 * @return
	 */
	public static FlexoResourceCenterService getNewInstance(boolean isDev) {
		try {
			ModelFactory factory = new ModelFactory(FlexoResourceCenterService.class);
			factory.setImplementingClassForInterface(DefaultResourceCenterService.class, FlexoResourceCenterService.class);
			DefaultResourceCenterService returned = (DefaultResourceCenterService) factory.newInstance(FlexoResourceCenterService.class);
			returned.setDevMode(isDev);
			returned.loadAvailableRCFromClassPath();
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Instantiate a new DefaultResourceCenterService by instantiating all {@link FlexoResourceCenter} as it is declared in supplied
	 * {@link ResourceCenterEntry} list
	 * 
	 * @return
	 */
	public static FlexoResourceCenterService getNewInstance(List<ResourceCenterEntry<?>> resourceCenterEntries, boolean isDev) {
		DefaultResourceCenterService returned = (DefaultResourceCenterService) getNewInstance(isDev);
		for (ResourceCenterEntry<?> entry : resourceCenterEntries) {
			FlexoResourceCenter<?> rc = entry.makeResourceCenter(returned);
			if (rc != null) {
				returned.addToResourceCenters(rc);
			}
		}
		return returned;
	}

	public DefaultResourceCenterService() {
	}

	/**
	 * Add all the RCs that contain an identification of a FlexoResourceCenter in META-INF
	 * 
	 * WARNING: should only be called once
	 * 
	 */
	private void loadAvailableRCFromClassPath() {

		logger.info("Loading available  ResourceCenters from classpath");

		Enumeration<URL> urlList;
		ArrayList<FlexoResourceCenter<?>> rcList = new ArrayList<>(this.getResourceCenters());

		try {
			urlList = cl.getResources("META-INF/resourceCenters/" + FlexoResourceCenter.class.getCanonicalName());

			if (urlList != null && urlList.hasMoreElements()) {
				FlexoResourceCenter<?> rc = null;
				boolean rcExists = false;
				while (urlList.hasMoreElements()) {
					URL url = urlList.nextElement();

					StringWriter writer = new StringWriter();
					IOUtils.copy(url.openStream(), writer, "UTF-8");
					String rcBaseUri = writer.toString().trim();

					System.out.println("Attempt to loading RC " + rcBaseUri);

					rcExists = false;
					for (FlexoResourceCenter<?> r : rcList) {
						rcExists = r.getDefaultBaseURI().equals(rcBaseUri) || rcExists;
					}
					if (!rcExists) {
						if (url.getProtocol().equals("file")) {
							// When it is a file and it is contained in
							// target/classes directory then we
							// replace with directory from source code
							// (development mode)
							String dirPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("META-INF")), "UTF-8");

							if (isDevMode()) {
								// Was like this with 1.8.0 infrastructure with Maven
								dirPath = dirPath.replace("target/classes", "src/main/resources");
								// Is now like this with 1.8.1+ infrastructure with Gradle
								dirPath = dirPath.replace("/bin/", "/src/main/resources/");
								dirPath = dirPath.replace("build/resources/main", "/src/main/resources/");
							}

							File rcDir = new File(dirPath);
							if (rcDir.exists()) {
								rc = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(rcDir, this);
							}
						}
						else if (url.getProtocol().equals("jar")) {

							String jarPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("!")).replace("+", "%2B"),
									"UTF-8");

							URL jarURL = new URL(jarPath);
							URI jarURI = new URI(jarURL.getProtocol(), jarURL.getUserInfo(), jarURL.getHost(), jarURL.getPort(),
									jarURL.getPath(), jarURL.getQuery(), jarURL.getRef());

							rc = JarResourceCenter.addJarFile(new JarFile(new File(jarURI)), this);

						}
						else {
							logger.warning("INVESTIGATE: don't know how to deal with RC accessed through " + url.getProtocol());
						}
					}
					else {
						logger.warning("an RC already exists with DefaultBaseURI: " + rcBaseUri);
					}

					if (rc != null) {
						rc.setDefaultBaseURI(rcBaseUri);
						rc.getResourceCenterEntry().setIsSystemEntry(true);
						this.addToResourceCenters(rc);
						rc = null;
					}

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addToResourceCenters(FlexoResourceCenter<?> resourceCenter) {
		if (!getResourceCenters().contains(resourceCenter)) {
			// logger.info("###################################
			// addToResourceCenters() " + resourceCenter);
			performSuperAdder(RESOURCE_CENTERS, resourceCenter);
			if (getServiceManager() != null) {
				getServiceManager().notify(this, new ResourceCenterAdded(resourceCenter));
			}
			getPropertyChangeSupport().firePropertyChange(RESOURCE_CENTERS, null, resourceCenter);
		}
	}

	@Override
	public void removeFromResourceCenters(FlexoResourceCenter<?> resourceCenter) {
		if (getResourceCenters().contains(resourceCenter)) {
			performSuperRemover(RESOURCE_CENTERS, resourceCenter);
		}

		// unload resources from resource center
		for (FlexoResource<?> resource : resourceCenter.getAllResources(null)) {
			if (resource.isLoaded()) {
				resource.unloadResourceData(true);
			}
		}
		// TODO: dereference all resources registered in this ResourceCenter

		if (getServiceManager() != null) {
			getServiceManager().notify(this, new ResourceCenterRemoved(resourceCenter));
		}
		getPropertyChangeSupport().firePropertyChange(RESOURCE_CENTERS, resourceCenter, null);
	}

	@Override
	public FlexoResourceCenter<?> getFlexoResourceCenter(String baseURI) {
		for (FlexoResourceCenter<?> rc : getResourceCenters()) {
			if (baseURI.equals(rc.getDefaultBaseURI())) {
				return rc;
			}
		}
		return null;
	}

	/**
	 * Notification of a new ResourceCenter added to the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class ResourceCenterAdded implements ServiceNotification {
		private final FlexoResourceCenter<?> addedResourceCenter;

		public ResourceCenterAdded(FlexoResourceCenter<?> addedResourceCenter) {
			this.addedResourceCenter = addedResourceCenter;
		}

		public FlexoResourceCenter<?> getAddedResourceCenter() {
			return addedResourceCenter;
		}
	}

	/**
	 * Notification of a new ResourceCenter removed from the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class ResourceCenterRemoved implements ServiceNotification {
		private final FlexoResourceCenter<?> removedResourceCenter;

		public ResourceCenterRemoved(FlexoResourceCenter<?> removedResourceCenter) {
			this.removedResourceCenter = removedResourceCenter;
		}

		public FlexoResourceCenter<?> getRemovedResourceCenter() {
			return removedResourceCenter;
		}
	}

	/**
	 * Save all locations for registered resource centers on disk
	 */
	@Override
	public void storeDirectoryResourceCenterLocations() {
		if (getServiceManager() != null) {
			System.out.println("Saving the directory resource center locations...");
			getServiceManager().notify(this, new ResourceCenterListShouldBeStored());
		}
	}

	/**
	 * Notification of a new ResourceCenter added to the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class ResourceCenterListShouldBeStored implements ServiceNotification {
	}

	@Override
	public void initialize() {
		if (getResourceCenters().size() < 1) {
			if (getServiceManager() != null) {
				System.out.println("Trying to install default packaged resource center");
				getServiceManager().notify(this, new DefaultPackageResourceCenterIsNotInstalled());
			}
		}

	}

	/**
	 * Notification of a new ResourceCenter added to the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class DefaultPackageResourceCenterIsNotInstalled implements ServiceNotification {
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (notification instanceof WillWriteFileOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					File rootDirectory = ((FileSystemBasedResourceCenter) rc).getRootDirectory();
					File fileBeeingAdded = ((WillWriteFileOnDiskNotification) notification).getFile();
					if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingAdded, true)) {
						((FileSystemBasedResourceCenter) rc).willWrite(fileBeeingAdded);
					}
				}
			}
		}
		if (notification instanceof FileHasBeenWrittenOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					File rootDirectory = ((FileSystemBasedResourceCenter) rc).getRootDirectory();
					File fileBeeingAdded = ((FileHasBeenWrittenOnDiskNotification) notification).getFile();
					if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingAdded, true)) {
						((FileSystemBasedResourceCenter) rc).hasBeenWritten(fileBeeingAdded);
					}
				}
			}
		}
		if (notification instanceof WillRenameFileOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					File rootDirectory = ((FileSystemBasedResourceCenter) rc).getRootDirectory();
					File fromFile = ((WillRenameFileOnDiskNotification) notification).getFromFile();
					File toFile = ((WillRenameFileOnDiskNotification) notification).getToFile();
					if (FileUtils.directoryContainsFile(rootDirectory, fromFile, true)) {
						((FileSystemBasedResourceCenter) rc).willRename(fromFile, toFile);
					}
				}
			}
		}
		if (notification instanceof WillDeleteFileOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					File rootDirectory = ((FileSystemBasedResourceCenter) rc).getRootDirectory();
					File fileBeeingDeleted = ((WillDeleteFileOnDiskNotification) notification).getFile();
					if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingDeleted, true)) {
						((FileSystemBasedResourceCenter) rc).willDelete(fileBeeingDeleted);
					}
				}
			}
		}
		if (caller instanceof TechnologyAdapterService) {
			if (notification instanceof ServiceRegistered) {
				/*
				 * for (FlexoResourceCenter rc : getResourceCenters()) {
				 * rc.initialize((TechnologyAdapterService) caller); }
				 */
			}
			else if (notification instanceof TechnologyAdapterHasBeenActivated) {
				// Avoid Concurrent Modification Exception issues
				ArrayList<FlexoResourceCenter<?>> listRC = new ArrayList<>(getResourceCenters());
				for (FlexoResourceCenter<?> rc : listRC) {
					if (rc != null) {
						rc.activateTechnology(((TechnologyAdapterHasBeenActivated) notification).getTechnologyAdapter());
					}
				}
			}
			else if (notification instanceof TechnologyAdapterHasBeenDisactivated) {
				for (FlexoResourceCenter<?> rc : getResourceCenters()) {
					if (rc != null) {
						rc.disactivateTechnology(((TechnologyAdapterHasBeenDisactivated) notification).getTechnologyAdapter());
					}
				}
			}
		}

	}

	/**
	 * Shutdowns all the ResourceCenter
	 */

	@Override
	public void stop() {
		List<FlexoResourceCenter<?>> RCs = getResourceCenters();

		for (FlexoResourceCenter<?> r : RCs) {
			r.stop();
		}
	}

	@Override
	public <I> FlexoResourceCenter<I> getResourceCenterContaining(I serializationArtefact) {
		System.out.println("Je cherche un RC qui pourrait contenir " + serializationArtefact);
		System.out.println("TODO !!!");
		return null;
	}

	private boolean devMode = false;

	@Override
	public boolean isDevMode() {
		return devMode;
	}

	@Override
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

}
