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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test View creation facilities with a ViewPoint created on the fly
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateVirtualModelInstanceFromExistingViewPoint extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel viewPoint;
	private static VirtualModel virtualModel;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstance newView;
	private static FMLRTVirtualModelInstance newVirtualModelInstance;

	/**
	 * Retrieve the ViewPoint
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(viewPoint);
		assertEquals(1, viewPoint.getVirtualModels(true).size());
		virtualModel = viewPoint.getVirtualModels(true).get(0);
		assertNotNull(virtualModel);
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
		action.setVirtualModel(viewPoint);
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

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		action.setNewVirtualModelInstanceTitle("Test creation of a new FMLRTVirtualModelInstance");
		action.setVirtualModel(virtualModel);
		action.doAction();
		System.out.println(action.getThrownException());
		assertTrue(action.hasActionExecutionSucceeded());
		newVirtualModelInstance = action.getNewVirtualModelInstance();
		assertNotNull(newVirtualModelInstance);
		assertNotNull(newVirtualModelInstance.getResource());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());

		assertEquals(virtualModel, newVirtualModelInstance.getFlexoConcept());
		assertEquals(virtualModel, newVirtualModelInstance.getVirtualModel());
	}

	@Test
	@TestOrder(5)
	public void testReloadProject() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		log("testReloadProject()");

		FlexoProject<File> oldProject = project;
		String oldViewURI = newView.getURI();
		// instanciateTestServiceManager();
		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();
		assertNotSame(oldProject, project);
		assertNotNull(editor);
		assertNotNull(project);
		FMLRTVirtualModelInstanceResource newViewResource = project.getVirtualModelInstanceRepository().getVirtualModelInstance(oldViewURI);
		System.out.println("all resources = " + project.getVirtualModelInstanceRepository().getAllResources());
		assertNotNull(newViewResource);
		newViewResource.loadResourceData();
		assertNotNull(newViewResource.getLoadedResourceData());
		newView = newViewResource.getLoadedResourceData();

		System.out.println("All resources=" + project.getAllResources());
		assertNotNull(project.getResource(newView.getURI()));

		CompilationUnitResource vpRes = newViewResource.getVirtualModelResource();
		viewPoint = vpRes.getResourceData().getVirtualModel();
		assertNotNull(viewPoint);

		viewPoint.getCompilationUnit().loadContainedVirtualModelsWhenUnloaded();

		assertEquals(1, viewPoint.getVirtualModels().size());
		virtualModel = viewPoint.getVirtualModels().get(0);

		assertEquals(1, newViewResource.getVirtualModelInstanceResources().size());
		FMLRTVirtualModelInstanceResource vmiRes = newViewResource.getVirtualModelInstanceResources().get(0);
		newVirtualModelInstance = vmiRes.getVirtualModelInstance();

		assertEquals(virtualModel, newVirtualModelInstance.getFlexoConcept());
		assertEquals(virtualModel, newVirtualModelInstance.getVirtualModel());
	}

}
