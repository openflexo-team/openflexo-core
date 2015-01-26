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

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.openflexo.help.FlexoHelp;
import org.openflexo.module.Module;
import org.openflexo.view.controller.FlexoController;

/**
 * Abstract definition of module's menu bar. Automatically handles 'Modules' menu.
 * 
 * @author sguerin
 */
public abstract class FlexoMenuBar extends JMenuBar {

	public static final String KEY_PRESSED = "KeyPressed";

	protected FlexoController _controller;

	private FileMenu _fileMenu = null;

	private EditMenu _editMenu = null;

	protected WindowMenu _windowMenu = null;

	private ToolsMenu _toolsMenu = null;

	private HelpMenu _helpMenu = null;

	public FlexoMenuBar(FlexoController controller, Module module) {
		super();
		_controller = controller;
		add(getFileMenu(controller));
		add(getEditMenu(controller));
		add(getToolsMenu(controller));
		add(getWindowMenu(controller, module));
		if (FlexoHelp.isAvailable()) {
			add(getHelpMenu(controller));
		}

	}

	public void dispose() {
		for (int i = 0; i < getMenuCount(); i++) {
			JMenu menu = getMenu(i);
			if (menu instanceof FlexoMenu) {
				((FlexoMenu) menu).dispose();
			}
		}
	}

	@Override
	public JMenu add(JMenu c) {
		if (c.getItemCount() == 0) {
			return c;
		}
		return super.add(c);
	}

	/**
	 * Build if required and return default 'File' menu. This method must be overriden if specific items for related module should be added.
	 * 
	 * @param controller
	 * @return a FileMenu instance
	 */
	public FileMenu getFileMenu(FlexoController controller) {
		if (_fileMenu == null) {
			_fileMenu = new FileMenu(_controller);
		}
		return _fileMenu;
	}

	/**
	 * Build if required and return default 'Edit' menu. This method must be overriden if specific items for related module should be added.
	 * 
	 * @param controller
	 * @return a EditMenu instance
	 */
	public EditMenu getEditMenu(FlexoController controller) {
		if (_editMenu == null) {
			_editMenu = new EditMenu(_controller);
		}
		return _editMenu;
	}

	/**
	 * Build if required and return default 'Window' menu. This method must be overriden if specific items for related module should be
	 * added.
	 * 
	 * @param controller
	 * @param module
	 *            TODO
	 * @return a WindowMenu instance
	 */
	public WindowMenu getWindowMenu(FlexoController controller, Module module) {
		if (_windowMenu == null) {
			_windowMenu = new WindowMenu(controller, module);
		}
		return _windowMenu;
	}

	/**
	 * Returns 'Window' menu, asserting menu is already built
	 * 
	 * @param controller
	 * @param module
	 *            TODO
	 * @return a WindowMenu instance
	 */
	public WindowMenu getWindowMenu() {
		return _windowMenu;
	}

	/**
	 * Build if required and return default 'Tools' menu. This method must be overriden if specific items for related module should be
	 * added.
	 * 
	 * @param controller
	 * @return a ToolsMenu instance
	 */
	public ToolsMenu getToolsMenu(FlexoController controller) {
		if (_toolsMenu == null) {
			_toolsMenu = new ToolsMenu(controller);
		}
		return _toolsMenu;
	}

	/**
	 * Build if required and return default 'Help' menu. This method must be overriden if specific items for related module should be added.
	 * 
	 * @param controller
	 * @return a HelpMenu instance
	 */
	public HelpMenu getHelpMenu(FlexoController controller) {
		if (_helpMenu == null) {
			_helpMenu = new HelpMenu(controller);
		}
		return _helpMenu;
	}

	/*@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		// TODO Auto-generated method stub
		boolean b = super.processKeyBinding(ks, e, condition, pressed);
		if (!b && ks != null) {
			AbstractAction actionForKey = _controller.getActionForKeyStroke(ks);
			if (actionForKey != null) {
				Object eventSource = e.getSource();
				if (!(eventSource instanceof FlexoActionSource) && eventSource instanceof Component) {
					eventSource = SwingUtilities.getAncestorOfClass(FlexoActionSource.class, (Component) eventSource);
				}
				actionForKey.actionPerformed(new ActionEvent(eventSource != null ? eventSource : e.getSource(), e.getID(), KEY_PRESSED));
				b = true;
			}
		}
		return b;
	}*/
}
