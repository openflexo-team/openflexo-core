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

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.FileResourceImpl;
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
public class TestFMLPrettyPrint extends OpenflexoTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("NewFMLExamples"), ".fml");
	}

	private final Resource fmlResource;

	public TestFMLPrettyPrint(Resource fmlResource, String name) {
		System.out.println("********* TestFMLParser " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
	}

	@Test
	public void testResource() throws ModelDefinitionException, ParseException {
		testFMLCompilationUnit(fmlResource);
	}

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

	private static void testFMLCompilationUnit(Resource fileResource) throws ModelDefinitionException, ParseException {
		FMLCompilationUnit compilationUnit = FMLParser.parse(((FileResourceImpl) fileResource).getFile(),
				new FMLModelFactory(null, serviceManager));
		FMLCompilationUnitNode rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate();
		debug(rootNode, 0);
		System.out.println("FML=\n" + compilationUnit.getVirtualModel().getFMLPrettyPrint());
	}

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

	// A decommenter et continuer
	/*private void testPrettyPrint(FMLCompilationUnit component) {
		System.out.println("testPrettyPrint with " + component);
	
		// Test syntax-preserving pretty-print
		try {
			String prettyPrint = component.getFMLPrettyPrint();
			System.out.println("prettyPrint=\n" + prettyPrint);
			FMLCompilationUnit reparsedComponent = FMLParser.parse(prettyPrint, component.getFMLModelFactory());
			reparsedComponent.getVirtualModel().setResource(component.getVirtualModel().getResource());
			// System.out.println("component=" + component);
			System.out.println("reparsedComponent=" + reparsedComponent);
			assertTrue("Objects are not equals after pretty-print", component.equalsObject(reparsedComponent));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
		// Test normalized pretty-print
		try {
			String normalizedB = component.getNormalizedBRepresentation();
			System.out.println("normalizedB=\n" + normalizedB);
			BComponent reparsedComponent = BParser.parse(normalizedB, component.getResource().getFactory());
			reparsedComponent.setResource(component.getResource());
			// System.out.println("component=" + component);
			System.out.println("reparsedComponent=" + reparsedComponent);
			assertTrue("Objects are not equals after normalized pretty-print", component.equalsObject(reparsedComponent));
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}*/

}
