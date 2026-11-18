/**
 * 
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.foundation.fml.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.foundation.fml.validation.BindingMustBeValid;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;

/**
 * A {@code new} climbs the containment tree to find its container (CORE-F-18): all the {@code new} of {@code Containment.fml} are valid
 * but the one of a nested concept created in the model, which has no Alpha to climb to. Their execution is checked by
 * {@code TestContainment.fmlscript}.
 */
public class TestContainmentValidation extends FMLScriptParserTestCase {

	@Test
	public void climbingNewsAreValid() throws Exception {
		instanciateTestServiceManager();
		VirtualModel vm = serviceManager.getVirtualModelLibrary()
				.getVirtualModel("http://openflexo.org/test/TestResourceCenter/Containment.fml");
		assertNotNull(vm);
		ValidationReport report = validate(vm);
		// Exactly one error: the new Beta(...) of Containment.orphanBeta, that nothing can hold
		assertEquals("Errors: " + report.getAllErrors(), 1, report.getAllErrors().size());
		ValidationError<?, ?> error = report.getAllErrors().iterator().next();
		assertTrue("Unexpected error on " + error.getValidable(), error.getValidable() instanceof FMLObject
				&& ((FMLObject) error.getValidable()).getStringRepresentation().contains("new Beta("));
		// The message names what was expected and where it was looked at
		String reason = null;
		if (error instanceof BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue) {
			reason = ((BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue<?>) error).getReason();
		}
		else if (error instanceof BindingMustBeValid.InvalidBindingIssue) {
			reason = ((BindingMustBeValid.InvalidBindingIssue<?>) error).getReason();
		}
		assertNotNull("No reason in " + error, reason);
		assertTrue("Reason: " + reason, reason.contains("an instance of Alpha") && reason.contains("looked at: Containment"));
	}

	/**
	 * CORE-D-89: a bare new of a concept inherited from the parent VirtualModel is valid in a concept of the child VirtualModel
	 */
	@Test
	public void inheritedRootConceptIsHosted() throws Exception {
		instanciateTestServiceManager();
		for (String uri : new String[] { "KDGh27B", "KDGh27C" }) {
			VirtualModel vm = serviceManager.getVirtualModelLibrary()
					.getVirtualModel("http://openflexo.org/test/TestResourceCenter/" + uri + ".fml");
			assertNotNull(vm);
			ValidationReport report = validate(vm);
			assertEquals(uri + " errors: " + report.getAllErrors(), 0, report.getAllErrors().size());
		}
	}
}
