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

package org.openflexo.foundation.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Pins a DIAGNOSTIC rather than a behaviour.
 *
 * {@code super(...)} targets the parent concept's ANONYMOUS creation scheme; {@code super.<name>(...)} is how a NAMED one is reached (see
 * TestSuperCallToNamedScheme.fml). A parent declaring only {@code create::init(...)} therefore cannot be reached with {@code super(...)},
 * and FML validation must report it.
 *
 * That report is the only thing catching the mistake: at runtime the invalid binding is simply not executed, so the instance is created and
 * the parent's fields silently stay null.
 *
 * Should someone later make {@code super(...)} fall back to a named scheme by signature, this test turns red and the change gets discussed
 * rather than slipping in.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestInvalidSuperCallIsReported extends OpenflexoTestCase {

	static final String INVALID_SUPER_URI = "http://openflexo.org/test/TestResourceCenter/TestInvalidSuperCall.fml";

	static VirtualModelLibrary vmLibrary;

	@Test
	@TestOrder(1)
	public void loadServiceManager() throws Exception {
		instanciateTestServiceManager();
		vmLibrary = serviceManager.getVirtualModelLibrary();
		assertNotNull(vmLibrary);
	}

	@Test
	@TestOrder(2)
	public void invalidSuperCallIsReported() throws Exception {

		VirtualModel vm = vmLibrary.getVirtualModel(INVALID_SUPER_URI);
		assertNotNull("VirtualModel not found by URI " + INVALID_SUPER_URI, vm);

		// A ParseException leaves an EMPTY compilation unit behind, and an empty unit validates with zero errors - so
		// the assertion below would pass for the wrong reason. Demand that the concepts actually got parsed.
		assertFalse("TestInvalidSuperCall declares no concept: it most likely failed to PARSE", vm.getFlexoConcepts().isEmpty());

		ValidationModel validationModel = vmLibrary.getFMLValidationModel();
		ValidationReport report = validationModel.validate(vm.getCompilationUnit());

		assertEquals("super(...) toward a parent without an anonymous creation scheme must be reported, exactly once", 1,
				report.getErrorsCount());

		String message = validationModel.localizedIssueMessage(report.getAllErrors().iterator().next());
		System.out.println("Diagnostic for the invalid super call: " + message);
		assertTrue("the diagnostic should name the offending super call, was: " + message, message.contains("super("));
	}
}
