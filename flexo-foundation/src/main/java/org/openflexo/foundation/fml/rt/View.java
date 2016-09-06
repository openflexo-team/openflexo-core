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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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
 * <p/>
 * A {@link View} is instantiated inside a {@link FlexoResourceCenter}.
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(View.ViewImpl.class)
@XMLElement
public interface View extends AbstractVirtualModelInstance<View, ViewPoint> {

	@PropertyIdentifier(type = String.class)
	public static final String VIEW_POINT_URI_KEY = "viewPointURI";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VIEW_POINT_VERSION_KEY = "viewPointVersion";

	@PropertyIdentifier(type = VirtualModelInstance.class, cardinality = Cardinality.LIST)
	public static final String VIRTUAL_MODEL_INSTANCES_KEY = "virtualModelInstances";

	@Getter(value = VIEW_POINT_URI_KEY)
	@XMLAttribute
	public String getViewPointURI();

	@Setter(VIEW_POINT_URI_KEY)
	public void setViewPointURI(String viewPointURI);

	@Getter(value = VIEW_POINT_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getViewPointVersion();

	@Setter(VIEW_POINT_VERSION_KEY)
	public void setViewPointVersion(FlexoVersion viewPointVersion);

	@Override
	public String getName();

	public boolean isValidVirtualModelInstanceName(String virtualModelName);

	/**
	 * Return all {@link VirtualModelInstance} defined in this {@link View}
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_INSTANCES_KEY, cardinality = Cardinality.LIST, inverse = VirtualModelInstance.VIEW_KEY, ignoreType = true)
	public List<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstances();

	/**
	 * Allow to retrieve VMIs given a virtual model.
	 * 
	 * @param virtualModel
	 *            key to find correct VMI
	 * @return the list
	 */
	public List<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstancesForVirtualModel(AbstractVirtualModel<?> virtualModel);

	@Setter(VIRTUAL_MODEL_INSTANCES_KEY)
	public void setVirtualModelInstances(List<AbstractVirtualModelInstance<?, ?>> virtualModelInstances);

	@Adder(VIRTUAL_MODEL_INSTANCES_KEY)
	public void addToVirtualModelInstances(AbstractVirtualModelInstance<?, ?> virtualModelInstance);

	@Remover(VIRTUAL_MODEL_INSTANCES_KEY)
	public void removeFromVirtualModelInstances(AbstractVirtualModelInstance<?, ?> virtualModelInstance);

	public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance(String name);

	public ViewPoint getViewPoint();

	public RepositoryFolder<ViewResource, ?> getFolder();

	public ViewLibrary getViewLibrary();

	public boolean hasNature(ViewNature nature);

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link View}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters();

	public static abstract class ViewImpl extends AbstractVirtualModelInstanceImpl<View, ViewPoint>implements View {

		private static final Logger logger = Logger.getLogger(View.class.getPackage().getName());

		// private ViewResource resource;
		// private List<VirtualModelInstance> vmInstances;
		// private final List<ModelSlotInstance<?, ?>> modelSlotInstances;

		// TODO: move this to ViewResource
		/*public static ViewResource newView(String viewName, String viewTitle, ViewPoint viewPoint, RepositoryFolder<ViewResource, ?> folder,
				FlexoResourceCenter<?> rc) throws SaveResourceException {
		
			FMLTechnologyAdapter vmTA = rc.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLTechnologyAdapter.class);
			
			FMLRTTechnologyAdapter rtTA = rc.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLRTTechnologyAdapter.class);			
			

			
			ViewResource newViewResource = ViewResourceImpl.makeViewResource(viewName, folder, viewPoint, (ViewLibrary) rc.getRepository(ViewLibrary.class, rtTA ));
		
			View newView = newViewResource.getFactory().newInstance(View.class);
		
			newView.setProject(project);
		
			newViewResource.setResourceData(newView);
			newView.setResource(newViewResource);
		
			newView.setTitle(viewTitle);
		
			// FD unused
			// FlexoIODelegate<?> delegate =
			newViewResource.getFlexoIODelegate();
			// System.out.println("Saving " + delegate.stringRepresentation());
		
			// Save it
			newViewResource.save(null);
			// File viewDirectory = new File(folder.getFile(), viewName + ViewResource.VIEW_SUFFIX);
			// newViewResource.setDirectory(ResourceLocator.locateResource(viewDirectory.getAbsolutePath()));
			// newView.save();
			vmTA.referenceResource(newViewResource, rc);
			return newViewResource;
		}*/

