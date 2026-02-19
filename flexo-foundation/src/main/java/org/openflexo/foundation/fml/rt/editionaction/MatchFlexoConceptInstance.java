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
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
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
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * This action is used to perform synchronization regarding an {@link FlexoConceptInstance} in a given
 * {@link FMLRTVirtualModelInstance}.<br>
 * The matching is performed on some pattern roles, with some values retrieved from an expression.<br>
 * If target {@link FlexoConceptInstance} could not been looked up, then a new {@link FlexoConceptInstance} is created using supplied
 * {@link CreationScheme} and some parameters
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */
@ModelEntity
@ImplementationClass(MatchFlexoConceptInstance.MatchFlexoConceptInstanceImpl.class)
@XMLElement
@FML("MatchFlexoConceptInstance")
public interface MatchFlexoConceptInstance extends FMLRTAction<FlexoConceptInstance, FMLRTVirtualModelInstance> {

	@PropertyIdentifier(type = List.class)
	public static final String MATCHING_CRITERIAS_KEY = "matchingCriterias";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String MATCHING_SET_KEY = "matchingSet";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String NEW_INSTANCE_KEY = "newInstance";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@Getter(value = MATCHING_SET_KEY)
	@XMLAttribute
	public DataBinding<MatchingSet> getMatchingSet();

	@Setter(MATCHING_SET_KEY)
	public void setMatchingSet(DataBinding<MatchingSet> matchingSet);

	/**
	 * Expression used to instantiate a new instance if searched instance was not matched
	 * 
	 * @return
	 */
	@Getter(value = NEW_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getNewInstance();

	/**
	 * Sets expression used to instantiate a new instance if searched instance was not matched
	 * 
	 * @param newInstance
	 */
	@Setter(NEW_INSTANCE_KEY)
	public void setNewInstance(DataBinding<FlexoConceptInstance> newInstance);

	public List<FlexoBehaviourParameter> getNewInstanceArguments();

	public DataBinding<?> getNewInstanceArgumentValue(FlexoBehaviourParameter argument);

	public void setNewInstanceArgumentValue(FlexoBehaviourParameter argument, DataBinding<?> value);

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

	public MatchingCriteria addToMatchingCriteria(FlexoProperty<?> property, DataBinding<?> value);

	public MatchingCriteria getMatchingCriteria(FlexoProperty<?> flexoProperty);

	public CreationScheme getCreationScheme();

	public void setCreationScheme(CreationScheme creationScheme);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public VirtualModel getAddressedVirtualModel();

	public FlexoConceptInstanceType getMatchedType();

	public void setMatchedType(FlexoConceptInstanceType matchedType);

	public FMLRTVirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext);

	public FlexoConceptInstance getContainer(RunTimeEvaluationContext evaluationContext);

