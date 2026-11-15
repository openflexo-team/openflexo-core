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

package org.openflexo.foundation.fml.rt.editionaction;

import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.foundation.fml.rt.error.FMLExecution;
import org.openflexo.foundation.fml.rt.error.FMLEvaluation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.AbstractFMLRTModelSlot;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * An action executing an action scheme ({@link #getFlexoBehaviour()}) on a {@link FlexoConceptInstance}, its receiver, and returning the
 * value returned by the action scheme.
 * <p>
 * This action is not produced by the FML parser, where a behaviour call is an expression ({@code shelf.rename("Classics")}); it can only
 * be written {@code FMLRT::ExecuteFlexoBehaviour(...)}. Beware: its arguments ({@link #getParameters()}) are not passed to the action
 * scheme.
 *
 * @author sylvain
 *
 * @param <T>
 *            type of the returned value
 * @deprecated use a behaviour call expression, such as {@code shelf.rename("Classics")}
 */
@ModelEntity
@ImplementationClass(ExecuteFlexoBehaviour.ExecuteFlexoBehaviourImpl.class)
@FML("ExecuteFlexoBehaviour")
@Deprecated
public interface ExecuteFlexoBehaviour<T>
		extends TechnologySpecificActionDefiningReceiver<AbstractFMLRTModelSlot<?, ?, ?>, FlexoConceptInstance, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String FLEXO_BEHAVIOUR_KEY = "flexoBehaviour";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = FLEXO_BEHAVIOUR_KEY)
	public DataBinding<AbstractActionScheme> getFlexoBehaviour();

	@Setter(FLEXO_BEHAVIOUR_KEY)
	public void setFlexoBehaviour(DataBinding<AbstractActionScheme> flexoBehaviour);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = CreateFlexoConceptInstanceParameter.ACTION_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<CreateFlexoConceptInstanceParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<CreateFlexoConceptInstanceParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(CreateFlexoConceptInstanceParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(CreateFlexoConceptInstanceParameter aParameter);

	public static abstract class ExecuteFlexoBehaviourImpl<T>
			extends TechnologySpecificActionDefiningReceiverImpl<AbstractFMLRTModelSlot<?, ?, ?>, FlexoConceptInstance, T>
			implements ExecuteFlexoBehaviour<T> {

		static final Logger logger = Logger.getLogger(FMLRTAction.class.getPackage().getName());

		private DataBinding<AbstractActionScheme> flexoBehaviour;

		@Override
		public DataBinding<AbstractActionScheme> getFlexoBehaviour() {
			if (flexoBehaviour == null) {
				flexoBehaviour = new DataBinding<>(this, AbstractActionScheme.class, BindingDefinitionType.GET);
				flexoBehaviour.setBindingName("flexoBehaviour");
			}
			return flexoBehaviour;
		}

		@Override
		public void setFlexoBehaviour(DataBinding<AbstractActionScheme> flexoBehaviour) {
			if (flexoBehaviour != null) {
				flexoBehaviour.setOwner(this);
				flexoBehaviour.setBindingName("flexoBehaviour");
				flexoBehaviour.setDeclaredType(AbstractActionScheme.class);
				flexoBehaviour.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.flexoBehaviour = flexoBehaviour;
		}

		private AbstractActionScheme getFlexoBehaviour(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			return FMLEvaluation.evaluate(getFlexoBehaviour(), evaluationContext, this);
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Perform perform ExecuteFlexoBehaviour " + evaluationContext);
			}
			FlexoConceptInstance fci = getReceiver(evaluationContext);
			if (fci == null) {
				// A null receiver is an error, no longer a NullPointerException (CORE-D-79)
				throw new FMLNullReferenceError("Null receiver for the call of " + getFlexoBehaviour());
			}
			AbstractActionScheme actionScheme = getFlexoBehaviour(evaluationContext);
			if (actionScheme == null) {
				throw new FMLNullReferenceError("Null behaviour: " + getFlexoBehaviour());
			}
			AbstractActionSchemeActionFactory actionType = actionScheme.getActionFactory(fci);
			AbstractActionSchemeAction<?, ?, ?> actionSchemeAction;

			if (evaluationContext instanceof FlexoBehaviourAction) {
				actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType.makeNewEmbeddedAction(fci, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
			}
			else {
				// Outside a behaviour, the action runs with the editor of the instance, if any: no resource center is needed (CORE-D-79)
				actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType.makeNewAction(fci.getVirtualModelInstance(), null,
						fci.getEditor());
			}
			// Started by an expression: reported to the console only, its failure reaches the expression (CORE-F-11)
			if (!actionSchemeAction.isEmbedded()) {
				actionSchemeAction.setStartedFromExpression(true);
			}
			actionSchemeAction.doAction();

			if (actionSchemeAction.hasActionExecutionSucceeded()) {
				logger.fine("Successfully performed ActionScheme " + getFlexoBehaviour() + " for " + fci);
				return (T) actionSchemeAction.getReturnedValue();
			}
			// The failure of the behaviour, rethrown unchanged (CORE-F-11)
			FMLExecution.rethrowFailure(actionSchemeAction);

			return null;
		}

		@Override
		public Type getAssignableType() {
			return Object.class;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getFlexoBehaviour().rebuild();
		}

	}
}
