/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.editionaction;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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
 * Abstract class representing a primitive to be executed as an atomic action of an FlexoBehaviour
 * 
 * An edition action adresses a {@link ModelSlot}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TechnologySpecificAction.TechnologySpecificActionImpl.class)
public abstract interface TechnologySpecificAction<MS extends ModelSlot<?>, T> extends AssignableAction<T> {

	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement(primary = false)
	public MS getModelSlot();

	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);

	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType);

	public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots();

	public ModelSlotInstance<MS, ?> getModelSlotInstance(FlexoBehaviourAction<?, ?, ?> action);

	public TechnologyAdapter getModelSlotTechnologyAdapter();

	public static abstract class TechnologySpecificActionImpl<MS extends ModelSlot<?>, T> extends AssignableActionImpl<T> implements
			TechnologySpecificAction<MS, T> {

		private static final Logger logger = Logger.getLogger(TechnologySpecificAction.class.getPackage().getName());

		private MS modelSlot;

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			return modelSlot.getModelSlotTechnologyAdapter();
		}

		@Override
		public MS getModelSlot() {
			return modelSlot;
		}

		@Override
		public void setModelSlot(MS modelSlot) {
			if (modelSlot != this.modelSlot) {
				MS oldValue = this.modelSlot;
				this.modelSlot = modelSlot;
				getPropertyChangeSupport().firePropertyChange("modelSlot", oldValue, modelSlot);
			}
		}

		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType) {
			if (getFlexoConcept() != null && getFlexoConcept().getVirtualModel() != null) {
				return getFlexoConcept().getVirtualModel().getModelSlots(msType);
			} else if (getFlexoConcept() instanceof VirtualModel) {
				return ((VirtualModel) getFlexoConcept()).getModelSlots(msType);
			}
			return null;
		}

		@Override
		public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots() {
			return getAvailableModelSlots(FMLRTModelSlot.class);
		}

		@Override
		public ModelSlotInstance<MS, ?> getModelSlotInstance(FlexoBehaviourAction<?, ?, ?> action) {
			if (action.getVirtualModelInstance() != null) {
				VirtualModelInstance vmi = action.getVirtualModelInstance();
				// Following line does not compile with Java7 (don't understand why)
				// That's the reason i tried to fix that compile issue with getGenericModelSlot() method (see below)
				return action.getVirtualModelInstance().getModelSlotInstance(getModelSlot());
				// return (ModelSlotInstance<MS, ?>) vmi.getModelSlotInstance(getGenericModelSlot());
			} else {
				logger.severe("Could not access virtual model instance for action " + action);
				return null;
			}
		}

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot extends
			ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>> {

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

		protected static class RemoveReflexiveVirtualModelModelSlot extends
				FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, TechnologySpecificAction<?, ?>> {

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

}
