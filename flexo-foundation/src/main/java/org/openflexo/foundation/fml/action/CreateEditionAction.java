/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.action;

import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.ControlStructureAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.localization.FlexoLocalization;

public class CreateEditionAction extends FlexoAction<CreateEditionAction, FMLControlGraph, FMLObject> implements Bindable {

	private static final Logger logger = Logger.getLogger(CreateEditionAction.class.getPackage().getName());

	public static FlexoActionType<CreateEditionAction, FMLControlGraph, FMLObject> actionType = new FlexoActionType<CreateEditionAction, FMLControlGraph, FMLObject>(
			"create_edition_action", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateEditionAction makeNewAction(FMLControlGraph focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateEditionAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLControlGraph object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLControlGraph object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateEditionAction.actionType, FMLControlGraph.class);
		// FlexoObjectImpl.addActionForClass(CreateEditionAction.actionType, EditionAction.class);
	}

	public static enum CreateEditionActionChoice {
		BuiltInAction, ModelSlotSpecificAction, RequestAction, ControlAction
	}

	public static enum LayoutChoice {
		InsertAfter, InsertBefore, InsertInside;
	}

	public String description;
	public CreateEditionActionChoice actionChoice = CreateEditionActionChoice.BuiltInAction;
	private LayoutChoice layoutChoice;
	private ModelSlot<?> modelSlot;
	private Class<? extends EditionAction> builtInActionClass;
	private Class<? extends ControlStructureAction> controlActionClass;
	private Class<? extends TechnologySpecificAction<?, ?>> modelSlotSpecificActionClass;
	private Class<? extends FetchRequest<?, ?>> requestActionClass;

	private EditionAction newEditionAction;

	private final List<Class<? extends EditionAction>> builtInActions;
	private final List<Class<? extends ControlStructureAction>> controlActions;

	CreateEditionAction(FMLControlGraph focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

		builtInActions = new ArrayList<Class<? extends EditionAction>>();
		builtInActions.add(org.openflexo.foundation.fml.editionaction.ExpressionAction.class);
		builtInActions.add(org.openflexo.foundation.fml.editionaction.AssignationAction.class);
		builtInActions.add(org.openflexo.foundation.fml.editionaction.DeclarationAction.class);
		builtInActions.add(org.openflexo.foundation.fml.editionaction.AddToListAction.class);
		builtInActions.add(org.openflexo.foundation.fml.editionaction.RemoveFromListAction.class);
		// builtInActions.add(org.openflexo.foundation.fml.editionaction.ExecutionAction.class);
		// builtInActions.add(org.openflexo.foundation.fml.editionaction.DeclareFlexoRole.class);
		builtInActions.add(org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance.class);
		builtInActions.add(org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance.class);
		builtInActions.add(org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance.class);
		builtInActions.add(DeleteAction.class);

		controlActions = new ArrayList<Class<? extends ControlStructureAction>>();
		controlActions.add(ConditionalAction.class);
		controlActions.add(IterationAction.class);
		// controlActions.add(FetchRequestIterationAction.class);

		// If the model slot is empty, then now it is the currentVirtualModel that is referenced
		/*
		System.out.println("focusedObject=" + focusedObject);
		System.out.println("focusedObject.getVirtualModel()=" + focusedObject.getVirtualModel());
		if (modelSlot == null && !focusedObject.getVirtualModel().getModelSlots().isEmpty()) {
			modelSlot = focusedObject.getVirtualModel().getModelSlots().get(0);
		}
		*/
	}

	public List<Class<? extends EditionAction>> getBuiltInActions() {
		return builtInActions;
	}

	public List<Class<? extends ControlStructureAction>> getControlActions() {
		return controlActions;
	}

	public List<Class<? extends EditionAction>> getModelSlotSpecificActions() {
		if (modelSlot != null) {
			return modelSlot.getAvailableEditionActionTypes();
		} else {
			// TODO : when modelSlot is null, return AvailableEditionActionTypes for VirtualModel
		}
		return null;
	}

