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

package org.openflexo.components.wizard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.gina.swing.test.FIBDialogGraphicalContextDelegate;
import org.openflexo.gina.test.OpenflexoFIBTestCase;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test Wizard
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoWizard extends OpenflexoFIBTestCase {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	// private static Resource fibResource;

	static FlexoEditor editor;

	private static Wizard wizard;
	private static WizardStep step1;
	private static WizardStep step2;
	private static WizardStep step3;

	/*@BeforeClass
	public static void setupClass() {
		// instanciateTestServiceManager();
		initGUI();
	}*/

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void buildWizard() {

		wizard = new FlexoWizard(null) {

			@Override
			public String getWizardTitle() {
				return "Wizard test";
			}

		};

		step1 = new WizardStep1();
		wizard.addStep(step1);

		step2 = new WizardStep2();
		wizard.addStep(step2);

		step3 = new WizardStep3();
		wizard.addStep(step3);

		assertFalse(step1.isValid());
		assertFalse(step2.isValid());
		assertTrue(step3.isValid());

		assertFalse(wizard.canFinish());

	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testDisplayWizard() {
		WizardDialog dialog = new WizardDialog(wizard, null);

		System.out.println("File: " + WizardDialog.FIB_FILE);

		Resource sourceCodeRes = ResourceLocator.locateSourceCodeResource(WizardDialog.FIB_FILE);

		System.out.println("sourceCodeRes=" + sourceCodeRes);
		System.out.println("sourceCodeRes.getLocator()=" + sourceCodeRes.getLocator());

		// FIBDialog dialog = FIBDialog.instanciateDialog(step1.getFIBComponent(), step1, null, true, (LocalizedDelegate) null);
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, WizardDialog.FIB_FILE/*.getLocator().retrieveResourceAsFile(
																						WizardDialog.FIB_FILE)*/);

		/*FIBJPanel<FlexoBehaviour> widget = instanciateFIB(fibResource, creationScheme, FlexoBehaviour.class);
		
		gcDelegate.addTab("CreationScheme", widget.getController());*/
	}

}
