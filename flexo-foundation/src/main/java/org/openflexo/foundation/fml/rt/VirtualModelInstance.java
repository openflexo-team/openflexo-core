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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionType;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResourceImpl;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link VirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * 
 * As such, a {@link VirtualModelInstance} is instantiated inside a {@link View}, and all model slot defined for the corresponding
 * {@link ViewPoint} are instantiated (reified) with existing or build-in managed {@link FlexoModel}.<br>
 * 
 * A {@link VirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself an {@link FlexoConceptInstance}.
 * <br>
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
		FlexoModel<VirtualModelInstance, VirtualModel>, TechnologyObject<FMLRTTechnologyAdapter> {

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
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ModelSlotInstance<?, ?>> getModelSlotInstances();

	@Setter(MODEL_SLOT_INSTANCES_KEY)
	public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> modelSlotInstances);

	@Adder(MODEL_SLOT_INSTANCES_KEY)
	public void addToModelSlotInstances(ModelSlotInstance<?, ?> aModelSlotInstance);

	@Remover(MODEL_SLOT_INSTANCES_KEY)
	public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> aModelSlotInstance);

	@Getter(value = FLEXO_CONCEPT_INSTANCES_KEY, cardinality = Cardinality.LIST, inverse = ModelSlotInstance.VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoConceptInstance> getFlexoConceptInstances();

	@Setter(FLEXO_CONCEPT_INSTANCES_KEY)
	public void setFlexoConceptInstances(List<FlexoConceptInstance> someFlexoConceptInstances);

	@Adder(FLEXO_CONCEPT_INSTANCES_KEY)
	@PastingPoint
	public void addToFlexoConceptInstances(FlexoConceptInstance aFlexoConceptInstance);

	@Remover(FLEXO_CONCEPT_INSTANCES_KEY)
	public void removeFromFlexoConceptInstances(FlexoConceptInstance aFlexoConceptInstance);

	/**
	 * Called when supplied concept instance changed of FlexoConcept (mutation scheme)
	 * 
	 * @param fci
	 * @param oldFlexoConcept
	 * @param newFlexoConcept
	 */
	public void flexoConceptInstanceChangedFlexoConcept(FlexoConceptInstance fci, FlexoConcept oldFlexoConcept,
			FlexoConcept newFlexoConcept);

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
	// public Object getValueForVariable(BindingVariable variable);

	/**
	 * Return a newly created list of all {@link FlexoConceptInstance} conform to the FlexoConcept identified by supplied String parameter
	 * (this could be either the name or the uri of concept)
	 * 
	 * @param flexoConceptNameOrURI
	 * @return
	 */
	public List<FlexoConceptInstance> getFlexoConceptInstances(String flexoConceptNameOrURI);

	/**
	 * Return a new list of FlexoConcept, which are all concepts used in this VirtualModelInstance
	 * 
	 * @return
	 */
	public List<FlexoConcept> getUsedFlexoConcepts();

	/**
	 * Return a newly created list of all {@link FlexoConceptInstance} conform to the supplied FlexoConcept
	 * 
	 * @param flexoConcept
	 * @return
	 */
	public List<FlexoConceptInstance> getFlexoConceptInstances(FlexoConcept flexoConcept);

	public boolean hasNature(VirtualModelInstanceNature nature);

	/**
	 * Try to lookup supplied object in the whole VirtualModelInstance.<br>
	 * This means that each {@link FlexoConceptInstance} is checked for any of its roles to see if the reference is the supplied object
	 * 
	 * @param object
	 *            : the object to lookup
	 * @return
	 */
	public ObjectLookupResult lookup(Object object);

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

			VirtualModelInstanceResource newVirtualModelResource = VirtualModelInstanceResourceImpl
					.makeVirtualModelInstanceResource(virtualModelName, virtualModel, view);

			VirtualModelInstanceImpl newVirtualModelInstance = (VirtualModelInstanceImpl) newVirtualModelResource.getFactory()
					.newInstance(VirtualModelInstance.class);
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
			// We override here getFlexoConcept() to retrieve matching VirtualModel
			if (getView() != null && getView().getViewPoint() != null && flexoConcept == null && StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getView().getViewPoint().getVirtualModelNamed(flexoConceptURI);
			}
			return (VirtualModel) flexoConcept;
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
		public FMLRTTechnologyAdapter getTechnologyAdapter() {
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
			addToFlexoConceptInstances(returned);
			return returned;
		}

		/**
		 * Called when supplied concept instance changed of FlexoConcept (mutation scheme)
		 * 
		 * @param fci
		 * @param oldFlexoConcept
		 * @param newFlexoConcept
		 */
		@Override
		public void flexoConceptInstanceChangedFlexoConcept(FlexoConceptInstance fci, FlexoConcept oldFlexoConcept,
				FlexoConcept newFlexoConcept) {
			Map<Long, FlexoConceptInstance> hash = null;

			if (oldFlexoConcept != null) {
				hash = flexoConceptInstances.get(oldFlexoConcept.getURI());
				if (hash != null) {
					hash.remove(fci.getFlexoID());
				}
			}
			if (newFlexoConcept != null) {
				hash = flexoConceptInstances.get(newFlexoConcept.getURI());
				if (hash == null) {
					hash = new Hashtable<Long, FlexoConceptInstance>();
					flexoConceptInstances.put(fci.getFlexoConceptURI(), hash);
				}
				hash.put(fci.getFlexoID(), fci);
			}
		}

		/**
		 * Add a {@link FlexoConceptInstance}
		 * 
		 * @param fci
		 * @return
		 */
		@Override
		public void addToFlexoConceptInstances(FlexoConceptInstance fci) {

			// System.out.println("Already storing: ");
			// for (FlexoConceptInstance i : getFlexoConceptInstances()) {
			// System.out.println("> " + i);
			// }

			// System.out.println("Adding " + fci);
			// System.out.println("fci.getFlexoConcept() = " + fci.getFlexoConcept());
			// System.out.println("fci.getActors() = " + fci.getActors());

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
			// System.out.println("<<<<<<<<<<<<<< removeFromFlexoConceptInstances " + fci);
			// Thread.dumpStack();

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

		/**
		 * Return a new list of FlexoConcept, which are all concepts used in this VirtualModelInstance
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getUsedFlexoConcepts() {
			List<FlexoConcept> returned = new ArrayList<FlexoConcept>();
			for (String s : flexoConceptInstances.keySet()) {
				FlexoConcept c = getVirtualModel().getFlexoConcept(s);
				returned.add(c);
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
			/*if (modelSlot instanceof FMLRTModelSlot && ((FMLRTModelSlot) modelSlot).isReflexiveModelSlot()) {
				VirtualModelModelSlotInstance reflexiveModelSlotInstance = getResource().getFactory().newInstance(
						VirtualModelModelSlotInstance.class);
				reflexiveModelSlotInstance.setModelSlot((FMLRTModelSlot) modelSlot);
				reflexiveModelSlotInstance.setAccessedResourceData(this);
				addToModelSlotInstances(reflexiveModelSlotInstance);
				return (ModelSlotInstance<MS, RD>) reflexiveModelSlotInstance;
			}*/
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

		@Override
		public <T> T getFlexoActor(FlexoRole<T> flexoRole) {
			if (super.getFlexoActor(flexoRole) != null) {
				return super.getFlexoActor(flexoRole);
			}
			if (flexoRole instanceof ModelSlot) {
				ModelSlotInstance<?, ?> modelSlotInstance = getModelSlotInstance((ModelSlot<?>) flexoRole);
				if (modelSlotInstance != null) {
					return (T) modelSlotInstance.getAccessedResourceData();
				}
			}
			return null;
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
		public final boolean delete(Object... context) {

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
		/*@Override
		public Object getValueForVariable(BindingVariable variable) {
			logger.warning("Not implemented: getValueForVariable() " + variable);
			return null;
		}*/

		@Override
		public Object getObject(String objectURI) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public VirtualModelInstance getVirtualModelInstance() {
			return this;
		}

		/**
		 * Try to lookup supplied object in the whole VirtualModelInstance.<br>
		 * This means that each {@link FlexoConceptInstance} is checked for any of its roles to see if the reference is the supplied object
		 * 
		 * @param object
		 *            : the object to lookup
		 * @return
		 */
		@Override
		public ObjectLookupResult lookup(Object object) {
			// TODO: PERFS !!!
			// @brutal mode
			if (object == null)
				return null;
			for (FlexoConceptInstance flexoConceptInstance : getFlexoConceptInstances()) {
				if (flexoConceptInstance.getFlexoConcept() != null) {
					for (FlexoRole<?> fr : flexoConceptInstance.getFlexoConcept().getDeclaredProperties(FlexoRole.class)) {
						if (flexoConceptInstance.getFlexoActor(fr) == object) {
							ObjectLookupResult answer = new ObjectLookupResult();
							answer.flexoConceptInstance = flexoConceptInstance;
							answer.property = fr;
							return answer;
						}
					}
				}
			}
			return null;
		}

		@Override
		public Object getValue(BindingVariable variable) {

			if (variable instanceof ModelSlotBindingVariable && getVirtualModel() != null) {
				ModelSlot<?> ms = getVirtualModel().getModelSlot(variable.getVariableName());
				if (ms != null) {
					return getModelSlotInstance(ms).getAccessedResourceData();
				}
				logger.warning("Unexpected model slot " + variable);
				return null;
			} else if (variable.getVariableName().equals(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getVirtualModel();
			} else if (variable.getVariableName().equals(ViewPointBindingModel.VIEW_PROPERTY)) {
				return getView();
			} else if (variable.getVariableName().equals(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY)) {
				return this;
			}

			Object returned = super.getValue(variable);

			if (returned != null) {
				return returned;
			}

			// When not found, delegate it to the view
			if (returned == null && getView() != null) {
				return getView().getValue(variable);
			}

			logger.warning("Unexpected variable requested in VirtualModelInstance: " + variable + " of " + variable.getClass());
			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
			if (variable instanceof ModelSlotBindingVariable && getVirtualModel() != null) {
				ModelSlot<?> ms = getVirtualModel().getModelSlot(variable.getVariableName());
				if (ms != null) {
					if (value instanceof ResourceData) {
						((ModelSlotInstance) getModelSlotInstance(ms)).setAccessedResourceData((ResourceData) value);
					} else {
						logger.warning("Unexpected resource data " + value + " for model slot " + ms);
					}
				} else {
					logger.warning("Unexpected property " + variable);
				}
				return;
			} else if (variable.getVariableName().equals(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				logger.warning("Forbidden write access " + VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			} else if (variable.getVariableName().equals(ViewPointBindingModel.VIEW_PROPERTY)) {
				logger.warning("Forbidden write access " + ViewPointBindingModel.VIEW_PROPERTY + " in " + this + " of " + getClass());
				return;
			} else if (variable.getVariableName().equals(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY)) {
				logger.warning("Forbidden write access " + VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			}

			super.setValue(value, variable);

		}

	}

	public class ObjectLookupResult {
		public FlexoConceptInstance flexoConceptInstance;
		public FlexoProperty<?> property;
	}

}
