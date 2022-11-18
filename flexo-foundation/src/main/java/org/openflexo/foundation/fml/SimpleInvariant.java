/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * An {@link SimpleInvariant} is an invariant represented by a simple expression and a violation hook
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(SimpleInvariant.SimpleInvariantImpl.class)
@XMLElement(xmlTag = "Constraint")
public interface SimpleInvariant extends AbstractInvariant, FMLControlGraphOwner {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONSTRAINT_KEY = "constraint";
	@PropertyIdentifier(type = IterationInvariant.class)
	public static final String PARENT_ITERATION_INVARIANT_KEY = "parentIterationInvariant";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String VIOLATION_CONTROL_GRAPH_KEY = "violationControlGraph";

	@Getter(value = PARENT_ITERATION_INVARIANT_KEY, inverse = IterationInvariant.SIMPLE_INVARIANTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public IterationInvariant getParentIterationInvariant();

	@Setter(PARENT_ITERATION_INVARIANT_KEY)
	public void setParentIterationInvariant(IterationInvariant parentIterationInvariant);

	@Getter(value = CONSTRAINT_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConstraint();

	@Setter(CONSTRAINT_KEY)
	public void setConstraint(DataBinding<Boolean> constraint);

	public boolean hasFailureClause();

	@Getter(value = VIOLATION_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public FMLControlGraph getViolationControlGraph();

	@Setter(VIOLATION_CONTROL_GRAPH_KEY)
	public void setViolationControlGraph(FMLControlGraph aControlGraph);

	public static abstract class SimpleInvariantImpl extends AbstractInvariantImpl implements SimpleInvariant {

		protected static final Logger logger = FlexoLogger.getLogger(SimpleInvariant.class.getPackage().getName());

		private DataBinding<Boolean> constraint;

		@Override
		public int getIndex() {
			if (getParentIterationInvariant() != null) {
				return getParentIterationInvariant().getSimpleInvariants().indexOf(this);
			}
			return super.getIndex();
		}

		@Override
		public BindingModel getBindingModel() {
			if (getParentIterationInvariant() != null) {
				return getParentIterationInvariant().getIterationInvariantBindingModel();
			}
			return super.getBindingModel();
		}

		@Override
		public DataBinding<Boolean> getConstraint() {
			if (constraint == null) {
				constraint = new DataBinding<>(this, Boolean.class, BindingDefinitionType.GET);
				constraint.setBindingName("constraint");
			}
			return constraint;
		}

		@Override
		public void setConstraint(DataBinding<Boolean> constraint) {
			if (constraint != null) {
				constraint.setOwner(this);
				constraint.setBindingName("constraint");
				constraint.setDeclaredType(Boolean.class);
				constraint.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.constraint = constraint;
		}

		@Override
		public boolean hasFailureClause() {
			return getViolationControlGraph() != null;
		}

		@Override
		public void setViolationControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(VIOLATION_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(VIOLATION_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (VIOLATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getViolationControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (VIOLATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setViolationControlGraph(controlGraph);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		@Override
		public void reduce() {
			if (getViolationControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getViolationControlGraph()).reduce();
			}
		}

	}

}
