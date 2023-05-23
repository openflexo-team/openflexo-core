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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.binding.MatchingCriteriaBindingModel;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(MatchingCriteria.MatchingCriteriaImpl.class)
@XMLElement
public interface MatchingCriteria extends FlexoConceptObject {

	@PropertyIdentifier(type = MatchFlexoConceptInstance.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = String.class)
	public static final String PATTERN_ROLE_NAME_KEY = "patternRoleName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@Getter(value = ACTION_KEY /*, inverse = MatchFlexoConceptInstance.MATCHING_CRITERIAS_KEY*/)
	public MatchFlexoConceptInstance getAction();

	@Setter(ACTION_KEY)
	public void setAction(MatchFlexoConceptInstance action);

	@Getter(value = PATTERN_ROLE_NAME_KEY)
	@XMLAttribute
	// TODO: name kept for compatibility, implements oldXMLTag / newXMLTag
	public String _getPatternRoleName();

	@Setter(PATTERN_ROLE_NAME_KEY)
	public void _setPatternRoleName(String patternRoleName);

	@Getter(value = VALUE_KEY)
	@XMLAttribute
	public DataBinding<?> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<?> value);

	public FlexoProperty<?> getFlexoProperty();

	public void setFlexoProperty(FlexoProperty<?> property);

	public Object evaluateCriteriaValue(RunTimeEvaluationContext evaluationContext);

	@Override
	public MatchingCriteriaBindingModel getBindingModel();

	public static abstract class MatchingCriteriaImpl extends FlexoConceptObjectImpl implements MatchingCriteria {

		private static final Logger logger = Logger.getLogger(MatchingCriteria.class.getPackage().getName());

		// private MatchFlexoConceptInstance action;

		private FlexoProperty<?> flexoProperty;
		private String propertyName;
		private DataBinding<?> value;

		private MatchingCriteriaBindingModel bindingModel;

		// Use it only for deserialization
		public MatchingCriteriaImpl() {
			super();
		}

		/*public MatchingCriteriaImpl(FlexoProperty<?> flexoProperty) {
			super();
			this.flexoProperty = flexoProperty;
		}*/

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getAction() != null) {
				return getAction().getFlexoConcept();
			}
			return null;
		}

		@Override
		public DataBinding<?> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, getFlexoProperty() != null ? getFlexoProperty().getResultingType() : Object.class,
						DataBinding.BindingDefinitionType.GET);
				value.setBindingName(getFlexoProperty() != null ? getFlexoProperty().getName() : "param");
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<?> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName(getFlexoProperty() != null ? getFlexoProperty().getName() : "param");
				value.setDeclaredType(getFlexoProperty() != null ? getFlexoProperty().getResultingType() : Object.class);
				value.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.value = value;
		}

		@Override
		public Object evaluateCriteriaValue(RunTimeEvaluationContext evaluationContext) {
			if (getValue() == null || getValue().isUnset()) {
				/*logger.info("Binding for " + param.getName() + " is not set");
				if (param instanceof URIParameter) {
					logger.info("C'est une URI, de base " + ((URIParameter) param).getBaseURI());
					logger.info("Je retourne " + ((URIParameter) param).getBaseURI().getBinding().getBindingValue(action));
					return ((URIParameter) param).getBaseURI().getBinding().getBindingValue(action);
				} else if (param.getDefaultValue() != null && param.getDefaultValue().isSet() && param.getDefaultValue().isValid()) {
					return param.getDefaultValue().getBinding().getBindingValue(action);
				}
				if (param.getIsRequired()) {
					logger.warning("Required parameter missing: " + param + ", some strange behaviour may happen from now...");
				}*/
				return null;
			}
			else if (getValue().isValid()) {
				try {
					return getValue().getBindingValue(evaluationContext);
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
			else {
				logger.warning("Invalid binding: " + getValue() + " Reason: " + getValue().invalidBindingReason());
			}
			return null;
		}

		@Override
		public MatchingCriteriaBindingModel getBindingModel() {
			if (bindingModel == null) {
				return bindingModel = new MatchingCriteriaBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FlexoProperty<?> getFlexoProperty() {
			if (flexoProperty == null && propertyName != null && getAction() != null && getAction().getFlexoConceptType() != null) {
				flexoProperty = getAction().getFlexoConceptType().getAccessibleProperty(propertyName);
			}
			return flexoProperty;
		}

		@Override
		public void setFlexoProperty(FlexoProperty<?> flexoProperty) {
			this.flexoProperty = flexoProperty;
		}

		@Override
		public String _getPatternRoleName() {
			if (flexoProperty != null) {
				return flexoProperty.getName();
			}
			return propertyName;
		}

		@Override
		public void _setPatternRoleName(String patternRoleName) {
			this.propertyName = patternRoleName;
		}

		@Override
		public String toString() {
			return "MatchingCriteria[" + _getPatternRoleName() + "=" + getValue() + "]";
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getValue().rebuild();
		}
	}

	@DefineValidationRule
	public static class ValueBindingMustBeValid extends BindingMustBeValid<MatchingCriteria> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_must_be_valid", MatchingCriteria.class);
		}

		@Override
		public DataBinding<?> getBinding(MatchingCriteria object) {
			return object.getValue();
		}

	}

}
