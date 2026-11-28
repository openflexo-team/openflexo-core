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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingPlan;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Loading <code>FML/CrossImports.fml</code> through a {@link ResourceLoadingPlan} (the double-click of the application) gives the same
 * units as loading it directly: Core, Base, DerivedA and DerivedB import each other, so none of them is a step of its own. Loaded alone
 * first, DerivedA became the root of the loading of the cycle and left <code>selected.declaringElement</code> unresolved in Core
 * (CORE-D-14).<br>
 * Runs <code>AutomatedTests/TestCrossImports.fmlscript</code>, which asserts the values, once the plan is executed.
 *
 * @author sylvain
 */
public class TestCrossImportsLoadingPlan extends FMLScriptParserTestCase {

	@Test
	public void loadingThroughThePlanResolvesUnitsImportingEachOther() throws Exception {
		instanciateTestServiceManager();
		FlexoResource<?> crossImports = serviceManager.getResourceManager()
				.getResource("http://openflexo.org/test/TestResourceCenter/CrossImports.fml");
		assertNotNull(crossImports);

		ResourceLoadingPlan plan = ResourceLoadingPlan.compute(crossImports);
		List<FlexoResource<?>> steps = plan.getResources();
		assertEquals("Only the unit outside the cycle is a step: " + steps, 1, steps.size());
		assertEquals(crossImports, steps.get(0));
		plan.execute();

		CommandInterpreter commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		Resource script = ResourceLocator.locateResource("TestResourceCenter/AutomatedTests/TestCrossImports.fmlscript");
		assertNotNull(script);
		FMLScript fmlScript = parseFMLScript(script, commandInterpreter);
		try {
			fmlScript.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}
}
