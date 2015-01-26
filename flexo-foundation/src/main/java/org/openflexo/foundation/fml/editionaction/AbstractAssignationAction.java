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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.FlexoException;
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

	// public DataBinding<? super T> getAssignation();

	public static abstract class AbstractAssignationActionImpl<T> extends AssignableActionImpl<T> implements AbstractAssignationAction<T> {

		private static final Logger logger = Logger.getLogger(AbstractAssignationAction.class.getPackage().getName());

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
