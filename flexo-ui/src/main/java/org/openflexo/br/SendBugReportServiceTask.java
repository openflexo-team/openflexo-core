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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.openflexo.ApplicationContext;
import org.openflexo.br.view.JIRAIssueReportDialog;
import org.openflexo.br.view.JIRAURLCredentialsDialog;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.FlexoModule;
import org.openflexo.task.FlexoApplicationTask;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.ws.jira.JIRAException;
import org.openflexo.ws.jira.UnauthorizedJIRAAccessException;
import org.openflexo.ws.jira.model.JIRAErrors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * A task used to initiate and send a bug report
 * 
 * @author sylvain
 *
 */
public class SendBugReportServiceTask extends FlexoApplicationTask {

	private final BugReportService bugReportService;

	private Exception causeException = null;
	private final FlexoProject project;
	private final FlexoModule<?> module;

	private JIRAIssueReportDialog report;
	JFIBDialog<JIRAIssueReportDialog> dialog;

	public SendBugReportServiceTask(Exception e, FlexoModule<?> module, FlexoProject project, ApplicationContext applicationContext) {
		super("SendBugReport", FlexoLocalization.getMainLocalizer().localizedForKey("send_issue"), applicationContext);
		this.bugReportService = applicationContext.getBugReportService();
		this.module = module;
		this.project = project;
		this.causeException = e;
		openDialog();
	}

	private void openDialog() {

		ApplicationContext serviceManager = (ApplicationContext) getServiceManager();

		try {
			report = new JIRAIssueReportDialog(causeException, serviceManager);
			if (module != null) {
				report.setProject(serviceManager.getBugReportService().getMostProbableProject(causeException, module));
				if (report.getIssue() != null) {
					report.getIssue().setComponent(serviceManager.getBugReportService().getMostProbableProjectComponent(report.getProject(),
							causeException, module));
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

			dialog = JFIBDialog.instanciateAndShowDialog(JIRAIssueReportDialog.FIB_FILE, report,
					serviceManager.getApplicationFIBLibraryService().getApplicationFIBLibrary(), FlexoFrame.getActiveFrame(), true,
					FlexoLocalization.getMainLocalizer());

		} catch (JsonSyntaxException e1) {
			e1.printStackTrace();
			FlexoController.showError(
					serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("cannot_read_JIRA_project_file"));
		} catch (JsonIOException e1) {
			e1.printStackTrace();
			FlexoController.showError(
					serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("cannot_read_JIRA_project_file"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			FlexoController.showError(
					serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("cannot_read_JIRA_project_file"));
		}

	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(10);

		ApplicationContext serviceManager = (ApplicationContext) getServiceManager();

		// FIB hrer
		boolean ok = false;
		while (!ok) {
			if (dialog.getStatus() == Status.VALIDATED) {
				try {
					while (serviceManager.getBugReportPreferences().getBugReportUser() == null
							|| serviceManager.getBugReportPreferences().getBugReportUser().trim().length() == 0
							|| serviceManager.getBugReportPreferences().getBugReportPassword() == null
							|| serviceManager.getBugReportPreferences().getBugReportPassword().trim().length() == 0) {
						if (!JIRAURLCredentialsDialog.askLoginPassword(serviceManager)) {
							break;
						}
					}
					Progress.progress("sending...");
					ok = dialog.getData().send();
				} catch (MalformedURLException e1) {
					FlexoController.showError(
							serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_send_bug_report") + " "
									+ e1.getMessage());
				} catch (UnknownHostException e1) {
					FlexoController.showError(
							serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_send_bug_report") + " "
									+ e1.getMessage());
					ok = true;
				} catch (UnauthorizedJIRAAccessException e1) {
					Progress.progress("ask_credentials");
					if (JIRAURLCredentialsDialog.askLoginPassword(serviceManager)) {
						continue;
					}
					else {
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
							sb.append(serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("field") + " "
									+ serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey(e2.getKey()) + " : "
									+ serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey(e2.getValue()));
						}
					}
					FlexoController
							.notify(serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_send_bug_report")
									+ ":\n" + sb.toString());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			else {
				break;
			}
			if (!ok) {
				dialog.setVisible(true);
			}
		}
	}

	public BugReportService getBugReportService() {
		return bugReportService;
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	protected synchronized void finishedExecution() {
		super.finishedExecution();
	}
}
