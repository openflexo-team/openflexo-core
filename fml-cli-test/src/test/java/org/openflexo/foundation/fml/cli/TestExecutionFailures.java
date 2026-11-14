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

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * A failure during an execution reaches the caller (CORE-D-47, CORE-D-8). Runs the FML-scripts of
 * {@code TestResourceCenter/ExecutionFailures} command by command: a command assigning {@code mustFail} must fail, and the commands
 * after it, run in the same context, state what the failure left behind (an assert that fails makes the test fail).
 */
@RunWith(Parameterized.class)
public class TestExecutionFailures extends FMLScriptParserTestCase {

	private static final String MUST_FAIL = "mustFail";

	/** The causes the fixture provokes: reading a method of a null property, or an invalid condition */
	private static final String[] EXPECTED_CAUSES = { "NullReferenceException", "Invalid condition" };

	private static boolean causeChainMentions(Throwable t, String[] markers) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			for (String marker : markers) {
				if (String.valueOf(c).contains(marker)) {
					return true;
				}
			}
			if (c.getCause() == c) {
				break;
			}
		}
		return false;
	}

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("TestResourceCenter/ExecutionFailures"), ".fmlscript");
	}

	private final Resource fmlResource;

	public TestExecutionFailures(Resource fmlResource, String name) {
		this.fmlResource = fmlResource;
	}

	@Test
	public void failureReachesTheCaller() throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		FMLScript script = parseFMLScript(fmlResource, interpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		int expectedFailures = 0;
		int failures = 0;
		for (AbstractCommand command : script.getCommands()) {
			if (command.toString().trim().startsWith(MUST_FAIL)) {
				expectedFailures++;
				try {
					command.execute();
				} catch (Exception e) {
					System.out.println("Expected failure at line " + command.getLine() + ": " + e);
					// The failure must be the one the fixture provokes (a null reference, or an invalid condition), not an unrelated
					// one such as an unresolved behaviour
					assertTrue(fmlResource.getRelativePath() + ", line " + command.getLine() + ": unexpected cause " + e,
							causeChainMentions(e, EXPECTED_CAUSES));
					failures++;
					continue;
				}
				assertTrue(fmlResource.getRelativePath() + ", line " + command.getLine() + ": " + command + " did not fail", false);
			}
			else {
				command.execute();
			}
		}
		assertTrue("No command assigning " + MUST_FAIL + " in " + fmlResource.getRelativePath(), expectedFailures > 0);
		assertTrue(failures == expectedFailures);
	}
}
