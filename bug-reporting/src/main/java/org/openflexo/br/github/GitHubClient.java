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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.br.github.model.GitHubIssue;
import org.openflexo.br.github.model.GitHubLabel;
import org.openflexo.br.github.model.GitHubMilestone;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.br.github.model.GitHubResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * HTTP client for the GitHub REST API v3.
 *
 * Replaces JIRAClient. Authenticates via Personal Access Token (Bearer).
 * Targets the openflexo-team organization for repository listings and issue creation.
 */
public class GitHubClient {

	private static final Logger logger = Logger.getLogger(GitHubClient.class.getPackage().getName());

	public static final String API_BASE = "https://api.github.com";
	public static final String ORG = "openflexo-team";
	public static final String AUTH_HEADER = "Authorization";

	private final String token;
	private int timeout;
	private final Gson gson;

	public GitHubClient(String token) {
		this.token = token;
		this.timeout = 30_000;
		this.gson = new GsonBuilder().create();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	// -----------------------------------------------------------------------
	// Connection helpers
	// -----------------------------------------------------------------------

	private HttpURLConnection openConnection(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
		conn.setRequestProperty(AUTH_HEADER, "Bearer " + token);
		conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
		conn.setRequestProperty("User-Agent", "OpenFlexo-BugReport/1.0");
		return conn;
	}

	private String readStream(InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int read;
		while ((read = bis.read(buf)) > 0) {
			baos.write(buf, 0, read);
		}
		return baos.toString("UTF-8");
	}

	private void checkStatus(HttpURLConnection conn) throws IOException, GitHubException {
		int status = conn.getResponseCode();
		if (status == 401 || status == 403) {
			throw new UnauthorizedGitHubAccessException();
		}
		if (status >= 400) {
			InputStream err = conn.getErrorStream();
			String body = err != null ? readStream(err) : "(no body)";
			GitHubResult errorResult = gson.fromJson(body, GitHubResult.class);
			String msg = (errorResult != null && errorResult.getMessage() != null)
					? errorResult.getMessage()
					: body;
			throw new GitHubException(msg, status);
		}
	}

	private <T> T get(String url, java.lang.reflect.Type type) throws IOException, GitHubException {
		HttpURLConnection conn = openConnection(url);
		conn.setRequestMethod("GET");
		checkStatus(conn);
		String body = readStream(conn.getInputStream());
		return gson.fromJson(body, type);
	}

	private <T> T post(String url, Object requestBody, Class<T> responseType) throws IOException, GitHubException {
		HttpURLConnection conn = openConnection(url);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		byte[] jsonBytes = gson.toJson(requestBody).getBytes("UTF-8");
		conn.setFixedLengthStreamingMode(jsonBytes.length);
		conn.connect();
		OutputStream os = conn.getOutputStream();
		os.write(jsonBytes);
		os.flush();
		checkStatus(conn);
		String body = readStream(conn.getInputStream());
		return gson.fromJson(body, responseType);
	}

	// -----------------------------------------------------------------------
	// Public API methods
	// -----------------------------------------------------------------------

	/**
	 * Tests whether the token is valid by calling GET /user.
	 */
	public boolean testConnection() {
		try {
			HttpURLConnection conn = openConnection(API_BASE + "/user");
			conn.setRequestMethod("GET");
			return conn.getResponseCode() == 200;
		} catch (Exception e) {
			logger.warning("GitHub connection test failed: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Lists all public, non-archived repositories of the openflexo-team organization.
	 * Fetches up to 100 repos per page (sufficient for openflexo-team).
	 */
	public List<GitHubRepository> listRepositories() throws IOException, GitHubException {
		String url = API_BASE + "/orgs/" + ORG + "/repos?type=public&per_page=100&sort=name";
		java.lang.reflect.Type listType = new TypeToken<List<GitHubRepository>>() {
		}.getType();
		List<GitHubRepository> repos = get(url, listType);
		if (repos != null) {
			repos.removeIf(r -> r.isArchived() || r.isFork());
		}
		return repos;
	}

	/**
	 * Lists open milestones for the given repository.
	 */
	public List<GitHubMilestone> listMilestones(GitHubRepository repo) throws IOException, GitHubException {
		String url = API_BASE + "/repos/" + ORG + "/" + repo.getName() + "/milestones?state=open&per_page=100";
		java.lang.reflect.Type listType = new TypeToken<List<GitHubMilestone>>() {
		}.getType();
		return get(url, listType);
	}

	/**
	 * Lists all labels for the given repository.
	 */
	public List<GitHubLabel> listLabels(GitHubRepository repo) throws IOException, GitHubException {
		String url = API_BASE + "/repos/" + ORG + "/" + repo.getName() + "/labels?per_page=100";
		java.lang.reflect.Type listType = new TypeToken<List<GitHubLabel>>() {
		}.getType();
		return get(url, listType);
	}

	/**
	 * Creates an issue in the given repository.
	 * The issue's body must be set before calling this method.
	 */
	public GitHubResult createIssue(GitHubRepository repo, GitHubIssue issue) throws IOException, GitHubException {
		String url = API_BASE + "/repos/" + ORG + "/" + repo.getName() + "/issues";
		return post(url, issue, GitHubResult.class);
	}

	/**
	 * Creates a secret Gist with the given content and returns its html_url.
	 * Used for attaching log files and system properties to issues.
	 *
	 * @param description gist description
	 * @param filename    filename to use inside the gist
	 * @param content     text content of the file
	 * @return the URL of the created gist
	 */
	public String createGist(String description, String filename, String content) throws IOException, GitHubException {
		Map<String, Object> fileEntry = new HashMap<>();
		fileEntry.put("content", content);

		Map<String, Object> files = new HashMap<>();
		files.put(filename, fileEntry);

		Map<String, Object> gistBody = new HashMap<>();
		gistBody.put("description", description);
		gistBody.put("public", false);
		gistBody.put("files", files);

		GitHubResult result = post(API_BASE + "/gists", gistBody, GitHubResult.class);
		return result != null ? result.getHtmlUrl() : null;
	}
}
