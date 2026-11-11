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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * Runs the FML-scripts of {@code TestResourceCenter/KnownDefects}: one script per known defect of KNOWN_DEFECTS.md, named after
 * its identifier, which asserts the behaviour the language intends. Each script fails until its defect is fixed; the fix then
 * moves the script to {@code AutomatedTests}.
 * <p>
 * Skipped unless the environment variable {@value #ENABLING_VARIABLE} is set, so that the ordinary test suite stays readable:
 * 
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-cli-test:cleanTest :fml-cli-test:test --tests '*KnownDefects*'
 * </pre>
 */
@RunWith(Parameterized.class)
public class KnownDefectsTests extends FMLScriptParserTestCase {

	static final String ENABLING_VARIABLE = "OPENFLEXO_KNOWN_DEFECTS";

	static void assumeKnownDefectsEnabled() {
		Assume.assumeTrue("Known defects are run only when " + ENABLING_VARIABLE + " is set", System.getenv(ENABLING_VARIABLE) != null);
	}

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("TestResourceCenter/KnownDefects"), ".fmlscript");
	}

	@BeforeClass
	public static void checkEnabled() {
		assumeKnownDefectsEnabled();
	}

	private final Resource fmlResource;

	public KnownDefectsTests(Resource fmlResource, String name) {
		this.fmlResource = fmlResource;
	}

	@Test
	public void checkScript() throws ModelDefinitionException, ParseException, IOException, FMLCommandExecutionException {
		instanciateTestServiceManager();
		CommandInterpreter commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		FMLScript script = parseFMLScript(fmlResource, commandInterpreter);
		assertNotNull(script);
		checkFMLScript(fmlResource.getRelativePath(), script);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}
}
