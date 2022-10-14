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

import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.ParseError;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.UnexpectedExceptionError;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.pamela.validation.ValidationError;

/**
 * FML implementation for {@link ParseResult}
 * 
 * @author sylvain
 *
 */
public class FMLParseResult extends DefaultParseResult {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	private FMLValidationReport validationReport;

	private List<ValidationError> errors = new ArrayList<>();

	public FMLParseResult(FMLEditorParser parser) {
		super(parser);
	}

	public FMLValidationReport getValidationReport() {
		return validationReport;
	}

	public void setValidationReport(FMLValidationReport validationReport) {
		this.validationReport = validationReport;
	}

	/*@Override
	public void addNotice(ParserNotice notice) {
		// System.out.println(">>>>>>>>>> addNotice " + notice + " at line " + notice.getLine());
		if (notice instanceof ValidationIssueNotice) {
			validationReport.setLineNumber(((ValidationIssueNotice) notice).getIssue(), notice.getLine());
		}
		super.addNotice(notice);
	}*/

	public void addSemanticAnalysisIssue(SemanticAnalysisIssue<?, ?> issue) {
		validationReport.appendSemanticAnalysisIssue(issue);
		errors.add(issue);
	}

	public void addParseError(ParseException e) {
		ParseError parseError = new ParseError(validationReport.getCompilationUnit(), e.getMessage(), e.getLine());
		validationReport.appendValidationError(parseError, e.getLine());
		errors.add(parseError);
	}

	public void addUnexpectedException(Exception e) {
		UnexpectedExceptionError unexpectedExceptionError = new UnexpectedExceptionError(validationReport.getCompilationUnit(), e);
		validationReport.appendValidationError(unexpectedExceptionError, 0);
		errors.add(unexpectedExceptionError);
	}

	@Override
	public void clearNotices() {
		super.clearNotices();
		for (ValidationError validationError : errors) {
			validationReport.removeValidationError(validationError);
		}
	}
}
