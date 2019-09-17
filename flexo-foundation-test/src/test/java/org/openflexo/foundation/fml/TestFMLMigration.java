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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.FixMethodOrder;
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
 * This unit test is intended to test ViewPoint loading
 * 
 * @author sylvain
 * 
 */
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
			Object[] o = new Object[2];
			o[0] = compilationUnitResource;
			o[1] = compilationUnitResource.getURI();
			returned.add(o);
		}

		return returned;
	}

	private CompilationUnitResource fmlResource;
	private FMLCompilationUnit initialXMLVersion;
	private FMLCompilationUnit reloadedFMLVersion;

	public TestFMLMigration(CompilationUnitResource fmlResource, String name) {
		System.out.println("********* TestFMLMigration " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
	}

	@Test
	public void test() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		step1_loadInitialXMLVersion();
		step2_prettyPrintInitialXMLVersion();
		step3_saveAsFML();
		step4_reloadFMLVersion();
		step5_compareBothVersions();
	}

	public void step1_loadInitialXMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Loading XML version for " + fmlResource);
		initialXMLVersion = fmlResource.getCompilationUnit();
		assertNotNull(initialXMLVersion);
		System.out.println("XML:");
		System.out.println(initialXMLVersion.getFMLModelFactory().stringRepresentation(initialXMLVersion.getVirtualModel()));
	}

	public void step2_prettyPrintInitialXMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Pretty-print FML version for " + fmlResource);
		System.out.println(initialXMLVersion.getFMLPrettyPrint());
	}

	public void step3_saveAsFML() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Saving FML version for " + fmlResource);
		((CompilationUnitResourceImpl) fmlResource).setPersistencyStrategy(PersistencyStrategy.FML);
		fmlResource.save();
		fmlResource.unloadResourceData(false);
		assertNull(fmlResource.getLoadedResourceData());
	}

	public void step4_reloadFMLVersion() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Reload FML version for " + fmlResource);
		reloadedFMLVersion = fmlResource.getCompilationUnit();
	}

	public void step5_compareBothVersions() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Compare both versions for " + fmlResource);
		assertTrue(initialXMLVersion.equalsObject(reloadedFMLVersion));
	}

}
