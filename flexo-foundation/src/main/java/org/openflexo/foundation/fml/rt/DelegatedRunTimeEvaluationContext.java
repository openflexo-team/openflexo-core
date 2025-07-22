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
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;

/**
 * An implementation of a {@link RunTimeEvaluationContext} delegating methods to an alternative {@link RunTimeEvaluationContext}
 * 
 * @author sylvain
 * 
 */
public class DelegatedRunTimeEvaluationContext implements RunTimeEvaluationContext {

	private final RunTimeEvaluationContext delegate;

	public DelegatedRunTimeEvaluationContext(RunTimeEvaluationContext delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return delegate.getEvaluator();
	}

	@Override
	public FMLRunTimeEngine getFMLRunTimeEngine() {
		return delegate.getFMLRunTimeEngine();
	}

	@Override
	public FlexoEditor getEditor() {
		return delegate.getEditor();
	}

	@Override
	public Object getValue(BindingVariable variable) {
		return delegate.getValue(variable);
	}

	@Override
	public void setValue(Object value, BindingVariable variable) {
		delegate.setValue(value, variable);
	}

	@Override
	public FlexoConceptInstance getFlexoConceptInstance() {
		return delegate.getFlexoConceptInstance();
	}

	@Override
	public VirtualModelInstance<?, ?> getVirtualModelInstance() {
		return delegate.getVirtualModelInstance();
	}

	@Override
	public void declareVariable(String variableName, Object value) {
		delegate.declareVariable(variableName, value);
	}

	@Override
	public void dereferenceVariable(String variableName) {
		delegate.dereferenceVariable(variableName);
	}

	@Override
	public FlexoObject getFocusedObject() {
		return delegate.getFocusedObject();
	}

	@Override
	public void logOut(String message, LogLevel logLevel) {
		delegate.logOut(message, logLevel);
	}

	@Override
	public void logErr(String message, LogLevel logLevel) {
		delegate.logErr(message, logLevel);
	}

}