	public List<Class<? extends EditionAction>> getRequestActions() {
		if (modelSlot != null) {
			return modelSlot.getAvailableFetchRequestActionTypes();
		}
		return null;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add edition action, modelSlot=" + modelSlot + " actionChoice=" + actionChoice);

		EditionAction baseEditionAction = makeEditionAction();

		if (baseEditionAction instanceof AssignableAction) {
			if (getAssignation() != null && getAssignation().isSet()) {
				AssignationAction<?> newAssignationAction = getFocusedObject().getFMLModelFactory().newAssignationAction();
				newAssignationAction.setAssignableAction((AssignableAction) baseEditionAction);
				newAssignationAction.setAssignation((DataBinding) getAssignation());
				newEditionAction = newAssignationAction;
			} else if (getDeclarationVariableName() != null) {
				DeclarationAction<?> newDeclarationAction = getFocusedObject().getFMLModelFactory().newDeclarationAction();
				newDeclarationAction.setAssignableAction((AssignableAction) baseEditionAction);
				newDeclarationAction.setVariableName(getDeclarationVariableName());
				newEditionAction = newDeclarationAction;
			}
		}
		if (newEditionAction == null) {
			newEditionAction = baseEditionAction;
		}

		if (newEditionAction != null) {
			getFocusedObject().sequentiallyAppend(newEditionAction);
		}

		else {
			throw new InvalidParameterException("cannot build EditionAction");
		}

	}

	public EditionAction getNewEditionAction() {
		return newEditionAction;
	}

	private String validityMessage = NO_ACTION_TYPE_SELECTED;

	private static final String NO_MODEL_SLOT = FlexoLocalization.localizedForKey("please_choose_a_model_slot");
	private static final String NO_ACTION_TYPE_SELECTED = FlexoLocalization.localizedForKey("please_select_an_action_type");

	public String getValidityMessage() {
		return validityMessage;
	}

