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
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AExpressionReturnValueStatement;
import org.openflexo.foundation.fml.parser.node.AFmlActionReturnValueStatement;
import org.openflexo.foundation.fml.parser.node.PReturnValueStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ReturnStatementNode extends AssignableActionNode<PReturnValueStatement, ReturnStatement<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReturnStatementNode.class.getPackage().getName());

	public ReturnStatementNode(PReturnValueStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public ReturnStatementNode(ReturnStatement<?> action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReturnStatement<?> buildModelObjectFromAST(PReturnValueStatement astNode) {
		ReturnStatement<?> returned = getFactory().newReturnStatement();

		// Right
		ControlGraphNode<?, ?> assignableActionNode = null;
		if (astNode instanceof AExpressionReturnValueStatement) {
			assignableActionNode = ControlGraphFactory.makeControlGraphNode(((AExpressionReturnValueStatement) astNode).getExpression(),
					getSemanticsAnalyzer());
		}
		if (astNode instanceof AFmlActionReturnValueStatement) {
			assignableActionNode = ControlGraphFactory.makeControlGraphNode(((AFmlActionReturnValueStatement) astNode).getFmlActionExp(),
					getSemanticsAnalyzer());
		}
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
		else {
			System.err.println("Unexpected " + astNode);
			Thread.dumpStack();
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("return"), getReturnFragment());
		append(childContents(SPACE, () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
		append(staticContents(";"), getSemiFragment());

	}

	@Override
	protected RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof AExpressionReturnValueStatement) {
			return getFragment(((AExpressionReturnValueStatement) getASTNode()).getSemi());
		}
		if (getASTNode() instanceof AFmlActionReturnValueStatement) {
			return getFragment(((AFmlActionReturnValueStatement) getASTNode()).getSemi());
		}
		return null;
	}

	protected RawSourceFragment getReturnFragment() {
		if (getASTNode() instanceof AExpressionReturnValueStatement) {
			return getFragment(((AExpressionReturnValueStatement) getASTNode()).getKwReturn());
		}
		if (getASTNode() instanceof AFmlActionReturnValueStatement) {
			return getFragment(((AFmlActionReturnValueStatement) getASTNode()).getKwReturn());
		}
		return null;
	}

}
