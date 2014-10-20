/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
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
package org.openflexo.foundation.viewpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.viewpoint.binding.VirtualModelBindingModel;
import org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResourceImpl;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

/**
 * A {@link VirtualModel} is the specification of a model which will be instantied in a {@link View} as a set of federated models.
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
@XMLElement
@Imports({ @Import(FlexoConceptStructuralFacet.class), @Import(FlexoConceptBehaviouralFacet.class),
		@Import(FlexoBehaviourParameters.class), @Import(DeleteFlexoConceptInstanceParameter.class),
		@Import(AddFlexoConceptInstanceParameter.class) })
public interface VirtualModel extends FlexoConcept, FlexoMetaModel<VirtualModel>, ResourceData<VirtualModel>,
		TechnologyObject<VirtualModelTechnologyAdapter> {

	// public static final String REFLEXIVE_MODEL_SLOT_NAME = "virtualModelInstance";

	public static final String RESOURCE = "resource";

	@PropertyIdentifier(type = ViewPoint.class)
	public static final String VIEW_POINT_KEY = "viewPoint";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = Vector.class)
	public static final String FLEXO_CONCEPTS_KEY = "flexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String MODEL_SLOTS_KEY = "modelSlots";

	@Override
	public VirtualModelModelFactory getVirtualModelFactory();

	/**
	 * Return resource for this virtual model
	 * 
	 * @return
	 */
	@Override
	@Getter(value = RESOURCE, ignoreType = true)
	public FlexoResource<VirtualModel> getResource();

	/**
	 * Sets resource for this virtual model
	 * 
	 * @param aName
	 */
	@Override
	@Setter(value = RESOURCE)
	public void setResource(FlexoResource<VirtualModel> aVirtualModelResource);

	@Override
	@Getter(value = VIEW_POINT_KEY /*, inverse = ViewPoint.VIRTUAL_MODELS_KEY*/)
	public ViewPoint getViewPoint();

	@Setter(VIEW_POINT_KEY)
	public void setViewPoint(ViewPoint aViewPoint);

	@Getter(value = VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getVersion();

	@Setter(VERSION_KEY)
	public void setVersion(FlexoVersion version);

	@Getter(value = MODEL_VERSION_KEY, isStringConvertable = true)
	public FlexoVersion getModelVersion();

	@Setter(MODEL_VERSION_KEY)
	public void setModelVersion(FlexoVersion modelVersion);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link VirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoConcept.VIRTUAL_MODEL_KEY)
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

	@Getter(value = MODEL_SLOTS_KEY, cardinality = Cardinality.LIST, inverse = ModelSlot.VIRTUAL_MODEL_KEY)
	@XMLElement(context = "ModelSlot_", primary = true)
	// Since ModelSlot are also FlexoRole instances, we need to distinguish both during serialization/deserialization process
	// To do it, we append ModelSlot_ as context
	public List<ModelSlot<?>> getModelSlots();

	@Setter(MODEL_SLOTS_KEY)
	public void setModelSlots(List<ModelSlot<?>> modelSlots);

	@Adder(MODEL_SLOTS_KEY)
	public void addToModelSlots(ModelSlot<?> aModelSlot);

	@Remover(MODEL_SLOTS_KEY)
	public void removeFromModelSlots(ModelSlot<?> aModelSlot);

	@Finder(collection = MODEL_SLOTS_KEY, attribute = ModelSlot.NAME_KEY)
	public ModelSlot<?> getModelSlot(String modelSlotName);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public <MS extends ModelSlot<?>> List<MS> getModelSlots(Class<MS> msType);

	/**
	 * Retrieve ontology object from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyObject getOntologyObject(String uri);

	/**
	 * Retrieve ontology class from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyClass getOntologyClass(String uri);

	/**
	 * Retrieve ontology individual from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyIndividual getOntologyIndividual(String uri);

	/**
	 * Retrieve ontology property from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyStructuralProperty getOntologyProperty(String uri);

	/**
	 * Retrieve ontology object property from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyObjectProperty getOntologyObjectProperty(String uri);

	/**
	 * Retrieve ontology object property from its URI.<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param uri
	 * @return
	 */
	public IFlexoOntologyDataProperty getOntologyDataProperty(String uri);

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
	 * Return reflexive model slot<br>
	 * The reflexive model slot is an abstraction which allow to consider the virtual model as a model which can be accessed from itself
	 * (Reentrance implementation)
	 * 
	 * @return
	 */
	// Not used anymore, but supported implicitely
	// public VirtualModelModelSlot getReflexiveModelSlot();

	/**
	 * Return flag indicating if supplied BindingVariable is set at runtime
	 * 
	 * @param variable
	 * @return
	 * @see VirtualModelInstance#getValueForVariable(BindingVariable)
	 */
	// public boolean handleVariable(BindingVariable variable);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no parent
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllRootFlexoConcepts();

	public boolean hasNature(VirtualModelNature nature);

	@Override
	public VirtualModelBindingModel getBindingModel();

	public static abstract class VirtualModelImpl extends FlexoConceptImpl implements VirtualModel {

		private static final Logger logger = Logger.getLogger(VirtualModel.class.getPackage().getName());

		private ViewPoint viewPoint;
		// private Vector<FlexoConcept> flexoConcepts;
		// private List<ModelSlot<?>> modelSlots;
		private VirtualModelBindingModel bindingModel;
		private VirtualModelResource resource;
		private ViewPointLocalizedDictionary localizedDictionary;

		private boolean readOnly = false;

		private final VirtualModelInstanceType vmInstanceType = new VirtualModelInstanceType(this);

		/**
		 * Stores a chained collections of objects which are involved in validation
		 */
		private final ChainedCollection<ViewPointObject> validableObjects = null;

		/**
		 * Creates a new VirtualModel on user request<br>
		 * Creates both the resource and the object
		 * 
		 * 
		 * @param baseName
		 * @param viewPoint
		 * @return
		 * @throws SaveResourceException
		 */
		public static VirtualModel newVirtualModel(String baseName, ViewPoint viewPoint) throws SaveResourceException {
			
			File diagramSpecificationDirectory = new File(ResourceLocator.retrieveResourceAsFile(((ViewPointResource) viewPoint.getResource()).getDirectory()), baseName);
			File diagramSpecificationXMLFile = new File(diagramSpecificationDirectory, baseName + ".xml");
			ViewPointLibrary viewPointLibrary = viewPoint.getViewPointLibrary();
			VirtualModelResource vmRes = VirtualModelResourceImpl.makeVirtualModelResource(diagramSpecificationDirectory,
					diagramSpecificationXMLFile, (ViewPointResource) viewPoint.getResource(), viewPointLibrary.getServiceManager());
			VirtualModel virtualModel = vmRes.getFactory().newVirtualModel();
			virtualModel.setViewPoint(viewPoint);
			viewPoint.addToVirtualModels(virtualModel);
			vmRes.setResourceData(virtualModel);
			virtualModel.setResource(vmRes);
			// ((VirtualModelImpl) virtualModel).makeReflexiveModelSlot();
			virtualModel.getResource().save(null);
			vmRes.setDirectory(ResourceLocator.locateResource(diagramSpecificationDirectory.getPath()));
			
			return virtualModel;
		}

		// Used during deserialization, do not use it
		public VirtualModelImpl() {
			super();
		}

		/**
		 * Creates a new VirtualModel in supplied viewpoint
		 * 
		 * @param viewPoint
		 */
		public VirtualModelImpl(ViewPoint viewPoint) {
			this();
			setViewPoint(viewPoint);
		}

		@Override
		public FlexoConceptInstanceType getInstanceType() {
			return vmInstanceType;
		}

		@Override
		public boolean delete() {
			if (bindingModel != null) {
				bindingModel.delete();
			}
			return super.delete();
		}

		@Override
		public VirtualModelModelFactory getVirtualModelFactory() {
			if (getResource() != null) {
				return getResource().getFactory();
			}
			else {
				return super.getVirtualModelFactory();
			}
		}

		@Override
		public void finalizeDeserialization() {
			finalizeFlexoConceptDeserialization();
			for (FlexoConcept ep : getFlexoConcepts()) {
				ep.finalizeFlexoConceptDeserialization();
			}
			// Ensure access to reflexive model slot
			// getReflexiveModelSlot();
		}

		@Override
		public final boolean hasNature(VirtualModelNature nature) {
			return nature.hasNature(this);
		}

		/**
		 * Return the URI of the {@link VirtualModel}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name >#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getViewPoint() != null) {
				return getViewPoint().getURI() + "/" + getName();
			}
			return null;
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
				if (getResource() != null) {
					getResource().setName(name);
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
			return "VirtualModel:" + getURI();
		}

		@Override
		public ViewPoint getViewPoint() {
			return viewPoint;
		}

		@Override
		public void setViewPoint(ViewPoint viewPoint) {
			if (this.viewPoint != viewPoint) {
				ViewPoint oldViewPoint = this.viewPoint;
				this.viewPoint = viewPoint;
				// updateBindingModel();
				getPropertyChangeSupport().firePropertyChange(VIEW_POINT_KEY, oldViewPoint, viewPoint);
			}
		}

		/*
		 * protected void notifyEditionSchemeModified() { }
		 */

		/**
		 * Return all {@link FlexoConcept} defined in this {@link VirtualModel} which have no parent
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllRootFlexoConcepts() {
			Vector<FlexoConcept> returned = new Vector<FlexoConcept>();
			for (FlexoConcept ep : getFlexoConcepts()) {
				if (ep.isRoot()) {
					returned.add(ep);
				}
			}
			return returned;
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void addToFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperAdder(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null, aFlexoConcept);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, null, aFlexoConcept);
				}
			}
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void removeFromFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperRemover(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", aFlexoConcept, null);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, aFlexoConcept, null);
				}
			}
		}

		/**
		 * Return all {@link FlexoConcept} defined in this {@link VirtualModel}
		 * 
		 * @return
		 */
		/*
		 * @Override public Vector<FlexoConcept> getFlexoConcepts() { return
		 * flexoConcepts; }
		 * 
		 * public void setFlexoConcepts(Vector<FlexoConcept>
		 * flexoConcepts) { this.flexoConcepts = flexoConcepts; }
		 * 
		 * @Override public void addToFlexoConcepts(FlexoConcept pattern) {
		 * pattern.setVirtualModel(this); flexoConcepts.add(pattern);
		 * setChanged(); notifyObservers(new FlexoConceptCreated(pattern)); }
		 * 
		 * @Override public void removeFromFlexoConcepts(FlexoConcept
		 * pattern) { pattern.setVirtualModel(null);
		 * flexoConcepts.remove(pattern); setChanged(); notifyObservers(new
		 * FlexoConceptDeleted(pattern)); }
		 */

		/**
		 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
		 * 
		 * @param flexoConceptId
		 * @return
		 */
		@Override
		public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI) {
			for (FlexoConcept flexoConcept : getFlexoConcepts()) {
				if (flexoConcept.getName() != null && flexoConcept.getName().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
				if (flexoConcept.getName() != null && flexoConcept.getURI().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
				// Special case to handle conversion from old VP version
				// TODO: to be removed when all VP are up-to-date
				if (getViewPoint() != null && flexoConcept != null) {
					if ((getViewPoint().getURI() + "#" + flexoConcept.getName()).equals(flexoConceptNameOrURI)) {
						return flexoConcept;
					}
				}

			}
			// logger.warning("Not found FlexoConcept:" + flexoConceptId);
			return null;
		}

		public SynchronizationScheme createSynchronizationScheme() {
			SynchronizationScheme newSynchronizationScheme = getVirtualModelFactory().newSynchronizationScheme();
			newSynchronizationScheme.setSynchronizedVirtualModel(this);
			newSynchronizationScheme.setName("synchronization");
			addToFlexoBehaviours(newSynchronizationScheme);
			return newSynchronizationScheme;
		}

		@Override
		public VirtualModelBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new VirtualModelBindingModel(this);
				// createBindingModel();
			}
			return bindingModel;
		}

		/*@Override
		public void updateBindingModel() {
			logger.fine("updateBindingModel()");
			bindingModel = null;
			createBindingModel();
			super.updateBindingModel();
		}*/

		/*private void createBindingModel() {
			bindingModel = new VirtualModelBindingModel(this);
			for (FlexoConcept ep : getFlexoConcepts()) {
				// bindingModel.addToBindingVariables(new
				// FlexoConceptPathElement<ViewPoint>(ep, this));
				bindingModel.addToBindingVariables(new BindingVariable(ep.getName(), FlexoConceptInstanceType
						.getFlexoConceptInstanceType(ep)));
			}
		}*/

		/*
		 * @Override public String simpleRepresentation() { return
		 * "VirtualModel:" +
		 * FlexoLocalization.localizedForKey(getLocalizedDictionary(),
		 * getName()); }
		 */

		// ==========================================================================
		// ============================== Model Slots
		// ===============================
		// ==========================================================================

		@Override
		public <MS extends ModelSlot<?>> List<MS> getModelSlots(Class<MS> msType) {
			List<MS> returned = new ArrayList<MS>();
			for (ModelSlot<?> ms : getModelSlots()) {
				if (TypeUtils.isTypeAssignableFrom(msType, ms.getClass())) {
					returned.add((MS) ms);
				}
			}
			return returned;
		}

		/*
		@Override
		@Deprecated
		public void addToModelSlots(ModelSlot<?> aModelSlot) {
			if (aModelSlot != null && aModelSlot.getName() != null && aModelSlot.getName().equals("virtualModelInstance")) {
				// Temporary hack to ignore reflexive model slot being inherited from 1.7-beta version
				logger.warning("Reflexive model slot being inherited from 1.7-beta version are ignored now");
				return;
			}
			performSuperAdder(MODEL_SLOTS_KEY, aModelSlot);
		}
		*/

		/*
		 * public ModelSlot<?> getModelSlot(String modelSlotName) { for
		 * (ModelSlot<?> ms : getModelSlots()) { if (ms.getName() != null &&
		 * ms.getName().equals(modelSlotName)) { return ms; } } return null; }
		 */

		public List<ModelSlot<?>> getRequiredModelSlots() {
			List<ModelSlot<?>> requiredModelSlots = new ArrayList<ModelSlot<?>>();
			for (ModelSlot<?> modelSlot : getModelSlots()) {
				if (modelSlot.getIsRequired()) {
					requiredModelSlots.add(modelSlot);
				}
			}
			return requiredModelSlots;
		}

		/**
		 * Retrieve object referenced by its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public Object getObject(String uri) {
			for (FlexoMetaModel<?> mm : getAllReferencedMetaModels()) {
				if (mm != null) {
					Object o = mm.getObject(uri);
					if (o != null) {
						return o;
					}
				}
			}
			return null;
		}

		/**
		 * Retrieve ontology object from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyObject getOntologyObject(String uri) {
			Object returned = getObject(uri);
			if (returned instanceof IFlexoOntologyObject) {
				return (IFlexoOntologyObject) returned;
			}
			return null;
		}

		/**
		 * Retrieve ontology class from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyClass getOntologyClass(String uri) {
			Object returned = getOntologyObject(uri);
			if (returned instanceof IFlexoOntologyClass) {
				return (IFlexoOntologyClass) returned;
			}
			return null;
		}

		/**
		 * Retrieve ontology individual from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyIndividual getOntologyIndividual(String uri) {
			Object returned = getOntologyObject(uri);
			if (returned instanceof IFlexoOntologyIndividual) {
				return (IFlexoOntologyIndividual) returned;
			}
			return null;
		}

		/**
		 * Retrieve ontology property from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyStructuralProperty getOntologyProperty(String uri) {
			Object returned = getOntologyObject(uri);
			if (returned instanceof IFlexoOntologyStructuralProperty) {
				return (IFlexoOntologyStructuralProperty) returned;
			}
			return null;
		}

		/**
		 * Retrieve ontology object property from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyObjectProperty getOntologyObjectProperty(String uri) {
			Object returned = getOntologyObject(uri);
			if (returned instanceof IFlexoOntologyObjectProperty) {
				return (IFlexoOntologyObjectProperty) returned;
			}
			return null;
		}

		/**
		 * Retrieve ontology object property from its URI.<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public IFlexoOntologyDataProperty getOntologyDataProperty(String uri) {
			Object returned = getOntologyObject(uri);
			if (returned instanceof IFlexoOntologyDataProperty) {
				return (IFlexoOntologyDataProperty) returned;
			}
			return null;
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
			return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(ontologyURI, conceptURI);
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
			HashSet<FlexoMetaModel<?>> returned = new HashSet<FlexoMetaModel<?>>();
			for (ModelSlot modelSlot : getModelSlots()) {
				if (modelSlot instanceof TypeAwareModelSlot) {
					TypeAwareModelSlot tsModelSlot = (TypeAwareModelSlot) modelSlot;
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

		@Override
		public void save() {
			logger.info("Saving ViewPoint to " + getResource().getFlexoIODelegate().toString() + "...");

			try {
				getResource().save(null);
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
		}

		@Override
		public VirtualModelTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		/**
		 * This is the builder used to deserialize {@link VirtualModel} objects.
		 * 
		 * @author sylvain
		 * 
		 */
		/*
		 * public static class VirtualModelBuilder { private VirtualModel
		 * virtualModel; private FlexoVersion modelVersion; private final
		 * ViewPointLibrary viewPointLibrary; private final ViewPoint viewPoint;
		 * VirtualModelResource resource;
		 * 
		 * public VirtualModelImplBuilder(ViewPointLibrary vpLibrary, ViewPoint
		 * viewPoint, VirtualModelResource resource) { this.viewPointLibrary =
		 * vpLibrary; this.viewPoint = viewPoint; this.resource = resource; }
		 * 
		 * public VirtualModelImplBuilder(ViewPointLibrary vpLibrary, ViewPoint
		 * viewPoint, VirtualModel virtualModel) { this.virtualModel =
		 * virtualModel; this.viewPointLibrary = vpLibrary; this.viewPoint =
		 * viewPoint; this.resource = virtualModel.getResource(); }
		 * 
		 * public VirtualModelImplBuilder(ViewPointLibrary vpLibrary, ViewPoint
		 * viewPoint, VirtualModelResource resource, FlexoVersion modelVersion)
		 * { this.modelVersion = modelVersion; this.viewPointLibrary =
		 * vpLibrary; this.viewPoint = viewPoint; this.resource = resource; }
		 * 
		 * public ViewPointLibrary getViewPointLibrary() { return
		 * viewPointLibrary; }
		 * 
		 * public FlexoVersion getModelVersion() { return modelVersion; }
		 * 
		 * public VirtualModelImpl getVirtualModel() { return virtualModel; }
		 * 
		 * public void setVirtualModel(VirtualModel virtualModel) {
		 * this.virtualModel = virtualModel; }
		 * 
		 * public ViewPoint getViewPoint() { return viewPoint; } }
		 */

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("VirtualModel " + getName() + " type=" + getClass().getSimpleName() + " uri=\"" + getURI() + "\"", context);
			out.append(" {" + StringUtils.LINE_SEPARATOR, context);

			if (getModelSlots().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (ModelSlot modelSlot : getModelSlots()) {
					// if (modelSlot.getMetaModelResource() != null) {
					out.append(modelSlot.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context, 1);
					// }
				}
			}

			if (getFlexoRoles().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoRole pr : getFlexoRoles()) {
					out.append(pr.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			if (getFlexoBehaviours().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoBehaviour es : getFlexoBehaviours()) {
					out.append(es.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			if (getFlexoConcepts().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoConcept ep : getFlexoConcepts()) {
					out.append(ep.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}
			out.append("}" + StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot extends
			ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, VirtualModel> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(VirtualModel.class, "virtual_model_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, VirtualModel> applyValidation(VirtualModel vm) {
			for (ModelSlot ms : vm.getModelSlots()) {
				if (ms instanceof VirtualModelModelSlot && "virtualModelInstance".equals(ms.getName())) {
					RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(vm);
					return new ValidationWarning<ShouldNotHaveReflexiveVirtualModelModelSlot, VirtualModel>(this, vm,
							"virtual_model_should_not_have_reflexive_model_slot_no_more", fixProposal);

				}
			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot extends
				FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, VirtualModel> {

			private final VirtualModel vm;

			public RemoveReflexiveVirtualModelModelSlot(VirtualModel vm) {
				super("remove_reflexive_modelslot");
				this.vm = vm;
			}

			@Override
			protected void fixAction() {
				for (ModelSlot ms : new ArrayList<ModelSlot>(vm.getModelSlots())) {
					if ("virtualModelInstance".equals(ms.getName())) {
						vm.removeFromModelSlots(ms);
					}
				}
			}

		}

	}

}
