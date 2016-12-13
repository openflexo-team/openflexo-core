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

package org.openflexo.foundation;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.openflexo.foundation.FlexoEditor.FlexoEditorFactory;
import org.openflexo.foundation.ProjectDataResource.ProjectDataResourceImpl;
import org.openflexo.foundation.ProjectDirectoryResource.ProjectDirectoryResourceImpl;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectWrapper;
import org.openflexo.foundation.resource.DuplicateExternalRepositoryNameException;
import org.openflexo.foundation.resource.ExternalRepositorySet;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ImportedProjectLoaded;
import org.openflexo.foundation.resource.ProjectExternalRepository;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectIDManager;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.foundation.validation.FlexoProjectValidationModel;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.CompoundIssue;
import org.openflexo.model.validation.InformationIssue;
import org.openflexo.model.validation.Validable;
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

import javax.naming.InvalidNameException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

/**
 * This class represents an Openflexo project.<br>
 * 
 * A FlexoProject is a logical container of resources located in project directory.<br>
 * 
 * The purpose of a {@link FlexoProject} againts work in ResourceCenter is to get a self-contained and transportable set of resources.<br>
 * 
 * {@link FlexoProject} is the entry point to navigate through the whole data of the project.<br>
 * Shared contents (along many projects) are preferabely located in a {@link FlexoResourceCenter}.
 * 
 * 
 * @author sguerin
 */
