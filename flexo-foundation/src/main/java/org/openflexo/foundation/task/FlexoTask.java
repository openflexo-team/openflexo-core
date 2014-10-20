package org.openflexo.foundation.task;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a long-running task launchable in Openflexo infrastructure<br>
 * A {@link FlexoTask} is executed in a given instance of {@link FlexoTaskManager}<br>
 * Life-cyle of a {@link FlexoTask} is this:
 * <ul>
 * <li>WAITING: at the creation of the task, and when execution of task is conditionned by the end of execution of some other tasks</li>
 * <li>READY_TO_EXECUTE: when the task is ready to execute (when all dependant tasks have finished their job)</li>
 * <li>RUNNING: while the task is beeing executed</li>
 * <li>FINISHED: when the task has successfully executed</li>
 * <li>EXCEPTION_THROWN: when the task has raised an exception</li>
 * <li>CANCEL_REQUESTED: when cancel has been requested for this task</li>
 * <li>CANCELLED: when the task has been cancelled (waiting tasks are also cancelled)</li>
 * </ul>
 * 
 * @author sylvain
 *
 */
public abstract class FlexoTask implements Runnable, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(FlexoTask.class.getPackage().getName());

	public static final String TASK_TITLE_PROPERTY = "taskTitle";
	public static final String TASK_STATUS_PROPERTY = "taskStatus";
	public static final String CURRENT_PROGRESS_PROPERTY = "currentProgress";
	public static final String EXPECTED_PROGRESS_STEPS_PROPERTY = "expectedProgressSteps";
	public static final String CURRENT_STEP_NAME_PROPERTY = "currentStepName";

	private final String taskTitle;
	private TaskStatus status;
	private FlexoTaskThread thread;

	private int currentProgress;
	private int expectedProgressSteps;
	private String currentStepName;

	private final List<FlexoTask> dependantTasks;

	private Exception thrownException = null;

	public enum TaskStatus {
		WAITING {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("waiting");
			}
		},
		READY_TO_EXECUTE {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("ready_to_execute");
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
		EXCEPTION_THROWN {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.localizedForKey("exception_thrown");
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
		dependantTasks = new ArrayList<FlexoTask>();
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

	protected synchronized void startExecution(FlexoTaskThread thread) {
		if (status == TaskStatus.READY_TO_EXECUTE) {
			this.thread = thread;
			thread.setTask(this);
			status = TaskStatus.RUNNING;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.WAITING, TaskStatus.READY_TO_EXECUTE);
		} else {
			logger.warning("Start execution of FlexoTask " + this + " called for a task with status " + status);
		}
	}

	protected synchronized void stopExecution() {
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

	protected synchronized void finishedExecution() {
		if (status == TaskStatus.RUNNING) {
			thread.setTask(null);
			status = TaskStatus.FINISHED;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.RUNNING, status);
		} else if (status == TaskStatus.CANCEL_REQUESTED) {
			thread.setTask(null);
			status = TaskStatus.CANCELLED;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.CANCEL_REQUESTED, status);
		} else if (status == TaskStatus.EXCEPTION_THROWN) {
			thread.setTask(null);
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.RUNNING, status);
		} else {
			Thread.dumpStack();
			logger.warning("Finished execution of FlexoTask called for a task with status " + status);
		}
	}

	public synchronized void executionScheduled() {
		if (status == TaskStatus.WAITING) {
			status = TaskStatus.READY_TO_EXECUTE;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.WAITING, status);
		} else {
			Thread.dumpStack();
			logger.warning("executionScheduled() of FlexoTask called for a task with status " + status);
		}
	}

	public FlexoTaskThread getThread() {
		return thread;
	}

	public synchronized TaskStatus getTaskStatus() {
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

	public String getCurrentStepName() {
		return currentStepName;
	}

	public void setCurrentStepName(String currentStepName) {
		if ((currentStepName == null && this.currentStepName != null)
				|| (currentStepName != null && !currentStepName.equals(this.currentStepName))) {
			String oldValue = this.currentStepName;
			this.currentStepName = currentStepName;
			getPropertyChangeSupport().firePropertyChange(CURRENT_STEP_NAME_PROPERTY, oldValue, currentStepName);
		}
	}

	public void progress() {
		currentProgress++;
		getPropertyChangeSupport().firePropertyChange(CURRENT_PROGRESS_PROPERTY, currentProgress - 1, currentProgress);
	}

	public void progress(String stepName) {
		progress();
		setCurrentStepName(stepName);
	}

	public void addToDependantTasks(FlexoTask task) {
		dependantTasks.add(task);
	}

	public void removeFromDependantTasks(FlexoTask task) {
		dependantTasks.remove(task);
	}

	public synchronized boolean isReadyToExecute() {
		if (getTaskStatus() != TaskStatus.WAITING) {
			return false;
		}
		for (FlexoTask t : dependantTasks) {
			if (t.getTaskStatus() != TaskStatus.FINISHED) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return boolean indicating if this task support cancel operation<br>
	 * Default value is false<br>
	 * Please override when supporting cancel operation
	 * 
	 * @return
	 */
	public boolean isCancellable() {
		return false;
	}

	/**
	 * Called to explicitely throw an Exception.<br>
	 * The task will be abnormaly interrupted with EXCEPTION_THROWN status
	 * 
	 * @param e
	 *            exception to be thrown
	 */
	public void throwException(Exception e) {
		thrownException = e;
		e.printStackTrace();
		status = TaskStatus.EXCEPTION_THROWN;
		thread.interrupt();
	}

	/**
	 * Return exception that has been thrown by the task, null if no exception was thrown (success ?)
	 * 
	 * @return
	 */
	public Exception getThrownException() {
		return thrownException;
	}

	/**
	 * Here comes the code that should be executed in task
	 */
	public abstract void performTask();

	/**
	 * Final run method<br>
	 * Please implement performTask()
	 * 
	 */
	@Override
	public final void run() {
		try {
			performTask();
		} catch (Exception e) {
			throwException(e);
		}

	}
}
