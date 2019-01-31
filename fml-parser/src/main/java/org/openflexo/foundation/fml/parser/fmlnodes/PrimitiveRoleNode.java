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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AJavaBasicRoleDeclaration;

/**
 * @author sylvain
 * 
 */
public class PrimitiveRoleNode extends FlexoPropertyNode<AJavaBasicRoleDeclaration, PrimitiveRole<?>> {

	private static final Logger logger = Logger.getLogger(PrimitiveRoleNode.class.getPackage().getName());

	public PrimitiveRoleNode(AJavaBasicRoleDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public PrimitiveRoleNode(PrimitiveRole<?> property, FMLSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public PrimitiveRole<?> buildFMLObjectFromAST() {
		PrimitiveRole<?> returned = getFactory().newPrimitiveRole();
		returned.setName(getName(getASTNode().getVariableDeclarator()).getText());
		returned.setPrimitiveType(getTypeFactory().getPrimitiveType(getASTNode().getType()));
		return returned;
	}

	@Override
	public String getNormalizedFMLRepresentation(PrettyPrintContext context) {
		return "<primitive_role>";
	}

	@Override
	public String updateFMLRepresentation(PrettyPrintContext context) {
		System.out.println("********* updateFMLRepresentation for primitive_role<?> " + getFMLObject());
		return "<primitive_role>";
	}

}
