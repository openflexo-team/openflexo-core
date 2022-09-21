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

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test BindingPath parsing
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLScript1 extends FMLScriptParserTestCase {

	private static final Logger logger = Logger.getLogger(TestFMLScript1.class.getPackage().getName());

	static FlexoEditor editor;

	static FMLScript script;
	private static CommandInterpreter commandInterpreter;

	private static FlexoResourceCenterService rcService;
	private static FlexoResourceCenter<?> testResourcesRC;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {

		// System.out.println("Prout: " + new File(System.getProperty("user.dir")));
		// System.out.println("Home: " + HOME_DIR);
		// System.exit(-1);

		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err,
				HOME_DIR /*new File(System.getProperty("user.dir"))*/);

		rcService = commandInterpreter.getServiceManager().getResourceCenterService();
		FlexoResourceCenter<?> existingResourcesRC = rcService.getFlexoResourceCenter("http://openflexo.org/test/flexo-test-resources");
		logger.info("Copying all files from " + existingResourcesRC);
		testResourcesRC = makeNewDirectoryResourceCenterFromExistingResourceCenter(serviceManager, existingResourcesRC);
		logger.info("Now working with " + testResourcesRC);

		System.out.println("Working from " + commandInterpreter.getWorkingDirectory());
		// System.exit(-1);

	}

	@Test
	@TestOrder(2)
	public void loadScript() throws ParseException, ModelDefinitionException, IOException {
		log("Load script");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLScript1.fmlscript");

		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		script = parseFMLScript(fmlFile, commandInterpreter);
		// assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	public void checkScript() throws ParseException, ModelDefinitionException, IOException {
		log("Check script");

		checkFMLScript("TestFMLScript1.fmlscript", script);

		assertEquals(19, script.getCommands().size());
		for (AbstractCommand command : script.getCommands()) {
			System.out.println("Check " + command + " with " + command.getNode() + " of " + command.getNode().getClass());
			assertEquals(command.getOriginalCommandAsString(), command.toString());
			System.out.println(">>> " + command.getOriginalCommandAsString());
		}
	}

	@Test
	@TestOrder(4)
	public void executeScript() throws ParseException, ModelDefinitionException, IOException, FMLCommandExecutionException {
		log("Execute script");

		script.execute();
	}

}
