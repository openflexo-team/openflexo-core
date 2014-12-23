package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestCommonFlexoFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib")).getFile(), "Fib/"));
	}

	@Test
	public void testAskResourceCenterDirectory() {
		validateFIB("Fib/AskResourceCenterDirectory.fib");
	}

	@Test
	public void testConflictedResourceEditor() {
		validateFIB("Fib/ConflictedResourceEditor.fib");
	}

	@Test
	public void testDescriptionWidget() {
		validateFIB("Fib/DescriptionWidget.fib");
	}

	@Test
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/Dialog/ReviewUnsavedDialog.fib");
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
	public void testFIBPropertySelector() {
		validateFIB("Fib/FIBPropertySelector.fib");
	}

	@Test
	public void testFIBTechnologyBrowser() {
		validateFIB("Fib/FIBTechnologyBrowser.fib");
	}

	@Test
	public void testFlexoCreateURL() {
		validateFIB("Fib/FlexoCreateURL.fib");
	}

	@Test
	public void testFlexoMarketEditor() {
		validateFIB("Fib/FlexoMarketEditor.fib");
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
	public void testOntologyView() {
		validateFIB("Fib/OntologyView.fib");
	}

	@Test
	public void testPreferences() {
		validateFIB("Fib/Preferences.fib");
	}

	@Test
	public void testAdvancedPrefs() {
		validateFIB("Fib/Prefs/AdvancedPrefs.fib");
	}

	@Test
	public void testGeneralPreferences() {
		validateFIB("Fib/Prefs/GeneralPreferences.fib");
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
	public void testResourceMissingEditor() {
		validateFIB("Fib/ResourceMissingEditor.fib");
	}

	@Test
	public void testResourceSelector() {
		validateFIB("Fib/ResourceSelector.fib");
	}

	@Test
	public void testSaveProjects() {
		validateFIB("Fib/SaveProjects.fib");
	}

	@Test
	public void testUndoManager() {
		validateFIB("Fib/UndoManager.fib");
	}

	@Test
	public void testWelcomePanel() {
		validateFIB("Fib/WelcomePanel.fib");
	}

	@Test
	public void testFIBProjectResourcesBrowser() {
		validateFIB("Fib/Widget/FIBProjectResourcesBrowser.fib");
	}

	@Test
	public void testWizardPanel() {
		validateFIB("Fib/WizardPanel.fib");
	}

}
