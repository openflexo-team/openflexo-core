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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPointRepository;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.DirectoryWatcher;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;

/**
 * An abstract implementation of a {@link FlexoResourceCenter} based on a file system.
 * 
 * It defines a {@link File} which is a directory containing all resources
 * 
 * @author sylvain
 * 
 */
public abstract class FileSystemBasedResourceCenter extends FileResourceRepository<FlexoResource<?>> implements FlexoResourceCenter<File> {

	protected static final Logger logger = Logger.getLogger(FileSystemBasedResourceCenter.class.getPackage().getName());

	private final File rootDirectory;

	private final FlexoResourceCenterService rcService;

	private final FileSystemMetaDataManager fsMetaDataManager = new FileSystemMetaDataManager();

	private final Map<TechnologyAdapter, HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>>> repositories = new HashMap<>();
	// private final Map<TechnologyAdapter, ResourceRepository<?>> globalRepositories = new HashMap<>();

	public FileSystemBasedResourceCenter(File rootDirectory, FlexoResourceCenterService rcService) {
		super(null, rootDirectory);
		this.rcService = rcService;
		this.rootDirectory = rootDirectory;
		startDirectoryWatching();
	}

	@Override
	public FlexoResourceCenter<?> getResourceCenter() {
		return this;
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * Return (first when many) resource matching supplied File
	 */
	@Override
	public <R extends FlexoResource<?>> R getResource(File aFile, Class<R> resourceClass) {
		if (!FileUtils.directoryContainsFile(getRootDirectory(), aFile, true)) {
			return null;
		}

		RepositoryFolder<?> folder = null;
		try {
			folder = getRepositoryFolder(aFile, false);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (folder == null) {
			return null;
		}

		for (FlexoResource<?> r : folder.getResources()) {
			if ((r.getFlexoIODelegate() instanceof FileFlexoIODelegate)
					&& ((FileFlexoIODelegate) r.getFlexoIODelegate()).getFile().equals(aFile)) {
				if (resourceClass.isAssignableFrom(r.getClass())) {
					return (R) r;
				}
				logger.warning("Found resource matching file " + aFile + " but not of desired type: " + r.getClass() + " instead of "
						+ resourceClass);
				return null;
			}
		}

		// Cannot find the resource
		return null;

	}

	@Override
	public String toString() {
		return super.toString() + " directory=" + (getRootDirectory() != null ? getRootDirectory().getAbsolutePath() : null);
	}

	public FlexoResourceCenterService getFlexoResourceCenterService() {
		return rcService;
	}

	@Override
	public ViewPointRepository getViewPointRepository() {
		if (rcService != null) {
			FMLTechnologyAdapter vmTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			return getRepository(ViewPointRepository.class, vmTA);
		}
		return null;
	}

	/*@Override
	public void initialize(ViewPointLibrary viewPointLibrary) {
		logger.info("Initializing ViewPointLibrary for " + this);
		viewPointRepository = new ViewPointRepository(this, viewPointLibrary);
		exploreDirectoryLookingForViewPoints(getRootDirectory(), viewPointLibrary);
	}*/

	/**
	 * Retrieve (creates it when not existant) folder containing supplied file
	 * 
	 * @param repository
	 * @param aFile
	 * @return
	 */
	protected <R extends FlexoResource<?>> RepositoryFolder<R> retrieveRepositoryFolder(ResourceRepository<R> repository, File aFile) {
		try {
			return repository.getRepositoryFolder(aFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return repository.getRootFolder();
		}
	}

	/**
	 * 
	 * @param directory
	 * @param folder
	 * @param viewPointLibrary
	 * @return a flag indicating if some ViewPoints were found
	 */
	/*private boolean exploreDirectoryLookingForViewPoints(File directory, ViewPointLibrary viewPointLibrary) {
		boolean returned = false;
		logger.fine("Exploring " + directory);
		if (directory.exists() && directory.isDirectory() && directory.canRead()) {
			for (File f : directory.listFiles()) {
				ViewPointResource vpRes = analyseAsViewPoint(f);
				if (f.isDirectory() && !f.getName().equals("CVS")) {
					if (exploreDirectoryLookingForViewPoints(f, viewPointLibrary)) {
						returned = true;
					}
				}
			}
		}
		return returned;
	}*/

	/**
	 * 
	 * @param directory
	 * @param folder
	 * @param viewPointLibrary
	 * @return a flag indicating if some ViewPoints were found
	 */
	/*private ViewPointResource analyseAsViewPoint(File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead()
				&& candidateFile.getName().endsWith(".viewpoint")) {
			ViewPointResource vpRes = ViewPointResourceImpl.retrieveViewPointResource(candidateFile,
					viewPointRepository.getViewPointLibrary());
			if (vpRes != null) {
				logger.info("Found and register viewpoint " + vpRes.getURI()
						+ (vpRes instanceof FlexoFileResource ? " file=" + ((FlexoFileResource) vpRes).getFile().getAbsolutePath() : ""));
				RepositoryFolder<ViewPointResource> folder = retrieveRepositoryFolder(viewPointRepository, candidateFile);
				viewPointRepository.registerResource(vpRes, folder);
				// Also register the resource in the ResourceCenter seen as a ResourceRepository
				try {
					registerResource(vpRes, getRepositoryFolder(candidateFile, true));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return vpRes;
			} else {
				logger.warning("While exploring resource center looking for viewpoints : cannot retrieve resource for file "
						+ candidateFile.getAbsolutePath());
			}
		}
	
		return null;
	}*/

	@Override
	public void activateTechnology(TechnologyAdapter technologyAdapter) {

		logger.info("Activating resource center " + this + " with adapter " + technologyAdapter.getName());
		Progress.progress(getLocales().localizedForKey("initializing_adapter") + " " + technologyAdapter.getName());
		technologyAdapter.initializeResourceCenter(this);
	}

	/**
	 * Finalize the FlexoResourceCenter<br>
	 * 
	 * @param technologyAdapterService
	 */
	@Override
	public void disactivateTechnology(TechnologyAdapter technologyAdapter) {

		logger.info("Disactivating resource center " + this + " with adapter " + technologyAdapter.getName());
		// TODO
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		if (getFlexoResourceCenterService() == null) {
			return super.getServiceManager();
		}
		return getFlexoResourceCenterService().getServiceManager();
	}

	@Override
	public Iterator<File> iterator() {
		List<File> allFiles = new ArrayList<File>();
		if (getRootDirectory() != null) {
			appendFiles(getRootDirectory(), allFiles);
		}
		else {
			logger.warning("ResourceCenter: " + this + " rootDirectory is null");
		}
		return allFiles.iterator();
	}

	private void appendFiles(File directory, List<File> files) {
		if (directory.exists() && directory.isDirectory() && directory.canRead()) {
			for (File f : directory.listFiles()) {
				if (!isIgnorable(f)) {
					files.add(f);
					if (f.isDirectory()) {
						appendFiles(f, files);
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		/*if (getRootDirectory() != null) {
			return getDefaultBaseURI() + " [" + getRootDirectory().getAbsolutePath() + "]";
		}*/
		return getDefaultBaseURI();
	}

	private DirectoryWatcher directoryWatcher;

	private ScheduledFuture<?> scheduleWithFixedDelay;

	public DirectoryWatcher getDirectoryWatcher() {
		return directoryWatcher;
	}

	/**
	 * Synchronous call to examine directory for structure modifications<br>
	 * Return when the exploring task is finished
	 */
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

	public class FileSystemBasedDirectoryWatcher extends DirectoryWatcher {
		public FileSystemBasedDirectoryWatcher(File directory) {
			super(directory);
		}

		@Override
		protected void fileModified(File file) {
			FileSystemBasedResourceCenter.this.fileModified(file);
		}

		@Override
		protected void fileAdded(File file) {
			FileSystemBasedResourceCenter.this.fileAdded(file);
		}

		@Override
		protected void fileDeleted(File file) {
			FileSystemBasedResourceCenter.this.fileDeleted(file);
		}

		@Override
		protected void fileRenamed(File oldFile, File renamedFile) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void performRun() {
			if (DEBUG) {
				System.out.println("BEGIN performRun() for " + FileSystemBasedDirectoryWatcher.this);
			}

			super.performRun();

			if (DEBUG) {
				System.out.println("Looking for files to be notified " + FileSystemBasedDirectoryWatcher.this);
			}

			fireAddedFilesToBeNotified();
			fireDeletedFilesToBeNotified();
			// System.out.println("Done for " + FileSystemBasedDirectoryWatcher.this);
			isRunning = false;

			if (DEBUG) {
				System.out.println("END performRun() for " + FileSystemBasedDirectoryWatcher.this);
			}

		}

	}

	/**
	 * Start asynchronous call to examine directory for structure modifications<br>
	 * This start a timer that will be triggered every second
	 */
	public void startDirectoryWatching() {
		if (getRootDirectory() != null && getRootDirectory().exists()) {
			if (directoryWatcher == null) {
				directoryWatcher = new FileSystemBasedDirectoryWatcher(getRootDirectory());
			}
			ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
			scheduleWithFixedDelay = newScheduledThreadPool.scheduleWithFixedDelay(directoryWatcher, 0, 1, TimeUnit.SECONDS);
			// System.out.println("startDirectoryWatching() for " + rootDirectory);
		}
	}

	public void stopDirectoryWatching() {
		if (getRootDirectory() != null && getRootDirectory().exists() && scheduleWithFixedDelay != null) {
			scheduleWithFixedDelay.cancel(true);
		}
	}

	protected void fileModified(File file) {
		if (!isIgnorable(file)) {
			System.out.println("File MODIFIED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
		}
	}

	private final Map<TechnologyAdapter, List<File>> addedFilesToBeRenotified = new HashMap<>();
	private final Map<TechnologyAdapter, List<File>> removedFilesToBeRenotified = new HashMap<>();
	private final Map<TechnologyAdapter, List<File>> modifiedFilesToBeRenotified = new HashMap<>();
	private final Map<TechnologyAdapter, Map<File, File>> renamedFilesToBeRenotified = new HashMap<>();

	/**
	 * Notify that a new File has been discovered in directory representing this ResourceCenter
	 * 
	 * @param file
	 * @return a boolean indicating if this file has been handled by a least one technology
	 */
	protected void fileAdded(File file) {
		if (!isIgnorable(file)) {
			System.out.println("File ADDED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					List<File> filesToBeNotified = addedFilesToBeRenotified.get(adapter);
					if (filesToBeNotified == null) {
						filesToBeNotified = new ArrayList<File>();
						addedFilesToBeRenotified.put(adapter, filesToBeNotified);
					}
					if (adapter.isActivated()) {
						if (!adapter.contentsAdded(this, file)) {
							filesToBeNotified.add(file);
						}
					}
				}

			}
			System.out.println("Done: File ADDED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
		}
	}

	protected void fireAddedFilesToBeNotified() {
		// System.out.println("fireAddedFilesToBeNotified()");
		if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
			for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (adapter.isActivated()) {
					List<File> filesToBeNotified = addedFilesToBeRenotified.get(adapter);
					if (filesToBeNotified != null && filesToBeNotified.size() > 0) {
						for (File f : new ArrayList<File>(filesToBeNotified)) {
							logger.info("fileAdded (discovered later)" + f + " with adapter " + adapter.getName() + " : " + f);
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

	protected void fileDeleted(File file) {
		if (!isIgnorable(file)) {
			System.out.println("File DELETED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			if (getServiceManager() != null) {
				for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					List<File> filesToBeNotified = removedFilesToBeRenotified.get(adapter);
					if (filesToBeNotified == null) {
						filesToBeNotified = new ArrayList<File>();
						removedFilesToBeRenotified.put(adapter, filesToBeNotified);
					}
					if (adapter.isActivated()) {
						logger.info("fileDeleted " + file + " with adapter " + adapter.getName());
						if (adapter.contentsDeleted(this, file)) {
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

	protected void fireDeletedFilesToBeNotified() {
		// System.out.println("fireDeletedFilesToBeNotified()");
		if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
			for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (adapter.isActivated()) {
					List<File> filesToBeNotified = removedFilesToBeRenotified.get(adapter);
					if (filesToBeNotified != null && filesToBeNotified.size() > 0) {
						for (File f : new ArrayList<File>(filesToBeNotified)) {
							if (adapter.contentsDeleted(this, f)) {
								filesToBeNotified.remove(f);
								logger.info("fileDeleted (discovered later)" + f + " with adapter " + adapter.getName() + " : " + f);
							}
							logger.info("fileDeleted but ignored for adapter " + adapter.getName() + " : " + f);
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
		if (!isIgnorable(renamedFile)) {
			System.out.println("File RENAMED from  " + oldFile.getName() + " to " + renamedFile.getName() + " in "
					+ renamedFile.getParentFile().getAbsolutePath());
			/*if (technologyAdapterService != null) {
				for (TechnologyAdapter adapter : technologyAdapterService.getTechnologyAdapters()) {
					logger.info("fileDeleted " + file + " with adapter " + adapter.getName());
					adapter.contentsDeleted(this, file);
				}
			}*/
		}
	}

	private final List<File> willBeWrittenFiles = new ArrayList<File>();
	private final List<File> willBeRenamedFiles = new ArrayList<File>();
	private final List<File> willBeRenamedAsFiles = new ArrayList<File>();
	private final List<File> willBeDeletedFiles = new ArrayList<File>();

	public void willWrite(File file) {
		willBeWrittenFiles.add(file);
	}

	public void willRename(File fromFile, File toFile) {
		willBeRenamedFiles.add(fromFile);
		willBeRenamedAsFiles.add(toFile);
	}

	public void willDelete(File file) {
		willBeDeletedFiles.add(file);
	}

	@Override
	public boolean isIgnorable(File file) {
		if (willBeWrittenFiles.contains(file)) {
			willBeWrittenFiles.remove(file);
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

	private HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>> getRepositoriesForAdapter(
			TechnologyAdapter technologyAdapter) {
		HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>> map = repositories.get(technologyAdapter);
		if (map == null) {
			map = new HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>>();
			repositories.put(technologyAdapter, map);
		}
		return map;
	}

	@Override
	public final <R extends ResourceRepository<?>> R getRepository(Class<? extends R> repositoryType, TechnologyAdapter technologyAdapter) {
		HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>> map = getRepositoriesForAdapter(technologyAdapter);
		return (R) map.get(repositoryType);
	}

	/**
	 * Register global repository for this resource center<br>
	 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may interpret<br>
	 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center for
	 * a given technology
	 * 
	 * @param repository
	 * @param technologyAdapter
	 */
	/*@Override
	public final void registerGlobalRepository(ResourceRepository<?> repository, TechnologyAdapter technologyAdapter) {
		if (repository != null && technologyAdapter != null) {
			globalRepositories.put(technologyAdapter, repository);
		}
	}*/

	/**
	 * Return the global repository for this resource center and for supplied technology adapter<br>
	 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may interpret<br>
	 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center for
	 * a given technology
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	/*@Override
	public ResourceRepository<?> getGlobalRepository(TechnologyAdapter technologyAdapter) {
		if (technologyAdapter != null) {
			return globalRepositories.get(technologyAdapter);
		}
		return null;
	}*/

	@Override
	public final <R extends ResourceRepository<?>> void registerRepository(R repository, Class<? extends R> repositoryType,
			TechnologyAdapter technologyAdapter) {
		HashMap<Class<? extends ResourceRepository<?>>, ResourceRepository<?>> map = getRepositoriesForAdapter(technologyAdapter);
		if (map.get(repositoryType) == null) {
			map.put(repositoryType, repository);
			getPropertyChangeSupport().firePropertyChange("getRegisteredRepositories(TechnologyAdapter)", null,
					getRegistedRepositories(technologyAdapter));
			// Call it to update the current repositories
			technologyAdapter.notifyRepositoryStructureChanged();
		}
		else {
			logger.warning("Repository already registered: " + repositoryType + " for " + repository);
		}
	}

	@Override
	public Collection<ResourceRepository<?>> getRegistedRepositories(TechnologyAdapter technologyAdapter) {
		return getRepositoriesForAdapter(technologyAdapter).values();
	}

	@Override
	public <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(String uri, Class<T> type, IProgress progress) {
		// TODO: provide support for class and version
		FlexoResource<T> uniqueResource = retrieveResource(uri, null, null, progress);
		return Collections.singletonList(uniqueResource);
	}

	@Override
	public <T extends ResourceData<T>> FlexoResource<T> retrieveResource(String uri, FlexoVersion version, Class<T> type,
			IProgress progress) {
		// TODO: provide support for class and version
		return (FlexoResource<T>) retrieveResource(uri, progress);
	}

	@Override
	public FlexoResource<?> retrieveResource(String uri, IProgress progress) {
		return getResource(uri);
	}

	private FSBasedResourceCenterEntry entry;

	@Override
	public ResourceCenterEntry<?> getResourceCenterEntry() {
		if (entry == null) {
			try {
				ModelFactory factory = new ModelFactory(FSBasedResourceCenterEntry.class);
				entry = factory.newInstance(FSBasedResourceCenterEntry.class);
				entry.setDirectory(getDirectory());
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return entry;
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
	public String getDefaultResourceURI(FlexoResource<?> resource) {
		if (resource instanceof TechnologyAdapterResource) {
			TechnologyAdapter ta = ((TechnologyAdapterResource<?, ?>) resource).getTechnologyAdapter();
			for (ResourceRepository repository : getRegistedRepositories(ta)) {
				if (repository.containsResource(resource)) {
					String path = "";
					RepositoryFolder f = repository.getRepositoryFolder(resource);
					while (f != null && !f.isRootFolder()) {
						path = f.getName() + File.separator + path;
						f = f.getParentFolder();
					}
					String defaultBaseURI = getDefaultBaseURI();
					if (defaultBaseURI.endsWith(File.separator) || defaultBaseURI.endsWith("/")) {
						return getDefaultBaseURI() + path.replace(File.separator, "/") + resource.getName();
					}
					else {
						return getDefaultBaseURI() + "/" + path.replace(File.separator, "/") + resource.getName();
					}
				}
			}
		}
		return null;
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
		fsMetaDataManager.setProperty(DEFAULT_BASE_URI, defaultBaseURI, getDirectory());
	}

	@Override
	public String retrieveName(File serializationArtefact) {
		return serializationArtefact.getName();
	}

	@Override
	public FileFlexoIODelegate makeFlexoIODelegate(File serializationArtefact, FlexoResourceFactory<?, ?, ?> resourceFactory) {
		return FileFlexoIODelegateImpl.makeFileFlexoIODelegate(serializationArtefact, resourceFactory);
	}

	@ModelEntity
	@ImplementationClass(FSBasedResourceCenterEntry.FSBasedResourceCenterEntryImpl.class)
	@XMLElement
	public static interface FSBasedResourceCenterEntry extends ResourceCenterEntry<DirectoryResourceCenter> {
		@PropertyIdentifier(type = File.class)
		public static final String DIRECTORY_KEY = "directory";

		@Getter(DIRECTORY_KEY)
		@XMLAttribute
		public File getDirectory();

		@Setter(DIRECTORY_KEY)
		public void setDirectory(File aDirectory);

		@Implementation
		public static abstract class FSBasedResourceCenterEntryImpl implements FSBasedResourceCenterEntry {

			private boolean isSystem = false;

			@Override
			public DirectoryResourceCenter makeResourceCenter(FlexoResourceCenterService rcService) {
				return DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(getDirectory(), rcService);
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof FSBasedResourceCenterEntry) {
					return getDirectory() != null && getDirectory().equals(((FSBasedResourceCenterEntry) obj).getDirectory());
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
