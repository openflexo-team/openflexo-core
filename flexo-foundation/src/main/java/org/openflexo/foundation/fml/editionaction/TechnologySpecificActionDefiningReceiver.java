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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.binding.FlexoConceptFlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.validation.ValidationIssue;

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
public abstract interface TechnologySpecificActionDefiningReceiver<MS extends ModelSlot<?>, R extends TechnologyObject<?>, T>
		extends TechnologySpecificAction<MS, T> {

	@PropertyIdentifier(type = DataBinding.class)
	String RECEIVER_KEY = "receiver";

	@Getter(value = RECEIVER_KEY)
	@XMLAttribute
	DataBinding<R> getReceiver();

	@Setter(RECEIVER_KEY)
	void setReceiver(DataBinding<R> receiver);

	public Class<? extends R> getReceiverClass();

	public Type getReceiverType();

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

	// @Deprecated
	// public ModelSlotInstance<MS, ?> getModelSlotInstance(RunTimeEvaluationContext action);

	public static abstract class TechnologySpecificActionDefiningReceiverImpl<MS extends ModelSlot<?>, R extends TechnologyObject<?>, T>
			extends TechnologySpecificActionImpl<MS, T> implements TechnologySpecificActionDefiningReceiver<MS, R, T> {

		private static final Logger logger = Logger.getLogger(TechnologySpecificActionDefiningReceiver.class.getPackage().getName());

		private DataBinding<R> receiver;

		/**
		 * Compute and return infered model slot from getReceiver() binding<br>
		 * Please not that infered model slot might be null if receiver value is not given through a ModelSlot
		 * 
		 * @return
		 */
		@Override
		public MS getInferedModelSlot() {
			if (getReceiver().isSet() && getReceiver().isValid() && getReceiver().isBindingValue()) {
				BindingValue bindingValue = ((BindingValue) getReceiver().getExpression());
				IBindingPathElement lastPathElement = bindingValue.getLastBindingPathElement();
				if (lastPathElement instanceof ModelSlotBindingVariable) {
					return (MS) ((ModelSlotBindingVariable) lastPathElement).getModelSlot();
				}
				else if (lastPathElement instanceof FlexoConceptFlexoPropertyPathElement
						&& ((FlexoConceptFlexoPropertyPathElement) lastPathElement).getFlexoProperty() instanceof ModelSlot) {
					return (MS) ((FlexoConceptFlexoPropertyPathElement) lastPathElement).getFlexoProperty();
				}
			}
			return null;
		}

		/*@Deprecated
		@Override
		public ModelSlotInstance<MS, ?> getModelSlotInstance(RunTimeEvaluationContext action) {
			FlexoConceptInstance fci = action.getFlexoConceptInstance();
			VirtualModelInstance<?, ?> vmi = null;
			if (fci != null && fci instanceof VirtualModelInstance<?, ?>) {
				vmi = (VirtualModelInstance<?, ?>) fci;
			}
			else if (action.getVirtualModelInstance() != null) {
				vmi = action.getVirtualModelInstance();
			}
			if (vmi != null) {
				// Following line does not compile with Java7 (don't understand why)
				// That's the reason i tried to fix that compile issue with getGenericModelSlot() method (see below)
				// FD, in Java 8 return vmi.getModelSlotInstance(getModelSlot()); does not compile hence the cast
				return vmi.getModelSlotInstance((ModelSlot) getInferedModelSlot());
				// return (ModelSlotInstance<MS, ?>) vmi.getModelSlotInstance(getGenericModelSlot
			}
			else {
				logger.severe("Could not access virtual model instance for action " + action);
				return null;
			}
		}*/

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
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return getReceiver().toString() + "." + getImplementedInterface().getSimpleName() + "()";
		}

		@Override
		public DataBinding<R> getReceiver() {
			if (receiver == null) {
				receiver = new DataBinding<>(this, getReceiverClass(), BindingDefinitionType.GET);
				receiver.setBindingName("receiver");
				receiver.setMandatory(isReceiverMandatory());
			}

			// TODO: this code should be removed from future release (after 1.8.1)
			/*if (!receiver.isSet() && getModelSlot() != null) {
				receiver.setUnparsedBinding(getModelSlot().getName());
				receiver.isValid();
			}*/

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

		@Override
		public boolean isReceiverMandatory() {
			return true;
		}

	}

	@DefineValidationRule
	public static class ReceiverBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<TechnologySpecificActionDefiningReceiver> {
		public ReceiverBindingIsRequiredAndMustBeValid() {
			super("'receiver'_binding_is_required_and_must_be_valid", TechnologySpecificActionDefiningReceiver.class);
		}

		@Override
		public DataBinding<Object> getBinding(TechnologySpecificActionDefiningReceiver object) {
			return object.getReceiver();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<TechnologySpecificActionDefiningReceiver>, TechnologySpecificActionDefiningReceiver> applyValidation(
				TechnologySpecificActionDefiningReceiver action) {

			if (action.isReceiverMandatory()) {
				return super.applyValidation(action);
			}
			return null;
		}

	}

}