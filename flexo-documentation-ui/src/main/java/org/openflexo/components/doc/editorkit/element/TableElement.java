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
import java.util.logging.Logger;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents table element.
 * 
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author Sylvain Guerin
 */
@SuppressWarnings("serial")
public class TableElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends BranchElement
		implements AbstractDocumentElement<FlexoDocTable<D, TA>, D, TA> {

	static final Logger logger = Logger.getLogger(TableElement.class.getPackage().getName());

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	private FlexoDocTable<D, TA> table = null;

	/**
	 * Conscructs a new table element in the document.
	 *
	 * @param rowOffsets
	 *            The start offsets for each table row.
	 * @param rowLengths
	 *            Lengths (char length) for each row.
	 * @param parent
	 *            The parent element.
	 * @param attr
	 *            The attributes for the table.
	 * @param rowCount
	 *            The number of rows.
	 * @param colCount
	 *            The number of columns.
	 * @param widths
	 *            The list of column's widths.
	 * @param heights
	 *            The list of rows' heights.
	 * @param flexoStyledDocument
	 *            TODO
	 */
	public TableElement(FlexoStyledDocument<D, TA> flexoStyledDocument, int[] rowOffsets, int[] rowLengths, Element parent,
			AttributeSet attr, int rowCount, int colCount, int[] widths, int[] heights) {
		flexoStyledDocument.super(parent, attr);
		this.flexoStyledDocument = flexoStyledDocument;
		BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
		if (ba == null) {
			ba = new BorderAttributes();
			ba.setBorders(1 + 2 + 4 + 8 + 16 + 32);
		}

		Element[] rows = new Element[rowCount];
		for (int i = 0; i < rowCount; i++) {
			MutableAttributeSet rowAttr = new SimpleAttributeSet(attr);
			BorderAttributes rowBorders = new BorderAttributes();
			rowBorders.lineColor = ba.lineColor;
			rowBorders.borderLeft = ba.borderLeft;
			rowBorders.borderRight = ba.borderRight;
			rowBorders.borderVertical = ba.borderVertical;
			if (i == 0) {
				rowBorders.borderTop = ba.borderTop;
			}
			else {
				rowBorders.borderTop = ba.borderHorizontal;
			}

			if (i == (rowCount - 1)) {
				rowBorders.borderBottom = ba.borderBottom;
			}

			rowAttr.addAttribute("BorderAttributes", rowBorders);
			int[] cellOffsets = new int[colCount];
			int[] cellLengths = new int[colCount];
			for (int j = 0; j < colCount; j++) {
				cellOffsets[j] = rowOffsets[i] + j; // offset+i*colCount+j;
				cellLengths[j] = 1;
			}
			rows[i] = new RowElement<>(this.flexoStyledDocument, this, rowAttr, colCount, cellOffsets, cellLengths, widths, heights[i]);
		}
		this.replace(0, 0, rows);
	}

	/**
	 * Gets the element name.
	 *
	 * @return The element name.
	 */
	@Override
	public String getName() {
		return "table";
	}

	/**
	 * Gets the table width (sum of column widths).
	 */
	@SuppressWarnings("unchecked")
	public int getWidth() {
		RowElement<D, TA> row = (RowElement<D, TA>) getElement(0);
		return row.getWidth();
	}

	/**
	 * Gets the table height (sum of row heights).
	 */
	@SuppressWarnings("unchecked")
	public int getHeight() {
		int cnt = getElementCount();
		int height = 1;
		for (int i = 0; i < cnt; i++) {
			RowElement<D, TA> row = (RowElement<D, TA>) getElement(i);
			height += row.getHeight();
		}
		return height;
	}

	/**
	 * Checks whether the element is a leaf.
	 *
	 * @return True if a leaf.
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	/**
	 * Sets table borders.
	 *
	 * @param ba
	 *            The new border attributes.
	 */
	@SuppressWarnings("unchecked")
	public void setBorders(BorderAttributes ba) {
		this.flexoStyledDocument.fireWriteLock();
		this.addAttribute("BorderAttributes", ba);
		for (int i = 0; i < getElementCount(); i++) {
			RowElement<D, TA> row = (RowElement<D, TA>) getElement(i);
			BorderAttributes rowBorders = (BorderAttributes) row.getAttribute("BorderAttributes");
			rowBorders.lineColor = ba.lineColor;
			rowBorders.borderLeft = ba.borderLeft;
			rowBorders.borderRight = ba.borderRight;
			rowBorders.borderVertical = ba.borderVertical;
			if (i == 0) {
				rowBorders.borderTop = ba.borderTop;
			}
			else {
				rowBorders.borderTop = ba.borderHorizontal;
			}

			if (i == (getElementCount() - 1)) {
				rowBorders.borderBottom = ba.borderBottom;
			}
			row.setBorders(rowBorders);
		}
		this.flexoStyledDocument.fireWriteUnlock();
	}

	public BorderAttributes getBorders() {
		return (BorderAttributes) getAttribute("BorderAttributes");
	}

	/**
	 * Sets table margins. (For each cell)
	 *
	 * @param ba
	 *            The new margins.
	 */
	@SuppressWarnings("unchecked")
	public void setMargins(Insets margins) {
		this.flexoStyledDocument.fireWriteLock();
		int cnt = getElementCount();
		for (int i = 0; i < cnt; i++) {
			RowElement<D, TA> row = (RowElement<D, TA>) getElement(i);
			int cnt2 = row.getElementCount();
			for (int j = 0; j < cnt2; j++) {
				CellElement<D, TA> cell = (CellElement<D, TA>) row.getElement(j);
				cell.setMargins(margins);
			}
		}
		this.flexoStyledDocument.fireWriteUnlock();
	}

	/**
	 * Sets table alignment.
	 *
	 * @param ba
	 *            The new margins.
	 */
	public void setAlignment(int align) {
		this.flexoStyledDocument.fireWriteLock();
		StyleConstants.setAlignment((MutableAttributeSet) this.getAttributes(), align);
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
	public FlexoDocTable<D, TA> getDocObject() {
		return table;
	}

	@Override
	public FlexoDocTable<D, TA> lookupDocObject() {

		int index = getParent().getIndex(this);

		int elementIndex = 0;
		if (getFlexoDocument() != null) {
			for (FlexoDocElement<D, TA> e : getFlexoDocument().getElements()) {
				if (e instanceof FlexoDocTable) {
					if (elementIndex == index) {
						table = (FlexoDocTable<D, TA>) e;
						break;
					}
					if (elementIndex > index) {
						logger.warning("Could not find FlexoDocTable for " + this);
						return null;
					}
				}
				if (e instanceof FlexoDocParagraph || e instanceof FlexoDocTable) {
					elementIndex++;
				}
			}
		}
		for (int i = 0; i < getElementCount(); i++) {
			Element e = getElement(i);
			if (e instanceof AbstractDocumentElement) {
				((AbstractDocumentElement<?, ?, ?>) e).lookupDocObject();
			}
		}
		return table;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {

		return AbstractDocumentElement.retrieveElement((AbstractDocumentElement<O, D, TA>) this, docObject);
	}

	@Override
	public String toString() {
		return "TableElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
	}
}
// ----- end TABLE --------------------------------------------------------------
// --- ROW ----------------------------------------------------------------------
