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

	public void stopExecution(FlexoTask task);

	@Deprecated
	public void forceStopExecution(FlexoTask task);

	public boolean isTerminated();

	public void shutdownAndWait();

	public void shutdown();

	public void shutdownAndExecute(final Runnable r);

	public List<FlexoTask> getScheduledTasks();

	/**
	 * Interrupts current thread until the task has finished<br>
	 * (more exactely, when the status of task is either of TaskStatus.WAITING, TaskStatus.RUNNING, TaskStatus.READY_TO_EXECUTE)
	 * 
	 * @param task
	 */
	public void waitTask(FlexoTask task);
}