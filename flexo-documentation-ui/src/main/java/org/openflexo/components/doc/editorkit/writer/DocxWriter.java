package org.openflexo.components.doc.deprecated.editorkit.writer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShortHexNumber;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.openflexo.components.doc.deprecated.editorkit.DocxDocument;

/**
 * Implements writer of Docx document.
 *
 * @author Stanislav Lapitsky
 */
public class DocxWriter {

	/**
	 * ico document instance to the writing.
	 */
	protected Document document;
	WordprocessingMLPackage wordMLPackage;

	/**
	 * Constructs new writer instance.
	 *
	 * @param doc
	 *            document for writing.
	 */
	public DocxWriter(Document doc) {
		document = doc;
	}

	/**
	 * Performs writing to a file.
	 *
	 * @param fileName
	 *            Name of file
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	public void write(String fileName) throws IOException {
		FileOutputStream out = new FileOutputStream(fileName);
		write(out, 0, document.getLength());
		out.close();
	}

	/**
	 * Performs writing to a writer.
	 *
	 * @param out
	 *            writer
	 * @param pos
	 *            start offset
	 * @param len
	 *            content length
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	public void write(OutputStream out, int pos, int len) throws IOException {
		Element root = document.getDefaultRootElement();
		try {
			wordMLPackage = WordprocessingMLPackage.createPackage();
			// body
			writeContent(root, pos, len, wordMLPackage.getMainDocumentPart().getContent());
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(out);
			// fillSectPr(wordMLPackage);
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
	}

	protected void fillSectPr(WordprocessingMLPackage wordMLPackage) {
		List<SectionWrapper> sections = wordMLPackage.getDocumentModel().getSections();

		SectPr sectPr = new SectPr();
		fillPageFormat(sectPr);
		wordMLPackage.getMainDocumentPart().addObject(sectPr);
		sections.get(sections.size() - 1).setSectPr(sectPr);
	}

	protected void fillPageFormat(SectPr sectPr) {
		SectPr.PgSz pgSz = new SectPr.PgSz();
		pgSz.setOrient(STPageOrientation.PORTRAIT);
		pgSz.setW(new BigInteger((int) (1440 * 8.5) + ""));
		pgSz.setH(new BigInteger(1440 * 11 + ""));
		sectPr.setPgSz(pgSz);

		SectPr.PgMar margins = new SectPr.PgMar();
		margins.setLeft(new BigInteger("1440"));
		margins.setRight(new BigInteger("1440"));
		margins.setTop(new BigInteger("1440"));
		margins.setBottom(new BigInteger("1440"));

		sectPr.setPgMar(margins);
	}

	/**
	 * writes content of the element.
	 *
	 * @param root
	 *            The root element.
	 * @param pos
	 *            pos in the document
	 * @param len
	 *            length of content to be stored
	 * @param content
	 *            target part
	 *
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeContent(Element root, int pos, int len, List<Object> content) throws IOException {
		int startIndex = root.getElementIndex(pos);
		int endIndex = root.getElementIndex(pos + len);
		for (int i = startIndex; i <= endIndex; i++) {
			Element child = root.getElement(i);

			if (child.getStartOffset() > pos + len) {
				return;
			}
			if (child.getName().equals("paragraph")) {
				writeParagraph(child, pos, len, content);
			}
			else if (child.getName().equals("table")) {
				writeTable(child, content);
			}
		}
	}

	/**
	 * writes content of single paragraph.
	 *
	 * @param paragraph
	 *            source model element
	 * @param pos
	 *            pos in the document
	 * @param len
	 *            length of content to be stored
	 * @param content
	 *            target part
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeParagraph(Element paragraph, int pos, int len, List<Object> content) throws IOException {
		P p = new P();

		content.add(p);
		int start = paragraph.getElementIndex(pos);
		int end = paragraph.getElementIndex(pos + len);
		PPr ppr = new PPr();
		p.setPPr(ppr);

		Jc jc = new Jc();
		p.getPPr().setJc(jc);

		if (StyleConstants.getAlignment(paragraph.getAttributes()) == StyleConstants.ALIGN_LEFT) {
			jc.setVal(JcEnumeration.LEFT);
		}
		else if (StyleConstants.getAlignment(paragraph.getAttributes()) == StyleConstants.ALIGN_CENTER) {
			jc.setVal(JcEnumeration.CENTER);
		}
		else if (StyleConstants.getAlignment(paragraph.getAttributes()) == StyleConstants.ALIGN_RIGHT) {
			jc.setVal(JcEnumeration.RIGHT);
		}
		else if (StyleConstants.getAlignment(paragraph.getAttributes()) == StyleConstants.ALIGN_JUSTIFIED) {
			jc.setVal(JcEnumeration.BOTH);
		}

		PPrBase.Ind ind = new PPrBase.Ind();
		if (paragraph.getAttributes().isDefined(StyleConstants.LeftIndent)) {
			ind.setLeft(new BigInteger((int) StyleConstants.getLeftIndent(paragraph.getAttributes()) * 20 + ""));
		}
		if (paragraph.getAttributes().isDefined(StyleConstants.RightIndent)) {
			ind.setRight(new BigInteger((int) StyleConstants.getRightIndent(paragraph.getAttributes()) * 20 + ""));
		}
		if (paragraph.getAttributes().isDefined(StyleConstants.FirstLineIndent)) {
			ind.setFirstLine(new BigInteger((int) StyleConstants.getFirstLineIndent(paragraph.getAttributes()) * 20 + ""));
		}
		p.getPPr().setInd(ind);

		PPrBase.Spacing spacing = new PPrBase.Spacing();
		if (paragraph.getAttributes().isDefined(StyleConstants.SpaceAbove)) {
			spacing.setBefore(new BigInteger((int) StyleConstants.getSpaceAbove(paragraph.getAttributes()) * 2 + ""));
		}
		if (paragraph.getAttributes().isDefined(StyleConstants.SpaceBelow)) {
			spacing.setAfter(new BigInteger((int) StyleConstants.getSpaceBelow(paragraph.getAttributes()) * 2 + ""));
		}
		if (paragraph.getAttributes().isDefined(StyleConstants.LineSpacing)) {
			spacing.setLine(new BigInteger(((int) StyleConstants.getLineSpacing(paragraph.getAttributes()) + 1) * 240 + ""));
			spacing.setLineRule(STLineSpacingRule.AT_LEAST);
		}
		p.getPPr().setSpacing(spacing);

		for (int i = start; i <= end; i++) {
			Element child = paragraph.getElement(i);
			if (child.getName().equals("content")) {
				writeLeaf(child, pos, len, p.getContent());
			}
			else if (child.getName().equals("icon")) {
				try {
					writeIcon(child, p.getContent());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * writes the leaf document's element.
	 *
	 * @param leaf
	 *            model element
	 * @param pos
	 *            offset
	 * @param len
	 *            content length
	 * @param content
	 *            target
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeLeaf(Element leaf, int pos, int len, List<Object> content) throws IOException {
		Document doc = leaf.getDocument();
		String contentText = "";
		try {
			int start = Math.max(leaf.getStartOffset(), pos);
			int end = Math.min(leaf.getEndOffset(), pos + len) - start;
			contentText = doc.getText(start, end);

			System.out.println("writeLeaf with " + contentText);
		} catch (Exception ex) {
			throw new IOException("Error reading leaf content from source document!");
		}
		if (contentText.length() <= 0) {
			return;
		}

		R r = new R();
		r.setRPr(createRPr(leaf));
		Text text = new Text();
		text.setValue(contentText);
		r.getContent().add(text);

		content.add(r);
	}

	protected RPr createRPr(Element leaf) {
		RPr rPr = new RPr();
		if (StyleConstants.isBold(leaf.getAttributes())) {
			rPr.setB(new BooleanDefaultTrue());
		}
		if (StyleConstants.isItalic(leaf.getAttributes())) {
			rPr.setI(new BooleanDefaultTrue());
		}
		if (StyleConstants.isUnderline(leaf.getAttributes())) {
			U u = new U();
			u.setVal(UnderlineEnumeration.SINGLE);
			u.setColor(getColorString(StyleConstants.getForeground(leaf.getAttributes())));
			rPr.setU(u);
		}
		RFonts fonts = new RFonts();
		fonts.setAscii(StyleConstants.getFontFamily(leaf.getAttributes()));
		HpsMeasure fs = new HpsMeasure();
		fs.setVal(new BigInteger(StyleConstants.getFontSize(leaf.getAttributes()) * 2 + ""));
		rPr.setSz(fs);
		rPr.setSzCs(fs);
		org.docx4j.wml.Color color = new org.docx4j.wml.Color();
		color.setVal(getColorString(StyleConstants.getForeground(leaf.getAttributes())));
		rPr.setColor(color);
		rPr.setRFonts(fonts);

		return rPr;
	}

	/**
	 * writes an image.
	 *
	 * @param leaf
	 *            model element
	 * @param content
	 *            target content
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeIcon(Element leaf, List<Object> content) throws IOException {
		AttributeSet attr = leaf.getAttributes();
		ImageIcon icon = (ImageIcon) StyleConstants.getIcon(attr);
		if (icon != null) {
			try {
				int w = StyleConstants.getIcon(attr).getIconWidth();
				int h = StyleConstants.getIcon(attr).getIconHeight();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);
				ImageIO.write(bi, "png", os);
				byte[] ba = os.toByteArray();

				BinaryPartAbstractImage img = BinaryPartAbstractImage.createImagePart(wordMLPackage, ba);
				Inline inline = img.createImageInline("test", "test image", 0, 1, false);
				R run = new R();
				content.add(run);
				ObjectFactory factory = new ObjectFactory();
				org.docx4j.wml.Drawing drawing = factory.createDrawing();
				run.getContent().add(drawing);
				drawing.getAnchorOrInline().add(inline);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * writes table element.
	 *
	 * @param table
	 *            model element
	 * @param content
	 *            target content
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeTable(Element table, List<Object> content) throws IOException {
		int rc = table.getElementCount();
		Tbl tbl = new Tbl();
		TblPr tblPr = new TblPr();
		TblWidth tblW = new TblWidth();
		tblW.setType("auto");
		tblW.setW(new BigInteger("0"));
		tblPr.setTblW(tblW);

		tblW = new TblWidth();
		tblW.setType("dxa");
		tblW.setW(new BigInteger("30"));
		tblPr.setTblInd(tblW);

		Jc jc = fillTableAlign(table);
		tblPr.setJc(jc);

		tblPr.setTblBorders(fillTableBorders((DocxDocument.TableElement) table));

		CTTblLayoutType layout = new CTTblLayoutType();
		layout.setType(STTblLayoutType.FIXED);
		tblPr.setTblLayout(layout);

		CTShortHexNumber look = new CTShortHexNumber();
		look.setVal("0000");
		// TODO: fix this
		// tblPr.setTblLook(look);

		TblGrid grid = new TblGrid();
		tbl.setTblGrid(grid);
		tbl.setTblPr(tblPr);
		writeGridInfo(grid, table);
		content.add(tbl);
		for (int i = 0; i < rc; i++) {
			Element row = table.getElement(i);

			writeRow(row, tbl.getContent());
		}
	}

	private Jc fillTableAlign(Element table) {
		Jc jc = new Jc();
		if (StyleConstants.getAlignment(table.getAttributes()) == StyleConstants.ALIGN_RIGHT) {
			jc.setVal(JcEnumeration.RIGHT);
		}
		else if (StyleConstants.getAlignment(table.getAttributes()) == StyleConstants.ALIGN_CENTER) {
			jc.setVal(JcEnumeration.CENTER);
		}
		else if (StyleConstants.getAlignment(table.getAttributes()) == StyleConstants.ALIGN_LEFT) {
			jc.setVal(JcEnumeration.LEFT);
		}
		return jc;
	}

	protected void writeGridInfo(TblGrid grid, Element table) {
		DocxDocument.RowElement modelRow = (DocxDocument.RowElement) table.getElement(0);
		for (int c = 0; c < modelRow.getElementCount(); c++) {
			TblGridCol col = new TblGridCol();
			col.setW(new BigInteger(modelRow.getCellWidth(c) * 20 + ""));
			grid.getGridCol().add(col);
		}
	}

	/**
	 * writes row element.
	 *
	 * @param row
	 *            model element
	 * @param content
	 *            target content
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeRow(Element row, List<Object> content) throws IOException {
		Tr tr = new Tr();
		content.add(tr);

		for (int i = 0; i < row.getElementCount(); i++) {
			writeCellContent(row.getElement(i), tr.getContent());
		}
	}

	/**
	 * writes content of the cell.
	 *
	 * @param cell
	 *            model element
	 * @param content
	 *            target content
	 * @exception IOException
	 *                occure when writing is failed.
	 */
	protected void writeCellContent(Element cell, List<Object> content) throws IOException {
		DocxDocument.CellElement cellElement = (DocxDocument.CellElement) cell;
		Tc tc = new Tc();
		TcPr tcPr = new TcPr();

		fillCellWidth(cellElement, tcPr);
		tc.setTcPr(tcPr);

		TcPrInner.TcBorders tcBorders = fillCellBorders(cellElement);
		tcPr.setTcBorders(tcBorders);

		TcMar tcMar = fillCellMargins();
		tcPr.setTcMar(tcMar);

		content.add(tc);

		writeContent(cell, cell.getStartOffset(), cell.getEndOffset() - cell.getStartOffset(), tc.getContent());
	}

