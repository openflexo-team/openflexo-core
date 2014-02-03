package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.toolbox.FileResource;

public class TestCommonFlexoFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(new FileResource("Fib"), "Fib/"));
	}

	@Test
	public void testAskResourceCenterDirectory() {
		validateFIB("Fib/AskResourceCenterDirectory.fib");
	}

	@Test
	public void testDescriptionWidget() {
		validateFIB("Fib/DescriptionWidget.fib");
	}

	@Test
	public void testDocGenerationChooser() {
		validateFIB("Fib/DocGenerationChooser.fib");
	}

	@Test
	public void testEditionPatternInstanceSelector() {
		validateFIB("Fib/EditionPatternInstanceSelector.fib");
	}

	@Test
	public void testEditionPatternSelector() {
		validateFIB("Fib/EditionPatternSelector.fib");
	}

	@Test
	public void testFIBClassSelector() {
		validateFIB("Fib/FIBClassSelector.fib");
	}

	@Test
	public void testFIBIndividualSelector() {
		validateFIB("Fib/FIBIndividualSelector.fib");
	}

	@Test
	public void testFIBInformationSpaceBrowser() {
		validateFIB("Fib/FIBInformationSpaceBrowser.fib");
	}

	@Test
	public void testFIBOntologyBrowser() {
		validateFIB("Fib/FIBOntologyBrowser.fib");
	}

	@Test
	public void testFIBOntologyClassEditor() {
		validateFIB("Fib/FIBOntologyClassEditor.fib");
	}

	@Test
	public void testFIBOntologyDataPropertyEditor() {
		validateFIB("Fib/FIBOntologyDataPropertyEditor.fib");
	}

	@Test
	public void testFIBOntologyEditor() {
		validateFIB("Fib/FIBOntologyEditor.fib");
	}

	@Test
	public void testFIBOntologyIndividualEditor() {
		validateFIB("Fib/FIBOntologyIndividualEditor.fib");
	}

	@Test
	public void testFIBOntologyObjectPropertyEditor() {
		validateFIB("Fib/FIBOntologyObjectPropertyEditor.fib");
	}

	@Test
	public void testFIBOntologySelector() {
		validateFIB("Fib/FIBOntologySelector.fib");
	}

	@Test
	public void testFIBPropertySelector() {
		validateFIB("Fib/FIBPropertySelector.fib");
	}

	@Test
	public void testFIBViewPointLibraryBrowser() {
		validateFIB("Fib/FIBViewPointLibraryBrowser.fib");
	}

	@Test
	public void testInstallDefaultPackagedResourceCenterDirectory() {
		validateFIB("Fib/InstallDefaultPackagedResourceCenterDirectory.fib");
	}

	@Test
	public void testJIRAIssueReportDialog() {
		validateFIB("Fib/JIRAIssueReportDialog.fib");
	}

	@Test
	public void testJIRASubmitIssueReportDialog() {
		validateFIB("Fib/JIRASubmitIssueReportDialog.fib");
	}

	@Test
	public void testJIRAURLCredentialsDialog() {
		validateFIB("Fib/JIRAURLCredentialsDialog.fib");
	}

	@Test
	public void testMainPaneTobBar() {
		validateFIB("Fib/MainPaneTobBar.fib");
	}

	@Test
	public void testMetaModelSelector() {
		validateFIB("Fib/MetaModelSelector.fib");
	}

	@Test
	public void testModelSelector() {
		validateFIB("Fib/ModelSelector.fib");
	}

	@Test
	public void testNewServerProject() {
		validateFIB("Fib/NewServerProject.fib");
	}

	@Test
	public void testProjectSelector() {
		validateFIB("Fib/ProjectSelector.fib");
	}

	@Test
	public void testRequestLoginDialog() {
		validateFIB("Fib/RequestLoginDialog.fib");
	}

	@Test
	public void testResourceCenterEditor() {
		validateFIB("Fib/ResourceCenterEditor.fib");
	}

	@Test
	public void testResourceCenterSelector() {
		validateFIB("Fib/ResourceCenterSelector.fib");
	}

	@Test
	public void testPreferences() {
		validateFIB("Fib/Preferences.fib");
	}

	@Test
	public void testResourceSelector() {
		validateFIB("Fib/ResourceSelector.fib");
	}

	@Test
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/ReviewUnsavedDialog.fib");
	}

	@Test
	public void testSaveProjects() {
		validateFIB("Fib/SaveProjects.fib");
	}

	@Test
	public void testServerClientModelView() {
		validateFIB("Fib/ServerClientModelView.fib");
	}

	@Test
	public void testViewFolderSelector() {
		validateFIB("Fib/ViewFolderSelector.fib");
	}

	@Test
	public void testViewPointSelector() {
		validateFIB("Fib/ViewPointSelector.fib");
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
	public void testVirtualModelSelector() {
		validateFIB("Fib/VirtualModelSelector.fib");
	}

	@Test
	public void testWebServiceURLDialog() {
		validateFIB("Fib/WebServiceURLDialog.fib");
	}

	@Test
	public void testWelcomePanel() {
		validateFIB("Fib/WelcomePanel.fib");
	}

}
