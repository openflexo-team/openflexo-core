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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.Deflater;

import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.GitHubException;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.br.github.model.GitHubResult;
import org.openflexo.foundation.BugReportService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.toolbox.ZipUtils;

/**
 * Service responsible for fetching GitHub repositories and submitting bug reports.
 *
 * The generic submission logic (body building, log upload, project zip, file upload) lives here. Subclasses may override
 * {@link #collectScreenshots(SubmitIssueReport)} to provide UI-captured screenshots — the default implementation returns an empty list.
 */
public abstract class BugReportServiceImpl extends FlexoServiceImpl implements BugReportService {

	private List<GitHubRepository> repositories;

	public BugReportServiceImpl() {
	}

	@Override
	public String getServiceName() {
		return "BugReportService";
	}

	// -----------------------------------------------------------------------
	// Repository management
	// -----------------------------------------------------------------------

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

	// -----------------------------------------------------------------------
	// Abstract / hook methods for subclasses
	// -----------------------------------------------------------------------

	/** Returns the stored GitHub personal access token, without prompting. */
	protected abstract String getStoredToken();

	/**
	 * Ensures a valid GitHub token is available, prompting the user if needed. Returns the token, or null if the user cancels.
	 */
	public abstract String askTokenWhenRequired();

	/**
	 * Captures screenshots to return as files to attach to the issue. The default implementation returns an empty list. UI subclasses
	 * override this to capture AWT windows.
	 */
	protected List<File> collectScreenshots(SubmitIssueReport report) {
		return Collections.emptyList();
	}

	// -----------------------------------------------------------------------
	// Initialization
	// -----------------------------------------------------------------------

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

	// -----------------------------------------------------------------------
	// Bug report submission
	// -----------------------------------------------------------------------

	/**
	 * Submits a bug report to GitHub. Collects screenshots via {@link #collectScreenshots}, builds the issue body, creates the issue, then
	 * uploads binary attachments and updates the issue body with their URLs.
	 *
	 * @throws IOException
	 *             on network or I/O error
	 * @throws GitHubException
	 *             on GitHub API error
	 */
	public SubmitIssueReport submitIssue(BugReportSubmission submission) throws IOException, GitHubException {
		SubmitIssueReport report = new SubmitIssueReport();

		List<File> screenshotFiles = new ArrayList<>();
		if (submission.isSendScreenshots()) {
			screenshotFiles = collectScreenshots(report);
		}

		File projectZipFile = null;
		if (submission.isSendProject() && submission.getProjectDirectory() != null) {
			projectZipFile = buildProjectZip(submission, report);
		}

		GitHubClient client = new GitHubClient(getStoredToken());

		Progress.progress(localized("creating_issue"));
		String body = buildIssueBody(client, submission, report);
		submission.getIssue().setBody(body);

		if (submission.getMilestone() != null) {
			submission.getIssue().setMilestone(submission.getMilestone().getNumber());
		}
		submission.getIssue().setLabels(Collections.singletonList("bug"));

		GitHubResult result = client.createIssue(submission.getRepository(), submission.getIssue());
		if (result == null || !result.isSuccess()) {
			report.addToErrors(localized("could_not_send_bug_report"));
			return report;
		}

		StringBuilder appendix = new StringBuilder();

		if (!screenshotFiles.isEmpty()) {
			Progress.progress(localized("sending_screenshots"));
			StringBuilder section = new StringBuilder("\n\n## Screenshots\n\n");
			boolean any = false;
			for (File f : screenshotFiles) {
				try {
					String rawUrl = client.uploadFileToRepo(submission.getRepository().getName(), "bug-report-screenshots", f.getName(),
							Files.readAllBytes(f.toPath()));
					if (rawUrl != null) {
						section.append("![").append(f.getName()).append("](").append(rawUrl).append(")\n\n");
						any = true;
					}
				} catch (Exception e) {
					report.addToWarning(localized("could_not_attach_screenshot") + " " + f.getName() + "\n\t" + e.getMessage());
					logger.log(Level.WARNING, "Could not upload screenshot: " + f.getName(), e);
				}
			}
			if (any) {
				appendix.append(section);
			}
		}

		if (projectZipFile != null && projectZipFile.exists()) {
			Progress.progress(localized("sending_project"));
			try {
				String rawUrl = client.uploadFileToRepo(submission.getRepository().getName(), "bug-report-archives",
						projectZipFile.getName(), Files.readAllBytes(projectZipFile.toPath()));
				if (rawUrl != null) {
					appendix.append("\n\n## Project Archive\n\n[").append(projectZipFile.getName()).append("](").append(rawUrl)
							.append(")\n\n");
				}
			} catch (Exception e) {
				report.addToWarning(localized("could_not_zip_project") + " " + projectZipFile.getName() + "\n\t" + e.getMessage());
				logger.log(Level.WARNING, "Could not upload project archive: " + projectZipFile.getName(), e);
			}
		}

		if (appendix.length() > 0) {
			client.updateIssueBody(submission.getRepository().getName(), result.getNumber(), body + appendix);
		}

		report.setIssueLink(result.getHtmlUrl());
		return report;
	}

