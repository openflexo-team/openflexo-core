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


package org.openflexo.foundation.fml.rt.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.openflexo.connie.binding.javareflect.AccessorInvocationException;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.BindingEvaluationException;
import org.openflexo.connie.exception.BindingEvaluationException.Kind;
import org.openflexo.connie.exception.InvalidCastException;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.EvaluationType;
import org.openflexo.foundation.fml.expr.FMLBooleanBinaryOperator;

/**
 * The conversions of {@link FMLEvaluation#convert(Throwable, Object)} and {@link FMLExecution#convertUnchecked(RuntimeException)}: what
 * Connie raises becomes the checked {@link FMLError} it stands for, an FML failure crossing an invocation is rethrown unchanged, and only a
 * defect of the platform stays unchecked (CORE-F-11 phase 3). The FML side, one script per kind, is {@code TestFMLErrorKinds}
 * (fml-cli-test).
 */
public class TestFMLEvaluation {

	private static void assertConverted(Class<? extends FMLExecutionException> expected, Throwable failure) {
		FMLExecutionException converted = FMLEvaluation.convert(failure, "a.b");
		assertEquals(String.valueOf(converted), expected, converted.getClass());
	}

	private static void assertInternal(Throwable failure) {
		try {
			FMLEvaluation.convert(failure, "a.b");
			fail("Not converted into an FMLInternalError: " + failure);
		} catch (FMLInternalError e) {
			// expected: a defect of the platform stays unchecked
		}
	}

	@Test
	public void checkedExceptionsOfConnie() {
		assertConverted(FMLNullReferenceError.class, new NullReferenceException("null occured"));
		assertConverted(FMLTypeError.class, new TypeMismatchException(FMLBooleanBinaryOperator.AND, EvaluationType.STRING, EvaluationType.BOOLEAN, EvaluationType.BOOLEAN));
		assertConverted(FMLTypeError.class, new InvalidCastException(String.class, Integer.class));
		assertConverted(FMLAssignmentError.class, new NotSettableContextException("not settable"));
		assertConverted(FMLResolutionError.class, new NoSuchMethodException("foo"));
		assertConverted(FMLHostError.class, new IllegalAccessException("private"));
	}

	@Test
	public void kindsOfBindingEvaluationException() {
		assertConverted(FMLResolutionError.class, new BindingEvaluationException(Kind.INVALID_BINDING, "a.b", "unknown a"));
		assertConverted(FMLResolutionError.class, new BindingEvaluationException(Kind.INCOMPLETE_EVALUATION, "a.b", null));
		assertConverted(FMLAssignmentError.class, new BindingEvaluationException(Kind.NOT_SETTABLE, "a.b", null));
		assertConverted(FMLTypeError.class, new BindingEvaluationException(Kind.ILLEGAL_ARGUMENT, "a.b", null));
		assertConverted(FMLHostError.class, new BindingEvaluationException(Kind.ILLEGAL_ACCESS, "a.b", null));
		assertConverted(FMLTypeError.class, new BindingEvaluationException(Kind.TRANSFORMATION_FAILURE, "a.b", null,
				new InvalidCastException(String.class, Integer.class)));
		assertConverted(FMLAssignmentError.class, new BindingEvaluationException(Kind.TRANSFORMATION_FAILURE, "a.b", null,
				new NotSettableContextException("not settable")));
		assertConverted(FMLResolutionError.class, new BindingEvaluationException(Kind.TRANSFORMATION_FAILURE, "a.b", null, null));
		assertInternal(new BindingEvaluationException(Kind.CONCURRENT_MODIFICATION, "a.b", null, null));
	}

	@Test
	public void invocationTargets() {
		// A failure of Java code called from FML, kept as the cause
		IllegalStateException javaFailure = new IllegalStateException("broken");
		FMLExecutionException host = FMLEvaluation.convert(new InvocationTargetException(javaFailure), "a.b");
		assertEquals(FMLHostError.class, host.getClass());
		assertSame(javaFailure, host.getCause());
		assertConverted(FMLHostError.class, new AccessorInvocationException(new InvocationTargetException(javaFailure)));

		// The failure of a behaviour called from the expression: the very object, unwrapped from the normalized tunnel of Connie
		FMLNullReferenceError inner = new FMLNullReferenceError("inner");
		assertSame(inner, FMLEvaluation.convert(new InvocationTargetException(inner), "a.b"));
		assertSame(inner, FMLEvaluation.convert(new InvocationTargetTransformException(new InvocationTargetException(inner)), "a.b"));
		assertSame(inner, FMLEvaluation.convert(new FMLFailureTunnel(inner), "a.b"));

		// A missing Java property is wrapped into an invocation by Connie: it is not a failure of the invoked code
		assertConverted(FMLResolutionError.class, new InvocationTargetException(new InvalidKeyValuePropertyException("no foo")));

		// An unchecked FML failure crosses unchanged
		FMLInternalError internal = new FMLInternalError(new NullPointerException());
		try {
			FMLEvaluation.convert(new InvocationTargetException(internal), "a.b");
			fail("FMLInternalError not rethrown");
		} catch (FMLInternalError e) {
			assertSame(internal, e);
		}
	}

	@Test
	public void uncheckedExceptions() throws FMLExecutionException {
		// A defect of the platform stays unchecked
		assertInternal(new NullPointerException());
		assertEquals(FMLInternalError.class, FMLExecution.convertUnchecked(new ClassCastException()).getClass());

		// The foreseen ones become checked at the first statement they cross
		assertCheckedFromStatement(FMLHostError.class,
				new AccessorInvocationException(new InvocationTargetException(new IllegalStateException("broken"))));
		assertCheckedFromStatement(FMLResolutionError.class, new InvalidKeyValuePropertyException("no foo"));
		assertCheckedFromStatement(FMLResolutionError.class, new BindingEvaluationException(Kind.INVALID_BINDING, "a.b", null));
		FMLTypeError tunnelled = new FMLTypeError("tunnelled");
		try {
			FMLExecution.convertUnchecked(new FMLFailureTunnel(tunnelled));
			fail("The failure of a tunnel is not rethrown");
		} catch (FMLExecutionException e) {
			assertSame(tunnelled, e);
		}
	}

	private static void assertCheckedFromStatement(Class<? extends FMLExecutionException> expected, RuntimeException failure) {
		try {
			FMLExecution.convertUnchecked(failure);
			fail("Not converted into a checked failure: " + failure);
		} catch (FMLExecutionException e) {
			assertEquals(String.valueOf(e), expected, e.getClass());
			// Foreseen: reported as an FML error, not as a defect
			assertEquals(true, FMLErrorReporter.isForeseen(e));
		}
	}
}
