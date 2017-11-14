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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.ModelRepository;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * A repository storing {@link FMLRTVirtualModelInstanceResource} for a resource center
 * 
 * @author sylvain
 * 
 */
@ModelEntity
public interface FMLRTVirtualModelInstanceRepository<I> extends
		ModelRepository<FMLRTVirtualModelInstanceResource, FMLRTVirtualModelInstance, VirtualModel, FMLRTTechnologyAdapter, FMLTechnologyAdapter, I> {

	public static <I> FMLRTVirtualModelInstanceRepository<I> instanciateNewRepository(FMLRTTechnologyAdapter technologyAdapter,
			FlexoResourceCenter<I> resourceCenter) throws IOException {
		ModelFactory factory;
		try {
			factory = new ModelFactory(FMLRTVirtualModelInstanceRepository.class);
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

	public static abstract class FMLRTVirtualModelInstanceRepositoryImpl<I> extends
			ModelRepositoryImpl<FMLRTVirtualModelInstanceResource, FMLRTVirtualModelInstance, VirtualModel, FMLRTTechnologyAdapter, FMLTechnologyAdapter, I> {

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getResourceCenter() != null) {
				return getResourceCenter().getServiceManager();
			}
			return null;
		}

		public List<FMLRTVirtualModelInstance> getVirtualModelInstancesConformToVirtualModel(String virtualModelURI) {
			List<FMLRTVirtualModelInstance> views = new ArrayList<>();
			for (FMLRTVirtualModelInstanceResource vmiRes : getAllResources()) {
				if (vmiRes.getVirtualModelResource() != null && vmiRes.getVirtualModelResource().getURI().equals(virtualModelURI)) {
					views.add(vmiRes.getVirtualModelInstance());
				}
			}
			return views;
		}

		public boolean isValidForANewVirtualModelInstanceName(String value) {
			if (value == null) {
				return false;
			}
			return getRootFolder().isValidResourceName(value);
		}

		public FMLRTVirtualModelInstanceResource getVirtualModelInstanceResourceNamed(String value) {
			if (value == null) {
				return null;
			}
			return getRootFolder().getResourceWithName(value);
		}

		public FMLRTVirtualModelInstanceResource getVirtualModelInstance(String virtualModelInstanceURI) {
			if (virtualModelInstanceURI == null) {
				return null;
			}
			return getResource(virtualModelInstanceURI);
		}

		/*public VirtualModelInstanceResource<?, ?> getVirtualModelInstance(String virtualModelInstanceURI) {
			if (virtualModelInstanceURI == null) {
				return null;
			}
			if (getView(virtualModelInstanceURI) != null) {
				return getView(virtualModelInstanceURI);
			}
			// System.out.println("lookup mvi " + virtualModelInstanceURI);
			String viewURI = virtualModelInstanceURI.substring(0, virtualModelInstanceURI.lastIndexOf("/"));
			// System.out.println("lookup view " + viewURI);
			ViewResource vr = getView(viewURI);
			if (vr != null) {
				for (VirtualModelInstanceResource<?, ?> vmir : vr.getContents(VirtualModelInstanceResource.class)) {
					if (vmir.getURI().equals(virtualModelInstanceURI)) {
						// System.out.println("Found " + vmir.getURI());
						return vmir;
					}
				}
			}
			else {
				logger.info("Cannot find View '" + viewURI + "' in '" + getDefaultBaseURI() + "'");
			}
			logger.info("Cannot find FMLRTVirtualModelInstance '" + virtualModelInstanceURI + "' in '" + getDefaultBaseURI() + "'");
			return null;
		}*/
	}
}
