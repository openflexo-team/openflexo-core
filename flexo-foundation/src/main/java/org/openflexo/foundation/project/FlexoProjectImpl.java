/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.project;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import javax.naming.InvalidNameException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectWrapper;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DuplicateExternalRepositoryNameException;
import org.openflexo.foundation.resource.ExternalRepositorySet;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ImportedProjectLoaded;
import org.openflexo.foundation.resource.ProjectExternalRepository;
import org.openflexo.foundation.resource.ProjectImportLoopException;
import org.openflexo.foundation.resource.ResourceRepositoryImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectIDManager;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.foundation.validation.FlexoProjectValidationModel;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.CompoundIssue;
import org.openflexo.model.validation.InformationIssue;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.DirectoryWatcher;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FileUtils.CopyStrategy;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.toolbox.ZipUtils;

/**
 * Base implementation for a {@link FlexoProject}
 * 
 * @author sylvain
 * @param <I>
 *            type of serialization artefact this project stores
 */
public class FlexoProjectImpl<I> extends ResourceRepositoryImpl<FlexoResource<?>, I> implements FlexoProject<I> {

	protected static final Logger logger = Logger.getLogger(FlexoProjectImpl.class.getPackage().getName());

	/**
	 * This is the generic API to create a new FlexoProject
	 * 
	 * @param aProjectDirectory
	 * @param editorFactory
	 * @param serviceManager
	 * @param progress
	 * @return
	 * @throws ProjectInitializerException
	 */
	/*public static FlexoEditor newProject(File aProjectDirectory, FlexoEditorFactory editorFactory, FlexoServiceManager serviceManager,
			FlexoProgress progress) throws ProjectInitializerException {
	
		// We should have already asked the user if the new project has to override the old one
		// When true, old directory was renamed to backup file
		// So, this is not normal to have here an existing file
		if (aProjectDirectory.exists()) {
			throw new ProjectInitializerException("This directory already exists: " + aProjectDirectory, aProjectDirectory);
		}
	
		FlexoProjectImpl project = new FlexoProjectImpl(aProjectDirectory, serviceManager);
		project.setServiceManager(serviceManager);
		FlexoEditor editor = editorFactory.makeFlexoEditor(project, serviceManager);
		project.setLastUniqueID(0);
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Building project: " + aProjectDirectory.getAbsolutePath());
		}
	
		try {
			project.setCreationUserId(FlexoObjectImpl.getCurrentUserIdentifier());
			project.setCreationDate(new Date());
			// TODO : Code to be removed, no more java Generation
			// project.initJavaFormatter();
	
			try {
				// This needs to be called to ensure the consistency of the project
				project.setGenerateSnapshot(false);
				project.save(progress);
				project.setGenerateSnapshot(true);
			} catch (SaveResourceException e) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe("Could not save all resources for project: " + project.getProjectName() + " located in "
							+ project.getProjectDirectory().getAbsolutePath());
				}
			}
		} catch (Exception e) {
			// Warns about the exception
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
			}
			e.printStackTrace();
		}
	
		// We add the newly created project as a ResourceCenter
		// Maybe this will be done now, but it may also be done in a task
		// In this case, we have to reference the task to wait for its execution
		FlexoTask addResourceCenterTask = serviceManager.resourceCenterAdded(project);
		if (addResourceCenterTask != null) {
			serviceManager.getTaskManager().waitTask(addResourceCenterTask);
		}
	
		return editor;
	}*/

	// TODO: add ProjectLoadingHandler to the parameters
	/*public static FlexoEditor openProject(File aProjectDirectory, FlexoEditorFactory editorFactory, FlexoServiceManager serviceManager,
			FlexoProgress progress) throws ProjectInitializerException, ProjectLoadingCancelledException {
	
		Progress.progress(getLocales(serviceManager).localizedForKey("opening_project") + aProjectDirectory.getAbsolutePath());
	
		if (!aProjectDirectory.exists()) {
			throw new ProjectInitializerException("This directory does not exists: " + aProjectDirectory, aProjectDirectory);
		}
	
		FlexoProjectImpl project = new FlexoProjectImpl(aProjectDirectory, serviceManager);
		project.setServiceManager(serviceManager);
		FlexoEditor editor = editorFactory.makeFlexoEditor(project, serviceManager);
		project.setLastUniqueID(0);
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Loading project: " + aProjectDirectory.getAbsolutePath());
		}
	
		try {
		} catch (Exception e) {
			// Warns about the exception
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
			}
			e.printStackTrace();
		}
	
		// We add the newly created project as a ResourceCenter
		FlexoTask addResourceCenterTask = serviceManager.resourceCenterAdded(project);
	
		// If resource center adding is executing in a task, we have to wait the task to be finished
		if (addResourceCenterTask != null) {
			serviceManager.getTaskManager().waitTask(addResourceCenterTask);
		}
	
		return editor;
	}*/

