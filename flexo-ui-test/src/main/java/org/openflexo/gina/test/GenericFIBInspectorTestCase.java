/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.gina.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;

/**
 * Generic test case allowing to test a FIB component used as an inspector (a .inspector file)
 * 
 * @author sylvain
 * 
 */
public abstract class GenericFIBInspectorTestCase extends GenericFIBTestCase {

	static final Logger logger = Logger.getLogger(GenericFIBInspectorTestCase.class.getPackage().getName());

	public static FIBModelFactory INSPECTOR_FACTORY;

	static {
		try {
			INSPECTOR_FACTORY = new FIBModelFactory(null, FIBInspector.class);
		} catch (ModelDefinitionException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void validateFIB(Resource fibResource) {
		try {
			FIBComponent component = ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(fibResource, false, INSPECTOR_FACTORY);
			if (component == null) {
				fail("Component not found: " + fibResource.getURI());
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError<?, ?> error : validationReport.getAllErrors()) {
				String message = validationReport.getValidationModel().localizedIssueMessage(error);
				String details = validationReport.getValidationModel().localizedIssueDetailedInformations(error);
				logger.severe("FIBComponent validation error: Object: " + error.getValidable() + " message: " + message + "\ndetails: "
						+ details);
			}
			assertEquals(0, validationReport.getErrorsCount());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interrupted FIB validation");
		} finally {
			ApplicationFIBLibraryImpl.instance().removeFIBComponentFromCache(fibResource);
		}
	}

	public static String generateInspectorTestCaseClass(File directory, String relativePath) {
		StringBuffer sb = new StringBuffer();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				generateInspectorTestCaseClass(f, relativePath + f.getName() + File.separator, sb);
			}
			else if (f.getName().endsWith(".inspector")) {
				String fibName = f.getName().substring(0, f.getName().indexOf(".inspector"));
				sb.append("@Test\n");
				sb.append("public void test" + fibName + "Inspector() {\n");
				sb.append("  validateFIB(\"" + relativePath + f.getName() + "\");\n");
				sb.append("}\n\n");
			}
		}
		return sb.toString();
	}

	private static void generateInspectorTestCaseClass(File directory, String relativePath, StringBuffer sb) {
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				generateFIBTestCaseClass(f, relativePath + f.getName() + File.separator);
			}
			else if (f.getName().endsWith(".inspector")) {
				String fibName = f.getName().substring(0, f.getName().indexOf(".inspector"));
				sb.append("@Test\n");
				sb.append("public void test" + fibName + "Inspector() {\n");
				sb.append("  validateFIB(\"" + relativePath + f.getName() + "\");\n");
				sb.append("}\n\n");
			}
		}
	}

}
