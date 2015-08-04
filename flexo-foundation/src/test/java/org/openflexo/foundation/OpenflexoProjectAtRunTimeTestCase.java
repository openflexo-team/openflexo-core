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

package org.openflexo.foundation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;

import org.junit.AfterClass;
import org.openflexo.foundation.FlexoEditor.FlexoEditorFactory;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DirectoryResourceCenter.DirectoryResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;

/**
 * Provides a JUnit 4 generic environment with a {@link FlexoProject} for testing purposes<br>
 */
public abstract class OpenflexoProjectAtRunTimeTestCase extends OpenflexoTestCase {

	private static final Logger logger = FlexoLogger.getLogger(OpenflexoProjectAtRunTimeTestCase.class.getPackage().getName());

	protected static FlexoEditor _editor;
	protected static FlexoProject _project;
	protected static File _projectDirectory;
	protected static String _projectIdentifier;

	static {
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.WARNING, null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClass() {
		deleteProject();
		deleteTestResourceCenters();
		unloadServiceManager();

	}

	protected static void deleteProject() {
		if (_project != null) {
			_project.close();
		}
		if (_projectDirectory != null) {
			FileUtils.deleteDir(_projectDirectory);
		}
		_editor = null;
		_projectDirectory = null;
		_project = null;
	}

	protected static final FlexoEditorFactory EDITOR_FACTORY = new FlexoEditorFactory() {
		@Override
		public DefaultFlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager serviceManager) {
			return new FlexoTestEditor(project, serviceManager);
		}
	};

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
		} else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Could not find resource " + resourceRelativeName);
		}
		return null;
	}

	// TODO: create a project where all those tests don't need a manual import of projects
	// TODO: copy all test VP in tmp dir and work with those VP instead of polling GIT workspace
	/*protected static FlexoServiceManager instanciateTestServiceManager() {
		serviceManager = new DefaultFlexoServiceManager() {

			@Override
			protected FlexoEditor createApplicationEditor() {
				return new FlexoTestEditor(null, this);
			}

			@Override
			protected FlexoResourceCenterService createResourceCenterService() {
				File tempFile;
				try {
					tempFile = File.createTempFile("TestResourceCenter", "");
					File testResourceCenterDirectory = new File(tempFile.getParentFile(), "TestResourceCenter");
					testResourceCenterDirectory.mkdirs();
					FileUtils.copyContentDirToDir(new FileResource("src/test/resources/TestResourceCenter"), testResourceCenterDirectory);
					FlexoResourceCenterService rcService = DefaultResourceCenterService.getNewInstance();
					rcService.addToResourceCenters(resourceCenter = new DirectoryResourceCenter(testResourceCenterDirectory));
					return rcService;
				} catch (IOException e) {
					e.printStackTrace();
					fail();
					return null;
				}

			}
		};
		return serviceManager;
	}*/

	protected FlexoEditor createProject(String projectName) {
		return createProject(projectName, null);
	}

	protected FlexoEditor createProject(String projectName, ProjectNature nature) {
		if (serviceManager == null) {
			serviceManager = instanciateTestServiceManager();
		}
		return createProject(projectName, nature, serviceManager);
	}

	protected static FlexoResourceCenterService getNewResourceCenter(String name) {
		try {
			ModelFactory factory = new ModelFactory(DirectoryResourceCenterEntry.class);
			DirectoryResourceCenterEntry entry = factory.newInstance(DirectoryResourceCenterEntry.class);
			entry.setDirectory(FileUtils.createTempDirectory(name, "ResourceCenter"));
			List<ResourceCenterEntry<?>> rcList = new ArrayList<ResourceCenterEntry<?>>();
			rcList.add(entry);
			return DefaultResourceCenterService.getNewInstance(rcList);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

	protected FlexoEditor createProject(String projectName, ProjectNature nature, FlexoServiceManager serviceManager) {
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
			reply = FlexoProject.newProject(_projectDirectory, nature, EDITOR_FACTORY, serviceManager, null);
		} catch (ProjectInitializerException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
			return null;
		}
		logger.info("Project has been SUCCESSFULLY created");
		try {
			reply.getProject().setProjectName(_projectIdentifier/*projectName*/);
			reply.getProject().saveModifiedResources(null);
		} catch (InvalidNameException e) {
			e.printStackTrace();
			fail();
		} catch (SaveResourceException e) {
			e.printStackTrace();
			fail();
		}
		_editor = reply;
		_project = _editor.getProject();
		return reply;
	}

	protected static FlexoServiceManager getFlexoServiceManager() {
		return serviceManager;
	}

	protected void saveProject(FlexoProject prj) {
		try {
			prj.save();
		} catch (SaveResourceException e) {
			fail("Cannot save project");
		}
	}

	protected FlexoEditor reloadProject(File prjDir) {
		try {
			FlexoEditor anEditor = null;
			assertNotNull(anEditor = FlexoProject.openProject(prjDir, EDITOR_FACTORY,
			/*new DefaultProjectLoadingHandler(),*/serviceManager, null));
			// The next line is really a trouble maker and eventually causes more problems than solutions. FlexoProject can't be renamed on
			// the fly
			// without having a severe impact on many resources and importer projects. I therefore now comment this line which made me lost
			// hundreds of hours
			// _editor.getProject().setProjectName(_editor.getProject().getProjectName() + new Random().nextInt());
			_project = anEditor.getProject();
			return anEditor;
		} catch (ProjectInitializerException e) {
			e.printStackTrace();
			fail();
		} catch (ProjectLoadingCancelledException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

}
