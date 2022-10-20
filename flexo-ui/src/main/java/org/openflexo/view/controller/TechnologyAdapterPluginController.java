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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterPlugin;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.FlexoPerspective;

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

	/**
	 * Indicates if supplied object is managed by this {@link TechnologyAdapterPlugin} regarding selection managing
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean handleObject(FlexoObject object);

	/**
	 * This method is used to specialize objects beeing managed by this {@link TechnologyAdapterPlugin}.
	 * 
	 * We provide here an indirection for the SelectionManager to consider a {@link FlexoObject}
	 * 
	 * @param object
	 * @return
	 */
	public abstract FlexoObject getRelevantObject(FlexoObject object);
	
	/**
	 * Return a boolean indicating if this {@link FlexoPerspective} handles supplied object by defining a {@link ModuleView} in which 
	 * supplied object is representable either as a master object or as an object representable in related view
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean isRepresentableInModuleView(FlexoObject object);
	
	/**
	 * Return {@link FlexoObject} for which this perspective defines a {@link ModuleView} where supplied object is also representable
	 * 
	 * @param object
	 * @return
	 */
	public abstract FlexoObject getRepresentableMasterObject(FlexoObject object);
	
	/**
	 * Return a newly created {@link ModuleView} for supplied technology object, when supported
	 * rendering
	 * 
	 * @param object
	 * @return
	 */
	public abstract ModuleView<?> createModuleViewForMasterObject(FlexoObject object, FlexoController controller,
			FlexoPerspective perspective);

	
}
