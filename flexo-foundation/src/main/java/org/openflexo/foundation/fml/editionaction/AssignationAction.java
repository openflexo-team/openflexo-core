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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.action.NavigationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/AssignationActionPanel.fib")
@ModelEntity
@ImplementationClass(AssignationAction.AssignationActionImpl.class)
@XMLElement
public interface AssignationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String ASSIGNATION_KEY = "assignation";
	@Deprecated
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DEPRECATED_VALUE_KEY = "value";

	@Getter(value = ASSIGNATION_KEY)
	@XMLAttribute(xmlTag = "assign")
	public DataBinding<? super T> getAssignation();

	@Setter(ASSIGNATION_KEY)
	public void setAssignation(DataBinding<? super T> assignation);

	@Deprecated
	@Getter(value = DEPRECATED_VALUE_KEY)
	@XMLAttribute
	public DataBinding<T> getDeprecatedValue();

	@Deprecated
	@Setter(DEPRECATED_VALUE_KEY)
	public void setDeprecatedValue(DataBinding<T> value);

	@Override
	public FlexoRole<T> getFlexoRole();

	public static abstract class AssignationActionImpl<T> extends AbstractAssignationActionImpl<T> implements AssignationAction<T> {

		private static final Logger logger = Logger.getLogger(AssignationAction.class.getPackage().getName());

		private DataBinding<? super T> assignation;

		@Override
		public DataBinding<? super T> getAssignation() {
			if (assignation == null) {
				assignation = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET) {
					@Override
					public Type getDeclaredType() {
						return getAssignableType();
					}
				};
				assignation.setDeclaredType(getAssignableType());
				assignation.setBindingName("assignation");
				assignation.setMandatory(true);

			}
			assignation.setDeclaredType(getAssignableType());
			return assignation;
		}

		@Override
		public void setAssignation(DataBinding<? super T> assignation) {
			if (assignation != null) {
				this.assignation = new DataBinding<Object>(assignation.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET_SET) {
					@Override
					public Type getDeclaredType() {
						return getAssignableType();
					}
				};
				assignation.setDeclaredType(getAssignableType());
				assignation.setBindingName("assignation");
				assignation.setMandatory(true);
			}
			notifiedBindingChanged(this.assignation);
		}

		@Override
		public FlexoRole<T> getFlexoRole() {
			if (getFlexoConcept() == null) {
				return null;
			}
			if (assignation != null && assignation.isBindingValue()) {
				BindingValue bindingValue = (BindingValue) assignation.getExpression();
				if (bindingValue.getBindingPath().size() == 0) {
					return (FlexoRole<T>) getFlexoConcept().getFlexoRole(bindingValue.getVariableName());
				}
			}
			return null;
		}

		@Override
		public T execute(FlexoBehaviourAction action) throws FlexoException {
			T value = getAssignationValue(action);
			try {
				getAssignation().setBindingValue(value, action);
			} catch (Exception e) {
				logger.warning("Unexpected assignation issue, " + getAssignation() + " value=" + value + " exception: " + e);
				e.printStackTrace();
				throw new FlexoException(e);
			}

			// TODO: check if following statements are necessary (i think it should not)
			if (getFlexoRole() != null && value instanceof FlexoObject) {
				if (action instanceof ActionSchemeAction) {
					((ActionSchemeAction) action).getFlexoConceptInstance().setObjectForFlexoRole((FlexoObject) value,
							(FlexoRole) getFlexoRole());
				}
				if (action instanceof CreationSchemeAction) {
					((CreationSchemeAction) action).getFlexoConceptInstance().setObjectForFlexoRole((FlexoObject) value,
							(FlexoRole) getFlexoRole());
				}
				if (action instanceof DeletionSchemeAction) {
					((DeletionSchemeAction) action).getFlexoConceptInstance().setObjectForFlexoRole((FlexoObject) value,
							(FlexoRole) getFlexoRole());
				}
				if (action instanceof NavigationSchemeAction) {
					((NavigationSchemeAction) action).getFlexoConceptInstance().setObjectForFlexoRole((FlexoObject) value,
							(FlexoRole) getFlexoRole());
				}
				if (action instanceof SynchronizationSchemeAction) {
					((SynchronizationSchemeAction) action).getFlexoConceptInstance().setObjectForFlexoRole((FlexoObject) value,
							(FlexoRole) getFlexoRole());
				}
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
			return getHeaderContext() + getAssignation().toString() + " = " + getAssignableAction().getStringRepresentation();
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
