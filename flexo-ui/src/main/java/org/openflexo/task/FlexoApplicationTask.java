/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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
