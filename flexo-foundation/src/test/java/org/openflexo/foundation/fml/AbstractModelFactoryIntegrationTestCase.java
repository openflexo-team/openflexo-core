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
package org.openflexo.foundation.fml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
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
			FMLModelFactory factory = new FMLModelFactory(null, null, taService);
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
