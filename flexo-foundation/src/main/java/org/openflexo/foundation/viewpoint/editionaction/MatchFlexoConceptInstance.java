/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.viewpoint.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.validation.CompoundIssue;
import org.openflexo.foundation.validation.ValidationError;
import org.openflexo.foundation.validation.ValidationIssue;
import org.openflexo.foundation.validation.ValidationRule;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.action.CreationSchemeAction;
import org.openflexo.foundation.view.action.EditionSchemeAction;
import org.openflexo.foundation.view.action.SynchronizationSchemeAction;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptInstancePatternRole;
import org.openflexo.foundation.viewpoint.FlexoConceptInstanceType;
import org.openflexo.foundation.viewpoint.EditionSchemeParameter;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.foundation.viewpoint.URIParameter;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.viewpoint.ViewPointObject.BindingIsRequiredAndMustBeValid;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

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
@FIBPanel("Fib/VPM/MatchEditionPatternInstancePanel.fib")
@ModelEntity
@ImplementationClass(MatchFlexoConceptInstance.MatchFlexoConceptInstanceImpl.class)
@XMLElement
public interface MatchFlexoConceptInstance extends AssignableAction<VirtualModelModelSlot, FlexoConceptInstance> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String MATCHING_CRITERIAS_KEY = "matchingCriterias";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	@Getter(value = MATCHING_CRITERIAS_KEY, cardinality = Cardinality.LIST, inverse = MatchingCriteria.ACTION_KEY)
	@XMLElement
	public List<MatchingCriteria> getMatchingCriterias();

	@Setter(MATCHING_CRITERIAS_KEY)
	public void setMatchingCriterias(List<MatchingCriteria> matchingCriterias);

	@Adder(MATCHING_CRITERIAS_KEY)
	public void addToMatchingCriterias(MatchingCriteria aMatchingCriteria);

	@Remover(MATCHING_CRITERIAS_KEY)
	public void removeFromMatchingCriterias(MatchingCriteria aMatchingCriteria);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = CreateFlexoConceptInstanceParameter.ACTION_KEY)
	@XMLElement
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

	public static abstract class MatchFlexoConceptInstanceImpl extends
			AssignableActionImpl<VirtualModelModelSlot, FlexoConceptInstance> implements MatchFlexoConceptInstance {

		static final Logger logger = Logger.getLogger(MatchFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private Vector<MatchingCriteria> matchingCriterias = new Vector<MatchingCriteria>();
		private Vector<CreateFlexoConceptInstanceParameter> parameters = new Vector<CreateFlexoConceptInstanceParameter>();

		public MatchFlexoConceptInstanceImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}
			out.append(getClass().getSimpleName() + " as " + getFlexoConceptType().getName() + " "
					+ getMatchingCriteriasFMLRepresentation(context) + " using " + getCreationScheme().getFlexoConcept().getName() + ":"
					+ getCreationScheme().getName() + "(" + getCreationSchemeParametersFMLRepresentation(context) + ")", context);
			if (getAssignation().isSet()) {
				out.append(")", context);
			}
			return out.toString();
		}

		protected String getMatchingCriteriasFMLRepresentation(FMLRepresentationContext context) {
			if (matchingCriterias.size() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("match ");
				if (matchingCriterias.size() > 1) {
					sb.append("(");
				}
				for (MatchingCriteria mc : matchingCriterias) {
					sb.append(mc.getPatternRole().getName() + "=" + mc.getValue().toString() + ";");
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

		public VirtualModelInstance getVirtualModelInstance(EditionSchemeAction action) {
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
			this.flexoConceptType = flexoConceptType;
			if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
				setCreationScheme(null);
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
			if (getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getEditionScheme(uri);
			}
			_creationSchemeURI = uri;
		}

		@Override
		public CreationScheme getCreationScheme() {
			if (getPatternRole() instanceof FlexoConceptInstancePatternRole
					&& ((FlexoConceptInstancePatternRole) getPatternRole()).getCreationScheme() != null) {
				return ((FlexoConceptInstancePatternRole) getPatternRole()).getCreationScheme();
			}
			if (creationScheme == null && _creationSchemeURI != null && getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getEditionScheme(_creationSchemeURI);
			}
			return creationScheme;
		}

		@Override
		public void setCreationScheme(CreationScheme creationScheme) {
			this.creationScheme = creationScheme;
			if (creationScheme != null) {
				_creationSchemeURI = creationScheme.getURI();
			}
		}

		@Override
		public Vector<CreateFlexoConceptInstanceParameter> getParameters() {
			updateParameters();
			return parameters;
		}

		public void setParameters(Vector<CreateFlexoConceptInstanceParameter> parameters) {
			this.parameters = parameters;
		}

		@Override
		public void addToParameters(CreateFlexoConceptInstanceParameter parameter) {
			parameter.setAction(this);
			parameters.add(parameter);
		}

		@Override
		public void removeFromParameters(CreateFlexoConceptInstanceParameter parameter) {
			parameter.setAction(null);
			parameters.remove(parameter);
		}

		public CreateFlexoConceptInstanceParameter getParameter(EditionSchemeParameter p) {
			for (CreateFlexoConceptInstanceParameter addEPParam : parameters) {
				if (addEPParam.getParam() == p) {
					return addEPParam;
				}
			}
			return null;
		}

		private void updateParameters() {
			Vector<CreateFlexoConceptInstanceParameter> parametersToRemove = new Vector<CreateFlexoConceptInstanceParameter>(parameters);
			if (getCreationScheme() != null) {
				for (EditionSchemeParameter p : getCreationScheme().getParameters()) {
					CreateFlexoConceptInstanceParameter existingParam = getParameter(p);
					if (existingParam != null) {
						parametersToRemove.remove(existingParam);
					} else {
						addToParameters(getVirtualModelFactory().newCreateEditionPatternInstanceParameter(p));
					}
				}
			}
			for (CreateFlexoConceptInstanceParameter removeThis : parametersToRemove) {
				removeFromParameters(removeThis);
			}
		}

		@Override
		public Vector<MatchingCriteria> getMatchingCriterias() {
			updateMatchingCriterias();
			return matchingCriterias;
		}

		public void setMatchingCriterias(Vector<MatchingCriteria> matchingCriterias) {
			this.matchingCriterias = matchingCriterias;
		}

		@Override
		public void addToMatchingCriterias(MatchingCriteria matchingCriteria) {
			matchingCriteria.setAction(this);
			matchingCriterias.add(matchingCriteria);
		}

		@Override
		public void removeFromMatchingCriterias(MatchingCriteria matchingCriteria) {
			matchingCriteria.setAction(null);
			matchingCriterias.remove(matchingCriteria);
		}

		public MatchingCriteria getMatchingCriteria(PatternRole pr) {
			for (MatchingCriteria mc : matchingCriterias) {
				if (mc.getPatternRole() == pr) {
					return mc;
				}
			}
			return null;
		}

		private void updateMatchingCriterias() {
			Vector<MatchingCriteria> criteriasToRemove = new Vector<MatchingCriteria>(matchingCriterias);
			if (getFlexoConceptType() != null) {
				for (PatternRole pr : getFlexoConceptType().getPatternRoles()) {
					MatchingCriteria existingCriteria = getMatchingCriteria(pr);
					if (existingCriteria != null) {
						criteriasToRemove.remove(existingCriteria);
					} else {
						addToMatchingCriterias(getVirtualModelFactory().newMatchingCriteria(pr));
					}
				}
			}
			for (MatchingCriteria removeThis : criteriasToRemove) {
				removeFromMatchingCriterias(removeThis);
			}
		}

		@Override
		public FlexoConceptInstance performAction(EditionSchemeAction action) {
			logger.info("Perform perform MatchFlexoConceptInstance " + action);
			VirtualModelInstance vmInstance = getVirtualModelInstance(action);
			logger.info("VirtualModelInstance: " + vmInstance);
			Hashtable<PatternRole, Object> criterias = new Hashtable<PatternRole, Object>();
			for (MatchingCriteria mc : getMatchingCriterias()) {
				Object value = mc.evaluateCriteriaValue(action);
				if (value != null) {
					criterias.put(mc.getPatternRole(), value);
				}
				System.out.println("Pour " + mc.getPatternRole().getPatternRoleName() + " value is " + value);
			}
			logger.info("On s'arrete pour regarder ");
			FlexoConceptInstance matchingEditionPatternInstance = ((SynchronizationSchemeAction) action).matchFlexoConceptInstance(
					getFlexoConceptType(), criterias);

			if (matchingEditionPatternInstance != null) {
				// A matching FlexoConceptInstance was found
				((SynchronizationSchemeAction) action).foundMatchingEditionPatternInstance(matchingEditionPatternInstance);
			} else {

				CreationSchemeAction creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(vmInstance, null, action);
				creationSchemeAction.setVirtualModelInstance(vmInstance);
				creationSchemeAction.setCreationScheme(getCreationScheme());
				for (CreateFlexoConceptInstanceParameter p : getParameters()) {
					EditionSchemeParameter param = p.getParam();
					Object value = p.evaluateParameterValue(action);
					logger.info("For parameter " + param + " value is " + value);
					if (value != null) {
						creationSchemeAction.setParameterValue(p.getParam(), p.evaluateParameterValue(action));
					}
				}
				logger.info(">> Creating a new EPI in " + vmInstance);
				creationSchemeAction.doAction();
				if (creationSchemeAction.hasActionExecutionSucceeded()) {
					logger.info("Successfully performed performAddEditionPattern " + action);
					matchingEditionPatternInstance = creationSchemeAction.getEditionPatternInstance();
					((SynchronizationSchemeAction) action).newEditionPatternInstance(matchingEditionPatternInstance);
				} else {
					logger.warning("Could not create FlexoConceptInstance for " + action);
				}
			}
			return matchingEditionPatternInstance;
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
						return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(
								this, action, "match_flexo_concept_action_doesn't_define_any_flexo_concept");
					} else {
						return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(
								this, action, "match_flexo_concept_action_doesn't_define_any_creation_scheme");
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
						return new CompoundIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance>(action,
								issues);
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
