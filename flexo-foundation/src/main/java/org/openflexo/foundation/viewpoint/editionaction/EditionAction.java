/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
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
package org.openflexo.foundation.viewpoint.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.view.ModelSlotInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.action.ActionSchemeAction;
import org.openflexo.foundation.view.action.CreationSchemeAction;
import org.openflexo.foundation.view.action.DeletionSchemeAction;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.view.action.NavigationSchemeAction;
import org.openflexo.foundation.view.action.SynchronizationSchemeAction;
import org.openflexo.foundation.viewpoint.ActionContainer;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourObject;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Abstract class representing a primitive to be executed as an atomic action of an FlexoBehaviour
 * 
 * An edition action adresses a {@link ModelSlot}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(EditionAction.EditionActionImpl.class)
@Imports({ @Import(AddClass.class), @Import(AddIndividual.class), @Import(AddToListAction.class), @Import(AddFlexoConceptInstance.class),
		@Import(DeclarePatternRole.class), @Import(AssignationAction.class), @Import(ExecutionAction.class),
		@Import(SelectFlexoConceptInstance.class), @Import(SelectIndividual.class), @Import(MatchFlexoConceptInstance.class),
		@Import(RemoveFromListAction.class), @Import(ProcedureAction.class), @Import(DeleteAction.class), @Import(ConditionalAction.class),
		@Import(IterationAction.class), @Import(FetchRequestIterationAction.class) })
public abstract interface EditionAction<MS extends ModelSlot<?>, T> extends FlexoBehaviourObject {

