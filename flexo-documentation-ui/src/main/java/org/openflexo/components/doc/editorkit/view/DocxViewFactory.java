package org.openflexo.components.doc.editorkit.view;

import javax.swing.text.AbstractDocument;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * A factory to create a view of some portion of document subject. This is intended to enable customization of how views get mapped over a
 * document model.
 *
 * @author Stanislav Lapitsky
 */
public class DocxViewFactory implements ViewFactory {

	/**
	 * Constructs new instance.
	 */
	public DocxViewFactory() {
	}

	/**
	 * Creates a view from the given structural element of a document.
	 *
	 * @param elem
	 *            the piece of the document to build a view of
	 * @return the view
	 * @see View
	 */
	@Override
	public View create(Element elem) {
		String kind = elem.getName();
		if (kind != null) {
			if (kind.equals(AbstractDocument.ContentElementName)) {
				return new LabelView(elem);
			}
			else if (kind.equals(AbstractDocument.ParagraphElementName)) {
				// return new NumberedParagraphView(elem);
				return new ParagraphView(elem);
			}
			else if (kind.equals(AbstractDocument.SectionElementName)) {
				return new SectionView(elem, View.Y_AXIS);
			}
			else if (kind.equals(StyleConstants.ComponentElementName)) {
				return new ComponentView(elem);
			}
			else if (kind.equals(StyleConstants.IconElementName)) {
				return new IconView(elem);
			}
			else if (kind.equals("table")) {
				return new TableView(elem);
			}
			else if (kind.equals("row")) {
				return new RowView(elem);
			}
			else if (kind.equals("cell")) {
				return new CellView(elem);
			}
		}
		// default to text display
		return new LabelView(elem);
	}
}
