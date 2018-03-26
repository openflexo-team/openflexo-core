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

import org.openflexo.components.widget.FIBProjectResourcesBrowser;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
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
				controller, controller.getFlexoLocales());

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
			return FlexoLocalization.getMainLocalizer().localizedForKey("no_selection");
		}
		if (object instanceof FMLRTVirtualModelInstanceRepository) {
			return FlexoLocalization.getMainLocalizer().localizedForKey("virtual_model_instance_repository");
		}
		if (object instanceof FMLRTVirtualModelInstance) {
			return ((FMLRTVirtualModelInstance) object).getTitle();
		}
		if (object instanceof TechnologyObject) {
			return getWindowTitleForTechnologyObject((TechnologyObject<?>) object, getController());
		}
		return object.toString();
	}

	private <TA extends TechnologyAdapter<TA>> String getWindowTitleForTechnologyObject(TechnologyObject<TA> object,
			FlexoController controller) {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterController<TA> tac = tacService.getTechnologyAdapterController(object.getTechnologyAdapter());
		return tac.getWindowTitleforObject(object, controller);
	}

	private void setProject(FlexoProject<?> project) {
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
