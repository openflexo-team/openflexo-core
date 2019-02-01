/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.parser.node.AAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.AFloatNumericType;
import org.openflexo.foundation.fml.parser.node.AGtTypeArguments;
import org.openflexo.foundation.fml.parser.node.AIntNumericType;
import org.openflexo.foundation.fml.parser.node.ANumericPrimitiveType;
import org.openflexo.foundation.fml.parser.node.APrimitiveType;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AReferenceTypeArgument;
import org.openflexo.foundation.fml.parser.node.AShrTypeArguments;
import org.openflexo.foundation.fml.parser.node.ASimpleType;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentList;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.AUshrTypeArguments;
import org.openflexo.foundation.fml.parser.node.AVoidType;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.PNumericType;
import org.openflexo.foundation.fml.parser.node.PPrimitiveType;
import org.openflexo.foundation.fml.parser.node.PReferenceType;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PTypeArgument;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.PTypeArguments;
import org.openflexo.foundation.fml.parser.node.TIdentifier;

/**
 * @author sylvain
 * 
 */
public class TypeFactory {

	private static final Logger logger = Logger.getLogger(TypeFactory.class.getPackage().getName());

	private FMLSemanticsAnalyzer analyser;

	public TypeFactory(FMLSemanticsAnalyzer analyser) {
		this.analyser = analyser;
	}

	public List<String> makeFullQualifiedIdentifierList(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		List<String> returned = new ArrayList<>();
		returned.add(identifier.getText());
		for (PAdditionalIdentifier pAdditionalIdentifier : additionalIdentifiers) {
			if (pAdditionalIdentifier instanceof AAdditionalIdentifier) {
				returned.add(((AAdditionalIdentifier) pAdditionalIdentifier).getIdentifier().getText());
			}
		}
		return returned;
	}

	public String makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		StringBuffer returned = new StringBuffer();
		returned.append(identifier.getText());
		for (PAdditionalIdentifier pAdditionalIdentifier : additionalIdentifiers) {
			if (pAdditionalIdentifier instanceof AAdditionalIdentifier) {
				returned.append("." + ((AAdditionalIdentifier) pAdditionalIdentifier).getIdentifier().getText());
			}
		}
		return returned.toString();
	}

	public Type makeType(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		String typeName = makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
		try {
			return Class.forName(typeName);
		} catch (ClassNotFoundException e) {
			// OK, continue
		}
		for (JavaImportDeclaration javaImportDeclaration : analyser.getCompilationUnit().getJavaImports()) {
			if (typeName.equals(javaImportDeclaration.getClassName())) {
				try {
					return Class.forName(javaImportDeclaration.getFullQualifiedClassName());
				} catch (ClassNotFoundException e) {
					// OK, continue
				}
			}
		}
		return new UnresolvedType(typeName);
	}

	public Type makeType(PType pType) {
		if (pType instanceof AVoidType) {
			return Void.TYPE;
		}
		else if (pType instanceof APrimitiveType) {
			PPrimitiveType primitiveType = ((APrimitiveType) pType).getPrimitiveType();
			if (primitiveType instanceof ABooleanPrimitiveType) {
				return Boolean.TYPE;
			}
			else if (primitiveType instanceof ANumericPrimitiveType) {
				PNumericType numericType = ((ANumericPrimitiveType) primitiveType).getNumericType();
				if (numericType instanceof AIntNumericType) {
					return Integer.TYPE;
				}
				else if (numericType instanceof AFloatNumericType) {
					return Float.TYPE;
				}
			}
		}
		else if (pType instanceof ASimpleType) {
			return makeType(((ASimpleType) pType).getIdentifier(), ((ASimpleType) pType).getAdditionalIdentifiers());
		}
		else if (pType instanceof AComplexType) {
			return makeType(((AComplexType) pType).getReferenceType());
		}
		logger.warning("Unexpected " + pType + " of " + pType.getClass());
		return null;
	}

	public Type makeType(PReferenceType referenceType) {
		if (referenceType instanceof AReferenceType) {
			if (((AReferenceType) referenceType).getArgs() == null) {
				return makeType(((AReferenceType) referenceType).getIdentifier(),
						((AReferenceType) referenceType).getAdditionalIdentifiers());
			}
			else {
				Type baseType = makeType(((AReferenceType) referenceType).getIdentifier(),
						((AReferenceType) referenceType).getAdditionalIdentifiers());
				if (baseType instanceof Class) {
					List<Type> typeArguments = makeTypeArguments(((AReferenceType) referenceType).getArgs());
					return new ParameterizedTypeImpl((Class) baseType, typeArguments.toArray(new Type[typeArguments.size()]));
				}
				else {
					logger.warning("Unexpected base type" + baseType);
				}
			}
		}
		logger.warning("Unexpected " + referenceType + " of " + referenceType.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(PTypeArguments someArgs) {
		if (someArgs instanceof AGtTypeArguments) {
			return makeTypeArguments(((AGtTypeArguments) someArgs).getTypeArgumentList());
		}
		if (someArgs instanceof AShrTypeArguments) {
		}
		if (someArgs instanceof AUshrTypeArguments) {
		}
		logger.warning("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(PTypeArgumentList someArgs) {
		if (someArgs instanceof ATypeArgumentList) {
			List<Type> returned = new ArrayList<>();
			returned.add(makeTypeArgument(((ATypeArgumentList) someArgs).getTypeArgument()));
			for (PTypeArgumentListHead pTypeArgumentListHead : ((ATypeArgumentList) someArgs).getTypeArgumentListHead()) {
				if (pTypeArgumentListHead instanceof ATypeArgumentListHead) {
					returned.add(makeTypeArgument(((ATypeArgumentListHead) pTypeArgumentListHead).getTypeArgument()));
				}
				else {
					logger.warning("Unexpected " + pTypeArgumentListHead + " of " + pTypeArgumentListHead.getClass());
				}
			}
			return returned;
		}
		logger.warning("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	private Type makeTypeArgument(PTypeArgument arg) {
		if (arg instanceof AReferenceTypeArgument) {
			return makeType(((AReferenceTypeArgument) arg).getReferenceType());
		}
		logger.warning("Unexpected " + arg + " of " + arg.getClass());
		return null;
	}

	public PrimitiveType getPrimitiveType(PType pType) {
		return getPrimitiveType(makeType(pType));
	}

	public PrimitiveType getPrimitiveType(Type t) {
		for (PrimitiveType primitiveType : PrimitiveType.values()) {
			if (TypeUtils.isTypeAssignableFrom(primitiveType.getType(), t, true)) {
				return primitiveType;
			}
		}
		return null;
	}
}
