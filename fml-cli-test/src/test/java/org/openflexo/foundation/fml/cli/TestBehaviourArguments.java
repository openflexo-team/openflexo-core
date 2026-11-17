/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * An argument of a behaviour is designated by its name, like a local variable; parameters.name is deprecated but still evaluated
 * (CORE-F-14)
 */
public class TestBehaviourArguments extends FMLScriptParserTestCase {

	private static final String URI = "http://openflexo.org/test/TestResourceCenter/ArgumentNotation.fml";

	@Test
	public void argumentsAreDesignatedByTheirName() throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		Resource fmlResource = ResourceLocator.locateResource("TestResourceCenter/BehaviourArguments/ArgumentNotation.fmlscript");
		FMLScript script = parseFMLScript(fmlResource, interpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		for (AbstractCommand command : script.getCommands()) {
			command.execute();
		}
	}

	@Test
	public void legacyNotationIsReportedAndFixedAndALocalMustNotHideAnArgument() throws Exception {
		instanciateTestServiceManager();
		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(URI);
		assertNotNull(vm);

		// A local variable of the name of an argument is an error
		ValidationReport report = validate(vm);
		List<String> redeclarations = new ArrayList<>();
		for (ValidationError<?, ?> error : report.getAllErrors()) {
			if (error.getValidable() instanceof FMLControlGraph) {
				FMLControlGraphOwner owner = ((FMLControlGraph) error.getValidable()).getRootOwner();
				redeclarations.add(((FlexoBehaviour) owner).getName());
			}
		}
		assertEquals(redeclarations.toString(), 1, redeclarations.size());
		assertEquals("redeclaresAnArgument", redeclarations.get(0));

		// The legacy notation is reported as a warning, not as an error (a name that hides a property is not offered the fix: the
		// short name would assign the argument to itself); a variable assigned to itself is reported too
		int legacyWarnings = 0, selfAssignments = 0;
		for (Object issue : report.getAllIssues()) {
			if (String.valueOf(issue).contains("parameters_notation_is_deprecated")) {
				legacyWarnings++;
			}
			if (String.valueOf(issue).contains("a_variable_is_assigned_to_itself")) {
				selfAssignments++;
			}
		}
		assertTrue("Expected warnings for the legacy notation", legacyWarnings >= 2);
		assertTrue("Expected a warning for the assignation of a variable to itself", selfAssignments >= 1);
	}
}
