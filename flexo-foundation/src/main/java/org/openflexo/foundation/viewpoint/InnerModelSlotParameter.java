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
package org.openflexo.foundation.viewpoint;

import java.util.List;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.technologyadapter.ModelSlot;
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

@ModelEntity(isAbstract = true)
@ImplementationClass(InnerModelSlotParameter.InnerModelSlotParameterImpl.class)
public abstract interface InnerModelSlotParameter<MS extends ModelSlot<?>> extends FlexoBehaviourParameter {

	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement
	public MS getModelSlot();

	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);

	public List<? extends ModelSlot> getAccessibleModelSlots();

	public static abstract class InnerModelSlotParameterImpl<MS extends ModelSlot<?>> extends FlexoBehaviourParameterImpl implements
			InnerModelSlotParameter<MS> {

		private MS modelSlot;

		public InnerModelSlotParameterImpl() {
			super();
		}

		@Override
		public MS getModelSlot() {
			return modelSlot;
		}

		@Override
		public void setModelSlot(MS modelSlot) {
			this.modelSlot = modelSlot;
			setChanged();
			notifyObservers(new DataModification("modelSlot", null, modelSlot));
		}

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot extends
			ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, InnerModelSlotParameter> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(InnerModelSlotParameter.class, "Parameter_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, InnerModelSlotParameter> applyValidation(
				InnerModelSlotParameter aParameter) {
			ModelSlot ms = aParameter.getModelSlot();
			if (ms instanceof VirtualModelModelSlot && "virtualModelInstance".equals(ms.getName())) {
				RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(aParameter);
				return new ValidationWarning<ShouldNotHaveReflexiveVirtualModelModelSlot, InnerModelSlotParameter>(this, aParameter,
						"Parameter_should_not_have_reflexive_model_slot_no_more", fixProposal);

			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot extends
				FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, InnerModelSlotParameter> {

			private final InnerModelSlotParameter parameter;

			public RemoveReflexiveVirtualModelModelSlot(InnerModelSlotParameter aParameter) {
				super("remove_reflexive_modelslot");
				this.parameter = aParameter;
			}

			@Override
			protected void fixAction() {
				parameter.setModelSlot(null);
			}
		}

	}

}
