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
	@PropertyIdentifier(type = FlexoConceptInstance.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_CONCEPT_INSTANCES_KEY = "flexoConceptInstances";

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

	@Getter(value = FLEXO_CONCEPT_INSTANCES_KEY, cardinality = Cardinality.LIST, inverse = ModelSlotInstance.VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLElement
	public List<FlexoConceptInstance> getFlexoConceptInstances();

	@Setter(FLEXO_CONCEPT_INSTANCES_KEY)
	public void setFlexoConceptInstances(List<FlexoConceptInstance> someFlexoConceptInstances);

	@Adder(FLEXO_CONCEPT_INSTANCES_KEY)
	public void addToFlexoConceptInstances(FlexoConceptInstance aFlexoConceptInstance);

	@Remover(FLEXO_CONCEPT_INSTANCES_KEY)
	public void removeFromFlexoConceptInstances(FlexoConceptInstance aFlexoConceptInstance);

	public void synchronize(FlexoEditor editor);

	public boolean isSynchronizable();

	/**
	 * Return {@link ModelSlotInstance} concretizing supplied modelSlot
	 * 
	 * @param modelSlot
	 * @return
	 */
	public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
			MS modelSlot);

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

	/**
	 * Return a newly created list of all {@link FlexoConceptInstance} conform to the FlexoConcept identified by supplied String parameter
	 * (this could be either the name or the uri of concept)
	 * 
	 * @param flexoConceptNameOrURI
	 * @return
	 */
	public List<FlexoConceptInstance> getFlexoConceptInstances(String flexoConceptNameOrURI);

	/**
	 * Return a newly created list of all {@link FlexoConceptInstance} conform to the supplied FlexoConcept
	 * 
	 * @param flexoConcept
	 * @return
	 */
	public List<FlexoConceptInstance> getFlexoConceptInstances(FlexoConcept flexoConcept);

	public boolean hasNature(VirtualModelInstanceNature nature);

	public static abstract class VirtualModelInstanceImpl extends FlexoConceptInstanceImpl implements VirtualModelInstance {

		private static final Logger logger = Logger.getLogger(VirtualModelInstance.class.getPackage().getName());

		private VirtualModelInstanceResource resource;
		// private List<ModelSlotInstance<?, ?>> modelSlotInstances;
		// private Map<ModelSlot, FlexoModel<?, ?>> modelsMap = new HashMap<ModelSlot, FlexoModel<?, ?>>(); // Do not serialize
		// this.
		private String title;

		private final Hashtable<String, Map<Long, FlexoConceptInstance>> flexoConceptInstances;

		// private final List<FlexoConceptInstance> orderedFlexoConceptInstances;

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
			flexoConceptInstances = new Hashtable<String, Map<Long, FlexoConceptInstance>>();
			// orderedFlexoConceptInstances = new ArrayList<FlexoConceptInstance>();
		}

		@Override
		public VirtualModelInstanceModelFactory getFactory() {
			if (getResource() != null) {
				return getResource().getFactory();
			}
			return null;
		}

		@Override
		public final boolean hasNature(VirtualModelInstanceNature nature) {
			return nature.hasNature(this);
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
			returned.setFlexoConcept(concept);
			addToFlexoConceptInstances(returned);
			return returned;
		}

		/**
		 * Add a {@link FlexoConceptInstance}
		 * 
		 * @param fci
		 * @return
		 */
		@Override
		public void addToFlexoConceptInstances(FlexoConceptInstance fci) {
			if (fci.getFlexoConceptURI() == null) {
				logger.warning("Could not register FlexoConceptInstance with null FlexoConceptURI: " + fci);
				// logger.warning("EPI: " + fci.debug());
			} else {
				Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(fci.getFlexoConceptURI());
				if (hash == null) {
					hash = new Hashtable<Long, FlexoConceptInstance>();
					flexoConceptInstances.put(fci.getFlexoConceptURI(), hash);
				}
				// We store here the FCI twice:
				// - first in double-entries hash map
				// - then in an ordered list (internally performed by PAMELA)
				// We rely on PAMELA schemes to handle notifications
				hash.put(fci.getFlexoID(), fci);

				performSuperAdder(FLEXO_CONCEPT_INSTANCES_KEY, fci);
				// orderedFlexoConceptInstances.add(fci);
				// System.out.println("Registered EPI " + epi + " in " + epi.getFlexoConcept());
				// System.out.println("Registered: " + getEPInstances(epi.getFlexoConcept()));

			}
		}

		/**
		 * Remove a {@link FlexoConceptInstance}
		 * 
		 * @param fci
		 * @return
		 */
		@Override
		public void removeFromFlexoConceptInstances(FlexoConceptInstance fci) {
			Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(fci.getFlexoConceptURI());
			if (hash == null) {
				hash = new Hashtable<Long, FlexoConceptInstance>();
				flexoConceptInstances.put(fci.getFlexoConceptURI(), hash);
			}
			hash.remove(fci.getFlexoID());
			performSuperRemover(FLEXO_CONCEPT_INSTANCES_KEY, fci);
			// orderedFlexoConceptInstances.remove(fci);
			// getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_INSTANCES_KEY, fci, null);
		}

		// Not required !!!
		/*@Override
		public List<FlexoConceptInstance> getFlexoConceptInstances() {
			return (List<FlexoConceptInstance>)performSuperGetter(FLEXO_CONCEPT_INSTANCES_KEY);
		}*/

		@Override
		public List<FlexoConceptInstance> getFlexoConceptInstances(String flexoConceptNameOrURI) {
			if (getVirtualModel() == null) {
				return Collections.emptyList();
			}
			FlexoConcept ep = getVirtualModel().getFlexoConcept(flexoConceptNameOrURI);
			return getFlexoConceptInstances(ep);
		}

		@Override
		public List<FlexoConceptInstance> getFlexoConceptInstances(FlexoConcept flexoConcept) {
			if (flexoConcept == null) {
				// logger.warning("Unexpected null FlexoConcept");
				return Collections.emptyList();
			}
			Map<Long, FlexoConceptInstance> hash = flexoConceptInstances.get(flexoConcept.getURI());
			if (hash == null) {
				hash = new Hashtable<Long, FlexoConceptInstance>();
				flexoConceptInstances.put(flexoConcept.getURI(), hash);
			}
			// TODO: performance issue here
			List<FlexoConceptInstance> returned = new ArrayList(hash.values());
			for (FlexoConcept childEP : flexoConcept.getChildFlexoConcepts()) {
				returned.addAll(getFlexoConceptInstances(childEP));
			}
			return returned;
		}

		// TODO: refactor this
		/*@Deprecated
		public List<FlexoConceptInstance> getEPInstancesWithPropertyEqualsTo(String epName, String epProperty, Object value) {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			Collection<FlexoConceptInstance> epis = getEPInstances(epName);
			for (FlexoConceptInstance epi : epis) {
				Object evaluate = epi.evaluate(epProperty);
				if (value == null && evaluate == value || value != null && value.equals(evaluate)) {
					returned.add(epi);
				}
			}
			return returned;
		}*/

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
		public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
				MS modelSlot) {
			for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
				if (msInstance.getModelSlot() == modelSlot) {
					return (ModelSlotInstance<MS, RD>) msInstance;
				}
			}
			if (modelSlot instanceof VirtualModelModelSlot && ((VirtualModelModelSlot) modelSlot).isReflexiveModelSlot()) {
				VirtualModelModelSlotInstance reflexiveModelSlotInstance = getResource().getFactory().newInstance(
						VirtualModelModelSlotInstance.class);
				reflexiveModelSlotInstance.setModelSlot((VirtualModelModelSlot) modelSlot);
				reflexiveModelSlotInstance.setAccessedResourceData(this);
				addToModelSlotInstances(reflexiveModelSlotInstance);
				return (ModelSlotInstance<MS, RD>) reflexiveModelSlotInstance;
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
