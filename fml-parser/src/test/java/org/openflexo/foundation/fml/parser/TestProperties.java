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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.StringUtils;

/**
 * A parameterized suite of unit tests iterating on FML files.
 * 
 * For each FML file, parse it.
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestProperties extends OpenflexoTestCase {

	static FlexoEditor editor;

	/*private static void testFMLCompilationUnit(Resource fileResource) throws ModelDefinitionException, ParseException, IOException {
	
		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnit compilationUnit = FMLParser.parse(((FileResourceImpl) fileResource).getFile(), fmlModelFactory);
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);
		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());
	
		// Test syntax-preserving pretty-print
		try {
			String prettyPrint = compilationUnit.getFMLPrettyPrint();
			System.out.println("prettyPrint=\n" + prettyPrint);
			FMLCompilationUnit reparsedCompilationUnit = FMLParser.parse(prettyPrint, fmlModelFactory);
			reparsedCompilationUnit.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnit);
			assertTrue("Objects are not equals after pretty-print", compilationUnit.equalsObject(reparsedCompilationUnit));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
		// Test normalized pretty-print
		try {
			String normalizedFML = compilationUnit.getNormalizedFML();
			System.out.println("normalizedFML=\n" + normalizedFML);
			FMLCompilationUnit reparsedCompilationUnit = FMLParser.parse(normalizedFML, fmlModelFactory);
			reparsedCompilationUnit.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnit);
			assertTrue("Objects are not equals after normalized pretty-print", compilationUnit.equalsObject(reparsedCompilationUnit));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
	}*/

	private static void debug(P2PPNode<?, ?> node, int indent) {
		System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " > " + node.getClass().getSimpleName() + " from "
				+ node.getLastParsedFragment());
		// System.err.println(node.getLastParsed());
		// node.getLastParsed();
		indent++;
		for (P2PPNode<?, ?> child : node.getChildren()) {
			debug(child, indent);
		}
	}

	private static FMLCompilationUnit parseFile(Resource fileResource) throws ModelDefinitionException, ParseException, IOException {
		return FMLParser.parse(((FileResourceImpl) fileResource).getFile(), new FMLModelFactory(null, serviceManager));
	}

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private static void testFMLCompilationUnit(Resource fileResource) throws ModelDefinitionException, ParseException, IOException {

		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnit compilationUnit = FMLParser.parse(((FileResourceImpl) fileResource).getFile(), fmlModelFactory);
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);
		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());

		// Test syntax-preserving pretty-print
		try {
			String prettyPrint = compilationUnit.getFMLPrettyPrint();
			System.out.println("prettyPrint=\n" + prettyPrint);
			FMLCompilationUnit reparsedCompilationUnit = FMLParser.parse(prettyPrint, fmlModelFactory);
			reparsedCompilationUnit.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnit);
			assertTrue("Objects are not equals after pretty-print", compilationUnit.equalsObject(reparsedCompilationUnit));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// Test normalized pretty-print
		try {
			String normalizedFML = compilationUnit.getNormalizedFML();
			System.out.println("normalizedFML=\n" + normalizedFML);
			FMLCompilationUnit reparsedCompilationUnit = FMLParser.parse(normalizedFML, fmlModelFactory);
			reparsedCompilationUnit.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnit);
			assertTrue("Objects are not equals after normalized pretty-print", compilationUnit.equalsObject(reparsedCompilationUnit));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("NewFMLExamples/TestProperties.fml");
		FMLCompilationUnit compilationUnit = parseFile(fmlFile);

		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);

		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());

		System.out.println("Normalized FML=\n" + compilationUnit.getVirtualModel().getNormalizedFML());

		testFMLCompilationUnit(fmlFile);

		/*assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());
		
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(vmNode = (VirtualModelNode) rootNode.getObjectNode(virtualModel));
		
		assertNotNull(stringImport = compilationUnit.getJavaImports().get(0));
		assertNotNull(listImport = compilationUnit.getJavaImports().get(1));
		
		assertNotNull(iProperty = virtualModel.getAccessibleProperty("i"));
		assertNotNull(fooProperty = virtualModel.getAccessibleProperty("foo"));
		
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		
		testNormalizedFMLRepresentationEquals("TestFMLPrettyPrint1/Step1Normalized.fml");
		testFMLPrettyPrintEquals("TestFMLPrettyPrint1/Step1PrettyPrint.fml");
		
		assertNotNull(stringImportNode = (JavaImportNode) rootNode.getObjectNode(stringImport));
		assertNotNull(listImportNode = (JavaImportNode) rootNode.getObjectNode(listImport));
		assertNotNull(iPropertyNode = (FlexoPropertyNode<?, ?>) vmNode.getObjectNode(iProperty));
		assertNotNull(fooPropertyNode = (FlexoPropertyNode<?, ?>) vmNode.getObjectNode(fooProperty));
		
		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);
		
		assertEquals("(5:0)-(5:24)", stringImportNode.getLastParsedFragment().toString());
		assertEquals(null, stringImportNode.getPrelude());
		assertEquals("(5:24)-(6:0)", stringImportNode.getPostlude().toString());
		
		assertEquals("(6:0)-(6:22)", listImportNode.getLastParsedFragment().toString());
		assertEquals(null, listImportNode.getPrelude());
		assertEquals("(6:22)-(7:0)", listImportNode.getPostlude().toString());
		
		assertEquals("(11:1)-(11:7)", iPropertyNode.getLastParsedFragment().toString());
		assertEquals("(11:0)-(11:1)", iPropertyNode.getPrelude().toString());
		assertEquals("(11:32)-(12:0)", iPropertyNode.getPostlude().toString());
		
		assertEquals("(12:1)-(12:20)", fooPropertyNode.getLastParsedFragment().toString());
		assertEquals("(12:0)-(12:1)", fooPropertyNode.getPrelude().toString());
		assertEquals("(12:20)-(13:0)", fooPropertyNode.getPostlude().toString());*/

	}

}
