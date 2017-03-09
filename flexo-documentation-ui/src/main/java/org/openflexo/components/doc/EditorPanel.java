/**
 * 
 */
package org.openflexo.components.doc;

import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.Property;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class EditorPanel<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends JPanel {

	private Editor<D, TA> editor;
	private JPanel toolBar;

	public EditorPanel(DocumentFactory<D, TA> documentFactory) {
		this.editor = new Editor<>(documentFactory);
		this.toolBar = new JPanel();
		// Let's create the buttons
		EditorButton boldButton = new EditorButton(new Property("font-weight", "bold", new HashSet<String>()));
		boldButton.setText("B");
		EditorButton italicButton = new EditorButton(new Property("font-style", "italic", new HashSet<String>()));
		italicButton.setText("I");
		toolBar.add(boldButton);
		toolBar.add(italicButton);
		boldButton.addActionListener(new EditorButtonListener(this, boldButton));
		italicButton.addActionListener(new EditorButtonListener(this, italicButton));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(toolBar);
		this.add(editor);
	}

	public EditorPanel(FlexoDocument<D, TA> document) {
		this(document.getFactory());
		setDocument(document);
	}

	public Editor<D, TA> getEditor() {
		return this.editor;
	}

	public FlexoDocument<D, TA> getDocument() {
		return getEditor().getDocumentModel();
	}

	public void setDocument(FlexoDocument<D, TA> document) {
		getEditor().setDocumentModel(document);
	}

}
