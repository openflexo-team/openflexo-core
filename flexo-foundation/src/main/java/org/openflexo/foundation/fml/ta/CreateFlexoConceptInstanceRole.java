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

package org.openflexo.foundation.fml.ta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.ActionExecutionCancelledException;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(CreateFlexoConceptInstanceRole.CreateFlexoConceptInstanceRoleImpl.class)
@XMLElement
@FML("CreateFlexoConceptInstanceRole")
public interface CreateFlexoConceptInstanceRole
		extends TechnologySpecificActionDefiningReceiver<FMLModelSlot, VirtualModel, FlexoConceptInstanceRole> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String FLEXO_CONCEPT_TYPE_KEY = "flexoConceptType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_AS_TYPE_KEY = "virtualModelInstanceAsString";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";

	@Getter(value = ROLE_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getRoleName();

	@Setter(ROLE_NAME_KEY)
	public void setRoleName(DataBinding<String> roleName);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConcept> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConcept> container);

	@Getter(value = FLEXO_CONCEPT_TYPE_KEY)
	@XMLAttribute
	public DataBinding<FlexoConcept> getFlexoConceptType();

	@Setter(FLEXO_CONCEPT_TYPE_KEY)
	public void setFlexoConceptType(DataBinding<FlexoConcept> flexoConceptType);

	@Getter(value = VIRTUAL_MODEL_INSTANCE_AS_TYPE_KEY)
	@XMLAttribute
	public DataBinding<String> getVirtualModelInstanceAsString();

	@Setter(VIRTUAL_MODEL_INSTANCE_AS_TYPE_KEY)
	public void setVirtualModelInstanceAsString(DataBinding<String> vmiAsString);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateFlexoConceptInstanceRoleImpl
			extends TechnologySpecificActionDefiningReceiverImpl<FMLModelSlot, VirtualModel, FlexoConceptInstanceRole>
			implements CreateFlexoConceptInstanceRole {

		private static final Logger logger = Logger.getLogger(CreateFlexoConceptInstanceRole.class.getPackage().getName());

		private DataBinding<String> roleName;
		private DataBinding<FlexoConcept> container;
		private DataBinding<FlexoConcept> flexoConceptType;
		private DataBinding<String> virtualModelInstanceAsString;

		private String getRoleName(RunTimeEvaluationContext evaluationContext) {
			try {
				return getRoleName().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		private FlexoConcept getFlexoConceptType(RunTimeEvaluationContext evaluationContext) {
			try {
				return getFlexoConceptType().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		private FlexoConcept getContainer(RunTimeEvaluationContext evaluationContext) {
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
			return null;
		}

		private String getVirtualModelInstanceAsString(RunTimeEvaluationContext evaluationContext) {
			try {
				return getVirtualModelInstanceAsString().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public DataBinding<String> getRoleName() {
			if (roleName == null) {
				roleName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				roleName.setBindingName("roleName");
			}
			return roleName;
		}

		@Override
		public void setRoleName(DataBinding<String> roleName) {
			if (roleName != null) {
				roleName.setOwner(this);
				roleName.setBindingName("roleName");
				roleName.setDeclaredType(String.class);
				roleName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.roleName = roleName;
		}

		@Override
		public DataBinding<FlexoConcept> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, FlexoConcept.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<FlexoConcept> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(FlexoConcept.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public DataBinding<String> getVirtualModelInstanceAsString() {
			if (virtualModelInstanceAsString == null) {
				virtualModelInstanceAsString = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				virtualModelInstanceAsString.setBindingName("virtualModelInstanceAsString");
			}
			return virtualModelInstanceAsString;
		}

		@Override
		public void setVirtualModelInstanceAsString(DataBinding<String> virtualModelInstanceAsString) {
			if (virtualModelInstanceAsString != null) {
				virtualModelInstanceAsString.setOwner(this);
				virtualModelInstanceAsString.setBindingName("virtualModelInstanceAsString");
				virtualModelInstanceAsString.setDeclaredType(String.class);
				virtualModelInstanceAsString.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.virtualModelInstanceAsString = virtualModelInstanceAsString;
		}

		@Override
		public DataBinding<FlexoConcept> getFlexoConceptType() {
			if (flexoConceptType == null) {
				flexoConceptType = new DataBinding<>(this, FlexoConcept.class, BindingDefinitionType.GET);
				flexoConceptType.setBindingName("flexoConceptType");
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(DataBinding<FlexoConcept> flexoConceptType) {
			if (flexoConceptType != null) {
				flexoConceptType.setOwner(this);
				flexoConceptType.setBindingName("flexoConceptType");
				flexoConceptType.setDeclaredType(FlexoConcept.class);
				flexoConceptType.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.flexoConceptType = flexoConceptType;
		}

		@Override
		public Type getAssignableType() {
			return PrimitiveRole.class;
		}

		@Override
		public FlexoConceptInstanceRole execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				String roleName = getRoleName(evaluationContext);
				FlexoConcept container = getContainer(evaluationContext);
				FlexoConcept flexoConceptType = getFlexoConceptType(evaluationContext);
				String virtualModelInstanceAsString = getVirtualModelInstanceAsString(evaluationContext);

				logger.info("on cree une property " + roleName + " dans " + container);
				logger.info("container=" + container);
				logger.info("flexoConceptType=" + flexoConceptType);
				logger.info("virtualModelInstanceAsString=" + virtualModelInstanceAsString);

				org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole action = org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(container, null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				action.setRoleName(roleName);
				if (StringUtils.isNotEmpty(virtualModelInstanceAsString)) {
					action.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>(virtualModelInstanceAsString));
				}
				action.setFlexoConceptInstanceType(flexoConceptType);
				action.setCardinality(PropertyCardinality.ZeroOne);
				action.setForceExecuteConfirmationPanel(getForceExecuteConfirmationPanel());
				action.doAction();

				if (action.hasBeenCancelled()) {
					throw new FMLExecutionException(new ActionExecutionCancelledException());
				}

				logger.info("return" + action.getNewFlexoRole());

				return action.getNewFlexoRole();
			}

			logger.warning("Unexpected context: " + evaluationContext);
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getFlexoConceptType().rebuild();
			getVirtualModelInstanceAsString().rebuild();
			getRoleName().rebuild();
		}

	}

	@DefineValidationRule
	public static class RoleNameBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoConceptInstanceRole> {
		public RoleNameBindingIsRequiredAndMustBeValid() {
			super("'role_name'_binding_is_not_valid", CreateFlexoConceptInstanceRole.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoConceptInstanceRole object) {
			return object.getRoleName();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoConceptInstanceRole> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", CreateFlexoConceptInstanceRole.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoConceptInstanceRole object) {
			return object.getContainer();
		}
	}

	@DefineValidationRule
	public static class FlexoConceptTypeTypeBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<CreateFlexoConceptInstanceRole> {
		public FlexoConceptTypeTypeBindingIsRequiredAndMustBeValid() {
			super("'flexo_concept_type'_binding_is_not_valid", CreateFlexoConceptInstanceRole.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoConceptInstanceRole object) {
			return object.getFlexoConceptType();
		}
	}

}
