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

import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AIfElseStatement;
import org.openflexo.foundation.fml.parser.node.AIfSimpleStatement;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ConditionalNode extends ControlGraphNode<Node, ConditionalAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ConditionalNode.class.getPackage().getName());

	public ConditionalNode(Node astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public ConditionalNode(ConditionalAction sequence, MainSemanticsAnalyzer analyser) {
		super(sequence, analyser);
	}

	@Override
	public ConditionalAction buildModelObjectFromAST(Node astNode) {
		ConditionalAction returned = getFactory().newConditionalAction();
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	

		append(staticContents("if"), getIfFragment());
		append(staticContents(SPACE, "(",""), getLParFragment());
		append(dynamicContents(() -> getModelObject().getCondition().toString()), getConditionFragment());
		append(staticContents(")"), getLParFragment());

		//append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getThenControlGraph(), "", Indentation.Indent));
		//append(staticContents(LINE_SEPARATOR,"}", ""), getRBrcFragment());

		/*when(() -> isSettable())
			.thenAppend(staticContents(LINE_SEPARATOR+DOUBLE_SPACE,"set",""), getSetFragment())
			.thenAppend(staticContents("("), getSetLParFragment())
			.thenAppend(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getSetTypeFragment())
			.thenAppend(dynamicContents(() -> ((GetSetProperty<?>)getModelObject()).getValueVariableName()), getSetVariableValueFragment())
			.thenAppend(staticContents(")"), getSetRParFragment())
			.thenAppend(staticContents(SPACE, "{", ""), getSetLBrcFragment())
			.thenAppend(childContents(LINE_SEPARATOR, () -> getSetControlGraph(), LINE_SEPARATOR, Indentation.Indent))
			.thenAppend(staticContents(LINE_SEPARATOR+DOUBLE_SPACE, "}", ""), getSetRBrcFragment());*/

		// @formatter:on

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

}
