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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.parser.FMLParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * An {@link AbstractParser} implementation for FML language
 * 
 * @author sylvain
 *
 */
public class FMLEditorParser extends AbstractParser {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	private final FMLEditor editor;
	private final FMLParser fmlParser;
	private DefaultParseResult result;

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
	 * Parse contents of editor, and update model accordingly
	 */
	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {

		if (fmlWillChange) {
			return result;
		}

		System.out.println("---------> Parsing FML document......");

		result.clearNotices();
		editor.getGutter().removeAllTrackingIcons();

		try {
			// Prevent editor from concurrent modification
			editor.modelWillChange();

			FMLCompilationUnit returned = getFMLParser().parse(editor.getTextArea().getText(), editor.getFactory());
			// System.out.println("Parsing succeeded");

			// This is the update process
			FMLCompilationUnit existingData = editor.getFMLResource().getCompilationUnit();
			existingData.updateWith(returned);

			// There is a trick here: the update has nullified the resource
			// (this is normal, just parsed FMLCompilationUnit does not knows the resource)
			// So set the resource again at the end of the update process
			existingData.setResource(editor.getFMLResource());
			// System.out.println("Check also: " + (editor.getFMLResource().getResourceData() == existingData));

			// Then we browse SemanticAnalysisIssue as raised by semantics analyzing
			for (SemanticAnalysisIssue semanticAnalysisIssue : existingData.getPrettyPrintDelegate().getSemanticAnalysisIssues()) {
				result.addNotice(new SemanticAnalyzerNotice(this, semanticAnalysisIssue));
			}

			// We finally perform a full validation to detect validation issues
			FMLValidationReport validationReport = validate(existingData);
			for (ValidationIssue<?, ?> validationIssue : validationReport.getAllIssues()) {
				result.addNotice(new ValidationIssueNotice(this, validationIssue));
			}

		} catch (ParseException e) {
			// Parse error: cannot do more than display position when parsing failed
			// TODO: handle errors during parsing
			result.addNotice(new ParseErrorNotice(this, e));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			editor.modelHasChanged();
		}

		result.setParsedLines(0, editor.getTextArea().getLineCount());

		for (ParserNotice parserNotice : result.getNotices()) {
			try {
				// System.out.println("Adding line " + parserNotice.getLine() + " message: " + parserNotice.getMessage());
				if (parserNotice.getLine() > 0) {
					editor.getGutter().addLineTrackingIcon(parserNotice.getLine() - 1, ((FMLNotice) parserNotice).getIcon());
				}
				else {
					logger.warning("Unexpected notice at line:" + parserNotice.getLength() + " " + parserNotice);
				}
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

	public FMLTechnologyAdapterController getFMLTechnologyAdapterController() {
		return editor.getFMLTechnologyAdapterController();
	}

	private FMLValidationReport validate(FMLCompilationUnit compilationUnit) {
		FMLValidationReport virtualModelReport = (FMLValidationReport) getFMLTechnologyAdapterController()
				.getValidationReport(compilationUnit);
		try {
			virtualModelReport.revalidate(compilationUnit);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return virtualModelReport;
	}

	private boolean fmlWillChange = false;

	protected void fmlWillChange() {
		fmlWillChange = true;
	}

	protected void fmlHasChanged() {
		fmlWillChange = false;
	}

}