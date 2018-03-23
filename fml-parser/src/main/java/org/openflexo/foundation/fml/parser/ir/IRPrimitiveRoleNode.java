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

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.APrimitivePrimitiveRoleDeclaration;

public class IRPrimitiveRoleNode extends IRFlexoPropertyNode<PrimitiveRole<?>, APrimitivePrimitiveRoleDeclaration> {

	public IRPrimitiveRoleNode(APrimitivePrimitiveRoleDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);

	}

	@Override
	PrimitiveRole<?> buildFMLObject() {
		Type javaType = TypeAnalyzingUtils.makeJavaType(getNode().getPrimitiveType());
		PrimitiveType primitiveType = TypeAnalyzingUtils.getPrimitiveType(javaType);
		PropertyCardinality cardinality = SemanticsAnalyzingUtils.makeCardinality(getNode().getCardinality());
		if (cardinality == null) {
			cardinality = PropertyCardinality.ZeroOne;
		}
		if (primitiveType != null) {
			PrimitiveRole<?> primitiveRole = getSemanticsAnalyzer().getFactory().newInstance(PrimitiveRole.class);
			primitiveRole.setName(getNode().getIdentifier().getText());
			primitiveRole.setPrimitiveType(primitiveType);
			primitiveRole.setCardinality(cardinality);
			// System.out.println("******** Hop je cree un nouveau PrimitiveRole " + getNode().getIdentifier().getText());
			getFragment().printFragment();
			return primitiveRole;
		}
		else {
			logger.warning("Cannot find primitive type for " + getNode().getPrimitiveType());
			return null;
		}
	}

	/*@Override
	protected String getFMLAnnotation(FMLRepresentationContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append("@" + getImplementedInterface().getSimpleName() + "(cardinality=" + getCardinality() + ",readOnly=" + isReadOnly()
				+ ")", context);
		if (isKey()) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append("@Key", context);
		}
		return out.toString();
	}*/

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append("public " + TypeUtils.simpleRepresentation(getFMLObject().getResultingType()) + " " + getFMLObject().getName() + ";",
				context);
		return out.toString();
	}
}
