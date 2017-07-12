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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.converter.FlexoObjectReferenceConverter;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModelRepository;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate.DirectoryBasedIODelegateImpl;
import org.openflexo.foundation.resource.FileIODelegate.FileIODelegateImpl;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
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
public abstract class FileSystemBasedResourceCenter extends ResourceRepository<FlexoResource<?>, File>
		implements FlexoResourceCenter<File> {

	protected static final Logger logger = Logger.getLogger(FileSystemBasedResourceCenter.class.getPackage().getName());

	// Delay of DirectoryWatcher, in seconds
	public static long DIRECTORY_WATCHER_DELAY = 3;

	private final File rootDirectory;

	private final FlexoResourceCenterService rcService;

	private final FileSystemMetaDataManager fsMetaDataManager = new FileSystemMetaDataManager();

	private final Map<TechnologyAdapter, HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>>> repositories = new HashMap<>();

	public FileSystemBasedResourceCenter(File rootDirectory, FlexoResourceCenterService rcService) {
		super(null, rootDirectory);
		// setBaseArtefact(rootDirectory);
		this.rcService = rcService;
		this.rootDirectory = rootDirectory;
		startDirectoryWatching();
	}

	@Override
	public FlexoResourceCenter<File> getResourceCenter() {
		return this;
	}

	public File getDirectory() {
		return getRootDirectory();
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * Return (first when many) resource matching supplied File
	 */
	@Override
	public <R extends FlexoResource<?>> R getResource(File resourceArtifact, Class<R> resourceClass) {
		if (!FileUtils.directoryContainsFile(getRootDirectory(), resourceArtifact, true)) {
			return null;
		}

		try {
			// searches for parent folder.
			RepositoryFolder<?, File> folder = getParentRepositoryFolder(resourceArtifact, false);
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
			}

			// Cannot find the resource
			return null;

		} catch (IOException e) {
			logger.log(Level.WARNING, "Error while getting parent folder for " + resourceArtifact, e);
			return null;
		}
	}

	@Override
	public String toString() {
		return super.toString() + " directory=" + (getRootDirectory() != null ? getRootDirectory().getAbsolutePath() : null);
	}

	public FlexoResourceCenterService getFlexoResourceCenterService() {
		return rcService;
	}

	@Override
	public VirtualModelRepository<File> getViewPointRepository() {
		if (rcService != null) {
			FMLTechnologyAdapter vmTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			// return getRepository(VirtualModelRepository.class, vmTA);
			return vmTA.getVirtualModelRepository(this);
		}
		return null;
	}

	/*@Override
	public void initialize(VirtualModelLibrary viewPointLibrary) {
		logger.info("Initializing VirtualModelLibrary for " + this);
		viewPointRepository = new VirtualModelRepository(this, viewPointLibrary);
		exploreDirectoryLookingForViewPoints(getRootDirectory(), viewPointLibrary);
	}*/

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
				if (!isIgnorable(f, null)) {
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
			scheduleWithFixedDelay = newScheduledThreadPool.scheduleWithFixedDelay(directoryWatcher, 0, DIRECTORY_WATCHER_DELAY,
					TimeUnit.SECONDS);
			// System.out.println("startDirectoryWatching() for " + rootDirectory);
		}
	}

	public void stopDirectoryWatching() {
		if (getRootDirectory() != null && getRootDirectory().exists() && scheduleWithFixedDelay != null) {
			scheduleWithFixedDelay.cancel(true);
		}
	}

	protected void fileModified(File file) {
		if (!isIgnorable(file, null)) {
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
		System.out.println(
				"File ADDED in resource center " + this + " : " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
		if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
			for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (!isIgnorable(file, adapter)) {
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
				dismissIgnoredFilesWhenRequired(file, adapter);
			}

		}
		System.out.println("Done: File ADDED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
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
		System.out.println("File DELETED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
		if (getServiceManager() != null) {
			for (TechnologyAdapter adapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (!isIgnorable(file, adapter)) {
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
		if (!isIgnorable(renamedFile, null)) {
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

	private final List<File> willBeWrittenFiles = new ArrayList<>();
	private final Map<TechnologyAdapter, List<File>> writtenFiles = new HashMap<>();
	private final List<File> willBeRenamedFiles = new ArrayList<>();
	private final List<File> willBeRenamedAsFiles = new ArrayList<>();
	private final List<File> willBeDeletedFiles = new ArrayList<>();

	public void willWrite(File file) {
		if (!willBeWrittenFiles.contains(file)) {
			willBeWrittenFiles.add(file);
		}
	}

	public void hasBeenWritten(File file) {
		for (TechnologyAdapter ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
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

	public void willRename(File fromFile, File toFile) {
		willBeRenamedFiles.add(fromFile);
		willBeRenamedAsFiles.add(toFile);
	}

	public void willDelete(File file) {
		willBeDeletedFiles.add(file);
	}

	private void dismissIgnoredFilesWhenRequired(File file, TechnologyAdapter technologyAdapter) {
		if (technologyAdapter == null) {
			return;
		}
		List<File> filesBeeingWritten = writtenFiles.get(technologyAdapter);
		if (filesBeeingWritten != null && filesBeeingWritten.contains(file)) {
			filesBeeingWritten.remove(file);
		}
		boolean fileIsStillToBeIgnored = false;
		for (TechnologyAdapter ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
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
		return f.getName().endsWith("~");
	}

	@Override
	public boolean isIgnorable(File file, TechnologyAdapter technologyAdapter) {
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
			TechnologyAdapter technologyAdapter, boolean considerEmptyRepositories) {
		if (considerEmptyRepositories) {
			technologyAdapter.ensureAllRepositoriesAreCreated(this);
		}
		HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> map = repositories.get(technologyAdapter);
		if (map == null) {
			map = new HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>>();
			repositories.put(technologyAdapter, map);
		}
		return map;
	}

	@Override
	public final <R extends ResourceRepository<?, File>> R retrieveRepository(Class<? extends R> repositoryType,
			TechnologyAdapter technologyAdapter) {
		HashMap<Class<? extends ResourceRepository<?, File>>, ResourceRepository<?, File>> map = getRepositoriesForAdapter(
				technologyAdapter, false);

		return (R) map.get(repositoryType);
	}

	@Override
	public final <R extends ResourceRepository<?, File>> void registerRepository(R repository, Class<? extends R> repositoryType,
			TechnologyAdapter technologyAdapter) {

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
	public Collection<ResourceRepository<?, File>> getRegistedRepositories(TechnologyAdapter technologyAdapter,
			boolean considerEmptyRepositories) {
		return getRepositoriesForAdapter(technologyAdapter, considerEmptyRepositories).values();
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
	public <R extends FlexoResource<?>> String getDefaultResourceURI(R resource) {
		String defaultBaseURI = getDefaultBaseURI();
		if (!defaultBaseURI.endsWith("/")) {
			defaultBaseURI = defaultBaseURI + "/";
		}
		String lastPath = resource.getName();
		String relativePath = "";

		if (resource.getIODelegate() != null) {
			File serializationArtefact = (File) resource.getIODelegate().getSerializationArtefact();
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

	/**
	 * access to ObjectReference Converter used to translate strings to ObjectReference
	 */

	protected FlexoObjectReferenceConverter objectReferenceConverter = new FlexoObjectReferenceConverter(this);

	@Override
	public FlexoObjectReferenceConverter getObjectReferenceConverter() {
		return objectReferenceConverter;
	}

	@Override
	public void setObjectReferenceConverter(FlexoObjectReferenceConverter objectReferenceConverter) {
		this.objectReferenceConverter = objectReferenceConverter;
	}

	@Override
	public void notifyObjectLoaded(FlexoObjectReference<?> reference) {
		// logger.warning("TODO: implement this");
	}

	@Override
	public void objectCantBeFound(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	@Override
	public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {
		setChanged();
	}

	@Override
	public void objectDeleted(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
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
		else {
			return serializationArtefact;
		}

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
	public File createDirectory(String name, File parentDirectory) {
		File returned = new File(parentDirectory, name);
		returned.mkdirs();
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
	public FileIODelegate makeFlexoIODelegate(File serializationArtefact, FlexoResourceFactory<?, ?, ?> resourceFactory)
			throws IOException {
		return FileIODelegateImpl.makeFileFlexoIODelegate(serializationArtefact, resourceFactory);
	}

	@Override
	public FlexoIODelegate<File> makeDirectoryBasedFlexoIODelegate(File serializationArtefact, String directoryExtension,
			String fileExtension, FlexoResourceFactory<?, ?, ?> resourceFactory) {
		String baseName = serializationArtefact.getName().substring(0,
				serializationArtefact.getName().length() - directoryExtension.length());
		return DirectoryBasedIODelegateImpl.makeDirectoryBasedFlexoIODelegate(serializationArtefact.getParentFile(), baseName,
				directoryExtension, fileExtension, resourceFactory);
	}

	@Override
	public XMLRootElementInfo getXMLRootElementInfo(File serializationArtefact) {
		if (!serializationArtefact.exists()) {
			// logger.warning("Could not extract XMLRootElementInfo from a non existant file: " + serializationArtefact);
			return null;
		}
		XMLRootElementReader reader = new XMLRootElementReader();
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
					returned.load(new FileReader(propertiesFiles[0]));
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
		try {
			RepositoryFolder<R, File> returned = resourceRepository.getParentRepositoryFolder(candidateFile, true);
			/*if (!returned.getSerializationArtefact().equals(candidateFile.getParentFile())
					&& !returned.getSerializationArtefact().getAbsolutePath().contains("target")) {
				System.out.println("N'importe quoi, on met " + candidateFile + " dans " + returned.getSerializationArtefact());
				System.out.println("Root=" + resourceRepository.getRootFolder().getSerializationArtefact());
				List<String> pathTo = getPathTo(candidateFile);
				System.out.println("pathTo=" + pathTo);
				System.exit(-1);
			}*/
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
		if (FileUtils.directoryContainsFile(getRootFolder().getSerializationArtefact(), aFile, true)) {
			List<String> pathTo = new ArrayList<String>();
			File f = aFile.getParentFile().getCanonicalFile();
			while (f != null && !f.equals(getRootFolder().getSerializationArtefact().getCanonicalFile())) {
				pathTo.add(0, f.getName());
				f = f.getParentFile();
			}
			return pathTo;
		}
		else {
			return null;
		}
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
