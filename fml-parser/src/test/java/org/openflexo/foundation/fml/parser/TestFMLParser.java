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

package org.openflexo.foundation.fml.parser;

import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;
import org.openflexo.toolbox.StringUtils;

/**
 * A parameterized suite of unit tests iterating on FML files.
 * 
 * For each FML file, parse it.
 * 
 * @author sylvain
 *
 */
@RunWith(Parameterized.class)
public class TestFMLParser {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("FMLParsingExamples"), ".fml");
	}

	private final Resource fmlResource;

	public TestFMLParser(Resource fmlResource, String name) {
		System.out.println("********* TestFMLParser " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
	}

	@Test
	public void parseFMLResource() throws ModelDefinitionException, ParseException, IOException {
		// testFMLCompilationUnit(fmlResource);
		System.out.println("Parsing FML resource " + fmlResource);
		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		FMLCompilationUnit compilationUnit = parser.parse(fmlResource.openInputStream(), fmlModelFactory, (modelSlotClasses) -> {
			return null;
		}, true);
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);

	}

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

	/**
	 * Display in console AbstractSyntaxTree of supplied node
	 * 
	 * @param node
	 *            node to display
	 * @param indent
	 *            identation level
	 */
	protected static void debug(P2PPNode<?, ?> node, int indent) {
		System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " > " + node.getClass().getSimpleName() + " from "
				+ node.getLastParsedFragment() /*+ " model:" + node.getModelObject()*/ + " pre=" + node.getPrelude() + " post="
				+ node.getPostlude() /*+ " astNode=" + node.getASTNode() + " of " + node.getASTNode().getClass()*/);
		// System.err.println(node.getLastParsed());
		// node.getLastParsed();
		indent++;
		for (P2PPNode<?, ?> child : node.getChildren()) {
			debug(child, indent);
		}
	}

	protected static FlexoServiceManager serviceManager;

	protected static FlexoServiceManager instanciateTestServiceManager() {
		serviceManager = new DefaultFlexoServiceManager(null, true) {

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
	}

}
