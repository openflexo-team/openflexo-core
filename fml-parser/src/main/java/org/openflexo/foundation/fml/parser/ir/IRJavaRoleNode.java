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
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.SemanticsException;
import org.openflexo.foundation.fml.parser.node.AJavaClassJavaRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AJavaRoleDeclarationPrimitiveRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AParameterizedTypeJavaRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.PJavaRoleDeclaration;

public class IRJavaRoleNode extends IRNode<FlexoRole<?>, AJavaRoleDeclarationPrimitiveRoleDeclaration> {

	public IRJavaRoleNode(AJavaRoleDeclarationPrimitiveRoleDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
	}

	@Override
	FlexoRole<?> buildFMLObject() {

		PJavaRoleDeclaration decl = getNode().getJavaRoleDeclaration();
		Type javaType = null;
		String roleName = null;
		PrimitiveType primitiveType = null;
		try {
			if (decl instanceof AJavaClassJavaRoleDeclaration) {
				javaType = TypeAnalyzingUtils.makeJavaType(((AJavaClassJavaRoleDeclaration) decl).getDotIdentifier(),
						getSemanticsAnalyzer());
				roleName = ((AJavaClassJavaRoleDeclaration) decl).getIdentifier().getText();
			}
			else if (decl instanceof AParameterizedTypeJavaRoleDeclaration) {
				javaType = TypeAnalyzingUtils.makeJavaType(((AParameterizedTypeJavaRoleDeclaration) decl).getDotIdentifier(),
						((AParameterizedTypeJavaRoleDeclaration) decl).getTypeArguments(), getSemanticsAnalyzer());
				roleName = ((AParameterizedTypeJavaRoleDeclaration) decl).getIdentifier().getText();
			}

			if (javaType != null) {
				primitiveType = TypeAnalyzingUtils.getPrimitiveType(javaType);
				if (primitiveType != null) {
					PrimitiveRole<?> primitiveRole = getSemanticsAnalyzer().getFactory().newInstance(PrimitiveRole.class);
					primitiveRole.setName(roleName);
					primitiveRole.setPrimitiveType(primitiveType);
					primitiveRole.setCardinality(PropertyCardinality.ZeroMany);
					// System.out.println("******** Hop je cree un nouveau PrimitiveRole " + roleName);
					getFragment().printFragment();
					return primitiveRole;
				}
				else {
					// TODO: create a JavaTypeRole
					logger.warning("JavaTypeRole name=" + roleName + " type=" + javaType + " not implemented");
				}
			}

		} catch (SemanticsException e) {
			fireSemanticsException(e);
		}

		/*PrimitiveRole<?> primitiveRole = getSemanticsAnalyzer().getFactory().newInstance(PrimitiveRole.class);
		primitiveRole.setName(getNode().getIdentifier().getText());
		System.out.println("******** Hop je cree un nouveau PrimitiveRole " + getNode().getIdentifier().getText());
		getFragment().printFragment();
		return primitiveRole;*/
		return null;
	}

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
