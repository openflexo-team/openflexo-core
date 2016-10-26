/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(ListParameter.ListParameterImpl.class)
@XMLElement
public interface ListParameter extends FlexoBehaviourParameter {

	/*public enum ListType {
		String, Property, ObjectProperty, DataProperty
	}*/

	// @PropertyIdentifier(type = ListType.class)
	// public static final String LIST_TYPE_KEY = "listType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LIST_KEY = "list";

	/*@Getter(value = LIST_TYPE_KEY)
	@XMLAttribute
	public ListType getListType();
	
	@Setter(LIST_TYPE_KEY)
	public void setListType(ListType listType);*/

	@Getter(value = LIST_KEY)
	@XMLAttribute
	public DataBinding<List<?>> getList();

	@Setter(LIST_KEY)
	public void setList(DataBinding<List<?>> list);

	public Object getList(FlexoBehaviourAction<?, ?, ?> action);

	public static abstract class ListParameterImpl extends FlexoBehaviourParameterImpl implements ListParameter {

		// private ListType listType;
		private DataBinding<List<?>> list;

		public ListParameterImpl() {
			super();
		}

		@Override
		public Type getType() {
			// return new ParameterizedTypeImpl(List.class, String.class);
			return List.class;
		};

		@Override
		public WidgetType getWidget() {
			return WidgetType.DROPDOWN;
		}

		/*@Override
		public ListType getListType() {
			return listType;
		}
		
		@Override
		public void setListType(ListType listType) {
			this.listType = listType;
		}*/

		@Override
		public DataBinding<List<?>> getList() {
			if (list == null) {
				list = new DataBinding<List<?>>(this, getType(), BindingDefinitionType.GET);
			}
			return list;
		}

		@Override
		public void setList(DataBinding<List<?>> list) {
			if (list != null) {
				list.setOwner(this);
				list.setBindingName("list");
				list.setDeclaredType(getType());
				list.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.list = list;
		}

		@Override
		public Object getList(FlexoBehaviourAction<?, ?, ?> action) {
			if (getList().isValid()) {
				try {
					return getList().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}
}
