/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.BindingValue;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.annotations.FIBPanel;
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

	@Getter(value = ASSIGNATION_KEY)
	@XMLAttribute(xmlTag = "assign")
	public DataBinding<? super T> getAssignation();

	@Setter(ASSIGNATION_KEY)
	public void setAssignation(DataBinding<? super T> assignation);

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
