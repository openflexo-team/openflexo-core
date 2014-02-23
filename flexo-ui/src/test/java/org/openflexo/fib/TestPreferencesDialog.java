package org.openflexo.fib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.OpenflexoTestCaseWithGUI;
import org.openflexo.TestApplicationContext;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.fib.testutils.FIBDialogGraphicalContextDelegate;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test the ReviewUnsavedDialog widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPreferencesDialog extends OpenflexoTestCaseWithGUI {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	protected static FlexoServiceManager instanciateTestServiceManager() {
		serviceManager = new TestApplicationContext() {
			@Override
			protected PreferencesService createPreferencesService() {
				return new PreferencesService();
			}
		};
		resourceCenter = (DirectoryResourceCenter) serviceManager.getResourceCenterService().getResourceCenters().get(0);
		return serviceManager;
	}

	@Test
	@TestOrder(1)
	public void testInstanciateTestServiceManager() {
		instanciateTestServiceManager();
	}

	@Test
	@TestOrder(2)
	public void testInstanciateWidget() {
		PreferencesDialog dialog = PreferencesDialog.getPreferencesDialog(serviceManager, null);

		log("instanciated " + dialog);
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, PreferencesDialog.PREFERENCES_FIB);
	}

	@Before
	public void setUp() {
		if (gcDelegate != null) {
			gcDelegate.setUp();
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (gcDelegate != null) {
			gcDelegate.tearDown();
		}
		super.tearDown();
	}

}
