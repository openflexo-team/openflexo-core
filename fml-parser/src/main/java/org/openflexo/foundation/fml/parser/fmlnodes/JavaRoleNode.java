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

import org.openflexo.foundation.fml.JavaRole;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AJavaBasicRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class JavaRoleNode extends FlexoPropertyNode<AJavaBasicRoleDeclaration, JavaRole<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JavaRoleNode.class.getPackage().getName());

	public JavaRoleNode(AJavaBasicRoleDeclaration astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public JavaRoleNode(JavaRole<?> property, MainSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public JavaRole<?> buildModelObjectFromAST(AJavaBasicRoleDeclaration astNode) {
		JavaRole<?> returned = getFactory().newJavaRole();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		returned.setName(getName(astNode.getVariableDeclarator()).getText());
		returned.setType(getTypeFactory().makeType(astNode.getType()));
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			PVariableDeclarator variableDeclarator = getASTNode().getVariableDeclarator();
			if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
				return getFragment(((AIdentifierVariableDeclarator) variableDeclarator).getIdentifier());
			}
			else if (variableDeclarator instanceof AInitializerVariableDeclarator) {
				return getFragment(((AInitializerVariableDeclarator) variableDeclarator).getIdentifier());
			}
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

}
