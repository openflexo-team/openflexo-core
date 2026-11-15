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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.binding.javareflect.AccessorInvocationException;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.BindingEvaluationException;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;

/**
 * Reports the failures of the actions run by an editor, once, where the outermost action ends (CORE-F-11, level L4).
 * <p>
 * A failure is written to the {@link FMLConsole} of the editor as a {@link FMLConsole.LogLevel#SEVERE} record, with its FML stack. A
 * foreseen failure (see {@link #isForeseen(Throwable)}) is not given its Java stack, except in the log at {@link Level#FINE}; an unforeseen
 * one (an {@link FMLInternalError}, any other unchecked exception, even wrapped into a checked one) is a defect of the platform, logged with
 * its Java stack. A cancellation ({@link FMLCancellation}) is not reported.
 * <p>
 * An interactive editor adds its own presentation (a dialog) on top of this one, by overriding
 * {@link #present(FlexoAction, Throwable)}.
 * <p>
 * A failure is reported once: the reporter remembers the failures it reported ({@link #isReported(Throwable)}), so that the code that
 * receives the same failure later (a GINA binding, the uncaught exception handler of the application) does not report it again.
 */
public class FMLErrorReporter {

	private static final Logger logger = FlexoLogger.getLogger(FMLErrorReporter.class.getPackage().getName());

	/** The scope of the outermost action running on a thread: the nested actions that failed in it */
	public static final class ExecutionScope {
		private final List<FlexoAction<?, ?, ?>> failedNestedActions = new ArrayList<>();

		/** Record that a nested action failed: its failure is left to its caller, and reported at the end if the caller went on */
		public void nestedActionFailed(FlexoAction<?, ?, ?> action) {
			failedNestedActions.add(action);
		}

		public List<FlexoAction<?, ?, ?>> getFailedNestedActions() {
			return failedNestedActions;
		}
	}

	private static final ThreadLocal<ExecutionScope> CURRENT_SCOPE = new ThreadLocal<>();

	/** The failures already reported, by identity (a throwable does not redefine equals), weakly held */
	private static final Map<Throwable, Boolean> REPORTED = Collections.synchronizedMap(new WeakHashMap<>());

	/** Remember that supplied failure was reported: whoever receives it later does not report it again */
	public static void markReported(Throwable failure) {
		if (failure != null) {
			REPORTED.put(failure, Boolean.TRUE);
		}
	}