	public static final String BASE_PROJECT_URI = "http://www.openflexo.org/projects";
	public static final String RESOURCES = "resources";

	public static final String PROJECT_DIRECTORY = "projectDirectory";
	public static final String PROJECT_DATA = "projectData";
	public static final String REVISION = "revision";
	public static final String VERSION = "version";
	public static final String PROJECT_URI = "projectURI";

	// public static final String VIEWPOINT_EXPECTED_DIRECTORY = "Viewpoints";

	// private FlexoServiceManager serviceManager;

	private FlexoObjectIDManager objectIDManager;

	// private ViewLibrary viewLibrary = null;

	private final List<FlexoObjectReference<?>> objectReferences = new ArrayList<>();

	private boolean lastUniqueIDHasBeenSet = false;
	private long lastID = Integer.MIN_VALUE;

	// protected String projectName;

	protected I projectDirectory;
	// private List<I> filesToDelete;

	// private String projectDescription = "";

	private final long firstOperationFlexoID = -1;

	// private FlexoVersion version = new FlexoVersion("1.0");

	// private long revision = 0;

	private FlexoProjectValidationModel projectValidationModel;

	private static int ID = 0;

	private final int id;

	// private Date creationDate;
	// private String creationUserId;
	// private String projectURI;
	// private String projectVersionURI;

	private boolean holdObjectRegistration = false;

	private ProjectLoadingHandler loadingHandler;

	private FlexoProjectResource resource;

	public int getID() {
		return id;
	}

	private List<FlexoEditor> editors;

	private FlexoProjectReferenceLoader projectReferenceLoader;

	private final List<ProjectExternalRepository> _externalRepositories;
	private final Map<String, ProjectExternalRepository> repositoriesCache;

	public static interface FlexoProjectReferenceLoader extends FlexoService {

		/**
		 * 
		 * @param reference
		 *            the referense to load
		 * @param silentlyOnly
		 *            if true, the loading should be silent. This flag is typically meant for interactive loaders.
		 * @return
		 */
		public FlexoProjectImpl loadProject(FlexoProjectReference reference, boolean silentlyOnly);

	}

	protected FlexoProjectImpl(/*File aProjectDirectory, FlexoServiceManager serviceManager*/) {
		super(null, null/*aProjectDirectory, serviceManager.getResourceCenterService()*/);
		// this.serviceManager = serviceManager;

		editors = new Vector<>();
		synchronized (FlexoProjectImpl.class) {
			id = ID++;
		}
		logger.info("Create new project, ID=" + id);
		_externalRepositories = new ArrayList<>();
		repositoriesCache = new Hashtable<>();
		// filesToDelete = new Vector<>();

	}

	@Override
	public String getProjectName() {
		return getResource().getName();
	}

	@Override
	public String getName() {
		return getProjectName();
	}

	public String getDisplayName() {
		return getProjectName();
	}

	/*public void setProjectName(String aName) throws InvalidNameException {
		if (!BAD_CHARACTERS_PATTERN.matcher(aName).find()) {
			projectName = aName;
			// resources.restoreKeys();
		}
		else {
			throw new InvalidNameException();
		}
	}*/

	@Override
	public String getPrefix() {
		String prefix = null;
		if (getProjectName().length() > 2) {
			prefix = getProjectName().substring(0, 3).toUpperCase();
		}
		else {
			prefix = getProjectName().toUpperCase();
		}
		return ToolBox.getJavaName(prefix, true, false);
	}

	@Override
	public I getProjectDirectory() {
		return (I) getResource().getIODelegate().getSerializationArtefact();
	}

	/*public void setProjectDirectory(File aProjectDirectory) {
		setProjectDirectory(aProjectDirectory, true);
	}
	
	public void setProjectDirectory(File aProjectDirectory, boolean notify) {
		if (!aProjectDirectory.equals(projectDirectory)) {
			File oldProjectDirectory = this.projectDirectory;
			projectDirectory = aProjectDirectory;
			setResource(ProjectDirectoryResourceImpl.makeProjectDirectoryResource(this));
			// clearCachedFiles();
			if (notify) {
				setChanged();
				notifyObservers(new DataModification(PROJECT_DIRECTORY, oldProjectDirectory, projectDirectory));
			}
		}
	}*/

	@Override
	public FlexoResource<FlexoProject<I>> getResource() {
		return (FlexoResource) resource;
	}

