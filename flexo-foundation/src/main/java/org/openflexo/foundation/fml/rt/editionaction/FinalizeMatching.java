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
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLNamedReference;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourArgument;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionFactory;
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
import org.openflexo.pamela.annotations.Updater;

/**
 * The FML statement {@code end match Concept in matchingSet unmatched: delete();}, closing a {@link MatchingSet}.
 * <p>
 * Execution runs a behaviour ({@link #getFlexoBehaviour()}) on each instance of the concept which has not been matched in the matching set.
 * With {@code unmatched: delete()}, this behaviour is the default deletion scheme of the concept (see backlog {@code CORE-F-2}), which
 * deletes the instance; a deleted instance is not removed from the roles of multiple cardinality holding it (known defect
 * {@code CORE-D-10}).
 * <p>
 * {@code unmatched: someBehaviour(args)} runs that behaviour of the concept, a deletion scheme other than the default one included. The
 * {@code unmatched:} clause is optional: without it, nothing runs and the unmatched instances are left as they are. For a complete
 * example, see
 * {@link MatchFlexoConceptInstance}.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FinalizeMatching.FinalizeMatchingMatchingImpl.class)
public interface FinalizeMatching extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String MATCHING_SET_KEY = "matchingSet";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = FlexoConceptInstanceType.class)
	public static final String MATCHED_TYPE_KEY = "matchedType";
	@PropertyIdentifier(type = FMLNamedReference.class)
	public static final String BEHAVIOUR_REFERENCE_KEY = "behaviourReference";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = MATCHING_SET_KEY)
	public DataBinding<MatchingSet> getMatchingSet();

	@Setter(MATCHING_SET_KEY)
	public void setMatchingSet(DataBinding<MatchingSet> matchingSet);

	@Getter(value = CONTAINER_KEY)
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	public FlexoConcept getOwnerConcept();

	public List<FlexoConcept> getAvailableFlexoConceptTypes();

	/**
	 * The type of the instances this action matches.<br>
	 * This is the property that carries the matched concept: when a compilation unit is merged with another one (the FML editor merges
	 * the text it has just parsed with {@code updateWith()}), {@link #updateMatchedType(FlexoConceptInstanceType)} translates the type
	 * into the typing space of this action.
	 *
	 * @return the matched type
	 */
	@Getter(value = MATCHED_TYPE_KEY, ignoreType = true)
	public FlexoConceptInstanceType getMatchedType();

	@Setter(MATCHED_TYPE_KEY)
	public void setMatchedType(FlexoConceptInstanceType matchedType);

	/**
	 * Translates the supplied type into the typing space of this action, then sets it
	 *
	 * @param matchedType
	 *            the matched type, possibly expressed in another typing space
	 */
	@Updater(MATCHED_TYPE_KEY)
	public void updateMatchedType(FlexoConceptInstanceType matchedType);

	/**
	 * The concept of the instances this action matches, the concept of {@link #getMatchedType()}
	 *
	 * @return the matched concept
	 */
	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	/**
	 * The behaviour executed on the instances which have not been matched, as a reference resolved in the matched concept (see
	 * {@link #getFlexoBehaviour()})
	 *
	 * @return the reference to the behaviour, null when none was set
	 */
	@Getter(value = BEHAVIOUR_REFERENCE_KEY, ignoreType = true)
	public FMLNamedReference<FlexoBehaviour> getBehaviourReference();

	@Setter(BEHAVIOUR_REFERENCE_KEY)
	public void setBehaviourReference(FMLNamedReference<FlexoBehaviour> behaviourReference);

	/**
	 * Keeps the name only of a reference coming from another compilation unit (merge with {@code updateWith()})
	 *
	 * @param behaviourReference
	 *            the reference held by the action of the other compilation unit
	 */
	@Updater(BEHAVIOUR_REFERENCE_KEY)
	public void updateBehaviourReference(FMLNamedReference<FlexoBehaviour> behaviourReference);

	/**
	 * Return the behaviour executed on the instances which have not been matched, null when none was set (no {@code unmatched:} clause):
	 * nothing runs then.
	 *
	 * @return the behaviour executed on unmatched instances
	 */
	public FlexoBehaviour getFlexoBehaviour();

	public void setFlexoBehaviour(FlexoBehaviour flexoBehaviour);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = ExecuteBehaviourParameter.ACTION_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ExecuteBehaviourParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<ExecuteBehaviourParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(ExecuteBehaviourParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(ExecuteBehaviourParameter aParameter);

	public ExecuteBehaviourParameter getParameter(FlexoBehaviourArgument p);

	public List<? extends FlexoBehaviour> getAvailableFlexoBehaviours();

	public static abstract class FinalizeMatchingMatchingImpl extends EditionActionImpl
			implements FinalizeMatching, PropertyChangeListener {

		private static final Logger logger = Logger.getLogger(FinalizeMatching.class.getPackage().getName());

		private DataBinding<MatchingSet> matchingSet;

		private FMLNamedReference<FlexoBehaviour> behaviourReference;
		// The behaviour this action listens to (to keep its parameters in step), the last one getFlexoBehaviour() returned
		private FlexoBehaviour listenedBehaviour;
		private DataBinding<FlexoConceptInstance> container;
		private FlexoConceptInstanceType matchedType;

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
		public DataBinding<FlexoConceptInstance> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, FlexoConceptInstance.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<FlexoConceptInstance> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(FlexoConceptInstance.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public FlexoConceptInstanceType getMatchedType() {
			return matchedType;
		}

		@Override
		public void setMatchedType(FlexoConceptInstanceType matchedType) {
			if (matchedType != this.matchedType) {
				FlexoConceptInstanceType oldValue = this.matchedType;
				// Do not force the resolution here: while parsing, the type may not be resolvable yet
				FlexoConcept oldConcept = oldValue != null && oldValue.isResolved() ? oldValue.getFlexoConcept() : null;
				this.matchedType = matchedType;
				getPropertyChangeSupport().firePropertyChange(MATCHED_TYPE_KEY, oldValue, matchedType);
				FlexoConcept newConcept = matchedType != null && matchedType.isResolved() ? matchedType.getFlexoConcept() : null;
				if (oldConcept != newConcept) {
					matchedConceptChanged(oldConcept, newConcept);
				}
			}
		}

		@Override
		public void updateMatchedType(FlexoConceptInstanceType matchedType) {
			if (matchedType != null && getDeclaringCompilationUnit() != null) {
				setMatchedType(matchedType.translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setMatchedType(matchedType);
			}
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (matchedType == null) {
				return null;
			}
			if (!matchedType.isResolved()) {
				matchedType.resolve();
			}
			return matchedType.getFlexoConcept();
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != getFlexoConceptType()) {
				setMatchedType(flexoConceptType != null ? flexoConceptType.getInstanceType() : null);
			}
		}

		private void matchedConceptChanged(FlexoConcept oldConcept, FlexoConcept newConcept) {
			getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldConcept, newConcept);
			getPropertyChangeSupport().firePropertyChange("availableFlexoBehaviours", null, getAvailableFlexoBehaviours());
			// The behaviour is resolved in the matched concept
			getFlexoBehaviour();
		}

		@Override
		public List<FlexoConcept> getAvailableFlexoConceptTypes() {
			FlexoConcept containerConcept = getOwnerConcept();
			if (containerConcept != null) {
				if (containerConcept instanceof VirtualModel)
					return ((VirtualModel) containerConcept).getAllRootFlexoConcepts();
				return containerConcept.getEmbeddedFlexoConcepts();
			}
			return Collections.emptyList();
		}

		@Override
		public FlexoConcept getOwnerConcept() {
			if (getContainer() != null && getContainer().isValid()) {
				Type analyzedType = getContainer().getAnalyzedType();
				if (analyzedType instanceof FlexoConceptInstanceType) {
					return ((FlexoConceptInstanceType) analyzedType).getFlexoConcept();
				}
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getContainer()) {
				getPropertyChangeSupport().firePropertyChange("ownerConcept", null, getOwnerConcept());
				getPropertyChangeSupport().firePropertyChange("availableFlexoConceptTypes", null, getAvailableFlexoConceptTypes());
			}
		}

		@Override
		public FMLNamedReference<FlexoBehaviour> getBehaviourReference() {
			return behaviourReference;
		}

		@Override
		public void setBehaviourReference(FMLNamedReference<FlexoBehaviour> behaviourReference) {
			if (requireChange(this.behaviourReference, behaviourReference)) {
				FMLNamedReference<FlexoBehaviour> oldValue = this.behaviourReference;
				this.behaviourReference = behaviourReference;
				getPropertyChangeSupport().firePropertyChange(BEHAVIOUR_REFERENCE_KEY, oldValue, behaviourReference);
				// Listen to the new behaviour and update the parameters
				getFlexoBehaviour();
			}
		}

		@Override
		public void updateBehaviourReference(FMLNamedReference<FlexoBehaviour> behaviourReference) {
			setBehaviourReference(behaviourReference != null ? behaviourReference.unresolved() : null);
		}

		@Override
		public List<? extends FlexoBehaviour> getAvailableFlexoBehaviours() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getAccessibleAbstractActionSchemes();
			}
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			FlexoConcept matchedConcept = getFlexoConceptType();
			FlexoBehaviour returned = null;
			// No behaviour set ('end match ... in set;' with no 'unmatched:' clause): nothing runs on the unmatched instances. There used to be
			// a fallback on the first action scheme of the concept, which ran a deletion scheme declared first (CORE-D-2, CORE-D-57)
			if (behaviourReference != null) {
				returned = behaviourReference.resolve(matchedConcept != null ? name -> matchedConcept.getFlexoBehaviour(name) : null);
			}
			if (returned != listenedBehaviour) {
				FlexoBehaviour oldValue = listenedBehaviour;
				listenedBehaviour = returned;
				fireBehaviourChanged(oldValue, returned);
			}
			return returned;
		}

		@Override
		public void setFlexoBehaviour(FlexoBehaviour flexoBehaviour) {
			setBehaviourReference(FMLNamedReference.to(flexoBehaviour));
		}

		@Override
		public ExecuteBehaviourParameter getParameter(FlexoBehaviourArgument p) {
			for (ExecuteBehaviourParameter addEPParam : getParameters()) {
				if (addEPParam.getParam() == p) {
					return addEPParam;
				}
			}
			return null;
		}

		private void fireBehaviourChanged(FlexoBehaviour oldValue, FlexoBehaviour newValue) {
			if (requireChange(oldValue, newValue)) {
				if (oldValue != null) {
					oldValue.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (newValue != null) {
					newValue.getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				getPropertyChangeSupport().firePropertyChange("flexoBehaviour", oldValue, newValue);
				updateParameters();
			}
		}

		private boolean isUpdatingParameters = false;

		private synchronized void updateParameters() {

			isUpdatingParameters = true;
			if (getFlexoBehaviour() == null) {
				for (ExecuteBehaviourParameter p : new ArrayList<>(getParameters())) {
					removeFromParameters(p);
				}
			}
			else {
				List<ExecuteBehaviourParameter> parametersToRemove = new ArrayList<>(getParameters());
				if (getFlexoBehaviour() != null) {
					for (FlexoBehaviourArgument p : getFlexoBehaviour().getParameters()) {
						ExecuteBehaviourParameter existingParam = getParameter(p);
						if (existingParam != null) {
							parametersToRemove.remove(existingParam);
						}
						else {
							if (getFMLModelFactory() != null) {
								addToParameters(getFMLModelFactory().newExecuteBehaviourParameter(p));
							}
						}
					}
				}
				for (ExecuteBehaviourParameter removeThis : parametersToRemove) {
					removeFromParameters(removeThis);
				}
			}
			isUpdatingParameters = false;
		}

		@Override
		public MatchingSet execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			if (evaluationContext instanceof FlexoBehaviourAction) {

				MatchingSet matchingSet = getMatchingSet(evaluationContext);

				for (FlexoConceptInstance fci : new ArrayList<>(matchingSet.getUnmatchedInstances())) {
					if (getFlexoConceptType().isAssignableFrom(fci.getFlexoConcept())) {
						FlexoBehaviourAction<?, ?, ?> behaviourAction = null;
						if (getFlexoBehaviour() instanceof ActionScheme) {
							ActionSchemeActionFactory actionType = new ActionSchemeActionFactory((ActionScheme) getFlexoBehaviour(), fci);
							behaviourAction = actionType.makeNewEmbeddedAction(fci, null,
									(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						}
						else if (getFlexoBehaviour() instanceof DeletionScheme) {
							DeletionSchemeActionFactory actionType = new DeletionSchemeActionFactory((DeletionScheme) getFlexoBehaviour(),
									fci);
							behaviourAction = actionType.makeNewEmbeddedAction(fci, null,
									(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
						}
						for (ExecuteBehaviourParameter p : getParameters()) {
							Object value = p.evaluateParameterValue(evaluationContext);
							if (value != null) {
								behaviourAction.setParameterValue(p.getParam(), value);
							}
						}
						if (behaviourAction != null) {
							behaviourAction.doAction();
							if (behaviourAction.hasActionExecutionSucceeded()) {
								matchingSet.finalizeFlexoConceptInstance(fci);
							}
							else if (behaviourAction.hasActionExecutionFailed()) {
								// The behaviour run on an unmatched instance failed: the end match fails (CORE-D-47)
								throw new FMLExecutionException("Could not execute " + behaviourAction.getFlexoBehaviour().getSignature()
										+ " on unmatched " + fci, behaviourAction.getThrownException());
							}
							else {
								// Not executed (disabled by @Enable): the instance is left as it is
								logger.warning("Did not execute " + behaviourAction.getFlexoBehaviour().getSignature() + " on unmatched " + fci);
							}
						}

					}
				}

			}
			else {
				logger.warning("Unexpected: " + evaluationContext);
				return null;
			}

			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource().equals(getFlexoBehaviour())) {
				updateParameters();
			}
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
		public Type getInferedType() {
			return Void.class;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getMatchingSet().rebuild();
		}

		@Override
		public String getStringRepresentation() {
			return "end match " + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "?");
		}

	}

	@DefineValidationRule
	public static class MatchingSetBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<FinalizeMatching> {
		public MatchingSetBindingIsRequiredAndMustBeValid() {
			super("'matching_set_'_binding_is_not_valid", FinalizeMatching.class);
		}

		@Override
		public DataBinding<?> getBinding(FinalizeMatching object) {
			return object.getMatchingSet();
		}

	}

}
