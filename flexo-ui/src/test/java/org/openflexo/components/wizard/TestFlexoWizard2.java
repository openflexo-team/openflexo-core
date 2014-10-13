package org.openflexo.components.wizard;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fib.testutils.FIBDialogGraphicalContextDelegate;
import org.openflexo.fib.utils.OpenflexoFIBTestCase;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test Wizard
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoWizard2 extends OpenflexoFIBTestCase {

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
	public void buildWizard() {

		wizard = new FlexoWizard(null) {

			@Override
			public String getWizardTitle() {
				return "Wizard test";
			}

		};

		step1 = new WizardStep1() {
			@Override
			public boolean isTransitionalStep() {
				return true;
			}

			@Override
			public void performTransition() {
				System.out.println("perform transition");
				step2 = new WizardStep2();
				wizard.addStep(step2);

				step3 = new WizardStep3();
				wizard.addStep(step3);
			}

			@Override
			public void discardTransition() {
				System.out.println("discard transition");
			}
		};
		wizard.addStep(step1);

		assertFalse(step1.isValid());

		assertFalse(wizard.canFinish());

	}

	@Test
	@TestOrder(2)
	public void testDisplayWizard() {

		// DeprecatedWizardDialog dialog = new DeprecatedWizardDialog(null, wizard);
		// dialog.setVisible(true);

		WizardDialog dialog = new WizardDialog(wizard);

		System.out.println("File: " + WizardDialog.FIB_FILE);

		Resource sourceCodeRes = ResourceLocator.locateSourceCodeResource(WizardDialog.FIB_FILE);

		System.out.println("sourceCodeRes=" + sourceCodeRes);
		System.out.println("sourceCodeRes.getLocator()=" + sourceCodeRes.getLocator());

		// FIBDialog dialog = FIBDialog.instanciateDialog(step1.getFIBComponent(), step1, null, true, (LocalizedDelegate) null);
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, WizardDialog.FIB_FILE/*.getLocator().retrieveResourceAsFile(
																						WizardDialog.FIB_FILE)*/);
	}

}
