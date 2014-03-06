package org.openflexo.components.widget;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.fib.controller.FIBController;
import org.openflexo.fib.editor.FIBAbstractEditor;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointModelFactory;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.ResourceLocator;

/**
 * Test the structural and behavioural features of FIBOntologyBrowser
 * 
 * @author sylvain
 * 
 */
public class TestDescriptionWidget extends OpenflexoTestCase {

	private static GraphicalContextDelegate gcDelegate;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	public void test1InstanciateWidget() throws ModelDefinitionException {

		ModelFactory factory = new ViewPointModelFactory();
		ViewPoint anObject = factory.newInstance(ViewPoint.class);
		FIBDescriptionWidget descriptionWidget = new FIBDescriptionWidget(anObject);
		gcDelegate.addTab("FIBDescriptionWidget", descriptionWidget.getController());
	}

	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestDescriptionWidget.class.getSimpleName());
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

	public static void main(String[] args) {
		ModelFactory factory = null;
		try {
			factory = new ViewPointModelFactory();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ViewPoint object1 = factory.newInstance(ViewPoint.class);
		object1.setDescription("This is the first object description");
		final ViewPoint object2 = factory.newInstance(ViewPoint.class);
		object2.setDescription("Here comes a description for the second object");
		object2.setHasSpecificDescriptions(true);
		object2.setSpecificDescriptionsForKey("a description for the first key", "key1");
		object2.setSpecificDescriptionsForKey("a description for the second key", "key2");

		final FIBDescriptionWidget widget = new FIBDescriptionWidget(object1);

		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				return makeArray(object1, object2);
			}

			@Override
			public File getFIBFile() {
				return ResourceLocator.locateFile(FIBDescriptionWidget.FIB_FILE_NAME);
			}

			@Override
			public FIBController makeNewController(FIBComponent fibComponent) {
				return widget.new DescriptionWidgetFIBController(fibComponent);
			}
		};
		editor.launch();
	}

}