	@PropertyIdentifier(type = ActionContainer.class)
	public static final String ACTION_CONTAINER_KEY = "actionContainer";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";
	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = ACTION_CONTAINER_KEY, inverse = ActionContainer.ACTIONS_KEY)
	public ActionContainer getActionContainer();

	@Setter(ACTION_CONTAINER_KEY)
	public void setActionContainer(ActionContainer actionContainer);

	@Override
	@Getter(value = DESCRIPTION_KEY)
	@XMLElement
	public String getDescription();

	@Override
	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement(primary = false)
	public MS getModelSlot();

	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);

	public boolean evaluateCondition(FlexoBehaviourAction<?, ?, ?> action);

	/**
	 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * Note than returned object will be used to be further reinjected in finalizer
	 * 
	 * @param action
	 * @return
	 */
	public T performAction(FlexoBehaviourAction<?, ?, ?> action);

	/**
	 * Provides hooks after executing edition action in the context provided by supplied {@link FlexoBehaviourAction}
	 * 
	 * @param action
	 * @param initialContext
	 *            the object that was returned during {@link #performAction(FlexoBehaviourAction)} call
	 * @return
	 */
	public void finalizePerformAction(FlexoBehaviourAction<?, ?, ?> action, T initialContext);

	public BindingModel getInferedBindingModel();

	public void rebuildInferedBindingModel();

	public int getIndex();

	public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType);

	public List<VirtualModelModelSlot> getAvailableVirtualModelModelSlots();

	public ModelSlotInstance<MS, ?> getModelSlotInstance(FlexoBehaviourAction<?, ?, ?> action);

	public static abstract class EditionActionImpl<MS extends ModelSlot<?>, T> extends FlexoBehaviourObjectImpl implements
			EditionAction<MS, T> {

		private static final Logger logger = Logger.getLogger(EditionAction.class.getPackage().getName());

		private MS modelSlot;

		private DataBinding<Boolean> conditional;

		// private ActionContainer actionContainer;

		private BindingModel inferedBindingModel = null;

		public EditionActionImpl() {
			super();
		}

		@Override
		public String getURI() {
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			if (getActionContainer() instanceof FlexoBehaviour) {
				return (FlexoBehaviour) getActionContainer();
			} else if (getActionContainer() != null) {
				return getActionContainer().getFlexoBehaviour();
			}
			return null;
		}

		@Override
		public VirtualModel getVirtualModel() {
			if (getFlexoBehaviour() != null) {
				return getFlexoBehaviour().getVirtualModel();
			}
			return null;
		}

		@Override
		public MS getModelSlot() {
			return modelSlot;
		}

		@Override
		public void setModelSlot(MS modelSlot) {
			this.modelSlot = modelSlot;
		}

		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType) {
			if (getFlexoConcept() != null && getFlexoConcept().getVirtualModel() != null) {
				return getFlexoConcept().getVirtualModel().getModelSlots(msType);
			} else if (getFlexoConcept() instanceof VirtualModel) {
				return ((VirtualModel) getFlexoConcept()).getModelSlots(msType);
			}
			return null;
		}

		@Override
		public List<VirtualModelModelSlot> getAvailableVirtualModelModelSlots() {
			return getAvailableModelSlots(VirtualModelModelSlot.class);
		}

		@Override
		public ModelSlotInstance<MS, ?> getModelSlotInstance(FlexoBehaviourAction<?, ?, ?> action) {
			if (action.getVirtualModelInstance() != null) {
				VirtualModelInstance vmi = action.getVirtualModelInstance();
				// Following line does not compile with Java7 (don't understand why)
				// That's the reason i tried to fix that compile issue with getGenericModelSlot() method (see below)
				 return action.getVirtualModelInstance().getModelSlotInstance(getModelSlot());
				//return (ModelSlotInstance<MS, ?>) vmi.getModelSlotInstance(getGenericModelSlot());
			} else {
				logger.severe("Could not access virtual model instance for action " + action);
				return null;
			}
		}

		// This method is used fix compile issue with Java7 (see above)
		/*private <MS2 extends ModelSlot<? extends RD>, RD extends ResourceData<RD> & TechnologyObject<?>> MS2 getGenericModelSlot() {
			return (MS2) getModelSlot();
		}*/

		@Override
		public boolean evaluateCondition(FlexoBehaviourAction action) {
			if (getConditional().isValid()) {
				try {
					return getConditional().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		/**
		 * Perform batch of actions, in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * An action is performed if and only if the condition evaluation returns true. All finalizers of actions are invoked in a second
		 * step when all actions are performed.
		 * 
		 * @param action
		 * @return
		 */
		public static void performBatchOfActions(Collection<EditionAction<?, ?>> actions, FlexoBehaviourAction<?, ?, ?> contextAction) {

			Hashtable<EditionAction<?, ?>, Object> performedActions = new Hashtable<EditionAction<?, ?>, Object>();

			for (EditionAction<?, ?> editionAction : actions) {
				if (editionAction.evaluateCondition(contextAction)) {
					Object assignedObject = editionAction.performAction(contextAction);
					if (assignedObject != null) {
						performedActions.put(editionAction, assignedObject);
					}

					if (assignedObject != null && editionAction instanceof AssignableAction) {
						AssignableAction assignableAction = (AssignableAction) editionAction;
						if (assignableAction.getIsVariableDeclaration()) {
							System.out.println("Setting variable " + assignableAction.getVariableName() + " with value " + assignedObject
									+ " of " + (assignedObject != null ? assignedObject.getClass() : "null"));
							contextAction.declareVariable(assignableAction.getVariableName(), assignedObject);
						}
						if (assignableAction.getAssignation().isSet() && assignableAction.getAssignation().isValid()) {
							try {
								assignableAction.getAssignation().setBindingValue(assignedObject, contextAction);
							} catch (Exception e) {
								logger.warning("Unexpected assignation issue, " + assignableAction.getAssignation() + " object="
										+ assignedObject + " exception: " + e);
								e.printStackTrace();
							}
						}
						if (assignableAction.getFlexoRole() != null && assignedObject instanceof FlexoObject) {
							if (contextAction instanceof ActionSchemeAction) {
								((ActionSchemeAction) contextAction).getFlexoConceptInstance().setObjectForFlexoRole(
										(FlexoObject) assignedObject, assignableAction.getFlexoRole());
							}
							if (contextAction instanceof CreationSchemeAction) {
								((CreationSchemeAction) contextAction).getFlexoConceptInstance().setObjectForFlexoRole(
										(FlexoObject) assignedObject, assignableAction.getFlexoRole());
							}
							if (contextAction instanceof DeletionSchemeAction) {
								((DeletionSchemeAction) contextAction).getFlexoConceptInstance().setObjectForFlexoRole(
										(FlexoObject) assignedObject, assignableAction.getFlexoRole());
							}
							if (contextAction instanceof NavigationSchemeAction) {
								((NavigationSchemeAction) contextAction).getFlexoConceptInstance().setObjectForFlexoRole(
										(FlexoObject) assignedObject, assignableAction.getFlexoRole());
							}
							if (contextAction instanceof SynchronizationSchemeAction) {
								((SynchronizationSchemeAction) contextAction).getFlexoConceptInstance().setObjectForFlexoRole(
										(FlexoObject) assignedObject, assignableAction.getFlexoRole());
							}
						}
					}
				}
			}

			for (EditionAction editionAction : performedActions.keySet()) {
				editionAction.finalizePerformAction(contextAction, performedActions.get(editionAction));
			}
		}

		/**
		 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * Note than returned object will be used to be further reinjected in finalizer
		 * 
		 * @param action
		 * @return
		 */
		@Override
		public abstract T performAction(FlexoBehaviourAction action);

		/**
		 * Provides hooks after executing edition action in the context provided by supplied {@link FlexoBehaviourAction}
		 * 
		 * @param action
		 * @param initialContext
		 *            the object that was returned during {@link #performAction(FlexoBehaviourAction)} call
		 * @return
		 */
		@Override
		public abstract void finalizePerformAction(FlexoBehaviourAction action, T initialContext);

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getFlexoBehaviour() == null) {
				return null;
			}
			return getFlexoBehaviour().getFlexoConcept();
		}

		public Type getActionClass() {
			return getClass();
		}

		@Override
		public int getIndex() {
			if (getFlexoBehaviour() != null && getFlexoBehaviour().getActions() != null) {
				return getFlexoBehaviour().getActions().indexOf(this);
			}
			return -1;
		}

		@Override
		public final BindingModel getBindingModel() {
			if (getActionContainer() != null) {
				return getActionContainer().getInferedBindingModel();
			}
			return null;
		}

		@Override
		public final BindingModel getInferedBindingModel() {
			if (inferedBindingModel == null) {
				rebuildInferedBindingModel();
			}
			return inferedBindingModel;
		}

		@Override
		public void rebuildInferedBindingModel() {
			inferedBindingModel = buildInferedBindingModel();
		}

		protected BindingModel buildInferedBindingModel() {
			BindingModel returned;
			if (getActionContainer() == null || isDeserializing()/* Prevent StackOverflow !!! */) {
				returned = new BindingModel();
			} else {
				returned = new BindingModel(getActionContainer().getInferedBindingModel());
			}
			return returned;
		}

		@Override
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}

		@Override
		public void setConditional(DataBinding<Boolean> conditional) {
			if (conditional != null) {
				conditional.setOwner(this);
				conditional.setDeclaredType(Boolean.class);
				conditional.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			this.conditional = conditional;
		}

		@Override
		public String getStringRepresentation() {
			return getImplementedInterface().getSimpleName();
		}

		@Override
		public String toString() {
			return getStringRepresentation();
		}

		/*@Override
		public ActionContainer getActionContainer() {
			return actionContainer;
		}*/

		@Override
		public void setActionContainer(ActionContainer actionContainer) {
			// this.actionContainer = actionContainer;
			performSuperSetter(ACTION_CONTAINER_KEY, actionContainer);
			rebuildInferedBindingModel();
		}

		private void insertActionAtCurrentIndex(EditionAction<?, ?> editionAction) {
			if (getActionContainer() != null) {
				getActionContainer().insertActionAtIndex(editionAction, getActionContainer().getIndex(this) + 1);
			}
		}

		/*public AddShape createAddShapeAction() {
			AddShape newAction = new AddShape(null);
			if (getFlexoConcept().getDefaultShapePatternRole() != null) {
				newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultShapePatternRole().getPatternRoleName()));
			}
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddClass createAddClassAction() {
			AddClass newAction = new AddClass(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddIndividual createAddIndividualAction() {
			AddIndividual newAction = new AddIndividual(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddObjectPropertyStatement createAddObjectPropertyStatementAction() {
			AddObjectPropertyStatement newAction = new AddObjectPropertyStatement(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddDataPropertyStatement createAddDataPropertyStatementAction() {
			AddDataPropertyStatement newAction = new AddDataPropertyStatement(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddIsAStatement createAddIsAPropertyAction() {
			AddIsAStatement newAction = new AddIsAStatement(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddRestrictionStatement createAddRestrictionAction() {
			AddRestrictionStatement newAction = new AddRestrictionStatement(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddConnector createAddConnectorAction() {
			AddConnector newAction = new AddConnector(null);
			if (getFlexoConcept().getDefaultConnectorPatternRole() != null) {
				newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultConnectorPatternRole().getPatternRoleName()));
			}
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public DeclarePatternRole createDeclarePatternRoleAction() {
			DeclarePatternRole newAction = new DeclarePatternRole(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public GraphicalAction createGraphicalAction() {
			GraphicalAction newAction = new GraphicalAction(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public CreateDiagram createAddDiagramAction() {
			CreateDiagram newAction = new CreateDiagram(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public AddFlexoConcept createAddFlexoConceptAction() {
			AddFlexoConcept newAction = new AddFlexoConcept(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public ConditionalAction createConditionalAction() {
			ConditionalAction newAction = new ConditionalAction(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public IterationAction createIterationAction() {
			IterationAction newAction = new IterationAction(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public CloneShape createCloneShapeAction() {
			CloneShape newAction = new CloneShape(null);
			if (getFlexoConcept().getDefaultShapePatternRole() != null) {
				newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultShapePatternRole().getPatternRoleName()));
			}
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public CloneConnector createCloneConnectorAction() {
			CloneConnector newAction = new CloneConnector(null);
			if (getFlexoConcept().getDefaultConnectorPatternRole() != null) {
				newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultConnectorPatternRole().getPatternRoleName()));
			}
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}

		public CloneIndividual createCloneIndividualAction() {
			CloneIndividual newAction = new CloneIndividual(null);
			insertActionAtCurrentIndex(newAction);
			return newAction;
		}*/

		/**
		 * Creates a new {@link EditionAction} of supplied class, and add it to parent container at the index where this action is itself
		 * registered<br>
		 * Delegates creation to model slot
		 * 
		 * @return newly created {@link EditionAction}
		 */
		public <A extends EditionAction<?, ?>> A createActionAtCurrentIndex(Class<A> actionClass, ModelSlot<?> modelSlot) {
			A newAction = modelSlot.createAction(actionClass);
			insertActionAtCurrentIndex(newAction);
			return null;
		}

	}

	public static class ConditionalBindingMustBeValid extends BindingMustBeValid<EditionAction> {
		public ConditionalBindingMustBeValid() {
			super("'conditional'_binding_is_not_valid", EditionAction.class);
		}

		@Override
		public DataBinding<Boolean> getBinding(EditionAction object) {
			return object.getConditional();
		}

	}

}
