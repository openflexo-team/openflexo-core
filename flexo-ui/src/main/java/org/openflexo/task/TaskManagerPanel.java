package org.openflexo.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import org.jdesktop.swingx.VerticalLayout;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;

public class TaskManagerPanel extends JDialog implements PropertyChangeListener {

	private static final int PREFERRED_HEIGHT = 50;
	private static final int PREFERRED_WIDTH = 400;

	private final FlexoTaskManager taskManager;
	private final JPanel contentPane;

	private final Map<FlexoTask, TaskPanel> taskPanels;

	private final boolean automaticallyBecomesVisible = true;

	public TaskManagerPanel(FlexoTaskManager taskManager) {
		super((Frame) null, "TaskManager", false);

		this.taskManager = taskManager;
		taskManager.getPropertyChangeSupport().addPropertyChangeListener(this);

		setAlwaysOnTop(true);
		setUndecorated(true);

		contentPane = new JPanel();
		contentPane.setLayout(new VerticalLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder());

		JScrollPane scrollPane = new JScrollPane(contentPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		getContentPane().add(scrollPane);

		taskPanels = new HashMap<FlexoTask, TaskPanel>();

		contentPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		validate();

		updateSizeAndCenter();
	}

	/*@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			center();
		}
	}*/

	public FlexoTaskManager getTaskManager() {
		return taskManager;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == taskManager) {
			updatePanel();
		}
	}

	private synchronized void updatePanel() {
		for (FlexoTask task : taskManager.getScheduledTasks()) {
			TaskPanel p = taskPanels.get(task);
			if (p == null) {
				p = new TaskPanel(task);
				contentPane.add(p);
				taskPanels.put(task, p);
			}
		}

		// contentPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT * 3));

		if (!isVisible() && taskManager.getScheduledTasks().size() > 0) {
			setVisible(true);
		}

		updateSizeAndCenter();
	}

	private synchronized void updateSizeAndCenter() {
		contentPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT * taskManager.getScheduledTasks().size()));

		((JComponent) getContentPane()).revalidate();
		repaint();

		pack();
		center();

		if (taskManager.getScheduledTasks().size() == 0) {
			setVisible(false);
		}
	}

	/**
	 * @param flexoFrame
	 */
	public void center() {
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
			cancelButton = new JButton(/*"stop"*/IconLibrary.SMALL_DELETE_ICON);
			cancelButton.setDisabledIcon(IconFactory.getDisabledIcon(IconLibrary.SMALL_DELETE_ICON));
			cancelButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == task) {
				if (evt.getPropertyName().equals(FlexoTask.TASK_STATUS_PROPERTY)) {
					cancelButton.setEnabled(task.getTaskStatus() == TaskStatus.RUNNING);
					statusLabel.setText(task.getTaskStatus().getLocalizedName());
					if (task.getTaskStatus() == TaskStatus.RUNNING) {
						progressBar.setStringPainted(false);
						progressBar.setIndeterminate(true);
						progressBar.setEnabled(true);
					} else if ((task.getTaskStatus() == TaskStatus.FINISHED) || (task.getTaskStatus() == TaskStatus.CANCELLED)) {
						taskPanels.remove(task);
						contentPane.remove(this);
						((JComponent) getContentPane()).revalidate();
						TaskManagerPanel.this.repaint();
						updateSizeAndCenter();
					}
				} else if (evt.getPropertyName().equals(FlexoTask.EXPECTED_PROGRESS_STEPS_PROPERTY)) {
					progressBar.setStringPainted(true);
					progressBar.setIndeterminate(false);
					progressBar.setMinimum(0);
					progressBar.setMaximum(task.getExpectedProgressSteps());
					progressBar.setValue(task.getCurrentProgress());
				} else if (evt.getPropertyName().equals(FlexoTask.CURRENT_PROGRESS_PROPERTY)) {
					progressBar.setValue(task.getCurrentProgress());
				}
			}
		}
	}
}
