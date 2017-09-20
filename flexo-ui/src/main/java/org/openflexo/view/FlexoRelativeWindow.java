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

package org.openflexo.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.openflexo.icon.IconLibrary;
import org.openflexo.module.FlexoModule;
import org.openflexo.view.controller.FlexoController;

/**
 * Abstract class defining behaviour of a window relating to a particular module frame (eg palettes)
 * 
 * @author sguerin
 */
public abstract class FlexoRelativeWindow extends JFrame /*implements FocusListener*/
{

	private static final Logger logger = Logger.getLogger(FlexoFrame.class.getPackage().getName());

	private FlexoFrame _parentFrame;

	private boolean isDisplayedWhenModuleIsActive;

	protected FlexoRelativeWindow(FlexoFrame frame) {
		// super(frame);
		super();
		setIconImage(IconLibrary.OPENFLEXO_NOTEXT_128.getImage());
		_parentFrame = frame;
		setLocationRelativeTo(frame);
		isDisplayedWhenModuleIsActive = false;
		_parentFrame.addToRelativeWindows(this);
		getController().notifyNewFlexoRelativeWindow(this);
		setFocusableWindowState(false);

	}

	/**
	 * Overrides dispose
	 * 
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (_parentFrame != null) {
			_parentFrame.removeFromRelativeWindows(this);
		}
		getController().notifyRemoveFlexoRelativeWindow(this);
		removeAll();
		_parentFrame = null;
	}

	public FlexoFrame getParentFrame() {
		return _parentFrame;
	}

	public FlexoController getController() {
		if (_parentFrame != null) {
			return _parentFrame.getController();
		}
		return null;
	}

	public FlexoModule<?> getModule() {
		if (getParentFrame() != null) {
			return getParentFrame().getModule();
		}
		return null;
	}

	/*public void focusGained(FocusEvent e)
	{
	    if (logger.isLoggable(Level.FINE))
	        logger.fine("focusGained in " + this.getClass().getName());
	}
	
	public void focusLost(FocusEvent e)
	{
	    if (logger.isLoggable(Level.FINE))
	        logger.fine("focusLost in " + this.getClass().getName());
	}*/

	@Override
	public void setVisible(boolean mustBeDisplayed) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("setVisible " + mustBeDisplayed + " in " + this.getClass().getName());
		}
		updateDisplayedWhenModuleIsActiveState(mustBeDisplayed);
		if (mustBeDisplayed) {
			if (getModule() != null && getModule().isActive()) {
				requestSetVisible = true;
				super.setVisible(true);
				requestSetVisible = false;
			}
		}
		else {
			requestSetVisible = true;
			super.setVisible(false);
			requestSetVisible = false;
		}
	}

	private boolean requestSetVisible = false;

	@Override
	public void show() {
		if (!requestSetVisible) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("show() is deprecated and should not be used in this context !");
			}
		}
		super.show();
	}

	@Override
	public void hide() {
		if (!requestSetVisible) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("hide() is deprecated and should not be used in this context !");
			}
		}
		super.hide();
	}

	private void updateDisplayedWhenModuleIsActiveState(boolean mustBeDisplayed) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("updateDisplayedWhenModuleIsActiveState in " + this.getClass().getName());
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.finer("isDisplayedWhenModuleIsActive=" + isDisplayedWhenModuleIsActive);
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.finer("mustBeDisplayed=" + mustBeDisplayed);
		}
		if (_parentFrame == null) {
			return;
		}
		if (isDisplayedWhenModuleIsActive) {
			if (!mustBeDisplayed) {
				_parentFrame.removeFromDisplayedRelativeWindows(this);
				isDisplayedWhenModuleIsActive = false;
			}
		}
		if (!isDisplayedWhenModuleIsActive) {
			if (mustBeDisplayed) {
				_parentFrame.addToDisplayedRelativeWindows(this);
				isDisplayedWhenModuleIsActive = true;
			}
		}
	}

	public void setVisibleNoParentFrameNotification(boolean mustBeDisplayed) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("setVisibleNoParentFrameNotification " + mustBeDisplayed + " in " + this.getClass().getName());
		}
		requestSetVisible = true;
		super.setVisible(mustBeDisplayed);
		requestSetVisible = false;
	}

	@Override
	public abstract String getName();

	public String getLocalizedName() {
		return getController().getModuleLocales().localizedForKey(getName());
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		getController().notifyRenameFlexoRelativeWindow(this, title);
	}

}
