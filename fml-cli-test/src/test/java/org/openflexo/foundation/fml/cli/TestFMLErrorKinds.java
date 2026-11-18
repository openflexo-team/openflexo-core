/**
 * 
 * Copyright (c) 2026, Openflexo
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.rt.error.FMLCardinalityError;
import org.openflexo.foundation.fml.rt.error.FMLExecutionFailure;
import org.openflexo.foundation.fml.rt.error.FMLHostError;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.foundation.fml.rt.error.FMLResolutionError;
import org.openflexo.foundation.fml.rt.error.FMLStack;
import org.openflexo.foundation.fml.rt.error.FMLStackFrame;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * One FML-script per kind of error (CORE-F-11 phase 3): the scripts of {@code TestResourceCenter/ErrorKinds}, on the behaviours appended
 * at the end of the fixture {@code FML/ExecutionFailures.fml}. Each failure is asserted on its type, on the line of the failing statement
 * and on the frame of the behaviour; nothing is printed to stderr. The line numbers asserted here are the ones of that fixture.
 */
public class TestFMLErrorKinds extends FMLScriptParserTestCase {

	private static final String MUST_FAIL = "mustFail";

	/**
	 * Run supplied script, stderr captured; return the failure of its command assigning {@code mustFail}, or null when it has none (any
	 * other failure fails the test)
	 */
	private Throwable runScript(String name) throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		Resource fmlResource = ResourceLocator.locateResource("TestResourceCenter/ErrorKinds/" + name);
		FMLScript script = parseFMLScript(fmlResource, interpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		Throwable failure = null;
		PrintStream err = System.err;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured, true));
		try {
			for (AbstractCommand command : script.getCommands()) {
				if (command.toString().trim().startsWith(MUST_FAIL)) {
					try {
						command.execute();
					} catch (Exception e) {
						failure = e;
						continue;
					}
					fail(name + ", line " + command.getLine() + ": " + command + " did not fail");
				}
				else {
					command.execute();
				}
			}
		} finally {
			System.setErr(err);
		}
		assertEquals(name + ": printed to stderr", "", captured.toString());
		return failure;
	}

	/**
	 * Assert that supplied failure is (or wraps) an FML error of supplied type, whose innermost frames are supplied ones: behaviour names
	 * and lines, innermost first
	 */
	private static void assertError(Throwable failure, Class<?> type, Object... behavioursAndLines) {
		assertNotNull("No failure", failure);
		FMLExecutionFailure fmlFailure = FMLExecutionFailure.findFailure(failure);
		assertNotNull("Not an FML failure: " + failure, fmlFailure);
		assertEquals(String.valueOf(fmlFailure), type, fmlFailure.getClass());
		FMLStack stack = fmlFailure.getFMLStack();
		System.out.println("FML stack of " + fmlFailure + ":\n" + stack);
		List<FMLStackFrame> frames = stack.getFrames();
		assertTrue(frames.toString(), frames.size() >= behavioursAndLines.length / 2);
		for (int i = 0; i < behavioursAndLines.length / 2; i++) {
			FMLStackFrame frame = frames.get(i);
			assertEquals(frame.toString(), behavioursAndLines[2 * i], frame.getBehaviour());
			assertEquals(frame.toString(), behavioursAndLines[2 * i + 1], frame.getLine());
			assertNotNull(frame.toString(), frame.getStatement());
		}
	}

	@Test
	public void matchCriterionIsNotAProperty() throws Exception {
		assertError(runScript("MatchCriterionNotAProperty.fmlscript"), FMLResolutionError.class);
	}

	@Test
	public void nullInConditionPath() throws Exception {
		assertError(runScript("NullInConditionPath.fmlscript"), FMLNullReferenceError.class, "nullInConditionPath()", 173);
	}

	@Test
	public void nullCondition() throws Exception {
		assertError(runScript("NullCondition.fmlscript"), FMLNullReferenceError.class, "nullCondition()", 80);
	}

	@Test
	public void nullTernaryCondition() throws Exception {
		assertError(runScript("NullTernaryCondition.fmlscript"), FMLNullReferenceError.class, "nullTernaryCondition()", 189);
	}

	@Test
	public void andOrAreLazy() throws Exception {
		assertNull(runScript("ShortCircuit.fmlscript"));
	}

	@Test
	public void failureOfJavaCode() throws Exception {
		assertError(runScript("HostFailure.fmlscript"), FMLHostError.class, "hostFailure()", 195);
	}

	@Test
	public void iterationOverNull() throws Exception {
		assertError(runScript("NullIteration.fmlscript"), FMLNullReferenceError.class, "nullIteration()", 205);
	}

	@Test
	public void failingLoopInitializer() throws Exception {
		assertError(runScript("FailingLoopInitializer.fmlscript"), FMLNullReferenceError.class, "failingLoopInitializer()", 213);
	}

	@Test
	public void failingGetterReadByItsName() throws Exception {
		assertError(runScript("FailingGetter.fmlscript"), FMLNullReferenceError.class, "get failingGetter", 166, "readFailingGetter()", 220);
	}

	@Test
	public void failingGetterReadThroughThis() throws Exception {
		assertError(runScript("FailingGetterThroughThis.fmlscript"), FMLNullReferenceError.class, "get failingGetter", 166,
				"readFailingGetterThroughThis()", 225);
	}

	@Test
	public void failingExpressionProperty() throws Exception {
		assertError(runScript("FailingExpressionProperty.fmlscript"), FMLNullReferenceError.class, "failingExpression", 170,
				"readFailingExpression()", 230);
	}

	@Test
	public void nullInWhere() throws Exception {
		assertError(runScript("NullInWhere.fmlscript"), FMLNullReferenceError.class, "nullInWhere()", 237);
	}

	@Test
	public void failingEnable() throws Exception {
		assertError(runScript("FailingEnable.fmlscript"), FMLNullReferenceError.class, "Pong from this (event)", 248, "firePong()", 244);
	}

	@Test
	public void bareNewOfAModelConcept() throws Exception {
		assertError(runScript("BareNewOfModelConcept.fmlscript"), FMLResolutionError.class, "orphanBeta(name)", 27);
	}

	@Test
	public void missingImport() throws Exception {
		assertError(runScript("MissingImport.fmlscript"), FMLResolutionError.class, "guestName()", 12);
	}

	@Test
	public void bareCall() throws Exception {
		assertError(runScript("BareCall.fmlscript"), FMLResolutionError.class, "bareCall()", 73);
	}

	@Test
	public void invalidBinding() throws Exception {
		assertError(runScript("InvalidBinding.fmlscript"), FMLResolutionError.class, "invalidCondition()", 69);
	}

	@Test
	public void uniqueSelectWithSeveralResults() throws Exception {
		// CORE-F-11 phase 6: the cardinality of a unique select
		assertError(runScript("UniqueOfSeveral.fmlscript"), FMLCardinalityError.class, "uniqueOfSeveral()", 262);
	}

	@Test
	public void selectFromNullModel() throws Exception {
		// CORE-F-11 phase 6: a null virtual model instance to select from
		assertError(runScript("SelectFromNull.fmlscript"), FMLNullReferenceError.class, "selectFromNull()", 273);
	}

	@Test
	public void fmlAdapterActionWithNullContainer() throws Exception {
		// CORE-F-11 phase 6: a technology action of the FML adapter
		assertError(runScript("FMLAdapterNullContainer.fmlscript"), FMLNullReferenceError.class, "createInNullContainer()", 12);
	}
}
