package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.toolbox.ResourceLocator;

public class TestCommonFlexoDialogFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(ResourceLocator.locateDirectory("Fib/Dialog"), "Fib/Dialog/"));
	}

	@Test
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/Dialog/ReviewUnsavedDialog.fib");
	}

}