	private void fillCellWidth(DocxDocument.CellElement cellElement, TcPr tcPr) {
		TblWidth tblWidth = new TblWidth();
		tblWidth.setType("dxa");
		tblWidth.setW(new BigInteger(cellElement.getWidth() * 20 + ""));
		tcPr.setTcW(tblWidth);
	}

	private TcPrInner.TcBorders fillCellBorders(DocxDocument.CellElement cellElement) {
		TcPrInner.TcBorders tcBorders = new TcPrInner.TcBorders();
		CTBorder topBorder = new CTBorder();
		topBorder.setColor(getColorString(cellElement.getBorders().lineColor));
		topBorder.setSpace(new BigInteger("0"));
		topBorder.setSz(new BigInteger("4"));
		topBorder.setVal(STBorder.SINGLE);
		tcBorders.setTop(topBorder);

		CTBorder leftBorder = new CTBorder();
		leftBorder.setColor(getColorString(cellElement.getBorders().lineColor));
		leftBorder.setSpace(new BigInteger("0"));
		leftBorder.setSz(new BigInteger("4"));
		leftBorder.setVal(STBorder.SINGLE);
		tcBorders.setLeft(leftBorder);

		CTBorder rightBorder = new CTBorder();
		rightBorder.setColor(getColorString(cellElement.getBorders().lineColor));
		rightBorder.setSpace(new BigInteger("0"));
		rightBorder.setSz(new BigInteger("4"));
		rightBorder.setVal(STBorder.SINGLE);
		tcBorders.setRight(rightBorder);

		CTBorder bottomBorder = new CTBorder();
		bottomBorder.setColor(getColorString(cellElement.getBorders().lineColor));
		bottomBorder.setSpace(new BigInteger("0"));
		bottomBorder.setSz(new BigInteger("4"));
		bottomBorder.setVal(STBorder.SINGLE);
		tcBorders.setBottom(bottomBorder);
		return tcBorders;
	}

