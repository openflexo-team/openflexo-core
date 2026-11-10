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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.ActionExecutionCancelledException;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;

@ModelEntity
@ImplementationClass(CreateFlexoConcept.CreateFlexoConceptImpl.class)
@FML("CreateFlexoConcept")
public interface CreateFlexoConcept extends TechnologySpecificActionDefiningReceiver<FMLModelSlot, VirtualModel, FlexoConcept> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "conceptName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";
	@PropertyIdentifier(type = FlexoConceptInstanceType.class)
	public static final String PARENT_TYPE_KEY = "parentType";

	@Getter(value = VALUE_KEY)
	@FMLAttribute(value = VALUE_KEY, required = true, description = "<html>name of the new concept</html>")
	public DataBinding<String> getConceptName();

	@Setter(VALUE_KEY)
	public void setConceptName(DataBinding<String> conceptName);

	@Getter(value = CONTAINER_KEY)
	@FMLAttribute(value = CONTAINER_KEY, required = true, description = "<html>concept (or VirtualModel) that will contain the new concept</html>")
	public DataBinding<FlexoConcept> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConcept> container);

	/**
	 * The type of the parent of the created concept (see {@link #getParentFlexoConceptType()})
	 *
	 * @return the parent type, null when the created concept has no parent
	 */
	@Getter(value = PARENT_TYPE_KEY, ignoreType = true)
	public FlexoConceptInstanceType getParentType();

	@Setter(PARENT_TYPE_KEY)
	public void setParentType(FlexoConceptInstanceType parentType);

	/**
	 * Translates the supplied type into the typing space of this action, then sets it
	 *
	 * @param parentType
	 *            the parent type, possibly expressed in another typing space
	 */
	@Updater(PARENT_TYPE_KEY)
	public void updateParentType(FlexoConceptInstanceType parentType);

	public FlexoConcept getParentFlexoConceptType();

	public void setParentFlexoConceptType(FlexoConcept flexoConceptType);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateFlexoConceptImpl
			extends TechnologySpecificActionDefiningReceiverImpl<FMLModelSlot, VirtualModel, FlexoConcept> implements CreateFlexoConcept {

		private static final Logger logger = Logger.getLogger(CreateFlexoConcept.class.getPackage().getName());

		private DataBinding<String> conceptName;
		private DataBinding<FlexoConcept> container;

		private FlexoConceptInstanceType parentType;

		private String getConceptName(RunTimeEvaluationContext evaluationContext) {
			try {
				return getConceptName().getBindingValue(evaluationContext);
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

		@Override
		public DataBinding<String> getConceptName() {
			if (conceptName == null) {
				conceptName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				conceptName.setBindingName("conceptName");
			}
			return conceptName;
		}

		@Override
		public void setConceptName(DataBinding<String> conceptName) {
			if (conceptName != null) {
				conceptName.setOwner(this);
				conceptName.setBindingName("conceptName");
				conceptName.setDeclaredType(String.class);
				conceptName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.conceptName = conceptName;
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
		public FlexoConceptInstanceType getParentType() {
			return parentType;
		}

		@Override
		public void setParentType(FlexoConceptInstanceType parentType) {
			if (parentType != this.parentType) {
				FlexoConceptInstanceType oldValue = this.parentType;
				this.parentType = parentType;
				getPropertyChangeSupport().firePropertyChange(PARENT_TYPE_KEY, oldValue, parentType);
			}
		}

		@Override
		public void updateParentType(FlexoConceptInstanceType parentType) {
			if (parentType != null && getDeclaringCompilationUnit() != null) {
				setParentType(parentType.translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setParentType(parentType);
			}
		}

		@Override
		public FlexoConcept getParentFlexoConceptType() {
			if (parentType == null) {
				return null;
			}
			if (!parentType.isResolved()) {
				parentType.resolve();
			}
			return parentType.getFlexoConcept();
		}

		@Override
		public void setParentFlexoConceptType(FlexoConcept flexoConceptType) {
			FlexoConcept oldValue = getParentFlexoConceptType();
			if (flexoConceptType != oldValue) {
				setParentType(flexoConceptType != null ? flexoConceptType.getInstanceType() : null);
				getPropertyChangeSupport().firePropertyChange("parentFlexoConceptType", oldValue, flexoConceptType);
			}
		}

		@Override
		public Type getAssignableType() {
			return FlexoConcept.class;
		}

		@Override
		public FlexoConcept execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				String conceptName = getConceptName(evaluationContext);
				FlexoConcept container = getContainer(evaluationContext);

				logger.info("on cree un FlexoConcept " + conceptName + " dans " + container);

				org.openflexo.foundation.fml.action.CreateFlexoConcept action = org.openflexo.foundation.fml.action.CreateFlexoConcept.actionType
						.makeNewEmbeddedAction(container, null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				action.setNewFlexoConceptName(conceptName);
				action.setForceExecuteConfirmationPanel(getForceExecuteConfirmationPanel());
				action.doAction();

				if (action.hasBeenCancelled()) {
					throw new FMLExecutionException(new ActionExecutionCancelledException());
				}

				if (getParentFlexoConceptType() != null) {
					try {
						action.getNewFlexoConcept().addToParentFlexoConcepts(getParentFlexoConceptType());
					} catch (InconsistentFlexoConceptHierarchyException e) {
						throw new FMLExecutionException(e);
					}
				}

				return action.getNewFlexoConcept();
			}

			logger.warning("Unexpected context: " + evaluationContext);
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getConceptName().rebuild();
		}

	}

	@DefineValidationRule
	public static class ConceptNameBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoConcept> {
		public ConceptNameBindingIsRequiredAndMustBeValid() {
			super("'concept_name'_binding_is_not_valid", CreateFlexoConcept.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoConcept object) {
			return object.getConceptName();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoConcept> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", CreateFlexoConcept.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoConcept object) {
			return object.getContainer();
		}
	}

}
