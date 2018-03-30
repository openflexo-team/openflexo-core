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

package org.openflexo.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.prefs.GeneralPreferences;
import org.openflexo.project.FlexoAutoSaveThread.FlexoAutoSaveFile;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;

/**
 * AutoSave service working with the {@link InteractiveProjectLoader}<br>
 * 
 * Perform automatic save for projects
 * 
 * @author sylvain
 * 
 */
public class AutoSaveService implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(AutoSaveService.class.getPackage().getName());

	private FlexoAutoSaveThread autoSaveThread = null;

	private final FlexoProject<File> project;

	private final InteractiveProjectLoader projectLoader;

	public AutoSaveService(InteractiveProjectLoader projectLoader, FlexoProject project) {
		super();
		this.projectLoader = projectLoader;
		this.project = project;
		getGeneralPreferences().getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	public GeneralPreferences getGeneralPreferences() {
		return projectLoader.getServiceManager().getGeneralPreferences();
	}

	public void close() {
		getGeneralPreferences().getPropertyChangeSupport().removePropertyChangeListener(this);
		stop();
	}

	public FlexoProject<File> getProject() {
		return project;
	}

	public boolean isRunning() {
		return autoSaveThread != null;
	}

	public void start() {
		if (autoSaveThread != null && autoSaveThread.isAlive()) {
			return;
		}
		if (getGeneralPreferences().getAutoSaveEnabled() && autoSaveThread == null) {
			autoSaveThread = new FlexoAutoSaveThread(project);
			setAutoSaveLimit();
			setAutoSaveInterval();
			autoSaveThread.start();
		}
	}

	public void stop() {
		if (autoSaveThread != null) {
			autoSaveThread.setRun(false);
			if (autoSaveThread.getState() == Thread.State.TIMED_WAITING) {
				autoSaveThread.interrupt();
			}
			autoSaveThread = null;
		}
	}

	private void pause() {
		if (autoSaveThread != null) {
			autoSaveThread.pause();
		}
	}

	private void resume() {
		if (autoSaveThread != null) {
			autoSaveThread.unpause();
		}
	}

	private void setAutoSaveLimit() {
		if (autoSaveThread != null) {
			autoSaveThread.setNumberOfIntermediateSave(getGeneralPreferences().getAutoSaveLimit());
		}
	}

	private void setAutoSaveInterval() {
		if (autoSaveThread != null) {
			autoSaveThread.setSleepTime(getGeneralPreferences().getAutoSaveInterval() * 60 * 1000);
		}
	}

	public File getAutoSaveDirectory() {
		if (autoSaveThread != null) {
			return autoSaveThread.getTempDirectory();
		}
		else {
			return null;
		}
	}

	// TODO reimplement this
	public void showTimeTravelerDialog() {
		pause();
		final FlexoDialog dialog = new FlexoDialog(FlexoFrame.getActiveFrame(), true);
		/*final ParameterDefinition[] parameters = new ParameterDefinition[2];
		parameters[0] = new ReadOnlyTextFieldParameter("directory", "save_directory", autoSaveThread.getTempDirectory().getAbsolutePath());
		parameters[1] = new PropertyListParameter<FlexoAutoSaveFile>("backUps", FlexoLocalization.localizedForKey("back-ups"),
				autoSaveThread.getProjects(), 20, 12);
		((PropertyListParameter) parameters[1]).addReadOnlyTextFieldColumn("creationDateAsAString", "creation_date", 100, true);
		((PropertyListParameter) parameters[1]).addReadOnlyTextFieldColumn("offset", "offset", 100, true);
		((PropertyListParameter) parameters[1]).addReadOnlyTextFieldColumn("path", "path", 450, true);*/
		JPanel north = new JPanel(new FlowLayout(FlowLayout.CENTER));
		north.setBackground(Color.WHITE);
		JLabel label = new JLabel("<html>" + FlexoLocalization.getMainLocalizer().localizedForKey("time_travel_info") + "</html>",
				IconLibrary.TIME_TRAVEL_ICON, SwingConstants.LEFT);
		north.add(label);
		// AskParametersPanel panel = new AskParametersPanel(project, parameters);
		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton cancel = new JButton(FlexoLocalization.getMainLocalizer().localizedForKey("cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				resume();
			}
		});
		JButton ok = new JButton(FlexoLocalization.getMainLocalizer().localizedForKey("restore"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				/*FlexoAutoSaveFile autoSaveFile = (FlexoAutoSaveFile) ((PropertyListParameter) parameters[1]).getSelectedObject();
				if (autoSaveFile != null) {
					if (FlexoController.confirm(FlexoLocalization.localizedForKey("are_you_sure_that_you_want_to_revert_to_that_version?"))) {
						try {
							ProgressWindow.showProgressWindow(FlexoFrame.getActiveFrame(),
									FlexoLocalization.localizedForKey("project_restoration"), 4);
							restoreAutoSaveProject(autoSaveFile, ProgressWindow.instance());
						} catch (IOException e1) {
							e1.printStackTrace();
							FlexoController.showError(FlexoLocalization
									.localizedForKey("an_error_occured_while_trying_to_restore_your_project")
									+ "\n"
									+ project.getProjectDirectory().getAbsolutePath());
						}
					} else {
						resume();
					}
				} else {
					resume();
				}*/

			}
		});
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				resume();
				super.windowClosing(e);
			}
		});
		south.add(ok);
		south.add(cancel);
		dialog.getContentPane().setLayout(new BorderLayout());
		// dialog.getContentPane().add(panel);
		dialog.getContentPane().add(new JLabel("Please reimplement this"));
		dialog.getContentPane().add(north, BorderLayout.NORTH);
		dialog.getContentPane().add(south, BorderLayout.SOUTH);
		dialog.setTitle(FlexoLocalization.getMainLocalizer().localizedForKey("time_traveler"));
		dialog.validate();
		dialog.pack();
		dialog.show();
	}

	public void restoreAutoSaveProject(FlexoAutoSaveFile autoSaveFile) throws IOException {
		File projectDirectory = project.getProjectDirectory();
		File dest = null;
		int attempt = 0;
		while (dest == null || dest.exists()) {
			dest = new File(projectDirectory.getParentFile(),
					projectDirectory.getName() + ".restore" + (attempt == 0 ? "" : "." + attempt));
			attempt++;
		}
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("creating_restore_project_at") + " " +
		// dest.getAbsolutePath());
		FileUtils.copyContentDirToDir(projectDirectory, dest);
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("closing_project"));
		projectLoader.closeProject(project);
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("deleting_project"));
		FileUtils.deleteDir(projectDirectory);
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("restoring_project"));
		FileUtils.copyContentDirToDir(autoSaveFile.getDirectory(), projectDirectory);
		try {
			projectLoader.loadProject(projectDirectory);
		} catch (ProjectLoadingCancelledException e) {
			e.printStackTrace();
		} catch (ProjectInitializerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getGeneralPreferences()) {
			String key = evt.getPropertyName();
			if (GeneralPreferences.AUTO_SAVE_ENABLED.equals(key)) {
				if (getGeneralPreferences().getAutoSaveEnabled()) {
					start();
				}
				else {
					stop();
				}
			}
			else if (GeneralPreferences.AUTO_SAVE_INTERVAL.equals(key)) {
				setAutoSaveInterval();
			}
			else if (GeneralPreferences.AUTO_SAVE_LIMIT.equals(key)) {
				setAutoSaveLimit();
			}
		}
	}

}
