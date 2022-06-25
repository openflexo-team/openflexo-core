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

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.command.AbstractCommand.ExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * A parameterized suite of unit tests iterating on FML script files.
 * 
 * For each FML-script file, execute it. All asserts are executed and must success
 * 
 * @author sylvain
 *
 */
@RunWith(Parameterized.class)
public class AutomatedTests extends FMLScriptParserTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("TestResourceCenter/AutomatedTests"), ".fmlscript");
	}

	private final Resource fmlResource;
	private FlexoEditor editor;
	private FMLScript script;
	private CommandInterpreter commandInterpreter;

	// private static FlexoResourceCenterService rcService;
	// private static FlexoResourceCenter<?> testResourcesRC;

	public AutomatedTests(Resource fmlResource, String name) throws ParseException, ModelDefinitionException, IOException {
		System.out.println("********* Launch FML-script " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
		initServiceManager();
	}

	@Test
	public void checkScript() throws ModelDefinitionException, ParseException, IOException, ExecutionException {
		System.out.println("Parse script " + fmlResource.getRelativePath());
		script = parseFMLScript(fmlResource, commandInterpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		script.execute();
	}

	/*	@Test
		public void checkScript() throws ModelDefinitionException, ParseException, IOException {
			System.out.println("Check script " + fmlResource.getRelativePath());
			System.out.println("script=" + script);
			checkFMLScript(fmlResource.getRelativePath(), script);
		}
	
		@Test
		public void executeScript() throws ModelDefinitionException, ParseException, IOException {
			System.out.println("Execute script " + fmlResource.getRelativePath());
		}*/

	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);

		/*rcService = commandInterpreter.getServiceManager().getResourceCenterService();
		FlexoResourceCenter<?> existingResourcesRC = rcService.getFlexoResourceCenter("http://openflexo.org/test/flexo-test-resources");
		logger.info("Copying all files from " + existingResourcesRC);
		testResourcesRC = makeNewDirectoryResourceCenterFromExistingResourceCenter(serviceManager, existingResourcesRC);
		logger.info("Now working with " + testResourcesRC);*/
	}

	/*@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}*/

	/*protected FlexoServiceManager serviceManager;
	
	protected FlexoServiceManager instanciateTestServiceManager() {
		serviceManager = new DefaultFlexoServiceManager(null, false, true) {
	
			@Override
			protected LocalizationService createLocalizationService(String relativePath) {
				LocalizationService returned = super.createLocalizationService(relativePath);
				returned.setAutomaticSaving(false);
				return returned;
			}
	
			@Override
			protected FlexoEditingContext createEditingContext() {
				// In unit tests, we do NOT want to be warned against unexpected
				// edits
				return FlexoEditingContext.createInstance(false);
			}
	
			@Override
			protected DefaultFlexoEditor createApplicationEditor() {
				return new DefaultFlexoEditor(null, this);
			}
	
			@Override
			protected ProjectLoader createProjectLoaderService() {
				return new ProjectLoader();
			}
	
		};
	
		serviceManager.getLocalizationService().setAutomaticSaving(false);
	
		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);
	
		return serviceManager;
	}*/

}
