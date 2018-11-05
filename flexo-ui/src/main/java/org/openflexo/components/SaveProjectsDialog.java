/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

package org.openflexo.components;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * @author gpolet
 * 
 */
public class SaveProjectsDialog {

	public static final Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/SaveProjects.fib");
	private final ProjectList data;

	public static class ProjectList extends PropertyChangedSupportDefaultImplementation {
		private final List<FlexoProject<?>> projects;
		private List<FlexoProject<?>> selected;

		public ProjectList(List<FlexoProject<?>> projects) {
			this.projects = projects;
			this.selected = new ArrayList<>(projects);
		}

		public List<FlexoProject<?>> getProjects() {
			return projects;
		}

		public List<FlexoProject<?>> getSelected() {
			return selected;
		}

		public void setSelected(List<FlexoProject<?>> selected) {
			List<FlexoProject<?>> old = this.selected;
			this.selected = selected;
			getPropertyChangeSupport().firePropertyChange("selected", old, selected);
		}

		public void selectAll() {
			for (FlexoProject<?> p : getProjects()) {
				if (!selected.contains(p)) {
					selected.add(p);
					getPropertyChangeSupport().firePropertyChange("selected", null, p);
				}
			}
		}

		public void deselectAll() {
			for (FlexoProject<?> p : getProjects()) {
				if (selected.contains(p)) {
					selected.remove(p);
					getPropertyChangeSupport().firePropertyChange("selected", p, null);
				}
			}
		}
	}

	private boolean ok = false;

	public SaveProjectsDialog(FlexoController controller, List<FlexoProject<?>> modifiedProjects) {
		data = new ProjectList(modifiedProjects);
		JFIBDialog<ProjectList> dialog = JFIBDialog.instanciateDialog(FIB_FILE_NAME, data,
				controller.getApplicationContext().getApplicationFIBLibraryService().getApplicationFIBLibrary(),
				FlexoFrame.getActiveFrame(), true, FlexoLocalization.getMainLocalizer());
		if (dialog.getController() instanceof FlexoFIBController) {
			((FlexoFIBController) dialog.getController()).setFlexoController(controller);
		}
		dialog.setTitle(controller.getFlexoLocales().localizedForKey("project_has_unsaved_changes"));
		dialog.showDialog();
		ok = dialog.getController().getStatus() == Status.YES;
	}

	public List<FlexoProject<?>> getSelectedProject() {
		return data.getSelected();
	}

	public boolean isOk() {
		return ok;
	}
}
