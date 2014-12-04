package org.openflexo.fib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBModelFactory;
import org.openflexo.inspector.FIBInspector;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationReport;
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
			INSPECTOR_FACTORY = new FIBModelFactory(FIBInspector.class);
		} catch (ModelDefinitionException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void validateFIB(Resource fibResource) {
		try {
			FIBComponent component = FIBLibrary.instance().retrieveFIBComponent(fibResource, false, INSPECTOR_FACTORY);
			if (component == null) {
				fail("Component not found: " + fibResource.getURI());
			}
			ValidationReport validationReport = component.validate();
			for (ValidationError error : validationReport.getErrors()) {
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
			FIBLibrary.instance().removeFIBComponentFromCache(fibResource);
		}
	}

	public static String generateInspectorTestCaseClass(File directory, String relativePath) {
		StringBuffer sb = new StringBuffer();
		for (File f : directory.listFiles()) {
			if (f.getName().endsWith(".inspector")) {
				String fibName = f.getName().substring(0, f.getName().indexOf(".inspector"));
				sb.append("@Test\n");
				sb.append("public void test" + fibName + "Inspector() {\n");
				sb.append("  validateFIB(\"" + relativePath + f.getName() + "\");\n");
				sb.append("}\n\n");
			}
		}
		return sb.toString();
	}

}
