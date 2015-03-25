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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.EditionActionBindingModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FetchRequestIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

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
		@Import(FetchRequestIterationAction.class), @Import(ExecutionAction.class), @Import(DeclareFlexoRole.class) })
public abstract interface EditionAction extends FMLControlGraph {

	@PropertyIdentifier(type = ActionContainer.class)
	public static final String ACTION_CONTAINER_KEY = "actionContainer";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	@Getter(value = ACTION_CONTAINER_KEY /*, inverse = ActionContainer.ACTIONS_KEY*/)
	@CloningStrategy(StrategyType.IGNORE)
	public ActionContainer getActionContainer();

	@Setter(ACTION_CONTAINER_KEY)
	public void setActionContainer(ActionContainer actionContainer);

	@Deprecated
	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Deprecated
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

		@Deprecated
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

		@Deprecated
		@Override
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}

		@Deprecated
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

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getImplementedInterface().getSimpleName();
		}

		public final String getHeaderContext() {
			Sequence s = getParentFlattenedSequence();
			if (s != null && s.getFlattenedSequence().get(0) == this) {
				if (StringUtils.isNotEmpty(disambiguate(s.getOwnerContext()))) {
					return "[" + disambiguate(s.getOwnerContext()) + "] ";
				}
			}
			if (StringUtils.isNotEmpty(disambiguate(getOwnerContext()))) {
				return "[" + disambiguate(getOwnerContext()) + "] ";
			}
			return "";
		}

		private String disambiguate(String context) {
			if (context == null) {
				return null;
			}
			if (context.equals(Sequence.CONTROL_GRAPH1_KEY)) {
				return null;
			}
			if (context.equals(Sequence.CONTROL_GRAPH2_KEY)) {
				return null;
			}
			if (context.equals(ConditionalAction.THEN_CONTROL_GRAPH_KEY)) {
				return "then";
			}
			if (context.equals(ConditionalAction.ELSE_CONTROL_GRAPH_KEY)) {
				return "else";
			}
			if (context.equals(GetProperty.GET_CONTROL_GRAPH_KEY)) {
				return "get";
			}
			if (context.equals(GetSetProperty.SET_CONTROL_GRAPH_KEY)) {
				return "set";
			}
			return null;
		}

		public abstract String editionActionRepresentation();

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
				// getActionContainer().insertActionAtIndex(editionAction, getActionContainer().getIndex(this) + 1);
				getActionContainer().addToActions(editionAction);
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

		@Override
		public List<? extends EditionAction> getFlattenedSequence() {
			return Collections.singletonList(this);
		}

		/*@Override
		public void setOwner(FMLControlGraphOwner owner) {
			System.out.println("BEGIN / EditionAction, on set le owner de " + this + " avec " + owner);
			performSuperSetter(OWNER_KEY, owner);
			System.out.println("END / EditionAction, on a sette le owner de " + this + " avec " + owner);
		}*/

	}

	/*@DefineValidationRule
	public static class ConditionalBindingMustBeValid extends BindingMustBeValid<EditionAction> {
		public ConditionalBindingMustBeValid() {
			super("'conditional'_binding_is_not_valid", EditionAction.class);
		}

		@Override
		public DataBinding<Boolean> getBinding(EditionAction object) {
			return object.getConditional();
		}

	}*/

}
