package org.openflexo.fib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.openflexo.OpenflexoProjectAtRunTimeTestCaseWithGUI;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.validation.ValidationError;
import org.openflexo.fib.model.validation.ValidationReport;
import org.openflexo.fib.view.widget.DefaultFIBCustomComponent;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.test.OrderedRunner;

/**
 * Provides a JUnit 4 generic environment of Openflexo-core for a FIB testing purposes in graphics environment
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public abstract class OpenflexoFIBTestCaseWithProjectAtRunTime extends OpenflexoProjectAtRunTimeTestCaseWithGUI {

	static final Logger logger = Logger.getLogger(OpenflexoFIBTestCaseWithProjectAtRunTime.class.getPackage().getName());

	public void validateFIB(File fibFile) {
		try {
			System.out.println("Validating fib file " + fibFile);
			FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibFile);
			if (component == null) {
				fail("Component not found: " + fibFile.getAbsolutePath());
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError error : validationReport.getErrors()) {
				logger.severe("FIBComponent validation error: Object: " + error.getObject() + " message: " + error.getMessage());
			}
			assertEquals(0, validationReport.getErrorNb());
		} finally {
			FIBLibrary.instance().removeFIBComponentFromCache(fibFile);
		}
	}

	public <T> DefaultFIBCustomComponent<T> instanciateFIB(File fibFile, T context, final Class<T> contextType) {
		return new DefaultFIBCustomComponent<T>(fibFile, context, FlexoLocalization.getMainLocalizer()) {

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
