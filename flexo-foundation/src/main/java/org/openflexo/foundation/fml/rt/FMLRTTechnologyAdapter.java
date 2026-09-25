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
 * The technology adapter of FML@RT, under the identifier {@code FML@RT}: it manages the instances of virtual models.
 * <p>
 * It declares the {@link FMLRTModelSlot} — the model slot giving access to a {@link FMLRTVirtualModelInstance}, written
 * {@code use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;} in FML — the resource of a {@code .fml.rt} instance (see
 * {@link org.openflexo.foundation.fml.rt.rm}), and the instance types. It also holds, per resource center, the repository of the instances
 * found there.
 * <p>
 * Exploration ignores any directory nested in a {@code .fml.rt} one: a contained instance is not discovered on its own, but explored from
 * its container.
 *
 * @author sylvain
 *
 */
@DeclareModelSlots({ FMLRTModelSlot.class })
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
	 * Creates and return a new {@link AbstractFMLRTModelSlot} adressing supplied VirtualModel.<br>
	 * 
	 * @param modelSlotClass
	 * @param containerVirtualModel
	 *            the virtual model in which model slot should be created
	 * @param addressedVirtualModel
	 *            the virtual model referenced by the model slot
	 * @return
	 */
	public AbstractFMLRTModelSlot makeVirtualModelModelSlot(final VirtualModel containerVirtualModel, final VirtualModel addressedVirtualModel) {
		final AbstractFMLRTModelSlot returned = this.makeModelSlot(AbstractFMLRTModelSlot.class, containerVirtualModel);
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
			// Another thread may have registered one meanwhile: use the registered one (CORE-D-25)
			returned = resourceCenter.registerRepository(returned, FMLRTVirtualModelInstanceRepository.class, this);
		}
		return returned;
	}

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
