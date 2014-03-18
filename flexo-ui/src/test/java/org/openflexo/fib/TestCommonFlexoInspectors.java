package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestCommonFlexoInspectors extends GenericFIBInspectorTestCase {

	/*
	 * Use this method to print all
	 * Then copy-paste 
	 */
	
	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(((FileResourceImpl ) ResourceLocator.locateResource("Inspectors/COMMON")).getFile(), "Inspectors/COMMON/"));
	}

	@Test
	public void testDocForModelObjectInspector() {
		validateFIB("Inspectors/COMMON/DocForModelObject.inspector");
	}

	@Test
	public void testFlexoConceptInstanceInspector() {
		validateFIB("Inspectors/COMMON/FlexoConceptInstance.inspector");
	}

	@Test
	public void testFlexoObjectInspector() {
		validateFIB("Inspectors/COMMON/FlexoObject.inspector");
	}

	@Test
	public void testFlexoProjectObjectInspector() {
		validateFIB("Inspectors/COMMON/FlexoProjectObject.inspector");
	}

	@Test
	public void testProjectInspector() {
		validateFIB("Inspectors/COMMON/Project.inspector");
	}

	@Test
	public void testRepositoryFolderInspector() {
		validateFIB("Inspectors/COMMON/RepositoryFolder.inspector");
	}

}
