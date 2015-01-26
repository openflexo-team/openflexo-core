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
