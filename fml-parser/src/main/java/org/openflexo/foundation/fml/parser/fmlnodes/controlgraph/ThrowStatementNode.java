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

import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.ThrowStatement;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AThrowStatementWithoutTrailingSubstatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * The node of {@code throw <expression>;} (CORE-F-11 phase 7)
 */
public class ThrowStatementNode extends AssignableActionNode<AThrowStatementWithoutTrailingSubstatement, ThrowStatement<?>> {

	public ThrowStatementNode(AThrowStatementWithoutTrailingSubstatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}
	}

	public ThrowStatementNode(ThrowStatement<?> action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ThrowStatement<?> buildModelObjectFromAST(AThrowStatementWithoutTrailingSubstatement astNode) {
		ThrowStatement<?> returned = getFactory().newThrowStatement();
		ControlGraphNode<?, ?> valueNode = ControlGraphFactory.makeControlGraphNode(astNode.getExpression(), getSemanticsAnalyzer());
		if (valueNode != null && valueNode.getModelObject() instanceof AssignableAction) {
			returned.setAssignableAction((AssignableAction) valueNode.getModelObject());
			addToChildren(valueNode);
		}
		else {
			throwIssue("Cannot throw " + astNode.getExpression(), getFragment(astNode.getExpression()));
		}
		return returned;
	}

	@Override
	public void definePrettyPrint(boolean hasParsedVersion) {
		super.definePrettyPrint(hasParsedVersion);

		append(staticContents("throw"), getThrowFragment());
		append(childContents(SPACE, () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
		append(staticContents(";"), getSemiFragment());
	}

	@Override
	protected RawSourceFragment getSemiFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getSemi()) : null;
	}

	protected RawSourceFragment getThrowFragment() {
		return getASTNode() != null ? getFragment(getASTNode().getKwThrow()) : null;
	}
}