	public static abstract class MatchFlexoConceptInstanceImpl extends FMLRTActionImpl<FlexoConceptInstance, FMLRTVirtualModelInstance>
			implements MatchFlexoConceptInstance, PropertyChangeListener {

		private FlexoConcept flexoConceptType;
		private DataBinding<MatchingSet> matchingSet;
		private FlexoConceptInstanceType matchedType;

		private DataBinding<FlexoConceptInstance> newInstance;

		@Override
		public DataBinding<MatchingSet> getMatchingSet() {
			if (matchingSet == null) {
				matchingSet = new DataBinding<>(this, MatchingSet.class, BindingDefinitionType.GET);
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
		public DataBinding<FlexoConceptInstance> getNewInstance() {
			if (newInstance == null) {
				newInstance = new DataBinding<>(this, FlexoConceptInstance.class, BindingDefinitionType.GET);
				newInstance.setBindingName("newInstance");
			}
			return newInstance;
		}

		@Override
		public void setNewInstance(DataBinding<FlexoConceptInstance> newInstance) {
			if (newInstance != null) {
				newInstance.setOwner(this);
				newInstance.setBindingName("newInstance");
				newInstance.setDeclaredType(FlexoConceptInstance.class);
				newInstance.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.newInstance = newInstance;
		}

		@Override
		public String getParametersStringRepresentation() {
			return "(type=" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "null") + ","
					+ getMatchingCriteriasFMLRepresentation() + ")";
		}

		protected String getMatchingCriteriasFMLRepresentation() {

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

		@Override
		public FMLRTVirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			try {
				return getReceiver().getBindingValue(evaluationContext);
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

		public MatchingSet getMatchingSet(RunTimeEvaluationContext evaluationContext) {
			try {
				return getMatchingSet().getBindingValue(evaluationContext);
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
		public FlexoConceptInstance getContainer(RunTimeEvaluationContext evaluationContext) {
			try {
				return getContainer().getBindingValue(evaluationContext);
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
				getPropertyChangeSupport().firePropertyChange("newInstanceArguments", null, getNewInstanceArguments());
				// updateParameters();
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

			if (getNewInstance().isValid()) {
				if (getNewInstance().isBindingPath()) {
					BindingPath bp = (BindingPath) getNewInstance().getExpression();
					if (bp.getRootPathElement() instanceof CreationSchemePathElement) {
						CreationSchemePathElement<CreationScheme> cspe = (CreationSchemePathElement) bp.getRootPathElement();
						return cspe.getCreationScheme();
					}
				}
			}

			return null;
		}

		@Override
		public void setCreationScheme(CreationScheme creationScheme) {

			if (requireChange(getCreationScheme(), creationScheme)) {
				CreationScheme oldCS = getCreationScheme();
				CreationSchemePathElement<?> newCreationSchemePathElement = getFMLModelFactory().newCreationSchemePathElement(null,
						creationScheme, getCreationSchemePathElement() != null ? getCreationSchemePathElement().getArguments() : null,
						this);
				BindingPath bp = new BindingPath(Collections.singletonList(newCreationSchemePathElement), this,
						FMLPrettyPrinter.getInstance());
				DataBinding<FlexoConceptInstance> newInstance = new DataBinding<>(this, FlexoConceptInstance.class,
						BindingDefinitionType.GET);
				newInstance.setBindingName("newInstance");
				newInstance.setExpression(bp);
				setNewInstance(newInstance);
				fireCreationSchemeChange(oldCS, getCreationScheme());
			}

		}

		private CreationSchemePathElement<CreationScheme> getCreationSchemePathElement() {
			if (getNewInstance().isBindingPath()) {
				BindingPath bp = (BindingPath) getNewInstance().getExpression();
				if (bp.getRootPathElement() instanceof CreationSchemePathElement) {
					return (CreationSchemePathElement) bp.getRootPathElement();
				}
			}
			return null;
		}

		@Override
		public List<FlexoBehaviourParameter> getNewInstanceArguments() {
			CreationSchemePathElement<CreationScheme> cspe = getCreationSchemePathElement();
			if (cspe != null) {
				return (List<FlexoBehaviourParameter>) cspe.getFunctionArguments();
			}
			return null;
		}

		@Override
		public DataBinding<?> getNewInstanceArgumentValue(FlexoBehaviourParameter argument) {
			CreationSchemePathElement<CreationScheme> cspe = getCreationSchemePathElement();
			if (cspe != null) {
				return cspe.getArgumentValue(argument);
			}
			return null;
		}

		@Override
		public void setNewInstanceArgumentValue(FlexoBehaviourParameter argument, DataBinding<?> value) {
			CreationSchemePathElement<CreationScheme> cspe = getCreationSchemePathElement();
			if (cspe != null) {
				value.setOwner(this);
				value.setBindingName(argument.getName());
				value.setDeclaredType(argument.getType());
				value.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				cspe.setArgumentValue(argument, value);
			}
		}

		@Override
		public MatchingCriteria getMatchingCriteria(FlexoProperty<?> pr) {
			for (MatchingCriteria mc : getMatchingCriterias()) {
				if (mc.getFlexoProperty() == pr) {
					return mc;
				}
			}
			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource().equals(getFlexoConceptType()) && !isDeleting) {
				updateMatchingCriterias();
			}
		}

		private synchronized void updateMatchingCriterias() {

			// Unused isUpdatingMatchingCriterias = true;
			if (getFlexoConceptType() == null) {
				for (MatchingCriteria criteriaToRemove : new ArrayList<>(getMatchingCriterias())) {
					removeFromMatchingCriterias(criteriaToRemove);
					criteriaToRemove.delete();
				}
			}
			else {
				List<MatchingCriteria> criteriasToRemove = new ArrayList<>(getMatchingCriterias());
				for (FlexoProperty<?> property : getFlexoConceptType().getAccessibleProperties()) {
					MatchingCriteria existingCriteria = getMatchingCriteria(property);
					if (existingCriteria != null) {
						criteriasToRemove.remove(existingCriteria);
					}
					else {
						// System.out.println("ADD " + property.getName() + " updateMatchingCriterias for " +
						// Integer.toHexString(hashCode()));
						// addToMatchingCriterias(getFMLModelFactory().newMatchingCriteria(property));
						// System.out.println("addToMatchingCriterias for " + property);
					}
				}
				for (MatchingCriteria removeThis : criteriasToRemove) {
					System.out.println("REMOVE " + removeThis.getFlexoProperty() + " value=" + removeThis.getValue()
							+ " updateMatchingCriterias for " + Integer.toHexString(hashCode()));
					removeFromMatchingCriterias(removeThis);
					removeThis.delete();
				}

			}
			// Unused isUpdatingMatchingCriterias = false;

		}

		@Override
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Perform perform MatchFlexoConceptInstance " + evaluationContext);
			}
			if (evaluationContext instanceof FlexoBehaviourAction) {

				MatchingSet matchingSet = null;

				if (getMatchingSet().isValid()) {
					matchingSet = getMatchingSet(evaluationContext);
				}

				if (matchingSet == null) {
					matchingSet = ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).initiateDefaultMatchingSet(this);
				}

				FMLRTVirtualModelInstance vmInstance = getVirtualModelInstance(evaluationContext);
				FlexoConceptInstance container = getContainer(evaluationContext);

				if (vmInstance == null) {
					if (container instanceof FMLRTVirtualModelInstance) {
						vmInstance = (FMLRTVirtualModelInstance) container;
					}
					else {
						if (container.getVirtualModelInstance() instanceof FMLRTVirtualModelInstance) {
							vmInstance = (FMLRTVirtualModelInstance) container.getVirtualModelInstance();
						}
					}
				}

				Hashtable<FlexoProperty<?>, Object> criterias = new Hashtable<>();
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
						logger.fine("Criteria: " + mc.getFlexoProperty().getPropertyName() + "=" + criterias.get(mc.getFlexoProperty())
								+ " valid=" + mc.getValue().isValid());
					}
				}

				FlexoConceptInstance matchingFlexoConceptInstance = matchingSet.matchFlexoConceptInstance(criterias);

				if (matchingFlexoConceptInstance != null) {
					// A matching FlexoConceptInstance was found

					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Found " + matchingFlexoConceptInstance);
					}
					matchingSet.foundMatchingFlexoConceptInstance(matchingFlexoConceptInstance);

				}
				else {

					// We have to create a new FlexoConceptInstance
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Creating new FCI for " + getCreationScheme().getFlexoConcept() + " using "
								+ getCreationScheme().getName());
					}

					try {
						matchingFlexoConceptInstance = getNewInstance().getBindingValue(evaluationContext);
					} catch (TypeMismatchException e) {
						throw new FMLExecutionException(e);
					} catch (NullReferenceException e) {
						throw new FMLExecutionException(e);
					} catch (ReflectiveOperationException e) {
						throw new FMLExecutionException(e);
					}
				}

				return matchingFlexoConceptInstance;
			}
			logger.warning("Unexpected: " + evaluationContext);
			return null;
		}

		@Override
		public Type getAssignableType() {
			return getMatchedType();
		}

		@Override
		public FlexoConceptInstanceType getMatchedType() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getInstanceType();
			}
			return matchedType;
		}

		@Override
		public void setMatchedType(FlexoConceptInstanceType matchedType) {
			this.matchedType = matchedType;
			if (matchedType.isResolved()) {
				flexoConceptType = matchedType.getFlexoConcept();
			}
		}

		@Override
		public Class<FMLRTVirtualModelInstance> getVirtualModelInstanceClass() {
			return FMLRTVirtualModelInstance.class;
		}

		private DataBinding<FlexoConceptInstance> container;

		@Override
		public DataBinding<FlexoConceptInstance> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, FlexoConceptInstance.class, DataBinding.BindingDefinitionType.GET);
				container.setBindingName("container");
				container.setDeclaredType(getFlexoConceptType() != null && getFlexoConceptType().getContainerFlexoConcept() != null
						? getFlexoConceptType().getApplicableContainerFlexoConcept().getInstanceType()
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
						? getFlexoConceptType().getApplicableContainerFlexoConcept().getInstanceType()
						: FlexoConceptInstance.class);
				aContainer.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.container = aContainer;
		}

		private boolean isAnalyzingContainer = false;

		/**
		 * Return the {@link VirtualModel} beeing addressed by this action, according to the {@link #getVirtualModelInstance()} binding
		 * 
		 * @return
		 */
		@Override
		public VirtualModel getAddressedVirtualModel() {

			if (getReceiver() != null && getReceiver().isSet()) {
				if (isAnalyzingContainer) {
					return null;
				}
				if (getReceiver().isValid()) {
					isAnalyzingContainer = true;
					Type vmiType = getReceiver().getAnalyzedType();
					isAnalyzingContainer = false;
					if (vmiType instanceof VirtualModelInstanceType) {
						return ((VirtualModelInstanceType) vmiType).getVirtualModel();
					}
				}
			}
			// I could not find VM, trying to "guess" (TODO: remove this hack ?)
			if (getFlexoConcept() instanceof VirtualModel) {
				return (VirtualModel) getFlexoConcept();
			}
			if (getInferedModelSlot() != null) {
				return getInferedModelSlot().getAccessedVirtualModel();
			}
			return getOwningVirtualModel();
		}

		@Override
		public MatchingCriteria addToMatchingCriteria(FlexoProperty<?> property, DataBinding<?> argValue) {
			if (getFMLModelFactory() != null) {
				MatchingCriteria newMatchingCriteria = getFMLModelFactory().newMatchingCriteria(null);
				newMatchingCriteria.setFlexoProperty(property);
				newMatchingCriteria.setValue(argValue);
				addToMatchingCriterias(newMatchingCriteria);
				return newMatchingCriteria;
			}
			return null;
		}

		@Override
		public boolean isReceiverMandatory() {
			return false;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getMatchingSet().rebuild();
		}

		@Override
		public String getStringRepresentation() {
			return "match " + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "?") + " from " + getContainer();
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
					return new ValidationError<>(this, action, "match_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				return new ValidationError<>(this, action, "match_flexo_concept_action_doesn't_define_any_creation_scheme");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class NewInstanceBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance> {
		public NewInstanceBindingIsRequiredAndMustBeValid() {
			super("'new_instance'_binding_is_not_valid", MatchFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<?> getBinding(MatchFlexoConceptInstance object) {
			return object.getNewInstance();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> applyValidation(
				MatchFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<MatchFlexoConceptInstance>, MatchFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned == null) {
				DataBinding<?> binding = getBinding(object);
				if (!binding.isBindingPath()) {
					return new ValidationError<>(this, object, "'new_instance'_binding_must_define_a_binding_path");
				}
				else {
					BindingPath bp = (BindingPath) binding.getExpression();
					if (!(bp.getRootPathElement() instanceof CreationSchemePathElement)) {
						return new ValidationError<>(this, object, "'new_instance'_binding_must_instantiate_a_new_instance");
					}
					// We should also check that the newly created instance type is compatible with the one which has been declared
					CreationSchemePathElement<CreationScheme> cspe = (CreationSchemePathElement<CreationScheme>) bp.getRootPathElement();
					if (!object.getFlexoConceptType().isAssignableFrom(cspe.getCreationScheme().getFlexoConcept())) {
						return new ValidationError<>(this, object, "'new_instance'_type_must_be_compatible_with_the_declaration");
					}
				}
			}
			return returned;
		}

	}

}
