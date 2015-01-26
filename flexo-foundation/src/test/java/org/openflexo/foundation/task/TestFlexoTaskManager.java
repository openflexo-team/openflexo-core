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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;

public class TestFlexoTaskManager {

	public static void main(String[] args) {

		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance();

		for (int i = 0; i < 10; i++) {
			ExampleTask task = new ExampleTask("ExampleTask" + i);
			taskManager.scheduleExecution(task);
		}

		taskManager.shutdownAndWait();

	}

	/**
	 * First basic test<br>
	 * We schedule some tasks and check they execute correctely
	 */
	@Test
	public void test1() {

		ExampleTask[] tasks = new ExampleTask[5];

		// Instanciate a TaskManager with a thread pool of 3 threads
		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(3);

		// Launch the threads
		for (int i = 0; i < 5; i++) {
			tasks[i] = new ExampleTask("ExampleTask" + i);
			taskManager.scheduleExecution(tasks[i]);
		}

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 5; i++) {
			System.out.println("status for task " + i + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.RUNNING, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[4].getTaskStatus());*/

		// Wait until three tasks have finished their job
		while (tasks[0].getTaskStatus().equals(TaskStatus.RUNNING) || tasks[1].getTaskStatus().equals(TaskStatus.RUNNING)
				|| tasks[2].getTaskStatus().equals(TaskStatus.RUNNING)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 5; i++) {
			System.out.println("status for task " + i + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.FINISHED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[4].getTaskStatus());*/

		taskManager.shutdownAndWait();

		assertTrue(taskManager.isTerminated());

		assertEquals(TaskStatus.FINISHED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[4].getTaskStatus());

	}

	/**
	 * First basic test<br>
	 * We schedule some tasks and check they execute correctely<br>
	 * Meanwhile, we launch some other tasks
	 */
	@Test
	public void test2() {

		ExampleTask[] tasks = new ExampleTask[8];

		// Instanciate a TaskManager with a thread pool of 3 threads
		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(3);

		// Launch the threads
		for (int i = 0; i < 5; i++) {
			tasks[i] = new ExampleTask("ExampleTask" + i);
			taskManager.scheduleExecution(tasks[i]);
		}

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 5; i++) {
			System.out.println("status for task " + tasks[i].getTaskTitle() + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.RUNNING, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[4].getTaskStatus());*/

		// Wait until three tasks have finished their job
		while (tasks[0].getTaskStatus().equals(TaskStatus.RUNNING) || tasks[1].getTaskStatus().equals(TaskStatus.RUNNING)
				|| tasks[2].getTaskStatus().equals(TaskStatus.RUNNING)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 5; i++) {
			System.out.println("status for task " + tasks[i].getTaskTitle() + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.FINISHED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[4].getTaskStatus());*/

		// Now we launch some more tasks

		// We now check status for running threads, two tasks should be waiting
		for (int i = 5; i < 8; i++) {
			tasks[i] = new ExampleTask("ExampleTaskLaunchedWithADelay" + i);
			taskManager.scheduleExecution(tasks[i]);
		}

		// Let the thread be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 8; i++) {
			System.out.println("status for task " + tasks[i].getTaskTitle() + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.FINISHED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[4].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[5].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[6].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[7].getTaskStatus());*/

		taskManager.shutdownAndWait();

		assertTrue(taskManager.isTerminated());

		assertEquals(TaskStatus.FINISHED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[4].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[5].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[6].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[7].getTaskStatus());

	}

	/**
	 * First basic test<br>
	 * We schedule some tasks and try to interrupt one
	 */
	@Test
	public void test3() {

		ExampleTask[] tasks = new ExampleTask[5];

		// Instanciate a TaskManager with a thread pool of 3 threads
		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(3);

		// Launch the threads
		for (int i = 0; i < 5; i++) {
			tasks[i] = new ExampleTask("ExampleTask" + i);
			taskManager.scheduleExecution(tasks[i]);
		}

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// We now check status for running threads, two tasks should be waiting
		for (int i = 0; i < 5; i++) {
			System.out.println("status for task " + i + ": " + tasks[i].getTaskStatus());
		}

		/*assertEquals(TaskStatus.RUNNING, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[4].getTaskStatus());*/

		taskManager.stopExecution(tasks[0]);

		/*while (tasks[0].getTaskStatus() == TaskStatus.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/

		/*assertEquals(TaskStatus.CANCEL_REQUESTED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.RUNNING, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.WAITING, tasks[4].getTaskStatus());*/

		taskManager.shutdownAndWait();

		assertTrue(taskManager.isTerminated());

		assertEquals(TaskStatus.CANCELLED, tasks[0].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[1].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[2].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[3].getTaskStatus());
		assertEquals(TaskStatus.FINISHED, tasks[4].getTaskStatus());

	}

	/**
	 * First basic test<br>
	 * We schedule some sequential tasks
	 */
	@Test
	public void test4() {

		// Instanciate a TaskManager with a thread pool of 3 threads
		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(3);

		ExampleTask sequentialTask1 = new ExampleTask("SequentialTask1");
		ExampleTask sequentialTask2 = new ExampleTask("SequentialTask2");
		ExampleTask sequentialTask3 = new ExampleTask("SequentialTask3");
		taskManager.scheduleExecution(sequentialTask1, sequentialTask2, sequentialTask3);

		// Let the threads be started
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(TaskStatus.RUNNING, sequentialTask1.getTaskStatus());
		assertEquals(TaskStatus.WAITING, sequentialTask2.getTaskStatus());
		assertEquals(TaskStatus.WAITING, sequentialTask3.getTaskStatus());

		taskManager.waitTask(sequentialTask1);

		assertEquals(TaskStatus.FINISHED, sequentialTask1.getTaskStatus());
		assertEquals(TaskStatus.RUNNING, sequentialTask2.getTaskStatus());
		assertEquals(TaskStatus.WAITING, sequentialTask3.getTaskStatus());

		taskManager.waitTask(sequentialTask2);

		assertEquals(TaskStatus.FINISHED, sequentialTask1.getTaskStatus());
		assertEquals(TaskStatus.FINISHED, sequentialTask2.getTaskStatus());
		assertEquals(TaskStatus.RUNNING, sequentialTask3.getTaskStatus());

		taskManager.waitTask(sequentialTask3);

		assertEquals(TaskStatus.FINISHED, sequentialTask1.getTaskStatus());
		assertEquals(TaskStatus.FINISHED, sequentialTask2.getTaskStatus());
		assertEquals(TaskStatus.FINISHED, sequentialTask3.getTaskStatus());

		taskManager.shutdownAndWait();

		assertTrue(taskManager.isTerminated());

	}

	/**
	 * First basic test<br>
	 * We schedule a task which execution throws an exception
	 */
	@Test
	public void test5() {

		// Instanciate a TaskManager with a thread pool of 3 threads
		final FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(3);

		ErrorTask task = new ErrorTask("ErrorTask");
		taskManager.scheduleExecution(task);

		taskManager.waitTask(task);

		assertEquals(TaskStatus.EXCEPTION_THROWN, task.getTaskStatus());

		assertTrue(task.getThrownException() instanceof ArrayIndexOutOfBoundsException);

	}
}