		// TODO: move this to ViewResource
		/*public static ViewResource newSubView(String viewName, String viewTitle, ViewPoint viewPoint, ViewResource container,
				FlexoResourceCenter<?> rc) throws SaveResourceException {
			
			FMLTechnologyAdapter vmTA = rc.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLTechnologyAdapter.class);

			FMLRTTechnologyAdapter rtTA = rc.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			
			ViewResource newViewResource = ViewResourceImpl.makeSubViewResource(viewName, container, viewPoint, (ViewLibrary) rc.getRepository(ViewLibrary.class, rtTA ));
		
		
			View newView = newViewResource.getFactory().newInstance(View.class);
		
		
			newViewResource.setResourceData(newView);
			newView.setResource(newViewResource);
		
			newView.setTitle(viewTitle);
		
			FlexoIODelegate<?> delegate = newViewResource.getFlexoIODelegate();
			System.out.println("Saving " + delegate.stringRepresentation());
		
			// Save it
			newViewResource.save(null);
			// File viewDirectory = new File(folder.getFile(), viewName + ViewResource.VIEW_SUFFIX);
			// newViewResource.setDirectory(ResourceLocator.locateResource(viewDirectory.getAbsolutePath()));
			// newView.save();
			vmTA.referenceResource(newViewResource, rc);
			return newViewResource;
		}*/

		@Override
		public final boolean hasNature(ViewNature nature) {
			return nature.hasNature(this);
		}

		@Override
		public ViewImpl getView() {
			return this;
		}

		@Override
		public String extendedStringRepresentation() {
			return getName() + ":" + getViewPoint().getName();
		}

		@Override
		public View getResourceData() {
			return this;
		}

		@Override
		public FlexoResourceCenter<?> getResourceCenter() {
			if (getResource() != null) {
				return getResource().getResourceCenter();
			}
			return null;
		}

		@Override
		public ViewPoint getFlexoConcept() {
			return getViewPoint();
		}

		@Override
		public ViewPoint getViewPoint() {
			if (getResource() instanceof ViewResource) {
				return ((ViewResource) getResource()).getViewPoint();
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
		public List<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstances() {
			if (getResource() != null && !getResource().isDeserializing()) {
				loadVirtualModelInstancesWhenUnloaded();
			}
			return (List<AbstractVirtualModelInstance<?, ?>>) performSuperGetter(VIRTUAL_MODEL_INSTANCES_KEY);
		}

		@Override
		public List<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstancesForVirtualModel(
				final AbstractVirtualModel<?> virtualModel) {
			List<AbstractVirtualModelInstance<?, ?>> returned = new ArrayList<AbstractVirtualModelInstance<?, ?>>();
			for (AbstractVirtualModelInstance<?, ?> vmi : getVirtualModelInstances()) {
				if (vmi.getVirtualModel() == virtualModel) {
					returned.add(vmi);
				}
			}
			return returned;
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
		public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance(String name) {
			for (AbstractVirtualModelInstance<?, ?> vmi : getVirtualModelInstances()) {
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

		@Override
		public ViewLibrary getViewLibrary() {

			FlexoResourceCenter<?> rc = getResourceCenter();
			FMLRTTechnologyAdapter rtTA = rc.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			
			return rc.getRepository(ViewLibrary.class, rtTA);
		}

		@Override
		public RepositoryFolder<ViewResource, ?> getFolder() {
			if (getResource() != null) {
				return getViewLibrary().getParentFolder((ViewResource) getResource());
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

		@Override
		public Object getValue(BindingVariable variable) {
			if (variable.getVariableName().equals(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getViewPoint();
			}

			return super.getValue(variable);

			// TODO: do it when View will be a VirtualModelInstance !!!!
			/*else if (variable instanceof FlexoPropertyBindingVariable) {
				return ((FlexoPropertyBindingVariable) variable).getValue(this);
			}*/

			/*logger.warning("Unexpected variable requested in View: " + variable + " of " + variable.getClass());
			Thread.dumpStack();
			return null;*/
		}

		/**
		 * Return the list of {@link TechnologyAdapter} used in the context of this {@link View}
		 * 
		 * @return
		 */
		@Override
		public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
			if (getViewPoint() != null) {
				List<TechnologyAdapter> returned = getViewPoint().getRequiredTechnologyAdapters();
				if (!returned.contains(getTechnologyAdapter())) {
					returned.add(getTechnologyAdapter());
				}
				return returned;
			}
			return Collections.singletonList((TechnologyAdapter) getTechnologyAdapter());
		}

	}
}
