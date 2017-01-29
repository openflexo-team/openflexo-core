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

package org.openflexo.components.widget;

import org.junit.Test;
import org.openflexo.gina.utils.OpenflexoGinaSwingEditorTestCase;

/**
 * Test the structural and behavioural features of {@link FIBDescriptionWidget}
 * 
 * @author sylvain
 * 
 */
public class TestDescriptionWidgetEDITOR extends OpenflexoGinaSwingEditorTestCase {

	@Test
	public void testComponent() {
		// TODO: fix controller instantiation
	}

	/*@Test
	public void testComponent()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
	
		ModelFactory factory = null;
		try {
			factory = new ViewPointModelFactory(null, null);
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ViewPoint object2 = factory.newInstance(ViewPoint.class);
		object2.setDescription("Here comes a description for the the object");
		object2.setHasSpecificDescriptions(true);
		object2.setSpecificDescriptionsForKey("a description for the first key", "key1");
		object2.setSpecificDescriptionsForKey("a description for the second key", "key2");
	
		instanciateFIBEdition("TestDescriptionWidget", FIBDescriptionWidget.FIB_FILE, object2);
	}*/

	/*private static SwingGraphicalContextDelegate gcDelegate;
	
	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}
	
	@Test
	public void test1InstanciateWidget() throws ModelDefinitionException {
	
		ModelFactory factory = new ViewPointModelFactory(null, null);
		ViewPoint anObject = factory.newInstance(ViewPoint.class);
		FIBDescriptionWidget descriptionWidget = new FIBDescriptionWidget(anObject);
		gcDelegate.addTab("FIBDescriptionWidget", descriptionWidget.getController());
	}
	
	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestDescriptionWidgetEDITOR.class.getSimpleName());
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
			factory = new ViewPointModelFactory(null, null);
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
			public Resource getFIBResource() {
				return ResourceLocator.locateSourceCodeResource(FIBDescriptionWidget.FIB_FILE);
			}
	
			@Override
			public FIBController makeNewController(FIBComponent fibComponent) {
				return widget.new DescriptionWidgetFIBController(fibComponent);
			}
		};
		editor.launch();
	}
	 */
}
