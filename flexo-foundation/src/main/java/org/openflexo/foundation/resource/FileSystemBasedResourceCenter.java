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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.CompilationUnitRepository;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate.DirectoryBasedIODelegateImpl;
import org.openflexo.foundation.resource.FileIODelegate.FileHasBeenWrittenOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.FileIODelegateImpl;
import org.openflexo.foundation.resource.FileIODelegate.WillWriteFileOnDiskNotification;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.DirectoryWatcher;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;
import org.openflexo.xml.XMLRootElementReader;

/**
 * An abstract implementation of a {@link FlexoResourceCenter} based on a file system.
 * 
 * It defines a {@link File} which is a directory containing all resources
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FileSystemBasedResourceCenter.FileSystemBasedResourceCenterImpl.class)
public interface FileSystemBasedResourceCenter extends FlexoResourceCenter<File> {

	/**
	 * Equivalent to {@link #getBaseArtefact()}
	 * 
	 * Return directory represented by this {@link FileSystemBasedResourceCenter}
	 * 
	 * @return
	 */
	public File getRootDirectory();

	/**
	 * Return {@link DirectoryWatcher} attached to this {@link FileSystemBasedResourceCenter}
	 * 
	 * @return
	 */
	public DirectoryWatcher getDirectoryWatcher();

	/**
	 * Start asynchronous call to examine directory for structure modifications<br>
	 * This start a timer that will be triggered every second
	 */
	public void startDirectoryWatching();

	/**
	 * Stop directory watching: stops the timer
	 */
	public void stopDirectoryWatching();

	/**
	 * Synchronous call to examine directory for structure modifications<br>
	 * Return when the exploring task is finished
	 */
	public void performDirectoryWatchingNow();

	/**
	 * Called when a {@link File} has been modified in directory representing this ResourceCenter
	 * 
	 * @param file
	 */
	public void fileModified(File file);

	/**
	 * Called when a new {@link File} has been discovered in directory representing this ResourceCenter
	 * 
	 * @param file
	 */
	public void fileAdded(File file);

	/**
	 * Called when a new {@link File} has been deleted in directory representing this ResourceCenter
	 * 
	 * @param file
	 */
	public void fileDeleted(File file);

	public void fireAddedFilesToBeNotified();

	public void fireDeletedFilesToBeNotified();

	public void willWrite(File file);

	public void hasBeenWritten(File file);

	public void willRename(File fromFile, File toFile);

	public void willDelete(File file);

	/**
	 * Return the {@link FileSystemMetaDataManager} attached to this {@link FileSystemBasedResourceCenter}
	 * 
	 * @return
	 */
	public FileSystemMetaDataManager getMetaDataManager();

	public File retrieveResourceFile(String resourceName, String relativePath, String extension);

	public static abstract class FileSystemBasedResourceCenterImpl extends ResourceRepositoryImpl<FlexoResource<?>, File>
			implements FileSystemBasedResourceCenter {

		protected static final Logger logger = Logger.getLogger(FileSystemBasedResourceCenter.class.getPackage().getName());

		// Delay of DirectoryWatcher, in seconds
		public static long DIRECTORY_WATCHER_DELAY = 3;

		// private File rootDirectory;
		private FlexoResourceCenterService rcService;
		private final FileSystemMetaDataManager fsMetaDataManager = new FileSystemMetaDataManager();

		private final Map<TechnologyAdapter<?>, HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>>> repositories = new HashMap<>();

		@Override
		public Class<File> getSerializationArtefactClass() {
			return File.class;
		}

		/**
		 * Return {@link FlexoResourceCenterService} managing this {@link FlexoResourceCenter}
		 * 
		 * @return
		 */
		@Override
		public FlexoResourceCenterService getFlexoResourceCenterService() {
			return rcService;
		}

		/**
		 * Sets {@link FlexoResourceCenterService} managing this {@link FlexoResourceCenter}
		 * 
		 * @return
		 */
		@Override
		public void setFlexoResourceCenterService(FlexoResourceCenterService rcService) {
			this.rcService = rcService;
		}

		@Override
		public FlexoResourceCenter<File> getResourceCenter() {
			return this;
		}

		public File getDirectory() {
			return getRootDirectory();
		}

		@Override
		public File getRootDirectory() {
			return getBaseArtefact();
		}

		private static final FileSystemResourceLocatorImpl FS_RESOURCE_LOCATOR = new FileSystemResourceLocatorImpl();

		@Override
		public Resource getBaseArtefactAsResource() {
			return FS_RESOURCE_LOCATOR.retrieveResource(getBaseArtefact());
		}

		/**
		 * Return (first when many) resource matching supplied File
		 */
		@Override
		public <R extends FlexoResource<?>> R getResource(File resourceArtifact, Class<R> resourceClass) {
			if (!FileUtils.directoryContainsFile(getRootDirectory(), resourceArtifact, true)) {
				return null;
			}

			// try {
			for (FlexoResource<?> r : getAllResources()) {
				if (Objects.equals(r.getIODelegate().getSerializationArtefact(), resourceArtifact)) {
					if (resourceClass.isInstance(r)) {
						return resourceClass.cast(r);
					}
					logger.warning("Found resource matching file " + resourceArtifact + " but not of desired type: " + r.getClass()
							+ " instead of " + resourceClass);
					return null;
				}
			}

			/*
			// searches for parent folder.
			RepositoryFolder<?, File> folder = getParentRepositoryFolder(resourceArtifact, false);
			
			// When not found
			// It might be a resource artefact encoded as a directory based, try to find parent folder
			if (folder == null) {
				folder = getParentRepositoryFolder(resourceArtifact.getParentFile(), false);
			}
			
			if (folder == null) {
				return null;
			}
			
			for (FlexoResource<?> r : folder.getResources()) {
				if (Objects.equals(r.getIODelegate().getSerializationArtefact(), resourceArtifact)) {
					if (resourceClass.isInstance(r)) {
						return resourceClass.cast(r);
					}
					logger.warning("Found resource matching file " + resourceArtifact + " but not of desired type: " + r.getClass()
							+ " instead of " + resourceClass);
					return null;
				}
			}*/

			// Cannot find the resource
			return null;

			/*} catch (IOException e) {
				logger.log(Level.WARNING, "Error while getting parent folder for " + resourceArtifact, e);
				return null;
			}*/
		}

		@Override
		public String toString() {
			return super.toString() + " directory=" + (getRootDirectory() != null ? getRootDirectory().getAbsolutePath() : null);
		}

		@Override
		public CompilationUnitRepository<File> getVirtualModelRepository() {
			if (getServiceManager() != null) {
				FMLTechnologyAdapter vmTA = getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLTechnologyAdapter.class);
				// return getRepository(VirtualModelRepository.class, vmTA);
				return vmTA.getVirtualModelRepository(this);
			}
			return null;
		}

		@Override
		public FMLRTVirtualModelInstanceRepository<File> getVirtualModelInstanceRepository() {
			if (getServiceManager() != null) {
				FMLRTTechnologyAdapter vmRTTA = getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
				return vmRTTA.getVirtualModelInstanceRepository(this);
			}
			return null;
		}

		/**
		 * Retrieve (creates it when not existant) folder containing supplied file
		 * 
		 * @param repository
		 * @param aFile
		 * @return
		 */
		protected <R extends FlexoResource<?>> RepositoryFolder<R, File> retrieveRepositoryFolder(ResourceRepository<R, File> repository,
				File aFile) {
			try {
				return repository.getParentRepositoryFolder(aFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				return repository.getRootFolder();
			}
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getFlexoResourceCenterService() == null)
				return super.getServiceManager();
			return getFlexoResourceCenterService().getServiceManager();
		}

		@Override
		public Iterator<File> iterator() {
			List<File> allFiles = new ArrayList<>();
			if (getRootDirectory() != null)
				appendFiles(getRootDirectory(), allFiles);
			else
				logger.warning("ResourceCenter: " + this + " rootDirectory is null");
			return allFiles.iterator();
		}

		private void appendFiles(File directory, List<File> files) {
			if (directory.exists() && directory.isDirectory() && directory.canRead()) {
				if (directory.listFiles() != null) {
					for (File f : directory.listFiles()) {
						if (!isIgnorable(f, null)) {
							files.add(f);
							if (f.isDirectory())
								appendFiles(f, files);
						}
					}
				}
			}
		}

		@Override
		public String getName() {
			return getDefaultBaseURI();
		}

		private DirectoryWatcher directoryWatcher;

		private ScheduledFuture<?> scheduleWithFixedDelay;

		@Override
		public DirectoryWatcher getDirectoryWatcher() {
			return directoryWatcher;
		}

		/**
		 * Synchronous call to examine directory for structure modifications<br>
		 * Return when the exploring task is finished
		 */
		@Override
		public void performDirectoryWatchingNow() {
			// directoryWatcher = new FileSystemBasedDirectoryWatcher(getRootDirectory());
			if (directoryWatcher != null) {
				if (directoryWatcher.isRunning()) {
					stopDirectoryWatching();
					directoryWatcher.waitCurrentExecution();
					directoryWatcher.runNow();
					startDirectoryWatching();
				}
				else {
					directoryWatcher.runNow();
				}
			}
		}

		public static class FileSystemBasedDirectoryWatcher extends DirectoryWatcher {

			private FileSystemBasedResourceCenter resourceCenter;

			public FileSystemBasedDirectoryWatcher(FileSystemBasedResourceCenter resourceCenter) {
				super(resourceCenter.getBaseArtefact());
				this.resourceCenter = resourceCenter;
			}

			@Override
			protected void fileModified(File file) {
				resourceCenter.fileModified(file);
			}

			@Override
			protected void fileAdded(File file) {
				resourceCenter.fileAdded(file);
			}

			@Override
			protected void fileDeleted(File file) {
				resourceCenter.fileDeleted(file);
			}

			@Override
			protected void fileRenamed(File oldFile, File renamedFile) {
			}

			@Override
			protected void performRun() {
				if (DEBUG)
					System.out.println("BEGIN performRun() for " + FileSystemBasedDirectoryWatcher.this);
				super.performRun();
				if (DEBUG)
					System.out.println("Looking for files to be notified " + FileSystemBasedDirectoryWatcher.this);
				resourceCenter.fireAddedFilesToBeNotified();
				resourceCenter.fireDeletedFilesToBeNotified();
				isRunning = false;
				if (DEBUG)
					System.out.println("END performRun() for " + FileSystemBasedDirectoryWatcher.this);
			}
		}

		/**
		 * Start asynchronous call to examine directory for structure modifications<br>
		 * This start a timer that will be triggered every second
		 */
		@Override
		public void startDirectoryWatching() {
			if (getRootDirectory() != null && getRootDirectory().exists()) {
				if (directoryWatcher == null) {
					directoryWatcher = new FileSystemBasedDirectoryWatcher(this);
				}
				ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
				scheduleWithFixedDelay = newScheduledThreadPool.scheduleWithFixedDelay(directoryWatcher, 0, DIRECTORY_WATCHER_DELAY,
						TimeUnit.SECONDS);
			}
		}

		@Override
		public void stopDirectoryWatching() {
			if (getRootDirectory() != null && getRootDirectory().exists() && scheduleWithFixedDelay != null) {
				scheduleWithFixedDelay.cancel(true);
			}
		}

		@Override
		public void fileModified(File file) {
			if (!isIgnorable(file, null)) {
				FlexoResource<?> updatedResource = registeredResources.get(file);
				System.out.println("File MODIFIED " + file.getName() + " in " + file.getParentFile().getAbsolutePath() + " resource="
						+ updatedResource);
				if (updatedResource != null && updatedResource.isLoaded() && updatedResource.isUpdatable()) {
					updatedResource.updateResourceData();
				}
			}
			else {
				// OK, the file was declared as beeing written
				// Now, we have to detect it again
				if (willBeWrittenFiles.contains(file)) {
					willBeWrittenFiles.remove(file);
				}
			}
		}

		private Map<File, FlexoResource<?>> registeredResources = new HashMap<>();

		/**
		 * Called to register a resource relatively to its serialization artefact
		 * 
		 * @param resource
		 * @param serializationArtefact
		 */
		@Override
		public void registerResource(FlexoResource<?> resource, File serializationArtefact) {
			resources.put(resource.getURI(), resource);
			registeredResources.put(serializationArtefact, resource);
		}

		/**
		 * Called to register a resource relatively to its serialization artefact
		 * 
		 * @param resource
		 * @param serializationArtefact
		 */
		@Override
		public void unregisterResource(FlexoResource<?> resource, File serializationArtefact) {
			// unregisterResource(resource);
			resources.remove(resource.getURI(), resource);
			registeredResources.remove(serializationArtefact);
		}

		private final Map<TechnologyAdapter<?>, List<File>> addedFilesToBeRenotified = new HashMap<>();
		private final Map<TechnologyAdapter<?>, List<File>> removedFilesToBeRenotified = new HashMap<>();
		// Unused private final Map<TechnologyAdapter<?>, List<File>> modifiedFilesToBeRenotified = new HashMap<>();
		// Unused private final Map<TechnologyAdapter<?>, Map<File, File>> renamedFilesToBeRenotified = new HashMap<>();

		/**
		 * Notify that a new File has been discovered in directory representing this ResourceCenter
		 * 
		 * @param file
		 * @return a boolean indicating if this file has been handled by a least one technology
		 */
		@Override
		public void fileAdded(File file) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(
						"File ADDED in resource center " + this + " : " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				for (TechnologyAdapter<?> adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					if (!isIgnorable(file, adapter)) {
						List<File> filesToBeNotified = addedFilesToBeRenotified.get(adapter);
						if (filesToBeNotified == null) {
							filesToBeNotified = new ArrayList<>();
							addedFilesToBeRenotified.put(adapter, filesToBeNotified);
						}
						if (adapter.isActivated()) {
							if (!adapter.contentsAdded(this, file)) {
								filesToBeNotified.add(file);
							}
						}
					}
					dismissIgnoredFilesWhenRequired(file, adapter);
				}

			}
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Done: File ADDED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}
		}

		@Override
		public void fireAddedFilesToBeNotified() {
			// System.out.println("fireAddedFilesToBeNotified()");
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				for (TechnologyAdapter<?> adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					if (adapter.isActivated()) {
						List<File> filesToBeNotified = addedFilesToBeRenotified.get(adapter);
						if (filesToBeNotified != null && filesToBeNotified.size() > 0) {
							for (File f : new ArrayList<>(filesToBeNotified)) {
								if (logger.isLoggable(Level.FINE)) {
									logger.fine("fileAdded (discovered later)" + f + " with adapter " + adapter.getName() + " : " + f);
								}
								adapter.contentsAdded(this, f);
								filesToBeNotified.remove(f);
							}
						}
					}
					/*else {
						System.out.println("Files ignored for TA: " + adapter + " : " + addedFilesToBeRenotified.get(adapter));
					}*/
				}
			}
		}

		@Override
		public void fileDeleted(File file) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("File DELETED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}
			if (getServiceManager() != null) {
				for (TechnologyAdapter<?> adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					if (!isIgnorable(file, adapter)) {
						List<File> filesToBeNotified = removedFilesToBeRenotified.get(adapter);
						if (filesToBeNotified == null) {
							filesToBeNotified = new ArrayList<>();
							removedFilesToBeRenotified.put(adapter, filesToBeNotified);
						}
						if (adapter.isActivated()) {
							if (logger.isLoggable(Level.FINE)) {
								logger.fine("fileDeleted " + file + " with adapter " + adapter.getName());
							}
							if (TechnologyAdapter.contentsDeleted(this, file)) {
								filesToBeNotified.remove(file);
							}
							else {
								filesToBeNotified.add(file);
							}
						}
					}
				}
			}
		}

		@Override
		public void fireDeletedFilesToBeNotified() {
			// System.out.println("fireDeletedFilesToBeNotified()");
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				for (TechnologyAdapter<?> adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					if (adapter.isActivated()) {
						List<File> filesToBeNotified = removedFilesToBeRenotified.get(adapter);
						if (filesToBeNotified != null && filesToBeNotified.size() > 0) {
							for (File f : new ArrayList<>(filesToBeNotified)) {
								if (TechnologyAdapter.contentsDeleted(this, f)) {
									filesToBeNotified.remove(f);
									if (logger.isLoggable(Level.FINE)) {
										logger.fine(
												"fileDeleted (discovered later)" + f + " with adapter " + adapter.getName() + " : " + f);
									}
								}
								if (logger.isLoggable(Level.FINE)) {
									logger.fine("fileDeleted but ignored for adapter " + adapter.getName() + " : " + f);
								}
							}
						}
					}
					// XTOF: avoid infinite loop of notifications
					if (removedFilesToBeRenotified != null && removedFilesToBeRenotified.get(adapter) != null) {
						removedFilesToBeRenotified.get(adapter).clear();
					}
				}
			}
		}

		protected void fileRenamed(File oldFile, File renamedFile) {
			if (!isIgnorable(renamedFile, null)) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("File RENAMED from  " + oldFile.getName() + " to " + renamedFile.getName() + " in "
							+ renamedFile.getParentFile().getAbsolutePath());
				}
				/*if (technologyAdapterService != null) {
					for (TechnologyAdapter adapter : technologyAdapterService.getTechnologyAdapters()) {
						logger.info("fileDeleted " + file + " with adapter " + adapter.getName());
						adapter.contentsDeleted(this, file);
					}
				}*/
			}
		}

		private final List<File> willBeWrittenFiles = new ArrayList<>();
		private final Map<TechnologyAdapter<?>, List<File>> writtenFiles = new HashMap<>();
		private final List<File> willBeRenamedFiles = new ArrayList<>();
		private final List<File> willBeRenamedAsFiles = new ArrayList<>();
		private final List<File> willBeDeletedFiles = new ArrayList<>();

		@Override
		public void willWrite(File file) {
			if (!willBeWrittenFiles.contains(file)) {
				willBeWrittenFiles.add(file);
			}
		}

		@Override
		public void hasBeenWritten(File file) {
			for (TechnologyAdapter<?> ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				List<File> l = writtenFiles.get(ta);
				if (l == null) {
					l = new ArrayList<>();
					writtenFiles.put(ta, l);
				}
				if (!l.contains(file)) {
					l.add(file);
				}
			}
		}

		@Override
		public void willRename(File fromFile, File toFile) {
			willBeRenamedFiles.add(fromFile);
			willBeRenamedAsFiles.add(toFile);
		}

		@Override
		public void willDelete(File file) {
			willBeDeletedFiles.add(file);
		}

		private void dismissIgnoredFilesWhenRequired(File file, TechnologyAdapter<?> technologyAdapter) {
			if (technologyAdapter == null) {
				return;
			}
			List<File> filesBeeingWritten = writtenFiles.get(technologyAdapter);
			if (filesBeeingWritten != null && filesBeeingWritten.contains(file)) {
				filesBeeingWritten.remove(file);
			}
			boolean fileIsStillToBeIgnored = false;
			for (TechnologyAdapter<?> ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				List<File> l = writtenFiles.get(ta);
				if (l != null && l.contains(file)) {
					fileIsStillToBeIgnored = true;
					break;
				}
			}
			if (!fileIsStillToBeIgnored) {
				// logger.info("End of file ignoring: " + file);
				willBeWrittenFiles.remove(file);
			}

		}

		protected boolean isToBeIgnored(File f) {
			return f.getName().endsWith("~") || f.getName().equals(".metadata");
		}

		@Override
		public boolean isIgnorable(File file, TechnologyAdapter<?> technologyAdapter) {

			if (isToBeIgnored(file)) {
				return true;
			}
			if (willBeWrittenFiles.contains(file)) {
				return true;
			}
			if (willBeRenamedFiles.contains(file)) {
				willBeRenamedFiles.remove(file);
				return true;
			}
			if (willBeRenamedAsFiles.contains(file)) {
				willBeRenamedAsFiles.remove(file);
				return true;
			}
			if (willBeDeletedFiles.contains(file)) {
				willBeDeletedFiles.remove(file);
				return true;
			}
			return false;
		}

		private HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> getRepositoriesForAdapter(
				TechnologyAdapter<?> technologyAdapter, boolean considerEmptyRepositories) {
			if (considerEmptyRepositories) {
				technologyAdapter.ensureAllRepositoriesAreCreated(this);
			}
			HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> map = repositories.get(technologyAdapter);
			if (map == null) {
				map = new HashMap<>();
				repositories.put(technologyAdapter, map);
			}
			return map;
		}

		@Override
		public final <R extends ResourceRepository<?, File>> R retrieveRepository(Class<? extends R> repositoryType,
				TechnologyAdapter<?> technologyAdapter) {
			HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> map = getRepositoriesForAdapter(
					technologyAdapter, false);

			return (R) map.get(repositoryType);
		}

		@Override
		public final <R extends ResourceRepository<?, File>> void registerRepository(R repository, Class<? extends R> repositoryType,
				TechnologyAdapter<?> technologyAdapter) {

			HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> map = getRepositoriesForAdapter(
					technologyAdapter, false);

			if (map.get(repositoryType) == null) {
				map.put(repositoryType, repository);
				getPropertyChangeSupport().firePropertyChange("getRegisteredRepositories(TechnologyAdapter)", null,
						getRegistedRepositories(technologyAdapter, false));
				// Call it to update the current repositories
				technologyAdapter.notifyRepositoryStructureChanged();
			}
			else {
				logger.warning("Repository already registered: " + repositoryType + " for " + repository);
			}
		}

		@Override
		public Collection<ResourceRepository<?, File>> getRegistedRepositories(TechnologyAdapter<?> technologyAdapter,
				boolean considerEmptyRepositories) {
			return getRepositoriesForAdapter(technologyAdapter, considerEmptyRepositories).values();
		}

		@Override
		public <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(String uri, Class<T> type) {
			// TODO: provide support for class and version
			FlexoResource<T> uniqueResource = retrieveResource(uri, null, null);
			return Collections.singletonList(uniqueResource);
		}

		@Override
		public <T extends ResourceData<T>> FlexoResource<T> retrieveResource(String uri, FlexoVersion version, Class<T> type) {
			// TODO: provide support for class and version
			return (FlexoResource<T>) retrieveResource(uri);
		}

		@Override
		public FlexoResource<?> retrieveResource(String uri) {
			return getResource(uri);
		}

		/**
		 * Stops the Resource Center (When needed)
		 */
		@Override
		public void stop() {
			this.stopDirectoryWatching();
		}

		/**
		 * Compute and return a default URI for supplied resource<br>
		 * If resource does not provide URI support, this might be delegated to the {@link FlexoResourceCenter} through this method
		 * 
		 * @param resource
		 * @return
		 */
		@Override
		public <R extends FlexoResource<?>> String getDefaultResourceURI(R resource) {
			String defaultBaseURI = getDefaultBaseURI();
			if (!defaultBaseURI.endsWith("/")) {
				defaultBaseURI = defaultBaseURI + "/";
			}
			String lastPath = resource.getName();
			String relativePath = "";

			if (resource.getIODelegate() != null) {
				File serializationArtefact = (File) resource.getIODelegate().getSerializationArtefact();
				if (resource.getIODelegate() instanceof DirectoryBasedIODelegate) {
					serializationArtefact = ((DirectoryBasedIODelegate) resource.getIODelegate()).getDirectory();
				}
				if (serializationArtefact != null) {
					File f = serializationArtefact.getParentFile();
					while (f != null && !(f.equals(getRootFolder().getSerializationArtefact()))) {
						relativePath = f.getName() + "/" + relativePath;
						f = f.getParentFile();
					}
				}
			}
			return defaultBaseURI + relativePath + lastPath;
		}

		/**
		 * Return File matching supplied configuration.<br>
		 * The parent folder is created
		 * 
		 * @param resourceName
		 * @param relativePath
		 * @param extension
		 * @return
		 */
		@Override
		public File retrieveResourceFile(String resourceName, String relativePath, String extension) {
			if (StringUtils.isEmpty(resourceName)) {
				return null;
			}
			String fileName;
			if (extension != null && !resourceName.endsWith(extension)) {
				if (!extension.startsWith(".")) {
					extension = "." + extension;
				}
				fileName = resourceName + extension;
			}
			else {
				fileName = resourceName;
			}
			if (relativePath != null) {
				if (!relativePath.endsWith(File.separator)) {
					fileName = relativePath + File.separator + fileName;
				}
				else {
					fileName = relativePath + fileName;
				}
			}
			File returned = new File(getDirectory(), fileName);
			returned.getParentFile().mkdirs();
			return returned;
		}

		@Override
		public String getDefaultBaseURI() {
			return fsMetaDataManager.getProperty(DEFAULT_BASE_URI, getDirectory().toURI().toString().replace(File.separator, "/"),
					getDirectory());
		}

		@Override
		public void setDefaultBaseURI(String defaultBaseURI) {
			fsMetaDataManager.setProperty(DEFAULT_BASE_URI, defaultBaseURI, getDirectory(), true);
		}

		@Override
		public String retrieveName(File serializationArtefact) {
			return serializationArtefact.getName();
		}

		@Override
		public File rename(File serializationArtefact, String newName) {
			if (serializationArtefact.exists() && newName != null && !newName.equals(retrieveName(serializationArtefact))) {
				File oldFile = serializationArtefact;
				File newFile = new File(oldFile.getParentFile(), newName);
				try {
					// System.out.println("Rename " + oldFile + " to " + newFile);
					// Thread.dumpStack();
					FileUtils.rename(oldFile, newFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return newFile;
			}
			return serializationArtefact;
		}

		/**
		 * Delete supplied serialization artefact<br>
		 * Return deleted artefact
		 * 
		 * @param serializationArtefact
		 * @return
		 */
		@Override
		public File delete(File serializationArtefact) {
			serializationArtefact.delete();
			return serializationArtefact;
		}

		/**
		 * Return serialization artefact containing supplied serialization artefact (parent directory)
		 * 
		 * @param serializationArtefact
		 * @return
		 */
		@Override
		public File getContainer(File serializationArtefact) {
			return serializationArtefact.getParentFile();
		}

		/**
		 * Return list of serialization artefacts contained in supplied serialization actifact<br>
		 * Return empty list if supplied serialization artefact has no contents
		 * 
		 * @param serializationArtefact
		 * @return
		 */
		@Override
		public List<File> getContents(File serializationArtefact) {
			File[] contents = serializationArtefact.listFiles();
			if (contents != null) {
				return Arrays.asList(contents);
			}
			return Collections.emptyList();
		}

		@Override
		public File getDirectoryWithRelativePath(String relativePath) {
			File serializationArtefact = getBaseArtefact();

			if (relativePath != null) {
				StringTokenizer st = new StringTokenizer(relativePath, "/\\");
				while (st.hasMoreElements()) {
					String pathName = st.nextToken();
					serializationArtefact = getDirectory(pathName, serializationArtefact);
					if (serializationArtefact == null) {
						serializationArtefact = createDirectory(pathName, serializationArtefact);
					}
				}
			}
			return serializationArtefact;
		}

		@Override
		public File createDirectory(String name, File parentDirectory) {
			File returned = new File(parentDirectory, name);
			getServiceManager().notify(null, new WillWriteFileOnDiskNotification(returned));
			returned.mkdirs();
			getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(returned));
			return returned;
		}

		/**
		 * Get container serialization artefact, with supplied name and parent serialization artefact
		 * 
		 * @param name
		 * @param parentDirectory
		 * @return
		 */
		@Override
		public File getDirectory(String name, File parentDirectory) {
			File returned = new File(parentDirectory, name);
			return returned;
		}

		/**
		 * Create simple serialization artefact, with supplied name and parent serialization artefact<br>
		 * Name can also be a relative path name (with '/' as path separator)
		 * 
		 * @param name
		 * @param parentDirectory
		 * @return
		 */
		@Override
		public File createEntry(String name, File parentDirectory) {
			parentDirectory.mkdirs();
			return new File(parentDirectory, name);
		}

		@Override
		public File getEntry(String name, File parentDirectory) {
			File returned = new File(parentDirectory, name);
			return returned;
		}

		@Override
		public boolean isDirectory(File serializationArtefact) {
			return serializationArtefact.isDirectory();
		}

		@Override
		public boolean exists(File serializationArtefact) {
			return serializationArtefact.exists();
		}

		@Override
		public boolean canRead(File serializationArtefact) {
			return serializationArtefact.canRead();
		}

		@Override
		public FileIODelegate makeFlexoIODelegate(File serializationArtefact, FlexoResourceFactory<?, ?> resourceFactory)
				throws IOException {
			return FileIODelegateImpl.makeFileFlexoIODelegate(serializationArtefact, resourceFactory);
		}

		@Override
		public FlexoIODelegate<File> makeDirectoryBasedFlexoIODelegate(File serializationArtefact, String directoryExtension,
				String fileExtension, FlexoResourceFactory<?, ?> resourceFactory) {
			String baseName = serializationArtefact.getName().substring(0,
					serializationArtefact.getName().length() - directoryExtension.length());
			File directory = new File(serializationArtefact.getParentFile(), baseName + directoryExtension);
			File file = new File(directory, baseName + fileExtension);
			return makeDirectoryBasedFlexoIODelegate(directory, file, resourceFactory);
		}

		@Override
		public FlexoIODelegate<File> makeDirectoryBasedFlexoIODelegate(File directory, File file,
				FlexoResourceFactory<?, ?> resourceFactory) {
			return DirectoryBasedIODelegateImpl.makeDirectoryBasedFlexoIODelegate(directory, file, resourceFactory);
		}

		@Override
		public XMLRootElementInfo getXMLRootElementInfo(File serializationArtefact) {
			return getXMLRootElementInfo(serializationArtefact, false, null);
		}

		@Override
		public XMLRootElementInfo getXMLRootElementInfo(File serializationArtefact, boolean parseFirstLevelElements,
				String firstLevelElementName) {
			if (!serializationArtefact.exists()) {
				// logger.warning("Could not extract XMLRootElementInfo from a non existant file: " + serializationArtefact);
				return null;
			}
			XMLRootElementReader reader = new XMLRootElementReader(parseFirstLevelElements, firstLevelElementName);
			try {
				return reader.readRootElement(serializationArtefact);
			} catch (IOException e) {
				logger.warning("Cannot parse document in File: " + serializationArtefact.getAbsolutePath());
				return null;
			}

		}

		/**
		 * Return properties stored in supplied directory<br>
		 * Find the first entry whose name ends with .properties and analyze it as a {@link Properties} serialization
		 * 
		 * @return
		 * @throws IOException
		 */
		@Override
		public Properties getProperties(File directory) throws IOException {
			Properties returned = null;
			if (directory != null && directory.isDirectory()) {
				// Read first <xxx>.properties file.
				File[] propertiesFiles = directory.listFiles(FileUtils.PropertiesFileNameFilter);
				if (propertiesFiles.length == 1) {
					try {
						returned = new Properties();
						try (FileReader fr = new FileReader(propertiesFiles[0])) {
							returned.load(fr);
						}
					} catch (FileNotFoundException e) {
						returned = null;
					}
				}
			}
			return returned;
		}

		@Override
		public <R extends FlexoResource<?>> RepositoryFolder<R, File> getRepositoryFolder(FlexoIODelegate<File> ioDelegate,
				ResourceRepository<R, File> resourceRepository) {

			File candidateFile = null;
			if (ioDelegate instanceof DirectoryBasedIODelegate) {
				candidateFile = ((DirectoryBasedIODelegate) ioDelegate).getDirectory();
			}
			else if (ioDelegate instanceof FileIODelegate) {
				candidateFile = ((FileIODelegate) ioDelegate).getFile();
			}
			if (getRootFolder().getSerializationArtefact().equals(candidateFile)) {
				return (RepositoryFolder<R, File>) getRootFolder();
			}
			try {
				RepositoryFolder<R, File> returned = resourceRepository.getParentRepositoryFolder(candidateFile, true);
				return returned;
			} catch (IOException e) {
				e.printStackTrace();
				return resourceRepository.getRootFolder();
			}

		}

		/**
		 * Get the set of path in the case of File
		 * 
		 * @param aFile
		 * @return
		 * @throws IOException
		 */
		@Override
		public List<String> getPathTo(File aFile) throws IOException {
			File rootFolder = getRootFolder().getSerializationArtefact().getCanonicalFile();
			if (FileUtils.directoryContainsFile(rootFolder, aFile.getCanonicalFile(), true)) {
				List<String> pathTo = new ArrayList<>();
				File f = aFile.getParentFile().getCanonicalFile();
				while (f != null && !f.equals(rootFolder)) {
					pathTo.add(0, f.getName());
					f = f.getParentFile();
				}
				return pathTo;
			}
			return null;
		}

		@Override
		public FileSystemMetaDataManager getMetaDataManager() {
			return fsMetaDataManager;
		}

		private FlexoProjectResource<File> delegatingProjectResource;

		/**
		 * Returns project which delegates it's FlexoResourceCenter to this<br>
		 * Returns null if this {@link FlexoResourceCenter} is not acting as a delegate for a {@link FlexoProject}
		 * 
		 * @return
		 */
		@Override
		public FlexoProjectResource<File> getDelegatingProjectResource() {
			return delegatingProjectResource;
		}

		/**
		 * Sets project which delegates it's FlexoResourceCenter to this<br>
		 * 
		 * @return
		 */
		@Override
		public void setDelegatingProjectResource(FlexoProjectResource<File> delegatingProjectResource) {
			this.delegatingProjectResource = delegatingProjectResource;
		}

		@Override
		public final String getDisplayableName() {
			if (getDelegatingProjectResource() != null) {
				return getDelegatingProjectResource().getName() + FlexoProjectResourceFactory.PROJECT_SUFFIX;
			}
			return getDefaultBaseURI();// getRootDirectory().getName();
		}

		@Override
		public boolean containsArtefact(File serializationArtefact) {
			return FileUtils.directoryContainsFile(getRootDirectory(), serializationArtefact, true);
		}

		@Override
		public String relativePath(File serializationArtefact) {
			try {
				return FileUtils.makeFilePathRelativeToDir(serializationArtefact, getDirectory());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public String getDisplayableStatus() {
			return "[uri=\"" + getDefaultBaseURI() + "\" dir=\"" + getRootDirectory().getAbsolutePath() + "\"] with "
					+ getAllResources().size() + " resources";
		}

	}

	@ModelEntity(isAbstract = true)
	@ImplementationClass(FSBasedResourceCenterEntry.FSBasedResourceCenterEntryImpl.class)
	@XMLElement(xmlTag = "AbstractFSBasedResourceCenterEntry")
	public static interface FSBasedResourceCenterEntry<RC extends FileSystemBasedResourceCenter> extends ResourceCenterEntry<RC> {
		@PropertyIdentifier(type = File.class)
		public static final String DIRECTORY_KEY = "directory";

		@Getter(DIRECTORY_KEY)
		@XMLAttribute
		public File getDirectory();

		@Setter(DIRECTORY_KEY)
		public void setDirectory(File aDirectory);

		@Implementation
		public static abstract class FSBasedResourceCenterEntryImpl<RC extends FileSystemBasedResourceCenter>
				implements FSBasedResourceCenterEntry<RC> {

			private boolean isSystem = false;

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof FSBasedResourceCenterEntry) {
					return getDirectory() != null && getDirectory().equals(((FSBasedResourceCenterEntry<?>) obj).getDirectory());
				}
				return false;
			}

			@Override
			public boolean isSystemEntry() {
				return isSystem;
			}

			@Override
			public void setIsSystemEntry(boolean isSystemEntry) {
				isSystem = isSystemEntry;
			}
		}

	}

}
