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

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents table's row element.
 * 
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author Sylvain Guerin
 */
@SuppressWarnings("serial")
public class RowElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends BranchElement
		implements AbstractDocumentElement<FlexoDocTableRow<D, TA>, D, TA> {

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	private FlexoDocTableRow<D, TA> tableRow = null;

	/**
	 * Conscructs a new row element in the table.
	 *
	 * @param parent
	 *            The parent table element.
	 * @param attr
	 *            The row attributes.
	 * @param cellCount
	 *            The number of cells.
	 * @param cellOffsets
	 *            Offsets for each cell.
	 * @param cellLengths
	 *            Lengths (char length) for each cell.
	 * @param widths
	 *            Widths (in pixels) for each cell.
	 * @param height
	 *            row height.
	 * @param flexoStyledDocument
	 *            TODO
	 */
	public RowElement(FlexoStyledDocument<D, TA> flexoStyledDocument, Element parent, AttributeSet attr, int cellCount, int[] cellOffsets,
			int[] cellLengths, int[] widths, int height) {
		flexoStyledDocument.super(parent, attr);
		this.flexoStyledDocument = flexoStyledDocument;

		BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
		Element[] cells = new Element[cellCount];
		for (int i = 0; i < cellCount; i++) {
			MutableAttributeSet cellAttr = new SimpleAttributeSet(attr);
			BorderAttributes cellBorders = new BorderAttributes();
			cellBorders.lineColor = ba.lineColor;
			cellBorders.borderTop = ba.borderTop;
			cellBorders.borderBottom = ba.borderBottom;
			if (i == 0) {
				cellBorders.borderLeft = ba.borderLeft;
			}
			else {
				cellBorders.borderLeft = ba.borderVertical;
			}

			if (i == (cellCount - 1)) {
				cellBorders.borderRight = ba.borderRight;
			}
			cellAttr.addAttribute("BorderAttributes", cellBorders);
			cells[i] = new CellElement<>(this.flexoStyledDocument, this, cellAttr, cellOffsets[i], cellLengths[i], widths[i], height);
		}
		this.replace(0, 0, cells);
	}

	/**
	 * Gets element name.
	 */
	@Override
	public String getName() {
		return "row";
	}

	/**
	 * Checks whether the element is a leaf.
	 *
	 * @return true if a leaf.
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	/**
	 * Gets row width (in pixels)
	 */
	@SuppressWarnings("unchecked")
	public int getWidth() {
		int width = 0;
		for (int i = 0; i < getElementCount(); i++) {
			CellElement<D, TA> cell = (CellElement<D, TA>) getElement(i);
			width += cell.getWidth();
		}
		return width;
	}

	/**
	 * Gets row height (in pixels)
	 */
	@SuppressWarnings("unchecked")
	public int getHeight() {
		int height = 0;
		for (int i = 0; i < getElementCount(); i++) {
			CellElement<D, TA> cell = (CellElement<D, TA>) getElement(i);
			height = Math.max(cell.getHeight(), height);
		}
		return height;
	}

	/**
	 * Gets widths of the cell.
	 *
	 * @param index
	 *            The number of cell.
	 */
	@SuppressWarnings("unchecked")
	public int getCellWidth(int index) {
		CellElement<D, TA> cell = (CellElement<D, TA>) getElement(index);
		return cell.getWidth();
	}

	/**
	 * Sets row borders attributes.
	 *
	 * @param ba
	 *            The border attributes.
	 */
	@SuppressWarnings("unchecked")
	public void setBorders(BorderAttributes ba) {
		BorderAttributes currentBorders = (BorderAttributes) getAttribute("BorderAttributes");
		currentBorders.setBorders(ba.getBorders());
		currentBorders.lineColor = ba.lineColor;

		for (int i = 0; i < getElementCount(); i++) {
			CellElement<D, TA> cell = (CellElement<D, TA>) getElement(i);
			BorderAttributes cellBorders = new BorderAttributes();
			cellBorders.lineColor = ba.lineColor;
			cellBorders.borderTop = ba.borderTop;
			cellBorders.borderBottom = ba.borderBottom;
			if (i == 0) {
				cellBorders.borderLeft = ba.borderLeft;
			}
			else {
				cellBorders.borderLeft = ba.borderVertical;
			}

			if (i == (getElementCount() - 1)) {
				cellBorders.borderRight = ba.borderRight;
			}
			cell.setBorders(cellBorders);
		} // for
	}

	/**
	 * Sets row margins.
	 *
	 * @param margins
	 *            new margins
	 */
	@SuppressWarnings("unchecked")
	public void setMargins(Insets margins) {
		this.flexoStyledDocument.fireWriteLock();
		int cnt = getElementCount();
		for (int i = 0; i < cnt; i++) {
			CellElement<D, TA> cell = (CellElement<D, TA>) getElement(i);
			cell.setMargins(margins);
		}
		this.flexoStyledDocument.fireWriteUnlock();
	}

	/**
	 * Sets row height (height for each cell).
	 *
	 * @param height
	 *            height value.
	 */
	@SuppressWarnings("unchecked")
	public void setHeight(int height) {
		this.flexoStyledDocument.fireWriteLock();
		int cnt = getElementCount();
		for (int i = 0; i < cnt; i++) {
			CellElement<D, TA> cell = (CellElement<D, TA>) getElement(i);
			cell.height = height;
		}
		this.flexoStyledDocument.fireWriteUnlock();
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
	public FlexoDocTableRow<D, TA> getDocObject() {
		return tableRow;
	}

	@Override
	public FlexoDocTableRow<D, TA> lookupDocObject() {
		// TODO
		return null;
	}

	@Override
	public String toString() {
		return "RowElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {

		return AbstractDocumentElement.retrieveElement((AbstractDocumentElement<O, D, TA>) this, docObject);
	}

}
