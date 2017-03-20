package org.openflexo.components.doc.editorkit;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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

import org.openflexo.components.doc.editorkit.FlexoStyledDocument.StructuralModification.FragmentStructure.RetainedParagraphElement;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument.StructuralModification.FragmentStructure.RetainedParagraphElement.RetainedRunElement;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocElementContainer;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

@SuppressWarnings("serial")
public class FlexoStyledDocument<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends DefaultStyledDocument {

	static final Logger logger = Logger.getLogger(FlexoStyledDocument.class.getPackage().getName());

	public int DOCUMENT_WIDTH = -1;
	/**
	 * Represents document margins
	 */
	private Insets margins = new Insets(0, 0, 0, 0);

	private FlexoDocument<D, TA> flexoDocument;

	private boolean isReadingDocument = false;

	/**
	 * Constructs a styled document.
	 *
	 * @param c
	 *            The container for the content.
	 * @param styles
	 *            Resources and style definitions which may be shared across documents.
	 */
	public FlexoStyledDocument(FlexoDocument<D, TA> flexoDocument, Content c, StyleContext styles) {
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
	public FlexoStyledDocument(FlexoDocument<D, TA> flexoDocument, StyleContext styles) {
		this(flexoDocument, new GapContent(BUFFER_SIZE_DEFAULT), styles);
	}

	/**
	 * Constructs a default styled document. This buffers input content by a size of BUFFER_SIZE_DEFAULT and has a style context that is
	 * scoped by the lifetime of the document and is not shared with other documents.
	 */
	public FlexoStyledDocument(FlexoDocument<D, TA> flexoDocument) {
		this(flexoDocument, new GapContent(BUFFER_SIZE_DEFAULT), new StyleContext());
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
			table = new TableElement(rowOffsets, rowLenghts, root, attr, rowCount, colCount, colWidths, rowHeights);
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
				widths[i] = ((FlexoStyledDocument<D, TA>.RowElement) row).getCellWidth(i);
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
			rows[0] = new RowElement(table, attr, cellCount, offsets, lengths, widths, 1);

			Element[] removed = new Element[cellCount];
			if (insertIndex < table.getElementCount()) {
				CellElement cell = (CellElement) row.getElement(0);
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
				RowElement editableRow = (RowElement) table.getElement(i);
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

				CellElement cell;
				if (colNum < editableRow.getElementCount()) {
					cell = (CellElement) editableRow.getElement(colNum);
				}
				else {
					cell = (CellElement) editableRow.getElement(editableRow.getElementCount() - 1); // last cell
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
				rows[0] = new CellElement(editableRow, attr, insertOffset, 1, colWidth, 1);
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
						deleteLastParagraph((CellElement) startCell);
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

		Vector tableList = getInnerTableList(offset, offset + length);
		if (tableList.size() == 0) {
			super.remove(offset, length);
		}
		else {
			int currentLength = length;
			boolean flag = true;
			for (int i = 0; i < tableList.size(); i++) {
				Element table = (Element) tableList.get(i);
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
	public Vector getInnerTableList(int startOffset, int endOffset) {
		Vector result = new Vector();
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
	public void deleteLastParagraph(CellElement cell) {
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
		DocumentElement rootElement = new DocumentElement();
		ParagraphElement paragraph = new ParagraphElement(rootElement, null);

		RunElement brk = new RunElement(paragraph, null, 0, 1, null);
		Element[] buff = new Element[1];
		buff[0] = brk;
		paragraph.replace(0, 0, buff);

		buff[0] = paragraph;
		rootElement.replace(0, 0, buff);
		writeUnlock();
		return rootElement;
	}

	public DocumentElement getRootElement() {
		return (DocumentElement) getRootElements()[0];
	}

	private StructuralModification currentModification = null;

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
			currentModification = new StructuralModification(offset, length);
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

	@Override
	protected Element createBranchElement(Element parent, AttributeSet a) {
		if (!isReadingDocument) {
			System.out.println("On cree un BranchElement pour parent=" + parent);
			Enumeration<?> en = a.getAttributeNames();
			while (en.hasMoreElements()) {
				Object next = en.nextElement();
				System.out.println(next + "=" + a.getAttribute(next));
			}
		}
		if (parent instanceof FlexoStyledDocument.DocumentElement) {
			// Thread.dumpStack();
			return new ParagraphElement((DocumentElement) parent, a);

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
		if (parent instanceof FlexoStyledDocument.ParagraphElement) {
			return new RunElement(parent, a, p0, p1, null);

		}
		return super.createLeafElement(parent, a, p0, p1);
	}

	protected void setIsReadingDocument(boolean isReadingDocument) {
		this.isReadingDocument = isReadingDocument;
	}

	// --- INNER CLASSES-------------------------------------------------------------

	public interface FlexoDocumentElement<E extends FlexoDocObject<?, ?>> {

		/**
		 * Return {@link FlexoDocObject} this element represents
		 * 
		 * @return
		 */
		public E getDocObject();

		/**
		 * Recursive call to doc object looking up
		 * 
		 * @return
		 */
		public E lookupDocObject();

		// public <O extends FlexoDocObject<D,TA>> FlexoDocumentElement<O> getElement(O docObject);
	}

	public class DocumentElement extends SectionElement implements FlexoDocumentElement<FlexoDocument<D, TA>> {

		@Override
		public FlexoDocument<D, TA> getDocObject() {
			return flexoDocument;
		}

		@Override
		public FlexoDocument<D, TA> lookupDocObject() {
			for (int i = 0; i < getElementCount(); i++) {
				Element e = getElement(i);
				if (e instanceof FlexoDocumentElement) {
					((FlexoDocumentElement) e).lookupDocObject();
				}
			}

			return flexoDocument;
		}

		@Override
		public String toString() {
			return "DocumentElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
		}
	}

	public class ParagraphElement extends BranchElement implements FlexoDocumentElement<FlexoDocParagraph<D, TA>> {

		private FlexoDocParagraph<D, TA> paragraph = null;

		public ParagraphElement(DocumentElement documentElement, AttributeSet a) {
			super(documentElement, a);
		}

		@Override
		public DocumentElement getParent() {
			return (DocumentElement) super.getParent();
		}

		public FlexoDocParagraph<D, TA> getParagraph() {
			return getDocObject();
		}

		@Override
		public FlexoDocParagraph<D, TA> getDocObject() {
			/*if (paragraph == null && currentModification != null) {
				int index = getParent().getIndex(this);
				int paragraphIndex = 0;
				if (flexoDocument != null) {
					for (FlexoDocElement<D,TA> e : flexoDocument.getElements()) {
						if (e instanceof FlexoDocParagraph) {
							if (paragraphIndex == index) {
								paragraph = (FlexoDocParagraph<D,TA>) e;
								break;
							}
							paragraphIndex++;
						}
					}
				}
			}*/
			return paragraph;
		}

		@Override
		public FlexoDocParagraph<D, TA> lookupDocObject() {
			int index = getParent().getIndex(this);
			int paragraphIndex = 0;
			if (flexoDocument != null) {
				for (FlexoDocElement<D, TA> e : flexoDocument.getElements()) {
					if (e instanceof FlexoDocParagraph) {
						if (paragraphIndex == index) {
							paragraph = (FlexoDocParagraph<D, TA>) e;
							break;
						}
						paragraphIndex++;
					}
				}
			}
			for (int i = 0; i < getElementCount(); i++) {
				Element e = getElement(i);
				if (e instanceof FlexoDocumentElement) {
					((FlexoDocumentElement) e).lookupDocObject();
				}
			}
			return paragraph;
		}

		/*@Override
		public void replace(int offset, int length, Element[] elems) {
			StringBuffer sb = new StringBuffer();
			for (Element el : elems) {
				sb.append(el.toString() + " ");
			}
			System.out.println("On remplace dans " + this + " avec " + sb.toString());
			super.replace(offset, length, elems);
			paragraph = null;
		}*/

		@Override
		public int getStartOffset() {
			if (getElementCount() > 0) {
				return super.getStartOffset();
			}
			return -1;
		}

		@Override
		public int getEndOffset() {
			if (getElementCount() > 0) {
				return super.getEndOffset();
			}
			return -1;
		}

		@Override
		public String toString() {
			return "ParagraphElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":"
					+ (getParagraph() != null ? getParagraph().getRawTextPreview() : "null");
		}
	}

	public class RunElement extends LeafElement implements FlexoDocumentElement<FlexoTextRun<D, TA>> {

		private FlexoTextRun<D, TA> run;

		public RunElement(Element parent, AttributeSet a, int offs0, int offs1, FlexoTextRun<D, TA> run) {
			super(parent, a, offs0, offs1);
			this.run = run;
		}

		public FlexoTextRun<D, TA> getRun() {
			return getDocObject();
		}

		@Override
		public FlexoTextRun<D, TA> getDocObject() {
			/*if (run == null && getParentElement() instanceof ParagraphElement && currentModification != null) {
				int index = getParent().getIndex(this);
				int runIndex = 0;
				if (flexoDocument != null) {
					for (FlexoDocRun<D,TA> r : ((ParagraphElement) getParentElement()).getParagraph().getRuns()) {
						if (r instanceof FlexoTextRun) {
							if (runIndex == index) {
								run = (FlexoTextRun<D,TA>) r;
								break;
							}
							runIndex++;
						}
					}
				}
			}*/
			return run;
		}

		@Override
		public FlexoTextRun<D, TA> lookupDocObject() {
			int index = getParent().getIndex(this);
			int runIndex = 0;
			if (flexoDocument != null && ((ParagraphElement) getParentElement()).getParagraph() != null) {
				for (FlexoDocRun<D, TA> r : ((ParagraphElement) getParentElement()).getParagraph().getRuns()) {
					if (r instanceof FlexoTextRun) {
						if (runIndex == index) {
							run = (FlexoTextRun<D, TA>) r;
							break;
						}
						runIndex++;
					}
				}
			}
			return run;
		}

		@Override
		public String toString() {
			String text = "???";
			try {
				text = getText(getStartOffset(), getEndOffset() - getStartOffset());
				if (text.length() > 20) {
					text = text.substring(0, 20);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return "RunElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":"
					+ /*run.getText()*/text;
		}

	}

	/**
	 * Represents table element.
	 */
	public class TableElement extends BranchElement {
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
		 */
		public TableElement(int[] rowOffsets, int[] rowLengths, Element parent, AttributeSet attr, int rowCount, int colCount, int[] widths,
				int[] heights) {
			super(parent, attr);
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
				rows[i] = new RowElement(this, rowAttr, colCount, cellOffsets, cellLengths, widths, heights[i]);
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
		public int getWidth() {
			RowElement row = (RowElement) getElement(0);
			return row.getWidth();
		}

		/**
		 * Gets the table height (sum of row heights).
		 */
		public int getHeight() {
			int cnt = getElementCount();
			int height = 1;
			for (int i = 0; i < cnt; i++) {
				RowElement row = (RowElement) getElement(i);
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
		public void setBorders(BorderAttributes ba) {
			writeLock();
			this.addAttribute("BorderAttributes", ba);
			for (int i = 0; i < getElementCount(); i++) {
				RowElement row = (RowElement) getElement(i);
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
			writeUnlock();
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
		public void setMargins(Insets margins) {
			writeLock();
			int cnt = getElementCount();
			for (int i = 0; i < cnt; i++) {
				RowElement row = (RowElement) getElement(i);
				int cnt2 = row.getElementCount();
				for (int j = 0; j < cnt2; j++) {
					CellElement cell = (CellElement) row.getElement(j);
					cell.setMargins(margins);
				}
			}
			writeUnlock();
		}

		/**
		 * Sets table alignment.
		 *
		 * @param ba
		 *            The new margins.
		 */
		public void setAlignment(int align) {
			writeLock();
			StyleConstants.setAlignment((MutableAttributeSet) this.getAttributes(), align);
			writeUnlock();
		}

		@Override
		public String toString() {
			return "TableElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
		}
	}
	// ----- end TABLE --------------------------------------------------------------
	// --- ROW ----------------------------------------------------------------------

	/**
	 * Represents table's row element.
	 */
	public class RowElement extends BranchElement {

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
		 */
		public RowElement(Element parent, AttributeSet attr, int cellCount, int[] cellOffsets, int[] cellLengths, int[] widths,
				int height) {
			super(parent, attr);

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
				cells[i] = new CellElement(this, cellAttr, cellOffsets[i], cellLengths[i], widths[i], height);
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
		public int getWidth() {
			int width = 0;
			for (int i = 0; i < getElementCount(); i++) {
				CellElement cell = (CellElement) getElement(i);
				width += cell.getWidth();
			}
			return width;
		}

		/**
		 * Gets row height (in pixels)
		 */
		public int getHeight() {
			int height = 0;
			for (int i = 0; i < getElementCount(); i++) {
				CellElement cell = (CellElement) getElement(i);
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
		public int getCellWidth(int index) {
			CellElement cell = (CellElement) getElement(index);
			return cell.getWidth();
		}

		/**
		 * Sets row borders attributes.
		 *
		 * @param ba
		 *            The border attributes.
		 */
		public void setBorders(BorderAttributes ba) {
			BorderAttributes currentBorders = (BorderAttributes) getAttribute("BorderAttributes");
			currentBorders.setBorders(ba.getBorders());
			currentBorders.lineColor = ba.lineColor;

			for (int i = 0; i < getElementCount(); i++) {
				CellElement cell = (CellElement) getElement(i);
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
		public void setMargins(Insets margins) {
			writeLock();
			int cnt = getElementCount();
			for (int i = 0; i < cnt; i++) {
				CellElement cell = (CellElement) getElement(i);
				cell.setMargins(margins);
			}
			writeUnlock();
		}

		/**
		 * Sets row height (height for each cell).
		 *
		 * @param height
		 *            height value.
		 */
		public void setHeight(int height) {
			writeLock();
			int cnt = getElementCount();
			for (int i = 0; i < cnt; i++) {
				CellElement cell = (CellElement) getElement(i);
				cell.height = height;
			}
			writeUnlock();
		}
	}
	// --- CELL ---------------------------------------------------------------------

	/**
	 * Represents table's cell element.
	 */
	public class CellElement extends BranchElement {
		/**
		 * Cell width (in pixels).
		 */
		private int width = 1;

		/**
		 * Cell height (in pixels).
		 */
		private int height = 1;

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
		 */
		public CellElement(Element parent, AttributeSet attr, int startOffset, int length, int width, int height) {
			super(parent, attr);
			this.width = width;
			this.height = height;
			BranchElement paragraph = new BranchElement(this, null);

			LeafElement brk = new LeafElement(paragraph, null, startOffset, startOffset + length);
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
		 */
		public CellElement(Element parent, AttributeSet attr, int[] paragraphOffsets, int[] paragraphLenghts, int width) {
			super(parent, attr);
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
			DefaultDocumentEvent dde = new DefaultDocumentEvent(Math.max(getStartOffset() - 1, 0), getEndOffset(),
					DocumentEvent.EventType.CHANGE);
			dde.end();
			fireChangedUpdate(dde);
		}

		public BorderAttributes getBorders() {
			return (BorderAttributes) this.getAttribute("BorderAttributes");
		}
	}
	// --- end CELL -----------------------------------------------------------------

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

	class StructuralModification {

		FragmentStructure previous;
		FragmentStructure now;

		public StructuralModification(int startOffet, int length) {
			// System.out.println("On va changer la structure du document");
			previous = new FragmentStructure(startOffet, length);
			// System.out.println("WAS:");
			// System.out.println(previous.toString());
			// Thread.dumpStack();
		}

		void fireDocumentChanged(DocumentEvent e) {
			now = new FragmentStructure(e.getOffset(), e.getLength());

			System.out.println("OK, on a change des trucs");
			System.out.println("WAS:");
			System.out.println(previous.toString());
			System.out.println("NOW:");
			System.out.println(now.toString());
			// Thread.dumpStack();

			RetainedParagraphElement currentP = null;

			if (previous.paragraphElements.size() <= now.paragraphElements.size()) {
				for (int i = 0; i < previous.paragraphElements.size(); i++) {
					RetainedParagraphElement oldP = previous.paragraphElements.get(i);
					RetainedParagraphElement newP = now.paragraphElements.get(i);
					fireUpdateParagraph(oldP, newP);
					currentP = newP;
				}
				// Now handle new paragraphs
				for (int i = previous.paragraphElements.size(); i < now.paragraphElements.size(); i++) {
					RetainedParagraphElement newP = now.paragraphElements.get(i);
					fireNewParagraph(newP, currentP);
					currentP = newP;
				}
			}
			else {
				// Some paragraphs have been removed
				for (int i = now.paragraphElements.size(); i < previous.paragraphElements.size(); i++) {
					RetainedParagraphElement oldPToRemove = previous.paragraphElements.get(i);
					if (oldPToRemove.paragraph != null) {
						oldPToRemove.paragraph.getContainer().removeFromElements(oldPToRemove.paragraph);
					}
				}
				for (int i = 0; i < now.paragraphElements.size(); i++) {
					RetainedParagraphElement oldP = previous.paragraphElements.get(i);
					RetainedParagraphElement newP = now.paragraphElements.get(i);
					fireUpdateParagraph(oldP, newP);
					currentP = newP;
				}
			}

		}

		private void fireUpdateParagraph(FlexoStyledDocument<D, TA>.StructuralModification.FragmentStructure.RetainedParagraphElement oldP,
				FlexoStyledDocument<D, TA>.StructuralModification.FragmentStructure.RetainedParagraphElement newP) {

			if (newP.paragraph == null) {
				newP.paragraph = newP.parElement.lookupDocObject();
				if (newP.paragraph != null) {
					System.out.println("Tiens j'ai quand meme trouve le paragraph: " + newP.paragraph);
				}
			}

			RetainedRunElement currentR = null;

			if (oldP.runElements.size() <= newP.runElements.size()) {
				for (int i = 0; i < oldP.runElements.size(); i++) {
					RetainedRunElement oldR = oldP.runElements.get(i);
					RetainedRunElement newR = newP.runElements.get(i);
					fireUpdateRun(oldR, newR);
				}
				// Now handle new runs
				for (int i = oldP.runElements.size(); i < newP.runElements.size(); i++) {
					RetainedRunElement newR = newP.runElements.get(i);
					fireNewRun(newP.paragraph, newR, newR.getText(), currentR);
					currentR = newR;
				}

			}
		}

		private void fireNewParagraph(FlexoStyledDocument<D, TA>.StructuralModification.FragmentStructure.RetainedParagraphElement newP,
				FlexoStyledDocument<D, TA>.StructuralModification.FragmentStructure.RetainedParagraphElement previousP) {
			System.out.println("Creating new paragraph just after " + previousP.paragraph);

			FlexoDocElementContainer<D, TA> container = previousP.paragraph.getContainer();
			int index = previousP.paragraph.getIndex();
			FlexoDocParagraph<D, TA> newParagraph = flexoDocument.getFactory().makeParagraph();
			container.insertElementAtIndex(newParagraph, index + 1);
			newP.parElement.paragraph = newParagraph;
			newP.paragraph = newParagraph;

			for (RetainedRunElement rre : newP.runElements) {
				String newText = rre.getText();
				if (newText.endsWith("\n")) {
					newText = newText.substring(0, newText.length() - 1);
				}
				System.out.println("Creating new run with [" + newText + "]");
				FlexoTextRun newRun = flexoDocument.getFactory().makeTextRun(newText);
				newParagraph.addToRuns(newRun);
			}

		}

		private void fireUpdateRun(RetainedRunElement oldR, RetainedRunElement newR) {
			if (oldR.run == null) {
				logger.warning("Unexpected null run");
				return;
			}
			else {
				if (newR.run == null) {
					newR.run = newR.runElement.lookupDocObject();
					if (newR.run != null) {
						System.out.println("Tiens j'ai quand meme trouve le run: " + newR.run);
					}
				}
				if (newR.run == null) {
					System.out.println("Tiens faut creer un nouveau run pour " + oldR.run.getParagraph());
				}
				else if (newR.run == oldR.run) {
					String newText = newR.getText();
					if (newText.endsWith("\n")) {
						newText = newText.substring(0, newText.length() - 1);
					}
					System.out.println("Mise a jour du run " + newR.run + " avec " + newText);
					newR.run.setText(newText);
				}
			}
		}

		private void fireNewRun(FlexoDocParagraph<D, TA> container, RetainedRunElement newR, String text, RetainedRunElement previousR) {
			System.out.println("Creating new run just after " + previousR);

			int index = -1;
			if (previousR != null && previousR.run != null) {
				index = previousR.run.getIndex();
			}
			FlexoTextRun newRun = container.getFlexoDocument().getFactory().makeTextRun(text);
			if (index == -1) {
				container.addToRuns(newRun);
			}
			else {
				container.insertRunAtIndex(newRun, index + 1);
			}
			newR.runElement.run = newRun;
			newR.run = newRun;
		}

		class FragmentStructure {

			List<RetainedParagraphElement> paragraphElements;

			public FragmentStructure(int offset, int length) {
				ParagraphElement pElStart = (ParagraphElement) getParagraphElement(offset);
				ParagraphElement pElEnd = (ParagraphElement) getParagraphElement(offset + length);
				DocumentElement docElement = pElStart.getParent();
				int startId = -1;
				int endId = -1;
				for (int i = 0; i < docElement.getElementCount(); i++) {
					Element e = docElement.getElement(i);
					if (e == pElStart) {
						startId = i;
					}
					if (e == pElEnd) {
						endId = i;
					}
				}
				paragraphElements = new ArrayList<>();
				for (int i = startId; i <= endId; i++) {
					Element e = docElement.getElement(i);
					if (e instanceof FlexoStyledDocument.ParagraphElement) {
						paragraphElements.add(new RetainedParagraphElement((ParagraphElement) e));
					}
				}
			}

			@Override
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("Document structure:\n");
				for (RetainedParagraphElement rpe : paragraphElements) {
					sb.append(rpe);
				}
				return sb.toString();
			}

			class RetainedParagraphElement {
				ParagraphElement parElement;
				FlexoDocParagraph<D, TA> paragraph;
				List<RetainedRunElement> runElements;
				int startIndex;
				int endIndex;

				public RetainedParagraphElement(ParagraphElement pEl) {
					this.parElement = pEl;
					startIndex = pEl.getStartOffset();
					endIndex = pEl.getEndOffset();
					paragraph = pEl.getParagraph();
					runElements = new ArrayList<>();
					for (int i = 0; i < pEl.getElementCount(); i++) {
						Element child = pEl.getElement(i);
						if (child instanceof FlexoStyledDocument.RunElement) {
							RetainedRunElement rEl = new RetainedRunElement((RunElement) child);
							runElements.add(rEl);
						}
					}
				}

				@Override
				public String toString() {
					StringBuffer sb = new StringBuffer();
					sb.append("  > " + "[ParagraphElement:" + Integer.toHexString(parElement.hashCode()) + "] start=" + startIndex + " end="
							+ endIndex + " [" + (paragraph != null ? Integer.toHexString(paragraph.hashCode()) : "null") + "]\n");
					for (RetainedRunElement rre : runElements) {
						sb.append(rre + "\n");
					}
					return sb.toString();
				}

				class RetainedRunElement {
					RunElement runElement;
					FlexoTextRun<D, TA> run;
					int startIndex;
					int endIndex;

					public RetainedRunElement(RunElement rEl) {
						this.runElement = rEl;
						startIndex = rEl.getStartOffset();
						endIndex = rEl.getEndOffset();
						run = rEl.getRun();
					}

					public String getText() {
						try {
							return FlexoStyledDocument.this.getText(startIndex, endIndex - startIndex);
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public String toString() {
						return "    > " + "[RunElement:" + Integer.toHexString(runElement.hashCode()) + "] start=" + startIndex + " end="
								+ endIndex + " [" + (run != null ? Integer.toHexString(run.hashCode()) : "null") + "]";
					}
				}
			}
		}
	}

}
