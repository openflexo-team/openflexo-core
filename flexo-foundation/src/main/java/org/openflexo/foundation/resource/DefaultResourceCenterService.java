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
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.resource.PamelaResourceImpl.WillDeleteFileOnDiskNotification;
import org.openflexo.foundation.resource.PamelaResourceImpl.WillRenameFileOnDiskNotification;
import org.openflexo.foundation.resource.PamelaResourceImpl.WillWriteFileOnDiskNotification;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

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
	public static FlexoResourceCenterService getNewInstance() {
		try {
			ModelFactory factory = new ModelFactory(FlexoResourceCenterService.class);
			factory.setImplementingClassForInterface(DefaultResourceCenterService.class, FlexoResourceCenterService.class);
			DefaultResourceCenterService returned = (DefaultResourceCenterService) factory.newInstance(FlexoResourceCenterService.class);
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
	public static FlexoResourceCenterService getNewInstance(List<ResourceCenterEntry<?>> resourceCenterEntries) {
		DefaultResourceCenterService returned = (DefaultResourceCenterService) getNewInstance();
		for (ResourceCenterEntry<?> entry : resourceCenterEntries) {
			FlexoResourceCenter rc = entry.makeResourceCenter(returned);
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
	 */
	public void loadAvailableRCFromClassPath() {

		logger.info("Loading available  ResourceCenters from classpath");

		Enumeration<URL> urlList;
		try {
			urlList = cl.getResources("META-INF/resourceCenters/" + FlexoResourceCenter.class.getCanonicalName());

			if (urlList != null && urlList.hasMoreElements()) {
				FlexoResourceCenter rc = null;
				while (urlList.hasMoreElements()) {
					URL url = urlList.nextElement();

					StringWriter writer = new StringWriter();
					IOUtils.copy(url.openStream(), writer, "UTF-8");
					String rcBaseUri = writer.toString();

					if (url.getProtocol().equals("file")) {
						String dirPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("META-INF")), "UTF-8");
						File rcDir = new File(dirPath);
						if (rcDir.exists()) {
							rc = new DirectoryResourceCenter(rcDir, this);
							rc.setDefaultBaseURI(rcBaseUri);
							this.addToResourceCenters(rc);
						}
					}
					else if (url.getProtocol().equals("jar")) {

						String jarPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("!")).replace("+", "%2B"),
								"UTF-8");

						URI jarURI = new URI(jarPath);
						rc = JarResourceCenter.addJarFile(new JarFile(new File(jarURI)), this);

					}
					else {
						logger.warning("INVESTIGATE: don't know how to deal with RC accessed through " + url.getProtocol());
					}

					if (rc != null) {
						rc.setDefaultBaseURI(rcBaseUri);
						this.addToResourceCenters(rc);
						rc = null;
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addToResourceCenters(FlexoResourceCenter resourceCenter) {
		if (!getResourceCenters().contains(resourceCenter)) {
			performSuperAdder(RESOURCE_CENTERS, resourceCenter);
		}
		if (getServiceManager() != null) {
			getServiceManager().notify(this, new ResourceCenterAdded(resourceCenter));
		}
		getPropertyChangeSupport().firePropertyChange(RESOURCE_CENTERS, null, resourceCenter);
	}

	@Override
	public void removeFromResourceCenters(FlexoResourceCenter resourceCenter) {
		if (getResourceCenters().contains(resourceCenter)) {
			performSuperRemover(RESOURCE_CENTERS, resourceCenter);
		}
		// TODO: dereference all resources registerd in this ResourceCenter

		// The resource center must be been dereferenced

		/*
		ViewPointRepository vpr = newRC.getViewPointRepository();
		for (ViewPointResource vpR : vpr.getAllResources()) {
			if (((FileSystemBasedResourceCenter) vpr.getResourceCenter()).getResource(vpR.getURI()) != null) {
				vpR.unloadResourceData();
				unregisterViewPoint(vpR);
				vpr.unregisterResource(vpR);
			}
		}
		vpr.delete();*/

		if (getServiceManager() != null) {
			getServiceManager().notify(this, new ResourceCenterRemoved(resourceCenter));
		}
		getPropertyChangeSupport().firePropertyChange(RESOURCE_CENTERS, null, resourceCenter);
	}

	/**
	 * Notification of a new ResourceCenter added to the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class ResourceCenterAdded implements ServiceNotification {
		private final FlexoResourceCenter addedResourceCenter;

		public ResourceCenterAdded(FlexoResourceCenter addedResourceCenter) {
			this.addedResourceCenter = addedResourceCenter;
		}

		public FlexoResourceCenter getAddedResourceCenter() {
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
		private final FlexoResourceCenter removedResourceCenter;

		public ResourceCenterRemoved(FlexoResourceCenter removedResourceCenter) {
			this.removedResourceCenter = removedResourceCenter;
		}

		public FlexoResourceCenter getRemovedResourceCenter() {
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
			for (FlexoResourceCenter rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					((FileSystemBasedResourceCenter) rc).willWrite(((WillWriteFileOnDiskNotification) notification).getFile());
				}
			}
		}
		if (notification instanceof WillRenameFileOnDiskNotification) {
			for (FlexoResourceCenter rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					((FileSystemBasedResourceCenter) rc).willRename(((WillRenameFileOnDiskNotification) notification).getFromFile(),
							((WillRenameFileOnDiskNotification) notification).getToFile());
				}
			}
		}
		if (notification instanceof WillDeleteFileOnDiskNotification) {
			for (FlexoResourceCenter rc : getResourceCenters()) {
				if (rc instanceof FileSystemBasedResourceCenter) {
					((FileSystemBasedResourceCenter) rc).willDelete(((WillDeleteFileOnDiskNotification) notification).getFile());
				}
			}
		}
		if (caller instanceof TechnologyAdapterService) {
			if (notification instanceof ServiceRegistered) {
				/*for (FlexoResourceCenter rc : getResourceCenters()) {
					rc.initialize((TechnologyAdapterService) caller);
				}*/
			}
			else if (notification instanceof TechnologyAdapterHasBeenActivated) {
				// Avoid Concurrent Modification Exception issues
				ArrayList<FlexoResourceCenter> listRC = new ArrayList<FlexoResourceCenter>(getResourceCenters());
				for (FlexoResourceCenter rc : listRC) {
					if (rc != null) {
						rc.activateTechnology(((TechnologyAdapterHasBeenActivated) notification).getTechnologyAdapter());
					}
				}
			}
			else if (notification instanceof TechnologyAdapterHasBeenDisactivated) {
				for (FlexoResourceCenter rc : getResourceCenters()) {
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
		List<FlexoResourceCenter> RCs = this.getResourceCenters();

		for (FlexoResourceCenter r : RCs) {
			r.stop();
		}
	}

}
