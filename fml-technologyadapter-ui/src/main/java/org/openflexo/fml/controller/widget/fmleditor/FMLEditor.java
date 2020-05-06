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

package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.FMLParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;

/**
 * Widget allowing to edit a FML virtual model
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLEditor extends JPanel implements DocumentListener {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/fml", "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	private final CompilationUnitResource fmlResource;
	private final FMLParser fmlParser;

	private RSyntaxTextArea textArea;
	private Gutter gutter;
	private TextFinderPanel finderToolbar;

	public FMLEditor(CompilationUnitResource fmlResource) {
		super(new BorderLayout());

		fmlParser = new FMLParser();

		this.fmlResource = fmlResource;

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

		finderToolbar = new TextFinderPanel(this);
		add(finderToolbar, BorderLayout.SOUTH);

		gutter = sp.getGutter();
		// gutter.setBookmarkIcon(new ImageIcon("bookmark.png"));
		// gutter.setBookmarkIcon(IconLibrary.FIXABLE_ERROR_ICON);
		gutter.setBookmarkingEnabled(true);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		add(errorStrip, BorderLayout.LINE_END);

		Parser p = new OnTheFlyFMLParser(this);// new XmlParser();
		textArea.addParser(p);
		// p.parse(textArea.getD, style)

		/*try {
			gutter.addLineTrackingIcon(0, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(1, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(3, IconLibrary.FIXABLE_ERROR_ICON);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/

		// textArea.getDocument().addDocumentListener(this);

		// RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
		// String style = textArea.getSyntaxEditingStyle();
		// ParseResult res = p.parse(doc, style);
		// System.out.println("res=" + res.getNotices());

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

	public Gutter getGutter() {
		return gutter;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		System.out.println("insertUpdate with " + e);
		parse();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		System.out.println("removeUpdate with " + e);
		parse();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		System.out.println("changedUpdate with " + e);
		parse();
	}

	public FMLModelFactory getFactory() {
		return fmlResource.getFactory();
	}

	public FMLParser getFMLParser() {
		return fmlParser;
	}

	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	public void parse() {
		System.out.println("On parse");
		List<Issue> issues = new ArrayList<>();
		try {
			FMLCompilationUnit returned = getFMLParser().parse(textArea.getText(), getFactory());
			System.out.println("OK c'est bien parse !!!");
			updateWithIssues(issues);
			return;
		} catch (ParseException e) {
			System.out.println("Le parsing a foire...");
			issues.add(new Issue("Syntax error", e.getLine(), e.getPosition()));
			updateWithIssues(issues);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateWithIssues(List<Issue> issues) {

	}

	public static class Issue {
		private String message;
		private int line;
		private int position;

		public Issue(String message, int line, int position) {
			super();
			this.message = message;
			this.line = line;
			this.position = position;
		}
	}

}
