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
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.type.ConnieType;
import org.openflexo.foundation.fml.binding.CatchClauseBindingModel;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.error.FMLError;
import org.openflexo.foundation.fml.rt.error.FMLErrorTypes;
import org.openflexo.foundation.fml.rt.error.FMLExecution;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.foundation.fml.rt.error.FMLInternalError;
import org.openflexo.foundation.fml.validation.TypeMustBeResolved;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A {@code catch (<Type> <name>) { ... }} clause of a {@link TryStatement} (CORE-F-11 phase 7).
 * <p>
 * It names an {@link FMLError} type (visible without import, {@code FMLError} catching every foreseen error) or an FML exception. Its
 * variable, typed by the caught type, is visible in its block only: it holds the instance of the FML exception when the clause names an
 * FML exception, the error itself otherwise. A clause is run by its {@link TryStatement} only.
 */
@ModelEntity
@ImplementationClass(CatchClause.CatchClauseImpl.class)
public interface CatchClause extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = String.class)
	public static final String VARIABLE_NAME_KEY = "variableName";
	@PropertyIdentifier(type = Type.class)
	public static final String CATCH_TYPE_KEY = "catchType";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";

	@Getter(value = VARIABLE_NAME_KEY)
	public String getVariableName();

	@Setter(VARIABLE_NAME_KEY)
	public void setVariableName(String variableName);

	@Getter(value = CATCH_TYPE_KEY, isStringConvertable = true)
	public Type getCatchType();

	@Setter(CATCH_TYPE_KEY)
	public void setCatchType(Type type);

	/**
	 * Translate supplied type to the typing space of this clause during updateWith() (see AbstractIterationAction)
	 */
	@Updater(CATCH_TYPE_KEY)
	public void updateCatchType(Type type);

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	/** The try statement this clause belongs to */
	public TryStatement getTryStatement();

	/** Tell if this clause catches supplied error */
	public boolean catches(FMLError error);

	/**
	 * Run the block of this clause for supplied caught error, its variable holding it (or the FML exception instance it carries)
	 */
	public Object executeCatch(RunTimeEvaluationContext evaluationContext, FMLError error) throws ReturnException, FMLExecutionException;

	public static abstract class CatchClauseImpl extends ControlStructureActionImpl implements CatchClause {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(CatchClause.class.getPackage().getName());

		private String variableName = "e";

		@Override
		public String getVariableName() {
			return variableName;
		}

		@Override
		public void setVariableName(String variableName) {
			if (this.variableName == null || !this.variableName.equals(variableName)) {
				String oldValue = this.variableName;
				this.variableName = variableName;
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_KEY, oldValue, variableName);
			}
		}

		@Override
		public void updateCatchType(Type type) {
			if (getDeclaringCompilationUnit() != null && type instanceof ConnieType) {
				setCatchType(((ConnieType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setCatchType(type);
			}
		}

		@Override
		public TryStatement getTryStatement() {
			return getOwner() instanceof TryStatement ? (TryStatement) getOwner() : null;
		}

		@Override
		public boolean delete(Object... context) {
			// A clause is held in the list of its try statement, not in a control graph slot that an empty control graph could replace
			TryStatement tryStatement = getTryStatement();
			if (tryStatement != null && !isDeleted()) {
				tryStatement.removeFromCatchClauses(this);
			}
			return super.delete(context);
		}

		@Override
		public boolean catches(FMLError error) {
			return getCatchType() != null && FMLErrorTypes.catches(getCatchType(), error);
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			throw new FMLInternalError(new IllegalStateException("A catch clause runs only for an error caught by its try statement"));
		}

		@Override
		public Object executeCatch(RunTimeEvaluationContext evaluationContext, FMLError error)
				throws ReturnException, FMLExecutionException {
			try {
				evaluationContext.declareVariable(getVariableName(), FMLErrorTypes.caughtValue(getCatchType(), error));
				if (getControlGraph() != null) {
					return FMLExecution.executeStatement(getControlGraph(), evaluationContext);
				}
				return null;
			} finally {
				// Also when the block fails or returns: the variable is visible in the block only
				evaluationContext.dereferenceVariable(getVariableName());
			}
		}

		@Override
		protected CatchClauseBindingModel makeInferedBindingModel() {
			return new CatchClauseBindingModel(this);
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			// The block sees the variable of the clause
			return getInferedBindingModel();
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			}
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH_KEY);
			}
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph()));
			}
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
		}

		@Override
		public Type getInferedType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getControlGraph() != null) {
				getControlGraph().accept(visitor);
			}
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "catch (" + (getCatchType() != null ? getCatchType().getTypeName() : "?") + " " + getVariableName()
					+ ")";
		}
	}

	@DefineValidationRule
	public static class CatchTypeMustBeResolved extends TypeMustBeResolved<CatchClause> {
		public CatchTypeMustBeResolved() {
			super("catch_type_must_be_resolved", CatchClause.class);
		}

		@Override
		public Type getType(CatchClause clause) {
			return clause.getCatchType();
		}
	}

	/**
	 * A clause names an {@link FMLError} type or an FML exception: {@code FMLInternalError} (a defect of the platform), {@code FMLCancellation}
	 * (the user cancelled) and the untyped {@code FMLExecutionException} cannot be caught
	 */
	@DefineValidationRule
	public static class CatchTypeMustBeCatchable extends ValidationRule<CatchTypeMustBeCatchable, CatchClause> {

		public CatchTypeMustBeCatchable() {
			super(CatchClause.class, "catch_type_must_be_an_error_or_an_exception");
		}

		@Override
		public ValidationIssue<CatchTypeMustBeCatchable, CatchClause> applyValidation(CatchClause clause) {
			if (clause.getCatchType() == null) {
				return new ValidationError<>(this, clause, "type_must_be_declared");
			}
			if (!FMLErrorTypes.isCatchable(clause.getCatchType())) {
				if (FMLErrorTypes.isImplicitType(clause.getCatchType())) {
					return new ValidationError<>(this, clause, "internal_errors_and_cancellations_cannot_be_caught");
				}
				return new ValidationError<>(this, clause, "only_an_fml_error_or_an_fml_exception_can_be_caught");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class LocalVariableMustNotRedeclareAVisibleLocal
			extends ValidationRule<LocalVariableMustNotRedeclareAVisibleLocal, CatchClause> {

		public LocalVariableMustNotRedeclareAVisibleLocal() {
			super(CatchClause.class, "local_variable_must_not_redeclare_a_visible_local");
		}

		@Override
		public ValidationIssue<LocalVariableMustNotRedeclareAVisibleLocal, CatchClause> applyValidation(CatchClause clause) {
			BindingModel bindingModel = clause.getBindingModel();
			if (bindingModel != null && clause.getVariableName() != null) {
				BindingVariable visible = bindingModel.bindingVariableNamed(clause.getVariableName());
				if (FlexoBehaviourAction.isLocalVariable(visible)) {
					return new ValidationError<>(this, clause, "local_variable_is_already_declared_in_an_enclosing_block");
				}
			}
			return null;
		}
	}
}
