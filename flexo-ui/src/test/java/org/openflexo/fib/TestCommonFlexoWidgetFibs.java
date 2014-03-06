package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.toolbox.ResourceLocator;

public class TestCommonFlexoWidgetFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(ResourceLocator.locateDirectory("Fib/Widget"), "Fib/Widget/"));
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
