package org.openflexo.foundation.task;

import java.util.Random;

public class ErrorTask extends FlexoTask {

	public ErrorTask(String name) {
		super(name);
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(10);

		try {
			Thread.sleep((new Random(System.currentTimeMillis())).nextInt(2000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[] someInts = new int[1];

		int v = someInts[2];
	}

}