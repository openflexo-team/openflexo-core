package org.openflexo.br;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FileUtils;
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

	/*
	private static final File PROJECT_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_openflexo_project.json");
	private static final File TA_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_ta_project.json");
	private static final File DIANA_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_diana_project.json");
	private static final File CONNIE_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_connie_project.json");
	private static final File PAMELA_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_pamela_project.json");
	private static final File CORE_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_core_project.json");
	private static final File GINA_FILE = CompositeResourceLocatorImpl.retrieveResourceAsFile("Config/jira_gina_project.json");
	 */
	private static final String OPENFLEXO_KEY = "OPENFLEXO";
	private static final String MODULES_KEY = "MODULES";
	private static final String TA_KEY = "TA";
	private static final String DIANA_KEY = "DIANA";
	private static final String CONNIE_KEY = "CONNIE";
	private static final String PAMELA_KEY = "PAMELA";
	private static final String CORE_KEY = "CORE";
	private static final String GINA_KEY = "GINA";

	private HashMap<String, Resource> userProjectFiles;
	//private File userProjectFile;
	private List<JIRAProject> projects;

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public BugReportService() {
	}

	public JIRAProject getOpenFlexoProject(String projectKey) {
		for(JIRAProject jp : projects){
			if(jp.getKey().equals(projectKey))
				return jp;
		}
		return null;
	}

	/*private File copyOriginalToUserFile(File projectFile) {
		try {
			FileUtils.copyFileToFile(projectFile, userProjectFile);
			return userProjectFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}*/

	public void loadProjectsFromFile(Resource file) {
		try {
			JIRAProjectList projects = JIRAGson.getInstance().fromJson(new InputStreamReader(file.openInputStream()),
					JIRAProjectList.class);
			if(this.projects==null){
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
		if(userProjectFiles==null || userProjectFiles.isEmpty()){
			userProjectFiles = new HashMap<String,Resource>();

			/*File moduleFile = new File(FileUtils.getApplicationDataDirectory(), MODULES_FILE.getName());
			File taFile = new File(FileUtils.getApplicationDataDirectory(), TA_FILE.getName());
			File dianaFile = new File(FileUtils.getApplicationDataDirectory(), DIANA_FILE.getName());
			File connieFile = new File(FileUtils.getApplicationDataDirectory(), CONNIE_FILE.getName());
			File pamelaFile = new File(FileUtils.getApplicationDataDirectory(), PAMELA_FILE.getName());
			File coreFile = new File(FileUtils.getApplicationDataDirectory(), CORE_FILE.getName());
			File ginaFile = new File(FileUtils.getApplicationDataDirectory(), GINA_FILE.getName());*/


			/*if (!userProjectFile.exists()) {
				userProjectFile = copyOriginalToUserFile(OPENFLEXO_FILE);
			}*/

			/*userProjectFiles.put(MODULES_KEY, MODULES_FILE);
			userProjectFiles.put(TA_KEY,TA_FILE);
			userProjectFiles.put(DIANA_KEY,DIANA_FILE);
			userProjectFiles.put(CONNIE_KEY,CONNIE_FILE);
			userProjectFiles.put(PAMELA_KEY,PAMELA_FILE);
			userProjectFiles.put(CORE_KEY,CORE_FILE);
			userProjectFiles.put(GINA_KEY,GINA_FILE);*/

			userProjectFiles.put(MODULES_KEY, MODULES_FILE);
		}

		try {
			Map<String, String> headers = new HashMap<String, String>();
			if (getServiceManager().getAdvancedPrefs().getBugReportUser() != null
					&& getServiceManager().getAdvancedPrefs().getBugReportUser().trim().length() > 0
					&& getServiceManager().getAdvancedPrefs().getBugReportPassword() != null
					&& getServiceManager().getAdvancedPrefs().getBugReportPassword().trim().length() > 0) {
				try {
					headers.put(
							"Authorization",
							"Basic "
									+ Base64.encodeBase64String((getServiceManager().getAdvancedPrefs().getBugReportUser() + ":" + getServiceManager()
											.getAdvancedPrefs().getBugReportPassword()).getBytes("ISO-8859-1")));
				} catch (UnsupportedEncodingException e) {
				}

				for(Entry<String, Resource> entry : userProjectFiles.entrySet()) {
					String key = entry.getKey();
					Resource file = entry.getValue();
					if (file != null && file instanceof FileResourceImpl){
						FileUtils.createOrUpdateFileFromURL(new URL(getServiceManager().getAdvancedPrefs().getBugReportUrl()
								+ "/rest/api/2/issue/createmeta?expand=projects.issuetypes.fields&projectKey=" + key),((FileResourceImpl) file).getFile(),
								headers);
					}
					else {
						logger.severe("Unable to create File for Bug");
					}
				}



			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		/*project = loadProjectsFromFile(userProjectFile);
		if (project == null) {
			copyOriginalToUserFile();
			project = loadProjectsFromFile(PROJECT_FILE);
		}*/
		if(!userProjectFiles.isEmpty()){
			for(Entry<String, Resource> entry : userProjectFiles.entrySet()) {
				Resource file = entry.getValue();
				loadProjectsFromFile(file);
			}
		}
	}

}
