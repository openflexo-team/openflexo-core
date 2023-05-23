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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.CustomType;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.fml.binding.ModelSlotPathElement;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoRole} is a particular implementation of a {@link FlexoProperty}<br>
 * As such, {@link FlexoRole} is a structural element of an FlexoConcept, which plays a property in this {@link FlexoConcept}<br>
 * A {@link FlexoRole} is a direct reference to a modelling element stored in an external resource accessed by a {@link ModelSlot}<br>
 * More formerly, a {@link FlexoRole} is the specification of an object accessed at run-time (inside an {@link FlexoConcept} instance)<br>
 * A {@link ModelSlot} formalizes a contract for accessing to a data
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoRole.FlexoRoleImpl.class)
@Imports({ @Import(FlexoConceptInstanceRole.class), @Import(PrimitiveRole.class), @Import(JavaRole.class) })
public interface FlexoRole<T> extends FlexoProperty<T> {

	@PropertyIdentifier(type = String.class)
	String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = Type.class)
	String TYPE_KEY = "type";
	@PropertyIdentifier(type = ModelSlot.class)
	String MODEL_SLOT_KEY = "modelSlot";
	@PropertyIdentifier(type = RoleCloningStrategy.class)
	String CLONING_STRATEGY_KEY = "cloningStrategy";
	@PropertyIdentifier(type = PropertyCardinality.class)
	String CARDINALITY_KEY = "cardinality";

	@PropertyIdentifier(type = DataBinding.class)
	String DEFAULT_VALUE_KEY = "defaultValue";
	@PropertyIdentifier(type = DataBinding.class)
	String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = boolean.class)
	String IS_REQUIRED_KEY = "isRequired";

	@Getter(value = ROLE_NAME_KEY)
	String getRoleName();

	@Setter(ROLE_NAME_KEY)
	void setRoleName(String patternRoleName) throws InvalidNameException;

	@Getter(value = MODEL_SLOT_KEY)
	@Embedded // TODO Why this property is embedded ?
	ModelSlot<?> getModelSlot();

	@Setter(MODEL_SLOT_KEY)
	void setModelSlot(ModelSlot<?> modelSlot);

	/**
	 * Return cardinality of this property
	 * 
	 * @return
	 */
	@Override
	@Getter(CARDINALITY_KEY)
	@XMLAttribute
	PropertyCardinality getCardinality();

	/**
	 * Sets cardinality of this property
	 * 
	 * @return
	 */
	@Setter(CARDINALITY_KEY)
	void setCardinality(PropertyCardinality cardinality);

	@Getter(value = DEFAULT_VALUE_KEY)
	@XMLAttribute
	DataBinding<?> getDefaultValue();

	@Setter(DEFAULT_VALUE_KEY)
	void setDefaultValue(DataBinding<?> defaultValue);

	@Getter(value = IS_REQUIRED_KEY, defaultValue = "false")
	@XMLAttribute
	boolean getIsRequired();

	@Setter(IS_REQUIRED_KEY)
	void setIsRequired(boolean isRequired);

	Object getDefaultValue(BindingEvaluationContext evaluationContext);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	@FMLAttribute(value = CONTAINER_KEY, required = false)
	DataBinding<?> getContainer();

	@Setter(CONTAINER_KEY)
	void setContainer(DataBinding<?> container);

	Object getContainer(BindingEvaluationContext evaluationContext);

	/**
	 * Return the {@link TechnologyAdapter} managing this kind of role
	 */
	TechnologyAdapter getRoleTechnologyAdapter();

	/**
	 * Return the class of {@link TechnologyAdapter} managing this kind of role
	 */
	Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass();

	/**
	 * Return cloning strategy to be applied for this property
	 * 
	 * @return
	 */
	@Getter(CLONING_STRATEGY_KEY)
	@XMLAttribute
	RoleCloningStrategy getCloningStrategy();

	/**
	 * Sets cloning strategy to be applied for this property
	 * 
	 * @return
	 */
	@Setter(CLONING_STRATEGY_KEY)
	void setCloningStrategy(RoleCloningStrategy cloningStrategy);

	/**
	 * Encodes the default cloning strategy
	 * 
	 * @return
	 */
	RoleCloningStrategy defaultCloningStrategy();

	/**
	 * Instantiate run-time-level object encoding reference to object (see {@link ActorReference})
	 * 
	 * @param object
	 *            the object which are pointing to
	 * @param fci
	 *            the {@link FlexoConceptInstance} where this {@link ActorReference} is defined
	 * @return
	 */
	ActorReference<? extends T> makeActorReference(T object, FlexoConceptInstance fci);

	/**
	 * Return a boolean indicating if this {@link FlexoRole} handles itself instantiation and management of related ActorReference
	 * 
	 * @return
	 */
	boolean supportSelfInstantiation();

	/**
	 * If this {@link FlexoRole} supports self instantiation, perform it. Otherwise return null;
	 * 
	 * @param fci
	 * @return
	 */
	public List<? extends ActorReference<? extends T>> selfInstantiate(FlexoConceptInstance fci);

	/**
	 * Build {@link CustomType} represented by supplied serialized version, asserting this type is the accessed type through this role
	 * 
	 * @param serializedType
	 * @return
	 */
	public Type buildType(String serializedType);

	/**
	 * Return the type of any instance of modelling element handled by this property.<br>
	 * Note that if the cardinality of this property is multiple, this method will return the type of each modelling element.<br>
	 * Getting type of the list of all modelling elements for a multiple cardinality property is obtained by {@link #getResultingType()}
	 * method.
	 * 
	 * @return
	 */
	@Override
	@Getter(value = TYPE_KEY, ignoreType = true)
	Type getType();

	/**
	 * Declare supplied type as the the accessed type through this role
	 * 
	 * @param type
	 */
	@Setter(TYPE_KEY)
	public void setType(Type type);

	/**
	 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(TYPE_KEY)
	public void updateType(Type type);

	abstract class FlexoRoleImpl<T> extends FlexoPropertyImpl<T> implements FlexoRole<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private ModelSlot<?> modelSlot;
		private DataBinding<?> defaultValue;
		private DataBinding<?> container;

		/*@Override
		public boolean hasParameters() {
			// TODO
			return false;
		}
		
		@Override
		public List<RolePropertyValue<?>> buildParameters() {
			return new ArrayList<>();
		}*/

		/**
		 * Return flag indicating whether this property is abstract
		 * 
		 * @return
		 */
		@Override
		public boolean isAbstract() {
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		/**
		 * Compute inferred model slot from getContainer() binding
		 * 
		 * @return
		 */
		private ModelSlot<?> getInferedModelSlot() {
			if (getContainer().isSet() && getContainer().isValid() && getContainer().isBindingPath()) {
				BindingPath bindingPath = ((BindingPath) getContainer().getExpression());
				IBindingPathElement lastPathElement = bindingPath.getLastBindingPathElement();
				// System.out.println(
				// "lastPathElement=" + lastPathElement + " of " + (lastPathElement != null ? lastPathElement.getClass() : "null"));
				if (lastPathElement instanceof ModelSlotBindingVariable) {
					return ((ModelSlotBindingVariable) lastPathElement).getModelSlot();
				}
				else if (lastPathElement instanceof FlexoPropertyPathElement
						&& ((FlexoPropertyPathElement<?>) lastPathElement).getFlexoProperty() instanceof ModelSlot) {
					return (ModelSlot<?>) ((FlexoPropertyPathElement<?>) lastPathElement).getFlexoProperty();
				}
				else if (lastPathElement instanceof ModelSlotPathElement) {
					return ((ModelSlotPathElement<?>) lastPathElement).getModelSlot();
				}

			}
			return null;
		}

		@Override
		public ModelSlot<?> getModelSlot() {
			if (modelSlot == null) {
				return getInferedModelSlot();
			}
			return modelSlot;
		}

		@Override
		public void setModelSlot(ModelSlot<?> modelSlot) {
			this.modelSlot = modelSlot;
			setChanged();
			notifyObservers(new DataModification<>("modelSlot", null, modelSlot));
		}

		@Override
		public String getRoleName() {
			return getName();
		}

		@Override
		public void setRoleName(String patternRoleName) throws InvalidNameException {
			setName(patternRoleName);
		}

		/**
		 * Return cloning strategy to be applied for this property
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy getCloningStrategy() {
			RoleCloningStrategy returned = (RoleCloningStrategy) performSuperGetter(CLONING_STRATEGY_KEY);
			if (returned == null) {
				return defaultCloningStrategy();
			}
			return returned;
		}

		@Override
		public PropertyCardinality getCardinality() {
			PropertyCardinality returned = (PropertyCardinality) performSuperGetter(CARDINALITY_KEY);
			if (returned == null) {
				return PropertyCardinality.ZeroOne;
			}
			return returned;
		}

		@Override
		public void setCardinality(PropertyCardinality cardinality) {
			if (cardinality != getCardinality()) {
				performSuperSetter(CARDINALITY_KEY, cardinality);
				notifyResultingTypeChanged();
			}
		}

		@Override
		public DataBinding<?> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<?> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(Object.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public Object getContainer(BindingEvaluationContext evaluationContext) {
			if (getContainer().isValid()) {
				try {
					return getContainer().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public DataBinding<?> getDefaultValue() {
			if (defaultValue == null) {
				defaultValue = new DataBinding<>(this, getType(), BindingDefinitionType.GET);
				defaultValue.setBindingName("defaultValue");
			}
			return defaultValue;
		}

		@Override
		public void setDefaultValue(DataBinding<?> defaultValue) {
			if (defaultValue != null) {
				defaultValue.setOwner(this);
				defaultValue.setBindingName("defaultValue");
				defaultValue.setDeclaredType(getType());
				defaultValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.defaultValue = defaultValue;
		}

		@Override
		public Object getDefaultValue(BindingEvaluationContext evaluationContext) {
			if (getDefaultValue().isValid()) {
				try {
					return getDefaultValue().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as "
					+ (getModelSlot() != null && getModelSlot().getModelSlotTechnologyAdapter() != null
							? getModelSlot().getModelSlotTechnologyAdapter().getIdentifier() : "???")
					+ "::" + getImplementedInterface().getSimpleName() + " conformTo " + getTypeDescription() + ";", context);
			return out.toString();
		}*/

		/*@Override
		public LocalizedDelegate getLocales() {
			if (getModelSlot() != null && getModelSlot().getModelSlotTechnologyAdapter() != null) {
				return getModelSlot().getModelSlotTechnologyAdapter().getLocales();
			}
			return super.getLocales();
		}*/

		/**
		 * Return the {@link TechnologyAdapter} managing this kind of role
		 */
		@Override
		public final TechnologyAdapter getRoleTechnologyAdapter() {
			if (getModelSlot() != null) {
				return getModelSlot().getModelSlotTechnologyAdapter();
			}

			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(getRoleTechnologyAdapterClass());
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getContainer()) {
				getPropertyChangeSupport().firePropertyChange(MODEL_SLOT_KEY, null, getModelSlot());
			}
		}

		/*@Override
		protected String getFMLAnnotation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("@" + getImplementedInterface().getSimpleName() + "(cardinality=" + getCardinality() + ",readOnly=" + isReadOnly()
					+ ")", context);
			if (isKey()) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				out.append("@Key", context);
			}
			return out.toString();
		}*/

		/**
		 * Return a boolean indicating if this {@link FlexoRole} handles itself instantiation and management of related ActorReference
		 * 
		 * @return
		 */
		@Override
		public boolean supportSelfInstantiation() {
			return false;
		}

		/**
		 * If this {@link FlexoRole} supports self instantiation, perform it. Otherwise return null;
		 * 
		 * @param fci
		 * @return
		 */
		@Override
		public List<? extends ActorReference<? extends T>> selfInstantiate(FlexoConceptInstance fci) {
			return null;
		}

		/**
		 * Return boolean indicating if this {@link FlexoProperty} is notification-safe (all modifications of data retrived from that
		 * property are notified using {@link PropertyChangeSupport} scheme)<br>
		 * 
		 * When tagged as unsafe, disable caching while evaluating related {@link DataBinding}.
		 * 
		 * @return
		 */
		@Override
		public boolean isNotificationSafe() {
			return true;
		}

		/**
		 * Build {@link CustomType} represented by supplied serialized version, asserting this type is the accessed type through this role
		 * 
		 * @param serializedType
		 * @return
		 */
		@Override
		public Type buildType(String serializedType) {
			if (getFMLModelFactory() != null) {
				try {
					return getFMLModelFactory().getTypeConverter().convertFromString(serializedType, getFMLModelFactory());
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/**
		 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof CustomType) {
				setType(((CustomType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setType(type);
			}
		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				compilationUnit.ensureJavaImportForType(getType());
			}
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			getDefaultValue().rebuild();
		}

	}

	public static enum RoleCloningStrategy {
		Clone, Reference, Ignore, Factory
	}

	@DefineValidationRule
	public static class FlexoRoleMustHaveAName extends ValidationRule<FlexoRoleMustHaveAName, FlexoRole<?>> {
		public FlexoRoleMustHaveAName() {
			super(FlexoRole.class, "flexo_role_must_have_a_name");
		}

		@Override
		public ValidationIssue<FlexoRoleMustHaveAName, FlexoRole<?>> applyValidation(FlexoRole<?> flexoRole) {
			if (StringUtils.isEmpty(flexoRole.getRoleName())) {
				return new ValidationError<>(this, flexoRole, "flexo_role_has_no_name");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot
			extends ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole<?>> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(FlexoRole.class, "FlexoRole_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole<?>> applyValidation(FlexoRole<?> aRole) {
			ModelSlot<?> ms = aRole.getModelSlot();
			if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
				RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(aRole);
				return new ValidationWarning<>(this, aRole, "FlexoRole_should_not_have_reflexive_model_slot_no_more", fixProposal);

			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot
				extends FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole<?>> {

			private final FlexoRole<?> role;

			public RemoveReflexiveVirtualModelModelSlot(FlexoRole<?> aRole) {
				super("remove_reflexive_modelslot");
				this.role = aRole;
			}

			@Override
			protected void fixAction() {
				role.setModelSlot(null);
			}
		}

	}

	@DefineValidationRule
	public static class DefaultValueMustBeValid extends BindingMustBeValid<FlexoRole> {
		public DefaultValueMustBeValid() {
			super("'default_value'_binding_must_be_valid", FlexoRole.class);
		}

		@Override
		public DataBinding<String> getBinding(FlexoRole object) {
			return object.getDefaultValue();
		}

	}

}
