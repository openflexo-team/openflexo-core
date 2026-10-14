/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter.FSBasedResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;

/**
 * Provides a JUnit 4 generic environment with a {@link FlexoProject} for testing purposes<br>
 * Note that we exclusively work on file system
 * 
 * @see OpenflexoTestCase
 */
public abstract class OpenflexoProjectAtRunTimeTestCase extends OpenflexoTestCase {

	/**
	 * !!!!! IMPORTANT !!!!!<br>
	 * Do not forget to set back this flag to true when committing into a production environment
	 */
	public static final boolean DELETE_PROJECT_AFTER_TEST_EXECUTION = true;

	private static final Logger logger = FlexoLogger.getLogger(OpenflexoProjectAtRunTimeTestCase.class.getPackage().getName());

	protected static FlexoEditor _editor;
	protected static FlexoProject<File> _project;
	protected static File _projectDirectory;
	protected static String _projectIdentifier;

	static {
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.WARNING, null);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClass() {
		if (DELETE_PROJECT_AFTER_TEST_EXECUTION) {
			deleteProject();
			deleteTestResourceCenters();
		}
		unloadServiceManager();
	}

	protected static void deleteProject() {
		if (_project != null) {
			_project.close();
		}
		if (DELETE_PROJECT_AFTER_TEST_EXECUTION) {
			if (_projectDirectory != null) {
				FileUtils.deleteDir(_projectDirectory);
			}
		}
		_editor = null;
		_projectDirectory = null;
		_project = null;
		_projectIdentifier = null;
	}

	@Override
	public File getResource(String resourceRelativeName) {
		File retval = new File("src/test/resources", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		retval = new File("../flexofoundation/src/test/resources", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		retval = new File("tmp/tests/FlexoResources/", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Could not find resource " + resourceRelativeName);
		}
		return null;
	}

	protected FlexoEditor createStandaloneProject(String projectName) {
		if (serviceManager == null) {
			serviceManager = instanciateTestServiceManager();
		}
		return createStandaloneProject(projectName, null, serviceManager);
	}

	protected FlexoEditor createStandaloneProject(String projectName, Class<? extends ProjectNature> projectNatureClass) {
		if (serviceManager == null) {
			serviceManager = instanciateTestServiceManager();
		}
		return createStandaloneProject(projectName, projectNatureClass, serviceManager);
	}

	protected FlexoEditor createStandaloneProject(String projectName, Class<? extends ProjectNature> projectNatureClass,
			FlexoServiceManager serviceManager) {
		FlexoLoggingManager.forceInitialize(-1, true, null, Level.INFO, null);
		try {
			File tempFile = File.createTempFile(projectName, "");
			_projectDirectory = new File(tempFile.getParentFile(), tempFile.getName() + ".prj");
			tempFile.delete();
		} catch (IOException e) {
			fail();
		}
		logger.info("Project directory: " + _projectDirectory.getAbsolutePath());
		_projectIdentifier = _projectDirectory.getName().substring(0, _projectDirectory.getName().length() - 4);
		logger.info("Project identifier: " + _projectIdentifier);

		FlexoEditor reply;
		try {
			reply = serviceManager.getProjectLoaderService().newStandaloneProject(_projectDirectory, projectNatureClass);
		} catch (ProjectInitializerException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
		logger.info("Project has been SUCCESSFULLY created");
		try {
			// reply.getProject().setProjectName(_projectIdentifier/* projectName */);
			reply.getProject().saveModifiedResources();
			reply.getProject().save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
			fail();
		}
		_editor = reply;
		_project = (FlexoProject<File>) _editor.getProject();
		return reply;
	}

	protected FlexoEditor createProjectInResourceCenter(String projectName, FlexoResourceCenter<File> rc) {
		return createProjectInResourceCenter(projectName, null, rc);
	}

	protected FlexoEditor createProjectInResourceCenter(String projectName, Class<? extends ProjectNature> projectNatureClass,
			FlexoResourceCenter<File> rc) {
		if (serviceManager == null) {
			serviceManager = instanciateTestServiceManager();
		}
		return createProjectInResourceCenter(projectName, projectNatureClass, serviceManager, rc);
	}

	protected static FlexoResourceCenterService getNewResourceCenter(String name) {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(FSBasedResourceCenterEntry.class);
			FSBasedResourceCenterEntry<?> entry = factory.newInstance(FSBasedResourceCenterEntry.class);
			entry.setDirectory(FileUtils.createTempDirectory(name, "ResourceCenter"));
			List<ResourceCenterEntry<?>> rcList = new ArrayList<>();
			rcList.add(entry);
			return DefaultResourceCenterService.getNewInstance(rcList, false, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

	protected FlexoEditor createProjectInResourceCenter(String projectName, Class<? extends ProjectNature> projectNatureClass,
			FlexoServiceManager serviceManager, FlexoResourceCenter<File> rc) {
		FlexoLoggingManager.forceInitialize(-1, true, null, Level.INFO, null);
		FlexoEditor reply;
		try {
			reply = serviceManager.getProjectLoaderService().newProjectInResourceCenter(projectName, (RepositoryFolder) rc.getRootFolder(),
					projectNatureClass);
		} catch (ProjectInitializerException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
		logger.info("Project has been SUCCESSFULLY created");
		try {
			reply.getProject().saveModifiedResources();
			reply.getProject().save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
			fail();
		}
		_editor = reply;
		_project = (FlexoProject<File>) _editor.getProject();
		return reply;
	}

	protected static FlexoServiceManager getFlexoServiceManager() {
		return serviceManager;
	}

	protected void saveProject(FlexoProject<?> prj) {
		try {
			prj.save();
		} catch (SaveResourceException e) {
			fail("Cannot save project");
		}
	}

	/**
	 * Load project denoted by supplied project directory
	 * 
	 * @param projectDirectory
	 * @return
	 */
	protected FlexoEditor loadProject(File projectDirectory) {

		try {
			FlexoEditor anEditor = null;

			try {
				anEditor = serviceManager.getProjectLoaderService().loadProject(projectDirectory);
			} catch (ProjectInitializerException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return null;
			}

			_project = (FlexoProject<File>) anEditor.getProject();
			return anEditor;
		} catch (ProjectLoadingCancelledException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

	protected FlexoEditor loadProject(FlexoProjectResource<?> projectResource) {
		if (projectResource.getIODelegate().getSerializationArtefact() instanceof File) {

			File projectDirectory = ((File) projectResource.getIODelegate().getSerializationArtefact()).getParentFile();
			System.out.println("projectDirectory=" + projectDirectory);

			return loadProject(projectDirectory);
		}
		return null;
	}

	/**
	 * Close supplied project, and reload using the same {@link FlexoServiceManager}
	 * 
	 * @param prjDir
	 * @return
	 */
	protected FlexoEditor reloadProject(FlexoProject<File> projectToReload) {

		File oldDirectory = projectToReload.getProjectDirectory();

		// Close the project first
		projectToReload.close();

		try {
			FlexoEditor anEditor = null;

			try {
				anEditor = serviceManager.getProjectLoaderService().loadProject(oldDirectory);
			} catch (ProjectInitializerException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return null;
			}

			_project = (FlexoProject<File>) anEditor.getProject();
			return anEditor;
		} catch (ProjectLoadingCancelledException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

}
