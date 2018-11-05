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

package org.openflexo.selection;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.openflexo.FlexoCst;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.controller.FlexoController;

/**
 * A MouseSelectionManager extends the concept of SelectionManager by providing a basic implementation of a MouseListener (and a
 * MouseMotionListener) allowing to basically handle custom view selection manageent.
 * 
 * @author sguerin
 */
public abstract class MouseSelectionManager extends SelectionManager implements MouseListener {

	private static final Logger logger = Logger.getLogger(MouseSelectionManager.class.getPackage().getName());

	private final PastingGraphicalContext _pastingGraphicalContext;

	// ==========================================================================
	// ============================= Constructor ================================
	// ==========================================================================

	public MouseSelectionManager(FlexoController controller) {
		super(controller);
		_pastingGraphicalContext = new PastingGraphicalContext();
	}

	// ==========================================================================
	// ============================= MouseListenerInterface =====================
	// ==========================================================================

	// Override when required
	public void processMouseClicked(JComponent clickedContainer, Point clickedPoint, int clickCount, boolean isShiftDown) {
	}

	// Override when required
	public void processMouseEntered(MouseEvent e) {
	}

	// Override when required
	public void processMouseExited(MouseEvent e) {
	}

	// Override when required
	public void processMousePressed(MouseEvent e) {
	}

	// Override when required
	public void processMouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		JComponent clickedContainer = (JComponent) e.getSource();
		Point clickedPoint = e.getPoint();

		setLastClickedContainer(clickedContainer);
		setLastClickedPoint(clickedPoint);
		if (!ToolBox.isMacOS() || e.getButton() != MouseEvent.BUTTON3) {
			processMouseClicked(clickedContainer, clickedPoint, e.getClickCount(), e.getModifiersEx() == FlexoCst.MULTI_SELECTION_MASK);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		processMouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		processMouseExited(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		processMousePressed(e);
		if (!e.isConsumed() && _contextualMenuManager != null) {
			_contextualMenuManager.processMousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		processMouseReleased(e);
		if (!e.isConsumed() && _contextualMenuManager != null) {
			_contextualMenuManager.processMouseReleased(e);
		}
	}

	public void processMouseMoved(MouseEvent e) {
		if (_contextualMenuManager != null) {
			_contextualMenuManager.processMouseMoved(e);
		}
	}

	// ==========================================================================
	// ============================= Focus Management ===========================
	// ==========================================================================

	/**
	 * Return currently focused view, if any
	 * 
	 * @return a FocusableView instance
	 */
	public FocusableView getFocusedView() {
		return _focusedPanel;
	}

	/**
	 * Remove focus on supplied view
	 */
	public void removeFocus(FocusableView p) {
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Remove focus on " + p);
		}
		_focusedPanel = null;
		p.setIsFocused(false);
	}

	/**
	 * Add focus on supplied view
	 */
	protected void setIsFocused(FocusableView p) {
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Set focus on " + p);
		}
		_focusedPanel = p;
		p.setIsFocused(true);
		setFocusedObject(p.getObject());
	}

	/**
	 * Return boolean indicating if supplied view if the currently focused view
	 */
	protected boolean isCurrentlyFocused(FocusableView p) {
		return _focusedPanel == p;
	}

	// ==========================================================================
	// ============================= Cut&Paste Management =======================
	// ==========================================================================

	/*@Override
	public FlexoObject getPasteContext() {
		return pasteContextForComponent(getLastClickedContainer());
	}
	
	public abstract FlexoObject pasteContextForComponent(JComponent aComponent);
	
	@Override
	public PastingGraphicalContext getPastingGraphicalContext() {
		return _pastingGraphicalContext;
	}*/

	// ===============================================================
	// ================= Graphical utilities =========================
	// ===============================================================

	public Point getLastClickedPoint() {
		return _pastingGraphicalContext.pastingLocation;
	}

	public JComponent getLastClickedContainer() {
		return _pastingGraphicalContext.targetContainer;
	}

	public void setLastClickedPoint(Point aPoint) {
		// logger.info("setLastClickedPoint=" + aPoint);
		_pastingGraphicalContext.pastingLocation = aPoint;
	}

	public void setLastClickedContainer(JComponent aContainer) {
		_pastingGraphicalContext.targetContainer = aContainer;
	}

}
