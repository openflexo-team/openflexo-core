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



import org.openflexo.connie.exception.InvocationTargetTransformException;
import java.lang.reflect.InvocationTargetException;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.javareflect.AccessorInvocationException;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.BindingEvaluationException;
import org.openflexo.connie.exception.InvalidCastException;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;

/**
 * The one way FML execution code evaluates a binding (CORE-F-11, level L1'): {@link #evaluate(DataBinding, BindingEvaluationContext, FMLObject)},
 * {@link #evaluateCondition(DataBinding, BindingEvaluationContext, FMLObject)} and {@link #assign(DataBinding, Object, BindingEvaluationContext, FMLObject)}
 * convert what Connie raises into the checked {@link FMLError} it stands for, instead of the former blocks that printed the stack and
 * went on with null:
 * <ul>
 * <li>{@link TypeMismatchException} → {@link FMLTypeError}; {@link NullReferenceException} → {@link FMLNullReferenceError};</li>
 * <li>{@link BindingEvaluationException}, thrown by Connie in a strict context, by its kind (see {@link #convert(Throwable, Object)});</li>
 * <li>an {@link InvocationTargetException} whose target is an {@link FMLExecutionException} or an {@link FMLInternalError} → that very
 * object, rethrown unchanged (a behaviour called from the expression failed); another target → {@link FMLHostError} (Java code called
 * from FML failed);</li>
 * <li>a null or non-boolean condition → {@link FMLNullReferenceError} / {@link FMLTypeError}; an invalid binding →
 * {@link FMLResolutionError}.</li>
 * </ul>
 * An unforeseen unchecked exception becomes an {@link FMLInternalError}. The failure is located at supplied owner (the FML object the
 * binding belongs to) unless a more precise location was already recorded. Nothing here logs.
 */
public final class FMLEvaluation {

	private FMLEvaluation() {
	}

