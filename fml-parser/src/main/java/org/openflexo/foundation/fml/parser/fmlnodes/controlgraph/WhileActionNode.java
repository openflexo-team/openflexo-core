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
import org.openflexo.foundation.fml.parser.node.AWhileStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *      | {while}     kw_while l_par expression r_par statement
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class WhileActionNode extends ControlGraphNode<AWhileStatement, WhileAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WhileActionNode.class.getPackage().getName());

	public WhileActionNode(AWhileStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public WhileActionNode(WhileAction whileAction, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(whileAction, analyzer);
	}

	@Override
	public WhileAction buildModelObjectFromAST(AWhileStatement astNode) {
		WhileAction returned = getFactory().newWhileAction();

		DataBinding<Boolean> condition = ExpressionFactory.makeDataBinding(astNode.getExpression(), returned, BindingDefinitionType.GET,
				Boolean.class, getSemanticsAnalyzer(), this);
		returned.setCondition(condition);

		ControlGraphNode<?, ?> iterationCGNode = ControlGraphFactory.makeControlGraphNode(astNode.getStatement(), getSemanticsAnalyzer());
		returned.setControlGraph(iterationCGNode.getModelObject());
		addToChildren(iterationCGNode);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents(FMLKeywords.While.getKeyword()), getWhileFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> getModelObject().getCondition().toString()), getConditionFragment());
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	protected RawSourceFragment getWhileFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwWhile());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}

	protected RawSourceFragment getConditionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRPar());
		}
		return null;
	}

	protected RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getLBrc(getASTNode().getStatement()));
		}
		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getRBrc(getASTNode().getStatement()));
		}
		return null;
	}

}
