/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.util.logging.Logger;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterPlugin;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.ResourceLocator;

/**
 * This class represents a technology-specific plugin
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapterPluginController<TA extends TechnologyAdapter<TA>> extends TechnologyAdapterPlugin<TA> {

	static final Logger logger = Logger.getLogger(TechnologyAdapterPluginController.class.getPackage().getName());

	private LocalizedDelegate locales;

	/**
	 * Return TechnologyAdapterController
	 * 
	 * @return
	 */
	public final TechnologyAdapterController<TA> getTargetTechnologyAdapterController() {
		return getTechnologyAdapterControllerService().getTechnologyAdapterController(getTargetTechnologyAdapter());
	}

	/**
	 * Called to activate the {@link TechnologyAdapterController} We do it for all loaded modules. This means that all features and GUIs
	 * available with this technology adapter will be made available to module<br>
	 * 
	 * From a technical point of view, we first initialize inspectors and then actions
	 */
	public void activate(FlexoModule<?> module) {

		if (locales == null) {
			initLocales();
		}

		logger.info("Activate plugin " + getClass() + "for module " + module);

		FlexoController controller = module.getFlexoController();
		if (controller != null) {
			initializeActions(controller.getControllerActionInitializer());
		}
	}

	/**
	 * Initialize actions for supplied module using supplied {@link ControllerActionInitializer}
	 * 
	 * @param actionInitializer
	 */
	protected abstract void initializeActions(ControllerActionInitializer actionInitializer);

	public abstract boolean isActivable(FlexoModule<?> module);

	public TechnologyAdapterControllerService getTechnologyAdapterControllerService() {
		return getServiceManager().getService(TechnologyAdapterControllerService.class);
	}

	private void initLocales() {
		locales = new LocalizedDelegateImpl(ResourceLocator.locateResource(getLocalizationDirectory()),
				getServiceManager().getLocalizationService().getFlexoLocalizer(),
				getServiceManager().getLocalizationService().getAutomaticSaving(), true);
	}

	protected abstract String getLocalizationDirectory();

	public LocalizedDelegate getLocales() {
		return locales;
	}

}
