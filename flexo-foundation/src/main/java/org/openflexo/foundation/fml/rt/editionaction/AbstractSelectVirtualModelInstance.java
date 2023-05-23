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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType.DefaultVirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
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
	@Deprecated
	public String _getVirtualModelTypeURI();

	@Setter(VIRTUAL_MODEL_TYPE_URI_KEY)
	@Deprecated
	public void _setVirtualModelTypeURI(String virtualModelTypeURI);

	public VirtualModel getVirtualModelType();

	public void setVirtualModelType(VirtualModel virtualModel);

	// public CompilationUnitResource getCompilationUnitResource();

	// public void setCompilationUnitResource(CompilationUnitResource resource);

	public VirtualModel getAddressedVirtualModel();

	public VirtualModelInstanceType getType();

	public void setType(VirtualModelInstanceType type);

	public static abstract class AbstractSelectVirtualModelInstanceImpl<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>, AT>
			extends AbstractFetchRequestImpl<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, VirtualModelInstance<?, ?>, AT>
			implements AbstractSelectVirtualModelInstance<VMI, AT> {

		protected static final Logger logger = FlexoLogger.getLogger(AbstractSelectVirtualModelInstance.class.getPackage().getName());

		private VirtualModelInstanceType type;

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
			String whereClauses = getWhereClausesFMLRepresentation();
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

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			getContainer().revalidate();
			getVirtualModelType();
		}

		@Override
		public VirtualModelInstanceType getFetchedType() {

			return getType();
		}

		@Override
		public String _getVirtualModelTypeURI() {
			if (type != null) {
				return type.getConceptURI();
			}
			return null;
		}

		private boolean isFetching = false;

		@Override
		public void _setVirtualModelTypeURI(String virtualModelTypeURI) {
			// this.virtualModelTypeURI = virtualModelTypeURI;
			type = new VirtualModelInstanceType(virtualModelTypeURI, new DefaultVirtualModelInstanceTypeFactory(getTechnologyAdapter()) {
				@Override
				public VirtualModel resolveVirtualModel(VirtualModelInstanceType typeToResolve) {
					if (!isFetching && getAddressedVirtualModel() != null) {
						isFetching = true;
						VirtualModel virtualModelType = getAddressedVirtualModel().getVirtualModelNamed(typeToResolve.getConceptURI());
						isFetching = false;
						return virtualModelType;
					}
					return null;
				}
			});
		}

		@Override
		public VirtualModel getVirtualModelType() {

			if (type != null) {
				if (!type.isResolved()) {
					type.resolve();
				}
				if (type.getVirtualModel() != null) {
					return type.getVirtualModel();
				}
			}
			return null;
		}

		@Override
		public void setVirtualModelType(VirtualModel virtualModelType) {
			if (virtualModelType != getVirtualModelType()) {
				VirtualModel oldValue = getVirtualModelType();
				if (virtualModelType != null) {
					type = virtualModelType.getVirtualModelInstanceType();
				}
				else {
					type = null;
				}

				getPropertyChangeSupport().firePropertyChange("virtualModelType", oldValue, virtualModelType);
			}
		}

		@Override
		public VirtualModelInstanceType getType() {
			return type;
		}

		@Override
		public void setType(VirtualModelInstanceType type) {
			if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
				VirtualModelInstanceType oldValue = this.type;
				this.type = type;
				getPropertyChangeSupport().firePropertyChange("type", oldValue, type);
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
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;

		}

		@Override
		public List<VirtualModelInstance<?, ?>> performExecute(RunTimeEvaluationContext evaluationContext) {
			VirtualModelInstance<?, ?> container = getContainer(evaluationContext);
			if (container != null) {
				return filterWithConditions(container.getVirtualModelInstancesForVirtualModel(getVirtualModelType()), evaluationContext);
			}
			logger.warning(getStringRepresentation() + " : Cannot find view on which to apply SelectVirtualModelInstance");
			logger.warning("Additional info: getContainer()=" + getContainer());
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
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