	private TblBorders fillTableBorders(DocxDocument.TableElement tableElement) {
		TblBorders tcBorders = new TblBorders();
		CTBorder topBorder = new CTBorder();
		topBorder.setColor(getColorString(tableElement.getBorders().lineColor));
		topBorder.setSpace(new BigInteger("0"));
		topBorder.setSz(new BigInteger("4"));
		topBorder.setVal(STBorder.SINGLE);
		tcBorders.setTop(topBorder);

		CTBorder leftBorder = new CTBorder();
		leftBorder.setColor(getColorString(tableElement.getBorders().lineColor));
		leftBorder.setSpace(new BigInteger("0"));
		leftBorder.setSz(new BigInteger("4"));
		leftBorder.setVal(STBorder.SINGLE);
		tcBorders.setLeft(leftBorder);

		CTBorder rightBorder = new CTBorder();
		rightBorder.setColor(getColorString(tableElement.getBorders().lineColor));
		rightBorder.setSpace(new BigInteger("0"));
		rightBorder.setSz(new BigInteger("4"));
		rightBorder.setVal(STBorder.SINGLE);
		tcBorders.setRight(rightBorder);

		CTBorder bottomBorder = new CTBorder();
		bottomBorder.setColor(getColorString(tableElement.getBorders().lineColor));
		bottomBorder.setSpace(new BigInteger("0"));
		bottomBorder.setSz(new BigInteger("4"));
		bottomBorder.setVal(STBorder.SINGLE);
		tcBorders.setBottom(bottomBorder);
		return tcBorders;
	}

