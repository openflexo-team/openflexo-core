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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoService.Status;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * Test command parser
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestCLICommands extends OpenflexoTestCase {

	private static CommandInterpreter commandInterpreter;

	private static File workingDirectory;
	private static FlexoResourceCenterService rcService;
	private static ResourceManager rm;
	private static FlexoResourceCenter<?> testResourcesRC;

	@BeforeClass
	public static void initialize() throws IOException {
		instanciateTestServiceManager();
		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err,
				new File(System.getProperty("user.dir")));
		workingDirectory = new File(System.getProperty("user.dir"));
		rcService = commandInterpreter.getServiceManager().getResourceCenterService();
		rm = commandInterpreter.getServiceManager().getResourceManager();
		testResourcesRC = rcService.getFlexoResourceCenter("http://openflexo.org/test/flexo-test-resources");
		assertTrue(rcService.getResourceCenters().contains(testResourcesRC));
	}

	@Test
	@TestOrder(1)
	public void testHelp() throws ParseException {
		log("tesHelp()");
		AbstractCommand help = CommandParser.parse("help", commandInterpreter);
		assertEquals("help", help.toString());
		help.execute();
	}

	@Test
	@TestOrder(2)
	public void testPwd() throws ParseException {
		log("testPwd()");
		AbstractCommand pwd = CommandParser.parse("pwd", commandInterpreter);
		assertEquals("pwd", pwd.toString());
		pwd.execute();
	}

	@Test
	@TestOrder(3)
	public void testCd() throws ParseException {
		log("testCd()");
		AbstractCommand cd1 = CommandParser.parse("cd ..", commandInterpreter);
		cd1.execute();
		assertEquals("cd ..", cd1.toString());
		assertEquals(commandInterpreter.getWorkingDirectory(), workingDirectory.getParentFile());
		AbstractCommand cd2 = CommandParser.parse("cd flexo-test-resources/src/main/resources/TestResourceCenter/ViewPoints",
				commandInterpreter);
		assertEquals("cd flexo-test-resources/src/main/resources/TestResourceCenter/ViewPoints", cd2.toString());
		cd2.execute();
		workingDirectory = commandInterpreter.getWorkingDirectory();
		assertEquals("ViewPoints", workingDirectory.getName());
		AbstractCommand pwd = CommandParser.parse("pwd", commandInterpreter);
		pwd.execute();
	}

	@Test
	@TestOrder(4)
	public void testLs() throws ParseException {
		log("testLs()");
		AbstractCommand ls = CommandParser.parse("ls", commandInterpreter);
		assertEquals("ls", ls.toString());
		ls.execute();
	}

	@Test
	@TestOrder(5)
	public void testServices() throws ParseException {
		log("testServices()");
		AbstractCommand services = CommandParser.parse("services", commandInterpreter);
		assertEquals("services", services.toString());
		services.execute();
	}

	@Test
	@TestOrder(6)
	public void testServiceOnResourceManager() throws ParseException {
		log("testServiceOnResourceManager()");
		AbstractCommand command1 = CommandParser.parse("service ResourceManager usage", commandInterpreter);
		assertEquals("service ResourceManager usage", command1.toString());
		command1.execute();
		assertEquals(Status.Started, commandInterpreter.getServiceManager().getResourceManager().getStatus());
		AbstractCommand command2 = CommandParser.parse("service ResourceManager stop", commandInterpreter);
		assertEquals("service ResourceManager stop", command2.toString());
		command2.execute();
		assertEquals(Status.Stopped, commandInterpreter.getServiceManager().getResourceManager().getStatus());
		AbstractCommand command3 = CommandParser.parse("service ResourceManager start", commandInterpreter);
		assertEquals("service ResourceManager start", command3.toString());
		command3.execute();
		assertEquals(Status.Started, commandInterpreter.getServiceManager().getResourceManager().getStatus());
		AbstractCommand command4 = CommandParser.parse("service ResourceManager status", commandInterpreter);
		assertEquals("service ResourceManager status", command4.toString());
		command4.execute();
		assertEquals(Status.Started, commandInterpreter.getServiceManager().getResourceManager().getStatus());
	}

	@Test
	@TestOrder(7)
	public void testServiceOnResourceCenter() throws ParseException, IOException {
		log("testServiceOnResourceCenter()");

		AbstractCommand command1 = CommandParser.parse("service ResourceCenterService usage", commandInterpreter);
		assertEquals("service ResourceCenterService usage", command1.toString());
		command1.execute();
		assertEquals(Status.Started, commandInterpreter.getServiceManager().getResourceCenterService().getStatus());
		AbstractCommand command2 = CommandParser.parse("service ResourceCenterService status", commandInterpreter);
		assertEquals("service ResourceCenterService status", command2.toString());
		command2.execute();

		int rcNb = rcService.getResourceCenters().size();

		File tempResourceCenterDir = FileUtils.createTempDirectory("TestResourceCenter", "");
		// System.out.println("tempResourceCenterDir=" + tempResourceCenterDir);
		// System.out.println("exists=" + tempResourceCenterDir.exists());
		AbstractCommand command3 = CommandParser.parse("service ResourceCenterService add_rc -d " + tempResourceCenterDir.getAbsolutePath(),
				commandInterpreter);
		assertEquals("service ResourceCenterService add_rc -d " + tempResourceCenterDir.getAbsolutePath(), command3.toString());
		command3.execute();
		assertEquals(rcNb + 1, rcService.getResourceCenters().size());

		AbstractCommand command4 = CommandParser.parse("service ResourceCenterService status", commandInterpreter);
		assertEquals("service ResourceCenterService status", command4.toString());
		command4.execute();

	}

	@Test
	@TestOrder(8)
	public void testActivate() throws ParseException, IOException {
		log("testActivate()");
		AbstractCommand command1 = CommandParser.parse("service TechnologyAdapterService status", commandInterpreter);
		assertEquals("service TechnologyAdapterService status", command1.toString());
		command1.execute();
		AbstractCommand command2 = CommandParser.parse("activate FML", commandInterpreter);
		assertEquals("activate FML", command2.toString());
		command2.execute();
	}

	@Test
	@TestOrder(9)
	public void testResources() throws ParseException, IOException {
		log("testResources()");
		AbstractCommand command1 = CommandParser.parse("resources", commandInterpreter);
		assertEquals("resources", command1.toString());
		command1.execute();

		AbstractCommand command2 = CommandParser.parse("resources FML", commandInterpreter);
		assertEquals("resources FML", command2.toString());
		command2.execute();

		AbstractCommand command3 = CommandParser.parse("resources * [\"" + testResourcesRC.getDefaultBaseURI() + "\"]", commandInterpreter);

		assertEquals("resources * [\"" + testResourcesRC.getDefaultBaseURI() + "\"]", command3.toString());
		command3.execute();

		AbstractCommand command4 = CommandParser.parse("resources FML [\"" + testResourcesRC.getDefaultBaseURI() + "\"]",
				commandInterpreter);
		assertEquals("resources FML [\"" + testResourcesRC.getDefaultBaseURI() + "\"]", command4.toString());
		command4.execute();
	}

	@Test
	@TestOrder(10)
	public void testOpen() throws ParseException, IOException {
		log("testOpen()");
		File rcDir = ((DirectoryResourceCenter) testResourcesRC).getRootDirectory();
		AbstractCommand command1 = CommandParser.parse("cd " + rcDir.getAbsolutePath(), commandInterpreter);
		assertEquals("cd " + rcDir, command1.toString());
		command1.execute();
		AbstractCommand command2 = CommandParser.parse("cd TestResourceCenter/PRJ", commandInterpreter);
		assertEquals("cd TestResourceCenter/PRJ", command2.toString());
		command2.execute();

		AbstractCommand command3 = CommandParser.parse("open TestSingleInheritance.prj", commandInterpreter);
		assertEquals("open TestSingleInheritance.prj", command3.toString());
		command3.execute();

		AbstractCommand command4 = CommandParser.parse("service ResourceCenterService status", commandInterpreter);
		assertEquals("service ResourceCenterService status", command4.toString());
		command4.execute();
	}

	@Test
	@TestOrder(11)
	public void testLoad() throws ParseException, IOException {
		log("testLoad()");
		AbstractCommand command1 = CommandParser.parse("cd TestSingleInheritance.prj", commandInterpreter);
		assertEquals("cd TestSingleInheritance.prj", command1.toString());
		command1.execute();

		AbstractCommand command2 = CommandParser.parse("resources", commandInterpreter);
		command2.execute();

		FlexoResource<?> vmResource = rm
				.getResource("http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj/Vm.fml");
		assertNotNull(vmResource);
		assertFalse(vmResource.isLoaded());

		AbstractCommand command3 = CommandParser.parse("load Vm.fml", commandInterpreter);
		assertEquals("load Vm.fml", command3.toString());
		command3.execute();
		assertTrue(vmResource.isLoaded());

		AbstractCommand command4 = CommandParser.parse("resources", commandInterpreter);
		command4.execute();

	}

	@Test
	@TestOrder(12)
	public void testMore() throws ParseException, IOException {
		log("testMore()");
		AbstractCommand command1 = CommandParser.parse("more -f Vm.fml", commandInterpreter);
		assertEquals("more -f Vm.fml", command1.toString());
		command1.execute();

	}

	@Test
	@TestOrder(13)
	public void testEnter() throws ParseException, IOException {
		log("testEnter()");
		AbstractCommand command1 = CommandParser.parse("enter -f Vm.fml", commandInterpreter);
		assertEquals("enter -f Vm.fml", command1.toString());
		command1.execute();

	}

	@Test
	@TestOrder(14)
	public void testContext() throws ParseException, IOException {
		log("testContext()");
		AbstractCommand command1 = CommandParser.parse("context", commandInterpreter);
		assertEquals("context", command1.toString());
		command1.execute();

	}

	@Test
	@TestOrder(15)
	public void testAssignations() throws ParseException, IOException {
		log("testAssignations()");
		AbstractCommand command1 = CommandParser.parse("a=1", commandInterpreter);
		assertEquals("a = 1", command1.toString());
		command1.execute();
		AbstractCommand command2 = CommandParser.parse("b=2", commandInterpreter);
		assertEquals("b = 2", command2.toString());
		command2.execute();
		AbstractCommand command3 = CommandParser.parse("context", commandInterpreter);
		assertEquals("context", command3.toString());
		command3.execute();
		AbstractCommand command4 = CommandParser.parse("a+b", commandInterpreter);
		assertEquals("a + b", command4.toString());
		assertEquals((long) 3, command4.execute());

	}

	@Test
	@TestOrder(100)
	public void testHistory() throws ParseException {
		log("testHistory()");
		AbstractCommand history = CommandParser.parse("history", commandInterpreter);
		assertEquals("history", history.toString());
		history.execute();
	}

	@Test
	@TestOrder(101)
	public void testQuit() throws ParseException {
		log("testQuit()");
		AbstractCommand history = CommandParser.parse("quit", commandInterpreter);
		assertEquals("quit", history.toString());
		history.execute();
	}

}
