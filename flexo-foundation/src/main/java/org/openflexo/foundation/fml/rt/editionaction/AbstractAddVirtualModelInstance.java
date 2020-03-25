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

package org.openflexo.foundation.fml.rt.editionaction;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

/**
 * Generic base action used to instanciate a {@link FMLRTVirtualModelInstance} at top-level or in a given {@link FMLRTVirtualModelInstance}.
 * 
 * @author sylvain
 * 
 * @param <FCI>
 *            type of {@link FMLRTVirtualModelInstance} beeing created by this action
 */

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractAddVirtualModelInstance.AbstractAddVirtualModelInstanceImpl.class)
public interface AbstractAddVirtualModelInstance
		extends AbstractAddFlexoConceptInstance<FMLRTVirtualModelInstance, FMLRTVirtualModelInstance> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_NAME_KEY = "virtualModelInstanceName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_TITLE_KEY = "virtualModelInstanceTitle";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getVirtualModelInstanceName();

	@Setter(VIRTUAL_MODEL_INSTANCE_NAME_KEY)
	public void setVirtualModelInstanceName(DataBinding<String> virtualModelInstanceName);

	@Getter(value = VIRTUAL_MODEL_INSTANCE_TITLE_KEY)
	@XMLAttribute
	public DataBinding<String> getVirtualModelInstanceTitle();

	@Setter(VIRTUAL_MODEL_INSTANCE_TITLE_KEY)
	public void setVirtualModelInstanceTitle(DataBinding<String> virtualModelInstanceTitle);

	public VirtualModelResource getVirtualModelType();

	public void setVirtualModelType(VirtualModelResource resource);

	public static abstract class AbstractAddVirtualModelInstanceImpl
			extends AbstractAddFlexoConceptInstanceImpl<FMLRTVirtualModelInstance, FMLRTVirtualModelInstance>
			implements AbstractAddVirtualModelInstance {

		static final Logger logger = Logger.getLogger(AbstractAddVirtualModelInstance.class.getPackage().getName());

		private DataBinding<String> virtualModelInstanceName;
		private DataBinding<String> virtualModelInstanceTitle;

		@Override
		public Class<FMLRTVirtualModelInstance> getVirtualModelInstanceClass() {
			return FMLRTVirtualModelInstance.class;
		}

		@Override
		protected Class<? extends FlexoConcept> getDynamicFlexoConceptTypeType() {
			return VirtualModel.class;
		}

		@Override
		public DataBinding<String> getVirtualModelInstanceName() {
			if (virtualModelInstanceName == null) {
				virtualModelInstanceName = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				virtualModelInstanceName.setBindingName("virtualModelInstanceName");
			}
			return virtualModelInstanceName;
		}

		@Override
		public void setVirtualModelInstanceName(DataBinding<String> aVirtualModelInstanceName) {
			if (aVirtualModelInstanceName != null) {
				aVirtualModelInstanceName.setOwner(this);
				aVirtualModelInstanceName.setBindingName("virtualModelInstanceName");
				aVirtualModelInstanceName.setDeclaredType(String.class);
				aVirtualModelInstanceName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstanceName = aVirtualModelInstanceName;
		}

		@Override
		public DataBinding<String> getVirtualModelInstanceTitle() {
			if (virtualModelInstanceTitle == null) {
				virtualModelInstanceTitle = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				virtualModelInstanceTitle.setBindingName("virtualModelInstanceTitle");
			}
			return virtualModelInstanceTitle;
		}

		@Override
		public void setVirtualModelInstanceTitle(DataBinding<String> aVirtualModelInstanceTitle) {
			if (aVirtualModelInstanceTitle != null) {
				aVirtualModelInstanceTitle.setOwner(this);
				aVirtualModelInstanceTitle.setBindingName("virtualModelInstanceTitle");
				aVirtualModelInstanceTitle.setDeclaredType(String.class);
				aVirtualModelInstanceTitle.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstanceTitle = aVirtualModelInstanceTitle;
		}

		@Override
		public FMLRTVirtualModelInstance execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			System.out.println("Now create a FMLRTVirtualModelInstance");
			return super.execute(evaluationContext);
		}

		@Override
		public VirtualModelResource getVirtualModelType() {
			if (getFlexoConceptType() instanceof VirtualModel) {
				return (VirtualModelResource) ((VirtualModel) getFlexoConceptType()).getResource();
			}
			return null;
		}

		protected VirtualModel retrieveVirtualModel(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			FlexoConcept retrievedFlexoConcept = retrieveFlexoConcept(evaluationContext);
			if (retrievedFlexoConcept instanceof VirtualModel) {
				return (VirtualModel) retrievedFlexoConcept;
			}
			return null;

		}

		@Override
		public void setVirtualModelType(VirtualModelResource resource) {
			CreationScheme oldCreationScheme = getCreationScheme();
			VirtualModelResource oldVMType = getVirtualModelType();
			try {
				setCreationScheme(null);
				setFlexoConceptType(resource.getResourceData());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
			getPropertyChangeSupport().firePropertyChange("virtualModelType", oldVMType, getVirtualModelType());
			getPropertyChangeSupport().firePropertyChange("creationScheme", oldCreationScheme, getCreationScheme());
			getPropertyChangeSupport().firePropertyChange("availableCreationSchemes", null, getAvailableCreationSchemes());

			// select first one when none selected
			if (getCreationScheme() != null && getAvailableCreationSchemes().size() > 0) {
				setCreationScheme(getAvailableCreationSchemes().get(0));
			}
		}

	}

	@DefineValidationRule
	public static class ResourceNameIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AbstractAddVirtualModelInstance> {
		public ResourceNameIsRequiredAndMustBeValid() {
			super("'virtualModelInstanceName'_binding_is_required_and_must_be_valid", AbstractAddVirtualModelInstance.class);
		}

		@Override
		public DataBinding<String> getBinding(AbstractAddVirtualModelInstance object) {
			return object.getVirtualModelInstanceName();
		}

	}

}
