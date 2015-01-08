/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml;

import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.NameChanged;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.DeserializationInitializer;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * This is the root class for all objects involved in an FMLModel.<br>
 * A {@link FMLObject} has a name, a description and can be identified by an URI
 * 
 * It represents an object which is part of a FML model.<br>
 * As such, you securely access to the {@link AbstractVirtualModel} in which this object "lives" #getR.<br>
 * 
 * A {@link FMLObject} is a {@link Bindable} as conforming to CONNIE binding scheme<br>
 * A {@link FMLObject} is a {@link InnerResourceData} (in a ViewPoint or in a VirtualModel)<br>
 * A {@link FMLObject} is a {@link TechnologyObject} (powered with {@link FMLTechnologyAdapter})
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLObject.FMLObjectImpl.class)
public interface FMLObject extends FlexoObject, Bindable, InnerResourceData/*<AbstractVirtualModel>*/,
		TechnologyObject<FMLTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";

	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	/**
	 * Return the URI of the {@link NamedFMLObject}<br>
	 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<edition_scheme_name> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyEditionScheme
	 * 
	 * @return String representing unique URI of this object
	 */
	public String getURI();

	public FlexoServiceManager getServiceManager();

	public InformationSpace getInformationSpace();

	/**
	 * Return the ViewPoint in which this {@link FMLObject} is defined<br>
	 * If container of this object is a {@link ViewPoint}, return this ViewPoint<br>
	 * Otherwise, container of this object is a {@link VirtualModel}, return ViewPoint of VirtualModel
	 * 
	 */
	public ViewPoint getViewPoint();

	public ViewPointLibrary getViewPointLibrary();

	public FMLModelFactory getFactory();

	/**
	 * Build and return a String encoding this {@link FMLObject} in FML textual language
	 * 
	 * @param context
	 * @return
	 */
	public String getFMLRepresentation(FMLRepresentationContext context);

	/**
	 * Build and return a String encoding this {@link FMLObject} in FML textual language
	 * 
	 * @return
	 */
	public String getFMLRepresentation();

	public String getStringRepresentation();

	// public void notifyBindingModelChanged();

	public ViewPointLocalizedDictionary getLocalizedDictionary();

	@DeserializationInitializer
	public void initializeDeserialization(FMLModelFactory factory);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	/**
	 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
	 * The container is the {@link ResourceData} of this object.<br>
	 * It is an instance of {@link AbstractVirtualModel} (a {@link VirtualModel} or a {@link ViewPoint})
	 * 
	 * @return
	 */
	@Override
	public AbstractVirtualModel<?> getResourceData();

	public static abstract class FMLObjectImpl extends FlexoObjectImpl implements FMLObject {

		private static final Logger logger = Logger.getLogger(FMLObject.class.getPackage().getName());

		private String name;

		/**
		 * Return the URI of the {@link NamedFMLObject}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		// TODO: change to abstract
		@Override
		public abstract String getURI();

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			if (requireChange(this.name, name)) {
				String oldName = this.name;
				this.name = name;
				setChanged();
				notifyObservers(new NameChanged(oldName, name));
				// getPropertyChangeSupport().firePropertyChange(NAME_KEY, oldName, name);
			}
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getViewPointLibrary() != null) {
				return getViewPoint().getViewPointLibrary().getServiceManager();
			}
			return null;
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		}

		@Override
		public ViewPointLibrary getViewPointLibrary() {
			if (getViewPoint() != null) {
				return getViewPoint().getViewPointLibrary();
			}
			return null;
		}

		@Override
		public InformationSpace getInformationSpace() {
			if (getViewPointLibrary() != null) {
				return getViewPointLibrary().getServiceManager().getInformationSpace();
			}
			return null;
		}

		/**
		 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
		 * The container is the {@link ResourceData} of this object.<br>
		 * It is an instance of {@link AbstractVirtualModel} (a {@link VirtualModel} or a {@link ViewPoint})
		 * 
		 * @return
		 */
		@Override
		public abstract AbstractVirtualModel<?> getResourceData();

		/**
		 * Return the ViewPoint in which this {@link FMLObject} is defined<br>
		 * If container of this object is a {@link ViewPoint}, return this ViewPoint<br>
		 * Otherwise, container of this object is a {@link VirtualModel}, return ViewPoint of VirtualModel
		 * 
		 */
		@Override
		public ViewPoint getViewPoint() {
			if (getResourceData() instanceof ViewPoint) {
				return (ViewPoint) getResourceData();
			}
			if (getResourceData() instanceof VirtualModel) {
				return ((VirtualModel) getResourceData()).getViewPoint();
			}
			logger.warning("Found FMLObject " + getClass() + " with resource data =" + getResourceData());
			return null;
		}

		@Override
		public final void setChanged() {
			super.setChanged();
			if (getResourceData() != null) {
				getResourceData().setIsModified();
			}
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (getPropertyChangeSupport() != null) {
				if (dataBinding != null && dataBinding.getBindingName() != null) {
					getPropertyChangeSupport().firePropertyChange(dataBinding.getBindingName(), null, dataBinding);
				}
			}
			if (getResourceData() != null) {
				getResourceData().setIsModified();
			}
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
			// logger.info("Binding decoded: " + dataBinding);
		}

		public void notifyChange(String propertyName, Object oldValue, Object newValue) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
			}
		}

		@Override
		public BindingFactory getBindingFactory() {
			if (getViewPoint() != null) {
				return getViewPoint().getBindingFactory();
			}
			return null;
		}

		/*@Override
		public void notifyBindingModelChanged() {
			getPropertyChangeSupport().firePropertyChange(BindingModelChanged.BINDING_MODEL_CHANGED, null, null);
		}*/

		@Override
		public ViewPointLocalizedDictionary getLocalizedDictionary() {
			return getViewPoint().getLocalizedDictionary();
		}

		// Voir du cote de GeneratorFormatter pour formatter tout ca
		@Override
		public abstract String getFMLRepresentation(FMLRepresentationContext context);

		@Override
		public final String getFMLRepresentation() {
			return getFMLRepresentation(new FMLRepresentationContext());
		}

		@Override
		public FMLModelFactory getFactory() {
			return ((ViewPointResource) getResourceData().getResource()).getFactory();
		}

		@Override
		public String getStringRepresentation() {
			return getFactory().stringRepresentation(this);
		}

		private FMLModelFactory deserializationFactory;

		@Override
		public void initializeDeserialization(FMLModelFactory factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			deserializationFactory = null;
		}

		public FMLModelFactory getDeserializationFactory() {
			return deserializationFactory;
		}
	}

	public static abstract class BindingMustBeValid<C extends FMLObject> extends ValidationRule<BindingMustBeValid<C>, C> {
		public BindingMustBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingMustBeValid<C>, C> applyValidation(C object) {
			if (getBinding(object) != null && getBinding(object).isSet()) {
				if (!getBinding(object).isValid()) {
					FMLObjectImpl.logger.info("Binding NOT valid: " + getBinding(object) + " for " + object.getStringRepresentation()
							+ ". Reason: " + getBinding(object).invalidBindingReason());
					DeleteBinding<C> deleteBinding = new DeleteBinding<C>(this);
					// return new ValidationError<BindingMustBeValid<C>, C>(this, object, BindingMustBeValid.this.getRuleName(), "Binding: "
					// + getBinding(object) + " reason: " + getBinding(object).invalidBindingReason(), deleteBinding);
					return new InvalidBindingIssue<C>(this, object, deleteBinding);
				}
			}
			return null;
		}

		public static class InvalidBindingIssue<C extends FMLObject> extends ValidationError<BindingMustBeValid<C>, C> {

			public InvalidBindingIssue(BindingMustBeValid<C> rule, C anObject, FixProposal<BindingMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_not_valid: ($binding)", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

		protected static class DeleteBinding<C extends FMLObject> extends FixProposal<BindingMustBeValid<C>, C> {

			private final BindingMustBeValid<C> rule;

			public DeleteBinding(BindingMustBeValid<C> rule) {
				super("delete_this_binding");
				this.rule = rule;
			}

			@Override
			protected void fixAction() {
				rule.getBinding(getValidable()).reset();
			}

		}
	}

	public static abstract class BindingIsRequiredAndMustBeValid<C extends FMLObject> extends
			ValidationRule<BindingIsRequiredAndMustBeValid<C>, C> {
		public BindingIsRequiredAndMustBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<C>, C> applyValidation(C object) {
			DataBinding<?> b = getBinding(object);
			if (b == null || !b.isSet()) {
				return new UndefinedRequiredBindingIssue<C>(this, object);
			} else if (!b.isValid()) {
				FMLObjectImpl.logger.info(getClass().getName() + ": Binding NOT valid: " + b + " for " + object.getStringRepresentation()
						+ ". Reason: " + b.invalidBindingReason());
				return new InvalidRequiredBindingIssue<C>(this, object);
				// return new ValidationError<BindingIsRequiredAndMustBeValid<C>, C>(this, object,
				// BindingIsRequiredAndMustBeValid.this.getRuleName(), "Binding: " + getBinding(object) + " reason: "
				// + getBinding(object).invalidBindingReason());
			}
			return null;
		}

		public static class UndefinedRequiredBindingIssue<C extends FMLObject> extends
				ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

			public UndefinedRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
					FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_was_not_set", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

		public static class InvalidRequiredBindingIssue<C extends FMLObject> extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

			public InvalidRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
					FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_set_value_is_invalid: ($binding)", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

	}

}
