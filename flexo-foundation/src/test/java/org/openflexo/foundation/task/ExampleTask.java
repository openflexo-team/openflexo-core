package org.openflexo.foundation.task;

import java.util.Random;

public class ExampleTask extends FlexoTask {

	public ExampleTask(String name) {
		super(name);
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(100);

		System.out.println(Thread.currentThread().getName() + " Start. Thread: " + Thread.currentThread());
		try {
			Thread.sleep((new Random(System.currentTimeMillis())).nextInt(2000) + 500);
			for (int i = 0; i < 100; i++) {
				Progress.progress();
				Thread.sleep(30);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " End.");
	}

	@Override
	public boolean isCancellable() {
		return true;
	}
}