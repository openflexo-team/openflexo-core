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

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.ReturnException;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(AddToListAction.AddToListActionImpl.class)
@XMLElement
public interface AddToListAction<T> extends AssignableAction<T>, FMLControlGraphOwner {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LIST_KEY = "list";
	@PropertyIdentifier(type = AssignableAction.class)
	public static final String ASSIGNABLE_ACTION_KEY = "assignableAction";

	@Getter(value = VALUE_KEY)
	@XMLAttribute
	@Deprecated
	public DataBinding<T> getValue();

	@Setter(VALUE_KEY)
	@Deprecated
	public void setValue(DataBinding<T> value);

	@Getter(value = LIST_KEY)
	@XMLAttribute
	public DataBinding<? extends List<T>> getList();

	@Setter(LIST_KEY)
	public void setList(DataBinding<? extends List<T>> list);

	@Getter(value = ASSIGNABLE_ACTION_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "AssignableAction_")
	public AssignableAction<T> getAssignableAction();

	@Setter(ASSIGNABLE_ACTION_KEY)
	public void setAssignableAction(AssignableAction<T> assignableAction);

	public static abstract class AddToListActionImpl<T> extends AssignableActionImpl<T> implements AddToListAction<T> {

		private static final Logger logger = Logger.getLogger(AddToListAction.class.getPackage().getName());

		private DataBinding<T> value;
		private DataBinding<? extends List<T>> list;

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append((getList() != null ? getList().toString() + "." : "") + "FML::AddToList("
					+ (getAssignableAction() != null ? getAssignableAction().getStringRepresentation() : "") + ")", context);
			return out.toString();
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + (getList() != null ? getList().toString() + "." : "") + "FML::AddToList("
					+ (getAssignableAction() != null ? getAssignableAction().getStringRepresentation() : "") + ")";
		}

		public boolean isListRequired() {
			return true;
		}

		public boolean isValueRequired() {
			return true;
		}

		public Type getListType() {
			if (getValue().isSet() && getValue().isValid()) {
				return new ParameterizedTypeImpl(List.class, getAssignableType());
			}
			return new ParameterizedTypeImpl(List.class, Object.class);
		}

		@Override
		public DataBinding<? extends List<T>> getList() {

			if (list == null) {
				list = new DataBinding<>(this, new ParameterizedTypeImpl(List.class, Object.class), BindingDefinitionType.GET);
				list.setBindingName("list");
			}
			return list;
		}

		@Override
		public void setList(DataBinding<? extends List<T>> list) {

			if (list != null) {
				list.setOwner(this);
				list.setBindingName("list");
				list.setDeclaredType(new ParameterizedTypeImpl(List.class, Object.class));
				list.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.list = list;
		}

		@Override
		@Deprecated
		public DataBinding<T> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
				value.setBindingName("value");
			}
			return value;
		}

		@Override
		@Deprecated
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
		public T execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			logger.fine("performing AddToListAction");

			DataBinding<? extends List<T>> list = getList();
			T objToAdd = getAssignationValue(evaluationContext);

			try {

				if (list != null) {
					// System.out.println(
					// "Attention, j'evalue la liste " + list + " valid=" + list.isValid() + " reason=" + list.invalidBindingReason());
					List<T> listObj = list.getBindingValue(evaluationContext);
					if (listObj == null) {
						logger.warning("Null list for binding " + list + " cannot add " + objToAdd);
						/*if (list.isBindingValue()) {
							System.out.println("last path= " + ((BindingValue) list.getExpression()).getLastBindingPathElement());
							System.out.println(
									"last path class = " + ((BindingValue) list.getExpression()).getLastBindingPathElement().getClass());
						}*/
					}
					else {
						if (objToAdd != null) {
							listObj.add(objToAdd);
						}
						else {
							logger.warning("Won't add null object to list");
						}
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
			}

			return objToAdd;
		}

		/*@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (dataBinding == getValue()) {
				// updateVariableValue();
			}
			if (dataBinding == getList()) {
				// updateVariableList();
			}
			super.notifiedBindingChanged(dataBinding);
		}*/

		public T getAssignationValue(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			if (getAssignableAction() != null) {
				try {
					return getAssignableAction().execute(evaluationContext);
				} catch (ReturnException e) {
					e.printStackTrace();
				}
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

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getAssignableAction() != null) {
				getAssignableAction().getBindingModel().setBaseBindingModel(getBaseBindingModel(getAssignableAction()));
			}
		}

	}

	// TODO: a rule that check that assignableAction is not null

	/*@DefineValidationRule
	public static class ValueBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AddToListAction> {
		public ValueBindingIsRequiredAndMustBeValid() {
			super("'value'_binding_is_not_valid", AddToListAction.class);
		}
	
		@Override
		public DataBinding<?> getBinding(AddToListAction object) {
			return object.getValue();
		}
	
	}*/

	@DefineValidationRule
	public static class ListBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AddToListAction> {
		public ListBindingIsRequiredAndMustBeValid() {
			super("'list'_binding_is_not_valid", AddToListAction.class);
		}

		@Override
		public DataBinding<?> getBinding(AddToListAction object) {
			return object.getList();
		}

	}

}
