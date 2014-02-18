/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.view.rm.ViewResourceImpl;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.viewpoint.ViewPoint;
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
import org.openflexo.toolbox.FlexoVersion;

/**
 * A {@link View} is the run-time concept (instance) of a {@link ViewPoint}.<br>
 * 
 * A {@link View} is instantiated inside a {@link FlexoProject}.
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(View.ViewImpl.class)
@XMLElement
public interface View extends ViewObject, ResourceData<View>, InnerResourceData<View> {

	@PropertyIdentifier(type = String.class)
	public static final String VIEW_POINT_URI_KEY = "viewPointURI";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VIEW_POINT_VERSION_KEY = "viewPointVersion";
	@PropertyIdentifier(type = String.class)
	public static final String TITLE_KEY = "title";
	// @PropertyIdentifier(type = List.class)
	// public static final String MODEL_SLOT_INSTANCES_KEY = "modelSlotInstances";

	@PropertyIdentifier(type = VirtualModelInstance.class, cardinality = Cardinality.LIST)
	public static final String VIRTUAL_MODEL_INSTANCES_KEY = "virtualModelInstances";

	@Getter(value = VIEW_POINT_URI_KEY)
	@XMLAttribute
	public String getViewPointURI();

	@Setter(VIEW_POINT_URI_KEY)
	public void setViewPointURI(String viewPointURI);

	@Getter(value = VIEW_POINT_VERSION_KEY)
	@XMLAttribute
	public FlexoVersion getViewPointVersion();

	@Setter(VIEW_POINT_VERSION_KEY)
	public void setViewPointVersion(FlexoVersion viewPointVersion);

	@Getter(value = TITLE_KEY)
	@XMLAttribute
	public String getTitle();

	@Setter(TITLE_KEY)
	public void setTitle(String title);

	/*@Getter(value = MODEL_SLOT_INSTANCES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	public List<ModelSlotInstance> getModelSlotInstances();

	@Setter(MODEL_SLOT_INSTANCES_KEY)
	public void setModelSlotInstances(List<ModelSlotInstance> modelSlotInstances);

	@Adder(MODEL_SLOT_INSTANCES_KEY)
	public void addToModelSlotInstances(ModelSlotInstance aModelSlotInstance);

	@Remover(MODEL_SLOT_INSTANCES_KEY)
	public void removeFromModelSlotInstance(ModelSlotInstance aModelSlotInstance);*/

	public String getName();

	public void setName(String name);

	public boolean isValidVirtualModelName(String virtualModelName);

	/**
	 * Return all {@link VirtualModelInstance} defined in this {@link View}
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_INSTANCES_KEY, cardinality = Cardinality.LIST, inverse = VirtualModelInstance.VIEW_KEY, ignoreType = true)
	public List<VirtualModelInstance> getVirtualModelInstances();

	@Setter(VIRTUAL_MODEL_INSTANCES_KEY)
	public void setVirtualModelInstances(List<VirtualModelInstance> virtualModelInstances);

	@Adder(VIRTUAL_MODEL_INSTANCES_KEY)
	public void addToVirtualModelInstances(VirtualModelInstance virtualModelInstance);

	@Remover(VIRTUAL_MODEL_INSTANCES_KEY)
	public void removeFromVirtualModelInstances(VirtualModelInstance virtualModelInstance);

	public VirtualModelInstance getVirtualModelInstance(String name);

	public ViewPoint getViewPoint();

	public RepositoryFolder<ViewResource> getFolder();

	public ViewLibrary getViewLibrary();

	public static abstract class ViewImpl extends ViewObjectImpl implements View {

		private static final Logger logger = Logger.getLogger(View.class.getPackage().getName());

		private ViewResource resource;
		// private List<VirtualModelInstance> vmInstances;
		private final List<ModelSlotInstance<?, ?>> modelSlotInstances;
		private String title;

		// TODO: move this to ViewResource
		public static View newView(String viewName, String viewTitle, ViewPoint viewPoint, RepositoryFolder<ViewResource> folder,
				FlexoProject project) throws SaveResourceException {

			ViewResource newViewResource = ViewResourceImpl.makeViewResource(viewName, folder, viewPoint, project.getViewLibrary());

			View newView = newViewResource.getFactory().newInstance(View.class);
			newView.setProject(project);
			newViewResource.setResourceData(newView);
			newView.setResource(newViewResource);

			newView.setTitle(viewTitle);

			// Save it
			newViewResource.save(null);

			// newView.save();

			return newView;
		}

		/**
		 * Default Constructor
		 * 
		 */
		public ViewImpl() {
			super();
			// vmInstances = new ArrayList<VirtualModelInstance>();
			modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();
		}

		/*public ViewImpl(FlexoProject project, ViewResource resource) {
			super(project);
			setResource(resource);
			// builder.view = this;
			// initializeDeserialization(builder);
		}*/

		/*public ViewImpl(FlexoProject project) {
			super(project);
			logger.info("Created new view with project " + project);
			vmInstances = new ArrayList<VirtualModelInstance>();
			modelSlotInstances = new ArrayList<ModelSlotInstance<?, ?>>();

		}*/

		/*@Override
		public String getURI() {
			if (getResource() == null) {
				return super.getURI();
			} else {
				return getResource().getURI();
			}
		}*/

		@Override
		public ViewImpl getView() {
			return this;
		}

		@Override
		public FlexoProject getProject() {
			if (getResource() != null) {
				return getResource().getProject();
			}
			return super.getProject();
		}

		@Override
		public View getResourceData() {
			return this;
		}

		@Override
		public ViewResource getResource() {
			return resource;
		}

		@Override
		public void setResource(org.openflexo.foundation.resource.FlexoResource<View> resource) {
			this.resource = (ViewResource) resource;
		}

		/*@Override
		public void save() throws SaveResourceException {
			getResource().save(null);
		}*/

		/*@Override
		public String getClassNameKey() {
			return "view";
		}*/

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
				getResource().setName(name);
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
		public ViewPoint getViewPoint() {
			if (getResource() != null) {
				return getResource().getViewPoint();
			}
			return null;
		}

		@Override
		public String toString() {
			return "View[name=" + getName() + "/viewpoint=" + (getViewPoint() != null ? getViewPoint().getName() : "null") + "/hash="
					+ Integer.toHexString(hashCode()) + "]";
		}

		// ==========================================================================
		// ======================== Virtual Model Instances =========================
		// ==========================================================================

		@Override
		public List<VirtualModelInstance> getVirtualModelInstances() {
			loadVirtualModelInstancesWhenUnloaded();
			return (List<VirtualModelInstance>) performSuperGetter(VIRTUAL_MODEL_INSTANCES_KEY);
		}

		/**
		 * Load eventually unloaded VirtualModelInstances<br>
		 * After this call return, we can assert that all {@link VirtualModelInstance} are loaded.
		 */
		private void loadVirtualModelInstancesWhenUnloaded() {
			for (org.openflexo.foundation.resource.FlexoResource<?> r : getResource().getContents()) {
				if (r instanceof VirtualModelInstanceResource) {
					((VirtualModelInstanceResource) r).getVirtualModelInstance();
				}
			}
		}

		@Override
		public VirtualModelInstance getVirtualModelInstance(String name) {
			for (VirtualModelInstance vmi : getVirtualModelInstances()) {
				String lName = vmi.getName();
				if (lName != null) {
					if (vmi.getName().equals(name)) {
						return vmi;
					}
				} else {
					logger.warning("Name of VirtualModel is null: " + this.toString());
				}
			}
			return null;
		}

		@Override
		public boolean isValidVirtualModelName(String virtualModelName) {
			return getVirtualModelInstance(virtualModelName) == null;
		}

		// ==========================================================================
		// ============================== Model Slots ===============================
		// ==========================================================================

		/**
		 * This is the binding point between a {@link ModelSlot} and its concretization in a {@link View} through notion of
		 * {@link ModelSlotInstance}
		 * 
		 * @param modelSlot
		 * @return
		 */
		/*public <RD extends ResourceData<RD>> ModelSlotInstance<?, RD> getModelSlotInstance(ModelSlot<RD> modelSlot) {
			for (ModelSlotInstance<?, ?> msInstance : getModelSlotInstances()) {
				if (msInstance.getModelSlot() == modelSlot) {
					return (ModelSlotInstance<?, RD>) msInstance;
				}
			}
			return null;
		}*/

		/*public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> instances) {
			this.modelSlotInstances = instances;
		}

		@Override
		public List<ModelSlotInstance<?, ?>> getModelSlotInstances() {
			return modelSlotInstances;
		}

		public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> instance) {
			modelSlotInstances.remove(instance);
		}

		public void addToModelSlotInstances(ModelSlotInstance<?, ?> instance) {
			modelSlotInstances.add(instance);
		}*/

		/*public <M extends FlexoModel<M, MM>, MM extends FlexoMetaModel<MM>> void setModel(ModelSlot modelSlot, M model) {
			modelsMap.put(modelSlot, model);
			for (ModelSlotInstance instance : modelSlotInstances) {
				if (instance.getModelSlot().equals(modelSlot)) {
					instance.setModel(model);
					return;
				}
			}
			ModelSlotInstance<M, MM> instance = new ModelSlotInstance<M, MM>(this, modelSlot);
			instance.setModel(model);
			modelSlotInstances.add(instance);
		}

		public <M extends FlexoModel<M, MM>, MM extends FlexoMetaModel<MM>> M getModel(ModelSlot modelSlot, boolean createIfDoesNotExist) {
			M model = (M) modelsMap.get(modelSlot);
			if (createIfDoesNotExist && model == null) {
				try {
					org.openflexo.foundation.resource.FlexoResource<M> modelResource = modelSlot.createEmptyModel(this,
							modelSlot.getMetaModelResource());
					model = modelResource.getResourceData(null);
					setModel(modelSlot, model);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceDependencyLoopException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FlexoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return model;
		}

		public <M extends FlexoModel<M, MM>, MM extends FlexoMetaModel<MM>> M getModel(ModelSlot modelSlot) {
			return getModel(modelSlot, true);
		}*/

		/*@Deprecated
		public Set<FlexoMetaModel<?>> getAllMetaModels() {
			Set<FlexoMetaModel<?>> allMetaModels = new HashSet<FlexoMetaModel<?>>();
			for (ModelSlotInstance<?, ?> instance : getModelSlotInstances()) {
				if (instance.getModelSlot() instanceof TypeAwareModelSlot
						&& ((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource() != null) {
					allMetaModels.add(((TypeAwareModelSlot) instance.getModelSlot()).getMetaModelResource().getMetaModelData());
				}
			}
			return allMetaModels;
		}

		@Deprecated
		public Set<FlexoModel<?, ?>> getAllModels() {
			Set<FlexoModel<?, ?>> allModels = new HashSet<FlexoModel<?, ?>>();
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
		public final boolean delete() {

			logger.info("Deleting view " + this);

			// Delete the view resource from the view library
			// Dereference the resource
			if (getProject() != null && getProject().getViewLibrary() != null && resource != null) {
				getProject().getViewLibrary().unregisterResource(resource);
				resource = null;
			}

			// Delete view
			super.delete();

			// Delete observers
			deleteObservers();

			return true;
		}

		@Override
		public ViewLibrary getViewLibrary() {
			return getProject().getViewLibrary();
		}

		@Override
		public RepositoryFolder<ViewResource> getFolder() {
			if (getResource() != null) {
				return getViewLibrary().getParentFolder(getResource());
			}
			return null;
		}

		@Override
		public String getViewPointURI() {
			if (getViewPoint() != null) {
				return getViewPoint().getURI();
			}
			return null;
		}

		// Not applicable
		@Override
		public void setViewPointURI(String viewPointURI) {
		}

		@Override
		public FlexoVersion getViewPointVersion() {
			if (getViewPoint() != null) {
				return getViewPoint().getVersion();
			}
			return null;
		}

		// Not applicable
		@Override
		public void setViewPointVersion(FlexoVersion viewPointVersion) {
		}

	}
}
