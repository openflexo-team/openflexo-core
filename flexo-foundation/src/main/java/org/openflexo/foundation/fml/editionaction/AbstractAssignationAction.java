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

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractAssignationAction.AbstractAssignationActionImpl.class)
@XMLElement
public interface AbstractAssignationAction<T> extends AssignableAction<T>, FMLControlGraphOwner {

	@PropertyIdentifier(type = AssignableAction.class)
	public static final String ASSIGNABLE_ACTION_KEY = "assignableAction";

	@Getter(value = ASSIGNABLE_ACTION_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@XMLElement(context = "AssignableAction_")
	public AssignableAction<T> getAssignableAction();

	@Setter(ASSIGNABLE_ACTION_KEY)
	public void setAssignableAction(AssignableAction<T> assignableAction);

	public DataBinding<? super T> getAssignation();

	public static abstract class AbstractAssignationActionImpl<T> extends AssignableActionImpl<T> implements AbstractAssignationAction<T> {

		private static final Logger logger = Logger.getLogger(AbstractAssignationAction.class.getPackage().getName());

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getAssignation().toString() + " = " + getAssignableAction().getFMLRepresentation() + ";", context);
			return out.toString();
		}

		public T getAssignationValue(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException {
			if (getAssignableAction() != null) {
				return getAssignableAction().execute(action);
			}
			return null;
		}

		@Override
		public Type getAssignableType() {
			if (getAssignableAction() != null) {
				return getAssignableAction().getAssignableType();
			}
			return Object.class;
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			return getAssignableAction();
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (controlGraph instanceof AssignableAction) {
				setAssignableAction((AssignableAction<T>) controlGraph);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		@Override
		public void reduce() {
		}
	}

	// @DefineValidationRule
	// TODO: check type compatibility

}