public class FlexoProject extends FileSystemBasedResourceCenter
		/*ResourceRepository<FlexoResource<?>>*/ implements Validable, ResourceData<FlexoProject>, ReferenceOwner {

	protected static final Logger logger = Logger.getLogger(FlexoProject.class.getPackage().getName());

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
	public static FlexoEditor newProject(File aProjectDirectory, FlexoEditorFactory editorFactory, FlexoServiceManager serviceManager,
			FlexoProgress progress) throws ProjectInitializerException {

		// We should have already asked the user if the new project has to override the old one
		// When true, old directory was renamed to backup file
		// So, this is not normal to have here an existing file
		if (aProjectDirectory.exists()) {
			throw new ProjectInitializerException("This directory already exists: " + aProjectDirectory, aProjectDirectory);
		}

		FlexoProject project = new FlexoProject(aProjectDirectory, serviceManager);
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
	}

	// TODO: add ProjectLoadingHandler to the parameters
	public static FlexoEditor openProject(File aProjectDirectory, FlexoEditorFactory editorFactory, FlexoServiceManager serviceManager,
			FlexoProgress progress) throws ProjectInitializerException, ProjectLoadingCancelledException {

		Progress.progress(getLocales(serviceManager).localizedForKey("opening_project") + aProjectDirectory.getAbsolutePath());

		if (!aProjectDirectory.exists()) {
			throw new ProjectInitializerException("This directory does not exists: " + aProjectDirectory, aProjectDirectory);
		}

		FlexoProject project = new FlexoProject(aProjectDirectory, serviceManager);
		project.setServiceManager(serviceManager);
		FlexoEditor editor = editorFactory.makeFlexoEditor(project, serviceManager);
		project.setLastUniqueID(0);
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Loading project: " + aProjectDirectory.getAbsolutePath());
		}

		try {
			// project.setCreationUserId(FlexoObjectImpl.getCurrentUserIdentifier());
			// project.setCreationDate(new Date());
			// TODO: Code to be Removed
			// project.initJavaFormatter();
			/*try {
				// This needs to be called to ensure the consistency of the
				// project
				project.setGenerateSnapshot(false);
				project.save(progress);
				project.setGenerateSnapshot(true);
			} catch (SaveResourceException e) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe("Could not save all resources for project: " + project.getProjectName() + " located in "
							+ project.getProjectDirectory().getAbsolutePath());
				}
			}*/
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
	}

	public static String nameFromDirectory(File projectDirectory) {
		String projectName = projectDirectory.getName().replaceAll(BAD_CHARACTERS_REG_EXP, " ");
		if (projectName.endsWith(".prj")) {
			projectName = projectName.substring(0, projectName.length() - 4);
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Project directory does not end with '.prj'");
			}
		}
		return projectName;
	}

	public static final String BASE_PROJECT_URI = "http://www.openflexo.org/projects";
	public static final String RESOURCES = "resources";

	public static final String PROJECT_DIRECTORY = "projectDirectory";
	public static final String PROJECT_DATA = "projectData";
	public static final String REVISION = "revision";
	public static final String VERSION = "version";
	public static final String PROJECT_URI = "projectURI";

	public static final String VIEWPOINT_EXPECTED_DIRECTORY = "Viewpoints";

	private FlexoServiceManager serviceManager;

	private FlexoObjectIDManager objectIDManager;

	private ViewLibrary viewLibrary = null;

	private final List<FlexoObjectReference> objectReferences = new ArrayList<FlexoObjectReference>();

	private boolean lastUniqueIDHasBeenSet = false;
	private long lastID = Integer.MIN_VALUE;

	// " | ? * [ ] / < > = { } & % # ~ \ _
	public static final String BAD_CHARACTERS_REG_EXP = "[\"|\\?\\*\\[\\]/<>:{}&%#~\\\\_]";

	public static final Pattern BAD_CHARACTERS_PATTERN = Pattern.compile(BAD_CHARACTERS_REG_EXP);

	protected String projectName;

	protected File projectDirectory;

	private List<File> filesToDelete;

	private String projectDescription = "";

	private final long firstOperationFlexoID = -1;

	private FlexoVersion version = new FlexoVersion("1.0");

	private long revision = 0;

	private FlexoProjectValidationModel projectValidationModel;

	private static int ID = 0;

	private final int id;

	// private Date creationDate;
	// private String creationUserId;
	// private String projectURI;
	// private String projectVersionURI;

	private boolean holdObjectRegistration = false;

	private ProjectLoadingHandler loadingHandler;

	private ProjectDirectoryResource projectDirectoryResource;

	public int getID() {
		return id;
	}

	private List<FlexoEditor> editors;

	private FlexoProjectReferenceLoader projectReferenceLoader;

	private List<ProjectExternalRepository> _externalRepositories;
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
		public FlexoProject loadProject(FlexoProjectReference reference, boolean silentlyOnly);

	}

	protected FlexoProject(File aProjectDirectory, FlexoServiceManager serviceManager) {
		super(aProjectDirectory, serviceManager.getResourceCenterService());
		this.serviceManager = serviceManager;

		editors = new Vector<FlexoEditor>();
		synchronized (FlexoProject.class) {
			id = ID++;
		}
		logger.info("Create new project, ID=" + id);
		_externalRepositories = new ArrayList<ProjectExternalRepository>();
		repositoriesCache = new Hashtable<String, ProjectExternalRepository>();
		filesToDelete = new Vector<File>();

		projectName = nameFromDirectory(aProjectDirectory);
		setProjectDirectory(aProjectDirectory);
	}

	@Override
	public ProjectDirectoryResource getResource() {
		return projectDirectoryResource;
	}

	@Override
	public void setResource(FlexoResource<FlexoProject> resource) {
		projectDirectoryResource = (ProjectDirectoryResource) resource;
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
		getProjectDataResource().save(progress);

		saveModifiedResources(progress, true);

		getServiceManager().getResourceManager().deleteFilesToBeDeleted();

		logger.info("Saving project... DONE");
	}

	public void copyTo(File newProjectDirectory) throws SaveResourceException, InvalidNameException {
		logger.info("Copy project to... (" + newProjectDirectory +")" );

		if (Objects.equals(getProjectDirectory(), newProjectDirectory)) {
			save();
		} else {
			try {
				// sets project directory
				setProjectDirectory(newProjectDirectory);

				File current = getRootDirectory();
				FileUtils.copyContentDirToDir(current, newProjectDirectory, CopyStrategy.REPLACE, FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter("~")));

			} catch (IOException e) {
				throw new SaveResourceException(getResource().getFlexoIODelegate(), e);
			}
		}

		logger.info("Copy project to... (" + newProjectDirectory +") DONE");
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
			tempProjectDirectory = new File(tempProjectDirectory, getProjectDirectory().getName());
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
			return getProjectValidationModel().validate(this);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
		List<FlexoResource<?>> unsaved = new ArrayList<FlexoResource<?>>();
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
				setRevision(revision + 1);
			}
		}
	}

	private boolean closed = false;

	/**
	 * Close this project Don't save anything
	 * 
	 * Overrides
	 * 
	 */
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
		serviceManager = null;
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
			if (projectNature != null) {
				return projectNature.getProjectWrapper(this);
			}
		}

		System.out.println("Could not lookup nature " + projectNatureClassName);
		return null;
	}

	public ViewLibrary<?> getViewLibrary() {
		if (viewLibrary == null) {
			viewLibrary = ViewLibrary.createNewViewLibrary(this);
		}
		return viewLibrary;
	}

	boolean generateSnapshot = true;

	private void setGenerateSnapshot(boolean b) {
		generateSnapshot = b;
	}

	public boolean isGenerateSnapshot() {
		return generateSnapshot;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(File aProjectDirectory) {
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
	}

	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getName() {
		return getProjectName();
	}

	public String getDisplayName() {
		String name = getProjectDirectory().getName();
		if (name.toLowerCase().endsWith(".prj")) {
			return name.substring(0, name.length() - 4);
		}
		return name;
	}

	public void setProjectName(String aName) throws InvalidNameException {
		if (!BAD_CHARACTERS_PATTERN.matcher(aName).find()) {
			projectName = aName;
			// resources.restoreKeys();
		}
		else {
			throw new InvalidNameException();
		}
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String aDescription) {
		projectDescription = aDescription;
		setChanged();
	}

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

	// ======================================================================
	// ============================= Validation =============================
	// ======================================================================

	public ValidationModel getProjectValidationModel() {
		if (projectValidationModel == null) {
			try {
				projectValidationModel = new FlexoProjectValidationModel();
			} catch (ModelDefinitionException e) {
				// TODO Auto-generated catch block
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

	public List<FlexoEditor> getEditors() {
		return editors;
	}

	public void setEditors(List<FlexoEditor> editors) {
		this.editors = editors;
	}

	public void addToEditors(FlexoEditor editor) {
		editors.add(editor);
	}

	public void removeFromEditors(FlexoEditor editor) {
		editors.remove(editor);
	}

	public ProjectLoadingHandler getLoadingHandler() {
		return loadingHandler;
	}

	public boolean lastUniqueIDHasBeenSet() {
		return lastUniqueIDHasBeenSet;
	}

	public long getNewFlexoID() {
		if (lastID < 0) {
			return -1;
		}
		return ++lastID;
	}

	/**
	 * @return Returns the lastUniqueID.
	 */
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
	public void setLastID(long lastUniqueID) {
		if (lastUniqueID > lastID) {
			lastID = lastUniqueID;
			lastUniqueIDHasBeenSet = true;
		}
	}

	public boolean getLastUniqueIDHasBeenSet() {
		return lastUniqueIDHasBeenSet;
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

	@Deprecated
	public void addToFilesToDelete(File f) {
		filesToDelete.add(f);
	}

	@Deprecated
	public void removeFromFilesToDelete(File f) {
		filesToDelete.remove(f);
	}

	@Deprecated
	public void deleteFilesToBeDeleted() {
		for (File f : filesToDelete) {
			try {
				if (FileUtils.recursiveDeleteFile(f)) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Successfully deleted " + f.getAbsolutePath());
						// filesToDelete.remove(f);
					}
				}
				else if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			}
		}
		filesToDelete.clear();
	}

	public List<File> getFilesToDelete() {
		return filesToDelete;
	}

	public void setFilesToDelete(List<File> filesToDel) {
		this.filesToDelete = filesToDel;
	}

	@DefineValidationRule
	public static class FlexoIDMustBeUnique extends ValidationRule<FlexoIDMustBeUnique, FlexoProject> {

		/**
		 * @param objectType
		 * @param ruleName
		 */
		public FlexoIDMustBeUnique() {
			super(FlexoProject.class, "flexo_id_must_be_unique");
		}

		/**
		 * Overrides applyValidation
		 * 
		 * @see org.openflexo.model.validation.ValidationRule#applyValidation(org.openflexo.model.validation.Validable)
		 */
		@Override
		public ValidationIssue<FlexoIDMustBeUnique, FlexoProject> applyValidation(FlexoProject object) {
			List<FlexoProjectObject> badObjects = object.getObjectIDManager().checkProject(true);
			if (badObjects.size() > 0) {
				DuplicateObjectIDIssue issues = new DuplicateObjectIDIssue(object);
				// TODO FD pour SG pourquoi obj pas utilisé dans la boucle ?
				for (FlexoProjectObject obj : badObjects) {
					issues.addToContainedIssues(new InformationIssue<FlexoIDMustBeUnique, FlexoProject>(object,
							"identifier_of_($object.fullyQualifiedName)_was_duplicated_and_reset_to_($object.flexoID)"));
				}
				return issues;
			}
			else {
				return new InformationIssue<FlexoIDMustBeUnique, FlexoProject>(object, "no_duplicated_identifiers_found");
			}
		}

		public static class DuplicateObjectIDIssue extends CompoundIssue<FlexoIDMustBeUnique, FlexoProject> {

			public DuplicateObjectIDIssue(FlexoProject anObject) {
				super(anObject);
			}

		}
	}

	public FlexoVersion getProjectVersion() {
		if (getProjectData() != null) {
			if (getProjectData().getProjectVersion() == null) {
				getProjectData().setProjectVersion(new FlexoVersion("0.1"));
			}
			return getProjectData().getProjectVersion();
		}
		else {
			return null;
		}
	}

	public void setProjectVersion(FlexoVersion projectVersion) {
		if (getProjectData() != null) {
			getProjectData().setProjectVersion(projectVersion);
		}
	}

	public String getProjectURI() {
		return getURI();
	}

	public void setProjectURI(String projectURI) {
		setURI(projectURI);
	}

	public String getURI() {
		if (getProjectData() != null) {
			if (getProjectData().getURI() == null) {
				Date currentDate = new Date();
				String projectURI = BASE_PROJECT_URI + "/" + (1900 + currentDate.getYear()) + "/" + (currentDate.getMonth() + 1) + "/"
						+ getProjectName() + "_" + System.currentTimeMillis();
				getProjectData().setURI(projectURI);
			}
			return getProjectData().getURI();
		}
		else {
			return null;
		}
	}

	public void setURI(String projectURI) {
		if (getProjectData() != null) {
			getProjectData().setURI(projectURI);
		}
	}

	@Override
	public String getDescription() {
		if (getProjectData() != null) {
			return getProjectData().getDescription();
		}
		else {
			return null;
		}
	}

	@Override
	public void setDescription(String description) {
		if (getProjectData() != null) {
			getProjectData().setDescription(description);
		}
	}

	public Date getCreationDate() {
		if (getProjectData() != null) {
			if (getProjectData().getCreationDate() == null) {
				getProjectData().setCreationDate(new Date());
			}
			return getProjectData().getCreationDate();
		}
		else {
			return null;
		}
	}

	public void setCreationDate(Date creationDate) {
		if (getProjectData() != null) {
			getProjectData().setCreationDate(creationDate);
		}
	}

	public String getCreationDateAsString() {
		if (getCreationDate() != null) {
			return new SimpleDateFormat("dd/MM HH:mm:ss").format(getCreationDate());
		}
		return getLocales().localizedForKey("unknown");
	}

	public String getCreationUserId() {
		if (getProjectData() != null) {
			return getProjectData().getCreationUserId();
		}
		else {
			return null;
		}
	}

	public void setCreationUserId(String creationUserId) {
		if (getProjectData() != null) {
			getProjectData().setCreationUserId(creationUserId);
		}
	}

	@Override
	public String toString() {
		return "PROJECT-" + getDisplayName() + " ID=" + getID();
	}

	private ProjectDataResource projectDataResource;

	private boolean projectDataResourceIsLoading = false;

	public ProjectDataResource getProjectDataResource() {
		if (projectDataResource == null && !projectDataResourceIsLoading) {
			projectDataResourceIsLoading = true;
			projectDataResource = ProjectDataResourceImpl.makeProjectDataResource(this);
			projectDataResourceIsLoading = false;
		}
		return projectDataResource;
	}

	public ProjectData getProjectData() {
		if (getProjectDataResource() != null) {
			return getProjectDataResource().getProjectData();
		}
		return null;
	}

	public boolean importsProject(FlexoProject project) {
		return project != null && importsProjectWithURI(project.getProjectURI());
	}

	public boolean importsProjectWithURI(String projectURI) {
		return getProjectData() != null && getProjectData().getProjectReferenceWithURI(projectURI, true) != null;
	}

	public FlexoObjectIDManager getObjectIDManager() {
		if (objectIDManager == null) {
			objectIDManager = new FlexoObjectIDManager(this);
		}
		return objectIDManager;
	}

	// TODO: Should be refactored with injectors
	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public FlexoVersion getVersion() {
		return version;
	}

	public void setVersion(FlexoVersion version) {
		if (version != null) {
			version = new FlexoVersion(version.toString().replace("~", ""));
		}
		FlexoVersion old = this.version;
		this.version = version;
		setChanged();
		notifyObservers(new DataModification(VERSION, old, version));
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		long old = this.revision;
		this.revision = revision;
		setChanged();
		notifyObservers(new DataModification(REVISION, old, revision));
	}

	public String canImportProject(FlexoProject project) {
		if (getProjectData() == null) {
			return null;
		}
		else {
			return getProjectData().canImportProject(project);
		}
	}

	public boolean hasImportedProjects() {
		return getProjectData() != null && getProjectData().getImportedProjects().size() > 0;
	}

	public List<FlexoProjectReference> getResolvedProjectReferences() {
		List<FlexoProjectReference> refs = new ArrayList<FlexoProjectReference>();
		appendResolvedReferences(this, refs);
		return refs;
	}

	private void appendResolvedReferences(FlexoProject project, List<FlexoProjectReference> refs) {
		if (project.getProjectData() != null) {
			for (FlexoProjectReference ref : project.getProjectData().getImportedProjects()) {
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
			for (FlexoProjectReference ref : project.getProjectData().getImportedProjects()) {
				if (ref.getReferredProject() != null) {
					appendResolvedReferences(ref.getReferredProject(), refs);
				}
			}
		}
	}

	public List<FlexoObjectReference> getObjectReferences() {
		return objectReferences;
	}

	public void addToObjectReferences(FlexoObjectReference<?> objectReference) {
		objectReferences.add(objectReference);
	}

	public void removeObjectReferences(FlexoObjectReference<?> objectReference) {
		objectReferences.remove(objectReference);
	}

	public boolean areAllImportedProjectsLoaded() {
		return areAllImportedProjectsLoaded(this);
	}

	private static boolean areAllImportedProjectsLoaded(FlexoProject project) {
		if (project.getProjectData() == null) {
			return true;
		}
		for (FlexoProjectReference ref : project.getProjectData().getImportedProjects()) {
			if (ref.getReferredProject() == null) {
				return false;
			}
			else if (!ref.getReferredProject().areAllImportedProjectsLoaded()) {
				return false;
			}
		}
		return true;
	}

	public FlexoProject loadProjectReference(FlexoProjectReference reference, boolean silentlyOnly) {
		if (projectReferenceLoader != null) {
			FlexoProject loadProject = projectReferenceLoader.loadProject(reference, silentlyOnly);
			if (loadProject != null) {
				setChanged();
				notifyObservers(new ImportedProjectLoaded(loadProject));
			}
			return loadProject;
		}
		return null;
	}

	public static File getProjectSpecificModelsDirectory(FlexoProject project) {
		File returned = new File(project.getProjectDirectory(), "Models");
		returned.mkdirs();
		return returned;
	}

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

	public void setExternalRepositories(List<ProjectExternalRepository> externalRepositories) {
		_externalRepositories = externalRepositories;
		repositoriesCache.clear();
		if (externalRepositories != null) {
			for (ProjectExternalRepository projectExternalRepository : externalRepositories) {
				repositoriesCache.put(projectExternalRepository.getIdentifier(), projectExternalRepository);
			}
		}
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

	public boolean hasUnsavedResources() {
		for (FlexoResource<?> r : getAllResources()) {
			if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
				return true;
			}
		}
		return false;
	}

	public File getExpectedViewPointDirectory() {
		return new File(getProjectDirectory(), VIEWPOINT_EXPECTED_DIRECTORY);
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
		return getURI();
	}

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link ViewPoint}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
		List<TechnologyAdapter> returned = new ArrayList<>();
		for (ViewResource vr : getViewLibrary().getAllResources()) {
			for (TechnologyAdapter ta : vr.getView().getRequiredTechnologyAdapters()) {
				if (!returned.contains(ta)) {
					returned.add(ta);
				}
			}
		}
		return returned;
	}

	@Override
	public String getDisplayableName() {
		return getName();
	}
}
