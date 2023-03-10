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
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import javax.naming.InvalidNameException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.CompilationUnitRepository;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DuplicateExternalRepositoryNameException;
import org.openflexo.foundation.resource.ExternalRepositorySet;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceFactory;
import org.openflexo.foundation.resource.ImportedProjectLoaded;
import org.openflexo.foundation.resource.ProjectExternalRepository;
import org.openflexo.foundation.resource.ProjectImportLoopException;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.resource.ResourceRepositoryImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.foundation.validation.FlexoProjectValidationModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.DirectoryWatcher;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FileUtils.CopyStrategy;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.ZipUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Base implementation for a {@link FlexoProject}
 * 
 * @author sylvain
 * @param <I>
 *            type of serialization artefact this project stores
 */
public abstract class FlexoProjectImpl<I> extends ResourceRepositoryImpl<FlexoResource<?>, I> implements FlexoProject<I> {

	protected static final Logger logger = Logger.getLogger(FlexoProjectImpl.class.getPackage().getName());

	public static final String RESOURCES = "resources";

	public static final String PROJECT_DIRECTORY = "projectDirectory";
	public static final String PROJECT_DATA = "projectData";
	public static final String REVISION = "revision";
	public static final String VERSION = "version";

	private final List<FlexoObjectReference<?>> objectReferences = new ArrayList<>();

	private boolean lastUniqueIDHasBeenSet = false;
	private long lastID = Integer.MIN_VALUE;

	protected I projectDirectory;

	private FlexoProjectValidationModel projectValidationModel;

	private static int ID = 0;
	private final int id;
	private boolean holdObjectRegistration = false;

	private ProjectLoadingHandler loadingHandler;
	private FlexoProjectResource<I> resource;
	private FlexoResourceCenterService rcService;

	private List<FlexoEditor> editors;

	private FlexoProjectReferenceLoader projectReferenceLoader;

	private final List<ProjectExternalRepository<?>> _externalRepositories;
	private final Map<String, ProjectExternalRepository<?>> repositoriesCache;

	public static interface FlexoProjectReferenceLoader extends FlexoService {

		/**
		 * 
		 * @param reference
		 *            the referense to load
		 * @param silentlyOnly
		 *            if true, the loading should be silent. This flag is typically meant for interactive loaders.
		 * @return
		 */
		public FlexoProject<?> loadProject(FlexoProjectReference<?> reference, boolean silentlyOnly);

	}

	protected FlexoProjectImpl() {
		super(null, null);

		editors = new Vector<>();
		synchronized (FlexoProjectImpl.class) {
			id = ID++;
		}
		logger.info("Create new project, ID=" + id);
		_externalRepositories = new ArrayList<>();
		repositoriesCache = new Hashtable<>();

	}

	@Override
	public String getProjectName() {
		if (getResource() != null) {
			return getResource().getName();
		}
		return null;
	}

	@Override
	public String getName() {
		return getProjectName();
	}

	public String getDisplayName() {
		return getProjectName();
	}

	@Override
	public String getPrefix() {
		String prefix = getProjectName();
		if (prefix.length() > 2) {
			prefix = prefix.substring(0, 3);
		}
		prefix = prefix.toUpperCase();
		return JavaUtils.getJavaName(prefix);
	}

	@Override
	public I getProjectDirectory() {
		if (getResource() != null && getResource().getIODelegate() != null) {
			return getContainer((I) getResource().getIODelegate().getSerializationArtefact());
		}
		return null;
	}

	public int getID() {
		return id;
	}

	@Override
	public String getProjectURI() {
		if (getResource() != null) {
			return getResource().getURI();
		}
		return (String) performSuperGetter(PROJECT_URI_KEY);
	}

	@Override
	public void setProjectURI(String projectURI) {

		performSuperSetter(PROJECT_URI_KEY, projectURI);
		if (getResource() != null) {
			getResource().setURI(projectURI);
		}
		if (getDelegateResourceCenter() != null) {
			getDelegateResourceCenter().setDefaultBaseURI(projectURI);
		}
	}

