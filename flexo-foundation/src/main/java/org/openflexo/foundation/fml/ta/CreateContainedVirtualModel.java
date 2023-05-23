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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.ActionExecutionCancelledException;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
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
@ImplementationClass(CreateContainedVirtualModel.CreateContainedVirtualModelImpl.class)
@XMLElement
@FML("CreateContainedVirtualModel")
public interface CreateContainedVirtualModel extends TechnologySpecificActionDefiningReceiver<FMLModelSlot, VirtualModel, VirtualModel> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_NAME_KEY = "virtualModelName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";

	@Getter(value = VIRTUAL_MODEL_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getVirtualModelName();

	@Setter(VIRTUAL_MODEL_NAME_KEY)
	public void setVirtualModelName(DataBinding<String> virtualModelName);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<VirtualModel> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<VirtualModel> container);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateContainedVirtualModelImpl extends
			TechnologySpecificActionDefiningReceiverImpl<FMLModelSlot, VirtualModel, VirtualModel> implements CreateContainedVirtualModel {

		private static final Logger logger = Logger.getLogger(CreateContainedVirtualModel.class.getPackage().getName());

		private DataBinding<String> virtualModelName;
		private DataBinding<VirtualModel> container;

		private String getVirtualModelName(RunTimeEvaluationContext evaluationContext) {
			try {
				return getVirtualModelName().getBindingValue(evaluationContext);
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
		public DataBinding<String> getVirtualModelName() {
			if (virtualModelName == null) {
				virtualModelName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				virtualModelName.setBindingName("virtualModelName");
			}
			return virtualModelName;
		}

		@Override
		public void setVirtualModelName(DataBinding<String> virtualModelName) {
			if (virtualModelName != null) {
				virtualModelName.setOwner(this);
				virtualModelName.setBindingName("virtualModelName");
				virtualModelName.setDeclaredType(String.class);
				virtualModelName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.virtualModelName = virtualModelName;
		}

		@Override
		public Type getAssignableType() {
			return VirtualModel.class;
		}

		private VirtualModel getContainer(RunTimeEvaluationContext evaluationContext) {
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

		@Override
		public DataBinding<VirtualModel> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, VirtualModel.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<VirtualModel> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(VirtualModel.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public VirtualModel execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				String virtualModelName = getVirtualModelName(evaluationContext);
				VirtualModel container = getContainer(evaluationContext);

				logger.info("on cree un VirtualModel " + virtualModelName + " dans " + container);

				org.openflexo.foundation.fml.action.CreateContainedVirtualModel action = org.openflexo.foundation.fml.action.CreateContainedVirtualModel.actionType
						.makeNewEmbeddedAction(container.getCompilationUnit(), null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				action.setNewVirtualModelName(virtualModelName);
				action.setForceExecuteConfirmationPanel(getForceExecuteConfirmationPanel());
				action.doAction();

				if (action.hasBeenCancelled()) {
					throw new FMLExecutionException(new ActionExecutionCancelledException());
				}

				return action.getNewVirtualModel();
			}

			logger.warning("Unexpected context: " + evaluationContext);
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getVirtualModelName().rebuild();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateContainedVirtualModel> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", CreateContainedVirtualModel.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateContainedVirtualModel object) {
			return object.getContainer();
		}
	}

	@DefineValidationRule
	public static class VirtualModelNameBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<CreateContainedVirtualModel> {
		public VirtualModelNameBindingIsRequiredAndMustBeValid() {
			super("'virtualModel_name'_binding_is_not_valid", CreateContainedVirtualModel.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateContainedVirtualModel object) {
			return object.getVirtualModelName();
		}

	}

}
