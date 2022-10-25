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
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
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
public class TestFMLPrettyPrint1 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;
	private static FlexoConcept conceptA;

	static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private static FMLCompilationUnitNode rootNode;
	private static VirtualModelNode vmNode;

	private static JavaImportDeclaration stringImport;
	private static JavaImportDeclaration listImport;
	private static JavaImportDeclaration dateImport;

	private static FlexoProperty<?> iProperty;
	private static FlexoProperty<?> fooProperty;
	private static FlexoProperty<?> stringProperty;
	private static FlexoProperty<?> dateProperty;

	private static FlexoPropertyNode<?, ?> iPropertyNode;
	private static FlexoPropertyNode<?, ?> fooPropertyNode;
	private static FlexoPropertyNode<?, ?> stringPropertyNode;
	private static FlexoPropertyNode<?, ?> datePropertyNode;

	private static JavaImportNode stringImportNode;
	private static JavaImportNode listImportNode;
	private static JavaImportNode dateImportNode;

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint1/InitialVersion.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());

		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(vmNode = (VirtualModelNode) rootNode.getObjectNode(virtualModel));

		assertNotNull(stringImport = compilationUnit.getJavaImports().get(0));
		assertNotNull(listImport = compilationUnit.getJavaImports().get(1));

		assertNotNull(iProperty = virtualModel.getAccessibleProperty("i"));
		assertNotNull(fooProperty = virtualModel.getAccessibleProperty("foo"));

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step1Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step1PrettyPrint.fml");

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
		assertEquals("(6:22)-(8:0)", listImportNode.getPostlude().toString());

		assertEquals("(11:1)-(11:7)", iPropertyNode.getLastParsedFragment().toString());
		assertEquals("(11:0)-(11:1)", iPropertyNode.getPrelude().toString());
		assertEquals("(11:32)-(12:0)", iPropertyNode.getPostlude().toString());

		assertEquals("(12:1)-(12:20)", fooPropertyNode.getLastParsedFragment().toString());
		assertEquals("(12:0)-(12:1)", fooPropertyNode.getPrelude().toString());
		assertEquals("(12:20)-(13:0)", fooPropertyNode.getPostlude().toString());

	}

	@Test
	@TestOrder(3)
	public void modifyImport() {

		log("Modify import");

		JavaImportDeclaration listDeclaration = compilationUnit.getJavaImports().get(1);
		listDeclaration.setFullQualifiedClassName("java.util.ArrayList");
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step2Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step2PrettyPrint.fml");

		assertEquals("(5:0)-(5:24)", stringImportNode.getLastParsedFragment().toString());
		assertEquals(null, stringImportNode.getPrelude());
		assertEquals("(5:24)-(6:0)", stringImportNode.getPostlude().toString());

		assertEquals("(6:0)-(6:22)", listImportNode.getLastParsedFragment().toString());
		assertEquals(null, listImportNode.getPrelude());
		assertEquals("(6:22)-(8:0)", listImportNode.getPostlude().toString());

		assertEquals("(11:1)-(11:7)", iPropertyNode.getLastParsedFragment().toString());
		assertEquals("(11:0)-(11:1)", iPropertyNode.getPrelude().toString());
		assertEquals("(11:32)-(12:0)", iPropertyNode.getPostlude().toString());

		assertEquals("(12:1)-(12:20)", fooPropertyNode.getLastParsedFragment().toString());
		assertEquals("(12:0)-(12:1)", fooPropertyNode.getPrelude().toString());
		assertEquals("(12:20)-(13:0)", fooPropertyNode.getPostlude().toString());

	}

	@Test
	@TestOrder(4)
	public void addStringProperty() {

		log("Add String property");

		CreatePrimitiveRole createStringProperty = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createStringProperty.setRoleName("newString");
		createStringProperty.setPrimitiveType(PrimitiveType.String);
		createStringProperty.doAction();
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step3Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step3PrettyPrint.fml");

		assertNotNull(stringProperty = virtualModel.getAccessibleProperty("newString"));
		assertNotNull(stringPropertyNode = (FlexoPropertyNode<?, ?>) vmNode.getObjectNode(stringProperty));

		debug(rootNode, 0);

		assertEquals("(11:1)-(11:7)", iPropertyNode.getLastParsedFragment().toString());
		assertEquals("(11:0)-(11:1)", iPropertyNode.getPrelude().toString());
		assertEquals("(11:32)-(12:0)", iPropertyNode.getPostlude().toString());

		assertEquals("(12:1)-(12:20)", fooPropertyNode.getLastParsedFragment().toString());
		assertEquals("(12:0)-(12:1)", fooPropertyNode.getPrelude().toString());
		assertEquals("(12:20)-(13:0)", fooPropertyNode.getPostlude().toString());
	}

	@Test
	@TestOrder(5)
	public void addDateProperty() {

		log("Add Date property");

		debug(rootNode, 0);

		CreatePrimitiveRole createDateProperty = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createDateProperty.setRoleName("newDate");
		createDateProperty.setPrimitiveType(PrimitiveType.Date);
		createDateProperty.doAction();
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step4Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step4PrettyPrint.fml");
	}

	@Test
	@TestOrder(6)
	public void removeFooProperty() {

		log("Remove Foo property");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		FlexoProperty<?> fooProperty = virtualModel.getAccessibleProperty("foo");
		virtualModel.removeFromFlexoProperties(fooProperty);
		fooProperty.delete();

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step5Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step5PrettyPrint.fml");

	}

	@Test
	@TestOrder(7)
	public void removeDateProperty() {

		log("Remove Date property");

		FlexoProperty<?> dateProperty = virtualModel.getAccessibleProperty("newDate");
		virtualModel.removeFromFlexoProperties(dateProperty);
		dateProperty.delete();

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint1/Step6Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint1/Step6PrettyPrint.fml");
	}

}
