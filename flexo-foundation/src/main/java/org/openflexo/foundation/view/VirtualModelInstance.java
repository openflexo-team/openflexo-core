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
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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

/**
 * A {@link VirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * 
 * As such, a {@link VirtualModelInstance} is instantiated inside a {@link View}, and all model slot defined for the corresponding
 * {@link ViewPoint} are instantiated (reified) with existing or build-in managed {@link FlexoModel}.<br>
 * 
 * A {@link VirtualModelInstance} mostly manages a collection of {@link EditionPatternInstance} and is itself an
 * {@link EditionPatternInstance}.<br>
 * 
 * A {@link VirtualModelInstance} might be used in the Design Space (for example to encode a Diagram)
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInstance extends EditionPatternInstance implements ResourceData<VirtualModelInstance>,
		FlexoModel<VirtualModelInstance, VirtualModel> {

	private static final Logger logger = Logger.getLogger(VirtualModelInstance.class.getPackage().getName());

	private VirtualModelInstanceResource resource;
	private List<ModelSlotInstance<?, ?>> modelSlotInstances;
	// private Map<ModelSlot, FlexoModel<?, ?>> modelsMap = new HashMap<ModelSlot, FlexoModel<?, ?>>(); // Do not serialize
	// this.
	private String title;

	private Hashtable<FlexoConcept, Map<Long, EditionPatternInstance>> flexoConceptInstances;

	public static VirtualModelInstanceResource newVirtualModelInstance(String virtualModelName, String virtualModelTitle,
			VirtualModel virtualModel, View view) throws SaveResourceException {

		VirtualModelInstanceResource newVirtualModelResource = VirtualModelInstanceResourceImpl.makeVirtualModelInstanceResource(
				virtualModelName, virtualModel, view);

		VirtualModelInstance newVirtualModelInstance = new VirtualModelInstance(view, virtualModel);
		newVirtualModelResource.setResourceData(newVirtualModelInstance);
		newVirtualModelInstance.setResource(newVirtualModelResource);
		newVirtualModelInstance.setTitle(virtualModelTitle);

		view.getResource().notifyContentsAdded(newVirtualModelResource);

		newVirtualModelResource.save(null);

		return newVirtualModelResource;
	}

	/**
	 * Default constructor with view, virtual model and resource
	 */
	public VirtualModelInstance(View view, VirtualModel virtualModel, VirtualModelInstanceResource resource) {
		this(view, virtualModel);
		setResource(resource);
	}

	/**
	 * Default constructor with view and virtual model
	 * 
	 */
	public VirtualModelInstance(View view, VirtualModel virtualModel) {
		super(virtualModel, null, view.getProject());
		logger.info("Created new VirtualModelInstance for virtual model " + virtualModel);
		modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();
		flexoConceptInstances = new Hashtable<FlexoConcept, Map<Long, EditionPatternInstance>>();
		view.addToVirtualModelInstances(this);
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

	public VirtualModel getVirtualModel() {
		return getFlexoConcept();
	}

	public String getVirtualModelURI() {
		return super.getEditionPatternURI();
	}

	public void setVirtualModelURI(String virtualModelURI) {
		super.setEditionPatternURI(virtualModelURI);
	}

	@Override
	public VirtualModel getMetaModel() {
		return getFlexoConcept();
	}

	@Override
	public TechnologyAdapter getTechnologyAdapter() {
		// TODO
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
	 * Instanciate and register a new {@link EditionPatternInstance}
	 * 
	 * @param pattern
	 * @return
	 */
	public EditionPatternInstance makeNewFlexoConceptInstance(FlexoConcept pattern) {
		EditionPatternInstance returned = new EditionPatternInstance(pattern, this, getProject());
		return registerEditionPatternInstance(returned);
	}

	/**
	 * Register an existing {@link EditionPatternInstance} (used in deserialization)
	 * 
	 * @param epi
	 * @return
	 */
	protected EditionPatternInstance registerEditionPatternInstance(EditionPatternInstance epi) {
		if (epi.getFlexoConcept() == null) {
			logger.warning("Could not register EditionPatternInstance with null FlexoConcept: " + epi);
			logger.warning("EPI: " + epi.debug());
		} else {
			Map<Long, EditionPatternInstance> hash = flexoConceptInstances.get(epi.getFlexoConcept());
			if (hash == null) {
				hash = new Hashtable<Long, EditionPatternInstance>();
				flexoConceptInstances.put(epi.getFlexoConcept(), hash);
			}
			hash.put(epi.getFlexoID(), epi);
			// System.out.println("Registered EPI " + epi + " in " + epi.getEditionPattern());
			// System.out.println("Registered: " + getEPInstances(epi.getEditionPattern()));
		}
		return epi;
	}

	/**
	 * Un-register an existing {@link EditionPatternInstance}
	 * 
	 * @param epi
	 * @return
	 */
	protected EditionPatternInstance unregisterEditionPatternInstance(EditionPatternInstance epi) {
		Map<Long, EditionPatternInstance> hash = flexoConceptInstances.get(epi.getFlexoConcept());
		if (hash == null) {
			hash = new Hashtable<Long, EditionPatternInstance>();
			flexoConceptInstances.put(epi.getFlexoConcept(), hash);
		}
		hash.remove(epi.getFlexoID());
		return epi;
	}

	// Do not use this since not efficient, used in deserialization only
	public List<EditionPatternInstance> getEditionPatternInstancesList() {
		List<EditionPatternInstance> returned = new ArrayList<EditionPatternInstance>();
		for (Map<Long, EditionPatternInstance> epMap : flexoConceptInstances.values()) {
			for (EditionPatternInstance epi : epMap.values()) {
				returned.add(epi);
			}
		}
		return returned;
	}

	public void setEditionPatternInstancesList(List<EditionPatternInstance> epiList) {
		for (EditionPatternInstance epi : epiList) {
			addToEditionPatternInstancesList(epi);
		}
	}

	public void addToEditionPatternInstancesList(EditionPatternInstance epi) {
		registerEditionPatternInstance(epi);
	}

	public void removeFromEditionPatternInstancesList(EditionPatternInstance epi) {
		unregisterEditionPatternInstance(epi);
	}

	public Hashtable<FlexoConcept, Map<Long, EditionPatternInstance>> getFlexoConceptInstances() {
		return flexoConceptInstances;
	}

	public void setFlexoConceptInstances(Hashtable<FlexoConcept, Map<Long, EditionPatternInstance>> flexoConceptInstances) {
		this.flexoConceptInstances = flexoConceptInstances;
	}

	// TODO: performance isssues
	public Collection<EditionPatternInstance> getAllEPInstances() {
		return getEditionPatternInstancesList();
	}

	public Collection<EditionPatternInstance> getEPInstances(String epName) {
		if (getVirtualModel() == null) {
			return Collections.emptyList();
		}
		FlexoConcept ep = getVirtualModel().getFlexoConcept(epName);
		return getEPInstances(ep);
	}

	public List<EditionPatternInstance> getEPInstances(FlexoConcept ep) {
		if (ep == null) {
			// logger.warning("Unexpected null FlexoConcept");
			return Collections.emptyList();
		}
		Map<Long, EditionPatternInstance> hash = flexoConceptInstances.get(ep);
		if (hash == null) {
			hash = new Hashtable<Long, EditionPatternInstance>();
			flexoConceptInstances.put(ep, hash);
		}
		// TODO: performance issue here
		List<EditionPatternInstance> returned = new ArrayList(hash.values());
		for (FlexoConcept childEP : ep.getChildFlexoConcepts()) {
			returned.addAll(getEPInstances(childEP));
		}
		return returned;
	}

	// TODO: refactor this
	@Deprecated
	public List<EditionPatternInstance> getEPInstancesWithPropertyEqualsTo(String epName, String epProperty, Object value) {
		/*List<EditionPatternInstance> returned = new ArrayList<EditionPatternInstance>();
		Collection<EditionPatternInstance> epis = getEPInstances(epName);
		for (EditionPatternInstance epi : epis) {
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

	public String getName() {
		if (getResource() != null) {
			return getResource().getName();
		}
		return null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		String oldTitle = this.title;
		if (requireChange(oldTitle, title)) {
			this.title = title;
			setChanged();
			notifyObservers(new VEDataModification("title", oldTitle, title));
		}
	}

	@Override
	public VirtualModelInstance getResourceData() {
		return this;
	}

	@Override
	public String toString() {
		return "VirtualModelInstance[name=" + getName() + "/virtualModel=" + getVirtualModel() + "/hash=" + Integer.toHexString(hashCode())
				+ "]";
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
	public <RD extends ResourceData<RD>> ModelSlotInstance<?, RD> getModelSlotInstance(ModelSlot<RD> modelSlot) {
		for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
			if (msInstance.getModelSlot() == modelSlot) {
				return (ModelSlotInstance<?, RD>) msInstance;
			}
		}
		if (modelSlot instanceof VirtualModelModelSlot && ((VirtualModelModelSlot) modelSlot).isReflexiveModelSlot()) {
			ModelSlotInstance reflexiveModelSlotInstance = new VirtualModelModelSlotInstance(this, (VirtualModelModelSlot) modelSlot);
			reflexiveModelSlotInstance.setAccessedResourceData(this);
			addToModelSlotInstances(reflexiveModelSlotInstance);
			return reflexiveModelSlotInstance;
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
	public <RD extends ResourceData<RD>> ModelSlotInstance<?, RD> getModelSlotInstance(String modelSlotName) {
		for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
			if (msInstance.getModelSlot().getName().equals(modelSlotName)) {
				return (ModelSlotInstance<?, RD>) msInstance;
			}
		}
		logger.warning("Cannot find ModelSlotInstance named " + modelSlotName);
		return null;
	}

	public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> instances) {
		this.modelSlotInstances = instances;
	}

	public List<ModelSlotInstance<?, ?>> getModelSlotInstances() {
		return modelSlotInstances;
	}

	public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> instance) {
		if (modelSlotInstances.contains(instance)) {
			instance.setVirtualModelInstance(null);
			modelSlotInstances.remove(instance);
			setChanged();
			notifyObservers(new VEDataModification("modelSlotInstances", instance, null));
		}
	}

	public void addToModelSlotInstances(ModelSlotInstance<?, ?> instance) {
		if (!modelSlotInstances.contains(instance)) {
			instance.setVirtualModelInstance(this);
			modelSlotInstances.add(instance);
			setChanged();
			notifyObservers(new VEDataModification("modelSlotInstances", null, instance));
		}
	}

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

	public boolean isSynchronizable() {
		return getVirtualModel() != null && getVirtualModel().hasSynchronizationScheme();
	}

	/**
	 * Return run-time value for {@link BindingVariable} variable
	 * 
	 * @param variable
	 * @return
	 */
	public Object getValueForVariable(BindingVariable variable) {
		return null;
	}

	@Override
	public Object getObject(String objectURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
