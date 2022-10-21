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

package org.openflexo;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openflexo.gina.test.OpenflexoTestCaseWithGUI;
import org.openflexo.gina.test.TestApplicationContext;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.test.UITest;

/**
 * Test PreferencesService
 * 
 */
public class TestPreferencesService extends OpenflexoTestCaseWithGUI {

	@SuppressWarnings("unused")
	private static final Logger logger = FlexoLogger.getLogger(TestPreferencesService.class.getPackage().getName());

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
		 * for (FlexoResourceCenter<?> rc :
		 * serviceManager.getResourceCenterService().getResourceCenters()) { //
		 * Select the first directory ResourceCenter if (rc instanceof
		 * DirectoryResourceCenter) { resourceCenter = (DirectoryResourceCenter)
		 * rc; break; } }
		 */
		return serviceManager;
	}

	@Test
	@Category(UITest.class)
	public void testPreferencesService() {
		instanciateTestServiceManager();
		assertNotNull(serviceManager.getPreferencesService());
		assertNotNull(serviceManager.getPreferencesService().getPreferencesFactory());

	}
}
