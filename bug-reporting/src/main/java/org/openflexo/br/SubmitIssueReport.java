/**
 *
 * Copyright (c) 2013-2014, Openflexo
 *
 * This file is part of Flexo-ui, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.br;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.toolbox.ToolBox;

/**
 * Result of a bug report submission. Carries the issue URL, errors, and warnings.
 * Used as data model for the GitHubSubmitIssueReportDialog FIB.
 */
public class SubmitIssueReport {

	private String issueLink;
	private final List<String> errors = new ArrayList<>();
	private final List<String> warnings = new ArrayList<>();

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public String getIssueLink() {
		return issueLink;
	}

	public void setIssueLink(String issueLink) {
		this.issueLink = issueLink;
	}

	public List<String> getErrors() {
		return errors;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void addToErrors(String error) {
		errors.add(error);
	}

	public void addToWarning(String warning) {
		warnings.add(warning);
	}

	public String errorsToString() {
		return String.join("\n", errors);
	}

	public String warningsToString() {
		return String.join("\n", warnings);
	}

	public String issueLinkHyperlink() {
		return "<html><a href=\"" + issueLink + "\">" + issueLink + "</a></html>";
	}

	public void openIssueLink() {
		ToolBox.openURL(issueLink);
	}
}
