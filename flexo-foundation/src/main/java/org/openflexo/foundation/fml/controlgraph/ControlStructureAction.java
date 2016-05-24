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

package org.openflexo.foundation.fml.controlgraph;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(ControlStructureAction.ControlStructureActionImpl.class)
@Imports({ @Import(ConditionalAction.class), @Import(IterationAction.class), @Import(WhileAction.class),
		@Import(IncrementalIterationAction.class) })
public abstract interface ControlStructureAction extends EditionAction, FMLControlGraph, ActionContainer {

	public static abstract class ControlStructureActionImpl extends EditionActionImpl implements ControlStructureAction {

		private static final Logger logger = Logger.getLogger(ControlStructureAction.class.getPackage().getName());

		private ControlGraphBindingModel<?> inferedBindingModel;

		public ControlStructureActionImpl() {
			super();
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			if (inferedBindingModel == null) {
				inferedBindingModel = makeInferedBindingModel();
			}
			return inferedBindingModel;
		}

		protected abstract ControlGraphBindingModel<?> makeInferedBindingModel();

		@Override
		public void variableAdded(AssignableAction action) {
			// rebuildInferedBindingModel();
		}

		@Override
		public void actionFirst(EditionAction a) {
			getActions().remove(a);
			getActions().add(0, a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionUp(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > 0) {
				getActions().remove(a);
				getActions().add(index - 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionDown(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > -1) {
				getActions().remove(a);
				getActions().add(index + 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionLast(EditionAction a) {
			getActions().remove(a);
			getActions().add(a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		/*@Override
		public Vector<EditionAction> getActions() {
			return actions;
		}
		
		@Override
		public void setActions(Vector<EditionAction> actions) {
			this.actions = actions;
			setChanged();
			notifyObservers();
		}
		
		@Override
		public void addToActions(EditionAction action) {
			// action.setScheme(getEditionScheme());
			action.setActionContainer(this);
			actions.add(action);
			setChanged();
			notifyObservers();
			notifyChange("actions", null, actions);
		}
		
		@Override
		public void removeFromActions(EditionAction action) {
			// action.setScheme(null);
			action.setActionContainer(null);
			actions.remove(action);
			setChanged();
			notifyObservers();
			notifyChange("actions", null, actions);
		}*/

		/*	@Override
			public int getIndex(EditionAction action) {
				return getActions().indexOf(action);
			}
		
			@Override
			public void insertActionAtIndex(EditionAction action, int index) {
				// action.setScheme(getEditionScheme());
				action.setActionContainer(this);
				getActions().add(index, action);
				setChanged();
				notifyObservers();
				notifyChange("actions", null, getActions());
			}
		
			@Override
			public void actionFirst(EditionAction a) {
				getActions().remove(a);
				getActions().add(0, a);
				setChanged();
				notifyChange("actions", null, getActions());
			}
		
			@Override
			public void actionUp(EditionAction a) {
				int index = getActions().indexOf(a);
				if (index > 0) {
					getActions().remove(a);
					getActions().add(index - 1, a);
					setChanged();
					notifyChange("actions", null, getActions());
				}
			}
		
			@Override
			public void actionDown(EditionAction a) {
				int index = getActions().indexOf(a);
				if (index > 0) {
					getActions().remove(a);
					getActions().add(index + 1, a);
					setChanged();
					notifyChange("actions", null, getActions());
				}
			}
		
			@Override
			public void actionLast(EditionAction a) {
				getActions().remove(a);
				getActions().add(a);
				setChanged();
				notifyChange("actions", null, getActions());
			}
		*/

		/*	@Override
			public AddShape createAddShapeAction() {
				AddShape newAction = new AddShape(null);
				if (getFlexoConcept().getDefaultShapePatternRole() != null) {
					newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultShapePatternRole().getPatternRoleName()));
				}
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddClass createAddClassAction() {
				AddClass newAction = new AddClass(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddIndividual createAddIndividualAction() {
				AddIndividual newAction = new AddIndividual(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddObjectPropertyStatement createAddObjectPropertyStatementAction() {
				AddObjectPropertyStatement newAction = new AddObjectPropertyStatement(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddDataPropertyStatement createAddDataPropertyStatementAction() {
				AddDataPropertyStatement newAction = new AddDataPropertyStatement(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddIsAStatement createAddIsAPropertyAction() {
				AddIsAStatement newAction = new AddIsAStatement(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddRestrictionStatement createAddRestrictionAction() {
				AddRestrictionStatement newAction = new AddRestrictionStatement(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddConnector createAddConnectorAction() {
				AddConnector newAction = new AddConnector(null);
				if (getFlexoConcept().getDefaultConnectorPatternRole() != null) {
					newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultConnectorPatternRole().getPatternRoleName()));
				}
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public DeclareFlexoRole createDeclarePatternRoleAction() {
				DeclareFlexoRole newAction = new DeclareFlexoRole(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public GraphicalAction createGraphicalAction() {
				GraphicalAction newAction = new GraphicalAction(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public CreateDiagram createAddDiagramAction() {
				CreateDiagram newAction = new CreateDiagram(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public AddFlexoConcept createAddFlexoConceptAction() {
				AddFlexoConcept newAction = new AddFlexoConcept(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public ConditionalAction createConditionalAction() {
				ConditionalAction newAction = new ConditionalAction(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public IterationAction createIterationAction() {
				IterationAction newAction = new IterationAction(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public CloneShape createCloneShapeAction() {
				CloneShape newAction = new CloneShape(null);
				if (getFlexoConcept().getDefaultShapePatternRole() != null) {
					newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultShapePatternRole().getPatternRoleName()));
				}
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public CloneConnector createCloneConnectorAction() {
				CloneConnector newAction = new CloneConnector(null);
				if (getFlexoConcept().getDefaultConnectorPatternRole() != null) {
					newAction.setAssignation(new ViewPointDataBinding(getFlexoConcept().getDefaultConnectorPatternRole().getPatternRoleName()));
				}
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public CloneIndividual createCloneIndividualAction() {
				CloneIndividual newAction = new CloneIndividual(null);
				addToActions(newAction);
				return newAction;
			}
		
			@Override
			public DeleteAction createDeleteAction() {
				DeleteAction newAction = new DeleteAction(null);
				addToActions(newAction);
				return newAction;
			}*/

		/**
		 * Creates a new {@link EditionAction} of supplied class, and add it to the list of contained action managed by this control
		 * structure action<br>
		 * Delegates creation to model slot
		 * 
		 * @return newly created {@link EditionAction}
		 */
		@Override
		public <A extends TechnologySpecificAction<?, ?>> A createAction(Class<A> actionClass, ModelSlot<?> modelSlot) {
			A newAction = modelSlot.createAction(actionClass);
			addToActions(newAction);
			return newAction;
		}

		@Override
		public EditionAction deleteAction(EditionAction anAction) {
			removeFromActions(anAction);
			anAction.delete();
			return anAction;
		}

		/*@Override
		public final void finalizePerformAction(FlexoBehaviourAction action, Object initialContext) {
			// Not applicable for ControlStructureAction
		};*/

	}
}
