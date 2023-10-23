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

import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerExpressionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerFmlActionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationBlockStatement;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class DeclarationActionNode extends AssignableActionNode<AVariableDeclarationBlockStatement, DeclarationAction<?>> {

	private static final Logger logger = Logger.getLogger(DeclarationActionNode.class.getPackage().getName());

	public DeclarationActionNode(AVariableDeclarationBlockStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}
	}

	public DeclarationActionNode(DeclarationAction<?> action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DeclarationAction<?> buildModelObjectFromAST(AVariableDeclarationBlockStatement astNode) {
		DeclarationAction<?> returned = getFactory().newDeclarationAction();
		// System.out.println(">>>>>> Declaration " + astNode);

		returned.setVariableName(getName(astNode.getVariableDeclarator()).getText());
		returned.setDeclaredType(TypeFactory.makeType(astNode.getType(), getSemanticsAnalyzer().getTypingSpace()));

		if (astNode.getVariableDeclarator() instanceof AInitializerExpressionVariableDeclarator) {

			ControlGraphNode<?, ?> assignableActionNode = ControlGraphFactory.makeControlGraphNode(
					((AInitializerExpressionVariableDeclarator) astNode.getVariableDeclarator()).getExpression(), getSemanticsAnalyzer());

			if (assignableActionNode != null) {
				if (assignableActionNode.getModelObject() instanceof AssignableAction) {
					returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
					addToChildren(assignableActionNode);
				}
				else {
					System.err.println("Unexpected " + assignableActionNode.getModelObject());
					Thread.dumpStack();
				}
			}

		}

		if (astNode.getVariableDeclarator() instanceof AInitializerFmlActionVariableDeclarator) {

			ControlGraphNode<?, ?> assignableActionNode = ControlGraphFactory.makeControlGraphNode(
					((AInitializerFmlActionVariableDeclarator) astNode.getVariableDeclarator()).getFmlActionExp(), getSemanticsAnalyzer());

			if (assignableActionNode != null) {
				System.out.println("On obtient " + assignableActionNode);
				// System.exit(-1);
				if (assignableActionNode.getModelObject() instanceof AssignableAction) {
					returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
					addToChildren(assignableActionNode);
				}
				else {
					System.err.println("Unexpected " + assignableActionNode.getModelObject());
					Thread.dumpStack();
				}
			}

			logger.warning("DeclarationAction with EditionAction not implemented yet");
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
		when(() -> hasInitializer()).thenAppend(staticContents(SPACE, "=", SPACE), getAssignOperatorFragment())
		.thenAppend(childContents("", () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
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
				return getFragment(((AIdentifierVariableDeclarator) variableDeclarator).getLidentifier());
			}
			else if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
				return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getLidentifier());
			}
			else if (variableDeclarator instanceof AInitializerFmlActionVariableDeclarator) {
				return getFragment(((AInitializerFmlActionVariableDeclarator) variableDeclarator).getLidentifier());
			}
		}
		return null;
	}

	private boolean hasInitializer() {
		if (getModelObject() != null) {
			return getModelObject().getAssignableAction() != null;
		}
		else if (getASTNode() != null) {
			PVariableDeclarator variableDeclarator = getASTNode().getVariableDeclarator();
			if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
				return false;
			}
			else if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
				return true;
			}
			else if (variableDeclarator instanceof AInitializerFmlActionVariableDeclarator) {
				return true;
			}
		}
		return false;
	}

	/*	private RawSourceFragment getAssignmentFragment() {
			if (hasInitializer()) {
				return getFragment(((AInitializerExpressionVariableDeclarator) getASTNode().getVariableDeclarator()).getExpression());
			}
			return null;
		}*/

	private RawSourceFragment getAssignOperatorFragment() {
		if (hasInitializer() && getASTNode() != null) {
			if (getASTNode().getVariableDeclarator() instanceof AInitializerExpressionVariableDeclarator) {
				return getFragment(((AInitializerExpressionVariableDeclarator) getASTNode().getVariableDeclarator()).getAssign());
			}
			if (getASTNode().getVariableDeclarator() instanceof AInitializerFmlActionVariableDeclarator) {
				return getFragment(((AInitializerFmlActionVariableDeclarator) getASTNode().getVariableDeclarator()).getAssign());
			}
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
