/**
 *
 * Copyright (c) 2014, Openflexo
 *
 * This file is part of Flexo-ui, a component of the software infrastructure
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

package org.openflexo.br;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.GitHubException;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.foundation.BugReportService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;

/**
 * Service responsible for fetching GitHub repositories from the openflexo-team organization and providing them for bug report submission.
 *
 * Replaces the former JIRA-based implementation.
 */
public abstract class BugReportServiceImpl extends FlexoServiceImpl implements BugReportService {

	private List<GitHubRepository> repositories;

	public BugReportServiceImpl() {
	}

	@Override
	public String getServiceName() {
		return "BugReportService";
	}

	public List<GitHubRepository> getRepositories() {
		return repositories;
	}

	public GitHubRepository getRepositoryByName(String name) {
		if (repositories == null || name == null) {
			return null;
		}
		for (GitHubRepository repo : repositories) {
			if (name.equals(repo.getName())) {
				return repo;
			}
		}
		return null;
	}

	public boolean isInitialized() {
		return status == Status.Started;
	}

	public abstract String askTokenWhenRequired();

	@Override
	public void initialize() {
		logger.info("Initializing BugReportService (GitHub)");

		repositories = new ArrayList<>();

		String token = askTokenWhenRequired();
		if (token == null) {
			logger.warning("No GitHub token provided — BugReportService initialization aborted");
			status = Status.Started;
			return;
		}

		GitHubClient client = new GitHubClient(token);

		try {
			if (FlexoLocalization.getMainLocalizer() != null) {
				Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("contacting") + " GitHub");
			}

			List<GitHubRepository> fetched = client.listRepositories();
			if (fetched != null) {
				repositories.addAll(fetched);
			}
			logger.info("Loaded " + repositories.size() + " repositories from " + GitHubClient.ORG);

		} catch (IOException e) {
			logger.warning("Network error while fetching GitHub repositories: " + e.getMessage());
		} catch (GitHubException e) {
			logger.warning("GitHub API error while fetching repositories: " + e.getMessage());
		}

		status = Status.Started;
	}
}
