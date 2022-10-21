/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.gina.test.GenericFIBInspectorTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLInspectors extends GenericFIBInspectorTestCase {

	/*
	 * Use this method to print all
	 * Then copy-paste 
	 */

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Inspectors/FML")).getFile(),
				"Inspectors/FML/"));
	}

	@Test
	public void testAbstractPropertyInspector() {
		validateFIB("Inspectors/FML/AbstractProperty.inspector");
	}

	@Test
	public void testAbstractAddFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/AbstractAddFlexoConceptInstance.inspector");
	}

	@Test
	public void testAbstractCreateResourceInspector() {
		validateFIB("Inspectors/FML/EditionAction/AbstractCreateResource.inspector");
	}

	@Test
	public void testAddClassInspector() {
		validateFIB("Inspectors/FML/EditionAction/AddClass.inspector");
	}

	@Test
	public void testAddClassInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/AddClassInstance.inspector");
	}

	@Test
	public void testAddFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/AddFlexoConceptInstance.inspector");
	}

	@Test
	public void testAddIndividualInspector() {
		validateFIB("Inspectors/FML/EditionAction/AddIndividual.inspector");
	}

	@Test
	public void testAddToListActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/AddToListAction.inspector");
	}

	@Test
	public void testAbstractAddVirtualModelInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/AbstractAddVirtualModelInstance.inspector");
	}

	@Test
	public void testAssignationActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/AssignationAction.inspector");
	}

	@Test
	public void testConditionalActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/ConditionalAction.inspector");
	}

	@Test
	public void testDeclarationActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/DeclarationAction.inspector");
	}

	@Test
	public void testDeleteActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/DeleteAction.inspector");
	}

	@Test
	public void testDeleteFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/DeleteFlexoConceptInstance.inspector");
	}

	@Test
	public void testEditionActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/EditionAction.inspector");
	}

	@Test
	public void testExpressionActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/ExpressionAction.inspector");
	}

	@Test
	public void testFetchRequestInspector() {
		validateFIB("Inspectors/FML/EditionAction/FetchRequest.inspector");
	}

	@Test
	public void testFinalizeMatchingInspector() {
		validateFIB("Inspectors/FML/EditionAction/FinalizeMatching.inspector");
	}

	@Test
	public void testFireEventActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/FireEventAction.inspector");
	}

	@Test
	public void testIncrementalIterationActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/IncrementalIterationAction.inspector");
	}

	@Test
	public void testInitiateMatchingInspector() {
		validateFIB("Inspectors/FML/EditionAction/InitiateMatching.inspector");
	}

	@Test
	public void testIterationActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/IterationAction.inspector");
	}

	@Test
	public void testLogActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/LogAction.inspector");
	}

	@Test
	public void testMatchFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/MatchFlexoConceptInstance.inspector");
	}

	@Test
	public void testNotifyPropertyChangedActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/NotifyPropertyChangedAction.inspector");
	}

	@Test
	public void testRemoveFromListActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/RemoveFromListAction.inspector");
	}

	@Test
	public void testReturnStatementInspector() {
		validateFIB("Inspectors/FML/EditionAction/ReturnStatement.inspector");
	}

	@Test
	public void testRoleSpecificActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/RoleSpecificAction.inspector");
	}

	@Test
	public void testSelectClassInspector() {
		validateFIB("Inspectors/FML/EditionAction/SelectClass.inspector");
	}

	@Test
	public void testSelectFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/SelectFlexoConceptInstance.inspector");
	}

	@Test
	public void testSelectVirtualModelInstanceInspector() {
		validateFIB("Inspectors/FML/EditionAction/SelectVirtualModelInstance.inspector");
	}

	@Test
	public void testTechnologySpecificActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/TechnologySpecificAction.inspector");
	}

	@Test
	public void testTechnologySpecificActionDefiningReceiverInspector() {
		validateFIB("Inspectors/FML/EditionAction/TechnologySpecificActionDefiningReceiver.inspector");
	}

	@Test
	public void testWhileActionInspector() {
		validateFIB("Inspectors/FML/EditionAction/WhileAction.inspector");
	}

	@Test
	public void testExpressionPropertyInspector() {
		validateFIB("Inspectors/FML/ExpressionProperty.inspector");
	}

	@Test
	public void testAbstractActionSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/AbstractActionScheme.inspector");
	}

	@Test
	public void testAbstractCreationSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/AbstractCreationScheme.inspector");
	}

	@Test
	public void testActionSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/ActionScheme.inspector");
	}

	@Test
	public void testCloningSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/CloningScheme.inspector");
	}

	@Test
	public void testCreationSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/CreationScheme.inspector");
	}

	@Test
	public void testDeletionSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/DeletionScheme.inspector");
	}

	@Test
	public void testEventListenerInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/EventListener.inspector");
	}

	@Test
	public void testFlexoBehaviourInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/FlexoBehaviour.inspector");
	}

	@Test
	public void testNavigationSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/NavigationScheme.inspector");
	}

	@Test
	public void testSynchronizationSchemeInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour/SynchronizationScheme.inspector");
	}

	@Test
	public void testFlexoBehaviourParameterInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviourParameter/FlexoBehaviourParameter.inspector");
	}

	@Test
	public void testFlexoConceptInspector() {
		validateFIB("Inspectors/FML/FlexoConcept.inspector");
	}

	@Test
	public void testFlexoConceptObjectInspector() {
		validateFIB("Inspectors/FML/FlexoConceptObject.inspector");
	}

	@Test
	public void testFlexoEnumInspector() {
		validateFIB("Inspectors/FML/FlexoEnum.inspector");
	}

	@Test
	public void testFlexoEnumValueInspector() {
		validateFIB("Inspectors/FML/FlexoEnumValue.inspector");
	}

	@Test
	public void testFlexoEventInspector() {
		validateFIB("Inspectors/FML/FlexoEvent.inspector");
	}

	@Test
	public void testFlexoPropertyInspector() {
		validateFIB("Inspectors/FML/FlexoProperty.inspector");
	}

	@Test
	public void testClassRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/ClassRole.inspector");
	}

	@Test
	public void testDataPropertyRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/DataPropertyRole.inspector");
	}

	@Test
	public void testFlexoConceptInstanceRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/FlexoConceptInstanceRole.inspector");
	}

	@Test
	public void testFlexoRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/FlexoRole.inspector");
	}

	@Test
	public void testIndividualRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/IndividualRole.inspector");
	}

	@Test
	public void testObjectPropertyRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/ObjectPropertyRole.inspector");
	}

	@Test
	public void testOntologicObjectRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/OntologicObjectRole.inspector");
	}

	@Test
	public void testPrimitiveRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/PrimitiveRole.inspector");
	}

	@Test
	public void testPropertyRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole/PropertyRole.inspector");
	}

	@Test
	public void testFMLObjectInspector() {
		validateFIB("Inspectors/FML/FMLObject.inspector");
	}

	@Test
	public void testGetPropertyInspector() {
		validateFIB("Inspectors/FML/GetProperty.inspector");
	}

	@Test
	public void testGetSetPropertyInspector() {
		validateFIB("Inspectors/FML/GetSetProperty.inspector");
	}

	@Test
	public void testFlexoConceptInspectorInspector() {
		validateFIB("Inspectors/FML/Inspector/FlexoConceptInspector.inspector");
	}

	@Test
	public void testInspectorEntryInspector() {
		validateFIB("Inspectors/FML/Inspector/InspectorEntry.inspector");
	}

	@Test
	public void testFMLRTModelSlotInspector() {
		validateFIB("Inspectors/FML/ModelSlot/FMLRTModelSlot.inspector");
	}

	@Test
	public void testFMLRTVirtualModelInstanceModelSlotInspector() {
		validateFIB("Inspectors/FML/ModelSlot/FMLRTVirtualModelInstanceModelSlot.inspector");
	}

	@Test
	public void testModelSlotInspector() {
		validateFIB("Inspectors/FML/ModelSlot/ModelSlot.inspector");
	}

	@Test
	public void testTypeAwareModelSlotInspector() {
		validateFIB("Inspectors/FML/ModelSlot/TypeAwareModelSlot.inspector");
	}

	@Test
	public void testVirtualModelInspector() {
		validateFIB("Inspectors/FML/VirtualModel.inspector");
	}

	@Test
	public void testVirtualModelLibraryInspector() {
		validateFIB("Inspectors/FML/VirtualModelLibrary.inspector");
	}

	@Test
	public void testCompilationUnitResourceInspector() {
		validateFIB("Inspectors/FML/CompilationUnitResource.inspector");
	}

}
