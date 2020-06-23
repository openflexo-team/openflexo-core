/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.view.controller.action;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;

import org.openflexo.ApplicationContext;
import org.openflexo.action.ModuleSpecificFlexoAction;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;

/**
 * Implementation of {@link AbstractAction} API while wrapping a {@link FlexoActionFactory}
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
@SuppressWarnings("serial")
public class MenuItemAction<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> extends AbstractAction {

	private FlexoActionSource<T1, T2> actionSource;
	private FlexoActionFactory<A, T1, T2> actionFactory;

	private T1 focusedObject;
	private Vector<T2> globalSelection;
	private FlexoEditor editor;

	public MenuItemAction(FlexoActionFactory<A, T1, T2> actionFactory, FlexoActionSource<T1, T2> actionSource) {
		super();
		this.actionSource = actionSource;
		this.actionFactory = actionFactory;
	}

	public MenuItemAction(FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super();
		this.actionFactory = actionFactory;
		this.focusedObject = focusedObject;
		this.globalSelection = globalSelection;
		this.editor = editor;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && getEditor() != null && getEditor().isActionEnabled(actionFactory, focusedObject, globalSelection);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getEditor().performActionFactory(actionFactory, getFocusedObject(), getGlobalSelection(), e);
	}

	public String getLocalizedActionName() {
		return getApplicableLocales().localizedForKey(actionFactory.getActionName());
	}

	private FlexoEditor getEditor() {
		if (actionSource != null) {
			return actionSource.getEditor();
		}
		return editor;
	}

	public T1 getFocusedObject() {
		if (actionSource != null) {
			return actionSource.getFocusedObject();
		}
		return focusedObject;
	}

	public Vector<T2> getGlobalSelection() {
		if (actionSource != null) {
			return (Vector<T2>) actionSource.getGlobalSelection();
		}
		return globalSelection;
	}

	public LocalizedDelegate getApplicableLocales() {

		if (getModuleClass() != null) {
			FlexoModule<?> module;
			try {
				ModuleLoader moduleLoader = ((ApplicationContext) getEditor().getServiceManager()).getModuleLoader();
				module = moduleLoader.getModuleInstance((Class) getModuleClass());
				return module.getLocales();
			} catch (ModuleLoadingException e) {
				e.printStackTrace();
			}
		}

		return actionFactory.getLocales(editor.getServiceManager());

	}

	public Class<? extends ModelSlot<?>> getModelSlotClass() {
		if (TechnologySpecificAction.class.isAssignableFrom(getEditionActionClass())) {
			return (Class<? extends ModelSlot<?>>) TypeUtils
					.getBaseClass(TypeUtils.getTypeArgument(getEditionActionClass(), TechnologySpecificAction.class, 0));
		}
		return null;
	}

	public Class<? extends TechnologyAdapter> getTechnologyAdapterClass() {
		if (TechnologySpecificFlexoAction.class.isAssignableFrom(getEditionActionClass())) {
			return (Class<? extends TechnologyAdapter>) TypeUtils
					.getBaseClass(TypeUtils.getTypeArgument(getEditionActionClass(), TechnologySpecificFlexoAction.class, 0));
		}
		return null;
	}

	public Class<? extends FlexoModule<?>> getModuleClass() {
		if (ModuleSpecificFlexoAction.class.isAssignableFrom(getEditionActionClass())) {
			return (Class<? extends FlexoModule<?>>) TypeUtils
					.getBaseClass(TypeUtils.getTypeArgument(getEditionActionClass(), ModuleSpecificFlexoAction.class, 0));
		}
		return null;
	}

	public Class<? extends EditionAction> getEditionActionClass() {
		if (actionFactory != null) {
			return (Class<? extends EditionAction>) TypeUtils
					.getBaseClass(TypeUtils.getTypeArgument(actionFactory.getClass(), FlexoActionFactory.class, 0));
		}
		return null;
	}

}
