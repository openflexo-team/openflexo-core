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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * Generic {@link AbstractFetchRequest} allowing to retrieve a selection of some {@link FMLRTVirtualModelInstance} matching some conditions
 * and a given {@link VirtualModel}.<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractSelectVirtualModelInstance.AbstractSelectVirtualModelInstanceImpl.class)
public interface AbstractSelectVirtualModelInstance<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>, AT>
		extends AbstractFetchRequest<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, VirtualModelInstance<?, ?>, AT> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_TYPE_URI_KEY = "virtualModelTypeURI";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance<?, ?>> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<VirtualModelInstance<?, ?>> container);

	@Getter(value = VIRTUAL_MODEL_TYPE_URI_KEY)
	@XMLAttribute
	public String _getVirtualModelTypeURI();

	@Setter(VIRTUAL_MODEL_TYPE_URI_KEY)
	public void _setVirtualModelTypeURI(String virtualModelTypeURI);

	public CompilationUnitResource getVirtualModelType();

	public void setVirtualModelType(CompilationUnitResource virtualModelType);

	public VirtualModel getAddressedVirtualModel();

	public static abstract class AbstractSelectVirtualModelInstanceImpl<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>, AT>
			extends AbstractFetchRequestImpl<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, VirtualModelInstance<?, ?>, AT>
			implements AbstractSelectVirtualModelInstance<VMI, AT> {

		protected static final Logger logger = FlexoLogger.getLogger(AbstractSelectVirtualModelInstance.class.getPackage().getName());

		private CompilationUnitResource virtualModelType;
		private String virtualModelTypeURI;

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return super.getModelSlotTechnologyAdapter();
		}

		private DataBinding<VirtualModelInstance<?, ?>> container;

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, VirtualModelInstance.class, DataBinding.BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<VirtualModelInstance<?, ?>> aContainer) {
			if (aContainer != null) {
				aContainer.setOwner(this);
				aContainer.setBindingName("container");
				aContainer.setDeclaredType(VirtualModelInstance.class);
				aContainer.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.container = aContainer;
			notifiedBindingChanged(container);
		}

		@Override
		public String getParametersStringRepresentation() {
			String whereClauses = getWhereClausesFMLRepresentation(null);
			return "(type=" + (getVirtualModelType() != null ? getVirtualModelType().getName() : "null")
					+ (whereClauses != null ? "," + whereClauses : "") + ")";
		}

		@Override
		public VirtualModel getAddressedVirtualModel() {
			if (getContainer() != null && getContainer().isSet() && getContainer().isValid()) {
				Type containerType = getContainer().getAnalyzedType();
				if (containerType instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) containerType).getVirtualModel();
				}
			}
			return null;
		}

		private boolean isFetching = false;

		@Override
		public VirtualModelInstanceType getFetchedType() {
			if (isFetching) {
				return VirtualModelInstanceType.UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;
			}
			isFetching = true;
			try {
				if (getVirtualModelType() != null) {
					if (getVirtualModelType().isLoaded()) {
						return VirtualModelInstanceType.getVirtualModelInstanceType(
								getVirtualModelType() != null ? getVirtualModelType().getLoadedResourceData().getVirtualModel() : null);
					}
					return new VirtualModelInstanceType(_getVirtualModelTypeURI(),
							getTechnologyAdapter().getVirtualModelInstanceTypeFactory());
				}
			} finally {
				isFetching = false;
			}
			if (getTechnologyAdapter() != null && StringUtils.isNotEmpty(_getVirtualModelTypeURI())) {
				return new VirtualModelInstanceType(_getVirtualModelTypeURI(), getTechnologyAdapter().getVirtualModelInstanceTypeFactory());
			}
			return VirtualModelInstanceType.UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;
		}

		@Override
		public String _getVirtualModelTypeURI() {
			if (virtualModelType != null) {
				return virtualModelType.getURI();
			}
			return virtualModelTypeURI;
		}

		@Override
		public void _setVirtualModelTypeURI(String virtualModelTypeURI) {
			this.virtualModelTypeURI = virtualModelTypeURI;
		}

		@Override
		public CompilationUnitResource getVirtualModelType() {
			if (virtualModelType == null && virtualModelTypeURI != null && getAddressedVirtualModel() != null) {
				VirtualModel vm = getAddressedVirtualModel().getVirtualModelNamed(virtualModelTypeURI);
				if (vm != null) {
					virtualModelType = vm.getResource();
				}
				/*else {
					logger.warning("?????????????????? je trouve pas " + virtualModelTypeURI);
					try {
						if (getAddressedVirtualModel() != null && getAddressedVirtualModel().getVirtualModelLibrary() != null) {
							System.out.println("Et: "
									+ getAddressedVirtualModel().getVirtualModelLibrary().getVirtualModel(virtualModelTypeURI, true));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (ResourceLoadingCancelledException e) {
						e.printStackTrace();
					} catch (FlexoException e) {
						e.printStackTrace();
					}
				}*/
			}

			return virtualModelType;
		}

		@Override
		public void setVirtualModelType(CompilationUnitResource virtualModelType) {
			if (virtualModelType != this.virtualModelType) {
				CompilationUnitResource oldValue = this.virtualModelType;
				this.virtualModelType = virtualModelType;
				getPropertyChangeSupport().firePropertyChange("virtualModelType", oldValue, virtualModelType);
			}
		}

		public VirtualModelInstance<?, ?> getContainer(RunTimeEvaluationContext evaluationContext) {
			if (getContainer() != null && getContainer().isSet() && getContainer().isValid()) {
				try {
					return getContainer().getBindingValue(evaluationContext);
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
		public List<VirtualModelInstance<?, ?>> performExecute(RunTimeEvaluationContext evaluationContext) {
			VirtualModelInstance<?, ?> container = getContainer(evaluationContext);
			if (container != null) {
				try {
					return filterWithConditions(
							container.getVirtualModelInstancesForVirtualModel(getVirtualModelType().getResourceData().getVirtualModel()),
							evaluationContext);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
				return null;
			}
			logger.warning(getStringRepresentation() + " : Cannot find view on which to apply SelectVirtualModelInstance");
			logger.warning("Additional info: getContainer()=" + getContainer());
			return null;
		}
	}

	@DefineValidationRule
	public static class SelectVirtualModelInstanceMustAddressAFlexoConceptType
			extends ValidationRule<SelectVirtualModelInstanceMustAddressAFlexoConceptType, AbstractSelectVirtualModelInstance<?, ?>> {
		public SelectVirtualModelInstanceMustAddressAFlexoConceptType() {
			super(AbstractSelectVirtualModelInstance.class, "select_virtual_model_instance_action_must_address_a_valid_virtual_model_type");
		}

		@Override
		public ValidationIssue<SelectVirtualModelInstanceMustAddressAFlexoConceptType, AbstractSelectVirtualModelInstance<?, ?>> applyValidation(
				AbstractSelectVirtualModelInstance<?, ?> action) {
			if (action.getVirtualModelType() == null) {
				return new ValidationError<>(this, action, "select_virtual_model_instance_action_doesn't_define_any_virtual_model_type");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<AbstractSelectVirtualModelInstance> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", AbstractSelectVirtualModelInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getBinding(AbstractSelectVirtualModelInstance object) {
			return object.getContainer();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractSelectVirtualModelInstance>, AbstractSelectVirtualModelInstance> applyValidation(
				AbstractSelectVirtualModelInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractSelectVirtualModelInstance>, AbstractSelectVirtualModelInstance> returned = super.applyValidation(
					object);
			return returned;
		}

	}

}
