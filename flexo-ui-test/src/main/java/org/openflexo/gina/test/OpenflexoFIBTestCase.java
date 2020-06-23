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

import org.junit.runner.RunWith;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;

/**
 * Provides a JUnit 4 generic environment of Openflexo-core for a FIB testing purposes in graphics environment
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public abstract class OpenflexoFIBTestCase extends OpenflexoTestCaseWithGUI {

	static final Logger logger = Logger.getLogger(OpenflexoFIBTestCase.class.getPackage().getName());

	public void validateFIB(String fibRelativePath) {
		try {
			validateFIB(ResourceLocator.locateResource(fibRelativePath));
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	public void validateFIB(Resource fibResouce) throws InterruptedException {

		if (serviceManager == null) {
			instanciateTestServiceManager();
		}

		try {
			System.out.println("Validating fib file " + fibResouce);
			FIBComponent component = ApplicationFIBLibraryImpl.instance(serviceManager.getTechnologyAdapterService())
					.retrieveFIBComponent(fibResouce);

			if (component == null) {
				fail("Component not found: " + fibResouce.getURI());
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError<?, ?> error : validationReport.getAllErrors()) {
				logger.severe("FIBComponent validation error: Object: " + error.getValidable() + " message: "
						+ validationReport.getValidationModel().localizedIssueMessage(error) + " details="
						+ validationReport.getValidationModel().localizedIssueDetailedInformations(error));
			}
			assertEquals(0, validationReport.getErrorsCount());
		} finally {
			ApplicationFIBLibraryImpl.instance().removeFIBComponentFromCache(fibResouce);
		}
	}

	public <T> FIBJPanel<T> instanciateFIB(Resource fibResource, T context, final Class<T> contextType) {
		return new FIBJPanel<T>(fibResource, context, ApplicationFIBLibraryImpl.instance(serviceManager.getTechnologyAdapterService()),
				FlexoLocalization.getMainLocalizer()) {

			@Override
			public Class<T> getRepresentedType() {
				return contextType;
			}

			@Override
			public void delete() {
			}

		};

	}

	public static String generateFIBTestCaseClass(File directory, String relativePath) {
		StringBuffer sb = new StringBuffer();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				generateFIBTestCaseClass(f, relativePath + f.getName() + File.separator, sb);
			}
			else if (f.getName().endsWith(".fib")) {
				String fibName = f.getName().substring(0, f.getName().indexOf(".fib"));
				sb.append("@Test\n");
				sb.append("public void test" + fibName + "() {\n");
				sb.append("  validateFIB(\"" + relativePath + f.getName() + "\");\n");
				sb.append("}\n\n");
			}
		}
		return sb.toString();
	}

	private static void generateFIBTestCaseClass(File directory, String relativePath, StringBuffer sb) {
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				generateFIBTestCaseClass(f, relativePath + f.getName() + File.separator);
			}
			else if (f.getName().endsWith(".fib")) {
				String fibName = f.getName().substring(0, f.getName().indexOf(".fib"));
				sb.append("@Test\n");
				sb.append("public void test" + fibName + "() {\n");
				sb.append("  validateFIB(\"" + relativePath + f.getName() + "\");\n");
				sb.append("}\n\n");
			}
		}
	}

}
