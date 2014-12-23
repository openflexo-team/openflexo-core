package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
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
	public void testAbstractActionSchemeInspector() {
		validateFIB("Inspectors/FML/AbstractActionScheme.inspector");
	}

	@Test
	public void testAbstractCreationSchemeInspector() {
		validateFIB("Inspectors/FML/AbstractCreationScheme.inspector");
	}

	@Test
	public void testActionSchemeInspector() {
		validateFIB("Inspectors/FML/ActionScheme.inspector");
	}

	@Test
	public void testCheckboxParameterInspector() {
		validateFIB("Inspectors/FML/CheckboxParameter.inspector");
	}

	@Test
	public void testClassParameterInspector() {
		validateFIB("Inspectors/FML/ClassParameter.inspector");
	}

	@Test
	public void testClassPatternRoleInspector() {
		validateFIB("Inspectors/FML/ClassPatternRole.inspector");
	}

	@Test
	public void testCloningSchemeInspector() {
		validateFIB("Inspectors/FML/CloningScheme.inspector");
	}

	@Test
	public void testCreationSchemeInspector() {
		validateFIB("Inspectors/FML/CreationScheme.inspector");
	}

	@Test
	public void testDataPropertyParameterInspector() {
		validateFIB("Inspectors/FML/DataPropertyParameter.inspector");
	}

	@Test
	public void testDataPropertyPatternRoleInspector() {
		validateFIB("Inspectors/FML/DataPropertyPatternRole.inspector");
	}

	@Test
	public void testDeletionSchemeInspector() {
		validateFIB("Inspectors/FML/DeletionScheme.inspector");
	}

	@Test
	public void testDropDownParameterInspector() {
		validateFIB("Inspectors/FML/DropDownParameter.inspector");
	}

	@Test
	public void testFlexoBehaviourInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviour.inspector");
	}

	@Test
	public void testFlexoBehaviourParameterInspector() {
		validateFIB("Inspectors/FML/FlexoBehaviourParameter.inspector");
	}

	@Test
	public void testFlexoConceptInspector() {
		validateFIB("Inspectors/FML/FlexoConcept.inspector");
	}

	@Test
	public void testFlexoConceptInstanceParameterInspector() {
		validateFIB("Inspectors/FML/FlexoConceptInstanceParameter.inspector");
	}

	@Test
	public void testFlexoConceptInstanceRoleInspector() {
		validateFIB("Inspectors/FML/FlexoConceptInstanceRole.inspector");
	}

	@Test
	public void testFlexoConceptObjectInspector() {
		validateFIB("Inspectors/FML/FlexoConceptObject.inspector");
	}

	@Test
	public void testFlexoRoleInspector() {
		validateFIB("Inspectors/FML/FlexoRole.inspector");
	}

	@Test
	public void testFloatParameterInspector() {
		validateFIB("Inspectors/FML/FloatParameter.inspector");
	}

	@Test
	public void testIndividualParameterInspector() {
		validateFIB("Inspectors/FML/IndividualParameter.inspector");
	}

	@Test
	public void testIndividualRoleInspector() {
		validateFIB("Inspectors/FML/IndividualRole.inspector");
	}

	@Test
	public void testInnerModelSlotParameterInspector() {
		validateFIB("Inspectors/FML/InnerModelSlotParameter.inspector");
	}

	@Test
	public void testIntegerParameterInspector() {
		validateFIB("Inspectors/FML/IntegerParameter.inspector");
	}

	@Test
	public void testListParameterInspector() {
		validateFIB("Inspectors/FML/ListParameter.inspector");
	}

	@Test
	public void testModelSlotInspector() {
		validateFIB("Inspectors/FML/ModelSlot.inspector");
	}

	@Test
	public void testNamedViewPointObjectInspector() {
		validateFIB("Inspectors/FML/NamedFMLObject.inspector");
	}

	@Test
	public void testNavigationSchemeInspector() {
		validateFIB("Inspectors/FML/NavigationScheme.inspector");
	}

	@Test
	public void testObjectPropertyParameterInspector() {
		validateFIB("Inspectors/FML/ObjectPropertyParameter.inspector");
	}

	@Test
	public void testObjectPropertyPatternRoleInspector() {
		validateFIB("Inspectors/FML/ObjectPropertyPatternRole.inspector");
	}

	@Test
	public void testOntologicObjectPatternRoleInspector() {
		validateFIB("Inspectors/FML/OntologicObjectPatternRole.inspector");
	}

	@Test
	public void testPrimitivePatternRoleInspector() {
		validateFIB("Inspectors/FML/PrimitivePatternRole.inspector");
	}

	@Test
	public void testPropertyParameterInspector() {
		validateFIB("Inspectors/FML/PropertyParameter.inspector");
	}

	@Test
	public void testPropertyPatternRoleInspector() {
		validateFIB("Inspectors/FML/PropertyPatternRole.inspector");
	}

	@Test
	public void testSynchronizationSchemeInspector() {
		validateFIB("Inspectors/FML/SynchronizationScheme.inspector");
	}

	@Test
	public void testTextAreaParameterInspector() {
		validateFIB("Inspectors/FML/TextAreaParameter.inspector");
	}

	@Test
	public void testTextFieldParameterInspector() {
		validateFIB("Inspectors/FML/TextFieldParameter.inspector");
	}

	@Test
	public void testTypeAwareModelSlotInspector() {
		validateFIB("Inspectors/FML/TypeAwareModelSlot.inspector");
	}

	@Test
	public void testURIParameterInspector() {
		validateFIB("Inspectors/FML/URIParameter.inspector");
	}

	@Test
	public void testViewPointInspector() {
		validateFIB("Inspectors/FML/ViewPoint.inspector");
	}

	@Test
	public void testViewPointLibraryInspector() {
		validateFIB("Inspectors/FML/ViewPointLibrary.inspector");
	}

	@Test
	public void testViewPointObjectInspector() {
		validateFIB("Inspectors/FML/FMLObject.inspector");
	}

	@Test
	public void testViewPointResourceInspector() {
		validateFIB("Inspectors/FML/ViewPointResource.inspector");
	}

	@Test
	public void testVirtualModelInspector() {
		validateFIB("Inspectors/FML/VirtualModel.inspector");
	}

	@Test
	public void testVirtualModelModelSlotInspector() {
		validateFIB("Inspectors/FML/VirtualModelModelSlot.inspector");
	}

	@Test
	public void testVirtualModelResourceInspector() {
		validateFIB("Inspectors/FML/VirtualModelResource.inspector");
	}

}
