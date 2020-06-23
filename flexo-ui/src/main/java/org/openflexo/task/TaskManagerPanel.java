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

package org.openflexo.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.VerticalLayout;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;

/**
 * Dialog showing all waiting and running tasks
 * 
 * @author sylvain
 *
 */
public class TaskManagerPanel extends JDialog implements PropertyChangeListener {

	private static final int PREFERRED_HEIGHT = 50;
	private static final int PREFERRED_WIDTH = 800;

	private final FlexoTaskManager taskManager;
	private final JPanel contentPane;

	private final Map<FlexoTask, TaskPanel> taskPanels;

	private boolean forceHide = false;

	// private final boolean automaticallyBecomesVisible = true;

	public TaskManagerPanel(FlexoTaskManager taskManager) {
		super((Frame) null, "TaskManager", false);

		this.taskManager = taskManager;
		taskManager.getPropertyChangeSupport().addPropertyChangeListener(this);

		// false until we have fixed issue with remaining and empty task bar
		setAlwaysOnTop(true);
		setUndecorated(true);

		contentPane = new JPanel();
		contentPane.setLayout(new VerticalLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder());

		JScrollPane scrollPane = new JScrollPane(contentPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		getContentPane().add(scrollPane);

		taskPanels = new HashMap<>();

		contentPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		validate();

		updateSizeAndCenter();
	}

	public FlexoTaskManager getTaskManager() {
		return taskManager;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> propertyChange(evt));
			return;
		}
		if (evt.getSource() == taskManager) {
			updatePanel();
		}
	}

	private void updatePanel() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> updatePanel());
			return;
		}
		for (FlexoTask task : new ArrayList<>(taskManager.getScheduledTasks())) {
			TaskPanel p = taskPanels.get(task);
			if (p == null) {
				p = new TaskPanel(task);
				contentPane.add(p);
				taskPanels.put(task, p);
			}
		}

		if (!isVisible() && taskManager.getScheduledTasks().size() > 0 && !forceHide) {
			performShowTaskManagerPanel();
		}

		updateSizeAndCenter();
	}

	private synchronized void updateSizeAndCenter() {

		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> updateSizeAndCenter());
			return;
		}

		contentPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT * taskManager.getScheduledTasks().size()));

		((JComponent) getContentPane()).revalidate();
		repaint();

		pack();
		center();

		if (taskManager.getScheduledTasks().size() == 0) {
			// System.out.println("Hidding TaskManagerPanel " + Integer.toHexString(TaskManagerPanel.this.hashCode()) + "...");
			setVisible(false);
			SwingUtilities.invokeLater(() -> {
				// System.out.println("Hidding Again TaskManagerPanel " + Integer.toHexString(TaskManagerPanel.this.hashCode()) +
				// "...");
				setVisible(false);
			});
		}
	}

	public boolean getForceHide() {
		return forceHide;
	}

	public void setForceHide(boolean forceHide) {
		this.forceHide = forceHide;
		if (forceHide) {
			if (isVisible()) {
				setVisible(false);
			}
		}
		else {
			if (!isVisible() && taskManager.getScheduledTasks().size() > 0) {
				performShowTaskManagerPanel();
			}

		}
	}

	private void performShowTaskManagerPanel() {
		// System.out.println("Showing TaskManagerPanel " + Integer.toHexString(TaskManagerPanel.this.hashCode()) + "...");
		setVisible(true);
		SwingUtilities.invokeLater(() -> {
			if (isVisible()) {
				// System.out.println("Perform Show TaskManagerPanel " + Integer.toHexString(TaskManagerPanel.this.hashCode()) + "...");
				requestFocusInWindow();
				requestFocus();
			}
		});
	}

	/**
	 * @param flexoFrame
	 */
	public void center() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> center());
			return;
		}

		Dimension dim;
		/*if (initOwner != null && initOwner.isVisible()) {
			dim = new Dimension(initOwner.getLocationOnScreen().x + initOwner.getWidth() / 2, initOwner.getLocationOnScreen().y
					+ initOwner.getHeight() / 2);
		} else {*/
		dim = Toolkit.getDefaultToolkit().getScreenSize();
		// }
		setLocation((dim.width - getSize().width) / 2, (dim.height - getSize().height) / 2);
	}

	public class TaskPanel extends JPanel implements PropertyChangeListener {

		private final FlexoTask task;

		private final JPanel top;
		private final JPanel bottom;
		private final JLabel titleLabel;
		private final JLabel statusLabel;
		private final JButton cancelButton;
		private final JProgressBar progressBar;

		public TaskPanel(FlexoTask aTask) {

			super(new BorderLayout());

			this.task = aTask;
			task.getPropertyChangeSupport().addPropertyChangeListener(this);

			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

			top = new JPanel(new BorderLayout());
			titleLabel = new JLabel(task.getTaskTitle());
			titleLabel.setHorizontalAlignment(JLabel.LEFT);
			statusLabel = new JLabel(task.getTaskStatus().getLocalizedName());
			statusLabel.setForeground(Color.DARK_GRAY);
			statusLabel.setFont(getFont().deriveFont(Font.ITALIC, 11f));
			statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

			top.add(titleLabel, BorderLayout.CENTER);
			top.add(statusLabel, BorderLayout.EAST);

			bottom = new JPanel(new BorderLayout());
			progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
			progressBar.setStringPainted(false);
			progressBar.setEnabled(false);
			cancelButton = new JButton(IconFactory.getDisabledIcon(IconLibrary.SMALL_DELETE_ICON));
			cancelButton.setDisabledIcon(IconFactory.getDisabledIcon(IconLibrary.SMALL_DELETE_ICON));
			cancelButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					cancelButton.setIcon(IconLibrary.SMALL_DELETE_ICON);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					cancelButton.setIcon(IconFactory.getDisabledIcon(IconLibrary.SMALL_DELETE_ICON));
				}
			});
			cancelButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			cancelButton.setContentAreaFilled(false);
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					taskManager.stopExecution(task);
				}
			});
			cancelButton.setEnabled(task.getTaskStatus() == TaskStatus.RUNNING);

			bottom.add(progressBar, BorderLayout.CENTER);
			bottom.add(cancelButton, BorderLayout.EAST);

			add(top, BorderLayout.NORTH);
			add(bottom, BorderLayout.CENTER);

			setPreferredSize(new Dimension(200, 50));
		}

		private void updateStatusLabel() {
			if (StringUtils.isEmpty(task.getCurrentStepName())) {
				statusLabel.setText(task.getTaskStatus().getLocalizedName());
			}
			else {
				statusLabel.setText(task.getCurrentStepName());
			}
		}

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> propertyChange(evt));
				return;
			}
			if (evt.getSource() == task) {
				if (evt.getPropertyName().equals(FlexoTask.TASK_STATUS_PROPERTY)) {
					cancelButton.setEnabled(task.getTaskStatus() == TaskStatus.RUNNING && task.isCancellable());
					updateStatusLabel();
					if (task.getTaskStatus() == TaskStatus.RUNNING) {
						progressBar.setStringPainted(false);
						progressBar.setIndeterminate(true);
						progressBar.setEnabled(true);
					}
					else if ((task.getTaskStatus() == TaskStatus.FINISHED) || (task.getTaskStatus() == TaskStatus.CANCELLED)
							|| (task.getTaskStatus() == TaskStatus.EXCEPTION_THROWN)) {
						taskPanels.remove(task);
						contentPane.remove(this);
						((JComponent) getContentPane()).revalidate();
						TaskManagerPanel.this.repaint();
						updateSizeAndCenter();
					}
				}
				else if (evt.getPropertyName().equals(FlexoTask.EXPECTED_PROGRESS_STEPS_PROPERTY)) {
					progressBar.setStringPainted(true);
					progressBar.setIndeterminate(false);
					progressBar.setMinimum(0);
					progressBar.setMaximum(task.getExpectedProgressSteps());
					progressBar.setValue(task.getCurrentProgress());
				}
				else if (evt.getPropertyName().equals(FlexoTask.CURRENT_PROGRESS_PROPERTY)) {
					progressBar.setValue(task.getCurrentProgress());
				}
				else if (evt.getPropertyName().equals(FlexoTask.CURRENT_STEP_NAME_PROPERTY)) {
					updateStatusLabel();
				}
				else if (evt.getPropertyName().equals(FlexoTask.TASK_BAR_HIDE)) {
					TaskManagerPanel.this.setVisible(false);
				}
				else if (evt.getPropertyName().equals(FlexoTask.TASK_BAR_SHOW)) {
					TaskManagerPanel.this.setVisible(true);
				}
				else if (evt.getPropertyName().equals(FlexoTask.TASK_BAR_FORCE_HIDE)) {
					setForceHide((Boolean) evt.getNewValue());
				}
			}
		}
	}
}
