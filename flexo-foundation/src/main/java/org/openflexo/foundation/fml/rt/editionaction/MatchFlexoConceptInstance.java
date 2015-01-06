/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.rt.editionaction;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.CompoundIssue;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * This action is used to perform synchronization regarding an {@link FlexoConceptInstance} in a given {@link VirtualModelInstance}.<br>
 * The matching is performed on some pattern roles, with some values retrieved from an expression.<br>
 * If target {@link FlexoConceptInstance} could not been looked up, then a new {@link FlexoConceptInstance} is created using supplied
 * {@link CreationScheme} and some parameters
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */
@FIBPanel("Fib/FML/MatchFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(MatchFlexoConceptInstance.MatchFlexoConceptInstanceImpl.class)
@XMLElement
public interface MatchFlexoConceptInstance extends FMLRTAction<FlexoConceptInstance> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String MATCHING_CRITERIAS_KEY = "matchingCriterias";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Override
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Override
	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	@Getter(value = MATCHING_CRITERIAS_KEY, cardinality = Cardinality.LIST, inverse = MatchingCriteria.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<MatchingCriteria> getMatchingCriterias();

	@Setter(MATCHING_CRITERIAS_KEY)
	public void setMatchingCriterias(List<MatchingCriteria> matchingCriterias);

	@Adder(MATCHING_CRITERIAS_KEY)
	public void addToMatchingCriterias(MatchingCriteria aMatchingCriteria);

	@Remover(MATCHING_CRITERIAS_KEY)
	public void removeFromMatchingCriterias(MatchingCriteria aMatchingCriteria);

	public MatchingCriteria getMatchingCriteria(FlexoRole flexoRole);

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

	public CreationScheme getCreationScheme();

	public void setCreationScheme(CreationScheme creationScheme);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public CreateFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p);

	public static abstract class MatchFlexoConceptInstanceImpl extends FMLRTActionImpl<FlexoConceptInstance> implements
			MatchFlexoConceptInstance, PropertyChangeListener {

		static final Logger logger = Logger.getLogger(MatchFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			/*if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}*/
			out.append(getImplementedInterface().getSimpleName() + " as "
					+ (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "no type specified") + " "
					+ getMatchingCriteriasFMLRepresentation(context) + " using "
					+ (getCreationScheme() != null ? getCreationScheme().getFlexoConcept().getName() : "no creation scheme specified")
					+ ":" + (getCreationScheme() != null ? getCreationScheme().getName() : "no creation scheme specified") + "("
					+ getCreationSchemeParametersFMLRepresentation(context) + ")", context);
			/*if (getAssignation().isSet()) {
				out.append(")", context);
			}*/
			return out.toString();
		}

		protected String getMatchingCriteriasFMLRepresentation(FMLRepresentationContext context) {

			List<MatchingCriteria> matchingCriterias = getMatchingCriterias();
			if (matchingCriterias.size() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("match ");
				if (matchingCriterias.size() > 1) {
					sb.append("(");
				}
				for (MatchingCriteria mc : matchingCriterias) {
					FlexoRole<?> role = mc.getFlexoRole();
					DataBinding<?> val = mc.getValue();
					if (role != null && val != null) {
						sb.append((mc.getFlexoRole().getName() != null ? mc.getFlexoRole().getName() : "null") + "="
								+ mc.getValue().toString() + ";");
					}
				}
				if (matchingCriterias.size() > 1) {
					sb.append(")");
				}
				return sb.toString();
			}
			return null;
		}

		protected String getCreationSchemeParametersFMLRepresentation(FMLRepresentationContext context) {
			if (getParameters().size() > 0) {
				StringBuffer sb = new StringBuffer();
				boolean isFirst = true;
				for (CreateFlexoConceptInstanceParameter p : getParameters()) {
					sb.append((isFirst ? "" : ",") + p.getValue().toString());
					isFirst = false;
				}
				return sb.toString();
			}
			return null;
		}

		public VirtualModelInstance getVirtualModelInstance(FlexoBehaviourAction action) {
			try {
				return getVirtualModelInstance().getBindingValue(action);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		private DataBinding<VirtualModelInstance> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<VirtualModelInstance>(this, VirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName("virtualModelInstance");
				virtualModelInstance.setMandatory(true);
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<VirtualModelInstance> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName("virtualModelInstance");
				aVirtualModelInstance.setDeclaredType(VirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				aVirtualModelInstance.setMandatory(true);
			}
			this.virtualModelInstance = aVirtualModelInstance;
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
			if (requireChange(this.flexoConceptType, flexoConceptType)) {
				FlexoConcept oldConcept = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
					setCreationScheme(null);
				}
				fireFlexoConceptChange(oldConcept, flexoConceptType);
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
			if (requireChange(_getCreationSchemeURI(), uri)) {
				String oldURI = _getCreationSchemeURI();
				CreationScheme oldCS = getCreationScheme();
				_creationSchemeURI = uri;
				if (getViewPointLibrary() != null) {
					creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(uri);
				}
				fireCreationSchemeChange(oldCS, getCreationScheme());
				getPropertyChangeSupport().firePropertyChange(CREATION_SCHEME_URI_KEY, oldURI, uri);
			}
		}

		private void fireCreationSchemeChange(CreationScheme oldValue, CreationScheme newValue) {
			if (requireChange(oldValue, newValue)) {
				FlexoConcept oldFlexoConcept = (oldValue != null ? oldValue.getFlexoConcept() : null);
				FlexoConcept newFlexoConcept = (newValue != null ? newValue.getFlexoConcept() : null);
				if (oldValue != null) {
					oldValue.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (newValue != null) {
					newValue.getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, newValue);
				fireFlexoConceptChange(oldFlexoConcept, newFlexoConcept);
				updateParameters();
			}
		}

		private void fireFlexoConceptChange(FlexoConcept oldValue, FlexoConcept newValue) {
			if (requireChange(oldValue, newValue)) {
				if (oldValue != null) {
					oldValue.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (newValue != null) {
					newValue.getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, newValue);
				updateMatchingCriterias();
			}
		}

		@Override
		public CreationScheme getCreationScheme() {
			// TODO : check if this is useful, but when it is assigned to a variable, it fails!
			/*
			if (getFlexoRole() instanceof FlexoConceptInstanceRole
					&& ((FlexoConceptInstanceRole) getFlexoRole()).getCreationScheme() != null) {
				return ((FlexoConceptInstanceRole) getFlexoRole()).getCreationScheme();
			}
			 */

			if (creationScheme == null && _creationSchemeURI != null && getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(_creationSchemeURI);
			}
			return creationScheme;
		}

		@Override
		public void setCreationScheme(CreationScheme creationScheme) {
			if (requireChange(getCreationScheme(), creationScheme)) {
				CreationScheme oldCS = getCreationScheme();
				this.creationScheme = creationScheme;
				if (creationScheme != null) {
					_creationSchemeURI = creationScheme.getURI();
				} else {
					_creationSchemeURI = null;
				}
				fireCreationSchemeChange(oldCS, creationScheme);
			}
		}

		/*@Override
		public List<CreateFlexoConceptInstanceParameter> getParameters() {
			if (!isUpdatingParameters) {
				updateParameters();
			}
			return (List<CreateFlexoConceptInstanceParameter>) performSuperGetter(PARAMETERS_KEY);
		}*/

		/*public void setParameters(Vector<CreateFlexoConceptInstanceParameter> parameters) {
			this.parameters = parameters;
		}*/

		/*@Override
		public void addToParameters(CreateFlexoConceptInstanceParameter parameter) {
			// if(parameter.getAction()!=this){
			parameter.setAction(this);
			// }
			parameters.add(parameter);
		}*/

		/*@Override
		public void removeFromParameters(CreateFlexoConceptInstanceParameter parameter) {
			// if(parameter.getAction()!=null){
			parameter.setAction(null);
			// }
			parameters.remove(parameter);
		}*/

		@Override
		public CreateFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p) {
			for (CreateFlexoConceptInstanceParameter addEPParam : getParameters()) {
				if (addEPParam.getParam() == p) {
					return addEPParam;
				}
			}
			return null;
		}

		private boolean isUpdatingParameters = false;

		private synchronized void updateParameters() {

			System.out.println("on met a jour les parametres pour " + getCreationScheme());

			isUpdatingParameters = true;
			if (getCreationScheme() == null) {
				for (CreateFlexoConceptInstanceParameter p : new ArrayList<CreateFlexoConceptInstanceParameter>(getParameters())) {
					removeFromParameters(p);
				}
			} else {
				List<CreateFlexoConceptInstanceParameter> parametersToRemove = new ArrayList<CreateFlexoConceptInstanceParameter>(
						getParameters());
				if (getCreationScheme() != null) {
					for (FlexoBehaviourParameter p : getCreationScheme().getParameters()) {
						CreateFlexoConceptInstanceParameter existingParam = getParameter(p);
						if (existingParam != null) {
							parametersToRemove.remove(existingParam);
						} else {
							if (getVirtualModelFactory() != null) {
								addToParameters(getVirtualModelFactory().newCreateFlexoConceptInstanceParameter(p));
							}
						}
					}
				}
				for (CreateFlexoConceptInstanceParameter removeThis : parametersToRemove) {
					removeFromParameters(removeThis);
				}
			}
			isUpdatingParameters = false;
		}

		/*@Override
		public synchronized List<MatchingCriteria> getMatchingCriterias() {
			if (!isUpdatingMatchingCriterias) {
				updateMatchingCriterias();
			}
			// return matchingCriterias;
			return (List<MatchingCriteria>) performSuperGetter(MATCHING_CRITERIAS_KEY);
		}*/

		/*public void setMatchingCriterias(Vector<MatchingCriteria> matchingCriterias) {
			this.matchingCriterias = matchingCriterias;
		}*/

		/*@Override
		public void addToMatchingCriterias(MatchingCriteria matchingCriteria) {
			//matchingCriteria.setAction(this);
			//matchingCriterias.add(matchingCriteria);
			if (matchingCriteria != null && matchingCriteria.getFlexoRole() != null) {
				MatchingCriteria existing = getMatchingCriteria(matchingCriteria.getFlexoRole());
				if (existing != null) {
					System.out.println("REMOVE " + existing.getFlexoRole().getName() + " value=" + existing.getValue()
							+ " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
					performSuperRemover(MATCHING_CRITERIAS_KEY, existing);
				}
				System.out.println("ADD " + matchingCriteria.getFlexoRole().getName() + " value=" + matchingCriteria.getValue()
						+ " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
				performSuperAdder(MATCHING_CRITERIAS_KEY, matchingCriteria);
			}
		}*/

		/*@Override
		public void removeFromMatchingCriterias(MatchingCriteria matchingCriteria) {
			matchingCriteria.setAction(null);
			matchingCriterias.remove(matchingCriteria);
		}*/

		@Override
		public MatchingCriteria getMatchingCriteria(FlexoRole pr) {
			for (MatchingCriteria mc : getMatchingCriterias()) {
				if (mc.getFlexoRole() == pr) {
					return mc;
				}
			}
			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource().equals(getCreationScheme())) {
				updateParameters();
			} else if (evt.getSource().equals(getFlexoConceptType())) {
				updateMatchingCriterias();
			}
		}

		private boolean isUpdatingMatchingCriterias = false;

		private synchronized void updateMatchingCriterias() {

			isUpdatingMatchingCriterias = true;
			if (getFlexoConceptType() == null) {
				for (MatchingCriteria criteriaToRemove : new ArrayList<MatchingCriteria>(getMatchingCriterias())) {
					removeFromMatchingCriterias(criteriaToRemove);
				}
			} else {
				List<MatchingCriteria> criteriasToRemove = new ArrayList<MatchingCriteria>(getMatchingCriterias());
				for (FlexoRole role : getFlexoConceptType().getFlexoRoles()) {
					MatchingCriteria existingCriteria = getMatchingCriteria(role);
					if (existingCriteria != null) {
						criteriasToRemove.remove(existingCriteria);
					} else {
						System.out.println("ADD " + role.getName() + " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
						addToMatchingCriterias(getVirtualModelFactory().newMatchingCriteria(role));
					}
				}
				for (MatchingCriteria removeThis : criteriasToRemove) {
					System.out.println("REMOVE " + removeThis.getFlexoRole().getName() + " value=" + removeThis.getValue()
							+ " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
					removeFromMatchingCriterias(removeThis);
				}

			}
			isUpdatingMatchingCriterias = false;

			/*System.out.println("START updateMatchingCriterias for " + Integer.toHexString(hashCode()));
			isUpdatingMatchingCriterias = true;
			List<MatchingCriteria> existingCriterias = (List<MatchingCriteria>) performSuperGetter(MATCHING_CRITERIAS_KEY);
			List<MatchingCriteria> criteriasToRemove = new ArrayList<MatchingCriteria>(existingCriterias);
			if (getFlexoConceptType() != null) {
				for (FlexoRole pr : getFlexoConceptType().getFlexoRoles()) {
					MatchingCriteria existingCriteria = getMatchingCriteria(pr);
					if (existingCriteria != null) {
						criteriasToRemove.remove(existingCriteria);
					} else {
						System.out.println("ADD2 " + pr.getName() + " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
						addToMatchingCriterias(getVirtualModelFactory().newMatchingCriteria(pr));
					}
				}
			}
			for (MatchingCriteria removeThis : criteriasToRemove) {
				System.out.println("REMOVE2 " + removeThis.getFlexoRole().getName() + " value=" + removeThis.getValue()
						+ " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
				removeFromMatchingCriterias(removeThis);
			}
			isUpdatingMatchingCriterias = false;
			System.out.println("END1 updateMatchingCriterias for " + Integer.toHexString(hashCode()));
			for (MatchingCriteria mc : (List<MatchingCriteria>) performSuperGetter(MATCHING_CRITERIAS_KEY)) {
				System.out.println("> Criteria " + mc.getFlexoRole().getName() + " : " + mc.getValue());
			}
			System.out.println("END2 updateMatchingCriterias for " + Integer.toHexString(hashCode()));*/
		}

		@Override
		public FlexoConceptInstance execute(FlexoBehaviourAction action) {
			logger.fine("Perform perform MatchFlexoConceptInstance " + action);
			VirtualModelInstance vmInstance = getVirtualModelInstance(action);
			Hashtable<FlexoRole, Object> criterias = new Hashtable<FlexoRole, Object>();
			for (MatchingCriteria mc : getMatchingCriterias()) {
				Object value = mc.evaluateCriteriaValue(action);
				if (value != null) {
					criterias.put(mc.getFlexoRole(), value);
				}
			}
			FlexoConceptInstance matchingFlexoConceptInstance = action.matchFlexoConceptInstance(getFlexoConceptType(), criterias);

			if (matchingFlexoConceptInstance != null) {
				// A matching FlexoConceptInstance was found
				action.foundMatchingFlexoConceptInstance(matchingFlexoConceptInstance);
			} else {

				CreationSchemeAction creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(vmInstance, null, action);
				creationSchemeAction.setVirtualModelInstance(vmInstance);
				creationSchemeAction.setCreationScheme(getCreationScheme());
				for (CreateFlexoConceptInstanceParameter p : getParameters()) {
					FlexoBehaviourParameter param = p.getParam();
					Object value = p.evaluateParameterValue(action);
					if (value != null) {
						creationSchemeAction.setParameterValue(p.getParam(), p.evaluateParameterValue(action));
					}
				}
				creationSchemeAction.doAction();
				if (creationSchemeAction.hasActionExecutionSucceeded()) {
					matchingFlexoConceptInstance = creationSchemeAction.getFlexoConceptInstance();
					action.newFlexoConceptInstance(matchingFlexoConceptInstance);
				} else {
					logger.warning("Could not create FlexoConceptInstance for " + action);
				}
			}
			return matchingFlexoConceptInstance;
		}

		@Override
		public Type getAssignableType() {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
		}

		public static class MatchFlexoConceptInstanceMustAddressACreationScheme extends
				ValidationRule<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance> {
			public MatchFlexoConceptInstanceMustAddressACreationScheme() {
				super(MatchFlexoConceptInstance.class, "match_flexo_concept_action_must_address_a_valid_creation_scheme");
			}

			@Override
			public ValidationIssue<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance> applyValidation(
					MatchFlexoConceptInstance action) {
				if (action.getCreationScheme() == null) {
					if (action.getFlexoConceptType() == null) {
						return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(this,
								action, "match_flexo_concept_action_doesn't_define_any_flexo_concept");
					} else {
						return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(this,
								action, "match_flexo_concept_action_doesn't_define_any_creation_scheme");
					}
				}
				return null;
			}
		}

		public static class MatchFlexoConceptInstanceParametersMustBeValid extends
				ValidationRule<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance> {
			public MatchFlexoConceptInstanceParametersMustBeValid() {
				super(MatchFlexoConceptInstance.class, "match_flexo_concept_parameters_must_be_valid");
			}

			@Override
			public ValidationIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance> applyValidation(
					MatchFlexoConceptInstance action) {
				if (action.getCreationScheme() != null) {
					Vector<ValidationIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance>> issues = new Vector<ValidationIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance>>();
					for (CreateFlexoConceptInstanceParameter p : action.getParameters()) {
						if (p.getParam().getIsRequired()) {
							if (p.getValue() == null || !p.getValue().isSet()) {
								if (p.getParam() instanceof URIParameter && ((URIParameter) p.getParam()).getBaseURI().isSet()
										&& ((URIParameter) p.getParam()).getBaseURI().isValid()) {
									// Special case, we will find a way to manage this
								} else {
									issues.add(new ValidationError(this, action, "parameter_s_value_is_not_defined: "
											+ p.getParam().getName()));
								}
							} else if (!p.getValue().isValid()) {
								logger.info("Binding NOT valid: " + p.getValue() + " for " + p.getName() + " object="
										+ p.getAction().getStringRepresentation() + ". Reason: " + p.getValue().invalidBindingReason());
								issues.add(new ValidationError(this, action, "parameter_s_value_is_not_valid: " + p.getParam().getName()));
							}
						}
					}
					if (issues.size() == 0) {
						return null;
					} else if (issues.size() == 1) {
						return issues.firstElement();
					} else {
						return new CompoundIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance>(action, issues);
					}
				}
				return null;
			}
		}

		public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid extends
				BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance> {
			public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
				super("'virtual_model_instance'_binding_is_not_valid", MatchFlexoConceptInstance.class);
			}

			@Override
			public DataBinding<VirtualModelInstance> getBinding(MatchFlexoConceptInstance object) {
				return object.getVirtualModelInstance();
			}

		}

	}
}
