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

import org.openflexo.action.CopyActionInitializer;
import org.openflexo.action.CutActionInitializer;
import org.openflexo.action.ImportProjectInitializer;
import org.openflexo.action.PasteActionInitializer;
import org.openflexo.action.RemoveImportedProjectInitializer;
import org.openflexo.action.SelectAllActionInitializer;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionizer.EditorProvider;
import org.openflexo.module.FlexoModule;
import org.openflexo.view.controller.action.AddRepositoryFolderInitializer;
import org.openflexo.view.controller.action.CreateProjectInitializer;
import org.openflexo.view.controller.action.DeleteRepositoryFolderInitializer;
import org.openflexo.view.controller.action.InspectActionizer;
import org.openflexo.view.controller.action.LoadAllImportedProjectInitializer;
import org.openflexo.view.controller.action.LoadResourceActionInitializer;

/**
 * A software component, generally associated to a {@link FlexoModule}, and providing hooks for the execution of {@link FlexoAction} in a
 * given context (for example a {@link FlexoModule}).<br>
 * This scheme allows share {@link FlexoAction} and to provide custom initializers, finalizers, enable and visible condition as well as
 * exception handlers for various contexts.<br>
 * 
 * Registering could be done using two methods:
 * <ul>
 * <li>Either register an {@link ActionInitializer} for a {@link FlexoActionFactory}</li>
 * <li>Or register an {@link ActionInitializer} for a simple {@link FlexoAction}'s class</li>
 * </ul>
 * 
 * From an internal point of view, stores an internal map of {@link ActionInitializer} associated to a given {@link FlexoAction} class or a
 * {@link FlexoActionFactory}<br>
 * 
 * Note that this component handle inheritance over stored {@link FlexoAction}'s initializers
 * 
 * @see ActionInitializer
 * 
 * @author sylvain
 *
 */
public class ControllerActionInitializer implements EditorProvider {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(ControllerActionInitializer.class.getPackage().getName());

	private final FlexoController _controller;

	private final InitializersMap initializers = new InitializersMap();

	/**
	 * Create a {@link ControllerActionInitializer} for a {@link FlexoController}
	 * 
	 * @param controller
	 */
	protected ControllerActionInitializer(FlexoController controller) {
		_controller = controller;
		initializeActions();
	}

	/**
	 * Return all {@link ActionInitializer} associated to a given {@link FlexoActionFactory}
	 * 
	 * @return
	 */
	public InitializersMap getActionInitializers() {
		return initializers;
	}

	/**
	 * Return the {@link FlexoModule} of this {@link ControllerActionInitializer}
	 * 
	 * @return
	 */
	public FlexoModule<?> getFlexoModule() {
		return getController().getModule();
	}

	/**
	 * Register an {@link ActionInitializer} for a given {@link FlexoActionFactory}
	 * 
	 * @param actionFactory
	 * @param initializer
	 */
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void registerInitializer(
			FlexoActionFactory<A, T1, T2> actionFactory, ActionInitializer<A, T1, T2> initializer) {
		if (actionFactory != null) {
			initializers.put(actionFactory, initializer);
		}
		else {
			logger.severe("Registered an action initializer without providing an action factory");
		}
	}

	/**
	 * Register an {@link ActionInitializer} for a given {@link FlexoAction}'s class
	 * 
	 * @param actionType
	 * @param initializer
	 */
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void registerInitializer(Class<A> actionType,
			ActionInitializer<A, T1, T2> initializer) {
		if (actionType != null) {
			initializers.put(actionType, initializer);
		}
		else {
			logger.severe("Registered an action initializer without providing an action type");
		}
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void clearInitializer(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		if (actionFactory != null) {
			System.out.println("Clear initializer for " + actionFactory);
			initializers.remove(actionFactory);
		}
	}

	public void clearInitializer(Class<?> actionType) {
		if (actionType != null) {
			System.out.println("Clear initializer for " + actionType);
			initializers.remove(actionType);
		}
	}

	@Override
	public FlexoEditor getEditor() {
		return getController().getEditor();
	}

	public FlexoController getController() {
		return _controller;
	}

	public FlexoEditingContext getEditingContext() {
		if (getController() != null) {
			return getController().getEditingContext();
		}
		return null;
	}

	public FlexoModule<?> getModule() {
		return getController().getModule();
	}

	public void initClipboardActions() {
		if (getEditingContext() != null) {
			new CopyActionInitializer(this);
			new CutActionInitializer(this);
			new PasteActionInitializer(this);
			new SelectAllActionInitializer(this);
		}
	}

	public void initializeActions() {
		new InspectActionizer(this);
		new LoadResourceActionInitializer(this);
		new ImportProjectInitializer(this);
		new RemoveImportedProjectInitializer(this);

		// Registering copy/cut/paste/selectAll actions
		initClipboardActions();

		// TODO : To be re-written when Wysiwyg editor is re-written
		// new SubmitDocumentationActionizer(this);
		// new UploadProjectInitializer(this);

		// Remove since it is unused now
		// new ProjectExcelExportInitializer(this);

		new LoadAllImportedProjectInitializer(this);

		new AddRepositoryFolderInitializer(this);
		new DeleteRepositoryFolderInitializer(this);

		new CreateProjectInitializer(this);
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			A action) {
		if (action == null) {
			return null;
		}
		FlexoActionFactory<A, T1, T2> af = action.getActionFactory();
		if (af != null) {
			return getActionInitializer(af);
		}
		else {
			return getActionInitializer((Class<A>) action.getClass());
		}
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		if (actionFactory == null) {
			return null;
		}
		ActionInitializer<A, T1, T2> actionInitializer = initializers.get(actionFactory);
		if (actionInitializer == null) {
			// Attempt to use class
			actionInitializer = getActionInitializer(getActionClass(actionFactory));
		}
		return actionInitializer;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			Class<A> actionType) {
		if (actionType == null) {
			return null;
		}
		return initializers.get(actionType);
	}

	@SuppressWarnings("unchecked")
	private static <A extends FlexoAction<A, T, T2>, T extends FlexoObject, T2 extends FlexoObject> Class<A> getActionClass(
			FlexoActionFactory<A, T, T2> actionFactory) {
		return (Class<A>) TypeUtils.getTypeArgument(actionFactory.getClass(), FlexoActionFactory.class, 0);
	}

	/*public <A extends FlexoAction<A, T, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionFinalizer<A> getFinalizerFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionFactory);
		if (initializer != null) {
			return initializer.getDefaultFinalizer();
		}
		return null;
	}
	
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionInitializer<A> getInitializerFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionFactory);
		if (initializer != null) {
			return initializer.getDefaultInitializer();
		}
		return null;
	}
	
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionEnableCondition<A, T1, T2> getEnableConditionFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionFactory);
		if (initializer != null) {
			return initializer.getEnableCondition();
		}
		return null;
	}
	
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionVisibleCondition<A, T1, T2> getVisibleConditionFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionFactory);
		if (initializer != null) {
			return initializer.getVisibleCondition();
		}
		return null;
	}
	
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoExceptionHandler<A> getExceptionHandlerFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionFactory);
		if (initializer != null) {
			return initializer.getDefaultExceptionHandler();
		}
		return null;
	}*/

}
