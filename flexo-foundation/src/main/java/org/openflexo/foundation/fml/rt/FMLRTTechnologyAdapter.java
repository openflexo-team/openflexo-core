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

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.ViewType;
import org.openflexo.foundation.fml.ViewType.ViewTypeFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType.VirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceTypes;
import org.openflexo.foundation.fml.annotations.DeclareTechnologySpecificTypes;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.ViewResourceImpl;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * This class defines and implements the Openflexo built-in FML@runtime technology adapter<br>
 * 
 * This adapter allows to manage {@link View} and {@link VirtualModelInstance} resources in Openflexo infrastructure.
 * 
 * @author sylvain
 * 
 */
@DeclareModelSlots({ VirtualModelInstanceModelSlot.class, ViewModelSlot.class })
@DeclareTechnologySpecificTypes({ FlexoConceptInstanceType.class, VirtualModelInstanceType.class, ViewType.class })
@DeclareResourceTypes({ ViewResource.class, VirtualModelInstanceResource.class })
public class FMLRTTechnologyAdapter extends TechnologyAdapter {

	private static final Logger logger = Logger.getLogger(FMLRTTechnologyAdapter.class.getPackage().getName());

	public FMLRTTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "FML@runtime technology adapter";
	}

	@Override
	public String getLocalizationDirectory() {
		return "FlexoLocalization/FMLRTTechnologyAdapter";
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
		returned.setAccessedVirtualModel(addressedVirtualModel);
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
	public FMLRTTechnologyContextManager getTechnologyContextManager() {
		return (FMLRTTechnologyContextManager) super.getTechnologyContextManager();
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
	public <I> void performInitializeResourceCenter(final FlexoResourceCenter<I> resourceCenter) {

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
		notifyRepositoryStructureChanged();
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
			if (candidateFile.getParentFile().getName().endsWith(ViewResource.VIEW_SUFFIX)) {
				// We dont try to interpret here a sub-view in a view
				return false;
			}
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
			}
			else {
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
		final ViewRepository returned = new ViewLibrary(this,resourceCenter);
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
	public <I> boolean contentsAdded(final FlexoResourceCenter<I> resourceCenter, final I contents) {
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
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public <I> boolean contentsDeleted(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (!this.isIgnorable(resourceCenter, contents)) {
			if (contents instanceof File) {
				System.out.println("FMLRTTechnologyAdapter: File DELETED " + ((File) contents).getName() + " in "
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

	private FlexoConceptInstanceTypeFactory fciFactory;
	private VirtualModelInstanceTypeFactory vmiFactory;
	private ViewTypeFactory viewFactory;

	@Override
	public void initTechnologySpecificTypes(TechnologyAdapterService taService) {
		taService.registerTypeClass(FlexoConceptInstanceType.class, getFlexoConceptInstanceTypeFactory());
		taService.registerTypeClass(VirtualModelInstanceType.class, getVirtualModelInstanceTypeFactory());
		taService.registerTypeClass(ViewType.class, getViewTypeFactory());
	}

	protected FlexoConceptInstanceTypeFactory getFlexoConceptInstanceTypeFactory() {
		if (fciFactory == null) {
			fciFactory = new FlexoConceptInstanceTypeFactory(this);
		}
		return fciFactory;
	}

	protected VirtualModelInstanceTypeFactory getVirtualModelInstanceTypeFactory() {
		if (vmiFactory == null) {
			vmiFactory = new VirtualModelInstanceTypeFactory(this);
		}
		return vmiFactory;
	}

	protected ViewTypeFactory getViewTypeFactory() {
		if (viewFactory == null) {
			viewFactory = new ViewTypeFactory(this);
		}
		return viewFactory;
	}

	@Override
	public String getIdentifier() {
		return "FML@RT";
	}

}
