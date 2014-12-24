package org.openflexo.task;

import javax.swing.SwingUtilities;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.view.controller.FlexoController;

/**
 * An abstract task used in the context of application.<br>
 * Thrown exceptions are managed here
 * 
 * @author sylvain
 *
 */
public abstract class FlexoApplicationTask extends FlexoTask {

	private final FlexoService service;

	public FlexoApplicationTask(String title, FlexoService service) {
		super(title);
		this.service = service;
	}

	public FlexoService getService() {
		return service;
	}

	public FlexoServiceManager getServiceManager() {
		if (getService() != null) {
			return getService().getServiceManager();
		}
		return null;
	}

	@Override
	protected synchronized void finishedExecution() {
		super.finishedExecution();

		if (getTaskStatus() == TaskStatus.EXCEPTION_THROWN) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					notifyThrownException(getThrownException());
				}
			});
		}
	}

	// Please override to get better user feedback
	protected void notifyThrownException(Exception e) {
		showException("Unexpected exception", "Unexpected exception occurs", e);
		e.printStackTrace();
	}

	protected void showException(String title, String message, Exception e) {
		FlexoController.showError(title, "<html><b>" + message + "</b><br><i>Exception</i>: " + e.getClass().getSimpleName()
				+ "<br><i>Message</i>: " + e.getMessage() + "</html>");
	}
}