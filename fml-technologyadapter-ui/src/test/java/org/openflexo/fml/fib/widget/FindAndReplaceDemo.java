package org.openflexo.fml.fib.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SmartHighlightPainter;
import org.openflexo.fml.controller.widget.fmleditor.FMLEditorParser;
import org.openflexo.icon.IconLibrary;

/**
 * A simple example showing how to do search and replace in a RSyntaxTextArea. The toolbar isn't very user-friendly, but this is just to
 * show you how to use the API.
 * <p>
 * 
 * This example uses RSyntaxTextArea 2.5.6.
 */
public class FindAndReplaceDemo extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private RSyntaxTextArea textArea;
	private JTextField searchField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;

	private SquiggleUnderlineHighlightPainter painter1 = new SquiggleUnderlineHighlightPainter(Color.RED);

	private SmartHighlightPainter painter2 = new SmartHighlightPainter();

	public FindAndReplaceDemo() {

		JPanel cp = new JPanel(new BorderLayout());

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		textArea.setText(
				"Une première ligne\nUne autre ligne arrive ensuite\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\n");
		RTextScrollPane sp = new RTextScrollPane(textArea);
		cp.add(sp);

		// Create a toolbar with searching options.
		JToolBar toolBar = new JToolBar();
		searchField = new JTextField(30);
		toolBar.add(searchField);
		final JButton nextButton = new JButton("Find Next");
		nextButton.setActionCommand("FindNext");
		nextButton.addActionListener(this);
		toolBar.add(nextButton);
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});
		JButton prevButton = new JButton("Find Previous");
		prevButton.setActionCommand("FindPrev");
		prevButton.addActionListener(this);
		toolBar.add(prevButton);
		regexCB = new JCheckBox("Regex");
		toolBar.add(regexCB);
		matchCaseCB = new JCheckBox("Match Case");
		toolBar.add(matchCaseCB);
		cp.add(toolBar, BorderLayout.NORTH);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		cp.add(errorStrip, BorderLayout.LINE_END);

		Parser p = new FMLEditorParser(null, null);// new XmlParser();
		textArea.addParser(p);
		// p.parse(textArea.getD, style)

		Gutter gutter = sp.getGutter();
		// gutter.setBookmarkIcon(new ImageIcon("bookmark.png"));
		// gutter.setBookmarkIcon(IconLibrary.FIXABLE_ERROR_ICON);
		gutter.setBookmarkingEnabled(true);
		try {
			gutter.addLineTrackingIcon(0, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(1, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(3, IconLibrary.FIXABLE_ERROR_ICON);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
		String style = textArea.getSyntaxEditingStyle();
		ParseResult res = p.parse(doc, style);

		System.out.println("res=" + res.getNotices());

		/*RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter) textArea.getHighlighter();
		
		for (ParserNotice notice : res.getNotices()) {
			HighlightInfo highlight = null;
			highlight = h.addParserHighlight(notice, painter1);
		}*/

		/*doc.readLock();
		try {
		for (int i=0; i<parserCount; i++) {
		Parser parser = getParser(i);
		if (parser.isEnabled()) {
			ParseResult res = parser.parse(doc, style);
			addParserNoticeHighlights(res);
		}
		else {
			clearParserNoticeHighlights(parser);
		}
		}
		textArea.fireParserNoticesChange();
		} finally {
		doc.readUnlock();
		}*/

		setContentPane(cp);
		setTitle("Find and Replace Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// "FindNext" => search forward, "FindPrev" => search backward
		String command = e.getActionCommand();
		boolean forward = "FindNext".equals(command);

		// Create an object defining our search parameters.
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.length() == 0) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
		context.setRegularExpression(regexCB.isSelected());
		context.setSearchForward(forward);
		context.setWholeWord(false);

		boolean found = SearchEngine.find(textArea, context).wasFound();
		if (!found) {
			JOptionPane.showMessageDialog(this, "Text not found");
		}

	}

	public static void main(String[] args) {
		// Start all Swing applications on the EDT.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					String laf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(laf);
				} catch (Exception e) {
					/* never happens */ }
				FindAndReplaceDemo demo = new FindAndReplaceDemo();
				demo.setVisible(true);
				demo.textArea.requestFocusInWindow();
			}
		});
	}

}
