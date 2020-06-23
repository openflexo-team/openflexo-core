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
import java.util.List;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceFactories;
import org.openflexo.foundation.fml.annotations.DeclareTechnologySpecificTypes;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;

/**
 * This class defines and implements the Openflexo built-in FML@runtime technology adapter<br>
 * 
 * This adapter allows to manage {@link VirtualModelInstance} and {@link FMLRTVirtualModelInstance} resources in Openflexo infrastructure.
 * 
 * @author sylvain
 * 
 */
@DeclareModelSlots({ FMLRTVirtualModelInstanceModelSlot.class })
@DeclareTechnologySpecificTypes({ FlexoConceptInstanceType.class, VirtualModelInstanceType.class })
@DeclareResourceFactories({ FMLRTVirtualModelInstanceResourceFactory.class })
public class FMLRTTechnologyAdapter extends TechnologyAdapter<FMLRTTechnologyAdapter> {
	public FMLRTTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "FML@runtime technology adapter";
	}

	@Override
	protected void initResourceFactories() {
		super.initResourceFactories();
		getAvailableResourceTypes().add(FMLRTVirtualModelInstanceResource.class);
	}

	@Override
	protected String getLocalizationDirectory() {
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

	@Override
	public FlexoServiceManager getServiceManager() {
		return this.getTechnologyAdapterService().getServiceManager();
	}

	@Override
	public void ensureAllRepositoriesAreCreated(FlexoResourceCenter<?> rc) {
		super.ensureAllRepositoriesAreCreated(rc);
		getVirtualModelInstanceRepository(rc);
	}

	public <I> FMLRTVirtualModelInstanceRepository<I> getVirtualModelInstanceRepository(FlexoResourceCenter<I> resourceCenter) {
		FMLRTVirtualModelInstanceRepository<I> returned = resourceCenter.retrieveRepository(FMLRTVirtualModelInstanceRepository.class,
				this);
		if (returned == null) {
			returned = FMLRTVirtualModelInstanceRepository.instanciateNewRepository(this, resourceCenter);
			resourceCenter.registerRepository(returned, FMLRTVirtualModelInstanceRepository.class, this);
		}
		return returned;
	}

	/*@Override
	public <I> void performInitializeResourceCenter(final FlexoResourceCenter<I> resourceCenter) {
	
		final FMLRTVirtualModelInstanceRepository viewRepository = this.getViewRepository(resourceCenter);
	
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
	}*/

	/**
	 * Return boolean indicating if supplied {@link File} has the general form of a ViewPoint directory
	 * 
	 * @param candidateFile
	 * @return
	 */
	/*private boolean isValidViewDirectory(final File candidateFile) {
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
	}*/

	/**
	 * Build and return {@link ViewResource} from a candidate file (a .view directory)<br>
	 * Register this {@link ViewResource} in the supplied {@link Viewepository} as well as in the {@link ViewLibrary} (repository for a
	 * FlexoProject)
	 * 
	 * @param candidateFile
	 * @param viewPointRepository
	 * @return the newly created {@link ViewPointResource}
	 */
	/*private ViewResource analyseAsView(final File candidateFile, final FMLRTVirtualModelInstanceRepository viewRepository) {
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
	}*/

	@Override
	public <I> boolean isIgnorable(final FlexoResourceCenter<I> resourceCenter, final I contents) {
		if (resourceCenter.isIgnorable(contents, this)) {
			return true;
		}

		// This allows to ignore all contained VirtualModel, that will be explored from their container resource
		if (resourceCenter.isDirectory(contents)) {
			if (FlexoResourceCenter.isContainedInDirectoryWithSuffix(resourceCenter, contents,
					FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected <I> boolean isFolderIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
		if (resourceCenter.isDirectory(contents)) {
			if (FlexoResourceCenter.isContainedInDirectoryWithSuffix(resourceCenter, contents,
					FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getIdentifier() {
		return "FML@RT";
	}

	public FMLRTVirtualModelInstanceResourceFactory getFMLRTVirtualModelInstanceResourceFactory() {
		return getResourceFactory(FMLRTVirtualModelInstanceResourceFactory.class);
	}

	public List<FMLRTVirtualModelInstanceRepository<?>> getVirtualModelInstanceRepositories() {
		List<FMLRTVirtualModelInstanceRepository<?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
			returned.add(getVirtualModelInstanceRepository(rc));
		}
		return returned;
	}

	@Override
	public void notifyRepositoryStructureChanged() {
		super.notifyRepositoryStructureChanged();
		getPropertyChangeSupport().firePropertyChange("getVirtualModelInstanceRepositories()", null, getVirtualModelInstanceRepositories());
	}

}
