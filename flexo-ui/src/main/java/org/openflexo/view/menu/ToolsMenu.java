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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openflexo.Flexo;
import org.openflexo.FlexoCst;
import org.openflexo.br.SendBugReportServiceTask;
import org.openflexo.components.ResourceCenterEditorDialog;
import org.openflexo.components.UndoManagerDialog;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.swing.utils.localization.LocalizedEditor;
import org.openflexo.gina.swing.utils.logging.FlexoLoggingViewer;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.module.Module;
import org.openflexo.project.AutoSaveService;
import org.openflexo.project.InteractiveProjectLoader;
import org.openflexo.view.FMLConsoleViewer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * Automatic builded 'Tools' menu for modules
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public class ToolsMenu extends FlexoMenu {

	static final Logger logger = FlexoLogger.getLogger(ToolsMenu.class.getPackage().getName());

	// ==========================================================================
	// ============================= Instance Variables
	// =========================
	// ==========================================================================

	public JMenuItem manageResourceCenterItem;
	public JMenuItem manageTechnologiesItem;

	public JMenuItem fmlConsoleItem;
	public JMenuItem loggingItem;

	public JMenuItem undoManagerItem;

	public FlexoMenu localizedEditorsMenu;
	public JMenuItem applicationFIBEditorItem;

	public JMenuItem rmItem;

	public JMenuItem submitBug;

	public JMenuItem repairProject;

	public JMenuItem timeTraveler;

	public SaveDocSubmissionItem saveDocSubmissions;

	public JMenuItem market;

	public ToolsMenu(FlexoController controller) {
		super("tools", controller);
		addSpecificItems();
		add(manageResourceCenterItem = new ManageResourceCenterItem());
		add(manageTechnologiesItem = new TechnologiesMenu(getController()));
		add(fmlConsoleItem = new FMLConsoleItem());
		add(loggingItem = new LoggingItem());
		if (Flexo.isDev) {
			addSeparator();
			add(applicationFIBEditorItem = new ApplicationFIBEditorItem());
			add(localizedEditorsMenu = new LocalizedEditorMenu(getController()));
		}
		add(rmItem = new ResourceManagerItem());
		add(undoManagerItem = new UndoManagerItem());
		addSeparator();
		add(submitBug = new SubmitBugItem());
		if (getModuleLoader().allowsDocSubmission()) {
			addSeparator();
			add(saveDocSubmissions = new SaveDocSubmissionItem());
		}
		addSeparator();
		add(repairProject = new ValidateProjectItem());
		add(timeTraveler = new TimeTraveler());
	}

	public void addSpecificItems() {
		// No specific item here, please override this method when required
	}

	// ===============================================================
	// ===================== Resource Centers ========================
	// ===============================================================

	public class ManageResourceCenterItem extends FlexoMenuItem {

		public ManageResourceCenterItem() {
			super(new ManageResourceCenterAction(), "manage_resource_centers", KeyStroke.getKeyStroke(KeyEvent.VK_G, FlexoCst.META_MASK),
					getController(), true);
		}

	}

	public class ManageResourceCenterAction extends AbstractAction {
		public ManageResourceCenterAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ResourceCenterEditorDialog.showResourceCenterEditorDialog(getController().getApplicationContext(),
					getController().getFlexoFrame(), false);
		}
	}

	// ==========================================================================
	// ======================== Technologies management =========================
	// ==========================================================================

	public class TechnologiesMenu extends FlexoMenu {

		public TechnologiesMenu(FlexoController controller) {
			super("technology_adapters", controller);

			FlexoMenuItem label = new FlexoMenuItem(getController(), "click_to_activate...");
			label.setEnabled(false);

			add(label);

			addSeparator();

			for (TechnologyAdapter ta : controller.getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
				JMenuItem item = new TechnologyAdapterItem(ta);
				if (controller.getApplicationContext().getTechnologyAdapterControllerService() != null && controller.getApplicationContext()
						.getTechnologyAdapterControllerService().getTechnologyAdapterController(ta) != null) {
					item.setIcon(controller.getApplicationContext().getTechnologyAdapterControllerService()
							.getTechnologyAdapterController(ta).getTechnologyIcon());
				}
				add(item);
			}

		}

	}

	public class TechnologyAdapterItem extends JCheckBoxMenuItem implements PropertyChangeListener {

		private final TechnologyAdapter technologyAdapter;

		public TechnologyAdapterItem(TechnologyAdapter technologyAdapter) {
			super(new TechnologyAdapterAction(technologyAdapter)/*, technologyAdapter.getName(), null, getController(), true*/);
			this.technologyAdapter = technologyAdapter;
			if (technologyAdapter != null) {
				technologyAdapter.getPropertyChangeSupport().addPropertyChangeListener(this);
				setSelected(technologyAdapter.isActivated());
			}
			else {
				setSelected(false);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("activated") && technologyAdapter != null) {
				setSelected(technologyAdapter.isActivated());
			}
		}

	}

	public class TechnologyAdapterAction extends AbstractAction {

		private final TechnologyAdapter technologyAdapter;

		public TechnologyAdapterAction(TechnologyAdapter technologyAdapter) {
			super(technologyAdapter.getName(), null);
			this.technologyAdapter = technologyAdapter;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("On active la techno " + technologyAdapter);
			getController().getApplicationContext().activateTechnologyAdapter(technologyAdapter);
		}

	}

	// ===================================================================
	// ========================== FML Console ============================
	// ===================================================================

	public class FMLConsoleItem extends FlexoMenuItem {

		public FMLConsoleItem() {
			super(new FMLConsoleAction(), "fml_console", KeyStroke.getKeyStroke(KeyEvent.VK_L, FlexoCst.META_MASK), getController(), true);
		}

	}

	public class FMLConsoleAction extends AbstractAction {
		public FMLConsoleAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			FMLConsoleViewer.showConsoleViewer(getController().getEditor().getFMLConsole(),
					getController().getApplicationFIBLibraryService().getApplicationFIBLibrary(), getController().getFlexoFrame());
		}
	}

	// ===============================================================
	// ========================== Logging ============================
	// ===============================================================

	public class LoggingItem extends FlexoMenuItem {

		public LoggingItem() {
			super(new LoggingAction(), "show_application_logging", null, getController(), true);
		}

	}

	public class LoggingAction extends AbstractAction {
		public LoggingAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			FlexoLoggingViewer.showLoggingViewer(FlexoLoggingManager.instance(),
					getController().getApplicationFIBLibraryService().getApplicationFIBLibrary(), getController().getFlexoFrame());
		}
	}

	// ===================================================================
	// ========================== FML Console ============================
	// ===================================================================

	public class ApplicationFIBEditorItem extends FlexoMenuItem {

		public ApplicationFIBEditorItem() {
			super(new ApplicationFIBEditorAction(), "GINA_FIB_editor", null, getController(), true);
		}

	}

	public class ApplicationFIBEditorAction extends AbstractAction {
		public ApplicationFIBEditorAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getController().getApplicationFIBLibraryService().getApplicationFIBEditor().setVisible(true);
			getController().getApplicationFIBLibraryService().getApplicationFIBEditor().toFront();
		}
	}

	// ===============================================================
	// ======================= UndoManager ===========================
	// ===============================================================

	public class UndoManagerItem extends FlexoMenuItem {

		public UndoManagerItem() {
			super(new UndoManagerAction(), "undo_manager", KeyStroke.getKeyStroke(KeyEvent.VK_U, FlexoCst.META_MASK), getController(),
					true);
		}

	}

	public class UndoManagerAction extends AbstractAction {
		public UndoManagerAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			UndoManagerDialog.showUndoManagerDialog(getController().getApplicationContext(), getController().getFlexoFrame());
		}
	}

	// ==========================================================================
	// ========================== Localized management ==========================
	// ==========================================================================

	public class LocalizedEditorMenu extends FlexoMenu {

		@SuppressWarnings("unused")
		private LocalizedEditorItem flexoLocalesItem;

		public LocalizedEditorMenu(FlexoController controller) {
			super("localization_editor", controller);

			add(flexoLocalesItem = new LocalizedEditorItem("general_locales", getController().getFlexoLocales()));

			addSeparator();

			for (TechnologyAdapter ta : controller.getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
				JMenuItem item = new TechnologyLocalizedItem(ta);
				if (controller.getApplicationContext().getTechnologyAdapterControllerService() != null && controller.getApplicationContext()
						.getTechnologyAdapterControllerService().getTechnologyAdapterController(ta) != null) {
					item.setIcon(controller.getApplicationContext().getTechnologyAdapterControllerService()
							.getTechnologyAdapterController(ta).getTechnologyIcon());
				}
				add(item);
			}

			addSeparator();

			for (Module<?> m : controller.getModuleLoader().getKnownModules()) {
				JMenuItem item = new ModuleLocalizedItem(m);
				item.setIcon(m.getSmallIcon());
				add(item);
			}

			/*for (Module<?> m : controller.getModuleLoader().getKnownModules()) {
				JMenuItem item = new TechnologyLocalizedItem(technologyAdapter)
			
			
						new JMenuItem(new SwitchToModuleAction(m));
				item.setText(controller.getModuleLocales().localizedForKey(m.getName(), item));
				item.setIcon(m.getSmallIcon());
				moduleMenuItems.put(m, item);
				if (getModuleLoader().isLoaded(m)) {
					add(item);
				}
				else {
					loadWindowMenu.add(item);
				}
			}*/

		}

	}

	public class LocalizedEditorItem extends FlexoMenuItem {

		public LocalizedEditorItem(String localesName, LocalizedDelegate locales) {
			super(new LocalizedEditorAction(localesName, locales), localesName, null, getController(), true);
		}

	}

	public class TechnologyLocalizedItem extends FlexoMenuItem {

		public TechnologyLocalizedItem(TechnologyAdapter technologyAdapter) {
			super(new TechnologyLocalizedEditorAction(technologyAdapter), technologyAdapter.getName(), null, getController(), true);
		}

	}

	public class ModuleLocalizedItem extends FlexoMenuItem {

		public ModuleLocalizedItem(Module<?> module) {
			super(new ModuleLocalizedEditorAction(module), module.getName(), null, getController(), true);
		}

	}

	public class TechnologyLocalizedEditorAction extends LocalizedEditorAction implements PropertyChangeListener {

		private final TechnologyAdapter technologyAdapter;

		public TechnologyLocalizedEditorAction(TechnologyAdapter technologyAdapter) {
			super(technologyAdapter.getName(), null);
			this.technologyAdapter = technologyAdapter;
			if (technologyAdapter != null) {
				technologyAdapter.getPropertyChangeSupport().addPropertyChangeListener(this);
				setEnabled(technologyAdapter.isActivated());
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("activated") && technologyAdapter != null) {
				setEnabled(technologyAdapter.isActivated());
			}
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (technologyAdapter != null) {
				return technologyAdapter.getLocales();
			}
			return null;
		}

		/*@Override
		public boolean isEnabled() {
			if (technologyAdapter != null) {
				return technologyAdapter.isActivated();
			}
			return super.isEnabled();
		}*/
	}

	public class ModuleLocalizedEditorAction extends LocalizedEditorAction implements PropertyChangeListener {

		private final Module<?> module;

		public ModuleLocalizedEditorAction(Module<?> module) {
			super(module.getName(), null);
			this.module = module;
			if (module != null) {
				module.getPropertyChangeSupport().addPropertyChangeListener(this);
				setEnabled(module.isLoaded());
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("loaded") && module != null) {
				setEnabled(module.isLoaded());
			}
		}

		@Override
		public LocalizedDelegate getLocales() {
			return module.getLoadedModuleInstance().getLocales();
		}

		/*@Override
		public boolean isEnabled() {
			if (module != null) {
				return module.isLoaded();
			}
			return super.isEnabled();
		}*/
	}

	public class LocalizedEditorAction extends AbstractAction {

		private final LocalizedDelegate locales;
		private final String localesName;
		private LocalizedEditor localizedEditor;

		public LocalizedEditorAction(String localesName, LocalizedDelegate locales) {
			super();
			this.locales = locales;
			this.localesName = localesName;
		}

		public LocalizedDelegate getLocales() {
			return locales;
		}

		private LocalizedEditor getLocalizedEditor() {
			if (localizedEditor == null) {
				localizedEditor = new LocalizedEditor(getController().getFlexoFrame(), localesName, getLocales(),
						FlexoLocalization.getMainLocalizer(), true, false);
			}
			return localizedEditor;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getLocalizedEditor().setVisible(true);
		}
	}

	// ===================================================
	// ================== Resource Manager ===============
	// ===================================================

	public class ResourceManagerItem extends FlexoMenuItem {

		public ResourceManagerItem() {
			super(new ResourceManagerAction(), "resource_manager", null, getController(), true);
		}

	}

	public class ResourceManagerAction extends AbstractAction implements PropertyChangeListener {
		public ResourceManagerAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getController().getProject() == null) {
				return;
			}
			// getController().getRMWindow(getController().getProject()).show();
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

	// ==========================================================================
	// ========================== Submit bug ==============================
	// ==========================================================================

	public class SubmitBugItem extends FlexoMenuItem {

		public SubmitBugItem() {
			super(new SubmitBugAction(), "submit_bug_report", null, getController(), true);
		}

	}

	public class SubmitBugAction extends AbstractAction {
		public SubmitBugAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getController().getApplicationContext().getBugReportService() == null
					|| !getController().getApplicationContext().getBugReportService().isInitialized()) {
				// not loaded yet, wait for the BugReportService to be activated
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						actionPerformed(null);
					}
				});
			}
			else {
				SendBugReportServiceTask sendBugReport = new SendBugReportServiceTask(null, getController().getModule(),
						getController().getProject(), getController().getApplicationContext());
				getController().getApplicationContext().getTaskManager().scheduleExecution(sendBugReport);
			}
		}

	}

	public class SaveDocSubmissionItem extends FlexoMenuItem {

		public SaveDocSubmissionItem() {
			super(new SaveDocSubmissionAction(), "save_doc_submission", null, getController(), true);
		}

	}

	public class SaveDocSubmissionAction extends AbstractAction {
		public SaveDocSubmissionAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle(FlexoLocalization.getMainLocalizer().localizedForKey("please_select_a_file"));
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(".dsr");
				}

				@Override
				public String getDescription() {
					return FlexoLocalization.getMainLocalizer().localizedForKey("doc_submission_report_files");
				}

			});
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
				try {
					File savedFile;
					if (!chooser.getSelectedFile().getName().endsWith(".dsr")) {
						savedFile = new File(chooser.getSelectedFile().getParentFile(), chooser.getSelectedFile().getName() + ".dsr");
					}
					else {
						savedFile = chooser.getSelectedFile();
					}
					DocResourceManager drm = getController().getApplicationContext().getDocResourceManager();
					drm.getSessionSubmissions().save(savedFile);
					drm.getSessionSubmissions().clear();
					FlexoController
							.notify(FlexoLocalization.getMainLocalizer().localizedForKey("doc_submission_report_successfully_saved"));
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
			}
			else {
				// cancelled, return.
				return;
			}
		}

	}

	public class ValidateProjectItem extends FlexoMenuItem {

		public ValidateProjectItem() {
			super(new ValidateProjectAction(), "validate_project", null, getController(), true);
		}

	}

	public class ValidateProjectAction extends AbstractAction implements GraphicalFlexoObserver, PropertyChangeListener {
		public ValidateProjectAction() {
			super();
			if (getController() != null) {
				manager.addListener(ControllerModel.CURRENT_EDITOR, this, getController().getControllerModel());
			}
			updateEnability();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Validation of project in progress...");
			}

			if (getController().getProject() == null) {
				return;
			}

			getController().getValidationWindow(true).validateAndDisplayReportForObject(getController().getProject(),
					getController().getProject().getProjectValidationModel());
		}

		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
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

	protected AutoSaveService getAutoSaveService() {
		if (getController().getProjectLoader() instanceof InteractiveProjectLoader) {
			return ((InteractiveProjectLoader) getController().getProjectLoader()).getAutoSaveService(getController().getProject());
		}
		return null;
	}

	public class TimeTraveler extends FlexoMenuItem {

		public TimeTraveler() {
			super(new TimeTravelAction(), "revert_to_auto_saved_version", null, getController(), true);
		}

	}

	public class TimeTravelAction extends AbstractAction {
		public TimeTravelAction() {
			super();
			setEnabled(false);
			if (getController().getProject() == null) {
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			AutoSaveService autoSaveService = getAutoSaveService();
			if (autoSaveService != null) {
				autoSaveService.showTimeTravelerDialog();
			}
			else {
				if (FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("time_traveling_is_disabled") + ". "
						+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_activate_it_now?"))) {
					getController().getApplicationContext().getGeneralPreferences().setAutoSaveEnabled(true);
					getController().getApplicationContext().getPreferencesService().savePreferences();
					getAutoSaveService().showTimeTravelerDialog();
				}
			}
		}
	}
}
