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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import org.openflexo.components.doc.editorkit.element.DocumentElement;
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
import org.openflexo.toolbox.StringUtils;

/**
 * A factory used to build {@link FlexoStyledDocument} from a given {@link FlexoDocument}
 *
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author sylvain
 */
public class FlexoDocumentEditorFactory<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> {
	/**
	 * document instance to the building.
	 */
	protected FlexoStyledDocument<D, TA> document;
	private FlexoDocument<D, TA> flexoDocument;

	/**
	 * Current offset in the document for insert action.
	 */
	protected int currentOffset = 0;

	SimpleAttributeSet parAttrs;
	SimpleAttributeSet charAttrs;

	private boolean numberNextParagraph = false;
	private String numberingString;

	private List<Integer> numbering = new ArrayList<>();

	protected FlexoDocumentEditorFactory() {
	}

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
	public FlexoDocumentEditorFactory(D flexoDocument) throws BadLocationException {
		this.flexoDocument = flexoDocument;
		document = new FlexoStyledDocument<>(flexoDocument);
		read(flexoDocument, 0);
	}

	/**
	 * Return the {@link FlexoStyledDocument} beeing built by this reader
	 * 
	 * @return
	 */
	public FlexoStyledDocument<D, TA> getDocument() {
		return document;
	}

	/**
	 * Return {@link FlexoDocument} beeing read by this reader
	 * 
	 * @return
	 */
	public FlexoDocument<D, TA> getFlexoDocument() {
		return flexoDocument;
	}

	/**
	 * Reads content of specified stream to the document.
	 *
	 * @param in
	 *            stream.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	protected void read(FlexoDocument<D, TA> flexoDocument, int offset) throws BadLocationException {
		System.out.println("Starting reading " + flexoDocument);

		document.setIsReadingDocument(true);
		iteratePart(flexoDocument.getElements());

		for (Element e : document.getRootElements()) {
			if (e instanceof DocumentElement) {
				((DocumentElement<D, TA>) e).lookupDocObject();
			}
		}
		document.setIsReadingDocument(false);

		this.currentOffset = offset;

	}

	public void iteratePart(FlexoDocObject<D, TA>... content) throws BadLocationException {
		iteratePart(Arrays.asList(content));
	}

	@SuppressWarnings("unchecked")
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
		// System.out.println("---------> un paragraphe: " + p + " avec " + p.getParagraphStyle());

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

	protected void applyParagraphStyle(FlexoParagraphStyle<D, TA> style) {
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

			if (style.getParagraphNumbering() != null) {
				numberNextParagraph = true;
				// Integer numId = style.getParagraphNumbering().getNumId();
				Integer ilvl = style.getParagraphNumbering().getIlvl();
				numberingString = handleNumbering(ilvl == null ? 0 : ilvl);
			}
		}
	}

	private String handleNumbering(int level) {
		if (numbering.size() <= level) {
			numbering.add(1);
		}
		else {
			numbering.set(level, numbering.get(level) + 1);
			int oldSize = numbering.size();
			for (int i = level + 1; i < oldSize; i++) {
				numbering.remove(numbering.get(numbering.size() - 1));
			}
		}
		StringBuffer sb = new StringBuffer();
		// Unused boolean isFirst = true;
		for (int i = 0; i <= level; i++) {
			while (i >= numbering.size()) {
				numbering.add(1);
			}
			// Disactivate numbering
			// sb.append((isFirst ? "" : ".") + numbering.get(i));
			// Unused isFirst = false;
		}
		return sb.toString();
	}

	protected void processTextRun(FlexoTextRun<D, TA> run) throws BadLocationException {
		// System.out.println("---------> un run: " + run.getText() + " avec " + run.getRunStyle());

		charAttrs = new SimpleAttributeSet();

		// IMPORTANT:
		// We add here a runId attribute, so that AbstractElement structure reflect
		// the structure of the underlying FlexoDocument
		charAttrs.addAttribute("runId", run.getParagraph().getIdentifier() + "." + run.getIndex());

		if (run.getParagraph() != null && run.getParagraph().getNamedStyle() != null) {
			applyRunStyle(run.getParagraph().getNamedStyle().getRunStyle());
		}
		if (run.getRunStyle() != null) {
			applyRunStyle(run.getRunStyle());
		}
		processText(run.getText());
	}

	protected void applyRunStyle(FlexoRunStyle<D, TA> style) {

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

		if (StringUtils.isNotEmpty(text)) {

			if (numberNextParagraph) {
				// System.out.println("Tiens faudrait numeroter " + text);
				// System.out.println("numId:" + numId + " ilvl:" + ilvl);
				text = numberingString + " " + text;
				numberNextParagraph = false;
			}

			document.insertString(currentOffset, text, charAttrs);
			currentOffset += text.length();
		}

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

	protected void processDrawingRun(FlexoDrawingRun<D, TA> drawingRun) {

		if (drawingRun.getImage() != null) {
			ImageIcon ii = new ImageIcon(drawingRun.getImage());
			document.insertPicture(ii, currentOffset);
			currentOffset++;
		}

	}

}
