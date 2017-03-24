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

import java.awt.Insets;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;

/**
 * Represents document root view.
 *
 * @author Stanislav Lapitsky
 */
public class SectionView extends BoxView {

	/**
	 * Constructs a section view.
	 *
	 * @param elem
	 *            the element this view is responsible for
	 * @param axis
	 *            either <code>View.X_AXIS</code> or <code>View.Y_AXIS</code>
	 */
	public SectionView(Element elem, int axis) {
		super(elem, axis);
	}

	/**
	 * Perform layout on the box
	 *
	 * @param width
	 *            the width (inside of the insets) >= 0
	 * @param height
	 *            the height (inside of the insets) >= 0
	 */
	@Override
	protected void layout(int width, int height) {
		FlexoStyledDocument doc = (FlexoStyledDocument) getDocument();
		Insets margins = doc.getDocumentMargins();
		this.setInsets((short) margins.top, (short) margins.left, (short) margins.bottom, (short) margins.right);

		if (doc.DOCUMENT_WIDTH > 0) {
			super.layout(doc.DOCUMENT_WIDTH - margins.left - margins.right, height);
		}
		else {
			super.layout(width, height);
		}
	}

	/**
	 * Determines the minimum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 * @exception IllegalArgumentException
	 *                for an invalid axis type
	 */
	@Override
	public float getMinimumSpan(int axis) {
		if (axis == View.X_AXIS) {
			FlexoStyledDocument doc = (FlexoStyledDocument) this.getDocument();
			if (doc.DOCUMENT_WIDTH > 0)
				return doc.DOCUMENT_WIDTH;
			else
				return super.getMinimumSpan(axis);
		}
		else {
			return super.getMinimumSpan(axis);
		}
	}

	/**
	 * Determines the maximum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 * @exception IllegalArgumentException
	 *                for an invalid axis type
	 */
	@Override
	public float getMaximumSpan(int axis) {
		if (axis == View.X_AXIS) {
			FlexoStyledDocument doc = (FlexoStyledDocument) this.getDocument();
			if (doc.DOCUMENT_WIDTH > 0)
				return doc.DOCUMENT_WIDTH;
			else
				return super.getMinimumSpan(axis);
		}
		else {
			return super.getMaximumSpan(axis);
		}
	}

	/**
	 * Determines the preferred span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 * @exception IllegalArgumentException
	 *                for an invalid axis type
	 */
	@Override
	public float getPreferredSpan(int axis) {
		if (axis == View.X_AXIS) {
			FlexoStyledDocument doc = (FlexoStyledDocument) this.getDocument();
			if (doc.DOCUMENT_WIDTH > 0)
				return doc.DOCUMENT_WIDTH;
			else
				return super.getMinimumSpan(axis);
		}
		else {
			return super.getPreferredSpan(axis);
		}
	}

}
