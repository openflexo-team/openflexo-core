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
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * Parse a FML file, perform some edits and checks that pretty-print is correct
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLPrettyPrint1 extends OpenflexoTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;
	private static FlexoConcept conceptA;

	static FlexoEditor editor;

	private static FMLCompilationUnit parseFile(Resource fileResource) throws ModelDefinitionException, ParseException {
		return FMLParser.parse(((FileResourceImpl) fileResource).getFile(), new FMLModelFactory(null, serviceManager));
	}

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private void testNormalizedFMLRepresentationEquals(String resourceFile) {
		testFileContentsEquals(compilationUnit.getPrettyPrintDelegate()
				.getNormalizedFMLRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()), resourceFile);
	}

	private void testFMLPrettyPrintEquals(String resourceFile) {
		testFileContentsEquals(compilationUnit.getPrettyPrintDelegate()
				.getFMLRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()), resourceFile);
	}

	private void testFileContentsEquals(String expected, String resourceFile) {
		final Resource resource = ResourceLocator.locateResource(resourceFile);
		try {
			String resourceContents = FileUtils.fileContents(resource.openInputStream(), null);
			assertSameContents(expected, resourceContents);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	private void assertSameContents(String s1, String s2) {

		List<String> rows1 = new ArrayList<>();
		List<String> rows2 = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new StringReader(s1))) {
			String nextLine = null;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					rows1.add(nextLine);
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
					System.out.println("2> [" + rows2.size() + "] : " + nextLine);
				}
			} while (nextLine != null);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		/*for (int i = 0; i < rows1.size(); i++) {
			System.out.println("*1> [" + i + "] : " + rows1.get(i));
		}
		for (int i = 0; i < rows2.size(); i++) {
			System.out.println("*2> [" + i + "] : " + rows2.get(i));
		}*/

		assertEquals("Row size differs", rows1.size(), rows2.size());
		for (int i = 0; i < rows1.size(); i++) {
			assertEquals(rows1.get(i).trim(), rows2.get(i).trim());
		}
	}

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint1/InitialVersion.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());

		testNormalizedFMLRepresentationEquals("TestFMLPrettyPrint1/Step1Normalized.fml");
		testFMLPrettyPrintEquals("TestFMLPrettyPrint1/Step1PrettyPrint.fml");

	}

	@Test
	@TestOrder(3)
	public void modifyImport() {
		JavaImportDeclaration listDeclaration = compilationUnit.getJavaImports().get(1);
		listDeclaration.setFullQualifiedClassName("java.util.ArrayList");
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals("TestFMLPrettyPrint1/Step2Normalized.fml");
		testFMLPrettyPrintEquals("TestFMLPrettyPrint1/Step2PrettyPrint.fml");
	}

	@Test
	@TestOrder(4)
	public void addStringProperty() {

		CreatePrimitiveRole createStringProperty = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createStringProperty.setRoleName("newString");
		createStringProperty.setPrimitiveType(PrimitiveType.String);
		createStringProperty.doAction();
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testNormalizedFMLRepresentationEquals("TestFMLPrettyPrint1/Step3Normalized.fml");
		testFMLPrettyPrintEquals("TestFMLPrettyPrint1/Step3PrettyPrint.fml");
	}

	/*@Test
	@TestOrder(5)
	public void addDateProperty() {
	
		CreatePrimitiveRole createStringProperty = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createStringProperty.setRoleName("newDate");
		createStringProperty.setPrimitiveType(PrimitiveType.Date);
		createStringProperty.doAction();
	
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
	}*/

	/*@Test
	@TestOrder(10)
	public void changeVirtualModelName() {
		log("Change name to AnOtherName");
		assertEquals("MyModel", virtualModel.getName());
		virtualModel.setName("AnOtherName");
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
	}*/

	/*@Test
	@TestOrder(3)
	public void addFlexoConcept() {
		log("addFlexoConcept");
	
		CreateFlexoConcept addConceptC = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptC.setNewFlexoConceptName("FlexoConceptC");
		addConceptC.doAction();
	
		FlexoConcept conceptC = addConceptC.getNewFlexoConcept();
	
		System.out.println("Normalized:");
		System.out.println(compilationUnit.getPrettyPrintDelegate()
				.getNormalizedFMLRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()));
	
		System.out.println("Current FML");
		System.out.println(">>>>>>>>>>>>>>>>" + compilationUnit.getPrettyPrintDelegate()
				.getFMLRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()) + "<<<<<<<<<<<<<<");
	}*/
}