	@Override
	public void setResource(FlexoResource<FlexoProject<I>> resource) {
		this.resource = (FlexoProjectResource) (FlexoResource) resource;
	}

	/**
	 * Overrides setChanged
	 */
	@Override
	public void setChanged() {
		super.setChanged();
	}

	/**
	 * Save this project
	 * 
	 */
	public void save() throws SaveResourceException {
		save(null);
	}

	public void save(FlexoProgress progress) throws SaveResourceException {
		logger.info("Saving project...");

		getResource().save(progress);

		saveModifiedResources(progress, true);

		getServiceManager().getResourceManager().deleteFilesToBeDeleted();

		logger.info("Saving project... DONE");
	}

	@Override
	public void copyTo(I newProjectDirectory) throws SaveResourceException, InvalidNameException, CannotRenameException {
		logger.info("Copy project to... (" + newProjectDirectory + ")");

		if (Objects.equals(getProjectDirectory(), newProjectDirectory)) {
			save();
		}
		else {
			try {
				// sets project directory
				getResource().setName(retrieveName(newProjectDirectory));

				if (newProjectDirectory instanceof File) {
					File current = (File) getResource().getIODelegate().getSerializationArtefact();
					FileUtils.copyContentDirToDir(current, (File) newProjectDirectory, CopyStrategy.REPLACE,
							FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter("~")));
				}

			} catch (IOException e) {
				throw new SaveResourceException(getResource().getIODelegate(), e);
			}
		}

