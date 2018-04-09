/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

package org.openflexo.view.menu;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.components.NewProjectComponent;
import org.openflexo.components.OpenProjectComponent;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.NatureSpecificModule;
import org.openflexo.print.PrintManagingController;
import org.openflexo.project.InteractiveProjectLoader;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * 'File' menu
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public class FileMenu extends FlexoMenu {

	static final Logger logger = Logger.getLogger(FileMenu.class.getPackage().getName());

	public JMenu recentProjectMenu;

	public JMenu exportMenu;

	public JMenu importMenu;

	protected FlexoController _controller;

	protected FileMenu(FlexoController controller, boolean empty) {
		super("file", controller);
		_controller = controller;
		if (!empty) {
			initDefaultEntries();
		}
	}

	protected FileMenu(FlexoController controller) {
		super("file", controller);
		_controller = controller;
		initDefaultEntries();
	}

	private void initDefaultEntries() {

		add(new NewProjectItem());
		add(new OpenProjectItem());
		add(recentProjectMenu = new JMenu());
		recentProjectMenu.setText(_controller.getFlexoLocales().localizedForKey("recent_projects", recentProjectMenu));
		add(new CloseProjectItem());
		add(new ImportProjectMenuItem());
		add(new SaveProjectItem());
		add(new SaveAllProjectItem());
		add(new SaveAsProjectItem());
		add(new SaveItem());
		// TODO: repair reload project. this includes to also support close project.
		// add(reloadProjectItem = new ReloadProjectItem());
		addSeparator();

		if (addImportItems()) {
			add(importMenu);
		}
		if (addExportItems()) {
			add(exportMenu);
		}
		if (importMenu != null || exportMenu != null) {
			addSeparator();
		}

		addSpecificItems();

		add(new InspectProjectItem());
		if (_controller instanceof PrintManagingController) {
			addSeparator();
			addPrintItems();
			add(new PageSetUpItem());
		}
		addSeparator();

		add(new QuitItem());

		updateRecentProjectMenu();
	}

	public void updateRecentProjectMenu() {
		if (recentProjectMenu != null) {
			recentProjectMenu.removeAll();
			if (getController().getApplicationContext().getGeneralPreferences() != null) {
				for (File f : getController().getApplicationContext().getGeneralPreferences().getLastOpenedProjects()) {
					recentProjectMenu.add(new ProjectItem(f));
				}
			}
		}
	}

	public void addToExportItems(FlexoMenuItem exportItem) {
		if (exportMenu == null) {
			exportMenu = new JMenu();
			exportMenu.setText(_controller.getFlexoLocales().localizedForKey("export", exportMenu));
		}
		exportMenu.add(exportItem);
	}

	public void addToImportItems(FlexoMenuItem importItem) {
		if (importMenu == null) {
			importMenu = new JMenu();
			importMenu.setText(_controller.getFlexoLocales().localizedForKey("import", importMenu));
		}
		importMenu.add(importItem);
	}

	protected boolean addExportItems() {
		return false;
	}

	protected boolean addImportItems() {
		return false;
	}

	public void addSpecificItems() {
		// No specific item here, please override this method when required
	}

	public void addPrintItems() {
		// No specific item here, please override this method when required
	}

	public class SaveItem extends FlexoMenuItem {

		public SaveItem() {
			super(new SaveAction(), "save_all", KeyStroke.getKeyStroke(KeyEvent.VK_S, FlexoCst.META_MASK), getController(), true);
			setIcon(IconLibrary.SAVE_ICON);
		}

	}

	public class SaveAction extends AbstractAction implements PropertyChangeListener {
		public SaveAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			// System.out.println("SaveProjectAction");

			if (getController() == null) {
				return;
			}
			getController().reviewModifiedResources();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getController() != null) {
				if (evt.getSource() == getController().getControllerModel()) {
					if (ControllerModel.CURRENT_EDITOR.equals(evt.getPropertyName())) {
						updateEnability();
					}
				}
			}
		}

		private void updateEnability() {
			setEnabled(getController() != null);
		}
	}

	public void quit() {
		try {
			getModuleLoader().quit(true);
		} catch (OperationCancelledException e) {
			// User pressed cancel.
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, "Cancelled saving", e);
			}
		}
	}

	public class NewProjectItem extends FlexoMenuItem {

		public NewProjectItem() {
			super(new NewProjectAction(), "new_project", KeyStroke.getKeyStroke(KeyEvent.VK_N, FlexoCst.META_MASK), getController(), true);
			setIcon(IconLibrary.NEW_ICON);
		}

	}

	public class NewProjectAction extends AbstractAction {
		public NewProjectAction() {
			super();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void actionPerformed(ActionEvent arg0) {
			File projectDirectory = NewProjectComponent.getProjectDirectory(getController().getApplicationContext());
			if (projectDirectory != null) {
				if (getController().getModule().getModule() instanceof NatureSpecificModule) {
					Class<? extends ProjectNature> projectNatureClass = ((NatureSpecificModule) getController().getModule().getModule())
							.getProjectNatureClass();
					try {
						getProjectLoader().newStandaloneProject(projectDirectory, projectNatureClass);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ProjectInitializerException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						getProjectLoader().newStandaloneProject(projectDirectory);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ProjectInitializerException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public class OpenProjectItem extends FlexoMenuItem {

		public OpenProjectItem() {
			super(new OpenProjectAction(), "open_project", KeyStroke.getKeyStroke(KeyEvent.VK_O, FlexoCst.META_MASK), getController(),
					true);
			setIcon(IconLibrary.OPEN_ICON);
		}
	}

	public class OpenProjectAction extends AbstractAction {
		public OpenProjectAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			File projectDirectory = OpenProjectComponent.getProjectDirectory(getController().getApplicationContext());
			if (projectDirectory != null) {
				// try {
				try {
					getProjectLoader().loadProject(projectDirectory);
				} catch (ProjectLoadingCancelledException e) {
					// Nothing to do
				} catch (ProjectInitializerException e) {
					e.printStackTrace();
					FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_project_located_at")
							+ projectDirectory.getAbsolutePath());
				}
			}
		}
	}

	public class CloseProjectItem extends FlexoMenuItem {

		public CloseProjectItem() {
			super(new CloseProjectAction(), "close_project", null, getController(), true);
		}
	}

	public class CloseProjectAction extends AbstractAction implements PropertyChangeListener {
		public CloseProjectAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			// System.out.println("SaveProjectAction");

			if (getController() == null || getController().getProject() == null) {
				return;
			}

			getProjectLoader().closeProject(getController().getProject());
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getController() != null) {
				if (evt.getSource() == getController().getControllerModel()) {
					if (ControllerModel.CURRENT_EDITOR.equals(evt.getPropertyName())) {
						updateEnability();
					}
				}
			}
		}

		private void updateEnability() {
			setEnabled(getController() != null && getController().getProject() != null);
		}
	}

	public class ProjectItem extends FlexoMenuItem {

		/**
		 *
		 */
		public ProjectItem(File project) {
			super(new RecentProjectAction(project), project.getName(), null, getController(), false);
		}

	}

	public class RecentProjectAction extends AbstractAction {
		private final File projectDirectory;

		public RecentProjectAction(File projectDirectory) {
			super();
			this.projectDirectory = projectDirectory;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				getProjectLoader().loadProject(projectDirectory);
			} catch (ProjectLoadingCancelledException e) {
				// Nothing to do
			} catch (ProjectInitializerException e) {
				e.printStackTrace();
				FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_project_located_at")
						+ projectDirectory.getAbsolutePath());
			}
		}
	}

	public class ImportProjectMenuItem extends FlexoMenuItem {

		public ImportProjectMenuItem() {
			super(new ImportProjectAction(), "import_project", null, getController(), true);
			setIcon(IconLibrary.IMPORT_ICON);
		}

	}

	public class ImportProjectAction extends AbstractAction {
		public ImportProjectAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FlexoEditor editor = getController().getEditor();
			if (editor != null) {
				logger.warning("Please implement this");
				// TODO
				// editor.performActionType(ImportProject.actionType, editor.getProject(), null, e);
			}
		}

	}

	public class SaveProjectItem extends FlexoMenuItem {

		public SaveProjectItem() {
			super(new SaveProjectAction(), "save_current_project", null, getController(), true);
			setIcon(IconLibrary.SAVE_ICON);
		}

	}

	public class SaveProjectAction extends AbstractAction implements PropertyChangeListener {
		public SaveProjectAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			// System.out.println("SaveProjectAction");

			if (getController() == null || getController().getProject() == null) {
				return;
			}
			if (getController().getProject().hasUnsavedResources()) {
				Cursor c = getController().getFlexoFrame().getCursor();
				// FileMenu.this._controller.getFlexoFrame().setCursor(Cursor.WAIT_CURSOR);
				try {
					getProjectLoader().saveProjects(Arrays.asList(getController().getProject()));
				} catch (SaveResourceExceptionList e) {
					e.printStackTrace();
					FlexoController.showError(_controller.getFlexoLocales().localizedForKey("errors_during_saving"),
							_controller.getFlexoLocales().localizedForKey("errors_during_saving"));
				} finally {
					getController().getFlexoFrame().setCursor(c);
				}
			}
			getController().reviewModifiedResources();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getController() != null) {
				if (evt.getSource() == getController().getControllerModel()) {
					if (ControllerModel.CURRENT_EDITOR.equals(evt.getPropertyName())) {
						updateEnability();
					}
				}
			}
		}

		private void updateEnability() {
			setEnabled(getController() != null && getController().getProject() != null);
		}
	}

	public class SaveAsProjectItem extends FlexoMenuItem {

		public SaveAsProjectItem() {
			super(new SaveAsProjectAction(), "save_current_project_as", null, getController(), true);
			setIcon(IconLibrary.SAVE_AS_ICON);
		}

	}

	public class SaveAsProjectAction extends AbstractAction implements PropertyChangeListener {
		public SaveAsProjectAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			File projectDirectory = NewProjectComponent.getProjectDirectory(getController().getApplicationContext());
			if (projectDirectory != null) {
				try {
					getProjectLoader().saveAsProject(projectDirectory, (FlexoProject<File>) getController().getProject());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getController() != null) {
				if (evt.getSource() == getController().getControllerModel()) {
					if (ControllerModel.CURRENT_EDITOR.equals(evt.getPropertyName())) {
						updateEnability();
					}
				}
			}
		}

		private void updateEnability() {
			setEnabled(getController() != null && getController().getProject() != null);
		}

	}

	public class SaveAllProjectItem extends FlexoMenuItem {

		public SaveAllProjectItem() {
			super(new SaveAllProjectAction(), "save_all_project",
					KeyStroke.getKeyStroke(KeyEvent.VK_S, FlexoCst.META_MASK | KeyEvent.SHIFT_MASK), getController(), true);
			setIcon(IconLibrary.SAVE_ALL_ICON);
		}

	}

	public class SaveAllProjectAction extends AbstractAction implements PropertyChangeListener {
		public SaveAllProjectAction() {
			super();
			if (getProjectLoader() != null) {
				manager.addListener(InteractiveProjectLoader.ROOT_PROJECTS, this, getProjectLoader());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getProjectLoader().someProjectsAreModified()) {
				Cursor c = getController().getFlexoFrame().getCursor();
				// FileMenu.this._controller.getFlexoFrame().setCursor(Cursor.WAIT_CURSOR);
				try {
					getProjectLoader().saveAllProjects();
				} catch (SaveResourceExceptionList e) {
					e.printStackTrace();
					FlexoController.showError(_controller.getFlexoLocales().localizedForKey("errors_during_saving"),
							_controller.getFlexoLocales().localizedForKey("errors_during_saving"));
				} finally {
					getController().getFlexoFrame().setCursor(c);
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getProjectLoader() != null) {
				if (evt.getSource() == getProjectLoader()) {
					if (InteractiveProjectLoader.ROOT_PROJECTS.equals(evt.getPropertyName())) {
						updateEnability();
					}
				}
			}
		}

		private void updateEnability() {
			setEnabled(getProjectLoader().getRootProjects().size() > 0);
		}
	}

	protected ProjectLoader getProjectLoader() {
		return getController().getProjectLoader();
	}

	// ==========================================================================
	// ============================= ReloadProject =============================
	// ==========================================================================

	public class ReloadProjectItem extends FlexoMenuItem {

		public ReloadProjectItem() {
			super(new ReloadProjectAction(), "reload_project", null, getController(), true);
		}

	}

	public class ReloadProjectAction extends AbstractAction {
		public ReloadProjectAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				getProjectLoader().reloadProject(getController().getProject());
			} catch (ProjectLoadingCancelledException e) {
				// Nothing to do
			} catch (ProjectInitializerException e) {
				e.printStackTrace();
				FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_project_located_at")
						+ e.getProjectDirectory());
			}
		}

	}

	// ==========================================================================
	// ============================= InspectProject
	// =============================
	// ==========================================================================

	public class InspectProjectItem extends FlexoMenuItem {

		public InspectProjectItem() {
			super(new InspectProjectAction(), "inspect_project", null, getController(), true);
			setIcon(IconLibrary.INSPECT_ICON);
		}

	}

	public class InspectProjectAction extends AbstractAction {
		public InspectProjectAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// FlexoModule.getActiveModule().getFlexoController().showInspector();
			// FlexoModule.getActiveModule().getFlexoController().setCurrentInspectedObject(FlexoModule.getActiveModule().getFlexoController().getProject());
			FlexoController controller = getController();
			controller.getSelectionManager().setSelectedObject(controller.getProject());
			controller.showInspector();
			/*
			 * int state = controller.getInspectorWindow().getExtendedState(); state &= ~Frame.ICONIFIED;
			 * controller.getInspectorWindow().setExtendedState(state);
			 */
		}
	}

	// ==========================================================================
	// ============================= Quit Flexo
	// =================================
	// ==========================================================================

	public class QuitItem extends FlexoMenuItem {

		public QuitItem() {
			super(new QuitAction(), "quit", ToolBox.isWindows() ? KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK)
					: KeyStroke.getKeyStroke(KeyEvent.VK_Q, FlexoCst.META_MASK), getController(), true);
		}

	}

	public class QuitAction extends AbstractAction {
		public QuitAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			quit();
		}

	}

	// ==========================================================================
	// ============================= PageSetUP ================================
	// ==========================================================================

	public class PageSetUpItem extends FlexoMenuItem {

		public PageSetUpItem() {
			super(new PageSetUpAction(), "page_setup", null, getController(), true);
		}

	}

	public class PageSetUpAction extends AbstractAction {
		public PageSetUpAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			((PrintManagingController) _controller).getPrintManager().pageSetup();
		}
	}

}
