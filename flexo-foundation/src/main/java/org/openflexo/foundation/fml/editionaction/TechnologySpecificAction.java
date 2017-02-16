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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;

/**
 * Represents an {@link EditionAction} which address a specific technology through the reference to a {@link ModelSlot}
 * 
 * Such action must reference a {@link ModelSlot}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TechnologySpecificAction.TechnologySpecificActionImpl.class)
public abstract interface TechnologySpecificAction<MS extends ModelSlot<?>, T> extends AssignableAction<T> {

	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";

	@Deprecated
	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement(primary = false)
	public MS getModelSlot();

	@Deprecated
	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);

	@Deprecated
	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType);

	@Deprecated
	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots();

	@Deprecated
	public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots();

	@Deprecated
	public ModelSlotInstance getModelSlotInstance(RunTimeEvaluationContext evaluationContext);

	@Deprecated
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	public static abstract class TechnologySpecificActionImpl<MS extends ModelSlot<?>, T> extends AssignableActionImpl<T>
			implements TechnologySpecificAction<MS, T> {

		private static final Logger logger = Logger.getLogger(TechnologySpecificAction.class.getPackage().getName());

		@Deprecated
		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getModelSlot() != null) {
				return getModelSlot().getModelSlotTechnologyAdapter();
			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (getModelSlot() != null && getModelSlot().getModelSlotTechnologyAdapter() != null) {
				return getModelSlot().getModelSlotTechnologyAdapter().getLocales();
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
				return vmi.getModelSlotInstance((ModelSlot) getModelSlot());
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
			FlexoConcept concept = getFlexoConcept();
			if (concept != null) {
				if (concept instanceof VirtualModel) {
					return (List<MS2>) ((VirtualModel) getFlexoConcept()).getModelSlots();
				}
				else if (concept instanceof ViewPoint) {
					return (List<MS2>) ((ViewPoint) getFlexoConcept()).getModelSlots();
				}
			}
			else if (concept != null && concept.getOwningVirtualModel() != null) {
				return (List<MS2>) getFlexoConcept().getOwningVirtualModel().getModelSlots();
			}
			return null;
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

		@Deprecated
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
			return (getModelSlot() != null ? getModelSlot().getName() + "." : "") + getTechnologyAdapterIdentifier() + "::"
					+ getImplementedInterface().getSimpleName() + "()";
		}

		protected final String getTechnologyAdapterIdentifier() {
			if (getModelSlotTechnologyAdapter() != null) {
				return getModelSlotTechnologyAdapter().getIdentifier();
			}
			return "FML";
		}

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot
			extends ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(TechnologySpecificAction.class, "EditionAction_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>> applyValidation(
				TechnologySpecificAction<?, ?> anAction) {
			ModelSlot ms = anAction.getModelSlot();
			if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
				RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(anAction);
				return new ValidationWarning<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>>(this, anAction,
						"EditionAction_should_not_have_reflexive_model_slot_no_more", fixProposal);

			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot
				extends FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>> {

			private final TechnologySpecificAction<?, ?> action;

			public RemoveReflexiveVirtualModelModelSlot(TechnologySpecificAction<?, ?> anAction) {
				super("remove_reflexive_modelslot");
				this.action = anAction;
			}

			@Override
			protected void fixAction() {
				action.setModelSlot(null);
			}
		}

	}

	/*@DefineValidationRule
	public static class TechnologypecificActionMustReferenceAModelSlot extends
			ValidationRule<TechnologypecificActionMustReferenceAModelSlot, TechnologySpecificAction> {
		public TechnologypecificActionMustReferenceAModelSlot() {
			super(TechnologySpecificAction.class, "technology_specific_action_must_adress_a_valid_model_slot");
		}
	
		@Override
		public ValidationIssue<TechnologypecificActionMustReferenceAModelSlot, TechnologySpecificAction> applyValidation(
				TechnologySpecificAction action) {
			if (action.getModelSlot() == null) {
				return new ValidationError<TechnologypecificActionMustReferenceAModelSlot, TechnologySpecificAction>(this, action,
						"action_does_not_define_any_model_slot");
			}
			return null;
		}
	}*/

}
