/*
 * (c) Copyright 2012-2013 Openflexo
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.binding.ParameterizedTypeImpl;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@FIBPanel("Fib/FML/RemoveFromListActionPanel.fib")
@ModelEntity
@ImplementationClass(RemoveFromListAction.RemoveFromListActionImpl.class)
@XMLElement
public interface RemoveFromListAction<T> extends AssignableAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LIST_KEY = "list";

	@Getter(value = VALUE_KEY)
	@XMLAttribute
	public DataBinding<T> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<T> value);

	@Getter(value = LIST_KEY)
	@XMLAttribute
	public DataBinding<? extends List<T>> getList();

	@Setter(LIST_KEY)
	public void setList(DataBinding<? extends List<T>> list);

	public static abstract class RemoveFromListActionImpl<T> extends AssignableActionImpl<T> implements RemoveFromListAction<T> {

		private static final Logger logger = Logger.getLogger(RemoveFromListAction.class.getPackage().getName());

		private DataBinding<T> value;
		private DataBinding<? extends List<T>> list;

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getAssignation().toString() + " = " + getValue().toString() + ";", context);
			return out.toString();
		}*/

		public T getDeclaredObject(FlexoBehaviourAction<?, ?, ?> action) {
			try {
				return getValue().getBindingValue(action);
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
		public DataBinding<? extends List<T>> getList() {

			// TODO Xtof: when I will have found how to set same kind of Individual:<name> type in the XSD TA
			if (list == null) {
				list = new DataBinding<List<T>>(this, new ParameterizedTypeImpl(List.class, Object.class), BindingDefinitionType.GET);
				list.setBindingName("list");
			}
			return list;
		}

		@Override
		public void setList(DataBinding<? extends List<T>> list) {

			// TODO Xtof: when I will have found how to set same kind of Individual:<name> type in the XSD TA
			if (list != null) {
				list.setOwner(this);
				list.setBindingName("list");
				list.setDeclaredType(new ParameterizedTypeImpl(List.class, Object.class));
				list.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.list = list;
		}

		@Override
		public DataBinding<T> getValue() {
			if (value == null) {
				value = new DataBinding<T>(this, Object.class, BindingDefinitionType.GET);
				value.setBindingName("value");
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<T> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName("value");
				value.setDeclaredType(Object.class);
				value.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.value = value;
		}

		@Override
		public Type getAssignableType() {
			if (getValue().isSet() && getValue().isValid()) {
				return new ParameterizedTypeImpl(List.class, getValue().getAnalyzedType());
			}
			return new ParameterizedTypeImpl(List.class, IFlexoOntologyIndividual.class);
		}

		@Override
		public T execute(FlexoBehaviourAction<?, ?, ?> action) {
			logger.info("performing RemoveFromListAction");

			DataBinding<? extends List<T>> list = getList();
			T objToRemove = getDeclaredObject(action);

			try {

				if (list != null) {
					List<T> listObj = list.getBindingValue(action);
					if (objToRemove != null) {
						listObj.remove(objToRemove);
					} else {
						logger.warning("Won't add null object to list");

					}
				} else {
					logger.warning("Cannot perform Assignation as assignation is null");
				}
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return objToRemove;
		}
	}

	@DefineValidationRule
	public static class ValueBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<RemoveFromListAction> {
		public ValueBindingIsRequiredAndMustBeValid() {
			super("'value'_binding_is_not_valid", RemoveFromListAction.class);
		}

		@Override
		public DataBinding<?> getBinding(RemoveFromListAction object) {
			return object.getValue();
		}

	}

	@DefineValidationRule
	public static class ListBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<RemoveFromListAction> {
		public ListBindingIsRequiredAndMustBeValid() {
			super("'list'_binding_is_not_valid", RemoveFromListAction.class);
		}

		@Override
		public DataBinding<?> getBinding(RemoveFromListAction object) {
			return object.getList();
		}

	}

}
