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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.FMLBindingFactory;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.InferedFMLRTModelSlot;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.rm.BasicResourceImpl.LocatorNotFoundException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * An {@link VirtualModel} is the specification of a model which will be instantied as a set of federated models.
 * 
 * The base modelling element of a {@link VirtualModel} is provided by {@link FlexoConcept} concept.
 * 
 * A {@link VirtualModel} instance contains a set of {@link FlexoConceptInstance}.
 * 
 * A {@link VirtualModel} is itself an {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModel.VirtualModelImpl.class)
@Imports({ @Import(FlexoConceptStructuralFacet.class), @Import(FlexoConceptBehaviouralFacet.class), @Import(InnerConceptsFacet.class),
		@Import(DeleteFlexoConceptInstanceParameter.class) })
@XMLElement
public interface VirtualModel extends FlexoConcept, FlexoMetaModel<VirtualModel>, ResourceData<VirtualModel> {

	public static final String RESOURCE = "resource";

	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = String.class)
	public static final String URI_KEY = "uri";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = FlexoConcept.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_CONCEPTS_KEY = "flexoConcepts";
	@PropertyIdentifier(type = UseModelSlotDeclaration.class, cardinality = Cardinality.LIST)
	public static final String USE_DECLARATIONS_KEY = "useDeclarations";

	@PropertyIdentifier(type = FMLLocalizedDictionary.class)
	String LOCALIZED_DICTIONARY_KEY = "localizedDictionary";
	@PropertyIdentifier(type = VirtualModel.class, cardinality = Cardinality.LIST)
	String VIRTUAL_MODELS_KEY = "virtualModels";

	@PropertyIdentifier(type = Class.class)
	public static final String MODEL_SLOT_NATURE_CLASS_KEY = "modelSlotNatureClass";

	@PropertyIdentifier(type = FMLCompilationUnit.class)
	public static final String COMPILATION_UNIT_KEY = "compilationUnit";

	/**
	 * Return the {@link FMLCompilationUnit} where this {@link VirtualModel} is defined
	 * 
	 * @return
	 */
	@Getter(value = COMPILATION_UNIT_KEY, ignoreType = true)
	public FMLCompilationUnit getCompilationUnit();

	@Setter(COMPILATION_UNIT_KEY)
	public void setCompilationUnit(FMLCompilationUnit virtualModel);

	@Getter(value = MODEL_SLOT_NATURE_CLASS_KEY)
	@XMLAttribute
	@Deprecated
	public Class<? extends InferedFMLRTModelSlot<?, ?>> getModelSlotNatureClass();

	@Setter(MODEL_SLOT_NATURE_CLASS_KEY)
	@Deprecated
	public void setModelSlotNatureClass(Class<? extends InferedFMLRTModelSlot<?, ?>> modelSlotNatureClass);

	@Deprecated
	public List<Class<? extends InferedFMLRTModelSlot<?, ?>>> getAvailableModelSlotNatureClasses();

	@Getter("virtualModelClass")
	@XMLAttribute
	public Class<? extends VirtualModel> getVirtualModelClass();

	@Override
	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return resource for this virtual model
	 * 
	 * @return
	 */
	@Override
	@Getter(value = RESOURCE, ignoreType = true)
	// @CloningStrategy(value = StrategyType.FACTORY, factory = "cloneResource()")
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoResource<VirtualModel> getResource();

	/**
	 * Sets resource for this virtual model
	 * 
	 * @param aName
	 */
	@Override
	@Setter(value = RESOURCE)
	public void setResource(FlexoResource<VirtualModel> aVirtualModelResource);

	/**
	 * Convenient method used to retrieved {@link VirtualModelResource}
	 * 
	 * @return
	 */
	public VirtualModelResource getVirtualModelResource();

	/**
	 * Called to clone the resource of this {@link VirtualModel}
	 * 
	 * @return
	 */
	// public FlexoResource<VM> cloneResource();

	@Getter(value = VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getVersion();

	@Setter(VERSION_KEY)
	public void setVersion(FlexoVersion version);

	@Getter(value = MODEL_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getModelVersion();

	@Setter(MODEL_VERSION_KEY)
	public void setModelVersion(FlexoVersion modelVersion);

	@Getter(value = LOCALIZED_DICTIONARY_KEY, inverse = FMLLocalizedDictionary.OWNER_KEY)
	@XMLElement
	FMLLocalizedDictionary getDeprecatedLocalizedDictionary();

	@Setter(LOCALIZED_DICTIONARY_KEY)
	void setDeprecatedLocalizedDictionary(FMLLocalizedDictionary localizedDictionary);

	public LocalizedDelegate getLocalizedDictionary();

	/**
	 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link VirtualModel}<br>
	 * It includes the list of uses declarations accessible from parent and container
	 * 
	 * @return
	 */
	public List<UseModelSlotDeclaration> getAccessibleUseDeclarations();

	/**
	 * Return list of {@link UseModelSlotDeclaration} explicitely declared in this {@link VirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = USE_DECLARATIONS_KEY, cardinality = Cardinality.LIST, inverse = UseModelSlotDeclaration.VIRTUAL_MODEL_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<UseModelSlotDeclaration> getUseDeclarations();

	@Setter(USE_DECLARATIONS_KEY)
	public void setUseDeclarations(List<UseModelSlotDeclaration> useDecls);

	@Adder(USE_DECLARATIONS_KEY)
	@PastingPoint
	public void addToUseDeclarations(UseModelSlotDeclaration useDecl);

	@Remover(USE_DECLARATIONS_KEY)
	public void removeFromUseDeclarations(UseModelSlotDeclaration useDecl);

	/**
	 * Return boolean indicating if this VirtualModel uses supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass);

	/**
	 * Declare use of supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link VirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoConcept.OWNER_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoConcept> getFlexoConcepts();

	@Setter(FLEXO_CONCEPTS_KEY)
	public void setFlexoConcepts(List<FlexoConcept> flexoConcepts);

	@Adder(FLEXO_CONCEPTS_KEY)
	@PastingPoint
	public void addToFlexoConcepts(FlexoConcept aFlexoConcept);

	@Remover(FLEXO_CONCEPTS_KEY)
	public void removeFromFlexoConcepts(FlexoConcept aFlexoConcept);

	/**
	 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
	 * 
	 * @param flexoConceptNameOrURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI);

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public boolean testValidURI(String ontologyURI, String conceptURI);

	/**
	 * Return true if URI is duplicated in the context of this project
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isDuplicatedURI(String modelURI, String conceptURI);

	/**
	 * Retrieve metamodel referenced by its URI<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param modelURI
	 * @return
	 */
	public FlexoMetaModel<?> getMetaModel(String metaModelURI);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no container (contaiment semantics)<br>
	 * (where container is the virtual model itself)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllRootFlexoConcepts();

	/**
	 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no parent (inheritance semantics)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllSuperFlexoConcepts();

	public boolean hasNature(VirtualModelNature nature);

	@Override
	public VirtualModelBindingModel getBindingModel();

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link VirtualModel}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters();

	@PropertyIdentifier(type = VirtualModel.class)
	public static final String CONTAINER_VIRTUAL_MODEL_KEY = "containerVirtualModel";

	/**
	 * Return the container VirtualModel<br>
	 * This is the VirtualModel in which this VirtualModel is declared, it's might be null if this VirtualModel is at the root level
	 * 
	 * @return
	 */
	@Getter(value = CONTAINER_VIRTUAL_MODEL_KEY)
	public VirtualModel getContainerVirtualModel();

	/**
	 * Sets container VirtualModel
	 * 
	 * @param aVirtualModel
	 */
	@Setter(CONTAINER_VIRTUAL_MODEL_KEY)
	public void setContainerVirtualModel(VirtualModel aVirtualModel);

	/**
	 * Returns URI for this {@link VirtualModel}.<br>
	 * Note that if this {@link VirtualModel} is contained in another {@link VirtualModel}, URI is computed from URI of container
	 * VirtualModel
	 * 
	 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
	 * 
	 */
	@Override
	@Getter(value = URI_KEY)
	@CloningStrategy(value = StrategyType.IGNORE)
	@XMLAttribute
	public abstract String getURI();

	/**
	 * Sets URI for this {@link VirtualModel}<br>
	 * Note that if this {@link VirtualModel} is contained in another {@link VirtualModel}, this method will be unefficient
	 * 
	 * @param anURI
	 */
	@Setter(URI_KEY)
	public void setURI(String anURI);

	/**
	 * Retrieves the type of a {@link FMLRTVirtualModelInstance} conform to this {@link VirtualModel}
	 */
	VirtualModelInstanceType getVirtualModelInstanceType();

	/**
	 * Return all loaded {@link VirtualModel} defined in this {@link VirtualModel}<br>
	 * Warning: if a VirtualModel was not loaded, it wont be added to the returned list<br>
	 * See {@link #getVirtualModels(boolean)} to force the loading of unloaded virtual models
	 * 
	 * @return
	 */
	@Getter(
			value = VIRTUAL_MODELS_KEY,
			cardinality = Cardinality.LIST,
			inverse = VirtualModel.CONTAINER_VIRTUAL_MODEL_KEY,
			ignoreType = true)
	List<VirtualModel> getVirtualModels();

	/**
	 * Return all {@link VirtualModel} defined in this {@link VirtualModel}<br>
	 * When forceLoad set to true, force the loading of all virtual models
	 * 
	 * @return
	 */
	List<VirtualModel> getVirtualModels(boolean forceLoad);

	@Setter(VIRTUAL_MODELS_KEY)
	void setVirtualModels(List<VirtualModel> virtualModels);

	@Adder(VIRTUAL_MODELS_KEY)
	void addToVirtualModels(VirtualModel virtualModel);

	@Remover(VIRTUAL_MODELS_KEY)
	void removeFromVirtualModels(VirtualModel virtualModel);

	VirtualModel getVirtualModelNamed(String virtualModelNameOrURI);

	/**
	 * Load eventually unloaded contained VirtualModels<br>
	 * After this call return, we can safely assert that all contained {@link VirtualModel} are loaded.
	 */
	void loadContainedVirtualModelsWhenUnloaded();

	/**
	 * Return boolean indicating in this {@link VirtualModel} is contained in supplied {@link VirtualModel} with the recursive semantics
	 * 
	 * Also return true if this {@link VirtualModel} is deeply contained in supplied {@link VirtualModel}<br>
	 * If both virtual models are same return true
	 * 
	 * @param virtualModel
	 * @return
	 */
	public boolean isContainedIn(VirtualModel virtualModel);

	/**
	 * Default implementation for {@link VirtualModel} API
	 * 
	 * @author sylvain
	 *
	 */
	public static abstract class VirtualModelImpl extends FlexoConceptImpl implements VirtualModel {

		private static final Logger logger = Logger.getLogger(VirtualModel.class.getPackage().getName());

		private VirtualModelResource resource;
		private boolean readOnly = false;
		private VirtualModelInstanceType vmInstanceType;
		private VirtualModelInstanceType defaultVMInstanceType = new VirtualModelInstanceType(this);

		// Used during deserialization, do not use it
		public VirtualModelImpl() {
			super();
			virtualModels = new ArrayList<>();
		}

		@Override
		public void addToUseDeclarations(UseModelSlotDeclaration useDecl) {
			performSuperAdder(USE_DECLARATIONS_KEY, useDecl);
			vmInstanceType = null;
			availableModelSlotNatureClasses = null;
			getPropertyChangeSupport().firePropertyChange("availableModelSlotNatureClasses", null, getAvailableModelSlotNatureClasses());
		}

		@Override
		public void removeFromUseDeclarations(UseModelSlotDeclaration useDecl) {
			performSuperRemover(USE_DECLARATIONS_KEY, useDecl);
			vmInstanceType = null;
			availableModelSlotNatureClasses = null;
			getPropertyChangeSupport().firePropertyChange("availableModelSlotNatureClasses", null, getAvailableModelSlotNatureClasses());
		}

		@Override
		public void setModelSlotNatureClass(Class<? extends InferedFMLRTModelSlot<?, ?>> modelSlotNatureClass) {
			performSuperSetter(MODEL_SLOT_NATURE_CLASS_KEY, modelSlotNatureClass);
			vmInstanceType = null;
		}

		private List<Class<? extends InferedFMLRTModelSlot<?, ?>>> availableModelSlotNatureClasses = null;

		@SuppressWarnings("unchecked")
		@Override
		public List<Class<? extends InferedFMLRTModelSlot<?, ?>>> getAvailableModelSlotNatureClasses() {
			if (availableModelSlotNatureClasses == null) {
				availableModelSlotNatureClasses = new ArrayList<>();
				for (UseModelSlotDeclaration useMSDecl : getUseDeclarations()) {
					if (InferedFMLRTModelSlot.class.isAssignableFrom(useMSDecl.getModelSlotClass())) {
						availableModelSlotNatureClasses.add((Class<? extends InferedFMLRTModelSlot<?, ?>>) useMSDecl.getModelSlotClass());
					}
				}
			}
			return availableModelSlotNatureClasses;
		}

		@Override
		public VirtualModelInstanceType getInstanceType() {
			if (vmInstanceType == null) {
				if (getModelSlotNatureClass() != null && getServiceManager() != null) {
					TechnologyAdapterService taService = getServiceManager().getTechnologyAdapterService();
					TechnologyAdapter ta = taService.getTechnologyAdapterForModelSlot(getModelSlotNatureClass());
					if (ta != null) {
						vmInstanceType = ta.getInferedVirtualModelInstanceType(this, getModelSlotNatureClass());
					}
				}
				else {
					return defaultVMInstanceType;
				}
			}
			return vmInstanceType;
		}

		@Override
		public VirtualModelInstanceType getVirtualModelInstanceType() {
			return getInstanceType();
		}

		@Override
		public FMLModelFactory getFMLModelFactory() {
			if (getDeserializationFactory() != null /*isDeserializing()*/) {
				return getDeserializationFactory();
			}
			if (getResource() != null) {
				return getResource().getFactory();
			}
			return getDeserializationFactory();
		}

		@Override
		public void finalizeDeserialization() {
			for (FlexoConcept concept : getFlexoConcepts()) {
				concept.finalizeDeserialization();
			}
			super.finalizeDeserialization();
		}

		@Override
		public final boolean hasNature(VirtualModelNature nature) {
			return nature.hasNature(this);
		}

		/*@Override
		public FMLLocalizedDictionary getLocalizedDictionary() {
			return (FMLLocalizedDictionary) performSuperGetter(LOCALIZED_DICTIONARY_KEY);
		}*/

		/**
		 * Returns URI for this {@link VirtualModel}.<br>
		 * Note that if this {@link VirtualModel} is contained in another {@link VirtualModel}, URI is computed from URI of container
		 * VirtualModel
		 * 
		 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
		 * <br>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getContainerVirtualModel() != null) {
				return getContainerVirtualModel().getURI() + "/" + getName()
						+ (getName().endsWith(VirtualModelResourceFactory.FML_SUFFIX) ? "" : VirtualModelResourceFactory.FML_SUFFIX);
			}
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		/**
		 * Sets URI for this {@link VirtualModel}<br>
		 * Note that if this {@link VirtualModel} is contained in another {@link VirtualModel}, this method will be unefficient
		 * 
		 * @param anURI
		 */
		@Override
		public void setURI(String anURI) {
			if (getContainerVirtualModel() == null) {
				if (anURI != null) {
					// We prevent ',' so that we can use it as a delimiter in tags.
					anURI = anURI.replace(",", "");
				}
				if (getResource() != null) {
					getResource().setURI(anURI);
				}
			}
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return super.getName();
		}

		@Override
		public void setName(String name) {
			if (requireChange(getName(), name)) {
				String oldValue = getName();
				if (getResource() != null) {
					try {
						getResource().setName(name);
						getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
					} catch (CannotRenameException e) {
						e.printStackTrace();
					}
				}
				else {
					super.setName(name);
				}
			}
		}

		@Override
		public FlexoVersion getVersion() {
			if (getResource() != null) {
				return getResource().getVersion();
			}
			return null;
		}

		@Override
		public void setVersion(FlexoVersion aVersion) {
			if (requireChange(getVersion(), aVersion)) {
				if (getResource() != null) {
					getResource().setVersion(aVersion);
				}
			}
		}

		@Override
		public String toString() {
			return "VirtualModel:" + getName();
		}

		/**
		 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no container (containment semantics)<br>
		 * (where container is the virtual model itself)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllRootFlexoConcepts() {

			Vector<FlexoConcept> returned = new Vector<>();
			for (FlexoConcept ep : getFlexoConcepts()) {
				if (ep.isRoot()) {
					returned.add(ep);
				}
			}
			return returned;
		}

		/**
		 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no parent (inheritance semantics), or have
		 * parents exclusively outside this {@link VirtualModel}
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllSuperFlexoConcepts() {
			ArrayList<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept fc : getFlexoConcepts()) {
				if (fc.isSuperConceptOfContainerVirtualModel()) {
					returned.add(fc);
				}
			}
			return returned;
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void addToFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperAdder(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", null, aFlexoConcept);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, null, aFlexoConcept);
				}
			}
			getInnerConceptsFacet().notifiedConceptsChanged();
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void removeFromFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperRemover(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", aFlexoConcept, null);
			getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", aFlexoConcept, null);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, aFlexoConcept, null);
				}
			}
			InnerConceptsFacet innerConceptsFacet = getInnerConceptsFacet();
			if (innerConceptsFacet != null)
				innerConceptsFacet.notifiedConceptsChanged();
		}

		/**
		 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
		 * 
		 * @param flexoConceptNameOrURI
		 * @return
		 */
		@Override
		public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI) {
			if (StringUtils.isEmpty(flexoConceptNameOrURI)) {
				return null;
			}

			for (FlexoConcept flexoConcept : getFlexoConcepts()) {
				if (flexoConcept.getName() != null && flexoConcept.getName().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
				if (flexoConcept.getName() != null && flexoConcept.getURI().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
			}

			VirtualModel virtualModel = getVirtualModelNamed(flexoConceptNameOrURI);
			if (virtualModel != null) {
				return virtualModel;
			}

			// Implemented lazy loading for VirtualModel while searching FlexoConcept from URI

			if (flexoConceptNameOrURI.indexOf("#") > -1 && getResource() != null) {
				String virtualModelURI = flexoConceptNameOrURI.substring(0, flexoConceptNameOrURI.indexOf("#"));
				VirtualModelResource vmRes = getResource().getContentWithURI(VirtualModelResource.class, virtualModelURI);
				if (vmRes != null) {
					return vmRes.getVirtualModel().getFlexoConcept(flexoConceptNameOrURI);
				}
			}

			// Is that a concept outside of scope of current ViewPoint ?
			// NPE Protection when de-serializing
			if (getVirtualModelLibrary() == null) {
				return null;
			}
			// Delegate this to the VirtualModelLibrary
			return getVirtualModelLibrary().getFlexoConcept(flexoConceptNameOrURI);

			// logger.warning("Not found FlexoConcept:" + flexoConceptId);
			// return null;
		}

		public SynchronizationScheme createSynchronizationScheme() {
			SynchronizationScheme newSynchronizationScheme = getFMLModelFactory().newSynchronizationScheme();
			newSynchronizationScheme.setSynchronizedVirtualModel(this);
			newSynchronizationScheme.setName("synchronization");
			addToFlexoBehaviours(newSynchronizationScheme);
			return newSynchronizationScheme;
		}

		/**
		 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public boolean testValidURI(String ontologyURI, String conceptURI) {
			if (StringUtils.isEmpty(conceptURI)) {
				return false;
			}
			if (StringUtils.isEmpty(conceptURI.trim())) {
				return false;
			}
			return conceptURI.equals(JavaUtils.getJavaName(conceptURI)) && !isDuplicatedURI(ontologyURI, conceptURI);
		}

		/**
		 * Return true if URI is duplicated in the context of this project
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public boolean isDuplicatedURI(String modelURI, String conceptURI) {
			FlexoMetaModel<?> m = getMetaModel(modelURI);
			if (m != null) {
				return m.getObject(modelURI + "#" + conceptURI) != null;
			}
			return false;
		}

		/**
		 * Retrieve metamodel referenced by its URI<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param modelURI
		 * @return
		 */
		@Override
		public FlexoMetaModel<?> getMetaModel(String metaModelURI) {
			for (FlexoMetaModel<?> m : getAllReferencedMetaModels()) {
				if (m.getURI().equals(metaModelURI)) {
					return m;
				}
			}
			return null;
		}

		/**
		 * Return the list of all metamodels used in the scope of this virtual model
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoMetaModel<?>> getAllReferencedMetaModels() {
			HashSet<FlexoMetaModel<?>> returned = new HashSet<>();
			for (ModelSlot<?> modelSlot : getModelSlots()) {
				if (modelSlot instanceof TypeAwareModelSlot) {
					TypeAwareModelSlot<?, ?> tsModelSlot = (TypeAwareModelSlot<?, ?>) modelSlot;
					if (tsModelSlot.getMetaModelResource() != null) {
						returned.add(tsModelSlot.getMetaModelResource().getMetaModelData());
					}
				}
			}
			return returned;
		}

		@Override
		public boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public void setIsReadOnly(boolean b) {
			readOnly = b;
		}

		@Override
		public FlexoVersion getModelVersion() {
			if (getResource() != null) {
				return getResource().getModelVersion();
			}
			return null;
		}

		@Override
		public void setModelVersion(FlexoVersion aVersion) {
			if (getResource() != null) {
				getResource().setModelVersion(aVersion);
			}
		}

		// Implementation of XMLStorageResourceData

		@Override
		public VirtualModelResource getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<VirtualModel> resource) {
			this.resource = (VirtualModelResource) resource;
		}

		/**
		 * Convenient method used to retrieved {@link VirtualModelResource}
		 * 
		 * @return
		 */
		@Override
		public VirtualModelResource getVirtualModelResource() {
			return getResource();
		}

		@Override
		public void save() {
			logger.info("Saving ViewPoint to " + getResource().getIODelegate().toString() + "...");

			try {
				getResource().save();
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		protected String getFMLAnnotation(FMLRepresentationContext context) {
			return "@VirtualModel(uri=" + '"' + getURI() + '"' + ")";
		}

		protected String getFMLDeclaredConcepts(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getFlexoConcepts().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoConcept ep : getFlexoConcepts()) {
					out.append(ep.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}
			return out.toString();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);

			for (UseModelSlotDeclaration msDecl : getUseDeclarations()) {
				out.append("use " + msDecl.getModelSlotClass().getCanonicalName() + ";" + StringUtils.LINE_SEPARATOR, context);
			}
			out.append(StringUtils.LINE_SEPARATOR, context);

			out.append(getFMLDocHeader(context), context);

			out.append(getFMLAnnotation(context), context);
			out.append(StringUtils.LINE_SEPARATOR, context);

			out.append("public class " + getName() + getExtends(context), context);
			out.append(" {" + StringUtils.LINE_SEPARATOR, context);

			out.append(getFMLDeclaredProperties(context), context);

			out.append(getFMLDeclaredBehaviours(context), context);

			out.append(getFMLDeclaredConcepts(context), context);

			out.append("}" + StringUtils.LINE_SEPARATOR, context);

			return out.toString();
		}

		@Override
		public VirtualModel getResourceData() {
			return this;
		}

		// Developper's note: we implement here VirtualModelObject API
		// Do not consider getOwningVirtualModel()
		/*
		@Override
		public VirtualModel getVirtualModel() {
			return this;
		}
		 */

		@Override
		public Object getObject(String objectURI) {
			return getVirtualModelLibrary().getFMLObject(objectURI, true);
		}

		/**
		 * Return the list of {@link TechnologyAdapter} used in the context of this {@link VirtualModel}
		 * 
		 * @return
		 */
		@Override
		public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
			List<TechnologyAdapter> returned = new ArrayList<>();
			returned.add(getTechnologyAdapter());
			for (ModelSlot<?> ms : getModelSlots()) {
				if (!returned.contains(ms.getModelSlotTechnologyAdapter())) {
					returned.add(ms.getModelSlotTechnologyAdapter());
				}
			}
			loadContainedVirtualModelsWhenUnloaded();
			for (VirtualModel vm : getVirtualModels()) {
				for (TechnologyAdapter<?> ta : vm.getRequiredTechnologyAdapters()) {
					if (!returned.contains(ta)) {
						returned.add(ta);
					}
				}
			}
			return returned;
		}

		/**
		 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all accessible properties<br>
		 * Note that returned property is not necessary one of declared property, but might be inherited.
		 * 
		 * @param flexoPropertyName
		 * @return
		 * @see #getAccessibleProperties()
		 */
		@Override
		public FlexoProperty<?> getAccessibleProperty(String propertyName) {

			FlexoProperty<?> returned = super.getAccessibleProperty(propertyName);

			if (returned != null) {
				return returned;
			}

			for (FlexoProperty<?> p : getModelSlots()) {
				if (p.getName().equals(propertyName)) {
					return p;
				}
			}
			return null;
		}

		/**
		 * Declare use of supplied modelSlotClass
		 * 
		 * @param modelSlotClass
		 * @return
		 */
		@Override
		public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass) {
			if (modelSlotClass == null) {
				return null;
			}

			List<Class<? extends ModelSlot<?>>> usedModelSlots = new ArrayList<>();
			for (UseModelSlotDeclaration msDecl : getUseDeclarations()) {
				usedModelSlots.add(msDecl.getModelSlotClass());
				if (modelSlotClass.equals(msDecl.getModelSlotClass())) {
					return msDecl;
				}
			}

			usedModelSlots.add(modelSlotClass);
			if (getResource() != null) {
				getResource().updateFMLModelFactory(usedModelSlots);
			}

			UseModelSlotDeclaration useDeclaration = getFMLModelFactory().newUseModelSlotDeclaration(modelSlotClass);
			addToUseDeclarations(useDeclaration);
			return useDeclaration;

		}

		@Override
		protected void notifiedPropertiesChanged(FlexoProperty<?> oldValue, FlexoProperty<?> newValue) {
			super.notifiedPropertiesChanged(oldValue, newValue);
			for (FlexoConcept embedded : getFlexoConcepts()) {
				((FlexoConceptImpl) embedded).notifiedPropertiesChanged(oldValue, newValue);
			}
		}

		// DEBUT: provient de VirtualModel

		private VirtualModel containerVirtualModel;
		private VirtualModelBindingModel bindingModel;

		@Override
		public VirtualModel getContainerVirtualModel() {
			return containerVirtualModel;
		}

		@Override
		public void setContainerVirtualModel(VirtualModel containerVirtualModel) {
			if (this.containerVirtualModel != containerVirtualModel) {
				VirtualModel oldViewPoint = this.containerVirtualModel;
				this.containerVirtualModel = containerVirtualModel;
				// updateBindingModel();
				getPropertyChangeSupport().firePropertyChange(CONTAINER_VIRTUAL_MODEL_KEY, oldViewPoint, containerVirtualModel);
				notifiedScopeChanged();
			}
		}

		@Override
		public VirtualModelBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new VirtualModelBindingModel(this);
				getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, null, bindingModel);
			}
			return bindingModel;
		}

		@Override
		public boolean delete(Object... context) {

			// Unregister the resource from the virtual model library
			if (getResource() != null && getVirtualModelLibrary() != null) {
				getVirtualModelLibrary().unregisterVirtualModel(getResource());
			}

			if (bindingModel != null) {
				bindingModel.delete();
			}

			boolean returned = super.delete();

			// Delete observers
			deleteObservers();

			return returned;
		}

		@Override
		public VirtualModel getOwner() {
			// Fixed CORE-293
			// return getDeclaringVirtualModel();
			return getContainerVirtualModel();
		}

		/**
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link VirtualModel}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link VirtualModel} enclosing, we must provide this hook to give a chance
		 * to objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
		 * 
		 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...<br>
		 * 
		 */
		@Override
		public void notifiedScopeChanged() {
			super.notifiedScopeChanged();
			for (FlexoConcept concept : getFlexoConcepts()) {
				concept.notifiedScopeChanged();
			}
		}

		@Override
		public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass) {
			if (modelSlotClass == null) {
				return false;
			}
			for (UseModelSlotDeclaration useDecl : getUseDeclarations()) {
				if (modelSlotClass.equals(useDecl.getModelSlotClass())) {
					return true;
				}
			}
			/*if (getContainerVirtualModel() != null && getContainerVirtualModel().uses(modelSlotClass)) {
				return true;
			}*/
			return false;
		}

		/**
		 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link VirtualModel}<br>
		 * It includes the list of uses declarations accessible from parent and container
		 * 
		 * @return
		 */
		@Override
		public List<UseModelSlotDeclaration> getAccessibleUseDeclarations() {
			List<UseModelSlotDeclaration> returned = new ArrayList<>();
			if (getContainerVirtualModel() != null) {
				returned.addAll(getContainerVirtualModel().getAccessibleUseDeclarations());
			}
			for (UseModelSlotDeclaration useDecl : getUseDeclarations()) {
				if (!returned.contains(useDecl)) {
					returned.add(useDecl);
				}
			}
			return returned;
		}

		private FMLBindingFactory bindingFactory = null;

		@Override
		public BindingFactory getBindingFactory() {
			if (getDeclaringVirtualModel() != null && getDeclaringVirtualModel() != this) {
				return getDeclaringVirtualModel().getBindingFactory();
			}
			if (bindingFactory == null) {
				bindingFactory = new FMLBindingFactory(this);
			}
			return bindingFactory;
		}

		@Override
		public VirtualModelLibrary getVirtualModelLibrary() {
			if (getResource() != null) {
				return getResource().getVirtualModelLibrary();
			}
			return null;
		}

		private boolean isLoading = false;

		/**
		 * Load eventually unloaded VirtualModels<br>
		 * After this call return, we can safely assert that all {@link VirtualModel} are loaded.
		 */
		@Override
		public void loadContainedVirtualModelsWhenUnloaded() {
			if (isLoading) {
				return;
			}
			if (!isLoading) {
				isLoading = true;
				if (getResource() != null) {
					for (org.openflexo.foundation.resource.FlexoResource<?> r : getResource().getContents()) {
						if (r instanceof VirtualModelResource) {
							((VirtualModelResource) r).getVirtualModel();
						}
					}
				}
			}

			isLoading = false;
		}

		private List<VirtualModel> virtualModels;

		/**
		 * Return all VirtualModel of a given class.<br>
		 * If onlyFinalInstances is set to true, only instances of supplied class (and not specialized classes) are retrieved
		 * 
		 * @return
		 */
		public <VM extends VirtualModel> List<VM> getVirtualModels(Class<VM> virtualModelClass, boolean onlyFinalInstances) {
			List<VM> returned = new ArrayList<>();
			for (VirtualModel vm : getVirtualModels()) {
				if (onlyFinalInstances) {
					if (virtualModelClass.equals(vm.getClass())) {
						returned.add((VM) vm);
					}
				}
				else {
					if (virtualModelClass.isAssignableFrom(vm.getClass())) {
						returned.add((VM) vm);
					}
				}
			}
			return returned;
		}

		/**
		 * Return all loaded {@link VirtualModel} defined in this {@link VirtualModel}<br>
		 * Warning: if a VirtualModel was not loaded, it wont be added to the returned list<br>
		 * See {@link #getVirtualModels(boolean)} to force the loading of unloaded virtual models
		 * 
		 * @return
		 */
		@Override
		public List<VirtualModel> getVirtualModels() {
			return getVirtualModels(false);
		}

		/**
		 * Return all {@link VirtualModel} defined in this {@link VirtualModel}<br>
		 * When forceLoad set to true, force the loading of all virtual models
		 * 
		 * @return
		 */
		@Override
		public List<VirtualModel> getVirtualModels(boolean forceLoad) {
			if (forceLoad) {
				loadContainedVirtualModelsWhenUnloaded();
			}
			return virtualModels;
		}

		@Override
		public void setVirtualModels(List<VirtualModel> virtualModels) {
			loadContainedVirtualModelsWhenUnloaded();
			this.virtualModels = virtualModels;
		}

		@Override
		public void addToVirtualModels(VirtualModel virtualModel) {
			virtualModel.setContainerVirtualModel(this);
			virtualModels.add(virtualModel);
			getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODELS_KEY, null, virtualModel);
		}

		@Override
		public void removeFromVirtualModels(VirtualModel virtualModel) {
			virtualModel.setContainerVirtualModel(null);
			virtualModels.remove(virtualModel);
			getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODELS_KEY, virtualModel, null);
		}

		/**
		 * Return {@link VirtualModel} with supplied name or URI
		 * 
		 * @return
		 */
		@Override
		public VirtualModel getVirtualModelNamed(String virtualModelNameOrURI) {

			if (getResource() != null) {
				VirtualModel returned = getContainedVirtualModelNamed(getResource(), virtualModelNameOrURI);
				if (returned != null) {
					return returned;
				}
			}

			if (getContainerVirtualModel() != null) {
				return getContainerVirtualModel().getVirtualModelNamed(virtualModelNameOrURI);
			}

			if (getVirtualModelLibrary() != null) {
				try {
					return getVirtualModelLibrary().getVirtualModel(virtualModelNameOrURI);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
			}

			// Not found
			return null;
		}

		private VirtualModel getContainedVirtualModelNamed(VirtualModelResource resource, String virtualModelNameOrURI) {

			if (resource != null) {
				for (VirtualModelResource vmRes : resource.getContents(VirtualModelResource.class)) {
					if (vmRes.getName().equals(virtualModelNameOrURI)) {
						return vmRes.getVirtualModel();
					}
					if (vmRes.getURI().equals(virtualModelNameOrURI)) {
						return vmRes.getVirtualModel();
					}
					VirtualModel returned = getContainedVirtualModelNamed(vmRes, virtualModelNameOrURI);
					if (returned != null) {
						return returned;
					}
				}
			}

			// Not found
			return null;
		}

		@Override
		public String getStringRepresentation() {
			return getURI();
		}

		@Override
		public Collection<Validable> getEmbeddedValidableObjects() {
			Collection<Validable> returned = super.getEmbeddedValidableObjects();
			returned.addAll(getVirtualModels());
			return returned;
		}

		/**
		 * Return boolean indicating in this {@link VirtualModel} is contained in supplied {@link VirtualModel} with the recursive semantics
		 * 
		 * Also return true if this {@link VirtualModel} is deeply contained in supplied {@link VirtualModel}<br>
		 * If both virtual models are same return true
		 * 
		 * @param virtualModel
		 * @return
		 */
		@Override
		public boolean isContainedIn(VirtualModel virtualModel) {
			if (virtualModel == this) {
				return true;
			}
			if (getContainerVirtualModel() == null) {
				return false;
			}
			return getContainerVirtualModel().isContainedIn(virtualModel);
		}

		@Override
		public Class<? extends VirtualModel> getVirtualModelClass() {
			return (Class<? extends VirtualModel>) getImplementedInterface();
		}

		private LocalizedDelegateImpl localized;

		private Resource getLocalizedDirectoryResource() {
			Resource virtualModelDirectory = getResource().getIODelegate().getSerializationArtefactAsResource().getContainer();
			List<? extends Resource> localizedDirs = virtualModelDirectory.getContents(Pattern.compile(".*/Localized"), false);
			if (localizedDirs.size() > 0) {
				return localizedDirs.get(0);
			}
			if (virtualModelDirectory instanceof FileResourceImpl) {
				try {
					return new FileResourceImpl(virtualModelDirectory.getLocator(),
							new File(((FileResourceImpl) virtualModelDirectory).getFile(), "Localized"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LocatorNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.warning("Cannot find localized directory for " + this);
			return null;
		}

		private LocalizedDelegateImpl instantiateOrLoadLocales() {
			if (getResource() != null) {
				Resource localizedDirectoryResource = getLocalizedDirectoryResource();
				if (localizedDirectoryResource == null) {
					return null;
				}
				boolean editSupport = getResource().getIODelegate().getSerializationArtefactAsResource() instanceof FileResourceImpl;
				logger.info("Reading locales from " + localizedDirectoryResource);
				LocalizedDelegateImpl returned = new LocalizedDelegateImpl(localizedDirectoryResource,
						getContainerVirtualModel() != null ? getContainerVirtualModel().getLocales()
								: getServiceManager().getLocalizationService().getFlexoLocalizer(),
						editSupport, editSupport);
				returned.setLocalizationRetriever(new Runnable() {
					@Override
					public void run() {
						searchNewLocalizedEntries();
					}
				});
				return returned;

			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocalizedDictionary() {
			if (localized == null) {
				localized = instantiateOrLoadLocales();
				if (localized == null) {
					// Cannot load locales
					if (getServiceManager() != null) {
						return getServiceManager().getLocalizationService().getFlexoLocalizer();
					}
					return null;
				}
				// Converting old dictionaries
				if (getDeprecatedLocalizedDictionary() != null) {
					for (FMLLocalizedEntry fmlLocalizedEntry : getDeprecatedLocalizedDictionary().getLocalizedEntries()) {
						localized.registerNewEntry(fmlLocalizedEntry.getKey(), Language.get(fmlLocalizedEntry.getLanguage()),
								fmlLocalizedEntry.getValue());
					}
				}
			}
			return localized;
		}

		public void createLocalizedDictionaryWhenNonExistant() {
			if (localized == null) {
				logger.info("createLocalizedDictionary");
				localized = instantiateOrLoadLocales();
			}
		}

		@Override
		public FMLLocalizedDictionary getDeprecatedLocalizedDictionary() {
			if (isSerializing()) {
				return null;
			}
			return (FMLLocalizedDictionary) performSuperGetter(LOCALIZED_DICTIONARY_KEY);
		}

		private void searchNewEntriesForConcept(FlexoConcept concept) {
			// checkAndRegisterLocalized(concept.getName());
			for (FlexoBehaviour es : concept.getFlexoBehaviours()) {
				checkAndRegisterLocalized(es.getLabel(), normalizedKey -> es.setLabel(normalizedKey));
				// checkAndRegisterLocalized(es.getDescription());
				for (FlexoBehaviourParameter p : es.getParameters()) {
					checkAndRegisterLocalized(p.getName());
				}
				for (InspectorEntry entry : concept.getInspector().getEntries()) {
					checkAndRegisterLocalized(entry.getLabel(), normalizedKey -> entry.setLabel(normalizedKey));
				}
			}
		}

		private void searchNewLocalizedEntries() {
			logger.info("Search new entries for " + this);

			CompoundEdit ce = null;
			FMLModelFactory factory = null;

			if (getOwner() != null) {
				factory = getFMLModelFactory();
				if (factory != null) {
					if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
						ce = factory.getEditingContext().getUndoManager().startRecording("localize_virtual_model");
					}
				}
			}

			searchNewEntriesForConcept(this);

			for (FlexoConcept concept : getFlexoConcepts()) {
				searchNewEntriesForConcept(concept);
			}

			if (factory != null) {
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
				else if (factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					factory.getEditingContext().getUndoManager()
							.stopRecording(factory.getEditingContext().getUndoManager().getCurrentEdition());
				}
			}

			// getViewPoint().setChanged();
			// getViewPoint().notifyObservers();
		}

		private String checkAndRegisterLocalized(String key, Consumer<String> updateKey) {

			// System.out.println("checkAndRegisterLocalized for " + key);
			if (StringUtils.isEmpty(key)) {
				return null;
			}

			String normalizedKey = StringUtils.toLocalizedKey(key.trim());

			if (!key.equals(normalizedKey)) {
				updateKey.accept(normalizedKey);
			}

			getLocalizedDictionary().addEntry(normalizedKey);
			return normalizedKey;
		}

		private String checkAndRegisterLocalized(String key) {

			// System.out.println("checkAndRegisterLocalized for " + key);
			if (StringUtils.isEmpty(key)) {
				return null;
			}

			getLocalizedDictionary().addEntry(key);
			return key;
		}

	}

	// FIN: provient de VirtualModel

	@DefineValidationRule
	class VirtualModelMustHaveAName extends ValidationRule<VirtualModelMustHaveAName, VirtualModel> {
		public VirtualModelMustHaveAName() {
			super(VirtualModel.class, "virtual_model_must_have_a_name");
		}

		@Override
		public ValidationIssue<VirtualModelMustHaveAName, VirtualModel> applyValidation(VirtualModel vp) {
			if (StringUtils.isEmpty(vp.getName())) {
				return new ValidationError<>(this, vp, "virtual_model_has_no_name");
			}
			return null;
		}
	}

	@DefineValidationRule
	class VirtualModelURIMustBeValid extends ValidationRule<VirtualModelURIMustBeValid, VirtualModel> {
		public VirtualModelURIMustBeValid() {
			super(VirtualModel.class, "virtual_model_uri_must_be_valid");
		}

		@Override
		public ValidationIssue<VirtualModelURIMustBeValid, VirtualModel> applyValidation(VirtualModel vm) {
			if (StringUtils.isEmpty(vm.getURI())) {
				return new ValidationError<>(this, vm, "virtual_model_has_no_uri");
			}
			try {
				new URL(vm.getURI());
			} catch (MalformedURLException e) {
				return new ValidationError<>(this, vm, "virtual_model_uri_is_not_valid");
			}
			return null;
		}
	}

}
