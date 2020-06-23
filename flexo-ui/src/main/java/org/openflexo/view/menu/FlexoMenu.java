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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.openflexo.module.ModuleLoader;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.view.controller.FlexoController;

/**
 * Abstract class implementing a Flexo Menu. Controller management is performed at this level.
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public abstract class FlexoMenu extends JMenu implements MouseListener, MenuListener {

	private static final Logger logger = Logger.getLogger(FlexoMenu.class.getPackage().getName());

	private FlexoController _controller;

	// ==========================================================================
	// ============================= Constructor
	// ================================
	// ==========================================================================

	protected FlexoMenu(String value, FlexoController controller) {
		super();
		manager = new PropertyChangeListenerRegistrationManager();
		setController(controller);
		setText(controller.getModuleLocales().localizedForKey(value, this));
		addMouseListener(this);
		addMenuListener(this);
	}

	protected PropertyChangeListenerRegistrationManager manager;

	public void dispose() {
		manager.delete();
	}

	// ==========================================================================
	// ============================= Accessors
	// ==================================
	// ==========================================================================

	public FlexoController getController() {
		return _controller;
	}

	protected ModuleLoader getModuleLoader() {
		return getController().getModuleLoader();
	}

	protected void setController(FlexoController controller) {
		_controller = controller;
	}

	public boolean moduleHasFocus() {
		boolean returned = getController().getModule().isActive();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("moduleHasFocus in " + getClass().getName() + " : " + returned);
		}
		return returned;
	}

	/**
	 * Overrides mouseClicked
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Overrides mouseEntered
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Overrides mouseExited
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Overrides mousePressed
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isPopupMenuVisible()) {
			refreshMenu();
		}
	}

	/**
	 *
	 */
	public void refreshMenu() {
		for (int i = 0; i < getItemCount(); i++) {
			JMenuItem item = getItem(i);
			if (item instanceof FlexoMenuItemWithFactory) {
				((FlexoMenuItemWithFactory<?>) item).itemWillShow();
			}
			else if (item instanceof FlexoMenu) {
				((FlexoMenu) item).refreshMenu();
			}
		}
	}

	/**
	 * Overrides mouseReleased
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {

	}

	@Override
	public void menuDeselected(MenuEvent e) {

	}

	@Override
	public void menuSelected(MenuEvent e) {
		refreshMenu();
	}

}
