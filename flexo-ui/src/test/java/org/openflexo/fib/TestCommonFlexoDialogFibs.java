package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestCommonFlexoDialogFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/Dialog")).getFile(),
				"Fib/Dialog/"));
	}

	@Test
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/Dialog/ReviewUnsavedDialog.fib");
	}

	@Test
	public void testChooseAndConfigureCreationSchemeDialog() {
		validateFIB("Fib/Dialog/ChooseAndConfigureCreationSchemeDialog.fib");
	}

	@Test
	public void testConfigureVirtualModelSlotInstanceDialog() {
		validateFIB("Fib/Dialog/ConfigureVirtualModelSlotInstanceDialog.fib");
	}

	@Test
	public void testConfigureModelSlotInstanceDialog() {
		validateFIB("Fib/Dialog/ConfigureModelSlotInstanceDialog.fib");
	}

	@Test
	public void testConfigureFreeModelSlotInstanceDialog() {
		validateFIB("Fib/Dialog/ConfigureFreeModelSlotInstanceDialog.fib");
	}

	@Test
	public void testConfigureTypeAwareModelSlotInstanceDialog() {
		validateFIB("Fib/Dialog/ConfigureTypeAwareModelSlotInstanceDialog.fib");
	}

}
