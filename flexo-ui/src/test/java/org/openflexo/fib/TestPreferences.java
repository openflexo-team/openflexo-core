package org.openflexo.fib;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.TestApplicationContext;
import org.openflexo.fib.controller.FIBController;
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileResource;

/**
 * Test the structural and behavioural features of FIBTextField widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPreferences extends FIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static TestApplicationContext applicationContext;

	@Test
	@TestOrder(1)
	public void instanciateTestServiceManager() {
		applicationContext = new TestApplicationContext() {
			@Override
			protected PreferencesService createPreferencesService() {
				return new PreferencesService();
			}
		};
	}

	@Test
	@TestOrder(2)
	public void testInstanciateGeneralPreferences() {
		File generalPreferences = new FileResource("Fib/Prefs/GeneralPreferences.fib");

		FIBController controller = FIBController.instanciateController(FIBLibrary.instance().retrieveFIBComponent(generalPreferences),
				FlexoLocalization.getMainLocalizer());
		assertNotNull(controller);
		controller.setDataObject(applicationContext.getGeneralPreferences());
		controller.buildView();

		gcDelegate.addTab("General Preferences", controller);

	}

	@Test
	@TestOrder(3)
	public void testInstanciateAdvancedPrefs() {
		File advancedPrefs = new FileResource("Fib/Prefs/AdvancedPrefs.fib");

		FIBController controller = FIBController.instanciateController(FIBLibrary.instance().retrieveFIBComponent(advancedPrefs),
				FlexoLocalization.getMainLocalizer());
		assertNotNull(controller);
		controller.setDataObject(applicationContext.getAdvancedPrefs());
		controller.buildView();

		gcDelegate.addTab("Advanced Prefs", controller);

	}

	@BeforeClass
	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestPreferences.class.getSimpleName());
	}

	@AfterClass
	public static void waitGUI() {
		gcDelegate.waitGUI();
	}

	@Before
	public void setUp() {
		gcDelegate.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		gcDelegate.tearDown();
	}

}
