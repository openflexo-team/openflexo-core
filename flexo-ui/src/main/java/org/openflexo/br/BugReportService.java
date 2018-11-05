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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.openflexo.ApplicationContext;
import org.openflexo.br.view.JIRAURLCredentialsDialog;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.FlexoModule;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.ws.jira.JIRAClient;
import org.openflexo.ws.jira.JIRAGson;
import org.openflexo.ws.jira.model.JIRAComponent;
import org.openflexo.ws.jira.model.JIRAProject;
import org.openflexo.ws.jira.model.JIRAProjectList;

public class BugReportService extends FlexoServiceImpl {

	private static final String JIRA_URL = "https://bugs.openflexo.org/rest/api/2/project";

	/*private static final Resource MODULES_FILE = ResourceLocator.locateResource("Config/jira_modules_project.json");
	private static final Resource CONNIE_FILE = ResourceLocator.locateResource("Config/jira_connie_project.json");
	private static final Resource TA_FILE = ResourceLocator.locateResource("Config/jira_ta_project.json");
	private static final Resource DIANA_FILE = ResourceLocator.locateResource("Config/jira_diana_project.json");
	private static final Resource PAMELA_FILE = ResourceLocator.locateResource("Config/jira_pamela_project.json");
	private static final Resource CORE_FILE = ResourceLocator.locateResource("Config/jira_core_project.json");
	private static final Resource GINA_FILE = ResourceLocator.locateResource("Config/jira_gina_project.json");
	
	private FlexoVersion ginaVersion;
	private FlexoVersion dianaVersion;
	private FlexoVersion pamelaVersion;
	private FlexoVersion connieVersion;
	private FlexoVersion distributionVersion;*/

	/*private static final String MODULES_KEY = "MODULES";
	private static final String TA_KEY = "TA";
	private static final String DIANA_KEY = "DIANA";
	private static final String CONNIE_KEY = "CONNIE";
	private static final String PAMELA_KEY = "PAMELA";
	private static final String CORE_KEY = "CORE";
	private static final String GINA_KEY = "GINA";*/

	// private HashMap<String, Resource> userProjectFiles;
	// private File userProjectFile;
	private List<JIRAProject> projects;

	public BugReportService() {
	}

	public List<JIRAProject> getProjects() {
		return projects;
	}

	@Override
	public String getServiceName() {
		return "BugReportService";
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public JIRAProject getJIRAProjectWithKey(String projectKey) {
		for (JIRAProject jp : projects) {
			if (jp.getKey().equals(projectKey))
				return jp;
		}
		return null;
	}

	public JIRAProject getJIRAProjectWithId(String id) {
		for (JIRAProject jp : projects) {
			if (jp.getId().equals(id))
				return jp;
		}
		return null;
	}

	public JIRAProject getMostProbableProject(Exception e, FlexoModule<?> activeModule) {
		for (JIRAProject project : getProjects()) {
			if (project.getId().equals(activeModule.getModule().getJiraComponentID())) {
				return project;
			}
		}
		return getJIRAProjectWithKey("MODULES");
	}

	public JIRAComponent getMostProbableProjectComponent(JIRAProject project, Exception e, FlexoModule<?> activeModule) {
		if (project != null && project.getComponents() != null && project.getComponents().size() > 0) {
			return project.getComponents().get(0);
		}
		return null;
	}

	/*public void loadProjectsFromFile(Resource file) {
		try {
			InputStreamReader is = new InputStreamReader(file.openInputStream());
			JIRAProjectList projects = JIRAGson.getInstance().fromJson(is, JIRAProjectList.class);
			if (this.projects == null) {
				logger.warning("INVESTIGATE : projects list is empty!! ");
				this.projects = new ArrayList<JIRAProject>();
			}
	
			for (JIRAProject p : projects) {
				if (p.getKey().equals(MODULES_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(TA_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(DIANA_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(CONNIE_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(PAMELA_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(CORE_KEY)) {
					this.projects.add(p);
				}
				if (p.getKey().equals(GINA_KEY)) {
					this.projects.add(p);
				}
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		}
	}*/

	private boolean testJIRAConnection() {
		if (StringUtils.isNotEmpty(getServiceManager().getBugReportPreferences().getBugReportUser())
				&& StringUtils.isNotEmpty(getServiceManager().getBugReportPreferences().getBugReportPassword())) {
			URL url;
			try {
				url = new URL(JIRA_URL);
				HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
				urlc.addRequestProperty(JIRAClient.BASIC_AUTH_HEADER, "Basic " + getBase64EncodedAuthentication());
				InputStream is = urlc.getInputStream();
				is.close();
				return true;
			} catch (IOException e) {
				logger.warning("IOException: " + e.getMessage());
				return false;
			}
		}
		return false;

	}

	private String askCredentialsWhenRequired() throws UnsupportedEncodingException {

		boolean validCredentials = testJIRAConnection();

		while (getServiceManager().getBugReportPreferences().getBugReportUser() == null
				|| getServiceManager().getBugReportPreferences().getBugReportUser().trim().length() == 0
				|| getServiceManager().getBugReportPreferences().getBugReportPassword() == null
				|| getServiceManager().getBugReportPreferences().getBugReportPassword().trim().length() == 0 || !validCredentials) {
			Progress.forceHideTaskBar();
			if (!JIRAURLCredentialsDialog.askLoginPassword(getServiceManager())) {
				Progress.stopForceHideTaskBar();
				return null;
			}

			validCredentials = testJIRAConnection();

		}

		Progress.stopForceHideTaskBar();
		return getBase64EncodedAuthentication();
	}

	private String getBase64EncodedAuthentication() throws UnsupportedEncodingException {

		String username = getServiceManager().getBugReportPreferences().getBugReportUser().trim();
		String password = getServiceManager().getBugReportPreferences().getBugReportPassword().trim();
		// Ok, it took me a while to find out but ISO-8859-1 is the one used by JIRA
		return Base64.encodeBase64String((username + ":" + password).getBytes("ISO-8859-1"));
	}

	@Override
	public void initialize() {
		logger.info("Initialized BugReportService");

		projects = new ArrayList<>();

		try {

			if (FlexoLocalization.getMainLocalizer() != null) {
				Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("contacting") + " " + JIRA_URL);
			}

			URL url = new URL(JIRA_URL);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.addRequestProperty(JIRAClient.BASIC_AUTH_HEADER, "Basic " + askCredentialsWhenRequired());

			BufferedReader bfr = null;
			try {
				bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			} catch (UnknownHostException e) {
				if (FlexoLocalization.getMainLocalizer() != null) {
					FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("cannot_contact") + " " + JIRA_URL);
				}
				return;
			}
			JIRAProjectList projects = JIRAGson.getInstance().fromJson(bfr, JIRAProjectList.class);
			// System.out.println("projects=" + projects);

			for (JIRAProject p : projects) {
				JIRAProject detailedProject = parseProject(p);
				this.projects.add(detailedProject);
				/*System.out.println("******** Project " + detailedProject);
				System.out.println("Id=" + detailedProject.getId());
				System.out.println("Lead=" + detailedProject.getLead());
				for (JIRAComponent component : detailedProject.getComponents()) {
					System.out.println("Component " + component.getName() + " " + component);
				}
				for (JIRAVersion version : detailedProject.getVersions()) {
					System.out.println("Version " + version.getName() + " " + version);
				}
				System.out.println("IssueTypes=" + detailedProject.getIssueTypes());
				System.out.println("Last released version: "
						+ (detailedProject.getLastReleasedVersion() != null ? detailedProject.getLastReleasedVersion().getName() : "none"));
				 */
			}
		} catch (Exception e) {
			System.out.println("exception: " + e);
			e.printStackTrace();
		}

