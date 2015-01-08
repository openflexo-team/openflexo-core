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
package org.openflexo.foundation.fml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(IndividualParameter.IndividualParameterImpl.class)
@XMLElement
public interface IndividualParameter extends InnerModelSlotParameter<TypeAwareModelSlot<?, ?>> {

	@PropertyIdentifier(type = String.class)
	public static final String CONCEPT_URI_KEY = "conceptURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONCEPT_VALUE_KEY = "conceptValue";
	@PropertyIdentifier(type = String.class)
	public static final String RENDERER_KEY = "renderer";

	@Getter(value = CONCEPT_URI_KEY)
	@XMLAttribute
	public String _getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void _setConceptURI(String conceptURI);

	@Getter(value = CONCEPT_VALUE_KEY)
	@XMLAttribute
	public DataBinding<IFlexoOntologyClass<?>> getConceptValue();

	@Setter(CONCEPT_VALUE_KEY)
	public void setConceptValue(DataBinding<IFlexoOntologyClass<?>> conceptValue);

	@Getter(value = RENDERER_KEY)
	@XMLAttribute
	public String getRenderer();

	@Setter(RENDERER_KEY)
	public void setRenderer(String renderer);

	public boolean getIsDynamicConceptValue();

	public void setIsDynamicConceptValue(boolean isDynamic);

	public IFlexoOntologyClass<?> getConcept();

	public void setConcept(IFlexoOntologyClass<?> c);

	public TypeAwareModelSlot<?, ?> getTypeAwareModelSlot();

	public static abstract class IndividualParameterImpl extends InnerModelSlotParameterImpl<TypeAwareModelSlot<?, ?>> implements
			IndividualParameter {

		private String conceptURI;
		private DataBinding<IFlexoOntologyClass<?>> conceptValue;
		private String renderer;
		private boolean isDynamicConceptValueSet = false;

		public IndividualParameterImpl() {
			super();
		}

		@Override
		public Type getType() {
			if (getConcept() != null) {
				return IndividualOfClass.getIndividualOfClass(getConcept());
			}
			return IFlexoOntologyIndividual.class;
		};

		@Override
		public WidgetType getWidget() {
			return WidgetType.INDIVIDUAL;
		}

		@Override
		public String _getConceptURI() {
			return conceptURI;
		}

		@Override
		public void _setConceptURI(String conceptURI) {
			this.conceptURI = conceptURI;
		}

		@Override
		public IFlexoOntologyClass<?> getConcept() {
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getOntologyClass(_getConceptURI());
			}
			return null;
		}

		@Override
		public void setConcept(IFlexoOntologyClass<?> c) {
			_setConceptURI(c != null ? c.getURI() : null);
		}

		@Override
		public DataBinding<IFlexoOntologyClass<?>> getConceptValue() {
			if (conceptValue == null) {
				conceptValue = new DataBinding<IFlexoOntologyClass<?>>(this, IFlexoOntologyClass.class, BindingDefinitionType.GET);
				conceptValue.setBindingName("conceptValue");
			}
			return conceptValue;
		}

		@Override
		public void setConceptValue(DataBinding<IFlexoOntologyClass<?>> conceptValue) {
			if (conceptValue != null) {
				conceptValue.setOwner(this);
				conceptValue.setBindingName("conceptValue");
				conceptValue.setDeclaredType(IFlexoOntologyClass.class);
				conceptValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.conceptValue = conceptValue;
		}

		@Override
		public boolean getIsDynamicConceptValue() {
			return getConceptValue().isSet() || isDynamicConceptValueSet;
		}

		@Override
		public void setIsDynamicConceptValue(boolean isDynamic) {
			if (isDynamic) {
				isDynamicConceptValueSet = true;
			} else {
				conceptValue = null;
				isDynamicConceptValueSet = false;
			}
		}

		public IFlexoOntologyClass<?> evaluateConceptValue(BindingEvaluationContext parameterRetriever) {
			if (getConceptValue().isValid()) {
				try {
					return getConceptValue().getBindingValue(parameterRetriever);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/**
		 * Return renderer for this individual, under the form eg individual.name
		 * 
		 * @return
		 */
		@Override
		public String getRenderer() {
			return renderer;
		}

		/**
		 * Sets renderer for this individual, under the form eg individual.name
		 * 
		 * @param renderer
		 */
		@Override
		public void setRenderer(String renderer) {
			this.renderer = renderer;
		}

		@Override
		public TypeAwareModelSlot<?, ?> getModelSlot() {
			if (super.getModelSlot() instanceof TypeAwareModelSlot) {
				TypeAwareModelSlot<?, ?> returned = super.getModelSlot();
				if (returned == null) {
					if (getOwningVirtualModel() != null && getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
						return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
					}
				}
				return returned;
			}
			return null;
		}

		@Override
		public TypeAwareModelSlot<?, ?> getTypeAwareModelSlot() {
			return getModelSlot();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List<TypeAwareModelSlot> getAccessibleModelSlots() {
			return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class);
		}
	}
}
