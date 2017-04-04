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

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openflexo.ApplicationContext;
import org.openflexo.components.ProgressWindow;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoAction.ExecutionStatus;
import org.openflexo.foundation.action.FlexoActionEnableCondition;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.FlexoGUIAction;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.LongRunningAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionType;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceUpdateHandler;
import org.openflexo.foundation.task.LongRunningActionTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.foundation.utils.FlexoProgressFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;

/**
 * Interactive implementation of a {@link FlexoEditor}
 * 
 * @author sylvain
 *
 */
public class InteractiveFlexoEditor extends DefaultFlexoEditor {

	private static final Logger logger = FlexoLogger.getLogger(InteractiveFlexoEditor.class.getPackage().getName());

	private ScenarioRecorder _scenarioRecorder;

	private final FlexoProgressFactory _progressFactory;

	private final ApplicationContext applicationContext;

	private Map<FlexoModule, ControllerActionInitializer> actionInitializers;

	public InteractiveFlexoEditor(ApplicationContext applicationContext, FlexoProject project) {
		super(project, applicationContext);
		this.applicationContext = applicationContext;
		actionInitializers = new Hashtable<>();
		if (ScenarioRecorder.ENABLE) {
			_scenarioRecorder = new ScenarioRecorder();
		}
		_progressFactory = new FlexoProgressFactory() {
			@Override
			public FlexoProgress makeFlexoProgress(String title, int steps) {
				return ProgressWindow.makeProgressWindow(title, steps);
			}
		};

	}

	private ModuleLoader getModuleLoader() {
		return applicationContext.getModuleLoader();
	}

	@Override
	public ResourceUpdateHandler getResourceUpdateHandler() {
		return new ResourceUpdateHandler() {
			@Override
			public void resourceChanged(FlexoResource<?> resource) {
				// TODO to be implemented !
				logger.warning("Please implement resource update handler");
			}
		};
	}

	@Override
	public boolean isInteractive() {
		return true;
	}

