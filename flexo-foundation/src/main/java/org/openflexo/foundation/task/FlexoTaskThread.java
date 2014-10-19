package org.openflexo.foundation.task;

public class FlexoTaskThread extends Thread {

	private FlexoTask task;

	public FlexoTaskThread(ThreadGroup group, Runnable r, String name, long stackSize) {
		super(group, r, name, stackSize);
	}

	public synchronized FlexoTask getTask() {
		return task;
	}

	public synchronized void setTask(FlexoTask task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return "FlexoTaskThread " + super.toString();
	}
}