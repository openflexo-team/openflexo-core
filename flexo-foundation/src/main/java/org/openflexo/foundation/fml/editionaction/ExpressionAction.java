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
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.type.UndefinedType;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * An {@link EditionAction} which can be represented as an expression
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ExpressionAction.ExpressionActionImpl.class)
@XMLElement
public interface ExpressionAction<T> extends AssignableAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXPRESSION_KEY = "expression";

	@Getter(value = EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<T> getExpression();

	@Setter(EXPRESSION_KEY)
	public void setExpression(DataBinding<T> expression);

	@Override
	public Type getAssignableType();

	public static abstract class ExpressionActionImpl<T> extends AssignableActionImpl<T> implements ExpressionAction<T> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ExpressionAction.class.getPackage().getName());

		private DataBinding<T> expression;

		// private Type assignableType = null;
		// private boolean isComputingAssignableType = false;

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			// Attempt to resolve assignable type at the deserialization
			getAssignableType();
		}

		private boolean isAnalyzingType = false;

		@Override
		public Type getAssignableType() {

			if (isAnalyzingType) {
				return UndefinedType.INSTANCE;
			}

			if (getExpression() != null && getExpression().isSet() && getExpression().isValid()) {
				isAnalyzingType = true;
				Type returned = getExpression().getAnalyzedType();
				isAnalyzingType = false;
				return returned;
			}

			// TODO
			// Gros hack ici: le probleme est que la BindingFactory n'etait pas valide au moment de l'analyse
			// Il faut donc ecouter les modifications de getBindingFactory()

			if (getExpression() != null && !getExpression().isValid()) {
				isAnalyzingType = true;
				getExpression().forceRevalidate();
				isAnalyzingType = false;
				if (getExpression().isValid()) {
					return getExpression().getAnalyzedType();
				}
			}

			return UndefinedType.INSTANCE;

			/*if (assignableType == null && !isComputingAssignableType) {
				isComputingAssignableType = true;
				if (getExpression() != null && getExpression().isSet() && getExpression().isValid()) {
					assignableType = getExpression().getAnalyzedType();
					System.out.println("Pour l'expression [" + getExpression() + "] le type c'est " + assignableType);
				}
				isComputingAssignableType = false;
				// Hacking area - No time yet to fix this
				// This case handles a CustomType which is not resolved yet
				// Since, we have to way to know when this type will be resolved, we don't cache it
				// TODO: handle this issue properly
				if (assignableType instanceof CustomType && !((CustomType) assignableType).isResolved()) {
					CustomType returned = (CustomType) assignableType;
					assignableType = null;
					return returned;
				}
			}
			if (assignableType == null) {
				return Object.class;
			}
			return assignableType;*/
		}

		@Override
		public void notifiedScopeChanged() {
			super.notifiedScopeChanged();
			notifyTypeMightHaveChanged();
		}

		/**
		 * Called to explicitely check assignable type<br>
		 * Force expression DataBinding to be re-evaluated
		 */
		private void notifyTypeMightHaveChanged() {
			// assignableType = null;
			getPropertyChangeSupport().firePropertyChange("assignableType", null, getAssignableType());
			getPropertyChangeSupport().firePropertyChange("iteratorType", null, getIteratorType());
			getPropertyChangeSupport().firePropertyChange("isIterable", null, isIterable());
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getExpression()) {
				notifyTypeMightHaveChanged();
			}
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
			super.notifiedBindingDecoded(dataBinding);
			if (dataBinding == getExpression()) {
				notifyTypeMightHaveChanged();
			}
		}

		@Override
		public DataBinding<T> getExpression() {
			if (expression == null) {
				expression = new DataBinding<>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				expression.setBindingName(EXPRESSION_KEY);
				expression.setMandatory(true);
			}
			return expression;
		}

		@Override
		public void setExpression(DataBinding<T> expression) {

			/*if (expression.toString().contains("log(\"CreationScheme in B\")")) {
				System.out.println("Qui est le con ?");
				Thread.dumpStack();
				System.exit(-1);
			}*/

			if (expression != null) {
				expression.setOwner(this);
				expression.setDeclaredType(Object.class);
				expression.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				expression.setBindingName(EXPRESSION_KEY);
				expression.setMandatory(true);
			}
			this.expression = expression;
			notifiedBindingChanged(expression);
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + (getExpression() != null ? getExpression().toString() : "");
		}

		private boolean forceRevalidated = false;

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {

			// Quick and dirty hack because found invalid binding
			// TODO: please investigate
			if (!getExpression().isValid() && !forceRevalidated) {
				forceRevalidated = true;
				getExpression().forceRevalidate();
			}

			try {
				return getExpression().getBindingValue(evaluationContext);
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof FlexoException) {
					throw (FlexoException) e.getTargetException();
				}
				throw new FlexoException(e);
			} catch (NullReferenceException e) {
				System.out.println("Unexpected NullReferenceException while executing " + getExpression());
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				throw new FlexoException(e);
			}
		}

		@Override
		public String toString() {
			// A virer bien sur !!!
			return "Coucou l'expression avec " + getExpression() + " valid=" + getExpression().isValid();
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
