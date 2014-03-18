package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestCommonFlexoVPMFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl ) ResourceLocator.locateResource("Fib/VPM")).getFile(), "Fib/VPM/"));
	}

	@Test
	public void testActionSchemePanel() {
		validateFIB("Fib/VPM/ActionSchemePanel.fib");
	}

	@Test
	public void testAddClassPanel() {
		validateFIB("Fib/VPM/AddClassPanel.fib");
	}

	@Test
	public void testAddFlexoConceptInstancePanel() {
		validateFIB("Fib/VPM/AddFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testAddIndividualPanel() {
		validateFIB("Fib/VPM/AddIndividualPanel.fib");
	}

	@Test
	public void testAddToListActionPanel() {
		validateFIB("Fib/VPM/AddToListActionPanel.fib");
	}

	@Test
	public void testAssignationActionPanel() {
		validateFIB("Fib/VPM/AssignationActionPanel.fib");
	}

	@Test
	public void testCloningSchemePanel() {
		validateFIB("Fib/VPM/CloningSchemePanel.fib");
	}

	@Test
	public void testConditionalActionPanel() {
		validateFIB("Fib/VPM/ConditionalActionPanel.fib");
	}

	@Test
	public void testCreationSchemePanel() {
		validateFIB("Fib/VPM/CreationSchemePanel.fib");
	}

	@Test
	public void testDeclarePatternRolePanel() {
		validateFIB("Fib/VPM/DeclarePatternRolePanel.fib");
	}

	@Test
	public void testDeleteFlexoConceptInstancePanel() {
		validateFIB("Fib/VPM/DeleteFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testDeletionActionPanel() {
		validateFIB("Fib/VPM/DeletionActionPanel.fib");
	}

	@Test
	public void testDeletionSchemePanel() {
		validateFIB("Fib/VPM/DeletionSchemePanel.fib");
	}

	@Test
	public void testFlexoConceptInspectorPanel() {
		validateFIB("Fib/VPM/FlexoConceptInspectorPanel.fib");
	}

	@Test
	public void testFlexoConceptPanel() {
		validateFIB("Fib/VPM/FlexoConceptPanel.fib");
	}

	@Test
	public void testEditionSchemePanel() {
		validateFIB("Fib/VPM/FlexoBehaviourPanel.fib");
	}

	@Test
	public void testExecutionActionPanel() {
		validateFIB("Fib/VPM/ExecutionActionPanel.fib");
	}

	@Test
	public void testFetchRequestIterationActionPanel() {
		validateFIB("Fib/VPM/FetchRequestIterationActionPanel.fib");
	}

	@Test
	public void testIterationActionPanel() {
		validateFIB("Fib/VPM/IterationActionPanel.fib");
	}

	@Test
	public void testLocalizedDictionaryPanel() {
		validateFIB("Fib/VPM/LocalizedDictionaryPanel.fib");
	}

	@Test
	public void testMatchFlexoConceptInstancePanel() {
		validateFIB("Fib/VPM/MatchFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testNavigationSchemePanel() {
		validateFIB("Fib/VPM/NavigationSchemePanel.fib");
	}

	@Test
	public void testProcedureActionPanel() {
		validateFIB("Fib/VPM/ProcedureActionPanel.fib");
	}

	@Test
	public void testSelectFlexoConceptInstancePanel() {
		validateFIB("Fib/VPM/SelectFlexoConceptInstancePanel.fib");
	}

	@Test
	public void testSelectIndividualPanel() {
		validateFIB("Fib/VPM/SelectIndividualPanel.fib");
	}

	@Test
	public void testStandardFlexoConceptView() {
		validateFIB("Fib/VPM/StandardFlexoConceptView.fib");
	}

	@Test
	public void testSynchronizationSchemePanel() {
		validateFIB("Fib/VPM/SynchronizationSchemePanel.fib");
	}

	@Test
	public void testViewPointView() {
		validateFIB("Fib/VPM/ViewPointView.fib");
	}

	@Test
	public void testVirtualModelStructuralPanel() {
		validateFIB("Fib/VPM/VirtualModelStructuralPanel.fib");
	}

	@Test
	public void testVirtualModelView() {
		validateFIB("Fib/VPM/VirtualModelView.fib");
	}

}
