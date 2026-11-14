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
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;

/**
 * A local variable cannot redeclare a local visible where it is declared (CORE-D-48): validation reports exactly the four behaviours
 * of {@code LocalRedeclaration.fml} whose name starts with {@code redeclared}; sibling blocks, sibling loops and a local hiding a
 * property are valid.
 */
public class TestLocalRedeclarationValidation extends FMLScriptParserTestCase {

	@Test
	public void redeclarationsAreReported() throws Exception {
		instanciateTestServiceManager();
		VirtualModel vm = serviceManager.getVirtualModelLibrary()
				.getVirtualModel("http://openflexo.org/test/TestResourceCenter/LocalRedeclaration.fml");
		assertNotNull(vm);
		ValidationReport report = validate(vm);
		Set<String> reported = new TreeSet<>();
		for (ValidationError<?, ?> error : report.getAllErrors()) {
			Object validable = error.getValidable();
			assertTrue("Unexpected error on " + validable, validable instanceof FMLControlGraph);
			FMLControlGraphOwner owner = ((FMLControlGraph) validable).getRootOwner();
			reported.add(owner instanceof FlexoBehaviour ? ((FlexoBehaviour) owner).getName() : String.valueOf(validable));
		}
		Set<String> expected = new TreeSet<>();
		expected.add("redeclaredInBlock");
		expected.add("redeclaredInSameBlock");
		expected.add("redeclaredByLoopVariable");
		expected.add("redeclaredInLoopBody");
		assertEquals(expected, reported);
		assertEquals(4, report.getAllErrors().size());
	}
}
