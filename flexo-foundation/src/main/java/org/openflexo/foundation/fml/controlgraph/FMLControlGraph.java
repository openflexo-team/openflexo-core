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

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

/**
 * Abstract definition of a control graph node in control flow graph paradigm.<br>
 * 
 * In a control flow graph each node in the graph represents a basic block, i.e. a straight-line piece of code without any jumps or jump
 * targets; jump targets start a block, and jumps end a block (from http://en.wikipedia.org/wiki/Control_flow_graph)
 * 
 * A {@link FMLControlGraph} might be typed. In this case, use {@link #getType()} and {@link #setType(Type)} methods. Return statements are
 * only usable in typed control graph (a {@link FMLControlGraph} with a non-null {@link #getType()}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLControlGraph.FMLControlGraphImpl.class)
@Imports({ @Import(EditionAction.class), @Import(EmptyControlGraph.class), @Import(Sequence.class) })
public abstract interface FMLControlGraph extends FlexoConceptObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLControlGraphOwner.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String OWNER_CONTEXT_KEY = "ownerContext";
	// @PropertyIdentifier(type = Type.class)
	// public static final String TYPE_KEY = "type";

	@Getter(value = OWNER_KEY, isDerived = true)
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
	public ControlGraphBindingModel<?> getInferedBindingModel();

	public FMLControlGraphOwner getRootOwner();

	/**
	 * Execute this control graph in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param evaluationContext
	 * @return
	 */
	public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException;

	/**
	 * This method allows to retrieve a flattened list of all chained control graphs
	 * 
	 * @return a flattened list of all chained control graphs
	 */
	public List<? extends FMLControlGraph> getFlattenedSequence();

	public Sequence getParentFlattenedSequence();

	/**
	 * Computed and return type of this {@link FMLControlGraph}, with the semantics of return statement<br>
	 * Unless a return (@see {@link ReturnStatement}) is declared, infered type is Void
	 * 
	 * @return
	 */
	public Type getInferedType();

	/**
	 * Called to explore an FML control graph
	 * 
	 * @param visitor
	 */
	public void accept(FMLControlGraphVisitor visitor);

	/**
	 * Used to replace in owner's context this control graph by supplied control graph
	 * 
	 * @param cg
	 */
	public void replaceWith(FMLControlGraph cg);

	/**
	 * Called to "disconnect" this control graph from its actual owner, and to append it sequentially on the supplied receiver
	 * 
	 * @param receiver
	 */
	public void moveWhileSequentiallyAppendingTo(FMLControlGraph receiver);

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
			return new ControlGraphBindingModel<FMLControlGraph>(this);
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			return getBindingModel();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() instanceof FlexoConceptObject) {
				return ((FlexoConceptObject) getOwner()).getFlexoConcept();
			}
			return null;
		}

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {

			FMLModelFactory factory = getFMLModelFactory();

			// We first store actual owning context
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			if (owner instanceof Sequence && getOwnerContext().equals(Sequence.CONTROL_GRAPH1_KEY)) {
				// Special case for sequence when we append between CG1 and CG2
				// We first consider right argument of sequence
				FMLControlGraph previousCG2 = ((Sequence) owner).getControlGraph2();
				// Then we nulllify right argument of sequence
				((Sequence) owner).setControlGraph2(null);
				// We create a new sequence with the new CG and the former right argument
				Sequence sequence = factory.newSequence(controlGraph, previousCG2);
				// And set it as right argument of sequence
				((Sequence) owner).setControlGraph2(sequence);
				owner.controlGraphChanged(sequence);
			}

			else {
				// Following statement is really important, we need first to "disconnect" actual control graph
				// Before to build the new sequence !!!
				owner.setControlGraph(null, ownerContext);

				Sequence sequence = factory.newSequence(this, controlGraph);
				replaceWith(sequence, owner, ownerContext);

				owner.controlGraphChanged(sequence);
			}
		}

		/**
		 * Used to replace in owner's context this control graph by supplied control graph
		 * 
		 * @param cg
		 */
		@Override
		public void replaceWith(FMLControlGraph cg) {

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			owner.setControlGraph(null, ownerContext);

			// We connect control graph
			setOwnerContext(ownerContext);
			owner.setControlGraph(cg, ownerContext);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

		}

		/**
		 * Internally used to replace in owner's context this control graph by supplied control graph
		 * 
		 * @param cg
		 */
		protected void replaceWith(FMLControlGraph cg, FMLControlGraphOwner owner, String ownerContext) {

			// Following statement is really important, we need first to "disconnect" actual control graph
			// owner.setControlGraph(null, ownerContext);

			// We connect control graph
			cg.setOwnerContext(ownerContext);
			owner.setControlGraph(cg, ownerContext);
		}

		/**
		 * Called to "disconnect" this control graph from its actual owner, and to append it sequentially on the supplied receiver, at given
		 * ownerContext
		 * 
		 * @param receiver
		 * @param ownerContext
		 */
		@Override
		public void moveWhileSequentiallyAppendingTo(FMLControlGraph receiver) {
			// We first store actual owning context
			FMLModelFactory factory = getFMLModelFactory();

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			// We first disconnect the control graph from its owner
			if (owner != null) {
				owner.setControlGraph(null, ownerContext);

				// Now we instantiate new EmptyControlGraph, and perform the replacement
				replaceWith(factory.newEmptyControlGraph(), owner, ownerContext);

				// We reduce owner
				owner.reduce();
			}

			// And then sequentially append
			receiver.sequentiallyAppend(this);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

		}

		protected boolean isDeleting = false;

		@Override
		public boolean delete(Object... context) {

			isDeleting = true;

			// This part is valid only if we are not deleting the owner also.

			// We first store actual owning context
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			// This part is valid only if we are not deleting the owner also.
			if (owner != null) {
				// Following statement is really important, we need first to "disconnect" actual control graph
				// owner.setControlGraph(null, ownerContext);

				// Now we instanciate new EmptyControlGraph, and perform the replacement
				FMLModelFactory factory = getFMLModelFactory();
				replaceWith(factory.newEmptyControlGraph(), owner, ownerContext);

				// We reduce owner
				owner.reduce();
			}

			// Now this control graph should be dereferenced
			// We finally call super delete, and this control graph will be really deleted
			boolean returned = performSuperDelete(context);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

			isDeleting = false;

			return returned;
		}

		@Override
		public FMLControlGraphOwner getRootOwner() {
			if (getOwner() instanceof FMLControlGraph) {
				return ((FMLControlGraph) getOwner()).getRootOwner();
			}
			return getOwner();
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

		@Override
		public synchronized void setIsModified() {
			super.setIsModified();
			if (getOwner() != null) {
				getOwner().setIsModified();
			}
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			performSuperSetter(OWNER_KEY, owner);
			// We should recursively call #notifiedScopeChanged() on all contained control graphs
			accept(new FMLControlGraphVisitor() {
				@Override
				public void visit(FMLControlGraph controlGraph) {
					controlGraph.notifiedScopeChanged();
				}
			});
		}

		@Override
		public BindingFactory getBindingFactory() {
			BindingFactory returned = super.getBindingFactory();
			if (returned == null) {
				// Maybe owner is not a FlexoConceptObject (for example in FML scripting)
				// So we give a chance here to retrieve the BindingFactory from owner
				if (getOwner() != null) {
					return getOwner().getBindingFactory();
				}
			}
			return returned;
		}

	}
}
