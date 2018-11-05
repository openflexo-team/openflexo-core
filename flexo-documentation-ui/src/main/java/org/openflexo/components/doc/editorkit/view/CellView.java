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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.SizeRequirements;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.element.CellElement;

/**
 * Represents view for a table cell element.
 *
 * @author Stanislav Lapitsky
 */
public class CellView extends BoxView {

	/**
	 * Constructs new cell view instance.
	 *
	 * @param elem
	 *            the element this view is responsible for
	 */
	public CellView(Element elem) {
		super(elem, View.Y_AXIS);
		CellElement<?, ?> cell = (CellElement<?, ?>) this.getElement();
		Insets margins = cell.getMargins();

		this.setInsets((short) (margins.top), (short) (margins.left), (short) (margins.bottom), (short) (margins.right));
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
		CellElement<?, ?> cell = (CellElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = cell.getWidth();
		}
		else {
			span = Math.max(super.getPreferredSpan(axis), cell.getHeight());
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
		CellElement<?, ?> cell = (CellElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = cell.getWidth();
		}
		else {
			span = Math.max(super.getMinimumSpan(axis), cell.getHeight());
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
		CellElement<?, ?> cell = (CellElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = cell.getWidth();
		}
		else {
			span = Math.max(super.getMaximumSpan(axis), cell.getHeight());
		}
		return span;
	}

	/**
	 * Determines base line requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 * @return A base line requirement.
	 */
	@Override
	protected SizeRequirements baselineRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.baselineRequirements(axis, r);
		if (axis == View.Y_AXIS)
			sr.alignment = 0f;
		return sr;
	}

	/**
	 * Determines major requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 * @return A major requirement.
	 */
	@Override
	protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMajorAxisRequirements(axis, r);
		if (axis == View.Y_AXIS)
			sr.alignment = 0f;
		return sr;
	}

	/**
	 * Determines minor requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 * @return A minor requirement.
	 */
	@Override
	protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMinorAxisRequirements(axis, r);
		if (axis == View.Y_AXIS)
			sr.alignment = 0f;
		return sr;
	}

	/**
	 * Performs layout of cells and cell's margins.
	 *
	 * @param width
	 *            - the width of cell.
	 * @param height
	 *            - the height of cell.
	 */
	@Override
	protected void layout(int width, int height) {
		CellElement<?, ?> cell = (CellElement<?, ?>) this.getElement();
		Insets margins = cell.getMargins();
		this.setInsets((short) (margins.top), (short) (margins.left), (short) (margins.bottom), (short) (margins.right));
		super.layout(width, height);
	}

	/**
	 * Renders using the given rendering surface and area on that surface. If page is printed then borders do not renders.
	 *
	 * @param g
	 *            the rendering surface to use
	 * @param a
	 *            the allocated region to render into
	 */
	@Override
	public void paint(Graphics g, Shape a) {
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		super.paint(g, a);
		CellElement<?, ?> cell = (CellElement<?, ?>) getElement();
		BorderAttributes ba = (BorderAttributes) cell.getAttribute("BorderAttributes");

		Color oldColor = g.getColor();
		g.setColor(ba.lineColor);

		// --- DRAW LEFT ---
		if (ba.borderLeft != 0) {
			g.drawLine(alloc.x, alloc.y, alloc.x, alloc.y + alloc.height);
		}
		// --- DRAW RIGHT ---
		if (ba.borderRight != 0) {
			g.drawLine(alloc.x + alloc.width, alloc.y, alloc.x + alloc.width, alloc.y + alloc.height);
		}
		// --- DRAW TOP ---
		if (ba.borderTop != 0) {
			g.drawLine(alloc.x, alloc.y, alloc.x + alloc.width, alloc.y);
		}
		// --- DRAW BOTTOM ---
		if (ba.borderBottom != 0) {
			g.drawLine(alloc.x, alloc.y + alloc.height, alloc.x + alloc.width, alloc.y + alloc.height);
		}
		g.setColor(oldColor);
	}
}
