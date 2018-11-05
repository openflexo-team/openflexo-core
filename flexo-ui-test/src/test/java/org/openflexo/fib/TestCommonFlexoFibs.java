/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.gina.test.GenericFIBTestCase;
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
	public void testReviewUnsavedDialog() {
		validateFIB("Fib/Dialog/ReviewUnsavedDialog.fib");
	}

	@Test
	public void testFIBInformationSpaceBrowser() {
		validateFIB("Fib/FIBInformationSpaceBrowser.fib");
	}

	@Test
	public void testFIBResourceManagerBrowser() {
		validateFIB("Fib/FIBResourceManagerBrowser.fib");
	}

	@Test
	public void testFIBTechnologyBrowser() {
		validateFIB("Fib/FIBTechnologyBrowser.fib");
	}

	@Test
	public void testFMLConsoleViewer() {
		validateFIB("Fib/FMLConsoleViewer.fib");
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
	public void testPreferences() {
		validateFIB("Fib/Preferences.fib");
	}

	@Test
	public void testAdvancedPrefs() {
		validateFIB("Fib/Prefs/AdvancedPrefs.fib");
	}

	@Test
	public void testBugReportPreferences() {
		validateFIB("Fib/Prefs/BugReportPreferences.fib");
	}

	@Test
	public void testGeneralPreferences() {
		validateFIB("Fib/Prefs/GeneralPreferences.fib");
	}

	@Test
	public void testLoggingPreferences() {
		validateFIB("Fib/Prefs/LoggingPreferences.fib");
	}

	@Test
	public void testModuleLoaderPreferences() {
		validateFIB("Fib/Prefs/ModuleLoaderPreferences.fib");
	}

	@Test
	public void testOpenflexoPreferences() {
		validateFIB("Fib/Prefs/OpenflexoPreferences.fib");
	}

	@Test
	public void testPresentationPreferences() {
		validateFIB("Fib/Prefs/PresentationPreferences.fib");
	}

	@Test
	public void testResourceCenterPreferences() {
		validateFIB("Fib/Prefs/ResourceCenterPreferences.fib");
	}

	@Test
	public void testProjectSelector() {
		validateFIB("Fib/ProjectSelector.fib");
	}

	@Test
	public void testRepositoryFolderSelector() {
		validateFIB("Fib/RepositoryFolderSelector.fib");
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
