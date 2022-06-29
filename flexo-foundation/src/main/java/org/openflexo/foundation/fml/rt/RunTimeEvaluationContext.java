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

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.binding.SettableBindingEvaluationContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;

/**
 * This interface is implemented by all classes defining a run-time context for FML execution<br>
 * The main purpose of this context is to provide read access to some {@link BindingVariable}
 * 
 * @author sylvain
 * 
 */
public interface RunTimeEvaluationContext extends SettableBindingEvaluationContext {

	/**
	 * Return execution engine attached to this editor, when any
	 * 
	 * @return
	 */
	public FMLRunTimeEngine getFMLRunTimeEngine();

	/**
	 * Return execution engine attached to this editor, when any
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
	 * Calling this method will register a new variable in the run-time context provided by this {@link FlexoBehaviourAction} instance in
	 * the context of its implementation of {@link RunTimeEvaluationContext}.<br>
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

	public void logOut(String message, LogLevel logLevel);

	public void logErr(String message, LogLevel logLevel);

	@SuppressWarnings("serial")
	public class ReturnException extends Exception {

		private final Object returnedValue;

		public ReturnException(Object returnedValue) {
			this.returnedValue = returnedValue;
		}

		public Object getReturnedValue() {
			return returnedValue;
		}
	}

}
