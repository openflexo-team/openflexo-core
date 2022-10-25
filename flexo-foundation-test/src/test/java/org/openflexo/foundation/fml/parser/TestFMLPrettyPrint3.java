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
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
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
public class TestFMLPrettyPrint3 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;
	private static CreationScheme creationScheme;

	static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint3/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertNotNull(creationScheme = compilationUnit.getVirtualModel().getCreationSchemes().get(0));
		assertEquals("MyModel", virtualModel.getName());

		testFMLCompilationUnit(fmlFile);
	}

	@Test
	@TestOrder(2)
	public void simpleRewrite() throws ParseException, ModelDefinitionException, IOException {
		log("simpleRewrite");

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint3/Step1Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint3/Step1PrettyPrint.fml");

	}

	@Test
	@TestOrder(3)
	public void changeAnonymous() throws InvalidNameException {

		log("changeAnonymous");

		CreationScheme creationScheme = compilationUnit.getVirtualModel().getCreationSchemes().get(0);
		creationScheme.setAnonymous(false);
		creationScheme.setName("newConstructorName");
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Normalized FML=\n" + compilationUnit.getNormalizedFML());
		// testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint3/Step2Normalized.fml");
		// testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint3/Step2PrettyPrint.fml");

		/*assertEquals("(5:0)-(5:24)", stringImportNode.getLastParsedFragment().toString());
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
