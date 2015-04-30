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
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultProjectNatureService extends FlexoServiceImpl implements ProjectNatureService {

	private static final Logger logger = Logger.getLogger(DefaultProjectNatureService.class.getPackage().getName());

	private Map<Class, ProjectNature<?, ?>> loadedProjectNatures;

	public static ProjectNatureService getNewInstance() {
		try {
			ModelFactory factory = new ModelFactory(ProjectNatureService.class);
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
	public void loadAvailableProjectNatures() {
		if (loadedProjectNatures == null) {
			loadedProjectNatures = new Hashtable<Class, ProjectNature<?, ?>>();
			logger.info("Loading available project natures...");
			ServiceLoader<ProjectNature> loader = ServiceLoader.load(ProjectNature.class);
			Iterator<ProjectNature> iterator = loader.iterator();
			while (iterator.hasNext()) {
				ProjectNature projectNature = iterator.next();
				registerProjectNature(projectNature);
			}
			logger.info("Loading available project natures. Done.");
		}

	}

	private void registerProjectNature(ProjectNature projectNature) {
		logger.info("Found " + projectNature);
		projectNature.setProjectNatureService(this);
		addToProjectNatures(projectNature);

		logger.info("Load " + projectNature + " as " + projectNature.getClass());

		if (loadedProjectNatures.containsKey(projectNature.getClass())) {
			logger.severe("Cannot include ProjectNature with classname '" + projectNature.getClass().getName()
					+ "' because it already exists !!!! A ProjectNature name MUST be unique !");
		} else {
			loadedProjectNatures.put(projectNature.getClass(), projectNature);
		}
	}

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@Override
	public <N extends ProjectNature<?, ?>> N getProjectNature(Class<N> projectNatureClass) {
		return (N) loadedProjectNatures.get(projectNatureClass);
	}

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@Override
	public ProjectNature<?, ?> getProjectNature(String projectNatureClassName) {
		for (Class<?> c : loadedProjectNatures.keySet()) {
			if (c.getName().equals(projectNatureClassName)) {
				return loadedProjectNatures.get(c);
			}
		}
		return null;
	}

	/**
	 * Iterates over loaded technology adapters
	 * 
	 * @return
	 */
	public Collection<ProjectNature<?, ?>> getLoadedProjectNatures() {
		return loadedProjectNatures.values();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
	}

	@Override
	public void initialize() {
		loadAvailableProjectNatures();
	}

}
