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
package org.openflexo.foundation.fml;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceImpl;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;
import org.openflexo.rm.InJarResourceImpl;

/**
 * This class defines and implements the Openflexo built-in virtual model technology adapter
 * 
 * @author sylvain
 * 
 */
public class FMLTechnologyAdapter extends TechnologyAdapter {

	private static final Logger logger = Logger.getLogger(FMLTechnologyAdapter.class.getPackage().getName());

	public FMLTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "FML technology adapter";
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		// no specific binding factory for this technology
		return null;
	}

	@Override
	public FMLTechnologyContextManager createTechnologyContextManager(final FlexoResourceCenterService service) {
		return new FMLTechnologyContextManager(this, service);
	}

	@Override
	public FMLTechnologyContextManager getTechnologyContextManager() {
		return (FMLTechnologyContextManager) super.getTechnologyContextManager();
	}

	public FlexoServiceManager getServiceManager() {
		return this.getTechnologyAdapterService().getServiceManager();
	}

	public ViewPointLibrary getViewPointLibrary() {
		return this.getServiceManager().getViewPointLibrary();
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

		final ViewPointRepository viewPointRepository = getViewPointRepository(resourceCenter);

		// Iterate
		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			final I item = it.next();
			if (!this.isIgnorable(resourceCenter, item)) {
				// if (item instanceof File) {
				// final File candidateFile = (File) item;
				// if (this.isValidViewPointDirectory(item)) {
				final ViewPointResource vpRes = analyseAsViewPoint(item, resourceCenter);
				// this.referenceResource(vpRes, resourceCenter);
				// }
				// }
				// if (item instanceof InJarResourceImpl) {
				// final InJarResourceImpl candidateJar = (InJarResourceImpl) item;
				// if (this.isValidViewPointDirectory(candidateJar)) {
				// final ViewPointResource vpRes = analyseAsViewPoint(candidateJar, resourceCenter);
				// this.referenceResource(vpRes, resourceCenter);
				// / }
				// }
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
		if (candidateFile.exists() && candidateFile.isDirectory() && candidateFile.canRead()
				&& candidateFile.getName().endsWith(ViewPointResource.VIEWPOINT_SUFFIX)) {
			final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.VIEWPOINT_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();
		}
		return false;
	}

	/**
	 * Return boolean indicating if supplied {@link InJarResourceImpl} has the general form of a ViewPoint directory
	 * 
	 * @param candidateJar
	 * @return
	 */
	private boolean isValidViewPointDirectory(final InJarResourceImpl candidateJar) {
		String candidateJarName = FilenameUtils.getBaseName(candidateJar.getRelativePath());
		if (candidateJar.getRelativePath().endsWith(".xml")
				&& candidateJar.getRelativePath().endsWith(
						candidateJarName + ViewPointResource.VIEWPOINT_SUFFIX + "/" + candidateJarName + ".xml")) {
			return true;
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
	private ViewPointResource analyseAsViewPoint(final Object candidateElement, FlexoResourceCenter resourceCenter) {
		if (this.isValidViewPoint(candidateElement)) {
			ViewPointResource vpRes = null;
			if (candidateElement instanceof File) {
				vpRes = ViewPointResourceImpl.retrieveViewPointResource((File) candidateElement, getServiceManager());
			} else if (candidateElement instanceof InJarResourceImpl) {
				vpRes = ViewPointResourceImpl.retrieveViewPointResource((InJarResourceImpl) candidateElement, this.getServiceManager());
			}
			if (vpRes != null) {
				ViewPointRepository viewPointFileBasedRepository = getViewPointRepository(resourceCenter);
				registerResource(vpRes, viewPointFileBasedRepository, candidateElement);
				referenceResource(vpRes, resourceCenter);
				return vpRes;
			}
		}
		return null;
	}

	private void registerResource(ViewPointResource vpRes, ViewPointRepository repository, Object candidateElement) {
		try {
			if (vpRes != null) {
				logger.info("Found and register viewpoint " + vpRes.getURI() + vpRes.getFlexoIODelegate().toString());
				RepositoryFolder<ViewPointResource> folder;
				folder = repository.getRepositoryFolder(candidateElement, true);
				repository.registerResource(vpRes, folder);
			} else {
				logger.warning("While exploring resource center looking for viewpoints : cannot retrieve resource for element "
						+ candidateElement);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Check it might correspond to a viewpoint
	 * 
	 * @param candidateElement
	 * @return
	 */
	private boolean isValidViewPoint(Object candidateElement) {
		if (candidateElement instanceof File && isValidViewPointDirectory(((File) candidateElement))) {
			return true;
		}
		if (candidateElement instanceof InJarResourceImpl && isValidViewPointDirectory((InJarResourceImpl) candidateElement)) {
			return true;
		}
		return false;
	}

	/**
	 * Creates and return a view repository for current {@link TechnologyAdapter} and supplied {@link FlexoResourceCenter}
	 */
	public ViewPointRepository createViewPointRepository(final FlexoResourceCenter<?> resourceCenter) {
		final ViewPointRepository returned = new ViewPointRepository(this, resourceCenter);
		resourceCenter.registerRepository(returned, ViewPointRepository.class, this);
		return returned;
	}

	@Override
	public <I> boolean isIgnorable(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (resourceCenter.isIgnorable(contents)) {
			return true;
		}
		// TODO: ignore .viewpoint subcontents
		return false;
	}

	@Override
	public <I> void contentsAdded(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("FMLTechnologyAdapter: File ADDED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
				final File candidateFile = (File) contents;
				if (isValidViewPointDirectory(candidateFile)) {
					final ViewPointResource vpRes = analyseAsViewPoint(contents, resourceCenter);
					referenceResource(vpRes, resourceCenter);
				}
			}
		}
	}

	@Override
	public <I> void contentsDeleted(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("FMLTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
			}
		}
	}

}
