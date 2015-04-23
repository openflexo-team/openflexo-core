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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.AbstractVirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
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
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

/**
 * An {@link AbstractVirtualModel} is the specification of a model which will be instantied in a {@link View} as a set of federated models.
 * An {@link AbstractVirtualModel} is either a {@link VirtualModel} of a {@link ViewPoint}
 * 
 * The base modelling element of a {@link AbstractVirtualModel} is provided by {@link FlexoConcept} concept.
 * 
 * A {@link AbstractVirtualModel} instance contains a set of {@link FlexoConceptInstance}.
 * 
 * A {@link AbstractVirtualModel} is itself an {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractVirtualModel.AbstractVirtualModelImpl.class)
@Imports({ @Import(FlexoConceptStructuralFacet.class), @Import(FlexoConceptBehaviouralFacet.class),
		@Import(DeleteFlexoConceptInstanceParameter.class), @Import(AddFlexoConceptInstanceParameter.class) })
public interface AbstractVirtualModel<VM extends AbstractVirtualModel<VM>> extends FlexoConcept, VirtualModelObject, FlexoMetaModel<VM>,
		ResourceData<VM>, TechnologyObject<FMLTechnologyAdapter> {

	// public static final String REFLEXIVE_MODEL_SLOT_NAME = "virtualModelInstance";

	public static final String RESOURCE = "resource";

	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = Vector.class)
	public static final String FLEXO_CONCEPTS_KEY = "flexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String MODEL_SLOTS_KEY = "modelSlots";

	@Override
	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return resource for this virtual model
	 * 
	 * @return
	 */
	@Override
	@Getter(value = RESOURCE, ignoreType = true)
	public FlexoResource<VM> getResource();

	/**
	 * Sets resource for this virtual model
	 * 
	 * @param aName
	 */
	@Override
	@Setter(value = RESOURCE)
	public void setResource(FlexoResource<VM> aVirtualModelResource);

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
	 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel}
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

	@Getter(value = MODEL_SLOTS_KEY, cardinality = Cardinality.LIST, inverse = ModelSlot.OWNER_KEY)
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
	// public FMLRTModelSlot getReflexiveModelSlot();

	/**
	 * Return flag indicating if supplied BindingVariable is set at runtime
	 * 
	 * @param variable
	 * @return
	 * @see VirtualModelInstance#getValueForVariable(BindingVariable)
	 */
	// public boolean handleVariable(BindingVariable variable);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no parent
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllRootFlexoConcepts();

	public boolean hasNature(VirtualModelNature nature);

	@Override
	public VirtualModelBindingModel getBindingModel();

	public static abstract class AbstractVirtualModelImpl<VM extends AbstractVirtualModel<VM>> extends FlexoConceptImpl implements
			AbstractVirtualModel<VM> {

		private static final Logger logger = Logger.getLogger(AbstractVirtualModel.class.getPackage().getName());

		// private Vector<FlexoConcept> flexoConcepts;
		// private List<ModelSlot<?>> modelSlots;
		private AbstractVirtualModelResource<VM> resource;
		// private ViewPointLocalizedDictionary localizedDictionary;

		private boolean readOnly = false;

		private final VirtualModelInstanceType vmInstanceType = new VirtualModelInstanceType(this);

		/**
		 * Stores a chained collections of objects which are involved in validation
		 */
		// private final ChainedCollection<FMLObject> validableObjects = null;

		// Used during deserialization, do not use it
		public AbstractVirtualModelImpl() {
			super();
		}

		@Override
		public FlexoConceptInstanceType getInstanceType() {
			return vmInstanceType;
		}

		@Override
		public FMLModelFactory getFMLModelFactory() {
			if (isDeserializing()) {
				return deserializationFactory;
			}
			if (getResource() != null) {
				return getResource().getFactory();
			} else {
				return getDeserializationFactory();
			}
		}

		private FMLModelFactory deserializationFactory;

		@Override
		public void initializeDeserialization(FMLModelFactory factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			for (FlexoConcept ep : getFlexoConcepts()) {
				ep.finalizeDeserialization();
			}
			// Ensure access to reflexive model slot
			// getReflexiveModelSlot();
		}

		@Override
		public final boolean hasNature(VirtualModelNature nature) {
			return nature.hasNature(this);
		}

		/**
		 * Return the URI of the {@link AbstractVirtualModel}<br>
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
				String oldValue = getName();
				if (getResource() != null) {
					try {
						getResource().setName(name);
						getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
					} catch (CannotRenameException e) {
						e.printStackTrace();
					}
				} else {
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
		 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no parent
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
			SynchronizationScheme newSynchronizationScheme = getFMLModelFactory().newSynchronizationScheme();
			newSynchronizationScheme.setSynchronizedVirtualModel(this);
			newSynchronizationScheme.setName("synchronization");
			addToFlexoBehaviours(newSynchronizationScheme);
			return newSynchronizationScheme;
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

		@Override
		public void addToModelSlots(ModelSlot<?> aModelSlot) {
			performSuperAdder(MODEL_SLOTS_KEY, aModelSlot);
			notifiedPropertiesChanged(null, aModelSlot);
		}

		@Override
		public void removeFromModelSlots(ModelSlot<?> aModelSlot) {
			performSuperRemover(MODEL_SLOTS_KEY, aModelSlot);
			notifiedPropertiesChanged(aModelSlot, null);
		}

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
		public AbstractVirtualModelResource<VM> getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<VM> resource) {
			this.resource = (AbstractVirtualModelResource<VM>) resource;
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
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		/**
		 * This is the builder used to deserialize {@link AbstractVirtualModel} objects.
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

			if (getDeclaredProperties().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoProperty<?> pr : getDeclaredProperties()) {
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

		@Override
		public AbstractVirtualModel<?> getResourceData() {
			return this;
		}

		// Developper's note: we implement here VirtualModelObject API
		// Do not consider getOwningVirtualModel()
		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			return this;
		}

		@Override
		public abstract VirtualModelBindingModel getBindingModel();

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot extends
			ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(AbstractVirtualModel.class, "virtual_model_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> applyValidation(AbstractVirtualModel vm) {
			for (ModelSlot ms : ((AbstractVirtualModel<?>) vm).getModelSlots()) {
				if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
					RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(vm);
					return new ValidationWarning<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel>(this, vm,
							"virtual_model_should_not_have_reflexive_model_slot_no_more", fixProposal);

				}
			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot extends
				FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> {

			private final AbstractVirtualModel vm;

			public RemoveReflexiveVirtualModelModelSlot(AbstractVirtualModel vm) {
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
