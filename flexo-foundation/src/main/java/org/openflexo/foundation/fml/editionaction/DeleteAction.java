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
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(DeleteAction.DeleteActionImpl.class)
@XMLElement
public interface DeleteAction<T extends FlexoObject> extends AssignableAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = OBJECT_KEY)
	@XMLAttribute
	public DataBinding<T> getObject();

	@Setter(OBJECT_KEY)
	public void setObject(DataBinding<T> object);

	@Override
	public FlexoProperty<T> getAssignedFlexoProperty();

	public static abstract class DeleteActionImpl<T extends FlexoObject> extends AssignableActionImpl<T> implements DeleteAction<T> {

		private static final Logger logger = Logger.getLogger(DeleteAction.class.getPackage().getName());

		private DataBinding<T> object;

		@Override
		public String getStringRepresentation() {
			return "delete " + getObject().toString();
		}

		public Object getDeclaredObject(FlexoBehaviourAction<?, ?, ?> action) {
			try {
				return getObject().getBindingValue(action);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public DataBinding<T> getObject() {
			if (object == null) {
				object = new DataBinding<>(this, FlexoObject.class, BindingDefinitionType.GET);
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
			notifiedBindingChanged(object);
		}

		@Override
		public FlexoProperty<T> getAssignedFlexoProperty() {
			if (getFlexoConcept() == null) {
				return null;
			}
			return (FlexoProperty) getFlexoConcept().getAccessibleProperty(getObject().toString());
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) {

			T objectToDelete = null;
			try {
				objectToDelete = getObject().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}

			// Handle special case of explicit call to super delete
			if (objectToDelete instanceof FlexoConceptInstance && getObject() != null && getObject().toString().equals("this")) {
				((FlexoConceptInstance) objectToDelete).performCoreDeletion();
				return objectToDelete;
			}

			if (objectToDelete == null) {
				return null;
			}
			try {

				FlexoResource<?> resourceToDelete = null;
				if (objectToDelete instanceof ResourceData) {
					resourceToDelete = ((ResourceData<?>) objectToDelete).getResource();
				}

				logger.info("Delete object " + objectToDelete + " for object " + getObject() + " this=" + this);
				if (objectToDelete instanceof FlexoConceptInstance) {
					logger.info("On supprime " + objectToDelete + " of " + ((FlexoConceptInstance) objectToDelete).getFlexoConcept());
				}
				objectToDelete.delete();
				logger.info("END Deleting object " + objectToDelete + " for object " + getObject() + " this=" + this);
				if (objectToDelete instanceof FlexoConceptInstance) {
					logger.info("END On vient de supprimer " + objectToDelete + " of "
							+ ((FlexoConceptInstance) objectToDelete).getFlexoConcept());
				}

				if (resourceToDelete != null) {
					logger.info("Also delete resource " + resourceToDelete);
					resourceToDelete.delete();
				}

			} catch (Exception e) {
				logger.warning("Unexpected exception occured during deletion: " + e.getMessage());
				e.printStackTrace();
			}
			return objectToDelete;
		}

		@Override
		public Type getInferedType() {
			return Void.class;
		}

		@Override
		public Type getAssignableType() {
			return Object.class;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getObject().rebuild();
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
