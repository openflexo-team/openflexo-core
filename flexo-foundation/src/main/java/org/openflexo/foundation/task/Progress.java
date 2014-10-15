package org.openflexo.foundation.task;

public class Progress {

	/*public static void startTask() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof FlexoTaskThread) {
			System.out.println("Hop on commence la tache " + ((FlexoTaskThread) currentThread).getTask());
		}
	}

	public static void endTask() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof FlexoTaskThread) {
			System.out.println("Hop on termine la tache " + ((FlexoTaskThread) currentThread).getTask());
		}
	}*/

	public static void setExpectedProgressSteps(int expectedProgressSteps) {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof FlexoTaskThread) {
			((FlexoTaskThread) currentThread).getTask().setExpectedProgressSteps(expectedProgressSteps);
		}
	}

	public static void progress() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof FlexoTaskThread) {
			((FlexoTaskThread) currentThread).getTask().progress();
		}
	}
}
