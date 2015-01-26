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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;

public class ThreadPoolFlexoTaskManager extends FlexoServiceImpl implements FlexoTaskManager {

	private static final int DEFAULT_THREAD_POOL_SIZE = 5;

	private final FlexoThreadFactory threadFactory;
	private final ExecutorService executor;

	private final List<FlexoTask> scheduledTasks;

	private final PropertyChangeSupport pcSupport;

	public static ThreadPoolFlexoTaskManager createInstance(int threadPoolSize) {
		return new ThreadPoolFlexoTaskManager(threadPoolSize);
	}

	public static ThreadPoolFlexoTaskManager createInstance() {
		return new ThreadPoolFlexoTaskManager(DEFAULT_THREAD_POOL_SIZE);
	}

	private ThreadPoolFlexoTaskManager(int threadPoolSize) {

		pcSupport = new PropertyChangeSupport(this);

		threadFactory = new FlexoThreadFactory();

		executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				threadFactory) {
			@Override
			protected synchronized void beforeExecute(Thread t, Runnable r) {
				if (t instanceof FlexoTaskThread && r instanceof FlexoTask) {
					((FlexoTask) r).startExecution((FlexoTaskThread) t);
					System.out.println("Executing " + r + " in thread " + t);
				}
			}

			@Override
			protected synchronized void afterExecute(Runnable task, Throwable t) {
				if (task instanceof FlexoTask) {
					scheduledTasks.remove(task);
					getPropertyChangeSupport().firePropertyChange(SCHEDULED_TASK_PROPERTY, task, null);
					((FlexoTask) task).finishedExecution();
					System.out.println("Finished executing " + task);
					launchReadyToExecuteTasks();
				}
			}

			/*@Override
			protected <V> RunnableFuture<V> newTaskFor(final Runnable runnable, V v) {
				return new FutureTask<V>(runnable, v) {
					@Override
					public String toString() {
						return runnable.toString();
					}
				};
			};*/
		};

		scheduledTasks = new ArrayList<FlexoTask>();
	}

	@Override
	public void initialize() {
		// Nothing to do
		logger.info("FlexoTaskManager has been initialized");
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	/**
	 * Sequentially execute supplied tasks
	 * 
	 * @param tasks
	 */
	@Override
	public synchronized void scheduleExecution(FlexoTask... tasks) {
		if (isTerminated()) {
			return;
		}
		FlexoTask previous = null;
		for (FlexoTask task : tasks) {
			if (previous != null) {
				task.addToDependantTasks(previous);
			}
			scheduledTasks.add(task);
			getPropertyChangeSupport().firePropertyChange(SCHEDULED_TASK_PROPERTY, null, task);
			previous = task;
		}

		launchReadyToExecuteTasks();
	}

	private synchronized void launchReadyToExecuteTasks(FlexoTask... ignoredTasks) {
		System.out.println("launchReadyToExecuteTasks()");
		for (FlexoTask task : getScheduledTasks()) {
			if (task.isReadyToExecute()) {
				boolean ignored = false;
				if (ignoredTasks.length > 0) {
					for (int i = 0; i < ignoredTasks.length; i++) {
						FlexoTask ignoredTask = ignoredTasks[i];
						if (task == ignoredTasks[i]) {
							System.out.println("Ignoring " + task);
							ignored = true;
						}
					}
				}
				if (!ignored) {
					System.out.println("Task " + task + " is ready to execute");
					task.executionScheduled();
					executor.execute(task);
				} else {
					System.out.println("Task " + task + " is to be ignored");
				}
			} else {
				System.out.println("Task " + task + " is NOT ready to execute");
			}
		}
	}

	@Override
	public void stopExecution(FlexoTask task) {
		task.stopExecution();
		// task.getThread().interrupt();
		// task.finishedExecution();
		// scheduledTasks.remove(task);
	}

	@Override
	@Deprecated
	public void forceStopExecution(FlexoTask task) {
		task.forceStopExecution();
		// task.getThread().stop();
		// task.finishedExecution();
		scheduledTasks.remove(task);
	}

	@Override
	public boolean isTerminated() {
		return (executor.isTerminated() && scheduledTasks.size() == 0);
	}

	@Override
	public void shutdownAndWait() {
		executor.shutdown();

		while (!isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// System.out.println("Finished all threads");
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public void shutdownAndExecute(final Runnable r) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				shutdownAndWait();
				r.run();
			}
		}, "Shutdown");
		t.start();
	}

	@Override
	public synchronized List<FlexoTask> getScheduledTasks() {
		return scheduledTasks;
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		// logger.info(getClass().getSimpleName() + " service received notification " + notification + " from " + caller);
	}

	@Override
	public void waitTask(FlexoTask task) {
		if (task.getTaskStatus() != TaskStatus.WAITING && task.getTaskStatus() != TaskStatus.RUNNING
				&& task.getTaskStatus() != TaskStatus.READY_TO_EXECUTE) {
			return;
		}
		while (task.getTaskStatus() == TaskStatus.WAITING || task.getTaskStatus() == TaskStatus.RUNNING
				|| task.getTaskStatus() == TaskStatus.READY_TO_EXECUTE) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return;
	}

}
