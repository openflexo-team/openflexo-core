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
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.type.ExplicitNullType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UndefinedType;
import org.openflexo.foundation.fml.FMLUtils;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * Encodes a sequence as a sequential definition of two control graphs
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(Sequence.SequenceImpl.class)
@XMLElement
public interface Sequence extends FMLControlGraph, FMLControlGraphOwner {

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH1_KEY = "controlGraph1";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH2_KEY = "controlGraph2";

	@Getter(value = CONTROL_GRAPH1_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph1_")
	public FMLControlGraph getControlGraph1();

	@Setter(CONTROL_GRAPH1_KEY)
	public void setControlGraph1(FMLControlGraph aControlGraph);

	@Getter(value = CONTROL_GRAPH2_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph2_")
	public FMLControlGraph getControlGraph2();

	@Setter(CONTROL_GRAPH2_KEY)
	public void setControlGraph2(FMLControlGraph aControlGraph);

	/**
	 * When this sequence represents a sequence of more than two control graphs, resulting structure is a sequence of sequence of
	 * sequence... This method allows to retrieve a flattened list of all chained control graphs
	 * 
	 * @return a flattened list of all chained control graphs
	 */
	@Override
	public List<FMLControlGraph> getFlattenedSequence();

	public static abstract class SequenceImpl extends FMLControlGraphImpl implements Sequence {
	
		@Override
		public void setControlGraph1(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH1_KEY);
			}
			performSuperSetter(CONTROL_GRAPH1_KEY, aControlGraph);
			
			// Because BindingModel of control graph 2 relies on control graph 1, update base BindingModel of CG2
			if (getControlGraph2() != null) {
				getControlGraph2().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph2()));
			}

		}

		@Override
		public void setControlGraph2(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH2_KEY);
			}
			performSuperSetter(CONTROL_GRAPH2_KEY, aControlGraph);
		}

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {

			getControlGraph2().sequentiallyAppend(controlGraph);
			getOwner().controlGraphChanged(this);
		}

		@Override
		public void reduce() {
			// We first store actual owning context
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			// We reduce each control graphs
			if (getControlGraph1() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph1()).reduce();
			}
			if (getControlGraph2() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph2()).reduce();
			}

			if (getControlGraph1() instanceof EmptyControlGraph) {
				replaceWith(getControlGraph2(), owner, ownerContext);
			}
			else if (getControlGraph2() instanceof EmptyControlGraph) {
				replaceWith(getControlGraph1(), owner, ownerContext);
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH1_KEY.equals(ownerContext)) {
				return getControlGraph1();
			}
			else if (CONTROL_GRAPH2_KEY.equals(ownerContext)) {
				return getControlGraph2();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH1_KEY.equals(ownerContext)) {
				setControlGraph1(controlGraph);
			}
			else if (CONTROL_GRAPH2_KEY.equals(ownerContext)) {
				setControlGraph2(controlGraph);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph1()) {
				return getBindingModel();
			}
			else if (controlGraph == getControlGraph2()) {
				// If control graph 1 declares a new variable, this variable should be added
				// to context of control graph 2 binding model
				if (getControlGraph1() instanceof AssignableAction) {
					return getControlGraph1().getInferedBindingModel();
				}
				return getBindingModel();
				// return getControlGraph1().getInferedBindingModel();
			}
			return null;
		}

		@Override
		public List<FMLControlGraph> getFlattenedSequence() {
			List<FMLControlGraph> returned = new ArrayList<>();
			if (getControlGraph1() instanceof Sequence) {
				returned.addAll(((Sequence) getControlGraph1()).getFlattenedSequence());
			}
			else if (getControlGraph1() != null) {
				returned.add(getControlGraph1());
			}
			if (getControlGraph2() instanceof Sequence) {
				returned.addAll(((Sequence) getControlGraph2()).getFlattenedSequence());
			}
			else if (getControlGraph2() != null) {
				returned.add(getControlGraph2());
			}
			return returned;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			getControlGraph1().execute(evaluationContext);
			getControlGraph2().execute(evaluationContext);
			return null;
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);

			if (getControlGraph1() != null) {
				getControlGraph1().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph1()));
			}
			if (getControlGraph2() != null) {
				getControlGraph2().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph2()));
			}
		}

		@Override
		public Type getInferedType() {
			if (getControlGraph1() == null) {
				return Void.class;
			}
			Type inferedType1 = getControlGraph1().getInferedType();
			if (getControlGraph2() == null) {
				return inferedType1;
			}
			Type inferedType2 = getControlGraph2().getInferedType();
			if (inferedType1.equals(Void.class)) {
				return inferedType2;
			}

			if (inferedType2.equals(Void.class)) {
				return inferedType1;
			}

			if (inferedType1 instanceof ExplicitNullType) {
				if (inferedType2 instanceof ExplicitNullType) {
					return Object.class;
				}
				return inferedType2;
			}

			if (inferedType1 instanceof UndefinedType) {
				if (inferedType2 instanceof UndefinedType) {
					return UndefinedType.INSTANCE;
				}
				return inferedType2;
			}

			if (inferedType2 instanceof ExplicitNullType || inferedType2 instanceof UndefinedType) {
				return inferedType1;
			}

			if (TypeUtils.isTypeAssignableFrom(inferedType1, inferedType2)) {
				return inferedType1;
			}

			if (TypeUtils.isTypeAssignableFrom(inferedType2, inferedType1)) {
				return inferedType2;
			}

			if (inferedType1 instanceof FlexoConceptInstanceType && ((FlexoConceptInstanceType) inferedType1).getFlexoConcept() != null
					&& inferedType2 instanceof FlexoConceptInstanceType
					&& ((FlexoConceptInstanceType) inferedType2).getFlexoConcept() != null) {
				// Both types are FCI, but not in the same hierarchy
				FlexoConcept mostSpecializedAncestor = FMLUtils.getMostSpecializedAncestor(
						((FlexoConceptInstanceType) inferedType1).getFlexoConcept(),
						((FlexoConceptInstanceType) inferedType2).getFlexoConcept());
				if (mostSpecializedAncestor != null) {
					return mostSpecializedAncestor.getInstanceType();
				}

			}

			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			visitor.visit(this);
			if (getControlGraph1() != null) {
				getControlGraph1().accept(visitor);
			}
			if (getControlGraph2() != null) {
				getControlGraph2().accept(visitor);
			}
		}
		
	}

	@DefineValidationRule
	public static class ControlGraph1IsRequired extends ValidationRule<ControlGraph1IsRequired, Sequence> {
		public ControlGraph1IsRequired() {
			super(Sequence.class, "sequence_must_contain_first_control_graph");
		}

		@Override
		public ValidationIssue<ControlGraph1IsRequired, Sequence> applyValidation(Sequence sequence) {

			if (sequence.getControlGraph1() == null) {
				System.err.println("Missing control graph for " + sequence);
				return new ValidationError<>(this, sequence, "missing_control_graph_(first_statement)");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ControlGraph2IsRequired extends ValidationRule<ControlGraph2IsRequired, Sequence> {
		public ControlGraph2IsRequired() {
			super(Sequence.class, "sequence_must_contain_first_control_graph");
		}

		@Override
		public ValidationIssue<ControlGraph2IsRequired, Sequence> applyValidation(Sequence sequence) {

			if (sequence.getControlGraph2() == null) {
				System.err.println("Missing control graph for " + sequence);
				return new ValidationError<>(this, sequence, "missing_control_graph_(second_statement)");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class InferedTypesMustBeCompatible extends ValidationRule<InferedTypesMustBeCompatible, Sequence> {
		public InferedTypesMustBeCompatible() {
			super(Sequence.class, "infered_types_must_be_compatible_in_a_sequence");
		}

		@Override
		public ValidationIssue<InferedTypesMustBeCompatible, Sequence> applyValidation(Sequence sequence) {

			if (sequence.getControlGraph1() == null || sequence.getControlGraph2() == null) {
				return null;
			}

			Type inferedType1 = sequence.getControlGraph1().getInferedType();
			Type inferedType2 = sequence.getControlGraph2().getInferedType();

			if (!(inferedType1.equals(Void.class)) && !(inferedType2.equals(Void.class))
					&& !TypeUtils.isTypeAssignableFrom(inferedType1, inferedType2)
					&& !TypeUtils.isTypeAssignableFrom(inferedType2, inferedType1)
					&& !(inferedType1 instanceof FlexoConceptInstanceType && inferedType2 instanceof FlexoConceptInstanceType)) {
				System.out.println("Types are not compatible in:");
				System.out.println(sequence.getFMLPrettyPrint());
				return new ValidationError<>(this, sequence, "types_are_not_compatible (" + TypeUtils.simpleRepresentation(inferedType1)
						+ " and " + TypeUtils.simpleRepresentation(inferedType2) + ")");
			}
			return null;
		}
	}

}