	@Override
	public boolean isValid() {
		switch (actionChoice) {
		case BuiltInAction:
			if (builtInActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case ControlAction:
			if (controlActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case ModelSlotSpecificAction:
			if (modelSlot == null) {
				validityMessage = NO_MODEL_SLOT;
				return false;
			}
			if (modelSlotSpecificActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case RequestAction:
			if (modelSlot == null) {
				validityMessage = NO_MODEL_SLOT;
				return false;
			}
			if (requestActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;

		default:
			return false;
		}

	}

	private EditionAction makeEditionAction() {
		EditionAction returned;
		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		switch (actionChoice) {
		case BuiltInAction:
			if (builtInActionClass == null) {
				logger.warning("Unexpected " + builtInActionClass);
				return null;
			}
			if (org.openflexo.foundation.fml.editionaction.AssignationAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newAssignationAction();
			} else if (org.openflexo.foundation.fml.editionaction.ExpressionAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newExpressionAction();
			} else if (org.openflexo.foundation.fml.editionaction.AddToListAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newAddToListAction();
			} else if (org.openflexo.foundation.fml.editionaction.RemoveFromListAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newRemoveFromListAction();
			} /*else if (org.openflexo.foundation.fml.editionaction.ExecutionAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newExecutionAction();
				} else if (org.openflexo.foundation.fml.editionaction.DeclareFlexoRole.class.isAssignableFrom(builtInActionClass)) {
				return factory.newDeclareFlexoRole();
				}*/else if (AddFlexoConceptInstance.class.isAssignableFrom(builtInActionClass)) {
				return factory.newAddFlexoConceptInstance();
			} else if (MatchFlexoConceptInstance.class.isAssignableFrom(builtInActionClass)) {
				return factory.newMatchFlexoConceptInstance();
			} else if (SelectFlexoConceptInstance.class.isAssignableFrom(builtInActionClass)) {
				return factory.newSelectFlexoConceptInstance();
			} else if (DeleteAction.class.isAssignableFrom(builtInActionClass)) {
				return factory.newDeleteAction();
			} else {
				logger.warning("Unexpected " + builtInActionClass);
				return null;
			}
		case ControlAction:
			if (controlActionClass == null) {
				logger.warning("Unexpected " + controlActionClass);
				return null;
			}
			if (ConditionalAction.class.isAssignableFrom(controlActionClass)) {
				return factory.newConditionalAction();
			} else if (IterationAction.class.isAssignableFrom(controlActionClass)) {
				return factory.newIterationAction();
			} /*else if (FetchRequestIterationAction.class.isAssignableFrom(controlActionClass) && requestActionClass != null) {
				returned = factory.newFetchRequestIterationAction();
				FetchRequest request = null;
				if (modelSlot != null) {
					request = modelSlot.makeFetchRequest(requestActionClass);
					request.setModelSlot(modelSlot);
					returned.setModelSlot(modelSlot);
				} else if (SelectFlexoConceptInstance.class.isAssignableFrom(requestActionClass)) {
					request = factory.newSelectFlexoConceptInstanceAction();
				}
				if (request != null) {
					((FetchRequestIterationAction) returned).setFetchRequest(request);
				}
				return returned;
				} else {
				logger.warning("Unexpected " + controlActionClass);
				return null;
				}*/
		case ModelSlotSpecificAction:
			if (modelSlotSpecificActionClass != null && modelSlot != null) {
				returned = modelSlot.makeEditionAction(modelSlotSpecificActionClass);
				((TechnologySpecificAction) returned).setModelSlot(modelSlot);
				return returned;
			}
			break;
		case RequestAction:
			if (SelectFlexoConceptInstance.class.isAssignableFrom(requestActionClass)) {
				return factory.newSelectFlexoConceptInstanceAction();
			} else if (requestActionClass != null && modelSlot != null) {
				returned = modelSlot.makeFetchRequest(requestActionClass);
				((FetchRequest) returned).setModelSlot(modelSlot);
				return returned;
			}

		default:
			break;
		}

		logger.warning("Cannot build EditionAction");
		return null;

	}

	public LayoutChoice getLayoutChoice() {
		if (layoutChoice == null) {
			if (getFocusedObject() instanceof ActionContainer) {
				return LayoutChoice.InsertInside;
			}
			return LayoutChoice.InsertAfter;
		}
		return layoutChoice;
	}

	public void setLayoutChoice(LayoutChoice layoutChoice) {
		this.layoutChoice = layoutChoice;
	}

	public ModelSlot<?> getModelSlot() {
		return modelSlot;
	}

	public void setModelSlot(ModelSlot<?> modelSlot) {
		this.modelSlot = modelSlot;
	}

	public Class<? extends EditionAction> getBuiltInActionClass() {
		return builtInActionClass;
	}

	public void setBuiltInActionClass(Class<? extends EditionAction> builtInActionClass) {
		this.builtInActionClass = builtInActionClass;
	}

	public Class<? extends EditionAction> getControlActionClass() {
		return controlActionClass;
	}

	public void setControlActionClass(Class<? extends ControlStructureAction> controlActionClass) {
		this.controlActionClass = controlActionClass;
	}

	public Class<? extends EditionAction> getModelSlotSpecificActionClass() {
		return modelSlotSpecificActionClass;
	}

	public void setModelSlotSpecificActionClass(Class<? extends TechnologySpecificAction<?, ?>> modelSlotSpecificActionClass) {
		this.modelSlotSpecificActionClass = modelSlotSpecificActionClass;
	}

	public Class<? extends FetchRequest<?, ?>> getRequestActionClass() {
		return requestActionClass;
	}

	public void setRequestActionClass(Class<? extends FetchRequest<?, ?>> requestActionClass) {
		this.requestActionClass = requestActionClass;
	}

	private DataBinding<?> assignation = null;
	private String declarationVariableName = null;

	private Type getAssignableType() {
		return Object.class;
	}

	public DataBinding<?> getAssignation() {
		if (assignation == null) {

			assignation = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET) {
				@Override
				public Type getDeclaredType() {
					return getAssignableType();
				}
			};
			assignation.setDeclaredType(getAssignableType());
			assignation.setBindingName("assignation");
			assignation.setMandatory(true);

		}
		assignation.setDeclaredType(getAssignableType());
		return assignation;
	}

	public void setAssignation(DataBinding<?> assignation) {
		if (assignation != null) {
			this.assignation = new DataBinding<Object>(assignation.toString(), this, Object.class,
					DataBinding.BindingDefinitionType.GET_SET) {
				@Override
				public Type getDeclaredType() {
					return getAssignableType();
				}
			};
			assignation.setDeclaredType(getAssignableType());
			assignation.setBindingName("assignation");
			assignation.setMandatory(true);
		}
		notifiedBindingChanged(this.assignation);
	}

	public String getDeclarationVariableName() {
		return declarationVariableName;
	}

	public void setDeclarationVariableName(String declarationVariableName) {
		if ((declarationVariableName == null && this.declarationVariableName != null)
				|| (declarationVariableName != null && !declarationVariableName.equals(this.declarationVariableName))) {
			String oldValue = this.declarationVariableName;
			this.declarationVariableName = declarationVariableName;
			getPropertyChangeSupport().firePropertyChange("declarationVariableName", oldValue, declarationVariableName);
		}
	}

	@Override
	public BindingFactory getBindingFactory() {
		return getFocusedObject().getBindingFactory();
	}

	@Override
	public BindingModel getBindingModel() {
		return getFocusedObject().getBindingModel();
	}

	@Override
	public void notifiedBindingChanged(org.openflexo.antar.binding.DataBinding<?> dataBinding) {
		// TODO
	}

	@Override
	public void notifiedBindingDecoded(org.openflexo.antar.binding.DataBinding<?> dataBinding) {
		// TODO
	}
}
