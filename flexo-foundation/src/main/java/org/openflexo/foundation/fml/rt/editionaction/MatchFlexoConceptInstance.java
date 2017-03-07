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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
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
import org.openflexo.model.validation.FixProposal;
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
public interface MatchFlexoConceptInstance extends FMLRTAction<FlexoConceptInstance, VirtualModelInstance> {

	// @PropertyIdentifier(type = DataBinding.class)
	// public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String MATCHING_CRITERIAS_KEY = "matchingCriterias";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String MATCHING_SET_KEY = "matchingSet";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@Getter(value = MATCHING_SET_KEY)
	@XMLAttribute
	public DataBinding<MatchingSet> getMatchingSet();

	@Setter(MATCHING_SET_KEY)
	public void setMatchingSet(DataBinding<MatchingSet> matchingSet);

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

	public MatchingCriteria getMatchingCriteria(FlexoProperty<?> flexoProperty);

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

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public CreateFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p);

	public static abstract class MatchFlexoConceptInstanceImpl extends FMLRTActionImpl<FlexoConceptInstance, VirtualModelInstance>
			implements MatchFlexoConceptInstance, PropertyChangeListener {

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private DataBinding<MatchingSet> matchingSet;

		@Override
		public DataBinding<MatchingSet> getMatchingSet() {
			if (matchingSet == null) {
				matchingSet = new DataBinding<MatchingSet>(this, MatchingSet.class, BindingDefinitionType.GET);
				matchingSet.setBindingName("matchingSet");
			}
			return matchingSet;
		}

		@Override
		public void setMatchingSet(DataBinding<MatchingSet> matchingSet) {
			if (matchingSet != null) {
				matchingSet.setOwner(this);
				matchingSet.setBindingName("matchingSet");
				matchingSet.setDeclaredType(MatchingSet.class);
				matchingSet.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.matchingSet = matchingSet;
		}

		@Override
		public String getParametersStringRepresentation() {
			return "(type=" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "null") + ","
					+ getMatchingCriteriasFMLRepresentation(null) + ")";
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			/*if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}*/
			out.append(getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName() + " as "
					+ (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "no type specified") + " "
					+ getMatchingCriteriasFMLRepresentation(context) + " using "
					+ (getCreationScheme() != null ? getCreationScheme().getFlexoConcept().getName() : "no creation scheme specified") + ":"
					+ (getCreationScheme() != null ? getCreationScheme().getName() : "no creation scheme specified") + "("
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
				sb.append("match=");
				if (matchingCriterias.size() > 1) {
					sb.append("(");
				}
				boolean isFirst = true;
				for (MatchingCriteria mc : matchingCriterias) {
					FlexoProperty<?> pr = mc.getFlexoProperty();
					DataBinding<?> val = mc.getValue();
					if (pr != null && val != null && val.isSet()) {
						sb.append((isFirst ? "" : ",") + (pr.getName() != null ? pr.getName() : "null") + "=" + mc.getValue().toString());
						isFirst = false;
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

		public VirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			try {
				return getVirtualModelInstance().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		public MatchingSet getMatchingSet(RunTimeEvaluationContext evaluationContext) {
			try {
				return getMatchingSet().getBindingValue(evaluationContext);
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
				fireFlexoConceptChange(oldFlexoConcept, newFlexoConcept);
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, newValue);
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
				}
				else {
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

			isUpdatingParameters = true;
			if (getCreationScheme() == null) {
				for (CreateFlexoConceptInstanceParameter p : new ArrayList<CreateFlexoConceptInstanceParameter>(getParameters())) {
					removeFromParameters(p);
				}
			}
			else {
				List<CreateFlexoConceptInstanceParameter> parametersToRemove = new ArrayList<CreateFlexoConceptInstanceParameter>(
						getParameters());
				if (getCreationScheme() != null) {
					for (FlexoBehaviourParameter p : getCreationScheme().getParameters()) {
						CreateFlexoConceptInstanceParameter existingParam = getParameter(p);
						if (existingParam != null) {
							parametersToRemove.remove(existingParam);
						}
						else {
							if (getFMLModelFactory() != null) {
								addToParameters(getFMLModelFactory().newCreateFlexoConceptInstanceParameter(p));
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
		public MatchingCriteria getMatchingCriteria(FlexoProperty pr) {
			for (MatchingCriteria mc : getMatchingCriterias()) {
				if (mc.getFlexoProperty() == pr) {
					return mc;
				}
			}
			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource().equals(getCreationScheme())) {
				updateParameters();
			}
			else if (evt.getSource().equals(getFlexoConceptType())) {
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
			}
			else {
				List<MatchingCriteria> criteriasToRemove = new ArrayList<MatchingCriteria>(getMatchingCriterias());
				for (FlexoProperty<?> property : getFlexoConceptType().getAccessibleProperties()) {
					MatchingCriteria existingCriteria = getMatchingCriteria(property);
					if (existingCriteria != null) {
						criteriasToRemove.remove(existingCriteria);
					}
					else {
						// System.out.println("ADD " + property.getName() + " updateMatchingCriterias for " +
						// Integer.toHexString(hashCode()));
						addToMatchingCriterias(getFMLModelFactory().newMatchingCriteria(property));
					}
				}
				for (MatchingCriteria removeThis : criteriasToRemove) {
					System.out.println("REMOVE " + removeThis.getFlexoProperty().getName() + " value=" + removeThis.getValue()
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
						addToMatchingCriterias(getFMLModelFactory().newMatchingCriteria(pr));
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
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) {
			logger.fine("Perform perform MatchFlexoConceptInstance " + evaluationContext);

			if (evaluationContext instanceof FlexoBehaviourAction) {

				MatchingSet matchingSet = null;

				if (getMatchingSet().isValid()) {
					matchingSet = getMatchingSet(evaluationContext);
				}

				if (matchingSet == null) {
					matchingSet = ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).initiateDefaultMatchingSet(this);
				}

				VirtualModelInstance vmInstance = getVirtualModelInstance(evaluationContext);
				Hashtable<FlexoProperty<?>, Object> criterias = new Hashtable<FlexoProperty<?>, Object>();
				for (MatchingCriteria mc : getMatchingCriterias()) {
					Object value = mc.evaluateCriteriaValue(evaluationContext);
					if (value != null) {
						criterias.put(mc.getFlexoProperty(), value);
					}
				}

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(">>>>>>>> Matching FCI with following criterias");
					logger.fine("Type=" + getFlexoConceptType());
					for (MatchingCriteria mc : getMatchingCriterias()) {
						logger.fine("Criteria: " + mc.getFlexoProperty().getPropertyName() + "=" + mc.getValue() + " valid="
								+ mc.getValue().isValid());
					}
				}

				FlexoConceptInstance matchingFlexoConceptInstance = matchingSet.matchFlexoConceptInstance(criterias);

				// FlexoConceptInstance matchingFlexoConceptInstance = ((FlexoBehaviourAction) evaluationContext)
				// .matchFlexoConceptInstance(getFlexoConceptType(), criterias);

				if (matchingFlexoConceptInstance != null) {
					// A matching FlexoConceptInstance was found

					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Found " + matchingFlexoConceptInstance);
					}
					// ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).foundMatchingFlexoConceptInstance(matchingFlexoConceptInstance);

					matchingSet.foundMatchingFlexoConceptInstance(matchingFlexoConceptInstance);

				}
				else {

					// We have to create a new FlexoConceptInstance
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Creating new FCI for " + getCreationScheme().getFlexoConcept() + " using "
								+ getCreationScheme().getName());
					}

					CreationSchemeAction creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(vmInstance, null,
							((FlexoBehaviourAction<?, ?, ?>) evaluationContext));
					creationSchemeAction.setVirtualModelInstance(vmInstance);

					FlexoConceptInstance container = getContainer(evaluationContext);
					if (container != null) {
						creationSchemeAction.setContainer(container);
					}

					creationSchemeAction.setCreationScheme(getCreationScheme());
					// System.out.println("Creation scheme: " + getCreationScheme());
					// System.out.println("FML=" + getCreationScheme().getFMLRepresentation());
					for (CreateFlexoConceptInstanceParameter p : getParameters()) {
						// FlexoBehaviourParameter param = p.getParam();
						Object value = p.evaluateParameterValue(evaluationContext);
						if (value != null) {
							// System.out.println("Param " + p.getParam() + " = " + value);
							creationSchemeAction.setParameterValue(p.getParam(), value/*p.evaluateParameterValue(action)*/);
						}
					}
					creationSchemeAction.doAction();
					if (creationSchemeAction.hasActionExecutionSucceeded()) {
						matchingFlexoConceptInstance = creationSchemeAction.getFlexoConceptInstance();
						// ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).newFlexoConceptInstance(matchingFlexoConceptInstance);
						matchingSet.newFlexoConceptInstance(matchingFlexoConceptInstance);
					}
					else {
						logger.warning("Could not create FlexoConceptInstance for " + evaluationContext);
					}
				}

				return matchingFlexoConceptInstance;
			}
			else {
				logger.warning("Unexpected: " + evaluationContext);
				return null;
			}

		}

		@Override
		public Type getAssignableType() {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
		}

		@Override
		public Class<VirtualModelInstance> getVirtualModelInstanceClass() {
			return VirtualModelInstance.class;
		}

		private DataBinding<FlexoConceptInstance> container;

		@Override
		public DataBinding<FlexoConceptInstance> getContainer() {
			if (container == null) {
				container = new DataBinding<FlexoConceptInstance>(this, FlexoConceptInstance.class, DataBinding.BindingDefinitionType.GET);
				container.setBindingName("container");
				container.setDeclaredType(getFlexoConceptType() != null && getFlexoConceptType().getContainerFlexoConcept() != null
						? getFlexoConceptType().getContainerFlexoConcept().getInstanceType() : FlexoConceptInstance.class);
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<FlexoConceptInstance> aContainer) {
			if (aContainer != null) {
				aContainer.setOwner(this);
				aContainer.setBindingName("container");
				aContainer.setDeclaredType(getFlexoConceptType() != null && getFlexoConceptType().getContainerFlexoConcept() != null
						? getFlexoConceptType().getContainerFlexoConcept().getInstanceType() : FlexoConceptInstance.class);
				aContainer.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.container = aContainer;
		}

	}

	@DefineValidationRule
	public static class MatchFlexoConceptInstanceMustAddressACreationScheme
			extends ValidationRule<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance> {
		public MatchFlexoConceptInstanceMustAddressACreationScheme() {
			super(MatchFlexoConceptInstance.class, "match_flexo_concept_action_must_address_a_valid_creation_scheme");
		}

		@Override
		public ValidationIssue<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance> applyValidation(
				MatchFlexoConceptInstance action) {
			if (action.getCreationScheme() == null) {
				if (action.getFlexoConceptType() == null) {
					return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(this, action,
							"match_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				else {
					return new ValidationError<MatchFlexoConceptInstanceMustAddressACreationScheme, MatchFlexoConceptInstance>(this, action,
							"match_flexo_concept_action_doesn't_define_any_creation_scheme");
				}
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class MatchFlexoConceptInstanceParametersMustBeValid
			extends ValidationRule<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance> {

		static final Logger logger = Logger.getLogger(MatchFlexoConceptInstance.class.getPackage().getName());

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
							}
							else {
								issues.add(
										new ValidationError(this, action, "parameter_s_value_is_not_defined: " + p.getParam().getName()));
							}
						}
						else if (!p.getValue().isValid()) {
							logger.info("Binding NOT valid: " + p.getValue() + " for " + p.getName() + " object="
									+ p.getAction().getStringRepresentation() + ". Reason: " + p.getValue().invalidBindingReason());
							issues.add(new ValidationError(this, action, "parameter_s_value_is_not_valid: " + p.getParam().getName()));
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
					return new CompoundIssue<MatchFlexoConceptInstanceParametersMustBeValid, MatchFlexoConceptInstance>(action, issues);
				}
			}
			return null;
		}
	}

	/*@DefineValidationRule
	public static class MatchingSetBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance> {
		public MatchingSetBindingIsRequiredAndMustBeValid() {
			super("'matching_set'_binding_is_not_valid", MatchFlexoConceptInstance.class);
		}
	
		@Override
		public DataBinding<MatchingSet> getBinding(MatchFlexoConceptInstance object) {
			return object.getMatchingSet();
		}
	
	}*/

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", MatchFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance> getBinding(MatchFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> applyValidation(
				MatchFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			else {
				DataBinding<VirtualModelInstance> binding = getBinding(object);
				if (binding.getAnalyzedType() instanceof VirtualModelInstanceType && object.getFlexoConceptType() != null) {
					if (object.getFlexoConceptType().getVirtualModel() != ((VirtualModelInstanceType) binding.getAnalyzedType())
							.getVirtualModel()) {
						System.out.println("VM1=" + object.getFlexoConceptType().getVirtualModel());
						System.out.println("VM1=" + Integer.toHexString(object.getFlexoConceptType().getVirtualModel().hashCode()));
						System.out.println("VM2=" + ((VirtualModelInstanceType) binding.getAnalyzedType()).getVirtualModel());
						System.out.println("VM2="
								+ Integer.toHexString(((VirtualModelInstanceType) binding.getAnalyzedType()).getVirtualModel().hashCode()));
						returned = new ValidationError(this, object, "incompatible_virtual_model_type avec " + object.getFlexoConceptType()
								+ " et " + binding.getAnalyzedType());

						/*System.out.println(object.getRootOwner().getFMLRepresentation());
						System.out.println("FC=" + object.getRootOwner().getFlexoConcept());
						System.out.println("VM=" + object.getOwningVirtualModel());
						System.out.println("modelSlots=" + object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class));*/

						// Attempt to find some solutions...

						for (FMLRTModelSlot ms : object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class)) {
							// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
							if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAccessedVirtualModel())) {
								((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
							}
						}

						if (object.getRootOwner().getFlexoConcept() instanceof AbstractVirtualModel) {
							for (FMLRTModelSlot ms : ((AbstractVirtualModel<?>) object.getRootOwner().getFlexoConcept())
									.getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAccessedVirtualModel())) {
									((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
								}
							}
						}

					}
				}
			}
			return returned;
		}

		protected static class UseLocalVirtualModelInstance
				extends FixProposal<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				MatchFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<VirtualModelInstance>("virtualModelInstance"));
			}
		}

		protected static class UseFMLRTModelSlot
				extends FixProposal<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> {

			private final FMLRTModelSlot modelSlot;

			public UseFMLRTModelSlot(FMLRTModelSlot modelSlot) {
				super("sets_virtual_model_instance_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}

			@Override
			protected void fixAction() {
				MatchFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<VirtualModelInstance>(modelSlot.getName()));
			}
		}

	}

}
