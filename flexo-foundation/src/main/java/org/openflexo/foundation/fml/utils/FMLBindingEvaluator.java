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

package org.openflexo.foundation.fml.utils;

import java.lang.reflect.Type;

import org.openflexo.connie.BindingEvaluator;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.connie.java.JavaTypingSpace;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;

/**
 * Utility class allowing to compute binding value over an expression and a given object in the context of FML expression language.<br>
 * Expression must be expressed with or without supplied object (when mentioned, use "object." prefix).<br>
 * Considering we are passing a String, valid binding path are for example:
 * <ul>
 * <li>toString</li>
 * <li>toString()</li>
 * <li>toString()+' hash='+object.hashCode()</li>
 * <li>substring(6,11)</li>
 * <li>substring(3,length()-2)+' hash='+hashCode()</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
final public class FMLBindingEvaluator extends BindingEvaluator {

	// TODO : reimplement this for FML

	private static JavaBindingFactory JAVA_BINDING_FACTORY = new JavaBindingFactory();
	private static JavaTypingSpace JAVA_TYPING_SPACE = new JavaTypingSpace();

	private FMLBindingEvaluator(Object object, Type objectType, BindingFactory bindingFactory) {
		super(object, objectType, bindingFactory, JAVA_TYPING_SPACE);
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}

	public static Object evaluateBinding(String bindingPath, Object object, Type objectType, BindingFactory bindingFactory)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, ReflectiveOperationException {

		FMLBindingEvaluator evaluator = new FMLBindingEvaluator(object, objectType, bindingFactory);
		Object returned = evaluator.evaluate(bindingPath);
		evaluator.delete();
		return returned;
	}

	public static Object evaluateBinding(String bindingPath, Object object, Type objectType)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, ReflectiveOperationException {
		return evaluateBinding(bindingPath, object, objectType, JAVA_BINDING_FACTORY);
	}

	public static Object evaluateBinding(String bindingPath, Object object, BindingFactory bindingFactory)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, ReflectiveOperationException {

		return evaluateBinding(bindingPath, object, object != null ? object.getClass() : null, bindingFactory);
	}

	public static Object evaluateBinding(String bindingPath, Object object)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, ReflectiveOperationException {
		return evaluateBinding(bindingPath, object, JAVA_BINDING_FACTORY);
	}

}
