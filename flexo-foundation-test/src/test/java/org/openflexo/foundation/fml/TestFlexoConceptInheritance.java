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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoProperty.OverridenPropertiesMustBeTypeCompatible;
import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.foundation.fml.action.CreateExpressionProperty;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test {@link FlexoConcept} inheritance features, as well as "isAbstract" management and
 * {@link FlexoProperty} inheritance and shadowing
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoConceptInheritance extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestResourceCenter/TestViewPoint.fml";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static FlexoEditor editor;
	static VirtualModel viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;

	public static AbstractProperty<String> property1InA;
	public static AbstractProperty<Boolean> property2InA;
	public static AbstractProperty<Number> property3InA;
	public static AbstractProperty<FlexoConceptInstanceType> property4InA;
	public static AbstractProperty<String> property5InA;
	public static PrimitiveRole<String> property6InA;

	public static PrimitiveRole<Boolean> property2InB;
	public static AbstractProperty<Integer> property3InB;
	public static PrimitiveRole<String> property7InB;

	public static FlexoConceptInstanceRole property4InC;
	public static PrimitiveRole<String> property8InC;

	public static PrimitiveRole<String> property1InD;
	public static ExpressionProperty<Integer> property3InD;
	public static PrimitiveRole<String> property5InD;
	public static PrimitiveRole<String> property9InD;

	static FlexoProject<?> project;
	static FMLRTVirtualModelInstance newView;
	static FMLRTVirtualModelInstance vmi;
	static FlexoConceptInstance a;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Init
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void init() throws IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);
	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();

		CompilationUnitResource newVirtualModelResource = factory.makeTopLevelCompilationUnitResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		viewPoint = newVirtualModelResource.getLoadedResourceData().getVirtualModel();

		// viewPoint = ViewPointImpl.newViewPoint(VIEWPOINT_NAME, VIEWPOINT_URI,
		// resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary(), resourceCenter);
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getFile().exists());

		System.out.println("viewPoint=" + viewPoint);
		// System.out.println("viewPoint.getResource()=" + viewPoint.getResource());

		assertTrue(viewPoint.getResource().getDirectory() != null);
		assertTrue(viewPoint.getResource().getIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(1, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();
		CompilationUnitResource newVMResource = factory.makeContainedCompilationUnitResource(VIRTUAL_MODEL_NAME, viewPoint.getResource(),
				true);
		virtualModel = newVMResource.getLoadedResourceData().getVirtualModel();

		// virtualModel = VirtualModelImpl.newVirtualModel(VIRTUAL_MODEL_NAME,
		// viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(virtualModel.getResource().getDirectory()).exists());
		assertTrue(virtualModel.getResource().getIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(2, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

	}

	/**
	 * Test FlexoConceptA creation, with 6 properties
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		log("testCreateFlexoConceptA()");

		CreateFlexoConcept addConceptA = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptA.setNewFlexoConceptName("FlexoConceptA");
		addConceptA.doAction();

		flexoConceptA = addConceptA.getNewFlexoConcept();

		System.out.println("flexoConceptA = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		CreateAbstractProperty createProperty1inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty1inA.setPropertyName("property1");
		createProperty1inA.setPropertyType(String.class);
		createProperty1inA.doAction();
		assertTrue(createProperty1inA.hasActionExecutionSucceeded());
		assertNotNull(property1InA = (AbstractProperty<String>) createProperty1inA.getNewFlexoProperty());

		CreateAbstractProperty createProperty2inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty2inA.setPropertyName("property2");
		createProperty2inA.setPropertyType(Boolean.class);
		createProperty2inA.doAction();
		assertTrue(createProperty2inA.hasActionExecutionSucceeded());
		assertNotNull(property2InA = (AbstractProperty<Boolean>) createProperty2inA.getNewFlexoProperty());

		CreateAbstractProperty createProperty3inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty3inA.setPropertyName("property3");
		createProperty3inA.setPropertyType(Number.class);
		createProperty3inA.doAction();
		assertTrue(createProperty3inA.hasActionExecutionSucceeded());
		assertNotNull(property3InA = (AbstractProperty<Number>) createProperty3inA.getNewFlexoProperty());

		CreateAbstractProperty createProperty4inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty4inA.setPropertyName("property4");
		createProperty4inA.setPropertyType(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE);
		createProperty4inA.doAction();
		assertTrue(createProperty4inA.hasActionExecutionSucceeded());
		assertNotNull(property4InA = (AbstractProperty<FlexoConceptInstanceType>) createProperty4inA.getNewFlexoProperty());

		CreateAbstractProperty createProperty5inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty5inA.setPropertyName("property5");
		createProperty5inA.setPropertyType(String.class);
		createProperty5inA.doAction();
		assertTrue(createProperty5inA.hasActionExecutionSucceeded());
		assertNotNull(property5InA = (AbstractProperty<String>) createProperty5inA.getNewFlexoProperty());

		CreatePrimitiveRole createProperty6inA = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty6inA.setRoleName("property6");
		createProperty6inA.setPrimitiveType(PrimitiveType.String);
		createProperty6inA.setCardinality(PropertyCardinality.One);
		createProperty6inA.doAction();
		assertTrue(createProperty6inA.hasActionExecutionSucceeded());
		assertNotNull(property6InA = (PrimitiveRole<String>) createProperty6inA.getNewFlexoRole());

		assertEquals(6, flexoConceptA.getFlexoProperties().size());
		assertEquals(6, flexoConceptA.getDeclaredProperties().size());
		assertEquals(6, flexoConceptA.getAccessibleProperties().size());
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty1inA.getNewFlexoProperty()));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty2inA.getNewFlexoProperty()));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty3inA.getNewFlexoProperty()));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty4inA.getNewFlexoProperty()));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty5inA.getNewFlexoProperty()));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty6inA.getNewFlexoProperty()));
		assertEquals(flexoConceptA.getDeclaredProperties(), flexoConceptA.getAccessibleProperties());

		assertSame(property1InA, flexoConceptA.getAccessibleProperty("property1"));
		assertEquals(String.class, property1InA.getType());
		assertEquals(String.class, property1InA.getResultingType());

		assertSame(property2InA, flexoConceptA.getAccessibleProperty("property2"));
		assertEquals(Boolean.class, property2InA.getType());
		assertEquals(Boolean.class, property2InA.getResultingType());

		assertSame(property3InA, flexoConceptA.getAccessibleProperty("property3"));
		assertEquals(Number.class, property3InA.getType());
		assertEquals(Number.class, property3InA.getResultingType());

		assertSame(property4InA, flexoConceptA.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getType());
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getResultingType());

		assertSame(property5InA, flexoConceptA.getAccessibleProperty("property5"));
		assertEquals(String.class, property5InA.getType());
		assertEquals(String.class, property5InA.getResultingType());

		assertSame(property6InA, flexoConceptA.getAccessibleProperty("property6"));
		assertEquals(String.class, property6InA.getType());
		assertEquals(String.class, property6InA.getResultingType());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptA.isAbstract());

		// We try to force to make it non abstract, and check that it is still
		// abstract
		flexoConceptA.setAbstract(false);
		assertTrue(flexoConceptA.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		virtualModel.getResource().save();

	}

	/**
	 * Test FlexoConceptB creation, define some overriden properties
	 * 
	 * @throws InconsistentFlexoConceptHierarchyException
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		log("testCreateFlexoConceptB()");

		CreateFlexoConcept addConceptB = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptB.setNewFlexoConceptName("FlexoConceptB");
		addConceptB.doAction();

		flexoConceptB = addConceptB.getNewFlexoConcept();

		flexoConceptB.addToParentFlexoConcepts(flexoConceptA);

		System.out.println("flexoConceptB = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		CreatePrimitiveRole createProperty2inB = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createProperty2inB.setRoleName("property2");
		createProperty2inB.setPrimitiveType(PrimitiveType.Boolean);
		createProperty2inB.setCardinality(PropertyCardinality.One);
		createProperty2inB.doAction();
		assertTrue(createProperty2inB.hasActionExecutionSucceeded());
		assertNotNull(property2InB = (PrimitiveRole<Boolean>) createProperty2inB.getNewFlexoRole());

		// Property3 is overriden by an AbstractProperty with a more specialized
		// type
		CreateAbstractProperty createProperty3inB = CreateAbstractProperty.actionType.makeNewAction(flexoConceptB, null, editor);
		createProperty3inB.setPropertyName("property3");
		createProperty3inB.setPropertyType(Integer.class);
		createProperty3inB.doAction();
		assertTrue(createProperty3inB.hasActionExecutionSucceeded());
		assertNotNull(property3InB = (AbstractProperty<Integer>) createProperty3inB.getNewFlexoProperty());

		CreatePrimitiveRole createProperty7inB = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createProperty7inB.setRoleName("property7");
		createProperty7inB.setPrimitiveType(PrimitiveType.String);
		createProperty7inB.setCardinality(PropertyCardinality.One);
		createProperty7inB.doAction();
		assertTrue(createProperty7inB.hasActionExecutionSucceeded());
		assertNotNull(property7InB = (PrimitiveRole<String>) createProperty7inB.getNewFlexoRole());

		assertEquals(3, flexoConceptB.getFlexoProperties().size());
		assertEquals(3, flexoConceptB.getDeclaredProperties().size());
		assertEquals(7, flexoConceptB.getAccessibleProperties().size());

		assertTrue(flexoConceptB.getDeclaredProperties().contains(createProperty2inB.getNewFlexoProperty()));
		assertTrue(flexoConceptB.getDeclaredProperties().contains(createProperty3inB.getNewFlexoProperty()));
		assertTrue(flexoConceptB.getDeclaredProperties().contains(createProperty7inB.getNewFlexoProperty()));

		assertSame(property2InB, flexoConceptB.getAccessibleProperty("property2"));
		assertEquals(Boolean.TYPE, property2InB.getType());
		assertEquals(Boolean.TYPE, property2InB.getResultingType());
		assertSameList(property2InB.getSuperProperties(), property2InA);
		assertSameList(property2InB.getAllSuperProperties(), property2InA);

		assertSame(property3InB, flexoConceptB.getAccessibleProperty("property3"));
		assertEquals(Integer.class, property3InB.getType());
		assertEquals(Integer.class, property3InB.getResultingType());
		assertSameList(property3InB.getSuperProperties(), property3InA);
		assertSameList(property3InB.getAllSuperProperties(), property3InA);

		assertSame(property7InB, flexoConceptB.getAccessibleProperty("property7"));
		assertEquals(String.class, property7InB.getType());
		assertEquals(String.class, property7InB.getResultingType());
		assertEquals(0, property7InB.getSuperProperties().size());
		assertEquals(0, property7InB.getAllSuperProperties().size());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptB.isAbstract());

		// We try to force to make it non abstract, and check that it is still
		// abstract
		flexoConceptB.setAbstract(false);
		assertTrue(flexoConceptB.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		virtualModel.getResource().save();

	}

	/**
	 * Test FlexoConceptC creation, define some overriden properties
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptC() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		log("testCreateFlexoConceptC()");

		CreateFlexoConcept addConceptC = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptC.setNewFlexoConceptName("FlexoConceptC");
		addConceptC.doAction();

		flexoConceptC = addConceptC.getNewFlexoConcept();

		flexoConceptC.addToParentFlexoConcepts(flexoConceptA);

		System.out.println("flexoConceptC = " + flexoConceptC);
		assertNotNull(flexoConceptC);

		CreateFlexoConceptInstanceRole createProperty4InC = CreateFlexoConceptInstanceRole.actionType.makeNewAction(flexoConceptC, null,
				editor);
		createProperty4InC.setRoleName("property4");
		createProperty4InC.setFlexoConceptInstanceType(flexoConceptA);
		createProperty4InC.setCardinality(PropertyCardinality.ZeroOne);
		createProperty4InC.doAction();
		assertTrue(createProperty4InC.hasActionExecutionSucceeded());
		assertNotNull(property4InC = createProperty4InC.getNewFlexoRole());

		CreatePrimitiveRole createProperty8InC = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptC, null, editor);
		createProperty8InC.setRoleName("property8");
		createProperty8InC.setPrimitiveType(PrimitiveType.String);
		createProperty8InC.setCardinality(PropertyCardinality.One);
		createProperty8InC.doAction();
		assertTrue(createProperty8InC.hasActionExecutionSucceeded());
		assertNotNull(property8InC = (PrimitiveRole<String>) createProperty8InC.getNewFlexoRole());

		assertEquals(2, flexoConceptC.getFlexoProperties().size());
		assertEquals(2, flexoConceptC.getDeclaredProperties().size());
		assertEquals(7, flexoConceptC.getAccessibleProperties().size());

		assertTrue(flexoConceptC.getDeclaredProperties().contains(createProperty4InC.getNewFlexoProperty()));
		assertTrue(flexoConceptC.getDeclaredProperties().contains(createProperty8InC.getNewFlexoProperty()));

		assertSame(property4InC, flexoConceptC.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InC.getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InC.getResultingType());
		assertSameList(property4InC.getSuperProperties(), property4InA);
		assertSameList(property4InC.getAllSuperProperties(), property4InA);

		assertSame(property8InC, flexoConceptC.getAccessibleProperty("property8"));
		assertEquals(String.class, property8InC.getType());
		assertEquals(String.class, property8InC.getResultingType());
		assertEquals(0, property8InC.getSuperProperties().size());
		assertEquals(0, property8InC.getAllSuperProperties().size());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptC.isAbstract());

		// We try to force to make it non abstract, and check that it is still
		// abstract
		flexoConceptC.setAbstract(false);
		assertTrue(flexoConceptC.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		virtualModel.getResource().save();

	}

	/**
	 * Test FlexoConceptC creation, inheriting from both B and C, and define some overriden properties
	 */
	@Test
	@TestOrder(7)
	public void testCreateFlexoConceptD() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		log("testCreateFlexoConceptD()");

		CreateFlexoConcept addConceptD = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptD.setNewFlexoConceptName("FlexoConceptD");
		addConceptD.doAction();

		flexoConceptD = addConceptD.getNewFlexoConcept();

		flexoConceptD.addToParentFlexoConcepts(flexoConceptB);
		flexoConceptD.addToParentFlexoConcepts(flexoConceptC);

		System.out.println("flexoConceptD = " + flexoConceptD);
		assertNotNull(flexoConceptD);

		CreatePrimitiveRole createProperty1InD = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptD, null, editor);
		createProperty1InD.setRoleName("property1");
		createProperty1InD.setPrimitiveType(PrimitiveType.String);
		createProperty1InD.setCardinality(PropertyCardinality.One);
		createProperty1InD.doAction();
		assertTrue(createProperty1InD.hasActionExecutionSucceeded());
		assertNotNull(property1InD = (PrimitiveRole<String>) createProperty1InD.getNewFlexoRole());

		CreateExpressionProperty createProperty3InD = CreateExpressionProperty.actionType.makeNewAction(flexoConceptD, null, editor);
		createProperty3InD.setPropertyName("property3");
		createProperty3InD.setExpression(new DataBinding<Integer>("property1.length"));
		createProperty3InD.doAction();
		assertTrue(createProperty3InD.hasActionExecutionSucceeded());
		assertNotNull(property3InD = (ExpressionProperty<Integer>) createProperty3InD.getNewFlexoProperty());

		CreatePrimitiveRole createProperty5InD = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptD, null, editor);
		createProperty5InD.setRoleName("property5");
		createProperty5InD.setPrimitiveType(PrimitiveType.String);
		createProperty5InD.setCardinality(PropertyCardinality.One);
		createProperty5InD.doAction();
		assertTrue(createProperty5InD.hasActionExecutionSucceeded());
		assertNotNull(property5InD = (PrimitiveRole<String>) createProperty5InD.getNewFlexoRole());

		CreatePrimitiveRole createProperty9InD = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptD, null, editor);
		createProperty9InD.setRoleName("property9");
		createProperty9InD.setPrimitiveType(PrimitiveType.String);
		createProperty9InD.setCardinality(PropertyCardinality.One);
		createProperty9InD.doAction();
		assertTrue(createProperty9InD.hasActionExecutionSucceeded());
		assertNotNull(property9InD = (PrimitiveRole<String>) createProperty9InD.getNewFlexoRole());

		assertEquals(4, flexoConceptD.getFlexoProperties().size());
		assertEquals(4, flexoConceptD.getDeclaredProperties().size());

		assertEquals(9, flexoConceptD.getAccessibleProperties().size());

		assertTrue(flexoConceptD.getDeclaredProperties().contains(createProperty1InD.getNewFlexoProperty()));
		assertTrue(flexoConceptD.getDeclaredProperties().contains(createProperty3InD.getNewFlexoProperty()));
		assertTrue(flexoConceptD.getDeclaredProperties().contains(createProperty5InD.getNewFlexoProperty()));
		assertTrue(flexoConceptD.getDeclaredProperties().contains(createProperty9InD.getNewFlexoProperty()));

		assertSame(property1InD, flexoConceptD.getAccessibleProperty("property1"));
		assertEquals(String.class, property1InD.getType());
		assertEquals(String.class, property1InD.getResultingType());
		assertSameList(property1InD.getSuperProperties(), property1InA);

		assertSame(property2InB, flexoConceptD.getAccessibleProperty("property2"));
		assertSameList(property2InB.getSuperProperties(), property2InA);

		assertSame(property3InD, flexoConceptD.getAccessibleProperty("property3"));

		System.out.println("FML=" + flexoConceptD.getFMLRepresentation());

		System.out.println("Exp=" + property3InD.getExpression() + " valid=" + property3InD.getExpression().isValid());
		System.out.println("reason: " + property3InD.getExpression().invalidBindingReason());

		assertEquals(Integer.TYPE, property3InD.getType());
		assertEquals(Integer.TYPE, property3InD.getResultingType());
		assertSameList(property3InD.getSuperProperties(), property3InA, property3InB);

		assertSame(property4InC, flexoConceptD.getAccessibleProperty("property4"));
		assertSameList(property4InC.getSuperProperties(), property4InA);

		assertSame(property5InD, flexoConceptD.getAccessibleProperty("property5"));
		assertSameList(property5InD.getSuperProperties(), property5InA);

		assertSame(property6InA, flexoConceptD.getAccessibleProperty("property6"));
		assertEquals(0, property6InA.getSuperProperties().size());

		assertSame(property7InB, flexoConceptD.getAccessibleProperty("property7"));
		assertEquals(0, property7InB.getSuperProperties().size());

		assertSame(property8InC, flexoConceptD.getAccessibleProperty("property8"));
		assertEquals(0, property8InC.getSuperProperties().size());

		assertSame(property9InD, flexoConceptD.getAccessibleProperty("property9"));
		assertEquals(String.class, property9InD.getType());
		assertEquals(String.class, property9InD.getResultingType());
		assertEquals(0, property9InD.getSuperProperties().size());

		// Because concept define some abstract properties, it is abstract
		assertFalse(flexoConceptD.isAbstract());

		// We try to force to make it abstract
		flexoConceptD.setAbstract(true);
		assertTrue(flexoConceptD.isAbstract());

		flexoConceptD.setAbstract(false);

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		virtualModel.getResource().save();

	}

	/**
	 * Test FlexoConcept inheritance inconsistency detection
	 */
	@Test
	@TestOrder(8)
	public void testHierarchyInconsistent() {

		log("testHierarchyInconsistent()");

		try {
			flexoConceptA.addToParentFlexoConcepts(flexoConceptD);
			fail();
		} catch (InconsistentFlexoConceptHierarchyException e) {
			// Excepted exception
			System.out.println("InconsistentFlexoConceptHierarchyException thrown as expected");
		}
	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		log("testViewPointIsValid()");

		assertVirtualModelIsValid(viewPoint);
		assertVirtualModelIsValid(virtualModel);

		// We change the type of property3 in B with incompatible type and we
		// check that an error occurs
		property3InB.setType(String.class);

		ValidationReport report = validate(virtualModel);
		assertEquals(2, report.getAllErrors().size());
		Iterator<ValidationError<?, ?>> iterator = report.getAllErrors().iterator();
		assertTrue(iterator.next().getValidationRule() instanceof OverridenPropertiesMustBeTypeCompatible);
		assertTrue(iterator.next().getValidationRule() instanceof OverridenPropertiesMustBeTypeCompatible);

		property3InB.setType(Integer.class);

		assertObjectIsValid(virtualModel);

	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(20)
	public void testReloadViewPoint() throws IOException {

		log("testReloadViewPoint()");

		CompilationUnitResource viewPointResource = viewPoint.getResource();

		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();

		File directory = ResourceLocator.retrieveResourceAsFile(viewPointResource.getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getRootDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect
			// new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			e.printStackTrace();
		}

		CompilationUnitResource retrievedVPResource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		VirtualModel reloadedViewPoint = retrievedVPResource.getCompilationUnit().getVirtualModel();
		assertEquals(reloadedViewPoint, reloadedViewPoint.getFlexoConcept());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getResourceData().getVirtualModel());

		VirtualModel reloadedVirtualModel = reloadedViewPoint.getVirtualModelNamed(VIRTUAL_MODEL_NAME);
		assertNotNull(reloadedVirtualModel);

		assertNotNull(flexoConceptA = reloadedVirtualModel.getFlexoConcept("FlexoConceptA"));
		assertNotNull(flexoConceptB = reloadedVirtualModel.getFlexoConcept("FlexoConceptB"));
		assertNotNull(flexoConceptC = reloadedVirtualModel.getFlexoConcept("FlexoConceptC"));
		assertNotNull(flexoConceptD = reloadedVirtualModel.getFlexoConcept("FlexoConceptD"));

		assertEquals(6, flexoConceptA.getFlexoProperties().size());
		assertEquals(6, flexoConceptA.getDeclaredProperties().size());
		assertEquals(6, flexoConceptA.getAccessibleProperties().size());

		assertNotNull(property1InA = (AbstractProperty<String>) flexoConceptA.getAccessibleProperty("property1"));
		assertEquals(String.class, property1InA.getType());
		assertEquals(String.class, property1InA.getResultingType());

		assertNotNull(property2InA = (AbstractProperty<Boolean>) flexoConceptA.getAccessibleProperty("property2"));
		assertEquals(Boolean.class, property2InA.getType());
		assertEquals(Boolean.class, property2InA.getResultingType());

		assertNotNull(property3InA = (AbstractProperty<Number>) flexoConceptA.getAccessibleProperty("property3"));
		assertEquals(Number.class, property3InA.getType());
		assertEquals(Number.class, property3InA.getResultingType());

		assertNotNull(property4InA = (AbstractProperty<FlexoConceptInstanceType>) flexoConceptA.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getType());
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getResultingType());

		assertNotNull(property5InA = (AbstractProperty<String>) flexoConceptA.getAccessibleProperty("property5"));
		assertEquals(String.class, property5InA.getType());
		assertEquals(String.class, property5InA.getResultingType());

		assertNotNull(property6InA = (PrimitiveRole<String>) flexoConceptA.getAccessibleProperty("property6"));
		assertEquals(String.class, property6InA.getType());
		assertEquals(String.class, property6InA.getResultingType());

		assertTrue(flexoConceptA.getDeclaredProperties().contains(property1InA));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(property2InA));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(property3InA));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(property4InA));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(property5InA));
		assertTrue(flexoConceptA.getDeclaredProperties().contains(property6InA));
		assertEquals(flexoConceptA.getDeclaredProperties(), flexoConceptA.getAccessibleProperties());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptA.isAbstract());

		// We try to force to make it non abstract, and check that it is still
		// abstract
		flexoConceptA.setAbstract(false);
		assertTrue(flexoConceptA.isAbstract());

		System.out.println("FML= " + virtualModel.getFMLRepresentation());
	}

	@Test
	@TestOrder(21)
	public void testUtils() {

		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptA, flexoConceptA));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptA, flexoConceptB));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptA, flexoConceptD));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptD, flexoConceptA));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptB, flexoConceptA));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptB, flexoConceptC));
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptC, flexoConceptB));

		assertSame(flexoConceptB, FMLUtils.getMostSpecializedAncestor(flexoConceptD, flexoConceptB));
		flexoConceptD.removeFromParentFlexoConcepts(flexoConceptB);
		assertSame(flexoConceptA, FMLUtils.getMostSpecializedAncestor(flexoConceptD, flexoConceptB));

	}

}
