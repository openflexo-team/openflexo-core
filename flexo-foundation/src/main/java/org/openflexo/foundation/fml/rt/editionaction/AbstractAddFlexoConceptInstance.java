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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance.AddFlexoConceptInstanceImpl;
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
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.CompoundIssue;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * Generic base action used to instanciate a {@link FlexoConceptInstance} in a given {@link FMLRTVirtualModelInstance}.
 * 
 * Note that this is also the base implementation for adding of a {@link FMLRTVirtualModelInstance} in a {@link VirtualModelInstance}, or a
 * {@link VirtualModelInstance} in its parent {@link VirtualModelInstance}
 * 
 * 
 * @author sylvain
 * 
 * @param <FCI>
 *            type of {@link FlexoConceptInstance} beeing created by this action
 * @param <VMI>
 *            type of the container of of {@link FlexoConceptInstance} beeing created by this action
 */

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractAddFlexoConceptInstance.AbstractAddFlexoConceptInstanceImpl.class)
public interface AbstractAddFlexoConceptInstance<FCI extends FlexoConceptInstance, VMI extends VirtualModelInstance<VMI, ?>>
		extends FMLRTAction<FCI, VMI> {

	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = List.class)
	public static final String PARAMETERS_KEY = "parameters";

	public static final String CREATION_SCHEME_KEY = "creationScheme";
	public static final String FLEXO_CONCEPT_TYPE_KEY = "flexoConceptType";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@PropertyIdentifier(type = Boolean.class)
	public static final String DYNAMIC_INSTANTIATION_KEY = "dynamicInstantiation";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DYNAMIC_FLEXO_CONCEPT_TYPE_KEY = "dynamicFlexoConceptType";

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	public CreationScheme getCreationScheme();

	public void setCreationScheme(CreationScheme creationScheme);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = AddFlexoConceptInstanceParameter.OWNER_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<AddFlexoConceptInstanceParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<AddFlexoConceptInstanceParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(AddFlexoConceptInstanceParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(AddFlexoConceptInstanceParameter aParameter);

	public AddFlexoConceptInstanceParameter getParameter(String paramName);

	public AddFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p);

	/**
	 * Get concept as type to be created by this action
	 * 
	 * @return
	 */
	public FlexoConcept getFlexoConceptType();

	/**
	 * Sets concept as type to be created by this action
	 * 
	 * @param flexoConceptType
	 */
	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	/**
	 * Return the list of available CreationScheme (depends of {@link #getFlexoConceptType()})
	 * 
	 * @return
	 */
	public List<CreationScheme> getAvailableCreationSchemes();

	@Getter(value = DYNAMIC_INSTANTIATION_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getDynamicInstantiation();

	@Setter(DYNAMIC_INSTANTIATION_KEY)
	public void setDynamicInstantiation(boolean dynamicInstanciation);

	@Getter(value = DYNAMIC_FLEXO_CONCEPT_TYPE_KEY)
	@XMLAttribute
	public DataBinding<FlexoConcept> getDynamicFlexoConceptType();

	@Setter(DYNAMIC_FLEXO_CONCEPT_TYPE_KEY)
	public void setDynamicFlexoConceptType(DataBinding<FlexoConcept> dynamicFlexoConceptType);

	public static abstract class AbstractAddFlexoConceptInstanceImpl<FCI extends FlexoConceptInstance, VMI extends VirtualModelInstance<VMI, ?>>
			extends FMLRTActionImpl<FCI, VMI> implements AbstractAddFlexoConceptInstance<FCI, VMI> {

		static final Logger logger = Logger.getLogger(AbstractAddFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private List<AddFlexoConceptInstanceParameter> parameters = null;

		private DataBinding<FlexoConceptInstance> container;
		private DataBinding<FlexoConcept> dynamicFlexoConceptType;

		protected abstract Class<? extends FlexoConcept> getDynamicFlexoConceptTypeType();

		@Override
		public DataBinding<FlexoConcept> getDynamicFlexoConceptType() {
			if (dynamicFlexoConceptType == null) {
				dynamicFlexoConceptType = new DataBinding<>(this, getDynamicFlexoConceptTypeType(), DataBinding.BindingDefinitionType.GET);
				dynamicFlexoConceptType.setBindingName("dynamicFlexoConceptType");
			}
			return dynamicFlexoConceptType;
		}

		@Override
		public void setDynamicFlexoConceptType(DataBinding<FlexoConcept> dynamicFlexoConceptType) {
			if (dynamicFlexoConceptType != null) {
				dynamicFlexoConceptType.setOwner(this);
				dynamicFlexoConceptType.setBindingName("dynamicFlexoConceptType");
				dynamicFlexoConceptType.setDeclaredType(getDynamicFlexoConceptTypeType());
				dynamicFlexoConceptType.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.dynamicFlexoConceptType = dynamicFlexoConceptType;
		}

		@Override
		public DataBinding<FlexoConceptInstance> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, FlexoConceptInstance.class, DataBinding.BindingDefinitionType.GET);
				container.setBindingName("container");
				container.setDeclaredType(getFlexoConceptType() != null && getFlexoConceptType().getContainerFlexoConcept() != null
						? getFlexoConceptType().getContainerFlexoConcept().getInstanceType()
						: FlexoConceptInstance.class);
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<FlexoConceptInstance> aContainer) {
			if (aContainer != null) {
				aContainer.setOwner(this);
				aContainer.setBindingName("container");
				aContainer.setDeclaredType(getFlexoConceptType() != null && getFlexoConceptType().getContainerFlexoConcept() != null
						? getFlexoConceptType().getContainerFlexoConcept().getInstanceType()
						: FlexoConceptInstance.class);
				aContainer.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.container = aContainer;
		}

		public VMI getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			try {
				// System.out.println("getVirtualModelInstance() with " + getVirtualModelInstance());
				// System.out.println("Valid=" + getVirtualModelInstance().isValid() + " " +
				// getVirtualModelInstance().invalidBindingReason());
				// System.out.println("returned: " + getVirtualModelInstance().getBindingValue(evaluationContext));
				// System.out.println("evaluationContext=" + evaluationContext);
				return getReceiver().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		public FlexoConceptInstance getContainer(RunTimeEvaluationContext evaluationContext) {
			try {
				return getContainer().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getFlexoConcept();
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (this.flexoConceptType != flexoConceptType) {
				FlexoConcept oldValue = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
					if (flexoConceptType.getCreationSchemes().size() > 0) {
						setCreationScheme(flexoConceptType.getCreationSchemes().get(0));
					}
					else {
						setCreationScheme(null);
					}
				}
				if (flexoConceptType != null && flexoConceptType.getContainerFlexoConcept() != null) {
					getContainer().setDeclaredType(flexoConceptType.getContainerFlexoConcept().getInstanceType());
				}
				else {
					getContainer().setDeclaredType(FlexoConceptInstance.class);
				}
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, oldValue, flexoConceptType);
				getPropertyChangeSupport().firePropertyChange("availableCreationSchemes", null, getAvailableCreationSchemes());
			}
		}

		@Override
		public String _getCreationSchemeURI() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getURI();
			}
			return _creationSchemeURI;
		}

		@Override
		public void _setCreationSchemeURI(String uri) {
			if (getVirtualModelLibrary() != null) {
				creationScheme = (CreationScheme) getVirtualModelLibrary().getFlexoBehaviour(uri, true);
			}
			_creationSchemeURI = uri;
		}

		private void loadMetaModelWhenRequired() {
			if (creationScheme == null && _creationSchemeURI != null && getVirtualModelLibrary() != null) {
				creationScheme = (CreationScheme) getVirtualModelLibrary().getFlexoBehaviour(_creationSchemeURI, true);
				updateParameters();
			}
		}

		@Override
		public CreationScheme getCreationScheme() {

			if (creationScheme == null && _creationSchemeURI != null && getVirtualModelLibrary() != null) {
				creationScheme = (CreationScheme) getVirtualModelLibrary().getFlexoBehaviour(_creationSchemeURI, false);
				updateParameters();
			}
			if (creationScheme == null && getAssignedFlexoProperty() instanceof FlexoConceptInstanceRole) {
				creationScheme = ((FlexoConceptInstanceRole) getAssignedFlexoProperty()).getCreationScheme();
				updateParameters();
			}
			return creationScheme;
		}

		@Override
		public void setCreationScheme(CreationScheme creationScheme) {
			if (this.creationScheme != creationScheme) {
				CreationScheme oldValue = this.creationScheme;
				this.creationScheme = creationScheme;
				if (creationScheme != null) {
					_creationSchemeURI = creationScheme.getURI();
				}
				else {
					_creationSchemeURI = null;
				}
				updateParameters();
				getPropertyChangeSupport().firePropertyChange(CREATION_SCHEME_KEY, oldValue, creationScheme);
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, null, getFlexoConceptType());
			}
		}

		@Override
		public List<CreationScheme> getAvailableCreationSchemes() {

			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getCreationSchemes();
			}
			return null;
		}

		// private Vector<AddFlexoConceptInstanceParameter> parameters = new Vector<AddFlexoConceptInstanceParameter>();

		@Override
		public List<AddFlexoConceptInstanceParameter> getParameters() {
			// Comment this because of an infinite loop with updateParameters() method
			if (parameters == null) {
				parameters = new ArrayList<>();
				updateParameters();
			}
			return parameters;
		}

		public void setParameters(Vector<AddFlexoConceptInstanceParameter> parameters) {
			this.parameters = parameters;
		}

		@Override
		public void addToParameters(AddFlexoConceptInstanceParameter parameter) {
			parameter.setOwner(this);
			if (parameters == null) {
				parameters = new ArrayList<>();
			}
			parameters.add(parameter);
		}

		@Override
		public void removeFromParameters(AddFlexoConceptInstanceParameter parameter) {
			parameter.setOwner(null);
			if (parameters == null) {
				parameters = new ArrayList<>();
			}
			parameters.remove(parameter);
		}

		@Override
		public AddFlexoConceptInstanceParameter getParameter(String paramName) {
			if (getCreationScheme() == null) {
				return null;
			}
			FlexoBehaviourParameter p = getCreationScheme().getParameter(paramName);
			return getParameter(p);
		}

		@Override
		public AddFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p) {
			for (AddFlexoConceptInstanceParameter addEPParam : getParameters()) {
				if (addEPParam.getParam() == p) {
					return addEPParam;
				}
			}
			return null;
		}

		private void updateParameters() {
			if (parameters == null) {
				parameters = new ArrayList<>();
			}
			List<AddFlexoConceptInstanceParameter> oldValue = new ArrayList<>(parameters);
			List<AddFlexoConceptInstanceParameter> parametersToRemove = new ArrayList<>(parameters);
			if (creationScheme != null) {
				for (FlexoBehaviourParameter p : creationScheme.getParameters()) {
					AddFlexoConceptInstanceParameter existingParam = getParameter(p);
					if (existingParam != null) {
						parametersToRemove.remove(existingParam);
						// Force revalidate the binding (binding factory was null)
						if (!existingParam.getValue().isValid()) {
							existingParam.getValue().forceRevalidate();
						}
					}
					else {
						if (getFMLModelFactory() != null) {
							AddFlexoConceptInstanceParameter newAddFlexoConceptInstanceParameter = getFMLModelFactory()
									.newAddFlexoConceptInstanceParameter(p);
							addToParameters(newAddFlexoConceptInstanceParameter);
						}
					}
				}
				for (AddFlexoConceptInstanceParameter removeThis : parametersToRemove) {
					removeFromParameters(removeThis);
				}
			}
			getPropertyChangeSupport().firePropertyChange(PARAMETERS_KEY, oldValue, parameters);
		}

		@Override
		public FCI execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("--------------> Perform performAddFlexoConceptInstance " + evaluationContext);
			}

			loadMetaModelWhenRequired();

			VMI vmInstance = getVirtualModelInstance(evaluationContext);
			if (vmInstance == null) {
				logger.warning("null FMLRTVirtualModelInstance");
				return null;
			}

			FlexoConceptInstance container = null;

			if (getDynamicInstantiation() && !getDynamicFlexoConceptType().isValid()) {
				logger.warning("undefined or invalid dynamic concept type while creating new concept");
				return null;
			}

			if (!getDynamicInstantiation() && getFlexoConceptType() == null) {
				logger.warning("null concept type while creating new concept");
				return null;
			}

			FlexoConcept instantiatedFlexoConcept = retrieveFlexoConcept(evaluationContext);

			if (instantiatedFlexoConcept.getContainerFlexoConcept() != null) {
				container = getContainer(evaluationContext);
				if (container == null) {
					logger.warning("null container while creating new concept " + getFlexoConceptType());
					return null;
				}
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("vmInstance=" + vmInstance);
				logger.fine("container=" + container);
				logger.fine("concept=" + (getCreationScheme() != null ? getCreationScheme().getFlexoConcept() : null));
				logger.fine("getCreationScheme()=" + getCreationScheme());
			}

			FCI newFCI = makeNewFlexoConceptInstance(evaluationContext);

			if (instantiatedFlexoConcept.getContainerFlexoConcept() != null) {
				container.addToEmbeddedFlexoConceptInstances(newFCI);
			}

			if (!getDynamicInstantiation()) {
				if (executeCreationScheme(newFCI, vmInstance, evaluationContext)) {
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
				logger.warning("I should find default creationScheme for dynamic instantiation");
				CreationScheme applicableCreationScheme = findBestCreationSchemeForDynamicInstantiation(evaluationContext);
				System.out.println("Found: " + applicableCreationScheme);
				if (applicableCreationScheme != null) {
					if (_performExecuteCreationScheme(applicableCreationScheme, newFCI, vmInstance, evaluationContext)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Successfully performed performAddFlexoConcept " + evaluationContext);
						}
						return newFCI;
					}
					else {
						logger.warning("Failing execution of creationScheme: " + applicableCreationScheme);
					}
				}
			}

			return null;
		}

		private CreationScheme findBestCreationSchemeForDynamicInstantiation(RunTimeEvaluationContext evaluationContext)
				throws FlexoException {
			FlexoConcept instantiatedFlexoConcept = retrieveFlexoConcept(evaluationContext);
			if (instantiatedFlexoConcept != null) {
				for (CreationScheme creationScheme : instantiatedFlexoConcept.getAccessibleCreationSchemes()) {
					if (creationScheme.getParameters().size() == getParameters().size()) {
						return creationScheme;
					}
				}
			}
			return null;
		}

		protected boolean executeCreationScheme(FCI newInstance, VirtualModelInstance<?, ?> vmInstance,
				RunTimeEvaluationContext evaluationContext) {
			return _performExecuteCreationScheme(getCreationScheme(), newInstance, vmInstance, evaluationContext);
		}

		private boolean _performExecuteCreationScheme(CreationScheme creationScheme, FCI newInstance, VirtualModelInstance<?, ?> vmInstance,
				RunTimeEvaluationContext evaluationContext) {
			if (evaluationContext instanceof FlexoBehaviourAction) {
				CreationSchemeAction creationSchemeAction = new CreationSchemeAction(creationScheme, vmInstance, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				creationSchemeAction.initWithFlexoConceptInstance(newInstance);
				for (AddFlexoConceptInstanceParameter p : getParameters()) {
					FlexoBehaviourParameter param = p.getParam();
					Object value = p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext);
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("For parameter " + param + " value is " + value);
					}
					if (value != null) {
						creationSchemeAction.setParameterValue(p.getParam(),
								p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext));
					}
				}
				// System.out.println("et maintenant on execute le creation scheme");
				creationSchemeAction.doAction();

				return creationSchemeAction.hasActionExecutionSucceeded();

			}
			logger.warning("Unexpected: " + evaluationContext);
			Thread.dumpStack();
			return false;
		}

		protected abstract FCI makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) throws FlexoException;

		protected FlexoConcept retrieveFlexoConcept(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			if (getDynamicInstantiation() && getDynamicFlexoConceptType().isValid()) {
				try {
					return getDynamicFlexoConceptType().getBindingValue(evaluationContext);
				} catch (TypeMismatchException | NullReferenceException | InvocationTargetException e) {
					e.printStackTrace();
					throw new FlexoException(e);
				}
			}
			return getFlexoConceptType();
		}

		@Override
		public Type getAssignableType() {
			if (getFlexoConcept() != null) {
				return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
			}
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			loadMetaModelWhenRequired();
		}

		@Override
		public String getStringRepresentation() {
			if (getFlexoConceptType() != null && getCreationScheme() != null) {
				return getFlexoConceptType().getName() + "." + getCreationScheme().getName() + "(" + getParameterValuesAsString() + ")";
			}
			return super.getStringRepresentation();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			if (getFlexoConceptType() != null && getCreationScheme() != null) {
				return getFlexoConceptType().getName() + "." + getCreationScheme().getName() + "(" + getParameterValuesAsString() + ")";
			}
			return super.getFMLRepresentation(context);
		}

		protected String getParameterValuesAsString() {
			StringBuffer returned = new StringBuffer();
			boolean isFirst = true;
			for (AddFlexoConceptInstanceParameter param : getParameters()) {
				returned.append((isFirst ? "" : ",") + param.getValue());
				isFirst = false;
			}
			return returned.toString();
		}

	}

	@DefineValidationRule
	public static class AddFlexoConceptInstanceParametersMustBeValid
			extends ValidationRule<AddFlexoConceptInstanceParametersMustBeValid, AbstractAddFlexoConceptInstance<?, ?>> {

		public AddFlexoConceptInstanceParametersMustBeValid() {
			super(AbstractAddFlexoConceptInstance.class, "add_flexo_concept_parameters_must_be_valid");
		}

		@Override
		public ValidationIssue<AddFlexoConceptInstanceParametersMustBeValid, AbstractAddFlexoConceptInstance<?, ?>> applyValidation(
				AbstractAddFlexoConceptInstance<?, ?> action) {
			if (action.getCreationScheme() != null) {
				Vector<ValidationIssue<AddFlexoConceptInstanceParametersMustBeValid, AbstractAddFlexoConceptInstance<?, ?>>> issues = new Vector<>();
				for (AddFlexoConceptInstanceParameter p : action.getParameters()) {
					FlexoBehaviourParameter param = p.getParam();
					if (param.getIsRequired()) {
						if (p.getValue() == null || !p.getValue().isSet()) {
							issues.add(new ValidationError<>(this, action, "parameter_s_value_is_not_defined: " + param.getName()));
						}
						else if (!p.getValue().forceRevalidate()) {
							AddFlexoConceptInstanceImpl.logger
									.info("Binding NOT valid: " + p.getValue() + " for " + p.getName() + " object="
											+ p.getOwner().getStringRepresentation() + ". Reason: " + p.getValue().invalidBindingReason());
							issues.add(new ValidationError<>(this, action, "parameter_s_value_is_not_valid: " + param.getName()));
						}
					}
				}
				if (issues.size() == 0) {
					return null;
				}
				else if (issues.size() == 1) {
					return issues.firstElement();
				}
				else {
					return new CompoundIssue<>(action, issues);
				}
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<AbstractAddFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", AbstractAddFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<FMLRTVirtualModelInstance> getBinding(AbstractAddFlexoConceptInstance object) {
			return object.getReceiver();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractAddFlexoConceptInstance>, AbstractAddFlexoConceptInstance> applyValidation(
				AbstractAddFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractAddFlexoConceptInstance>, AbstractAddFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			else {
				DataBinding<FMLRTVirtualModelInstance> binding = getBinding(object);

				if (binding.getAnalyzedType() instanceof VirtualModelInstanceType && object.getFlexoConceptType() != null) {
					if (!(object.getFlexoConceptType() instanceof VirtualModel) && object.getFlexoConceptType()
							.getOwningVirtualModel() != ((VirtualModelInstanceType) binding.getAnalyzedType()).getVirtualModel()) {
						returned = new ValidationError<>(this, object, "incompatible_virtual_model_type");

						/*System.out.println(object.getRootOwner().getFMLRepresentation());
						System.out.println("binding=" + binding);
						System.out.println("binding.getAnalyzedType()=" + binding.getAnalyzedType());
						System.out.println("object.getFlexoConceptType()=" + object.getFlexoConceptType());
						System.out.println("object.getFlexoConceptType().getOwningVirtualModel()="
								+ object.getFlexoConceptType().getOwningVirtualModel());*/

						// Attempt to find some solutions...

						for (FMLRTModelSlot<?, ?> ms : object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class)) {
							// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
							if (object.getFlexoConceptType().getOwner().isAssignableFrom(ms.getAccessedVirtualModel())) {
								((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
							}
						}

						if (object.getRootOwner().getFlexoConcept() instanceof VirtualModel) {
							for (FMLRTModelSlot<?, ?> ms : ((VirtualModel) object.getRootOwner().getFlexoConcept())
									.getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getOwner().isAssignableFrom(ms.getAccessedVirtualModel())) {
									((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
								}
							}
						}

					}
				}
			}
			return returned;
		}

		protected static class UseLocalVirtualModelInstance extends
				FixProposal<BindingIsRequiredAndMustBeValid<AbstractAddFlexoConceptInstance<?, ?>>, AbstractAddFlexoConceptInstance<?, ?>> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				AbstractAddFlexoConceptInstance<?, ?> action = getValidable();
				action.setReceiver(new DataBinding<>("virtualModelInstance"));
			}
		}

		protected static class UseFMLRTModelSlot extends
				FixProposal<BindingIsRequiredAndMustBeValid<AbstractAddFlexoConceptInstance<?, ?>>, AbstractAddFlexoConceptInstance<?, ?>> {

			private final FMLRTModelSlot<?, ?> modelSlot;

			public UseFMLRTModelSlot(FMLRTModelSlot<?, ?> modelSlot) {
				super("sets_receiver_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}

			@Override
			protected void fixAction() {
				AbstractAddFlexoConceptInstance<?, ?> action = getValidable();
				action.setReceiver(new DataBinding<>(modelSlot.getName()));
			}
		}

	}

}
