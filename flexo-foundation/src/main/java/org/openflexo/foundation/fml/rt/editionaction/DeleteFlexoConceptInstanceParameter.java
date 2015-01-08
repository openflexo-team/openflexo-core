package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.binding.DeleteFlexoConceptInstanceParameterBindingModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstanceParameter.DeleteFlexoConceptInstanceParameterImpl.class)
@XMLElement
public interface DeleteFlexoConceptInstanceParameter extends FlexoBehaviourObject, Bindable {

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

	public Object evaluateParameterValue(FlexoBehaviourAction action);

	@Override
	public DeleteFlexoConceptInstanceParameterBindingModel getBindingModel();

	public static abstract class DeleteFlexoConceptInstanceParameterImpl extends FlexoBehaviourObjectImpl implements
			DeleteFlexoConceptInstanceParameter {

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
				value = new DataBinding<Object>(this, param.getType(), DataBinding.BindingDefinitionType.GET);
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
		public Object evaluateParameterValue(FlexoBehaviourAction action) {
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
			} else if (getValue().isValid()) {
				try {
					return getValue().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				return null;
			} else {
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
		public String getURI() {
			return null;
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