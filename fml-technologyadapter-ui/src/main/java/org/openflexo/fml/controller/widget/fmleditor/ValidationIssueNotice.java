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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.pamela.validation.InformationIssue;
import org.openflexo.pamela.validation.ProblemIssue;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationWarning;

/**
 * A {@link FMLNotice} wrapping a {@link ValidationIssue}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class ValidationIssueNotice extends FMLNotice {

	static final Logger logger = Logger.getLogger(ValidationIssueNotice.class.getPackage().getName());

	private ValidationIssue<?, ?> issue;

	public ValidationIssueNotice(FMLEditorParser parser, ValidationIssue<?, ?> issue) {
		super(parser, issue.getValidationReport().getValidationModel().localizedIssueMessage(issue), -1, -1, -1);

		/*ValidationModel validationModel = issue.getValidationReport().getValidationModel();
		String localizedIssueMessage = validationModel.localizedIssueMessage(issue);
		
		System.out.println("Message: " + issue.getMessage());
		System.out.println("Message: " + localizedIssueMessage);*/

		this.issue = issue;
		if (issue instanceof InformationIssue) {
			setLevel(Level.INFO);
		}
		else if (issue instanceof ValidationWarning) {
			setLevel(Level.WARNING);
		}
		if (issue instanceof ValidationError) {
			setLevel(Level.ERROR);
		}

	}

	@Override
	public boolean isFixable() {
		return (issue instanceof ProblemIssue) && ((ProblemIssue) issue).isFixable();
	}

	@Override
	public int getLine() {
		if (issue != null && issue.getValidable() instanceof FMLPrettyPrintable) {
			RawSourcePosition startLocation = ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getStartLocation();
			if (startLocation != null) {
				return startLocation.getLine();
			}
		}
		return super.getLine();
	}

	@Override
	public int getOffset() {
		if (issue != null && issue.getValidable() instanceof FMLPrettyPrintable) {
			RawSourcePosition startLocation = ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getStartLocation();
			if (startLocation != null) {
				return startLocation.getPos();
			}
		}
		return super.getOffset();
	}

	@Override
	public int getLength() {
		if (issue != null && issue.getValidable() instanceof FMLPrettyPrintable) {
			RawSourceFragment fragment = ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getFragment();
			if (fragment != null) {
				return fragment.getLength();
			}
		}
		return super.getLength();
	}
}
