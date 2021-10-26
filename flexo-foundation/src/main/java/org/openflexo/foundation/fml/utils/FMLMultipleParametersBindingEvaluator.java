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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.MultipleParametersBindingEvaluator;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;

/**
 * Utility class allowing to compute binding value over an expression and a given set of objects in the context of Java expression
 * language.<br>
 * Expression must be expressed with or without supplied object (when mentioned, use "this." prefix).<br>
 * 
 * Syntax is this:
 * 
 * <pre>
 * {$variable1}+' '+{$variable2}+' !'"
 * </pre>
 * 
 * for an expression with the two variables variable1 and variable2
 * 
 * @author sylvain
 * 
 */
final public class FMLMultipleParametersBindingEvaluator extends MultipleParametersBindingEvaluator {

	private FMLMultipleParametersBindingEvaluator(Map<String, Object> objects, BindingFactory bindingFactory) {
		super(objects, bindingFactory);
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}

	/**
	 * Utility method used to instanciate a {@link FMLMultipleParametersBindingEvaluator} to compute a given expression expressed in CONNIE
	 * language, and a set of arguments given in appearing order in the expression<br>
	 * 
	 * @param bindingPath
	 *            expression to compute
	 * @param bindingFactory
	 *            {@link BindingFactory} to use, JavaBindingFactory is used if none supplied
	 * @param receiver
	 *            the object which is the default target ('object' path)
	 * @param args
	 *            arguments given in appearing order in the expression
	 * @return computed value
	 * @throws InvalidKeyValuePropertyException
	 * @throws TypeMismatchException
	 * @throws NullReferenceException
	 * @throws InvocationTargetException
	 */
	public static Object evaluateBinding(String bindingPath, BindingFactory bindingFactory, Object receiver, Object... args)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, ReflectiveOperationException {

		Map<String, Object> objects = new HashMap<>();

		List<String> parameters = new ArrayList<>();
		String extractedBindingPath = extractParameters(bindingPath, parameters, args);
		// System.out.println("extractedBindingPath=" + extractedBindingPath);
		if (args.length != parameters.size()) {
			throw new InvalidKeyValuePropertyException("Wrong number of args");
		}
		objects.put("this", receiver);
		for (int i = 0; i < args.length; i++) {
			// System.out.println("i=" + i + " " + parameters.get(i) + "=" + args[i]);
			objects.put(parameters.get(i), args[i]);
		}

		FMLMultipleParametersBindingEvaluator evaluator = new FMLMultipleParametersBindingEvaluator(objects, bindingFactory);

		String normalizedBindingPath = evaluator.normalizeBindingPath(extractedBindingPath, parameters);
		// System.out.println("normalizedBindingPath=" + normalizedBindingPath);

		Object returned = evaluator.evaluate(normalizedBindingPath);
		evaluator.delete();
		return returned;
	}
}
