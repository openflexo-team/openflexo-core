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

package org.openflexo.components.doc.editorkit;

import java.awt.Insets;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoableEdit;

import org.openflexo.components.doc.editorkit.element.AbstractDocumentElement;
import org.openflexo.components.doc.editorkit.element.CellElement;
import org.openflexo.components.doc.editorkit.element.DocumentElement;
import org.openflexo.components.doc.editorkit.element.ParagraphElement;
import org.openflexo.components.doc.editorkit.element.RowElement;
import org.openflexo.components.doc.editorkit.element.RunElement;
import org.openflexo.components.doc.editorkit.element.TableElement;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Internal representation of a {@link FlexoDocument} (a document conform to FlexoDocumentation API)<br
 * 
 * Represent the logical structure of underlying document while providing character and paragraph styles in a manner similar to the Rich
 * Text Format. The element structure for this document represents style crossings for style runs. These style runs are mapped into a
 * paragraph element structure (which may reside in some other structure). The style runs break at paragraph boundaries since logical styles
 * are assigned to paragraph boundaries.
 * 
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author Sylvain Guerin
 * @see DefaultStyledDocument
 */
@SuppressWarnings("serial")
public class FlexoStyledDocument<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends DefaultStyledDocument {

	static final Logger logger = Logger.getLogger(FlexoStyledDocument.class.getPackage().getName());

	public int DOCUMENT_WIDTH = -1;
	/**
	 * Represents document margins
	 */
	private Insets margins = new Insets(0, 0, 0, 0);

	D flexoDocument;

	private boolean isReadingDocument = false;

	/**
	 * Constructs a styled document.
	 *
	 * @param c
	 *            The container for the content.
	 * @param styles
	 *            Resources and style definitions which may be shared across documents.
	 */
	public FlexoStyledDocument(D flexoDocument, Content c, StyleContext styles) {
		super(c, styles);
		this.flexoDocument = flexoDocument;
		addDocumentListener(new StructuredContentListener());
	}

	/**
	 * Constructs a styled document with the default content storage implementation and a shared set of styles.
	 *
	 * @param styles
	 *            The styles.
	 */
	public FlexoStyledDocument(D flexoDocument, StyleContext styles) {
		this(flexoDocument, new GapContent(BUFFER_SIZE_DEFAULT), styles);
	}

	/**
	 * Constructs a default styled document. This buffers input content by a size of BUFFER_SIZE_DEFAULT and has a style context that is
	 * scoped by the lifetime of the document and is not shared with other documents.
	 */
	public FlexoStyledDocument(D flexoDocument) {
		this(flexoDocument, new GapContent(BUFFER_SIZE_DEFAULT), new StyleContext());
	}

	/**
	 * Return conceptual document beeing represented by this {@link FlexoStyledDocument}
	 * 
	 * @return
	 */
	public D getFlexoDocument() {
		return flexoDocument;
	}

