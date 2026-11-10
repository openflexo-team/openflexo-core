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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
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
 * The FML action {@code begin match Concept from (container)}, opening a {@link MatchingSet}, as in
 * {@code MatchingSet<IndexEntry> entries = begin match IndexEntry from this;}.
 * <p>
 * The matching set holds the instances of the concept found in the container: the instances of a VirtualModelInstance, or the instances
 * embedded in a concept instance. The following {@code match ... in entries} statements ({@link MatchFlexoConceptInstance}) mark them as
 * matched, and {@code end match ... in entries} ({@link FinalizeMatching}) processes the others.
 * <p>
 * The {@code where} clause of {@code begin match} is parsed but ignored (known defect {@code CORE-D-12}): the conditions
 * ({@link #getConditions()}) are only set programmatically. For a complete example, see {@link MatchFlexoConceptInstance}.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(InitiateMatching.InitiateMatchingImpl.class)
public interface InitiateMatching extends AssignableAction<MatchingSet> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = FlexoConceptInstanceType.class)
	public static final String MATCHED_TYPE_KEY = "matchedType";
	@PropertyIdentifier(type = Vector.class)
	public static final String CONDITIONS_KEY = "conditions";

	@Getter(value = CONTAINER_KEY)
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

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

	@Getter(value = CONDITIONS_KEY, cardinality = Cardinality.LIST, inverse = MatchCondition.ACTION_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<MatchCondition> getConditions();

	@Setter(CONDITIONS_KEY)
	public void setConditions(List<MatchCondition> conditions);

	@Adder(CONDITIONS_KEY)
	public void addToConditions(MatchCondition aCondition);

	@Remover(CONDITIONS_KEY)
	public void removeFromConditions(MatchCondition aCondition);

	public FlexoConcept getOwnerConcept();

	public MatchCondition createCondition();

	public void deleteCondition(MatchCondition aCondition);

	public List<FlexoConcept> getAvailableFlexoConceptTypes();

	public static abstract class InitiateMatchingImpl extends AssignableActionImpl<MatchingSet> implements InitiateMatching {

		private static final Logger logger = Logger.getLogger(InitiateMatching.class.getPackage().getName());

		private DataBinding<FlexoConceptInstance> container;

		private FlexoConceptInstanceType matchedType;

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
		}

		@Override
		public List<FlexoConcept> getAvailableFlexoConceptTypes() {
			FlexoConcept containerConcept = getOwnerConcept();
			if (containerConcept != null) {
				if (containerConcept instanceof VirtualModel) {
					return ((VirtualModel) containerConcept).getAllRootFlexoConcepts();
				}
				return containerConcept.getEmbeddedFlexoConcepts();
			}
			return Collections.emptyList();
		}

		@Override
		public Type getAssignableType() {
			return MatchingSet.class;
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
		public MatchCondition createCondition() {
			MatchCondition newCondition = getFMLModelFactory().newMatchCondition();
			addToConditions(newCondition);
			return newCondition;
		}

		@Override
		public void deleteCondition(MatchCondition aCondition) {
			removeFromConditions(aCondition);
		}

		@Override
		public MatchingSet execute(RunTimeEvaluationContext evaluationContext) {
			MatchingSet returned = new MatchingSet(this, evaluationContext);
			return returned;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
		}

		@Override
		public String getStringRepresentation() {
			return "begin match " + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "?") + " from " + getContainer();
		}

	}

	@DefineValidationRule
	public static class ContainerBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<InitiateMatching> {
		public ContainerBindingIsRequiredAndMustBeValid() {
			super("'container'_binding_is_not_valid", InitiateMatching.class);
		}

		@Override
		public DataBinding<?> getBinding(InitiateMatching object) {
			return object.getContainer();
		}

	}

}
