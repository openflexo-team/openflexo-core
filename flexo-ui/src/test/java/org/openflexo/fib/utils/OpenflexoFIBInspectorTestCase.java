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

package org.openflexo.fib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.openflexo.OpenflexoTestCaseWithGUI;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBModelFactory;
import org.openflexo.fib.swing.utils.FIBJPanel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.test.OrderedRunner;

/**
 * Provides a JUnit 4 generic environment of Openflexo-core for a FIB testing purposes in graphics environment
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public abstract class OpenflexoFIBInspectorTestCase extends OpenflexoTestCaseWithGUI {

	static final Logger logger = Logger.getLogger(OpenflexoFIBInspectorTestCase.class.getPackage().getName());

	public static FIBModelFactory INSPECTOR_FACTORY;

	static {
		try {
			INSPECTOR_FACTORY = new FIBModelFactory(FIBInspector.class);
		} catch (ModelDefinitionException e1) {
			e1.printStackTrace();
		}
	}

	public void validateFIB(Resource fibResouce) throws InterruptedException {
		try {
			System.out.println("Validating fib file " + fibResouce);
			FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibResouce, false, INSPECTOR_FACTORY);
			if (component == null) {
				fail("Component not found: " + fibResouce.getURI());
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError error : validationReport.getErrors()) {
				logger.severe("FIBComponent validation error: Object: " + error.getValidable() + " message: " + error.getMessage());
			}
			assertEquals(0, validationReport.getErrorsCount());
		} finally {
			FIBLibrary.instance().removeFIBComponentFromCache(fibResouce);
		}
	}

	public <T> FIBJPanel<T> instanciateFIB(Resource fibResource, T context, final Class<T> contextType) {

		FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibResource, false, INSPECTOR_FACTORY);

		return new FIBJPanel<T>(component, context, FlexoLocalization.getMainLocalizer()) {

			@Override
			public Class<T> getRepresentedType() {
				return contextType;
			}

			@Override
			public void delete() {
			}

		};

	}
}
