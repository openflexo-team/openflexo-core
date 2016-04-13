/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.view.controller;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionEnableCondition;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.gina.ApplicationFIBLibrary;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.Resource;
import org.openflexo.view.FlexoFrame;

public abstract class ActionInitializer<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> {
	private final ControllerActionInitializer _controllerActionInitializer;
	private final FlexoActionType<A, T1, T2> _actionType;

	public ActionInitializer(FlexoActionType<A, T1, T2> actionType, ControllerActionInitializer controllerActionInitializer) {
		super();
		_controllerActionInitializer = controllerActionInitializer;
		_actionType = actionType;
		_controllerActionInitializer.registerInitializer(_actionType, this);
	}

	protected ControllerActionInitializer getControllerActionInitializer() {
		return _controllerActionInitializer;
	}

	public FlexoEditor getEditor() {
		return getControllerActionInitializer().getEditor();
	}

	public FlexoController getController() {
		return _controllerActionInitializer.getController();
	}

	public FlexoModule getModule() {
		return getController().getModule();
	}

	public FlexoProject getProject() {
		if (getEditor() != null) {
			return getEditor().getProject();
		}
		else {
			return null;
		}
	}

	/*public boolean instanciateAndShowDialog(Object object, File fibResource) {
		FIBComponent fibComponent = FIBLibrary.instance().retrieveFIBComponent(fibResource);
		FIBDialog dialog = FIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, getController()));
		return dialog.getStatus() == Status.VALIDATED;
	}*/

	public ApplicationFIBLibrary getApplicationFIBLibrary() {
		return getController().getApplicationContext().getApplicationFIBLibraryService().getApplicationFIBLibrary();
	}

	public boolean instanciateAndShowDialog(Object object, Resource fibResource) {
		FIBComponent fibComponent = getApplicationFIBLibrary().retrieveFIBComponent(fibResource);
		JFIBDialog dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getController()));
		return dialog.getStatus() == Status.VALIDATED;
	}

	/*public Status instanciateShowDialogAndReturnStatus(Object object, File fibResource) {
		FIBComponent fibComponent = FIBLibrary.instance().retrieveFIBComponent(fibResource);
		FIBDialog dialog = FIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, getController()));
		return dialog.getStatus();
	}*/

	public Status instanciateShowDialogAndReturnStatus(Object object, Resource fibResource) {
		FIBComponent fibComponent = getApplicationFIBLibrary().retrieveFIBComponent(fibResource);
		JFIBDialog dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getController()));
		return dialog.getStatus();
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionInitializer<A> getDefaultInitializer() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionFinalizer<A> getDefaultFinalizer() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoExceptionHandler<A> getDefaultExceptionHandler() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionEnableCondition<A, T1, T2> getEnableCondition() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionVisibleCondition<A, T1, T2> getVisibleCondition() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected KeyStroke getShortcut() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected Icon getEnabledIcon() {
		return null;
	}

	/**
	 * Please override if required Default implementation return null
	 * 
	 * @return null
	 */
	protected Icon getDisabledIcon() {
		return null;
	}

}
