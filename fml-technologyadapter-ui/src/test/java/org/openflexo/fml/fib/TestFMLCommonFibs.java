package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLCommonFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib")).getFile(), "Fib/"));
	}

	@Test
	public void testFlexoConceptSelector() {
		validateFIB("Fib/FlexoConceptSelector.fib");
	}

	@Test
	public void testViewPointSelector() {
		validateFIB("Fib/ViewPointSelector.fib");
	}

	@Test
	public void testVirtualModelSelector() {
		validateFIB("Fib/VirtualModelSelector.fib");
	}

}
