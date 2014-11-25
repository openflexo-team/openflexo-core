package org.openflexo.foundation.task;

import java.util.List;

import org.openflexo.foundation.FlexoService;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A {@link FlexoTaskManager} controls and monitors the execution of some {@link FlexoTask} where some dependancies between tasks might be
 * declared.<br>
 * The implementation of a {@link FlexoTaskManager} may vary and can be either multithreaded or monothreaded
 * 
 * @author sylvain
 *
 */
public interface FlexoTaskManager extends FlexoService, HasPropertyChangeSupport {

	public static final String SCHEDULED_TASK_PROPERTY = "scheduledTasks";

	/**
	 * Sequentially execute supplied tasks
	 * 
	 * @param tasks
	 */
	public abstract void scheduleExecution(FlexoTask... tasks);

	/**
	 * Request stop execution of a task<br>
	 * This is a 'cancel' request
	 * 
	 */
	public void stopExecution(FlexoTask task);

	/**
	 * Return a list of all tasks beeing scheduled for execution (more exactely, when the status of tasks is either of TaskStatus.WAITING,
	 * TaskStatus.RUNNING, TaskStatus.READY_TO_EXECUTE) *
	 * 
	 * @return
	 */
	public List<FlexoTask> getScheduledTasks();

	/**
	 * Interrupts current thread until the task has finished<br>
	 * (more exactely, when the status of task is either of TaskStatus.WAITING, TaskStatus.RUNNING, TaskStatus.READY_TO_EXECUTE)
	 * 
	 * @param task
	 */
	public void waitTask(FlexoTask task);

	/**
	 * Shutdown the task manager, let all running and scheduled tasks been executed
	 */
	public void shutdown();

	/**
	 * Shutdown the task manager, and blocks current thread until all tasks have been executed
	 */
	public void shutdownAndWait();

	/**
	 * Shutdown the task manager, and blocks a new thread until all tasks have been executed, then execute runnable in this thread
	 */
	public void shutdownAndExecute(final Runnable r);

	/**
	 * Return boolean indicating if all tasks (scheduled and/or running) have finished and if task manager is ready for shutdown
	 * 
	 * @return
	 */
	public boolean isTerminated();

	/**
	 * Should not be used as it uses {@link #Thread.stop()} to force interruption of task<br>
	 * Consistency is no more guaranteed<br>
	 * Use this only as rescue service
	 * 
	 * @param task
	 */
	@Deprecated
	public void forceStopExecution(FlexoTask task);

}