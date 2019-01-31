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

package org.openflexo.foundation.fml.parser.fmlnodes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.AFloatNumericType;
import org.openflexo.foundation.fml.parser.node.AIntNumericType;
import org.openflexo.foundation.fml.parser.node.ANumericPrimitiveType;
import org.openflexo.foundation.fml.parser.node.APrimitiveType;
import org.openflexo.foundation.fml.parser.node.ASimpleType;
import org.openflexo.foundation.fml.parser.node.AVoidType;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.PNumericType;
import org.openflexo.foundation.fml.parser.node.PPrimitiveType;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.TIdentifier;

/**
 * @author sylvain
 * 
 */
public class AbstractPropertyNode extends FlexoPropertyNode<AAbstractPropertyDeclaration, AbstractProperty<?>> {

	private static final Logger logger = Logger.getLogger(AbstractPropertyNode.class.getPackage().getName());

	public AbstractPropertyNode(AAbstractPropertyDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public AbstractPropertyNode(AbstractProperty<?> property, FMLSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public AbstractProperty<?> makeFMLObject() {
		AbstractProperty<?> returned = getFactory().newAbstractProperty();
		returned.setName(getName(getASTNode().getVariableDeclarator()).getText());

		System.out.println("Pour le type, il s'agit de " + makeType(getASTNode().getType()));

		return returned;
	}

	protected Type makeType(PType pType) {
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
		}
		else if (pType instanceof AComplexType) {
		}
		logger.warning("Unexpected " + pType);
		return null;
	}

	protected List<String> makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		List<String> returned = new ArrayList<>();
		returned.add(identifier.getText());
		for (PAdditionalIdentifier pAdditionalIdentifier : additionalIdentifiers) {
			if (pAdditionalIdentifier instanceof AAdditionalIdentifier) {
				returned.add(((AAdditionalIdentifier) pAdditionalIdentifier).getIdentifier().getText());
			}
		}
		return returned;
	}

	protected Type makeType(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		List<String> fqIds = makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
		System.out.println("On cherche " + fqIds);
		return null;
	}

	@Override
	public String getNormalizedFMLRepresentation(PrettyPrintContext context) {
		return "<abstract_property>";
	}

	@Override
	public String updateFMLRepresentation(PrettyPrintContext context) {
		System.out.println("********* updateFMLRepresentation for AbstractProperty<?> " + getFMLObject());
		return "<abstract_property>";
	}

}
