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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationBlockStatement;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class DeclarationActionNode extends AssignableActionNode<AVariableDeclarationBlockStatement, DeclarationAction<?>> {

	private static final Logger logger = Logger.getLogger(DeclarationActionNode.class.getPackage().getName());

	public DeclarationActionNode(AVariableDeclarationBlockStatement astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);
	}

	public DeclarationActionNode(DeclarationAction<?> action, ControlGraphFactory cgFactory) {
		super(action, cgFactory);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DeclarationAction<?> buildModelObjectFromAST(AVariableDeclarationBlockStatement astNode) {
		DeclarationAction<?> returned = getFactory().newDeclarationAction();
		// System.out.println(">>>>>> Declaration " + astNode);

		returned.setVariableName(getName(astNode.getVariableDeclarator()).getText());
		returned.setDeclaredType(getTypeFactory().makeType(astNode.getType()));

		if (astNode.getVariableDeclarator() instanceof AInitializerVariableDeclarator) {
			// TODO: on doit plutot trouver une autre action, c'est plus complique que ca
			DataBinding<Object> initValue = makeBinding(((AInitializerVariableDeclarator) astNode.getVariableDeclarator()).getExpression(),
					returned);
			returned.setAssignableAction((ExpressionAction) getFactory().newExpressionAction(initValue));
		}

		return returned;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		append(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getVariableName()), getNameFragment());
		when(() -> hasInitializer())
			.thenAppend(staticContents("="), getAssignOperatorFragment())
			.thenAppend(dynamicContents(() -> ((ExpressionAction) getModelObject().getAssignableAction()).getExpression().toString()),
				getAssignmentFragment());
		append(staticContents(";"), getSemiFragment());
		// @formatter:on	

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

	private boolean hasInitializer() {
		if (getModelObject() != null) {
			logger.warning("Il manque ici le code qui detecte l'initialiseur !!!!");
		}
		else if (getASTNode() != null) {
			PVariableDeclarator variableDeclarator = getASTNode().getVariableDeclarator();
			if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
				return false;
			}
			else if (variableDeclarator instanceof AInitializerVariableDeclarator) {
				return true;
			}
		}
		return false;
	}

	private RawSourceFragment getAssignmentFragment() {
		if (hasInitializer()) {
			return getFragment(((AInitializerVariableDeclarator) getASTNode().getVariableDeclarator()).getExpression());
		}
		return null;
	}

	private RawSourceFragment getAssignOperatorFragment() {
		if (hasInitializer()) {
			return getFragment(((AInitializerVariableDeclarator) getASTNode().getVariableDeclarator()).getAssign());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

}
