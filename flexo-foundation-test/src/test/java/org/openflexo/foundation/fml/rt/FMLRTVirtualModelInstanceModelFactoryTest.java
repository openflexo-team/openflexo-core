/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.logging.Logger;

import org.junit.Test;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test instanciation of VirtualModelInstanceModelFactory<br>
 * 
 */
public class FMLRTVirtualModelInstanceModelFactoryTest extends OpenflexoTestCase {

	private static final Logger logger = FlexoLogger.getLogger(FMLRTVirtualModelInstanceModelFactoryTest.class.getPackage().getName());

	@Test
	public void testInstantiateVirtualModelModelFactory() {
		try {
			instanciateTestServiceManager();
			System.out.println("Instanciating FMLRTVirtualModelInstanceModelFactory");
			FMLRTVirtualModelInstanceModelFactory factory = new FMLRTVirtualModelInstanceModelFactory(null, null,
					serviceManager.getTechnologyAdapterService());
			PamelaMetaModel pamelaMetaModel = factory.getModelContext();
			for (Iterator<ModelEntity> it = pamelaMetaModel.getEntities(); it.hasNext();) {
				System.out.println("> Found " + it.next().getImplementedInterface());
			}
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
