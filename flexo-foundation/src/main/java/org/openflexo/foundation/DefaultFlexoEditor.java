/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.fml.rt.FMLRunTimeEngine;
import org.openflexo.foundation.fml.rt.SynchronousFMLRunTimeEngine;
import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.foundation.nature.FlexoNature;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceUpdateHandler;
import org.openflexo.foundation.utils.FlexoProgressFactory;

/**
 * Default implementation of {@link FlexoEditor}
 * 
 * @author sylvain
 * @see FlexoEditor
 *
 */
public class DefaultFlexoEditor implements FlexoEditor {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(DefaultFlexoEditor.class.getPackage().getName());

	private final FlexoProject<?> project;
	private final FlexoServiceManager serviceManager;
	private final ResourceUpdateHandler resourceUpdateHandler;
	private final FMLConsole console;

	public DefaultFlexoEditor(FlexoProject<?> project, FlexoServiceManager serviceManager) {
		this.project = project;
		this.serviceManager = serviceManager;
		if (project != null) {
			project.addToEditors(this);
		}
		resourceUpdateHandler = new ResourceUpdateHandler() {
			@Override
			public void resourceChanged(FlexoResource<?> resource) {
				// TODO Auto-generated method stub
			}
		};
		console = new FMLConsole(this);
	}

	@Override
	public final FlexoProject<?> getProject() {
		return project;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	private FMLRunTimeEngine runTimeEngine = null;

	@Override
	public FMLRunTimeEngine getFMLRunTimeEngine() {
		if (runTimeEngine == null) {
			runTimeEngine = new SynchronousFMLRunTimeEngine();
		}
		return runTimeEngine;
	}

	@Override
	public FMLConsole getFMLConsole() {
		return console;
	}

	@Override
	public boolean performResourceScanning() {
		return true;
	}

	@Override
	public FlexoProgressFactory getFlexoProgressFactory() {
		// Only interactive editor have a progress window
		return null;
	}

	/**
	 * Focus on supplied object, trying to display a view adapted to supplied displayNature
	 * 
	 * @param object
	 * @param displayNature
	 */
	@Override
	public <O extends FlexoObject> void focusOn(O object, FlexoNature<O> displayNature) {
		// Only interactive editor handle this
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	@Override
	public ResourceUpdateHandler getResourceUpdateHandler() {
		return resourceUpdateHandler;
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performActionFactory(
			FlexoActionFactory<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, EventObject e) {
		A action = actionType.makeNewAction(focusedObject, globalSelection, this);
		return performAction(action, e);
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performAction(A action, EventObject e) {
		// If the factory is not null, check that factory allows execution in its context
		if (action.getActionFactory() != null
				&& !action.getActionFactory().isEnabled(action.getFocusedObject(), action.getGlobalSelection())) {
			return null;
		}
		try {
			return action.doActionInContext();
		} catch (FlexoException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	/**
	 * Return flag indicating if supplied {@link FlexoActionFactory} implies an enabled action for supplied context (focused object and
	 * global selection)
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @return
	 */
	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionEnabled(
			FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, Vector<T2> globalSelection) {
		return actionFactory.isEnabled(focusedObject, globalSelection);
	}

	/**
	 * Return flag indicating if supplied {@link FlexoActionFactory} implies an visible action for supplied context (focused object and
	 * global selection)
	 *
	 * Default implementation returns true
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @return
	 */
	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionVisible(
			FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, Vector<T2> globalSelection) {
		return true;
	}

	/**
	 * Return 'enabled' icon for supplied {@link FlexoActionFactory}
	 * 
	 * @param actionFactory
	 * @return
	 */
	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getEnabledIconFor(
			FlexoActionFactory<A, T1, T2> action) {
		return null;
	}

	/**
	 * Return 'disabled' icon for supplied {@link FlexoActionFactory}
	 * 
	 * @param actionFactory
	 * @return
	 */
	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getDisabledIconFor(
			FlexoActionFactory<A, T1, T2> action) {
		return null;
	}

	/**
	 * Return eventual {@link KeyStroke} for supplied {@link FlexoActionFactory}<br>
	 * 
	 * Default implementation returns null
	 * 
	 * @param actionFactory
	 * @return
	 */
	@Override
	public KeyStroke getKeyStrokeFor(FlexoActionFactory<?, ?, ?> actionFactory) {
		return null;
	}

}
