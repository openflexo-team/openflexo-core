/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.fml.rt;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyContextManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareModelSlot;
import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.ViewResourceImpl;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;

/**
 * This class defines and implements the Openflexo built-in FML@runtime technology adapter<br>
 * 
 * This adapter allows to manage {@link View} and {@link VirtualModelInstance} resources in Openflexo infrastructure.
 * 
 * @author sylvain
 * 
 */
@DeclareModelSlots({ // ModelSlot(s) declaration
@DeclareModelSlot(FML = "FMLRTModelSlot", modelSlotClass = FMLRTModelSlot.class), // Classical type-safe interpretation
})
public class FMLRTTechnologyAdapter extends TechnologyAdapter {

	private static final Logger logger = Logger.getLogger(FMLRTTechnologyAdapter.class.getPackage().getName());

	public FMLRTTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "FML@runtime technology adapter";
	}

	/**
	 * Creates and return a new {@link FMLRTModelSlot} adressing supplied VirtualModel.<br>
	 * 
	 * @param modelSlotClass
	 * @param containerVirtualModel
	 *            the virtual model in which model slot should be created
	 * @param addressedVirtualModel
	 *            the virtual model referenced by the model slot
	 * @return
	 */
	public FMLRTModelSlot makeVirtualModelModelSlot(final VirtualModel containerVirtualModel, final VirtualModel addressedVirtualModel) {
		final FMLRTModelSlot returned = this.makeModelSlot(FMLRTModelSlot.class, containerVirtualModel);
		returned.setAddressedVirtualModel(addressedVirtualModel);
		return returned;
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		// no specific binding factory for this technology
		return null;
	}

	@Override
	public FMLRTTechnologyContextManager createTechnologyContextManager(final FlexoResourceCenterService service) {
		return new FMLRTTechnologyContextManager(this, service);
	}

	@Override
	public FMLTechnologyContextManager getTechnologyContextManager() {
		return (FMLTechnologyContextManager) super.getTechnologyContextManager();
	}

	public FlexoServiceManager getServiceManager() {
		return this.getTechnologyAdapterService().getServiceManager();
	}

	public <I> ViewRepository getViewRepository(final FlexoResourceCenter<I> resourceCenter) {
		if (resourceCenter instanceof FlexoProject) {
			return ((FlexoProject) resourceCenter).getViewLibrary();
		}
		ViewRepository viewRepository = resourceCenter.getRepository(ViewRepository.class, this);
		if (viewRepository == null) {
			viewRepository = createViewRepository(resourceCenter);
		}
		return viewRepository;
	}

	@Override
	public <I> void initializeResourceCenter(final FlexoResourceCenter<I> resourceCenter) {

		final ViewRepository viewRepository = this.getViewRepository(resourceCenter);

		// Iterate
		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			final I item = it.next();
			if (!this.isIgnorable(resourceCenter, item)) {
				if (item instanceof File) {
					final File candidateFile = (File) item;
					if (this.isValidViewDirectory(candidateFile)) {
						final ViewResource vRes = this.analyseAsView(candidateFile, viewRepository);
						if (vRes != null) {
							this.referenceResource(vRes, resourceCenter);
						}
					}
				}
			}
		}

		// Call it to update the current repositories
		getPropertyChangeSupport().firePropertyChange("getAllRepositories()", null, resourceCenter);
	}

	/**
	 * Return boolean indicating if supplied {@link File} has the general form of a ViewPoint directory
	 * 
	 * @param candidateFile
	 * @return
	 */
	private boolean isValidViewDirectory(final File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead()
				&& candidateFile.getName().endsWith(ViewResource.VIEW_SUFFIX)) {
			final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewResource.VIEW_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();
		}
		return false;
	}

	/**
	 * Build and return {@link ViewResource} from a candidate file (a .view directory)<br>
	 * Register this {@link ViewResource} in the supplied {@link Viewepository} as well as in the {@link ViewLibrary} (repository for a
	 * FlexoProject)
	 * 
	 * @param candidateFile
	 * @param viewPointRepository
	 * @return the newly created {@link ViewPointResource}
	 */
	private ViewResource analyseAsView(final File candidateFile, final ViewRepository viewRepository) {
		if (viewRepository instanceof ViewLibrary && this.isValidViewDirectory(candidateFile)) {
			final RepositoryFolder<ViewResource> folder = this.retrieveRepositoryFolder(viewRepository, candidateFile);
			final ViewResource vRes = ViewResourceImpl.retrieveViewResource(candidateFile, folder, (ViewLibrary) viewRepository);
			if (vRes != null) {
				logger.info("Found and register view " + vRes.getURI() + vRes.getFlexoIODelegate().toString());
				viewRepository.registerResource(vRes, folder);
				return vRes;
			} else {
				logger.warning("While exploring resource center looking for views : cannot retrieve resource for file "
						+ candidateFile.getAbsolutePath());
			}
		}

		return null;
	}

	/**
	 * Creates and return a view repository for current {@link TechnologyAdapter} and supplied {@link FlexoResourceCenter}
	 */
	public ViewRepository createViewRepository(final FlexoResourceCenter<?> resourceCenter) {
		final ViewRepository returned = new ViewRepository(this, resourceCenter);
		resourceCenter.registerRepository(returned, ViewRepository.class, this);
		return returned;
	}

	@Override
	public <I> boolean isIgnorable(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (resourceCenter.isIgnorable(contents)) {
			return true;
		}
		// TODO: ignore .view subcontents
		return false;
	}

	@Override
	public <I> void contentsAdded(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			final ViewRepository viewRepository = this.getViewRepository(resourceCenter);
			if (contents instanceof File) {
				File candidateFile = (File) contents;
				System.out.println("FMLRTTechnologyAdapter: File ADDED " + candidateFile.getName() + " in "
						+ candidateFile.getParentFile().getAbsolutePath());
				if (this.isValidViewDirectory(candidateFile)) {
					final ViewResource vRes = this.analyseAsView(candidateFile, viewRepository);
					if (vRes != null) {
						this.referenceResource(vRes, resourceCenter);
					}
				}
			}
		}
	}

	@Override
	public <I> void contentsDeleted(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("FMLRTTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
			}
		}
	}

}
