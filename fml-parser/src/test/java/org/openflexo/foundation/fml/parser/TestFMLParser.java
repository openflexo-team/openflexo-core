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

import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.fml.rm.FMLParser.ParseException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * A parameterized suite of unit tests iterating on FML files.
 * 
 * For each FML file, parse it.
 * 
 * @author sylvain
 *
 */
@RunWith(Parameterized.class)
public class TestFMLParser extends FMLParserTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("NewFMLExamples"), ".fml");
	}

	private final Resource fmlResource;

	public TestFMLParser(Resource fmlResource, String name) {
		System.out.println("********* TestFMLParser " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
	}

	@Test
	public void testResource() throws ModelDefinitionException, ParseException, IOException {
		testFMLCompilationUnit(fmlResource);
	}

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

}
