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
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(ClassParameter.ClassParameterImpl.class)
@XMLElement
public interface ClassParameter extends InnerModelSlotParameter<TypeAwareModelSlot<?, ?>> {

	@PropertyIdentifier(type = String.class)
	public static final String CONCEPT_URI_KEY = "conceptURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONCEPT_VALUE_KEY = "conceptValue";

	@Getter(value = CONCEPT_URI_KEY)
	@XMLAttribute
	public String _getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void _setConceptURI(String conceptURI);

	@Getter(value = CONCEPT_VALUE_KEY)
	@XMLAttribute
	public DataBinding<IFlexoOntologyClass> getConceptValue();

	@Setter(CONCEPT_VALUE_KEY)
	public void setConceptValue(DataBinding<IFlexoOntologyClass> conceptValue);

	public IFlexoOntologyClass getConcept();

	public void setConcept(IFlexoOntologyClass c);

	public boolean getIsDynamicConceptValue();

	public void setIsDynamicConceptValue(boolean isDynamic);

	public IFlexoOntologyClass evaluateConceptValue(BindingEvaluationContext evaluationContext);

	public TypeAwareModelSlot<?, ?> getTypeAwareModelSlot();

	public static abstract class ClassParameterImpl extends InnerModelSlotParameterImpl<TypeAwareModelSlot<?, ?>> implements ClassParameter {

		private String conceptURI;
		private DataBinding<IFlexoOntologyClass> conceptValue;
		private boolean isDynamicConceptValueSet = false;

		public ClassParameterImpl() {
			super();
		}

		@Override
		public Type getType() {
			return IFlexoOntologyClass.class;
		};

		@Override
		public WidgetType getWidget() {
			return WidgetType.CLASS;
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
		public IFlexoOntologyClass getConcept() {
			return getOwningVirtualModel().getOntologyClass(_getConceptURI());
		}

		@Override
		public void setConcept(IFlexoOntologyClass c) {
			_setConceptURI(c != null ? c.getURI() : null);
		}

		@Override
		public DataBinding<IFlexoOntologyClass> getConceptValue() {
			if (conceptValue == null) {
				conceptValue = new DataBinding<IFlexoOntologyClass>(this, IFlexoOntologyClass.class, BindingDefinitionType.GET);
				conceptValue.setBindingName("conceptValue");
			}
			return conceptValue;
		}

		@Override
		public void setConceptValue(DataBinding<IFlexoOntologyClass> conceptValue) {
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

		@Override
		public IFlexoOntologyClass evaluateConceptValue(BindingEvaluationContext parameterRetriever) {
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

		@Override
		public TypeAwareModelSlot<?, ?> getModelSlot() {
			TypeAwareModelSlot<?, ?> returned = super.getModelSlot();
			if (returned == null) {
				if (getOwningVirtualModel() != null && getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
					return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
				}
			}
			return returned;
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
