/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.CachingStrategy;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.localization.DefaultLocalizationService;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.nature.DefaultProjectNatureService;
import org.openflexo.foundation.nature.DefaultScreenshotService;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.nature.ScreenshotService;
import org.openflexo.foundation.project.FlexoProjectImpl.FlexoProjectReferenceLoader;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.task.ThreadPoolFlexoTaskManager;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * Default implementation of {@link FlexoServiceManager}
 * 
 * 
 * @author sylvain
 * 
 */
public class DefaultFlexoServiceManager extends FlexoServiceManager {

	/**
	 * Initialize a new {@link DefaultFlexoServiceManager}
	 * 
	 * @param localizationRelativePath
	 *            a String identifying a relative path to use for main localization (such as "FlexoLocalization/MyLocales") of the
	 *            application
	 * @param devMode
	 *            true when 'developer' mode set to true (enable more services)
	 */
	public DefaultFlexoServiceManager(String localizationRelativePath, boolean devMode) {

		DataBinding.setDefaultCachingStrategy(CachingStrategy.PRAGMATIC_CACHE);

		LocalizationService localizationService = createLocalizationService(localizationRelativePath);
		registerService(localizationService);

		FlexoEditingContext editingContext = createEditingContext();
		registerService(editingContext);

		FlexoTaskManager taskManager = createTaskManager();
		registerService(taskManager);

		ResourceManager resourceManager = createResourceManager();
		registerService(resourceManager);
		FlexoProjectReferenceLoader projectReferenceLoader = createProjectReferenceLoader();
		if (projectReferenceLoader != null) {
			registerService(projectReferenceLoader);
		}

		FlexoResourceCenterService resourceCenterService = createResourceCenterService();
		registerService(resourceCenterService);

		TechnologyAdapterService technologyAdapterService = createTechnologyAdapterService(resourceCenterService);
		registerService(technologyAdapterService);

		ProjectNatureService projectNatureService = createProjectNatureService();
		registerService(projectNatureService);

		VirtualModelLibrary virtualModelLibrary = createViewPointLibraryService();
		registerService(virtualModelLibrary);

		ScreenshotService screenshotService = createScreenshotService();
		registerService(screenshotService);

		ProjectLoader projectLoaderService = createProjectLoaderService();
		registerService(projectLoaderService);

	}

	@Override
	protected FlexoEditingContext createEditingContext() {
		return FlexoEditingContext.createInstance();
	}

	@Override
	protected ResourceManager createResourceManager() {
		return ResourceManager.createInstance();
	}

	@Override
	protected FlexoResourceCenterService createResourceCenterService() {
		return DefaultResourceCenterService.getNewInstance(false);
	}

	@Override
	protected TechnologyAdapterService createTechnologyAdapterService(FlexoResourceCenterService resourceCenterService) {
		return DefaultTechnologyAdapterService.getNewInstance(resourceCenterService);
	}

	@Override
	protected ProjectNatureService createProjectNatureService() {
		return DefaultProjectNatureService.getNewInstance();
	}

	@Override
	protected VirtualModelLibrary createViewPointLibraryService() {
		return new VirtualModelLibrary();
	}

	@Override
	protected FlexoTaskManager createTaskManager() {
		return ThreadPoolFlexoTaskManager.createInstance();
	}

	@Override
	protected ScreenshotService createScreenshotService() {
		return DefaultScreenshotService.createInstance();
	}

	@Override
	protected ProjectLoader createProjectLoaderService() {
		return new ProjectLoader();
	}

	@Override
	protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
		// Please override
		return null;
	}

	@Override
	protected FlexoEditor createApplicationEditor() {
		// Please override
		return null;
	}

	@Override
	protected LocalizationService createLocalizationService(String relativePath) {
		LocalizationService returned = new DefaultLocalizationService();
		returned.setGeneralLocalizerRelativePath(relativePath);
		return returned;
	}

	public String debug() {
		StringBuffer sb = new StringBuffer();
		sb.append("**********************************************\n");
		sb.append("FLEXO SERVICE MANAGER: " + getClass() + "\n");
		sb.append("**********************************************\n");
		sb.append("Registered services: " + getRegisteredServices().size() + "\n");
		for (FlexoService s : getRegisteredServices()) {
			sb.append("Service: " + s.getClass().getSimpleName() + "\n");
		}
		if (getTechnologyAdapterService() != null) {
			sb.append("**********************************************\n");
			sb.append("Technology Adapter Service: " + getTechnologyAdapterService().getClass().getSimpleName() + " technology adapters: "
					+ getTechnologyAdapterService().getTechnologyAdapters().size() + "\n");
			for (TechnologyAdapter ta : getTechnologyAdapterService().getTechnologyAdapters()) {
				sb.append("> " + ta.getName() + "\n");
			}
		}
		if (getResourceCenterService() != null) {
			sb.append("**********************************************\n");
			sb.append("Resource Center Service: " + getResourceCenterService().getClass().getSimpleName() + " resource centers: "
					+ getResourceCenterService().getResourceCenters().size() + "\n");
			for (FlexoResourceCenter<?> rc : getResourceCenterService().getResourceCenters()) {
				sb.append("> " + rc.getName() + "\n");
			}
		}
		if (getResourceManager() != null) {
			sb.append("**********************************************\n");
			sb.append("ResourceManager / Information Space\n");
			if (getTechnologyAdapterService() != null) {
				for (TechnologyAdapter ta : getTechnologyAdapterService().getTechnologyAdapters()) {
					for (ResourceRepository<?, ?> rep : getResourceManager().getAllRepositories(ta)) {
						System.out.println("Technology adapter: " + ta + " repository: " + rep + "\n");
						for (FlexoResource<?> r : rep.getAllResources()) {
							sb.append("> " + r.getURI() + "\n");
						}
					}
				}
			}
		}
		sb.append("**********************************************");
		return sb.toString();
	}
}
