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
public class CellElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends BranchElement
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

}
