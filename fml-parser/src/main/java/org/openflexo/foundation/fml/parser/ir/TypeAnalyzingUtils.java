/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.ir;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.SemanticsException;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AByteIntegralType;
import org.openflexo.foundation.fml.parser.node.ACharIntegralType;
import org.openflexo.foundation.fml.parser.node.ADoubleFloatingPointType;
import org.openflexo.foundation.fml.parser.node.AFloatFloatingPointType;
import org.openflexo.foundation.fml.parser.node.AFloatingNumericType;
import org.openflexo.foundation.fml.parser.node.AIntIntegralType;
import org.openflexo.foundation.fml.parser.node.AIntegralNumericType;
import org.openflexo.foundation.fml.parser.node.AJavaTypeActualTypeArgument;
import org.openflexo.foundation.fml.parser.node.ALongIntegralType;
import org.openflexo.foundation.fml.parser.node.AManyTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.ANumericPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AOneTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.APrimitiveActualTypeArgument;
import org.openflexo.foundation.fml.parser.node.AShortIntegralType;
import org.openflexo.foundation.fml.parser.node.ATypeArguments;
import org.openflexo.foundation.fml.parser.node.PActualTypeArgument;
import org.openflexo.foundation.fml.parser.node.PDotIdentifier;
import org.openflexo.foundation.fml.parser.node.PFloatingPointType;
import org.openflexo.foundation.fml.parser.node.PIntegralType;
import org.openflexo.foundation.fml.parser.node.PNumericType;
import org.openflexo.foundation.fml.parser.node.PPrimitiveType;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.PTypeArguments;

/**
 * This class implements some utils to convert node to Type<br>
 * 
 * @author sylvain
 * 
 */
public class TypeAnalyzingUtils {

	static PrimitiveType getPrimitiveType(Type javaType) {
		if (TypeUtils.isLong(javaType)) {
			return PrimitiveType.Long;
		}
		else if (TypeUtils.isByte(javaType) || TypeUtils.isShort(javaType) || TypeUtils.isInteger(javaType)) {
			return PrimitiveType.Integer;
		}
		else if (TypeUtils.isDouble(javaType)) {
			return PrimitiveType.Double;
		}
		else if (TypeUtils.isFloat(javaType)) {
			return PrimitiveType.Float;
		}
		else if (TypeUtils.isBoolean(javaType)) {
			return PrimitiveType.Boolean;
		}
		else if (String.class.equals(javaType)) {
			return PrimitiveType.String;
		}
		else if (Date.class.equals(javaType)) {
			return PrimitiveType.Date;
		}
		return null;
	}

	static Class<?> makeJavaType(PDotIdentifier dotIdentifier, FMLSemanticsAnalyzer analyzer) throws SemanticsException {
		String className = TextAnalyzingUtils.asText(dotIdentifier);

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			for (String javaImport : analyzer.getFMLCompilationUnit().getJavaImports()) {
				String lastPathElement = javaImport.substring(javaImport.lastIndexOf(".") + 1);
				if (lastPathElement.equals(className)) {
					try {
						return Class.forName(javaImport);
					} catch (ClassNotFoundException e1) {
					}
				}
				else if (lastPathElement.equals("*")) {
					String attempt = javaImport.substring(0, javaImport.lastIndexOf(".")) + "." + className;
					try {
						return Class.forName(attempt);
					} catch (ClassNotFoundException e1) {
					}
				}
				else {
					String attempt = javaImport + "." + className;
					try {
						return Class.forName(attempt);
					} catch (ClassNotFoundException e1) {
					}
				}
			}

			throw new SemanticsException("Cannot find class " + className, dotIdentifier, analyzer);
		}
	}

	static Type makeJavaType(PDotIdentifier dotIdentifier, PTypeArguments args, FMLSemanticsAnalyzer analyzer) throws SemanticsException {
		Class<?> baseClass = makeJavaType(dotIdentifier, analyzer);
		List<Type> typeVariables = makeTypeList(args, analyzer);
		return new ParameterizedTypeImpl(baseClass, typeVariables.toArray(new Type[typeVariables.size()]));
	}

	private static Type makeType(PActualTypeArgument typeArg, FMLSemanticsAnalyzer analyzer) throws SemanticsException {
		if (typeArg instanceof AJavaTypeActualTypeArgument) {
			if (((AJavaTypeActualTypeArgument) typeArg).getTypeArguments() != null) {
				return makeJavaType(((AJavaTypeActualTypeArgument) typeArg).getDotIdentifier(),
						((AJavaTypeActualTypeArgument) typeArg).getTypeArguments(), analyzer);
			}
			else {
				return makeJavaType(((AJavaTypeActualTypeArgument) typeArg).getDotIdentifier(), analyzer);
			}
		}
		else if (typeArg instanceof APrimitiveActualTypeArgument) {
			return makeJavaType(((APrimitiveActualTypeArgument) typeArg).getPrimitiveType());
		}
		return null;
	}

	static Type makeJavaType(PPrimitiveType primitiveType) {
		if (primitiveType instanceof ABooleanPrimitiveType) {
			return Boolean.TYPE;
		}
		else if (primitiveType instanceof ANumericPrimitiveType) {
			return makeJavaType((ANumericPrimitiveType) primitiveType);
		}
		return null;
	}

	private static Type makeJavaType(ANumericPrimitiveType numericType) {
		PNumericType pNumericType = numericType.getNumericType();
		if (pNumericType instanceof AIntegralNumericType) {
			return makeJavaType((AIntegralNumericType) pNumericType);
		}
		else if (pNumericType instanceof AFloatingNumericType) {
			return makeJavaType((AFloatingNumericType) pNumericType);
		}
		return null;
	}

	private static Type makeJavaType(AIntegralNumericType numericType) {
		PIntegralType integralType = numericType.getIntegralType();
		if (integralType instanceof AByteIntegralType) {
			return Byte.TYPE;
		}
		if (integralType instanceof ACharIntegralType) {
			return Character.TYPE;
		}
		if (integralType instanceof AIntIntegralType) {
			return Integer.TYPE;
		}
		if (integralType instanceof ALongIntegralType) {
			return Long.TYPE;
		}
		if (integralType instanceof AShortIntegralType) {
			return Short.TYPE;
		}
		return null;
	}

	private static Type makeJavaType(AFloatingNumericType numericType) {
		PFloatingPointType floatingPointType = numericType.getFloatingPointType();
		if (floatingPointType instanceof ADoubleFloatingPointType) {
			return Double.TYPE;
		}
		if (floatingPointType instanceof AFloatFloatingPointType) {
			return Float.TYPE;
		}
		return null;
	}

	private static List<Type> makeTypeList(PTypeArguments args, FMLSemanticsAnalyzer analyzer) throws SemanticsException {
		if (args instanceof ATypeArguments) {
			return makeTypeList((ATypeArguments) args, analyzer);
		}
		return null;
	}

	private static List<Type> makeTypeList(ATypeArguments args, FMLSemanticsAnalyzer analyzer) throws SemanticsException {
		PTypeArgumentList current = args.getTypeArgumentList();
		List<Type> returned = new ArrayList<>();
		while (current != null) {
			Type type = null;
			if (current instanceof AOneTypeArgumentList) {
				type = makeType(((AOneTypeArgumentList) current).getActualTypeArgument(), analyzer);
				current = null;
			}
			else if (current instanceof AManyTypeArgumentList) {
				type = makeType(((AManyTypeArgumentList) current).getActualTypeArgument(), analyzer);
				current = ((AManyTypeArgumentList) current).getTypeArgumentList();
			}
			returned.add(0, type);
		}
		return returned;
	}

}
