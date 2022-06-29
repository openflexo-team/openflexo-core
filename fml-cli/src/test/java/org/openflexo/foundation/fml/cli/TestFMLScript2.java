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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.command.ExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertExpression;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssignation;
import org.openflexo.foundation.fml.cli.command.fml.FMLContextCommand;
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
public class TestFMLScript2 extends FMLScriptParserTestCase {

	private static final Logger logger = Logger.getLogger(TestFMLScript2.class.getPackage().getName());

	static FlexoEditor editor;

	static FMLScript script;
	private static CommandInterpreter commandInterpreter;

	private static FlexoResourceCenterService rcService;
	private static FlexoResourceCenter<?> testResourcesRC;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);

		rcService = commandInterpreter.getServiceManager().getResourceCenterService();
		FlexoResourceCenter<?> existingResourcesRC = rcService.getFlexoResourceCenter("http://openflexo.org/test/flexo-test-resources");
		logger.info("Copying all files from " + existingResourcesRC);
		testResourcesRC = makeNewDirectoryResourceCenterFromExistingResourceCenter(serviceManager, existingResourcesRC);
		logger.info("Now working with " + testResourcesRC);
	}

	@Test
	@TestOrder(2)
	public void loadScript() throws ParseException, ModelDefinitionException, IOException {
		log("Load script");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLScript2.fmlscript");

		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		script = parseFMLScript(fmlFile, commandInterpreter);
		// assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	public void checkScript() throws ParseException, ModelDefinitionException, IOException {
		log("Check script");

		// checkFMLScript("TestFMLScript2.fmlscript", script);

		assertEquals(6, script.getCommands().size());
		/*for (AbstractCommand command : script.getCommands()) {
			System.out.println("Check " + command + " with " + command.getNode() + " of " + command.getNode().getClass());
			assertEquals(command.getOriginalCommandAsString(), command.toString());
			System.out.println(">>> " + command.getOriginalCommandAsString());
		}*/

		FMLContextCommand context1 = (FMLContextCommand) script.getCommands().get(0);
		FMLAssignation assignation1 = (FMLAssignation) script.getCommands().get(1);
		FMLAssignation assignation2 = (FMLAssignation) script.getCommands().get(2);
		FMLContextCommand context2 = (FMLContextCommand) script.getCommands().get(3);
		FMLAssignation assignation3 = (FMLAssignation) script.getCommands().get(4);
		FMLAssertExpression assertExpression = (FMLAssertExpression) script.getCommands().get(5);

		assertEquals(context1.getParentCommand(), null);
		assertEquals(assignation1.getParentCommand(), context1);
		assertEquals(assignation2.getParentCommand(), assignation1);
		assertEquals(context2.getParentCommand(), assignation2);
		assertEquals(assignation3.getParentCommand(), context2);
		assertEquals(assertExpression.getParentCommand(), assignation3);

		System.out.println("BM0: " + context1.getBindingModel());
		System.out.println("BM1: " + assignation1.getBindingModel());
		System.out.println("BM2: " + assignation2.getBindingModel());
		System.out.println("BM3: " + context2.getBindingModel());
		System.out.println("BM4: " + assignation3.getBindingModel());
		System.out.println("BM5: " + assertExpression.getBindingModel());

		assertNull(context1.getBindingModel().bindingVariableNamed("a"));
		assertNull(assignation1.getBindingModel().bindingVariableNamed("a"));
		assertNotNull(assignation2.getBindingModel().bindingVariableNamed("a"));
		assertNotNull(context2.getBindingModel().bindingVariableNamed("a"));
		assertNotNull(assignation3.getBindingModel().bindingVariableNamed("a"));
		assertNotNull(assertExpression.getBindingModel().bindingVariableNamed("a"));

		assertNull(context1.getBindingModel().bindingVariableNamed("b"));
		assertNull(assignation1.getBindingModel().bindingVariableNamed("b"));
		assertNull(assignation2.getBindingModel().bindingVariableNamed("b"));
		assertNotNull(context2.getBindingModel().bindingVariableNamed("b"));
		assertNotNull(assignation3.getBindingModel().bindingVariableNamed("b"));
		assertNotNull(assertExpression.getBindingModel().bindingVariableNamed("b"));

		assertNull(context1.getBindingModel().bindingVariableNamed("c"));
		assertNull(assignation1.getBindingModel().bindingVariableNamed("c"));
		assertNull(assignation2.getBindingModel().bindingVariableNamed("c"));
		assertNull(context2.getBindingModel().bindingVariableNamed("c"));
		assertNull(assignation3.getBindingModel().bindingVariableNamed("c"));
		assertNotNull(assertExpression.getBindingModel().bindingVariableNamed("c"));

	}

	@Test
	@TestOrder(4)
	public void executeScript() throws ParseException, ModelDefinitionException, IOException, ExecutionException {
		log("Execute script");

		script.execute();
	}

}