	private TcMar fillCellMargins() {
		TcMar tcMar = new TcMar();
		TblWidth topMargin = new TblWidth();
		topMargin.setType("dxa");
		topMargin.setW(new BigInteger("60"));
		tcMar.setTop(topMargin);
		TblWidth bottomMargin = new TblWidth();
		bottomMargin.setType("dxa");
		bottomMargin.setW(new BigInteger("60"));
		tcMar.setBottom(bottomMargin);
		TblWidth leftMargin = new TblWidth();
		leftMargin.setType("dxa");
		leftMargin.setW(new BigInteger("60"));
		tcMar.setLeft(leftMargin);
		TblWidth rightMargin = new TblWidth();
		rightMargin.setType("dxa");
		rightMargin.setW(new BigInteger("60"));
		tcMar.setRight(rightMargin);
		return tcMar;
	}

	/**
	 * Gets precede font description.
	 *
	 * @param attr
	 *            Font attributes.
	 */
	protected String getBeforeFontDescription(AttributeSet attr, boolean isStyle) {
		String result = "";
		// --- FONT ---
		if (StyleConstants.isBold(attr))
			result += "\\b";
		if (StyleConstants.isItalic(attr))
			result += "\\i";
		if (StyleConstants.isUnderline(attr))
			result += "\\ul";

		if (StyleConstants.isStrikeThrough(attr))
			result += "\\strike";
		if (StyleConstants.isSubscript(attr))
			result += "\\sub";
		if (StyleConstants.isSuperscript(attr))
			result += "\\super";

		// result+="\\f"+fontList.indexOf(StyleConstants.getFontFamily(attr));
		result += "\\fs" + new Integer(StyleConstants.getFontSize(attr) * 2).toString();
		// --- COLORS --- attr.isDefined("foreground")
		Color fg = (Color) attr.getAttribute(StyleConstants.Foreground);
		if (fg != null) {
			if (!isStyle)
				result += "{";
			// result+="\\cf"+(colorList.indexOf(fg)+1);
		}

		Color bg = (Color) attr.getAttribute(StyleConstants.Background);
		if (bg != null) {
			if (!isStyle)
				result += "{";
			// result+="\\highlight"+(colorList.indexOf(bg)+1);
		}
		return result;
	}

	protected String getColorString(Color color) {
		return Integer.toHexString(color.getRGB()).substring(2);
	}
}
