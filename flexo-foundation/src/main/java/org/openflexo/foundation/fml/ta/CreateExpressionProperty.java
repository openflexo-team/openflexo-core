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
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.ActionExecutionCancelledException;
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
@ImplementationClass(CreateExpressionProperty.CreateExpressionPropertyImpl.class)
@XMLElement
public interface CreateExpressionProperty
		extends TechnologySpecificActionDefiningReceiver<FMLModelSlot, FlexoConcept, ExpressionProperty<?>> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String ROLE_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXPRESSION_KEY = "expression";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";

	@Getter(value = ROLE_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getPropertyName();

	@Setter(ROLE_NAME_KEY)
	public void setPropertyName(DataBinding<String> roleName);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConcept> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConcept> container);

	@Getter(value = EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<DataBinding<Object>> getExpression();

	@Setter(EXPRESSION_KEY)
	public void setExpression(DataBinding<DataBinding<Object>> expression);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateExpressionPropertyImpl
			extends TechnologySpecificActionDefiningReceiverImpl<FMLModelSlot, FlexoConcept, ExpressionProperty<?>>
			implements CreateExpressionProperty {

		private static final Logger logger = Logger.getLogger(CreateExpressionProperty.class.getPackage().getName());

		private DataBinding<String> propertyName;
		private DataBinding<FlexoConcept> container;
		private DataBinding<DataBinding<Object>> expression;

		private String getPropertyName(RunTimeEvaluationContext evaluationContext) {
			try {
				return getPropertyName().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
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
			}
			return null;
		}

		private DataBinding<Object> getExpression(RunTimeEvaluationContext evaluationContext) {
			try {
				return getExpression().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public DataBinding<String> getPropertyName() {
			if (propertyName == null) {
				propertyName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				propertyName.setBindingName("propertyName");
			}
			return propertyName;
		}

		@Override
		public void setPropertyName(DataBinding<String> propertyName) {
			if (propertyName != null) {
				propertyName.setOwner(this);
				propertyName.setBindingName("propertyName");
				propertyName.setDeclaredType(String.class);
				propertyName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.propertyName = propertyName;
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
		public DataBinding<DataBinding<Object>> getExpression() {
			if (expression == null) {
				expression = new DataBinding<>(this, DataBinding.class, BindingDefinitionType.GET);
				expression.setBindingName("expression");
			}
			return expression;
		}

		@Override
		public void setExpression(DataBinding<DataBinding<Object>> expression) {
			if (expression != null) {
				expression.setOwner(this);
				expression.setBindingName("expression");
				expression.setDeclaredType(DataBinding.class);
				expression.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.expression = expression;
		}

		@Override
		public Type getAssignableType() {
			return ExpressionProperty.class;
		}

		@Override
		public ExpressionProperty<?> execute(RunTimeEvaluationContext evaluationContext) throws ActionExecutionCancelledException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				String propertyName = getPropertyName(evaluationContext);
				FlexoConcept container = getContainer(evaluationContext);
				DataBinding<Object> expression = getExpression(evaluationContext);

				logger.info("on cree une ExpressionProperty " + propertyName + " dans " + container + " avec " + expression);
				logger.info("container=" + container);

				org.openflexo.foundation.fml.action.CreateExpressionProperty action = org.openflexo.foundation.fml.action.CreateExpressionProperty.actionType
						.makeNewEmbeddedAction(container, null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				action.setPropertyName(propertyName);
				action.setExpression(expression);
				action.setForceExecuteConfirmationPanel(getForceExecuteConfirmationPanel());
				action.doAction();

				if (action.hasBeenCancelled()) {
					throw new ActionExecutionCancelledException();
				}

				logger.info("return" + action.getNewFlexoProperty());

				return action.getNewFlexoProperty();
			}

			logger.warning("Unexpected context: " + evaluationContext);
			return null;
		}
	}

	@DefineValidationRule
	public static class RoleNameBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateExpressionProperty> {
		public RoleNameBindingIsRequiredAndMustBeValid() {
			super("'role_name'_binding_is_not_valid", CreateExpressionProperty.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateExpressionProperty object) {
			return object.getPropertyName();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateExpressionProperty> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", CreateExpressionProperty.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateExpressionProperty object) {
			return object.getContainer();
		}
	}

}
