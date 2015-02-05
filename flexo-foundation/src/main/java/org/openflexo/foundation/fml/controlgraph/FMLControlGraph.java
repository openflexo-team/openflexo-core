/**
 * 
 * Copyright (c) 2015, Openflexo
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

import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
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

/**
 * Abstract definition of a control graph node in control flow graph paradigm.<br>
 * 
 * In a control flow graph each node in the graph represents a basic block, i.e. a straight-line piece of code without any jumps or jump
 * targets; jump targets start a block, and jumps end a block (from http://en.wikipedia.org/wiki/Control_flow_graph)
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLControlGraph.FMLControlGraphImpl.class)
@Imports({ @Import(EditionAction.class), @Import(EmptyControlGraph.class), @Import(Sequence.class) })
public abstract interface FMLControlGraph extends FlexoConceptObject {

	@PropertyIdentifier(type = FMLControlGraphOwner.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String OWNER_CONTEXT_KEY = "ownerContext";

	@Getter(value = OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLControlGraphOwner getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(FMLControlGraphOwner owner);

	@Getter(value = OWNER_CONTEXT_KEY)
	@XMLAttribute
	public String getOwnerContext();

	@Setter(OWNER_CONTEXT_KEY)
	public void setOwnerContext(String context);

	/**
	 * Build and return a String encoding this {@link FMLControlGraph} in FML textual language
	 * 
	 * @param context
	 * @return
	 */
	@Override
	public String getFMLRepresentation(FMLRepresentationContext context);

	/**
	 * Sequentially append supplied control graph to this control graph<br>
	 * This method is generic and will be handled differently by subclasses to perform the most adapted job to the sequential semantics
	 * 
	 * @param controlGraph
	 */
	public void sequentiallyAppend(FMLControlGraph controlGraph);

	/**
	 * Delete this control graph<br>
	 * Also perform structural modifications to parent control graph and reduce structure when possible
	 */
	@Override
	public boolean delete(Object... context);

	/**
	 * Return {@link BindingModel} to be used in the context of this {@link FMLControlGraph}<br>
	 * This is the {@link BindingModel} to be used to decode {@link DataBinding} which are defined in the context of this
	 * {@link FMLControlGraph}
	 * 
	 * @return
	 */
	@Override
	public BindingModel getBindingModel();

	/**
	 * Return {@link BindingModel} infered from this {@link FMLControlGraph}<br>
	 * The infered BindingModel might be the {@link BindingModel} given by {@link #getBindingModel()}, but can also redefine this one by
	 * supporting new {@link BindingVariable} which are defined in the scope of current {@link FMLControlGraph}
	 * 
	 * @return
	 */
	public BindingModel getInferedBindingModel();

	public FMLControlGraphOwner getRootOwner();

	/**
	 * Execute this control graph in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param action
	 * @return
	 */
	public Object execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException;

	/**
	 * This method allows to retrieve a flattened list of all chained control graphs
	 * 
	 * @return a flattened list of all chained control graphs
	 */
	public List<? extends FMLControlGraph> getFlattenedSequence();

	public Sequence getParentFlattenedSequence();

	public static abstract class FMLControlGraphImpl extends FlexoConceptObjectImpl implements FMLControlGraph {

		private ControlGraphBindingModel<?> bindingModel;

		@Override
		public ControlGraphBindingModel<?> getBindingModel() {
			if (bindingModel == null) {
				bindingModel = makeBindingModel();
			}
			return bindingModel;
		}

		protected ControlGraphBindingModel<?> makeBindingModel() {
			return new ControlGraphBindingModel(this);
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			return getBindingModel();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() != null) {
				return getOwner().getFlexoConcept();
			}
			return null;
		}

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {

			FMLModelFactory factory = getFMLModelFactory();

			// We first store actual owning context
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			// Following statement is really important, we need first to "disconnect" actual control graph
			// Before to build the new sequence !!!
			owner.setControlGraph(null, ownerContext);

			Sequence sequence = factory.newSequence(this, controlGraph);
			replaceWith(sequence, owner, ownerContext);

			owner.controlGraphChanged(sequence);

		}

		/**
		 * Internally used to replace in owner's context this control graph by supplied control graph
		 * 
		 * @param cg
		 */
		protected void replaceWith(FMLControlGraph cg, FMLControlGraphOwner owner, String ownerContext) {

			// Following statement is really important, we need first to "disconnect" actual control graph
			// Before to build the new sequence !!!
			// owner.setControlGraph(null, ownerContext);

			// We connect control graph
			cg.setOwnerContext(ownerContext);
			owner.setControlGraph(cg, ownerContext);
		}

		@Override
		public boolean delete(Object... context) {

			// We first store actual owning context
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			// Following statement is really important, we need first to "disconnect" actual control graph
			// owner.setControlGraph(null, ownerContext);

			// Now we instanciate new EmptyControlGraph, and perform the replacement
			FMLModelFactory factory = getFMLModelFactory();
			replaceWith(factory.newEmptyControlGraph(), owner, ownerContext);

			// We reduce owner
			owner.reduce();

			// Now this control graph should be dereferenced
			// We finally call super delete, and this control graph will be really deleted
			return performSuperDelete(context);
		}

		@Override
		public FMLControlGraphOwner getRootOwner() {
			if (getOwner() instanceof FMLControlGraph) {
				return ((FMLControlGraph) getOwner()).getRootOwner();
			}
			return getOwner();
		}

		@Deprecated
		public FlexoBehaviour getFlexoBehaviour() {
			if (getRootOwner() instanceof FlexoBehaviourObject) {
				return ((FlexoBehaviourObject) getRootOwner()).getFlexoBehaviour();
			}
			return null;
		}

		@Override
		public Sequence getParentFlattenedSequence() {
			Sequence returned = null;
			FMLControlGraphOwner p = getOwner();
			while (p instanceof Sequence) {
				returned = (Sequence) p;
				p = returned.getOwner();
			}
			return returned;
		}

	}
}