	// -----------------------------------------------------------------------
	// Private helpers
	// -----------------------------------------------------------------------

	private String buildIssueBody(GitHubClient client, BugReportSubmission submission, SubmitIssueReport report) {
		StringBuilder body = new StringBuilder();

		if (StringUtils.isNotEmpty(submission.getIssue().getDescription())) {
			body.append("## Description\n\n").append(submission.getIssue().getDescription()).append("\n\n");
		}

		String buildInfo = "- Build: `" + submission.getBuildId() + "`\n- Commit: `" + submission.getCommitId() + "`";
		if (submission.isSendSystemProperties()) {
			buildInfo += "\n\n```\n" + ToolBox.getSystemProperties(true) + "\n```";
		}
		body.append("## Environment\n\n").append(buildInfo).append("\n\n");

		if (StringUtils.isNotEmpty(submission.getIssue().getStacktrace())) {
			body.append("## Stack Trace\n\n```\n").append(submission.getIssue().getStacktrace()).append("\n```\n\n");
		}

		if (submission.isSendLogs()) {
			File logFile = submission.getLogFile();
			if (logFile != null && logFile.exists()) {
				Progress.progress(localized("sending_logs"));
				try {
					String logContent = readFileWithTruncation(logFile, 512 * 1024);
					String gistUrl = client.createGist("OpenFlexo error log", logFile.getName(), logContent);
					if (gistUrl != null) {
						body.append("## Log File\n\n").append(gistUrl).append("\n\n");
					}
				} catch (Exception e) {
					report.addToWarning(localized("could_not_attach_file") + " " + logFile.getName() + "\n\t" + e.getMessage());
				}
			}
		}

		if (submission.getAttachFile() != null && submission.getAttachFile().exists()) {
			File attachFile = submission.getAttachFile();
			Progress.progress(localized("sending_file") + " " + attachFile.getName());
			try {
				String content = readFileWithTruncation(attachFile, 512 * 1024);
				String gistUrl = client.createGist("Attached: " + attachFile.getName(), attachFile.getName(), content);
				body.append("## Attached File\n\n").append(gistUrl != null ? gistUrl : attachFile.getAbsolutePath()).append("\n\n");
			} catch (Exception e) {
				body.append("## Attached File\n\nLocal path: ").append(attachFile.getAbsolutePath()).append("\n\n");
			}
		}

		return body.toString();
	}

	private File buildProjectZip(BugReportSubmission submission, SubmitIssueReport report) {
		Progress.progress(localized("compressing_project"));
		File projectDir = submission.getProjectDirectory();
		String dirName = projectDir.getName();
		String zipName = dirName.endsWith(".prj") ? dirName.substring(0, dirName.length() - 4) + ".zip" : dirName + ".zip";
		File zipFile = new File(System.getProperty("java.io.tmpdir"), zipName);
		try {
			ZipUtils.makeZip(zipFile, projectDir, f -> !f.getName().endsWith("~"), Deflater.BEST_COMPRESSION);
			return zipFile;
		} catch (IOException e) {
			report.addToWarning(localized("could_not_zip_project") + " " + e.getMessage());
			return null;
		}
	}

	private static String readFileWithTruncation(File file, int maxBytes) throws IOException {
		long length = file.length();
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] buf;
			if (length <= maxBytes) {
				buf = new byte[(int) length];
				fis.read(buf);
			}
			else {
				long skip = length - maxBytes;
				fis.skip(skip);
				buf = new byte[maxBytes];
				fis.read(buf);
			}
			return new String(buf, "UTF-8");
		}
	}

	private String localized(String key) {
		if (FlexoLocalization.getMainLocalizer() != null) {
			return FlexoLocalization.getMainLocalizer().localizedForKey(key);
		}
		return key;
	}
}
