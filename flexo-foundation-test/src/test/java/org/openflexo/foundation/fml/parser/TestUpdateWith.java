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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestUpdateWith extends OpenflexoTestCase {

	// static FlexoEditor editor;
	static FMLCompilationUnitParser fmlParser = new FMLCompilationUnitParser();

	static FMLCompilationUnit compilationUnit;
	private static CompilationUnitResource fmlResource;

	@Test
	@TestOrder(1)
	public void initServiceManager()
			throws ParseException, ModelDefinitionException, IOException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();

		// editor = new DefaultFlexoEditor(null, serviceManager);
		// assertNotNull(editor);

		log("Initial version");

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		VirtualModel viewPoint = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(viewPoint);

		fmlResource = viewPoint.getResource();
		assertNotNull(fmlResource);

		compilationUnit = fmlResource.getCompilationUnit();

		System.out.println("Initial version: " + compilationUnit.getFMLPrettyPrint());

	}

	@Test
	@TestOrder(2)
	public void updateCompilationUnit() throws ParseException, ModelDefinitionException, IOException {
		log("updateCompilationUnit");

		String fml = "use org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot as FMLRT;\n\n"
				+ "@URI(\"http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml\")\n" + "@Version(\"0.1\")\n"
				+ "model TestVirtualModelA {\n" + "	TestVirtualModelA myModel with ModelInstance();\n" + "}\n";
		System.out.println(fml);

		FMLCompilationUnit compilationUnit2 = fmlParser.parse(fml, fmlResource.getFactory(), (modelSlotClasses) -> {
			// System.out.println("Parsing: " + editor.getTextArea().getText());
			// System.out.println("Uses model slot classes : " + modelSlotClasses);
			return fmlResource.updateFMLModelFactory(modelSlotClasses);
		}, true); // Finalize deserialization now

		System.out.println("Initial version\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Parsed version:\n" + compilationUnit2.getFMLPrettyPrint());

		compilationUnit.updateWith(compilationUnit2);
		System.out.println("Updated version\n" + compilationUnit.getFMLPrettyPrint());

	}

}
