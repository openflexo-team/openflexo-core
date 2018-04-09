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
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
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
import org.openflexo.toolbox.StringUtils;

/**
 * Primitive used to match as a result set some {@link FlexoConceptInstance} in a container, matching a type and some conditions<br>
 * Result is computed ans returned as a {@link MatchingSet}
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(InitiateMatching.InitiateMatchingImpl.class)
@XMLElement
public interface InitiateMatching extends AssignableAction<MatchingSet> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String CONDITIONS_KEY = "conditions";

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String uri);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	@Getter(value = CONDITIONS_KEY, cardinality = Cardinality.LIST, inverse = MatchCondition.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<MatchCondition> getConditions();

	@Setter(CONDITIONS_KEY)
	public void setConditions(List<MatchCondition> conditions);

	@Adder(CONDITIONS_KEY)
	public void addToConditions(MatchCondition aCondition);

	@Remover(CONDITIONS_KEY)
	public void removeFromConditions(MatchCondition aCondition);

	public FlexoConceptInstanceType getMatchedType();

	public FlexoConcept getOwnerConcept();

	public MatchCondition createCondition();

	public void deleteCondition(MatchCondition aCondition);

	public List<FlexoConcept> getAvailableFlexoConceptTypes();

	public static abstract class InitiateMatchingImpl extends AssignableActionImpl<MatchingSet> implements InitiateMatching {

		private static final Logger logger = Logger.getLogger(InitiateMatching.class.getPackage().getName());

		private DataBinding<FlexoConceptInstance> container;
		private String flexoConceptTypeURI;
		private FlexoConcept flexoConceptType;

		@Getter(value = CONTAINER_KEY)
		@XMLAttribute
		@Override
		public String _getFlexoConceptTypeURI() {
			if (flexoConceptType != null) {
				return flexoConceptType.getURI();
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
			}
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
		public String getStringRepresentation() {
			return getHeaderContext() + getContainer() + ".initiateMatching("
					+ (getFlexoConceptType() != null ? getFlexoConceptType().getName() : null) + ")";
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(
					getContainer() + ".initiateMatching(" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : null) + ")",
					context);
			return out.toString();
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
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
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
			// System.out.println("Computing MatchingSet for " + getFlexoConceptType() + " on " + getContainer());
			MatchingSet returned = new MatchingSet(this, evaluationContext);
			// System.out.println("Matching set with " + returned.getAllInstances());
			return returned;
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
