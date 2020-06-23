/**
 * 
 * Copyright (c) 2014, Openflexo
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

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBResourceManagerBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.icon.IconLibrary;
import org.openflexo.module.FlexoModule.WelcomePanel;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * A perspective allowing to show and edit resources using loaded technology adapters<br>
 * 
 * Viewing and editing teehnology-specific resources is delegated to specific {@link TechnologyAdapterController}
 * 
 * @author sylvain
 * 
 */
public class GenericPerspective extends FlexoPerspective {

	static final Logger logger = Logger.getLogger(GenericPerspective.class.getPackage().getName());

	private final FIBResourceManagerBrowser browser;

	/**
	 * @param controller
	 * @param name
	 */
	public GenericPerspective(FlexoController controller) {
		super("Generic", controller);
		browser = controller.getSharedBrowser();
		setTopLeftView(browser);

	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return IconLibrary.OPENFLEXO_NOTEXT_16;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getWindowTitleforObject(FlexoObject object, FlexoController controller) {
		if (object instanceof WelcomePanel) {
			return "Welcome";
		}
		if (object instanceof FlexoProject) {
			return ((FlexoProject<?>) object).getName();
		}
		if (object instanceof TechnologyObject) {
			TechnologyAdapter ta = ((TechnologyObject<?>) object).getTechnologyAdapter();
			if (ta == null) {
				return null;
			}
			TechnologyAdapterController<?> tac = controller.getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			if (tac == null) {
				return null;
			}
			return tac.getWindowTitleforObject((TechnologyObject) object, controller);
		}
		if (object != null) {
			return object.toString();
		}
		logger.warning("Unexpected null object here");
		return null;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof WelcomePanel) {
			return true;
		}
		if (object instanceof TechnologyObject) {
			TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
			TechnologyAdapterController<?> tac = tacService
					.getTechnologyAdapterController(((TechnologyObject) object).getTechnologyAdapter());
			if (tac == null) {
				return false;
			}
			return tac.hasModuleViewForObject((TechnologyObject) object, getController());
		}
		return super.hasModuleViewForObject(object);
	}

	/**
	 * Hook triggered when a perspective is about to be shown
	 */
	@Override
	public void willShow() {
		System.out.println("GenericPerspective will be activated");
		super.willShow();
	}

}
