package org.openflexo.foundation.viewpoint.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourObject;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.binding.AddFlexoConceptInstanceParameterBindingModel;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(AddFlexoConceptInstanceParameter.AddFlexoConceptInstanceParameterImpl.class)
@XMLElement
public interface AddFlexoConceptInstanceParameter extends FlexoBehaviourObject, Bindable {

	@PropertyIdentifier(type = AddFlexoConceptInstance.class)
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

	@Getter(value = ACTION_KEY /*, inverse = AddFlexoConceptInstance.PARAMETERS_KEY*/)
	public AddFlexoConceptInstance getAction();

	@Setter(ACTION_KEY)
	public void setAction(AddFlexoConceptInstance action);

	// TODO: PAMELA
	public FlexoBehaviourParameter getParam();

	// TODO: PAMELA
	public void setParam(FlexoBehaviourParameter param);

	public Object evaluateParameterValue(FlexoBehaviourAction action);

	@Override
	public AddFlexoConceptInstanceParameterBindingModel getBindingModel();

	public static abstract class AddFlexoConceptInstanceParameterImpl extends FlexoBehaviourObjectImpl implements
			AddFlexoConceptInstanceParameter {

		static final Logger logger = Logger.getLogger(AddFlexoConceptInstanceParameter.class.getPackage().getName());

		// AddFlexoConceptInstance action;

		private FlexoBehaviourParameter param;
		String paramName;
		private DataBinding<Object> value;

		private AddFlexoConceptInstanceParameterBindingModel bindingModel;

		// Use it only for deserialization
		public AddFlexoConceptInstanceParameterImpl() {
			super();
		}

		public AddFlexoConceptInstanceParameterImpl(FlexoBehaviourParameter param) {
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
				value = new DataBinding<Object>(this, param != null ? param.getType() : Object.class, DataBinding.BindingDefinitionType.GET);
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
		public AddFlexoConceptInstanceParameterBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new AddFlexoConceptInstanceParameterBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public VirtualModel getVirtualModel() {
			if (getAction() != null) {
				return getAction().getVirtualModel();
			}
			return null;
		}

		/*@Override
		public AddFlexoConceptInstance getAction() {
			return action;
		}

		@Override
		public void setAction(AddFlexoConceptInstance action) {
			this.action = action;
		}*/

		@Override
		public FlexoBehaviourParameter getParam() {
			if (param == null && paramName != null && getAction() != null && getAction().getCreationScheme() != null) {
				param = getAction().getCreationScheme().getParameter(paramName);
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
	public static class ValueBindingMustBeValid extends BindingIsRequiredAndMustBeValid<AddFlexoConceptInstanceParameter> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_is_required_and_must_be_valid", AddFlexoConceptInstanceParameter.class);
		}

		@Override
		public DataBinding<?> getBinding(AddFlexoConceptInstanceParameter object) {
			return object.getValue();
		}

	}

}
