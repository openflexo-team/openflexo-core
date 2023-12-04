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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Abstract class representing an {@link EditionAction} with the particularity of returning a value which can be assigned<br>
 * This value is of type T.<br>
 * An {@link AssignableAction} might be embedded in an {@link AbstractAssignationAction} ({@link AssignationAction} or
 * {@link DeclarationAction}) as right-hand side.
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of assignable
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AssignableAction.AssignableActionImpl.class)
public abstract interface AssignableAction<T> extends EditionAction {

	/**
	 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param evaluationContext
	 * @return
	 */
	@Override
	public T execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException;

	/**
	 * Return property to which this action is bound with an assignation, if this action is the right-hand side of an
	 * {@link AssignationAction}
	 */
	public FlexoProperty<T> getAssignedFlexoProperty();

	/**
	 * Return type resulting of execution of this action
	 * 
	 * @return
	 */
	public Type getAssignableType();

	/**
	 * Return boolean indicating if assignable type resulting of this action is iterable
	 * 
	 * @return
	 */
	public boolean isIterable();

	/**
	 * Return type of iterated items when assignable type resulting of this action is iterable
	 * 
	 * @return
	 */
	public Type getIteratorType();

	/**
	 * Used to declare a new variable and assigning assignableAction to it
	 * 
	 * @param cg
	 */
	public DeclarationAction<T> declaresNewVariable(String variableName);

	/**
	 * Used to instantiate AssignationAction while value set to this action
	 * 
	 * @param cg
	 */
	public AssignationAction<T> assignTo(DataBinding<? super T> assignation);

	/**
	 * Used to instantiate {@link AddToListAction} while added value set to this action
	 * 
	 * @param cg
	 */
	public AddToListAction<T> addToList(DataBinding<? extends List<T>> assignation);

	/**
	 * Used to instantiate {@link ReturnStatement} while returning current assignable action
	 * 
	 * @param cg
	 */
	public ReturnStatement<T> addReturnStatement();

	public static abstract class AssignableActionImpl<T> extends EditionActionImpl implements AssignableAction<T> {

		private static final Logger logger = Logger.getLogger(AssignableAction.class.getPackage().getName());

		/**
		 * Return property to which this action is bound with an assignation, if this action is the right-hand side of an
		 * {@link AssignationAction}
		 */
		@Override
		public FlexoProperty<T> getAssignedFlexoProperty() {
			// We might find the FlexoRole is this action is the assignableAction of an AssignationAction
			if (getOwner() instanceof AssignationAction) {
				return ((AssignationAction) getOwner()).getAssignedFlexoProperty();
			}
			return null;
		}

		@Override
		public abstract Type getAssignableType();

		@Override
		public Type getInferedType() {
			return Void.class;
		}

		@Override
		public boolean isIterable() {
			if (!TypeUtils.isTypeAssignableFrom(List.class, getAssignableType())) {
				return false;
			}
			return true;
		}

		@Override
		public Type getIteratorType() {
			if (!isIterable()) {
				return null;
			}
			if (getAssignableType() instanceof ParameterizedType) {
				Type returned = TypeUtils.getTypeArgument(getAssignableType(), List.class, 0);
				if (returned instanceof WildcardType) {
					WildcardType wt = (WildcardType) returned;
					if (wt.getUpperBounds() != null && wt.getUpperBounds().length == 1) {
						// Special case "? extends XXX" > XXX
						return wt.getUpperBounds()[0];
					}
				}
				return returned;
			}
			return Object.class;
		}

		/**
		 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * 
		 * @param evaluationContext
		 * @return
		 */
		@Override
		public abstract T execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException;

		/**
		 * Used to instantiate AssignationAction while value set to this action
		 * 
		 * @param cg
		 */
		@Override
		public AssignationAction<T> assignTo(DataBinding<? super T> assignation) {
			FMLModelFactory factory = getFMLModelFactory();

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			owner.setControlGraph(null, ownerContext);

			AssignationAction<T> assignationAction = factory.newAssignationAction(this);
			assignationAction.setAssignation(assignation);

			// We connect control graph
			setOwnerContext(ownerContext);
			owner.setControlGraph(assignationAction, ownerContext);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

			return assignationAction;

		}

		/**
		 * Used to declare a new variable and assigning assignableAction to it
		 * 
		 * @param cg
		 */
		@Override
		public DeclarationAction<T> declaresNewVariable(String variableName) {
			FMLModelFactory factory = getFMLModelFactory();

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			owner.setControlGraph(null, ownerContext);

			DeclarationAction<T> declarationAction = factory.newDeclarationAction(variableName, this);

			// We connect control graph
			setOwnerContext(ownerContext);
			owner.setControlGraph(declarationAction, ownerContext);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

			return declarationAction;

		}

		/**
		 * Used to instantiate {@link AddToListAction} while added value set to this action
		 * 
		 * @param cg
		 */
		@Override
		public AddToListAction<T> addToList(DataBinding<? extends List<T>> list) {
			FMLModelFactory factory = getFMLModelFactory();

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			owner.setControlGraph(null, ownerContext);

			AddToListAction<T> addToListAction = factory.newAddToListAction();
			addToListAction.setAssignableAction(this);
			addToListAction.setList(list);

			// We connect control graph
			setOwnerContext(ownerContext);
			owner.setControlGraph(addToListAction, ownerContext);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

			return addToListAction;

		}

		/**
		 * Used to instantiate {@link ReturnStatement} while returning current assignable action
		 * 
		 * @param cg
		 */
		@Override
		public ReturnStatement<T> addReturnStatement() {
			FMLModelFactory factory = getFMLModelFactory();

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			Sequence parentFlattenedSequence = getParentFlattenedSequence();

			owner.setControlGraph(null, ownerContext);

			ReturnStatement<T> returnStatement = factory.newReturnStatement(this);

			// We connect control graph
			setOwnerContext(ownerContext);
			owner.setControlGraph(returnStatement, ownerContext);

			// Then we must notify the parent flattenedSequence where this control graph was presented as a sequence
			// This fixes issue TA-81
			if (parentFlattenedSequence != null) {
				parentFlattenedSequence.controlGraphChanged(this);
			}

			return returnStatement;

		}

	}

}
