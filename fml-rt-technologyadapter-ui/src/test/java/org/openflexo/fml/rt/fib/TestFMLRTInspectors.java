package org.openflexo.fml.rt.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLRTInspectors extends GenericFIBInspectorTestCase {

	/*
	 * Use this method to print all
	 * Then copy-paste 
	 */

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(
				((FileResourceImpl) ResourceLocator.locateResource("Inspectors/FML-RT")).getFile(), "Inspectors/FML-RT/"));
	}

	@Test
	public void testViewInspector() {
		validateFIB("Inspectors/FML-RT/View.inspector");
	}

	@Test
	public void testViewLibraryInspector() {
		validateFIB("Inspectors/FML-RT/ViewLibrary.inspector");
	}

	@Test
	public void testVirtualModelInstanceInspector() {
		validateFIB("Inspectors/FML-RT/VirtualModelInstance.inspector");
	}

}
