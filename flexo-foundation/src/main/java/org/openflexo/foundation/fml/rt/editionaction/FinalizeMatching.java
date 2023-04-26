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
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
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
import org.openflexo.toolbox.StringUtils;

/**
 * Primitive used to finalize a {@link MatchingSet}
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FinalizeMatching.FinalizeMatchingMatchingImpl.class)
@XMLElement
public interface FinalizeMatching extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String MATCHING_SET_KEY = "matchingSet";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";
	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_BEHAVIOUR_URI_KEY = "flexoBehaviourURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = MATCHING_SET_KEY)
	@XMLAttribute
	public DataBinding<MatchingSet> getMatchingSet();

	@Setter(MATCHING_SET_KEY)
	public void setMatchingSet(DataBinding<MatchingSet> matchingSet);

	@Getter(value = CONTAINER_KEY, ignoreForEquality = true)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	public FlexoConcept getOwnerConcept();

	public List<FlexoConcept> getAvailableFlexoConceptTypes();

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String uri);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	@Getter(value = FLEXO_BEHAVIOUR_URI_KEY)
	@XMLAttribute
	public String getFlexoBehaviourURI();

	@Setter(FLEXO_BEHAVIOUR_URI_KEY)
	public void setFlexoBehaviourURI(String flexoBehaviourURI);

	public FlexoBehaviour getFlexoBehaviour();

	public void setFlexoBehaviour(FlexoBehaviour flexoBehaviour);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = ExecuteBehaviourParameter.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ExecuteBehaviourParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<ExecuteBehaviourParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(ExecuteBehaviourParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(ExecuteBehaviourParameter aParameter);

	public ExecuteBehaviourParameter getParameter(FlexoBehaviourParameter p);

	public List<? extends FlexoBehaviour> getAvailableFlexoBehaviours();

	public FlexoConceptInstanceType getMatchedType();

	public void setMatchedType(FlexoConceptInstanceType matchedType);

	public static abstract class FinalizeMatchingMatchingImpl extends EditionActionImpl
			implements FinalizeMatching, PropertyChangeListener {

		private static final Logger logger = Logger.getLogger(FinalizeMatching.class.getPackage().getName());

		private DataBinding<MatchingSet> matchingSet;

		private FlexoBehaviour flexoBehaviour;
		private String flexoBehaviourURI;
		private DataBinding<FlexoConceptInstance> container;
		private String flexoConceptTypeURI;
		private FlexoConcept flexoConceptType;
		private FlexoConceptInstanceType matchedType;

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getMatchingSet() + ".finalizeMatching()";
		}

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

		@Getter(value = CONTAINER_KEY)
		@XMLAttribute
		@Override
		public String _getFlexoConceptTypeURI() {
			if (flexoConceptType != null) {
				return flexoConceptType.getURI();
			}
			if (matchedType != null) {
				if (matchedType.isResolved()) {
					return matchedType.getFlexoConcept().getURI();
				}
				return matchedType.getConceptURI();
			}
			return flexoConceptTypeURI;
		}

		@Override
		public void _setFlexoConceptTypeURI(String uri) {
			if (getVirtualModelLibrary() != null) {
				flexoConceptType = getVirtualModelLibrary().getFlexoConcept(uri);
			}
			flexoConceptTypeURI = uri;
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (flexoConceptType == null && StringUtils.isNotEmpty(flexoConceptTypeURI) && getVirtualModelLibrary() != null) {
				flexoConceptType = getVirtualModelLibrary().getFlexoConcept(flexoConceptTypeURI, false);
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				FlexoConcept oldValue = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, flexoConceptType);
				getPropertyChangeSupport().firePropertyChange("flexoBehaviour", false, true);
				getPropertyChangeSupport().firePropertyChange("availableFlexoBehaviours", null, getAvailableFlexoBehaviours());
			}
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
		public String getFlexoBehaviourURI() {
			if (getFlexoBehaviour() != null) {
				return getFlexoBehaviour().getURI();
			}
			return flexoBehaviourURI;
		}

		@Override
		public void setFlexoBehaviourURI(String uri) {
			if (requireChange(getFlexoBehaviourURI(), uri)) {
				String oldURI = getFlexoBehaviourURI();
				FlexoBehaviour oldCS = getFlexoBehaviour();
				flexoBehaviourURI = uri;
				if (getVirtualModelLibrary() != null) {
					flexoBehaviour = getVirtualModelLibrary().getFlexoBehaviour(uri, true);
				}
				fireBehaviourChanged(oldCS, getFlexoBehaviour());
				getPropertyChangeSupport().firePropertyChange(FLEXO_BEHAVIOUR_URI_KEY, oldURI, uri);
			}
		}

		@Override
		public List<? extends FlexoBehaviour> getAvailableFlexoBehaviours() {
			if (getFlexoConceptType() != null) {
				// return new ConcatenedList<>(getFlexoConceptType().getAccessibleDeletionSchemes(),
				// getFlexoConceptType().getAccessibleAbstractActionSchemes());
				return getFlexoConceptType().getAccessibleAbstractActionSchemes();
			}
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			if (flexoBehaviour == null && flexoBehaviourURI != null && getVirtualModelLibrary() != null) {
				flexoBehaviour = getVirtualModelLibrary().getFlexoBehaviour(flexoBehaviourURI, true);
			}
			if (flexoBehaviour == null && getFlexoConceptType() != null && getAvailableFlexoBehaviours() != null
					&& getAvailableFlexoBehaviours().size() > 0) {
				flexoBehaviour = getAvailableFlexoBehaviours().get(0);
				fireBehaviourChanged(null, flexoBehaviour);
			}
			return flexoBehaviour;
		}

		@Override
		public void setFlexoBehaviour(FlexoBehaviour creationScheme) {
			if (requireChange(getFlexoBehaviour(), creationScheme)) {
				FlexoBehaviour oldCS = getFlexoBehaviour();
				this.flexoBehaviour = creationScheme;
				if (creationScheme != null) {
					flexoBehaviourURI = creationScheme.getURI();
				}
				else {
					flexoBehaviourURI = null;
				}
				fireBehaviourChanged(oldCS, creationScheme);
			}
		}

		@Override
		public ExecuteBehaviourParameter getParameter(FlexoBehaviourParameter p) {
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
					for (FlexoBehaviourParameter p : getFlexoBehaviour().getParameters()) {
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
		public MatchingSet execute(RunTimeEvaluationContext evaluationContext) {
			/*try {
				System.out.println("Finalizing MatchingSet " + getMatchingSet() + " pour " + getFlexoConceptType() + " dans "
						+ getContainer().getBindingValue(evaluationContext));
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}*/

			// System.out.println("evaluationContext=" + evaluationContext);

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
							// FlexoBehaviourParameter param = p.getParam();
							Object value = p.evaluateParameterValue(evaluationContext);
							if (value != null) {
								// System.out.println("Param " + p.getParam() + " = " + value);
								behaviourAction.setParameterValue(p.getParam(), value/*p.evaluateParameterValue(action)*/);
							}
						}
						// System.out.println("executing " + behaviourAction.getFlexoBehaviour().getSignature() + " for " + fci);
						if (behaviourAction != null) {
							behaviourAction.doAction();
							if (behaviourAction.hasActionExecutionSucceeded()) {
								matchingSet.finalizeFlexoConceptInstance(fci);
							}
							else {
								logger.warning("Could not execute " + behaviourAction.getFlexoBehaviour().getSignature() + " for "
										+ evaluationContext);
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
