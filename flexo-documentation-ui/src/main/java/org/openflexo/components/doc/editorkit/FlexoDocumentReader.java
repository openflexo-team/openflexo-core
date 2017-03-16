package org.openflexo.components.doc.editorkit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoParagraphStyle;
import org.openflexo.foundation.doc.FlexoParagraphStyle.ParagraphSpacing.LineSpacingRule;
import org.openflexo.foundation.doc.FlexoParagraphStyle.ParagraphTab;
import org.openflexo.foundation.doc.FlexoRunStyle;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Implements reader of document.
 *
 * @author Stanislav Lapitsky
 */
public class FlexoDocumentReader<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> {
	/**
	 * document instance to the building.
	 */
	protected FlexoStyledDocument document;
	private FlexoDocument<D, TA> flexoDocument;

	/**
	 * Current offset in the document for insert action.
	 */
	private int currentOffset = 0;

	SimpleAttributeSet parAttrs;
	SimpleAttributeSet charAttrs;

	/**
	 * Builds new instance of reader.
	 *
	 * @param doc
	 *            document for reading to.
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws BadLocationException
	 * @throws IOException
	 */
	public FlexoDocumentReader(FlexoDocument<D, TA> flexoDocument) throws BadLocationException {
		this.flexoDocument = flexoDocument;
		document = new FlexoStyledDocument(flexoDocument);
		read(flexoDocument, 0);
	}

	public FlexoStyledDocument getDocument() {
		return document;
	}

	/**
	 * Reads content of specified stream to the document.
	 *
	 * @param in
	 *            stream.
	 * @throws BadLocationException
	 */
	private void read(FlexoDocument<D, TA> flexoDocument, int offset) throws BadLocationException {
		System.out.println("Starting reading " + flexoDocument);

		iteratePart(flexoDocument.getElements());

		this.currentOffset = offset;

	}

	public void iteratePart(FlexoDocObject<D, TA>... content) throws BadLocationException {
		iteratePart(Arrays.asList(content));
	}

	public void iteratePart(List<? extends FlexoDocObject<D, TA>> content) throws BadLocationException {
		for (Object obj : content) {
			// System.out.println(" * handling " + obj + " of " + obj.getClass());
			if (obj instanceof FlexoDocParagraph) {
				processParagraph((FlexoDocParagraph<D, TA>) obj);
				if (obj != content.get(content.size() - 1)) {
					document.insertString(currentOffset, "\n", charAttrs);
					document.setParagraphAttributes(currentOffset, 1, parAttrs, true);
					currentOffset++;
				}
				else {
					document.setParagraphAttributes(currentOffset, 1, parAttrs, true);
				}
			}
			else if (obj instanceof FlexoTextRun) {
				processTextRun((FlexoTextRun<D, TA>) obj);
			}
			else if (obj instanceof FlexoDrawingRun) {
				processDrawingRun((FlexoDrawingRun<D, TA>) obj);
			}
			else if (obj instanceof FlexoDocTable) {
				processTable((FlexoDocTable<D, TA>) obj);
			}
			/*else if (obj instanceof Drawing) {
				processDrawing((Drawing) obj);
			}
			else if (obj instanceof JAXBElement) {
				JAXBElement el = (JAXBElement) obj;
				if (el.getDeclaredType().equals(Text.class)) {
					String text = ((Text) el.getValue()).getValue();
					System.out.println("Et hop, du texte: " + text);
					document.insertString(currentOffset, text, charAttrs);
					currentOffset += text.length();
				}
				else if (el.getDeclaredType().equals(Tbl.class)) {
					Tbl tbl = (Tbl) el.getValue();
					processTable(tbl);
				}
				else if (el.getDeclaredType().equals(Drawing.class)) {
					Drawing d = (Drawing) el.getValue();
					processDrawing(d);
				}
			}
			else {
				System.out.println(obj);
			}*/
		}
	}

	protected void processParagraph(FlexoDocParagraph<D, TA> p) throws BadLocationException {
		System.out.println("---------> un paragraphe: " + p + " avec " + p.getParagraphStyle());

		parAttrs = new SimpleAttributeSet();

		if (p.getNamedStyle() != null) {
			applyParagraphStyle(p.getNamedStyle().getParagraphStyle());
			// applyRunStyle(p.getNamedStyle().getRunStyle());
		}
		if (p.getParagraphStyle() != null) {
			applyParagraphStyle(p.getParagraphStyle());
		}
		iteratePart(p.getRuns());
	}

