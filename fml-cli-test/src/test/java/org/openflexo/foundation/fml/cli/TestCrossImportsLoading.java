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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Loading compilation units which import each other (<code>FML/CrossImports.fml</code>: Core types its members with DerivedA and DerivedB,
 * which extend Base, which types its own member with a concept of Core) must be quiet: a unit which is being loaded higher in the stack is
 * not a failure, it is completed by the load in progress (two-passes loading, CORE-D-34).
 * 
 * Runs <code>AutomatedTests/TestCrossImports.fmlscript</code> (CORE-D-14, which asserts the values) while collecting what the loading
 * logs, and checks that none of the warnings raised by a unit used before it is complete shows up.
 * 
 * @author sylvain
 *
 */
public class TestCrossImportsLoading extends FMLScriptParserTestCase {

	/** What the loading of units importing each other logged while it ignored that one of them was incomplete */
	private static final String[] CYCLE_WARNINGS = { "Preventing StackOverflow", "Unresolved type", "Parent concept", "still invalid" };

	private final List<String> warnings = Collections.synchronizedList(new ArrayList<>());

	private final Handler handler = new Handler() {
		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
				warnings.add(record.getMessage());
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	};

	@Before
	public void collectWarnings() {
		Logger.getLogger("").addHandler(handler);
	}

	@After
	public void stopCollectingWarnings() {
		Logger.getLogger("").removeHandler(handler);
	}

	@Test
	public void loadingUnitsImportingEachOtherIsQuiet()
			throws ParseException, ModelDefinitionException, IOException, FMLCommandExecutionException {

		instanciateTestServiceManager();
		CommandInterpreter commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);

		Resource fmlResource = ResourceLocator.locateResource("TestResourceCenter/AutomatedTests/TestCrossImports.fmlscript");
		assertNotNull(fmlResource);
		FMLScript script = parseFMLScript(fmlResource, commandInterpreter);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}

		List<String> unexpected = new ArrayList<>();
		synchronized (warnings) {
			for (String warning : warnings) {
				for (String cycleWarning : CYCLE_WARNINGS) {
					if (warning != null && warning.contains(cycleWarning)) {
						unexpected.add(warning);
					}
				}
			}
		}
		assertTrue("Loading units importing each other logged: " + unexpected, unexpected.isEmpty());
	}
}
