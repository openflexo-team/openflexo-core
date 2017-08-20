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

import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.NameChanged;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.DeserializationInitializer;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * This is the root class for all objects involved in an {@link VirtualModel} (a FML "program").<br>
 * A {@link FMLObject} has a name, a description and can be identified by an URI
 * 
 * It represents an object which is part of a FML model.<br>
 * As such, you securely access to the {@link VirtualModel} in which this object "lives" using {@link #getResourceData()}<br>
 * 
 * A {@link FMLObject} is a {@link Bindable} as conforming to CONNIE binding scheme<br>
 * A {@link FMLObject} is a {@link InnerResourceData} (in a VirtualModel)<br>
 * A {@link FMLObject} is a {@link TechnologyObject} (powered with {@link FMLTechnologyAdapter})
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLObject.FMLObjectImpl.class)
@XMLElement(idFactory = "userIdentifier+'-'+flexoID")
public interface FMLObject extends FlexoObject, Bindable, InnerResourceData/*<VirtualModel>*/, TechnologyObject<FMLTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = String.class)
	String AUTHOR_KEY = "author";

	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(value = AUTHOR_KEY)
	@XMLAttribute
	public String getAuthor();

	@Setter(AUTHOR_KEY)
	public void setAuthor(String author);

	@Getter(value = DESCRIPTION_KEY)
	@XMLAttribute
	public String getDescription();

	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	public boolean hasDescription();

	/**
	 * Return the URI of the {@link NamedFMLObject}<br>
	 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<behaviour_name> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyBehaviour
	 * 
	 * @return String representing unique URI of this object
	 */
	public String getURI();

	@Override
	public FlexoServiceManager getServiceManager();

	/**
	 * Return the {@link VirtualModel} in which this {@link FMLObject} is declared<br>
	 * 
	 */
	public VirtualModel getDeclaringVirtualModel();

	/**
	 * Return the {@link VirtualModelResource} in which this {@link FMLObject} is declared<br>
	 * 
	 */
	public VirtualModelResource getDeclaringVirtualModelResource();

	public VirtualModelLibrary getVirtualModelLibrary();

	public FMLModelFactory getFMLModelFactory();

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

	/**
	 * Return a string representation suitable for a common user<br>
	 * This representation will used in all GUIs
	 */
	public String getStringRepresentation();

	// public void notifyBindingModelChanged();

	public FMLLocalizedDictionary getLocalizedDictionary();

	@DeserializationInitializer
	public void initializeDeserialization(FMLModelFactory factory);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	/**
	 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
	 * The container is the {@link ResourceData} of this object.<br>
	 * It is an instance of {@link VirtualModel} (a {@link VirtualModel} or a {@link ViewPoint})
	 * 
	 * @return
	 */
	@Override
	public VirtualModel getResourceData();

	/**
	 * Hook called when scope of a FMLObject changed.<br>
	 * 
	 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link ViewPoint}<br>
	 * On that example {@link #getBindingFactory()} rely on {@link ViewPoint} enclosing, we must provide this hook to give a chance to
	 * objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
	 * 
	 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...
	 * 
	 */
	public void notifiedScopeChanged();

	public TechnologyAdapterService getTechnologyAdapterService();

	public static abstract class FMLObjectImpl extends FlexoObjectImpl implements FMLObject {

		private static final Logger logger = Logger.getLogger(FMLObject.class.getPackage().getName());

		private String name;

		/**
		 * Return the URI of the {@link NamedFMLObject}<br>
		 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
		 * 
		 * @return String representing unique URI of this object
		 */
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
		public boolean hasDescription() {
			return StringUtils.isNotEmpty(getDescription());
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getVirtualModelLibrary() != null) {
				return getDeclaringVirtualModel().getVirtualModelLibrary().getServiceManager();
			}
			return null;
		}

		@Override
		public TechnologyAdapterService getTechnologyAdapterService() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService();
			}
			return null;
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			}
			return null;
		}

		@Override
		public VirtualModelLibrary getVirtualModelLibrary() {
			if (getDeclaringVirtualModel() != null) {
				return getDeclaringVirtualModel().getVirtualModelLibrary();
			}
			return null;
		}

		/**
		 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
		 * The container is the {@link ResourceData} of this object.<br>
		 * It is an instance of {@link VirtualModel} (a {@link VirtualModel} or a {@link ViewPoint})
		 * 
		 * @return
		 */
		@Override
		public abstract VirtualModel getResourceData();

		/**
		 * Return the ViewPoint in which this {@link FMLObject} is defined<br>
		 * If container of this object is a {@link ViewPoint}, return this ViewPoint<br>
		 * Otherwise, container of this object is a {@link VirtualModel}, return ViewPoint of VirtualModel
		 * 
		 */
		@Override
		public VirtualModel getDeclaringVirtualModel() {
			return getResourceData();
		}

		@Override
		public VirtualModelResource getDeclaringVirtualModelResource() {
			if (getDeclaringVirtualModel() != null) {
				return (VirtualModelResource) getDeclaringVirtualModel().getResource();
			}
			return null;
		}

		@Override
		public synchronized void setIsModified() {
			super.setIsModified();
			fmlRepresentation = null;
			getPropertyChangeSupport().firePropertyChange("fMLRepresentation", false, true);
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", false, true);
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
			setIsModified();
			/*if (getResourceData() != null) {
				getResourceData().setIsModified();
			}*/
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
			if (getDeclaringVirtualModel() != null) {
				return getDeclaringVirtualModel().getBindingFactory();
			}
			return null;
		}

		/**
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link ViewPoint}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link ViewPoint} enclosing, we must provide this hook to give a chance to
		 * objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
		 * 
		 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...<br>
		 * 
		 * Default implementation does nothing
		 */
		@Override
		public void notifiedScopeChanged() {
		}

		/*@Override
		public void notifyBindingModelChanged() {
			getPropertyChangeSupport().firePropertyChange(BindingModelChanged.BINDING_MODEL_CHANGED, null, null);
		}*/

		@Override
		public FMLLocalizedDictionary getLocalizedDictionary() {
			return getDeclaringVirtualModel().getLocalizedDictionary();
		}

		// Voir du cote de GeneratorFormatter pour formatter tout ca
		@Override
		public abstract String getFMLRepresentation(FMLRepresentationContext context);

		private String fmlRepresentation;

		@Override
		public final String getFMLRepresentation() {
			if (fmlRepresentation == null) {
				fmlRepresentation = getFMLRepresentation(new FMLRepresentationContext());
			}
			return fmlRepresentation;
		}

		@Override
		public FMLModelFactory getFMLModelFactory() {
			return ((VirtualModelResource) getResourceData().getResource()).getFactory();
		}

		@Override
		public String getStringRepresentation() {
			return getFMLModelFactory().stringRepresentation(this);
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

	public static abstract class BindingIsRequiredAndMustBeValid<C extends FMLObject>
			extends ValidationRule<BindingIsRequiredAndMustBeValid<C>, C> {
		public BindingIsRequiredAndMustBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<C>, C> applyValidation(C object) {
			DataBinding<?> b = getBinding(object);
			if (b == null || !b.isSet()) {
				return new UndefinedRequiredBindingIssue<C>(this, object);
			}
			else if (!b.isValid()) {
				// FMLObjectImpl.logger.info(getClass().getName() + ": Binding NOT valid: " + b + " for " + object.getStringRepresentation()
				// + ". Reason: " + b.invalidBindingReason());
				// Thread.dumpStack();

				InvalidRequiredBindingIssue<C> returned = new InvalidRequiredBindingIssue<C>(this, object);

				if (object instanceof FlexoConceptObject) {
					String proposal = b.toString();
					if (((FlexoConceptObject) object).getFlexoConcept() instanceof VirtualModel) {
						FMLObjectImpl.logger
								.info("Not valid for VirtualModel " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
						proposal = proposal.replace("virtualModelInstance.virtualModelDefinition", "this.virtualModel");
						proposal = proposal.replace("virtualModelInstance", "this");
					}
					else {
						FMLObjectImpl.logger.info("Not valid for Concept " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
						proposal = proposal.replace("virtualModelInstance", "container");
						proposal = proposal.replace("flexoConceptInstance", "this");
					}
					if (!proposal.equals(b.toString())) {
						FMLObjectImpl.logger.info("DataBinding validation: providing proposal " + proposal + " instead of " + b.toString());
						returned.addToFixProposals(new UseProposedBinding(b, proposal));
					}
					else {
						FMLObjectImpl.logger
								.info("DataBinding validation: cannot find any proposal " + proposal + " instead of " + b.toString());
					}
				}

				return returned;
				// return new ValidationError<BindingIsRequiredAndMustBeValid<C>, C>(this, object,
				// BindingIsRequiredAndMustBeValid.this.getRuleName(), "Binding: " + getBinding(object) + " reason: "
				// + getBinding(object).invalidBindingReason());
			}
			return null;
		}

		protected static class UseProposedBinding<C extends FMLObject> extends FixProposal<BindingIsRequiredAndMustBeValid<C>, C> {

			private DataBinding<?> binding;
			private String proposedValue;

			public UseProposedBinding(DataBinding<?> binding, String proposedValue) {
				super("sets_value_to_($proposedValue)");
				this.binding = binding;
				this.proposedValue = proposedValue;
			}

			public DataBinding<?> getBinding() {
				return binding;
			}

			public String getProposedValue() {
				return proposedValue;
			}

			@Override
			protected void fixAction() {
				binding.setUnparsedBinding(proposedValue);
				// binding.markedAsToBeReanalized();
			}
		}

		public static class UndefinedRequiredBindingIssue<C extends FMLObject>
				extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

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

		public static class InvalidRequiredBindingIssue<C extends FMLObject>
				extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

			public InvalidRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
					FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_value_is_invalid: ($binding)", fixProposals);

				/*System.out.println("InvalidRequiredBindingIssue:");
				System.out.println("object: " + anObject);
				System.out.println(anObject.getFMLRepresentation());
				System.out.println("binding=" + rule.getBinding(anObject));
				System.out.println("reason=" + rule.getBinding(anObject).invalidBindingReason());*/
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
