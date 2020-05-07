package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.FoldingAwareIconRowHeader;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

public class FMLIconRowHeader extends FoldingAwareIconRowHeader {

	public FMLIconRowHeader(FMLRSyntaxTextArea textArea) {
		super(textArea);
	}

	public FMLRSyntaxTextArea getTextArea() {
		return (FMLRSyntaxTextArea) textArea;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("On appuie sur le bouton");

		if (e.getButton() == MouseEvent.BUTTON3) {
			System.out.println("click droit...");
			try {
				int line = viewToModelLine(e.getPoint()) + 1;
				/*if (line>-1) {
					toggleBookmark(line);
				}*/
				System.out.println("Ligne: " + line);
				List<ParserNotice> parserNotices = getParserNotices(line);
				System.out.println("Notices: " + parserNotices);
			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}

		}

		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("On relache le bouton");
		super.mouseReleased(e);
	}

	/**
	 * Returns the line rendered at the specified location.
	 *
	 * @param p
	 *            The location in this row header.
	 * @return The corresponding line in the editor.
	 * @throws BadLocationException
	 *             ble If an error occurs.
	 */
	private int viewToModelLine(Point p) throws BadLocationException {
		int offs = textArea.viewToModel(p);
		return offs > -1 ? textArea.getLineOfOffset(offs) : -1;
	}

	public List<ParserNotice> getParserNotices(int line) {
		List<ParserNotice> returned = new ArrayList<>();
		for (ParserNotice parserNotice : getTextArea().getParserNotices()) {
			if (parserNotice.getLine() == line) {
				returned.add(parserNotice);
			}
		}
		return returned;
	}

}
