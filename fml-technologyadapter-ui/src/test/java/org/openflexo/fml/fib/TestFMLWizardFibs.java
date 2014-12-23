package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLWizardFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/Wizard")).getFile(),
				"Fib/Wizard/"));
	}

	@Test
	public void testConfigureAdditionalStepsForNewFlexoConcept() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureAdditionalStepsForNewFlexoConcept.fib");
	}

	@Test
	public void testConfigureFlexoBehaviourParameters() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureFlexoBehaviourParameters.fib");
	}

	@Test
	public void testConfigureFreeModelSlot() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureFreeModelSlot.fib");
	}

	@Test
	public void testConfigureModelSlots() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureModelSlots.fib");
	}

	@Test
	public void testConfigureTypeAwareModelSlot() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureTypeAwareModelSlot.fib");
	}

	@Test
	public void testConfigureVirtualModelModelSlot() {
		validateFIB("Fib/Wizard/CreateFMLElement/ConfigureVirtualModelModelSlot.fib");
	}

	@Test
	public void testDescribeFlexoBehaviour() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeFlexoBehaviour.fib");
	}

	@Test
	public void testDescribeFlexoConcept() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeFlexoConcept.fib");
	}

	@Test
	public void testDescribeFlexoRole() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeFlexoRole.fib");
	}

	@Test
	public void testDescribeModelSlot() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeModelSlot.fib");
	}

	@Test
	public void testDescribeViewPoint() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeViewPoint.fib");
	}

	@Test
	public void testDescribeVirtualModel() {
		validateFIB("Fib/Wizard/CreateFMLElement/DescribeVirtualModel.fib");
	}

}
