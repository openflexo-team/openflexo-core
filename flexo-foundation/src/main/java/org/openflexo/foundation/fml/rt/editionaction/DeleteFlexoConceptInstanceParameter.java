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
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.binding.DeleteFlexoConceptInstanceParameterBindingModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

//TODO: merge and use BehaviourParameter instead
@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstanceParameter.DeleteFlexoConceptInstanceParameterImpl.class)
@XMLElement
public interface DeleteFlexoConceptInstanceParameter extends FlexoBehaviourObject {

	@PropertyIdentifier(type = DeleteFlexoConceptInstance.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = String.class)
	public static final String PARAM_NAME_KEY = "paramName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@Getter(value = PARAM_NAME_KEY)
	@XMLAttribute
	public String _getParamName();

	@Setter(PARAM_NAME_KEY)
	public void _setParamName(String paramName);

	@Getter(value = VALUE_KEY)
	@XMLAttribute
	public DataBinding<Object> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<Object> value);

	@Getter(value = ACTION_KEY /*, inverse = DeleteFlexoConceptInstance.PARAMETERS_KEY*/)
	public DeleteFlexoConceptInstance getAction();

	@Setter(ACTION_KEY)
	public void setAction(DeleteFlexoConceptInstance action);

	// TODO: PAMELA
	public FlexoBehaviourParameter getParam();

	// TODO: PAMELA
	public void setParam(FlexoBehaviourParameter param);

	public Object evaluateParameterValue(FlexoBehaviourAction<?, ?, ?> action);

	@Override
	public DeleteFlexoConceptInstanceParameterBindingModel getBindingModel();

	public static abstract class DeleteFlexoConceptInstanceParameterImpl extends FlexoBehaviourObjectImpl
			implements DeleteFlexoConceptInstanceParameter {

		static final Logger logger = Logger.getLogger(DeleteFlexoConceptInstanceParameter.class.getPackage().getName());

		// DeleteFlexoConceptInstance action;

		private FlexoBehaviourParameter param;
		String paramName;
		private DataBinding<Object> value;

		private DeleteFlexoConceptInstanceParameterBindingModel bindingModel;

		// Use it only for deserialization
		public DeleteFlexoConceptInstanceParameterImpl() {
			super();
		}

		public DeleteFlexoConceptInstanceParameterImpl(FlexoBehaviourParameter param) {
			super();
			this.param = param;
		}

		public FlexoBehaviourParameter getParameter() {
			return param;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (param != null) {
				return param.getFlexoConcept();
			}
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			if (param != null) {
				return param.getFlexoBehaviour();
			}
			return null;
		}

		@Override
		public DataBinding<Object> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, param.getType(), DataBinding.BindingDefinitionType.GET);
				value.setBindingName(param.getName());
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<Object> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName(param != null ? param.getName() : "param");
				value.setDeclaredType(param != null ? param.getType() : Object.class);
				value.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.value = value;
		}

		@Override
		public Object evaluateParameterValue(FlexoBehaviourAction<?, ?, ?> action) {
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
					return getValue().getBindingValue(action);
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
		public DeleteFlexoConceptInstanceParameterBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new DeleteFlexoConceptInstanceParameterBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FlexoBehaviourParameter getParam() {
			if (param == null && paramName != null && getAction() != null && getAction().getDeletionScheme() != null) {
				param = getAction().getDeletionScheme().getParameter(paramName);
			}
			return param;
		}

		@Override
		public void setParam(FlexoBehaviourParameter param) {
			this.param = param;
		}

		@Override
		public String _getParamName() {
			if (param != null) {
				return param.getName();
			}
			return paramName;
		}

		@Override
		public void _setParamName(String param) {
			this.paramName = param;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getValue().rebuild();
		}

	}

	@DefineValidationRule
	public static class ValueBindingMustBeValid extends BindingIsRequiredAndMustBeValid<DeleteFlexoConceptInstanceParameter> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_is_required_and_must_be_valid", DeleteFlexoConceptInstanceParameter.class);
		}

		@Override
		public DataBinding<?> getBinding(DeleteFlexoConceptInstanceParameter object) {
			return object.getValue();
		}

	}

}
