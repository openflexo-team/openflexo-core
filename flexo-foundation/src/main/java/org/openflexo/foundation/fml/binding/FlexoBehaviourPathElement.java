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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance.FlexoConceptInstanceImpl.SuperReference;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.action.SuperCreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SuperCreationSchemeActionFactory;

/**
 * Modelize a call for execution of an FlexoBehaviour
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourPathElement extends FunctionPathElement implements PropertyChangeListener {

	static final Logger logger = Logger.getLogger(FlexoBehaviourPathElement.class.getPackage().getName());

	private Type lastKnownType = null;

	public FlexoBehaviourPathElement(IBindingPathElement parent, FlexoBehaviour flexoBehaviour, List<DataBinding<?>> args) {
		super(parent, flexoBehaviour, args);

	}

	@Override
	public void activate() {
		super.activate();
		// Do not instanciate parameters now, we will do it later
		// instanciateParameters(owner);
		if (getFlexoBehaviour() != null) {
			if (getFlexoBehaviour() != null && getFlexoBehaviour().getPropertyChangeSupport() != null) {
				getFlexoBehaviour().getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			for (FunctionArgument arg : getFlexoBehaviour().getArguments()) {
				DataBinding<?> argValue = getParameter(arg);
				if (argValue != null && arg != null) {
					argValue.setDeclaredType(arg.getArgumentType());
				}
			}
			lastKnownType = getFlexoBehaviour().getReturnType();
		}
		else {
			logger.warning("Inconsistent data: null FlexoBehaviour");
		}
	}

	@Override
	public void desactivate() {
		if (getFlexoBehaviour() != null && getFlexoBehaviour().getPropertyChangeSupport() != null) {
			getFlexoBehaviour().getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.desactivate();
	}

	@Override
	public Type getType() {
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getReturnType();
		}
		return super.getType();
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return getFunction();
	}

	@Override
	public FlexoBehaviour getFunction() {
		return (FlexoBehaviour) super.getFunction();
	}

	@Override
	public String getLabel() {
		return getFlexoBehaviour().getSignature();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return getFlexoBehaviour().getDescription();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getFlexoBehaviour()) {
			if (evt.getPropertyName().equals(FlexoBehaviourParameter.NAME_KEY)) {
				// System.out.println("Notify behaviour name changing for " + getFlexoBehaviour() + " new=" +
				// getFlexoBehaviour().getName());
				serializationRepresentation = null;
				if (getFlexoBehaviour() != null && getFlexoBehaviour().getFlexoConcept() != null
						&& getFlexoBehaviour().getFlexoConcept().getBindingModel() != null
						&& getFlexoBehaviour().getFlexoConcept().getBindingModel().getPropertyChangeSupport() != null) {
					getFlexoBehaviour().getFlexoConcept().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_NAME_CHANGED, null, this);
				}
			}
			if (lastKnownType != getType()) {
				lastKnownType = getType();
				serializationRepresentation = null;
				if (getFlexoBehaviour() != null && getFlexoBehaviour().getFlexoConcept() != null
						&& getFlexoBehaviour().getFlexoConcept().getBindingModel() != null
						&& getFlexoBehaviour().getFlexoConcept().getBindingModel().getPropertyChangeSupport() != null) {
					getFlexoBehaviour().getFlexoConcept().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_TYPE_CHANGED, null, this);
				}
			}

		}
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException, InvocationTargetTransformException {

		try {

			FlexoConcept conceptualLevel = null;

			// In this case, caller is 'super', we should identify the conceptual level of execution
			if (target instanceof SuperReference) {
				conceptualLevel = ((SuperReference) target).getSuperConcept();
				target = ((SuperReference) target).getInstance();
			}

			if (target instanceof FlexoConceptInstance) {

				FlexoConceptInstance fci = (FlexoConceptInstance) target;

				// This is "normal" execution context
				if (getFlexoBehaviour() instanceof AbstractActionScheme) {

					AbstractActionSchemeActionFactory actionType = ((AbstractActionScheme) getFlexoBehaviour()).getActionFactory(fci);
					AbstractActionSchemeAction<?, ?, ?> actionSchemeAction = null;

					if (context instanceof FlexoBehaviourAction) {
						actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType
								.makeNewEmbeddedAction(fci.getVirtualModelInstance(), null, (FlexoBehaviourAction<?, ?, ?>) context);
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

							actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType
									.makeNewAction(fci.getVirtualModelInstance(), null, editor);
						}
					}
					actionSchemeAction.setDeclaredConceptualLevel(conceptualLevel);

					for (FlexoBehaviourParameter p : getFlexoBehaviour().getParameters()) {
						DataBinding<?> param = getParameter(p);
						Object paramValue = TypeUtils.castTo(param.getBindingValue(context), p.getType());
						// logger.fine("For parameter " + param + " value is " + paramValue);
						if (paramValue != null) {
							actionSchemeAction.setParameterValue(p, paramValue);
						}
					}
					actionSchemeAction.doAction();

					if (actionSchemeAction.hasActionExecutionSucceeded()) {
						logger.fine("Successfully performed FlexoBehaviour " + getFlexoBehaviour() + " for " + fci);
						return actionSchemeAction.getReturnedValue();
					}
					if (actionSchemeAction.getThrownException() != null) {
						throw new InvocationTargetTransformException(
								new InvocationTargetException(actionSchemeAction.getThrownException()));
					}
				}

				// This is a special context, where we are executing a CreationScheme and where we want to
				// call a super-concept CreationScheme
				else if (((context instanceof CreationSchemeAction) || (context instanceof SuperCreationSchemeAction))
						&& getFlexoBehaviour() instanceof CreationScheme) {
					SuperCreationSchemeActionFactory actionType = ((CreationScheme) getFlexoBehaviour())
							.getSuperCreationSchemeActionFactory(fci);
					SuperCreationSchemeAction actionSchemeAction = actionType.makeNewEmbeddedAction(fci.getVirtualModelInstance(), null,
							(FlexoBehaviourAction) context);

					actionSchemeAction.setDeclaredConceptualLevel(conceptualLevel);

					for (FlexoBehaviourParameter p : getFlexoBehaviour().getParameters()) {
						DataBinding<?> param = getParameter(p);
						Object paramValue = TypeUtils.castTo(param.getBindingValue(context), p.getType());
						// logger.fine("For parameter " + param + " value is " + paramValue);
						if (paramValue != null) {
							actionSchemeAction.setParameterValue(p, paramValue);
						}
					}
					actionSchemeAction.doAction();

					if (actionSchemeAction.hasActionExecutionSucceeded()) {
						logger.fine("Successfully performed FlexoBehaviour " + getFlexoBehaviour() + " for " + fci);
						return actionSchemeAction.getReturnedValue();
					}
					if (actionSchemeAction.getThrownException() != null) {
						throw new InvocationTargetTransformException(
								new InvocationTargetException(actionSchemeAction.getThrownException()));
					}
				}

				else {
					logger.warning("Unexpected behaviour " + getFlexoBehaviour().getSignature() + " for context: " + context);
				}
			}

			else {
				logger.warning("Don't know what to do with " + target);
			}
			// return getMethodDefinition().getMethod().invoke(target, args);
		} catch (

		IllegalArgumentException e) {
			StringBuffer warningMessage = new StringBuffer(
					"While evaluating edition scheme " + getFlexoBehaviour() + " exception occured: " + e.getMessage());
			warningMessage.append(", object = " + target);
			/*for (i = 0; i < getFunction().getArguments().size(); i++) {
				warningMessage.append(", arg[" + i + "] = " + args[i]);
			}*/
			logger.warning(warningMessage.toString());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public FunctionPathElement transform(ExpressionTransformer transformer) throws TransformException {
		return this;
	}

	@Override
	public String getSerializationRepresentation() {
		return super.getSerializationRepresentation();
	}

	@Override
	public boolean isNotificationSafe() {
		// By default, we assume that the result of the execution of a FlexoBehaviour is not notification-safe
		// (we cannot rely on the fact that a notification will be thrown if the result of the execution of the behaviour change)
		return false;
	}

}