	protected void applyParagraphStyle(FlexoParagraphStyle<D, TA> style) throws BadLocationException {
		if (style != null) {
			if (style.getParagraphAlignment() != null) {
				switch (style.getParagraphAlignment()) {
					case Left:
						StyleConstants.setAlignment(parAttrs, StyleConstants.ALIGN_LEFT);
						break;
					case Right:
						StyleConstants.setAlignment(parAttrs, StyleConstants.ALIGN_RIGHT);
						break;
					case Center:
						StyleConstants.setAlignment(parAttrs, StyleConstants.ALIGN_CENTER);
						break;
					case Justify:
						StyleConstants.setAlignment(parAttrs, StyleConstants.ALIGN_JUSTIFIED);
						break;
				}
			}

			if (style.getParagraphTabs().size() > 0) {
				TabStop[] tabs = new TabStop[style.getParagraphTabs().size()];
				int i = 0;
				for (ParagraphTab pTab : style.getParagraphTabs()) {
					TabStop ts = new TabStop(pTab.getPos(), pTab.getAlign(), pTab.getLeader());
					tabs[i] = ts;
					i++;
				}
				StyleConstants.setTabSet(parAttrs, new TabSet(tabs));
			}

			if (style.getParagraphSpacing() != null) {

				if (style.getParagraphSpacing().getLineSpacingRule() == LineSpacingRule.AT_LEAST) {
					float ls = style.getParagraphSpacing().getLine() / 240;
					StyleConstants.setLineSpacing(parAttrs, ls);
				}
				if (style.getParagraphSpacing().getLineSpacingRule() == LineSpacingRule.AUTO) {
					float ls = style.getParagraphSpacing().getLine() / 240;
					StyleConstants.setLineSpacing(parAttrs, ls);
				}
				if (style.getParagraphSpacing().getBefore() != null) {
					StyleConstants.setSpaceAbove(parAttrs, style.getParagraphSpacing().getBefore());
				}
				if (style.getParagraphSpacing().getAfter() != null) {
					StyleConstants.setSpaceBelow(parAttrs, style.getParagraphSpacing().getAfter());
				}
			}

			if (style.getParagraphIndent() != null) {
				if (style.getParagraphIndent().getLeft() != null) {
					StyleConstants.setLeftIndent(parAttrs, style.getParagraphIndent().getLeft());
				}
				if (style.getParagraphIndent().getRight() != null) {
					StyleConstants.setRightIndent(parAttrs, style.getParagraphIndent().getRight());
				}
				if (style.getParagraphIndent().getFirst() != null) {
					StyleConstants.setLeftIndent(parAttrs, style.getParagraphIndent().getFirst());
				}
			}
		}
	}

	protected void processTextRun(FlexoTextRun<D, TA> run) throws BadLocationException {
		System.out.println("---------> un run: " + run.getText() + " avec " + run.getRunStyle());

		charAttrs = new SimpleAttributeSet();

		if (run.getParagraph() != null && run.getParagraph().getNamedStyle() != null) {
			applyRunStyle(run.getParagraph().getNamedStyle().getRunStyle());
		}
		if (run.getRunStyle() != null) {
			applyRunStyle(run.getRunStyle());
		}
		processText(run.getText());
	}

	protected void applyRunStyle(FlexoRunStyle<D, TA> style) throws BadLocationException {

		if (style != null) {
			if (style.getFontSize() != null) {
				StyleConstants.setFontSize(charAttrs, style.getFontSize());
			}
			if (style.getFont() != null) {
				StyleConstants.setFontFamily(charAttrs, style.getFont().getFamily());
			}
			if (style.getFontColor() != null) {
				StyleConstants.setForeground(charAttrs, style.getFontColor());
			}
			if (style.getBackgroundColor() != null) {
				StyleConstants.setBackground(charAttrs, style.getBackgroundColor());
			}
			if (style.getBold() != null) {
				StyleConstants.setBold(charAttrs, true);
			}
			if (style.getItalic() != null) {
				StyleConstants.setItalic(charAttrs, true);
			}
			if (style.getUnderline() != null) {
				StyleConstants.setUnderline(charAttrs, true);
			}
		}
	}

	protected void processText(String text) throws BadLocationException {
		// System.out.println("Et hop, du texte: " + text);
		document.insertString(currentOffset, text, charAttrs);
		currentOffset += text.length();

	}

	protected void processTable(FlexoDocTable<D, TA> table) throws BadLocationException {

		SimpleAttributeSet tableAttrs = new SimpleAttributeSet();
		int cellCount = 0;
		int rowCount = 0;
		for (FlexoDocTableRow<D, TA> row : table.getTableRows()) {
			rowCount++;
			cellCount = Math.max(cellCount, row.getTableCells().size());
		}

		int[] rowHeights = new int[rowCount];
		for (int i = 0; i < rowHeights.length; i++) {
			rowHeights[i] = 1;
		}
		int[] colWidths = new int[cellCount];
		for (int i = 0; i < colWidths.length; i++) {
			colWidths[i] = table.getColumnWidth(i);
		}

		document.insertTable(currentOffset, rowCount, cellCount, tableAttrs, colWidths, rowHeights);
		for (FlexoDocTableRow<D, TA> row : table.getTableRows()) {
			for (FlexoDocTableCell<D, TA> cell : row.getTableCells()) {
				iteratePart(cell.getElements());
				currentOffset++;
			}
		}
	}

	protected void processDrawingRun(FlexoDrawingRun<D, TA> drawingRun) throws BadLocationException {

		ImageIcon ii = new ImageIcon(drawingRun.getImage());
		document.insertPicture(ii, currentOffset);
		currentOffset++;

	}

}
