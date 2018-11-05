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

import javax.swing.SizeRequirements;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

import org.openflexo.components.doc.editorkit.element.TableElement;

/**
 * Represents view for a table element.
 *
 * @author Stanislav Lapitsky
 */
public class TableView extends BoxView {

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent table element.
	 * @param axis
	 *            either View.X_AXIS or View.Y_AXIS
	 */
	public TableView(TableElement<?, ?> elem, int axis) {
		super(elem, axis);
	}

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent cell element.
	 */
	public TableView(Element elem) {
		super(elem, View.Y_AXIS);
	}

	/**
	 * Determines base line requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements baselineRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.baselineRequirements(axis, r);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
	}

	/**
	 * Determines major requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMajorAxisRequirements(axis, r);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
	}

	/**
	 * Determines minor requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMinorAxisRequirements(axis, r);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
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
		span = super.getPreferredSpan(axis);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
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
		span = super.getMinimumSpan(axis);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
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
		span = super.getMaximumSpan(axis);
		TableElement<?, ?> table = (TableElement<?, ?>) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
		}
		return span;
	}

	/**
	 * Determines the desired alignment for this view along an axis. This is implemented to give the alignment to the center of the first
	 * row along the y axis, and the default along the x axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the desired alignment. This should be a value between 0.0 and 1.0 inclusive, where 0 indicates alignment at the origin and
	 *          1.0 indicates alignment to the full span away from the origin. An alignment of 0.5 would be the center of the view.
	 */
	@Override
	public float getAlignment(int axis) {
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(getElement().getAttributes());
			float a;
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					a = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					a = 1;
					break;
				default:
					a = 0.5f;
			}
			return a;
		}
		else {
			return super.getAlignment(axis);
		}
	}
}
