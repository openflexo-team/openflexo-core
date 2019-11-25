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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * This action is used to explicitely instanciate a new {@link FMLRTVirtualModelInstance} in an other {@link FMLRTVirtualModelInstance} with
 * some parameters
 * 
 * @author sylvain
 * 
 * @param <FCI>
 *            type of {@link FMLRTVirtualModelInstance} beeing created by this action
 */

@ModelEntity
@ImplementationClass(AddVirtualModelInstance.AddVirtualModelInstanceImpl.class)
@XMLElement
@FML("AddVirtualModelInstance")
public interface AddVirtualModelInstance extends AbstractAddFlexoConceptInstance<FMLRTVirtualModelInstance, FMLRTVirtualModelInstance> {

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

	public CompilationUnitResource getVirtualModelType();

	public void setVirtualModelType(CompilationUnitResource resource);

	/**
	 * Return type of View, when {@link #getVirtualModelInstance()} is set and valid
	 * 
	 * @return
	 */
	public CompilationUnitResource getOwnerVirtualModelResource();

	public static abstract class AddVirtualModelInstanceImpl extends
			AbstractAddFlexoConceptInstanceImpl<FMLRTVirtualModelInstance, FMLRTVirtualModelInstance> implements AddVirtualModelInstance {

		static final Logger logger = Logger.getLogger(AddVirtualModelInstance.class.getPackage().getName());

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
		public CompilationUnitResource getVirtualModelType() {
			if (getFlexoConceptType() instanceof VirtualModel) {
				return ((VirtualModel) getFlexoConceptType()).getResource();
			}
			return null;
		}

		@Override
		public void setVirtualModelType(CompilationUnitResource resource) {
			CreationScheme oldCreationScheme = getCreationScheme();
			CompilationUnitResource oldVMType = getVirtualModelType();
			try {
				setCreationScheme(null);
				setFlexoConceptType(resource.getResourceData().getVirtualModel());
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

		@Override
		public CompilationUnitResource getOwnerVirtualModelResource() {
			if (getReceiver().isSet() && getReceiver().isValid()) {
				Type type = getReceiver().getAnalyzedType();
				if (type instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) type).getVirtualModel().getResource();
				}
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getReceiver()) {
				getPropertyChangeSupport().firePropertyChange("ownerVirtualModelResource", null, getOwnerVirtualModelResource());
			}
		}

		@Override
		protected FMLRTVirtualModelInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			FMLRTVirtualModelInstance container = getVirtualModelInstance(evaluationContext);
			logger.info("container: " + container);
			if (container == null) {
				logger.warning("null container");
				return null;
			}
			if (evaluationContext instanceof FlexoBehaviourAction) {
				String name = null;
				String title = null;
				try {
					name = getVirtualModelInstanceName().getBindingValue(evaluationContext);
					title = getVirtualModelInstanceTitle().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

				VirtualModel instantiatedVirtualModel = (VirtualModel) retrieveFlexoConcept(evaluationContext);

				CreateBasicVirtualModelInstance createVMIAction = CreateBasicVirtualModelInstance.actionType
						.makeNewEmbeddedAction(container, null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				createVMIAction.setSkipChoosePopup(true);
				createVMIAction.setNewVirtualModelInstanceName(name);
				createVMIAction.setNewVirtualModelInstanceTitle(title);
				createVMIAction.setVirtualModel(instantiatedVirtualModel);
				// He we just want to create a PLAIN and EMPTY FMLRTVirtualModelInstance,
				// eventual CreationScheme will be executed later
				// DONT UNCOMMENT THIS !!!!
				/*if (getCreationScheme() != null) {
					createVMIAction.setCreationScheme(getCreationScheme());
				}*/
				createVMIAction.doAction();
				return createVMIAction.getNewVirtualModelInstance();
			}

			logger.warning("Unexpected RunTimeEvaluationContext");
			return null;

		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			compilationUnit.ensureImport(getVirtualModelType().getCompilationUnit().getVirtualModel());
		}
	}
}
