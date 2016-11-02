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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.binding.ModelSlotBindingVariable;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration.DefaultModelSlotInstanceConfigurationOption;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionType;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
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
 * A {@link AbstractVirtualModelInstance} is the run-time concept (instance) of a {@link AbstractVirtualModel}.<br>
 * 
 * As such, a {@link AbstractVirtualModelInstance} is instantiated inside a {@link View}, and all model slot defined for the corresponding
 * {@link ViewPoint} are instantiated (reified) with existing or build-in managed {@link FlexoModel}.<br>
 * 
 * A {@link AbstractVirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself an
 * {@link FlexoConceptInstance}. <br>
 * 
 * A {@link AbstractVirtualModelInstance} is the common abstraction of {@link VirtualModelInstance} and {@link View}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractVirtualModelInstance.AbstractVirtualModelInstanceImpl.class)
@Imports({ @Import(VirtualModelInstance.class), @Import(View.class) })
public interface AbstractVirtualModelInstance<VMI extends AbstractVirtualModelInstance<VMI, VM>, VM extends AbstractVirtualModel<VM>>
		extends FlexoConceptInstance, ResourceData<VMI>, FlexoModel<VMI, VM>, TechnologyObject<FMLRTTechnologyAdapter> {

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
	@PropertyIdentifier(type = List.class)
	public static final String MODEL_SLOT_INSTANCES_KEY = "modelSlotInstances";
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

	public VM getVirtualModel();

	public void setVirtualModel(VM virtualModel);

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

	@Setter(NAME_KEY)
	public void setName(String name);

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

	public boolean hasNature(AbstractVirtualModelInstanceNature<VMI, VM> nature);

	/**
	 * Try to lookup supplied object in the whole VirtualModelInstance.<br>
	 * This means that each {@link FlexoConceptInstance} is checked for any of its roles to see if the reference is the supplied object
	 * 
	 * @param object
	 *            : the object to lookup
	 * @return
	 */
	public ObjectLookupResult lookup(Object object);

	public static abstract class AbstractVirtualModelInstanceImpl<VMI extends AbstractVirtualModelInstance<VMI, VM>, VM extends AbstractVirtualModel<VM>>
			extends FlexoConceptInstanceImpl implements AbstractVirtualModelInstance<VMI, VM> {

		private static final Logger logger = Logger.getLogger(AbstractVirtualModelInstance.class.getPackage().getName());

		private AbstractVirtualModelInstanceResource<VMI, VM> resource;
		private String title;

		private final Hashtable<String, List<FlexoConceptInstance>> flexoConceptInstances;

		// private final List<FlexoConceptInstance> orderedFlexoConceptInstances;

		/**
		 * Default constructor with
		 */
		public AbstractVirtualModelInstanceImpl() {
			super();
			// modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();
			flexoConceptInstances = new Hashtable<String, List<FlexoConceptInstance>>();
			// orderedFlexoConceptInstances = new ArrayList<FlexoConceptInstance>();
		}

		@Override
		public AbstractVirtualModelInstanceModelFactory<?> getFactory() {
			if (getResource() != null) {
				return getResource().getFactory();
			}
			return null;
		}

		@Override
		public final boolean hasNature(AbstractVirtualModelInstanceNature<VMI, VM> nature) {
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
		public View getView() {
			if (getResource() != null && getResource().getContainer() != null) {
				return getResource().getContainer().getView();
			}
			return null;
		}

		@Override
		public VM getFlexoConcept() {
			// We override here getFlexoConcept() to retrieve matching VirtualModel
			if (getView() != null && getView().getViewPoint() != null && flexoConcept == null && StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getView().getViewPoint().getVirtualModelNamed(flexoConceptURI);
			}
			return (VM) flexoConcept;
		}

		public ViewPoint getViewPoint() {
			if (getVirtualModel() != null) {
				return getVirtualModel().getViewPoint();
			}
			return null;
		}

		@Override
		public VM getVirtualModel() {
			return getFlexoConcept();
		}

		@Override
		public void setVirtualModel(VM virtualModel) {
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
		public VM getMetaModel() {
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
		public FMLRTTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
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
				list = flexoConceptInstances.get(oldFlexoConcept.getURI());
				if (list != null) {
					list.remove(fci);
				}
			}
			if (newFlexoConcept != null) {
				list = flexoConceptInstances.get(newFlexoConcept.getURI());
				if (list == null) {
					list = new ArrayList<FlexoConceptInstance>();
					flexoConceptInstances.put(newFlexoConcept.getURI(), list);
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

			/*System.out.println("Already storing: ");
			for (FlexoConceptInstance i : getFlexoConceptInstances()) {
				System.out.println("> " + i);
			}*/

			// System.out.println("Adding " + fci);
			// System.out.println("fci.getFlexoConcept() = " + fci.getFlexoConcept());
			// System.out.println("fci.getActors() = " + fci.getActors());

			if (fci.getFlexoConceptURI() == null) {
				logger.warning("Could not register FlexoConceptInstance with null FlexoConceptURI: " + fci);
				// logger.warning("EPI: " + fci.debug());
			}
			else {
				List<FlexoConceptInstance> list = flexoConceptInstances.get(fci.getFlexoConceptURI());
				if (list == null) {
					list = new ArrayList<FlexoConceptInstance>();
					flexoConceptInstances.put(fci.getFlexoConceptURI(), list);
				}
				// We store here the FCI twice:
				// - first in list hash map
				// - then in an ordered list (internally performed by PAMELA)
				// We rely on PAMELA schemes to handle notifications
				list.add(fci);

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

			List<FlexoConceptInstance> list = flexoConceptInstances.get(fci.getFlexoConceptURI());
			if (list == null) {
				list = new ArrayList<FlexoConceptInstance>();
				flexoConceptInstances.put(fci.getFlexoConceptURI(), list);
			}
			list.remove(fci);
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
			if (flexoConcept == getVirtualModel()) {
				return Collections.singletonList((FlexoConceptInstance) this);
			}
			List<FlexoConceptInstance> list = flexoConceptInstances.get(flexoConcept.getURI());
			if (list == null) {
				list = new ArrayList<FlexoConceptInstance>();
				flexoConceptInstances.put(flexoConcept.getURI(), list);
			}
			// TODO: performance issue here
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>(list);
			for (FlexoConcept childEP : flexoConcept.getChildFlexoConcepts()) {
				returned.addAll(getFlexoConceptInstances(childEP));
			}
			// TODO: performance issue here
			// We attempt to return the FCI in the order they are registered in the VMI
			Collections.sort(returned, new Comparator<FlexoConceptInstance>() {
				@Override
				public int compare(FlexoConceptInstance o1, FlexoConceptInstance o2) {
					return getFlexoConceptInstances().indexOf(o1) - getFlexoConceptInstances().indexOf(o2);
				}
			});
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
		public AbstractVirtualModelInstanceResource<VMI, VM> getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<VMI> resource) {
			this.resource = (AbstractVirtualModelInstanceResource<VMI, VM>) resource;
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
		public AbstractVirtualModelInstance<VMI, VM> getResourceData() {
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
		 * Return a set of all meta models (load them when unloaded) used in this {@link AbstractVirtualModelInstance}
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoMetaModel> getAllMetaModels() {
			Set<FlexoMetaModel> allMetaModels = new HashSet<FlexoMetaModel>();
			for (ModelSlotInstance<?, ?> instance : getModelSlotInstances()) {
				if (instance.getModelSlot() instanceof TypeAwareModelSlot
						&& ((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource() != null) {
					allMetaModels.add(((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource().getMetaModelData());
				}
			}
			return allMetaModels;
		}

		/**
		 * Return a set of all models (load them when unloaded) used in this {@link AbstractVirtualModelInstance}
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoModel<?, ?>> getAllModels() {
			Set<FlexoModel<?, ?>> allModels = new HashSet<FlexoModel<?, ?>>();
			for (ModelSlotInstance<?, ?> instance : getModelSlotInstances()) {
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
				VM vm = getVirtualModel();
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
			VM vm = getVirtualModel();
			boolean synchronizable = vm != null && vm.hasSynchronizationScheme();
			for (TechnologyAdapter neededTA : vm.getRequiredTechnologyAdapters()) {
				synchronizable = synchronizable && neededTA.isActivated();
			}
			return synchronizable;
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

			if (variable instanceof ModelSlotBindingVariable && getVirtualModel() != null
					&& ((ModelSlotBindingVariable) variable).getFlexoProperty().getFlexoConcept() == getVirtualModel()) {
				ModelSlot<?> ms = getVirtualModel().getModelSlot(variable.getVariableName());
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
			else if (variable.getVariableName().equals(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getVirtualModel();
			}
			else if (variable.getVariableName().equals(ViewPointBindingModel.VIEW_PROPERTY)) {
				return getView();
			}
			else if (variable.getVariableName().equals(ViewPointBindingModel.PROJECT_PROPERTY)) {
				return getResourceCenter();
			}
			else if (variable.getVariableName().equals(ViewPointBindingModel.RC_PROPERTY)) {
				return getResourceCenter();
			}
			else if (variable.getVariableName().equals(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY)) {
				return this;
			}

			Object returned = super.getValue(variable);

			if (returned != null) {
				return returned;
			}

			// When not found, delegate it to the view
			if (returned == null && getView() != null && getView() != this) {
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
					if (value instanceof TechnologyAdapterResource) {
						ModelSlotInstance msi = (getModelSlotInstance(ms));
						if (msi == null) {
							AbstractVirtualModelInstance<?, ?> flexoConceptInstance = (AbstractVirtualModelInstance<?, ?>) getFlexoConceptInstance();
							ModelSlotInstanceConfiguration<?, ?> msiConfiguration = ms.createConfiguration(flexoConceptInstance,
									getResourceCenter());
							msiConfiguration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingResource);
							msi = msiConfiguration.createModelSlotInstance(flexoConceptInstance, getView());
							msi.setVirtualModelInstance(flexoConceptInstance);
							flexoConceptInstance.addToModelSlotInstances(msi);
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
							flexoConceptInstance.addToModelSlotInstances(msi);
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
			}

			super.setValue(value, variable);

		}

	}

	public class ObjectLookupResult {
		public FlexoConceptInstance flexoConceptInstance;
		public FlexoProperty<?> property;
	}

}
