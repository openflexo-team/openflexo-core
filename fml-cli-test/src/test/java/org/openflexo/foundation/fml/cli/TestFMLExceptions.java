/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of FML-cli-test, a component of the software infrastructure 
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLExecutionFailure;
import org.openflexo.foundation.fml.rt.error.FMLInternalError;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.foundation.fml.rt.error.FMLStackFrame;
import org.openflexo.foundation.fml.rt.error.FMLUserError;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * FML exceptions, throw, try / catch / finally (CORE-F-11 phase 7): the scripts of {@code TestResourceCenter/Exceptions/}, on the model
 * {@code FML/Exceptions.fml} (and {@code FML/ExceptionsDefect.fml} for a defect of the platform). Each script asserts the values; this test
 * asserts the type and the FML stack of the failures that go on, and that nothing is printed to stderr (a caught error is not reported).
 */
public class TestFMLExceptions extends FMLScriptParserTestCase {

	private static final String MUST_FAIL = "mustFail";

	/** Lines of Exceptions.fml asserted here */
	private static final int THROW_IN_TAKE = 15;
	private static final int TAKE_IN_NO_MATCHING_CLAUSE = 62;
	private static final int TAKE_IN_RETHROW = 115;
	private static final int SUBSTRING_IN_RETHROW_BUILT_IN = 125;
	private static final int THROW_IN_UNCAUGHT = 149;
	private static final int SECOND_THROW_IN_THROW_TWICE = 168;

	private Throwable runScript(String name) throws Exception {
		return runScript(name, true);
	}

