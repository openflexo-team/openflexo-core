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
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.fml.rt.FMLConsole;
import org.openflexo.foundation.resource.ResourceUpdateHandler;
import org.openflexo.foundation.utils.FlexoProgressFactory;

/**
 * A {@link FlexoEditor} represents the run-time environment where a user is interacting with a {@link FlexoProject}<br>
 * 
 * A {@link FlexoEditor} provides access to:
 * <ul>
 * <li>the {@link FlexoServiceManager}</li>
 * <li>a {@link FlexoUndoManager}</li>
 * <li>a {@link FMLConsole}</li>
 * </ul>
 * 
 * A {@link FlexoEditor} defines execution context for {@link FlexoAction} beeing executed on a {@link FlexoProject}.
 * 
 * @author sylvain
 *
 */
public interface FlexoEditor {

	/**
	 * API for {@link FlexoEditor} factory
	 * 
	 * @author sylvain
	 *
	 */
	public static interface FlexoEditorFactory {
		public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager serviceManager);
	}

	/**
	 * Return project beeing edited/executed
	 * 
	 * @return
	 */
	public FlexoProject getProject();

	/**
	 * Return {@link FlexoServiceManager} provided by the running application
	 * 
	 * @return
	 */
	public FlexoServiceManager getServiceManager();

	/**
	 * Return progress factory for this {@link FlexoEditor}<br>
	 * (deprecated)
	 * 
	 * @return
	 */
	public FlexoProgressFactory getFlexoProgressFactory();

	/**
	 * Return {@link ResourceUpdateHandler} for this {@link FlexoEditor}<br>
	 * 
	 * @return
	 */
	public ResourceUpdateHandler getResourceUpdateHandler();

	/**
	 * Return {@link FMLConsole} for this {@link FlexoEditor}<br>
	 * 
	 * @return
	 */
	public FMLConsole getFMLConsole();

	public boolean isInteractive();

	public boolean performResourceScanning();

	public void focusOn(FlexoObject object);

	public FlexoUndoManager getUndoManager();

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performActionType(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, EventObject e);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performAction(A action, EventObject e);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionEnabled(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionVisible(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getEnabledIconFor(
			FlexoActionType<A, T1, T2> action);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getDisabledIconFor(
			FlexoActionType<A, T1, T2> action);

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> KeyStroke getKeyStrokeFor(
			FlexoActionType<A, T1, T2> action);

}
