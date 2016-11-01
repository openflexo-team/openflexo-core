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

package org.openflexo.foundation.ontology.fml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(ObjectPropertyParameter.ObjectPropertyParameterImpl.class)
@XMLElement
public interface ObjectPropertyParameter extends PropertyParameter {

	@PropertyIdentifier(type = String.class)
	public static final String RANGE_URI_KEY = "rangeURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RANGE_VALUE_KEY = "rangeValue";

	@Getter(value = RANGE_URI_KEY)
	@XMLAttribute(xmlTag = "range")
	public String _getRangeURI();

	@Setter(RANGE_URI_KEY)
	public void _setRangeURI(String rangeURI);

	@Getter(value = RANGE_VALUE_KEY)
	@XMLAttribute
	public DataBinding<IFlexoOntologyClass> getRangeValue();

	@Setter(RANGE_VALUE_KEY)
	public void setRangeValue(DataBinding<IFlexoOntologyClass> rangeValue);

	public IFlexoOntologyClass getRange();

	public void setRange(IFlexoOntologyClass c);

	public boolean getIsDynamicRangeValue();

	public void setIsDynamicRangeValue(boolean isDynamic);

	public IFlexoOntologyClass evaluateRangeValue(BindingEvaluationContext parameterRetriever);

	public static abstract class ObjectPropertyParameterImpl extends PropertyParameterImpl implements ObjectPropertyParameter {

		private String rangeURI;
		private DataBinding<IFlexoOntologyClass> rangeValue;
		private boolean isDynamicRangeValueSet = false;

		public ObjectPropertyParameterImpl() {
			super();
		}

		@Override
		public Type getType() {
			return IFlexoOntologyObjectProperty.class;
		};

		@Override
		public WidgetType getWidget() {
			// return WidgetType.OBJECT_PROPERTY;
			return WidgetType.CUSTOM_WIDGET;
		}

		@Override
		public String _getRangeURI() {
			return rangeURI;
		}

		@Override
		public void _setRangeURI(String rangeURI) {
			this.rangeURI = rangeURI;
		}

		@Override
		public IFlexoOntologyClass getRange() {
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return FlexoOntologyVirtualModelNature.getOntologyClass(_getRangeURI(), getOwningVirtualModel());
			}
			return null;
		}

		@Override
		public void setRange(IFlexoOntologyClass c) {
			_setRangeURI(c != null ? c.getURI() : null);
		}

		@Override
		public DataBinding<IFlexoOntologyClass> getRangeValue() {
			if (rangeValue == null) {
				rangeValue = new DataBinding<IFlexoOntologyClass>(this, IFlexoOntologyClass.class, BindingDefinitionType.GET);
				rangeValue.setBindingName("rangeValue");
			}
			return rangeValue;
		}

		@Override
		public void setRangeValue(DataBinding<IFlexoOntologyClass> rangeValue) {
			if (rangeValue != null) {
				rangeValue.setOwner(this);
				rangeValue.setBindingName("rangeValue");
				rangeValue.setDeclaredType(IFlexoOntologyClass.class);
				rangeValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.rangeValue = rangeValue;
		}

		@Override
		public boolean getIsDynamicRangeValue() {
			return getRangeValue().isSet() || isDynamicRangeValueSet;
		}

		@Override
		public void setIsDynamicRangeValue(boolean isDynamic) {
			if (isDynamic) {
				isDynamicRangeValueSet = true;
			}
			else {
				rangeValue = null;
				isDynamicRangeValueSet = false;
			}
		}

		@Override
		public IFlexoOntologyClass evaluateRangeValue(BindingEvaluationContext parameterRetriever) {
			if (getRangeValue().isValid()) {
				try {
					return getRangeValue().getBindingValue(parameterRetriever);
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

	}
}
