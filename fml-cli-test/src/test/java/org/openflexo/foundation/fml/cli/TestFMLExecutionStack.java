/**
 * 
 * Copyright (c) 2026, Openflexo
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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.rt.error.FMLExecutionFailure;
import org.openflexo.foundation.fml.rt.error.FMLStack;
import org.openflexo.foundation.fml.rt.error.FMLStackFrame;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * The FML stack of a failure, and a return in a creation or a deletion scheme (CORE-F-11). Runs the FML-scripts of
 * {@code TestResourceCenter/ExecutionStack} command by command, on the fixture {@code FML/ExecutionFailures.fml}: the line numbers
 * asserted here are the ones of that fixture.
 */
public class TestFMLExecutionStack extends FMLScriptParserTestCase {

	private static final String MUST_FAIL = "mustFail";

	/**
	 * Run supplied script; return the failure of its command assigning {@code mustFail}, or null when it has none (any other failure
	 * fails the test)
	 */
	private Exception runScript(String name) throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		Resource fmlResource = ResourceLocator.locateResource("TestResourceCenter/ExecutionStack/" + name);
		FMLScript script = parseFMLScript(fmlResource, interpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		Exception failure = null;
		for (AbstractCommand command : script.getCommands()) {
			if (command.toString().trim().startsWith(MUST_FAIL)) {
				try {
					command.execute();
				} catch (Exception e) {
					failure = e;
					continue;
				}
				fail(name + ", line " + command.getLine() + ": " + command + " did not fail");
			}
			else {
				command.execute();
			}
		}
		return failure;
	}

	private static List<FMLStackFrame> framesOf(Exception failure) {
		assertNotNull("No failure", failure);
		FMLStack stack = FMLExecutionFailure.findFMLStack(failure);
		assertNotNull("The failure carries no FML stack: " + failure, stack);
		System.out.println("FML stack of " + failure + ":\n" + stack);
		return stack.getFrames();
	}

	private static void assertFrame(FMLStackFrame frame, String behaviour, String concept, int line) {
		assertEquals(frame.toString(), behaviour, frame.getBehaviour());
		assertEquals(frame.toString(), concept, frame.getConceptName());
		assertEquals(frame.toString(), line, frame.getLine());
		assertNotNull(frame.toString(), frame.getStatement());
	}

	@Test
	public void nestedFailureHasOneFramePerBehaviour() throws Exception {
		List<FMLStackFrame> frames = framesOf(runScript("NestedFailure.fmlscript"));
		assertEquals(frames.toString(), 3, frames.size());
		assertFrame(frames.get(0), "inner()", "ExecutionFailures", 88);
		assertTrue(frames.get(0).getStatement(), frames.get(0).getStatement().contains("absent.substring"));
		assertFrame(frames.get(1), "middle()", "ExecutionFailures", 93);
		assertFrame(frames.get(2), "nested()", "ExecutionFailures", 97);
	}

	@Test
	public void creationSchemeFailureHasTheFrameOfTheScheme() throws Exception {
		List<FMLStackFrame> frames = framesOf(runScript("CreationFailure.fmlscript"));
		assertEquals(frames.toString(), 2, frames.size());
		assertFrame(frames.get(0), "create()", "Bomb", 126);
		assertTrue(frames.get(0).getInstance(), frames.get(0).getInstance().startsWith("Bomb#"));
		assertFrame(frames.get(1), "outer()", "ExecutionFailures", 15);
	}

	@Test
	public void returnEndsCreationAndDeletionSchemes() throws Exception {
		assertEquals(null, runScript("ReturnInSchemes.fmlscript"));
	}
}
