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

import javax.swing.Icon;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.FragmentContext;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.icon.IconLibrary;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.pamela.validation.ConsistencySuccessfullyChecked;
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

	private final ValidationIssue<?, ?> issue;

	public ValidationIssueNotice(FMLEditorParser parser, ValidationIssue<?, ?> issue) {
		super(parser, issue.getValidationReport().getValidationModel().localizedIssueMessage(issue), -1, -1, -1);

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

	public ValidationIssue<?, ?> getIssue() {
		return issue;
	}

	@Override
	public Icon getIcon() {
		if (issue instanceof ConsistencySuccessfullyChecked) {
			return IconLibrary.VALID_ICON;
		}
		return super.getIcon();
	}

	@Override
	public boolean isFixable() {
		return (issue instanceof ProblemIssue) && ((ProblemIssue) issue).isFixable();
	}

	public RawSourceFragment getFragment() {
		if (issue != null && issue.getCause() != null && issue.getValidable() instanceof FMLPrettyPrintable
				&& ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate() != null) {
			if (StringUtils.isNotEmpty(issue.getCause().getFragmentContext())) {
				FragmentContext context = FragmentContext.valueOf(issue.getCause().getFragmentContext());
				if (context != null) {
					return ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getFragment(context);
				}
			}
			return ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getFragment();
		}
		return null;
	}

	@Override
	public int getLine() {
		if (issue instanceof InformationIssue) {
			return 1;
		}
		if (getFragment() != null) {
			// System.out.println("For " + issue.getMessage() + " line: " + getFragment().getStartPosition().getLine());
			return getFragment().getStartPosition().getLine();
		}
		return -1;
	}

	@Override
	public int getOffset() {
		if (getFragment() != null) {
			// System.out.println("For " + issue.getMessage() + " offset: " + getFragment().getStartPosition().getOffset());
			return getFragment().getStartPosition().getOffset();
		}
		return super.getOffset();
	}

	@Override
	public int getLength() {
		if (getFragment() != null) {
			// System.out.println("For " + issue.getMessage() + " length: " + getFragment().getLength());
			return getFragment().getLength();
		}
		return super.getLength();
	}
}
