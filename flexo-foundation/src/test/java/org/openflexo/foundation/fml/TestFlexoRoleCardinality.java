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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model<br>
 * 
 * In this test we try to move a complete control graph from an iteration to another iteration, and we check that binding model management
 * is consistent
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoRoleCardinality extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConcept1;
	static FlexoConcept flexoConcept2;

	/**
	 * Init
	 */
	@Test
	@TestOrder(1)
	public void init() {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		System.out.println("ResourceCenter= " + resourceCenter);
	}

	/**
	 * Test {@link ViewPoint} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() {
		viewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint", resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary());
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) viewPoint.getResource()).getFlexoIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(2, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModel() throws SaveResourceException {

		virtualModel = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getFlexoIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We disconnect VirtualModel from ViewPoint, and we check BindingModel evolution
		viewPoint.removeFromVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertNotNull(virtualModel.getBindingModel());
		assertEquals(2, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		// assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
		// .bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We reconnect VirtualModel again, and we check BindingModel evolution
		viewPoint.addToVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Test FlexoConcept creation, check BindingModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConcept() throws SaveResourceException {

		CreateFlexoConcept addEP1 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP1.setNewFlexoConceptName("FlexoConcept1");
		addEP1.doAction();

		flexoConcept1 = addEP1.getNewFlexoConcept();

		System.out.println("flexoConcept1 = " + flexoConcept1);
		assertNotNull(flexoConcept1);

		CreateFlexoConcept addEP2 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP2.setNewFlexoConceptName("FlexoConcept2");
		addEP2.doAction();

		flexoConcept2 = addEP2.getNewFlexoConcept();

		System.out.println("flexoConcept2 = " + flexoConcept2);
		assertNotNull(flexoConcept2);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.setRoleCardinality(RoleCardinality.One);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR2.setRoleName("someBooleanInA");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.setRoleCardinality(RoleCardinality.ZeroMany);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR3.setRoleName("someIntegerInA");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.setRoleCardinality(RoleCardinality.OneMany);
		createPR3.doAction();

		CreateFlexoRole createPR4 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR4.setRoleName("someFlexoConcept2");
		createPR4.setFlexoRoleClass(FlexoConceptInstanceRole.class);
		createPR4.setFlexoConceptInstanceType(flexoConcept2);
		createPR4.setRoleCardinality(RoleCardinality.ZeroMany);
		createPR4.doAction();

		assertEquals(4, flexoConcept1.getFlexoRoles().size());
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR3.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR4.getNewFlexoRole()));

		PrimitiveRole<String> aStringInA = (PrimitiveRole<String>) flexoConcept1.getFlexoRole("aStringInA");
		assertNotNull(aStringInA);
		assertEquals(String.class, aStringInA.getType());
		assertEquals(String.class, aStringInA.getResultingType());
		PrimitiveRole<Boolean> aBooleanInA = (PrimitiveRole<Boolean>) flexoConcept1.getFlexoRole("someBooleanInA");
		assertNotNull(aBooleanInA);
		assertEquals(Boolean.class, aBooleanInA.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, Boolean.class), aBooleanInA.getResultingType());
		PrimitiveRole<Integer> anIntegerInA = (PrimitiveRole<Integer>) flexoConcept1.getFlexoRole("someIntegerInA");
		assertNotNull(anIntegerInA);

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		assertViewPointIsValid(viewPoint);

	}

}
