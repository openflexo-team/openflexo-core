/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.fml.controller.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.openflexo.fml.fib.widget.OnTheFlyFMLParser;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.icon.IconLibrary;

/**
 * Widget allowing to edit a FML virtual model
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLEditor extends JPanel {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/fml", "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	private RSyntaxTextArea textArea;
	private JTextField searchField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;
	private TextFinder textFinder;

	public FMLEditor(VirtualModelResource fmlResource) {
		super(new BorderLayout());

		textArea = new RSyntaxTextArea();
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		// textArea.setText(
		// "Une première ligne\nUne autre ligne arrive ensuite\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\nUne
		// dernière ligne arrive à la fin\\nUne dernière ligne arrive à la fin\nUne dernière ligne arrive à la fin\nUne dernière ligne
		// arrive à la fin\n");
		textArea.setText(fmlResource.getLoadedResourceData().getFMLPrettyPrint());
		RTextScrollPane sp = new RTextScrollPane(textArea);
		((RSyntaxTextArea) sp.getTextArea()).setSyntaxEditingStyle("text/fml");
		add(sp, BorderLayout.CENTER);

		// Create a toolbar with searching options.
		textFinder = new TextFinder();
		JToolBar toolBar = new JToolBar();
		searchField = new JTextField(30);
		toolBar.add(searchField);
		final JButton nextButton = new JButton("Find Next");
		nextButton.setActionCommand("FindNext");
		nextButton.addActionListener(textFinder);
		toolBar.add(nextButton);
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});
		JButton prevButton = new JButton("Find Previous");
		prevButton.setActionCommand("FindPrev");
		prevButton.addActionListener(textFinder);
		toolBar.add(prevButton);
		regexCB = new JCheckBox("Regex");
		toolBar.add(regexCB);
		matchCaseCB = new JCheckBox("Match Case");
		toolBar.add(matchCaseCB);
		add(toolBar, BorderLayout.NORTH);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		add(errorStrip, BorderLayout.LINE_END);

		Parser p = new OnTheFlyFMLParser();// new XmlParser();
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

		/*setContentPane(cp);
		setTitle("Find and Replace Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);*/

	}

	public class TextFinder implements ActionListener {
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
				JOptionPane.showMessageDialog(FMLEditor.this, "Text not found");
			}

		}

	}
}