	/** Tell if supplied failure, or one of its causes, was already reported */
	public static boolean isReported(Throwable failure) {
		for (Throwable c = failure; c != null; c = next(c)) {
			if (REPORTED.containsKey(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Enter the execution of an action on the current thread
	 * 
	 * @return a new scope when this action is the outermost one (the caller must then {@link #exitOutermostAction()}), null when it runs
	 *         inside another action
	 */
	public static ExecutionScope enterAction() {
		if (CURRENT_SCOPE.get() != null) {
			return null;
		}
		ExecutionScope scope = new ExecutionScope();
		CURRENT_SCOPE.set(scope);
		return scope;
	}

	/** The scope of the outermost action running on the current thread, or null */
	public static ExecutionScope currentScope() {
		return CURRENT_SCOPE.get();
	}

	/** Leave the execution of the outermost action on the current thread */
	public static void exitOutermostAction() {
		CURRENT_SCOPE.remove();
	}

	/**
	 * The nested actions of supplied scope whose failure nothing else reports: all of them when supplied outermost action succeeded, those
	 * whose failure is not part of the failure of the outermost action otherwise
	 */
	public static List<FlexoAction<?, ?, ?>> unreportedNestedFailures(ExecutionScope scope, FlexoAction<?, ?, ?> outermost) {
		List<FlexoAction<?, ?, ?>> returned = new ArrayList<>();
		Throwable outerFailure = outermost.getFailure();
		for (FlexoAction<?, ?, ?> nested : scope.getFailedNestedActions()) {
			if (nested.getFailure() != null && (outerFailure == null || !isPartOf(nested.getFailure(), outerFailure))) {
				returned.add(nested);
			}
		}
		return returned;
	}

	/** Tell if supplied failure is supplied other one, one of its causes, or one of the failures suppressed by them */
	private static boolean isPartOf(Throwable failure, Throwable other) {
		for (Throwable c = other; c != null; c = next(c)) {
			if (c == failure) {
				return true;
			}
			for (Throwable s : c.getSuppressed()) {
				if (isPartOf(failure, s)) {
					return true;
				}
			}
		}
		return false;
	}

	/** The next throwable of a cause chain, including the exception held by an {@link InvocationTargetTransformException} */
	private static Throwable next(Throwable t) {
		if (t instanceof InvocationTargetTransformException && ((InvocationTargetTransformException) t).getException() != null) {
			return ((InvocationTargetTransformException) t).getException();
		}
		Throwable cause = t.getCause();
		return cause == t ? null : cause;
	}

	private final FlexoEditor editor;

	public FMLErrorReporter(FlexoEditor editor) {
		this.editor = editor;
	}

	public FlexoEditor getEditor() {
		return editor;
	}

	/**
	 * Report the failure of supplied outermost action
	 */
	public void report(FlexoAction<?, ?, ?> action, Throwable failure) {
		if (failure == null || isCancellation(failure) || isReported(failure)) {
			return;
		}
		String text = describe("Action " + actionName(action) + " failed", failure);
		publish(action, text, failure);
		present(action, failure);
	}

	/**
	 * Present the failure of supplied outermost action to the user, once it is written to the console. Does nothing here: an
	 * interactive editor overrides it (a dialog)
	 */
	protected void present(FlexoAction<?, ?, ?> action, Throwable failure) {
	}

	/**
	 * Report a failure that no action of this editor reported: raised by a binding of the user interface (GINA), a listener, a
	 * synchronization. Written to the console only, never presented in a dialog; nothing is done when the failure was already reported
	 * (by the action that failed) or is a cancellation
	 * 
	 * @param title
	 *            what was being done (English, as the console records are)
	 */
	public void reportToConsole(String title, Throwable failure) {
		if (failure == null || isCancellation(failure) || isReported(failure)) {
			return;
		}
		publish(null, describe(title, failure), failure);
	}

	/**
	 * Report the failures of nested actions that their callers went on after: the outermost action succeeded, so nothing else tells
	 * about them
	 */
	public void reportIgnoredFailures(List<FlexoAction<?, ?, ?>> failedActions) {
		for (FlexoAction<?, ?, ?> action : failedActions) {
			Throwable failure = action.getFailure();
			if (failure == null || isCancellation(failure)) {
				continue;
			}
			String text = describe("Nested action " + actionName(action) + " failed, and its caller went on", failure);
			publish(action, text, failure);
			markReported(failure);
		}
	}

	/**
	 * Write the text to the console of the editor (which also prints it), and to the log when there is no console; an unforeseen failure
	 * is always logged with its Java stack
	 */
	private void publish(FlexoAction<?, ?, ?> action, String text, Throwable failure) {
		markReported(failure);
		FMLConsole console = editor != null ? editor.getFMLConsole() : null;
		if (console == null) {
			log(text, failure);
			return;
		}
		writeToConsole(console, action, text, failure);
		if (!isForeseen(failure)) {
			logger.log(Level.SEVERE, text, failure);
		}
		else if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "Java stack of the failure above", failure);
		}
	}

	private static void writeToConsole(FMLConsole console, FlexoAction<?, ?, ?> action, String text, Throwable failure) {
		FlexoConceptInstance fci = null;
		FlexoBehaviour behaviour = null;
		if (action instanceof FlexoBehaviourAction) {
			fci = ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoConceptInstance();
			behaviour = ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoBehaviour();
		}
		console.logFailure(text, fci, behaviour, failure);
	}

	/**
	 * Log the failures of nested actions that their callers went on after, when there is no editor to report to
	 */
	public static void logIgnoredFailures(List<FlexoAction<?, ?, ?>> failedActions) {
		for (FlexoAction<?, ?, ?> action : failedActions) {
			Throwable failure = action.getFailure();
			if (failure != null && !isCancellation(failure)) {
				log(describe("Nested action " + actionName(action) + " failed, and its caller went on", failure), failure);
			}
		}
	}

	/**
	 * Log the failure of supplied action, run without an editor to report to
	 */
	public static void logFailure(FlexoAction<?, ?, ?> action, Throwable failure) {
		if (failure == null || isCancellation(failure)) {
			return;
		}
		markReported(failure);
		log(describe("Action " + actionName(action) + " failed", failure), failure);
	}

	private static void log(String text, Throwable failure) {
		if (isForeseen(failure)) {
			logger.warning(text);
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Java stack of the failure above", failure);
			}
		}
		else {
			logger.log(Level.SEVERE, text, failure);
		}
	}

	/**
	 * Tell if supplied failure is a cancellation, possibly wrapped by the callers of the cancelled action: not an error, not reported
	 */
	public static boolean isCancellation(Throwable failure) {
		for (Throwable c = failure; c != null; c = next(c)) {
			if (c instanceof FMLCancellation) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tell if supplied failure is foreseen by the semantics of FML (the FML or its data are at fault), as opposed to a defect of the
	 * platform
	 */
	public static boolean isForeseen(Throwable failure) {
		// A defect of the platform may have been wrapped into a checked exception: the whole cause chain is examined. A failure of Java
		// code called from FML (under an InvocationTargetException) is a failure of that code, foreseen; any other unchecked exception, or
		// an FMLInternalError, is a defect
		for (Throwable c = failure; c != null; c = next(c)) {
			if (c instanceof FMLInternalError || c instanceof Error) {
				return false;
			}
			if (c instanceof InvocationTargetException || c instanceof AccessorInvocationException || c instanceof FMLHostError) {
				return true;
			}
			// A tunnel only carries the failure it holds; an exception of Connie in a strict evaluation, or a missing Java property, is a
			// fault of the FML
			if (c instanceof RuntimeException && !(c instanceof InvalidKeyValuePropertyException) && !(c instanceof BindingEvaluationException)
					&& !(c instanceof FMLFailureTunnel)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A text describing supplied failure: the title, the message of the failure, and its FML stack when it has one
	 */
	public static String describe(String title, Throwable failure) {
		StringBuilder sb = new StringBuilder(title);
		// The kind in English, as the rest of the record: the console, the logs, fml-cli and the server give a stable text
		sb.append(": ").append(defaultKindText(kindKey(failure))).append(": ")
				.append(failure.getMessage() != null ? failure.getMessage() : failure.getClass().getSimpleName());
		FMLStack stack = FMLExecutionFailure.findFMLStack(failure);
		if (stack != null && !stack.isEmpty()) {
			sb.append('\n').append(stack);
		}
		// Failures that went with this one (for instance the other failing listeners of an event), on any link of the cause chain
		for (Throwable c = failure; c != null; c = next(c)) {
			for (Throwable suppressed : c.getSuppressed()) {
				sb.append('\n').append(describe("Also", suppressed));
			}
		}
		return sb.toString();
	}

	/**
	 * The localization key of the kind of supplied failure (one per type of {@link FMLError}, and one for an untyped failure, a
	 * {@link NotImplementedException} and a defect of the platform), in the dictionary of the core
	 */
	public static String kindKey(Throwable failure) {
		if (!isForeseen(failure)) {
			return "fml_internal_error";
		}
		for (Throwable c = failure; c != null; c = next(c)) {
			if (c instanceof NotImplementedException) {
				return "fml_not_implemented";
			}
		}
		FMLExecutionFailure typed = FMLExecutionFailure.findFailure(failure);
		if (typed instanceof FMLNullReferenceError) {
			return "fml_null_reference_error";
		}
		if (typed instanceof FMLTypeError) {
			return "fml_type_error";
		}
		if (typed instanceof FMLResolutionError) {
			return "fml_resolution_error";
		}
		if (typed instanceof FMLAssignmentError) {
			return "fml_assignment_error";
		}
		if (typed instanceof FMLArgumentError) {
			return "fml_argument_error";
		}
		if (typed instanceof FMLCardinalityError) {
			return "fml_cardinality_error";
		}
		if (typed instanceof FMLTechnologyError) {
			return "fml_technology_error";
		}
		if (typed instanceof FMLHostError) {
			return "fml_host_error";
		}
		return "fml_execution_error";
	}

	/** The English title of the kind of supplied failure, as written in the console and the logs */
	public static String kind(Throwable failure) {
		return defaultKindText(kindKey(failure));
	}

	/** English texts of the kinds, written in the records, and used when no dictionary is available */
	private static String defaultKindText(String key) {
		switch (key) {
			case "fml_internal_error":
				return "Internal error";
			case "fml_not_implemented":
				return "Not implemented";
			case "fml_null_reference_error":
				return "Null reference";
			case "fml_type_error":
				return "Type error";
			case "fml_resolution_error":
				return "Resolution error";
			case "fml_assignment_error":
				return "Assignment error";
			case "fml_argument_error":
				return "Argument error";
			case "fml_cardinality_error":
				return "Cardinality error";
			case "fml_technology_error":
				return "Technology error";
			case "fml_host_error":
				return "Failure of Java code";
			default:
				return "FML execution error";
		}
	}

	/** The localized title of the kind of supplied failure (for instance "Null reference"), in the current language */
	public static String localizedKind(Throwable failure) {
		String key = kindKey(failure);
		try {
			LocalizedDelegate locales = FlexoLocalization.getMainLocalizer();
			String localized = locales != null ? locales.localizedForKey(key) : null;
			if (localized != null && !localized.equals(key)) {
				return localized;
			}
		} catch (RuntimeException e) {
			// The localization must never hide the failure being reported
		}
		return defaultKindText(key);
	}

	private static String actionName(FlexoAction<?, ?, ?> action) {
		if (action instanceof FlexoBehaviourAction && ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoBehaviour() != null) {
			return ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoBehaviour().getSignature();
		}
		return action.getClass().getSimpleName();
	}
}
