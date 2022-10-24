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
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Provides a testing environment for testing FMLParser
 * 
 * @author sylvain
 *
 */
public abstract class FMLParserTestCase extends OpenflexoTestCase {

	/**
	 * Test supplied Resource asserting this resource is a .fml (a FLM-serialized version of a FMLCompilationUnit)
	 * 
	 * This test
	 * <ul>
	 * <li>Deserialize resource and instantiate a {@link FMLCompilationUnit}</li>
	 * <li>Compute pretty-print and re-deserialize from pretty-print and test equility with previous {@link FMLCompilationUnit}</li>
	 * <li></li>
	 * </ul>
	 * 
	 * 
	 * @param fileResource
	 * @throws ModelDefinitionException
	 * @throws ParseException
	 * @throws IOException
	 */
	protected static FMLCompilationUnit testFMLCompilationUnit(Resource fileResource)
			throws ModelDefinitionException, ParseException, IOException {

		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		FMLCompilationUnit compilationUnit = parser.parse(fileResource.openInputStream(), fmlModelFactory, (modelSlotClasses) -> {
			// We dont expect to have particular ModelSlots in this context, but be aware of that
			return null;
		}, true);
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);

		/*
		System.out.println("normalizedFML=\n" + compilationUnit.getNormalizedFML());
		
		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());
		
		FMLCompilationUnit reparsedCompilationUnitFromPrettyPrint = null;
		FMLCompilationUnit reparsedCompilationUnitFromNormalizedFML = null;
		
		// Test syntax-preserving pretty-print
		try {
			String prettyPrint = compilationUnit.getFMLPrettyPrint();
			System.out.println("prettyPrint=\n" + prettyPrint);
			reparsedCompilationUnitFromPrettyPrint = parser.parse(prettyPrint, fmlModelFactory);
			reparsedCompilationUnitFromPrettyPrint.setResource(compilationUnit.getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnitFromPrettyPrint);
			assertTrue("Objects are not equals after pretty-print", compilationUnit.equalsObject(reparsedCompilationUnitFromPrettyPrint));
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
			reparsedCompilationUnitFromNormalizedFML = parser.parse(normalizedFML, fmlModelFactory);
			reparsedCompilationUnitFromPrettyPrint.setResource(compilationUnit.getResource());
			// System.out.println("compilationUnit=" + compilationUnit);
			System.out.println("reparsedCompilationUnit=" + reparsedCompilationUnitFromNormalizedFML);
			assertTrue("Objects are not equals after normalized pretty-print",
					compilationUnit.equalsObject(reparsedCompilationUnitFromNormalizedFML));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertTrue("Pretty-print and normalized FML are not equals",
				reparsedCompilationUnitFromPrettyPrint.equalsObject(reparsedCompilationUnitFromNormalizedFML));
		
		*/

