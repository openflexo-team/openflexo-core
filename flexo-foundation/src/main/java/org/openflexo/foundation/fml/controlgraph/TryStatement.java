/**
 * 
 * Copyright (c) 2026, Openflexo
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
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.error.FMLError;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLErrorTypes;
import org.openflexo.foundation.fml.rt.error.FMLExecution;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;

/**
 * The FML statement {@code try { ... } catch (<Type> e) { ... } ... finally { ... }} (CORE-F-11 phase 7).
 * <p>
 * The block of the try runs; a foreseen error ({@link FMLError}, an FML exception included) raised by it is given to the first
 * {@link CatchClause} that catches it, and is then handled: it is not reported, and the edits made before it are kept. An error that no
 * clause catches goes on. A defect of the platform ({@code FMLInternalError}), a cancellation and a {@code return} are never caught. The
 * {@code finally} block runs on every exit: normal end, error caught or not, {@code return}; an error or a {@code return} of the
 * {@code finally} block itself replaces the one going on, as in Java.
 * <p>
 * Nothing is added to the context: variables declared in a block are visible in that block only.
 */
@ModelEntity
@ImplementationClass(TryStatement.TryStatementImpl.class)
public interface TryStatement extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String TRY_CONTROL_GRAPH_KEY = "tryControlGraph";
	@PropertyIdentifier(type = CatchClause.class, cardinality = Cardinality.LIST)
	public static final String CATCH_CLAUSES_KEY = "catchClauses";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String FINALLY_CONTROL_GRAPH_KEY = "finallyControlGraph";

	@Getter(value = TRY_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public FMLControlGraph getTryControlGraph();

	@Setter(TRY_CONTROL_GRAPH_KEY)
	public void setTryControlGraph(FMLControlGraph aControlGraph);

	@Getter(value = CATCH_CLAUSES_KEY, cardinality = Cardinality.LIST, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<CatchClause> getCatchClauses();

	@Setter(CATCH_CLAUSES_KEY)
	public void setCatchClauses(List<CatchClause> catchClauses);

	@Adder(CATCH_CLAUSES_KEY)
	public void addToCatchClauses(CatchClause catchClause);

	@Remover(CATCH_CLAUSES_KEY)
	public void removeFromCatchClauses(CatchClause catchClause);

	/** The block run on every exit of the try statement, null when there is none */
	@Getter(value = FINALLY_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public FMLControlGraph getFinallyControlGraph();

	@Setter(FINALLY_CONTROL_GRAPH_KEY)
	public void setFinallyControlGraph(FMLControlGraph aControlGraph);

	/** The first clause catching supplied error, or null */
	public CatchClause getCatchClause(FMLError error);

	public static abstract class TryStatementImpl extends ControlStructureActionImpl implements TryStatement {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TryStatement.class.getPackage().getName());

		@Override
		public void setTryControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(TRY_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(TRY_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void setFinallyControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(FINALLY_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(FINALLY_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void addToCatchClauses(CatchClause catchClause) {
			if (catchClause != null) {
				catchClause.setOwnerContext(CATCH_CLAUSES_KEY);
			}
			performSuperAdder(CATCH_CLAUSES_KEY, catchClause);
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (TRY_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getTryControlGraph();
			}
			if (FINALLY_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getFinallyControlGraph();
			}
			// A catch clause is not a control graph slot: it is replaced through the list
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {
			if (TRY_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setTryControlGraph(controlGraph);
			}
			else if (FINALLY_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setFinallyControlGraph(controlGraph);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		// Nothing is appended to the context
		@Override
		protected ControlGraphBindingModel<?> makeInferedBindingModel() {
			return getBindingModel();
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getTryControlGraph() != null) {
				getTryControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getTryControlGraph()));
			}
			for (CatchClause clause : getCatchClauses()) {
				clause.getBindingModel().setBaseBindingModel(getBaseBindingModel(clause));
			}
			if (getFinallyControlGraph() != null) {
				getFinallyControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getFinallyControlGraph()));
			}
		}

		@Override
		public CatchClause getCatchClause(FMLError error) {
			for (CatchClause clause : getCatchClauses()) {
				if (clause.catches(error)) {
					return clause;
				}
			}
			return null;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			try {
				if (getTryControlGraph() != null) {
					return FMLExecution.executeStatement(getTryControlGraph(), evaluationContext);
				}
				return null;
			} catch (FMLError error) {
				// Only a foreseen error: a ReturnException, a cancellation and an FMLInternalError (unchecked) pass through
				CatchClause clause = getCatchClause(error);
				if (clause == null) {
					throw error;
				}
				// Handled: not reported, even when it is the failure of a nested action
				FMLErrorReporter.failureHandled(error);
				return FMLErrorTypes.whileHandling(error, () -> clause.executeCatch(evaluationContext, error));
			} finally {
				if (getFinallyControlGraph() != null) {
					FMLExecution.executeStatement(getFinallyControlGraph(), evaluationContext);
				}
			}
		}

		@Override
		public void reduce() {
			if (getTryControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getTryControlGraph()).reduce();
			}
			for (CatchClause clause : getCatchClauses()) {
				clause.reduce();
			}
			if (getFinallyControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getFinallyControlGraph()).reduce();
			}
		}

		@Override
		public Type getInferedType() {
			// The first block giving a value: the try block, then the catch blocks (a try ending with a throw gives none)
			if (getTryControlGraph() != null && !Void.class.equals(getTryControlGraph().getInferedType())) {
				return getTryControlGraph().getInferedType();
			}
			for (CatchClause clause : getCatchClauses()) {
				if (!Void.class.equals(clause.getInferedType())) {
					return clause.getInferedType();
				}
			}
			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getTryControlGraph() != null) {
				getTryControlGraph().accept(visitor);
			}
			for (CatchClause clause : getCatchClauses()) {
				clause.accept(visitor);
			}
			if (getFinallyControlGraph() != null) {
				getFinallyControlGraph().accept(visitor);
			}
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "try";
		}
	}

	/** A try has at least one catch clause or a finally block */
	@DefineValidationRule
	public static class TryNeedsACatchOrAFinally extends ValidationRule<TryNeedsACatchOrAFinally, TryStatement> {

		public TryNeedsACatchOrAFinally() {
			super(TryStatement.class, "try_needs_a_catch_or_a_finally");
		}

		@Override
		public ValidationIssue<TryNeedsACatchOrAFinally, TryStatement> applyValidation(TryStatement statement) {
			if (statement.getCatchClauses().isEmpty() && statement.getFinallyControlGraph() == null) {
				return new ValidationError<>(this, statement, "try_has_neither_catch_nor_finally");
			}
			return null;
		}
	}

	/** A catch clause after one that catches all it would catch is never run */
	@DefineValidationRule
	public static class CatchClauseMustBeReachable extends ValidationRule<CatchClauseMustBeReachable, TryStatement> {

		public CatchClauseMustBeReachable() {
			super(TryStatement.class, "catch_clause_must_be_reachable");
		}

		@Override
		public ValidationIssue<CatchClauseMustBeReachable, TryStatement> applyValidation(TryStatement statement) {
			List<CatchClause> clauses = statement.getCatchClauses();
			for (int j = 1; j < clauses.size(); j++) {
				for (int i = 0; i < j; i++) {
					Type wider = clauses.get(i).getCatchType();
					Type narrower = clauses.get(j).getCatchType();
					if (wider != null && narrower != null && FMLErrorTypes.covers(wider, narrower)) {
						return new ValidationWarning<>(this, statement, "catch_clause_is_unreachable");
					}
				}
			}
			return null;
		}
	}
}
