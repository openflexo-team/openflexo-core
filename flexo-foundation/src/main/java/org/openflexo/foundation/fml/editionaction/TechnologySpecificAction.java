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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.FlexoConceptFlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationIssue;

/**
 * 
 * Represents an {@link EditionAction} which address a specific technology through the reference to a container object (which is generally a
 * pointer to a ModelSlot)
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
@ImplementationClass(TechnologySpecificAction.TechnologySpecificActionImpl.class)
public abstract interface TechnologySpecificAction<MS extends ModelSlot<?>, R extends TechnologyObject<?>, T> extends AssignableAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	String RECEIVER_KEY = "receiver";
	@PropertyIdentifier(type = ModelSlot.class)
	public static final String DEPRECATED_MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = RECEIVER_KEY)
	@XMLAttribute
	DataBinding<R> getReceiver();

	@Setter(RECEIVER_KEY)
	void setReceiver(DataBinding<R> receiver);

	public Class<? extends R> getReceiverClass();

	public Type getReceiverType();

	public boolean isReceiverMandatory();

	// Should not be used anymore, will be removed from future releases after 1.8.1
	@Deprecated
	@Getter(value = DEPRECATED_MODEL_SLOT_KEY)
	// TODO: should not be serialized anymore in future releases
	@XMLElement(primary = false)
	public MS getDeprecatedModelSlot();

	// Should not be used anymore, will be removed from future releases after 1.8.1
	@Deprecated
	@Setter(DEPRECATED_MODEL_SLOT_KEY)
	public void setDeprecatedModelSlot(MS modelSlot);

	@Deprecated
	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType);

	@Deprecated
	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots();

	@Deprecated
	public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots();

	// TODO: remove this method
	@Deprecated
	public ModelSlotInstance getModelSlotInstance(RunTimeEvaluationContext evaluationContext);

	@Deprecated
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	/**
	 * Compute and return infered model slot from getReceiver() binding<br>
	 * Please not that infered model slot might be null if receiver value is not given through a ModelSlot
	 * 
	 * @return
	 */
	public MS getInferedModelSlot();

	public Class<? extends MS> getModelSlotClass();

	public static abstract class TechnologySpecificActionImpl<MS extends ModelSlot<?>, R extends TechnologyObject<?>, T>
			extends AssignableActionImpl<T> implements TechnologySpecificAction<MS, R, T> {

		private static final Logger logger = Logger.getLogger(TechnologySpecificAction.class.getPackage().getName());

		private DataBinding<R> receiver;

		@Override
		public LocalizedDelegate getLocales() {
			if (getInferedModelSlot() != null && getInferedModelSlot().getModelSlotTechnologyAdapter() != null) {
				return getInferedModelSlot().getModelSlotTechnologyAdapter().getLocales();
			}
			return super.getLocales();
		}

		@Deprecated
		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType) {
			if (getFlexoConcept() instanceof VirtualModel) {
				return ((VirtualModel) getFlexoConcept()).getModelSlots(msType);
			}
			else if (getFlexoConcept() != null && getFlexoConcept().getOwningVirtualModel() != null) {
				return getFlexoConcept().getOwningVirtualModel().getModelSlots(msType);
			}
			return null;
		}

		@Deprecated
		@SuppressWarnings("unchecked")
		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots() {
			List<ModelSlot<?>> availableMS = getAllAvailableModelSlots();
			if (availableMS != null) {
				return (List<MS2>) findAvailableModelSlots(availableMS);
			}
			return Collections.emptyList();
		}

		@Deprecated
		@Override
		public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots() {
			return getAvailableModelSlots(FMLRTModelSlot.class);
		}

		@Deprecated
		@Override
		public ModelSlotInstance getModelSlotInstance(RunTimeEvaluationContext action) {
			FlexoConceptInstance fci = action.getFlexoConceptInstance();
			AbstractVirtualModelInstance<?, ?> vmi = null;
			if (fci != null && fci instanceof AbstractVirtualModelInstance<?, ?>) {
				vmi = (AbstractVirtualModelInstance<?, ?>) fci;
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
		}

		@Deprecated
		@SuppressWarnings("unchecked")
		private <MS2 extends ModelSlot<?>> List<MS2> getAllAvailableModelSlots() {
			List returned = new ArrayList<>();
			FlexoConcept concept = getFlexoConcept();
			if (concept != null) {
				returned.addAll(concept.getModelSlots());
				if (concept.getOwningVirtualModel() != null) {
					returned.addAll(getFlexoConcept().getOwningVirtualModel().getModelSlots());
				}
			}
			return returned;
		}

		@Deprecated
		private List<ModelSlot<?>> findAvailableModelSlots(List<ModelSlot<?>> msList) {
			List<ModelSlot<?>> returned = new ArrayList<ModelSlot<?>>();
			if (msList == null) {
				msList = getAllAvailableModelSlots();
			}
			for (ModelSlot<?> ms : msList) {
				for (Class<?> editionActionType : ms.getAvailableEditionActionTypes()) {
					if (TypeUtils.isAssignableTo(this, editionActionType)) {
						if (!returned.contains(ms))
							returned.add(ms);
					}
				}
				for (Class<?> editionActionType : ms.getAvailableFetchRequestActionTypes()) {
					if (TypeUtils.isAssignableTo(this, editionActionType)) {
						if (!returned.contains(ms))
							returned.add(ms);
					}
				}
			}
			return returned;
		}

		// Should not be used anymore, will be removed from future releases after 1.8.1
		/*@Deprecated
		@Override
		public MS getModelSlot() {
			MS ms = (MS) performSuperGetter(MODEL_SLOT_KEY);
			if (ms == null) {
				List<ModelSlot<?>> availableMSs = this.getAvailableModelSlots();
				if (availableMSs.size() == 1) {
					ms = (MS) availableMSs.get(0);
				}
			}
			return ms;
		}*/

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
				BindingPathElement lastPathElement = bindingValue.getLastBindingPathElement();
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

		// @Deprecated
		// private MS modelSlot;

		// Should not be used anymore, will be removed from future releases after 1.8.1
		@Deprecated
		@Override
		public MS getDeprecatedModelSlot() {
			/*if (modelSlot == null && receiver != null && receiver.isValid()) {
				return getInferedModelSlot();
			}
			return modelSlot;*/
			return null;
		}

		// Should not be used anymore, will be removed from future releases after 1.8.1
		@Deprecated
		@Override
		public void setDeprecatedModelSlot(MS modelSlot) {
			// performSuperSetter(MODEL_SLOT_KEY, modelSlot);
			if (modelSlot != null) {
				getReceiver().setUnparsedBinding(modelSlot.getName());
				/*if (getReceiver().isValid()) {
					// Nothing to do
				}
				else {
					getReceiver().reset();
				}*/
			}
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName()
					+ getParametersStringRepresentation();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return (getReceiver().isValid() ? getReceiver().toString() + "." : "<???" + getReceiver() + ">")
					+ getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName() + "()";
		}

		protected final String getTechnologyAdapterIdentifier() {
			if (getModelSlotTechnologyAdapter() != null) {
				return getModelSlotTechnologyAdapter().getIdentifier();
			}
			return "FML";
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
			return TypeUtils.getTypeArgument(getClass(), TechnologySpecificAction.class, 1);
		}

		@Override
		public boolean isReceiverMandatory() {
			return true;
		}

		@Override
		public final Class<? extends MS> getModelSlotClass() {
			return (Class<? extends MS>) TypeUtils.getTypeArgument(getClass(), TechnologySpecificAction.class, 0);
		}

		/*@Deprecated
		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getInferedModelSlot() != null) {
				return getInferedModelSlot().getModelSlotTechnologyAdapter();
			}
			return null;
		}*/

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(getModelSlotClass());
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ReceiverBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<TechnologySpecificAction> {
		public ReceiverBindingIsRequiredAndMustBeValid() {
			super("'receiver'_binding_is_required_and_must_be_valid", TechnologySpecificAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(TechnologySpecificAction object) {
			return object.getReceiver();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<TechnologySpecificAction>, TechnologySpecificAction> applyValidation(
				TechnologySpecificAction action) {

			System.out.println("On valide l'action " + action);
			if (action.isReceiverMandatory()) {
				return super.applyValidation(action);
			}
			return null;
		}

	}

}