	/**
	 * Inserts a new table in the document.
	 *
	 * @param offset
	 *            The document offset where table will be inserted.
	 * @param rowCount
	 *            The number of rows in the table.
	 * @param colCount
	 *            The number of columns in the table.
	 * @param attr
	 *            The table attributes. (contains border parameters)
	 * @param colWidths
	 *            Widths for each table's column.
	 * @param rowHeights
	 *            heights for each table's row.
	 */
	public Element insertTable(int offset, int rowCount, int colCount, AttributeSet attr, int[] colWidths, int[] rowHeights) {
		Element table = null;
		try {
			// search for table's parent element
			// for plain table this parent is document root
			// for nested table this parent is table-container
			Element root = getDefaultRootElement();

			Element elem = root;
			while (!elem.isLeaf()) {
				root = elem;
				elem = elem.getElement(elem.getElementIndex(offset));
			}
			Element paragraph = root;
			root = root.getParentElement();
			int insertIndex = root.getElementIndex(offset);

			if ((offset > paragraph.getStartOffset()) && (offset < paragraph.getEndOffset())) {
				insertString(offset, "\n", new SimpleAttributeSet());
				insertIndex++;
			}

			int insertOffset = root.getElement(insertIndex).getStartOffset();
			Content c = getContent();
			String ins = "";
			// insert number of paragraphs (one paragraph for each table cell)
			for (int i = 0; i < rowCount * colCount; i++) {
				ins += '\n';
			}
			writeLock();
			UndoableEdit u = c.insertString(insertOffset, ins);
			DefaultDocumentEvent dde = new DefaultDocumentEvent(insertOffset, rowCount * colCount, DocumentEvent.EventType.INSERT);
			dde.addEdit(u);
			insertUpdate(dde, new SimpleAttributeSet());
			dde.end();

			fireInsertUpdate(dde);

			// calculates rows' offsets and lengths
			DefaultDocumentEvent e = new DefaultDocumentEvent(insertOffset, rowCount * colCount, DocumentEvent.EventType.INSERT);
			e.addEdit(u);
			int[] rowOffsets = new int[rowCount];
			int[] rowLenghts = new int[rowCount];
			for (int i = 0; i < rowCount; i++) {
				rowOffsets[i] = insertOffset + i * colCount;
				rowLenghts[i] = colCount;
			}

			// create table element
			table = new TableElement<>(this, rowOffsets, rowLenghts, root, attr, rowCount, colCount, colWidths, rowHeights);
			Element[] el = new Element[1];
			el[0] = table;
			Element[] repl = new Element[rowCount * colCount];
			for (int i = 0; i < rowCount * colCount; i++) {
				repl[i] = root.getElement(insertIndex + i);
			}
			// replace paragraphs with table
			((BranchElement) root).replace(insertIndex, rowCount * colCount, el);
			ElementEdit uu = new ElementEdit(root, insertIndex, repl, el);
			e.addEdit(uu);
			fireInsertUpdate(e);
			e.end();
		} catch (Exception error) {
			System.err.println("Can't insert table!");
			error.printStackTrace();
		} finally {
			writeUnlock();
		}
		return table;
	}

	/**
	 * Inserts picture to specified document offset.
	 *
	 * @param icon
	 *            picture for inserting.
	 * @param pos
	 *            offset in the document.
	 */
	public void insertPicture(ImageIcon icon, int pos) {
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setIcon(attrs, icon);
		try {
			insertString(pos, " ", attrs);
		} catch (BadLocationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Can't insert image!");
		}
	}

