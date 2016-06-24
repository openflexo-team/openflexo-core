/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.annotations.DeclareResourceTypes;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceImpl;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
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
@DeclareResourceTypes({ ViewPointResource.class, VirtualModelResource.class })
public class FMLTechnologyAdapter extends TechnologyAdapter {

	private static final Logger logger = Logger.getLogger(FMLTechnologyAdapter.class.getPackage().getName());

	public FMLTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "FML technology adapter";
	}

	@Override
	public String getLocalizationDirectory() {
		return "FlexoLocalization/FMLTechnologyAdapter";
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
	public <I> void performInitializeResourceCenter(final FlexoResourceCenter<I> resourceCenter) {
		// Take care that this call will create the ViewPointRepository
		// Do not comment this line
		final ViewPointRepository viewPointRepository = getViewPointRepository(resourceCenter);

		// @FD: this is an other way to remove the warning ;-)
		System.out.println("I'm beeing using the " + viewPointRepository);

		// Iterate
		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			final I item = it.next();
			if (!this.isIgnorable(resourceCenter, item)) {
				// if (item instanceof File) {
				// final File candidateFile = (File) item;
				// if (this.isValidViewPointDirectory(item)) {
				// FD unused final ViewPointResource vpRes =
				analyseAsViewPoint(item, resourceCenter);
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
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					getPropertyChangeSupport().firePropertyChange("getAllRepositories()", null, resourceCenter);
				}
			});
		}
		else {
			getPropertyChangeSupport().firePropertyChange("getAllRepositories()", null, resourceCenter);
		}
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
		if (candidateJar.getRelativePath().endsWith(".xml") && candidateJar.getRelativePath()
				.endsWith(candidateJarName + ViewPointResource.VIEWPOINT_SUFFIX + "/" + candidateJarName + ".xml")) {
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
				vpRes = ViewPointResourceImpl.retrieveViewPointResource((File) candidateElement, resourceCenter, getServiceManager());
			}
			else if (candidateElement instanceof InJarResourceImpl) {
				vpRes = ViewPointResourceImpl.retrieveViewPointResource((InJarResourceImpl) candidateElement, resourceCenter,
						getServiceManager());
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
			}
			else {
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
	public <I> boolean contentsAdded(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				final File candidateFile = (File) contents;
				System.out.println("FMLTechnologyAdapter: File ADDED " + candidateFile.getName() + " in "
						+ candidateFile.getParentFile().getAbsolutePath());
				if (isValidViewPointDirectory(candidateFile)) {
					final ViewPointResource vpRes = analyseAsViewPoint(contents, resourceCenter);
					if (vpRes != null) {
						referenceResource(vpRes, resourceCenter);
						return true;
					}
					else
						return false;
				}
			}
		}
		return false;
	}

	@Override
	public <I> boolean contentsDeleted(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("FMLTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in "
						+ ((File) contents).getParentFile().getAbsolutePath());
			}
		}
		return false;
	}

	@Override
	public <I> boolean contentsModified(FlexoResourceCenter<I> resourceCenter, I contents) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <I> boolean contentsRenamed(FlexoResourceCenter<I> resourceCenter, I contents, String oldName, String newName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getIdentifier() {
		return "FML";
	}

}
