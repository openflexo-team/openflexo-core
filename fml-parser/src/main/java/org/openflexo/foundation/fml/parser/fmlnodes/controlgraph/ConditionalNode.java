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
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AIfElseStatement;
import org.openflexo.foundation.fml.parser.node.AIfSimpleStatement;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ConditionalNode extends ControlGraphNode<Node, ConditionalAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ConditionalNode.class.getPackage().getName());

	public ConditionalNode(Node astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ConditionalNode(ConditionalAction sequence, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(sequence, analyzer);
	}

	@Override
	public ConditionalAction buildModelObjectFromAST(Node astNode) {
		ConditionalAction returned = getFactory().newConditionalAction();
		// DataBinding<Boolean> condition = makeBinding(getExpression(astNode), returned);
		DataBinding<Boolean> condition = ExpressionFactory.makeDataBinding(getExpression(astNode), returned, BindingDefinitionType.GET,
				Boolean.class, getSemanticsAnalyzer(), this);

		returned.setCondition(condition);
		if (astNode instanceof AIfSimpleStatement) {
			ControlGraphNode<?, ?> thenCGNode = ControlGraphFactory.makeControlGraphNode(((AIfSimpleStatement) astNode).getStatement(),
					getSemanticsAnalyzer());
			returned.setThenControlGraph(thenCGNode.getModelObject());
			addToChildren(thenCGNode);
		}
		if (astNode instanceof AIfElseStatement) {
			ControlGraphNode<?, ?> thenCGNode = ControlGraphFactory
					.makeControlGraphNode(((AIfElseStatement) astNode).getStatementNoShortIf(), getSemanticsAnalyzer());
			returned.setThenControlGraph(thenCGNode.getModelObject());
			addToChildren(thenCGNode);
			ControlGraphNode<?, ?> elseCGNode = ControlGraphFactory.makeControlGraphNode(((AIfElseStatement) astNode).getStatement(),
					getSemanticsAnalyzer());
			returned.setElseControlGraph(elseCGNode.getModelObject());
			addToChildren(elseCGNode);
		}
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("if"), getIfFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> getModelObject().getCondition().toString()), getConditionFragment());
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getThenLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getThenControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getThenRBrcFragment());

		when(() -> hasElse()).thenAppend(staticContents(LINE_SEPARATOR, "else", ""), getElseFragment())
				.thenAppend(staticContents(SPACE, "{", ""), getElseLBrcFragment())
				.thenAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getElseControlGraph(), "", Indentation.Indent))
				.thenAppend(staticContents(LINE_SEPARATOR, "}", ""), getElseRBrcFragment());

		// @formatter:on

	}

	public boolean hasElse() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return false;
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return true;
		}
		if (getModelObject() != null) {
			return getModelObject().getElseControlGraph() != null && !(getModelObject().getElseControlGraph() instanceof EmptyControlGraph);
		}
		return false;
	}

	protected RawSourceFragment getIfFragment() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(((AIfSimpleStatement) getASTNode()).getKwIf());
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(((AIfElseStatement) getASTNode()).getKwIf());
		}
		return null;
	}

	protected RawSourceFragment getElseFragment() {
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(((AIfElseStatement) getASTNode()).getKwElse());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(((AIfSimpleStatement) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(((AIfElseStatement) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getConditionFragment() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(((AIfSimpleStatement) getASTNode()).getExpression());
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(((AIfElseStatement) getASTNode()).getExpression());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(((AIfSimpleStatement) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(((AIfElseStatement) getASTNode()).getRPar());
		}
		return null;
	}

	public PExpression getExpression(Node astNode) {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return ((AIfSimpleStatement) getASTNode()).getExpression();
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return ((AIfElseStatement) getASTNode()).getExpression();
		}
		return null;
	}

	protected RawSourceFragment getThenLBrcFragment() {

		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(getLBrc(((AIfSimpleStatement) getASTNode()).getStatement()));
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(getLBrc(((AIfElseStatement) getASTNode()).getStatementNoShortIf()));
		}
		return null;
	}

	protected RawSourceFragment getThenRBrcFragment() {
		if (getASTNode() instanceof AIfSimpleStatement) {
			return getFragment(getRBrc(((AIfSimpleStatement) getASTNode()).getStatement()));
		}
		if (getASTNode() instanceof AIfElseStatement) {
			return getFragment(getRBrc(((AIfElseStatement) getASTNode()).getStatementNoShortIf()));
		}
		return null;
	}

	protected RawSourceFragment getElseLBrcFragment() {
		return getFragment(getLBrc(getElseStatement()));
	}

	protected RawSourceFragment getElseRBrcFragment() {
		return getFragment(getRBrc(getElseStatement()));
	}

	protected PStatement getElseStatement() {
		if (getASTNode() instanceof AIfElseStatement) {
			return (((AIfElseStatement) getASTNode()).getStatement());
		}
		return null;
	}

}
