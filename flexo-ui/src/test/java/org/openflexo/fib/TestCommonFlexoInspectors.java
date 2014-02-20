package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
import org.openflexo.toolbox.FileResource;

public class TestCommonFlexoInspectors extends GenericFIBInspectorTestCase {

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(new FileResource("Inspectors/COMMON"), "Inspectors/COMMON/"));
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
