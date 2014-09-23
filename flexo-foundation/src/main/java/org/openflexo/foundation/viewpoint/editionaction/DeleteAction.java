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
package org.openflexo.foundation.viewpoint.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.validation.annotations.DefineValidationRule;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/VPM/DeletionActionPanel.fib")
@ModelEntity
@ImplementationClass(DeleteAction.DeleteActionImpl.class)
@XMLElement
public interface DeleteAction<MS extends ModelSlot<?>, T extends FlexoObject> extends EditionAction<MS, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = OBJECT_KEY)
	@XMLAttribute
	public DataBinding<?> getObject();

	@Setter(OBJECT_KEY)
	public void setObject(DataBinding<?> object);

	public FlexoRole getFlexoRole();

	public static abstract class DeleteActionImpl<MS extends ModelSlot<?>, T extends FlexoObject> extends EditionActionImpl<MS, T>
			implements DeleteAction<MS, T> {

		private static final Logger logger = Logger.getLogger(DeleteAction.class.getPackage().getName());

		private DataBinding<?> object;

		public DeleteActionImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("delete " + getObject().toString(), context);
			return out.toString();
		}

		public Object getDeclaredObject(FlexoBehaviourAction action) {
			try {
				return getObject().getBindingValue(action);
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
		public DataBinding<?> getObject() {
			if (object == null) {
				object = new DataBinding<Object>(this, Object.class, BindingDefinitionType.GET);
				object.setBindingName("object");
			}
			return object;
		}

		@Override
		public void setObject(DataBinding<?> object) {
			if (object != null) {
				object.setOwner(this);
				object.setBindingName("object");
				object.setDeclaredType(Object.class);
				object.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.object = object;
		}

		@Override
		public String getStringRepresentation() {
			return "Delete " + getObject();
		}

		@Override
		public FlexoRole getFlexoRole() {
			if (getFlexoConcept() == null) {
				return null;
			}
			return getFlexoConcept().getFlexoRole(getObject().toString());
		}

		@Override
		public T performAction(FlexoBehaviourAction action) {
			T objectToDelete = null;
			try {
				objectToDelete = (T) getObject().getBindingValue(action);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
			if (objectToDelete == null) {
				return null;
			}
			try {
				logger.info("Delete object " + objectToDelete + " for object " + getObject() + " this=" + this);
				objectToDelete.delete();
			} catch (Exception e) {
				logger.warning("Unexpected exception occured during deletion: " + e.getMessage());
				e.printStackTrace();
			}
			return objectToDelete;
		}

		@Override
		public void finalizePerformAction(FlexoBehaviourAction action, T initialContext) {
			// TODO Auto-generated method stub

		}

	}

	@DefineValidationRule
	public static class ObjectToDeleteBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<DeleteAction> {
		public ObjectToDeleteBindingIsRequiredAndMustBeValid() {
			super("'object_to_delete'_binding_is_not_valid", DeleteAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(DeleteAction object) {
			return object.getObject();
		}

	}

}
