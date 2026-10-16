/**
 * 
 * Copyright (c) 2026, Openflexo
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Loading a compilation unit must leave it unmodified: what is loaded from disk and not edited is not to be saved.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestLoadDoesNotModify extends OpenflexoTestCase {

	public static final String CONTAINER_URI = "http://openflexo.org/test/TestResourceCenter/PingPong.fml";
	public static final String CROSS_IMPORTS_URI = "http://openflexo.org/test/TestResourceCenter/CrossImports.fml";

	@Test
	@TestOrder(1)
	public void testContainedUnitsAreNotModifiedByLoading() throws Exception {
		instanciateTestServiceManager();

		assertNotModified(CONTAINER_URI);
		assertNotModified(CROSS_IMPORTS_URI);
	}

	private void assertNotModified(String containerURI) throws Exception {

		VirtualModel container = serviceManager.getVirtualModelLibrary().getVirtualModel(containerURI);
		assertNotNull(container);
		assertFalse("Loading " + containerURI + " left it modified", container.getCompilationUnit().isModified());

		for (VirtualModel contained : container.getVirtualModels()) {
			assertFalse("Loading " + contained.getURI() + " left it modified", contained.getCompilationUnit().isModified());
		}
	}

}
