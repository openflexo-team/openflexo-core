/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Fml-cli-test, a component of the software infrastructure 
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.rm.ResourceLocator;

/**
 * Known defect CORE-D-101: loading first a unit that is itself in an import cycle (DerivedA of <code>FML/CrossImports.fml</code>, which
 * imports Core and Base, which import it back) leaves <code>selected.declaringElement</code> unresolved in Core, so the
 * <code>where</code> clause of <code>applicableDerivedA</code> selects nothing. The same units loaded from CrossImports, outside the
 * cycle, are correct ({@link TestCrossImportsLoading}). The <code>load -r</code> command of a script does not reproduce it.<br>
 * Asserts the correct behaviour: fails until the defect is fixed. Skipped unless the environment variable {@code OPENFLEXO_KNOWN_DEFECTS}
 * is set:
 * 
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-cli-test:cleanTest :fml-cli-test:test --tests '*KnownDefectsCrossImportsFromACycle*'
 * </pre>
 * 
 * @author sylvain
 */
public class KnownDefectsCrossImportsFromACycleTest extends FMLScriptParserTestCase {

	@Test
	public void loadingFromAUnitOfTheCycleResolvesItsReferences() throws Exception {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
		instanciateTestServiceManager();
		FlexoResource<?> derivedA = serviceManager.getResourceManager()
				.getResource("http://openflexo.org/test/TestResourceCenter/CrossImports.fml/DerivedA.fml");
		assertNotNull(derivedA);
		derivedA.getResourceData();

		CommandInterpreter commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		FMLScript script = parseFMLScript(ResourceLocator.locateResource("TestResourceCenter/AutomatedTests/TestCrossImports.fmlscript"),
				commandInterpreter);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}
}
