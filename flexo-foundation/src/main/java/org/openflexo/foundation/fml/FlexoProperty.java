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

import java.beans.PropertyChangeSupport;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoProperty} is a structural element of an FlexoConcept, which contractualize the access to a typed data<br>
 * More formerly, a {@link FlexoProperty} is the specification of a data accessed at run-time (inside an {@link FlexoConcept} instance)<br>
 * A {@link FlexoProperty} formalizes a contract for accessing to a data.<br>
 * A {@link FlexoProperty} is abstract and might be implemented by a sub-class of {@link FlexoProperty} (a {@link FlexoRole} is for instance
 * a direct reference to a modelling element stored in an external resource accessed by a {@link ModelSlot})
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoProperty.FlexoPropertyImpl.class)
@Imports({ @Import(AbstractProperty.class), @Import(FlexoRole.class), @Import(ExpressionProperty.class), @Import(GetProperty.class),
		@Import(GetSetProperty.class) })
public abstract interface FlexoProperty<T> extends FlexoConceptObject {

	public static final String RESULTING_TYPE_PROPERTY = "resultingType";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String PROPERTY_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY /*, inverse = FlexoConcept.FLEXO_PROPERTIES_KEY*/)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = PROPERTY_NAME_KEY)
	public String getPropertyName();

	@Setter(PROPERTY_NAME_KEY)
	public void setPropertyName(String propertyName);

	/**
	 * Return cardinality of this property
	 * 
	 * @return
	 */
	public PropertyCardinality getCardinality();

	/**
	 * Return the type of any instance of data handled by this property.<br>
	 * Note that if the cardinality of this property is multiple, this method will return the type of each item of data.<br>
	 * Getting type of the list of all modelling elements for a multiple cardinality property is obtained by {@link #getResultingType()}
	 * method.
	 * 
	 * @return
	 */
	public Type getType();

	/**
	 * Return the resulting type of this {@link FlexoProperty} access. This type is the same as the one obtained by {@link #getType()}
	 * method if the cardinality of this property is single, or a {@link List} of {@link #getType()} method if the cardinality of this
	 * property is multiple.
	 * 
	 * @return
	 */
	public Type getResultingType();

	public String getTypeDescription();

	/**
	 * Encodes the default deletion strategy
	 * 
	 * @return
	 */
	public abstract boolean defaultBehaviourIsToBeDeleted();

	/**
	 * Return the super properties of this property<br>
	 * A super property is a {@link FlexoProperty} declared in any ancestor {@link FlexoConcept}, which is overriden by this property
	 * 
	 * @return
	 */
	public List<? extends FlexoProperty<?>> getSuperProperties();

	/**
	 * Return the full hiearchy of properties of this property<br>
	 * A super property is a {@link FlexoProperty} declared in any ancestor {@link FlexoConcept}, which is overriden by this property
	 * 
	 * @return
	 */
	public List<? extends FlexoProperty<?>> getAllSuperProperties();

	/**
	 * Return flag indicating whether data accessed though this property is read-only
	 * 
	 * @return
	 */
	public boolean isReadOnly();

	/**
	 * Return flag indicating whether this property is abstract
	 * 
	 * @return
	 */
	public boolean isAbstract();

	public boolean isSuperPropertyOf(FlexoProperty<?> property);

	/**
	 * Return boolean indicating if this {@link FlexoProperty} is notification-safe (all modifications of data retrived from that property
	 * are notified using {@link PropertyChangeSupport} scheme)<br>
	 * 
	 * When tagged as unsafe, disable caching while evaluating related {@link DataBinding}.
	 * 
	 * @return
	 */
	public boolean isNotificationSafe();

	/**
	 * Return boolean indicating if this {@link FlexoProperty} is a key property (declared in key properties of its declaring FlexoConcept)
	 * 
	 * @return
	 */
	public boolean isKeyProperty();

	public static abstract class FlexoPropertyImpl<T> extends FlexoConceptObjectImpl implements FlexoProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private PropertyChangeSupport pcSupport;

		private ModelSlot<?> modelSlot;

		private Type resultingType;

		/*		@Override
				public PropertyChangeSupport getPropertyChangeSupport() {
					if (pcSupport == null) {
						pcSupport = new PropertyChangeSupport(this) {
							@Override
							public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		
								if (listener instanceof FlexoPropertyBindingVariable) {
									System.out.println("prout");
		
									PropertyChangeListener[] l = getPropertyChangeListeners();
									for (int i = 0; i < l.length; i++) {
										if (l[i] instanceof FlexoPropertyBindingVariable
												&& ((FlexoPropertyBindingVariable) l[i]).getFlexoProperty() == ((FlexoPropertyBindingVariable) listener)
														.getFlexoProperty()) {
											System.out.println("Merde 2 fois le meme objet pour " + listener);
											System.exit(-1);
										}
									}
		
								}
		
								super.addPropertyChangeListener(listener);
							}
		
							@Override
							public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
								PropertyChangeListener[] l = getPropertyChangeListeners(propertyName);
								for (int i = 0; i < l.length; i++) {
									if (l[i] == listener) {
										System.out.println("Merde 2 fois le meme objet2");
										System.exit(-1);
									}
								}
								super.addPropertyChangeListener(propertyName, listener);
							}
						};
					}
					return pcSupport;
				}*/

		@Override
		public String getPropertyName() {
			return getName();
		}

		@Override
		public void setPropertyName(String patternRoleName) {
			setName(patternRoleName);
		}

		@Override
		public String getURI() {
			// Prevent NPE in case of null FlexoConcept (that should not happen, but....)
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getURI() + "." + getPropertyName();
			}
			else {
				return null;
			}
		}

		@Override
		public PropertyCardinality getCardinality() {
			return PropertyCardinality.ZeroOne;
		}

		protected void notifyResultingTypeChanged() {
			resultingType = null;
			getPropertyChangeSupport().firePropertyChange(RESULTING_TYPE_PROPERTY, null, getResultingType());
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ":" + getPropertyName() + "[container="
					+ (getFlexoConcept() != null
							? getFlexoConcept().getName() + "/"
									+ (getFlexoConcept().getOwningVirtualModel() != null
											? getFlexoConcept().getOwningVirtualModel().getName() : "null")
							: "null")
					+ "][" + Integer.toHexString(hashCode()) + "]";
		}

		/**
		 * Return the type of any instance of modelling element handled by this property.<br>
		 * Note that if the cardinality of this property is multiple, this method will return the type of each modelling element.<br>
		 * Getting type of the list of all modelling elements for a multiple cardinality property is obtained by {@link #getResultingType()}
		 * method.
		 * 
		 * @return
		 */
		@Override
		public abstract Type getType();

		/**
		 * Return the resulting type of this {@link FlexoProperty} access. This type is the same as the one obtained by {@link #getType()}
		 * method if the cardinality of this property is single, or a {@link List} of {@link #getType()} method if the cardinality of this
		 * property is multiple.
		 * 
		 * @return
		 */
		@Override
		public final Type getResultingType() {
			if (resultingType != null) {
				// Check type
				if (getCardinality().isMultipleCardinality()) {
					if (resultingType instanceof ParameterizedType
							&& ((ParameterizedType) resultingType).getActualTypeArguments().length == 1
							&& ((ParameterizedType) resultingType).getActualTypeArguments()[0] != null
							&& ((ParameterizedType) resultingType).getActualTypeArguments()[0].equals(getType())) {
						// OK type is valid
						return resultingType;
					}
				}
				else {
					if (resultingType.equals(getType())) {
						return resultingType;
					}
				}
			}

			// Otherwise, compute the resulting type again
			resultingType = makeResultingType();

			return resultingType;
		}

		protected Type makeResultingType() {
			if (getCardinality().isMultipleCardinality()) {
				return new ParameterizedTypeImpl(List.class, getType());
			}
			else {
				return getType();
			}
		}

		@Override
		public String getTypeDescription() {
			return TypeUtils.simpleRepresentation(getType());
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
		}

		@Override
		public final BindingModel getBindingModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getBindingModel();
			}
			return null;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			BindingModel oldBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			performSuperSetter(FLEXO_CONCEPT_KEY, flexoConcept);
			BindingModel newBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, oldBM, newBM);
		}

		@Override
		public abstract boolean defaultBehaviourIsToBeDeleted();

		/**
		 * Return direct super properties of this property
		 */
		// TODO: perfs issues, implement cache
		@Override
		public List<FlexoProperty<?>> getSuperProperties() {
			if (getFlexoConcept() == null) {
				return Collections.emptyList();
			}
			if (getFlexoConcept().getParentFlexoConcepts() == null || getFlexoConcept().getParentFlexoConcepts().size() == 0) {
				return Collections.emptyList();
			}
			List<FlexoProperty<?>> returned = new ArrayList<>();
			for (FlexoConcept parentConcept : getFlexoConcept().getParentFlexoConcepts()) {
				FlexoProperty<?> p = parentConcept.getAccessibleProperty(getPropertyName());
				if (p != null) {
					if (!returned.contains(p)) {
						returned.add(p);
					}
				}
			}
			return returned;
		}

		/**
		 * Return the full hiearchy of properties of this property<br>
		 * A super property is a {@link FlexoProperty} declared in any ancestor {@link FlexoConcept}, which is overriden by this property
		 * 
		 * @return
		 */
		@Override
		public List<FlexoProperty<?>> getAllSuperProperties() {
			List<FlexoProperty<?>> returned = new ArrayList<>();
			appendAllSuperProperties(returned);
			return returned;
		}

		private void appendAllSuperProperties(List<FlexoProperty<?>> list) {
			for (FlexoProperty<?> p : getSuperProperties()) {
				list.add(p);
				((FlexoPropertyImpl<?>) p).appendAllSuperProperties(list);
			}
		}

		@Override
		public boolean isSuperPropertyOf(FlexoProperty<?> property) {
			if (property == this) {
				return true;
			}
			for (FlexoProperty<?> superProperty : property.getSuperProperties()) {
				if (isSuperPropertyOf(superProperty)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean delete(Object... context) {
			if (getFlexoConcept() != null) {
				getFlexoConcept().removeFromFlexoProperties(this);
			}
			return performSuperDelete(context);
		}

		public boolean isKey() {
			return getFlexoConcept() != null && getFlexoConcept().getKeyProperties().contains(this);
		}

		protected String getFMLAnnotation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("@" + getImplementedInterface().getSimpleName(), context);
			if (isKey()) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				out.append("@Key", context);
			}
			return out.toString();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getFMLAnnotation(context), context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			if (detailedFMLSpecifications(context) == null) {
				out.append("public " + TypeUtils.simpleRepresentation(getResultingType()) + " " + getName() + ";", context);
			}
			else {
				out.append("public " + TypeUtils.simpleRepresentation(getResultingType()) + " " + getName() + " {", context);
				out.append(StringUtils.LINE_SEPARATOR, context);
				out.append(detailedFMLSpecifications(context), context, 1);
				// out.append(StringUtils.LINE_SEPARATOR, context);
				out.append("}", context);
			}
			return out.toString();
		}

		public String detailedFMLSpecifications(FMLRepresentationContext context) {
			return null;
		}

		/**
		 * Return boolean indicating if this {@link FlexoProperty} is a key property (declared in key properties of its declaring
		 * FlexoConcept)
		 * 
		 * @return
		 */
		@Override
		public boolean isKeyProperty() {
			if (getFlexoConcept() != null && getFlexoConcept().getKeyProperties().contains(this)) {
				return true;
			}
			return false;
		}

	}

	@DefineValidationRule
	public static class FlexoPropertyMustHaveAName extends ValidationRule<FlexoPropertyMustHaveAName, FlexoProperty> {
		public FlexoPropertyMustHaveAName() {
			super(FlexoProperty.class, "flexo_property_must_have_a_name");
		}

		@Override
		public ValidationIssue<FlexoPropertyMustHaveAName, FlexoProperty> applyValidation(FlexoProperty flexoRole) {
			if (StringUtils.isEmpty(flexoRole.getPropertyName())) {
				return new ValidationError<>(this, flexoRole, "flexo_property_has_no_name");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class OverridenPropertiesMustBeTypeCompatible
			extends ValidationRule<OverridenPropertiesMustBeTypeCompatible, FlexoProperty<?>> {
		public OverridenPropertiesMustBeTypeCompatible() {
			super(FlexoProperty.class, "overriden_properties_must_define_compatible_types");
		}

		@Override
		public ValidationIssue<OverridenPropertiesMustBeTypeCompatible, FlexoProperty<?>> applyValidation(FlexoProperty<?> property) {
			for (FlexoProperty<?> superProperty : property.getSuperProperties()) {
				if (!TypeUtils.isTypeAssignableFrom(superProperty.getResultingType(), property.getResultingType())) {

					/*System.out.println("FML=" + property.getDeclaringVirtualModel().getFMLRepresentation());
					
					System.out.println("overriding= " + property.getFMLRepresentation());
					System.out.println("getType=" + property.getType());
					System.out.println("getResultingType=" + property.getResultingType());
					System.out.println("getType=" + property.getType());
					System.out.println("getResultingType=" + property.getResultingType());*/

					return new IncompatibleTypes(this, property, superProperty.getResultingType(), property.getResultingType());
				}
			}
			return null;
		}

		public static class IncompatibleTypes extends ValidationError<OverridenPropertiesMustBeTypeCompatible, FlexoProperty<?>> {

			private Type expectedType;
			private Type overridingType;

			public IncompatibleTypes(OverridenPropertiesMustBeTypeCompatible rule, FlexoProperty<?> anObject, Type expectedType,
					Type overridingType) {
				super(rule, anObject, "overriding_property_($validable.propertyName)_does_not_define_compatible_type");
				this.expectedType = expectedType;
				this.overridingType = overridingType;
			}

			public Type getExpectedType() {
				return expectedType;
			}

			public Type getOverridingType() {
				return overridingType;
			}

			@Override
			public String getDetailedInformations() {
				return "expected: ($expectedType.toString) overriden as ($overridingType.toString)";
			}
		}

	}

	@DefineValidationRule
	public static class PropertyShadowingAnOtherOne extends ValidationRule<PropertyShadowingAnOtherOne, FlexoProperty<?>> {
		public PropertyShadowingAnOtherOne() {
			super(FlexoProperty.class, "controlling_property_shadowing");
		}

		@Override
		public ValidationIssue<PropertyShadowingAnOtherOne, FlexoProperty<?>> applyValidation(FlexoProperty<?> property) {
			if (property.getFlexoConcept().getContainerFlexoConcept() != null) {
				if (property.getFlexoConcept().getContainerFlexoConcept().getAccessibleProperty(property.getName()) != null) {
					return new ValidationWarning<>(this, property, "property_($validable.propertyName)_shadows_an_other_property");
				}
			}
			if (property.getFlexoConcept().getVirtualModel() != null
					&& property.getFlexoConcept().getVirtualModel() != property.getFlexoConcept()) {
				if (property.getFlexoConcept().getVirtualModel().getAccessibleProperty(property.getName()) != null) {
					return new ValidationWarning<>(this, property, "property_($validable.propertyName)_shadows_an_other_property");
				}
			}
			return null;
		}
	}

}
