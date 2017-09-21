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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * Modelize a call for execution of an FlexoBehaviour
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourPathElement extends FunctionPathElement {

	static final Logger logger = Logger.getLogger(FlexoBehaviourPathElement.class.getPackage().getName());

	public FlexoBehaviourPathElement(BindingPathElement parent, FlexoBehaviour flexoBehaviour, List<DataBinding<?>> args) {
		super(parent, flexoBehaviour, args);

		// Do not instanciate parameters now, we will do it later
		// instanciateParameters(owner);
		if (flexoBehaviour != null) {
			for (FunctionArgument arg : flexoBehaviour.getArguments()) {
				DataBinding<?> argValue = getParameter(arg);
				if (argValue != null && arg != null) {
					argValue.setDeclaredType(arg.getArgumentType());
				}
			}
			setType(flexoBehaviour.getReturnType());
		}
		else {
			logger.warning("Inconsistent data: null FlexoBehaviour");
		}
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
	public Object getBindingValue(Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException, InvocationTargetTransformException {

		/*System.out.println("****************** Computing FlexoBehaviourPathElement");
		System.out.println("getFunction()=" + getFunction());
		System.out.println("target=" + target);
		System.out.println("context=" + context);*/

		// System.out.println("Evaluating getBindingValue() in FlexoBehaviourPathElement for " + getFlexoBehaviour().getSignature() + " in "
		// + getFlexoBehaviour().getFlexoConcept());
		// Thread.dumpStack();

		Object[] args = new Object[getFunction().getArguments().size()];
		int i = 0;

		for (FlexoBehaviourParameter param : getFunction().getArguments()) {
			try {
				args[i] = TypeUtils.castTo(getParameter(param).getBindingValue(context), param.getType());
				// System.out.println("> " + param.getArgumentName() + " " + getParameter(param) + "=" + args[i]);
			} catch (InvocationTargetException e) {
				throw new InvocationTargetTransformException(e);
			}
			i++;
		}
		try {
			// logger.warning("Please implements execution of FlexoBehaviourPathElement here !!!! context=" + context + " of "
			// + context.getClass() + " target=" + target);

			if (/*context instanceof FlexoBehaviourAction &&*/ target instanceof FlexoConceptInstance) {

				// FlexoBehaviourAction action = (FlexoBehaviourAction) context;
				FlexoConceptInstance fci = (FlexoConceptInstance) target;
				AbstractActionSchemeActionFactory actionType = ((AbstractActionScheme) getFlexoBehaviour()).getActionFactory(fci);
				AbstractActionSchemeAction<?, ?, ?> actionSchemeAction = null;

				if (context instanceof FlexoBehaviourAction) {
					actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType
							.makeNewEmbeddedAction(fci.getVirtualModelInstance(), null, (FlexoBehaviourAction) context);
				}
				else {
					FlexoEditor editor = null;

					if (fci.getResourceCenter() instanceof FlexoProject) {
						FlexoProject prj = (FlexoProject) fci.getResourceCenter();
						editor = prj.getServiceManager().getProjectLoaderService().getEditorForProject(prj);
					}

					actionSchemeAction = (AbstractActionSchemeAction<?, ?, ?>) actionType.makeNewAction(fci.getVirtualModelInstance(), null,
							editor);
				}
				for (FlexoBehaviourParameter p : getFlexoBehaviour().getParameters()) {
					DataBinding<?> param = getParameter(p);
					Object paramValue = param.getBindingValue(context);
					// logger.fine("For parameter " + param + " value is " + paramValue);
					if (paramValue != null) {
						actionSchemeAction.setParameterValue(p, paramValue);
					}
				}
				actionSchemeAction.doAction();
				if (actionSchemeAction.hasActionExecutionSucceeded()) {
					logger.fine("Successfully performed ActionScheme " + getFlexoBehaviour() + " for " + fci);
					return actionSchemeAction.getReturnedValue();
				}
				if (actionSchemeAction.getThrownException() != null) {
					throw new InvocationTargetTransformException(new InvocationTargetException(actionSchemeAction.getThrownException()));
				}
			}
			// return getMethodDefinition().getMethod().invoke(target, args);
		} catch (IllegalArgumentException e) {
			StringBuffer warningMessage = new StringBuffer(
					"While evaluating edition scheme " + getFlexoBehaviour() + " exception occured: " + e.getMessage());
			warningMessage.append(", object = " + target);
			for (i = 0; i < getFunction().getArguments().size(); i++) {
				warningMessage.append(", arg[" + i + "] = " + args[i]);
			}
			logger.warning(warningMessage.toString());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;

	}

}
