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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.AddClassInstance.AddClassInstanceImpl;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * This action is used to instantiate a new type with some parameters
 * 
 * @author jean-charles roger
 */

@ModelEntity
@ImplementationClass(AddClassInstanceImpl.class)
@XMLElement
@FML("AddClassInstance")
@FMLMigration("ExpressionAction should be used instead")
@Deprecated
public interface AddClassInstance extends AssignableAction<Object> {

	String TYPE = "type";
	String PARAMETERS = "parameters";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@Getter(value = TYPE, isStringConvertable = true)
	@XMLAttribute
	Type getType();

	@Setter(TYPE)
	void setType(Type type);

	@Getter(value = PARAMETERS, cardinality = Cardinality.LIST)
	@XMLElement(xmlTag = "parameter")
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	List<DataBinding<?>> getParameters();

	@Setter(PARAMETERS)
	void setParameters(List<DataBinding<?>> parameters);

	@Adder(PARAMETERS)
	void addToParameters(DataBinding<?> aParameter);

	void createParameter();

	@Remover(PARAMETERS)
	void removeFromParameters(DataBinding<?> aParameter);

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<?> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<?> container);

	abstract class AddClassInstanceImpl extends AssignableActionImpl<Object> implements AddClassInstance {

		static final Logger logger = Logger.getLogger(AddClassInstance.class.getPackage().getName());

		private DataBinding<?> container;

		@Override
		public DataBinding<?> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<?> aContainer) {
			if (aContainer != null) {
				aContainer.setOwner(this);
				aContainer.setBindingName("container");
				aContainer.setDeclaredType(Object.class);
				aContainer.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.container = aContainer;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) {
			try {
				List<DataBinding<?>> parameterDataBindings = getParameters();
				Object[] parameters = new Object[parameterDataBindings.size()];
				Class<?>[] parameterTypes = new Class[parameterDataBindings.size()];

				int n = 0;
				for (DataBinding<?> dataBinding : parameterDataBindings) {
					Object value = dataBinding.getBindingValue(evaluationContext);
					parameters[n] = value;
					parameterTypes[n] = value == null ? Object.class : parameters[n].getClass();
					n += 1;
				}

				Class<?> typeClass = TypeUtils.getBaseClass(getType());

				Constructor<?> constructor = typeClass.getConstructor(parameterTypes);
				return constructor.newInstance(parameters);

			} catch (ReflectiveOperationException | TypeMismatchException | NullReferenceException e) {
				logger.log(Level.SEVERE, "Can't create instance " + getType(), e);
				return null;
			}
		}

		@Override
		public Type getAssignableType() {
			return getType();
		}

		@Override
		public void createParameter() {
			DataBinding<Object> aParameter = new DataBinding<>("\"value\"", this, Object.class, BindingDefinitionType.GET);
			addToParameters(aParameter);
		}

		@Override
		public String getStringRepresentation() {
			return "new " + TypeUtils.simpleRepresentation(getType()) + "()";
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getContainer().rebuild();
			// getParameters().rebuild();
		}

	}

	@DefineValidationRule
	class AddClassMustAddressAType extends ValidationRule<AddClassMustAddressAType, AddClassInstance> {
		public AddClassMustAddressAType() {
			super(AddClassInstance.class, "add_instance_action_must_address_a_valid_type");
		}

		@Override
		public ValidationIssue<AddClassMustAddressAType, AddClassInstance> applyValidation(AddClassInstance action) {
			if (action.getType() == null) {
				return new ValidationError<>(this, action, "add_class_instance_action_doesn't_define_any_type");
			}
			return null;
		}
	}
}
