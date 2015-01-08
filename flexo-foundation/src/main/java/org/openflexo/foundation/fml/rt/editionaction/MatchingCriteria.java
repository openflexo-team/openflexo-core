package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.binding.MatchingCriteriaBindingModel;
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
@ImplementationClass(MatchingCriteria.MatchingCriteriaImpl.class)
@XMLElement
public interface MatchingCriteria extends FlexoConceptObject, Bindable {

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
	public String _getPatternRoleName();

	@Setter(PATTERN_ROLE_NAME_KEY)
	public void _setPatternRoleName(String patternRoleName);

	@Getter(value = VALUE_KEY)
	@XMLAttribute
	public DataBinding<?> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<?> value);

	public FlexoRole<?> getFlexoRole();

	public void setFlexoRole(FlexoRole<?> patternRole);

	public Object evaluateCriteriaValue(FlexoBehaviourAction action);

	@Override
	public MatchingCriteriaBindingModel getBindingModel();

	public static abstract class MatchingCriteriaImpl extends FlexoConceptObjectImpl implements MatchingCriteria {

		private static final Logger logger = Logger.getLogger(MatchingCriteria.class.getPackage().getName());

		// private MatchFlexoConceptInstance action;

		private FlexoRole flexoRole;
		private String patternRoleName;
		private DataBinding<?> value;

		private MatchingCriteriaBindingModel bindingModel;

		// Use it only for deserialization
		public MatchingCriteriaImpl() {
			super();
		}

		public MatchingCriteriaImpl(FlexoRole flexoRole) {
			super();
			this.flexoRole = flexoRole;
		}

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
				value = new DataBinding<Object>(this, getFlexoRole() != null ? getFlexoRole().getType() : Object.class,
						DataBinding.BindingDefinitionType.GET);
				value.setBindingName(getFlexoRole() != null ? getFlexoRole().getName() : "param");
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<?> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName(getFlexoRole() != null ? getFlexoRole().getName() : "param");
				value.setDeclaredType(getFlexoRole() != null ? getFlexoRole().getType() : Object.class);
				value.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.value = value;
		}

		@Override
		public Object evaluateCriteriaValue(FlexoBehaviourAction action) {
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
		public MatchingCriteriaBindingModel getBindingModel() {
			if (bindingModel == null) {
				return bindingModel = new MatchingCriteriaBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FlexoRole getFlexoRole() {
			if (flexoRole == null && patternRoleName != null && getAction() != null && getAction().getFlexoConceptType() != null) {
				flexoRole = getAction().getFlexoConceptType().getFlexoRole(patternRoleName);
			}
			return flexoRole;
		}

		@Override
		public void setFlexoRole(FlexoRole flexoRole) {
			this.flexoRole = flexoRole;
		}

		@Override
		public String _getPatternRoleName() {
			if (flexoRole != null) {
				return flexoRole.getName();
			}
			return patternRoleName;
		}

		@Override
		public void _setPatternRoleName(String patternRoleName) {
			this.patternRoleName = patternRoleName;
		}

		@Override
		public String getURI() {
			return null;
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
