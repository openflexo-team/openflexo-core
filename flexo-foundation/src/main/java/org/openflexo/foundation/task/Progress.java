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

/**
 * Static entry point through which the code executed by a {@link FlexoTask} reports its progress, without holding the task.<br>
 * The task is the one executed by the current {@link FlexoTaskThread}, or the one bound to the current thread with
 * {@link #bindToCurrentThread(FlexoTask)} (the startup of the application, executed in the main thread). Elsewhere, calls do nothing.
 */
public class Progress {

	private static final ThreadLocal<FlexoTask> BOUND_TASK = new ThreadLocal<>();

	/**
	 * Report the progress of the current thread to supplied task, which is not executed by a {@link FlexoTaskManager}
	 */
	public static void bindToCurrentThread(FlexoTask task) {
		BOUND_TASK.set(task);
	}

	public static void unbindFromCurrentThread() {
		BOUND_TASK.remove();
	}

	private static FlexoTask currentTask() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof FlexoTaskThread) {
			return ((FlexoTaskThread) currentThread).getTask();
		}
		return BOUND_TASK.get();
	}

	public static void setExpectedProgressSteps(int expectedProgressSteps) {
		FlexoTask task = currentTask();
		if (task != null) {
			task.setExpectedProgressSteps(expectedProgressSteps);
		}
	}

	public static void progress() {
		FlexoTask task = currentTask();
		if (task != null) {
			task.progress();
		}
	}

	public static void progress(String stepName) {
		FlexoTask task = currentTask();
		if (task != null) {
			task.progress(stepName);
		}
	}

	public static void hideTaskBar() {
		FlexoTask task = currentTask();
		if (task != null) {
			task.hideTaskBar();
		}
	}

	public static void showTaskBar() {
		FlexoTask task = currentTask();
		if (task != null) {
			task.showTaskBar();
		}
	}

	public static void forceHideTaskBar() {
		FlexoTask task = currentTask();
		if (task != null) {
			task.forceHideTaskBar();
		}
	}

	public static void stopForceHideTaskBar() {
		FlexoTask task = currentTask();
		if (task != null) {
			task.stopForceHideTaskBar();
		}
	}

}
