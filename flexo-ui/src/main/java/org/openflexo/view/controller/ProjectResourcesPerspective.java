/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.view.controller;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBProjectResourcesBrowser;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.icon.IconLibrary;
import org.openflexo.inspector.FIBInspectorPanel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.model.FlexoPerspective;

public class ProjectResourcesPerspective extends FlexoPerspective {

	protected static final Logger logger = Logger.getLogger(ProjectResourcesPerspective.class.getPackage().getName());

	private final FIBProjectResourcesBrowser projectResourcesBrowser;

	private final FIBInspectorPanel inspectorPanel;

	/**
	 * @param controller
	 *            TODO
	 * @param name
	 */
	public ProjectResourcesPerspective(FlexoController controller) {
		super("resources_perspective", controller);

		projectResourcesBrowser = new FIBProjectResourcesBrowser(controller.getProject() != null ? controller.getProject() : null,
				controller);

		setTopLeftView(projectResourcesBrowser);

		// Initialized inspector panel
		inspectorPanel = new FIBInspectorPanel(controller.getModuleInspectorController());
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

	/*public VirtualModelInstanceView getCurrentVirtualModelInstanceView() {
		if (getController() != null && getController().getCurrentModuleView() instanceof VirtualModelInstanceView) {
			return (VirtualModelInstanceView) getController().getCurrentModuleView();
		}
		return null;
	}*/

	@Override
	public String getWindowTitleforObject(FlexoObject object, FlexoController controller) {
		if (object == null) {
			return FlexoLocalization.localizedForKey("no_selection");
		}
		if (object instanceof ViewLibrary) {
			return FlexoLocalization.localizedForKey("view_library");
		}
		if (object instanceof VirtualModelInstance) {
			return ((VirtualModelInstance) object).getTitle();
		}
		if (object instanceof View) {
			return ((View) object).getName();
		}
		if (object instanceof TechnologyObject) {
			return getWindowTitleForTechnologyObject((TechnologyObject<?>) object, getController());
		}
		return object.toString();
	}

	private <TA extends TechnologyAdapter> String getWindowTitleForTechnologyObject(TechnologyObject<TA> object, FlexoController controller) {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterController<TA> tac = tacService.getTechnologyAdapterController(object.getTechnologyAdapter());
		return tac.getWindowTitleforObject(object, controller);
	}

	private void setProject(FlexoProject project) {
		projectResourcesBrowser.setRootObject(project);
	}

	@Override
	public void updateEditor(FlexoEditor from, FlexoEditor to) {
		super.updateEditor(from, to);
		if (to != null) {
			setProject(to.getProject());
		}
	}
}
