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
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/DeletionActionPanel.fib")
@ModelEntity
@ImplementationClass(DeleteAction.DeleteActionImpl.class)
@XMLElement
public interface DeleteAction<T extends FlexoObject> extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = OBJECT_KEY)
	@XMLAttribute
	public DataBinding<T> getObject();

	@Setter(OBJECT_KEY)
	public void setObject(DataBinding<T> object);

	public FlexoProperty<?> getAssignedFlexoProperty();

	public static abstract class DeleteActionImpl<T extends FlexoObject> extends EditionActionImpl implements DeleteAction<T> {

		private static final Logger logger = Logger.getLogger(DeleteAction.class.getPackage().getName());

		private DataBinding<T> object;

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
		public DataBinding<T> getObject() {
			if (object == null) {
				object = new DataBinding<T>(this, FlexoObject.class, BindingDefinitionType.GET);
				object.setBindingName("object");
			}
			return object;
		}

		@Override
		public void setObject(DataBinding<T> object) {
			if (object != null) {
				object.setOwner(this);
				object.setBindingName("object");
				object.setDeclaredType(FlexoObject.class);
				object.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.object = object;
		}

		@Override
		public String getStringRepresentation() {
			return "Delete " + getObject();
		}

		@Override
		public FlexoProperty<?> getAssignedFlexoProperty() {
			if (getFlexoConcept() == null) {
				return null;
			}
			return getFlexoConcept().getFlexoProperty(getObject().toString());
		}

		@Override
		public T execute(FlexoBehaviourAction action) {
			T objectToDelete = null;
			try {
				objectToDelete = getObject().getBindingValue(action);
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

	}

	@DefineValidationRule
	public static class ObjectToDeleteBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<DeleteAction> {
		public ObjectToDeleteBindingIsRequiredAndMustBeValid() {
			super("'object_to_delete'_binding_is_not_valid", DeleteAction.class);
		}

		@Override
		public DataBinding<?> getBinding(DeleteAction object) {
			return object.getObject();
		}

	}

}
