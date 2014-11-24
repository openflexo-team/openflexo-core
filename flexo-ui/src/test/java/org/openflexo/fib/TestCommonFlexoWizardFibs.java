package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestCommonFlexoWizardFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/Wizard")).getFile(),
				"Fib/Wizard/"));
	}

	@Test
	public void testChooseViewPoint() {
		validateFIB("Fib/Wizard/CreateView/ChooseViewPoint.fib");
	}

	@Test
	public void testChooseAndConfigureCreationScheme() {
		validateFIB("Fib/Wizard/CreateVirtualModelInstance/ChooseAndConfigureCreationScheme.fib");
	}

	@Test
	public void testChooseVirtualModel() {
		validateFIB("Fib/Wizard/CreateVirtualModelInstance/ChooseVirtualModel.fib");
	}

	@Test
	public void testConfigureFreeModelSlotInstance() {
		validateFIB("Fib/Wizard/CreateVirtualModelInstance/ConfigureFreeModelSlotInstance.fib");
	}

	@Test
	public void testConfigureTypeAwareModelSlotInstance() {
		validateFIB("Fib/Wizard/CreateVirtualModelInstance/ConfigureTypeAwareModelSlotInstance.fib");
	}

	@Test
	public void testConfigureVirtualModelSlotInstance() {
		validateFIB("Fib/Wizard/CreateVirtualModelInstance/ConfigureVirtualModelSlotInstance.fib");
	}

}
