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

import org.openflexo.foundation.fml.controlgraph.CatchClause;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ACatchClause;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * The node of a {@code catch (<Type> <name>) { ... }} clause of a try statement (CORE-F-11 phase 7)
 */
public class CatchClauseNode extends ControlGraphNode<ACatchClause, CatchClause> {

	public CatchClauseNode(ACatchClause astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public CatchClauseNode(CatchClause clause, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(clause, analyzer);
	}

	@Override
	public CatchClause buildModelObjectFromAST(ACatchClause astNode) {
		CatchClause returned = getFactory().newCatchClause();
		returned.setVariableName(astNode.getIdent().getText());
		returned.setCatchType(TypeFactory.makeType(astNode.getReferenceType(), getSemanticsAnalyzer().getTypingSpace()));
		ControlGraphNode<?, ?> blockNode = ControlGraphFactory.makeControlGraphNode(astNode.getBlock(), getSemanticsAnalyzer());
		if (blockNode != null) {
			returned.setControlGraph(blockNode.getModelObject());
			addToChildren(blockNode);
		}
		return returned;
	}

	@Override
	public void definePrettyPrint(boolean hasParsedVersion) {
		super.definePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(staticContents("catch"), getCatchFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getCatchType()), SPACE).keepingParsedTextWhileUnchanged(), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getVariableName()), getVariableNameFragment());
		append(staticContents(")"), getRParFragment());
		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());
		// @formatter:on
	}

	protected RawSourceFragment getCatchFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getKwCatch()) : null;
	}

	protected RawSourceFragment getLParFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getLPar()) : null;
	}

	protected RawSourceFragment getTypeFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getReferenceType()) : null;
	}

	protected RawSourceFragment getVariableNameFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getIdent()) : null;
	}

	protected RawSourceFragment getRParFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getRPar()) : null;
	}

	protected RawSourceFragment getLBrcFragment() {
		return getASTNode() != null ? getFragment(((ABlock) getASTNode().getBlock()).getLBrc()) : null;
	}

	protected RawSourceFragment getRBrcFragment() {
		return getASTNode() != null ? getFragment(((ABlock) getASTNode().getBlock()).getRBrc()) : null;
	}
}
