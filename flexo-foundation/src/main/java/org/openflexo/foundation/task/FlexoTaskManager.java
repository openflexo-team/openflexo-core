package org.openflexo.foundation.task;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openflexo.toolbox.HasPropertyChangeSupport;

public class FlexoTaskManager implements HasPropertyChangeSupport {

	private static final int DEFAULT_THREAD_POOL_SIZE = 5;

	public static final String SCHEDULED_TASK_PROPERTY = "scheduledTasks";

	private final FlexoThreadFactory threadFactory;
	private final ExecutorService executor;

	private final List<FlexoTask> scheduledTasks;

	private final PropertyChangeSupport pcSupport;

	public FlexoTaskManager() {
		this(DEFAULT_THREAD_POOL_SIZE);
	}

	public FlexoTaskManager(int threadPoolSize) {

		pcSupport = new PropertyChangeSupport(this);

		threadFactory = new FlexoThreadFactory();
		// executor = Executors.newFixedThreadPool(5, threadFactory);

		executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				threadFactory) {
			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				if (t instanceof FlexoTaskThread && r instanceof FlexoTask) {
					((FlexoTask) r).startExecution((FlexoTaskThread) t);
					// System.out.println("Executing " + r + " in thread " + t);
				}
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				if (r instanceof FlexoTask) {
					scheduledTasks.remove(r);
					((FlexoTask) r).finishedExecution();
					// System.out.println("Finished executing " + r);
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
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public void scheduleExecution(FlexoTask task) {
		if (isTerminated()) {
			return;
		}
		scheduledTasks.add(task);
		getPropertyChangeSupport().firePropertyChange(SCHEDULED_TASK_PROPERTY, null, task);
		executor.execute(task);
	}

	public void stopExecution(FlexoTask task) {
		task.stopExecution();
		// task.getThread().interrupt();
		// task.finishedExecution();
		// scheduledTasks.remove(task);
	}

	@Deprecated
	public void forceStopExecution(FlexoTask task) {
		task.forceStopExecution();
		// task.getThread().stop();
		// task.finishedExecution();
		scheduledTasks.remove(task);
	}

	public boolean isTerminated() {
		return (executor.isTerminated() && scheduledTasks.size() == 0);
	}

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

	public void shutdown() {
		executor.shutdown();
	}

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

	public List<FlexoTask> getScheduledTasks() {
		return scheduledTasks;
	}

}