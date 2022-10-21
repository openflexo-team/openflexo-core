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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.ApplicationContext;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.gina.swing.test.FIBDialogGraphicalContextDelegate;
import org.openflexo.gina.test.OpenflexoTestCaseWithGUI;
import org.openflexo.gina.test.TestApplicationContext;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test the ReviewUnsavedDialog widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPreferencesDialog extends OpenflexoTestCaseWithGUI {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	protected static ApplicationContext instanciateTestServiceManager() {
		serviceManager = new TestApplicationContext() {
			@Override
			protected PreferencesService createPreferencesService() {
				return new PreferencesService();
			}
			
			@Override
			protected void registerPreferencesService() {
				if (getPreferencesService() == null) {
					PreferencesService preferencesService = createPreferencesService();
					registerService(preferencesService);
				}
			}

		};
		/*
		 * for (FlexoResourceCenter rc :
		 * serviceManager.getResourceCenterService().getResourceCenters()) { //
		 * Select the first directory ResourceCenter if (rc instanceof
		 * DirectoryResourceCenter) { resourceCenter = (DirectoryResourceCenter)
		 * rc; break; } }
		 */
		return serviceManager;
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testInstanciateTestServiceManager() {
		instanciateTestServiceManager();
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
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
