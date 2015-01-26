/**
 * 
 * Copyright (c) 2015, Openflexo
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

import org.openflexo.antar.binding.TypeUtils;
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
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/DeclarationActionPanel.fib")
@ModelEntity
@ImplementationClass(DeclarationAction.DeclarationActionImpl.class)
@XMLElement
public interface DeclarationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = String.class)
	public static final String VARIABLE_NAME_KEY = "variableName";

	@Getter(value = VARIABLE_NAME_KEY)
	@XMLAttribute(xmlTag = "variable")
	public String getVariableName();

	@Setter(VARIABLE_NAME_KEY)
	public void setVariableName(String variableName);

	public String getDeclarationTypeAsString();

	public String getFullQualifiedDeclarationTypeAsString();

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
			out.append(getFullQualifiedDeclarationTypeAsString() + " " + getVariableName() + " = "
					+ getAssignableAction().getFMLRepresentation() + ";", context);
			return out.toString();
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getDeclarationTypeAsString() + " " + getVariableName() + " = "
					+ getAssignableAction().getStringRepresentation();
		}

		@Override
		public String getDeclarationTypeAsString() {
			return TypeUtils.simpleRepresentation(getAssignableAction().getAssignableType());
		}

		@Override
		public String getFullQualifiedDeclarationTypeAsString() {
			return TypeUtils.fullQualifiedRepresentation(getAssignableAction().getAssignableType());
		}

	}

	// @DefineValidationRule
	// TODO: check variable name and validity
	// TODO: check type compatibility
}
