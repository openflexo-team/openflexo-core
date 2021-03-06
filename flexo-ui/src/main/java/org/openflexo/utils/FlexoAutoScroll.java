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

package org.openflexo.utils;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openflexo.logging.FlexoLogger;

/**
 * @author gpolet
 * 
 */
public class FlexoAutoScroll {
	private static final Logger logger = FlexoLogger.getLogger(FlexoAutoScroll.class.getPackage().getName());

	/**
	 * 
	 * @param scrollable
	 *            - a component contained in a JScrollPane
	 * @param p
	 * @param margin
	 *            - the width of your insets where to scroll (imagine a border of that width all around your component. Whenever the mouse
	 *            enters that border, it will start scrolling
	 */
	public static void autoscroll(JComponent scrollable, Point p, int margin) {
		JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, scrollable);
		if (scroll == null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Not inside a scroll pane, cannot scroll!");
			}
			return;
		}
		Rectangle visible = scrollable.getVisibleRect();
		p.x -= visible.x;
		p.y -= visible.y;
		Rectangle inner = scrollable.getParent().getBounds();
		inner.x += margin;
		inner.y += margin;
		inner.height -= 2 * margin;
		inner.width -= 2 * margin;
		if (p.x < inner.x) {// Move Left
			JScrollBar bar = scroll.getHorizontalScrollBar();
			if (bar != null) {
				if (bar.getValue() > bar.getMinimum()) {
					bar.setValue(bar.getValue() - bar.getUnitIncrement(-1));
				}
			}
		} else if (p.x > inner.x + inner.width) { // Move right
			JScrollBar bar = scroll.getHorizontalScrollBar();
			if (bar != null) {
				if (bar.getValue() < bar.getMaximum()) {
					bar.setValue(bar.getValue() + bar.getUnitIncrement(1));
				}
			}
		}
		if (p.y < inner.y) { // Move up
			JScrollBar bar = scroll.getVerticalScrollBar();
			if (bar != null) {
				if (bar.getValue() > bar.getMinimum()) {
					bar.setValue(bar.getValue() - bar.getUnitIncrement(-1));
				}
			}
		} else if (p.y > inner.y + inner.height) { // Move down
			JScrollBar bar = scroll.getVerticalScrollBar();
			if (bar != null) {
				if (bar.getValue() < bar.getMaximum()) {
					bar.setValue(bar.getValue() + bar.getUnitIncrement(1));
				}
			}
		}
	}

	public static Insets getAutoscrollInsets(JComponent scrollable, int margin) {
		Rectangle outer = scrollable.getBounds();
		Rectangle inner = scrollable.getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y + outer.y + margin,
				outer.width - inner.width - inner.x + outer.x + margin);
	}
}
