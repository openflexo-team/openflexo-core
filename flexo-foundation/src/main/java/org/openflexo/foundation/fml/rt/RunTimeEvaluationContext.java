/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt;

import org.openflexo.connie.binding.SettableBindingEvaluationContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;

/**
 * A run-time context in which FML is executed: it gives access to the variables of the execution (such as {@code this}, the behaviour
 * {@code parameters}, or the local variables declared with {@link #declareVariable(String, Object)}), to the {@link FlexoConceptInstance}
 * and the {@link VirtualModelInstance} on which it works, to the editor and its {@link FMLRunTimeEngine}, and to the FML console.
 * <p>
 * Main implementations are the execution of a behaviour ({@link FlexoBehaviourAction}), a {@link FlexoConceptInstance} itself, and the
 * fetch requests (which declare the {@code selected} variable).
 *
 * @author sylvain
 *
 */
public interface RunTimeEvaluationContext extends SettableBindingEvaluationContext {

	/**
	 * Return execution engine attached to the editor of this context, when any
	 *
	 * @return
	 */
	public FMLRunTimeEngine getFMLRunTimeEngine();

	/**
	 * Return the editor in which FML is executed, when any
	 *
	 * @return
	 */
	public FlexoEditor getEditor();

	/**
	 * Return the {@link FlexoConceptInstance} on which we work
	 * 
	 * @return
	 */
	public FlexoConceptInstance getFlexoConceptInstance();

	/**
	 * Return the VirtualModelInstance on which we work
	 * 
	 * @return
	 */
	public VirtualModelInstance<?, ?> getVirtualModelInstance();

	/**
	 * Calling this method will register a new variable in this run-time context.<br>
	 * Variable is initialized with supplied name and value
	 *
	 * @param variableName
	 * @param value
	 */
	public void declareVariable(String variableName, Object value);

	/**
	 * Calling this method will dereference variable identified by supplied name
	 * 
	 * @param variableName
	 */
	public void dereferenceVariable(String variableName);

	/**
	 * Return object representing local context for this {@link RunTimeEvaluationContext}, if any
	 * 
	 * @return
	 */
	public FlexoObject getFocusedObject();

	/**
	 * Log supplied message and log level to "conceptual" out stream (might not be System.out, but console or terminal, etc.)
	 * 
	 * @param message
	 * @param logLevel
	 */
	public void logOut(String message, LogLevel logLevel);

	/**
	 * Log supplied message and log level to "conceptual" err stream (might not be System.out, but console or terminal, etc.)
	 * 
	 * @param message
	 * @param logLevel
	 */
	public void logErr(String message, LogLevel logLevel);

}
