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

package org.openflexo.foundation.fml.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.ResourceLocator;

/**
 * Known defects of KNOWN_DEFECTS.md that an FML-script alone cannot state: a call that must fail, and what validation must
 * report. Each test asserts the intended behaviour and fails until its defect is fixed. Skipped unless
 * {@value KnownDefectsTests#ENABLING_VARIABLE} is set (see {@link KnownDefectsTests}).
 */
public class KnownDefectsJavaTests extends FMLScriptParserTestCase {

	private static final String SIGNATURES_URI = "http://openflexo.org/test/TestResourceCenter/KDSignatures.fml";

	@BeforeClass
	public static void checkEnabled() {
		KnownDefectsTests.assumeKnownDefectsEnabled();
	}

	/**
	 * CORE-D-47: a creation scheme that fails must fail the {@code new} that called it, and leave no instance behind.
	 * <p>
	 * Runs {@code KnownDefectsSupport/CORE-D-47.fmlscript} command by command: the call of line 5 must fail, and the asserts
	 * that follow it state what it must leave.
	 */
	@Test
	public void coreD47_failedCreationSchemeFailsTheCaller() throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		FMLScript script = parseFMLScript(ResourceLocator.locateResource("TestResourceCenter/KnownDefectsSupport/CORE-D-47.fmlscript"),
				interpreter);
		boolean callFailed = false;
		for (AbstractCommand command : script.getCommands()) {
			if (command.getLine() == 5) {
				try {
					command.execute();
				} catch (Exception e) {
					callFailed = true;
				}
			}
			else {
				command.execute();
			}
		}
		assertTrue("CORE-D-47: outer() reported success although the creation scheme of Bomb failed", callFailed);
	}

	private VirtualModel signatures() throws Exception {
		instanciateTestServiceManager();
		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(SIGNATURES_URI);
		assertNotNull(vm);
		return vm;
	}

	private List<ValidationError<?, ?>> errorsOn(VirtualModel vm, String conceptName) {
		ValidationReport report = validate(vm);
		List<ValidationError<?, ?>> returned = new ArrayList<>();
		for (ValidationError<?, ?> error : report.getAllErrors()) {
			Object validable = error.getValidable();
			FlexoConcept concept = validable instanceof FlexoConcept ? (FlexoConcept) validable
					: validable instanceof FlexoConceptObject ? ((FlexoConceptObject) validable).getFlexoConcept() : null;
			if (concept != null && conceptName.equals(concept.getName())) {
				returned.add(error);
			}
		}
		return returned;
	}

	/** CORE-D-49: overloads differing by parameter type are distinct behaviours. */
	@Test
	public void coreD49_typedOverloadsAreNotDuplicates() throws Exception {
		assertEquals("CORE-D-49: f(int x) and f(String x) reported as duplicates", 0, errorsOn(signatures(), "TypedOverloads").size());
	}

	/** CORE-D-49: two behaviours differing only by parameter names have the same signature. */
	@Test
	public void coreD49_renamedParametersAreDuplicates() throws Exception {
		assertTrue("CORE-D-49: g(int x) and g(int y) not reported as duplicates", errorsOn(signatures(), "RenamedDuplicate").size() > 0);
	}

	/** CORE-D-49: an override must return a type compatible with the overridden behaviour. */
	@Test
	public void coreD49_overrideReturnTypeIsChecked() throws Exception {
		assertTrue("CORE-D-49: String h() accepted as an override of int h()", errorsOn(signatures(), "ReturnChild").size() > 0);
	}

	/** CORE-D-49: a concrete concept must implement the abstract behaviours it inherits. */
	@Test
	public void coreD49_inheritedAbstractBehaviourMustBeImplemented() throws Exception {
		VirtualModel vm = signatures();
		if (errorsOn(vm, "ConcreteChild").isEmpty()) {
			fail("CORE-D-49: ConcreteChild does not implement the inherited abstract k(), and validation says nothing");
		}
	}
}
