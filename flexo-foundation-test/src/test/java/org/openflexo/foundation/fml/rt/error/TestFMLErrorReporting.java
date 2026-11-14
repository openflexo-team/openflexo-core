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


package org.openflexo.foundation.fml.rt.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoAction.ExecutionStatus;

/**
 * How the editor reports the failures of the actions it runs, and what an action keeps of its failure (CORE-F-11): the branches of
 * {@link DefaultFlexoEditor#performAction}, the outermost-action scope, and the classification of {@link FMLErrorReporter#isForeseen}.
 */
public class TestFMLErrorReporting {

	/** An action whose body is given by the test */
	private static class TestAction extends FlexoAction<TestAction, FlexoObject, FlexoObject> {

		interface Body {
			void run() throws FlexoException;
		}

		private final Body body;

		TestAction(FlexoEditor editor, Body body) {
			super(null, null, null, editor);
			this.body = body;
		}

		@Override
		protected void doAction(Object context) throws FlexoException {
			body.run();
		}
	}

	private DefaultFlexoEditor editor;

	@Before
	public void setUp() {
		editor = new DefaultFlexoEditor(null, null);
	}

	private int severe() {
		return editor.getFMLConsole().getSevereLogs();
	}

	@Test
	public void outermostUnforeseenFailureIsReportedAndRethrown() {
		NullPointerException npe = new NullPointerException("platform defect");
		TestAction action = new TestAction(editor, () -> {
			throw npe;
		});
		try {
			action.doAction();
			fail("An unforeseen failure must not be swallowed");
		} catch (NullPointerException e) {
			assertSame(npe, e);
		}
		assertEquals(ExecutionStatus.FAILED_EXECUTION, action.getExecutionStatus());
		assertSame(npe, action.getFailure());
		assertEquals(1, severe());
		assertNull("The scope must be left", FMLErrorReporter.currentScope());
	}

	@Test
	public void outermostCheckedFailureIsReportedAndKeptInStatus() {
		FlexoException failure = new FMLExecutionException("checked");
		TestAction action = new TestAction(editor, () -> {
			throw failure;
		});
		action.doAction();
		assertTrue(action.hasActionExecutionFailed());
		assertSame(failure, action.getThrownException());
		assertSame(failure, action.getFailure());
		assertEquals(1, severe());
	}

	@Test
	public void outermostTypedFailureIsReportedAndKeptInStatus() {
		FMLTypeError failure = new FMLTypeError("foreseen");
		TestAction action = new TestAction(editor, () -> {
			throw failure;
		});
		action.doAction();
		assertTrue(action.hasActionExecutionFailed());
		assertSame(failure, action.getFailure());
		assertSame(failure, action.getThrownException());
		assertEquals(1, severe());
		// A checked failure is not rethrown by rethrowUncheckedFailure
		action.rethrowUncheckedFailure();
	}

	@Test
	public void cancellationIsNotReported() {
		TestAction action = new TestAction(editor, () -> {
			throw new FMLCancellation("cancelled");
		});
		action.doAction();
		assertEquals(ExecutionStatus.HAS_BEEN_CANCELLED, action.getExecutionStatus());
		assertFalse(action.hasActionExecutionFailed());
		assertEquals(0, severe());
	}

	@Test
	public void nestedFailureIgnoredByItsCallerIsReportedOnce() {
		TestAction outer = new TestAction(editor, () -> {
			TestAction inner = new TestAction(editor, () -> {
				throw new FMLExecutionException("nested");
			});
			inner.doAction();
			assertTrue(inner.hasActionExecutionFailed());
			// The caller goes on
		});
		outer.doAction();
		assertTrue(outer.hasActionExecutionSucceeded());
		assertEquals(1, severe());
		assertTrue(editor.getFMLConsole().getRecords().get(0).message.contains("its caller went on"));
	}

	@Test
	public void nestedFailureRethrownByItsCallerIsReportedOnce() {
		TestAction outer = new TestAction(editor, () -> {
			TestAction inner = new TestAction(editor, () -> {
				throw new FMLExecutionException("nested");
			});
			inner.doAction();
			throw inner.getThrownException();
		});
		outer.doAction();
		assertTrue(outer.hasActionExecutionFailed());
		assertEquals(1, severe());
	}

	@Test
	public void nestedInternalErrorPropagatesToItsCaller() {
		FMLInternalError failure = new FMLInternalError(new IllegalStateException("platform defect"));
		TestAction outer = new TestAction(editor, () -> {
			new TestAction(editor, () -> {
				throw failure;
			}).doAction();
			fail("A nested unchecked failure must propagate");
		});
		try {
			outer.doAction();
			fail("An unforeseen failure must not be swallowed");
		} catch (FMLInternalError e) {
			assertSame(failure, e);
		}
		assertSame(failure, outer.getFailure());
		assertEquals(1, severe());
	}

	@Test
	public void nestedTypedFailureStaysInStatusAndIsRethrownByItsCaller() {
		FMLNullReferenceError failure = new FMLNullReferenceError("nested");
		TestAction outer = new TestAction(editor, () -> {
			TestAction inner = new TestAction(editor, () -> {
				throw failure;
			});
			inner.doAction();
			assertSame(failure, inner.getThrownException());
			throw inner.getThrownException();
		});
		outer.doAction();
		assertSame(failure, outer.getThrownException());
		assertEquals(1, severe());
	}

	@Test
	public void withoutEditorAnOutermostFailureIsKeptInStatus() {
		TestAction action = new TestAction(null, () -> {
			throw new FMLTypeError("foreseen");
		});
		action.doAction();
		assertTrue(action.hasActionExecutionFailed());
		assertNull(FMLErrorReporter.currentScope());
	}

	@Test
	public void classification() {
		assertTrue(FMLErrorReporter.isForeseen(new FMLTypeError("x")));
		assertTrue(FMLErrorReporter.isForeseen(new FMLExecutionException(new NullReferenceException("x"))));
		// A failure of Java code called from FML is a failure of that code
		assertTrue(FMLErrorReporter
				.isForeseen(new FMLExecutionException(new InvocationTargetException(new StringIndexOutOfBoundsException()))));
		// A defect of the platform stays one, even wrapped into a checked exception
		assertFalse(FMLErrorReporter.isForeseen(new NullPointerException()));
		assertFalse(FMLErrorReporter.isForeseen(new FMLExecutionException(new NullPointerException())));
		assertFalse(FMLErrorReporter.isForeseen(new FMLExecutionException(new FMLInternalError(new IllegalStateException()))));
	}

	@Test
	public void aWrappingFailureStartsWithTheFramesOfItsCause() {
		FMLTypeError cause = new FMLTypeError("inner");
		cause.getFMLStack().pushFrame("inner()", "C", null);
		FMLExecutionException wrapper = new FMLExecutionException(cause);
		assertEquals(1, wrapper.getFMLStack().getFrames().size());
		assertEquals("inner()", wrapper.getFMLStack().getFrames().get(0).getBehaviour());
		assertSame(cause.getFMLStack(), FMLExecutionFailure.findFMLStack(new RuntimeException(cause)));
	}
}
