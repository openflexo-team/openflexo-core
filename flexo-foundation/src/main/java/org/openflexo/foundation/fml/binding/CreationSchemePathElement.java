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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.NewInstancePathElement;
import org.openflexo.connie.exception.InvocationTargetTransformException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.TypingSpace;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.AbstractCreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

/**
 * Modelize a new instance of a given {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(CreationSchemePathElement.CreationSchemePathElementImpl.class)
@FML("CreationSchemePathElement")
public interface CreationSchemePathElement<CS extends AbstractCreationScheme>
		extends FMLObject, NewInstancePathElement<CS>, PropertyChangeListener {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_NAME_KEY = "name";
	@Deprecated /* Will disappear */
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_TITLE_KEY = "virtualModelInstanceTitle";
	// @PropertyIdentifier(type = DataBinding.class)
	// public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RESOURCE_URI_KEY = "uri";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RESOURCE_CENTER_KEY = "rc";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DYNAMIC_RELATIVE_PATH_KEY = "path";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String REPOSITORY_FOLDER_KEY = "folder";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_NAME_KEY, ignoreForEquality = true)
	@XMLAttribute
	@FMLAttribute(value = VIRTUAL_MODEL_INSTANCE_NAME_KEY, required = true)
	public DataBinding<String> getVirtualModelInstanceName();

	@Setter(VIRTUAL_MODEL_INSTANCE_NAME_KEY)
	public void setVirtualModelInstanceName(DataBinding<String> virtualModelInstanceName);

	@Deprecated /* Will disappear */
	@Getter(value = VIRTUAL_MODEL_INSTANCE_TITLE_KEY, ignoreForEquality = true)
	@XMLAttribute
	@FMLAttribute(value = VIRTUAL_MODEL_INSTANCE_TITLE_KEY, required = false)
	public DataBinding<String> getVirtualModelInstanceTitle();

	@Deprecated /* Will disappear */
	@Setter(VIRTUAL_MODEL_INSTANCE_TITLE_KEY)
	public void setVirtualModelInstanceTitle(DataBinding<String> virtualModelInstanceTitle);

	@Getter(value = RESOURCE_URI_KEY)
	@XMLAttribute
	@FMLAttribute(value = RESOURCE_URI_KEY, required = false)
	public DataBinding<String> getResourceURI();

	@Setter(RESOURCE_URI_KEY)
	public void setResourceURI(DataBinding<String> resourceURI);

	@Getter(value = RESOURCE_CENTER_KEY)
	@XMLAttribute
	@FMLAttribute(value = RESOURCE_CENTER_KEY, required = false)
	public DataBinding<FlexoResourceCenter<?>> getResourceCenter();

	@Setter(RESOURCE_CENTER_KEY)
	public void setResourceCenter(DataBinding<FlexoResourceCenter<?>> resourceCenter);

	@Getter(value = DYNAMIC_RELATIVE_PATH_KEY)
	@XMLAttribute
	@FMLAttribute(value = DYNAMIC_RELATIVE_PATH_KEY, required = false)
	public DataBinding<String> getDynamicRelativePath();

	@Setter(DYNAMIC_RELATIVE_PATH_KEY)
	public void setDynamicRelativePath(DataBinding<String> relativePath);

	@Getter(value = REPOSITORY_FOLDER_KEY)
	@XMLAttribute
	@FMLAttribute(value = REPOSITORY_FOLDER_KEY, required = false)
	public DataBinding<RepositoryFolder> getRepositoryFolder();

	@Setter(REPOSITORY_FOLDER_KEY)
	public void setRepositoryFolder(DataBinding<RepositoryFolder> folder);

	@Override
	public FlexoConceptInstanceType getType();

	public CS getCreationScheme();

	public abstract class CreationSchemePathElementImpl<CS extends AbstractCreationScheme> extends FMLNewInstancePathElementImpl<CS>
			implements CreationSchemePathElement<CS> {

		static final Logger logger = Logger.getLogger(CreationSchemePathElement.class.getPackage().getName());

		private Type lastKnownType = null;

		private DataBinding<String> virtualModelInstanceName;
		// private DataBinding<FlexoConceptInstance> container;

		private DataBinding<String> dynamicRelativePath;
		private DataBinding<String> resourceURI;
		private DataBinding<FlexoResourceCenter<?>> resourceCenter;
		private DataBinding<RepositoryFolder> repositoryFolder;

		@Override
		public void activate(BindingPath bindingPath) {
			super.activate(bindingPath);
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
				if (getCreationScheme().getNewInstanceType() instanceof FlexoConceptInstanceType) {
					return (FlexoConceptInstanceType) getCreationScheme().getNewInstanceType();
				}
				return null;
			}
			return (FlexoConceptInstanceType) super.getType();
		}

		public FlexoConcept getFlexoConcept() {
			if (getType() != null) {
				return getType().getFlexoConcept();
			}
			return null;
		}

		@Override
		public CS getCreationScheme() {
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
		public DataBinding<String> getVirtualModelInstanceName() {
			if (virtualModelInstanceName == null) {
				virtualModelInstanceName = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				virtualModelInstanceName.setBindingName("virtualModelInstanceName");
			}
			return virtualModelInstanceName;
		}

		@Override
		public void setVirtualModelInstanceName(DataBinding<String> aVirtualModelInstanceName) {
			if (aVirtualModelInstanceName != null) {
				aVirtualModelInstanceName.setOwner(this);
				aVirtualModelInstanceName.setBindingName("virtualModelInstanceName");
				aVirtualModelInstanceName.setDeclaredType(String.class);
				aVirtualModelInstanceName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstanceName = aVirtualModelInstanceName;
		}

		@Override
		public DataBinding<String> getResourceURI() {
			if (resourceURI == null) {
				resourceURI = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				resourceURI.setBindingName("resourceURI");
			}
			return resourceURI;
		}

		@Override
		public void setResourceURI(DataBinding<String> resourceURI) {
			if (resourceURI != null) {
				resourceURI.setOwner(this);
				resourceURI.setDeclaredType(String.class);
				resourceURI.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				resourceURI.setBindingName("resourceURI");
			}
			this.resourceURI = resourceURI;
		}

		@Override
		public DataBinding<String> getDynamicRelativePath() {
			if (dynamicRelativePath == null) {
				dynamicRelativePath = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				dynamicRelativePath.setBindingName("dynamicRelativePath");
			}
			return dynamicRelativePath;
		}

		@Override
		public void setDynamicRelativePath(DataBinding<String> dynamicRelativePath) {
			if (dynamicRelativePath != null) {
				dynamicRelativePath.setOwner(this);
				dynamicRelativePath.setDeclaredType(String.class);
				dynamicRelativePath.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				dynamicRelativePath.setBindingName("dynamicRelativePath");
			}
			this.dynamicRelativePath = dynamicRelativePath;
		}

		@Override
		public DataBinding<FlexoResourceCenter<?>> getResourceCenter() {
			if (resourceCenter == null) {
				resourceCenter = new DataBinding<>(this, FlexoResourceCenter.class, DataBinding.BindingDefinitionType.GET);
				resourceCenter.setBindingName("resourceCenter");
			}
			return resourceCenter;
		}

		@Override
		public void setResourceCenter(DataBinding<FlexoResourceCenter<?>> resourceCenter) {
			if (resourceCenter != null) {
				resourceCenter.setOwner(this);
				resourceCenter.setDeclaredType(FlexoResourceCenter.class);
				resourceCenter.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				resourceCenter.setBindingName("resourceCenter");
			}
			this.resourceCenter = resourceCenter;
		}

		@Override
		public DataBinding<RepositoryFolder> getRepositoryFolder() {
			if (repositoryFolder == null) {
				repositoryFolder = new DataBinding<>(this, RepositoryFolder.class, DataBinding.BindingDefinitionType.GET);
				repositoryFolder.setBindingName("repositoryFolder");
			}
			return repositoryFolder;
		}

		@Override
		public void setRepositoryFolder(DataBinding<RepositoryFolder> repositoryFolder) {
			if (repositoryFolder != null) {
				repositoryFolder.setOwner(this);
				repositoryFolder.setDeclaredType(RepositoryFolder.class);
				repositoryFolder.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				repositoryFolder.setBindingName("repositoryFolder");
			}
			this.repositoryFolder = repositoryFolder;
		}

		/*@Override
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
		
		}*/

		@Override
		public CreationSchemePathElement transform(ExpressionTransformer transformer) throws TransformException {
			return this;
		}

		@Override
		public String getSerializationRepresentation() {
			// if (serializationRepresentation == null) {

			StringBuffer returned = new StringBuffer();

			if (isResolved()) {
				if ((getFunction() instanceof CreationScheme && ((CreationScheme) getFunction()).isAnonymous())
						|| getFunction().isDefaultCreationScheme()) {
					returned.append("new " + TypeUtils.simpleRepresentation(getType()) + "(");
				}
				else {
					returned.append("new " + TypeUtils.simpleRepresentation(getType()) + "::" + getFunction().getName() + "(");
				}
				boolean isFirst = true;
				for (Function.FunctionArgument a : getFunction().getArguments()) {
					returned.append((isFirst ? "" : ",") + getArgumentValue(a));
					isFirst = false;
				}
				returned.append(")");
			}
			else {
				returned.append("new " + TypeUtils.simpleRepresentation(getType()) + (getParsed() != null ? "::" + getParsed() : "") + "(");
				boolean isFirst = true;
				if (getArguments() != null) {
					for (DataBinding<?> arg : getArguments()) {
						returned.append((isFirst ? "" : ",") + arg);
						isFirst = false;
					}
				}
				returned.append(")");
			}

			if (hasWithClause()) {
				returned.append(" with (");
				boolean isFirst = true;

				// System.out.println(">>>> properties");
				for (FMLPropertyValue<?, ?> propertyValue : getFMLPropertyValues()) {
					// System.out.println("> " + propertyValue + " " + propertyValue.getPropertyName() + "=" + propertyValue.getValue());
					returned.append((isFirst ? "" : ",") + propertyValue.getPropertyName() + "=" + propertyValue.getValue());
					isFirst = false;
				}
				// System.out.println("<<<< done");

				returned.append(")");
			}

			// serializationRepresentation = returned.toString();
			return returned.toString();
			// }
			// return serializationRepresentation;
		}

		private boolean hasWithClause() {
			return getFMLPropertyValues() != null && getFMLPropertyValues().size() > 0;
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
						if (getFlexoConcept() instanceof VirtualModel) {
							// A VirtualModel inside another VirtualModel
							if (!(((VirtualModel) getFlexoConcept()).getContainerVirtualModel()).isAssignableFrom(vm)) {
								check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName()
										+ " in " + parentContext.getName();
								check.valid = false;
								return check;
							}
						}
						else {
							// A simple FlexoConcept in a VirtualModel
							if (!vm.getAllRootFlexoConcepts().contains(getCreationScheme().getFlexoConcept())) {
								check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName()
										+ " in " + parentContext.getName();
								check.valid = false;
								return check;
							}
						}
					}
					else if (parentContext instanceof FlexoConcept) {

						FlexoConcept applicableContainer = getCreationScheme().getFlexoConcept().getApplicableContainerFlexoConcept();

						if (applicableContainer == null) {
							check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName() + " in "
									+ parentContext.getName() + " because no applicable container declared for this constructor";
							check.valid = false;
							return check;
						}
						else if (!applicableContainer.isAssignableFrom(parentContext)) {
							check.invalidBindingReason = "cannot instantiate " + getCreationScheme().getFlexoConcept().getName() + " in "
									+ parentContext.getName() + " because no applicable container is not assignable from "
									+ parentContext.getName();
							check.valid = false;
							return check;
						}
					}
				}
			}

			for (FMLPropertyValue<?, ?> pValue : getFMLPropertyValues()) {
				if (pValue.getProperty() == null && StringUtils.isNotEmpty(pValue.getUnresolvedPropertyName())) {
					check.invalidBindingReason = "unresolved property " + pValue.getUnresolvedPropertyName();
					check.valid = false;
					return check;
				}
			}

			if (getVirtualModelInstanceName() != null && getVirtualModelInstanceName().isSet()) {
				if (!getVirtualModelInstanceName().isValid()) {
					check.invalidBindingReason = "unresolved property " + getVirtualModelInstanceName().invalidBindingReason();
					check.valid = false;
					return check;
				}
			}
			if (getResourceURI() != null && getResourceURI().isSet()) {
				if (!getResourceURI().isValid()) {
					check.invalidBindingReason = "unresolved property " + getResourceURI().invalidBindingReason();
					check.valid = false;
					return check;
				}
			}
			if (getDynamicRelativePath() != null && getDynamicRelativePath().isSet()) {
				if (!getDynamicRelativePath().isValid()) {
					check.invalidBindingReason = "unresolved property " + getDynamicRelativePath().invalidBindingReason();
					check.valid = false;
					return check;
				}
			}
			if (getResourceCenter() != null && getResourceCenter().isSet()) {
				if (!getResourceCenter().isValid()) {
					check.invalidBindingReason = "unresolved property " + getResourceCenter().invalidBindingReason();
					check.valid = false;
					return check;
				}
			}
			if (getRepositoryFolder() != null && getRepositoryFolder().isSet()) {
				if (!getRepositoryFolder().isValid()) {
					check.invalidBindingReason = "unresolved property " + getRepositoryFolder().invalidBindingReason();
					check.valid = false;
					return check;
				}
			}
			if (getFlexoConcept() instanceof VirtualModel) {
				if (!getVirtualModelInstanceName().isSet()) {
					check.invalidBindingReason = "cannot instantiate " + getFlexoConcept().getName()
							+ " because new VirtualModelInstance name was not set. Use [with(name=...)]";
					check.valid = false;
					return check;
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

		/*private boolean performExecuteCreationScheme(FlexoConceptInstance newInstance, VirtualModelInstance<?, ?> vmInstance,
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
		}*/

		@Override
		public boolean isResolved() {
			return getCreationScheme() != null;
		}

		@Override
		public void invalidate() {
			invalidate(null);
		}

		@Override
		public void invalidate(TypingSpace typingSpace) {
			if (getType() != null && typingSpace != null) {
				FlexoConceptInstanceType translatedType = getType().translateTo(typingSpace);
				setFunction(null);
				setType(translatedType);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void resolve() {

			// System.out.println("resolve() CreationSchemePathElement ");
			// System.out.println("type=" + getType());
			// System.out.println("resolved=" + getType().isResolved());
			// System.out.println("name=" + getParsed());
			// System.out.println("args=" + getArguments());
			// System.out.println("bindable=" + getBindable());
			// System.out.println("bindable.getBindingFactory()=" + getBindable().getBindingFactory());

			if (getBindable() == null || getBindable().getBindingFactory() == null) {
				return;
			}

			CS function = (CS) ((FMLBindingFactory) getBindable().getBindingFactory()).retrieveConstructor(getType(),
					getParent() != null ? getParent().getType() : null, getParsed(), getArguments());
			/*System.out.println("########## Je cherche le constructeur " + getParsed() + " pour " + getType());
			System.out.println("Je retourne: " + function);
			if (function != null) {
				System.out.println(function.getFMLPrettyPrint());
			}*/
			setFunction(function);
			if (function == null && getType().isResolved()) {
				// Do not warn for unresolved type
				// logger.warning("cannot find constructor " + getParsed() + " for type " + getType() + " with arguments " + getArguments()
				// + (getParent() != null ? " and parent " + getParent().getType() : ""));
				// System.out.println("type: " + getType() + " resolved=" + getType().isResolved());
				// System.out.println("arguments: " + getArguments() + " size: " + getArguments().size());
			}
		}

		@Override
		public Object getBindingValue(Object target, BindingEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, InvocationTargetTransformException {

			// System.out.println("Executing CreationSchemePathElement: " + this);
			// System.out.println("target=" + target);
			// System.out.println("evaluationContext=" + evaluationContext);

			try {

				FlexoObject container = null;

				if (target == null && evaluationContext instanceof FlexoBehaviourAction) {
					container = ((FlexoBehaviourAction) evaluationContext).getFlexoConceptInstance();
				}

				if (target instanceof FlexoConceptInstance) {
					container = (FlexoConceptInstance) target;
				}

				if (container == null) {
					if (evaluationContext instanceof RunTimeEvaluationContext) {
						FlexoObject focusedObject = ((RunTimeEvaluationContext) evaluationContext).getFocusedObject();
						// System.out.println("focusedObject=" + focusedObject);
						if (focusedObject instanceof RepositoryFolder || focusedObject instanceof FlexoConceptInstance) {
							container = focusedObject;
						}
					}
				}
				// System.out.println("container=" + container);

				if (container == null) {
					throw new NullReferenceException("Unable to find executable context for " + this);
				}

				// Special case to create VirtualModelInstance
				if (getCreationScheme().getFlexoConcept() instanceof VirtualModel && (getCreationScheme() instanceof CreationScheme)) {

					String vmiName = getVirtualModelInstanceName().getBindingValue(evaluationContext);

					// System.out.println("Creating new VMI VM=" + getCreationScheme().getFlexoConcept());
					// System.out.println("vmiName=" + vmiName);

					/*System.out.println("getVirtualModelInstanceName()=" + getVirtualModelInstanceName());
					System.out.println("valid=" + getVirtualModelInstanceName().isValid());
					System.out.println("reason=" + getVirtualModelInstanceName().invalidBindingReason());
					System.out.println("BM=" + getBindingModel());
					System.out.println("vmiName=" + vmiName);*/

					VirtualModel instantiatedVirtualModel = (VirtualModel) getCreationScheme().getFlexoConcept();

					// We have to instantiate a VirtualModel
					// At this point, 3 cases may happen:
					// 1. The VirtualModel has a container VirtualModel : in this case the container MUST match the type
					// 2. We should supply the RepositoryFolder
					// 3. We should supply the ResourceCenter AND the relative path
					// Otherwise, we choose a default repository folder where the focused FCI resides
					// We may supply an URI if no container VirtualModel

					if (instantiatedVirtualModel.getContainerVirtualModel() != null) {
						// TODO: check that container matches expected container VirtualModel
					}
					else {
						// We should find the adequate folder where to instantiate the VMI
						if (getResourceCenter().isSet() && getResourceCenter().isValid() && getDynamicRelativePath().isSet()
								&& getDynamicRelativePath().isValid()) {
							FlexoResourceCenter rc = getResourceCenter().getBindingValue(evaluationContext);
							String relativePath = getDynamicRelativePath().getBindingValue(evaluationContext);
							Object serializationArtefact = rc.getDirectoryWithRelativePath(relativePath);
							container = rc.getRepositoryFolder(serializationArtefact, true);
						}
						else if (getRepositoryFolder().isSet() && getRepositoryFolder().isValid()) {
							container = getRepositoryFolder().getBindingValue(evaluationContext);
						}
						else {
							// In this case, find the folder in which root current VMI is defined
							if (container instanceof FlexoConceptInstance) {
								VirtualModelInstance<?, ?> currentVMI = ((FlexoConceptInstance) container).getVirtualModelInstance();
								while (currentVMI.getContainerVirtualModelInstance() != null) {
									currentVMI = currentVMI.getContainerVirtualModelInstance();
								}
								container = currentVMI.getResourceCenter().getRepositoryFolder(currentVMI.getResource());
							}
						}
					}

					CreateBasicVirtualModelInstance createVMIAction;
					if (evaluationContext instanceof FlexoBehaviourAction) {
						createVMIAction = CreateBasicVirtualModelInstance.actionType.makeNewEmbeddedAction(container, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
					}
					else if (evaluationContext instanceof RunTimeEvaluationContext) {
						createVMIAction = CreateBasicVirtualModelInstance.actionType.makeNewAction(container, null,
								((RunTimeEvaluationContext) evaluationContext).getEditor());
					}
					else {
						logger.warning("Invalid evaluation context " + evaluationContext);
						return null;
					}

					createVMIAction.setSkipChoosePopup(true);
					createVMIAction.setNewVirtualModelInstanceName(vmiName);
					createVMIAction.setVirtualModel(instantiatedVirtualModel);
					createVMIAction.setCreationScheme((CreationScheme) getCreationScheme());

					// System.out.println("On execute le CS: " + getCreationScheme());
					// System.out.println("FML: " + getCreationScheme().getFMLPrettyPrint());

					for (FunctionArgument functionArgument : getFunctionArguments()) {
						// System.out.println("functionArgument:" + functionArgument + " = " + getArgumentValue(functionArgument));
						Object v = getArgumentValue(functionArgument).getBindingValue(evaluationContext);
						// System.out.println("values:" + v);
						createVMIAction.setParameterValue((FlexoBehaviourParameter) functionArgument, v);
					}

					// System.out.println("Doing the action...");
					createVMIAction.doAction();
					FMLRTVirtualModelInstance returned = createVMIAction.getNewVirtualModelInstance();
					// System.out.println("returned=" + returned);
					return returned;
				}
				else if (container instanceof FlexoConceptInstance) {
					FlexoConceptInstance containerFCI = (FlexoConceptInstance) container;
					FlexoConceptInstance newFCI = containerFCI.getVirtualModelInstance()
							.makeNewFlexoConceptInstance(getCreationScheme().getFlexoConcept(), containerFCI);
					if (getCreationScheme().getFlexoConcept().getContainerFlexoConcept() != null) {
						containerFCI.addToEmbeddedFlexoConceptInstances(newFCI);
					}

					if (performExecuteCreationScheme(newFCI, containerFCI.getVirtualModelInstance(), evaluationContext)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Successfully performed performAddFlexoConcept " + evaluationContext);
						}
						return newFCI;
					}
					else {
						logger.warning("Failing execution of creationScheme: " + getCreationScheme());
					}
				}
				else {
					logger.warning("Do not know what to do with: " + container);
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;

		}

		private AbstractCreationSchemeAction<?, CS, ?> makeCreationSchemeAction(CS behaviour, VirtualModelInstance<?, ?> vmInstance,
				BindingEvaluationContext evaluationContext) {
			// System.out.println("SM=" + getServiceManager());
			// System.out.println("SM2=" + behaviour.getServiceManager());
			TechnologyAdapter<?> ta = behaviour.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapterForBehaviourType(behaviour.getClass());
			if (ta == null) {
				logger.warning("Cannot find TechnologyAdapter for behaviour " + behaviour);
				Thread.dumpStack();
				return null;
			}
			if (evaluationContext instanceof FlexoBehaviourAction) {
				return ta.makeCreationSchemeAction(behaviour, vmInstance, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
			}
			else if (evaluationContext instanceof RunTimeEvaluationContext) {
				return ta.makeCreationSchemeAction(behaviour, vmInstance, ((RunTimeEvaluationContext) evaluationContext).getEditor());
			}
			else {
				logger.warning("Unexpected evaluation context : " + evaluationContext);
				Thread.dumpStack();
				return null;
			}
		}

		private boolean performExecuteCreationScheme(FlexoConceptInstance newInstance, VirtualModelInstance<?, ?> vmInstance,
				BindingEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {

			AbstractCreationSchemeAction<?, CS, ?> creationSchemeAction = makeCreationSchemeAction(getCreationScheme(), vmInstance,
					evaluationContext);

			/*if (evaluationContext instanceof FlexoBehaviourAction) {
				creationSchemeAction = new CreationSchemeAction(getCreationScheme(), vmInstance, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
			}
			else if (evaluationContext instanceof RunTimeEvaluationContext) {
				creationSchemeAction = new CreationSchemeAction(getCreationScheme(), vmInstance, null,
						((RunTimeEvaluationContext) evaluationContext).getEditor());
			}
			else {
				logger.warning("Unexpected: " + evaluationContext);
				Thread.dumpStack();
			}*/

			creationSchemeAction.initWithFlexoConceptInstance(newInstance);

			for (FlexoBehaviourParameter p : getCreationScheme().getParameters()) {
				DataBinding<?> param = getArgumentValue(p);
				Object paramValue = TypeUtils.castTo(param.getBindingValue(evaluationContext), p.getType());
				// System.out.println("For parameter " + param + " value is " + paramValue);
				if (paramValue != null) {
					creationSchemeAction.setParameterValue(p, paramValue);
				}
			}

			creationSchemeAction.doAction();

			return creationSchemeAction.hasActionExecutionSucceeded();

		}

	}
}
