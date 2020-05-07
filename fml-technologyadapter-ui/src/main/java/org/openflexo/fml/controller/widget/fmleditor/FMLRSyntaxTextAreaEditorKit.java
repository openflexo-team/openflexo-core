package org.openflexo.fml.controller.widget.fmleditor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rtextarea.IconRowHeader;
import org.fife.ui.rtextarea.RTextArea;

public class FMLRSyntaxTextAreaEditorKit extends RSyntaxTextAreaEditorKit {

	@Override
	public IconRowHeader createIconRowHeader(RTextArea textArea) {
		return new FMLIconRowHeader((FMLRSyntaxTextArea) textArea);
	}
}
