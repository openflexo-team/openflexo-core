/**
 * 
 * Copyright (c) 2026, Openflexo
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

import java.util.List;

import org.openflexo.foundation.fml.controlgraph.CatchClause;
import org.openflexo.foundation.fml.controlgraph.TryStatement;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ACatchClause;
import org.openflexo.foundation.fml.parser.node.ACatchTryStatement;
import org.openflexo.foundation.fml.parser.node.AFinallyClause;
import org.openflexo.foundation.fml.parser.node.AFinallyTryStatement;
import org.openflexo.foundation.fml.parser.node.PBlock;
import org.openflexo.foundation.fml.parser.node.PCatchClause;
import org.openflexo.foundation.fml.parser.node.PTryStatement;
import org.openflexo.foundation.fml.parser.node.TKwTry;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * The node of {@code try { ... } catch (<Type> e) { ... } ... finally { ... }} (CORE-F-11 phase 7)
 */
public class TryStatementNode extends ControlGraphNode<PTryStatement, TryStatement> {

	public TryStatementNode(PTryStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public TryStatementNode(TryStatement statement, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(statement, analyzer);
	}

	@Override
	public TryStatement buildModelObjectFromAST(PTryStatement astNode) {
		TryStatement returned = getFactory().newTryStatement();

		ControlGraphNode<?, ?> tryNode = ControlGraphFactory.makeControlGraphNode(getTryBlock(astNode), getSemanticsAnalyzer());
		if (tryNode != null) {
			returned.setTryControlGraph(tryNode.getModelObject());
			addToChildren(tryNode);
		}

		for (PCatchClause catchClause : getCatchClauses(astNode)) {
			CatchClauseNode clauseNode = getSemanticsAnalyzer().retrieveFMLNode((ACatchClause) catchClause,
					n -> new CatchClauseNode(n, getSemanticsAnalyzer()));
			returned.addToCatchClauses(clauseNode.getModelObject());
			addToChildren(clauseNode);
		}

		if (astNode instanceof AFinallyTryStatement) {
			PBlock finallyBlock = ((AFinallyClause) ((AFinallyTryStatement) astNode).getFinallyClause()).getBlock();
			ControlGraphNode<?, ?> finallyNode = ControlGraphFactory.makeControlGraphNode(finallyBlock, getSemanticsAnalyzer());
			if (finallyNode != null) {
				returned.setFinallyControlGraph(finallyNode.getModelObject());
				addToChildren(finallyNode);
			}
		}
		return returned;
	}

	private static PBlock getTryBlock(PTryStatement astNode) {
		if (astNode instanceof ACatchTryStatement) {
			return ((ACatchTryStatement) astNode).getBlock();
		}
		if (astNode instanceof AFinallyTryStatement) {
			return ((AFinallyTryStatement) astNode).getBlock();
		}
		return null;
	}

	private static List<PCatchClause> getCatchClauses(PTryStatement astNode) {
		if (astNode instanceof ACatchTryStatement) {
			return ((ACatchTryStatement) astNode).getCatchClauses();
		}
		if (astNode instanceof AFinallyTryStatement) {
			return ((AFinallyTryStatement) astNode).getCatchClauses();
		}
		return List.of();
	}

	private boolean hasFinally() {
		if (getModelObject() != null) {
			return getModelObject().getFinallyControlGraph() != null;
		}
		return getASTNode() instanceof AFinallyTryStatement;
	}

	@Override
	public void definePrettyPrint(boolean hasParsedVersion) {
		super.definePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(staticContents("try"), getTryFragment());
		append(staticContents(SPACE, "{", ""), getTryLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getTryControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getTryRBrcFragment());
		append(childrenContents(SPACE, SPACE, () -> getModelObject().getCatchClauses(), "", "", Indentation.DoNotIndent, CatchClause.class));
		when(() -> hasFinally())
				.thenAppend(staticContents(SPACE, "finally", ""), getFinallyFragment())
				.thenAppend(staticContents(SPACE, "{", ""), getFinallyLBrcFragment())
				.thenAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getFinallyControlGraph(), "", Indentation.Indent))
				.thenAppend(staticContents(LINE_SEPARATOR, "}", ""), getFinallyRBrcFragment());
		// @formatter:on
	}

	protected RawSourceFragment getTryFragment() {
		TKwTry kw = getASTNode() instanceof ACatchTryStatement ? ((ACatchTryStatement) getASTNode()).getKwTry()
				: getASTNode() instanceof AFinallyTryStatement ? ((AFinallyTryStatement) getASTNode()).getKwTry() : null;
		return kw != null ? getFragment(kw) : null;
	}

	protected RawSourceFragment getTryLBrcFragment() {
		PBlock block = getASTNode() != null ? getTryBlock(getASTNode()) : null;
		return block != null ? getFragment(((ABlock) block).getLBrc()) : null;
	}

	protected RawSourceFragment getTryRBrcFragment() {
		PBlock block = getASTNode() != null ? getTryBlock(getASTNode()) : null;
		return block != null ? getFragment(((ABlock) block).getRBrc()) : null;
	}

	private AFinallyClause getFinallyClause() {
		return getASTNode() instanceof AFinallyTryStatement ? (AFinallyClause) ((AFinallyTryStatement) getASTNode()).getFinallyClause()
				: null;
	}

	protected RawSourceFragment getFinallyFragment() {
		return getFinallyClause() != null ? getFragment(getFinallyClause().getKwFinally()) : null;
	}

	protected RawSourceFragment getFinallyLBrcFragment() {
		return getFinallyClause() != null ? getFragment(((ABlock) getFinallyClause().getBlock()).getLBrc()) : null;
	}

	protected RawSourceFragment getFinallyRBrcFragment() {
		return getFinallyClause() != null ? getFragment(((ABlock) getFinallyClause().getBlock()).getRBrc()) : null;
	}
}
