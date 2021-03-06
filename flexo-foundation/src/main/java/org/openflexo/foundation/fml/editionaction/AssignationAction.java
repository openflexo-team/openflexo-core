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

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(AssignationAction.AssignationActionImpl.class)
@XMLElement
public interface AssignationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String ASSIGNATION_KEY = "assignation";

	@Getter(value = ASSIGNATION_KEY)
	@XMLAttribute(xmlTag = "assign")
	public DataBinding<? super T> getAssignation();

	@Setter(ASSIGNATION_KEY)
	public void setAssignation(DataBinding<? super T> assignation);

	@Override
	public FlexoProperty<T> getAssignedFlexoProperty();

	public static abstract class AssignationActionImpl<T> extends AbstractAssignationActionImpl<T> implements AssignationAction<T> {

		private static final Logger logger = Logger.getLogger(AssignationAction.class.getPackage().getName());

		private DataBinding<? super T> assignation;

		@Override
		public DataBinding<? super T> getAssignation() {
			if (assignation == null) {
				assignation = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET);
				assignation.setBindingName("assignation");
				assignation.setMandatory(true);

			}
			return assignation;
		}

		@Override
		public void setAssignation(DataBinding<? super T> assignation) {
			if (assignation != null) {
				this.assignation = new DataBinding<Object>(assignation.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET_SET);
				assignation.setBindingName("assignation");
				assignation.setMandatory(true);
			}
			notifiedBindingChanged(this.assignation);
		}

		@Override
		public FlexoProperty<T> getAssignedFlexoProperty() {
			if (getFlexoConcept() == null) {
				return null;
			}
			if (assignation != null && assignation.isValid() && assignation.isBindingValue()) {
				BindingValue bindingValue = (BindingValue) assignation.getExpression();
				if (bindingValue.isValid() && bindingValue.getBindingPath().size() == 0) {
					return (FlexoProperty<T>) getFlexoConcept().getAccessibleProperty(bindingValue.getVariableName());
				}
			}
			return null;
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			T value = getAssignationValue(evaluationContext);
			try {
				getAssignation().setBindingValue(value, evaluationContext);
			} catch (Exception e) {
				logger.warning("Unexpected assignation issue, " + getAssignation() + " value=" + value + " exception: " + e);
				e.printStackTrace();
				throw new FlexoException(e);
			}
			return value;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append((getAssignation() != null ? getAssignation().toString() + " = " : "")
					+ (getAssignableAction() != null ? getAssignableAction().getFMLRepresentation() : "<no_assignable_action>") + ";",
					context);
			return out.toString();
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + (getAssignation() != null ? getAssignation().toString() : "") + " = "
					+ (getAssignableAction() != null ? getAssignableAction().getStringRepresentation() : "<no_assignable_action>");
		}

	}

	@DefineValidationRule
	public static class AssignationBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AssignationAction> {
		public AssignationBindingIsRequiredAndMustBeValid() {
			super("'assignation'_binding_is_required_and_must_be_valid", AssignationAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(AssignationAction object) {
			return object.getAssignation();
		}

	}

}
