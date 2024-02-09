package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.RTextAreaUI;

/**
 * Specialization of RSyntaxTextArea with a specialized UI
 * 
 * Also implements the binding with the {@link FMLEditorParser} and management of parsing life-cycle
 *
 */
public class FMLRSyntaxTextArea extends RSyntaxTextArea implements DocumentListener {

	private final FMLEditor editor;
	private FMLEditorParser parser;

	public FMLRSyntaxTextArea(FMLEditor editor) {
		super();
		this.editor = editor;
		getDocument().addDocumentListener(this);
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// System.out.println("FMLRSyntaxTextArea gains focus");
			}

			@Override
			public void focusLost(FocusEvent e) {
				// System.out.println("FMLRSyntaxTextArea looses focus");
				// unregisterParser();
			}
		});
	}

	public FMLEditor getEditor() {
		return editor;
	}

	/**
	 * Returns the a real UI to install on this text area.
	 *
	 * @return The UI.
	 */
	@Override
	protected RTextAreaUI createRTextAreaUI() {
		return new FMLRSyntaxTextAreaUI(this);
	}

	public FMLEditorParser getParser() {
		return parser;
	}

	public void setParser(FMLEditorParser parser) {
		this.parser = parser;
		putClientProperty(LanguageSupport.PROPERTY_LANGUAGE_PARSER, parser);
	}

	private void registerParser() {
		addParser(parser);
		// System.out.println("******************** PARSER ON " + getParserCount());
	}

	private void unregisterParser() {
		// System.out.println("******************** PARSER OFF " + getParserCount());
		removeParser(parser);
	}

	private boolean documentModified = false;

	public boolean isDocumentModified() {
		return documentModified;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentModified = true;
		if (!preventParsing)
			registerParser();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentModified = true;
		if (!preventParsing)
			registerParser();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		documentModified = true;
		if (!preventParsing)
			registerParser();
	}

	/**
	 * Sets the text of this {@link FMLRSyntaxTextArea}
	 * 
	 * Note that this text will be parsed by {@link FMLEditorParser}. If you don't want that parser processes this, use
	 * {@link #setTextNoParsingAnalysis(String)} method
	 */
	@Override
	public void setText(String t) {
		super.setText(t);
	}

	private boolean preventParsing = false;

	/**
	 * Update the text of this {@link FMLRSyntaxTextArea} while preventing a new parsing
	 * 
	 * @param t
	 */
	public void setTextNoParsingAnalysis(String t) {
		unregisterParser();
		preventParsing = true;
		super.setText(t);
		preventParsing = false;
	}

	@Override
	public List<ParserNotice> getParserNotices() {
		if (parser != null) {
			return parser.getParserNotices();
		}
		return super.getParserNotices();
	}

}
