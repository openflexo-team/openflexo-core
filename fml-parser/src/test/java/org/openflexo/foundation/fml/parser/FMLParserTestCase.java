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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
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
	protected final FMLCompilationUnit testFMLCompilationUnit(Resource fileResource)
			throws ModelDefinitionException, ParseException, IOException {

		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnit compilationUnit = FMLParser.parse(((FileResourceImpl) fileResource).getFile(), fmlModelFactory);
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);

		System.out.println("normalizedFML=\n" + compilationUnit.getNormalizedFML());

		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());

		FMLCompilationUnit reparsedCompilationUnitFromPrettyPrint = null;
		FMLCompilationUnit reparsedCompilationUnitFromNormalizedFML = null;

		// Test syntax-preserving pretty-print
		try {
			String prettyPrint = compilationUnit.getFMLPrettyPrint();
			System.out.println("prettyPrint=\n" + prettyPrint);
			reparsedCompilationUnitFromPrettyPrint = FMLParser.parse(prettyPrint, fmlModelFactory);
			reparsedCompilationUnitFromPrettyPrint.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
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
			reparsedCompilationUnitFromNormalizedFML = FMLParser.parse(normalizedFML, fmlModelFactory);
			reparsedCompilationUnitFromNormalizedFML.getVirtualModel().setResource(compilationUnit.getVirtualModel().getResource());
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
	protected final void debug(P2PPNode<?, ?> node, int indent) {
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

	/**
	 * Parse supplied Resource asserting this resource is a .fml (a FLM-serialized version of a FMLCompilationUnit)
	 * 
	 * @param fileResource
	 * @return
	 * @throws ModelDefinitionException
	 * @throws ParseException
	 * @throws IOException
	 */
	protected FMLCompilationUnit parseFile(Resource fileResource) throws ModelDefinitionException, ParseException, IOException {
		return FMLParser.parse(((FileResourceImpl) fileResource).getFile(), new FMLModelFactory(null, serviceManager));
	}

}
