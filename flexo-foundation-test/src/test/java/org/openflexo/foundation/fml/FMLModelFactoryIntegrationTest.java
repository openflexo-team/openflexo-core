/**
 * 
 * Copyright (c) 2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.test.fml.AbstractModelFactoryIntegrationTestCase;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test instanciation of FMLModelFactory<br>
 * Here the model factory is instanciated with all FML and FML@RT technology adapters
 * 
 */
@RunWith(OrderedRunner.class)
public class FMLModelFactoryIntegrationTest extends AbstractModelFactoryIntegrationTestCase {

	private static final Logger logger = FlexoLogger.getLogger(FMLModelFactoryIntegrationTest.class.getPackage().getName());

	/**
	 * Instanciate test ServiceManager
	 */
	@Test
	@TestOrder(1)
	public void initializeServiceManager() {
		log("initializeServiceManager()");
		instanciateTestServiceManager();

		assertNotNull(serviceManager.getService(FlexoResourceCenterService.class));
		assertNotNull(serviceManager.getService(TechnologyAdapterService.class));

		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		assertEquals(taService, serviceManager.getService(TechnologyAdapterService.class));

		assertNotNull(taService.getTechnologyAdapter(FMLTechnologyAdapter.class));
		assertNotNull(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));
	}

	/**
	 * Check the presence of {@link FMLTechnologyAdapter}, instanciate FMLModelFactory with this TA
	 */
	@Test
	@TestOrder(2)
	public void checkFMLTechnologyAdapter() {
		log("checkFMLTechnologyAdapter()");

		testVirtualModelModelFactoryWithTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
				FMLTechnologyAdapter.class));
	}

	/**
	 * Check the presence of {@link FMLRTTechnologyAdapter}, instanciate FMLModelFactory with this TA
	 */
	@Test
	@TestOrder(3)
	public void checkFMLRTTechnologyAdapter() {
		log("checkFMLRTTechnologyAdapter()");

		testVirtualModelModelFactoryWithTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
				FMLRTTechnologyAdapter.class));

	}

}
