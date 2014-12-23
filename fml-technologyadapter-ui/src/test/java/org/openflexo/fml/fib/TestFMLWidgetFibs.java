package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLWidgetFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl ) ResourceLocator.locateResource("Fib/Widget")).getFile(), "Fib/Widget/"));
	}

	@Test
	public void testFIBViewPointBrowser() {
		validateFIB("Fib/Widget/FIBViewPointBrowser.fib");
	}

	@Test
	public void testFIBViewPointLibraryBrowser() {
		validateFIB("Fib/Widget/FIBViewPointLibraryBrowser.fib");
	}

	@Test
	public void testFIBVirtualModelBrowser() {
		validateFIB("Fib/Widget/FIBVirtualModelBrowser.fib");
	}

}
