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
import java.util.List;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.gina.task.GenericTaskThreadPool;

public class ThreadPoolFlexoTaskManager extends FlexoServiceImpl implements FlexoTaskManager {

	private static final int DEFAULT_THREAD_POOL_SIZE = 5;

	private GenericTaskThreadPool<FlexoTask, FlexoTaskThread> threadPool;

	public static ThreadPoolFlexoTaskManager createInstance(int threadPoolSize) {
		return new ThreadPoolFlexoTaskManager(threadPoolSize);
	}

	public static ThreadPoolFlexoTaskManager createInstance() {
		return new ThreadPoolFlexoTaskManager(DEFAULT_THREAD_POOL_SIZE);
	}

	private ThreadPoolFlexoTaskManager(int threadPoolSize) {
		threadPool = new GenericTaskThreadPool<FlexoTask, FlexoTaskThread>(threadPoolSize, new FlexoTaskAdapter(), this);
	}

	@Override
	public void initialize() {
		// Nothing to do
		logger.info("FlexoTaskManager has been initialized");
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return threadPool.getPropertyChangeSupport();
	}

	@Override
	public String getDeletedProperty() {
		return threadPool.getDeletedProperty();
	}

	/**
	 * Sequentially execute supplied tasks
	 * 
	 * @param tasks
	 */
	@Override
	public synchronized void scheduleExecution(FlexoTask... tasks) {
		threadPool.scheduleExecution(tasks);
	}

	@Override
	public void stopExecution(FlexoTask task) {
		threadPool.stopExecution(task);
	}

	@Override
	@Deprecated
	public void forceStopExecution(FlexoTask task) {
		threadPool.forceStopExecution(task);
	}

	@Override
	public boolean isTerminated() {
		return threadPool.isTerminated();
	}

	@Override
	public void shutdownAndWait() {
		threadPool.shutdownAndWait();
	}

	@Override
	public void stop() {
		threadPool.stop();
	}
	

	@Override
	public void shutdownAndExecute(final Runnable r) {
		threadPool.shutdownAndExecute(r);
	}

	@Override
	public synchronized List<FlexoTask> getScheduledTasks() {
		return threadPool.getScheduledTasks();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		// logger.info(getClass().getSimpleName() + " service received notification " + notification + " from " + caller);
	}

	@Override
	public void waitTask(FlexoTask task) {
		threadPool.waitTask(task);
	}

}
