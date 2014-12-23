package org.openflexo.fml.rt.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLRTCommonFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib")).getFile(), "Fib/"));
	}

	@Test
	public void testFlexoConceptInstanceSelector() {
		validateFIB("Fib/FlexoConceptInstanceSelector.fib");
	}

	@Test
	public void testViewFolderSelector() {
		validateFIB("Fib/ViewFolderSelector.fib");
	}

	@Test
	public void testViewSelector() {
		validateFIB("Fib/ViewSelector.fib");
	}

	@Test
	public void testVirtualModelInstanceSelector() {
		validateFIB("Fib/VirtualModelInstanceSelector.fib");
	}

	@Test
	public void testVirtualModelInstanceView() {
		validateFIB("Fib/VirtualModelInstanceView.fib");
	}

	@Test
	public void testFIBViewLibraryBrowser() {
		validateFIB("Fib/Widget/FIBViewLibraryBrowser.fib");
	}

}
