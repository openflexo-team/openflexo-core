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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationVersion;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.ws.jira.JIRAGson;
import org.openflexo.ws.jira.model.JIRAProject;
import org.openflexo.ws.jira.model.JIRAProjectList;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class BugReportService extends FlexoServiceImpl {

	public List<JIRAProject> getProjects() {
		return projects;
	}

	public void setProjects(List<JIRAProject> projects) {
		this.projects = projects;
	}

	private static final Resource MODULES_FILE = ResourceLocator.locateResource("Config/jira_modules_project.json");
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
	private FlexoVersion distributionVersion;

	private static final String MODULES_KEY = "MODULES";
	private static final String TA_KEY = "TA";
	private static final String DIANA_KEY = "DIANA";
	private static final String CONNIE_KEY = "CONNIE";
	private static final String PAMELA_KEY = "PAMELA";
	private static final String CORE_KEY = "CORE";
	private static final String GINA_KEY = "GINA";

	private HashMap<String, Resource> userProjectFiles;
	// private File userProjectFile;
	private List<JIRAProject> projects;

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public BugReportService() {
	}

	public JIRAProject getOpenFlexoProject(String projectKey) {
		for (JIRAProject jp : projects) {
			if (jp.getKey().equals(projectKey))
				return jp;
		}
		return null;
	}

	public void loadProjectsFromFile(Resource file) {
		try {
			InputStreamReader is = new InputStreamReader(file.openInputStream());
			JIRAProjectList projects = JIRAGson.getInstance().fromJson(is, JIRAProjectList.class);
			if (this.projects == null) {
				logger.warning("INVESTIGATE : projects list is empty!! ");
				this.projects = new ArrayList<JIRAProject>();
			}

			for (JIRAProject p : projects.getProjects()) {
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
	}

	@Override
	public void initialize() {
		logger.info("Initialized BugReportService");
		if (userProjectFiles == null || userProjectFiles.isEmpty()) {
			userProjectFiles = new HashMap<String, Resource>();
			userProjectFiles.put(MODULES_KEY, MODULES_FILE);
			userProjectFiles.put(CONNIE_KEY, CONNIE_FILE);
			userProjectFiles.put(TA_KEY, TA_FILE);
			userProjectFiles.put(DIANA_KEY, DIANA_FILE);
			userProjectFiles.put(CONNIE_KEY, CONNIE_FILE);
			userProjectFiles.put(PAMELA_KEY, PAMELA_FILE);
			userProjectFiles.put(CORE_KEY, CORE_FILE);
			userProjectFiles.put(GINA_KEY, GINA_FILE);
		}

		loadProjectVersions();

		try {
			Map<String, String> headers = new HashMap<String, String>();
			if (getServiceManager().getAdvancedPrefs().getBugReportUser() != null
					&& getServiceManager().getAdvancedPrefs().getBugReportUser().trim().length() > 0
					&& getServiceManager().getAdvancedPrefs().getBugReportPassword() != null
					&& getServiceManager().getAdvancedPrefs().getBugReportPassword().trim().length() > 0) {
				headers.put(
						"Authorization",
						"Basic "
								+ Base64.encodeBase64String((getServiceManager().getAdvancedPrefs().getBugReportUser() + ":" + getServiceManager()
										.getAdvancedPrefs().getBugReportPassword()).getBytes("ISO-8859-1")));
			}

			for (Entry<String, Resource> entry : userProjectFiles.entrySet()) {
				String key = entry.getKey();
				Resource file = entry.getValue();
				// Do not execute update if anonymous login, as it will not work!
				if (file != null && file instanceof FileResourceImpl && headers.size() > 0) {
					FileUtils.createOrUpdateFileFromURL(new URL(getServiceManager().getAdvancedPrefs().getBugReportUrl()
							+ "/rest/api/2/issue/createmeta?expand=projects.issuetypes.fields&projectKeys=" + key),
							((FileResourceImpl) file).getFile(), headers);
				}
				else {
					logger.severe("Unable to create File for Bug");
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			logger.warning("Encoding error in a bug service request.");
		}
		if (!userProjectFiles.isEmpty()) {
			for (Entry<String, Resource> entry : userProjectFiles.entrySet()) {
				Resource file = entry.getValue();
				loadProjectsFromFile(file);
			}
		}

	}

	/**
	 * Load the version of the current project software components
	 */
	private void loadProjectVersions() {
		Properties prop = new Properties();
		try {
			if (ResourceLocator.locateResource("gina.properties") != null) {
				prop.load(ResourceLocator.locateResource("gina.properties").openInputStream());
				ginaVersion = new FlexoVersion(prop.getProperty("gina.version"));
			}
			if (ResourceLocator.locateResource("diana.properties") != null) {
				prop.load(ResourceLocator.locateResource("diana.properties").openInputStream());
				dianaVersion = new FlexoVersion(prop.getProperty("diana.version"));
			}
			if (ResourceLocator.locateResource("connie.properties") != null) {
				prop.load(ResourceLocator.locateResource("connie.properties").openInputStream());
				connieVersion = new FlexoVersion(prop.getProperty("connie.version"));
			}
			if (ResourceLocator.locateResource("pamela.properties") != null) {
				prop.load(ResourceLocator.locateResource("pamela.properties").openInputStream());
				pamelaVersion = new FlexoVersion(prop.getProperty("pamela.version"));
			}
			distributionVersion = new FlexoVersion(ApplicationVersion.BUSINESS_APPLICATION_VERSION);
		} catch (IOException e) {
			logger.warning("Unable to identify a flexo component version.");
		}
	}

	public FlexoVersion getProjectVersion(JIRAProject project) {
		FlexoVersion returned = null;
		if (project.getKey().equals(DIANA_KEY)) {
			returned = dianaVersion;
		}
		else if (project.getKey().equals(CONNIE_KEY)) {
			returned = connieVersion;
		}
		else if (project.getKey().equals(PAMELA_KEY)) {
			returned = pamelaVersion;
		}
		else if (project.getKey().equals(GINA_KEY)) {
			returned = ginaVersion;
		}
		if (returned == null) {
			returned = new FlexoVersion(distributionVersion.major, distributionVersion.minor, distributionVersion.patch, -1, false, false);
		}
		return returned;
	}

}
