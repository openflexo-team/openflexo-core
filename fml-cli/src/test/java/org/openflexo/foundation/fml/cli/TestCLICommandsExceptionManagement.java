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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
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
			if (e.getCause() instanceof StringIndexOutOfBoundsException) {
				// That's right
				return;
			}
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

}