	/**
	 * Evaluate supplied binding in supplied context
	 * 
	 * @return the value, null for a binding that is null or not set
	 * @throws FMLExecutionException
	 *             a typed {@link FMLError} when the evaluation fails
	 */
	public static <T> T evaluate(DataBinding<T> binding, BindingEvaluationContext context, FMLObject owner) throws FMLExecutionException {
		if (binding == null || !binding.isSet()) {
			return null;
		}
		checkValid(binding, owner);
		try {
			return binding.getBindingValue(context);
		} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException | RuntimeException e) {
			throw fail(e, binding, owner);
		}
	}

	/**
	 * Evaluate supplied condition in supplied context. A condition that is not set, invalid, null (a null in its path included) or not a
	 * boolean is an error: it is never taken as true nor as false (CORE-F-11, decided with the user)
	 */
	public static boolean evaluateCondition(DataBinding<?> condition, BindingEvaluationContext context, FMLObject owner)
			throws FMLExecutionException {
		if (condition == null || !condition.isSet()) {
			throw located(new FMLResolutionError("Condition is not set"), owner);
		}
		checkValid(condition, owner);
		Object value;
		try {
			value = condition.getBindingValue(context);
		} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException | RuntimeException e) {
			throw fail(e, condition, owner);
		}
		if (value == null) {
			throw located(new FMLNullReferenceError("Condition '" + text(condition) + "' is null"), owner);
		}
		if (!(value instanceof Boolean)) {
			throw located(new FMLTypeError("Condition '" + text(condition) + "' is not a boolean: " + value), owner);
		}
		return (Boolean) value;
	}

	/**
	 * Assign supplied value to supplied binding, in supplied context
	 */
	public static void assign(DataBinding<?> binding, Object value, BindingEvaluationContext context, FMLObject owner)
			throws FMLExecutionException {
		if (binding == null || !binding.isSet()) {
			throw located(new FMLAssignmentError("No target to assign"), owner);
		}
		checkValid(binding, owner);
		if (!binding.isSettable()) {
			throw located(new FMLAssignmentError("'" + text(binding) + "' cannot be assigned"), owner);
		}
		try {
			binding.setBindingValue(value, context);
		} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException | NotSettableContextException
				| RuntimeException e) {
			throw fail(e, binding, owner);
		}
	}

	/**
	 * Execute supplied binding (an expression evaluated for its effect), in supplied context
	 */
	public static void execute(DataBinding<?> binding, BindingEvaluationContext context, FMLObject owner) throws FMLExecutionException {
		if (binding == null || !binding.isSet()) {
			return;
		}
		checkValid(binding, owner);
		try {
			binding.execute(context);
		} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException | RuntimeException e) {
			throw fail(e, binding, owner);
		}
	}

	private static void checkValid(DataBinding<?> binding, FMLObject owner) throws FMLResolutionError {
		if (!binding.isValid()) {
			throw located(new FMLResolutionError("Invalid binding '" + text(binding) + "': " + binding.invalidBindingReason()), owner);
		}
	}

	/**
	 * Convert supplied failure of the evaluation of supplied binding (or expression, or text) into the checked FML failure it stands for,
	 * for the caller to throw: {@code throw FMLEvaluation.convert(e, binding);}. A failure that already is an FML failure is returned (or
	 * thrown, when unchecked) unchanged. An unforeseen unchecked exception is thrown as an {@link FMLInternalError}.
	 */
	public static FMLExecutionException convert(Throwable failure, Object binding) {
		String where = binding != null ? " in '" + text(binding) + "'" : "";
		// Unwrap the wrappers of Connie and the tunnels, noting whether Java code was invoked on the way
		Throwable t = failure;
		boolean invoked = false;
		for (int i = 0; i < 20; i++) {
			if (t instanceof InvocationTargetException && ((InvocationTargetException) t).getTargetException() != null) {
				t = ((InvocationTargetException) t).getTargetException();
				invoked = true;
			}
			else if (t instanceof InvocationTargetTransformException && ((InvocationTargetTransformException) t).getException() != null) {
				t = ((InvocationTargetTransformException) t).getException();
			}
			else if (t instanceof AccessorInvocationException && ((AccessorInvocationException) t).getTargetException() != null) {
				t = ((AccessorInvocationException) t).getTargetException();
				invoked = true;
			}
			else if (t instanceof FMLFailureTunnel) {
				t = ((FMLFailureTunnel) t).getFailure();
			}
			else {
				break;
			}
		}
		if (t instanceof FMLExecutionException) {
			return (FMLExecutionException) t;
		}
		if (t instanceof FMLInternalError) {
			throw (FMLInternalError) t;
		}
		if (t instanceof Error) {
			// A JVM error, even raised by invoked Java code, is not an FML error
			throw (Error) t;
		}
		// BindingPath wraps a missing Java property into an invocation: not a failure of the invoked code
		if (t instanceof InvalidKeyValuePropertyException) {
			return new FMLResolutionError("Unknown Java property" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof BindingEvaluationException) {
			return convertBindingEvaluationException((BindingEvaluationException) t);
		}
		if (invoked) {
			// The target of an invocation: a failure of Java code called from FML, kept as the cause
			return new FMLHostError(t.getClass().getSimpleName() + where + (t.getMessage() != null ? ": " + t.getMessage() : ""), t);
		}
		if (t instanceof NullReferenceException) {
			return new FMLNullReferenceError("Null reference" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof TypeMismatchException || t instanceof InvalidCastException) {
			return new FMLTypeError("Type mismatch" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof NotSettableContextException) {
			return new FMLAssignmentError("Not settable" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof IllegalAccessException) {
			return new FMLHostError("Illegal access" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof ReflectiveOperationException) {
			return new FMLResolutionError("Cannot resolve" + where + ": " + t.getMessage(), t);
		}
		if (t instanceof RuntimeException) {
			// Unforeseen: a defect of the platform
			throw new FMLInternalError(t);
		}
		// Another checked exception, raised by the code that evaluates
		return new FMLHostError("Evaluation failed" + where + ": " + t, t);
	}

	/**
	 * Convert supplied failure, located at supplied owner, whether it is converted into a checked failure (returned) or into an unchecked
	 * one (thrown)
	 */
	private static FMLExecutionException fail(Throwable e, Object binding, FMLObject owner) {
		try {
			return located(convert(e, binding), owner);
		} catch (FMLInternalError internal) {
			throw located(internal, owner);
		}
	}

	/**
	 * Convert supplied exception of Connie, thrown in a strict context, by its kind (decided with the user, CORE-F-11 phase 3)
	 */
	static FMLExecutionException convertBindingEvaluationException(BindingEvaluationException e) {
		switch (e.getKind()) {
			case INVALID_BINDING:
			case INCOMPLETE_EVALUATION:
				return new FMLResolutionError(e.getMessage(), e);
			case NOT_SETTABLE:
				return new FMLAssignmentError(e.getMessage(), e);
			case ILLEGAL_ARGUMENT:
				return new FMLTypeError(e.getMessage(), e);
			case ILLEGAL_ACCESS:
				return new FMLHostError(e.getMessage(), e);
			case TRANSFORMATION_FAILURE:
				if (e.getCause() instanceof InvalidCastException) {
					return new FMLTypeError(e.getMessage(), e);
				}
				if (e.getCause() instanceof NotSettableContextException) {
					return new FMLAssignmentError(e.getMessage(), e);
				}
				return new FMLResolutionError(e.getMessage(), e);
			case CONCURRENT_MODIFICATION:
			default:
				// A collection changed while it was read: a defect of the platform, unchecked
				throw new FMLInternalError(e.getMessage(), e);
		}
	}

	private static <E extends Throwable> E located(E failure, FMLObject owner) {
		if (owner instanceof FMLPrettyPrintable && failure instanceof FMLExecutionFailure) {
			((FMLExecutionFailure) failure).getFMLStack().recordLocation((FMLPrettyPrintable) owner);
		}
		return failure;
	}

	private static String text(Object binding) {
		try {
			return String.valueOf(binding);
		} catch (RuntimeException e) {
			return "<" + binding.getClass().getSimpleName() + ">";
		}
	}
}
