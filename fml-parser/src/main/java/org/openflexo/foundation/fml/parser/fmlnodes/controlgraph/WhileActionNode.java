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
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ADoStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AWhileStatement;
import org.openflexo.foundation.fml.parser.node.AWhileStatementNoShortIfStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.foundation.fml.parser.node.PStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.TKwWhile;
import org.openflexo.foundation.fml.parser.node.TLPar;
import org.openflexo.foundation.fml.parser.node.TRPar;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * A {@link WhileAction}, built from one of
 * 
 * <pre>
 *      statement                {while}                       kw_while l_par expression r_par statement
 *      statement_no_short_if    {while_statement_no_short_if} kw_while l_par expression r_par statement_no_short_if
 *      statement_without_trailing_substatement
 *                               {do_statement}                kw_do statement kw_while l_par expression r_par semi
 * </pre>
 * 
 * The second form is a while nested, unbraced, in the then arm of an if/else; the third one is a loop whose condition is evaluated
 * after each cycle (CORE-D-46, CORE-D-6).
 * 
 * @author sylvain
 * 
 */
public class WhileActionNode extends ControlGraphNode<Node, WhileAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WhileActionNode.class.getPackage().getName());

	public WhileActionNode(Node astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public WhileActionNode(WhileAction whileAction, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(whileAction, analyzer);
	}

	@Override
	public WhileAction buildModelObjectFromAST(Node astNode) {
		WhileAction returned = getFactory().newWhileAction();

		DataBinding<Boolean> condition = ExpressionFactory.makeDataBinding(getExpression(astNode), returned, BindingDefinitionType.GET,
				Boolean.class, getSemanticsAnalyzer(), this);
		returned.setCondition(condition);

		ControlGraphNode<?, ?> iterationCGNode = null;
		if (astNode instanceof AWhileStatement) {
			iterationCGNode = ControlGraphFactory.makeControlGraphNode(((AWhileStatement) astNode).getStatement(), getSemanticsAnalyzer());
		}
		else if (astNode instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			iterationCGNode = ControlGraphFactory.makeControlGraphNode(
					((AWhileStatementNoShortIfStatementNoShortIf) astNode).getStatementNoShortIf(), getSemanticsAnalyzer());
		}
		else if (astNode instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			returned.setEvaluateConditionAfterCycle(true);
			iterationCGNode = ControlGraphFactory.makeControlGraphNode(
					((ADoStatementStatementWithoutTrailingSubstatement) astNode).getStatement(), getSemanticsAnalyzer());
		}
		if (iterationCGNode != null) {
			returned.setControlGraph(iterationCGNode.getModelObject());
			addToChildren(iterationCGNode);
		}

		return returned;
	}

	// The model decides (the inspector can change it); the parsed text only when there is no model yet
	private boolean isDoWhile() {
		if (getModelObject() != null) {
			return getModelObject().getEvaluateConditionAfterCycle();
		}
		return getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		when(() -> !isDoWhile())
				.thenAppend(staticContents(FMLKeywords.While.getKeyword()), getWhileFragment())
				.thenAppend(staticContents(SPACE, "(", ""), getLParFragment())
				.thenAppend(bindingContents(() -> getModelObject().getCondition()), getConditionFragment())
				.thenAppend(staticContents(")"), getRParFragment())
				.thenAppend(staticContents(SPACE, "{", ""), getLBrcFragment())
				.thenAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent))
				.thenAppend(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment())
				.elseAppend(staticContents(FMLKeywords.Do.getKeyword()), getDoFragment())
				.elseAppend(staticContents(SPACE, "{", ""), getLBrcFragment())
				.elseAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent))
				.elseAppend(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment())
				.elseAppend(staticContents(SPACE, FMLKeywords.While.getKeyword(), ""), getWhileFragment())
				.elseAppend(staticContents(SPACE, "(", ""), getLParFragment())
				.elseAppend(bindingContents(() -> getModelObject().getCondition()), getConditionFragment())
				.elseAppend(staticContents(")"), getRParFragment())
				.elseAppend(staticContents(";"), getSemiFragment());

		// @formatter:on

	}

	private static PExpression getExpression(Node astNode) {
		if (astNode instanceof AWhileStatement) {
			return ((AWhileStatement) astNode).getExpression();
		}
		if (astNode instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return ((AWhileStatementNoShortIfStatementNoShortIf) astNode).getExpression();
		}
		if (astNode instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return ((ADoStatementStatementWithoutTrailingSubstatement) astNode).getExpression();
		}
		return null;
	}

	private TKwWhile getKwWhile() {
		if (getASTNode() instanceof AWhileStatement) {
			return ((AWhileStatement) getASTNode()).getKwWhile();
		}
		if (getASTNode() instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return ((AWhileStatementNoShortIfStatementNoShortIf) getASTNode()).getKwWhile();
		}
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return ((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getKwWhile();
		}
		return null;
	}

	private TLPar getLPar() {
		if (getASTNode() instanceof AWhileStatement) {
			return ((AWhileStatement) getASTNode()).getLPar();
		}
		if (getASTNode() instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return ((AWhileStatementNoShortIfStatementNoShortIf) getASTNode()).getLPar();
		}
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return ((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getLPar();
		}
		return null;
	}

	private TRPar getRPar() {
		if (getASTNode() instanceof AWhileStatement) {
			return ((AWhileStatement) getASTNode()).getRPar();
		}
		if (getASTNode() instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return ((AWhileStatementNoShortIfStatementNoShortIf) getASTNode()).getRPar();
		}
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return ((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getRPar();
		}
		return null;
	}

	protected RawSourceFragment getWhileFragment() {
		return getKwWhile() != null ? getFragment(getKwWhile()) : null;
	}

	protected RawSourceFragment getDoFragment() {
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return getFragment(((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getKwDo());
		}
		return null;
	}

	protected RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return getFragment(((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getSemi());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		return getLPar() != null ? getFragment(getLPar()) : null;
	}

	protected RawSourceFragment getConditionFragment() {
		PExpression expression = getExpression(getASTNode());
		return expression != null ? getFragment(expression) : null;
	}

	protected RawSourceFragment getRParFragment() {
		return getRPar() != null ? getFragment(getRPar()) : null;
	}

	protected RawSourceFragment getLBrcFragment() {
		if (getASTNode() instanceof AWhileStatement) {
			return getFragment(getLBrc(((AWhileStatement) getASTNode()).getStatement()));
		}
		if (getASTNode() instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return getFragment(getLBrc(((AWhileStatementNoShortIfStatementNoShortIf) getASTNode()).getStatementNoShortIf()));
		}
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return getFragment(getLBrc(((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getStatement()));
		}
		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		if (getASTNode() instanceof AWhileStatement) {
			return getFragment(getRBrc(((AWhileStatement) getASTNode()).getStatement()));
		}
		if (getASTNode() instanceof AWhileStatementNoShortIfStatementNoShortIf) {
			return getFragment(getRBrc(((AWhileStatementNoShortIfStatementNoShortIf) getASTNode()).getStatementNoShortIf()));
		}
		if (getASTNode() instanceof ADoStatementStatementWithoutTrailingSubstatement) {
			return getFragment(getRBrc(((ADoStatementStatementWithoutTrailingSubstatement) getASTNode()).getStatement()));
		}
		return null;
	}

}
