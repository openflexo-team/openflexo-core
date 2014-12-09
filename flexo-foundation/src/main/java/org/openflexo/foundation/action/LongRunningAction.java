package org.openflexo.foundation.action;

import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTaskManager;

/**
 * Implemented by {@link FlexoAction} classes which should be invoked as a {@link FlexoTask} in the {@link FlexoTaskManager}<br>
 * 
 * Developpers should be aware of specific issues related to those long-running actions, especially multi-threading and synchronization
 * issues. Also think of cancellation.
 * 
 * @author sylvain
 *
 */
public interface LongRunningAction {

	public int getExpectedProgressSteps();
}