		status = Status.Started;

		// loadProjectVersions();

		try {
			Map<String, String> headers = new HashMap<String, String>();
			if (getServiceManager() != null && getServiceManager().getBugReportPreferences().getBugReportUser() != null
					&& getServiceManager().getBugReportPreferences().getBugReportUser().trim().length() > 0
					&& getServiceManager().getBugReportPreferences().getBugReportPassword() != null
					&& getServiceManager().getBugReportPreferences().getBugReportPassword().trim().length() > 0) {
				headers.put("Authorization",
						"Basic " + Base64.encodeBase64String((getServiceManager().getBugReportPreferences().getBugReportUser() + ":"
								+ getServiceManager().getBugReportPreferences().getBugReportPassword()).getBytes("ISO-8859-1")));
			}

			/*for (Entry<String, Resource> entry : userProjectFiles.entrySet()) {
				String key = entry.getKey();
				Resource file = entry.getValue();
				// Do not execute update if anonymous login, as it will not work!
				if (file != null && file instanceof FileResourceImpl && headers.size() > 0) {
					FileUtils.createOrUpdateFileFromURL(
							new URL(getServiceManager().getBugReportPreferences().getBugReportUrl()
									+ "/rest/api/2/issue/createmeta?expand=projects.issuetypes.fields&projectKeys=" + key),
							((FileResourceImpl) file).getFile(), headers);
				}
				else {
					logger.severe("Unable to create File for Bug");
				}
			}*/

		} /*catch (MalformedURLException e) {
			e.printStackTrace();
			}*/ catch (UnsupportedEncodingException e) {
			logger.warning("Encoding error in a bug service request.");
		}
		/*if (!userProjectFiles.isEmpty()) {
			for (Entry<String, Resource> entry : userProjectFiles.entrySet()) {
				Resource file = entry.getValue();
				loadProjectsFromFile(file);
			}
		}*/

	}

	public boolean isInitialized() {
		return status == Status.Started;
	}

	private JIRAProject parseProject(JIRAProject p) {

		if (FlexoLocalization.getMainLocalizer() != null) {
			Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("getting_informations_for") + " " + p.getSelf());
		}
		try {
			URL url = new URL(p.getSelf());
			URLConnection urlc = url.openConnection();
			urlc.addRequestProperty(JIRAClient.BASIC_AUTH_HEADER, "Basic " + askCredentialsWhenRequired());
			BufferedReader bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			return JIRAGson.getInstance().fromJson(bfr, JIRAProject.class);
		} catch (Exception e) {
			System.out.println("exception: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		BugReportService service = new BugReportService();
		service.initialize();
	}

}
