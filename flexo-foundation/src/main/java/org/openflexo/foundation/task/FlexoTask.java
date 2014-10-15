package org.openflexo.foundation.task;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public abstract class FlexoTask implements Runnable, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(FlexoTask.class.getPackage().getName());

	public static final String TASK_TITLE_PROPERTY = "taskTitle";
	public static final String TASK_STATUS_PROPERTY = "taskStatus";
	public static final String CURRENT_PROGRESS_PROPERTY = "currentProgress";
	public static final String EXPECTED_PROGRESS_STEPS_PROPERTY = "expectedProgressSteps";

	private final String taskTitle;
	private TaskStatus status;
	private FlexoTaskThread thread;

	private int currentProgress;
	private int expectedProgressSteps;

	public enum TaskStatus {
		WAITING {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("waiting");
			}
		},
		RUNNING {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("running");
			}
		},
		FINISHED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("finished");
			}
		},
		CANCEL_REQUESTED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("cancel_requested");
			}
		},
		CANCELLED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("cancelled");
			}
		},
		UNKNOWN {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("unknown_task_status");
			}
		};
		public abstract String getLocalizedName();
	}

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	public FlexoTask(String title) {
		this.taskTitle = title;
		status = TaskStatus.WAITING;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public String getTaskTitle() {
		return taskTitle;
	}

	@Override
	public String toString() {
		return "FlexoTask:" + getTaskTitle();
	}

	protected void startExecution(FlexoTaskThread thread) {
		if (status == TaskStatus.WAITING) {
			this.thread = thread;
			thread.setTask(this);
			status = TaskStatus.RUNNING;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.WAITING, TaskStatus.RUNNING);
		} else {
			logger.warning("Start execution of FlexoTask called for a task with status " + status);
		}
	}

	protected void stopExecution() {
		if (status == TaskStatus.RUNNING) {
			status = TaskStatus.CANCEL_REQUESTED;
			thread.interrupt();
		} else {
			logger.warning("Stop execution of FlexoTask called for a task with status " + status);
		}
	}

	@Deprecated
	protected void forceStopExecution() {
		if (status == TaskStatus.RUNNING) {
			status = TaskStatus.CANCEL_REQUESTED;
			thread.stop();
		} else {
			logger.warning("Force stop execution of FlexoTask called for a task with status " + status);
		}
	}

	protected void finishedExecution() {
		if (status == TaskStatus.RUNNING) {
			thread.setTask(null);
			status = TaskStatus.FINISHED;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.RUNNING, status);
		} else if (status == TaskStatus.CANCEL_REQUESTED) {
			thread.setTask(null);
			status = TaskStatus.CANCELLED;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.CANCEL_REQUESTED, status);
		} else {
			logger.warning("Finished execution of FlexoTask called for a task with status " + status);
		}
	}

	public FlexoTaskThread getThread() {
		return thread;
	}

	public TaskStatus getTaskStatus() {
		return status;
	}

	public int getExpectedProgressSteps() {
		return expectedProgressSteps;
	}

	public void setExpectedProgressSteps(int expectedProgressSteps) {
		this.expectedProgressSteps = expectedProgressSteps;
		currentProgress = 0;
		getPropertyChangeSupport().firePropertyChange(EXPECTED_PROGRESS_STEPS_PROPERTY, 0, expectedProgressSteps);
	}

	public int getCurrentProgress() {
		return currentProgress;
	}

	public void progress() {
		currentProgress++;
		getPropertyChangeSupport().firePropertyChange(CURRENT_PROGRESS_PROPERTY, currentProgress - 1, currentProgress);
	}

}
