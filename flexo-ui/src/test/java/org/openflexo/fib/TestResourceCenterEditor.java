package org.openflexo.fib;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.components.ResourceCenterEditorDialog;
import org.openflexo.fib.testutils.FIBDialogGraphicalContextDelegate;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test the ReviewUnsavedDialog widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestResourceCenterEditor extends OpenflexoTestCase {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	@Test
	@TestOrder(1)
	public void testInstanciateTestServiceManager() {
		instanciateTestServiceManager(true);
		File newEmptyRC = new File(resourceCenter.getDirectory().getParent(), resourceCenter.getDirectory().getName() + "New");
		newEmptyRC.mkdirs();
		serviceManager.getResourceCenterService().addToResourceCenters(resourceCenter = new DirectoryResourceCenter(newEmptyRC));

	}

	@Test
	@TestOrder(2)
	public void testInstanciateWidget() {
		ResourceCenterEditorDialog dialog = ResourceCenterEditorDialog.getResourceCenterEditorDialog(serviceManager, null);

		log("instanciated " + dialog);
		System.out.println("rcs= " + serviceManager.getResourceCenterService().getResourceCenters());
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, ResourceCenterEditorDialog.RESOURCE_CENTER_EDITOR_FIB);
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
