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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager.ServiceRegistered;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenActivated;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenDisactivated;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.DefaultResourceCenterService.DefaultPackageResourceCenterIsNotInstalled;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterListShouldBeStored;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
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

	private FlexoProjectResourceFactory<?> flexoProjectResourceFactory;

	public DefaultResourceCenterService() {
	}

	@Override
	public String getServiceName() {
		return "ResourceCenterService";
	}

	/**
	 * Return the {@link FlexoProjectResourceFactory}
	 * 
	 * @return
	 */
	@Override
	public FlexoProjectResourceFactory<?> getFlexoProjectResourceFactory() {
		return flexoProjectResourceFactory;
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

					// System.out.println("Attempt to loading RC " + rcBaseUri);

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

							// TODO : non local resource, is it closed somewhere?
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

			lookupProjectsInResourceCenter(resourceCenter);

			getPropertyChangeSupport().firePropertyChange(RESOURCE_CENTERS, null, resourceCenter);
		}
	}

	/**
	 * Initialize the supplied resource center according to project management
	 * 
	 * @param resourceCenter
	 */
	public final <I> void lookupProjectsInResourceCenter(FlexoResourceCenter<I> resourceCenter) {

		logger.info("--------> lookupProjectsInResourceCenter for " + resourceCenter);

		Iterator<I> it;

		// Then we iterate on all resources found in the resource factory
		it = resourceCenter.iterator();

		while (it.hasNext()) {
			I serializationArtefact = it.next();
			if (!isIgnorable(resourceCenter, serializationArtefact)) {
				FlexoResource<?> r = tryToLookupResource(resourceCenter, serializationArtefact);
				if (r != null) {
					logger.info(">>>>>>>>>> Look-up resource " + r.getImplementedInterface().getSimpleName() + " " + r.getURI());
				}
			}
			if (resourceCenter.isDirectory(serializationArtefact)) {
				foundFolder(resourceCenter, serializationArtefact);
			}
		}

		resourceCenterHasBeenInitialized(resourceCenter);

	}

	public <I> boolean isIgnorable(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		// This allows to ignore all contained VirtualModel, that will be explored from their container resource
		if (resourceCenter.isDirectory(contents)) {
			if (FlexoResourceCenter.isContainedInDirectoryWithSuffix(resourceCenter, contents,
					FlexoProjectResourceFactory.PROJECT_SUFFIX)) {
				return true;
			}
		}
		return false;
	}

	public <I> boolean isFolderIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
		// System.out.println("Tiens, faudrait pas ignorer le folder " + contents);
		if (resourceCenter.isDirectory(contents)) {
			if (FlexoResourceCenter.isContainedInDirectoryWithSuffix(resourceCenter, contents,
					FlexoProjectResourceFactory.PROJECT_SUFFIX)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Internally called to lookup resources from serialization artefacts
	 * 
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return
	 */
	private <I> FlexoProjectResource<I> tryToLookupResource(FlexoResourceCenter<I> resourceCenter, I serializationArtefact) {

		if (getFlexoProjectResourceFactory() == null) {
			// logger.warning("Cannot lookup FlexoProject resources in " + resourceCenter + " because resource factory not available yet");
			return null;
		}

		try {
			if (getFlexoProjectResourceFactory().isValidArtefact(serializationArtefact, resourceCenter)) {
				return ((FlexoProjectResourceFactory) getFlexoProjectResourceFactory()).retrieveResource(serializationArtefact,
						resourceCenter);
			}
			else {
				// Attempt to convert it from older format
				// TODO ??? Any needs ?
				/*I convertedSerializationArtefact = resourceFactory.getConvertableArtefact(serializationArtefact, resourceCenter);
				if (convertedSerializationArtefact != null) {
					R returned = resourceFactory.retrieveResource(convertedSerializationArtefact, resourceCenter);
					returned.setNeedsConversion();
					return returned;
				}*/
			}
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected final <I> void foundFolder(FlexoResourceCenter<I> resourceCenter, I folder) {
		if (resourceCenter.isDirectory(folder) && !isFolderIgnorable(resourceCenter, folder)) {
			// logger.warning("TODO: handle folder for " + folder);
			/*TechnologyAdapterGlobalRepository globalRepository = getGlobalRepository(resourceCenter);
			// Unused RepositoryFolder newRepositoryFolder =
			globalRepository.getRepositoryFolder(folder, true);
			for (ResourceRepository<?, ?> repository : getAllRepositories()) {
				if (repository.getResourceCenter() == resourceCenter)
					((ResourceRepository<?, I>) repository).getRepositoryFolder(folder, true);
			}*/
		}
	}

	protected void resourceCenterHasBeenInitialized(FlexoResourceCenter<?> rc) {
		// Call it to update the current repositories
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> notifyRepositoryStructureChanged());
			// Call it to update the current repositories
		}
		else {
			// Call it to update the current repositories
			notifyRepositoryStructureChanged();
		}
	}

	/**
	 * Called to notify that the structure of registered and/or global repositories has changed
	 */
	public void notifyRepositoryStructureChanged() {

		// TODO ??? Something to fire ?
		/*getPropertyChangeSupport().firePropertyChange("getAllRepositories()", null, getAllRepositories());
		getPropertyChangeSupport().firePropertyChange("getGlobalRepositories()", null, getGlobalRepositories());*/

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
	public class ResourceCenterListShouldBeStored implements ServiceNotification {}

	@Override
	public void initialize() {

		try {
			flexoProjectResourceFactory = new FlexoProjectResourceFactory<Object>(getServiceManager());
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (FlexoResourceCenter<?> resourceCenter : getResourceCenters()) {
			lookupProjectsInResourceCenter(resourceCenter);
		}

		if (getResourceCenters().size() < 1) {
			if (getServiceManager() != null) {
				System.out.println("Trying to install default packaged resource center");
				getServiceManager().notify(this, new DefaultPackageResourceCenterIsNotInstalled());
			}
		}

		status = Status.Started;

	}

	/**
	 * Notification of a new ResourceCenter added to the list of referenced resource centers
	 * 
	 * @author sylvain
	 * 
	 */
	public class DefaultPackageResourceCenterIsNotInstalled implements ServiceNotification {}

	private static void notifyWillWrite(File fileBeeingAdded, FileSystemBasedResourceCenter rc) {
		File rootDirectory = rc.getRootDirectory();
		if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingAdded, true)) {
			rc.willWrite(fileBeeingAdded);
		}
	}

	private static void notifyHasBeenWritten(File fileBeeingAdded, FileSystemBasedResourceCenter rc) {
		File rootDirectory = rc.getRootDirectory();
		if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingAdded, true)) {
			rc.hasBeenWritten(fileBeeingAdded);
		}
	}

	private static void notifyWillRename(File fromFile, File toFile, FileSystemBasedResourceCenter rc) {
		File rootDirectory = rc.getRootDirectory();
		if (FileUtils.directoryContainsFile(rootDirectory, toFile, true)) {
			rc.willRename(fromFile, toFile);
		}
	}

	private static void notifyWillDelete(File fileBeeingDeleted, FileSystemBasedResourceCenter rc) {
		File rootDirectory = rc.getRootDirectory();
		if (FileUtils.directoryContainsFile(rootDirectory, fileBeeingDeleted, true)) {
			rc.willDelete(fileBeeingDeleted);
		}
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (notification instanceof WillWriteFileOnDiskNotification) {
			File fileBeeingAdded = ((WillWriteFileOnDiskNotification) notification).getFile();
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FlexoProject) {
					FlexoProject<?> prj = (FlexoProject<?>) rc;
					if (prj.getDelegateResourceCenter() instanceof FileSystemBasedResourceCenter) {
						notifyWillWrite(fileBeeingAdded, (FileSystemBasedResourceCenter) prj.getDelegateResourceCenter());
					}
					if (!prj.isStandAlone()) {
						if (prj.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
							notifyWillWrite(fileBeeingAdded, (FileSystemBasedResourceCenter) prj.getResourceCenter());
						}
					}
				}
				if (rc instanceof FileSystemBasedResourceCenter) {
					notifyWillWrite(fileBeeingAdded, (FileSystemBasedResourceCenter) rc);
					if (rc.getDelegatingProjectResource() != null) {
						FlexoProjectResource<?> projectResource = rc.getDelegatingProjectResource();
						if (!projectResource.isStandAlone()) {
							if (projectResource.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
								notifyWillWrite(fileBeeingAdded, (FileSystemBasedResourceCenter) projectResource.getResourceCenter());
							}
						}
					}
				}
			}
		}
		if (notification instanceof FileHasBeenWrittenOnDiskNotification) {
			File fileBeeingAdded = ((FileHasBeenWrittenOnDiskNotification) notification).getFile();
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				if (rc instanceof FlexoProject) {
					FlexoProject<?> prj = (FlexoProject<?>) rc;
					if (prj.getDelegateResourceCenter() instanceof FileSystemBasedResourceCenter) {
						notifyHasBeenWritten(fileBeeingAdded, (FileSystemBasedResourceCenter) prj.getDelegateResourceCenter());
					}
					if (!prj.isStandAlone()) {
						if (prj.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
							notifyHasBeenWritten(fileBeeingAdded, (FileSystemBasedResourceCenter) prj.getResourceCenter());
						}
					}
				}
				if (rc instanceof FileSystemBasedResourceCenter) {
					notifyHasBeenWritten(fileBeeingAdded, (FileSystemBasedResourceCenter) rc);
					if (rc.getDelegatingProjectResource() != null) {
						FlexoProjectResource<?> projectResource = rc.getDelegatingProjectResource();
						if (!projectResource.isStandAlone()) {
							if (projectResource.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
								notifyHasBeenWritten(fileBeeingAdded, (FileSystemBasedResourceCenter) projectResource.getResourceCenter());
							}
						}
					}
				}
			}
		}
		if (notification instanceof WillRenameFileOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {

				File fromFile = ((WillRenameFileOnDiskNotification) notification).getFromFile();
				File toFile = ((WillRenameFileOnDiskNotification) notification).getToFile();

				if (rc instanceof FlexoProject) {
					FlexoProject<?> prj = (FlexoProject<?>) rc;
					if (prj.getDelegateResourceCenter() instanceof FileSystemBasedResourceCenter) {
						notifyWillRename(fromFile, toFile, (FileSystemBasedResourceCenter) prj.getDelegateResourceCenter());
					}
					if (!prj.isStandAlone()) {
						if (prj.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
							notifyWillRename(fromFile, toFile, (FileSystemBasedResourceCenter) prj.getResourceCenter());
						}
					}
				}
				if (rc instanceof FileSystemBasedResourceCenter) {
					notifyWillRename(fromFile, toFile, (FileSystemBasedResourceCenter) rc);
					if (rc.getDelegatingProjectResource() != null) {
						FlexoProjectResource<?> projectResource = rc.getDelegatingProjectResource();
						if (!projectResource.isStandAlone()) {
							if (projectResource.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
								notifyWillRename(fromFile, toFile, (FileSystemBasedResourceCenter) projectResource.getResourceCenter());
							}
						}
					}
				}

			}
		}
		if (notification instanceof WillDeleteFileOnDiskNotification) {
			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				File fileBeeingDeleted = ((WillDeleteFileOnDiskNotification) notification).getFile();
				if (rc instanceof FlexoProject) {
					FlexoProject<?> prj = (FlexoProject<?>) rc;
					if (prj.getDelegateResourceCenter() instanceof FileSystemBasedResourceCenter) {
						notifyWillDelete(fileBeeingDeleted, (FileSystemBasedResourceCenter) prj.getDelegateResourceCenter());
					}
					if (!prj.isStandAlone()) {
						if (prj.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
							notifyWillDelete(fileBeeingDeleted, (FileSystemBasedResourceCenter) prj.getResourceCenter());
						}
					}
				}
				if (rc instanceof FileSystemBasedResourceCenter) {
					notifyWillDelete(fileBeeingDeleted, (FileSystemBasedResourceCenter) rc);
					if (rc.getDelegatingProjectResource() != null) {
						FlexoProjectResource<?> projectResource = rc.getDelegatingProjectResource();
						if (!projectResource.isStandAlone()) {
							if (projectResource.getResourceCenter() instanceof FileSystemBasedResourceCenter) {
								notifyWillDelete(fileBeeingDeleted, (FileSystemBasedResourceCenter) projectResource.getResourceCenter());
							}
						}
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
		for (FlexoResourceCenter<?> rc : getResourceCenters()) {
			if (rc.getSerializationArtefactClass().isAssignableFrom(serializationArtefact.getClass())) {
				if (((FlexoResourceCenter<I>) rc).containsArtefact(serializationArtefact)) {
					return (FlexoResourceCenter<I>) rc;
				}
			}
		}

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

	@Override
	public String getDisplayableStatus() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.getDisplayableStatus() + " with " + getResourceCenters().size() + " resource centers");
		for (FlexoResourceCenter<?> rc : getResourceCenters()) {
			sb.append("\n[" + rc.getDefaultBaseURI() + "] with " + rc.getAllResources().size() + " resources");
		}
		return sb.toString();
	}

	@Override
	protected Collection<ServiceOperation<?>> makeAvailableServiceOperations() {
		Collection<ServiceOperation<?>> returned = super.makeAvailableServiceOperations();
		returned.add(ADD_RC);
		return returned;
	}

	public static AddResourceCenter ADD_RC = new AddResourceCenter();

	public static class AddResourceCenter implements ServiceOperation<FlexoResourceCenterService> {
		private AddResourceCenter() {
		}

		@Override
		public String getOperationName() {
			return "add_rc";
		}

		@Override
		public String usage(FlexoResourceCenterService service) {
			return "service " + service.getServiceName() + " add_rc <path>";
		}

		@Override
		public String description() {
			return "add a resource center denoted by supplied path";
		}

		@Override
		public List<String> getOptions() {
			return Arrays.asList("<path>");
		}

		@Override
		public void execute(FlexoResourceCenterService service, Object... options) {
			if (options.length > 0) {
				File directory = (File) options[0];
				System.out.println("Add ResourceCenter from directory " + directory);
				DirectoryResourceCenter newRC;
				try {
					newRC = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(directory, service);
					service.addToResourceCenters(newRC);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
