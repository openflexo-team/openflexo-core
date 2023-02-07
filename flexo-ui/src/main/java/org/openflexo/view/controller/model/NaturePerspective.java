/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.view.controller.model;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

public abstract class NaturePerspective<N extends ProjectNature<N>> extends FlexoPerspective {

	static final Logger logger = Logger.getLogger(NaturePerspective.class.getPackage().getName());

	public NaturePerspective(FlexoController controller) {
		super(controller);
	}

	public abstract Class<N> getNatureClass();

	@Override
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {

		if (object instanceof FlexoProject) {
			FlexoProject<?> project = (FlexoProject<?>) object;
			if (project.hasNature(getNatureClass())) {
				N nature = project.getNature(getNatureClass());
				return getModuleViewForProject(project, nature);
			}
			return getController().makeDefaultProjectView((FlexoProject<?>) object, this);
		}
		return super.createModuleViewForMasterObject(object);
	}

	public ModuleView<FlexoProject<?>> getModuleViewForProject(FlexoProject<?> project, N nature) {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = getController().getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<FlexoProject<?>> returned = tac.createFlexoProjectModuleViewForSpecificNature(project, nature, getController(),
					this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

}
