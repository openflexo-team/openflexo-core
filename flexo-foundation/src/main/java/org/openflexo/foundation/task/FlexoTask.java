/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.task;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a long-running task launch-able in OpenFlexo infrastructure<br>
 * A {@link FlexoTask} is executed in a given instance of {@link FlexoTaskManager}<br>
 * Life-cycle of a {@link FlexoTask} is this:
 * <ul>
 * <li>WAITING: at the creation of the task, and when execution of task is conditioned by the end of execution of some other tasks</li>
 * <li>READY_TO_EXECUTE: when the task is ready to execute (when all dependant tasks have finished their job)</li>
 * <li>RUNNING: while the task is being executed</li>
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
	public static final String TASK_BAR_SHOW = "showTaskBar";
	public static final String TASK_BAR_HIDE = "hideTaskBar";
	public static final String TASK_BAR_FORCE_HIDE = "forceHideTaskBar";

	private final String taskId;
	private final String taskTitle;
	private TaskStatus status;
	private FlexoTaskThread thread;

	private int currentProgress;
	private int expectedProgressSteps;
	private String currentStepName;

	private final List<FlexoTask> dependantTasks;

	// Progress replayed from the previous execution (see getProgressHistoryKey())
	private ProgressHistory previousHistory;
	private List<ProgressHistory.Milestone> milestones;
	private long startTime;
	private int matchedMilestone = -1;
	private long matchedTime;
	private double lastFraction;

	private Exception thrownException = null;

	public enum TaskStatus {
		WAITING {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("waiting");
			}
		},
		READY_TO_EXECUTE {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("ready_to_execute");
			}
		},
		RUNNING {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("running");
			}
		},
		FINISHED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("finished");
			}
		},
		EXCEPTION_THROWN {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("exception_thrown");
			}
		},
		CANCEL_REQUESTED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("cancel_requested");
			}
		},
		CANCELLED {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("cancelled");
			}
		},
		UNKNOWN {
			@Override
			public String getLocalizedName() {
				return FlexoLocalization.getMainLocalizer().localizedForKey("unknown_task_status");
			}
		};

		public abstract String getLocalizedName();
	}

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	/**
	 * 
	 * @param taskId
	 * @param title
	 */
	public FlexoTask(String taskId, String title) {
		this.taskId = taskId;
		this.taskTitle = title;
		status = TaskStatus.WAITING;
		dependantTasks = new ArrayList<>();
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getTaskTitle() {
		return taskTitle;
	}

	@Override
	public String toString() {
		return "<FlexoTask:" + getTaskTitle() + " status=" + getTaskStatus() + " depends of: " + dependantTasks + ">";
	}

	protected synchronized void startExecution(FlexoTaskThread thread) {
		if (status == TaskStatus.READY_TO_EXECUTE) {
			this.thread = thread;
			thread.setTask(this);
			status = TaskStatus.RUNNING;
			startProgressHistory();
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.WAITING, TaskStatus.READY_TO_EXECUTE);
		}
		else {
			logger.warning("Start execution of FlexoTask " + this + " called for a task with status " + status);
		}
	}

	protected synchronized void stopExecution() {
		if (status == TaskStatus.RUNNING) {
			status = TaskStatus.CANCEL_REQUESTED;
			thread.interrupt();
		}
		else {
			logger.warning("Stop execution of FlexoTask called for a task with status " + status);
		}
	}

	protected synchronized void finishedExecution() {
		if (status == TaskStatus.RUNNING) {
			thread.setTask(null);
			status = TaskStatus.FINISHED;
			storeProgressHistory();
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.RUNNING, status);
		}
		else if (status == TaskStatus.CANCEL_REQUESTED) {
			thread.setTask(null);
			status = TaskStatus.CANCELLED;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.CANCEL_REQUESTED, status);
		}
		else if (status == TaskStatus.EXCEPTION_THROWN) {
			thread.setTask(null);
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.RUNNING, status);
		}
		else {
			Thread.dumpStack();
			logger.warning("Finished execution of FlexoTask called for a task with status " + status);
		}
	}

	public synchronized void executionScheduled() {
		if (status == TaskStatus.WAITING) {
			status = TaskStatus.READY_TO_EXECUTE;
			getPropertyChangeSupport().firePropertyChange(TASK_STATUS_PROPERTY, TaskStatus.WAITING, status);
		}
		else {
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

	public synchronized void setTaskStatus(TaskStatus status) {
		if ((status == null && this.status != null) || (status != null && !status.equals(this.status))) {
			TaskStatus oldValue = this.status;
			this.status = status;
			getPropertyChangeSupport().firePropertyChange("status", oldValue, status);
		}
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
		incrementProgress();
		reachMilestone(null);
	}

	public void progress(String stepName) {
		incrementProgress();
		setCurrentStepName(stepName);
		reachMilestone(stepName);
	}

	private void incrementProgress() {
		currentProgress++;
		getPropertyChangeSupport().firePropertyChange(CURRENT_PROGRESS_PROPERTY, currentProgress - 1, currentProgress);
	}

	/**
	 * Return the key under which the milestones of an execution of this task are stored, to be replayed by the next execution
	 * (see {@link #getProgressFraction()}).<br>
	 * Default is null: the progress of the task is the counted steps ({@link #getCurrentProgress()} out of
	 * {@link #getExpectedProgressSteps()}). Override to give the task a progress linear in time.
	 * 
	 * @return a key identifying this kind of task, the same from one execution to the next
	 */
	public String getProgressHistoryKey() {
		return null;
	}

	/**
	 * Return true when this task is shown, with its progress bar, in the task panel of the user interface<br>
	 * Default is true. Override for a short task scheduled only to run after the others.
	 */
	public boolean isShownInTaskPanel() {
		return true;
	}

	/**
	 * Return the expected duration of this task, in milliseconds, used while no history of a previous execution exists
	 * 
	 * @return 10 seconds by default
	 */
	public long getDefaultExpectedDuration() {
		return 10000;
	}

	/**
	 * Return true when the progress of this task is computed from the history of its previous execution, in time
	 */
	public boolean hasTimedProgress() {
		return getProgressHistoryKey() != null;
	}

	private synchronized void startProgressHistory() {
		if (getProgressHistoryKey() != null) {
			previousHistory = ProgressHistory.load(getProgressHistoryKey());
			milestones = new ArrayList<>();
			startTime = System.currentTimeMillis();
			matchedMilestone = -1;
			matchedTime = 0;
			lastFraction = 0;
		}
	}

	private synchronized void reachMilestone(String name) {
		if (milestones == null) {
			return;
		}
		long now = System.currentTimeMillis() - startTime;
		milestones.add(new ProgressHistory.Milestone(name, now));
		if (previousHistory != null) {
			int index = previousHistory.indexOf(name, matchedMilestone + 1);
			if (index >= 0) {
				matchedMilestone = index;
				matchedTime = now;
			}
			// An unknown milestone changes nothing: the bar keeps moving towards the next known one
		}
	}

	private synchronized void storeProgressHistory() {
		if (milestones != null) {
			new ProgressHistory(milestones, System.currentTimeMillis() - startTime).store(getProgressHistoryKey());
			milestones = null;
		}
	}

	/**
	 * Return the progress of this task, between 0 and 1, never decreasing while the task runs.<br>
	 * 
	 * When the task has a {@link #getProgressHistoryKey()}, this progress is linear in time: each milestone (a call to
	 * {@link #progress(String)}) reached again places it at the fraction of the total duration that this milestone represented in the
	 * previous execution, and between two milestones it moves with time towards the next one, without passing it. Without any
	 * previous execution, it tends towards 1 according to {@link #getDefaultExpectedDuration()}.<br>
	 * 
	 * Otherwise, it is the counted steps.
	 * 
	 * @return the progress, between 0 and 1
	 */
	public synchronized double getProgressFraction() {
		if (status == TaskStatus.FINISHED) {
			return 1;
		}
		double fraction;
		if (milestones == null) {
			fraction = expectedProgressSteps > 0 ? Math.min(1.0, (double) currentProgress / expectedProgressSteps) : 0;
		}
		else {
			long now = System.currentTimeMillis() - startTime;
			if (previousHistory == null || previousHistory.getTotalDuration() <= 0) {
				// Never reached, approaches 1 asymptotically: 63% after the expected duration
				fraction = 1 - Math.exp(-(double) now / getDefaultExpectedDuration());
			}
			else {
				int next = matchedMilestone + 1;
				double from = previousHistory.getFraction(matchedMilestone);
				double to = previousHistory.getFraction(next);
				long expectedStepDuration = previousHistory.getTime(next) - previousHistory.getTime(matchedMilestone);
				double ratio = expectedStepDuration > 0 ? Math.min(1.0, (double) (now - matchedTime) / expectedStepDuration) : 1;
				fraction = from + (to - from) * ratio;
			}
			// Leave the end of the bar to the end of the task
			fraction = Math.min(fraction, 0.99);
		}
		lastFraction = Math.max(lastFraction, fraction);
		return lastFraction;
	}

	public void hideTaskBar() {
		getPropertyChangeSupport().firePropertyChange(TASK_BAR_HIDE, true, false);
	}

	public void showTaskBar() {
		getPropertyChangeSupport().firePropertyChange(TASK_BAR_SHOW, false, true);
	}

	public void forceHideTaskBar() {
		getPropertyChangeSupport().firePropertyChange(TASK_BAR_FORCE_HIDE, false, true);
	}

	public void stopForceHideTaskBar() {
		getPropertyChangeSupport().firePropertyChange(TASK_BAR_FORCE_HIDE, true, false);
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
		// status = TaskStatus.EXCEPTION_THROWN;
		setTaskStatus(TaskStatus.EXCEPTION_THROWN);
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
	public abstract void performTask() throws InterruptedException;

	/**
	 * Final run method<br>
	 * Please implement performTask()
	 * 
	 */
	@Override
	public void run() {
		try {
			performTask();
		} catch (InterruptedException e) {
			System.out.println("Tiens, je choppe bien l'interruption");
		} catch (Exception e) {
			throwException(e);
		}

	}
}
