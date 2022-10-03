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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.p2pp.RawSource;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Parse a FML file, perform some edits and checks that pretty-print is correct
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLPrettyPrint8 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;

	static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private static VirtualModelNode vmNode;

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint8/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());

		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(vmNode = (VirtualModelNode) rootNode.getObjectNode(virtualModel));

		// assertNotNull(getSetProperty = (GetSetProperty) virtualModel.getAccessibleProperty("fooList"));
		// assertNotNull(getSetNode = (GetSetPropertyNode) (P2PPNode) rootNode.getObjectNode(getSetProperty));

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint8/Step1Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint8/Step1PrettyPrint.fml");

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);

	}

	/*@Test
	@TestOrder(3)
	public void changeAbstractBehaviour() throws ParseException, IOException {
	
		log("changeAbstractBehaviour()");
	
		behaviour1.setAbstract(true);
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint6/Step2Normalized.fml");
	
		System.out.println("DEBUG PP:");
		System.out.println(getSetNode.debug());
	
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint6/Step2PrettyPrint.fml");
	
	}*/

}