	@Override
	public <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performAction(
			final A action, final EventObject e) {
		// NPE Protection
		if (action != null) {
			FlexoActionType<A, T1, T2> at = action.getActionType();
			if (at != null) {
				if (!action.getActionType().isEnabled(action.getFocusedObject(), action.getGlobalSelection())) {
					return null;
				}
				if (!(action instanceof FlexoGUIAction<?, ?, ?>) && (action.getFocusedObject() instanceof FlexoProjectObject)
						&& ((FlexoProjectObject) action.getFocusedObject()).getProject() != getProject()) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Cannot execute action because focused object is within another project than the one of this editor");
					}
					return null;
				}

				executeAction(action, e);
				return action;
			}
			else {
				logger.warning("Action Type was NULL!");
				return null;
			}
		}
		else {
			logger.warning("Action was NULL!");
			return null;
		}
	}

	/**
	 * This is the CORE execution method
	 * 
	 * @param action
	 * @param event
	 * @return
	 */
	private <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A executeAction(
			final A action, final EventObject event) {
		final boolean progressIsShowing = ProgressWindow.hasInstance();

		// We do it sooner to embed eventual initializer execution in the record session of the undo manager
		if (!action.isEmbedded()) {
			actionWillBePerformed(action);
		}

		// If action is embedded and valid, we skip initializer
		boolean confirmDoAction = (action.isEmbedded() && action.isValid()) ? true : runInitializer(action, event);
		if (confirmDoAction) {
			if (action instanceof LongRunningAction && (!action.isEmbedded()) && SwingUtilities.isEventDispatchThread()) {
				LongRunningActionTask task = new LongRunningActionTask((LongRunningAction) action) {
					@Override
					public void performTask() throws InterruptedException {
						Progress.setExpectedProgressSteps(((LongRunningAction) action).getExpectedProgressSteps());
						doExecuteAction(action, event);
					}
				};
				applicationContext.getTaskManager().scheduleExecution(task);
			}
			else {
				// Do it now, in this thread
				doExecuteAction(action, event);
			}
		}

		return action;
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void doExecuteAction(final A action,
			final EventObject event) {
		try {
			runAction(action);
		} catch (FlexoException exception) {
			if (!runExceptionHandler(exception, action)) {
				return;
			}
		}
		if (!action.isEmbedded()) {
			runFinalizer(action, event);
		}

		// We do it later to embed eventual finalizer execution in the record session of the undo manager
		if (!action.isEmbedded()) {
			actionHasBeenPerformed(action, true); // Action succeeded
		}

	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean runInitializer(A action,
			EventObject event) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(action.getActionType());
		if (actionInitializer != null) {
			FlexoActionInitializer<A> initializer = actionInitializer.getDefaultInitializer();
			if (initializer != null) {
				return initializer.run(event, action);
			}
		}
		return true;
	}

	private <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void runAction(
			final A action) throws FlexoException {
		/*if (getProject() != null) {
			getProject().clearRecentlyCreatedObjects();
		}*/
		action.doActionInContext();
		/*if (getProject() != null) {
			getProject().notifyRecentlyCreatedObjects();
		}*/
		// actionHasBeenPerformed(action, true); // Action succeeded
	}

	private <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void runFinalizer(
			final A action, EventObject event) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(action.getActionType());
		if (actionInitializer != null) {
			FlexoActionFinalizer<A> finalizer = actionInitializer.getDefaultFinalizer();
			if (finalizer != null) {
				finalizer.run(event, action);
			}
		}
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean runExceptionHandler(
			FlexoException exception, final A action) {
		actionHasBeenPerformed(action, false); // Action failed
		ProgressWindow.hideProgressWindow();
		FlexoExceptionHandler<A> exceptionHandler = null;
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(action.getActionType());
		if (actionInitializer != null) {
			exceptionHandler = actionInitializer.getDefaultExceptionHandler();
		}
		if (exceptionHandler != null) {
			if (exceptionHandler.handleException(exception, action)) {
				// The exception has been handled, we may still have to execute finalizer, if any
				return true;
			}
			else {
				return false;
			}

		}
		else {
			return false;
		}
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		return applicationContext.getEditingContext().getUndoManager();
	}

	private <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void actionWillBePerformed(
			A action) {
		getUndoManager().actionWillBePerformed(action);
	}

	private <A extends org.openflexo.foundation.action.FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void actionHasBeenPerformed(
			A action, boolean success) {
		getUndoManager().actionHasBeenPerformed(action, success);
		if (success) {
			if (_scenarioRecorder != null) {
				if (!action.isEmbedded() || action.getOwnerAction().getExecutionStatus() != ExecutionStatus.EXECUTING_CORE) {
					_scenarioRecorder.registerDoneAction(action);
				}
			}
		}
	}

	@Override
	public boolean performResourceScanning() {
		return true;
	}

	@Override
	public FlexoProgressFactory getFlexoProgressFactory() {
		return _progressFactory;
	}

	public boolean isTestEditor() {
		return false;
	}

	@Override
	public void focusOn(FlexoObject object) {

		// Only interactive editor handle this
		getModuleLoader().getActiveModule().getFlexoController().setCurrentEditedObjectAsModuleView(object);
		getModuleLoader().getActiveModule().getFlexoController().getSelectionManager().setSelectedObject(object);
	}

	public void registerControllerActionInitializer(ControllerActionInitializer controllerActionInitializer) {
		actionInitializers.put(controllerActionInitializer.getModule(), controllerActionInitializer);
	}

	public void unregisterControllerActionInitializer(ControllerActionInitializer controllerActionInitializer) {
		actionInitializers.remove(controllerActionInitializer.getModule());
	}

	private ControllerActionInitializer getCurrentControllerActionInitializer() {
		if (getModuleLoader().getActiveModule() != null) {
			return actionInitializers.get(getModuleLoader().getActiveModule());
		}
		return null;
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			FlexoActionType<A, T1, T2> actionType) {
		ControllerActionInitializer currentControllerActionInitializer = getCurrentControllerActionInitializer();
		if (currentControllerActionInitializer != null) {
			return currentControllerActionInitializer.getActionInitializer(actionType);
		}
		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionEnabled(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection) {
		if (actionType instanceof ActionSchemeActionType) {
			return true;
		}
		if (actionType instanceof DeletionSchemeActionType) {
			return true;
		}
		if (actionType.isEnabled(focusedObject, globalSelection)) {
			ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionType);
			if (actionInitializer != null) {
				FlexoActionEnableCondition<A, T1, T2> condition = actionInitializer.getEnableCondition();
				if (condition != null) {
					return condition.isEnabled(actionType, focusedObject, globalSelection, this);
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		return true;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionVisible(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection) {
		if (actionType.isVisibleForSelection(focusedObject, globalSelection)) {
			ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionType);
			if (actionInitializer != null) {
				FlexoActionVisibleCondition<A, T1, T2> condition = actionInitializer.getVisibleCondition();
				if (condition != null) {
					return condition.isVisible(actionType, focusedObject, globalSelection, this);
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		return true;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getEnabledIconFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionType);
		if (actionInitializer != null) {
			return actionInitializer.getEnabledIcon();
		}
		if (actionType instanceof DeletionSchemeActionType) {
			return FlexoController.statelessIconForObject(((DeletionSchemeActionType) actionType).getDeletionScheme());
		}
		else if (actionType instanceof ActionSchemeActionType) {
			return FlexoController.statelessIconForObject(((ActionSchemeActionType) actionType).getActionScheme());
		}

		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getDisabledIconFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionType);
		if (actionInitializer != null) {
			return actionInitializer.getDisabledIcon();
		}
		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> KeyStroke getKeyStrokeFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionType);
		if (actionInitializer != null) {
			return actionInitializer.getShortcut();
		}
		return null;
	}

}
