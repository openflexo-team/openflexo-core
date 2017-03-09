/**
 * 
 */
package org.openflexo.components.doc;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class Editor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends JEditorPane {

	private FlexoDocument<D, TA> document;
	private DocumentFactory<D, TA> documentFactory;

	/**
	 * Creates an empty Editor.
	 */
	public Editor(DocumentFactory<D, TA> documentFactory) {
		super("text/html", "");
		this.documentFactory = documentFactory;
		this.document = documentFactory.makeDocument();
		this.getDocument().addDocumentListener(new EditorListener(this));
	}

	/**
	 * Creates an Editor with a Document to be displayed and edited.
	 * 
	 * @param document
	 *            the document.
	 */
	public Editor(FlexoDocument<D, TA> document) {
		super("text/html", "");
		this.document = document;
		this.documentFactory = document.getFactory();
		this.getDocument().addDocumentListener(new EditorListener(this));
	}

	public FlexoDocument<?, ?> getDocumentModel() {
		return document;
	}

}

class EditorListener implements DocumentListener {

	private Editor e;

	public EditorListener(Editor e) {
		this.e = e;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		int length = arg0.getLength();
		System.out.println("Une insertion de " + length + "caracteres a ete effectuee en position " + offset);
		Element body = e.getDocument().getDefaultRootElement().getElement(1);
		Element paragraph = body.getElement(0);
		if (offset != 0) {
			String insertedText;
			try {
				insertedText = e.getText(offset, length);
				// e.document.insert(insertedText, offset - length);
				System.out.println("On doit inserer " + insertedText + " offset=" + offset + " length=" + length);
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {

	}

}
