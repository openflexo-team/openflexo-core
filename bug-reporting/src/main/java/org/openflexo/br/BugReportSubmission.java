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

import java.io.File;

import org.openflexo.br.github.model.GitHubIssue;
import org.openflexo.br.github.model.GitHubMilestone;
import org.openflexo.br.github.model.GitHubRepository;

/**
 * Data transfer object carrying all parameters needed to submit a bug report to GitHub.
 * Built by the UI layer (GitHubIssueReportDialog) and consumed by BugReportServiceImpl.
 */
public class BugReportSubmission {

	private GitHubIssue issue;
	private GitHubRepository repository;
	private GitHubMilestone milestone;

	private boolean sendLogs;
	private boolean sendScreenshots;
	private boolean sendSystemProperties;
	private boolean sendProject;

	private File logFile;
	private File attachFile;
	private File projectDirectory;

	private String buildId;
	private String commitId;

	public GitHubIssue getIssue() {
		return issue;
	}

	public void setIssue(GitHubIssue issue) {
		this.issue = issue;
	}

	public GitHubRepository getRepository() {
		return repository;
	}

	public void setRepository(GitHubRepository repository) {
		this.repository = repository;
	}

	public GitHubMilestone getMilestone() {
		return milestone;
	}

	public void setMilestone(GitHubMilestone milestone) {
		this.milestone = milestone;
	}

	public boolean isSendLogs() {
		return sendLogs;
	}

	public void setSendLogs(boolean sendLogs) {
		this.sendLogs = sendLogs;
	}

	public boolean isSendScreenshots() {
		return sendScreenshots;
	}

	public void setSendScreenshots(boolean sendScreenshots) {
		this.sendScreenshots = sendScreenshots;
	}

	public boolean isSendSystemProperties() {
		return sendSystemProperties;
	}

	public void setSendSystemProperties(boolean sendSystemProperties) {
		this.sendSystemProperties = sendSystemProperties;
	}

	public boolean isSendProject() {
		return sendProject;
	}

	public void setSendProject(boolean sendProject) {
		this.sendProject = sendProject;
	}

	public File getLogFile() {
		return logFile;
	}

	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

	public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(File projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
}
