package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
import org.openflexo.toolbox.FileResource;

public class TestCommonFlexoInspectors extends GenericFIBInspectorTestCase {

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(new FileResource("Inspectors/COMMON"), "Inspectors/COMMON/"));
	}

	@Test
	public void testConditionalSectionInspector() {
		validateFIB("Inspectors/COMMON/ConditionalSection.inspector");
	}

	@Test
	public void testDocForModelObjectInspector() {
		validateFIB("Inspectors/COMMON/DocForModelObject.inspector");
	}

	@Test
	public void testEditionPatternInstanceInspector() {
		validateFIB("Inspectors/COMMON/EditionPatternInstance.inspector");
	}

	@Test
	public void testEntitySectionInspector() {
		validateFIB("Inspectors/COMMON/EntitySection.inspector");
	}

	@Test
	public void testERDiagramSectionInspector() {
		validateFIB("Inspectors/COMMON/ERDiagramSection.inspector");
	}

	@Test
	public void testFlexoModelObjectInspector() {
		validateFIB("Inspectors/COMMON/FlexoModelObject.inspector");
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
	public void testIterationSectionInspector() {
		validateFIB("Inspectors/COMMON/IterationSection.inspector");
	}

	@Test
	public void testModelObjectSectionInspector() {
		validateFIB("Inspectors/COMMON/ModelObjectSection.inspector");
	}

	@Test
	public void testNormalSectionInspector() {
		validateFIB("Inspectors/COMMON/NormalSection.inspector");
	}

	@Test
	public void testOperationScreenSectionInspector() {
		validateFIB("Inspectors/COMMON/OperationScreenSection.inspector");
	}

	@Test
	public void testPredefinedSectionInspector() {
		validateFIB("Inspectors/COMMON/PredefinedSection.inspector");
	}

	@Test
	public void testProcessSectionInspector() {
		validateFIB("Inspectors/COMMON/ProcessSection.inspector");
	}

	@Test
	public void testProjectInspector() {
		validateFIB("Inspectors/COMMON/Project.inspector");
	}

	@Test
	public void testRepositoryFolderInspector() {
		validateFIB("Inspectors/COMMON/RepositoryFolder.inspector");
	}

	@Test
	public void testRoleSectionInspector() {
		validateFIB("Inspectors/COMMON/RoleSection.inspector");
	}

	@Test
	public void testTOCEntryInspector() {
		validateFIB("Inspectors/COMMON/TOCEntry.inspector");
	}

	@Test
	public void testTOCRepositoryInspector() {
		validateFIB("Inspectors/COMMON/TOCRepository.inspector");
	}

	@Test
	public void testViewSectionInspector() {
		validateFIB("Inspectors/COMMON/ViewSection.inspector");
	}

}
