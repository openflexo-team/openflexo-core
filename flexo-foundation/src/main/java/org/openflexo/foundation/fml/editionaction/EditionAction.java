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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.EditionActionBindingModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.FireEvent;
import org.openflexo.foundation.fml.rt.editionaction.FireEventAction;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract class representing a primitive to be executed as an atomic action of a {@link FMLControlGraph}
 * 
 * An edition action adresses a {@link ModelSlot}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(EditionAction.EditionActionImpl.class)
// Following imports are required for those actions be available in a VirtualModel even if no model slot is declared
@Imports({ @Import(AddToListAction.class), @Import(AddFlexoConceptInstance.class), @Import(CreateTopLevelVirtualModelInstance.class),
		@Import(AddVirtualModelInstance.class), @Import(AddClassInstance.class), @Import(DeclarationAction.class),
		@Import(AssignationAction.class), @Import(ReturnStatement.class), @Import(ExpressionAction.class), @Import(LogAction.class),
		@Import(NotifyProgressAction.class), @Import(SelectUniqueFlexoConceptInstance.class), @Import(SelectFlexoConceptInstance.class),
		@Import(SelectUniqueVirtualModelInstance.class), @Import(SelectVirtualModelInstance.class), @Import(InitiateMatching.class),
		@Import(FinalizeMatching.class), @Import(MatchFlexoConceptInstance.class), @Import(RemoveFromListAction.class),
		@Import(DeleteAction.class), @Import(ConditionalAction.class), @Import(IterationAction.class), @Import(WhileAction.class),
		@Import(IncrementalIterationAction.class), @Import(FireEvent.class), @Import(FireEventAction.class),
		@Import(NotifyPropertyChangedAction.class), @Import(AddClassInstance.class), @Import(UnresolvedTechnologySpecificAction.class) })
public abstract interface EditionAction extends FMLControlGraph {

	/**
	 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param evaluationContext
	 * @return
	 */
	@Override
	public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException;

	@Override
	public BindingModel getBindingModel();

	public static abstract class EditionActionImpl extends FMLControlGraphImpl implements EditionAction {

		// private static final Logger logger = Logger.getLogger(EditionAction.class.getPackage().getName());

		private ControlGraphBindingModel<?> bindingModel;

		/**
		 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * Note than returned object will be used to be further reinjected in finalizer
		 * 
		 * @param evaluationContext
		 * @return
		 */
		@Override
		public abstract Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException;

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() instanceof FlexoConceptObject) {
				return ((FlexoConceptObject) getOwner()).getFlexoConcept();
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

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getImplementedInterface().getSimpleName() + getParametersStringRepresentation();
		}

		public String getParametersStringRepresentation() {
			return "()";
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

		private static String disambiguate(String context) {
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

		@Override
		public List<? extends EditionAction> getFlattenedSequence() {
			return Collections.singletonList(this);
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			visitor.visit(this);
		}

	}

}
