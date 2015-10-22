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
import org.openflexo.fib.swing.utils.FIBJPanel;
import org.openflexo.localization.FlexoLocalization;
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
public abstract class OpenflexoFIBTestCase extends OpenflexoTestCaseWithGUI {

	static final Logger logger = Logger.getLogger(OpenflexoFIBTestCase.class.getPackage().getName());

	public void validateFIB(Resource fibResouce) throws InterruptedException {
		try {
			System.out.println("Validating fib file " + fibResouce);
			FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibResouce);
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

	// SHould not be used any more
	/*

	public void validateFIB(String fibFileName) {
		try {
			System.out.println("Validating fib file " + fibFileName);
			FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibFileName);
			if (component == null) {
				fail("Component not found: " + fibFileName);
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError error : validationReport.getErrors()) {
				logger.severe("FIBComponent validation error: Object: " + error.getObject() + " message: " + error.getMessage());
			}
			assertEquals(0, validationReport.getErrorNb());
		} finally {
			FIBLibrary.instance().removeFIBComponentFromCache(fibFileName);
		}
	}
	*/

	public <T> FIBJPanel<T> instanciateFIB(Resource fibResource, T context, final Class<T> contextType) {
		return new FIBJPanel<T>(fibResource, context, FlexoLocalization.getMainLocalizer()) {

			@Override
			public Class<T> getRepresentedType() {
				return contextType;
			}

			@Override
			public void delete() {
			}

		};

	}
	// Should not be used anymore
	/*
		public <T> FIBJPanel<T> instanciateFIB(String fibFileName, T context, final Class<T> contextType) {
			return new FIBJPanel<T>(fibFileName, context, FlexoLocalization.getMainLocalizer()) {

				@Override
				public Class<T> getRepresentedType() {
					return contextType;
				}

				@Override
				public void delete() {
				}

			};

		}
		*/
}
