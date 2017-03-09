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
	private Translator<D, TA> translator;

	private FlexoDocument<D, TA> document;

	public EditorPanel(DocumentFactory<D, TA> documentFactory) {
		this.editor = new Editor<>(documentFactory);
		this.toolBar = new JPanel();
		this.translator = new Translator<>();
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

	public Editor getEditor() {
		return this.editor;
	}

	public Translator getTranslator() {
		return this.translator;
	}

	public FlexoDocument<D, TA> getDocument() {
		return document;
	}

	public void setDocument(FlexoDocument<D, TA> document) {
		this.document = document;

		String content = translator.generateHTML(document);

		// Monitoring
		// document.addListener(new DocumentMonitor(document, monitorEditor));

		// Display
		System.out.println(content);
		editor.setText(content);
	}

	public void updateDocument() {
		String content = translator.generateHTML(document);
		System.out.println(content);
		editor.setText(content);
	}
}
