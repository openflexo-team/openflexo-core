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

package org.openflexo.foundation.fml.rt;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link ModelSlot} allowing to access an {@link VirtualModelInstance}<br>
 * 
 * Such {@link ModelSlot} is defining a general contract modellized by an abstract {@link VirtualModel}<br>
 * 
 * There are two different implementations of a {@link FMLRTModelSlot}:
 * <ul>
 * <li>Native implementation (see {@link FMLRTVirtualModelInstanceModelSlot}) provided by {@link FMLRTTechnologyAdapter}</li>
 * <li>Alternative implementation provided by some {@link TechnologyAdapter} which present data as instances of {@link FlexoConcept} (see
 * )</li>
 * </ul>
 * 
 * @author sylvain
 *
 * @param <VMI>
 *            type of {@link VirtualModelInstance} presented by this model slot
 * @param <TA>
 *            technology providing this model slot
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FMLRTVirtualModelInstanceModelSlot.class) })
@ImplementationClass(FMLRTModelSlot.FMLRTModelSlotImpl.class)
public interface FMLRTModelSlot<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter> extends ModelSlot<VMI> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute(xmlTag = "virtualModelURI")
	public String getAccessedVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setAccessedVirtualModelURI(String virtualModelURI);

	public VirtualModelResource getAccessedVirtualModelResource();

	public void setAccessedVirtualModelResource(VirtualModelResource virtualModelResource);

	public VirtualModel getAccessedVirtualModel();

	public void setAccessedVirtualModel(VirtualModel aVirtualModel);

	public FlexoConceptInstanceRole makeFlexoConceptInstanceRole(FlexoConcept flexoConcept);

	public Class<TA> getTechnologyAdapterClass();

	public static abstract class FMLRTModelSlotImpl<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
			extends ModelSlotImpl<VMI> implements FMLRTModelSlot<VMI, TA> {

		private static final Logger logger = Logger.getLogger(FMLRTModelSlot.class.getPackage().getName());

		@Override
		public FlexoConceptInstanceRole makeFlexoConceptInstanceRole(FlexoConcept flexoConcept) {
			FlexoConceptInstanceRole returned = makeFlexoRole(FlexoConceptInstanceRole.class);
			returned.setFlexoConceptType(flexoConcept);
			returned.setModelSlot(this);
			return returned;
		}

		@Override
		public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> flexoRoleClass) {
			if (FlexoConceptInstanceRole.class.isAssignableFrom(flexoRoleClass)) {
				return "flexoConceptInstance";
			}
			else if (PrimitiveRole.class.isAssignableFrom(flexoRoleClass)) {
				return "primitive";
			}
			logger.warning("Unexpected role: " + flexoRoleClass.getName());
			return null;
		}

		protected VirtualModelResource virtualModelResource;
		private String virtualModelURI;

		@Override
		public VirtualModelResource getAccessedVirtualModelResource() {

			if (virtualModelResource == null && StringUtils.isNotEmpty(virtualModelURI) && getVirtualModelLibrary() != null) {
				virtualModelResource = getVirtualModelLibrary().getVirtualModelResource(virtualModelURI);
				if (virtualModelResource != null) {
					logger.info("Looked-up " + virtualModelResource);
					getPropertyChangeSupport().firePropertyChange("type", null, getType());
					getPropertyChangeSupport().firePropertyChange("resultingType", null, getResultingType());
				}
			}

			return virtualModelResource;
		}

		@Override
		public void setAccessedVirtualModelResource(VirtualModelResource virtualModelResource) {
			this.virtualModelResource = virtualModelResource;
			if (virtualModelResource == null) {
				virtualModelURI = null;
			}
		}

		@Override
		public Type getType() {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getAccessedVirtualModel());
		}

		@Override
		public String getTypeDescription() {
			return "Virtual Model";
		};

		@Override
		public String getAccessedVirtualModelURI() {
			if (virtualModelResource != null) {
				return virtualModelResource.getURI();
			}
			return virtualModelURI;
		}

		@Override
		public void setAccessedVirtualModelURI(String metaModelURI) {
			this.virtualModelURI = metaModelURI;
		}

		/**
		 * Return adressed virtual model (the virtual model this model slot specifically adresses, not the one in which it is defined)
		 * 
		 * @return
		 */
		@Override
		public final VirtualModel getAccessedVirtualModel() {
			if (getAccessedVirtualModelResource() != null && !getAccessedVirtualModelResource().isLoading()) {
				// Do not load virtual model when unloaded
				// return getAccessedVirtualModelResource().getLoadedResourceData();
				try {
					return getAccessedVirtualModelResource().getResourceData(null);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FlexoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public void setAccessedVirtualModel(VirtualModel aVirtualModel) {
			this.virtualModelURI = aVirtualModel.getURI();
			notifyResultingTypeChanged();
		}

		/**
		 * 
		 * @param msInstance
		 * @param o
		 * @return URI as String
		 */
		@Override
		public String getURIForObject(ModelSlotInstance msInstance, Object o) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		/**
		 * @param msInstance
		 * @param objectURI
		 * @return the Object
		 */
		@Override
		public Object retrieveObjectWithURI(ModelSlotInstance msInstance, String objectURI) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		@Override
		public String getModelSlotDescription() {
			return "Virtual Model conform to " + getAccessedVirtualModelURI() /*+ (isReflexiveModelSlot() ? " [reflexive]" : "")*/;
		}

		@Override
		protected String getFMLRepresentationForConformToStatement() {
			return "conformTo " + getAccessedVirtualModelURI() + " ";
		}

		@SuppressWarnings("unchecked")
		@Override
		public VirtualModelModelSlotInstance<VMI, TA> makeActorReference(VMI object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			VirtualModelModelSlotInstance<VMI, TA> returned = factory.newInstance(VirtualModelModelSlotInstance.class);
			returned.setModelSlot(this);
			returned.setFlexoConceptInstance(fci);
			returned.setVirtualModelInstanceURI(object.getURI());
			return returned;

		}

	}

	@DefineValidationRule
	public static class VirtualModelIsRequired extends ValidationRule<VirtualModelIsRequired, FMLRTModelSlot<?, ?>> {
		public VirtualModelIsRequired() {
			super(FMLRTModelSlot.class, "virtual_model_is_required");
		}

		@Override
		public ValidationIssue<VirtualModelIsRequired, FMLRTModelSlot<?, ?>> applyValidation(FMLRTModelSlot<?, ?> modelSlot) {

			if (modelSlot.getAccessedVirtualModel() == null) {
				return new ValidationError<>(this, modelSlot, "fml_rt_model_slot_does_not_define_a_valid_virtual_model");
			}
			return null;
		}

	}

}
