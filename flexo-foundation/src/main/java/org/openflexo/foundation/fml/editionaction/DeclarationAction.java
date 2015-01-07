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

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.DeclarationActionBindingModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/AssignationActionPanel.fib")
@ModelEntity
@ImplementationClass(DeclarationAction.DeclarationActionImpl.class)
@XMLElement
public interface DeclarationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = String.class)
	public static final String VARIABLE_NAME_KEY = "variableName";

	@Getter(value = VARIABLE_NAME_KEY)
	public String getVariableName();

	@Setter(VARIABLE_NAME_KEY)
	public void setVariableName(String variableName);

	public static abstract class DeclarationActionImpl<T> extends AbstractAssignationActionImpl<T> implements DeclarationAction<T> {

		private static final Logger logger = Logger.getLogger(DeclarationAction.class.getPackage().getName());

		private ControlGraphBindingModel<?> inferedBindingModel = null;

		@Override
		public T execute(FlexoBehaviourAction action) throws FlexoException {
			T value = getAssignationValue(action);
			action.declareVariable(getVariableName(), value);
			return value;
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			if (inferedBindingModel == null) {
				inferedBindingModel = makeInferedBindingModel();
			}
			return inferedBindingModel;
		}

		protected ControlGraphBindingModel<?> makeInferedBindingModel() {
			return new DeclarationActionBindingModel(this);
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("<Type> " + getVariableName() + " = " + getAssignableAction().getFMLRepresentation() + ";", context);
			return out.toString();
		}

	}

	// @DefineValidationRule
	// TODO: check variable name and validity
	// TODO: check type compatibility
}
