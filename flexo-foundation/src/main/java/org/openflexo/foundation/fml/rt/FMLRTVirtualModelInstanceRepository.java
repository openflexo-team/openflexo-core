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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * A repository storing {@link FMLRTVirtualModelInstanceResource} for a resource center
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLRTVirtualModelInstanceRepository.FMLRTVirtualModelInstanceRepositoryImpl.class)
public interface FMLRTVirtualModelInstanceRepository<I> extends
		TechnologyAdapterResourceRepository<FMLRTVirtualModelInstanceResource, FMLRTTechnologyAdapter, FMLRTVirtualModelInstance, I> {

	public static <I> FMLRTVirtualModelInstanceRepository<I> instanciateNewRepository(FMLRTTechnologyAdapter technologyAdapter,
			FlexoResourceCenter<I> resourceCenter) {
		PamelaModelFactory factory;
		try {
			factory = new PamelaModelFactory(FMLRTVirtualModelInstanceRepository.class);
			FMLRTVirtualModelInstanceRepository<I> newRepository = factory.newInstance(FMLRTVirtualModelInstanceRepository.class);
			newRepository.setTechnologyAdapter(technologyAdapter);
			newRepository.setResourceCenter(resourceCenter);
			newRepository.setBaseArtefact(resourceCenter.getBaseArtefact());
			newRepository.getRootFolder().setRepositoryContext(resourceCenter.getLocales().localizedForKey("[Models]"));
			return newRepository;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public FMLRTVirtualModelInstanceResource getVirtualModelInstance(String virtualModelInstanceURI);

	public FMLRTVirtualModelInstanceResource getVirtualModelInstanceResourceNamed(String value);

	public List<FMLRTVirtualModelInstance> getVirtualModelInstancesConformToVirtualModel(String virtualModelURI);

	public boolean isValidForANewVirtualModelInstanceName(String value);

	public List<FMLRTVirtualModelInstanceResource> getTopLevelVirtualModelInstanceResources();

	public static abstract class FMLRTVirtualModelInstanceRepositoryImpl<I> extends
			TechnologyAdapterResourceRepositoryImpl<FMLRTVirtualModelInstanceResource, FMLRTTechnologyAdapter, FMLRTVirtualModelInstance, I>
			// ModelRepositoryImpl<FMLRTVirtualModelInstanceResource, FMLRTVirtualModelInstance, VirtualModel, FMLRTTechnologyAdapter,
			// FMLTechnologyAdapter, I>
			implements FMLRTVirtualModelInstanceRepository<I> {

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getResourceCenter() != null) {
				return getResourceCenter().getServiceManager();
			}
			return null;
		}

		@Override
		public List<FMLRTVirtualModelInstance> getVirtualModelInstancesConformToVirtualModel(String virtualModelURI) {
			List<FMLRTVirtualModelInstance> views = new ArrayList<>();
			for (FMLRTVirtualModelInstanceResource vmiRes : getAllResources()) {
				if (vmiRes.getVirtualModelResource() != null && vmiRes.getVirtualModelResource().getURI().equals(virtualModelURI)) {
					views.add(vmiRes.getVirtualModelInstance());
				}
			}
			return views;
		}

		@Override
		public boolean isValidForANewVirtualModelInstanceName(String value) {
			if (value == null) {
				return false;
			}
			return getRootFolder().isValidResourceName(value);
		}

		@Override
		public FMLRTVirtualModelInstanceResource getVirtualModelInstanceResourceNamed(String value) {
			if (value == null) {
				return null;
			}
			return getRootFolder().getResourceWithName(value);
		}

		@Override
		public FMLRTVirtualModelInstanceResource getVirtualModelInstance(String virtualModelInstanceURI) {
			if (virtualModelInstanceURI == null) {
				return null;
			}
			return getResource(virtualModelInstanceURI);
		}

		private List<FMLRTVirtualModelInstanceResource> topLevelVirtualModelInstanceResources = null;

		@Override
		public List<FMLRTVirtualModelInstanceResource> getTopLevelVirtualModelInstanceResources() {
			if (topLevelVirtualModelInstanceResources == null) {
				topLevelVirtualModelInstanceResources = new ArrayList<>();
				for (FMLRTVirtualModelInstanceResource r : getAllResources()) {
					if (r.getContainer() == null) {
						topLevelVirtualModelInstanceResources.add(r);
					}
				}
			}
			return topLevelVirtualModelInstanceResources;
		}

		@Override
		public void unregisterResource(FMLRTVirtualModelInstanceResource flexoResource) {
			super.unregisterResource(flexoResource);
			topLevelVirtualModelInstanceResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelVirtualModelInstanceResources", null,
					getTopLevelVirtualModelInstanceResources());
		}

		@Override
		public void registerResource(FMLRTVirtualModelInstanceResource resource,
				RepositoryFolder<FMLRTVirtualModelInstanceResource, I> parentFolder) {
			super.registerResource(resource, parentFolder);
			topLevelVirtualModelInstanceResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelVirtualModelInstanceResources", null,
					getTopLevelVirtualModelInstanceResources());
		}

		@Override
		public void registerResource(FMLRTVirtualModelInstanceResource resource, FMLRTVirtualModelInstanceResource parentResource) {
			super.registerResource(resource, parentResource);
			topLevelVirtualModelInstanceResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelVirtualModelInstanceResources", null,
					getTopLevelVirtualModelInstanceResources());
		}

	}
}
