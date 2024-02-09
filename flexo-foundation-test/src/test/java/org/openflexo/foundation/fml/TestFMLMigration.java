/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceImpl;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceImpl.PersistencyStrategy;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * This unit test is intended to test XML to FML format migration
 * 
 * VERY IMPORTANT: uncomment "ignoreForEquality = true" in {@link FMLPrettyPrintable} to run that tests
 * 
 * @author sylvain
 * 
 */
@Ignore
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFMLMigration extends OpenflexoTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		instanciateTestServiceManager();

		System.out.println("ServiceManager= " + serviceManager);
		assertNotNull(serviceManager);

		Collection<Object[]> returned = new ArrayList<>();

		for (CompilationUnitResource compilationUnitResource : serviceManager.getResourceManager()
				.getRegisteredResources(CompilationUnitResource.class)) {
			if (!compilationUnitResource.getURI().contains("TestAnnotations")) {
				Object[] o = new Object[2];
				o[0] = new TestInfo(compilationUnitResource);
				o[1] = compilationUnitResource.getURI();
				returned.add(o);
			}
		}

		return returned;
	}

	private TestInfo testInfo;

	static class TestInfo {
		private CompilationUnitResource fmlResource;
		private FMLCompilationUnit initialXMLVersion;
		private FMLCompilationUnit reloadedFMLVersion;

		public TestInfo(CompilationUnitResource fmlResource) {
			this.fmlResource = fmlResource;
		}
	}

	public TestFMLMigration(TestInfo testInfo, String name) {
		System.out.println("********* TestFMLMigration " + testInfo.fmlResource + " name=" + name);
		this.testInfo = testInfo;
	}

	@Test
	public void step1_loadInitialXMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Loading XML version for " + testInfo.fmlResource);
		testInfo.initialXMLVersion = testInfo.fmlResource.getCompilationUnit();
		assertNotNull(testInfo.initialXMLVersion);
		System.out.println("XML:");
		System.out.println(
				testInfo.initialXMLVersion.getFMLModelFactory().stringRepresentation(testInfo.initialXMLVersion.getVirtualModel()));
	}

	@Test
	public void step2_prettyPrintInitialXMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Pretty-print FML version for " + testInfo.fmlResource);
		testInfo.initialXMLVersion.manageImports();
		System.out.println(testInfo.initialXMLVersion.getFMLPrettyPrint());

		/*VirtualModel virtualModel = testInfo.initialXMLVersion.getVirtualModel();
		System.out.println("virtualModel=" + virtualModel);
		System.out.println("cs:" + virtualModel.getCreationSchemes().get(0));
		System.out.println(virtualModel.getCreationSchemes().get(0).getNormalizedFML());*/
	}

	@Test
	public void step3_saveAsFML() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		if (!(testInfo.fmlResource.getIODelegate().getSerializationArtefact() instanceof File)) {
			return;
		}

		System.out.println("Saving FML version for " + testInfo.fmlResource);
		((CompilationUnitResourceImpl) testInfo.fmlResource).setPersistencyStrategy(PersistencyStrategy.FML);
		testInfo.fmlResource.save();

		System.out.println("contained: " + testInfo.fmlResource.getContainedCompilationUnitResources());
		for (CompilationUnitResource compilationUnitResource : testInfo.fmlResource.getContainedCompilationUnitResources()) {
			System.out.println(" > " + compilationUnitResource + " container: " + compilationUnitResource.getContainer());
		}

		for (CompilationUnitResource compilationUnitResource : testInfo.fmlResource.getContainedCompilationUnitResources()) {
			compilationUnitResource.unloadResourceData(false);
		}

		testInfo.fmlResource.unloadResourceData(false);
		assertNull(testInfo.fmlResource.getLoadedResourceData());

		System.out.println("contained: " + testInfo.fmlResource.getContainedCompilationUnitResources());
		for (CompilationUnitResource compilationUnitResource : testInfo.fmlResource.getContainedCompilationUnitResources()) {
			System.out.println(" > " + compilationUnitResource + " container: " + compilationUnitResource.getContainer());
		}
	}

	@Test
	public void step4_reloadFMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		if (!(testInfo.fmlResource.getIODelegate().getSerializationArtefact() instanceof File)) {
			return;
		}
		System.out.println("Reload FML version for " + testInfo.fmlResource);
		testInfo.reloadedFMLVersion = testInfo.fmlResource.getCompilationUnit();
	}

	@Test
	public void step5_compareBothVersions() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		if (!(testInfo.fmlResource.getIODelegate().getSerializationArtefact() instanceof File)) {
			return;
		}
		System.out.println("Compare both versions for " + testInfo.fmlResource);

		System.out.println("D'un cote: ");
		System.out.println(testInfo.initialXMLVersion.getNormalizedFML());
		System.out.println("De l'autre: ");
		System.out.println(testInfo.reloadedFMLVersion.getNormalizedFML());

		assertEquals(testInfo.initialXMLVersion.getVirtualModel().getVirtualModels(true),
				testInfo.reloadedFMLVersion.getVirtualModel().getVirtualModels(true));

		/*System.out.println("VMs pour :" + testInfo.initialXMLVersion.getVirtualModel() + " : "
				+ testInfo.initialXMLVersion.getVirtualModel().getVirtualModels(true));
		System.out.println("VMs la:   " + testInfo.reloadedFMLVersion.getVirtualModel() + " : "
				+ testInfo.reloadedFMLVersion.getVirtualModel().getVirtualModels(true));
		
		System.out.println("testInfo.initialXMLVersion.getResource()=" + testInfo.initialXMLVersion.getResource());
		System.out.println(
				"testInfo.initialXMLVersion.getResource().getContainer()=" + testInfo.initialXMLVersion.getResource().getContainer());*/

		/*VirtualModel initialVM = testInfo.initialXMLVersion.getVirtualModel();
		FlexoConcept initialConcept = initialVM.getFlexoConcepts().get(0);
		VirtualModel reloadedVM = testInfo.reloadedFMLVersion.getVirtualModel();
		FlexoConcept reloadedConcept = reloadedVM.getFlexoConcepts().get(0);
		
		System.out.println("author initial: " + initialVM.getAuthor());
		System.out.println("author reloaded: " + reloadedVM.getAuthor());
		
		System.out.println("concept author initial: " + initialConcept.getAuthor());
		System.out.println("concept author reloaded: " + reloadedConcept.getAuthor());*/

		assertTrue(testInfo.initialXMLVersion.equalsObject(testInfo.reloadedFMLVersion));
	}

}
