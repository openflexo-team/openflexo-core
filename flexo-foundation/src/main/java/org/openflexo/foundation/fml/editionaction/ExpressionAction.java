/**
 * 
 * Copyright (c) 2015, Openflexo
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.UndefinedType;
import org.openflexo.foundation.fml.binding.DataBindingPropertyImplementation;
import org.openflexo.foundation.fml.binding.DataBindingPropertyImplementation.DataBindingProperty;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.PropertyImplementation;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * An expression used as an FML action.
 * <p>
 * An expression action is built for an expression written as a statement, such as a method call ({@code books.add(book);}) or an
 * instance creation, and for an expression used as the value of a declaration, of an assignation or of a return statement, or as the
 * iterated list of a {@code for (Type item : expression)} loop. Execution evaluates the expression ({@link #getExpression()}) and returns
 * its value; the type of that value ({@link #getAssignableType()}) is the analyzed type of the expression.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * public Book newBook(required String title) {
 *     Book book = new Book(parameters.title);  // the value new Book(parameters.title) is an expression action
 *     books.add(book);                         // an expression action used as a statement
 *     return book;                             // the value book is an expression action
 * }
 * </pre>
 *
 * @author sylvain
 *
 * @param <T>
 *            type of the value of the expression
 */
@ModelEntity
@ImplementationClass(ExpressionAction.ExpressionActionImpl.class)
@XMLElement
public interface ExpressionAction<T> extends AssignableAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXPRESSION_KEY = "expression";

	@Getter(value = EXPRESSION_KEY)
	@PropertyImplementation(DataBindingPropertyImplementation.class)
	@DataBindingProperty(
			bindingName = EXPRESSION_KEY,
			declaredType = Object.class,
			owner = "this",
			bindingDefinitionType = BindingDefinitionType.GET,
			isMandatory = true)
	@XMLAttribute
	public DataBinding<T> getExpression();

	@Setter(EXPRESSION_KEY)
	public void setExpression(DataBinding<T> expression);

	@Updater(EXPRESSION_KEY)
	public void updateExpression(DataBinding<T> expression);

	@Override
	public Type getAssignableType();

	public static abstract class ExpressionActionImpl<T> extends AssignableActionImpl<T> implements ExpressionAction<T> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ExpressionAction.class.getPackage().getName());

		// This is the last assignable type that was computed
		// This value will be used to notify type changes
		private Type lastKnownAssignableType = UndefinedType.INSTANCE;

		// Flag to prevent stack overflow
		private boolean isAnalyzingType = false;

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			// Attempt to resolve assignable type at the deserialization
			getAssignableType();
		}

		@Override
		public Type getAssignableType() {

			if (isAnalyzingType) {
				return lastKnownAssignableType;
			}

			if (getExpression() != null && getExpression().isSet() && getExpression().isValid()) {
				isAnalyzingType = true;
				Type lastKnown = lastKnownAssignableType;
				lastKnownAssignableType = getExpression().getAnalyzedType();
				notifyAssignableTypeMightHaveChanged(lastKnown, lastKnownAssignableType);
				isAnalyzingType = false;
				return lastKnownAssignableType;
			}

			// TODO : je pense que ceci n'est plus utile et doit etre supprime
			// Gros hack ici: le probleme est que la BindingFactory n'etait pas valide au moment de l'analyse
			// Il faut donc ecouter les modifications de getBindingFactory()
			if (getExpression() != null && !getExpression().isValid()) {
				isAnalyzingType = true;
				getExpression().revalidate();
				isAnalyzingType = false;
				if (getExpression().isValid()) {
					return getExpression().getAnalyzedType();
				}
			}

			return UndefinedType.INSTANCE;

		}

		@Override
		public void notifiedScopeChanged() {
			super.notifiedScopeChanged();
			notifyAssignableTypeMightHaveChanged(lastKnownAssignableType, getExpression().getAnalyzedType());
		}

		/**
		 * Notify assignableType changes
		 */
		private void notifyAssignableTypeMightHaveChanged(Type lastKnown, Type newAssignableType) {

			if ((lastKnown == null && newAssignableType != null) || (lastKnown != null && !lastKnown.equals(newAssignableType))) {
				getPropertyChangeSupport().firePropertyChange("assignableType", lastKnown, newAssignableType);
				lastKnownAssignableType = newAssignableType;
				// TODO : virer ces deux lignes qui me semblent inutiles
				getPropertyChangeSupport().firePropertyChange("iteratorType", null, getIteratorType());
				getPropertyChangeSupport().firePropertyChange("isIterable", null, isIterable());
			}
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getExpression()) {
				notifyAssignableTypeMightHaveChanged(lastKnownAssignableType, getExpression().getAnalyzedType());
			}
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
			super.notifiedBindingDecoded(dataBinding);
			if (dataBinding == getExpression()) {
				notifyAssignableTypeMightHaveChanged(lastKnownAssignableType, getExpression().getAnalyzedType());
			}
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + (getExpression() != null ? getExpression().toString() : "");
		}

		private boolean forceRevalidated = false;

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			// Quick and dirty hack because found invalid binding
			// TODO: i think this is no more required
			if (!getExpression().isValid() && !forceRevalidated) {
				forceRevalidated = true;
				getExpression().revalidate();
			}

			try {
				return getExpression().getBindingValue(evaluationContext);
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof FMLExecutionException) {
					throw (FMLExecutionException) e.getTargetException();
				}
				throw new FMLExecutionException(e);
			} catch (NullReferenceException e) {
				throw new FMLExecutionException(e);
			} catch (TypeMismatchException e) {
				throw new FMLExecutionException(e);
			} catch (ReflectiveOperationException e) {
				throw new FMLExecutionException(e);
			}
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getExpression().rebuild();
		}
	}

	@DefineValidationRule
	public static class ExpressionBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ExpressionAction> {
		public ExpressionBindingIsRequiredAndMustBeValid() {
			super("'expression'_binding_is_required_and_must_be_valid", ExpressionAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(ExpressionAction object) {
			return object.getExpression();
		}

	}

}
