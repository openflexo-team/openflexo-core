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
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.parser.fmlnodes.ElementImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLSimplePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ModelSlotPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.UseDeclarationNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
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
public class TestFMLPrettyPrint4 extends FMLParserTestCase {

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

	private static FMLCompilationUnitNode rootNode;
	private static UseDeclarationNode useDeclNode;
	private static ElementImportNode elementImportNode;
	private static VirtualModelNode vmNode;
	private static ModelSlotPropertyNode modelSlotNode;
	private static FMLSimplePropertyValueNode modelSlotP1Node;
	private static FMLSimplePropertyValueNode modelSlotP2Node;

	private static UseModelSlotDeclaration useDeclaration;
	private static ElementImportDeclaration importDeclaration;
	private static JavaImportDeclaration stringImport;
	private static FMLRTModelSlot<?, ?> myModelModelSlot;

	private static JavaImportNode stringImportNode;

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint4/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("TestViewPointA", virtualModel.getName());

		assertEquals(1, compilationUnit.getUseDeclarations().size());
		assertNotNull(useDeclaration = compilationUnit.getUseDeclarations().get(0));

		assertEquals(1, compilationUnit.getElementImports().size());
		assertNotNull(importDeclaration = compilationUnit.getElementImports().get(0));

		assertEquals(1, virtualModel.getFlexoProperties().size());
		assertNotNull(myModelModelSlot = (FMLRTModelSlot<?, ?>) virtualModel.getFlexoProperties().get(0));

		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(vmNode = (VirtualModelNode) rootNode.getObjectNode(virtualModel));
		assertNotNull(useDeclNode = (UseDeclarationNode) rootNode.getObjectNode(useDeclaration));
		assertNotNull(elementImportNode = (ElementImportNode) rootNode.getObjectNode(importDeclaration));
		assertNotNull(modelSlotNode = (ModelSlotPropertyNode) rootNode.getObjectNode(myModelModelSlot));
		assertEquals(2, modelSlotNode.getChildren().size());
		modelSlotP1Node = (FMLSimplePropertyValueNode) modelSlotNode.getChildren().get(0);
		modelSlotP2Node = (FMLSimplePropertyValueNode) modelSlotNode.getChildren().get(1);

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		/*System.out.println("Prout");
		for (FMLPropertyValue<?, ?> fmlPropertyValue : myModelModelSlot.getFMLPropertyValues(myModelModelSlot.getFMLModelFactory())) {
			System.out.println(" > " + fmlPropertyValue);
		}
		System.exit(-1);*/

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint4/Step1Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint4/Step1PrettyPrint.fml");

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);

		assertEquals("(1:0)-(13:1)", rootNode.getLastParsedFragment().toString());
		assertEquals(null, rootNode.getPrelude());
		assertEquals(null, rootNode.getPostlude());

		assertEquals("(1:0)-(1:80)", useDeclNode.getLastParsedFragment().toString());
		assertEquals(null, useDeclNode.getPrelude());
		assertEquals("(1:80)-(3:0)", useDeclNode.getPostlude().toString());

		assertEquals("(3:0)-(3:73)", elementImportNode.getLastParsedFragment().toString());
		assertEquals(null, elementImportNode.getPrelude());
		assertEquals("(3:73)-(5:0)", elementImportNode.getPostlude().toString());

		assertEquals("(5:0)-(13:1)", vmNode.getLastParsedFragment().toString());
		assertEquals(null, vmNode.getPrelude());
		assertEquals(null, vmNode.getPostlude());

		assertEquals("(8:1)-(8:67)", modelSlotNode.getLastParsedFragment().toString());
		assertEquals("(8:0)-(8:1)", modelSlotNode.getPrelude().toString());
		assertEquals("(8:67)-(9:0)", modelSlotNode.getPostlude().toString());

		assertEquals("(8:33)-(8:48)", modelSlotP1Node.getLastParsedFragment().toString());
		assertEquals(null, modelSlotP1Node.getPrelude());
		assertEquals("(8:48)-(8:49)", modelSlotP1Node.getPostlude().toString());

		assertEquals("(8:49)-(8:65)", modelSlotP2Node.getLastParsedFragment().toString());
		assertEquals(null, modelSlotP2Node.getPrelude());
		assertEquals(null, modelSlotP2Node.getPostlude());

	}

	@Test
	@TestOrder(3)
	public void editStringProperty() throws ParseException, IOException {

		String fml = compilationUnit.getFMLPrettyPrint();
		fml = fml.substring(0, fml.length() - 2);
		fml = fml + "\nString foo;";
		fml = fml + "\n" + "}" + "\n";

		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();

		FMLCompilationUnit returned = parser.parse(fml, compilationUnit.getFMLModelFactory(), (modelSlotClasses) -> {
			return null;
		}, false);

		// This is the update process
		compilationUnit.updateWith(returned);

		compilationUnit.manageImports();

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint4/Step2Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint4/Step2PrettyPrint.fml");

	}

	@Test
	@TestOrder(4)
	public void addDateProperty() {

		log("Add Date property");

		CreatePrimitiveRole createStringProperty = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createStringProperty.setRoleName("newDate");
		createStringProperty.setPrimitiveType(PrimitiveType.Date);
		createStringProperty.doAction();

		debug(rootNode, 0);

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint4/Step3Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint4/Step3PrettyPrint.fml");

	}
}
