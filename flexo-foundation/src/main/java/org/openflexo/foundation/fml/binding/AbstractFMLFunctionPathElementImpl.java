/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.ExpressionTransformer;

/**
 * Default implementation for a {@link FunctionPathElement}
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractFMLFunctionPathElementImpl<F extends Function> extends AbstractFMLPathElementImpl
		implements FunctionPathElement<F> {

	private static final Logger LOGGER = Logger.getLogger(AbstractFMLFunctionPathElementImpl.class.getPackage().getName());

	private F function;
	private Type type;
	private final List<DataBinding<?>> arguments;

	public AbstractFMLFunctionPathElementImpl() {
		arguments = new ArrayList<>();
	}

	/*public AbstractFMLFunctionPathElementImpl(IBindingPathElement parent, String parsed, F function, List<DataBinding<?>> someArguments) {
		super(parent, parsed);
		this.function = function;
		arguments = new ArrayList<>();
		if (someArguments != null) {
			arguments.addAll(someArguments);
		}
		if (function != null) {
			setFunction(function);
		}
	}*/

	@Override
	public void instanciateParameters(Bindable bindable) {
		for (Function.FunctionArgument arg : function.getArguments()) {
			DataBinding<?> parameter = getArgumentValue(arg);
			if (parameter == null) {
				parameter = new DataBinding<>(bindable, arg.getArgumentType(), DataBinding.BindingDefinitionType.GET);
				parameter.setBindingName(arg.getArgumentName());
				setArgumentValue(arg, parameter);
			}
			else {
				parameter.setOwner(bindable);
				parameter.setDeclaredType(arg.getArgumentType());
				parameter.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
		}
	}

	@Override
	public final F getFunction() {
		return function;
	}

	@Override
	public void setFunction(F function) {
		this.function = function;
		if (function != null) {
			for (FunctionArgument arg : function.getArguments()) {
				DataBinding<?> argValue = getArgumentValue(arg);
				if (argValue != null) {
					argValue.setDeclaredType(arg.getArgumentType());
				}
			}
			setType(function.getReturnType());
			setMethodName(function.getName());
		}
		clearSerializationRepresentation();
	}

	@Override
	public String getMethodName() {
		if (getFunction() != null) {
			return getFunction().getName();
		}
		return getParsed();
	}

	@Override
	public void setMethodName(String methodName) {
		setParsed(methodName);
	}

	@Override
	public Type getType() {
		if (getFunction() != null) {
			return getFunction().getReturnType();
		}
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
	}

	private String serializationRepresentation = null;

	// TODO
	// It's better not to cache serialization representation
	// See TestBindingEvaluator, test9
	@Override
	public String getSerializationRepresentation() {
		// if (serializationRepresentation == null) {
		StringBuffer returned = new StringBuffer();
		if (getFunction() != null) {
			returned.append(getFunction().getName());
		}
		else {
			returned.append(getParsed());
		}
		returned.append("(");
		boolean isFirst = true;
		for (DataBinding<?> arg : getArguments()) {
			returned.append((isFirst ? "" : ",") + arg);
			isFirst = false;
		}
		returned.append(")");

		// TODO: keep this commented code for test purposes
		/*if (serializationRepresentation != null && !serializationRepresentation.equals(returned.toString())) {
			System.out.println("C'est la qu'il y a un probleme");
			System.out.println("On avait: " + serializationRepresentation);
			System.out.println("Et maintenant: " + returned.toString());
			Thread.dumpStack();
			System.exit(-1);
		}*/

		serializationRepresentation = returned.toString();
		// }
		// return serializationRepresentation;
		return returned.toString();
	}

	public void clearSerializationRepresentation() {
		serializationRepresentation = null;
	}

	@Override
	public boolean isSettable() {
		return false;
	}

	@Override
	public List<? extends FunctionArgument> getFunctionArguments() {
		if (function != null) {
			return function.getArguments();
		}
		return null;
	}

	@Override
	public List<DataBinding<?>> getArguments() {
		return arguments;
	}

	@Override
	public void setArguments(List<DataBinding<?>> arguments) {
		this.arguments.clear();
		if (arguments != null) {
			for (DataBinding<?> arg : arguments) {
				arg.setOwner(getBindable());
				if (arg.getBindingDefinitionType() == null) {
					arg.setBindingDefinitionType(BindingDefinitionType.GET);
				}
				if (arg.getDeclaredType() == null) {
					arg.setDeclaredType(Object.class);
				}
				this.arguments.add(arg);
			}
		}
	}

	@Override
	public DataBinding<?> getArgumentValue(String argumentName) {
		if (getFunction() == null) {
			return null;
		}
		int i = 0;
		for (FunctionArgument arg : getFunctionArguments()) {
			if (arg.getArgumentName().equals(argumentName) && i < arguments.size()) {
				return arguments.get(i);
			}
			i++;
		}
		return null;
	}

	@Override
	public DataBinding<?> getArgumentValue(Function.FunctionArgument argument) {
		if (getFunction() == null) {
			return null;
		}
		int index = getFunctionArguments().indexOf(argument);
		if (index > -1 && index < arguments.size()) {
			return arguments.get(index);
		}
		return null;
	}

	@Override
	public void setArgumentValue(Function.FunctionArgument argument, DataBinding<?> value) {

		if (getFunction() == null) {
			return;
		}
		int index = getFunctionArguments().indexOf(argument);
		if (index == -1) {
			LOGGER.warning("Unexpected argument: " + argument);
			return;
		}
		if (index < arguments.size()) {
			arguments.remove(index);
			arguments.add(index, value);
		}
		else {
			for (int i = arguments.size(); i < index; i++) {
				arguments.add(i, null);
			}
			arguments.add(index, value);
		}
		serializationRepresentation = null;
	}

	/*@Override
	public final String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "/" + getSerializationRepresentation();
	}*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (isResolved()) {
			result = prime * result + Objects.hash(getFunction());
		}
		else {
			result = prime * result + Objects.hash(getParsed());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractFMLFunctionPathElementImpl<?> other = (AbstractFMLFunctionPathElementImpl<?>) obj;
		if (!Objects.equals(getArguments(), other.getArguments())) {
			return false;
		}
		if (isResolved() != other.isResolved()) {
			return false;
		}
		if (isResolved()) {
			return Objects.equals(getFunction(), other.getFunction());
		}
		else {
			return Objects.equals(getParsed(), other.getParsed());
		}
	}

	@Override
	public boolean isNotifyingBindingPathChanged() {
		return false;
	}

	@Override
	public abstract FunctionPathElement<F> transform(ExpressionTransformer transformer) throws TransformException;

	@Override
	public BindingPathCheck checkBindingPathIsValid(IBindingPathElement parentElement, Type parentType) {

		BindingPathCheck check = super.checkBindingPathIsValid(parentElement, parentType);

		if (getFunction() == null) {
			check.invalidBindingReason = "invalid null function";
			check.valid = false;
			return check;
		}

		for (FunctionArgument arg : getFunction().getArguments()) {
			DataBinding<?> argValue = getArgumentValue(arg);
			// System.out.println("Checking " + argValue + " valid="
			// + argValue.isValid());
			if (argValue == null) {
				check.invalidBindingReason = "Parameter value for function: " + getFunction() + " : " + "invalid null argument "
						+ arg.getArgumentName();
				check.valid = false;
				return check;
			}
			if (!argValue.isValid()) {
				check.invalidBindingReason = "Parameter value for function: " + getFunction() + " : " + "invalid argument "
						+ arg.getArgumentName() + " reason=" + argValue.invalidBindingReason();
				check.valid = false;
				return check;
			}
		}

		return check;
	}

}
