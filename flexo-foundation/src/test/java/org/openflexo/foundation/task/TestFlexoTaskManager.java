package org.openflexo.foundation.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;

public class TestFlexoTaskManager {

	public static void main(String[] args) {

		final FlexoTaskManager taskManager = new FlexoTaskManager();

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
		final FlexoTaskManager taskManager = new FlexoTaskManager(3);

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
		final FlexoTaskManager taskManager = new FlexoTaskManager(3);

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
		final FlexoTaskManager taskManager = new FlexoTaskManager(3);

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

}