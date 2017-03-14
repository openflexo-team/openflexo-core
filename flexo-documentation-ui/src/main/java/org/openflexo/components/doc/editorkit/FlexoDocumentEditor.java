/**
 * 
 */
package org.openflexo.components.doc.editorkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import org.openflexo.foundation.doc.DocumentFactory;
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

	private JEditorPane editorPane;

	private PropertyChangeSupport pcSupport;

	/**
	 * Creates an empty FlexoDocumentEditor.
	 */
	public FlexoDocumentEditor(DocumentFactory<D, TA> documentFactory) {
		super();
		editorPane = new JEditorPane("text/rtf", "");
		editorPane.setEditorKit(new FlexoDocumentEditorKit());
		pcSupport = new PropertyChangeSupport(this);
		this.documentFactory = documentFactory;
		highlighter = editorPane.getHighlighter();
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
				editorPane.setDocument(reader.getDocument());
				editorPane.getDocument().addDocumentListener(this);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getPropertyChangeSupport().firePropertyChange("documentModel", oldValue, flexoDocument);
		}
	}

	public DocumentFactory<D, TA> getDocumentFactory() {
		return documentFactory;
	}

	public JEditorPane getEditorPane() {
		return editorPane;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		int length = arg0.getLength();
		System.out.println("Une insertion de " + length + "caracteres a ete effectuee en position " + offset);
		Element body = editorPane.getDocument().getDefaultRootElement().getElement(1);
		Element paragraph = body.getElement(0);
		if (offset != 0) {
			String insertedText;
			try {
				insertedText = editorPane.getText(offset, length);
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

		private JPanel toolBar;

		private FlexoDocumentEditorPanel() {
			this.toolBar = new JPanel();
			// Let's create the buttons
			toolBar.add(new JLabel("Prout"));
			this.setLayout(new BorderLayout());
			this.add(toolBar, BorderLayout.NORTH);
			this.add(new JScrollPane(editorPane), BorderLayout.CENTER);
		}

		public Element getElement(Object docObject) {
			// TODO : implement this
			return null;
		}

		public void setSelectedElements(List<? extends Element> elements) {
			// TODO : implement this
		}

		public boolean scrollToElement(Element element) {
			// TODO : implement this
			return false;
		}
	}

}
