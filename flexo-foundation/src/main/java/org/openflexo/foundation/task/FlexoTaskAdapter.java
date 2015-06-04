package org.openflexo.foundation.task;

import org.openflexo.gina.task.GenericTaskAdapter;
import org.openflexo.gina.task.GinaTaskThread;

public class FlexoTaskAdapter implements GenericTaskAdapter {

	@Override
	public boolean checkTask(Runnable r) {
		return r instanceof FlexoTask;
	}
	
	@Override
	public boolean checkThread(Thread t) {
		return t instanceof FlexoTaskThread;
	}
	
	@Override
	public GinaTaskThread createThread(ThreadGroup group, Runnable r, String name, long stackSize) {
		return new FlexoTaskThread(group, r, name, stackSize);
	}

}
