package org.openflexo.fml.rt.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLRTWidgetFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/Widget")).getFile(),
				"Fib/Widget/"));
	}

	@Test
	public void testFIBViewLibraryBrowser() {
		validateFIB("Fib/Widget/FIBViewLibraryBrowser.fib");
	}

}
