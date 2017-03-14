/**
 * 
 */
package org.openflexo.components.doc.deprecated;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Element;

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
		this.setLayout(new BorderLayout());
		this.add(toolBar, BorderLayout.NORTH);
		this.add(new JScrollPane(editor), BorderLayout.CENTER);
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
