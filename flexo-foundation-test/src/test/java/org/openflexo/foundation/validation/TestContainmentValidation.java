/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation-test, a component of the software infrastructure 
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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Containment of concepts (CORE-D-95): a concept extending a nested one is declared in the container of its parent, or in a subtype of
 * it. The fixtures of the test resource center which extend a nested concept stay valid for this rule. This only checks that nothing is
 * reported: the cases which must be reported are in TestConceptContainment (fml-parser), which parses them.
 */
@RunWith(OrderedRunner.class)
public class TestContainmentValidation extends OpenflexoTestCase {

	static final String BASE = "http://openflexo.org/test/TestResourceCenter/";

	static VirtualModelLibrary vmLibrary;

	@Test
	@TestOrder(1)
	public void loadServiceManager() throws Exception {
		instanciateTestServiceManager();
		vmLibrary = serviceManager.getVirtualModelLibrary();
		assertNotNull(vmLibrary);
	}

	private static List<String> containmentIssues(String uri) throws Exception {
		VirtualModel vm = vmLibrary.getVirtualModel(uri);
		assertNotNull(uri, vm);
		ValidationReport report = vmLibrary.getFMLValidationModel().validate(vm.getCompilationUnit());
		List<String> found = new ArrayList<>();
		for (ValidationIssue<?, ?> issue : report.getAllIssues()) {
			if (issue.getCause() instanceof FlexoConcept.ContainerFlexoConceptMustBeConsistentWithParents) {
				found.add(((FlexoConcept) issue.getValidable()).getName());
			}
		}
		return found;
	}

	@Test
	@TestOrder(2)
	public void inheritedConceptsFixtureIsValid() throws Exception {
		assertEquals(List.of(), containmentIssues(BASE + "TestInheritedConcepts.fml"));
	}

	/** ChildVM1 declares two concepts named ChildConceptB (one nested, one at the root): pins that none is reported. */
	@Test
	@TestOrder(3)
	public void childVM1IsValid() throws Exception {
		assertEquals(List.of(), containmentIssues(BASE + "TestVirtualModelA2.fml/ChildVM1.fml"));
	}

	/**
	 * The fixtures migrated for CORE-D-96, which extended a root concept from outside the root of its VirtualModel: TestMetaData (now
	 * extending TestCrossReferences1), Library (Publication nested in Shelf with Book), Containment (SubAlpha no longer an Alpha)
	 */
	@Test
	@TestOrder(4)
	public void fixturesMigratedForCoreD96AreValid() throws Exception {
		assertEquals(List.of(), containmentIssues(BASE + "TestMetaData.fml"));
		assertEquals(List.of(), containmentIssues(BASE + "Library.fml"));
		assertEquals(List.of(), containmentIssues(BASE + "Containment.fml"));
	}
}
