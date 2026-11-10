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
import org.openflexo.foundation.fml.FMLNamedReference;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourArgument;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.binding.DeleteFlexoConceptInstanceParameterBindingModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;

//TODO: merge and use BehaviourParameter instead
@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstanceParameter.DeleteFlexoConceptInstanceParameterImpl.class)
public interface DeleteFlexoConceptInstanceParameter extends FlexoBehaviourObject {

	@PropertyIdentifier(type = DeleteFlexoConceptInstance.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = FMLNamedReference.class)
	public static final String PARAM_REFERENCE_KEY = "paramReference";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	/**
	 * The parameter this argument gives a value to, as a reference resolved in the behaviour it is given to (see {@link #getParam()})
	 *
	 * @return the reference to the parameter
	 */
	@Getter(value = PARAM_REFERENCE_KEY, ignoreType = true)
	public FMLNamedReference<FlexoBehaviourArgument> getParamReference();

	@Setter(PARAM_REFERENCE_KEY)
	public void setParamReference(FMLNamedReference<FlexoBehaviourArgument> paramReference);

	/**
	 * Keeps the name only of a reference coming from another compilation unit (merge with {@code updateWith()})
	 *
	 * @param paramReference
	 *            the reference held by the argument of the other compilation unit
	 */
	@Updater(PARAM_REFERENCE_KEY)
	public void updateParamReference(FMLNamedReference<FlexoBehaviourArgument> paramReference);

	@Getter(value = VALUE_KEY)
	public DataBinding<Object> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<Object> value);

	@Getter(value = ACTION_KEY /*, inverse = DeleteFlexoConceptInstance.PARAMETERS_KEY*/)
	public DeleteFlexoConceptInstance getAction();

	@Setter(ACTION_KEY)
	public void setAction(DeleteFlexoConceptInstance action);

	// TODO: PAMELA
	public FlexoBehaviourArgument getParam();

	// TODO: PAMELA
	public void setParam(FlexoBehaviourArgument param);

	public Object evaluateParameterValue(FlexoBehaviourAction<?, ?, ?> action);

	@Override
	public DeleteFlexoConceptInstanceParameterBindingModel getBindingModel();

	public static abstract class DeleteFlexoConceptInstanceParameterImpl extends FlexoBehaviourObjectImpl
			implements DeleteFlexoConceptInstanceParameter {

		static final Logger logger = Logger.getLogger(DeleteFlexoConceptInstanceParameter.class.getPackage().getName());

		// DeleteFlexoConceptInstance action;

		private FMLNamedReference<FlexoBehaviourArgument> paramReference;
		private DataBinding<Object> value;

		private DeleteFlexoConceptInstanceParameterBindingModel bindingModel;

		// Use it only for deserialization
		public DeleteFlexoConceptInstanceParameterImpl() {
			super();
		}

		public DeleteFlexoConceptInstanceParameterImpl(FlexoBehaviourArgument param) {
			super();
			this.paramReference = FMLNamedReference.to(param);
		}

		public FlexoBehaviourArgument getParameter() {
			return getParam();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getParam() != null) {
				return getParam().getFlexoConcept();
			}
			return null;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			if (getParam() != null) {
				return getParam().getFlexoBehaviour();
			}
			return null;
		}

		@Override
		public DataBinding<Object> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, getParam().getType(), DataBinding.BindingDefinitionType.GET);
				value.setBindingName(getParam().getName());
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<Object> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName(getParam() != null ? getParam().getName() : "param");
				value.setDeclaredType(getParam() != null ? getParam().getType() : Object.class);
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
		public FMLNamedReference<FlexoBehaviourArgument> getParamReference() {
			return paramReference;
		}

		@Override
		public void setParamReference(FMLNamedReference<FlexoBehaviourArgument> paramReference) {
			FMLNamedReference<FlexoBehaviourArgument> oldValue = this.paramReference;
			this.paramReference = paramReference;
			getPropertyChangeSupport().firePropertyChange(PARAM_REFERENCE_KEY, oldValue, paramReference);
		}

		@Override
		public void updateParamReference(FMLNamedReference<FlexoBehaviourArgument> paramReference) {
			setParamReference(paramReference != null ? paramReference.unresolved() : null);
		}

		@Override
		public FlexoBehaviourArgument getParam() {
			if (paramReference == null) {
				return null;
			}
			FlexoBehaviour behaviour = getAction() != null ? getAction().getDeletionScheme() : null;
			return paramReference.resolve(behaviour != null ? behaviour::getParameter : null);
		}

		@Override
		public void setParam(FlexoBehaviourArgument param) {
			setParamReference(FMLNamedReference.to(param));
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
