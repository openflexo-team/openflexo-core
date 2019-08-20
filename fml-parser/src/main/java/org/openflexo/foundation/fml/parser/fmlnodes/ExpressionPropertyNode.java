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

import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AExpressionExpressionPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AIdentifierExpressionPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.PExpressionPropertyDeclaration;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 * 	expression_property_declaration =
 *       {identifier} visibility? type [identifier]:identifier is [base_identifier]:identifier [additional_identifiers]:additional_identifier* semi |
 *       {expression} visibility? type [identifier]:identifier is [expression_value]:expression semi;
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ExpressionPropertyNode extends FlexoPropertyNode<PExpressionPropertyDeclaration, ExpressionProperty<?>> {

	private static final Logger logger = Logger.getLogger(ExpressionPropertyNode.class.getPackage().getName());

	public ExpressionPropertyNode(PExpressionPropertyDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public ExpressionPropertyNode(ExpressionProperty<?> property, FMLSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public ExpressionProperty<?> buildModelObjectFromAST(PExpressionPropertyDeclaration anAstNode) {
		ExpressionProperty<?> returned = getFactory().newExpressionProperty();

		if (anAstNode instanceof AExpressionExpressionPropertyDeclaration) {
			AExpressionExpressionPropertyDeclaration astNode = (AExpressionExpressionPropertyDeclaration) anAstNode;
			returned.setVisibility(getVisibility(astNode.getVisibility()));
			returned.setName(astNode.getIdentifier().getText());
			returned.setDeclaredType(getTypeFactory().makeType(astNode.getType()));
			returned.setExpression(makeBinding(astNode.getExpressionValue(), returned));
		}
		else if (anAstNode instanceof AIdentifierExpressionPropertyDeclaration) {
			AIdentifierExpressionPropertyDeclaration astNode = (AIdentifierExpressionPropertyDeclaration) anAstNode;
			returned.setVisibility(getVisibility(astNode.getVisibility()));
			returned.setName(astNode.getIdentifier().getText());
			returned.setDeclaredType(getTypeFactory().makeType(astNode.getType()));
			returned.setExpression(makeBinding(astNode.getBaseIdentifier(), astNode.getAdditionalIdentifiers(), returned));
		}
		else {
			logger.warning("Unexpected: " + anAstNode);
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		if (hasParsedVersion && getVisibilityFragment() != null) {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		}
		else {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE);
		}
		if (hasParsedVersion) {
			appendDynamicContents(() -> serializeType(getModelObject().getType()), SPACE, getTypeFragment());
			appendDynamicContents(() -> getModelObject().getName(), SPACE, getNameFragment());
			appendStaticContents("is", SPACE, getIsFragment());
			appendDynamicContents(() -> getModelObject().getExpression().toString(), getExpressionFragment());
			appendStaticContents(";", getSemiFragment());
		}
		else {
			appendDynamicContents(() -> serializeType(getModelObject().getType()), SPACE);
			appendDynamicContents(() -> getModelObject().getName(), SPACE);
			appendStaticContents("is", SPACE);
			appendDynamicContents(() -> getModelObject().getExpression().toString());
			appendStaticContents(";");
		}
	}

	private RawSourceFragment getVisibilityFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getVisibility());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getVisibility());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getType());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getType());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getIdentifier());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getIsFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getIs());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getIs());
		}
		return null;
	}

	private RawSourceFragment getExpressionFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getExpressionValue());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getBaseIdentifier(),
					((AIdentifierExpressionPropertyDeclaration) getASTNode()).getAdditionalIdentifiers());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof AExpressionExpressionPropertyDeclaration) {
			return getFragment(((AExpressionExpressionPropertyDeclaration) getASTNode()).getSemi());
		}
		if (getASTNode() instanceof AIdentifierExpressionPropertyDeclaration) {
			return getFragment(((AIdentifierExpressionPropertyDeclaration) getASTNode()).getSemi());
		}
		return null;
	}

}
