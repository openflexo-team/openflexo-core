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

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoException;

/**
 * The error types as FML sees them (CORE-F-11 phase 7).
 * <ul>
 * <li>The built-in error types are named in FML by their simple name, without import, like {@code String}: {@link FMLError} and its
 * subclasses, and also the types that cannot be caught ({@link FMLInternalError}, {@link FMLCancellation}, the untyped
 * {@link FMLExecutionException}), so that naming one in a {@code catch} is reported as such rather than as an unknown type.</li>
 * <li>A {@code catch} clause names either one of the {@link FMLError} types (a Java class) or an FML exception ({@link FlexoException}). A built-in
 * type catches an error of that class, an FML exception included ({@code catch (FMLError e)} catches every foreseen error); an FML
 * exception catches the {@link FMLUserError}s carrying an instance of it or of one of its sub exceptions.</li>
 * </ul>
 */
public class FMLErrorTypes {

	private static final Map<String, Class<?>> IMPLICIT_TYPES;

	static {
		Map<String, Class<?>> map = new LinkedHashMap<>();
		for (Class<?> c : new Class<?>[] { FMLError.class, FMLNullReferenceError.class, FMLTypeError.class, FMLResolutionError.class,
				FMLAssignmentError.class, FMLArgumentError.class, FMLCardinalityError.class, FMLTechnologyError.class, FMLHostError.class,
				FMLUserError.class, FMLAssertionError.class, FMLInternalError.class, FMLCancellation.class, FMLExecutionException.class }) {
			map.put(c.getSimpleName(), c);
		}
		IMPLICIT_TYPES = Collections.unmodifiableMap(map);
	}

	/** The errors being handled by a catch block on the current thread, innermost last */
	private static final ThreadLocal<Deque<FMLError>> HANDLED = ThreadLocal.withInitial(ArrayDeque::new);

	private FMLErrorTypes() {
	}

	/**
	 * Run supplied catch block for supplied error: while it runs, the error is being handled, and {@code throw} of it (or of the exception
	 * it carries) rethrows that very error
	 */
	public static <T> T whileHandling(FMLError error, HandledBlock<T> block)
			throws org.openflexo.foundation.fml.rt.ReturnException, FMLExecutionException {
		Deque<FMLError> handled = HANDLED.get();
		handled.addLast(error);
		try {
			return block.run();
		} finally {
			handled.removeLastOccurrence(error);
			if (handled.isEmpty()) {
				HANDLED.remove();
			}
		}
	}

	/** Tell if supplied error is being handled by a catch block of the current thread */
	public static boolean isBeingHandled(FMLError error) {
		return error != null && HANDLED.get().contains(error);
	}

	@FunctionalInterface
	public interface HandledBlock<T> {
		T run() throws org.openflexo.foundation.fml.rt.ReturnException, FMLExecutionException;
	}

	/** The built-in error types visible in FML without import, by simple name */
	public static Map<String, Class<?>> getImplicitTypes() {
		return IMPLICIT_TYPES;
	}

	/** The built-in error type of supplied simple name, or null */
	public static Class<?> getImplicitType(String simpleName) {
		return IMPLICIT_TYPES.get(simpleName);
	}

	/** Tell if supplied type is one of the built-in error types (catchable or not) */
	public static boolean isImplicitType(Type type) {
		return type instanceof Class && IMPLICIT_TYPES.containsValue(type);
	}

	/** The FML exception that supplied type stands for, or null when it is not the instance type of an FML exception */
	public static FlexoException getFlexoException(Type type) {
		if (type instanceof FlexoConceptInstanceType && ((FlexoConceptInstanceType) type).getFlexoConcept() instanceof FlexoException) {
			return (FlexoException) ((FlexoConceptInstanceType) type).getFlexoConcept();
		}
		return null;
	}

	/**
	 * Tell if a {@code catch} clause may name supplied type: an {@link FMLError} type, or an FML exception. {@link FMLInternalError} (a
	 * defect of the platform), {@link FMLCancellation} (the user cancelled) and the untyped {@link FMLExecutionException} cannot be caught
	 */
	public static boolean isCatchable(Type type) {
		if (getFlexoException(type) != null) {
			return true;
		}
		Class<?> raw = type instanceof Class ? (Class<?>) type : null;
		return raw != null && FMLError.class.isAssignableFrom(raw);
	}

	/** Tell if supplied type may be thrown by FML: an FML exception, or a caught error (rethrown) */
	public static boolean isThrowable(Type type) {
		if (getFlexoException(type) != null) {
			return true;
		}
		Class<?> raw = TypeUtils.getRawType(type);
		return raw != null && FMLError.class.isAssignableFrom(raw);
	}

	/** Tell if a {@code catch} clause naming supplied type catches supplied error */
	public static boolean catches(Type catchType, FMLError error) {
		FlexoException exception = getFlexoException(catchType);
		if (exception != null) {
			return error instanceof FMLUserError && ((FMLUserError) error).getExceptionInstance() != null
					&& exception.isAssignableFrom(((FMLUserError) error).getExceptionInstance().getFlexoConcept());
		}
		return catchType instanceof Class && FMLError.class.isAssignableFrom((Class<?>) catchType)
				&& ((Class<?>) catchType).isInstance(error);
	}

	/** Tell if every error caught by a clause naming {@code wider} would be caught by a clause naming {@code narrower} before it */
	public static boolean covers(Type wider, Type narrower) {
		FlexoException wideException = getFlexoException(wider);
		FlexoException narrowException = getFlexoException(narrower);
		if (wideException != null) {
			return narrowException != null && wideException.isAssignableFrom(narrowException);
		}
		if (!(wider instanceof Class) || !FMLError.class.isAssignableFrom((Class<?>) wider)) {
			return false;
		}
		if (narrowException != null) {
			return ((Class<?>) wider).isAssignableFrom(FMLUserError.class);
		}
		return narrower instanceof Class && ((Class<?>) wider).isAssignableFrom((Class<?>) narrower);
	}

	/**
	 * The value given to the variable of a {@code catch} clause naming supplied type: the FML exception instance when the clause names an
	 * FML exception, the error itself otherwise
	 */
	public static Object caughtValue(Type catchType, FMLError error) {
		if (getFlexoException(catchType) != null && error instanceof FMLUserError) {
			return ((FMLUserError) error).getExceptionInstance();
		}
		return error;
	}
}
