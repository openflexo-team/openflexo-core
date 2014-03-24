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

	private static final Logger logger = Logger.getLogger(VirtualModelTechnologyAdapter.class.getPackage().getName());

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
	public VirtualModelModelSlot makeVirtualModelModelSlot(VirtualModel containerVirtualModel, VirtualModel addressedVirtualModel) {
		VirtualModelModelSlot returned = makeModelSlot(VirtualModelModelSlot.class, containerVirtualModel);
		returned.setAddressedVirtualModel(addressedVirtualModel);
		return returned;
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		// no specific binding factory for this technology
		return null;
	}

	@Override
	public VirtualModelTechnologyContextManager createTechnologyContextManager(FlexoResourceCenterService service) {
		return new VirtualModelTechnologyContextManager(this, service);
	}

	@Override
	public VirtualModelTechnologyContextManager getTechnologyContextManager() {
		return (VirtualModelTechnologyContextManager) super.getTechnologyContextManager();
	}

	public FlexoServiceManager getServiceManager() {
		return getTechnologyAdapterService().getServiceManager();
	}

	public ViewPointLibrary getViewPointLibrary() {
		return getServiceManager().getViewPointLibrary();
	}

	public <I> ViewRepository getViewRepository(FlexoResourceCenter<I> resourceCenter) {
		ViewRepository viewRepository = resourceCenter.getRepository(ViewRepository.class, this);
		if (viewRepository == null) {
			viewRepository = createViewRepository(resourceCenter);
		}
		return viewRepository;
	}

	public <I> ViewPointRepository getViewPointRepository(FlexoResourceCenter<I> resourceCenter) {
		ViewPointRepository viewPointRepository = resourceCenter.getRepository(ViewPointRepository.class, this);
		if (viewPointRepository == null) {
			viewPointRepository = createViewPointRepository(resourceCenter);
		}
		return viewPointRepository;
	}

	@Override
	public <I> void initializeResourceCenter(FlexoResourceCenter<I> resourceCenter) {

		// A single DiagramSpecification Repository for all ResourceCenters

		ViewRepository viewRepository = getViewRepository(resourceCenter);
		ViewPointRepository viewPointRepository = getViewPointRepository(resourceCenter);
		ViewLibrary viewLibrary = null;

		if (resourceCenter instanceof FlexoProject) {
			viewLibrary = ((FlexoProject) resourceCenter).getViewLibrary();
		}

		// Iterate
		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			I item = it.next();
			if (!isIgnorable(resourceCenter, item)) {
				if (item instanceof File) {
					File candidateFile = (File) item;
					if (isValidViewPointDirectory(candidateFile)) {
						analyseAsViewPoint(candidateFile, viewPointRepository);
					}
					if (viewLibrary != null && isValidViewDirectory(candidateFile)) {
						analyseAsView(candidateFile, viewRepository, viewLibrary);
					}
				}
			}
		}

	}

	/**
	 * Return boolean indicating if supplied {@link File} has the general form of a ViewPoint directory
	 * 
	 * @param candidateFile
	 * @return
	 */
	private boolean isValidViewPointDirectory(File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead()
				&& candidateFile.getName().endsWith(ViewPointResource.VIEWPOINT_SUFFIX)) {
			String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.VIEWPOINT_SUFFIX.length());
			File xmlFile = new File(candidateFile, baseName + ".xml");
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
	private ViewPointResource analyseAsViewPoint(File candidateFile, ViewPointRepository viewPointRepository) {
		if (isValidViewPointDirectory(candidateFile)) {
			ViewPointResource vpRes = ViewPointResourceImpl.retrieveViewPointResource(candidateFile, getServiceManager());
			if (vpRes != null) {
				logger.info("Found and register viewpoint " + vpRes.getURI()
						+ (vpRes instanceof FlexoFileResource ? " file=" + ((FlexoFileResource<?>) vpRes).getFile().getAbsolutePath() : ""));
				RepositoryFolder<ViewPointResource> folder = retrieveRepositoryFolder(viewPointRepository, candidateFile);
				viewPointRepository.registerResource(vpRes, folder);
				return vpRes;
			} else {
				logger.warning("While exploring resource center looking for viewpoints : cannot retrieve resource for file "
						+ candidateFile.getAbsolutePath());
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
	private boolean isValidViewDirectory(File candidateFile) {
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead()
				&& candidateFile.getName().endsWith(ViewResource.VIEW_SUFFIX)) {
			String baseName = candidateFile.getName().substring(0, candidateFile.getName().length() - ViewResource.VIEW_SUFFIX.length());
			File xmlFile = new File(candidateFile, baseName + ".xml");
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
	private ViewResource analyseAsView(File candidateFile, ViewRepository viewRepository, ViewLibrary viewLibrary) {
		if (isValidViewDirectory(candidateFile)) {
			RepositoryFolder<ViewResource> folder = retrieveRepositoryFolder(viewRepository, candidateFile);
			ViewResource vRes = ViewResourceImpl.retrieveViewResource(candidateFile, folder, viewLibrary);
			if (vRes != null) {
				logger.info("Found and register view " + vRes.getURI()
						+ (vRes instanceof FlexoFileResource ? " file=" + ((FlexoFileResource<?>) vRes).getFile().getAbsolutePath() : ""));
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
	public ViewRepository createViewRepository(FlexoResourceCenter<?> resourceCenter) {
		ViewRepository returned = new ViewRepository(this, resourceCenter);
		resourceCenter.registerRepository(returned, ViewRepository.class, this);
		return returned;
	}

	/**
	 * Creates and return a view repository for current {@link TechnologyAdapter} and supplied {@link FlexoResourceCenter}
	 */
	public ViewPointRepository createViewPointRepository(FlexoResourceCenter<?> resourceCenter) {
		ViewPointRepository returned = new ViewPointRepository(resourceCenter, getTechnologyAdapterService().getServiceManager()
				.getViewPointLibrary());
		resourceCenter.registerRepository(returned, ViewPointRepository.class, this);
		return returned;
	}

	@Override
	public <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
		if (resourceCenter.isIgnorable(contents)) {
			return true;
		}
		// TODO: ignore .viewpoint subcontents
		// TODO: ignore .view subcontents
		return false;
	}

	@Override
	public <I> void contentsAdded(FlexoResourceCenter<I> resourceCenter, I contents) {
		if (!isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("VirtualModelTechnologyAdapter: File ADDED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
				ViewRepository viewRepository = getViewRepository(resourceCenter);
				ViewPointRepository viewPointRepository = getViewPointRepository(resourceCenter);
				ViewLibrary viewLibrary = null;
				if (resourceCenter instanceof FlexoProject) {
					viewLibrary = ((FlexoProject) resourceCenter).getViewLibrary();
				}
				File candidateFile = (File) contents;
				if (isValidViewPointDirectory(candidateFile)) {
					analyseAsViewPoint(candidateFile, viewPointRepository);
				}
				if (viewLibrary != null && isValidViewDirectory(candidateFile)) {
					analyseAsView(candidateFile, viewRepository, viewLibrary);
				}
			}
		}
	}

	@Override
	public <I> void contentsDeleted(FlexoResourceCenter<I> resourceCenter, I contents) {
		if (!isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("VirtualModelTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
			}
		}
	}

}
