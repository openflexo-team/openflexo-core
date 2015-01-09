package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/FML")).getFile(), "Fib/FML/"));
	}

	@Test
	public void testActionSchemePanel() {
		validateFIB("Fib/FML/ActionSchemePanel.fib");
	}

	@Test
	public void testAddClassPanel() {
		validateFIB("Fib/FML/AddClassPanel.fib");
	}

	@Test
	public void testAddFlexoConceptInstancePanel() {
		validateFIB("Fib/FML/AddFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testAddIndividualPanel() {
		validateFIB("Fib/FML/AddIndividualPanel.fib");
	}

	@Test
	public void testAddToListActionPanel() {
		validateFIB("Fib/FML/AddToListActionPanel.fib");
	}

	@Test
	public void testAssignationActionPanel() {
		validateFIB("Fib/FML/AssignationActionPanel.fib");
	}

	@Test
	public void testDeclarationActionPanel() {
		validateFIB("Fib/FML/DeclarationActionPanel.fib");
	}

	@Test
	public void testCloningSchemePanel() {
		validateFIB("Fib/FML/CloningSchemePanel.fib");
	}

	@Test
	public void testConditionalActionPanel() {
		validateFIB("Fib/FML/ConditionalActionPanel.fib");
	}

	@Test
	public void testCreationSchemePanel() {
		validateFIB("Fib/FML/CreationSchemePanel.fib");
	}

	@Test
	public void testDeleteFlexoConceptInstancePanel() {
		validateFIB("Fib/FML/DeleteFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testDeletionActionPanel() {
		validateFIB("Fib/FML/DeletionActionPanel.fib");
	}

	@Test
	public void testDeletionSchemePanel() {
		validateFIB("Fib/FML/DeletionSchemePanel.fib");
	}

	@Test
	public void testFetchRequestIterationActionPanel() {
		validateFIB("Fib/FML/FetchRequestIterationActionPanel.fib");
	}

	@Test
	public void testFlexoBehaviourPanel() {
		validateFIB("Fib/FML/FlexoBehaviourPanel.fib");
	}

	@Test
	public void testFlexoConceptInspectorPanel() {
		validateFIB("Fib/FML/FlexoConceptInspectorPanel.fib");
	}

	@Test
	public void testFlexoConceptPanel() {
		validateFIB("Fib/FML/FlexoConceptPanel.fib");
	}

	@Test
	public void testIterationActionPanel() {
		validateFIB("Fib/FML/IterationActionPanel.fib");
	}

	@Test
	public void testLocalizedDictionaryPanel() {
		validateFIB("Fib/FML/LocalizedDictionaryPanel.fib");
	}

	@Test
	public void testMatchFlexoConceptInstancePanel() {
		validateFIB("Fib/FML/MatchFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testNavigationSchemePanel() {
		validateFIB("Fib/FML/NavigationSchemePanel.fib");
	}

	/*@Test
	public void testRemoveFromListActionPanel() {
		validateFIB("Fib/FML/RemoveFromListActionPanel.fib");
	}*/

	@Test
	public void testSelectFlexoConceptInstancePanel() {
		validateFIB("Fib/FML/SelectFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testSelectIndividualPanel() {
		validateFIB("Fib/FML/SelectIndividualPanel.fib");
	}

	@Test
	public void testStandardFlexoConceptView() {
		validateFIB("Fib/FML/StandardFlexoConceptView.fib");
	}

	@Test
	public void testSynchronizationSchemePanel() {
		validateFIB("Fib/FML/SynchronizationSchemePanel.fib");
	}

	@Test
	public void testViewPointView() {
		validateFIB("Fib/FML/ViewPointView.fib");
	}

	@Test
	public void testVirtualModelStructuralPanel() {
		validateFIB("Fib/FML/VirtualModelStructuralPanel.fib");
	}

	@Test
	public void testVirtualModelView() {
		validateFIB("Fib/FML/VirtualModelView.fib");
	}

}
