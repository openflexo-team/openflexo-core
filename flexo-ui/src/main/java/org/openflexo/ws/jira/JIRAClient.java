/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.ws.jira;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.FileTypeMap;

import org.apache.commons.codec.binary.Base64;
import org.openflexo.ws.jira.action.JIRAAction;
import org.openflexo.ws.jira.model.JIRAErrors;
import org.openflexo.ws.jira.model.JIRAIssue;
import org.openflexo.ws.jira.result.JIRAResult;

import com.google.gson.JsonSyntaxException;

public class JIRAClient {

	public enum Method {
		GET, POST, PUT, DELETE;
	}

	private static final String REST_API_ROOT = "/rest/api/2";

	private static final String SET_COOKIE_HEADER = "Set-Cookie"; // list of name=value;
	private static final String COOKIE_HEADER = "Cookie"; // list of name=value;
	public static final String BASIC_AUTH_HEADER = "Authorization";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String X_ATLASSIAN_TOKEN_HEADER = "X-Atlassian-Token";

	private static final String SUBMIT_ISSUE_REST_API = "/issue";
	private static final String SUBMIT_ISSUE_ATTACHMENT_REST_API = "/attachments";

	private static final String TWO_HYPHENS = "--";
	private static final String BOUNDARY = "cestquoicebazaryeuxbraguette";
	private static final String SUB_BOUNDARY = "chaudecommeunebarraqueafrite";

	private static final String CR_LF = "\r\n";

	private final URL jiraBaseURL;
	private final String username;
	private final String password;

	private int timeout;

	public JIRAClient(String jiraBaseURL, String username, String password) throws MalformedURLException {
		super();
		if (!jiraBaseURL.toLowerCase().startsWith("http://") && !jiraBaseURL.toLowerCase().startsWith("https://")) {
			throw new MalformedURLException("The JIRA Client only supports http or https protocols");
		}
		this.timeout = 30 * 1000;
		this.jiraBaseURL = new URL(jiraBaseURL);
		this.username = username;
		this.password = password;
	}

	public <A extends JIRAAction<R>, R extends JIRAResult> R submit(A submit, Method method) throws IOException, JIRAException {
		URL url = new URL(jiraBaseURL, REST_API_ROOT + SUBMIT_ISSUE_REST_API);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setChunkedStreamingMode(4096);
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setDoOutput(true);
		connection.setRequestMethod(method.name());
		connection.addRequestProperty(BASIC_AUTH_HEADER, "Basic " + getBase64EncodedAuthentication());
		connection.addRequestProperty(CONTENT_TYPE_HEADER, "application/json");
		connection.connect();
		String json = JIRAGson.getInstance().toJson(submit);
		byte[] bytes = json.getBytes("UTF-8");
		for (int i = 0; i < bytes.length;) {
			connection.getOutputStream().write(bytes, i, Math.min(4096, bytes.length - i));
			i += 4096;
			/*if (progress != null) {
				progress.setProgress((double) i / bytes.length);
			}*/
		}
		switch (connection.getResponseCode()) {
			case 401:
			case 403:
				throw new UnauthorizedJIRAAccessException();
		}
		InputStream is;
		boolean isErrorStatus = connection.getResponseCode() > 399;
		if (isErrorStatus) {
			is = new BufferedInputStream(connection.getErrorStream());
		}
		else {
			is = new BufferedInputStream(connection.getInputStream());
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			byte[] b = new byte[4096];
			while ((read = is.read(b)) > 0) {
				baos.write(b, 0, read);
			}
			String json2 = new String(baos.toByteArray(), "UTF-8");
			if (isErrorStatus) {
				try {
					JIRAErrors errors = JIRAGson.getInstance().fromJson(json2, JIRAErrors.class);
					throw new JIRAException(errors);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					throw new IOException(json2 + "\n(Status: " + connection.getResponseCode() + ")");
				}
			}
			return JIRAGson.getInstance().fromJson(json2, submit.getResultClass());
		} finally {
			is.close();
		}
	}

	public void attachFilesToIssue(JIRAIssue issue, File file) throws IOException {
		attachFilesToIssue(issue, new File[] { file });
	}

