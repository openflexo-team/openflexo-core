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

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.AbstractCommand.ExecutionException;
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
public class TestCLICommands2 extends OpenflexoTestCase {

	private static final Logger logger = Logger.getLogger(TestCLICommands2.class.getPackage().getName());

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

	@Test
	@TestOrder(10)
	public void testOpen() throws ParseException, IOException, ExecutionException {
		log("testOpen()");

		assertNull(rcService.getFlexoResourceCenter("http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj"));

		AbstractCommand command3 = CommandParser.parse(
				"open -r [\"http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj\"]", commandInterpreter);
		assertEquals("open -r [\"http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj\"]", command3.toString());
		command3.execute();
		assertNotNull(rcService.getFlexoResourceCenter("http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj"));

		AbstractCommand command4 = CommandParser.parse("service ResourceCenterService status", commandInterpreter);
		assertEquals("service ResourceCenterService status", command4.toString());
		command4.execute();

	}

	@Test
	@TestOrder(11)
	public void testEnter() throws ParseException, IOException, ExecutionException {
		log("testEnter()");
		AbstractCommand command1 = CommandParser.parse(
				"enter -r [\"http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj/Vm.fml\"]",
				commandInterpreter);
		assertEquals("enter -r [\"http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj/Vm.fml\"]",
				command1.toString());
		command1.execute();

	}

}
