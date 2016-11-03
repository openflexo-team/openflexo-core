/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(FlexoConceptInstanceParameter.FlexoConceptInstanceParameterImpl.class)
@XMLElement
// TODO: deprecated, use generic FlexoBehaviourParameter instead
@Deprecated
public interface FlexoConceptInstanceParameter extends InnerModelSlotParameter<FMLRTModelSlot<?, ?>> {

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "aVirtualModelInstance";

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public AbstractVirtualModel<?> getModelSlotVirtualModel();

	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> vminstance);

	public static abstract class FlexoConceptInstanceParameterImpl extends InnerModelSlotParameterImpl<FMLRTModelSlot<?, ?>>
			implements FlexoConceptInstanceParameter {

		private FlexoConcept flexoConceptType;
		private String flexoConceptTypeURI;

		public FlexoConceptInstanceParameterImpl() {
			super();

		}

		@Override
		public Type getType() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getInstanceType();
			}
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}

		@Override
		public WidgetType getWidget() {
			// return WidgetType.FLEXO_CONCEPT;
			return WidgetType.CUSTOM_WIDGET;
		}

		@Override
		public String _getFlexoConceptTypeURI() {
			if (flexoConceptType != null) {
				return flexoConceptType.getURI();
			}
			return flexoConceptTypeURI;
		}

		@Override
		public void _setFlexoConceptTypeURI(String flexoConceptURI) {
			this.flexoConceptTypeURI = flexoConceptURI;
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (flexoConceptType == null && flexoConceptTypeURI != null && getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(flexoConceptTypeURI);
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				FlexoConcept oldValue = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				/*for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
					s.updateBindingModels();
				}*/
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, flexoConceptType);
			}
		}

		private DataBinding<VirtualModelInstance> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<VirtualModelInstance>(this, VirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName(VIRTUAL_MODEL_INSTANCE_KEY);
				virtualModelInstance.setMandatory(true);
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<VirtualModelInstance> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName(VIRTUAL_MODEL_INSTANCE_KEY);
				aVirtualModelInstance.setDeclaredType(VirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				aVirtualModelInstance.setMandatory(true);
			}
			this.virtualModelInstance = aVirtualModelInstance;
		}

		@Override
		public AbstractVirtualModel<?> getModelSlotVirtualModel() {
			if (getModelSlot() != null && getModelSlot().getAccessedVirtualModelResource() != null) {
				return getModelSlot().getAccessedVirtualModelResource().getVirtualModel();
			}
			return null;
		}

		@Override
		public void setModelSlot(FMLRTModelSlot<?, ?> modelSlot) {
			super.setModelSlot(modelSlot);
			setChanged();
			notifyObservers(new DataModification("modelSlotVirtualModel", null, modelSlot));
		}

		@Override
		public FMLRTModelSlot<?, ?> getModelSlot() {
			if (super.getModelSlot() instanceof FMLRTModelSlot) {
				FMLRTModelSlot<?, ?> returned = super.getModelSlot();
				if (returned == null) {
					if (getOwningVirtualModel() != null && getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class).size() > 0) {
						return getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class).get(0);
					}
				}
				return returned;
			}
			return null;
		}

		@Override
		public List<FMLRTModelSlot> getAccessibleModelSlots() {
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class);
			}
			return null;
		}
	}
}
