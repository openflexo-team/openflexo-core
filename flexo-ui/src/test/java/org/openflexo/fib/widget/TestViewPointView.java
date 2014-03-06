package org.openflexo.fib.widget;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.fib.utils.OpenflexoFIBTestCase;
import org.openflexo.fib.view.widget.DefaultFIBCustomComponent;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointLibrary;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.ResourceLocator;

/**
 * Test FlexoConceptPanel fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestViewPointView extends OpenflexoFIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static String fibFileName;

	static ViewPoint viewPoint;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	@TestOrder(1)
	public void testLoadWidget() {

		fibFileName = "Fib/VPM/ViewPointView.fib";
		assertTrue(ResourceLocator.locateFile(fibFileName).exists());
	}

	@Test
	@TestOrder(2)
	public void testValidateWidget() {

		validateFIB(fibFileName);
	}

	@Test
	@TestOrder(3)
	public void loadConcepts() {

		// instanciateTestServiceManager();

		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint1");
		assertNotNull(viewPoint);
		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		assertNotNull(virtualModel);

	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidget() {

		DefaultFIBCustomComponent<ViewPoint> widget = instanciateFIB(fibFileName, viewPoint, ViewPoint.class);

		gcDelegate.addTab("TestViewPoint1", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestViewPointView.class.getSimpleName());
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
