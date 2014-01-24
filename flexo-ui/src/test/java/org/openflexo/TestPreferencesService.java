/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test PreferencesService
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPreferencesService extends OpenflexoTestCaseWithGUI {

	private static final Logger logger = FlexoLogger.getLogger(TestPreferencesService.class.getPackage().getName());

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
	public void testPreferencesService() {
		instanciateTestServiceManager();
		assertNotNull(serviceManager.getPreferencesService());
		assertNotNull(serviceManager.getPreferencesService().getPreferencesFactory());
	}
}
