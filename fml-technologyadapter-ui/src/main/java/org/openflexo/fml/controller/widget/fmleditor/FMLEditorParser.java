/**
 * 
 * Copyright (c) 2020, Openflexo
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.parser.FMLParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;

/**
 * An {@link AbstractParser} implementation for FML language
 * 
 * @author sylvain
 *
 */
public class FMLEditorParser extends AbstractParser {

	private final FMLEditor editor;

	private final FMLParser fmlParser;

	// private SAXParserFactory spf;
	private DefaultParseResult result;
	// private EntityResolver entityResolver;

	public FMLEditorParser(FMLEditor editor) {
		this.editor = editor;
		fmlParser = new FMLParser();
		result = new DefaultParseResult(this);
	}

	public FMLParser getFMLParser() {
		return fmlParser;
	}

	public CompilationUnitResource getFMLResource() {
		return editor.getFMLResource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {

		System.out.println("---------> tiens, je reparse mon document......");

		result.clearNotices();
		editor.getGutter().removeAllTrackingIcons();

		try {
			FMLCompilationUnit returned = getFMLParser().parse(editor.getTextArea().getText(), editor.getFactory());
			System.out.println("OK c'est bien parse !!!");
			FMLCompilationUnit existingData = editor.getFMLResource().getCompilationUnit();
			existingData.updateWith(returned);

		} catch (ParseException e) {
			System.out.println("Le parsing a foire...");
			// issues.add(new Issue("Syntax error", e.getLine(), e.getPosition()));
			// updateWithIssues(issues);
			result.addNotice(new ParseErrorNotice(this, e));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Element root = doc.getDefaultRootElement();
		result.setParsedLines(0, editor.getTextArea().getLineCount());

		for (ParserNotice parserNotice : result.getNotices()) {
			try {
				System.out.println("On ajoute line " + parserNotice.getLine() + " message: " + parserNotice.getMessage());
				editor.getGutter().addLineTrackingIcon(parserNotice.getLine() - 1, ((FMLNotice) parserNotice).getIcon());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		return result;

	}

	public List<ParserNotice> getParserNotices(int line) {
		List<ParserNotice> returned = new ArrayList<>();
		for (ParserNotice parserNotice : result.getNotices()) {
			if (parserNotice.getLine() == line) {
				returned.add(parserNotice);
			}
		}
		return returned;
	}

}
