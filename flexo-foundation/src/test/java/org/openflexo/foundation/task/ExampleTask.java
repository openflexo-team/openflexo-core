package org.openflexo.foundation.task;

import java.util.Random;

public class ExampleTask extends FlexoTask {

	public ExampleTask(String name) {
		super(name);
	}

	@Override
	public void run() {

		Progress.setExpectedProgressSteps(10);

		System.out.println(Thread.currentThread().getName() + " Start. Thread: " + Thread.currentThread());
		try {
			Thread.sleep((new Random(System.currentTimeMillis())).nextInt(2000));
			for (int i = 0; i < 10; i++) {
				Progress.progress();
				Thread.sleep(300);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " End.");
	}
}