	/**
	 * Deletes table from the document. Method tries to find a deepest table for given offset and deletes it. If no table found method does
	 * nothing.
	 *
	 * @param offset
	 *            offset in the document.
	 */
	public void deleteTable(int offset) {
		Element elem = getDefaultRootElement();

		Element table = null;
		// search for the deepest table
		while (!elem.isLeaf()) {
			if (elem.getName().equals("table"))
				table = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		if (table != null) {
			BranchElement root = (BranchElement) table.getParentElement();
			// if table contains only one row delete whole table
			if (root.getChildCount() == 1) {
				return;
			}
			int start = table.getStartOffset();
			int end = table.getEndOffset();
			try {
				DefaultDocumentEvent e = new DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
				int index = root.getElementIndex(offset);
				ElementEdit ee = new ElementEdit(root, index, new Element[] { table }, new Element[0]);
				this.getContent().remove(start, end - start);
				root.replace(index, 1, new Element[0]);
				e.addEdit(ee);
				e.end();
				this.fireRemoveUpdate(e);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Can't delete table! " + ex.getMessage());
			}
		}
	}

	/**
	 * Deletes row from table. Method tries to find a deepest table for given offset and deletes row which contains given offset. If no
	 * table found method does nothing.
	 *
	 * @param offset
	 *            offset in the document.
	 */
	public void deleteRow(int offset) {
		Element elem = getDefaultRootElement();

		Element row = null;
		// search for the deepest table
		while (!elem.isLeaf()) {
			if (elem.getName().equals("row"))
				row = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		if (row != null) {
			BranchElement table = (BranchElement) row.getParentElement();
			// if table contains only one row delete whole table
			if (table.getChildCount() == 1) {
				try {
					remove(table.getStartOffset(), table.getEndOffset() - table.getStartOffset());
					return;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				;
			}
			int start = row.getStartOffset();
			int end = row.getEndOffset();
			try {
				DefaultDocumentEvent e = new DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
				int rowNum = table.getElementIndex(offset);
				ElementEdit ee = new ElementEdit(table, rowNum, new Element[] { row }, new Element[0]);
				this.getContent().remove(start, end - start);
				table.replace(rowNum, 1, new Element[0]);
				e.addEdit(ee);
				e.end();
				this.fireRemoveUpdate(e);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Can't delete row! " + ex.getMessage());
			}
		}
	}

	/**
	 * Deletes column from table. Method tries to find a deepest table for given offset and deletes column which contains given offset. It
	 * means that we get index of cell which contains given offset and for each table's row delete cell with this index.
	 * <p/>
	 * If no table found method does nothing.
	 *
	 * @param offset
	 *            offset in the document.
	 */
	public void deleteColumn(int offset) {
		Element elem = getDefaultRootElement();

		Element cell = null;
		Element table = null;
		// search for the deepest table
		while (!elem.isLeaf()) {
			if (elem.getName().equals("table"))
				table = elem;
			if (elem.getName().equals("cell"))
				cell = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		if (cell != null) {
			Element row = cell.getParentElement();
			// if table contains only one column delete whole table
			if (row.getElementCount() == 1) {
				try {
					remove(table.getStartOffset(), table.getEndOffset() - table.getStartOffset());
					return;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			int colNum = row.getElementIndex(offset);
			// for each row delete cell with specified index
			for (int i = 0; i < table.getElementCount(); i++) {
				BranchElement editableRow = (BranchElement) table.getElement(i);
				Element editableCell = editableRow.getElement(colNum);
				DefaultDocumentEvent e = new DefaultDocumentEvent(editableCell.getStartOffset(),
						editableCell.getEndOffset() - editableCell.getStartOffset(), DocumentEvent.EventType.REMOVE);
				ElementEdit ee = new ElementEdit(editableRow, colNum, new Element[] { editableCell }, new Element[0]);
				try {
					this.getContent().remove(editableCell.getStartOffset(), editableCell.getEndOffset() - editableCell.getStartOffset());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				e.addEdit(ee);
				e.end();

				editableRow.replace(colNum, 1, new Element[0]);

				this.fireRemoveUpdate(e);
			}

		}
	}

	/**
	 * Inserts new row into deepest table for specified offset. Method tries to find a deepest table for given offset than defines row which
	 * contains given offset and inserts new row above or below (depends on flag) current.
	 * <p/>
	 * If no table found method does nothing.
	 *
	 * @param offset
	 *            offset in the document.
	 * @param insertAbove
	 *            if true inserts row above current row
	 */
	@SuppressWarnings("unchecked")
	public void insertRow(int offset, boolean insertAbove) {
		Element elem = getDefaultRootElement();

		Element row = null;
		// search for the deepest table
		while (!elem.isLeaf()) {
			if (elem.getName().equals("row"))
				row = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		if (row != null) {
			BranchElement table = (BranchElement) row.getParentElement();
			int insertOffset = row.getStartOffset();
			int insertIndex = table.getElementIndex(insertOffset);
			if (!insertAbove) {
				insertIndex++;
				insertOffset = row.getEndOffset();
				if (insertIndex < table.getElementCount()) {
					row = table.getElement(insertIndex);
				}
			}
			int cellCount = row.getElementCount();

			Content c = getContent();
			String ins = "";
			for (int i = 0; i < cellCount; i++) {
				ins += '\n';
			}
			writeLock();
			try {
				UndoableEdit u = c.insertString(insertOffset, ins);
				DefaultDocumentEvent dde = new DefaultDocumentEvent(insertOffset, cellCount, DocumentEvent.EventType.INSERT);
				dde.addEdit(u);
				MutableAttributeSet attr = new SimpleAttributeSet();
				insertUpdate(dde, attr);
				dde.end();
				fireInsertUpdate(dde);
			} catch (Exception ex) {
				System.err.println("Insert row error! " + ex.getMessage());
			}

			DefaultDocumentEvent e = new DefaultDocumentEvent(insertOffset, cellCount, DocumentEvent.EventType.INSERT);
			int[] widths = new int[cellCount];
			int[] offsets = new int[cellCount];
			int[] lengths = new int[cellCount];
			for (int i = 0; i < cellCount; i++) {
				widths[i] = ((RowElement<D, TA>) row).getCellWidth(i);
				offsets[i] = insertOffset + i;
				lengths[i] = 1;
			}
			MutableAttributeSet attr = new SimpleAttributeSet();
			BorderAttributes rowBorders = (BorderAttributes) row.getAttributes().getAttribute("BorderAttributes");
			BorderAttributes ba = new BorderAttributes();
			ba.setBorders(rowBorders.getBorders());
			ba.lineColor = rowBorders.lineColor;
			attr.addAttribute("BorderAttributes", ba);

			Element[] rows = new Element[1];
			rows[0] = new RowElement<>(this, table, attr, cellCount, offsets, lengths, widths, 1);

			Element[] removed = new Element[cellCount];
			if (insertIndex < table.getElementCount()) {
				CellElement<D, TA> cell = (CellElement<D, TA>) row.getElement(0);
				for (int k = 0; k < cellCount; k++) {
					removed[k] = cell.getElement(k);
				}
				cell.replace(0, cellCount, new Element[0]);
				e.addEdit(new ElementEdit(cell, 0, removed, new Element[0]));
			}
			else {
				BranchElement tableParent = (BranchElement) table.getParentElement();
				int replIndex = tableParent.getElementIndex(table.getEndOffset());
				for (int k = 0; k < cellCount; k++) {
					removed[k] = tableParent.getElement(replIndex + k);
				}
				tableParent.replace(replIndex, cellCount, new Element[0]);
				e.addEdit(new ElementEdit(tableParent, replIndex, removed, new Element[0]));
			}
			table.replace(insertIndex, 0, rows);
			e.addEdit(new ElementEdit(table, insertIndex, new Element[0], rows));
			e.end();
			fireInsertUpdate(e);
			writeUnlock();
		}
	}

	/**
	 * Inserts new column into deepest table for specified offset. Method tries to find a deepest table for given offset than defines column
	 * which contains given offset and inserts new column before or after (depends on flag) current.
	 * <p/>
	 * If no table found method does nothing.
	 *
	 * @param offset
	 *            offset in the document.
	 * @param colWidth
	 *            width of new column.
	 * @param insertBefore
	 *            if true inserts column before current column
	 */
	@SuppressWarnings("unchecked")
	public void insertColumn(int offset, int colWidth, boolean insertBefore) {
		Element elem = getDefaultRootElement();

		Element row = null;
		Element table = null;
		while (!elem.isLeaf()) {
			if (elem.getName().equals("table"))
				table = elem;
			if (elem.getName().equals("row"))
				row = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		if (row != null) {
			int colNum = row.getElementIndex(offset);
			if (!insertBefore) {
				colNum++;
			}
			// for each row
			Element[] addedCells = new Element[table.getElementCount()];
			for (int i = 0; i < table.getElementCount(); i++) {
				RowElement<D, TA> editableRow = (RowElement<D, TA>) table.getElement(i);
				int insertOffset;
				if (colNum < editableRow.getElementCount()) {
					insertOffset = editableRow.getElement(colNum).getStartOffset();
				}
				else {
					insertOffset = editableRow.getEndOffset();
				}
				Content c = getContent();
				writeLock();
				try {
					UndoableEdit u = c.insertString(insertOffset, "\n");
					DefaultDocumentEvent dde = new DefaultDocumentEvent(insertOffset, 1, DocumentEvent.EventType.INSERT);
					dde.addEdit(u);
					MutableAttributeSet attr = new SimpleAttributeSet();
					super.insertUpdate(dde, attr);
					dde.end();
					fireInsertUpdate(dde);
				} catch (Exception ex) {
					System.err.println("Insert column error! " + ex.getMessage());
				}

				DefaultDocumentEvent e = new DefaultDocumentEvent(insertOffset, 1, DocumentEvent.EventType.INSERT);

				CellElement<D, TA> cell;
				if (colNum < editableRow.getElementCount()) {
					cell = (CellElement<D, TA>) editableRow.getElement(colNum);
				}
				else {
					cell = (CellElement<D, TA>) editableRow.getElement(editableRow.getElementCount() - 1); // last cell
				}
				BranchElement remove;
				BranchElement paragraph;
				int removeIndex;
				if (colNum < editableRow.getElementCount()) {
					remove = (BranchElement) editableRow.getElement(colNum); // cell
					paragraph = (BranchElement) remove.getElement(0); // first paragraph in the cell
					removeIndex = 0;
				}
				else {
					BranchElement parent;
					parent = (BranchElement) editableRow.getParentElement(); // table
					int rowIndex = parent.getElementIndex(editableRow.getStartOffset());
					rowIndex++;
					if (rowIndex < parent.getElementCount()) {
						remove = (BranchElement) parent.getElement(rowIndex).getElement(0); // first cell of the next row
						paragraph = (BranchElement) remove.getElement(0); // first paragraph in the cell
						removeIndex = 0;
					}
					else {// string "\n" was inserted after table
						remove = (BranchElement) parent.getParentElement(); // table's parent
						removeIndex = remove.getElementIndex(parent.getStartOffset());
						removeIndex++;
						paragraph = (BranchElement) remove.getElement(removeIndex);
					}
				}
				remove.replace(removeIndex, 1, new Element[0]);
				Element[] removed = new Element[1];
				removed[0] = paragraph;
				e.addEdit(new ElementEdit(remove, removeIndex, removed, new Element[0]));

				MutableAttributeSet attr = new SimpleAttributeSet();
				BorderAttributes cellBorders = (BorderAttributes) cell.getAttributes().getAttribute("BorderAttributes");
				BorderAttributes ba = new BorderAttributes();
				ba.setBorders(cellBorders.getBorders());
				ba.lineColor = cellBorders.lineColor;
				attr.addAttribute("BorderAttributes", ba);

				Element[] rows = new Element[1];
				rows[0] = new CellElement<>(this, editableRow, attr, insertOffset, 1, colWidth, 1);
				addedCells[i] = rows[0];
				editableRow.replace(colNum, 0, rows);
				e.addEdit(new ElementEdit(editableRow, colNum, new Element[0], rows));
				e.end();
				fireInsertUpdate(e);
				writeUnlock();
			} // for
		} // if (cell!=null
	}

	/**
	 * Sets margins of the document.
	 *
	 * @param margins
	 *            new document margins.
	 */
	public void setDocumentMargins(Insets margins) {
		this.margins = margins;
		refresh();
	}

	/**
	 * Gets margins of the document.
	 *
	 * @return current document margins.
	 */
	public Insets getDocumentMargins() {
		return margins;
	}

	/**
	 * Removes some content from the document. Removing content causes a write lock to be held while the actual changes are taking place.
	 * Observers are notified of the change on the thread that called this method.
	 * <p/>
	 * This method is thread safe, although most Swing methods are not. Please see
	 * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads and Swing</A> for more information.
	 *
	 * @param offset
	 *            the starting offset >= 0
	 * @param length
	 *            the number of characters to remove >= 0
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void remove(int offset, int length) throws BadLocationException {

		initiateStructuralModification(offset, length);

		// --- checking delete table element ---
		Element startCell = getCell(offset);
		Element endCell = getCell(offset + length);
		String text = getText(offset, length);
		if (startCell != endCell) {
			Element startCellTable = null;
			Element endCellTable = null;
			if (startCell != null) {
				if (startCell.getEndOffset() == offset + length) {
					String s = getText(startCell.getStartOffset(), offset - startCell.getStartOffset());
					if (s.length() == 0) {
						return;
					}
					if ((s.charAt(s.length() - 1) == '\n') && (text.equals("\n"))) {
						deleteLastParagraph((CellElement<D, TA>) startCell);
					}
				}
				startCellTable = startCell.getParentElement().getParentElement();
				if (!((startCellTable.getStartOffset() >= offset) && (startCellTable.getEndOffset() <= offset + length))) {
					return;
				}
			}
			if (endCell != null) {
				endCellTable = endCell.getParentElement().getParentElement();
				if (!((endCellTable.getStartOffset() >= offset) && (endCellTable.getEndOffset() <= offset + length))) {
					return;
				}
			}

			// return;
		}

		Vector<Element> tableList = getInnerTableList(offset, offset + length);
		if (tableList.size() == 0) {
			super.remove(offset, length);
		}
		else {
			// int currentLength = length;
			boolean flag = true;
			for (int i = 0; i < tableList.size(); i++) {
				Element table = tableList.get(i);
				if ((offset > table.getStartOffset()) && (offset + length < table.getEndOffset())) {
					flag = false;
					break;
				}
			} // for
			if (flag) {
				super.remove(offset, length);
			}
		}
	}

	/**
	 * Gets the deepest cell element from the document tree.
	 *
	 * @param offset
	 *            The offset in the document.
	 */
	public Element getCell(int offset) {
		Element cell = null;
		Element elem = getDefaultRootElement();

		while (!elem.isLeaf()) {
			if (elem.getName().equals("cell"))
				cell = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return cell;
	}

	/**
	 * Gets the deepest row element from the document tree.
	 *
	 * @param offset
	 *            The offset in the document.
	 */
	public Element getRow(int offset) {
		Element row = null;
		Element elem = getDefaultRootElement();

		while (!elem.isLeaf()) {
			if (elem.getName().equals("row"))
				row = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return row;
	}

	/**
	 * Gets the deepest paragraph element from the document tree.
	 *
	 * @param offset
	 *            The offset in the document.
	 */
	public Element getParagraph(int offset) {
		Element paragraph = null;
		Element elem = getDefaultRootElement();

		while (!elem.isLeaf()) {
			if (elem.getName().equals("paragraph"))
				paragraph = elem;
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return paragraph;
	}

	/**
	 * Gets the list of tables which placed in the definite interval.
	 *
	 * @param startOffset
	 *            The start interval offset.
	 * @param endOffset
	 *            The end interval offset.
	 */
	public Vector<Element> getInnerTableList(int startOffset, int endOffset) {
		Vector<Element> result = new Vector<>();
		Element root = getDefaultRootElement();
		for (int i = 0; i < root.getElementCount(); i++) {
			Element elem = root.getElement(i);
			if ((elem.getName().equals("table")) && (startOffset <= elem.getEndOffset()) && (endOffset >= elem.getEndOffset())) {
				// intersect
				result.add(elem);
			}
		}
		return result;
	}

	/**
	 * Removes last paragraph from cell.
	 *
	 * @param cell
	 */
	public void deleteLastParagraph(CellElement<D, TA> cell) {
		int cnt = cell.getElementCount();
		if (cnt <= 1) {
			return;
		}
		Element par = cell.getElement(cnt - 1);
		int start = par.getStartOffset();
		int end = par.getEndOffset();
		DefaultDocumentEvent de = new DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
		ElementEdit ee = new ElementEdit(cell, cnt - 1, new Element[] { par }, new Element[0]);
		cell.replace(cnt - 1, 1, new Element[0]);
		try {
			this.getContent().remove(start, end - start);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		de.addEdit(ee);
		de.end();
		this.fireRemoveUpdate(de);
	}

	/**
	 * Gets the deepest table element from the document tree.
	 *
	 * @param offset
	 *            The offset in the document.
	 */
	public Element getTable(int offset) {
		Element table = null;
		Element elem = getDefaultRootElement();
		while (!elem.isLeaf()) {
			if (elem.getName().equals("table")) {
				table = elem;
			}
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return table;
	}

	/**
	 * Sets attributes for a paragraphs. This method is thread safe, although most Swing methods are not.
	 *
	 * @param offset
	 *            the offset into the paragraph >= 0
	 * @param length
	 *            the number of characters affected >= 0
	 * @param s
	 *            the attributes
	 * @param replace
	 *            whether to replace existing attributes, or merge them
	 */
	@Override
	public void setParagraphAttributes(int offset, int length, AttributeSet attrs, boolean replace) {
		try {
			writeLock();
			DefaultDocumentEvent changes = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);

			AttributeSet sCopy = attrs.copyAttributes();

			int pos = offset;
			Element paragraph = getParagraph(pos);
			MutableAttributeSet attr = (MutableAttributeSet) paragraph.getAttributes();
			changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy, replace));
			if (replace) {
				attr.removeAttributes(attr);
			}
			attr.addAttributes(attrs);
			while (pos < offset + length) {
				attr = (MutableAttributeSet) paragraph.getAttributes();
				changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy, replace));
				if (replace) {
					attr.removeAttributes(attr);
				}
				attr.addAttributes(attrs);
				if (pos == getLength())
					break;
				pos = paragraph.getEndOffset();
				paragraph = getParagraph(pos);
			}

			changes.end();
			fireChangedUpdate(changes);
			fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
		} finally {
			writeUnlock();
		}
	}

	/**
	 * Performs refresh of the document.
	 */
	public void refresh() {
		DefaultDocumentEvent e = new DefaultDocumentEvent(0, getLength() - 1, DocumentEvent.EventType.CHANGE);
		e.end();
		fireChangedUpdate(e);
	}

	/**
	 * Creates the root element to be used to represent the default document structure.
	 *
	 * @return the element base
	 */
	@Override
	protected AbstractElement createDefaultRoot() {
		// grabs a write-lock for this initialization and
		// abandon it during initialization so in normal
		// operation we can detect an illegitimate attempt
		// to mutate attributes.
		writeLock();
		DocumentElement<D, TA> rootElement = new DocumentElement<>(this);
		ParagraphElement<D, TA> paragraph = new ParagraphElement<>(this, rootElement, null);

		RunElement<D, TA> brk = new RunElement<>(this, paragraph, null, 0, 1, null);
		Element[] buff = new Element[1];
		buff[0] = brk;
		paragraph.replace(0, 0, buff);

		buff[0] = paragraph;
		rootElement.replace(0, 0, buff);
		writeUnlock();
		return rootElement;
	}

	@SuppressWarnings("unchecked")
	public DocumentRootElement getRootElement() {
		return (DocumentRootElement) getRootElements()[0];
	}

	private StructuralModification<D, TA> currentModification = null;

	/*@Override
	protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
		if (!isReadingDocument) {
			currentModification = new StructuralModification(chng);
			System.out.println("START UPDATE with " + chng + " attr=" + attr);
			System.out.println("offset=" + chng.getOffset() + " length=" + chng.getLength());
			System.out.println("start char element = " + getCharacterElement(chng.getOffset()));
			System.out.println("start par element = " + getParagraphElement(chng.getOffset()));
		}
		super.insertUpdate(chng, attr);
		if (!isReadingDocument) {
			System.out.println("END UPDATE with " + chng + " attr=" + attr);
		}
	}*/

	@Override
	public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
		initiateStructuralModification(offset, length);
		super.setCharacterAttributes(offset, length, s, replace);
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		initiateStructuralModification(offs, 0);
		super.insertString(offs, str, a);
	}

	private void initiateStructuralModification(int offset, int length) {
		if (!isReadingDocument && currentModification == null) {
			currentModification = new StructuralModification<>(this, offset, length);
		}
	}

	/*@Override
	protected void fireInsertUpdate(DocumentEvent e) {
		if (!isReadingDocument) {
			System.out.println("Hop, on fait un insert de offset=" + e.getOffset() + " length=" + e.getLength());
		}
		super.fireInsertUpdate(e);
		if (!isReadingDocument) {
			System.out.println("Done, on a fait un insert de offset=" + e.getOffset() + " length=" + e.getLength());
		}
		// documentChanged();
	}*/

	/*private void documentChanged() {
		getPropertyChangeSupport().firePropertyChange("documentChanged", false, true);
	}*/

	@SuppressWarnings("unchecked")
	@Override
	protected Element createBranchElement(Element parent, AttributeSet a) {
		/*if (!isReadingDocument) {
			System.out.println("Creating BranchElement for parent=" + parent);
			Enumeration<?> en = a.getAttributeNames();
			while (en.hasMoreElements()) {
				Object next = en.nextElement();
				System.out.println(next + "=" + a.getAttribute(next));
			}
		}*/
		if (parent instanceof DocumentElement) {
			return new ParagraphElement<>(this, (DocumentElement<D, TA>) parent, a);

		}
		return super.createBranchElement(parent, a);
	}

	@Override
	protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
		if (!isReadingDocument) {
			System.out.println("On cree un LeafElement pour parent=" + parent);
			// Thread.dumpStack();
			Enumeration<?> en = a.getAttributeNames();
			while (en.hasMoreElements()) {
				Object next = en.nextElement();
				System.out.println(next + "=" + a.getAttribute(next));
			}
		}
		if (parent instanceof ParagraphElement) {
			return new RunElement<>(this, parent, a, p0, p1, null);

		}
		return super.createLeafElement(parent, a, p0, p1);
	}

	protected void setIsReadingDocument(boolean isReadingDocument) {
		this.isReadingDocument = isReadingDocument;
	}

	public void fireWriteLock() {
		writeLock();
	}

	public void fireWriteUnlock() {
		writeUnlock();
	}

	@Override
	public void fireChangedUpdate(DocumentEvent e) {
		super.fireChangedUpdate(e);
	}

	// --- INNER CLASSES-------------------------------------------------------------

	class StructuredContentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateDocumentStructure(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateDocumentStructure(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateDocumentStructure(e);
		}

		private void updateDocumentStructure(DocumentEvent e) {
			/*System.out.println("Je detecte une modification structurelle pour offset=" + e.getOffset() + " length=" + e.getLength());
			Element pElStart = getParagraphElement(e.getOffset());
			Element cElStart = getCharacterElement(e.getOffset());
			Element pElEnd = getParagraphElement(e.getOffset() + e.getLength());
			Element cElEnd = getCharacterElement(e.getOffset() + e.getLength());
			System.out.println("From: pElStart=" + pElStart + " cElStart=" + cElStart);
			System.out.println("To: pElEnd=" + pElEnd + " cElEnd=" + cElEnd);*/
			if (currentModification != null) {
				currentModification.fireDocumentChanged(e);
				currentModification = null;
			}
		}

	}

	public abstract class DocumentRootElement<E extends FlexoDocObject<D, TA>> extends SectionElement
			implements AbstractDocumentElement<E, D, TA> {

		/**
		 * Creates a new SectionElement.
		 */
		public DocumentRootElement() {
			super(/*null, null*/);
		}

		/**
		 * Gets the name of the element.
		 *
		 * @return the name
		 */
		/*@Override
		public String getName() {
			return SectionElementName;
		}*/
	}

}
