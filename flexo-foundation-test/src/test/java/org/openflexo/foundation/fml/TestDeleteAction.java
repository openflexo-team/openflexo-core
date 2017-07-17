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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.action.DeleteVirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class TestDeleteAction extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static FlexoProject project;

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static VirtualModel viewPoint;
	static VirtualModel virtualModel;
	static FlexoConcept flexoConcept;

	private static DirectoryResourceCenter resourceCenter;

	@Test
	@TestOrder(1)
	public void testInstanciateServiceManagerAndProject() throws IOException {
		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getIODelegate().exists());
	}

	/**
	 * Test the deletion of the viewpoint
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		VirtualModelResource newVirtualModelResource = factory.makeTopLevelVirtualModelResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		viewPoint = newVirtualModelResource.getLoadedResourceData();

		assertNotNull(viewPoint);
		assertNotNull(viewPoint.getResource());

		VirtualModelResource newVMResource = factory.makeContainedVirtualModelResource(VIRTUAL_MODEL_NAME, newVirtualModelResource,
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel = newVMResource.getLoadedResourceData();

		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getIODelegate().exists());

		assertEquals(viewPoint, virtualModel.getContainerVirtualModel());
		assertEquals(virtualModel, virtualModel.getVirtualModel());
		assertEquals(virtualModel, virtualModel.getFlexoConcept());
		assertEquals(virtualModel, virtualModel.getResourceData());

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConcept");
		addEP.doAction();

		flexoConcept = addEP.getNewFlexoConcept();

		assertNotNull(flexoConcept);

		assertEquals(virtualModel, flexoConcept.getVirtualModel());
		assertEquals(virtualModel, flexoConcept.getOwningVirtualModel());
		assertEquals(flexoConcept, flexoConcept.getFlexoConcept());
		assertEquals(virtualModel, flexoConcept.getResourceData());

		try {
			((VirtualModelResource) virtualModel.getResource()).save(null);
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Test the deletion of the viewpoint
	 */
	@Test
	@TestOrder(3)
	public void testDeleteFlexoConcept() {
		DeleteFlexoConceptObjects action1 = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept, null, editor);
		assertTrue(action1.isValid());
		action1.doAction();
		assertTrue(action1.hasActionExecutionSucceeded());
		assertTrue(virtualModel.getFlexoConcepts().isEmpty());
	}

	@Test
	@TestOrder(4)
	public void testDeleteVirtualModel() {
		DeleteVirtualModel action2 = DeleteVirtualModel.actionType.makeNewAction(virtualModel, null, editor);
		assertTrue(action2.isValid());
		action2.doAction();
		assertTrue(action2.hasActionExecutionSucceeded());
		assertTrue(viewPoint.getVirtualModels().isEmpty());
	}

	@Test
	@TestOrder(5)
	public void testDeleteViewPoint() {
		DeleteVirtualModel action = DeleteVirtualModel.actionType.makeNewAction(viewPoint, null, editor);
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
	}

}
