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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;

import org.openflexo.connie.type.UndefinedType;
import org.openflexo.foundation.fml.rt.FlexoExceptionInstance;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.error.FMLError;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLErrorTypes;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.foundation.fml.rt.error.FMLTypeError;
import org.openflexo.foundation.fml.rt.error.FMLUserError;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * The FML statement {@code throw <expression>;} (CORE-F-11 phase 7).
 * <p>
 * The expression gives either a new instance of an FML exception ({@code throw new OutOfStock(n);}), raised as an {@link FMLUserError},
 * or an error caught by a {@code catch} clause ({@code throw e;}), rethrown unchanged with its FML stack. Throwing null is an
 * {@link FMLNullReferenceError}.
 */
@ModelEntity
@ImplementationClass(ThrowStatement.ThrowStatementImpl.class)
public interface ThrowStatement<T> extends AbstractAssignationAction<T> {

	public static abstract class ThrowStatementImpl<T> extends AbstractAssignationActionImpl<T> implements ThrowStatement<T> {

		@Override
		public Type getInferedType() {
			// A throw gives no value to the enclosing control graph
			return Void.class;
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			Object value = getAssignationValue(evaluationContext);
			if (value == null) {
				throw new FMLNullReferenceError("Cannot throw null: " + getThrownExpressionText());
			}
			if (value instanceof FlexoExceptionInstance) {
				FlexoExceptionInstance exception = (FlexoExceptionInstance) value;
				if (FMLErrorTypes.isBeingHandled(exception.getThrownError())) {
					// Thrown again by the catch block handling it: the same error, with the FML stack of its first throw
					throw exception.getThrownError();
				}
				// A new exception, or one kept and thrown again later: a new error, with a stack of its own, reported again
				throw new FMLUserError(exception);
			}
			if (value instanceof FMLError) {
				FMLError error = (FMLError) value;
				if (!FMLErrorTypes.isBeingHandled(error)) {
					// A built-in error kept and thrown again outside the block that caught it: it cannot be rebuilt, it is reported again
					FMLErrorReporter.unmarkReported(error);
				}
				throw error;
			}
			throw new FMLTypeError("Cannot throw " + value + ": not an exception (" + getThrownExpressionText() + ")");
		}

		private String getThrownExpressionText() {
			return getAssignableAction() != null ? getAssignableAction().getStringRepresentation() : "?";
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "throw " + getThrownExpressionText();
		}
	}

	/**
	 * The thrown value is an FML exception, or a caught error
	 */
	@DefineValidationRule
	public static class ThrownValueMustBeAnException extends ValidationRule<ThrownValueMustBeAnException, ThrowStatement<?>> {

		public ThrownValueMustBeAnException() {
			super(ThrowStatement.class, "thrown_value_must_be_an_exception");
		}

		@Override
		public ValidationIssue<ThrownValueMustBeAnException, ThrowStatement<?>> applyValidation(ThrowStatement<?> statement) {
			if (statement.getAssignableAction() == null) {
				return new ValidationError<>(this, statement, "throw_has_no_value");
			}
			Type type = statement.getAssignableAction().getAssignableType();
			if (type == null || type instanceof UndefinedType) {
				// Reported by the validation of the expression
				return null;
			}
			if (!FMLErrorTypes.isThrowable(type)) {
				return new ValidationError<>(this, statement, "only_an_fml_exception_or_a_caught_error_can_be_thrown");
			}
			return null;
		}
	}
}
