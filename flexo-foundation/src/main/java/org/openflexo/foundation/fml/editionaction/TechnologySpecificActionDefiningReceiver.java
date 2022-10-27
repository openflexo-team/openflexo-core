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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * 
 * Represents a {@link TechnologySpecificAction} which applies on a given technology object<br>
 * This object is called the 'receiver' of the action
 * 
 * @author sylvain
 *
 * @param <MS>
 *            Type of model slot which contractualize access to a given technology resource on which this action applies
 * @param <R>
 *            Type of receiver on this action (the precise technology object on which this action apply)
 * @param <T>
 *            Type of assigned value
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TechnologySpecificActionDefiningReceiver.TechnologySpecificActionImpl.class)
public abstract interface TechnologySpecificActionDefiningReceiver<MS extends ModelSlot<?>, R /*extends TechnologyObject<?>*/, T>
		extends TechnologySpecificAction<MS, T> {

	@PropertyIdentifier(type = DataBinding.class)
	String RECEIVER_KEY = "receiver";

	@Getter(value = RECEIVER_KEY, ignoreForEquality = true)
	@XMLAttribute
	@Deprecated
	DataBinding<R> getReceiver();

	@Setter(RECEIVER_KEY)
	@Deprecated
	void setReceiver(DataBinding<R> receiver);

	public Class<? extends R> getReceiverClass();

	public Type getReceiverType();

	@Deprecated
	public boolean isReceiverMandatory();

	/**
	 * Compute and return infered model slot from getReceiver() binding<br>
	 * Please note that infered model slot might be null if receiver value is not given through a ModelSlot
	 * 
	 * @return
	 */
	public MS getInferedModelSlot();

	/**
	 * Evaluate and return receiver of this action, given supplied context
	 * 
	 * @param evaluationContext
	 * @return
	 */
	public R getReceiver(BindingEvaluationContext evaluationContext);

	public static abstract class TechnologySpecificActionDefiningReceiverImpl<MS extends ModelSlot<?>, R /*extends TechnologyObject<?>*/, T>
			extends TechnologySpecificActionImpl<MS, T> implements TechnologySpecificActionDefiningReceiver<MS, R, T> {
		private DataBinding<R> receiver;

		/**
		 * Compute and return infered model slot from getReceiver() binding<br>
		 * Please not that infered model slot might be null if receiver value is not given through a ModelSlot
		 * 
		 * @return
		 */
		@Override
		public MS getInferedModelSlot() {
			if (getReceiver().isSet() && getReceiver().isValid() && getReceiver().isBindingPath()) {
				BindingPath bindingPath = ((BindingPath) getReceiver().getExpression());
				IBindingPathElement lastPathElement = bindingPath.getLastBindingPathElement();
				if (lastPathElement instanceof ModelSlotBindingVariable) {
					return (MS) ((ModelSlotBindingVariable) lastPathElement).getModelSlot();
				}
				else if (lastPathElement instanceof FlexoPropertyPathElement
						&& ((FlexoPropertyPathElement) lastPathElement).getFlexoProperty() instanceof ModelSlot) {
					return (MS) ((FlexoPropertyPathElement) lastPathElement).getFlexoProperty();
				}
			}
			return null;
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getReceiver().toString() + "." + getImplementedInterface().getSimpleName()
					+ getParametersStringRepresentation();
		}

		@Override
		public DataBinding<R> getReceiver() {
			if (receiver == null) {
				receiver = new DataBinding<>(this, getReceiverClass(), BindingDefinitionType.GET);
				receiver.setBindingName("receiver");
				receiver.setMandatory(isReceiverMandatory());
			}
			return receiver;
		}

		@Override
		public void setReceiver(DataBinding<R> receiver) {
			if (receiver != null) {
				receiver.setOwner(this);
				receiver.setBindingName("receiver");
				receiver.setDeclaredType(getReceiverClass());
				receiver.setBindingDefinitionType(BindingDefinitionType.GET);
				receiver.setMandatory(isReceiverMandatory());
			}
			this.receiver = receiver;
		}

		/**
		 * Evaluate and return receiver of this action, given supplied context
		 * 
		 * @param evaluationContext
		 * @return
		 */
		@Override
		public R getReceiver(BindingEvaluationContext evaluationContext) {
			if (getReceiver().isValid()) {
				try {
					return getReceiver().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public final Class<? extends R> getReceiverClass() {
			return (Class<? extends R>) TypeUtils.getBaseClass(getReceiverType());
		}

		@Override
		public final Type getReceiverType() {
			return TypeUtils.getTypeArgument(getClass(), TechnologySpecificActionDefiningReceiver.class, 1);
		}

		@Deprecated
		@Override
		public boolean isReceiverMandatory() {
			return true;
		}

	}

	@DefineValidationRule
	public static class ReceiverBindingIsRequiredAndMustBeValid
			extends BindingIsRecommandedAndShouldBeValid<TechnologySpecificActionDefiningReceiver> {
		public ReceiverBindingIsRequiredAndMustBeValid() {
			super("'receiver'_binding_is_required_and_must_be_valid", TechnologySpecificActionDefiningReceiver.class);
		}

		@Override
		public DataBinding<Object> getBinding(TechnologySpecificActionDefiningReceiver object) {
			return object.getReceiver();
		}

		@Override
		public ValidationIssue<BindingIsRecommandedAndShouldBeValid<TechnologySpecificActionDefiningReceiver>, TechnologySpecificActionDefiningReceiver> applyValidation(
				TechnologySpecificActionDefiningReceiver action) {

			if (!action.isReceiverMandatory()) {
				return null;
			}

			return super.applyValidation(action);

		}

	}

}
