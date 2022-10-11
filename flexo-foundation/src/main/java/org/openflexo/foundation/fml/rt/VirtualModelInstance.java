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
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingValueChangeListener;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IndexableContainer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionFactory;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link VirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * A {@link VirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself a
 * {@link FlexoConceptInstance}.<br>
 * 
 * Note that this is a base implementation, common for FMLRTVirtualModelInstance (native implementation managed by the
 * {@link FMLRTTechnologyAdapter}) and InferedVirtualModelInstance (managed through a ModelSlot by a {@link TechnologyAdapter})<br>
 * 
 * @author sylvain
 * 
 * @param <VMI>
 *            Type of reflected {@link VirtualModelInstance}
 * @param <TA>
 *            TechnologyAdapter managing this {@link VirtualModelInstance}
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelInstance.VirtualModelInstanceImpl.class)
public interface VirtualModelInstance<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoConceptInstance, ResourceData<VMI>, TechnologyObject<TA> /*, FlexoModel<VMI, VirtualModel>, TechnologyObject<TA>*/,
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
	public static final String FINALIZE_FLEXO_CONCEPT_INSTANCE_ADDING_KEY = "finalizeFlexoConceptInstanceAdding";

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String CONTAINER_VIRTUAL_MODEL_INSTANCE_KEY = "containerVirtualModelInstance";
	@PropertyIdentifier(type = FMLRTVirtualModelInstance.class, cardinality = Cardinality.LIST)
	public static final String VIRTUAL_MODEL_INSTANCES_KEY = "virtualModelInstances";

	/**
	 * Returns URI for this {@link VirtualModelInstance}.<br>
	 * Note that if this {@link VirtualModelInstance} is contained in another {@link VirtualModelInstance}, URI is computed from URI of
	 * container VirtualModel
	 * 
	 * The convention for URI are following:
	 * <container_virtual_model_instance_uri>/<virtual_model_instance_name >#<flexo_concept_instance_id> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyVirtuaModelInstance1/MyVirtualModelInstance2#ID
	 * 
	 * @return String representing unique URI of this object
	 */
	// @Override
	public String getURI();

	/**
	 * Sets URI for this {@link VirtualModelInstance}<br>
	 * Note that if this {@link VirtualModelInstance} is contained in another {@link VirtualModelInstance}, this method will be unefficient
	 * 
	 * @param anURI
	 */
	public void setURI(String anURI);

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
	 * Return all {@link FlexoConceptInstance} defined in this {@link FMLRTVirtualModelInstance} which have no container (contaiment
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
	@PastingPoint(priority = 0)
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
	 * Build a new FlexoConceptInstance<br>
	 * Just instantiate, do not register yet
	 * 
	 * @return
	 */
	public FlexoConceptInstance buildNewFlexoConceptInstance(FlexoConcept concept);

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
	 * Return a new list of FlexoConcept, which are all concepts used in this FMLRTVirtualModelInstance
	 * 
	 * @return
	 */
	public List<FlexoConcept> getUsedFlexoConcepts();

	/**
	 * Return a new list of FlexoConcept, which are all top-level concepts used in this FMLRTVirtualModelInstance
	 * 
	 * @return
	 */
	public List<FlexoConcept> getUsedTopLevelFlexoConcepts();

	/**
	 * Return a newly created list of all {@link FlexoConceptInstance} conform to the supplied FlexoConcept
	 * 
	 * @param flexoConcept
	 * @return
	 */
	public List<FlexoConceptInstance> getFlexoConceptInstances(FlexoConcept flexoConcept);

	public boolean hasNature(AbstractVirtualModelInstanceNature<VMI, TA> nature);

	/**
	 * Try to lookup supplied object in the whole FMLRTVirtualModelInstance.<br>
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
	 * Delete all instances of this {@link FMLRTVirtualModelInstance}
	 */
	public void clear();

	public void contentsAdded(FlexoConceptInstance objectBeeingAdded, FlexoConcept concept);

	public void contentsRemoved(FlexoConceptInstance objectBeeingRemoved, FlexoConcept concept);

	/**
	 * Return (eventually null) container {@link VirtualModelInstance}
	 * 
	 * @return
	 */
	@Getter(value = CONTAINER_VIRTUAL_MODEL_INSTANCE_KEY, ignoreType = true)
	public VirtualModelInstance<?, ?> getContainerVirtualModelInstance();

	/**
	 * Sets (eventually null) container {@link VirtualModelInstance}
	 * 
	 * @return
	 */
	@Setter(CONTAINER_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setContainerVirtualModelInstance(VirtualModelInstance<?, ?> containerVMI);

	/**
	 * Return all {@link FMLRTVirtualModelInstance} defined in this {@link VirtualModelInstance}
	 * 
	 * @return
	 */
	@Getter(
			value = VIRTUAL_MODEL_INSTANCES_KEY,
			cardinality = Cardinality.LIST,
			inverse = VirtualModelInstance.CONTAINER_VIRTUAL_MODEL_INSTANCE_KEY,
			ignoreType = true)
	public List<VirtualModelInstance<?, ?>> getVirtualModelInstances();

	/**
	 * Allow to retrieve VMIs given a virtual model.
	 * 
	 * @param virtualModel
	 *            key to find correct VMI
	 * @return the list
	 */
	public List<VirtualModelInstance<?, ?>> getVirtualModelInstancesForVirtualModel(VirtualModel virtualModel);

	@Setter(VIRTUAL_MODEL_INSTANCES_KEY)
	public void setVirtualModelInstances(List<VirtualModelInstance<?, ?>> virtualModelInstances);

	@Adder(VIRTUAL_MODEL_INSTANCES_KEY)
	public void addToVirtualModelInstances(VirtualModelInstance<?, ?> virtualModelInstance);

	@Remover(VIRTUAL_MODEL_INSTANCES_KEY)
	public void removeFromVirtualModelInstances(VirtualModelInstance<?, ?> virtualModelInstance);

	public VirtualModelInstance<?, ?> getVirtualModelInstance(String name);

	public boolean isValidVirtualModelInstanceName(String virtualModelName);

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link VirtualModelInstance}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters();

	public FMLRTVirtualModelInstanceRepository<?> getVirtualModelInstanceRepository();

	/**
	 * Asynchronously execute firing of 'allRootFlexoConceptInstances' change event All notifications are merged and executed in EDT
	 */
	public void notifyAllRootFlexoConceptInstancesMayHaveChanged();

	@Override
	public FlexoResource<VMI> getResource();

	@Override
	public void setResource(FlexoResource<VMI> resource);

	@Override
	public TA getTechnologyAdapter();

	/**
	 * Clone this {@link FlexoConceptInstance} given supplied factory.<br>
	 * Clone is computed using roles, where property values are kept references<br>
	 * Inside {@link FlexoConceptInstance} are cloned using same semantics
	 * 
	 * @param factory
	 * @return
	 */
	@Override
	public VirtualModelInstance<VMI, TA> cloneUsingRoles(AbstractVirtualModelInstanceModelFactory<?> factory);

	public void setLocalServiceManager(FlexoServiceManager localServiceManager);

	public Class<VMI> getInferedImplementedInterface();

	/**
	 * Base implementation for VirtualModelInstance
	 * 
	 * @author sylvain
	 *
	 * @param <VMI>
	 * @param <TA>
	 */
	public static abstract class VirtualModelInstanceImpl<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoConceptInstanceImpl implements VirtualModelInstance<VMI, TA> {

		private static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstance.class.getPackage().getName());

		private AbstractVirtualModelInstanceResource<VMI, TA> resource;
		private String title;

		/**
		 * This map stores the concept instances as they are declared relatively to their final type (the unique FlexoConcept whose
		 * FlexoConceptInstance is instance)
		 */
		private final Map<FlexoConcept, List<FlexoConceptInstance>> flexoConceptInstances;

		private List<FlexoConceptInstance> rootFlexoConceptInstances = new ArrayList<>();

		private Map<Type, Map<String, FlexoConceptInstanceIndex<?>>> indexes = new WeakHashMap<>();

		/**
		 * Default constructor with
		 */
		public VirtualModelInstanceImpl() {
			super();
			flexoConceptInstances = new Hashtable<>();
		}

		private FlexoServiceManager localServiceManager = null;

		@Override
		public Class<VMI> getInferedImplementedInterface() {
			return (Class<VMI>) TypeUtils.getTypeArgument(getClass(), VirtualModelInstance.class, 0);
		}

		@Override
		public Class<?> getImplementedInterface() {
			Class<?> returned = super.getImplementedInterface();
			if (returned == getClass()) {
				return getInferedImplementedInterface();
			}
			return returned;
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			FlexoServiceManager returned = super.getServiceManager();
			if (returned != null) {
				return returned;
			}
			return localServiceManager;
		}

		@Override
		public void setLocalServiceManager(FlexoServiceManager localServiceManager) {
			this.localServiceManager = localServiceManager;
		}

		/**
		 * Overrides identifier for this VirtualModelInstance under the form 'ConceptName'+ID+':name'
		 * 
		 * @return
		 */
		@Override
		public String getUserFriendlyIdentifier() {
			return (getFlexoConcept() != null ? getFlexoConcept().getName() : "?VirtualModelInstance?") + getFlexoID() + ":" + getName();
		}

		@Override
		public String getStringRepresentation() {
			return getName();
		}

		@Override
		public AbstractVirtualModelInstanceModelFactory<?> getFactory() {
			if (getVirtualModelInstanceResource() != null) {
				return getVirtualModelInstanceResource().getFactory();
			}
			return super.getFactory();
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
		}

		@Override
		public final boolean hasNature(AbstractVirtualModelInstanceNature<VMI, TA> nature) {
			return nature.hasNature((VMI) this);
		}

		/**
		 * Returns URI for this {@link VirtualModelInstance}.<br>
		 * Note that if this {@link VirtualModelInstance} is contained in another {@link VirtualModelInstance}, URI is computed from URI of
		 * container VirtualModel
		 * 
		 * The convention for URI are following:
		 * <container_virtual_model_instance_uri>/<virtual_model_instance_name >#<flexo_concept_instance_id> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModelInstance1/MyVirtualModelInstance2#ID
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getContainerVirtualModelInstance() != null) {
				return getContainerVirtualModelInstance().getURI() + "/" + getName()
						+ FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX;
			}
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		/**
		 * Sets URI for this {@link VirtualModelInstance}<br>
		 * Note that if this {@link VirtualModelInstance} is contained in another {@link VirtualModelInstance}, this method will be
		 * unefficient
		 * 
		 * @param anURI
		 */
		@Override
		public void setURI(String anURI) {
			if (getContainerVirtualModelInstance() == null) {
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
		public VirtualModelInstance<?, ?> getContainerVirtualModelInstance() {
			if (getVirtualModelInstanceResource() != null && getVirtualModelInstanceResource().getContainer() != null) {
				return getVirtualModelInstanceResource().getContainer().getVirtualModelInstance();
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
			if (flexoConcept == null && getVirtualModelInstanceResource() != null) {
				// We can sometimes arrive here: flexoConcept is still not set
				// But we have another chance to retrieve the VirtualModel while requesting it
				// to the resource
				// Then set the FlexoConcept
				flexoConcept = getVirtualModelInstanceResource().getVirtualModel();
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

		/*@Override
		public VirtualModel getMetaModel() {
			return getFlexoConcept();
		}*/

		@Override
		public FlexoVersion getModelVersion() {
			if (getVirtualModelInstanceResource() != null) {
				return getVirtualModelInstanceResource().getModelVersion();
			}
			return null;
		}

		@Override
		public void setModelVersion(FlexoVersion aVersion) {
			if (getVirtualModelInstanceResource() != null) {
				getVirtualModelInstanceResource().setModelVersion(aVersion);
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
		 * Return all {@link FlexoConceptInstance} defined in this {@link FMLRTVirtualModelInstance} which have no container (containment
		 * semantics)<br>
		 * (where container is the virtual model instance itself)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConceptInstance> getAllRootFlexoConceptInstances() {

			return rootFlexoConceptInstances;
		}

		private List<FlexoConceptInstance> lastNotifiedRootFlexoConceptInstances = null;
		private boolean willNotifyAllRootFlexoConceptInstancesMayHaveChanged = false;

		/**
		 * Asynchronously execute firing of 'allRootFlexoConceptInstances' change event All notifications are merged and executed in EDT
		 */
		@Override
		public void notifyAllRootFlexoConceptInstancesMayHaveChanged() {
			if (willNotifyAllRootFlexoConceptInstancesMayHaveChanged) {
				return;
			}
			willNotifyAllRootFlexoConceptInstancesMayHaveChanged = true;
			SwingUtilities.invokeLater(() -> {
				if (!isDeleted()) {
					getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances",
							(lastNotifiedRootFlexoConceptInstances != null ? new ArrayList<>(lastNotifiedRootFlexoConceptInstances) : null),
							new ArrayList<>(getAllRootFlexoConceptInstances()));
					lastNotifiedRootFlexoConceptInstances = new ArrayList<>(getAllRootFlexoConceptInstances());
					willNotifyAllRootFlexoConceptInstancesMayHaveChanged = false;
				}
			});
		}

		/**
		 * Instanciate and register a new {@link FlexoConceptInstance}
		 * 
		 * @param pattern
		 * @return
		 */
		@Override
		public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept) {

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

			FlexoConceptInstance returned = buildNewFlexoConceptInstance(concept);
			if (container != null && container != this) {
				container.addToEmbeddedFlexoConceptInstances(returned);
			}
			addToFlexoConceptInstances(returned);
			return returned;
		}

		/**
		 * Build a new FlexoConceptInstance<br>
		 * Just instantiate, do not register yet
		 * 
		 * @return
		 */
		@Override
		public FlexoConceptInstance buildNewFlexoConceptInstance(FlexoConcept concept) {
			FlexoConceptInstance returned = getVirtualModelInstanceResource().getFactory().newInstance(FlexoConceptInstance.class);
			returned.setFlexoConcept(concept);
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

			FlexoEventInstance returned = getVirtualModelInstanceResource().getFactory().newInstance(FlexoEventInstance.class);
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

			// long time1 = 0, time2 = 0, time3 = 0, time4 = 0;

			// long start = System.currentTimeMillis();

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

				// time1 = System.currentTimeMillis();

				ensureRegisterFCIInConcept(fci, fci.getFlexoConcept());

			}

			// time2 = System.currentTimeMillis();

			performSuperAdder(FLEXO_CONCEPT_INSTANCES_KEY, fci);

			// time3 = System.currentTimeMillis();

			notifyAllRootFlexoConceptInstancesMayHaveChanged();
			// getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances",
			// false, true);

			// long end = System.currentTimeMillis();

			// System.out.println("Adding instance took " + (end - start) + " ms start=" +
			// start + " t1=" + time1 + " t2=" + time2 + " t3="
			// + time3 + " end=" + end);

			// We notify this now, because the inverse property wasn't set during adding
			// notifying
			// So we renotify it now
			getPropertyChangeSupport().firePropertyChange(FINALIZE_FLEXO_CONCEPT_INSTANCE_ADDING_KEY, null, fci);
		}

		/**
		 * Force re-index all FCI relatively to their {@link FlexoConcept}<br>
		 * Use this method with caution, since it is really time costly
		 */
		@Override
		public void reindexAllConceptInstances() {
			rootFlexoConceptInstances.clear();
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
			if (fci.isRoot()) {
				// Prevent when ensureRegisterFCIInConcept method is invoked twice, in case an
				// instance has a parent.
				if (!rootFlexoConceptInstances.contains(fci)) {
					rootFlexoConceptInstances.add(fci);
				}
			}

			if (concept == null) {
				logger.warning("ensureRegisterFCIInConcept called for FCI without concept: " + fci);
				return;
			}

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

			// remove inconditionnaly from roots
			// if (fci.isRoot()) {
			rootFlexoConceptInstances.remove(fci);
			// }

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

		private void ensureUnregisterFCI(FlexoConceptInstance fci) {
			if (rootFlexoConceptInstances.contains(fci)) {
				rootFlexoConceptInstances.remove(fci);
			}

			for (FlexoConcept concept : flexoConceptInstances.keySet()) {
				List<FlexoConceptInstance> list = flexoConceptInstances.get(concept);
				if (list != null && list.contains(fci)) {
					list.remove(fci);
					contentsRemoved(fci, concept);
				}
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

			if (fci.getFlexoConcept() == null) {
				// Special computing
				ensureUnregisterFCI(fci);
				// logger.warning("Could not remove FlexoConceptInstance with null FlexoConcept:
				// " + fci);
			}
			else {
				ensureUnregisterFCIFromConcept(fci, fci.getFlexoConcept());
			}

			performSuperRemover(FLEXO_CONCEPT_INSTANCES_KEY, fci);

			notifyAllRootFlexoConceptInstancesMayHaveChanged();
			// getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances",
			// false, true);
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
				/*
				 * System.out.println("Bizarre, pourtant j'ai ca: "); for (FlexoConceptInstance
				 * fci : getFlexoConceptInstances()) { System.out.println(" > " + fci); } for
				 * (FlexoConcept concept : flexoConceptInstances.keySet()) {
				 * System.out.println("Key: " + concept + " list: " +
				 * flexoConceptInstances.get(concept)); }
				 */
				return Collections.emptyList();
			}
			return returned;
		}

		/**
		 * Return a new list of FlexoConcept, which are all concepts used in this FMLRTVirtualModelInstance
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

		/**
		 * Return a new list of FlexoConcept, which are all concepts used in this FMLRTVirtualModelInstance
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getUsedTopLevelFlexoConcepts() {
			List<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept concept : flexoConceptInstances.keySet()) {
				if (concept.getApplicableContainerFlexoConcept() == null) {
					returned.add(concept);
				}
			}
			return returned;
		}

		public final AbstractVirtualModelInstanceResource<VMI, TA> getVirtualModelInstanceResource() {
			return resource;
		}

		@Override
		public final FlexoResource<VMI>/*AbstractVirtualModelInstanceResource<VMI, TA>*/ getResource() {
			return resource;
		}

		@Override
		public final void setResource(FlexoResource<VMI>/*AbstractVirtualModelInstanceResource<VMI, TA>*/ resource) {
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

		// TODO: this implemenation should disappear
		@Override
		public <T> T getFlexoActor(FlexoRole<T> flexoRole) {
			if (flexoRole instanceof ModelSlot) {
				ModelSlotInstance<?, ?> modelSlotInstance = getModelSlotInstance((ModelSlot) flexoRole);
				if (modelSlotInstance != null) {
					return (T) modelSlotInstance.getAccessedResourceData();
				}
			}
			if (super.getFlexoActor(flexoRole) != null) {
				return super.getFlexoActor(flexoRole);
			}
			return null;
		}

		// ==========================================================================
		// ================================= Delete ===============================
		// ==========================================================================

		@Override
		public final boolean delete(Object... context) {

			logger.info("Deleting virtual model instance ");

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
				SynchronizationSchemeActionFactory actionType = new SynchronizationSchemeActionFactory(ss, this);
				SynchronizationSchemeAction action = actionType.makeNewAction(this, null, editor);
				action.doAction();
			}
			else {
				logger.warning("No synchronization scheme defined for " + getVirtualModel());
			}
		}

		@Override
		public boolean isSynchronizable() {
			// is synchronizable if virtualModel is not null, it has SynchronizationScheme
			// and all needed TA are activated
			VirtualModel vm = getVirtualModel();
			boolean synchronizable = vm != null && vm.hasSynchronizationScheme();
			if (synchronizable) {
				for (TechnologyAdapter<?> neededTA : vm.getCompilationUnit().getRequiredTechnologyAdapters()) {
					synchronizable = synchronizable && neededTA.isActivated();
				}
			}
			return synchronizable;
		}

		/*@Override
		public Object getObject(String objectURI) {
			return null;
		}*/

		@Override
		public VirtualModelInstance<?, ?> getVirtualModelInstance() {
			return this;
		}

		/**
		 * Try to lookup supplied object in the whole FMLRTVirtualModelInstance.<br>
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
					for (FlexoRole<?> fr : flexoConceptInstance.getFlexoConcept().getAccessibleProperties(FlexoRole.class)) {
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

			if (variable == null) {
				return null;
			}
			if (variable.getVariableName().equals(FlexoConceptBindingModel.CONTAINER_PROPERTY_NAME) && getVirtualModel() != null
					&& getVirtualModel().getContainerVirtualModel() != null) {
				return getContainerVirtualModelInstance();
			}

			Object returned = super.getValue(variable);

			if (returned != null) {
				return returned;
			}

			// When not found, delegate it to the container virtual model instance
			if (getContainerVirtualModelInstance() != null && getContainerVirtualModelInstance() != this) {
				return getContainerVirtualModelInstance().getValue(variable);
			}

			// Warning is not required, since it will will warn each time a value is null !
			// logger.warning("Unexpected variable requested in FMLRTVirtualModelInstance: "
			// + variable + " of " + variable.getClass());
			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {

			super.setValue(value, variable);

		}

		public class FlexoConceptInstanceIndex<T> extends WeakHashMap<T, List<FlexoConceptInstance>> {

			public class IndexedValueListener extends BindingValueChangeListener<T> {

				private FlexoConceptInstance fci;
				private T currentValue;

				public IndexedValueListener(FlexoConceptInstance fci) {
					super(indexableTerm, new BindingEvaluationContext() {
						@Override
						public ExpressionEvaluator getEvaluator() {
							return new FMLExpressionEvaluator(this);
						}

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
					} catch (ReflectiveOperationException e) {
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
				/*
				 * for (List<FlexoConceptInstance> l : values()) {
				 * l.remove(objectBeeingRemoved); }
				 */
			}
		}

		@Override
		public FlexoConceptInstanceIndex getIndex(Type type, DataBinding<?> indexableTerm) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Retrieving index for " + type + " and " + indexableTerm);
			}

			/*
			 * System.out.println("Retrieving index for " + type + " and " + indexableTerm);
			 * 
			 * System.out.println("Les FCI par type:");
			 * 
			 * for (FlexoConcept c : flexoConceptInstances.keySet()) {
			 * System.out.println("Concept " + c); for (FlexoConceptInstance fci :
			 * flexoConceptInstances.get(c)) { System.out.println(" > " + fci); } }
			 * 
			 * System.out.println("Les indexes:"); for (Type t : indexes.keySet()) {
			 * System.out.println("Index for " + t); Map<String, FlexoConceptInstanceIndex>
			 * map = indexes.get(t); for (String s : map.keySet()) {
			 * System.out.println(" > " + s + " : " + map.get(s)); } }
			 */

			if (type instanceof FlexoConceptInstanceType) {
				Map<String, FlexoConceptInstanceIndex<?>> mapForType = indexes.get(type);
				if (mapForType == null) {
					mapForType = new WeakHashMap<>();
					indexes.put(type, mapForType);
				}
				FlexoConceptInstanceIndex<?> returned = mapForType.get(indexableTerm.toString());
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
			Map<String, FlexoConceptInstanceIndex<?>> mapForType = indexes.get(type);
			if (mapForType != null) {
				for (FlexoConceptInstanceIndex<?> index : mapForType.values()) {
					index.contentsAdded(objectBeeingAdded);
				}
			}
		}

		@Override
		public void contentsRemoved(FlexoConceptInstance objectBeeingRemoved, FlexoConcept concept) {
			FlexoConceptInstanceType type = concept.getInstanceType();
			Map<String, FlexoConceptInstanceIndex<?>> mapForType = indexes.get(type);
			if (mapForType != null) {
				for (FlexoConceptInstanceIndex<?> index : mapForType.values()) {
					index.contentsRemoved(objectBeeingRemoved);
				}
			}
		}

		/**
		 * Delete all instances of this {@link FMLRTVirtualModelInstance}
		 */
		@Override
		public void clear() {
			for (FlexoConceptInstance fci : new ArrayList<>(getFlexoConceptInstances())) {
				fci.delete();
			}
		}

		@Override
		public List<VirtualModelInstance<?, ?>> getVirtualModelInstances() {
			if (getResource() != null && !getResource().isDeserializing()) {
				loadVirtualModelInstancesWhenUnloaded();
			}
			return (List<VirtualModelInstance<?, ?>>) performSuperGetter(VIRTUAL_MODEL_INSTANCES_KEY);
		}

		@Override
		public List<VirtualModelInstance<?, ?>> getVirtualModelInstancesForVirtualModel(final VirtualModel virtualModel) {
			List<VirtualModelInstance<?, ?>> returned = new ArrayList<>();
			for (VirtualModelInstance<?, ?> vmi : getVirtualModelInstances()) {
				if (virtualModel.isAssignableFrom(vmi.getVirtualModel())) {
					returned.add(vmi);
				}
			}
			return returned;
		}

		/**
		 * Load eventually unloaded VirtualModelInstances<br>
		 * After this call return, we can assert that all {@link FMLRTVirtualModelInstance} are loaded.
		 */
		private void loadVirtualModelInstancesWhenUnloaded() {
			for (org.openflexo.foundation.resource.FlexoResource<?> r : getResource().getContents()) {
				if (r instanceof AbstractVirtualModelInstanceResource) {
					((AbstractVirtualModelInstanceResource<?, ?>) r).getVirtualModelInstance();
				}
			}
		}

		@Override
		public VirtualModelInstance<?, ?> getVirtualModelInstance(String name) {
			for (VirtualModelInstance<?, ?> vmi : getVirtualModelInstances()) {
				String lName = vmi.getName();
				if (lName != null) {
					if (vmi.getName().equals(name)) {
						return vmi;
					}
				}
				else {
					logger.warning("Name of VirtualModel is null: " + this.toString());
				}
			}
			return null;
		}

		@Override
		public boolean isValidVirtualModelInstanceName(String virtualModelName) {
			return getVirtualModelInstance(virtualModelName) == null;
		}

		/**
		 * Return the list of {@link TechnologyAdapter} used in the context of this {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Override
		public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
			if (getVirtualModel() != null) {
				List<TechnologyAdapter> returned = getVirtualModel().getCompilationUnit().getRequiredTechnologyAdapters();
				if (!returned.contains(getTechnologyAdapter())) {
					returned.add(getTechnologyAdapter());
				}
				return returned;
			}
			return Collections.singletonList((TechnologyAdapter) getTechnologyAdapter());
		}

		@Override
		public void addToVirtualModelInstances(VirtualModelInstance<?, ?> virtualModelInstance) {
			performSuperAdder(VIRTUAL_MODEL_INSTANCES_KEY, virtualModelInstance);
			// We notify now all properties from container
			if (getVirtualModel() != null) {
				for (FlexoProperty<?> property : getVirtualModel().getAccessibleProperties()) {
					virtualModelInstance.getPropertyChangeSupport().firePropertyChange(property.getName(), null,
							getFlexoPropertyValue(property));
				}
			}
		}

		@Override
		public void removeFromVirtualModelInstances(VirtualModelInstance<?, ?> virtualModelInstance) {
			performSuperRemover(VIRTUAL_MODEL_INSTANCES_KEY, virtualModelInstance);
			// We notify now all properties from container
			if (getVirtualModel() != null) {
				for (FlexoProperty<?> property : getVirtualModel().getAccessibleProperties()) {
					virtualModelInstance.getPropertyChangeSupport().firePropertyChange(property.getName(), new Object(), null);
				}
			}
		}

		@Override
		public String render() {
			StringBuffer sb = new StringBuffer();
			String line = StringUtils.buildString('-', 80) + "\n";
			sb.append(line);
			sb.append("VirtualModelInstance : " + getUserFriendlyIdentifier() + "\n");
			if (hasValidRenderer()) {
				sb.append("Renderer             : " + getStringRepresentation() + "\n");
			}
			sb.append("VirtualModel         : " + getVirtualModel().getName() + "\n");
			sb.append("Instances            : " + getFlexoConceptInstances().size() + "\n");
			sb.append(line);
			List<FlexoRole> roles = getFlexoConcept().getAccessibleProperties(FlexoRole.class);
			if (roles.size() > 0) {
				for (FlexoRole<?> role : roles) {
					if (role.getFlexoConcept().isAssignableFrom(getVirtualModel())) {
						sb.append(role.getName() + " = " + getFlexoPropertyValue(role) + "\n");
					}
				}
			}
			else {
				sb.append("No values" + "\n");
			}
			sb.append(line);
			if (getAllRootFlexoConceptInstances().size() > 0) {
				for (FlexoConceptInstance child : getAllRootFlexoConceptInstances()) {
					appendFCI(child, sb, 0);
				}
			}
			else {
				sb.append("No contents" + "\n");
			}
			sb.append(line);
			return sb.toString();
		}

		/**
		 * Clone this {@link VirtualModelInstance} given supplied factory.<br>
		 * Clone is computed using roles, where property values are kept references<br>
		 * Inside {@link FlexoConceptInstance} are cloned using same semantics
		 * 
		 * @param factory
		 * @return
		 */
		@Override
		public VirtualModelInstance<VMI, TA> cloneUsingRoles(AbstractVirtualModelInstanceModelFactory<?> factory) {

			VirtualModelInstance<VMI, TA> clone = (VirtualModelInstance<VMI, TA>) factory.newInstance(getImplementedInterface());
			clone.setVirtualModel(getVirtualModel());
			clone.setLocalFactory(factory);
			clone.setLocalServiceManager(getServiceManager());
			for (FlexoRole flexoRole : getVirtualModel().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				clone.setFlexoPropertyValue(flexoRole, value);
			}
			for (FlexoConceptInstance fci : getFlexoConceptInstances()) {
				FlexoConceptInstance clonedFCI = fci.cloneUsingRoles(factory);
				clone.addToFlexoConceptInstances(clonedFCI);
			}
			return clone;
		}

		/**
		 * An {@link #equals(Object)} implementation for {@link FlexoConceptInstance}, focused on roles
		 * 
		 * @param obj
		 * @return
		 */
		@Override
		public boolean equalsUsingRoles(Object obj) {

			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof VirtualModelInstance)) {
				return false;
			}
			VirtualModelInstance<?, ?> other = (VirtualModelInstance<?, ?>) obj;
			if (getVirtualModel() != other.getVirtualModel()) {
				return false;
			}
			for (FlexoRole<?> flexoRole : getFlexoConcept().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				Object otherValue = other.getFlexoPropertyValue(flexoRole);
				if (value == null) {
					if (otherValue != null) {
						return false;
					}
				}
				else if (!value.equals(otherValue)) {
					return false;
				}
			}

			ListIterator<FlexoConceptInstance> e1 = getFlexoConceptInstances().listIterator();
			ListIterator<FlexoConceptInstance> e2 = other.getFlexoConceptInstances().listIterator();

			while (e1.hasNext() && e2.hasNext()) {
				FlexoConceptInstance o1 = e1.next();
				FlexoConceptInstance o2 = e2.next();
				if (!(o1 == null ? o2 == null : o1.equalsUsingRoles(o2))) {
					return false;
				}
			}
			if (e1.hasNext() || e2.hasNext()) {
				return false;
			}

			return true;
		}

		/**
		 * A {@link #hashCode()} implementation for {@link FlexoConceptInstance}, focused on roles
		 * 
		 * @return
		 */
		@Override
		public int hashCodeUsingRoles() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getVirtualModel() == null) ? 0 : getVirtualModel().hashCode());
			for (FlexoRole<?> flexoRole : getFlexoConcept().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				result = prime * result + ((value == null) ? 0 : value.hashCode());
			}
			for (FlexoConceptInstance flexoConceptInstance : getFlexoConceptInstances()) {
				result = prime * result + ((flexoConceptInstance == null) ? 0 : flexoConceptInstance.hashCodeUsingRoles());
			}
			return result;
		}

	}

	public class ObjectLookupResult {
		public FlexoConceptInstance flexoConceptInstance;
		public FlexoProperty<?> property;
	}

}
