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
package org.openflexo.components;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.FlexoProject;
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
		private final List<FlexoProject> projects;
		private List<FlexoProject> selected;

		public ProjectList(List<FlexoProject> projects) {
			this.projects = projects;
			this.selected = new ArrayList<FlexoProject>(projects);
		}

		public List<FlexoProject> getProjects() {
			return projects;
		}

		public List<FlexoProject> getSelected() {
			return selected;
		}

		public void setSelected(List<FlexoProject> selected) {
			List<FlexoProject> old = this.selected;
			this.selected = selected;
			getPropertyChangeSupport().firePropertyChange("selected", old, selected);
		}

		public void selectAll() {
			for (FlexoProject p : getProjects()) {
				if (!selected.contains(p)) {
					selected.add(p);
					getPropertyChangeSupport().firePropertyChange("selected", null, p);
				}
			}
		}

		public void deselectAll() {
			for (FlexoProject p : getProjects()) {
				if (selected.contains(p)) {
					selected.remove(p);
					getPropertyChangeSupport().firePropertyChange("selected", p, null);
				}
			}
		}
	}

	private boolean ok = false;

	public SaveProjectsDialog(FlexoController controller, List<FlexoProject> modifiedProjects) {
		data = new ProjectList(modifiedProjects);
		FIBDialog<ProjectList> dialog = FIBDialog.instanciateDialog(FIB_FILE_NAME, data, FlexoFrame.getActiveFrame(), true,
				FlexoLocalization.getMainLocalizer());
		if (dialog.getController() instanceof FlexoFIBController) {
			((FlexoFIBController) dialog.getController()).setFlexoController(controller);
		}
		dialog.setTitle(FlexoLocalization.localizedForKey("project_has_unsaved_changes"));
		dialog.showDialog();
		ok = dialog.getController().getStatus() == Status.YES;
	}

	public List<FlexoProject> getSelectedProject() {
		return data.getSelected();
	}

	public boolean isOk() {
		return ok;
	}
}
