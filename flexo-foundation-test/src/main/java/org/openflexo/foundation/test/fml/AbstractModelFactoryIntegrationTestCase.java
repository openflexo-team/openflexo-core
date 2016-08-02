/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.test.fml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.exceptions.MissingImplementationException;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * Abstract test case for FMLModelFactory<br>
 * 
 */
public class AbstractModelFactoryIntegrationTestCase extends OpenflexoTestCase {

	private static final Logger logger = FlexoLogger.getLogger(AbstractModelFactoryIntegrationTestCase.class.getPackage().getName());

	protected void testVirtualModelModelFactoryWithTechnologyAdapter(TechnologyAdapter ta) {
		assertNotNull(ta);
		try {
			System.out.println("Instanciating FMLModelFactory integrating technology adapter " + ta);
			TechnologyAdapterService taService = DefaultTechnologyAdapterService.getNewInstance(null);
			taService.addToTechnologyAdapters(ta);
			FMLModelFactory factory = new FMLModelFactory(null, serviceManager);
			for (Class<?> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				assertNotNull(factory.getModelContext().getModelEntity(modelSlotClass));
			}
			factory.checkMethodImplementations();

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MissingImplementationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