		logger.info("Copy project to... (" + newProjectDirectory + ") DONE");
	}

	/**
	 * Zip the current projects into the given zip file <code>zipFile</code>. If <code>lighten</code> is set to true, the project will be
	 * lighten, i.e., jars will be emptied and screenshots will be removed.
	 * 
	 * @param zipFile
	 *            the file on which to zip the project
	 * @param lightenProject
	 *            wheter the zipped project should be lighten of unrequired resources or not.
	 * @throws SaveResourceException
	 */
	public void saveAsZipFile(File zipFile, FlexoProgress progress, boolean lightenProject, boolean copyCVSFiles)
			throws SaveResourceException {
		try {
			FileUtils.createNewFile(zipFile);
			File tempProjectDirectory = FileUtils
					.createTempDirectory(getProjectName().length() > 2 ? getProjectName() : "FlexoProject-" + getProjectName(), "");
			tempProjectDirectory = new File(tempProjectDirectory, getProjectName());
			// copyTo(tempProjectDirectory, progress, null, false, copyCVSFiles);
			if (lightenProject) {
				// replaceBigJarsWithEmtpyJars(progress, tempProjectDirectory);
				// removeScreenshots(progress, tempProjectDirectory);
				removeBackupFiles(progress, tempProjectDirectory);
			}
			if (progress != null) {
				progress.setProgress(getLocales().localizedForKey("zipping_project"));
			}
			ZipUtils.makeZip(zipFile, tempProjectDirectory, progress, null,
					lightenProject ? Deflater.BEST_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SaveResourceException(null, e);
		}
	}

	private void removeBackupFiles(FlexoProgress progress, File tempProjectDirectory) {
		List<File> tildes = FileUtils.listFilesRecursively(tempProjectDirectory, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("~");
			}
		});
		if (progress != null) {
			progress.setProgress(getLocales().localizedForKey("removing_backups"));
			progress.resetSecondaryProgress(tildes.size());
		}
		for (File tilde : tildes) {
			if (progress != null) {
				progress.setSecondaryProgress(getLocales().localizedForKey("removing") + " " + tilde.getName());
			}
			tilde.delete();
		}
	}

	/**
	 * Save this project using FlexoEditingContext scheme Additionnaly save all known resources related to this project
	 * 
	 * Overrides
	 * 
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 */
	public synchronized void saveModifiedResources(FlexoProgress progress) throws SaveResourceException {
		saveModifiedResources(progress, true);
	}

	/**
	 * Save this project using FlexoEditingContext scheme Additionally save all known resources related to this project
	 * 
	 * Overrides
	 * 
	 * @param clearModifiedStatus
	 *            TODO
	 * 
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 */
	public synchronized void saveModifiedResources(FlexoProgress progress, boolean clearModifiedStatus) throws SaveResourceException {
		try {
			_saveModifiedResources(progress, clearModifiedStatus);
		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Exception occurred during saving " + getDisplayName() + ". Trying to repair and save again");
			}
			repairProject();
			_saveModifiedResources(progress, clearModifiedStatus);
		}
	}

	private ValidationReport repairProject() {
		try {
			if (getProjectValidationModel() != null) {
				return getProjectValidationModel().validate(this);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * Return all resources encoding current project<br>
	 * Return a collection of all resources that are part of this project
	 */
	@Override
	public Collection<FlexoResource<?>> getAllResources() {
		return super.getAllResources();
	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		return getAllResources();
	}

	/**
	 * Return a collection of all resources of this project which are to be saved (resource data is modified)
	 */
	public List<FlexoResource<?>> getUnsavedResources() {
		List<FlexoResource<?>> unsaved = new ArrayList<>();
		for (FlexoResource<?> r : getAllResources()) {
			if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
				unsaved.add(r);
			}
		}
		return unsaved;
	}

	private void _saveModifiedResources(FlexoProgress progress, boolean clearModifiedStatus) throws SaveResourceException
	/*SaveXMLResourceException, SaveResourcePermissionDeniedException*/ {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Saving modified resources of project...");
		}
		List<FlexoResource<?>> unsaved = getUnsavedResources();
		if (progress != null) {
			progress.setProgress(getLocales().localizedForKey("saving_modified_resources"));
			progress.resetSecondaryProgress(unsaved.size() + 1);
		}
		boolean resourceSaved = false;
		try {
			for (FlexoResource<?> r : unsaved) {
				if (progress != null) {
					progress.setSecondaryProgress(getLocales().localizedForKey("saving_resource_") + r);
				}
				r.save(null);
			}
		} finally {
			if (resourceSaved) {
				// Revision is incremented only if a FlexoStorageResource has
				// been changed. This is allows essentially to track if the
				// model of the project has changed.
				setProjectRevision(getProjectRevision() + 1);
			}
		}
	}

	/**
	 * Overrides super {@link #getResourceCenter()} by returning itself
	 */
	@Override
	public FlexoResourceCenter<I> getResourceCenter() {
		/*if (getResource() != null) {
			return (FlexoResourceCenter<I>)getResource().getResourceCenter();
		}*/
		System.out.println("Returned project instead of" + super.getResourceCenter());
		// return super.getResourceCenter();
		return this;
	}

	public DirectoryWatcher getDirectoryWatcher() {
		if (getResourceCenter() instanceof FileSystemBasedResourceCenter) {
			return ((FileSystemBasedResourceCenter) getResourceCenter()).getDirectoryWatcher();
		}
		System.out.println("Problem: cannot find DirectoryWatcher");
		return null;
	}

	private boolean closed = false;

	/**
	 * Close this project<br>
	 * Don't save anything
	 * 
	 */
	@Override
	public void close() {

		if (closed) {
			return;
		}
		// Stops DirectoryWatcher if it exists?
		DirectoryWatcher dw = this.getDirectoryWatcher();
		if (dw != null) {
			dw.cancel();
		}
		// Removes from resourceCenters
		FlexoServiceManager svcManager = getServiceManager();
		if (svcManager != null) {
			svcManager.getResourceCenterService().removeFromResourceCenters(this);
		}
		closed = true;
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Closing project...");
		}
		deleteObservers();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Closing project... DONE");
		}

	}

	/**
	 * Return boolean indicating if this project might be interpreted according to this project nature
	 * 
	 * @param projectNature
	 * @return
	 */
	public final boolean hasNature(ProjectNature<?, ?> projectNature) {
		return projectNature.hasNature(this);
	}

	/**
	 * Return boolean indicating if this project might be interpreted according to this project nature (supplied as string representing full
	 * class name)
	 * 
	 * @param projectNature
	 * @return
	 */
	public final boolean hasNature(String projectNatureClassName) {
		ProjectNature<?, ?> projectNature = getServiceManager().getProjectNatureService().getProjectNature(projectNatureClassName);
		return projectNature.hasNature(this);
	}

	/**
	 * Ensure this project has the supplied {@link ProjectNature}<br>
	 * 
	 * If the project already has this nature, do nothing<br>
	 * If the project does't have this nature, perform nature giving using supplied {@link FlexoEditor}
	 * 
	 * @param projectNature
	 * @param editor
	 *            the editor to use to edit the project
	 */
	public final void givesNature(ProjectNature<?, ?> projectNature, FlexoEditor editor) {
		if (!hasNature(projectNature)) {
			projectNature.givesNature(this, editor);
		}
	}

	/**
	 * Return project wrapper object representing this project according to supplied nature
	 * 
	 * @param projectNature
	 * @return
	 */
	public final <N extends ProjectNature<N, P>, P extends ProjectWrapper<N>> P asNature(N projectNature) {
		return projectNature.getProjectWrapper(this);
	}

	/**
	 * Return project wrapper object representing this project according to supplied nature
	 * 
	 * @param projectNature
	 * @return
	 */
	public final ProjectWrapper<?> asNature(String projectNatureClassName) {
		if (!closed) {
			ProjectNature<?, ?> projectNature = getServiceManager().getProjectNatureService().getProjectNature(projectNatureClassName);
			if (projectNature != null && hasNature(projectNature)) {
				return projectNature.getProjectWrapper(this);
			}
		}

		// System.out.println("Could not lookup nature " + projectNatureClassName);
		return null;
	}

	boolean generateSnapshot = true;

	private void setGenerateSnapshot(boolean b) {
		generateSnapshot = b;
	}

	public boolean isGenerateSnapshot() {
		return generateSnapshot;
	}

	// ======================================================================
	// ============================= Validation =============================
	// ======================================================================

	public ValidationModel getProjectValidationModel() {
		if (projectValidationModel == null) {
			try {
				projectValidationModel = new FlexoProjectValidationModel();
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return projectValidationModel;
	}

	/**
	 * Don't use this method to get a new ID. Use getNewUniqueID instead
	 * 
	 * @return Returns the lastUniqueID.
	 */
	@Override
	public long getLastUniqueID() {
		if (getLastID() < 1) {
			setLastID(1);
		}
		return getLastID();

	}

	/**
	 * @param lastUniqueID
	 *            The lastUniqueID to set.
	 */
	@Override
	public void setLastUniqueID(long lastUniqueID) {
		setLastID(lastUniqueID);
	}

	// TODO Code to be Removed as no use for that
	/*
	public FlexoProjectFile getJavaFormatterSettings() {
		if (!new FlexoProjectFile(this, "FlexoJavaFormatSettings.xml").getFile().exists()) {
			initJavaFormatter();
		}
		return new FlexoProjectFile(this, "FlexoJavaFormatSettings.xml");
	}
	 */
	/**
	 *
	 */

	// TODO Code to be Removed as no use for that
	/*
	protected void initJavaFormatter() {
		File file = ResourceLocator.retrieveResourceAsFile("Config/FlexoJavaFormatSettings.xml");
		FlexoProjectFile prjFile = new FlexoProjectFile(this, "FlexoJavaFormatSettings.xml");
		try {
			FileUtils.copyFileToFile(file, prjFile.getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 */

	/**
	 * Ensure that all .prj contains required .cvsignore files
	 * 
	 */
	public static void cvsIgnorize(File projectDirectory) {
		File mainCVSIgnoreFile = new File(projectDirectory, ".cvsignore");
		if (!mainCVSIgnoreFile.exists()) {
			try {
				FileUtils.saveToFile(mainCVSIgnoreFile,
						"*~\n" + ".#*\n" + "*.rmxml.ts\n" + "temp?.xml\n" + "*.ini\n" + "*.cvsrepository\n" + "*.bak\n" + "*.autosave\n");
			} catch (IOException e) {
				logger.warning("Could not create file " + mainCVSIgnoreFile + ": " + e.getMessage());
				e.printStackTrace();
			}
		}

		cvsIgnorizeDir(projectDirectory);

	}

	private static void cvsIgnorizeDir(File aDirectory) {
		File cvsIgnoreFile = new File(aDirectory, ".cvsignore");
		if (!cvsIgnoreFile.exists()) {
			try {
				FileUtils.saveToFile(cvsIgnoreFile, "*~\n" + ".#*\n" + "temp?.xml\n" + "*.rmxml.ts\n" + "*.history\n" + "PB.project\n"
						+ "*.xcodeproj\n" + "*.autosave\n");
			} catch (IOException e) {
				logger.warning("Could not create file " + cvsIgnoreFile + ": " + e.getMessage());
				e.printStackTrace();
			}
		}

		File[] allDirs = aDirectory.listFiles(new java.io.FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.getName().equals("CVS") && !pathname.getName().equals(".history")
						&& !pathname.getName().equals(".wo.LAST_ACCEPTED") && !pathname.getName().equals(".wo.LAST_GENERATED");
			}
		});

		for (File f : allDirs) {
			cvsIgnorizeDir(f);
		}
	}

	/**
	 * Remove all CVS directories
	 */
	public static void removeCVSDirs(File projectDirectory) {
		removeCVSDirs(projectDirectory, true);
	}

	/**
	 * Remove all CVS directories
	 */
	public static void removeCVSDirs(File aDirectory, boolean recurse) {
		File cvsDir = new File(aDirectory, "CVS");
		if (cvsDir.exists()) {
			FileUtils.recursiveDeleteFile(cvsDir);
		}
		if (recurse) {
			File[] allDirs = aDirectory.listFiles(new java.io.FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory() && !pathname.getName().equals("CVS");
				}
			});
			for (File f : allDirs) {
				removeCVSDirs(f);
			}
		}
	}

	/**
	 * Search (deeply) CVS directories Return true if at least one CVS dir was found
	 */
	public static boolean searchCVSDirs(File aDirectory) {
		File cvsDir = new File(aDirectory, "CVS");
		if (cvsDir.exists()) {
			return true;
		}
		File[] allDirs = aDirectory.listFiles(new java.io.FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.getName().equals("CVS");
			}
		});
		for (File f : allDirs) {
			if (searchCVSDirs(f)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<FlexoEditor> getEditors() {
		return editors;
	}

	@Override
	public void setEditors(List<FlexoEditor> editors) {
		this.editors = editors;
	}

	@Override
	public void addToEditors(FlexoEditor editor) {
		editors.add(editor);
	}

	@Override
	public void removeFromEditors(FlexoEditor editor) {
		editors.remove(editor);
	}

	public ProjectLoadingHandler getLoadingHandler() {
		return loadingHandler;
	}

	@Override
	public boolean lastUniqueIDHasBeenSet() {
		return lastUniqueIDHasBeenSet;
	}

	@Override
	public long getNewFlexoID() {
		if (lastID < 0) {
			return -1;
		}
		return ++lastID;
	}

	/**
	 * @return Returns the lastUniqueID.
	 */
	@Override
	public long getLastID() {
		if (lastUniqueIDHasBeenSet && lastID < 0) {
			lastID = 0;
		}
		return lastID;
	}

	/**
	 * @param lastUniqueID
	 *            The lastUniqueID to set.
	 */
	@Override
	public void setLastID(long lastUniqueID) {
		if (lastUniqueID > lastID) {
			lastID = lastUniqueID;
			lastUniqueIDHasBeenSet = true;
		}
	}

	public static boolean getIsLoadingAProject() {
		return false;
	}

	/**
	 * Overrides finalize
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		System.err.println("##########################################################\n" + "# Project finalization" + getID() + "\n"
				+ "##########################################################");
		super.finalize();
	}

	/*public String getUnusedImageName(String name) {
			String attempt = name;
			int i = 1;
			while (resourceForFile(new File(getImportedImagesDir(), attempt)) != null) {
				if (name.indexOf('.') > -1) {
					attempt = name.substring(0, name.lastIndexOf('.')) + "-" + i + name.substring(name.lastIndexOf('.')).toLowerCase();
				}
				i++;
			}
			return attempt;
		}*/

	public boolean isHoldingProjectRegistration() {
		return holdObjectRegistration;
	}

	public void holdObjectRegistration() {
		holdObjectRegistration = true;
	}

	public void unholdObjectRegistration() {
		holdObjectRegistration = false;
	}

	private boolean _rebuildDependanciesIsRequired = false;

	public void setRebuildDependanciesIsRequired() {
		_rebuildDependanciesIsRequired = true;
	}

	public boolean rebuildDependanciesIsRequired() {
		return _rebuildDependanciesIsRequired;
	}

	@DefineValidationRule
	public static class FlexoIDMustBeUnique extends ValidationRule<FlexoIDMustBeUnique, FlexoProjectImpl> {

		/**
		 * @param objectType
		 * @param ruleName
		 */
		public FlexoIDMustBeUnique() {
			super(FlexoProjectImpl.class, "flexo_id_must_be_unique");
		}

		/**
		 * Overrides applyValidation
		 * 
		 * @see org.openflexo.model.validation.ValidationRule#applyValidation(org.openflexo.model.validation.Validable)
		 */
		@Override
		public ValidationIssue<FlexoIDMustBeUnique, FlexoProjectImpl> applyValidation(FlexoProjectImpl object) {
			List<FlexoProjectObject> badObjects = object.getObjectIDManager().checkProject(true);
			if (badObjects.size() > 0) {
				DuplicateObjectIDIssue issues = new DuplicateObjectIDIssue(object);
				// TODO FD pour SG pourquoi obj pas utilisé dans la boucle ?
				for (FlexoProjectObject obj : badObjects) {
					issues.addToContainedIssues(new InformationIssue<FlexoIDMustBeUnique, FlexoProjectImpl>(object,
							"identifier_of_($object.fullyQualifiedName)_was_duplicated_and_reset_to_($object.flexoID)"));
				}
				return issues;
			}
			else {
				return new InformationIssue<>(object, "no_duplicated_identifiers_found");
			}
		}

		public static class DuplicateObjectIDIssue extends CompoundIssue<FlexoIDMustBeUnique, FlexoProjectImpl> {

			public DuplicateObjectIDIssue(FlexoProjectImpl anObject) {
				super(anObject);
			}

		}
	}

	public String buildProjectURI() {
		Calendar rightNow = Calendar.getInstance();
		return BASE_PROJECT_URI + "/" + rightNow.get(Calendar.YEAR) + "/" + (rightNow.get(Calendar.MONTH) + 1) + "/" + getProjectName()
				+ "_" + System.currentTimeMillis();
	}

	public String getCreationDateAsString() {
		if (getCreationDate() != null) {
			return new SimpleDateFormat("dd/MM HH:mm:ss").format(getCreationDate());
		}
		return getLocales().localizedForKey("unknown");
	}

	@Override
	public String toString() {
		return "PROJECT-" + getDisplayName() + " ID=" + getID();
	}

	@Override
	public boolean importsProject(FlexoProject project) {
		return project != null && importsProjectWithURI(project.getProjectURI());
	}

	@Override
	public boolean importsProjectWithURI(String projectURI) {
		return getProjectReferenceWithURI(projectURI, true) != null;
	}

	public FlexoObjectIDManager getObjectIDManager() {
		if (objectIDManager == null) {
			objectIDManager = new FlexoObjectIDManager(this);
		}
		return objectIDManager;
	}

	/*public String canImportProject(FlexoProjectImpl project) {
		if (getProjectData() == null) {
			return null;
		}
		else {
			return getProjectData().canImportProject(project);
		}
	}*/

	public boolean hasImportedProjects() {
		return getImportedProjects().size() > 0;
	}

	public List<FlexoProjectReference> getResolvedProjectReferences() {
		List<FlexoProjectReference> refs = new ArrayList<>();
		appendResolvedReferences(this, refs);
		return refs;
	}

	private void appendResolvedReferences(FlexoProject<?> project, List<FlexoProjectReference> refs) {
		if (project != null) {
			for (FlexoProjectReference ref : project.getImportedProjects()) {
				boolean alreadyAdded = false;
				for (FlexoProjectReference addedRef : refs) {
					if (addedRef.getURI().equals(ref.getURI())) {
						alreadyAdded = true;
						break;
					}
				}
				if (!alreadyAdded) {
					refs.add(ref);
				}
			}
			for (FlexoProjectReference ref : project.getImportedProjects()) {
				if (ref.getReferencedProject() != null) {
					appendResolvedReferences(ref.getReferencedProject(), refs);
				}
			}
		}
	}

	@Override
	public List<FlexoObjectReference<?>> getObjectReferences() {
		return objectReferences;
	}

	@Override
	public void addToObjectReferences(FlexoObjectReference<?> objectReference) {
		objectReferences.add(objectReference);
	}

	@Override
	public void removeObjectReferences(FlexoObjectReference<?> objectReference) {
		objectReferences.remove(objectReference);
	}

	@Override
	public boolean areAllImportedProjectsLoaded() {
		return areAllImportedProjectsLoaded(this);
	}

	private static boolean areAllImportedProjectsLoaded(FlexoProject<?> project) {
		for (FlexoProjectReference ref : project.getImportedProjects()) {
			if (ref.getReferencedProject() == null) {
				return false;
			}
			else if (!ref.getReferencedProject().areAllImportedProjectsLoaded()) {
				return false;
			}
		}
		return true;
	}

	public FlexoProjectImpl loadProjectReference(FlexoProjectReference reference, boolean silentlyOnly) {
		if (projectReferenceLoader != null) {
			FlexoProjectImpl loadProject = projectReferenceLoader.loadProject(reference, silentlyOnly);
			if (loadProject != null) {
				setChanged();
				notifyObservers(new ImportedProjectLoaded(loadProject));
			}
			return loadProject;
		}
		return null;
	}

	/*public static File getProjectSpecificModelsDirectory(FlexoProjectImpl project) {
		File returned = new File(project.getProjectDirectory(), "Models");
		returned.mkdirs();
		return returned;
	}*/

	public ProjectExternalRepository createExternalRepositoryWithKey(String identifier) throws DuplicateExternalRepositoryNameException {
		for (ProjectExternalRepository rep : _externalRepositories) {
			if (rep.getIdentifier().equals(identifier)) {
				throw new DuplicateExternalRepositoryNameException(null, identifier);
			}
		}
		ProjectExternalRepository returned = new ProjectExternalRepository(this, identifier);
		addToExternalRepositories(returned);
		return returned;
	}

	public String getNextExternalRepositoryIdentifier(String base) {
		String attempt = base;
		int i = 0;
		while (getExternalRepositoryWithKey(attempt) != null) {
			i++;
			attempt = base + i;
		}
		return attempt;
	}

	public ProjectExternalRepository getExternalRepositoryWithKey(String identifier) {
		for (ProjectExternalRepository rep : _externalRepositories) {
			if (rep.getIdentifier().equals(identifier)) {
				return rep;
			}
		}
		return null;
	}

	public ProjectExternalRepository getExternalRepositoryWithDirectory(File directory) {
		if (directory == null) {
			return null;
		}
		for (ProjectExternalRepository rep : _externalRepositories) {
			if (rep.getDirectory() != null && rep.getDirectory().equals(directory)) {
				return rep;
			}
		}
		return null;
	}

	public ProjectExternalRepository setDirectoryForRepositoryName(String identifier, File directory) {
		ProjectExternalRepository returned = getExternalRepositoryWithKey(identifier);
		if (returned == null) {
			returned = new ProjectExternalRepository(this, identifier);
			addToExternalRepositories(returned);
		}
		if (returned.getDirectory() == null || !returned.getDirectory().equals(directory)) {
			returned.setDirectory(directory);
			setChanged();
			notifyObservers(new ExternalRepositorySet(returned));
		}
		return returned;
	}

	public List<ProjectExternalRepository> getExternalRepositories() {
		return _externalRepositories;
	}

	public void addToExternalRepositories(ProjectExternalRepository anExternalRepository) {
		_externalRepositories.add(anExternalRepository);
		repositoriesCache.put(anExternalRepository.getIdentifier(), anExternalRepository);
		setChanged();
	}

	public void removeFromExternalRepositories(ProjectExternalRepository anExternalRepository) {
		_externalRepositories.remove(anExternalRepository);
		repositoriesCache.remove(anExternalRepository.getIdentifier());
		setChanged();
	}

	@Override
	public boolean hasUnsavedResources() {
		for (FlexoResource<?> r : getAllResources()) {
			if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultBaseURI() {
		return getProjectURI();
	}

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link FlexoProject}
	 * 
	 * @return
	 */
	@Override
	public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
		List<TechnologyAdapter> returned = new ArrayList<>();
		// TODO
		/*for (ViewResource vr : getViewLibrary().getAllResources()) {
			for (TechnologyAdapter ta : vr.getView().getRequiredTechnologyAdapters()) {
				if (!returned.contains(ta)) {
					returned.add(ta);
				}
			}
		}*/
		return returned;
	}

	@Override
	public String getDisplayableName() {
		return getName();
	}

	@Override
	public FlexoProjectReference getProjectReferenceWithURI(String projectURI, boolean searchRecursively) {
		FlexoProjectReference ref = getProjectReferenceWithURI(projectURI);
		if (ref != null) {
			return ref;
		}
		if (searchRecursively) {
			for (FlexoProjectReference ref2 : getImportedProjects()) {
				if (ref2.getReferencedProject().getProjectReferenceWithURI(projectURI, searchRecursively) != null) {
					return ref2.getReferencedProject().getProjectReferenceWithURI(projectURI, searchRecursively);
				}
			}
		}
		return null;
	}

	@Override
	public void addToImportedProjects(FlexoProjectReference projectReference) throws ProjectImportLoopException {
		if (!isDeserializing()) {
			if (getImportedProjects().contains(projectReference)) {
				return;
			}
			String reason = canImportProject(projectReference.getReferencedProject());
			if (reason != null) {
				throw new ProjectImportLoopException(reason);
			}
		}
		performSuperAdder(IMPORTED_PROJECTS, projectReference);
		/*if (!isDeserializing() && projectReference.getReferredProject() != null) {
			getProject().getFlexoRMResource().addToDependentResources(projectReference.getReferredProject().getFlexoRMResource());
			getProject().getImportedWorkflow(projectReference, true);
		}*/
	}

	@Override
	public void removeFromImportedProjects(FlexoProjectReference projectReference) {
		// TODO: remove dependency toward external resource.
		// getProject().getFlexoRMResource().removeFromDependentResources(projectReference.getReferredProject().getFlexoRMResource());
		performSuperRemover(IMPORTED_PROJECTS, projectReference);
	}

	@Override
	public String canImportProject(FlexoProject project) {
		if (project.getProjectURI().equals(getProject().getProjectURI())) {
			return getLocales().localizedForKey("cannot_import_itself");
		}
		if (getProjectReferenceWithURI(project.getProjectURI()) != null) {
			return getLocales().localizedForKey("project_already_imported");
		}
		return null;
	}

	@Override
	public void removeFromImportedProjects(FlexoProject project) {
		for (FlexoProjectReference ref : getImportedProjects()) {
			if (ref.getReferredProject() == project) {
				removeFromImportedProjects(ref);
				break;
			}
		}
	}

	@Override
	public FlexoProjectFactory getModelFactory() {
		return ((FlexoProjectResource) getResourceData().getResource()).getFactory();
	}

}
