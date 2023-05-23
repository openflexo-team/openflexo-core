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
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.binding.BehaviourParameterBindingModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * An instance of a parameter used to parameterize a FML behaviour call
 * 
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(BehaviourCallArgument.BehaviourCallArgumentImpl.class)
public interface BehaviourCallArgument<T extends FlexoConceptObject> extends FlexoBehaviourObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = FlexoConceptObject.class)
	public static final String OWNER_KEY = "owner";

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

	@Getter(value = OWNER_KEY /*, inverse = AddFlexoConceptInstance.PARAMETERS_KEY*/)
	public T getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(T owner);

	public FlexoBehaviour getAccessedBehaviour();

	// TODO: PAMELA
	public FlexoBehaviourParameter getParam();

	// TODO: PAMELA
	public void setParam(FlexoBehaviourParameter param);

	public Object evaluateParameterValue(FlexoBehaviourAction<?, ?, ?> action);

	@Override
	public BehaviourParameterBindingModel getBindingModel();

	public FlexoBehaviourParameter getParameter();

	public static abstract class BehaviourCallArgumentImpl<T extends FlexoConceptObject> extends FlexoBehaviourObjectImpl
			implements BehaviourCallArgument<T> {

		static final Logger logger = Logger.getLogger(BehaviourCallArgument.class.getPackage().getName());

		// AddFlexoConceptInstance action;

		private FlexoBehaviourParameter param;
		String paramName;
		private DataBinding<Object> value;

		private BehaviourParameterBindingModel bindingModel;

		/*@Override
		public FMLCompilationUnit getResourceData() {
			if (getOwner() != null) {
			return getOwner().getCompilationUnit();
			}
		}*/

		@Override
		public FlexoBehaviourParameter getParameter() {
			return param;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() != null) {
				return getOwner().getFlexoConcept();
			}
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			/*if (param != null) {
				return param.getFlexoBehaviour();
			}*/
			return getAccessedBehaviour();
		}

		@Override
		public DataBinding<Object> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, param != null ? param.getType() : Object.class, DataBinding.BindingDefinitionType.GET);
				value.setBindingName(param != null ? param.getName() : "param");
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
			else if (getValue().revalidate()) {
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
		public BehaviourParameterBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new BehaviourParameterBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FlexoBehaviourParameter getParam() {
			if (param == null && paramName != null && getAccessedBehaviour() != null) {
				param = getAccessedBehaviour().getParameter(paramName);
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
	public static class ValueBindingMustBeValid extends BindingIsRequiredAndMustBeValid<BehaviourCallArgument> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_is_required_and_must_be_valid", BehaviourCallArgument.class);
		}

		@Override
		public DataBinding<?> getBinding(BehaviourCallArgument object) {
			return object.getValue();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<BehaviourCallArgument>, BehaviourCallArgument> applyValidation(
				BehaviourCallArgument object) {
			// Should return an issue only if parameter is required
			if (object.getParam() != null && object.getParam().getIsRequired()) {
				return super.applyValidation(object);
			}
			return null;
		}

	}

}
