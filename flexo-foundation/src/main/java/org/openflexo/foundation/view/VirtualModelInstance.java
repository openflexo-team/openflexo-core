/*
 * (c) Copyright 2010-201 AgileBirds
 * (c) Copyright 2013 Openflexo
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
package org.openflexo.foundation.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.view.action.SynchronizationSchemeAction;
import org.openflexo.foundation.view.action.SynchronizationSchemeActionType;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResourceImpl;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.SynchronizationScheme;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link VirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * 
 * As such, a {@link VirtualModelInstance} is instantiated inside a {@link View}, and all model slot defined for the corresponding
 * {@link ViewPoint} are instantiated (reified) with existing or build-in managed {@link FlexoModel}.<br>
 * 
 * A {@link VirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself an {@link FlexoConceptInstance}.<br>
 * 
 * A {@link VirtualModelInstance} might be used in the Design Space (for example to encode a Diagram)
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModelInstance.VirtualModelInstanceImpl.class)
@XMLElement
public interface VirtualModelInstance extends FlexoConceptInstance, ResourceData<VirtualModelInstance>,
		FlexoModel<VirtualModelInstance, VirtualModel>, TechnologyObject<VirtualModelTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String TITLE_KEY = "title";
	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";
	@PropertyIdentifier(type = List.class)
	public static final String MODEL_SLOT_INSTANCES_KEY = "modelSlotInstances";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_CONCEPT_INSTANCES_LIST_KEY = "flexoConceptInstancesList";

	@Getter(value = TITLE_KEY)
	@XMLAttribute
	public String getTitle();

	@Setter(TITLE_KEY)
	public void setTitle(String title);

	public VirtualModel getVirtualModel();

	public void setVirtualModel(VirtualModel virtualModel);

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute
	public String getVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setVirtualModelURI(String virtualModelURI);

	@Getter(value = MODEL_SLOT_INSTANCES_KEY, cardinality = Cardinality.LIST, inverse = ModelSlotInstance.VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLElement
	public List<ModelSlotInstance<?, ?>> getModelSlotInstances();

	@Setter(MODEL_SLOT_INSTANCES_KEY)
	public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> modelSlotInstances);

	@Adder(MODEL_SLOT_INSTANCES_KEY)
	public void addToModelSlotInstances(ModelSlotInstance<?, ?> aModelSlotInstance);

	@Remover(MODEL_SLOT_INSTANCES_KEY)
	public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> aModelSlotInstance);

	@Getter(value = FLEXO_CONCEPT_INSTANCES_LIST_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	public List<FlexoConceptInstance> getFlexoConceptInstancesList();

	@Setter(FLEXO_CONCEPT_INSTANCES_LIST_KEY)
	public void setFlexoConceptInstancesList(List<FlexoConceptInstance> flexoConceptInstancesList);

	@Adder(FLEXO_CONCEPT_INSTANCES_LIST_KEY)
	public void addToFlexoConceptInstancesList(FlexoConceptInstance aFlexoConceptInstancesList);

	@Remover(FLEXO_CONCEPT_INSTANCES_LIST_KEY)
	public void removeFromFlexoConceptInstancesList(FlexoConceptInstance aFlexoConceptInstancesList);

	public void synchronize(FlexoEditor editor);

	public boolean isSynchronizable();

	/**
	 * Return {@link ModelSlotInstance} concretizing supplied modelSlot
	 * 
	 * @param modelSlot
	 * @return
	 */
	public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(ModelSlot<RD> modelSlot);

	/**
	 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name
	 * 
	 * @param modelSlot
	 * @return
	 */
	public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(String modelSlotName);

	@Getter(NAME_KEY)
	public String getName();

	/**
	 * Instanciate and register a new {@link FlexoConceptInstance}
	 * 
	 * @param pattern
	 * @return
	 */
	public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept);

	/**
	 * Return run-time value for {@link BindingVariable} variable
	 * 
	 * @param variable
	 * @return
	 */
	public Object getValueForVariable(BindingVariable variable);

	public Collection<FlexoConceptInstance> getAllEPInstances();

	public Collection<FlexoConceptInstance> getEPInstances(String epName);

	public List<FlexoConceptInstance> getEPInstances(FlexoConcept ep);

	public static abstract class VirtualModelInstanceImpl extends FlexoConceptInstanceImpl implements VirtualModelInstance {

		private static final Logger logger = Logger.getLogger(VirtualModelInstance.class.getPackage().getName());

		private VirtualModelInstanceResource resource;
		// private List<ModelSlotInstance<?, ?>> modelSlotInstances;
		// private Map<ModelSlot, FlexoModel<?, ?>> modelsMap = new HashMap<ModelSlot, FlexoModel<?, ?>>(); // Do not serialize
		// this.
		private String title;

		private Hashtable<FlexoConcept, Map<Long, FlexoConceptInstance>> flexoConceptInstances;

		// TODO: move this code to the VirtualModelInstanceResource
		public static VirtualModelInstanceResource newVirtualModelInstance(String virtualModelName, String virtualModelTitle,
				VirtualModel virtualModel, View view) throws SaveResourceException {

			VirtualModelInstanceResource newVirtualModelResource = VirtualModelInstanceResourceImpl.makeVirtualModelInstanceResource(
					virtualModelName, virtualModel, view);

			VirtualModelInstanceImpl newVirtualModelInstance = (VirtualModelInstanceImpl) newVirtualModelResource.getFactory().newInstance(
					VirtualModelInstance.class);
			newVirtualModelInstance.setVirtualModel(virtualModel);

			newVirtualModelResource.setResourceData(newVirtualModelInstance);
			newVirtualModelInstance.setResource(newVirtualModelResource);
			newVirtualModelInstance.setTitle(virtualModelTitle);

			view.getResource().notifyContentsAdded(newVirtualModelResource);

			newVirtualModelResource.save(null);

			return newVirtualModelResource;
		}

		/**
		 * Default constructor with
		 */
		public VirtualModelInstanceImpl() {
			super();
			// modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();
			flexoConceptInstances = new Hashtable<FlexoConcept, Map<Long, FlexoConceptInstance>>();
		}

		@Override
		public VirtualModelInstanceModelFactory getFactory() {
			if (getResource() != null) {
				return getResource().getFactory();
			}
			return null;
		}

		@Override
		public String getURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		@Override
		public View getView() {
			if (getResource() != null && getResource().getContainer() != null) {
				return getResource().getContainer().getView();
			}
			return null;
		}

		@Override
		public VirtualModel getFlexoConcept() {
			return (VirtualModel) super.getFlexoConcept();
		}

		public ViewPoint getViewPoint() {
			if (getVirtualModel() != null) {
				return getVirtualModel().getViewPoint();
			}
			return null;
		}

		@Override
		public VirtualModel getVirtualModel() {
			return getFlexoConcept();
		}

		@Override
		public void setVirtualModel(VirtualModel virtualModel) {
			setFlexoConcept(virtualModel);
		}

		@Override
		public String getVirtualModelURI() {
			return super.getFlexoConceptURI();
		}

		@Override
		public void setVirtualModelURI(String virtualModelURI) {
			super.setFlexoConceptURI(virtualModelURI);
		}

		@Override
		public VirtualModel getMetaModel() {
			return getFlexoConcept();
		}

		@Override
		public VirtualModelTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public FlexoProject getProject() {
			if (getView() != null) {
				return getView().getProject();
			}
			return super.getProject();
		}

		/**
		 * Instanciate and register a new {@link FlexoConceptInstance}
		 * 
		 * @param pattern
		 * @return
		 */
		@Override
		public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept) {
			FlexoConceptInstance returned = getResource().getFactory().newInstance(FlexoConceptInstance.class);
			returned.setVirtualModelInstance(this);
			returned.setFlexoConcept(concept);
			return registerFlexoConceptInstance(returned);
		}

		/**
		 * Register an existing {@link FlexoConceptInstance} (used in deserialization)
		 * 
		 * @param epi
		 * @return
		 */
		protected FlexoConceptInstance registerFlexoConceptInstance(FlexoConceptInstance epi) {
			if (epi.getFlexoConcept() == null) {
				logger.warning("Could not register FlexoConceptInstance with null FlexoConcept: " + epi);
				logger.warning("EPI: " + epi.debug());
			} else {
				Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(epi.getFlexoConcept());
				if (hash == null) {
					hash = new Hashtable<Long, FlexoConceptInstance>();
					flexoConceptInstances.put(epi.getFlexoConcept(), hash);
				}
				hash.put(epi.getFlexoID(), epi);
				// System.out.println("Registered EPI " + epi + " in " + epi.getFlexoConcept());
				// System.out.println("Registered: " + getEPInstances(epi.getFlexoConcept()));
			}
			return epi;
		}

		/**
		 * Un-register an existing {@link FlexoConceptInstance}
		 * 
		 * @param epi
		 * @return
		 */
		protected FlexoConceptInstance unregisterFlexoConceptInstance(FlexoConceptInstance epi) {
			Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(epi.getFlexoConcept());
			if (hash == null) {
				hash = new Hashtable<Long, FlexoConceptInstance>();
				flexoConceptInstances.put(epi.getFlexoConcept(), hash);
			}
			hash.remove(epi.getFlexoID());
			return epi;
		}

		// Do not use this since not efficient, used in deserialization only
		@Override
		public List<FlexoConceptInstance> getFlexoConceptInstancesList() {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			for (Map<Long, FlexoConceptInstance> epMap : flexoConceptInstances.values()) {
				for (FlexoConceptInstance epi : epMap.values()) {
					returned.add(epi);
				}
			}
			return returned;
		}

		@Override
		public void setFlexoConceptInstancesList(List<FlexoConceptInstance> epiList) {
			for (FlexoConceptInstance epi : epiList) {
				addToFlexoConceptInstancesList(epi);
			}
		}

		@Override
		public void addToFlexoConceptInstancesList(FlexoConceptInstance epi) {
			registerFlexoConceptInstance(epi);
		}

		@Override
		public void removeFromFlexoConceptInstancesList(FlexoConceptInstance epi) {
			unregisterFlexoConceptInstance(epi);
		}

		public Hashtable<FlexoConcept, Map<Long, FlexoConceptInstance>> getFlexoConceptInstances() {
			return flexoConceptInstances;
		}

		public void setFlexoConceptInstances(Hashtable<FlexoConcept, Map<Long, FlexoConceptInstance>> flexoConceptInstances) {
			this.flexoConceptInstances = flexoConceptInstances;
		}

		// TODO: performance isssues
		@Override
		public Collection<FlexoConceptInstance> getAllEPInstances() {
			return getFlexoConceptInstancesList();
		}

		@Override
		public Collection<FlexoConceptInstance> getEPInstances(String epName) {
			if (getVirtualModel() == null) {
				return Collections.emptyList();
			}
			FlexoConcept ep = getVirtualModel().getFlexoConcept(epName);
			return getEPInstances(ep);
		}

		@Override
		public List<FlexoConceptInstance> getEPInstances(FlexoConcept ep) {
			if (ep == null) {
				// logger.warning("Unexpected null FlexoConcept");
				return Collections.emptyList();
			}
			Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(ep);
			if (hash == null) {
				hash = new Hashtable<Long, FlexoConceptInstance>();
				flexoConceptInstances.put(ep, hash);
			}
			// TODO: performance issue here
			List<FlexoConceptInstance> returned = new ArrayList(hash.values());
			for (FlexoConcept childEP : ep.getChildFlexoConcepts()) {
				returned.addAll(getEPInstances(childEP));
			}
			return returned;
		}

		// TODO: refactor this
		@Deprecated
		public List<FlexoConceptInstance> getEPInstancesWithPropertyEqualsTo(String epName, String epProperty, Object value) {
			/*List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			Collection<FlexoConceptInstance> epis = getEPInstances(epName);
			for (FlexoConceptInstance epi : epis) {
				Object evaluate = epi.evaluate(epProperty);
				if (value == null && evaluate == value || value != null && value.equals(evaluate)) {
					returned.add(epi);
				}
			}
			return returned;*/
			return null;
		}

		@Override
		public VirtualModelInstanceResource getResource() {
			return resource;
		}

		@Override
		public void setResource(org.openflexo.foundation.resource.FlexoResource<VirtualModelInstance> resource) {
			this.resource = (VirtualModelInstanceResource) resource;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return null;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public void setTitle(String title) {
			String oldTitle = this.title;
			if (requireChange(oldTitle, title)) {
				this.title = title;
				setChanged();
				notifyObservers(new VEDataModification("title", oldTitle, title));
			}
		}

		@Override
		public VirtualModelInstanceImpl getResourceData() {
			return this;
		}

		@Override
		public String toString() {
			return "VirtualModelInstance[name=" + getName() + "/virtualModel=" + getVirtualModel() + "/hash="
					+ Integer.toHexString(hashCode()) + "]";
		}

		// ==========================================================================
		// ============================== Model Slots ===============================
		// ==========================================================================

		/**
		 * Return {@link ModelSlotInstance} concretizing supplied modelSlot
		 * 
		 * @param modelSlot
		 * @return
		 */
		@Override
		public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(ModelSlot<RD> modelSlot) {
			for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
				if (msInstance.getModelSlot() == modelSlot) {
					return (ModelSlotInstance<?, RD>) msInstance;
				}
			}
			if (modelSlot instanceof VirtualModelModelSlot && ((VirtualModelModelSlot) modelSlot).isReflexiveModelSlot()) {
				VirtualModelModelSlotInstance reflexiveModelSlotInstance = getResource().getFactory().newInstance(
						VirtualModelModelSlotInstance.class);
				reflexiveModelSlotInstance.setModelSlot((VirtualModelModelSlot) modelSlot);
				reflexiveModelSlotInstance.setAccessedResourceData(this);
				addToModelSlotInstances(reflexiveModelSlotInstance);
				return (ModelSlotInstance<?, RD>) reflexiveModelSlotInstance;
			}
			logger.warning("Cannot find ModelSlotInstance for ModelSlot " + modelSlot);
			if (getVirtualModel() != null && !getVirtualModel().getModelSlots().contains(modelSlot)) {
				logger.warning("Worse than that, supplied ModelSlot is not part of virtual model " + getVirtualModel());
			}
			return null;
		}

		/**
		 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name
		 * 
		 * @param modelSlot
		 * @return
		 */
		@Override
		public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(String modelSlotName) {
			for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
				if (msInstance.getModelSlot().getName().equals(modelSlotName)) {
					return (ModelSlotInstance<?, RD>) msInstance;
				}
			}
			logger.warning("Cannot find ModelSlotInstance named " + modelSlotName);
			return null;
		}

		/*	@Override
			public List<ModelSlotInstance<?, ?>> getModelSlotInstances() {
				return modelSlotInstances;
			}

			@Override
			public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> instances) {
				this.modelSlotInstances = instances;
			}

			@Override
			public void addToModelSlotInstances(ModelSlotInstance<?, ?> instance) {
				if (!modelSlotInstances.contains(instance)) {
					instance.setVirtualModelInstance(this);
					modelSlotInstances.add(instance);
					setChanged();
					notifyObservers(new VEDataModification("modelSlotInstances", null, instance));
				}
			}

			@Override
			public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> instance) {
				if (modelSlotInstances.contains(instance)) {
					instance.setVirtualModelInstance(null);
					modelSlotInstances.remove(instance);
					setChanged();
					notifyObservers(new VEDataModification("modelSlotInstances", instance, null));
				}
			}*/

		/**
		 * Return a set of all meta models (load them when unloaded) used in this {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoMetaModel> getAllMetaModels() {
			Set<FlexoMetaModel> allMetaModels = new HashSet<FlexoMetaModel>();
			for (ModelSlotInstance instance : getModelSlotInstances()) {
				if (instance.getModelSlot() instanceof TypeAwareModelSlot
						&& ((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource() != null) {
					allMetaModels.add(((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource().getMetaModelData());
				}
			}
			return allMetaModels;
		}

		/**
		 * Return a set of all models (load them when unloaded) used in this {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoModel<?, ?>> getAllModels() {
			Set<FlexoModel<?, ?>> allModels = new HashSet<FlexoModel<?, ?>>();
			for (ModelSlotInstance instance : getModelSlotInstances()) {
				if (instance.getResourceData() instanceof FlexoModel) {
					allModels.add(instance.getResourceData());
				}
			}
			return allModels;
		}

		// ==========================================================================
		// ================================= Delete ===============================
		// ==========================================================================

		@Override
		public final boolean delete() {

			logger.info("Deleting virtual model instance " + this);

			// Dereference the resource
			if (resource != null) {
				resource = null;
			}

			super.delete();

			deleteObservers();

			return true;
		}

		// ==========================================================================
		// =============================== Synchronize ==============================
		// ==========================================================================

		@Override
		public void synchronize(FlexoEditor editor) {
			if (isSynchronizable()) {
				VirtualModel vm = getVirtualModel();
				SynchronizationScheme ss = vm.getSynchronizationScheme();
				SynchronizationSchemeActionType actionType = new SynchronizationSchemeActionType(ss, this);
				SynchronizationSchemeAction action = actionType.makeNewAction(this, null, editor);
				action.doAction();
			} else {
				logger.warning("No synchronization scheme defined for " + getVirtualModel());
			}
		}

		@Override
		public boolean isSynchronizable() {
			return getVirtualModel() != null && getVirtualModel().hasSynchronizationScheme();
		}

		/**
		 * Return run-time value for {@link BindingVariable} variable
		 * 
		 * @param variable
		 * @return
		 */
		@Override
		public Object getValueForVariable(BindingVariable variable) {
			logger.warning("Not implemented: getValueForVariable() " + variable);
			return null;
		}

		@Override
		public Object getObject(String objectURI) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
