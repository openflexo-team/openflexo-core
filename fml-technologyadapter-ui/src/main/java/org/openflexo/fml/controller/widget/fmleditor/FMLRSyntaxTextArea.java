package org.openflexo.fml.controller.widget.fmleditor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextAreaUI;

/**
 * Specialization of RSyntaxTextArea with a specialized UI
 *
 */
public class FMLRSyntaxTextArea extends RSyntaxTextArea {

	private final FMLEditor editor;

	public FMLRSyntaxTextArea(FMLEditor editor) {
		this.editor = editor;
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

}
