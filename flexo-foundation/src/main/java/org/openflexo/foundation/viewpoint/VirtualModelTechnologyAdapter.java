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
package org.openflexo.foundation.viewpoint;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoFileResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.DeclareModelSlot;
import org.openflexo.foundation.technologyadapter.DeclareModelSlots;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;
import org.openflexo.foundation.view.ViewLibrary;
import org.openflexo.foundation.view.ViewRepository;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.view.rm.ViewResourceImpl;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.ViewPointResourceImpl;

/**
 * This class defines and implements the Openflexo built-in virtual model technology adapter
 * 
 * @author sylvain
 * 
 */
@DeclareModelSlots({ // ModelSlot(s) declaration
@DeclareModelSlot(FML = "VirtualModelModelSlot", modelSlotClass = VirtualModelModelSlot.class), // Classical type-safe interpretation
})
public class VirtualModelTechnologyAdapter extends TechnologyAdapter {

	private static final Logger	logger	= Logger.getLogger(VirtualModelTechnologyAdapter.class.getPackage().getName());

	public VirtualModelTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "Openflexo FML";
	}

	/**
	 * Creates and return a new {@link VirtualModelModelSlot} adressing supplied VirtualModel.<br>
	 * 
	 * @param modelSlotClass
	 * @param containerVirtualModel
	 *            the virtual model in which model slot should be created
	 * @param addressedVirtualModel
	 *            the virtual model referenced by the model slot
	 * @return
	 */
	public VirtualModelModelSlot makeVirtualModelModelSlot(final VirtualModel containerVirtualModel, final VirtualModel addressedVirtualModel) {
		final VirtualModelModelSlot returned = this.makeModelSlot(VirtualModelModelSlot.class, containerVirtualModel);
		returned.setAddressedVirtualModel(addressedVirtualModel);
		return returned;
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		// no specific binding factory for this technology
		return null;
	}

	@Override
	public VirtualModelTechnologyContextManager createTechnologyContextManager(final FlexoResourceCenterService service) {
		return new VirtualModelTechnologyContextManager(this, service);
	}

	@Override
	public VirtualModelTechnologyContextManager getTechnologyContextManager() {
		return (VirtualModelTechnologyContextManager) super.getTechnologyContextManager();
	}

	public FlexoServiceManager getServiceManager() {
		return this.getTechnologyAdapterService().getServiceManager();
	}

	public ViewPointLibrary getViewPointLibrary() {
		return this.getServiceManager().getViewPointLibrary();
	}

	public <I> ViewRepository getViewRepository(final FlexoResourceCenter<I> resourceCenter) {
		if (resourceCenter instanceof FlexoProject) {
			return ((FlexoProject) resourceCenter).getViewLibrary();
		}
		ViewRepository viewRepository = resourceCenter.getRepository(ViewRepository.class, this);
		if (viewRepository == null) {
			viewRepository = this.createViewRepository(resourceCenter);
		}
		return viewRepository;
	}

	public <I> ViewPointRepository getViewPointRepository(final FlexoResourceCenter<I> resourceCenter) {
		ViewPointRepository viewPointRepository = resourceCenter.getRepository(ViewPointRepository.class, this);
		if (viewPointRepository == null) {
			viewPointRepository = this.createViewPointRepository(resourceCenter);
		}
		return viewPointRepository;
	}

	@Override
	public <I> void initializeResourceCenter(final FlexoResourceCenter<I> resourceCenter) {

		// A single DiagramSpecification Repository for all ResourceCenters

		final ViewRepository viewRepository = this.getViewRepository(resourceCenter);
		final ViewPointRepository viewPointRepository = this.getViewPointRepository(resourceCenter);
		// ViewLibrary viewLibrary = null;

		/*if (resourceCenter instanceof FlexoProject) {
			viewLibrary = ((FlexoProject) resourceCenter).getViewLibrary();
		}*/

		// Iterate
		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			final I item = it.next();
			if (!this.isIgnorable(resourceCenter, item)) {
				if (item instanceof File) {
					final File candidateFile = (File) item;
					if (this.isValidViewPointDirectory(candidateFile)) {
						final ViewPointResource vpRes = this.analyseAsViewPoint(candidateFile, viewPointRepository);
						this.referenceResource(vpRes, resourceCenter);
					}
				}
			}
		}
		// Iterate
		it = resourceCenter.iterator();

		while (it.hasNext()) {
			final I item = it.next();
			if (!this.isIgnorable(resourceCenter, item)) {
				if (item instanceof File) {
					final File candidateFile = (File) item;
					if (this.isValidViewDirectory(candidateFile)) {
						final ViewResource vRes = this.analyseAsView(candidateFile, viewRepository);
						this.referenceResource(vRes, resourceCenter);
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
	private boolean isValidViewPointDirectory(final File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead() && candidateFile.getName().endsWith(ViewPointResource.VIEWPOINT_SUFFIX)) {
			final String baseName = candidateFile.getName().substring(0, candidateFile.getName().length() - ViewPointResource.VIEWPOINT_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();
		}
		return false;
	}

	/**
	 * Build and return {@link ViewPointResource} from a candidate file (a .viewpoint directory)<br>
	 * Register this {@link ViewPointResource} in the supplied {@link ViewPointRepository} as well as in the {@link ViewPointLibrary}
	 * 
	 * @param candidateFile
	 * @param viewPointRepository
	 * @return the newly created {@link ViewPointResource}
	 */
	private ViewPointResource analyseAsViewPoint(final File candidateFile, final ViewPointRepository viewPointRepository) {
		if (this.isValidViewPointDirectory(candidateFile)) {
			final ViewPointResource vpRes = ViewPointResourceImpl.retrieveViewPointResource(candidateFile, this.getServiceManager());
			if (vpRes != null) {
				logger.info("Found and register viewpoint " + vpRes.getURI() + (vpRes instanceof FlexoFileResource ? " file=" + ((FlexoFileResource<?>) vpRes).getFile().getAbsolutePath() : ""));
				final RepositoryFolder<ViewPointResource> folder = this.retrieveRepositoryFolder(viewPointRepository, candidateFile);
				viewPointRepository.registerResource(vpRes, folder);
				return vpRes;
			} else {
				logger.warning("While exploring resource center looking for viewpoints : cannot retrieve resource for file " + candidateFile.getAbsolutePath());
			}
		}

		return null;
	}

	/**
	 * Return boolean indicating if supplied {@link File} has the general form of a ViewPoint directory
	 * 
	 * @param candidateFile
	 * @return
	 */
	private boolean isValidViewDirectory(final File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead() && candidateFile.getName().endsWith(ViewResource.VIEW_SUFFIX)) {
			final String baseName = candidateFile.getName().substring(0, candidateFile.getName().length() - ViewResource.VIEW_SUFFIX.length());
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
				logger.info("Found and register view " + vRes.getURI() + (vRes instanceof FlexoFileResource ? " file=" + ((FlexoFileResource<?>) vRes).getFile().getAbsolutePath() : ""));
				viewRepository.registerResource(vRes, folder);
				return vRes;
			} else {
				logger.warning("While exploring resource center looking for views : cannot retrieve resource for file " + candidateFile.getAbsolutePath());
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

	/**
	 * Creates and return a view repository for current {@link TechnologyAdapter} and supplied {@link FlexoResourceCenter}
	 */
	public ViewPointRepository createViewPointRepository(final FlexoResourceCenter<?> resourceCenter) {
		final ViewPointRepository returned = new ViewPointRepository(resourceCenter, this.getTechnologyAdapterService().getServiceManager());
		resourceCenter.registerRepository(returned, ViewPointRepository.class, this);
		return returned;
	}

	@Override
	public <I> boolean isIgnorable(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (resourceCenter.isIgnorable(contents)) {
			return true;
		}
		// TODO: ignore .viewpoint subcontents
		// TODO: ignore .view subcontents
		return false;
	}

	@Override
	public <I> void contentsAdded(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("VirtualModelTechnologyAdapter: File ADDED " + ((File) contents).getName() + " in " + ((File) contents).getParentFile().getAbsolutePath());
				final ViewRepository viewRepository = this.getViewRepository(resourceCenter);
				final ViewPointRepository viewPointRepository = this.getViewPointRepository(resourceCenter);
				final File candidateFile = (File) contents;
				if (this.isValidViewPointDirectory(candidateFile)) {
					final ViewPointResource vpRes = this.analyseAsViewPoint(candidateFile, viewPointRepository);
					this.referenceResource(vpRes, resourceCenter);
				}
				if (this.isValidViewDirectory(candidateFile)) {
					final ViewResource vRes = this.analyseAsView(candidateFile, viewRepository);
					this.referenceResource(vRes, resourceCenter);
				}
			}
		}
	}

	@Override
	public <I> void contentsDeleted(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("VirtualModelTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in " + ((File) contents).getParentFile().getAbsolutePath());
			}
		}
	}

}
