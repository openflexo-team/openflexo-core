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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Modelize a new instance of a given {@link FlexoConcept} using a {@link CreationScheme}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(CreationSchemePathElement.CreationSchemePathElementImpl.class)
@FML("CreationSchemePathElement")
public interface CreationSchemePathElement extends AbstractCreationSchemePathElement<CreationScheme> {

	public abstract class CreationSchemePathElementImpl extends AbstractCreationSchemePathElementImpl<CreationScheme>
			implements CreationSchemePathElement {

		static final Logger logger = Logger.getLogger(CreationSchemePathElement.class.getPackage().getName());

		@Override
		public Object getBindingValue(Object target, BindingEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, InvocationTargetTransformException {

			System.out.println("Executing CreationSchemePathElement: " + this);
			System.out.println("target=" + target);
			System.out.println("evaluationContext=" + evaluationContext);

			try {

				FlexoConceptInstance container = null;

				if (target == null && evaluationContext instanceof FlexoBehaviourAction) {
					container = ((FlexoBehaviourAction) evaluationContext).getFlexoConceptInstance();
				}

				if (target instanceof FlexoConceptInstance) {
					container = (FlexoConceptInstance) target;
				}

				if (container == null) {
					throw new NullReferenceException("Unable to find executable context for " + this);
				}

				if (getCreationScheme().getFlexoConcept() instanceof VirtualModel) {

					String vmiName = getVirtualModelInstanceName().getBindingValue(evaluationContext);

					/*System.out.println("getVirtualModelInstanceName()=" + getVirtualModelInstanceName());
					System.out.println("valid=" + getVirtualModelInstanceName().isValid());
					System.out.println("reason=" + getVirtualModelInstanceName().invalidBindingReason());
					System.out.println("BM=" + getBindingModel());
					System.out.println("vmiName=" + vmiName);*/

					VirtualModel instantiatedVirtualModel = (VirtualModel) getCreationScheme().getFlexoConcept();

					// TODO: manage container
					logger.warning("What about the container ??? " + container);

					CreateBasicVirtualModelInstance createVMIAction = CreateBasicVirtualModelInstance.actionType
							.makeNewEmbeddedAction(container, null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
					createVMIAction.setSkipChoosePopup(true);
					createVMIAction.setNewVirtualModelInstanceName(vmiName);
					createVMIAction.setVirtualModel(instantiatedVirtualModel);
					createVMIAction.setCreationScheme(getCreationScheme());

					for (FunctionArgument functionArgument : getFunctionArguments()) {
						// System.out.println("functionArgument:" + functionArgument + " = " + getArgumentValue(functionArgument));
						Object v = getArgumentValue(functionArgument).getBindingValue(evaluationContext);
						// System.out.println("values:" + v);
						createVMIAction.setParameterValue((FlexoBehaviourParameter) functionArgument, v);
					}

					createVMIAction.doAction();
					FMLRTVirtualModelInstance returned = createVMIAction.getNewVirtualModelInstance();
					// System.out.println("returned=" + returned);
					return returned;
				}
				else {
					FlexoConceptInstance newFCI = container.getVirtualModelInstance()
							.makeNewFlexoConceptInstance(getCreationScheme().getFlexoConcept(), container);
					if (getCreationScheme().getFlexoConcept().getContainerFlexoConcept() != null) {
						container.addToEmbeddedFlexoConceptInstances(newFCI);
					}

					if (performExecuteCreationScheme(newFCI, container.getVirtualModelInstance(), evaluationContext)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Successfully performed performAddFlexoConcept " + evaluationContext);
						}
						return newFCI;
					}
					else {
						logger.warning("Failing execution of creationScheme: " + getCreationScheme());
					}
				}

			} catch (IllegalArgumentException e) {
				StringBuffer warningMessage = new StringBuffer(
						"While evaluating edition scheme " + getCreationScheme() + " exception occured: " + e.getMessage());
				warningMessage.append(", object = " + target);
				logger.warning(warningMessage.toString());
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new InvocationTargetTransformException(e);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
				throw new InvocationTargetTransformException(e);
			}
			return null;

		}

		private boolean performExecuteCreationScheme(FlexoConceptInstance newInstance, VirtualModelInstance<?, ?> vmInstance,
				BindingEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {

			if (evaluationContext instanceof FlexoBehaviourAction) {
				CreationSchemeAction creationSchemeAction = new CreationSchemeAction(getCreationScheme(), vmInstance, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				creationSchemeAction.initWithFlexoConceptInstance(newInstance);

				for (FlexoBehaviourParameter p : getCreationScheme().getParameters()) {
					DataBinding<?> param = getArgumentValue(p);
					Object paramValue = TypeUtils.castTo(param.getBindingValue(evaluationContext), p.getType());
					System.out.println("For parameter " + param + " value is " + paramValue);
					if (paramValue != null) {
						creationSchemeAction.setParameterValue(p, paramValue);
					}
				}

				creationSchemeAction.doAction();

				return creationSchemeAction.hasActionExecutionSucceeded();

			}
			logger.warning("Unexpected: " + evaluationContext);
			Thread.dumpStack();
			return false;
		}

	}
}
