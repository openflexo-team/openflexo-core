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

package org.openflexo.fml.fib.widget;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
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
@Ignore
public class TestFMLUpdateWith extends OpenflexoTestCase {

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
				o[1] = compilationUnitResource.getName(); // compilationUnitResource.getURI();
				returned.add(o);
			}
		}

		return returned;
	}

	private TestInfo testInfo;

	static class TestInfo {
		private CompilationUnitResource fmlResource;
		private FMLCompilationUnit initialVersion;
		private FMLCompilationUnit reloadedVersion;

		public TestInfo(CompilationUnitResource fmlResource) {
			this.fmlResource = fmlResource;
		}
	}

	public TestFMLUpdateWith(TestInfo testInfo, String name) {
		System.out.println("********* TestFMLMigration " + testInfo.fmlResource + " name=" + name);
		this.testInfo = testInfo;
	}

	@Test
	public void step1_load() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {
		System.out.println("Loading XML version for " + testInfo.fmlResource);
		testInfo.initialVersion = testInfo.fmlResource.getCompilationUnit();
		assertNotNull(testInfo.initialVersion);
	}

	@Test
	public void step2_updateWith() throws ModelDefinitionException, ParseException, IOException, SaveResourceException {

		String toParse = testInfo.fmlResource.getCompilationUnit().getFMLPrettyPrint();
		System.out.println("Parsing:");
		System.out.println(toParse);
		FMLCompilationUnitParser fmlParser = new FMLCompilationUnitParser();
		FMLCompilationUnit returned = fmlParser.parse(toParse, testInfo.fmlResource.getFactory(), (modelSlotClasses) -> {
			// We dont expect to have particular ModelSlots in this context, but be aware of that
			return null;
		}, true);
		System.out.println("OK c'est bien parse !!!");

		// assertTrue(testInfo.initialVersion.equalsObject(returned));

		testInfo.initialVersion.updateWith(returned);

		assertTrue(testInfo.initialVersion.equalsObject(returned, (p -> p.getPropertyIdentifier().equals("prettyPrintDelegate"))));
	}

}
