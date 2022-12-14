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

package org.openflexo.foundation.nature;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultProjectNatureService extends FlexoServiceImpl implements ProjectNatureService {

	private static final Logger logger = Logger.getLogger(DefaultProjectNatureService.class.getPackage().getName());

	private Map<Class<?>, ProjectNatureFactory<?>> loadedProjectNatureFactories;

	public static ProjectNatureService getNewInstance() {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(ProjectNatureService.class);
			factory.setImplementingClassForInterface(DefaultProjectNatureService.class, ProjectNatureService.class);
			ProjectNatureService returned = factory.newInstance(ProjectNatureService.class);
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load all available project natures<br>
	 * Retrieve all {@link ProjectNature} available from classpath. <br>
	 * Map contains the {@link ProjectNature} class name as key and the {@link ProjectNature} itself as value.
	 * 
	 * @return the retrieved TechnologyModuleDefinition map.
	 */
	public void loadAvailableProjectNatureFactories() {
		if (loadedProjectNatureFactories == null) {
			loadedProjectNatureFactories = new Hashtable<>();
			logger.fine("Loading available project natures...");
			ServiceLoader<ProjectNatureFactory> loader = ServiceLoader.load(ProjectNatureFactory.class);
			for (ProjectNatureFactory<?> projectNatureFactory : loader) {
				registerProjectNatureFactory(projectNatureFactory);
			}
			logger.fine("Loading available project natures. Done.");
		}

	}

	private void registerProjectNatureFactory(ProjectNatureFactory<?> projectNatureFactory) {
		logger.info("Found " + projectNatureFactory);
		projectNatureFactory.setProjectNatureService(this);
		addToProjectNatureFactories(projectNatureFactory);

		logger.info("Load " + projectNatureFactory + " as " + projectNatureFactory.getProjectNatureClass());

		if (loadedProjectNatureFactories.containsKey(projectNatureFactory.getProjectNatureClass())) {
			logger.severe("Cannot include ProjectNature with classname '" + projectNatureFactory.getProjectNatureClass().getName()
					+ "' because it already exists !!!! A ProjectNature name MUST be unique !");
		}
		else {
			loadedProjectNatureFactories.put(projectNatureFactory.getProjectNatureClass(), projectNatureFactory);
		}
	}

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@Override
	public <F extends ProjectNatureFactory<N>, N extends ProjectNature<N>> F getProjectNatureFactory(Class<N> projectNatureClass) {
		return (F) loadedProjectNatureFactories.get(projectNatureClass);
	}

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@Override
	public ProjectNatureFactory<?> getProjectNatureFactory(String projectNatureClassName) {
		for (Class<?> c : loadedProjectNatureFactories.keySet()) {
			if (c.getName().equals(projectNatureClassName)) {
				return loadedProjectNatureFactories.get(c);
			}
		}
		return null;
	}

	/**
	 * Iterates over loaded technology adapters
	 * 
	 * @return
	 */
	public Collection<ProjectNatureFactory<?>> getLoadedProjectNatures() {
		return loadedProjectNatureFactories.values();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
	}

	@Override
	public String getServiceName() {
		return "ProjectNatureService";
	}

	@Override
	public void initialize() {
		loadAvailableProjectNatureFactories();
		status = Status.Started;
	}

}