	/**
	 * Run supplied script; return the failure of its command assigning {@code mustFail}, or null when it has none (any other failure fails
	 * the test). When asked, stderr is captured and must stay empty
	 */
	private Throwable runScript(String name, boolean nothingOnStderr) throws Exception {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
		Resource fmlResource = ResourceLocator.locateResource("TestResourceCenter/Exceptions/" + name);
		FMLScript script = parseFMLScript(fmlResource, interpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		Throwable failure = null;
		PrintStream err = System.err;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		if (nothingOnStderr) {
			System.setErr(new PrintStream(captured, true));
		}
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

	/** The FML failure of supplied type that supplied failure is or wraps */
	private static FMLExecutionFailure assertFailure(Throwable failure, Class<?> type) {
		assertNotNull("No failure", failure);
		FMLExecutionFailure fmlFailure = FMLExecutionFailure.findFailure(failure);
		assertNotNull("Not an FML failure: " + failure, fmlFailure);
		assertEquals(String.valueOf(fmlFailure), type, fmlFailure.getClass());
		System.out.println("FML stack of " + fmlFailure + ":\n" + fmlFailure.getFMLStack());
		return fmlFailure;
	}

	/** Assert the innermost frames of supplied failure: behaviour names and lines, innermost first */
	private static void assertFrames(FMLExecutionFailure failure, Object... behavioursAndLines) {
		List<FMLStackFrame> frames = failure.getFMLStack().getFrames();
		assertTrue(frames.toString(), frames.size() >= behavioursAndLines.length / 2);
		for (int i = 0; i < behavioursAndLines.length / 2; i++) {
			FMLStackFrame frame = frames.get(i);
			assertEquals(frame.toString(), behavioursAndLines[2 * i], frame.getBehaviour());
			assertEquals(frame.toString(), behavioursAndLines[2 * i + 1], frame.getLine());
		}
	}

	@Test
	public void catchByExactType() throws Exception {
		assertNull(runScript("CatchExact.fmlscript"));
	}

	@Test
	public void catchBySuperException() throws Exception {
		assertNull(runScript("CatchBySuperException.fmlscript"));
	}

	@Test
	public void catchByFMLError() throws Exception {
		assertNull(runScript("CatchByFMLError.fmlscript"));
	}

	@Test
	public void firstMatchingClause() throws Exception {
		assertNull(runScript("FirstMatchingClause.fmlscript"));
	}

	@Test
	public void noMatchingClause() throws Exception {
		FMLExecutionFailure failure = assertFailure(runScript("NoMatchingClause.fmlscript", false), FMLUserError.class);
		assertFrames(failure, "take(n)", THROW_IN_TAKE, "noMatchingClause()", TAKE_IN_NO_MATCHING_CLAUSE);
		assertEquals("OutOfStock", FMLErrorReporter.kind((Throwable) failure));
		assertEquals("cannot take 1000, only 0", failure.getMessage());
	}

	@Test
	public void builtInErrorCaught() throws Exception {
		assertNull(runScript("NullInTry.fmlscript"));
	}

	@Test
	public void returnThroughTry() throws Exception {
		assertNull(runScript("ReturnThroughTry.fmlscript"));
	}

	@Test
	public void finallyOnError() throws Exception {
		assertFailure(runScript("FinallyOnError.fmlscript", false), FMLUserError.class);
	}

	@Test
	public void nestedTry() throws Exception {
		assertNull(runScript("Nested.fmlscript"));
	}

	@Test
	public void rethrowKeepsTheFirstStack() throws Exception {
		FMLExecutionFailure failure = assertFailure(runScript("Rethrow.fmlscript", false), FMLUserError.class);
		assertFrames(failure, "take(n)", THROW_IN_TAKE, "rethrow()", TAKE_IN_RETHROW);
	}

	@Test
	public void rethrowBuiltIn() throws Exception {
		FMLExecutionFailure failure = assertFailure(runScript("RethrowBuiltIn.fmlscript", false), FMLNullReferenceError.class);
		assertFrames(failure, "rethrowBuiltIn()", SUBSTRING_IN_RETHROW_BUILT_IN);
	}

	@Test
	public void messageWithoutRenderer() throws Exception {
		assertNull(runScript("MessageWithoutRenderer.fmlscript"));
	}

	@Test
	public void stackOfCaughtException() throws Exception {
		assertNull(runScript("StackOfCaught.fmlscript"));
	}

	@Test
	public void uncaughtException() throws Exception {
		FMLExecutionFailure failure = assertFailure(runScript("Uncaught.fmlscript", false), FMLUserError.class);
		assertFrames(failure, "uncaught()", THROW_IN_UNCAUGHT);
		assertEquals("Refused", FMLErrorReporter.kind((Throwable) failure));
		assertEquals("fml_user_error", FMLErrorReporter.kindKey((Throwable) failure));
		assertSame(failure, ((FMLUserError) failure).getExceptionInstance().getThrownError());
	}

	@Test
	public void caughtFailureOfANestedCallIsNotReported() throws Exception {
		// Nothing on stderr: the failure of the nested call take() is handled, not reported as "failed, and its caller went on"
		assertNull(runScript("CaughtThenGoOn.fmlscript"));
	}

	@Test
	public void exceptionThrownAgainOutsideItsCatchIsANewError() throws Exception {
		// Not the error handled by the catch block: a new one, located at the second throw, and reported (it is not marked reported)
		FMLExecutionFailure failure = assertFailure(runScript("ThrowTwice.fmlscript", false), FMLUserError.class);
		assertFrames(failure, "throwTwice()", SECOND_THROW_IN_THROW_TWICE);
	}

	@Test
	public void exceptionDeclaredInAConcept() throws Exception {
		assertNull(runScript("NestedInConcept.fmlscript"));
	}

	@Test
	public void listenerExceptionCaughtByTheFire() throws Exception {
		assertNull(runScript("FireCaught.fmlscript"));
	}

	@Test
	public void internalErrorIsNeverCaught() throws Exception {
		FMLExecutionFailure failure = assertFailure(runScript("InternalErrorNotCaught.fmlscript", false), FMLInternalError.class);
		assertTrue(failure.getMessage(), failure.getMessage().contains(PlatformDefect.MESSAGE));
	}
}
