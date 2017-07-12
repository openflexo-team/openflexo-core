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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingValueChangeListener;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.IndexableContainer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionType;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
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
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link AbstractVirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * A {@link AbstractVirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself a
 * {@link FlexoConceptInstance}.<br>
 * 
 * Note that this is a base implementation, common for FMLRTVirtualModelInstance (native implementation managed by the
 * {@link FMLRTTechnologyAdapter}) and InferedVirtualModelInstance (managed through a ModelSlot by a {@link TechnologyAdapter})<br>
 * 
 * @author sylvain
 * 
 * @param <VMI>
 * @param <TA>
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelInstance.VirtualModelInstanceImpl.class)
@Imports({ @Import(VirtualModelInstance.class), @Import(View.class) })
public interface AbstractVirtualModelInstance<VMI extends AbstractVirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
		extends FlexoConceptInstance, ResourceData<VMI>, FlexoModel<VMI, VirtualModel>, TechnologyObject<TA>,
		IndexableContainer<FlexoConceptInstance> {

	public static final String EVENT_FIRED = "EventFired";

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String TITLE_KEY = "title";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";
	@PropertyIdentifier(type = FlexoConceptInstance.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_CONCEPT_INSTANCES_KEY = "flexoConceptInstances";

	@Getter(value = TITLE_KEY)
	@XMLAttribute
	public String getTitle();

	@Setter(TITLE_KEY)
	public void setTitle(String title);

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

	public VirtualModel getVirtualModel();

	public void setVirtualModel(VirtualModel virtualModel);

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute
	public String getVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setVirtualModelURI(String virtualModelURI);

	/**
	 * Return all {@link FlexoConceptInstance} defined in this {@link VirtualModelInstance} which have no container (contaiment
	 * semantics)<br>
	 * (where container is the virtual model instance itself)
	 * 
	 * @return
	 */
	public List<FlexoConceptInstance> getAllRootFlexoConceptInstances();

	@Getter(
			value = FLEXO_CONCEPT_INSTANCES_KEY,
			cardinality = Cardinality.LIST,
			inverse = FlexoConceptInstance.OWNING_VIRTUAL_MODEL_INSTANCE_KEY)
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

	@Getter(NAME_KEY)
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	/**
	 * Instantiate and register a new {@link FlexoConceptInstance}
	 * 
	 * @param pattern
	 * @return
	 */
	public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept);

	/**
	 * Instantiate and register a new {@link FlexoConceptInstance} in a container FlexoConceptInstance
	 * 
	 * @param pattern
	 * @return
	 */
	public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept, FlexoConceptInstance container);

	/**
	 * Instanciate and fire a new {@link FlexoConceptInstance} as a Flexo event
	 * 
	 * @param pattern
	 * @return
	 */
	public FlexoEventInstance makeNewEvent(FlexoEvent event);

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

	public boolean hasNature(AbstractVirtualModelInstanceNature<VMI, TA> nature);

	/**
	 * Try to lookup supplied object in the whole VirtualModelInstance.<br>
	 * This means that each {@link FlexoConceptInstance} is checked for any of its roles to see if the reference is the supplied object
	 * 
	 * @param object
	 *            : the object to lookup
	 * @return
	 */
	public ObjectLookupResult lookup(Object object);

	/**
	 * Force re-index all FCI relatively to their {@link FlexoConcept}<br>
	 * Use this method with caution, since it is really time costly
	 */
	public void reindexAllConceptInstances();

	/**
	 * Delete all instances of this {@link VirtualModelInstance}
	 */
	public void clear();

	public void contentsAdded(FlexoConceptInstance objectBeeingAdded, FlexoConcept concept);

	public void contentsRemoved(FlexoConceptInstance objectBeeingRemoved, FlexoConcept concept);

	/**
	 * Return (eventually null) container {@link AbstractVirtualModelInstance}
	 * 
	 * @return
	 */
	public AbstractVirtualModelInstance<?, ?> getContainerVirtualModelInstance();

	/**
	 * Base implementation for AbstractVirtualModelInstance
	 * 
	 * @author sylvain
	 *
	 * @param <VMI>
	 * @param <TA>
	 */
	public static abstract class AbstractVirtualModelInstanceImpl<VMI extends AbstractVirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
			extends FlexoConceptInstanceImpl implements AbstractVirtualModelInstance<VMI, TA> {

		private static final Logger logger = Logger.getLogger(VirtualModelInstance.class.getPackage().getName());

		private AbstractVirtualModelInstanceResource<VMI, TA> resource;
		private String title;

		/**
		 * This map stores the concept instances as they are declared relatively to their final type (the unique FlexoConcept whose
		 * FlexoConceptInstance is instance)
		 */
		private final Map<FlexoConcept, List<FlexoConceptInstance>> flexoConceptInstances;

		private Map<Type, Map<String, FlexoConceptInstanceIndex>> indexes = new WeakHashMap<>();

		/**
		 * Default constructor with
		 */
		public AbstractVirtualModelInstanceImpl() {
			super();
			// modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();
			flexoConceptInstances = new Hashtable<>();
		}

		@Override
		public String getStringRepresentation() {
			return getName();
		}

		@Override
		public AbstractVirtualModelInstanceModelFactory<?> getFactory() {
			if (getResource() != null) {
				return getResource().getFactory();
			}
			return null;
		}

		/*@Override
		public void initializeDeserialization(AbstractVirtualModelInstanceModelFactory<?> factory) {
			super.initializeDeserialization(factory);
		}*/

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
		}

		@Override
		public final boolean hasNature(AbstractVirtualModelInstanceNature<VMI, TA> nature) {
			return nature.hasNature((VMI) this);
		}

		@Override
		public String getURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		@Override
		public AbstractVirtualModelInstance<?, ?> getContainerVirtualModelInstance() {
			if (getResource() != null && getResource().getContainer() != null) {
				return getResource().getContainer().getVirtualModelInstance();
			}
			return null;
		}

		@Override
		public VirtualModel getFlexoConcept() {
			// We override here getFlexoConcept() to retrieve matching VirtualModel
			if (getContainerVirtualModelInstance() != null && getContainerVirtualModelInstance().getVirtualModel() != null
					&& flexoConcept == null && StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getContainerVirtualModelInstance().getVirtualModel().getVirtualModelNamed(flexoConceptURI);
			}
			return (VirtualModel) flexoConcept;
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

		/**
		 * Return all {@link FlexoConceptInstance} defined in this {@link VirtualModelInstance} which have no container (containment
		 * semantics)<br>
		 * (where container is the virtual model instance itself)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConceptInstance> getAllRootFlexoConceptInstances() {

			ArrayList<FlexoConceptInstance> returned = new ArrayList<>();
			for (FlexoConceptInstance fci : getFlexoConceptInstances()) {
				if (fci.isRoot()) {
					returned.add(fci);
				}
			}
			return returned;
		}

		/**
		 * Instanciate and register a new {@link FlexoConceptInstance}
		 * 
		 * @param pattern
		 * @return
		 */
		@Override
		public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept) {

			/*FlexoConceptInstance returned = getResource().getFactory().newInstance(FlexoConceptInstance.class);
			returned.setFlexoConcept(concept);
			addToFlexoConceptInstances(returned);
			return returned;*/
			return makeNewFlexoConceptInstance(concept, null);
		}

		/**
		 * Instantiate and register a new {@link FlexoConceptInstance} in a container FlexoConceptInstance
		 * 
		 * @param pattern
		 * @return
		 */
		@Override
		public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept, FlexoConceptInstance container) {

			FlexoConceptInstance returned = getResource().getFactory().newInstance(FlexoConceptInstance.class);
			returned.setFlexoConcept(concept);
			if (container != null) {
				container.addToEmbeddedFlexoConceptInstances(returned);
			}
			addToFlexoConceptInstances(returned);
			return returned;
		}

		/**
		 * Instanciate and fire a new {@link FlexoEventInstance} as a Flexo event
		 * 
		 * @param pattern
		 * @return
		 */
		@Override
		public FlexoEventInstance makeNewEvent(FlexoEvent event) {

			FlexoEventInstance returned = getResource().getFactory().newInstance(FlexoEventInstance.class);
			returned.setFlexoConcept(event);
			returned.setSourceVirtualModelInstance(this);

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
			List<FlexoConceptInstance> list = null;

			if (oldFlexoConcept != null) {
				list = flexoConceptInstances.get(oldFlexoConcept);
				if (list != null) {
					list.remove(fci);
				}
			}
			if (newFlexoConcept != null) {
				list = flexoConceptInstances.get(newFlexoConcept);
				if (list == null) {
					list = new ArrayList<>();
					flexoConceptInstances.put(newFlexoConcept, list);
				}
				list.add(fci);
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

			if (fci.getFlexoConcept() == null) {
				if (!isDeserializing()) {
					logger.warning("Could not register FlexoConceptInstance with null FlexoConcept: " + fci);
				}
			}
			else {
				// We store here the FCI twice:
				// - first in list hash map
				// - then in an ordered list (internally performed by PAMELA)
				// We rely on PAMELA schemes to handle notifications

				ensureRegisterFCIInConcept(fci, fci.getFlexoConcept());

			}

			performSuperAdder(FLEXO_CONCEPT_INSTANCES_KEY, fci);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false, true);
		}

		/**
		 * Force re-index all FCI relatively to their {@link FlexoConcept}<br>
		 * Use this method with caution, since it is really time costly
		 */
		@Override
		public void reindexAllConceptInstances() {
			for (FlexoConcept concept : flexoConceptInstances.keySet()) {
				List<FlexoConceptInstance> l = flexoConceptInstances.get(concept);
				if (l != null) {
					l.clear();
				}
			}
			flexoConceptInstances.clear();
			for (FlexoConceptInstance fci : getFlexoConceptInstances()) {
				ensureRegisterFCIInConcept(fci, fci.getFlexoConcept());
			}
		}

		private void ensureRegisterFCIInConcept(FlexoConceptInstance fci, FlexoConcept concept) {

			// System.out.println("**** ensure register FCI " + fci + " in " + concept);

			List<FlexoConceptInstance> list = flexoConceptInstances.get(concept);
			if (list == null) {
				list = new ArrayList<>();
				flexoConceptInstances.put(concept, list);
			}
			if (!list.contains(fci)) {
				list.add(fci);
			}
			for (FlexoConcept parentConcept : concept.getParentFlexoConcepts()) {
				if (parentConcept != concept) { // In case of loops
					ensureRegisterFCIInConcept(fci, parentConcept);
				}
			}

			// Update indexes when relevant
			contentsAdded(fci, concept);
		}

		private void ensureUnregisterFCIFromConcept(FlexoConceptInstance fci, FlexoConcept concept) {
			List<FlexoConceptInstance> list = flexoConceptInstances.get(concept);
			if (list != null && list.contains(fci)) {
				list.remove(fci);
			}
			for (FlexoConcept parentConcept : concept.getParentFlexoConcepts()) {
				if (parentConcept != concept) { // In case of loops
					ensureUnregisterFCIFromConcept(fci, parentConcept);
				}
			}

			// Update indexes when relevant
			contentsRemoved(fci, concept);

		}

		/**
		 * Remove a {@link FlexoConceptInstance}
		 * 
		 * @param fci
		 * @return
		 */
		@Override
		public void removeFromFlexoConceptInstances(FlexoConceptInstance fci) {

			if (fci.getFlexoConcept() == null) {
				logger.warning("Could not remove FlexoConceptInstance with null FlexoConcept: " + fci);
			}
			else {
				ensureUnregisterFCIFromConcept(fci, fci.getFlexoConcept());
			}

			performSuperRemover(FLEXO_CONCEPT_INSTANCES_KEY, fci);

			getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false, true);
		}

		@Override
		public List<FlexoConceptInstance> getFlexoConceptInstances(String flexoConceptNameOrURI) {
			if (getVirtualModel() == null) {
				return Collections.emptyList();
			}
			FlexoConcept flexoConcept = getVirtualModel().getFlexoConcept(flexoConceptNameOrURI);
			return getFlexoConceptInstances(flexoConcept);
		}

		@Override
		public List<FlexoConceptInstance> getFlexoConceptInstances(FlexoConcept flexoConcept) {

			if (flexoConcept == null) {
				// logger.warning("Unexpected null FlexoConcept");
				return Collections.emptyList();
			}

			if (flexoConcept == getVirtualModel()) {
				return Collections.singletonList((FlexoConceptInstance) this);
			}

			List<FlexoConceptInstance> returned = flexoConceptInstances.get(flexoConcept);

			if (returned == null) {
				/*System.out.println("Bizarre, pourtant j'ai ca: ");
				for (FlexoConceptInstance fci : getFlexoConceptInstances()) {
					System.out.println(" > " + fci);
				}
				for (FlexoConcept concept : flexoConceptInstances.keySet()) {
					System.out.println("Key: " + concept + " list: " + flexoConceptInstances.get(concept));
				}*/
				return Collections.emptyList();
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
			List<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept concept : flexoConceptInstances.keySet()) {
				returned.add(concept);
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
		public AbstractVirtualModelInstanceResource<VMI, TA> getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<VMI> resource) {
			this.resource = (AbstractVirtualModelInstanceResource<VMI, TA>) resource;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return null;
		}

		@Override
		public void setName(String name) {
			if (getResource() != null) {
				try {
					getResource().setName(name);
				} catch (CannotRenameException e) {
					e.printStackTrace();
				}
			}
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
		public VMI getResourceData() {
			return (VMI) this;
		}

		@Override
		public <T> T getFlexoActor(FlexoRole<T> flexoRole) {
			if (super.getFlexoActor(flexoRole) != null) {
				return super.getFlexoActor(flexoRole);
			}
			if (flexoRole instanceof ModelSlot) {
				ModelSlotInstance<?, ?> modelSlotInstance = getModelSlotInstance((ModelSlot) flexoRole);
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
		/*@Deprecated
		public Set<FlexoMetaModel> getAllMetaModels() {
			Set<FlexoMetaModel> allMetaModels = new HashSet<>();
			for (ModelSlotInstance<?, ?> instance : getModelSlotInstances()) {
				if (instance.getModelSlot() instanceof TypeAwareModelSlot
						&& ((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource() != null) {
					allMetaModels.add(((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource().getMetaModelData());
				}
			}
			return allMetaModels;
		}*/

		/**
		 * Return a set of all models (load them when unloaded) used in this {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		/*@Deprecated
		public Set<FlexoModel<?, ?>> getAllModels() {
			Set<FlexoModel<?, ?>> allModels = new HashSet<>();
			for (ModelSlotInstance<?, ?> instance : getModelSlotInstances()) {
				if (instance.getResourceData() instanceof FlexoModel) {
					allModels.add(instance.getResourceData());
				}
			}
			return allModels;
		}*/

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
			}
			else {
				logger.warning("No synchronization scheme defined for " + getVirtualModel());
			}
		}

		@Override
		public boolean isSynchronizable() {
			// is synchronizable if virtualModel is not null, it has SynchronizationScheme and all needed TA are activated
			VirtualModel vm = getVirtualModel();
			boolean synchronizable = vm != null && vm.hasSynchronizationScheme();
			if (synchronizable) {
				for (TechnologyAdapter neededTA : vm.getRequiredTechnologyAdapters()) {
					synchronizable = synchronizable && neededTA.isActivated();
				}
			}
			return synchronizable;
		}

		@Override
		public Object getObject(String objectURI) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance() {
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
			/*if (variable instanceof ModelSlotBindingVariable && getVirtualModel() != null
					&& ((ModelSlotBindingVariable) variable).getFlexoProperty().getFlexoConcept() == getVirtualModel()) {
				ModelSlot ms = getVirtualModel().getModelSlot(variable.getVariableName());
				if (ms != null) {
					ModelSlotInstance<?, ?> modelSlotInstance = getModelSlotInstance(ms);
					if (modelSlotInstance != null) {
						return modelSlotInstance.getAccessedResourceData();
					}
					else {
						logger.warning("Unexpected null model slot instance for " + variable + " in " + getURI());
					}
				}
				logger.warning("Unexpected model slot " + variable);
				return null;
			}
			else*/

			/*if (variable.getVariableName().equals(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getVirtualModel();
			}
			else if (variable.getVariableName().equals(ViewPointBindingModel.VIEW_PROPERTY)) {
				return getView();
			}
			else*/ if (variable.getVariableName().equals(VirtualModelBindingModel.PROJECT_PROPERTY)) {
				return getResourceCenter();
			}
			else if (variable.getVariableName().equals(VirtualModelBindingModel.RC_PROPERTY)) {
				return getResourceCenter();
			}
			/*else if (variable.getVariableName().equals(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY)) {
				return this;
			}*/

			Object returned = super.getValue(variable);

			if (returned != null) {
				return returned;
			}

			// When not found, delegate it to the view
			if (returned == null && getContainerVirtualModelInstance() != null && getContainerVirtualModelInstance() != this) {
				return getContainerVirtualModelInstance().getValue(variable);
			}

			// Warning is not required, since it will will warn each time a value is null !
			// logger.warning("Unexpected variable requested in VirtualModelInstance: " + variable + " of " + variable.getClass());
			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
			/*if (variable instanceof ModelSlotBindingVariable && getVirtualModel() != null) {
				ModelSlot ms = getVirtualModel().getModelSlot(variable.getVariableName());
				if (ms != null) {
					if (value instanceof TechnologyAdapterResource) {
						ModelSlotInstance msi = (getModelSlotInstance(ms));
						if (msi == null) {
							AbstractVirtualModelInstance<?, ?> flexoConceptInstance = (AbstractVirtualModelInstance<?, ?>) getFlexoConceptInstance();
							ModelSlotInstanceConfiguration<?, ?> msiConfiguration = ms.createConfiguration(flexoConceptInstance,
									getResourceCenter());
							msiConfiguration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingResource);
							msi = msiConfiguration.createModelSlotInstance(flexoConceptInstance, getView());
							msi.setVirtualModelInstance(flexoConceptInstance);
							flexoConceptInstance.addToActors(msi);
						}
						msi.setResource((TechnologyAdapterResource) value);
					}
					if (value instanceof ResourceData) {
						ModelSlotInstance msi = (getModelSlotInstance(ms));
						if (msi == null) {
							AbstractVirtualModelInstance<?, ?> flexoConceptInstance = (AbstractVirtualModelInstance<?, ?>) getFlexoConceptInstance();
							ModelSlotInstanceConfiguration<?, ?> msiConfiguration = ms.createConfiguration(flexoConceptInstance,
									getResourceCenter());
							msiConfiguration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingResource);
							msi = msiConfiguration.createModelSlotInstance(flexoConceptInstance, getView());
							msi.setVirtualModelInstance(flexoConceptInstance);
							flexoConceptInstance.addToActors(msi);
						}
						msi.setAccessedResourceData((ResourceData) value);
					}
					else {
						logger.warning("Unexpected resource data " + value + " for model slot " + ms);
					}
				}
				else {
					logger.warning("Unexpected property " + variable);
				}
				return;
			}
			else if (variable.getVariableName().equals(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				logger.warning("Forbidden write access " + VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			}
			else if (variable.getVariableName().equals(ViewPointBindingModel.VIEW_PROPERTY)) {
				logger.warning("Forbidden write access " + ViewPointBindingModel.VIEW_PROPERTY + " in " + this + " of " + getClass());
				return;
			}
			else if (variable.getVariableName().equals(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY)) {
				logger.warning("Forbidden write access " + VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			}*/

			super.setValue(value, variable);

		}

		public class FlexoConceptInstanceIndex<T> extends WeakHashMap<T, List<FlexoConceptInstance>> {

			public class IndexedValueListener extends BindingValueChangeListener<T> {

				private FlexoConceptInstance fci;
				private T currentValue;

				public IndexedValueListener(FlexoConceptInstance fci) {
					super(indexableTerm, new BindingEvaluationContext() {
						@Override
						public Object getValue(BindingVariable variable) {
							if (variable.getVariableName().equals(FetchRequestCondition.SELECTED)) {
								return fci;
							}
							return fci.getValue(variable);
						}
					});
					this.fci = fci;
					try {
						currentValue = indexableTerm.getBindingValue(getContext());
					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

					if (logger.isLoggable(Level.FINE)) {
						logger.fine("For " + fci + " value=" + currentValue);
					}
				}

				public T getCurrentValue() {
					return currentValue;
				}

				@Override
				public void bindingValueChanged(Object source, T newValue) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("******** For FCI " + fci + " evaluated value of " + indexableTerm + " changed from " + currentValue
								+ " to " + newValue);
					}
					List<FlexoConceptInstance> oldList = FlexoConceptInstanceIndex.this.get(currentValue);
					if (oldList != null) {
						oldList.remove(fci);
					}
					List<FlexoConceptInstance> newList = FlexoConceptInstanceIndex.this.get(newValue);
					if (newList == null) {
						newList = new ArrayList<>();
						FlexoConceptInstanceIndex.this.put(newValue, newList);
					}

					newList.add(fci);
				}

			}

			private FlexoConceptInstanceType type;
			private DataBinding<T> indexableTerm;

			private boolean needsReindex = true;

			private WeakHashMap<FlexoConceptInstance, IndexedValueListener> listeners = new WeakHashMap<>();

			public FlexoConceptInstanceIndex(FlexoConceptInstanceType type, DataBinding<T> indexableTerm) {
				this.type = type;
				this.indexableTerm = indexableTerm;
			}

			public void updateWhenRequired() {
				if (needsReindex) {
					updateIndex();
				}
			}

			private void updateIndex() {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Computing index for " + this + " type=" + type + " with indexable term " + indexableTerm);
				}
				for (BindingValueChangeListener<T> l : listeners.values()) {
					l.stopObserving();
					l.delete();
				}
				listeners.clear();
				clear();
				for (FlexoConceptInstance fci : getFlexoConceptInstances(type.getFlexoConcept())) {
					indexFlexoConceptInstance(fci);
				}
				needsReindex = false;
			}

			private void indexFlexoConceptInstance(FlexoConceptInstance fci) {

				IndexedValueListener l = new IndexedValueListener(fci);
				listeners.put(fci, l);

				List<FlexoConceptInstance> fciList = get(l.getCurrentValue());
				if (fciList == null) {
					fciList = new ArrayList<>();
					put(l.getCurrentValue(), fciList);
				}

				fciList.add(fci);
			}

			public void contentsAdded(FlexoConceptInstance objectBeeingAdded) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("contentsAdded() for " + objectBeeingAdded + " indexableTerm=" + indexableTerm);
				}
				needsReindex = true;
			}

			public void contentsRemoved(FlexoConceptInstance objectBeeingRemoved) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("contentsRemoved() for " + objectBeeingRemoved + " indexableTerm=" + indexableTerm);
				}
				needsReindex = true;
				/*for (List<FlexoConceptInstance> l : values()) {
					l.remove(objectBeeingRemoved);
				}*/
			}
		}

		@Override
		public FlexoConceptInstanceIndex getIndex(Type type, DataBinding<?> indexableTerm) {

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Retrieving index for " + type + " and " + indexableTerm);
			}

			/*System.out.println("Retrieving index for " + type + " and " + indexableTerm);
			
			System.out.println("Les FCI par type:");
			
			for (FlexoConcept c : flexoConceptInstances.keySet()) {
				System.out.println("Concept " + c);
				for (FlexoConceptInstance fci : flexoConceptInstances.get(c)) {
					System.out.println(" > " + fci);
				}
			}
			
			System.out.println("Les indexes:");
			for (Type t : indexes.keySet()) {
				System.out.println("Index for " + t);
				Map<String, FlexoConceptInstanceIndex> map = indexes.get(t);
				for (String s : map.keySet()) {
					System.out.println(" > " + s + " : " + map.get(s));
				}
			}*/

			if (type instanceof FlexoConceptInstanceType) {
				Map<String, FlexoConceptInstanceIndex> mapForType = indexes.get(type);
				if (mapForType == null) {
					mapForType = new WeakHashMap<>();
					indexes.put(type, mapForType);
				}
				FlexoConceptInstanceIndex returned = mapForType.get(indexableTerm.toString());
				if (returned == null) {
					returned = makeIndex((FlexoConceptInstanceType) type, indexableTerm);
					mapForType.put(indexableTerm.toString(), returned);
				}
				// Now compute index when required
				returned.updateWhenRequired();

				// System.out.println("return " + returned);

				return returned;
			}
			return null;
		}

		public <T> FlexoConceptInstanceIndex<T> makeIndex(FlexoConceptInstanceType type, DataBinding<T> indexableTerm) {
			return new FlexoConceptInstanceIndex<>(type, indexableTerm);
		}

		@Override
		public void contentsAdded(FlexoConceptInstance objectBeeingAdded) {
			contentsAdded(objectBeeingAdded, objectBeeingAdded.getFlexoConcept());
		}

		@Override
		public void contentsRemoved(FlexoConceptInstance objectBeeingAdded) {
			contentsRemoved(objectBeeingAdded, objectBeeingAdded.getFlexoConcept());
		}

		@Override
		public void contentsAdded(FlexoConceptInstance objectBeeingAdded, FlexoConcept concept) {
			FlexoConceptInstanceType type = concept.getInstanceType();
			Map<String, FlexoConceptInstanceIndex> mapForType = indexes.get(type);
			if (mapForType != null) {
				for (FlexoConceptInstanceIndex index : mapForType.values()) {
					index.contentsAdded(objectBeeingAdded);
				}
			}
		}

		@Override
		public void contentsRemoved(FlexoConceptInstance objectBeeingRemoved, FlexoConcept concept) {
			FlexoConceptInstanceType type = concept.getInstanceType();
			Map<String, FlexoConceptInstanceIndex> mapForType = indexes.get(type);
			if (mapForType != null) {
				for (FlexoConceptInstanceIndex index : mapForType.values()) {
					index.contentsRemoved(objectBeeingRemoved);
				}
			}
		}

		/**
		 * Delete all instances of this {@link VirtualModelInstance}
		 */
		@Override
		public void clear() {
			for (FlexoConceptInstance fci : new ArrayList<>(getFlexoConceptInstances())) {
				fci.delete();
			}
		}

	}

	public class ObjectLookupResult {
		public FlexoConceptInstance flexoConceptInstance;
		public FlexoProperty<?> property;
	}

}
