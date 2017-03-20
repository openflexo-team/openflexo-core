package org.openflexo.components.doc.editorkit.element;

import java.awt.Insets;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocElement;
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
public class TableElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends BranchElement
		implements AbstractDocumentElement<FlexoDocTable<D, TA>, D, TA> {

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
			rows[i] = new RowElement<D, TA>(this.flexoStyledDocument, this, rowAttr, colCount, cellOffsets, cellLengths, widths,
					heights[i]);
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
		int paragraphIndex = 0;
		if (getFlexoDocument() != null) {
			for (FlexoDocElement<D, TA> e : getFlexoDocument().getElements()) {
				if (e instanceof FlexoDocTable) {
					if (paragraphIndex == index) {
						table = (FlexoDocTable<D, TA>) e;
						break;
					}
					paragraphIndex++;
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

	@Override
	public String toString() {
		return "TableElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
	}
}
// ----- end TABLE --------------------------------------------------------------
// --- ROW ----------------------------------------------------------------------
