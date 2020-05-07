package org.openflexo.fml.controller.widget.fmleditor;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaUI;

/**
 * UI used by <code>RSyntaxTextArea</code>. This allows us to implement syntax highlighting.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class FMLRSyntaxTextAreaUI extends RSyntaxTextAreaUI {

	private static final EditorKit FML_KIT = new FMLRSyntaxTextAreaEditorKit();

	public static ComponentUI createUI(JComponent ta) {
		return new FMLRSyntaxTextAreaUI(ta);
	}

	/**
	 * Constructor.
	 */
	public FMLRSyntaxTextAreaUI(JComponent rSyntaxTextArea) {
		super(rSyntaxTextArea);
	}

	/**
	 * Fetches the EditorKit for the UI.
	 *
	 * @param tc
	 *            The text component for which this UI is installed.
	 * @return The editor capabilities.
	 * @see javax.swing.plaf.TextUI#getEditorKit
	 */
	@Override
	public EditorKit getEditorKit(JTextComponent tc) {
		return FML_KIT;
	}

}
