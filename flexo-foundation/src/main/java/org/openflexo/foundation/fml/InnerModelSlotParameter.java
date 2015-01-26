/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.util.List;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
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
			if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
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
