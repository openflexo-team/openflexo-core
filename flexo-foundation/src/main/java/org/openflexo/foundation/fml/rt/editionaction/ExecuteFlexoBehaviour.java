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
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
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
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * This action is used to execute a FlexoBehaviour in a {@link FlexoConceptInstance}
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(ExecuteFlexoBehaviour.ExecuteFlexoBehaviourImpl.class)
@XMLElement
@FML("ExecuteFlexoBehaviour")
public interface ExecuteFlexoBehaviour<T> extends TechnologySpecificActionDefiningReceiver<FMLRTModelSlot<?, ?>, FlexoConceptInstance, T> {

	// <FCI extends FlexoConceptInstance, VMI extends VirtualModelInstance<VMI, ?>>
	// public interface FMLRTAction<T extends VirtualModelInstanceObject, VMI extends VirtualModelInstance<VMI, ?>>
	// extends TechnologySpecificActionDefiningReceiver<FMLRTModelSlot<VMI, ?>, VMI, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String FLEXO_BEHAVIOUR_KEY = "flexoBehaviour";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = FLEXO_BEHAVIOUR_KEY)
	@XMLAttribute
	public DataBinding<AbstractActionScheme> getFlexoBehaviour();

	@Setter(FLEXO_BEHAVIOUR_KEY)
	public void setFlexoBehaviour(DataBinding<AbstractActionScheme> flexoBehaviour);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = CreateFlexoConceptInstanceParameter.ACTION_KEY)
	@XMLElement
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
			extends TechnologySpecificActionDefiningReceiverImpl<FMLRTModelSlot<?, ?>, FlexoConceptInstance, T>
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

		private AbstractActionScheme getFlexoBehaviour(RunTimeEvaluationContext evaluationContext) {
			try {
				return getFlexoBehaviour().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Perform perform ExecuteFlexoBehaviour " + evaluationContext);
			}
			FlexoConceptInstance fci = getReceiver(evaluationContext);
			AbstractActionScheme actionScheme = getFlexoBehaviour(evaluationContext);
			AbstractActionSchemeActionFactory actionType = actionScheme.getActionFactory(fci);
			AbstractActionSchemeAction<?, ?, ?> actionSchemeAction = null;

			if (evaluationContext instanceof FlexoBehaviourAction) {
				actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType.makeNewEmbeddedAction(fci, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
			}
			else {
				FlexoEditor editor = null;
				if (fci.getResourceCenter() != null) {
					if (fci.getResourceCenter() instanceof FlexoProject) {
						FlexoProject<?> prj = (FlexoProject<?>) fci.getResourceCenter();
						editor = prj.getServiceManager().getProjectLoaderService().getEditorForProject(prj);
					}
					else if (fci.getResourceCenter().getDelegatingProjectResource() != null) {
						FlexoProject<?> prj = fci.getResourceCenter().getDelegatingProjectResource().getFlexoProject();
						editor = prj.getServiceManager().getProjectLoaderService().getEditorForProject(prj);
					}

					actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType.makeNewAction(fci.getVirtualModelInstance(), null,
							editor);
				}
			}
			// TODO
			/*for (FlexoBehaviourParameter p : actionScheme.getParameters()) {
				DataBinding<?> param = getParameter(p);
				Object paramValue = TypeUtils.castTo(param.getBindingValue(context), p.getType());
				// logger.fine("For parameter " + param + " value is " + paramValue);
				if (paramValue != null) {
					actionSchemeAction.setParameterValue(p, paramValue);
				}
			}*/
			actionSchemeAction.doAction();

			if (actionSchemeAction.hasActionExecutionSucceeded()) {
				logger.fine("Successfully performed ActionScheme " + getFlexoBehaviour() + " for " + fci);
				return (T) actionSchemeAction.getReturnedValue();
			}
			if (actionSchemeAction.getThrownException() != null) {
				throw new FMLExecutionException(new InvocationTargetException(actionSchemeAction.getThrownException()));
			}

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
