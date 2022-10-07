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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(RemoveFromListAction.RemoveFromListActionImpl.class)
@XMLElement
@FMLMigration("ExpressionAction should be used instead")
@Deprecated
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

		public T getDeclaredObject(RunTimeEvaluationContext evaluationContext) {
			try {
				return getValue().getBindingValue(evaluationContext);
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
		public DataBinding<? extends List<T>> getList() {

			// TODO Xtof: when I will have found how to set same kind of Individual:<name> type in the XSD TA
			if (list == null) {
				list = new DataBinding<>(this, new ParameterizedTypeImpl(List.class, Object.class), BindingDefinitionType.GET);
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
				value = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
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
			/*if (getValue().isSet() && getValue().isValid()) {
				return new ParameterizedTypeImpl(List.class, getValue().getAnalyzedType());
			}
			return new ParameterizedTypeImpl(List.class, IFlexoOntologyIndividual.class);*/
			if (getValue().isSet() && getValue().isValid()) {
				return getValue().getAnalyzedType();
			}
			return Object.class;
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) {
			logger.info("performing RemoveFromListAction");

			T objToRemove = getDeclaredObject(evaluationContext);

			try {

				if (getList() != null) {
					List<T> listObj = getList().getBindingValue(evaluationContext);
					if (listObj != null && objToRemove != null) {
						listObj.remove(objToRemove);
					}
					else {
						logger.warning("Won't add null object to list");

					}
				}
				else {
					logger.warning("Cannot perform Assignation as assignation is null");
				}
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
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
