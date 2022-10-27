/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.foundation.fml.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.java.util.JavaBindingEvaluator;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.binding.DataBindingPropertyImplementation.DataBindingProperty;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.property.DefaultSinglePropertyImplementation;
import org.openflexo.pamela.model.property.ParameteredPropertyImplementation;

public class DataBindingPropertyImplementation<O extends FMLObject, T> extends DefaultSinglePropertyImplementation<O, DataBinding<T>>
		implements ParameteredPropertyImplementation<DataBindingProperty> {

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Target(value = ElementType.METHOD)
	public @interface DataBindingProperty {
		String bindingName();

		Class<?> declaredType();

		String owner();

		BindingDefinitionType bindingDefinitionType();

		boolean isMandatory();
	}

	private static final Logger logger = Logger.getLogger(DataBindingPropertyImplementation.class.getPackage().getName());

	private DataBindingProperty propertyImplementationParameters = null;

	public DataBindingPropertyImplementation(ProxyMethodHandler<O> handler, ModelProperty<O> property) throws InvalidDataException {
		super(handler, property);
	}

	@Override
	public DataBindingProperty getParameters() {
		if (propertyImplementationParameters == null) {
			propertyImplementationParameters = getProperty().getGetterMethod().getAnnotation(DataBindingProperty.class);
		}
		if (propertyImplementationParameters == null) {
			logger.warning("Cannot find annotation DataBindingProperty in " + getProperty().getGetterMethod());
		}
		return propertyImplementationParameters;
	}

	public Class<T> getDeclaredType() {
		if (getParameters() != null) {
			return (Class<T>) getParameters().declaredType();
		}
		return (Class<T>) Object.class;
	}

	public O getOwner() {
		if (getParameters() != null) {
			Object owner;
			try {
				owner = JavaBindingEvaluator.evaluateBinding(getParameters().owner(), getObject());
				// System.out.println(getParameters().owner() + " > " + computedOwner);
				if (owner == null) {
					logger.warning("Cannot compute owner with " + getParameters().owner() + " : null value");
				}
				return (O) owner;
			} catch (InvalidKeyValuePropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// return (Class<T>) getParameters().declaredType();
		}
		return getObject();
	}

	public BindingDefinitionType getBindingDefinitionType() {
		if (getParameters() != null) {
			return getParameters().bindingDefinitionType();
		}
		return DataBinding.BindingDefinitionType.GET;
	}

	public String getBindingName() {
		if (getParameters() != null) {
			return getParameters().bindingName();
		}
		return getProperty().getPropertyIdentifier();
	}

	public boolean isMandatory() {
		if (getParameters() != null) {
			return getParameters().isMandatory();
		}
		return true;
	}

	@Override
	public DataBinding<T> get() throws ModelDefinitionException {
		DataBinding<T> returned = super.get();
		if (returned == null) {
			returned = new DataBinding<>(getObject(), getDeclaredType(), getBindingDefinitionType());
			returned.setBindingName(getBindingName());
			returned.setMandatory(isMandatory());
			setInternalValue(returned);
		}
		return returned;
	}

	@Override
	public void set(DataBinding<T> aValue) throws ModelDefinitionException {
		if (aValue != null) {
			aValue.setOwner(getOwner());
			aValue.setDeclaredType(getDeclaredType());
			aValue.setBindingDefinitionType(getBindingDefinitionType());
			aValue.setBindingName(getBindingName());
			aValue.setMandatory(isMandatory());
		}
		setInternalValue(aValue);
		getObject().notifiedBindingChanged(aValue);

	}

	@Override
	public void update(DataBinding<T> aValue) throws ModelDefinitionException {
		// System.out.println("Hop, l'objet " + getObject() + " change son binding " + getBindingName() + " pour " + aValue.toString());
		get().setUnparsedBinding(aValue.toString());
	}
}
