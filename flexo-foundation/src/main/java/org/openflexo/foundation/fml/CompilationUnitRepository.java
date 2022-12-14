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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * A {@link CompilationUnitRepository} references {@link CompilationUnitResource} stored in a given {@link FlexoResourceCenter}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(CompilationUnitRepository.CompilationUnitRepositoryImpl.class)
public interface CompilationUnitRepository<I>
		extends TechnologyAdapterResourceRepository<CompilationUnitResource, FMLTechnologyAdapter, FMLCompilationUnit, I> {

	public List<CompilationUnitResource> getTopLevelCompilationUnitResources();

	public static <I> CompilationUnitRepository<I> instanciateNewRepository(FMLTechnologyAdapter technologyAdapter,
			FlexoResourceCenter<I> resourceCenter) {
		PamelaModelFactory factory;
		try {
			factory = new PamelaModelFactory(CompilationUnitRepository.class);
			CompilationUnitRepository<I> newRepository = factory.newInstance(CompilationUnitRepository.class);
			newRepository.setTechnologyAdapter(technologyAdapter);
			newRepository.setResourceCenter(resourceCenter);
			newRepository.setBaseArtefact(resourceCenter.getBaseArtefact());
			newRepository.getRootFolder().setRepositoryContext(null);
			return newRepository;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static abstract class CompilationUnitRepositoryImpl<I>
			extends TechnologyAdapterResourceRepositoryImpl<CompilationUnitResource, FMLTechnologyAdapter, FMLCompilationUnit, I>
			implements CompilationUnitRepository<I> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TechnologyAdapterResourceRepository.class.getPackage().getName());

		private List<CompilationUnitResource> topLevelCompilationUnitResources = null;

		@Override
		public List<CompilationUnitResource> getTopLevelCompilationUnitResources() {
			if (topLevelCompilationUnitResources == null) {
				topLevelCompilationUnitResources = new ArrayList<>();
				for (CompilationUnitResource r : getAllResources()) {
					if (r.getContainer() == null) {
						topLevelCompilationUnitResources.add(r);
					}
				}
			}
			return topLevelCompilationUnitResources;
		}

		@Override
		public void unregisterResource(CompilationUnitResource flexoResource) {
			super.unregisterResource(flexoResource);
			topLevelCompilationUnitResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelCompilationUnitResources", null, getTopLevelCompilationUnitResources());
		}

		@Override
		public void registerResource(CompilationUnitResource resource, RepositoryFolder<CompilationUnitResource, I> parentFolder) {
			super.registerResource(resource, parentFolder);
			topLevelCompilationUnitResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelCompilationUnitResources", null, getTopLevelCompilationUnitResources());
		}

		@Override
		public void registerResource(CompilationUnitResource resource, CompilationUnitResource parentResource) {
			super.registerResource(resource, parentResource);
			topLevelCompilationUnitResources = null;
			getPropertyChangeSupport().firePropertyChange("topLevelCompilationUnitResources", null, getTopLevelCompilationUnitResources());
		}

	}
}
