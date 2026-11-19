package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.undo.UndoManager;

/**
 * PAMELA-D-8 (openflexo-team/pamela#17): undoing the deletion of a concept leaves the restored objects marked deleted, and their lists
 * in reverse order. Asserts the intended behaviour and fails until the defect is fixed. Skipped unless the environment variable
 * {@code OPENFLEXO_KNOWN_DEFECTS} is set:
 *
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-parser:cleanTest :fml-parser:test --tests '*KnownDefectsUndoDeleteConcept*'
 * </pre>
 */
public class KnownDefectsUndoDeleteConceptTest {

	@BeforeClass
	public static void initServiceManager() {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
		TestFMLReindent.initServiceManager();
	}

	@Test
	public void undoingTheDeletionOfAContainerRestoresIt() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(TestConceptContainment.NESTED);
		VirtualModel vm = unit.getVirtualModel();
		UndoManager undoManager = unit.getFMLModelFactory().getEditingContext().getUndoManager();
		if (undoManager.isBeeingRecording()) {
			// The parse leaves an unidentified recording open
			undoManager.stopRecording(undoManager.getCurrentEdition());
		}
		FlexoConcept root1 = vm.getFlexoConcept("Root1");
		FlexoConcept alpha = vm.getFlexoConcept("Alpha");
		FlexoConcept leaf = vm.getFlexoConcept("Leaf");
		FlexoConcept beta = vm.getFlexoConcept("Beta");
		List<FlexoConcept> rootsBefore = List.copyOf(vm.getContents());
		List<FlexoConcept> allBefore = vm.getAllFlexoConcepts();

		CompoundEdit delete = undoManager.startRecording("delete");
		root1.delete();
		undoManager.stopRecording(delete);
		undoManager.undo();

		for (FlexoConcept concept : List.of(root1, alpha, leaf, beta)) {
			assertFalse(concept.getName() + " still deleted", concept.isDeleted());
		}
		assertEquals(rootsBefore, vm.getContents());
		assertEquals(List.of(alpha, beta), root1.getContents());
		assertSame(vm, root1.getContainer());
		assertEquals(allBefore, vm.getAllFlexoConcepts());
	}
}
