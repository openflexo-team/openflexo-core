/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test {@link DataBinding} analysis in the context of FML {@link VirtualModel} on which we perform changes
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLDataBindingPerfIssue extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	private static DirectoryResourceCenter resourceCenter;

	static FlexoEditor editor;
	static VirtualModel topVirtualModel;
	static VirtualModel virtualModel;

	private static PrimitiveRole<String> stringProperty1;
	private static ActionScheme actionScheme;

	public void genericTest(Bindable bindable, String bindingPath, boolean expectedValidity, Type expectedType) {

		DataBinding<?> db = makeBinding(bindable, bindingPath, expectedValidity, expectedType);
		db.delete();
	}

	public DataBinding<?> makeBinding(Bindable bindable, String bindingPath, boolean expectedValidity, Type expectedType) {

		System.out.println("Evaluate " + bindingPath);

		DataBinding<?> dataBinding = new DataBinding<>(bindingPath, bindable, expectedType, DataBinding.BindingDefinitionType.GET);

		if (dataBinding.getExpression() != null) {
			System.out.println(
					"Parsed " + dataBinding + " as " + dataBinding.getExpression() + " of " + dataBinding.getExpression().getClass());

			assertEquals(expectedValidity, dataBinding.isValid());

			if (dataBinding.isValid()) {
				assertEquals(expectedType, dataBinding.getAnalyzedType());
			}

			return dataBinding;

		}
		System.out.println("Could not Parse " + dataBinding + " defined as " + dataBinding.getUnparsedBinding());
		fail("Unparseable binding");
		return null;
	}

	/**
	 * Test the VP creation
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		VirtualModelResource newVirtualModelResource = factory.makeTopLevelVirtualModelResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		topVirtualModel = newVirtualModelResource.getLoadedResourceData();

		assertTrue(((VirtualModelResource) topVirtualModel.getResource()).getDirectory() != null);
		assertTrue(((VirtualModelResource) topVirtualModel.getResource()).getIODelegate().exists());

		assertEquals(topVirtualModel, topVirtualModel.getVirtualModel());
		assertEquals(null, topVirtualModel.getContainerVirtualModel());
		assertEquals(topVirtualModel, topVirtualModel.getFlexoConcept());
		assertEquals(topVirtualModel, topVirtualModel.getResourceData());
	}

	/**
	 * Test the VirtualModel creation
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeContainedVirtualModelResource(VIRTUAL_MODEL_NAME,
				topVirtualModel.getVirtualModelResource(), true);
		virtualModel = newVMResource.getLoadedResourceData();

		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getIODelegate().exists());

		assertSame(topVirtualModel, virtualModel.getContainerVirtualModel());

		assertEquals(virtualModel, virtualModel.getFlexoConcept());

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createPR1.setRoleName("aString");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();
		stringProperty1 = (PrimitiveRole<String>) createPR1.getNewFlexoRole();
		assertNotNull(stringProperty1);

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(3)
	public void testCreateEditor() {
		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);
	}

	@Test
	@TestOrder(11)
	public void testCreateAnActionScheme() {

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(virtualModel, null, editor);
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateEditionAction createExpressionAction = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		createExpressionAction.setEditionActionClass(ExpressionAction.class);
		createExpressionAction.doAction();
		ExpressionAction<String> expression = (ExpressionAction) createExpressionAction.getNewEditionAction();
		assertNotNull(expression);
		expression.setExpression(expr1 = new DataBinding<>("aString"));
		if (!expr1.isValid()) {
			System.out.println("Not valid: " + expr1);
			System.out.println("Reason: " + expr1.invalidBindingReason());
		}
		assertTrue(expr1.isValid());

		CreateEditionAction createExpressionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		createExpressionAction2.setEditionActionClass(ExpressionAction.class);
		createExpressionAction2.doAction();
		ExpressionAction<String> expression2 = (ExpressionAction) createExpressionAction2.getNewEditionAction();
		expression2.setExpression(expr2 = new DataBinding<>("aString+'toto'"));

		assertNotNull(expression2);
		assertTrue(expr2.isValid());

	}

	private static DataBinding<String> expr1;
	private static DataBinding<String> expr2;

	@Test
	@TestOrder(20)
	public void testTrivialCase() {

		System.out.println(virtualModel.getFMLRepresentation());

		stringProperty1.setName("renamedProperty");

		System.out.println(virtualModel.getFMLRepresentation());

		assertTrue(expr1.isValid());
		assertEquals("renamedProperty", expr1.toString());
		assertTrue(expr2.isValid());
		assertEquals("(renamedProperty + \"toto\")", expr2.toString());
	}

}