	@Override
	public I getBaseArtefact() {
		return getProjectDirectory();
	}

	@Override
	public Resource getBaseArtefactAsResource() {
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().getBaseArtefactAsResource();
		}
		return null;
	}

	@Override
	public Class<? extends I> getSerializationArtefactClass() {
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().getSerializationArtefactClass();
		}
		return null;
	}

	@Override
	public RepositoryFolder<FlexoResource<?>, I> getRootFolder() {
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().getRootFolder();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public FlexoProjectResource<I> getProjectResource() {
		return (FlexoProjectResource<I>) (FlexoResource) getResource();
	}

	@Override
	public FlexoResource<FlexoProject<I>> getResource() {
		return resource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setResource(FlexoResource<FlexoProject<I>> resource) {
		// We first take the URI value as found in serialized XML
		String projectURI = getProjectURI();
		// We set the resource
		this.resource = (FlexoProjectResource<I>) (FlexoResource) resource;
		// Then we give serialized project URI to the resource
		if (resource != null) {
			resource.setURI(projectURI);
		}
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

	/**
	 * Overrides setChanged
	 */
	@Override
	public synchronized void setChanged() {
		super.setChanged();
	}

	/**
	 * Save this project
	 * 
	 */
	@Override
	public void save() throws SaveResourceException {
		logger.info("Saving project...");

		getResource().save();

		saveModifiedResources(true);

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
	public void saveAsZipFile(File zipFile, boolean lightenProject, boolean copyCVSFiles) throws SaveResourceException {
		try {
			FileUtils.createNewFile(zipFile);
			File tempProjectDirectory = FileUtils
					.createTempDirectory(getProjectName().length() > 2 ? getProjectName() : "FlexoProject-" + getProjectName(), "");
			tempProjectDirectory = new File(tempProjectDirectory, getProjectName());
			if (lightenProject) {
				// replaceBigJarsWithEmtpyJars(progress, tempProjectDirectory);
				// removeScreenshots(progress, tempProjectDirectory);
				removeBackupFiles(tempProjectDirectory);
			}
			// progress.setProgress(getLocales().localizedForKey("zipping_project"));
			ZipUtils.makeZip(zipFile, tempProjectDirectory, null,
					lightenProject ? Deflater.BEST_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SaveResourceException(null, e);
		}
	}

	private static void removeBackupFiles(File tempProjectDirectory) {
		List<File> tildes = FileUtils.listFilesRecursively(tempProjectDirectory, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("~");
			}
		});
		// progress.setProgress(getLocales().localizedForKey("removing_backups"));
		// progress.resetSecondaryProgress(tildes.size());
		for (File tilde : tildes)
			// progress.setSecondaryProgress(getLocales().localizedForKey("removing") + " " + tilde.getName());
			tilde.delete();
	}

	/**
	 * Save this project using FlexoEditingContext scheme Additionnaly save all known resources related to this project
	 * 
	 * Overrides
	 * 
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 * @see org.openflexo.foundation.rm.FlexoResourceData#save()
	 */
	@Override
	public synchronized void saveModifiedResources() throws SaveResourceException {
		saveModifiedResources(true);
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
	@Override
	public synchronized void saveModifiedResources(boolean clearModifiedStatus) throws SaveResourceException {
		try {
			_saveModifiedResources(clearModifiedStatus);
		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Exception occurred during saving " + getDisplayName() + ". Trying to repair and save again");
			}
			repairProject();
			_saveModifiedResources(clearModifiedStatus);
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
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().getAllResources();
		}
		return super.getAllResources();
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

	private void _saveModifiedResources(boolean clearModifiedStatus) throws SaveResourceException
	/*SaveXMLResourceException, SaveResourcePermissionDeniedException*/ {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Saving modified resources of project...");
		}
		List<FlexoResource<?>> unsaved = getUnsavedResources();
		// progress.setProgress(getLocales().localizedForKey("saving_modified_resources"));
		// progress.resetSecondaryProgress(unsaved.size() + 1);
		boolean resourceSaved = false;
		try {
			for (FlexoResource<?> r : unsaved)
				// progress.setSecondaryProgress(getLocales().localizedForKey("saving_resource_") + r);
				r.save();
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
	 * Returns owner {@link FlexoResourceCenter} Overrides super {@link #getResourceCenter()} by returning {@link FlexoResourceCenter} of
	 * related resource (where the project resource is defined)
	 */
	@Override
	public FlexoResourceCenter<I> getResourceCenter() {
		if (getResource() != null) {
			return (FlexoResourceCenter<I>) getResource().getResourceCenter();
		}
		return null;
	}

	/**
	 * Return {@link FlexoResourceCenter} acting as a delegate for the {@link FlexoProject}
	 */
	@Override
	public FlexoResourceCenter<I> getDelegateResourceCenter() {
		if (getResource() != null) {
			return (getProjectResource()).getDelegateResourceCenter();
		}
		return null;
	}

	/**
	 * When true, indicates that this {@link FlexoProject} has no parent {@link FlexoResourceCenter}
	 * 
	 * @return
	 */
	@Override
	public boolean isStandAlone() {
		return (getProjectResource() != null
				&& getProjectResource().getResourceCenter() == getProjectResource().getDelegateResourceCenter());
	}

	public DirectoryWatcher getDirectoryWatcher() {
		if (getDelegateResourceCenter() instanceof FileSystemBasedResourceCenter) {
			return ((FileSystemBasedResourceCenter) getDelegateResourceCenter()).getDirectoryWatcher();
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

		if (getProjectResource() != null) {
			getProjectResource().setClosing();
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

		if (getProjectResource() != null) {
			getProjectResource().setClosed();
		}

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
	@Override
	@NotificationUnsafe
	public final boolean hasNature(Class<? extends ProjectNature> projectNatureClass) {
		for (ProjectNature<?> n : getProjectNatures()) {
			if (projectNatureClass.isAssignableFrom(n.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return boolean indicating if this project might be interpreted according to this project nature (supplied as string representing full
	 * class name)
	 * 
	 * @param projectNature
	 * @return
	 */
	@Override
	@NotificationUnsafe
	public final boolean hasNature(String projectNatureClassName) {

		Class<? extends ProjectNature> projectNatureClass;
		try {
			projectNatureClass = (Class<? extends ProjectNature>) Class.forName(projectNatureClassName);
			return hasNature(projectNatureClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Return nature of supplied class when existing.<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@Override
	@NotificationUnsafe
	public <N extends ProjectNature<N>> N getNature(Class<N> projectNatureClass) {
		for (ProjectNature<?> n : getProjectNatures()) {
			if (projectNatureClass.isAssignableFrom(n.getClass())) {
				return (N) n;
			}
		}
		return null;
	}

	/**
	 * Return nature of supplied class when existing.<br>
	 * 
	 * @param projectNatureClassName
	 * @return
	 */
	@Override
	@NotificationUnsafe
	public <N extends ProjectNature<N>> N getNature(String projectNatureClassName) {

		Class<? extends ProjectNature> projectNatureClass;
		try {
			projectNatureClass = (Class<? extends ProjectNature>) Class.forName(projectNatureClassName);
			return (N) getNature(projectNatureClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
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
	/*public final void givesNature(ProjectNature<?, ?> projectNature, FlexoEditor editor) {
		if (!hasNature(projectNature)) {
			projectNature.givesNature(this, editor);
		}
	}*/

	/**
	 * Return project wrapper object representing this project according to supplied nature
	 * 
	 * @param projectNature
	 * @return
	 */
	/*@Override
	public final <N extends ProjectNature<N, P>, P extends ProjectWrapper<N>> P asNature(N projectNature) {
		return projectNature.getProjectWrapper(this);
	}*/

	/**
	 * Return project wrapper object representing this project according to supplied nature
	 * 
	 * @param projectNature
	 * @return
	 */
	/*@Override
	public final ProjectWrapper<?> asNature(String projectNatureClassName) {
		if (!closed) {
			ProjectNature<?, ?> projectNature = getServiceManager().getProjectNatureService().getProjectNature(projectNatureClassName);
			if (projectNature != null && hasNature(projectNature)) {
				return projectNature.getProjectWrapper(this);
			}
		}
	
		// System.out.println("Could not lookup nature " + projectNatureClassName);
		return null;
	}*/

	// ======================================================================
	// ============================= Validation =============================
	// ======================================================================

	@Override
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

	@Override
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
	public boolean importsProject(FlexoProject<?> project) {
		return project != null && importsProjectWithURI(project.getProjectURI());
	}

	@Override
	public boolean importsProjectWithURI(String projectURI) {
		return getProjectReferenceWithURI(projectURI, true) != null;
	}

	public boolean hasImportedProjects() {
		return getImportedProjects().size() > 0;
	}

	public List<FlexoProjectReference<?>> getResolvedProjectReferences() {
		List<FlexoProjectReference<?>> refs = new ArrayList<>();
		appendResolvedReferences(this, refs);
		return refs;
	}

	private void appendResolvedReferences(FlexoProject<?> project, List<FlexoProjectReference<?>> refs) {
		if (project != null) {
			for (FlexoProjectReference<?> ref : project.getImportedProjects()) {
				boolean alreadyAdded = false;
				for (FlexoProjectReference<?> addedRef : refs) {
					if (addedRef.getURI().equals(ref.getURI())) {
						alreadyAdded = true;
						break;
					}
				}
				if (!alreadyAdded) {
					refs.add(ref);
				}
			}
			for (FlexoProjectReference<?> ref : project.getImportedProjects()) {
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
		for (FlexoProjectReference<?> ref : project.getImportedProjects()) {
			if (ref.getReferencedProject() == null) {
				return false;
			}
			else if (!ref.getReferencedProject().areAllImportedProjectsLoaded()) {
				return false;
			}
		}
		return true;
	}

	public FlexoProject<?> loadProjectReference(FlexoProjectReference<?> reference, boolean silentlyOnly) {
		if (projectReferenceLoader != null) {
			FlexoProject<?> loadProject = projectReferenceLoader.loadProject(reference, silentlyOnly);
			if (loadProject != null) {
				setChanged();
				notifyObservers(new ImportedProjectLoaded<>(loadProject));
			}
			return loadProject;
		}
		return null;
	}

	public ProjectExternalRepository<?> createExternalRepositoryWithKey(String identifier) throws DuplicateExternalRepositoryNameException {
		for (ProjectExternalRepository<?> rep : _externalRepositories) {
			if (rep.getIdentifier().equals(identifier)) {
				throw new DuplicateExternalRepositoryNameException(null, identifier);
			}
		}
		ProjectExternalRepository<?> returned = new ProjectExternalRepository<>(this, identifier);
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

	public ProjectExternalRepository<?> getExternalRepositoryWithKey(String identifier) {
		for (ProjectExternalRepository<?> rep : _externalRepositories) {
			if (rep.getIdentifier().equals(identifier)) {
				return rep;
			}
		}
		return null;
	}

	public ProjectExternalRepository<?> getExternalRepositoryWithDirectory(File directory) {
		if (directory == null) {
			return null;
		}
		for (ProjectExternalRepository<?> rep : _externalRepositories) {
			if (rep.getDirectory() != null && rep.getDirectory().equals(directory)) {
				return rep;
			}
		}
		return null;
	}

	public ProjectExternalRepository<?> setDirectoryForRepositoryName(String identifier, File directory) {
		ProjectExternalRepository<?> returned = getExternalRepositoryWithKey(identifier);
		if (returned == null) {
			returned = new ProjectExternalRepository<>(this, identifier);
			addToExternalRepositories(returned);
		}
		if (returned.getDirectory() == null || !returned.getDirectory().equals(directory)) {
			returned.setDirectory(directory);
			setChanged();
			notifyObservers(new ExternalRepositorySet(returned));
		}
		return returned;
	}

	public List<ProjectExternalRepository<?>> getExternalRepositories() {
		return _externalRepositories;
	}

	public void addToExternalRepositories(ProjectExternalRepository<?> anExternalRepository) {
		_externalRepositories.add(anExternalRepository);
		repositoriesCache.put(anExternalRepository.getIdentifier(), anExternalRepository);
		setChanged();
	}

	public void removeFromExternalRepositories(ProjectExternalRepository<?> anExternalRepository) {
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
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion) throws Exception {

	}

	@Override
	public void update() throws IOException {

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
	public List<TechnologyAdapter<?>> getRequiredTechnologyAdapters() {
		List<TechnologyAdapter<?>> returned = new ArrayList<>();
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
	public FlexoProjectReference<?> getProjectReferenceWithURI(String projectURI, boolean searchRecursively) {
		FlexoProjectReference<?> ref = getProjectReferenceWithURI(projectURI);
		if (ref != null) {
			return ref;
		}
		if (searchRecursively) {
			for (FlexoProjectReference<?> ref2 : getImportedProjects()) {
				if (ref2.getReferencedProject().getProjectReferenceWithURI(projectURI, searchRecursively) != null) {
					return ref2.getReferencedProject().getProjectReferenceWithURI(projectURI, searchRecursively);
				}
			}
		}
		return null;
	}

	@Override
	public void addToImportedProjects(FlexoProjectReference<?> projectReference) throws ProjectImportLoopException {
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
	public void removeFromImportedProjects(FlexoProjectReference<?> projectReference) {
		// TODO: remove dependency toward external resource.
		// getProject().getFlexoRMResource().removeFromDependentResources(projectReference.getReferredProject().getFlexoRMResource());
		performSuperRemover(IMPORTED_PROJECTS, projectReference);
	}

	@Override
	public String canImportProject(FlexoProject<?> project) {
		if (project.getProjectURI().equals(getProject().getProjectURI())) {
			return getLocales().localizedForKey("cannot_import_itself");
		}
		if (getProjectReferenceWithURI(project.getProjectURI()) != null) {
			return getLocales().localizedForKey("project_already_imported");
		}
		return null;
	}

	@Override
	public void removeFromImportedProjects(FlexoProject<?> project) {
		for (FlexoProjectReference<?> ref : new ArrayList<>(getImportedProjects())) {
			if (ref.getReferencedProject() == project) {
				removeFromImportedProjects(ref);
				break;
			}
		}
	}

	@Override
	public FlexoProjectFactory getModelFactory() {
		return ((FlexoProjectResource<I>) getResource()).getFactory();
	}

	@Override
	public void setDefaultBaseURI(String defaultBaseURI) {
		setProjectURI(defaultBaseURI);
	}

	// Following code is delegated to effective resource center

	@Override
	public FMLRTVirtualModelInstanceRepository<I> getVirtualModelInstanceRepository() {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getVirtualModelInstanceRepository();
	}

	@Override
	public CompilationUnitRepository<I> getVirtualModelRepository() {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getVirtualModelRepository();
	}

	@Override
	public <R extends FlexoResource<?>> String getDefaultResourceURI(R resource) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getDefaultResourceURI(resource);
	}

	@Override
	public FlexoResource<?> getResource(String resourceURI) {
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().getResource(resourceURI);
		}
		return super.getResource(resourceURI);
	}

	@Override
	public <R extends FlexoResource<?>> R getResource(I resourceArtefact, Class<R> resourceClass) {

		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getResource(resourceArtefact, resourceClass);
	}

	@Override
	public <T extends ResourceData<T>> FlexoResource<T> retrieveResource(String uri, FlexoVersion version, Class<T> type) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().retrieveResource(uri, version, type);
	}

	@Override
	public FlexoResource<?> retrieveResource(String uri) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().retrieveResource(uri);
	}

	@Override
	public <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(String uri, Class<T> type) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().retrieveResource(uri, type);
	}

	@Override
	public void updateWith(Object obj) {
		if (getDelegateResourceCenter() == null) {
			return;
		}
		getDelegateResourceCenter().updateWith(obj);
	}

	@Override
	public boolean isIgnorable(I artefact, TechnologyAdapter<?> technologyAdapter) {
		if (getDelegateResourceCenter() == null) {
			return true;
		}
		return getDelegateResourceCenter().isIgnorable(artefact, technologyAdapter);
	}

	@Override
	public <R extends ResourceRepository<?, I>> R retrieveRepository(Class<? extends R> repositoryType,
			TechnologyAdapter<?> technologyAdapter) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().retrieveRepository(repositoryType, technologyAdapter);
	}

	@Override
	public <R extends ResourceRepository<?, I>> void registerRepository(R repository, Class<? extends R> repositoryType,
			TechnologyAdapter<?> technologyAdapter) {
		if (getDelegateResourceCenter() == null) {
			return;
		}
		getDelegateResourceCenter().registerRepository(repository, repositoryType, technologyAdapter);
	}

	@Override
	public Collection<? extends ResourceRepository<?, I>> getRegistedRepositories(TechnologyAdapter<?> technologyAdapter,
			boolean considerEmptyRepositories) {
		if (getDelegateResourceCenter() == null) {
			return Collections.emptyList();
		}
		return getDelegateResourceCenter().getRegistedRepositories(technologyAdapter, considerEmptyRepositories);
	}

	@Override
	public ResourceCenterEntry<?> getResourceCenterEntry() {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getResourceCenterEntry();
	}

	@Override
	public String retrieveName(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().retrieveName(serializationArtefact);
	}

	@Override
	public I rename(I serializationArtefact, String newName) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().rename(serializationArtefact, newName);
	}

	@Override
	public void registerResource(FlexoResource<?> resource, I serializationArtefact) {
		// System.out.println("********** on enregistre " + resource + " with " + serializationArtefact);
		// System.out.println("delRC=" + getDelegateResourceCenter());
		// System.out.println("URI=" + resource.getURI());
		if (getDelegateResourceCenter() == null) {
			return;
		}
		getDelegateResourceCenter().registerResource(resource, serializationArtefact);
		/*System.out.println("j'ai maintenant");
		for (FlexoResource<?> r : getAllResources()) {
			System.out.println(" > " + r.getURI());
		}*/
		/*if (getDelegateResourceCenter() instanceof FileSystemBasedResourceCenterImpl) {
			Map<File, FlexoResource<?>> registeredResources = ((FileSystemBasedResourceCenterImpl) getDelegateResourceCenter())
					.getRegisteredResources();
			System.out.println("registered:");
			for (File f : registeredResources.keySet()) {
				System.out.println(" > " + f.getAbsolutePath() + " " + registeredResources.get(f));
			}
		}*/
		// System.out.println("et donc: " + getResource(resource.getURI()));
	}

	@Override
	public void unregisterResource(FlexoResource<?> resource, I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return;
		}
		getDelegateResourceCenter().unregisterResource(resource, serializationArtefact);
	}

	@Override
	public FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceFactory<?, ?> resourceFactory) throws IOException {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().makeFlexoIODelegate(serializationArtefact, resourceFactory);
	}

	@Override
	public FlexoIODelegate<I> makeDirectoryBasedFlexoIODelegate(I serializationArtefact, String directoryExtension, String fileExtension,
			FlexoResourceFactory<?, ?> resourceFactory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().makeDirectoryBasedFlexoIODelegate(serializationArtefact, directoryExtension, fileExtension,
				resourceFactory);
	}

	@Override
	public FlexoIODelegate<I> makeDirectoryBasedFlexoIODelegate(I directory, I file, FlexoResourceFactory<?, ?> resourceFactory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().makeDirectoryBasedFlexoIODelegate(directory, file, resourceFactory);
	}

	@Override
	public <R extends FlexoResource<?>> RepositoryFolder<R, I> getRepositoryFolder(FlexoIODelegate<I> ioDelegate,
			ResourceRepository<R, I> resourceRepository) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getRepositoryFolder(ioDelegate, resourceRepository);
	}

	@Override
	public boolean isDirectory(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return false;
		}
		return getDelegateResourceCenter().isDirectory(serializationArtefact);
	}

	@Override
	public XMLRootElementInfo getXMLRootElementInfo(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getXMLRootElementInfo(serializationArtefact);
	}

	@Override
	public XMLRootElementInfo getXMLRootElementInfo(I serializationArtefact, boolean parseFirstLevelElements,
			String firstLevelElementName) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getXMLRootElementInfo(serializationArtefact, parseFirstLevelElements, firstLevelElementName);
	}

	@Override
	public Iterator<I> iterator() {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().iterator();
	}

	@Override
	public Properties getProperties(I directory) throws IOException {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getProperties(directory);
	}

	@Override
	public I delete(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().delete(serializationArtefact);
	}

	@Override
	public void stop() {
		if (getDelegateResourceCenter() == null) {
			return;
		}
		getDelegateResourceCenter().stop();
	}

	@Override
	public boolean canRead(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return false;
		}
		return getDelegateResourceCenter().canRead(serializationArtefact);
	}

	@Override
	public boolean exists(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return false;
		}
		return getDelegateResourceCenter().exists(serializationArtefact);
	}

	@Override
	public I getDirectoryWithRelativePath(String relativePath) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getDirectoryWithRelativePath(relativePath);
	}

	@Override
	public I createDirectory(String name, I parentDirectory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().createDirectory(name, parentDirectory);
	}

	@Override
	public I createEntry(String name, I parentDirectory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().createEntry(name, parentDirectory);
	}

	@Override
	public I getEntry(String name, I parentDirectory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getEntry(name, parentDirectory);
	}

	@Override
	public I getContainer(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getContainer(serializationArtefact);
	}

	@Override
	public List<I> getContents(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getContents(serializationArtefact);
	}

	@Override
	public I getDirectory(String name, I parentDirectory) {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getDirectory(name, parentDirectory);
	}

	@Override
	public List<String> getPathTo(I serializationArtefact) throws IOException {
		if (getDelegateResourceCenter() == null) {
			return null;
		}
		return getDelegateResourceCenter().getPathTo(serializationArtefact);
	}

	@Override
	public FlexoProjectResource<I> getDelegatingProjectResource() {
		return null;
	}

	/**
	 * Sets project which delegates it's FlexoResourceCenter to this<br>
	 * 
	 * @return
	 */
	@Override
	public void setDelegatingProjectResource(FlexoProjectResource<I> delegatingProjectResource) {
		// Not applicable
	}

	@Override
	public boolean containsArtefact(I serializationArtefact) {
		if (getDelegateResourceCenter() == null) {
			return false;
		}
		return getDelegateResourceCenter().containsArtefact(serializationArtefact);
	}

	@Override
	public String relativePath(I serializationArtefact) {
		if (getDelegateResourceCenter() != null) {
			return getDelegateResourceCenter().relativePath(serializationArtefact);
		}
		return null;
	}

	@Override
	public String getDisplayableStatus() {
		return "[uri=\"" + getProjectURI() + "\"] with " + getAllResources().size() + " resources";
	}

}
