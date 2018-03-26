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

package org.openflexo.components.doc.editorkit.element;

import java.awt.Insets;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents table's cell element.
 * 
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author Sylvain Guerin
 */
@SuppressWarnings("serial")
public class CellElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends BranchElement
		implements AbstractDocumentElement<FlexoDocTableCell<D, TA>, D, TA> {

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	private FlexoDocTableCell<D, TA> tableCell = null;

	/**
	 * Cell width (in pixels).
	 */
	private int width = 1;

	/**
	 * Cell height (in pixels).
	 */
	int height = 1;

	/**
	 * Initial margin value.
	 */
	public static final int MARGIN_MIN = 2;
	/**
	 * Stores the cell's margins: top, left, bottom, right.
	 */
	private Insets m_margins = new Insets(MARGIN_MIN, MARGIN_MIN, MARGIN_MIN, MARGIN_MIN);

	/**
	 * Constructs new empty cell element (cell without content) in the row.
	 *
	 * @param parent
	 *            The parent row element.
	 * @param attr
	 *            The cell's attributes.
	 * @param startOffset
	 *            The start offset in the document content.
	 * @param length
	 *            The length of cell (in chars).
	 * @param width
	 *            The cell width (in pixels).
	 * @param flexoStyledDocument
	 *            TODO
	 */
	public CellElement(FlexoStyledDocument<D, TA> flexoStyledDocument, Element parent, AttributeSet attr, int startOffset, int length,
			int width, int height) {
		flexoStyledDocument.super(parent, attr);
		this.flexoStyledDocument = flexoStyledDocument;
		this.width = width;
		this.height = height;
		BranchElement paragraph = flexoStyledDocument.new BranchElement(this, null);

		LeafElement brk = flexoStyledDocument.new LeafElement(paragraph, null, startOffset, startOffset + length);
		Element[] buff = new Element[1];
		buff[0] = brk;
		paragraph.replace(0, 0, buff);

		buff[0] = paragraph;
		this.replace(0, 0, buff);
	}

	/**
	 * Constructs cell element with definite content.
	 *
	 * @param parent
	 *            The parent row.
	 * @param attr
	 *            The row attributes.
	 * @param paragraphOffsets
	 *            Offsets of inner elements.
	 * @param paragraphLenghts
	 *            Lengths of inner elements.
	 * @param width
	 *            The cell width.
	 * @param flexoStyledDocument
	 *            TODO
	 */
	public CellElement(FlexoStyledDocument<D, TA> flexoStyledDocument, Element parent, AttributeSet attr, int[] paragraphOffsets,
			int[] paragraphLenghts, int width) {
		flexoStyledDocument.super(parent, attr);
		this.flexoStyledDocument = flexoStyledDocument;
		this.width = width;
	}

	/**
	 * Gets element name.
	 */
	@Override
	public String getName() {
		return "cell";
	}

	/**
	 * Gets cell width (in pixels).
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets cell width (in pixels).
	 *
	 * @param w
	 *            New cell widths.
	 */
	public void setWidth(int w) {
		width = w;
	}

	/**
	 * Gets cell height (in pixels).
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets cell height (in pixels).
	 *
	 * @param h
	 *            New cell widths.
	 */
	public void setHeight(int h) {
		height = h;
	}

	/**
	 * Gets the cell's margins.
	 *
	 * @return the page's margins.
	 */
	public Insets getMargins() {
		return m_margins;
	}

	/**
	 * Sets the cell's margins.
	 *
	 * @param margins
	 *            - the page's margins.
	 */
	public void setMargins(Insets margins) {
		this.m_margins = margins;
	}

	/**
	 * Sets the cell's margins. Limits is between 5 and 300.
	 *
	 * @param top
	 *            - the top margin.
	 * @param left
	 *            - the left margin.
	 * @param bottom
	 *            - the bottom margin.
	 * @param right
	 *            - the right margin.
	 */
	public void setMargins(int top, int left, int bottom, int right) {
		this.m_margins.top = top;
		this.m_margins.left = left;
		this.m_margins.bottom = bottom;
		this.m_margins.right = right;
	}

	/**
	 * Sets row borders attributes.
	 *
	 * @param ba
	 *            The border attributes.
	 */
	public void setBorders(BorderAttributes ba) {
		BorderAttributes cellBorders = (BorderAttributes) this.getAttribute("BorderAttributes");
		cellBorders.lineColor = ba.lineColor;
		cellBorders.borderTop = ba.borderTop;
		cellBorders.borderBottom = ba.borderBottom;
		cellBorders.borderLeft = ba.borderLeft;
		cellBorders.borderRight = ba.borderRight;
		DefaultDocumentEvent dde = flexoStyledDocument.new DefaultDocumentEvent(Math.max(getStartOffset() - 1, 0), getEndOffset(),
				DocumentEvent.EventType.CHANGE);
		dde.end();
		this.flexoStyledDocument.fireChangedUpdate(dde);
	}

	public BorderAttributes getBorders() {
		return (BorderAttributes) this.getAttribute("BorderAttributes");
	}

	@Override
	public FlexoStyledDocument<D, TA> getFlexoStyledDocument() {
		return flexoStyledDocument;
	}

	@Override
	public D getFlexoDocument() {
		return getFlexoStyledDocument().getFlexoDocument();
	}

	@Override
	public FlexoDocTableCell<D, TA> getDocObject() {
		return tableCell;
	}

	@Override
	public FlexoDocTableCell<D, TA> lookupDocObject() {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {

		return AbstractDocumentElement.retrieveElement((AbstractDocumentElement<O, D, TA>) this, docObject);
	}

}
