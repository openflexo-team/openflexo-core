/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Cartoeditor, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;

/**
 * The models of {@code InheritedCreation.fml} (inherited creation scheme and default values) and {@code PropertyInitializers.fml}
 * (initializers, an edition action as initializer, cardinality) validate without error (CORE-D-15, CORE-D-50, CORE-D-51). Their
 * execution is checked by {@code TestInheritedCreation.fmlscript} and {@code TestPropertyInitializers.fmlscript}.
 */
public class TestPropertyInitializersValidation extends FMLScriptParserTestCase {

	private void assertValid(String uri) throws Exception {
		instanciateTestServiceManager();
		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(uri);
		assertNotNull(vm);
		assertEquals(uri, 0, validate(vm).getErrorsCount());
	}

	@Test
	public void inheritedCreationValidates() throws Exception {
		assertValid("http://openflexo.org/test/TestResourceCenter/InheritedCreation.fml");
	}

	@Test
	public void propertyInitializersValidate() throws Exception {
		assertValid("http://openflexo.org/test/TestResourceCenter/PropertyInitializers.fml");
	}
}
