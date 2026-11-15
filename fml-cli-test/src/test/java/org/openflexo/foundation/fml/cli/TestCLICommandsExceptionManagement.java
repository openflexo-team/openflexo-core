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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLInvalidCommandException;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.rt.error.FMLHostError;
import org.openflexo.foundation.fml.rt.error.FMLTypeError;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test command parser
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestCLICommandsExceptionManagement extends OpenflexoTestCase {

	private static final Logger logger = Logger.getLogger(TestCLICommandsExceptionManagement.class.getPackage().getName());

	private static CommandInterpreter commandInterpreter;

	private static FlexoResourceCenterService rcService;
	private static FlexoResourceCenter<?> testResourcesRC;

	@BeforeClass
	public static void initialize() throws IOException {
		instanciateTestServiceManager();
		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		rcService = commandInterpreter.getServiceManager().getResourceCenterService();
		FlexoResourceCenter<?> existingResourcesRC = rcService.getFlexoResourceCenter("http://openflexo.org/test/flexo-test-resources");
		logger.info("Copying all files from " + existingResourcesRC);
		testResourcesRC = makeNewDirectoryResourceCenterFromExistingResourceCenter(serviceManager, existingResourcesRC);
		logger.info("Now working with " + testResourcesRC);
	}

	/**
	 * Execute 's = "Hello world !"'
	 */
	@Test
	@TestOrder(10)
	public void assignString() throws ParseException, FMLCommandExecutionException {
		log("assignString()");

		AbstractCommand assignS = CommandParser.parse("s = \"Hello world !\"", commandInterpreter);
		assignS.execute();

		AbstractCommand context = CommandParser.parse("context", commandInterpreter);
		context.execute();

	}

	/**
	 * Execute 'assert s.substring(2,4) == "ll"'
	 */
	@Test
	@TestOrder(11)
	public void assertSuccess() throws ParseException, FMLCommandExecutionException {
		log("assertSuccess()");

		AbstractCommand assertCommand = CommandParser.parse("assert s.substring(2,4) == \"ll\"", commandInterpreter);
		assertCommand.execute();

	}

	/**
	 * Execute 'assert s.substring(2,4) == "lll"'
	 */
	@Test
	@TestOrder(12)
	public void assertFails() throws ParseException, FMLCommandExecutionException {
		log("assertFails()");

		AbstractCommand assertCommand = CommandParser.parse("assert s.substring(2,4) == \"lll\"", commandInterpreter);
		try {
			assertCommand.execute();
		} catch (FMLAssertException e) {
			return;
		}

		fail("This assertion must fail");

	}

	/**
	 * Execute 's.substring(1,42)'
	 */
	@Test
	@TestOrder(13)
	public void assertExceptionIsThrownInExpression() throws ParseException, FMLCommandExecutionException {
		log("assertExceptionIsThrownInExpression()");

		AbstractCommand assertCommand = CommandParser.parse("s.substring(1,42)", commandInterpreter);
		try {
			assertCommand.execute();
		} catch (FMLCommandExecutionException e) {
			// The failure of Java code called from FML: a foreseen FMLHostError, keeping the Java exception as its cause (CORE-F-11)
			assertTrue(String.valueOf(e.getFailure()), e.getFailure() instanceof FMLHostError);
			assertTrue(e.getFailure().getCause() instanceof StringIndexOutOfBoundsException);
			assertTrue(e.describe(), e.describe().startsWith("Command failed: Failure of Java code: "));
			return;
		}

		fail("This command must throw a StringIndexOfBoundsException");

	}

	/**
	 * Execute 'log s.substring(1,42)'
	 */
	@Test
	@TestOrder(14)
	public void assertExceptionIsThrownInLog() throws ParseException, FMLCommandExecutionException {
		log("assertExceptionIsThrownInLog()");

		AbstractCommand assertCommand = CommandParser.parse("log s.substring(1,42)", commandInterpreter);
		try {
			assertCommand.execute();
		} catch (FMLCommandExecutionException e) {
			if (e.getCause() instanceof FMLExecutionException) {
				if (((FMLExecutionException) e.getCause()).getCause() instanceof StringIndexOutOfBoundsException) {
					// That's right
					return;
				}
			}
		}

		fail("This command must throw a StringIndexOfBoundsException embedded in a FMLExecutionException");

	}

	/**
	 * Execute 'assert false': the message of a failed assertion is never null, even when its expression is not a binary one (CORE-F-11)
	 */
	@Test
	@TestOrder(15)
	public void assertFailsWithoutBinaryExpression() throws ParseException, FMLCommandExecutionException {
		log("assertFailsWithoutBinaryExpression()");

		AbstractCommand assertCommand = CommandParser.parse("assert false", commandInterpreter);
		try {
			assertCommand.execute();
		} catch (FMLAssertException e) {
			assertEquals("assert failed: false", e.getMessage());
			assertEquals(-1, e.getLine());
			assertTrue(e.isForeseen());
			return;
		}
		fail("This assertion must fail");
	}

	/**
	 * Execute 'assert s.substring(2,4) == "lll"': the message shows the value of each operand that is not a constant
	 */
	@Test
	@TestOrder(16)
	public void assertMessageShowsOperands() throws ParseException, FMLCommandExecutionException {
		log("assertMessageShowsOperands()");

		AbstractCommand assertCommand = CommandParser.parse("assert s.substring(2,4) == \"lll\"", commandInterpreter);
		try {
			assertCommand.execute();
		} catch (FMLAssertException e) {
			assertEquals("assert failed: s.substring(2,4)[\"ll\"] == \"lll\"", e.getMessage());
			return;
		}
		fail("This assertion must fail");
	}

	/**
	 * Execute 'assert s': an assertion on a value that is not a boolean is a type error, not an internal one
	 */
	@Test
	@TestOrder(17)
	public void assertOnNonBoolean() throws ParseException {
		log("assertOnNonBoolean()");

		try {
			CommandParser.parse("assert s", commandInterpreter).execute();
		} catch (FMLCommandExecutionException e) {
			assertTrue(String.valueOf(e.getFailure()), e.getFailure() instanceof FMLTypeError);
			return;
		}
		fail("This assertion must fail");
	}

	/**
	 * An invalid command is an {@link FMLInvalidCommandException}, its reason being the output
	 */
	@Test
	@TestOrder(18)
	public void invalidCommand() throws ParseException, FMLCommandExecutionException {
		log("invalidCommand()");

		try {
			commandInterpreter.executeCommand("unknownVariable.foo()");
		} catch (FMLInvalidCommandException e) {
			assertNotNull(e.getMessage());
			assertEquals(e.getMessage(), commandInterpreter.getOutput().get(0).trim());
			return;
		}
		fail("This command must be invalid");
	}

	/**
	 * The interactive loop survives any failure of a command, a defect of the platform included: it is described on the error stream
	 */
	@Test
	@TestOrder(19)
	public void interactiveLoopSurvivesRuntimeException() throws IOException {
		log("interactiveLoopSurvivesRuntimeException()");

		ByteArrayOutputStream err = new ByteArrayOutputStream();
		boolean[] broken = { true };
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, new PrintStream(err, true), HOME_DIR) {
			@Override
			protected AbstractCommand makeCommand(String commandString) throws ParseException {
				if (broken[0]) {
					throw new IllegalStateException("broken interpreter");
				}
				return super.makeCommand(commandString);
			}
		};
		assertNull(interpreter.executeAndReport("x = 1"));
		assertTrue(err.toString(), err.toString().contains("Unexpected failure: Internal error: broken interpreter"));

		// The interpreter goes on
		broken[0] = false;
		err.reset();
		assertNotNull(interpreter.executeAndReport("x = 1"));
		assertNotNull(interpreter.executeAndReport("assert x == 1"));
		assertNull(interpreter.executeAndReport("assert x == 2"));
		assertTrue(err.toString(), err.toString().contains("assert failed: x[1] == 2"));
		assertNotNull(interpreter.executeAndReport("y = \"abc\""));
		assertNull(interpreter.executeAndReport("y.substring(1,42)"));
		assertTrue(err.toString(), err.toString().contains("Failure of Java code"));
	}

}
