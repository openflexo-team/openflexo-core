package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.undo.UndoManager;

/**
 * PAMELA-D-8: undoing the deletion of a concept restores it and its nested concepts, not deleted, in their original order.
 */
public class TestUndoDeleteConcept {

	@BeforeClass
	public static void initServiceManager() {
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
