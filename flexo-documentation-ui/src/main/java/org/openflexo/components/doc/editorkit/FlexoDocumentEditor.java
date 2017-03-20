/**
 * 
 */
package org.openflexo.components.doc.editorkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import org.openflexo.components.doc.editorkit.element.DocumentElement;
import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A Wysiwyg editor for {@link FlexoDocument} API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocumentEditor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		implements HasPropertyChangeSupport, DocumentListener {

	private FlexoDocument<D, TA> flexoDocument;
	private DocumentFactory<D, TA> documentFactory;
	private Highlighter highlighter;
	private DefaultHighlightPainter highlighterPainter;

	private JEditorPane jEditorPane;
	private FlexoDocumentEditorPanel editorPanel;

	private PropertyChangeSupport pcSupport;

	/**
	 * Creates an empty FlexoDocumentEditor.
	 */
	public FlexoDocumentEditor(DocumentFactory<D, TA> documentFactory) {
		super();
		pcSupport = new PropertyChangeSupport(this);
		jEditorPane = new JEditorPane("text/rtf", "");
		jEditorPane.setEditorKit(new FlexoDocumentEditorKit());
		editorPanel = new FlexoDocumentEditorPanel();
		this.documentFactory = documentFactory;
		highlighter = jEditorPane.getHighlighter();
		highlighterPainter = new DefaultHighlightPainter(Color.GREEN);
	}

	/**
	 * Creates an FlexoDocumentEditor with a Document to be displayed and edited.
	 * 
	 * @param flexoDocument
	 *            the flexoDocument.
	 */
	public FlexoDocumentEditor(FlexoDocument<D, TA> flexoDocument) {
		this(flexoDocument.getFactory());
		setFlexoDocument(flexoDocument);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	public void highlight(int startIndex, int endIndex) {
		try {
			highlighter.addHighlight(startIndex, endIndex, highlighterPainter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FlexoDocument<D, TA> getFlexoDocument() {
		return flexoDocument;
	}

	public void setFlexoDocument(FlexoDocument<D, TA> flexoDocument) {
		if (flexoDocument != this.flexoDocument) {
			FlexoDocument<D, TA> oldValue = this.flexoDocument;
			this.flexoDocument = flexoDocument;
			FlexoDocumentReader<D, TA> reader;
			try {
				reader = new FlexoDocumentReader<>(flexoDocument);
				jEditorPane.setDocument(reader.getDocument());
				jEditorPane.getDocument().addDocumentListener(this);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getPropertyChangeSupport().firePropertyChange("flexoDocument", oldValue, flexoDocument);
		}
	}

	public DocumentFactory<D, TA> getDocumentFactory() {
		return documentFactory;
	}

	public JEditorPane getJEditorPane() {
		return jEditorPane;
	}

	public FlexoDocumentEditorPanel getEditorPanel() {
		return editorPanel;
	}

	public FlexoStyledDocument getStyledDocument() {
		return (FlexoStyledDocument) jEditorPane.getDocument();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		int length = arg0.getLength();
		System.out.println("Une insertion de " + length + "caracteres a ete effectuee en position " + offset);
		Element body = jEditorPane.getDocument().getDefaultRootElement().getElement(1);
		Element paragraph = body.getElement(0);
		if (offset != 0) {
			String insertedText;
			try {
				insertedText = jEditorPane.getText(offset, length);
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

	public class FlexoDocumentEditorPanel extends JPanel {

		private FlexoDocumentToolbar toolBar;

		private FlexoDocumentEditorPanel() {
			this.toolBar = new FlexoDocumentToolbar(FlexoDocumentEditor.this, null);
			// Let's create the buttons
			this.setLayout(new BorderLayout());
			this.add(toolBar, BorderLayout.NORTH);
			this.add(new JScrollPane(jEditorPane), BorderLayout.CENTER);
		}

	}

	public DocumentElement getElement(FlexoDocObject<D, TA> docObject) {
		System.out.println("On cherche " + docObject);
		return null;
	}

	public void setSelectedElements(List<DocumentElement> elts) {
		// TODO Auto-generated method stub

	}

	public boolean scrollToElement(Element element) {
		// TODO : implement this
		return false;
	}

	public void highlight(FlexoDocObject<D, TA> docObject) {
		DocumentElement docElement = getElement(docObject);
		if (docElement != null) {
			try {
				highlighter.addHighlight(docElement.getStartOffset(), docElement.getEndOffset(), highlighterPainter);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
