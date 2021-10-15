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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.NewInstancePathElement;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * Modelize a new instance of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class CreationSchemePathElement extends NewInstancePathElement<CreationScheme> implements PropertyChangeListener {

	static final Logger logger = Logger.getLogger(CreationSchemePathElement.class.getPackage().getName());

	private Type lastKnownType = null;

	private FMLBindingFactory bindingFactory;

	public CreationSchemePathElement(FlexoConceptInstanceType type, IBindingPathElement parent, String constructorName,
			List<DataBinding<?>> args, FMLBindingFactory bindingFactory) {
		super(type, parent, constructorName, args);
		this.bindingFactory = bindingFactory;
	}

	public CreationSchemePathElement(FlexoConceptInstanceType type, IBindingPathElement parent, CreationScheme constructor,
			List<DataBinding<?>> args, FMLBindingFactory bindingFactory) {
		super(type, parent, constructor, args);
		this.bindingFactory = bindingFactory;
	}

	@Override
	public void activate() {
		super.activate();
		// Do not instanciate parameters now, we will do it later
		// instanciateParameters(owner);
		if (getCreationScheme() != null) {
			if (getCreationScheme() != null && getCreationScheme().getPropertyChangeSupport() != null) {
				getCreationScheme().getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			for (FunctionArgument arg : getCreationScheme().getArguments()) {
				DataBinding<?> argValue = getArgumentValue(arg);
				if (argValue != null && arg != null) {
					argValue.setDeclaredType(arg.getArgumentType());
				}
			}
			lastKnownType = getCreationScheme().getReturnType();
		}
		else {
			logger.warning("Inconsistent data: null CreationScheme");
		}
	}

	@Override
	public void desactivate() {
		if (getCreationScheme() != null && getCreationScheme().getPropertyChangeSupport() != null) {
			getCreationScheme().getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.desactivate();
	}

	@Override
	public FlexoConceptInstanceType getType() {
		if (getCreationScheme() != null) {
			return (FlexoConceptInstanceType) getCreationScheme().getReturnType();
		}
		return (FlexoConceptInstanceType) super.getType();
	}

	public CreationScheme getCreationScheme() {
		return getFunction();
	}

	@Override
	public String getLabel() {
		return getCreationScheme().getSignature();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return getCreationScheme().getDescription();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getCreationScheme()) {
			if (evt.getPropertyName().equals(FlexoBehaviourParameter.NAME_KEY)) {
				// System.out.println("Notify behaviour name changing for " + getFlexoBehaviour() + " new=" +
				// getFlexoBehaviour().getName());
				clearSerializationRepresentation();
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != null
						&& getCreationScheme().getFlexoConcept().getBindingModel() != null
						&& getCreationScheme().getFlexoConcept().getBindingModel().getPropertyChangeSupport() != null) {
					getCreationScheme().getFlexoConcept().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_NAME_CHANGED, null, this);
				}
			}
			if (lastKnownType != getType()) {
				lastKnownType = getType();
				clearSerializationRepresentation();
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != null
						&& getCreationScheme().getFlexoConcept().getBindingModel() != null
						&& getCreationScheme().getFlexoConcept().getBindingModel().getPropertyChangeSupport() != null) {
					getCreationScheme().getFlexoConcept().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_TYPE_CHANGED, null, this);
				}
			}

		}
	}

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
				// TODO
				// check code on AddVirtualModelInstance.makeNewFlexoConceptInstance
				logger.warning("New VirtualModel instance not supported yet !!!");
				return null;
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

	@Override
	public FunctionPathElement transform(ExpressionTransformer transformer) throws TransformException {
		return this;
	}

	@Override
	public String getSerializationRepresentation() {
		// if (serializationRepresentation == null) {
		StringBuffer returned = new StringBuffer();
		if (getFunction() != null) {
			returned.append("new " + TypeUtils.simpleRepresentation(getType()) + "(");
			boolean isFirst = true;
			for (Function.FunctionArgument a : getFunction().getArguments()) {
				returned.append((isFirst ? "" : ",") + getArgumentValue(a));
				isFirst = false;
			}
			returned.append(")");
		}
		else {
			returned.append("unknown_new()");
		}
		// serializationRepresentation = returned.toString();
		return returned.toString();
		// }
		// return serializationRepresentation;
	}

	@Override
	public BindingPathCheck checkBindingPathIsValid(IBindingPathElement parentElement, Type parentType) {

		BindingPathCheck check = super.checkBindingPathIsValid(parentElement, parentType);

		if (parentType != null) {
			if (getParent() == null) {
				check.invalidBindingReason = "No parent for: " + this;
				check.valid = false;
				return check;
			}

			if (getParent() != parentElement) {
				check.invalidBindingReason = "Inconsistent parent for: " + this;
				check.valid = false;
				return check;
			}

			if (!TypeUtils.isTypeAssignableFrom(parentElement.getType(), getParent().getType(), true)) {
				check.invalidBindingReason = "Mismatched: " + parentElement.getType() + " and " + getParent().getType();
				check.valid = false;
				return check;
			}

			if (parentType instanceof FlexoConceptInstanceType) {
				FlexoConcept parentContext = ((FlexoConceptInstanceType) parentType).getFlexoConcept();
				if (parentContext instanceof VirtualModel) {
					VirtualModel vm = (VirtualModel) parentContext;
					if (!vm.getAllRootFlexoConcepts().contains(getCreationScheme().getFlexoConcept())) {
						check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName() + " in "
								+ parentContext.getName();
						check.valid = false;
						return check;
					}
				}
				else if (parentContext instanceof FlexoConcept) {
					if (!parentContext.getEmbeddedFlexoConcepts().contains(getCreationScheme().getFlexoConcept())) {
						check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName() + " in "
								+ parentContext.getName();
						check.valid = false;
						return check;
					}
				}
			}
		}

		check.returnedType = getType();
		check.valid = true;
		return check;
	}

	@Override
	public boolean requiresContext() {
		return false;
	}

	private boolean performExecuteCreationScheme(FlexoConceptInstance newInstance, VirtualModelInstance<?, ?> vmInstance,
			BindingEvaluationContext evaluationContext) throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {

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

	@Override
	public boolean isResolved() {
		return getCreationScheme() != null;
	}

	@Override
	public void resolve() {
		CreationScheme function = (CreationScheme) bindingFactory.retrieveConstructor(getType(),
				getParent() != null ? getParent().getType() : null, getParsed(), getArguments());
		setFunction(function);
		if (function == null) {
			logger.warning("cannot find constructor " + getParsed() + " for type " + getType() + " with arguments " + getArguments());
		}
	}

}
