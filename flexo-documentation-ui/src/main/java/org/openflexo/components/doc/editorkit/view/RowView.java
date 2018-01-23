/**
 * 
 * Copyright (c) 2014-2017, Openflexo
 * 
 * This file is part of Flexo-Documentation-UI, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Please not that some parts of that component are freely inspired from
 * Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
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
package org.openflexo.components.doc.editorkit.view;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

import org.openflexo.components.doc.editorkit.element.RowElement;

/**
 * Represens view for table's row.
 *
 * @author Stanislav Lapitsky
 */
public class RowView extends BoxView {

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent row element.
	 * @param axis
	 *            either View.X_AXIS or View.Y_AXIS
	 */
	public RowView(RowElement<?, ?> elem, int axis) {
		super(elem, axis);
	}

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent row element.
	 */
	public RowView(Element elem) {
		super(elem, View.X_AXIS);
	}

	/**
	 * Renders using the given rendering surface and area on that surface.
	 *
	 * @param g
	 *            the rendering surface to use
	 * @param a
	 *            the allocated region to render into
	 */
	@Override
	public void paint(Graphics g, Shape a) {
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		int n = getViewCount();
		RowElement<?, ?> row = (RowElement<?, ?>) this.getElement();
		// Unused int cellWidth = (row.getWidth() / row.getChildCount());
		int shift = 0;
		for (int i = 0; i < n; i++) {
			Rectangle tempRect = new Rectangle(alloc.x + shift, alloc.y, row.getCellWidth(i), alloc.height);
			paintChild(g, tempRect, i);
			shift += row.getCellWidth(i);
		}
	}

	/**
	 * Determines the preferred span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getPreferredSpan(int axis) {
		float span = 0;
		if (axis == View.X_AXIS) {
			RowElement<?, ?> row = (RowElement<?, ?>) getElement();
			span = row.getWidth();
		}
		else {
			span = 1;
			for (int i = 0; i < this.getViewCount(); i++) {
				span = Math.max(span, getView(i).getPreferredSpan(axis));
			}
		}
		return span;
	}

	/**
	 * Determines the minimum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getMinimumSpan(int axis) {
		float span = 0;
		if (axis == View.X_AXIS) {
			RowElement<?, ?> row = (RowElement<?, ?>) getElement();
			span = row.getWidth();
		}
		else {
			span = 1;
			for (int i = 0; i < this.getViewCount(); i++) {
				span = Math.max(span, getView(i).getMinimumSpan(axis));
			}
		}
		return span;
	}

	/**
	 * Determines the maximum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getMaximumSpan(int axis) {
		float span = 0;
		if (axis == View.X_AXIS) {
			RowElement<?, ?> row = (RowElement<?, ?>) getElement();
			span = row.getWidth();
		}
		else {
			span = 1;
			for (int i = 0; i < this.getViewCount(); i++) {
				span = Math.max(span, getView(i).getMaximumSpan(axis));
			}
		}
		return span;
	}

	/**
	 * Paints a child. By default that is all it does, but a subclass can use this to paint things relative to the child.
	 *
	 * @param g
	 *            the graphics context
	 * @param alloc
	 *            the allocated region to paint into
	 * @param index
	 *            the child index, >= 0 && < getViewCount()
	 */
	@Override
	protected void paintChild(Graphics g, Rectangle alloc, int index) {
		View child = getView(index);
		child.paint(g, alloc);
	}
}
