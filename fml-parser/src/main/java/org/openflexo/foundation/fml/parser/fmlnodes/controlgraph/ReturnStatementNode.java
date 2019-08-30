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
import org.openflexo.foundation.fml.parser.AssignableActionFactory;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.AEmptyReturnStatement;
import org.openflexo.foundation.fml.parser.node.AExpressionReturnStatement;
import org.openflexo.foundation.fml.parser.node.AIdentifierReturnStatement;
import org.openflexo.foundation.fml.parser.node.PReturnStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ReturnStatementNode extends AssignableActionNode<PReturnStatement, ReturnStatement<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReturnStatementNode.class.getPackage().getName());

	public ReturnStatementNode(PReturnStatement astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public ReturnStatementNode(ReturnStatement<?> action, ControlGraphFactory cgFactory) {
		super(action, cgFactory);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReturnStatement<?> buildModelObjectFromAST(PReturnStatement astNode) {
		ReturnStatement<?> returned = getFactory().newReturnStatement();

		if (getASTNode() instanceof AExpressionReturnStatement) {
			AssignableActionNode<?, ?> assignableActionNode = AssignableActionFactory
					.makeAssignableActionNode(((AExpressionReturnStatement) getASTNode()).getExpression(), getAbstractAnalyser());
			if (assignableActionNode != null) {
				returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
				addToChildren(assignableActionNode);
			}
		}
		else if (getASTNode() instanceof AIdentifierReturnStatement) {
			InnerExpressionActionNode expressionNode = new InnerExpressionActionNode((AIdentifierReturnStatement) getASTNode(),
					getAbstractAnalyser());
			expressionNode.deserialize();
			returned.setAssignableAction((AssignableAction) expressionNode.getModelObject());
			addToChildren(expressionNode);
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("return"), getReturnFragment());
		when(() -> !isEmpty()).thenAppend(childContents(SPACE, () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
		append(staticContents(";"), getSemiFragment());

	}

	private boolean isEmpty() {
		if (getModelObject() != null) {
			return getModelObject().getAssignableAction() == null;
		}
		else {
			return getASTNode() instanceof AEmptyReturnStatement;
		}
	}

	/*private DataBinding<?> extractLeft(ReturnStatement<?> bindable) {
		if (getASTNode() instanceof AExpressionReturnStatement) {
			return makeBinding(((AExpressionReturnStatement) getASTNode()).getExpression(), bindable);
		}
		else if (getASTNode() instanceof AIdentifierReturnStatement) {
			return makeBinding(((AIdentifierReturnStatement) getASTNode()).getIdentifier(),
					((AIdentifierReturnStatement) getASTNode()).getAdditionalIdentifiers(), bindable);
		}
		return null;
	}*/

	@Override
	protected RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof AEmptyReturnStatement) {
			return getFragment(((AEmptyReturnStatement) getASTNode()).getSemi());
		}
		else if (getASTNode() instanceof AExpressionReturnStatement) {
			return getFragment(((AExpressionReturnStatement) getASTNode()).getSemi());
		}
		else if (getASTNode() instanceof AIdentifierReturnStatement) {
			return getFragment(((AIdentifierReturnStatement) getASTNode()).getSemi());
		}
		return null;
	}

	protected RawSourceFragment getReturnFragment() {
		if (getASTNode() instanceof AEmptyReturnStatement) {
			return getFragment(((AEmptyReturnStatement) getASTNode()).getReturn());
		}
		else if (getASTNode() instanceof AExpressionReturnStatement) {
			return getFragment(((AExpressionReturnStatement) getASTNode()).getReturn());
		}
		else if (getASTNode() instanceof AIdentifierReturnStatement) {
			return getFragment(((AIdentifierReturnStatement) getASTNode()).getReturn());
		}
		return null;
	}

}
