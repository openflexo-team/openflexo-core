package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.toolbox.FileResource;

public class TestCommonFlexoDialogFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(new FileResource("Fib/Dialog"), "Fib/Dialog/"));
	}

	@Test
	public void testPushToPaletteDialog() {
		validateFIB("Fib/Dialog/PushToPaletteDialog.fib");
	}

	@Test
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/Dialog/ReviewUnsavedDialog.fib");
	}

}
