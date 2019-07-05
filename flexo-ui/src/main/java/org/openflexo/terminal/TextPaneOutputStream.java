package org.openflexo.terminal;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class TextPaneOutputStream extends OutputStream {

	private final JTextPane textPane;
	private final StringBuilder sb = new StringBuilder();
	private Color color;

	public TextPaneOutputStream(final JTextPane textPane, Color color) {
		this.textPane = textPane;
		this.color = color;
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	@Override
	public void write(int b) throws IOException {

		if (b == '\r')
			return;

		if (b == '\n') {
			final String text = sb.toString() + "\n";

			try {
				Document doc = textPane.getDocument();

				SimpleAttributeSet attributes = new SimpleAttributeSet();
				attributes = new SimpleAttributeSet();
				attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, color);

				doc.insertString(doc.getLength(), text, attributes);
				textPane.setCaretPosition(doc.getLength());
			} catch (BadLocationException e) {
				UIManager.getLookAndFeel().provideErrorFeedback(textPane);
			}

			sb.setLength(0);

			return;
		}

		sb.append((char) b);
	}
}
