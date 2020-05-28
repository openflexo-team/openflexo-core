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

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test View creation facilities with a ViewPoint created on the fly
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateVirtualModelInstance extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";

	private static VirtualModel newViewPoint;
	private static VirtualModel newVirtualModel;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstance newView;
	private static FMLRTVirtualModelInstance newVirtualModelInstance;
	private static CompilationUnitResource newVirtualModelResource;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Instantiate a ViewPoint with a VirtualModel
	 * 
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException, IOException {

		log("testCreateViewPoint()");

		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();

		newVirtualModelResource = factory.makeTopLevelCompilationUnitResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		newViewPoint = newVirtualModelResource.getLoadedResourceData().getVirtualModel();

		// newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint",
		// "http://openflexo.org/test/TestViewPoint",
		// resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary(), resourceCenter);
		assertNotNull(newViewPoint);
		assertNotNull(newViewPoint.getResource());
		// assertTrue(((VirtualModelResource)
		// newViewPoint.getResource()).getDirectory().exists());
		// assertTrue(((VirtualModelResource)
		// newViewPoint.getResource()).getFile().exists());
		assertTrue(newViewPoint.getResource().getDirectory() != null);
		assertTrue(newViewPoint.getResource().getIODelegate().exists());

		CreateContainedVirtualModel action = CreateContainedVirtualModel.actionType.makeNewAction(newViewPoint.getCompilationUnit(), null,
				editor);
		action.setNewVirtualModelName("TestVirtualModel");
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newVirtualModel = action.getNewVirtualModel();
		// newVirtualModel =
		// VirtualModelImpl.newVirtualModel("TestVirtualModel", newViewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(newVirtualModel.getResource().getDirectory()).exists());
		assertTrue(newVirtualModel.getResource().getIODelegate().exists());

		newViewPoint.getResource().save();
		newVirtualModel.getResource().save();
	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {

		log("testCreateProject()");

		editor = createStandaloneProject("TestProject");
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
	}

	/**
	 * Instantiate in project a View conform to the ViewPoint
	 */
	@Test
	@TestOrder(3)
	public void testCreateView() {

		log("testCreateView()");

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("MyView");
		action.setNewVirtualModelInstanceTitle("Test creation of a new view");
		action.setVirtualModel(newViewPoint);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewVirtualModelInstance();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		try {
			newView.getResource().save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getVirtualModelInstanceRepository().getResource(newView.getURI()));

	}

	/**
	 * Instantiate in project a FMLRTVirtualModelInstance conform to the VirtualModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateVirtualModelInstance() {

		log("testCreateVirtualModelInstance()");

		System.out.println("newView=" + newView);
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		action.setNewVirtualModelInstanceTitle("Test creation of a new FMLRTVirtualModelInstance");
		action.setVirtualModel(newVirtualModel);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newVirtualModelInstance = action.getNewVirtualModelInstance();
		assertNotNull(newVirtualModelInstance);
		assertNotNull(newVirtualModelInstance.getResource());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());

		assertEquals(newVirtualModel, newVirtualModelInstance.getFlexoConcept());
		assertEquals(newVirtualModel, newVirtualModelInstance.getVirtualModel());
	}

	/**
	 * Reload the project
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws IOException
	 */
	@Test
	@TestOrder(5)
	public void testReloadProject() throws ResourceLoadingCancelledException, FlexoException, IOException {

		log("testReloadProject()");

		FlexoProject<?> oldProject = project;

		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
		reloadResourceCenter(newVirtualModelResource.getDirectory());

		editor = loadProject(project.getProjectDirectory());
		project = (FlexoProject<File>) editor.getProject();
		assertNotSame(oldProject, project);
		assertNotNull(editor);
		assertNotNull(project);

		FMLRTVirtualModelInstanceResource newViewResource = project.getVirtualModelInstanceRepository()
				.getVirtualModelInstance(newView.getURI());
		assertNotNull(newViewResource);
		assertNull(newViewResource.getLoadedResourceData());
		newViewResource.loadResourceData();
		assertNotNull(newViewResource.getLoadedResourceData());
		newView = newViewResource.getLoadedResourceData();

		System.out.println("All resources=" + project.getAllResources());
		assertNotNull(project.getResource(newView.getURI()));

	}

}
