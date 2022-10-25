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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test BindingPath parsing
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestCrossReferences extends FMLParserTestCase {

	static FlexoEditor editor;

	static FMLCompilationUnit compilationUnit;

	private static VirtualModel crossReferences1VM;
	private static VirtualModel crossReferences2VM;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoConcept conceptC;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		// editor = new DefaultFlexoEditor(null, serviceManager);
		// assertNotNull(editor);

	}

	@Test
	@TestOrder(2)
	public void checkCrossReferences()
			throws ParseException, ModelDefinitionException, IOException, ResourceLoadingCancelledException, FlexoException {
		log("checkCrossReferences");

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);

		CompilationUnitResource vm1Resource = (CompilationUnitResource) serviceManager.getResourceManager()
				.getResource("http://openflexo.org/test/TestResourceCenter/TestCrossReferences1.fml");
		CompilationUnitResource vm2Resource = (CompilationUnitResource) serviceManager.getResourceManager()
				.getResource("http://openflexo.org/test/TestResourceCenter/TestCrossReferences2.fml");

		System.out.println("vm1Resource=" + vm1Resource);
		System.out.println("vm2Resource=" + vm2Resource);
		assertNotNull(vm1Resource);
		assertNotNull(vm2Resource);

		assertTrue(vm1Resource.getDependencies().contains(vm2Resource));
		assertTrue(vm2Resource.getDependencies().contains(vm1Resource));

	}

	@Test
	@TestOrder(3)
	public void loadCompilationUnit()
			throws ParseException, ModelDefinitionException, IOException, ResourceLoadingCancelledException, FlexoException {
		log("Initial version");

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		crossReferences1VM = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestCrossReferences1.fml");
		assertNotNull(crossReferences1VM);

		crossReferences2VM = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestCrossReferences2.fml", false);
		assertNotNull(crossReferences2VM);

		for (FlexoResource<?> resource : serviceManager.getResourceManager().getLoadedResources()) {
			System.out.println("Loaded: " + resource);
		}

	}

	@Test
	@TestOrder(4)
	public void checkReferencesAreOK() throws ParseException, ModelDefinitionException, IOException {
		log("checkReferencesAreOK()");

		conceptB = crossReferences1VM.getFlexoConcept("ConceptB");
		assertNotNull(conceptB);
		conceptC = crossReferences1VM.getFlexoConcept("ConceptC");
		assertNotNull(conceptC);

		conceptA = crossReferences2VM.getFlexoConcept("ConceptA");
		assertNotNull(conceptA);

		assertTrue(conceptB.getParentFlexoConcepts().contains(conceptA));
		assertTrue(crossReferences1VM.getParentFlexoConcepts().contains(crossReferences2VM));

	}

}