	public void attachFilesToIssue(JIRAIssue issue, File... files) throws IOException {
		String idOrKey = issue.getId() != null ? issue.getId() : issue.getKey();
		if (idOrKey == null) {
			throw new NullPointerException("Issue has no id nor key");
		}
		URL url = new URL(jiraBaseURL, REST_API_ROOT + SUBMIT_ISSUE_REST_API + "/" + idOrKey + SUBMIT_ISSUE_ATTACHMENT_REST_API);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setChunkedStreamingMode(4096);
		connection.setConnectTimeout(timeout);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod(Method.POST.name());
		connection.addRequestProperty(BASIC_AUTH_HEADER, "Basic " + getBase64EncodedAuthentication());
		connection.addRequestProperty(X_ATLASSIAN_TOKEN_HEADER, "nocheck");
		connection.addRequestProperty(CONTENT_TYPE_HEADER, "multipart/form-data; boundary=" + BOUNDARY);
		connection.addRequestProperty("Connection", "Keep-Alive");
		// connection.addRequestProperty("Content-Length", String.valueOf(SINGLE_FILE_OVERHEAD + files[0].length()));
		connection.connect();
		boolean isMultipleFiles = files.length > 1;
		UTF8OutputStream os = new UTF8OutputStream(connection.getOutputStream());
		writeBoundary(os, BOUNDARY).write(CR_LF);

		os.write("content-disposition: form-data; name=\"file\""); // Attachment is linked to a field name 'file'
		if (isMultipleFiles) {
			os.write(CR_LF);
			os.write("Content-type: multipart/mixed, boundary=").write(SUB_BOUNDARY).write(CR_LF).write(CR_LF);
			writeBoundary(os, SUB_BOUNDARY).write(CR_LF);
		}
		else {
			os.write("; filename=\"" + files[0].getName() + "\"").write(CR_LF);
			writeContentType(os, files[0]);
			os.write(CR_LF); // Close part header
		}
		// Unused long count = 0;
		// Unused long total = 0;

		// Unused for (File file : files) {
		// Unused total += file.length();
		// Unused }
		for (File file : files) {
			if (isMultipleFiles) {
				os.write("Content-disposition: attachment; filename=\"").write(file.getName()).write("\"").write(CR_LF);
				writeContentType(os, file);
				os.write(CR_LF);// Close part header
			}
			long length = file.length();// This is necessary if we are sending a log file and that more logs are getting appended.
			InputStream is = new FileInputStream(file);
			try {
				int read;
				byte[] b = new byte[8192];
				while ((read = is.read(b)) > 0 && length > 0) {
					os.write(b, 0, (int) Math.min(read, length));
					length -= read;
					// Unused count += read;
					/*if (progress != null) {
						progress.setProgress((double) count / total);
					}*/
				}
				if (isMultipleFiles) {
					os.write(CR_LF);
					writeBoundary(os, SUB_BOUNDARY).write(TWO_HYPHENS).write(CR_LF);
				}
			} finally {
				is.close();
			}
		}
		// End of transfer
		os.write(CR_LF);
		writeBoundary(os, BOUNDARY).write(TWO_HYPHENS).write(CR_LF).write(CR_LF);
		InputStream is = new BufferedInputStream(connection.getInputStream());
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			while ((read = is.read()) > -1) {
				baos.write(read);
			}
			// System.err.println(new String(baos.toByteArray(), "UTF-8"));
		} finally {
			is.close();
		}
	}

	private void writeContentType(UTF8OutputStream os, File file) throws UnsupportedEncodingException, IOException {
		String type = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
		if (type == null) {
			type = "attachment/octet-stream";
		}
		os.write("Content-Type: ").write(type).write(CR_LF);
	}

	public UTF8OutputStream writeBoundary(UTF8OutputStream os, String boundary) throws IOException, UnsupportedEncodingException {
		os.write(TWO_HYPHENS);
		os.write(boundary);
		return os;
	}

	private String getBase64EncodedAuthentication() throws UnsupportedEncodingException {
		// Ok, it took me a while to find out but ISO-8859-1 is the one used by JIRA
		return Base64.encodeBase64String((username + ":" + password).getBytes("ISO-8859-1"));
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int connectionTimeout) {
		this.timeout = connectionTimeout;
	}

	public static class UTF8OutputStream extends OutputStream {

		private final OutputStream os;

		protected UTF8OutputStream(OutputStream os) {
			super();
			this.os = os;
		}

		@Override
		public void write(int b) throws IOException {
			os.write(b);
		}

		public UTF8OutputStream write(String string) throws UnsupportedEncodingException, IOException {
			write(string.getBytes("UTF-8"));
			return this;
		}

	}
}
