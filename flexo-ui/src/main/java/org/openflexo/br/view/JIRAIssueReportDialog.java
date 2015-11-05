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

package org.openflexo.br.view;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationVersion;
import org.openflexo.Flexo;
import org.openflexo.components.ProgressWindow;
import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.fib.swing.utils.JFIBDialog;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.swing.ImageUtils;
import org.openflexo.swing.ImageUtils.ImageType;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.toolbox.ZipUtils;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.ws.jira.JIRAClient;
import org.openflexo.ws.jira.JIRAClient.Method;
import org.openflexo.ws.jira.JIRAClient.Progress;
import org.openflexo.ws.jira.JIRAException;
import org.openflexo.ws.jira.UnauthorizedJIRAAccessException;
import org.openflexo.ws.jira.action.SubmitIssue;
import org.openflexo.ws.jira.model.JIRAComponent;
import org.openflexo.ws.jira.model.JIRAErrors;
import org.openflexo.ws.jira.model.JIRAIssue;
import org.openflexo.ws.jira.model.JIRAObject;
import org.openflexo.ws.jira.model.JIRAPriority;
import org.openflexo.ws.jira.model.JIRAProject;
import org.openflexo.ws.jira.model.JIRAVersion;
import org.openflexo.ws.jira.result.JIRAResult;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class JIRAIssueReportDialog {

	private final class ReportProgress implements Progress {
		int count = 0;

		@Override
		public void setProgress(double percentage) {
			int steps = (int) (percentage * 100);
			steps -= count;
			for (int i = 0; i < steps; i++) {
				ProgressWindow.instance().setSecondaryProgress("");
			}
			count += steps;
		}

		public void resetCount() {
			ProgressWindow.instance().resetSecondaryProgress(100);
			count = 0;
		}

	}

	public static class SubmitIssueReport {

		private String issueLink;

		private final List<String> errors;
		private final List<String> warnings;

		public SubmitIssueReport() {
			errors = new ArrayList<String>();
			warnings = new ArrayList<String>();
		}

		public boolean hasErrors() {
			return errors.size() > 0;
		}

		public boolean hasWarnings() {
			return warnings.size() > 0;
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
			StringBuilder sb = new StringBuilder();
			for (String e : errors) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(e);
			}
			return sb.toString();
		}

		public String warningsToString() {
			StringBuilder sb = new StringBuilder();
			for (String w : warnings) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(w);
			}
			return sb.toString();
		}

		public String issueLinkHyperlink() {
			return "<html><a href=\"" + issueLink + "\">" + issueLink + "</a></href>";
		}

		public void openIssueLink() {
			ToolBox.openURL(getIssueLink());
		}
	}

	private static final Logger logger = FlexoLogger.getLogger(JIRAIssueReportDialog.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/JIRAIssueReportDialog.fib");
	public static final Resource REPORT_FIB_FILE = ResourceLocator.locateResource("Fib/JIRASubmitIssueReportDialog.fib");
	private static final List<JIRAComponent> EMPTY_LIST = new ArrayList<JIRAComponent>(0);

	private JIRAIssue issue;
	private JIRAProject project;

	private boolean sendLogs;
	private boolean sendScreenshots;
	private boolean sendProject;
	private boolean sendSystemProperties;

	private File attachFile;

	private ApplicationContext serviceManager;
	private FlexoProject flexoProject;

	public static void newBugReport(FlexoModule module, FlexoProject project, ApplicationContext serviceManager) {
		newBugReport(null, module, project, serviceManager);
	}

	public static void newBugReport(Exception e, FlexoModule module, FlexoProject project, ApplicationContext serviceManager) {
		try {
			JIRAIssueReportDialog report = new JIRAIssueReportDialog(e,serviceManager);
			if (module != null) {
				JIRAProject moduleProject = serviceManager.getBugReportService().getOpenFlexoProject("MODULES");
				if(moduleProject!=null){
					report.setProject(moduleProject);
					for (JIRAComponent comp :report.getAvailableComponents()) {
						if (comp.getId().equals(module.getModule().getJiraComponentID())) {
							report.getIssue().setComponent(comp);
							break;
						}
					}
				}
			}
			report.setFlexoProject(project);
			report.setServiceManager(serviceManager);
			/*
			if (module != null) {
				if (report.getIssue().getIssuetype().getComponentField() != null
						&& report.getIssue().getIssuetype().getComponentField().getAllowedValues() != null) {
					for (JIRAComponent comp : report.getIssue().getIssuetype().getComponentField().getAllowedValues()) {
						if (comp.getId().equals(module.getModule().getJiraComponentID())) {
							report.getIssue().setComponent(comp);
							break;
						}
					}
				}
			}*/
			
			JFIBDialog<JIRAIssueReportDialog> dialog = JFIBDialog.instanciateAndShowDialog(FIB_FILE, report, FlexoFrame.getActiveFrame(),
					true, FlexoLocalization.getMainLocalizer());
			
			
			//FIB hrer
			boolean ok = false;
			while (!ok) {
				if (dialog.getStatus() == Status.VALIDATED) {
					try {
						while (serviceManager.getAdvancedPrefs().getBugReportUser() == null
								|| serviceManager.getAdvancedPrefs().getBugReportUser().trim().length() == 0
								|| serviceManager.getAdvancedPrefs().getBugReportPassword() == null
								|| serviceManager.getAdvancedPrefs().getBugReportPassword().trim().length() == 0) {
							if (!JIRAURLCredentialsDialog.askLoginPassword(serviceManager)) {
								break;
							}
						}
						ok = dialog.getData().send();
					} catch (MalformedURLException e1) {
						FlexoController.showError(FlexoLocalization.localizedForKey("could_not_send_bug_report") + " " + e1.getMessage());
					} catch (UnknownHostException e1){
						FlexoController.showError(FlexoLocalization.localizedForKey("could_not_send_bug_report") + " " + e1.getMessage());
						ok = true;
					}
					catch (UnauthorizedJIRAAccessException e1) {	if (JIRAURLCredentialsDialog.askLoginPassword(serviceManager)) {
							continue;
						} else {
							break;
						}
					} catch (JIRAException e1) {
						StringBuilder sb = new StringBuilder();
						JIRAErrors errors = e1.getErrors();
						if (errors.getErrorMessages() != null) {
							for (String s : errors.getErrorMessages()) {
								if (sb.length() > 0) {
									sb.append('\n');
								}
								sb.append(s);
							}
						}
						if (errors.getErrors() != null) {
							for (Entry<String, String> e2 : errors.getErrors().entrySet()) {
								if (sb.length() > 0) {
									sb.append('\n');
								}
								sb.append(FlexoLocalization.localizedForKey("field") + " " + FlexoLocalization.localizedForKey(e2.getKey())
										+ " : " + FlexoLocalization.localizedForKey(e2.getValue()));
							}
						}
						FlexoController.notify(FlexoLocalization.localizedForKey("could_not_send_bug_report") + ":\n" + sb.toString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else {
					break;
				}
				if (!ok) {
					dialog.setVisible(true);
				}
			}
		} catch (JsonSyntaxException e1) {
			e1.printStackTrace();
			FlexoController.showError(FlexoLocalization.localizedForKey("cannot_read_JIRA_project_file"));
		} catch (JsonIOException e1) {
			e1.printStackTrace();
			FlexoController.showError(FlexoLocalization.localizedForKey("cannot_read_JIRA_project_file"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			FlexoController.showError(FlexoLocalization.localizedForKey("cannot_read_JIRA_project_file"));
		}
	}

	private void setServiceManager(ApplicationContext serviceManager) {
		this.serviceManager = serviceManager;
	}

	private void setFlexoProject(FlexoProject project) {
		this.flexoProject = project;
	}

	private JIRAIssueReportDialog() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		this(null,null);
	}

	private JIRAIssueReportDialog(Exception e,ApplicationContext serviceManager) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		this.serviceManager = serviceManager;
		//this.project = serviceManager.getBugReportService().getOpenFlexoProject();
		issue = new JIRAIssue();
				
		//issue.setIssuetype(project.getIssuetypes().get(0));
		//issue.setProject(project);
		/*if (issue.getIssuetype().getPriorityField() != null && issue.getIssuetype().getPriorityField().getAllowedValues() != null) {
			JIRAPriority major = null;
			for (JIRAPriority p : issue.getIssuetype().getPriorityField().getAllowedValues()) {
				if ("Major".equals(p.getName())) {
					major = p;
					break;
				}
				if ("3".equals(p.getId())) {
					major = p;
				}
			}
			issue.setPriority(major);
		}*/
		sendSystemProperties = false;
		sendScreenshots = false;
		sendLogs = true;
		if (e != null) {
			issue.setStacktrace(e.getClass().getName() + ": " + e.getMessage() + "\n" + ToolBox.getStackTraceAsString(e));
		}
	}

	public List<JIRAProject> getProjects(){
		return serviceManager.getBugReportService().getProjects();
	}
	
	public JIRAProject getProject() {
		return project;
	}

	public void setProject(JIRAProject project){
		this.project = project;
		issue.setIssuetype(project.getIssuetypes().get(0));
		issue.setProject(project);
		if (issue.getIssuetype().getPriorityField() != null && issue.getIssuetype().getPriorityField().getAllowedValues() != null) {
			JIRAPriority major = null;
			for (JIRAPriority p : issue.getIssuetype().getPriorityField().getAllowedValues()) {
				if ("Major".equals(p.getName())) {
					major = p;
					break;
				}
				if ("3".equals(p.getId())) {
					major = p;
				}
			}
			issue.setPriority(major);
		}
	}
	
	public JIRAIssue getIssue() {
		return issue;
	}

	public void setIssue(JIRAIssue issue) {
		this.issue = issue;
	}

	public boolean send() throws Exception {
		JIRAClient client = new JIRAClient(serviceManager.getAdvancedPrefs().getBugReportUrl(), serviceManager.getAdvancedPrefs()
				.getBugReportUser(), serviceManager.getAdvancedPrefs().getBugReportPassword());
		final SubmitIssueReport report = new SubmitIssueReport();
		SubmitIssueToJIRA target = new SubmitIssueToJIRA(client, report);
		int steps = target.getNumberOfSteps();
		ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("submitting_bug_report"), steps);
		try {
			boolean submit = true;
			while (submit) {
				target.run();
				if (target.getException() != null) {
					if (target.getException() instanceof SocketTimeoutException) {
						submit = FlexoController.confirm(FlexoLocalization.localizedForKey("could_not_send_incident_so_far_keep_trying")
								+ "? ");
						if (submit) {
							client.setTimeout(client.getTimeout() * 2);// Let's increase time out
						}
					} else if (target.getException() instanceof UnknownHostException) {
						submit = FlexoController.confirm(FlexoLocalization.localizedForKey("could_not_send_to_host_check_internet_connexion_and_try_again")
								+ "? ");
						// If the user want to stop, quit, otherwise clean the exception and try again
						if(submit == false){
							throw target.getException();
						}else{
							target.exception = null;
						}
					}else {
						throw target.getException();
					}
				} else {
					submit = false;
				}
			}
		} finally {
			ProgressWindow.hideProgressWindow();
		}
		JFIBDialog
				.instanciateAndShowDialog(REPORT_FIB_FILE, report, FlexoFrame.getActiveFrame(), true, FlexoLocalization.getMainLocalizer());
		return !report.hasErrors();
	}

	private class SubmitIssueToJIRA implements Runnable {

		private final SubmitIssueReport report;
		private Exception exception;

		private final JIRAClient client;

		protected SubmitIssueToJIRA(JIRAClient client, SubmitIssueReport report) {
			super();
			this.client = client;
			this.report = report;
		}

		public int getNumberOfSteps() {
			int steps = 1;
			if (sendProject) {
				steps += 2;
			}
			if (sendLogs) {
				steps++;
			}
			if (attachFile != null) {
				steps++;
			}
			if (sendScreenshots) {
				for (int i = 0; i < Frame.getFrames().length; i++) {
					Frame frame = Frame.getFrames()[i];
					if (frame instanceof FlexoFrame) {
						steps++;
						for (Window w : frame.getOwnedWindows()) {
							if (w instanceof FlexoDialog || w instanceof JFIBDialog) {
								steps++;
							}
						}
					}
				}
			}
			return steps;
		}

		@Override
		public void run() {
			ReportProgress progressAdapter = new ReportProgress();
			String buildid = "build.id = " + ApplicationVersion.BUILD_ID + "\n";
			String commitID = "commit.id = " + ApplicationVersion.COMMIT_ID + "\n";
			if (sendSystemProperties) {
				issue.setSystemProperties(buildid + commitID + ToolBox.getSystemProperties(true));
			} else {
				issue.setSystemProperties(buildid + commitID);
			}

			if (getIssue().getIssuetype().getVersionField() != null) {
				List<JIRAVersion> allowedValues = getIssue().getIssuetype().getVersionField().getAllowedValues();
				//FlexoVersion flexoVersion = new FlexoVersion(ApplicationVersion.BUSINESS_APPLICATION_VERSION);
				//FlexoVersion simpleVersion = new FlexoVersion(flexoVersion.major, flexoVersion.minor, flexoVersion.patch, -1, false, false);
				FlexoVersion version = serviceManager.getBugReportService().getProjectVersion(project);
				getIssue().setVersion(findClosedVersion(version, allowedValues));
				/*JIRAVersion selected = null;
				for (JIRAVersion version : allowedValues) {
					if (serviceManager.getBugReportService().getGinaVersion().equals(version.getName())) {
						selected = version;
						break;
					}
				}
				if (selected == null) {
					for (JIRAVersion version : allowedValues) {
						if (serviceManager.getBugReportService().getGinaVersion().equals(version.getName())) {
							selected = version;
							break;
						}
					}
				}*/
				//getIssue().setVersion(selected);
			} else {
				getIssue().setVersion(null);
			}
			// Always call make valid before replacing by identity members
			getIssue().makeValid();
			getIssue().<JIRAObject> replaceMembersByIdentityMembers();
			try {
				ProgressWindow.instance().setProgress(FlexoLocalization.localizedForKey("creating_issue"));
				progressAdapter.resetCount();
				JIRAResult submit = client.submit(new SubmitIssue(getIssue()), Method.POST, progressAdapter);
				if (submit.getErrorMessages() != null && submit.getErrorMessages().size() > 0) {
					for (String error : submit.getErrorMessages()) {
						report.addToErrors(error);
					}
				}
				if (submit.getKey() != null) {
					JIRAIssue result = new JIRAIssue();
					result.setKey(submit.getKey());
					report.setIssueLink(serviceManager.getAdvancedPrefs().getBugReportUrl() + "/browse/" + submit.getKey());
					if (sendLogs) {
						ProgressWindow.instance().setProgress(FlexoLocalization.localizedForKey("sending_logs"));
						progressAdapter.resetCount();
						try {
							client.attachFilesToIssue(result, progressAdapter, Flexo.getErrLogFile());
						} catch (IOException e) {
							report.addToErrors(FlexoLocalization.localizedForKey("could_not_attach_file") + " "
									+ Flexo.getErrLogFile().getAbsolutePath() + "\n\t" + e.getMessage());
						}
					}
					if (attachFile != null) {
						ProgressWindow.instance().setProgress(
								FlexoLocalization.localizedForKey("sending_file") + " " + attachFile.getAbsolutePath());
						progressAdapter.resetCount();
						try {
							client.attachFilesToIssue(result, progressAdapter, attachFile);
						} catch (IOException e) {
							report.addToErrors(FlexoLocalization.localizedForKey("could_not_attach_file") + " "
									+ attachFile.getAbsolutePath() + "\n\t" + e.getMessage());
						}
					}
					if (sendProject) {
						if (flexoProject != null) {
							File projectDirectory = flexoProject.getProjectDirectory();
							String directoryName = projectDirectory.getName();
							File zipFile = new File(System.getProperty("java.io.tmpdir"), directoryName.substring(0,
									directoryName.length() - 4) + ".zip");
							FileFilter filter = new FileFilter() {

								@Override
								public boolean accept(File pathname) {
									return !pathname.getName().endsWith("~");
								}
							};
							ProgressWindow.instance().setProgress(FlexoLocalization.localizedForKey("compressing_project"));
							ProgressWindow.instance().resetSecondaryProgress(
									FileUtils.countFilesInDirectory(projectDirectory, true, filter));
							try {
								ZipUtils.makeZip(zipFile, projectDirectory, new IProgress() {

									@Override
									public void setSecondaryProgress(String stepName) {
										ProgressWindow.instance().setSecondaryProgress(stepName);
									}

									@Override
									public void setProgress(String stepName) {

									}

									@Override
									public void resetSecondaryProgress(int steps) {
									}

									@Override
									public void hideWindow() {

									}
								}, filter, Deflater.BEST_COMPRESSION);
								try {
									ProgressWindow.instance().setProgress(FlexoLocalization.localizedForKey("sending_project"));
									progressAdapter.resetCount();
									client.attachFilesToIssue(result, progressAdapter, zipFile);
								} catch (IOException e) {
									report.addToErrors(FlexoLocalization.localizedForKey("could_not_attach_project") + " " + e.getMessage());
								}
							} catch (IOException e) {
								report.addToErrors(FlexoLocalization.localizedForKey("could_not_zip_project") + " " + e.getMessage());
							}
						}
					}
					if (sendScreenshots) {
						for (int i = 0; i < Frame.getFrames().length; i++) {
							Frame frame = Frame.getFrames()[i];
							if (frame instanceof FlexoFrame) {
								ProgressWindow.instance().setProgress(
										FlexoLocalization.localizedForKey("sending_screenshot") + " " + frame.getTitle());
								progressAdapter.resetCount();
								attachScreenshotToIssue(client, result, frame, frame.getTitle(), progressAdapter, report);
								for (Window w : frame.getOwnedWindows()) {
									if (w instanceof FlexoDialog || w instanceof JFIBDialog) {
										ProgressWindow.instance().setProgress(
												FlexoLocalization.localizedForKey("sending_screenshot") + " " + ((Dialog) w).getTitle());
										progressAdapter.resetCount();
										attachScreenshotToIssue(client, result, w, ((Dialog) w).getTitle(), progressAdapter, report);
									}
								}
							}
						}
					}
				}
			} 
			catch (UnknownHostException e) {
				logger.severe("Not able to connect to URL " + serviceManager.getAdvancedPrefs().getBugReportUrl() + ". Please check your internet connection.");
				this.exception = e;
			}catch (IOException e) {
				e.printStackTrace();
				this.exception = e;
			} catch (JIRAException e) {
				e.printStackTrace();
				this.exception = e;
			} finally {
				getIssue().<JIRAObject> replaceMembersByOriginalMembers();
			}

		}

		public Exception getException() {
			return exception;
		}
	}

	private void attachScreenshotToIssue(JIRAClient client, JIRAIssue result, Window window, String title, Progress progress,
			SubmitIssueReport report) {
		if (window.isVisible() && window.getSize().getWidth() > 0 && window.getSize().getHeight() > 0) {
			try {
				File file = new File(System.getProperty("java.io.tmpdir"), FileUtils.getValidFileName(title + ".png"));
				ImageUtils.saveImageToFile(ImageUtils.createImageFromComponent(window), file, ImageType.PNG);
				client.attachFilesToIssue(result, progress, file);
			} catch (Exception e) {
				report.addToErrors(FlexoLocalization.localizedForKey("could_not_attach_screenshot") + " " + title + "\n\t" + e.getMessage());
				logger.log(Level.SEVERE, "Error when trying to send screenshot: " + title, e);
			}
		}
	}

	/*public Icon getIcon(JIRAProject project){
		
	}*/
	
	public List<JIRAComponent> getAvailableComponents() {
		if (getIssue().getIssuetype() == null) {
			return EMPTY_LIST;
		}
		List<JIRAComponent> availableComponents = new ArrayList<JIRAComponent>();
		
		for (JIRAComponent component : getIssue().getIssuetype().getComponentField().getAllowedValues()) {
			//if (module.getJiraComponentID().equals(component.getId())) {
				availableComponents.add(component);
			//	break;
			//}
		}
		return availableComponents;
	}
	
	/*public List<JIRAComponent> getAvailableModules() {
		if (getIssue().getIssuetype() == null) {
			return EMPTY_LIST;
		}
		List<JIRAComponent> availableModules = new ArrayList<JIRAComponent>();
		for (Module<?> module : serviceManager.getModuleLoader().getKnownModules()) {
			for (JIRAComponent component : getIssue().getIssuetype().getComponentField().getAllowedValues()) {
				if (module.getJiraComponentID().equals(component.getId())) {
					availableModules.add(component);
					break;
				}
			}
		}
		return availableModules;
	}*/

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

	public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public boolean isSendProject() {
		return sendProject;
	}

	public void setSendProject(boolean sendProject) {
		this.sendProject = sendProject;
	}

	public boolean isValid() {
		return getIssue() != null && getIssue().getIssuetype() != null && StringUtils.isNotEmpty(getIssue().getSummary())
				&& StringUtils.isNotEmpty(getIssue().getDescription());
	}
	
	private JIRAVersion findClosedVersion(FlexoVersion version, List<JIRAVersion> allowedValues){
		
		JIRAVersion closedJiraVersion = null;
		
		// Prepare allowed versions from jira
		List<FlexoVersion> flexoVersions = new ArrayList<FlexoVersion>();
		for (JIRAVersion value : allowedValues) {
			flexoVersions.add(new FlexoVersion(value.getName()));
		}
		Collections.sort(flexoVersions);
		
		// Compare with patch
		closedJiraVersion = retrieveJiraVersion(allowedValues,version, true);
		// Compare without patch
		if(closedJiraVersion==null){
			closedJiraVersion =  retrieveJiraVersion(allowedValues,version, false);
		}
		// Otherwise get the one of the allowed versions
		if(closedJiraVersion == null && !allowedValues.isEmpty()){
			closedJiraVersion = allowedValues.get(allowedValues.size()-1);
		}
		return closedJiraVersion;
	}
	
	private JIRAVersion retrieveJiraVersion(List<JIRAVersion> allowedValues,FlexoVersion version, boolean withPatch){
		for(JIRAVersion jiraVersion :allowedValues){
			if(jiraVersion.getName().equals(version.toString(withPatch))){
				return jiraVersion;
			}
		}
		return null;
	}
	
}
