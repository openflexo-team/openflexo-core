/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/ExecutionActionPanel.fib")
@ModelEntity
@ImplementationClass(ExecutionAction.ExecutionActionImpl.class)
@XMLElement
@Deprecated
// Use ExpressionAction instead
public interface ExecutionAction<MS extends ModelSlot<?>> extends AssignableAction<FlexoObject> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXECUTION_KEY = "execution";

	@Getter(value = EXECUTION_KEY)
	@XMLAttribute
	public DataBinding<?> getExecution();

	@Setter(EXECUTION_KEY)
	public void setExecution(DataBinding<?> execution);

	public static abstract class ExecutionActionImpl<MS extends ModelSlot<?>> extends AssignableActionImpl<FlexoObject> implements
			ExecutionAction<MS> {

		private static final Logger logger = Logger.getLogger(ExecutionAction.class.getPackage().getName());

		private DataBinding<?> execution;

		public ExecutionActionImpl() {
			super();
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append((getAssignation().isSet() ? getAssignation().toString() + " = " : "") + getExecution().toString() + ";", context);
			return out.toString();
		}*/

		@Override
		public DataBinding<?> getExecution() {
			if (execution == null) {
				execution = new DataBinding<Object>(this, Object.class, BindingDefinitionType.EXECUTE);
				execution.setBindingName("execution");
			}
			return execution;
		}

		@Override
		public void setExecution(DataBinding<?> execution) {
			if (execution != null) {
				execution.setOwner(this);
				execution.setBindingName("execution");
				execution.setDeclaredType(Object.class);
				execution.setBindingDefinitionType(BindingDefinitionType.EXECUTE);
			}
			this.execution = execution;
		}

		@Override
		public Type getAssignableType() {
			if (getExecution().isSet() && getExecution().isValid()) {
				return getExecution().getAnalyzedType();
			}
			return Object.class;
		}

		@Override
		public FlexoObject execute(FlexoBehaviourAction action) {
			try {
				getExecution().execute(action);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (dataBinding == getExecution()) {
			}
			super.notifiedBindingChanged(dataBinding);
		}

	}

	@DefineValidationRule
	public static class ExecutionBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ExecutionAction> {
		public ExecutionBindingIsRequiredAndMustBeValid() {
			super("'execution'_binding_is_not_valid", ExecutionAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(ExecutionAction object) {
			return object.getExecution();
		}
	}
}
