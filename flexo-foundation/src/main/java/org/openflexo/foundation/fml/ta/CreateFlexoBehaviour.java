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
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.CreateActionScheme;
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
@ImplementationClass(CreateFlexoBehaviour.CreateFlexoBehaviourImpl.class)
@XMLElement
@FML("CreateFlexoBehaviour")
public interface CreateFlexoBehaviour<B extends FlexoBehaviour>
		extends TechnologySpecificActionDefiningReceiver<FMLModelSlot, VirtualModel, B> {

	public enum BehaviourType {
		ActionScheme, CreationScheme, DeletionScheme, EventListener, NavigationScheme, SynchronizationScheme, CloningScheme
	}

	@PropertyIdentifier(type = DataBinding.class)
	public static final String BEHAVIOUR_NAME_KEY = "behaviourName";
	@PropertyIdentifier(type = BehaviourType.class)
	public static final String BEHAVIOUR_TYPE_KEY = "behaviourType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";

	@Getter(value = BEHAVIOUR_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getBehaviourName();

	@Setter(BEHAVIOUR_NAME_KEY)
	public void setBehaviourName(DataBinding<String> behaviourName);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConcept> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConcept> container);

	@Getter(value = BEHAVIOUR_TYPE_KEY)
	@XMLAttribute
	public BehaviourType getBehaviourType();

	@Setter(BEHAVIOUR_TYPE_KEY)
	public void setBehaviourType(BehaviourType behaviourType);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateFlexoBehaviourImpl<B extends FlexoBehaviour>
			extends TechnologySpecificActionDefiningReceiverImpl<FMLModelSlot, VirtualModel, B> implements CreateFlexoBehaviour<B> {

		private static final Logger logger = Logger.getLogger(CreateFlexoBehaviour.class.getPackage().getName());

		private DataBinding<String> behaviourName;
		private DataBinding<FlexoConcept> container;
		private BehaviourType behaviourType = BehaviourType.ActionScheme;

		private String getBehaviourName(RunTimeEvaluationContext evaluationContext) {
			try {
				return getBehaviourName().getBindingValue(evaluationContext);
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
		public DataBinding<String> getBehaviourName() {
			if (behaviourName == null) {
				behaviourName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				behaviourName.setBindingName("behaviourName");
			}
			return behaviourName;
		}

		@Override
		public void setBehaviourName(DataBinding<String> behaviourName) {
			if (behaviourName != null) {
				behaviourName.setOwner(this);
				behaviourName.setBindingName("behaviourName");
				behaviourName.setDeclaredType(String.class);
				behaviourName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.behaviourName = behaviourName;
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
		public BehaviourType getBehaviourType() {
			return behaviourType;
		}

		@Override
		public void setBehaviourType(BehaviourType behaviourType) {
			if ((behaviourType == null && this.behaviourType != null)
					|| (behaviourType != null && !behaviourType.equals(this.behaviourType))) {
				BehaviourType oldValue = this.behaviourType;
				this.behaviourType = behaviourType;
				getPropertyChangeSupport().firePropertyChange("behaviourType", oldValue, behaviourType);
			}
		}

		@Override
		public Type getAssignableType() {
			return FlexoBehaviour.class;
		}

		@Override
		public B execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				String behaviourName = getBehaviourName(evaluationContext);
				FlexoConcept container = getContainer(evaluationContext);

				logger.info("on cree une FlexoBehaviour " + behaviourName + " de type " + getBehaviourType() + " dans " + container);

				org.openflexo.foundation.fml.action.CreateFlexoBehaviour action = null;
				switch (getBehaviourType()) {
					case ActionScheme:
						action = CreateActionScheme.createActionSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case CreationScheme:
						action = CreateActionScheme.createCreationSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case DeletionScheme:
						action = CreateActionScheme.createDeletionSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case EventListener:
						action = CreateActionScheme.createEventListenerType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case NavigationScheme:
						action = CreateActionScheme.createNavigationSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case CloningScheme:
						action = CreateActionScheme.createCloningSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
					case SynchronizationScheme:
						action = CreateActionScheme.createSynchronizationSchemeType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						break;
				}

				action.setFlexoBehaviourName(behaviourName);
				action.setForceExecuteConfirmationPanel(getForceExecuteConfirmationPanel());
				action.doAction();

				if (action.hasBeenCancelled()) {
					throw new FMLExecutionException(new ActionExecutionCancelledException());
				}

				return (B) action.getNewFlexoBehaviour();
			}

			logger.warning("Unexpected context: " + evaluationContext);
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getBehaviourName().rebuild();
		}

	}

	@DefineValidationRule
	public static class BehaviourNameBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoBehaviour> {
		public BehaviourNameBindingIsRequiredAndMustBeValid() {
			super("'role_name'_binding_is_not_valid", CreateFlexoBehaviour.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoBehaviour object) {
			return object.getBehaviourName();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<CreateFlexoBehaviour> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", CreateFlexoBehaviour.class);
		}

		@Override
		public DataBinding<?> getBinding(CreateFlexoBehaviour object) {
			return object.getContainer();
		}
	}

}