		return compilationUnit;
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
				+ ((FMLPrettyPrintDelegate) node).getStartLocation() + "-" + node.getEndPosition() + " pre=" + node.getPrelude() + " post="
				+ node.getPostlude() /*+ " astNode=" + node.getASTNode() + " of " + node.getASTNode().getClass()*/
				+ " model=" + node.getModelObject());
		// System.err.println(node.getLastParsed());
		// node.getLastParsed();
		indent++;
		for (P2PPNode<?, ?> child : node.getChildren()) {
			debug(child, indent);
		}
	}

	/**
	 * Parse supplied Resource asserting this resource is a .fml (a FLM-serialized version of a FMLCompilationUnit)
	 * 
	 * @param fileResource
	 * @return
	 * @throws ModelDefinitionException
	 * @throws ParseException
	 * @throws IOException
	 */
	protected static FMLCompilationUnit parseFile(Resource resource) throws ModelDefinitionException, ParseException, IOException {
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		return parser.parse(resource.openInputStream(), new FMLModelFactory(null, serviceManager), (modelSlotClasses) -> {
			// We dont expect to have particular ModelSlots in this context, but be aware of that
			return null;
		}, true);
	}

	protected static void testNormalizedFMLRepresentationEquals(FMLCompilationUnit compilationUnit, String resourceFile) {
		// System.out.println("Normalized=");
		// System.out.println(compilationUnit.getPrettyPrintDelegate()
		// .getNormalizedRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()));
		testFileContentsEquals(compilationUnit.getPrettyPrintDelegate()
				.getNormalizedRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()), resourceFile);
	}

	protected static void testFMLPrettyPrintEquals(FMLCompilationUnit compilationUnit, String resourceFile) {
		testFileContentsEquals(compilationUnit.getPrettyPrintDelegate()
				.getRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()), resourceFile);
	}

	protected static void testFileContentsEquals(String expected, String resourceFile) {
		final Resource resource = ResourceLocator.locateResource(resourceFile);
		try {
			String resourceContents = FileUtils.fileContents(resource.openInputStream(), null);
			assertSameContents(expected, resourceContents);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	public static final boolean DEBUG = true;

	protected static void assertSameContents(String s1, String s2) {

		List<String> rows1 = new ArrayList<>();
		List<String> rows2 = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new StringReader(s1))) {
			String nextLine = null;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					rows1.add(nextLine);
					if (DEBUG)
						System.out.println("1> [" + rows1.size() + "] : " + nextLine);
				}
			} while (nextLine != null);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		if (rows1.get(rows1.size() - 1).trim().equals("")) {
			rows1.remove(rows1.size() - 1);
		}

		try (BufferedReader br = new BufferedReader(new StringReader(s2))) {
			String nextLine = null;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					rows2.add(nextLine);
					if (DEBUG)
						System.out.println("2> [" + rows2.size() + "] : " + nextLine);
				}
			} while (nextLine != null);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		/*if (DEBUG) {
			for (int i = 0; i < rows1.size(); i++) {
				System.out.println("*1> [" + i + "] : " + rows1.get(i));
			}
			for (int i = 0; i < rows2.size(); i++) {
				System.out.println("*2> [" + i + "] : " + rows2.get(i));
			}
		}*/

		assertEquals("Row size differs", rows1.size(), rows2.size());
		for (int i = 0; i < rows1.size(); i++) {
			assertEquals(rows1.get(i)/*.trim()*/, rows2.get(i)/*.trim()*/);
		}
	}

	public static FMLCompilationUnitNode rootNode;

	protected <N extends P2PPNode, M> N checkNodeForObject(String expectedFragment, String expectedModelObjectValue, M object) {
		return checkNodeForObject(expectedFragment, null, null, expectedModelObjectValue, object);
	}

	protected <N extends P2PPNode, M> N checkNodeForObject(String expectedFragment, String expectedPrelude, String expectedPostlude,
			String expectedModelObjectValue, M object) {
		return checkNode(expectedFragment, expectedPrelude, expectedPostlude, expectedModelObjectValue, (N) rootNode.getObjectNode(object));
	}

	protected <N extends P2PPNode, M> N checkNodeForObject(String expectedFragment, String expectedPrelude, String expectedPostlude,
			M object) {
		return checkNode(expectedFragment, expectedPrelude, expectedPostlude, null, (N) rootNode.getObjectNode(object));
	}

	protected <N extends P2PPNode, M> N checkNode(String expectedFragment, String expectedModelObjectValue, N node) {
		return checkNode(expectedFragment, null, null, expectedModelObjectValue, node);
	}

	protected <N extends P2PPNode, M> N checkNode(String expectedFragment, String expectedPrelude, String expectedPostlude,
			String expectedModelObjectValue, N node) {
		assertEquals(expectedFragment, ((FMLPrettyPrintDelegate) node).getStartLocation() + "-" + node.getEndPosition());
		assertEquals(expectedPrelude, node.getPrelude() != null ? node.getPrelude().toString() : null);
		assertEquals(expectedPostlude, node.getPostlude() != null ? node.getPostlude().toString() : null);
		if (expectedModelObjectValue != null) {
			assertEquals(expectedModelObjectValue, node.getModelObject().toString());
		}
		return node;
	}

}
