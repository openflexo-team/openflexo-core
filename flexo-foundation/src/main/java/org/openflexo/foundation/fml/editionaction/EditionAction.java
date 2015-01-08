/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
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
package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.EditionActionBindingModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FetchRequestIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

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
		@Import(DeclarationAction.class), @Import(AssignationAction.class), @Import(ExpressionAction.class),
		@Import(SelectFlexoConceptInstance.class), @Import(SelectIndividual.class), @Import(MatchFlexoConceptInstance.class),
		@Import(RemoveFromListAction.class), @Import(DeleteAction.class), @Import(ConditionalAction.class), @Import(IterationAction.class),
		@Import(FetchRequestIterationAction.class) })
public abstract interface EditionAction extends FMLControlGraph {

	@PropertyIdentifier(type = ActionContainer.class)
	public static final String ACTION_CONTAINER_KEY = "actionContainer";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	@Getter(value = ACTION_CONTAINER_KEY, inverse = ActionContainer.ACTIONS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public ActionContainer getActionContainer();

	@Setter(ACTION_CONTAINER_KEY)
	public void setActionContainer(ActionContainer actionContainer);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	public boolean evaluateCondition(FlexoBehaviourAction<?, ?, ?> action);

	/**
	 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param action
	 * @return
	 */
	@Override
	public Object execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException;

	@Override
	public BindingModel getBindingModel();

	public static abstract class EditionActionImpl extends FMLControlGraphImpl implements EditionAction {

		private static final Logger logger = Logger.getLogger(EditionAction.class.getPackage().getName());

		private DataBinding<Boolean> conditional;

		private ControlGraphBindingModel<?> bindingModel;

		@Override
		public String getURI() {
			return null;
		}

		@Override
		public boolean evaluateCondition(FlexoBehaviourAction<?, ?, ?> action) {
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
		 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * Note than returned object will be used to be further reinjected in finalizer
		 * 
		 * @param action
		 * @return
		 */
		@Override
		public abstract Object execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException;

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() != null) {
				return getOwner().getFlexoConcept();
			}
			return null;
		}

		public Type getActionClass() {
			return getClass();
		}

		@Override
		public ControlGraphBindingModel<?> getBindingModel() {
			if (bindingModel == null) {
				bindingModel = makeBindingModel();
			}
			return bindingModel;
		}

		@Override
		protected final ControlGraphBindingModel<?> makeBindingModel() {
			return new EditionActionBindingModel(this);
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			return getBindingModel();
		}

		/*@Override
		public final BindingModel getInferedBindingModel() {
			if (inferedBindingModel == null) {
				rebuildInferedBindingModel();
			}
			return inferedBindingModel;
		}*/

		/*@Override
		public void rebuildInferedBindingModel() {
			inferedBindingModel = buildInferedBindingModel();
			getPropertyChangeSupport().firePropertyChange(BindingModelChanged.BINDING_MODEL_CHANGED, null, inferedBindingModel);
		}*/

		/*protected BindingModel buildInferedBindingModel() {
			BindingModel returned;
			if (getActionContainer() == null || isDeserializing()) {
				returned = new BindingModel();
			} else {
				returned = new BindingModel(getActionContainer().getInferedBindingModel());
			}
			return returned;
		}*/

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

		/*@Override
		public ActionContainer getActionContainer() {
			return actionContainer;
		}*/

		/*@Override
		public void setActionContainer(ActionContainer actionContainer) {
			// this.actionContainer = actionContainer;
			performSuperSetter(ACTION_CONTAINER_KEY, actionContainer);
			rebuildInferedBindingModel();
		}*/

		private void insertActionAtCurrentIndex(EditionAction editionAction) {
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

		public DeclareFlexoRole createDeclarePatternRoleAction() {
			DeclareFlexoRole newAction = new DeclareFlexoRole(null);
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
		public <A extends TechnologySpecificAction<?, ?>> A createActionAtCurrentIndex(Class<A> actionClass, ModelSlot<?> modelSlot) {
			A newAction = modelSlot.createAction(actionClass);
			insertActionAtCurrentIndex(newAction);
			return null;
		}

		/*@Override
		public void setOwner(FMLControlGraphOwner owner) {
			System.out.println("BEGIN / EditionAction, on set le owner de " + this + " avec " + owner);
			performSuperSetter(OWNER_KEY, owner);
			System.out.println("END / EditionAction, on a sette le owner de " + this + " avec " + owner);
		}*/

	}

	@DefineValidationRule
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
