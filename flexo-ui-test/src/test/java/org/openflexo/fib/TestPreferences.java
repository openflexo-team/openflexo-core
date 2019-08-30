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

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.test.FIBTestCase;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.gina.test.TestApplicationContext;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test the structural and behavioural features of FIBTextField widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPreferences extends FIBTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	private static TestApplicationContext applicationContext;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
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
	@Category(UITest.class)
	public void testInstanciateGeneralPreferences() {
		Resource generalPreferences = ResourceLocator.locateResource("Fib/Prefs/GeneralPreferences.fib");

		FIBController controller = FIBController.instanciateController(
				ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(generalPreferences), SwingViewFactory.INSTANCE,
				FlexoLocalization.getMainLocalizer());
		assertNotNull(controller);
		controller.setDataObject(applicationContext.getGeneralPreferences());
		controller.buildView();

		gcDelegate.addTab("General Preferences", controller);

	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testInstanciateAdvancedPrefs() {
		Resource advancedPrefs = ResourceLocator.locateResource("Fib/Prefs/AdvancedPrefs.fib");

		FIBController controller = FIBController.instanciateController(
				ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(advancedPrefs), SwingViewFactory.INSTANCE,
				FlexoLocalization.getMainLocalizer());
		assertNotNull(controller);
		controller.setDataObject(applicationContext.getAdvancedPrefs());
		controller.buildView();

		gcDelegate.addTab("Advanced Prefs", controller);

	}

	@BeforeClass
	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestPreferences.class.getSimpleName());
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
		if (gcDelegate != null) {
			gcDelegate.tearDown();
		}
	}

}
