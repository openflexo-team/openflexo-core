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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

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
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.AddFlexoProperty;
import org.openflexo.foundation.action.DeleteFlexoProperty;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionEnableCondition;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.FlexoActionizer;
import org.openflexo.foundation.action.FlexoActionizer.EditorProvider;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.SortFlexoProperties;
import org.openflexo.module.FlexoModule;
import org.openflexo.view.controller.action.AddFlexoPropertyActionizer;
import org.openflexo.view.controller.action.AddRepositoryFolderInitializer;
import org.openflexo.view.controller.action.DeleteFlexoPropertyActionizer;
import org.openflexo.view.controller.action.DeleteRepositoryFolderInitializer;
import org.openflexo.view.controller.action.HelpActionizer;
import org.openflexo.view.controller.action.InspectActionizer;
import org.openflexo.view.controller.action.LoadAllImportedProjectInitializer;
import org.openflexo.view.controller.action.LoadResourceActionInitializer;
import org.openflexo.view.controller.action.SortFlexoPropertiesActionizer;

// import org.openflexo.view.controller.action.SubmitDocumentationActionizer;

public class ControllerActionInitializer implements EditorProvider {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(ControllerActionInitializer.class.getPackage().getName());

	private final FlexoController _controller;

	private final Map<FlexoActionType<?, ?, ?>, ActionInitializer<?, ?, ?>> initializers;
	private final Map<Class<?>, ActionInitializer<?, ?, ?>> initializersByActionClass;

	protected ControllerActionInitializer(FlexoController controller) {
		super();
		initializers = new Hashtable<>();
		initializersByActionClass = new Hashtable<>();
		_controller = controller;
		initializeActions();
	}

	public Map<FlexoActionType<?, ?, ?>, ActionInitializer<?, ?, ?>> getActionInitializers() {
		return initializers;
	}

	public FlexoModule<?> getFlexoModule() {
		return getController().getModule();
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void registerInitializer(
			FlexoActionType<A, T1, T2> actionType, ActionInitializer<A, T1, T2> initializer) {
		if (actionType != null) {
			initializers.put(actionType, initializer);
		}
		else {
			Type superClass = initializer.getClass().getGenericSuperclass();
			if (superClass instanceof ParameterizedType) {
				Class<?> actionClass = TypeUtils.getBaseClass(((ParameterizedType) superClass).getActualTypeArguments()[0]);
				initializersByActionClass.put(actionClass, initializer);
			}
			else {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe(
							"You registered an action initializer without providing an action type and this method does not know how to retrieve the action Action class.");
				}
			}
		}
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void clearInitializer(
			FlexoActionType<A, T1, T2> actionType) {
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
		new HelpActionizer(this);

		// Registering copy/cut/paste/selectAll actions
		initClipboardActions();

		// TODO : To be re-written when Wysiwyg editor is re-written
		// new SubmitDocumentationActionizer(this);
		// new UploadProjectInitializer(this);

		// Remove sinc it is unused now
		// new ProjectExcelExportInitializer(this);

		new LoadAllImportedProjectInitializer(this);

		new AddRepositoryFolderInitializer(this);
		new DeleteRepositoryFolderInitializer(this);

		new AddFlexoPropertyActionizer(this);
		new DeleteFlexoPropertyActionizer(this);
		new SortFlexoPropertiesActionizer(this);
		if (FlexoObjectImpl.addFlexoPropertyActionizer == null) {
			FlexoObjectImpl.addFlexoPropertyActionizer = new FlexoActionizer<>(AddFlexoProperty.actionType, this);
		}
		if (FlexoObjectImpl.sortFlexoPropertiesActionizer == null) {
			FlexoObjectImpl.sortFlexoPropertiesActionizer = new FlexoActionizer<>(SortFlexoProperties.actionType, this);
		}
		if (FlexoObjectImpl.deleteFlexoPropertyActionizer == null) {
			FlexoObjectImpl.deleteFlexoPropertyActionizer = new FlexoActionizer<>(DeleteFlexoProperty.actionType, this);
		}
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> actionInitializer = (ActionInitializer<A, T1, T2>) initializers.get(actionType);
		if (actionInitializer == null) {
			Type superClass = actionType.getClass().getGenericSuperclass();
			if (superClass instanceof ParameterizedType) {
				Class<?> actionClass = TypeUtils.getBaseClass(((ParameterizedType) superClass).getActualTypeArguments()[0]);
				actionInitializer = (ActionInitializer<A, T1, T2>) initializersByActionClass.get(actionClass);
			}
		}
		return actionInitializer;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionFinalizer<A> getFinalizerFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionType);
		if (initializer != null) {
			return initializer.getDefaultFinalizer();
		}
		return null;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionInitializer<A> getInitializerFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionType);
		if (initializer != null) {
			return initializer.getDefaultInitializer();
		}
		return null;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionEnableCondition<A, T1, T2> getEnableConditionFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionType);
		if (initializer != null) {
			return initializer.getEnableCondition();
		}
		return null;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoActionVisibleCondition<A, T1, T2> getVisibleConditionFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionType);
		if (initializer != null) {
			return initializer.getVisibleCondition();
		}
		return null;
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> FlexoExceptionHandler<A> getExceptionHandlerFor(
			FlexoActionType<A, T1, T2> actionType) {
		ActionInitializer<A, T1, T2> initializer = getActionInitializer(actionType);
		if (initializer != null) {
			return initializer.getDefaultExceptionHandler();
		}
		return null;
	}

}
