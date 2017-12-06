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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.openflexo.FlexoCst;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.view.FlexoRelativeWindow;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * Automatic builded 'Windows' menu for modules
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public class WindowMenu extends FlexoMenu implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(WindowMenu.class.getPackage().getName());

	// ==========================================================================
	// =========================== Instance variables
	// ===========================
	// ==========================================================================

	public FlexoMenuItem mainWindowItem;

	public JCheckBoxMenuItem inspectorWindowItem;

	public JCheckBoxMenuItem preferencesWindowItem;

	public JCheckBoxMenuItem checkConsistencyWindowItem;

	private int windowFirstIndex = -1;

	private final Map<FlexoRelativeWindow, RelativeWindowItem> relativeWindowItems;

	protected CloseModuleItem closeModuleItem;

	private final JMenu loadWindowMenu;

	/**
	 * Hashtable where key is the class object representing module and value a JMenuItem
	 */
	private final Map<Module<?>, JMenuItem> moduleMenuItems = new Hashtable<>();

	protected FlexoMenuItem controlPanelItem;

	protected BrowserItem browserItem;

	protected PaletteItem paletteItem;

	public WindowMenu(FlexoController controller, Module<?> module) {
		super("window", controller);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Build NEW module menu for " + module.getName());
		}
		loadWindowMenu = new JMenu();
		for (Module<?> m : controller.getModuleLoader().getKnownModules()) {
			JMenuItem item = new JMenuItem(new SwitchToModuleAction(m));
			item.setText(m.getName());
			item.setIcon(m.getSmallIcon());
			moduleMenuItems.put(m, item);
			if (getModuleLoader().isLoaded(m)) {
				add(item);
			}
			else {
				loadWindowMenu.add(item);
			}
		}
		addSeparator();

		loadWindowMenu.setText(FlexoLocalization.getMainLocalizer().localizedForKey("load_module", loadWindowMenu));
		add(loadWindowMenu);

		add(closeModuleItem = new CloseModuleItem());

		addSeparator();

		add(inspectorWindowItem = new InspectorWindowItem());
		add(preferencesWindowItem = new PreferencesWindowItem());
		windowFirstIndex = getItemCount();
		add(checkConsistencyWindowItem = new CheckConsistencyWindowItem());
		windowFirstIndex = getItemCount();
		relativeWindowItems = new Hashtable<FlexoRelativeWindow, RelativeWindowItem>();

		addSeparator();
		add(controlPanelItem = new ControlPanelItem());
		add(browserItem = new BrowserItem());
		add(paletteItem = new PaletteItem());

		addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				updateWindowState();
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
		manager.addListener(ModuleLoader.MODULE_LOADED, this, getModuleLoader());
		manager.addListener(ModuleLoader.MODULE_UNLOADED, this, getModuleLoader());
	}

	@Override
	protected ModuleLoader getModuleLoader() {
		return getController().getModuleLoader();
	}

	protected void updateWindowState() {
		if (getController().getModuleInspectorController().getInspectorDialog() != null) {
			inspectorWindowItem.setState(getController().getModuleInspectorController().getInspectorDialog().isVisible());
		}
		if (getController().getApplicationContext().getPreferencesService().getPreferencesDialog() != null) {
			preferencesWindowItem
					.setState(getController().getApplicationContext().getPreferencesService().getPreferencesDialog().isVisible());
		}
		if (checkConsistencyWindowItem != null) {
			if (getController().getValidationWindow(false) != null) {
				checkConsistencyWindowItem.setState(getController().getValidationWindow().isVisible());
			}
			else {
				checkConsistencyWindowItem.setEnabled(false);
			}
		}
		for (Map.Entry<FlexoRelativeWindow, RelativeWindowItem> next : relativeWindowItems.entrySet()) {
			next.getValue().setState(next.getValue().action.window.isVisible());
		}
	}

	public void addFlexoRelativeWindowMenu(FlexoRelativeWindow window) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("addFlexoRelativeWindowMenu() with " + window);
		}
		RelativeWindowItem relativeWindowItem;
		insert(relativeWindowItem = new RelativeWindowItem(window), windowFirstIndex);
		relativeWindowItems.put(window, relativeWindowItem);
	}

	public void removeFlexoRelativeWindowMenu(FlexoRelativeWindow window) {
		RelativeWindowItem relativeWindowItem = relativeWindowItems.get(window);
		if (relativeWindowItem != null) {
			remove(relativeWindowItem);
			relativeWindowItems.remove(window);
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Inconsistent data in WindowMenu");
			}
		}
	}

	public void renameFlexoRelativeWindowMenu(FlexoRelativeWindow window, String name) {
		RelativeWindowItem relativeWindowItem = relativeWindowItems.get(window);

		if (relativeWindowItem != null) {
			relativeWindowItem.setText(name);
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Inconsistent data in WindowMenu");
			}
		}
	}

	public class InspectorWindowItem extends JCheckBoxMenuItem {

		public InspectorWindowItem() {
			super();
			InspectorWindowAction action = new InspectorWindowAction();
			setAction(action);
			KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_I, FlexoCst.META_MASK);
			setAccelerator(accelerator);
			getController().registerActionForKeyStroke(action, accelerator, "inspectFromMenu");
			setText(FlexoLocalization.getMainLocalizer().localizedForKey("inspector", this));
		}

	}

	public class InspectorWindowAction extends AbstractAction {

		public InspectorWindowAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			/*if (getController().getModuleInspectorController().getInspectorDialog().isVisible()) {
				getController().hideInspector();
			} else {*/
			getController().showInspector();
			// }
		}

	}

	public class PreferencesWindowItem extends JCheckBoxMenuItem {

		public PreferencesWindowItem() {
			super();
			PreferencesWindowAction action = new PreferencesWindowAction();
			setAction(action);
			setText(FlexoLocalization.getMainLocalizer().localizedForKey("preferences", this));
		}
	}

	public class PreferencesWindowAction extends AbstractAction {
		public PreferencesWindowAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getController().getApplicationContext().getPreferencesService().showPreferences();
		}

	}

	public class CheckConsistencyWindowItem extends JCheckBoxMenuItem {

		public CheckConsistencyWindowItem() {
			super();
			CheckConsistencyWindowAction action = new CheckConsistencyWindowAction();
			setAction(action);
			setText(FlexoLocalization.getMainLocalizer().localizedForKey("validation_window", this));
		}

	}

	public class CheckConsistencyWindowAction extends AbstractAction {
		public CheckConsistencyWindowAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getController().getValidationWindow() != null) {
				getController().getValidationWindow().setVisible(true);
			}
		}

	}

	public class RelativeWindowItem extends JCheckBoxMenuItem {
		protected RelativeWindowAction action;

		public RelativeWindowItem(FlexoRelativeWindow window) {
			super();
			action = new RelativeWindowAction(window);
			setAction(action);
			setText(FlexoLocalization.getMainLocalizer().localizedForKey(window.getName(), this));
		}

	}

	public class RelativeWindowAction extends AbstractAction {

		protected FlexoRelativeWindow window;

		public RelativeWindowAction(FlexoRelativeWindow aWindow) {
			super();
			window = aWindow;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			window.setVisible(true);
		}
	}

	public class SwitchToModuleAction extends AbstractAction {
		private final Module<?> module;

		private JCheckBoxMenuItem menuItem;

		public SwitchToModuleAction(Module<?> module) {
			super();
			this.module = module;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				getController().getModuleLoader().switchToModule(module);
			} catch (ModuleLoadingException e) {
				FlexoController.notify("Cannot load module." + e.getMessage());
				return;
			}
		}

		public JCheckBoxMenuItem getItem() {
			return menuItem;
		}

		public void setItem(JCheckBoxMenuItem menuItem) {
			this.menuItem = menuItem;
		}
	}

	private void notifyModuleHasBeenLoaded(Module<?> module) {
		JMenuItem item = moduleMenuItems.get(module);
		insert(item, getController().getModuleLoader().getLoadedModules().indexOf(module));
	}

	private void notifyModuleHasBeenUnloaded(Module<?> module) {
		JMenuItem item = moduleMenuItems.get(module);
		loadWindowMenu.insert(item, getController().getModuleLoader().getUnloadedModules().indexOf(module));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ModuleLoader.MODULE_LOADED)) {
			notifyModuleHasBeenLoaded((Module<?>) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(ModuleLoader.MODULE_UNLOADED)) {
			notifyModuleHasBeenUnloaded((Module<?>) evt.getOldValue());
		}
	}

	public class CloseModuleItem extends JMenuItem {

		public CloseModuleItem() {
			super(new CloseModuleAction());
			setText(FlexoLocalization.getMainLocalizer().localizedForKey("close_module", this));
		}

	}

	public class CloseModuleAction extends AbstractAction {
		public CloseModuleAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getModuleLoader().closeModule(getController().getModule());
		}
	}

	public class ControlPanelItem extends FlexoMenuItem {
		public ControlPanelItem() {
			super(new ControlPanelAction(), "hide_control_panel", null, getController(), true);
			((ControlPanelAction) getAction()).setItem(this);
		}

	}

	public class ControlPanelAction extends AbstractAction {
		private boolean isShowed = true;

		private ControlPanelItem _item;

		public ControlPanelAction() {
			super();
		}

		public void setItem(ControlPanelItem item) {
			_item = item;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (isShowed) {
				getController().hideControlPanel();
				_item.setText(FlexoLocalization.getMainLocalizer().localizedForKey("show_control_panel"));
				isShowed = false;
			}
			else {
				getController().showControlPanel();
				_item.setText(FlexoLocalization.getMainLocalizer().localizedForKey("hide_control_panel"));
				isShowed = true;
			}
		}

	}

	public String getHidePaletteString() {
		return "hide_palette";
	}

	public String getShowPaletteString() {
		return "show_palette";
	}

	public class PaletteItem extends FlexoMenuItem {
		public PaletteItem() {
			super(new PaletteAction(), getHidePaletteString(), null, getController(), true);
			updateText();
			manager.addListener(ControllerModel.RIGHT_VIEW_VISIBLE, this, getController().getControllerModel());
		}

		public void updateText() {
			if (!getController().getControllerModel().isRightViewVisible()) {
				setText(FlexoLocalization.getMainLocalizer().localizedForKey(getShowPaletteString()));
			}
			else {
				setText(FlexoLocalization.getMainLocalizer().localizedForKey(getHidePaletteString()));
			}
		}

	}

	public class PaletteAction extends AbstractAction {

		public PaletteAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getController().getControllerModel().setRightViewVisible(!getController().getControllerModel().isRightViewVisible());
		}

	}

	public class BrowserItem extends FlexoMenuItem {
		public BrowserItem() {
			super(new BrowserAction(), "hide_browser", null, getController(), true);
			updateText();
			manager.addListener(ControllerModel.LEFT_VIEW_VISIBLE, this, getController().getControllerModel());
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ControllerModel.LEFT_VIEW_VISIBLE)) {
				updateText();
			}
			else {
				super.propertyChange(evt);
			}
		}

		private void updateText() {
			if (!getController().getControllerModel().isLeftViewVisible()) {
				setText(FlexoLocalization.getMainLocalizer().localizedForKey("show_browser"));
			}
			else {
				setText(FlexoLocalization.getMainLocalizer().localizedForKey("hide_browser"));
			}
		}

		public BrowserAction getProcessBrowserAction() {
			return (BrowserAction) getAction();
		}
	}

	public class BrowserAction extends AbstractAction {

		public BrowserAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getController().getControllerModel().setLeftViewVisible(!getController().getControllerModel().isLeftViewVisible());
		}

	}

	public BrowserItem getBrowserItem() {
		return browserItem;
	}

	public PaletteItem getPaletteItem() {
		return paletteItem;
	}

	public class WindowMenuItem extends JCheckBoxMenuItem implements WindowListener {

		private final Window window;

		public WindowMenuItem(String menuName, Window aWindow) {
			super(menuName);
			this.window = aWindow;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					window.setVisible(!window.isVisible());
				}
			});
			aWindow.addWindowListener(this);
		}

		@Override
		public void windowOpened(WindowEvent e) {
			setState(window.isVisible());
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			setState(window.isVisible());
		}

		@Override
		public void windowClosing(WindowEvent e) {
			setState(window.isVisible());
		}

		@Override
		public void windowClosed(WindowEvent e) {
			setState(window.isVisible());
		}

		@Override
		public void windowActivated(WindowEvent e) {
			setState(window.isVisible());
		}

	}

}
