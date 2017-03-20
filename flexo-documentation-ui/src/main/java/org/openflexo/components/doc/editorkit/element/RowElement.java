package org.openflexo.components.doc.editorkit.element;

import java.awt.Insets;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
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
public class RowElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends BranchElement
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
			cells[i] = new CellElement<D, TA>(this.flexoStyledDocument, this, cellAttr, cellOffsets[i], cellLengths[i], widths[i], height);
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
}
