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

import org.openflexo.ApplicationContext;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionEnableCondition;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.gina.ApplicationFIBLibrary;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.Resource;
import org.openflexo.view.FlexoFrame;

/**
 * An {@link ActionInitializer} allows to define the integration of the execution of a {@link FlexoAction} in a given editing
 * environment<br>
 * 
 * For a given ControllerActionInitializer it provides hooks for custom initializers, finalizers, enable and visible condition as well as
 * exception handlers for various contexts.<br>
 * 
 * An {@link ActionInitializer} is either register to a {@link FlexoActionFactory} or a {@link FlexoAction} class
 * 
 * @see ControllerActionInitializer
 * 
 * @author sylvain
 *
 * @param <A>
 *            type of FlexoAction
 * @param <T1>
 *            type of object such {@link FlexoAction} is to be applied as focused object
 * @param <T2>
 *            type of additional object such {@link FlexoAction} is to be applied as global selection
 */
public abstract class ActionInitializer<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> {

	private final ControllerActionInitializer controllerActionInitializer;
	private FlexoActionFactory<A, T1, T2> actionFactory;
	private Class<A> actionType;

	public ActionInitializer(FlexoActionFactory<A, T1, T2> actionFactory, ControllerActionInitializer controllerActionInitializer) {
		super();
		this.controllerActionInitializer = controllerActionInitializer;
		this.actionFactory = actionFactory;
		controllerActionInitializer.registerInitializer(actionFactory, this);
	}

	public ActionInitializer(Class<A> actionType, ControllerActionInitializer controllerActionInitializer) {
		super();
		this.controllerActionInitializer = controllerActionInitializer;
		this.actionType = actionType;
		controllerActionInitializer.registerInitializer(actionType, this);
	}

	public FlexoActionFactory<A, T1, T2> getActionFactory() {
		return actionFactory;
	}

	@SuppressWarnings("unchecked")
	public Class<A> getActionType() {
		if (getActionFactory() != null) {
			return (Class<A>) TypeUtils.getTypeArgument(actionFactory.getClass(), FlexoActionFactory.class, 0);
		}
		return actionType;
	}

	protected ControllerActionInitializer getControllerActionInitializer() {
		return controllerActionInitializer;
	}

	public FlexoEditor getEditor() {
		return getControllerActionInitializer().getEditor();
	}

	public FlexoController getController() {
		return controllerActionInitializer.getController();
	}

	public FlexoModule<?> getModule() {
		return getController().getModule();
	}

	public FlexoProject<?> getProject() {
		if (getEditor() != null) {
			return getEditor().getProject();
		}
		else {
			return null;
		}
	}

	public ApplicationFIBLibrary getApplicationFIBLibrary() {
		return getController().getApplicationContext().getApplicationFIBLibraryService().getApplicationFIBLibrary();
	}

	public boolean instanciateAndShowDialog(Object object, Resource fibResource) {
		FIBComponent fibComponent = getApplicationFIBLibrary().retrieveFIBComponent(fibResource);
		JFIBDialog<?> dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getController()));
		return dialog.getStatus() == Status.VALIDATED;
	}

	public Status instanciateShowDialogAndReturnStatus(Object object, Resource fibResource) {
		FIBComponent fibComponent = getApplicationFIBLibrary().retrieveFIBComponent(fibResource);
		JFIBDialog<?> dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, object, FlexoFrame.getActiveFrame(), true,
				new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getController()));
		return dialog.getStatus();
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionRunnable<A, T1, T2> getDefaultInitializer() {
		return (e, action) -> true;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionRunnable<A, T1, T2> getDefaultFinalizer() {
		return (e, action) -> true;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoExceptionHandler<A, T1, T2> getDefaultExceptionHandler() {
		return null;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionEnableCondition<A, T1, T2> getEnableCondition() {
		return null;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected FlexoActionVisibleCondition<A, T1, T2> getVisibleCondition() {
		return null;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @return null
	 */
	protected KeyStroke getShortcut() {
		return null;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @param actionFactory
	 * 
	 * @return null
	 */
	protected Icon getEnabledIcon(FlexoActionFactory<A, T1, T2> actionFactory) {
		return null;
	}

	/**
	 * Please override if required<br>
	 * Default implementation return null
	 * 
	 * @param actionFactory
	 * 
	 * @return null
	 */
	protected Icon getDisabledIcon(FlexoActionFactory<A, T1, T2> actionFactory) {
		return null;
	}

	public LocalizedDelegate getModuleLocales(FlexoAction<A, T1, T2> action) {
		if (action != null) {
			if (action.getServiceManager() instanceof ApplicationContext) {
				return ((ApplicationContext) action.getServiceManager()).getModuleLoader().getActiveModule().getLocales();
			}
			else {
				return action.getLocales();
			}
		}
		return FlexoLocalization.getMainLocalizer();
	}

	public LocalizedDelegate getActionLocales(FlexoAction<A, T1, T2> action) {
		if (action != null) {
			return action.getLocales();
		}
		return FlexoLocalization.getMainLocalizer();
	}

}
