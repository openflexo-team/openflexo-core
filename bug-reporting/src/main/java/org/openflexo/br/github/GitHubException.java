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

package org.openflexo.br.github;

/**
 * Exception thrown when the GitHub API returns an error response.
 */
public class GitHubException extends Exception {

	private final int statusCode;
	private final String githubMessage;

	public GitHubException(String githubMessage, int statusCode) {
		super(githubMessage + " (HTTP " + statusCode + ")");
		this.githubMessage = githubMessage;
		this.statusCode = statusCode;
	}

	public String getGitHubMessage() {
		return githubMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
