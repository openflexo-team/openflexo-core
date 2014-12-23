package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLDialogFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/Dialog")).getFile(),
				"Fib/Dialog/"));
	}

	@Test
	public void testCreateEditionActionDialog() {
		validateFIB("Fib/Dialog/CreateEditionActionDialog.fib");
	}

	@Test
	public void testShowFMLRepresentationDialog() {
		validateFIB("Fib/Dialog/ShowFMLRepresentationDialog.fib");
	}

}
