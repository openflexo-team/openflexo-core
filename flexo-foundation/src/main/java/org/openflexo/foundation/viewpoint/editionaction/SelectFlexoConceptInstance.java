/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.viewpoint.editionaction;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.ModelSlotInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptInstanceType;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Generic {@link FetchRequest} allowing to retrieve a selection of some {@link FlexoConceptInstance} matching some conditions and a given
 * {@link FlexoConcept}.<br>
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 * @param <T>
 */
@FIBPanel("Fib/VPM/SelectFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(SelectFlexoConceptInstance.SelectFlexoConceptInstanceImpl.class)
@XMLElement
public interface SelectFlexoConceptInstance extends FetchRequest<VirtualModelModelSlot, FlexoConceptInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public static abstract class SelectFlexoConceptInstanceImpl extends FetchRequestImpl<VirtualModelModelSlot, FlexoConceptInstance>
			implements SelectFlexoConceptInstance {

		protected static final Logger logger = FlexoLogger.getLogger(SelectFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private String flexoConceptTypeURI;

		public SelectFlexoConceptInstanceImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}
			out.append(getClass().getSimpleName() + (getModelSlot() != null ? " from " + getModelSlot().getName() : " ") + " as "
					+ (getFlexoConceptType()!=null ? getFlexoConceptType().getName():"No Type Specified")
					+ (getConditions().size() > 0 ? " " + getWhereClausesFMLRepresentation(context) : ""), context);
			if (getAssignation().isSet()) {
				out.append(")", context);
			}
			return out.toString();
		}

		@Override
		public FlexoConceptInstanceType getFetchedType() {
			return (FlexoConceptInstanceType) FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
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

		// private boolean isUpdatingBindingModels = false;

		@Override
		public FlexoConcept getFlexoConceptType() {
			// System.out.println("getFlexoConceptType() for " + flexoConceptTypeURI);
			// System.out.println("vm=" + getVirtualModel());
			// System.out.println("ep=" + getFlexoConcept());
			// System.out.println("ms=" + getModelSlot());
			// if (getModelSlot() instanceof VirtualModelModelSlot) {
			// System.out.println("ms.vm=" + ((VirtualModelModelSlot) getModelSlot()).getAddressedVirtualModel());
			// }
			if (flexoConceptType == null && flexoConceptTypeURI != null && getVirtualModel() != null) {
				flexoConceptType = getVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				/*if (!isUpdatingBindingModels) {
					isUpdatingBindingModels = true;
					for (FlexoBehaviour s : getFlexoConcept().getEditionSchemes()) {
						s.updateBindingModels();
					}
					isUpdatingBindingModels = false;
				}*/
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getFlexoConcept() instanceof VirtualModel) {
				flexoConceptType = ((VirtualModel) getFlexoConcept()).getFlexoConcept(flexoConceptTypeURI);
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getModelSlot() instanceof VirtualModelModelSlot) {
				if (getModelSlot().getAddressedVirtualModel() != null) {
					flexoConceptType = getModelSlot().getAddressedVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				}
			}
			// System.out.println("return " + flexoConceptType);
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				this.flexoConceptType = flexoConceptType;
				for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
					s.updateBindingModels();
				}
			}
		}

		@Override
		public String getStringRepresentation() {
			return getImplementedInterface().getSimpleName() + (getFlexoConceptType() != null ? " : " + getFlexoConceptType().getName() : "")
					+ (StringUtils.isNotEmpty(getAssignation().toString()) ? " (" + getAssignation().toString() + ")" : "");
		}

		@Override
		public List<FlexoConceptInstance> performAction(FlexoBehaviourAction action) {
			VirtualModelInstance vmi = null;
			if (getModelSlot() instanceof VirtualModelModelSlot) {
				ModelSlotInstance modelSlotInstance = action.getVirtualModelInstance().getModelSlotInstance(getModelSlot());
				if (modelSlotInstance != null) {
					// System.out.println("modelSlotInstance=" + modelSlotInstance + " model=" + modelSlotInstance.getModel());
					vmi = (VirtualModelInstance) modelSlotInstance.getAccessedResourceData();
				} else {
					logger.warning("Cannot find ModelSlotInstance for " + getModelSlot());
				}
			} else {
				vmi = action.getVirtualModelInstance();
			}
			if (vmi != null) {
				System.out.println("Returning " + vmi.getFlexoConceptInstances(getFlexoConceptType()));
				return filterWithConditions(vmi.getFlexoConceptInstances(getFlexoConceptType()), action);
			} else {
				logger.warning(getStringRepresentation()
						+ " : Cannot find virtual model instance on which to apply SelectFlexoConceptInstance");
				// logger.warning("Additional info: getModelSlot()=" + getModelSlot());
				// logger.warning("Additional info: action.getVirtualModelInstance()=" + action.getVirtualModelInstance());
				// logger.warning("Additional info: action.getVirtualModelInstance().getModelSlotInstance(getModelSlot())="
				// + action.getVirtualModelInstance().getModelSlotInstance(getModelSlot()));
				return null;
			}
		}
	}
}
