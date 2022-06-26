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

import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AForEnhancedExpressionStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *      | {for_enhanced} kw_for l_par type identifier colon expression r_par statement
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class IterationActionNode extends ControlGraphNode<AForEnhancedExpressionStatement, IterationAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IterationActionNode.class.getPackage().getName());

	public IterationActionNode(AForEnhancedExpressionStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public IterationActionNode(IterationAction iteration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(iteration, analyzer);
	}

	@Override
	public IterationAction buildModelObjectFromAST(AForEnhancedExpressionStatement astNode) {
		IterationAction returned = getFactory().newIterationAction();

		returned.setIteratorName(astNode.getLidentifier().getText());

		ControlGraphNode<?, ?> iterationActionCGNode = ControlGraphFactory.makeControlGraphNode(astNode.getExpression(),
				getSemanticsAnalyzer());
		if (iterationActionCGNode.getModelObject() instanceof AssignableAction) {
			returned.setIterationAction((AssignableAction) iterationActionCGNode.getModelObject());
			addToChildren(iterationActionCGNode);
		}
		else {
			throwIssue("Cannot iterate on " + iterationActionCGNode.getModelObject(), getFragment(astNode.getExpression()));
		}

		ControlGraphNode<?, ?> iterationCGNode = ControlGraphFactory.makeControlGraphNode(astNode.getStatement(), getSemanticsAnalyzer());
		returned.setControlGraph(iterationCGNode.getModelObject());
		addToChildren(iterationCGNode);

		return returned;
	}

	/**
	 * <pre>
	 *      | {for_enhanced} kw_for l_par type identifier colon expression r_par statement
	 * </pre>
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("for"), getForFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getItemType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getIteratorName()), getIteratorNameFragment());
		append(staticContents(SPACE, ":", SPACE), getColonFragment());
		append(childContents("", () -> getModelObject().getIterationAction(), "", Indentation.DoNotIndent));
		append(staticContents(")"), getLParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	protected RawSourceFragment getForFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwFor());
		}
		return null;
	}

	protected RawSourceFragment getColonFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getColon());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}

	protected RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	protected RawSourceFragment getIteratorNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLidentifier());
		}
		return null;
	}

	protected RawSourceFragment getIterationActionFragment() {
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
		// System.out.println("getASTNode()=" + getASTNode());

		/*if (getASTNode() instanceof AIfSimpleStatement) {
			PStatement statement = ((AIfSimpleStatement) getASTNode()).getStatement();
			System.out.println("Je cherche le LEFT dans " + statement + " of " + statement.getClass());
		}
		if (getASTNode() instanceof AIfElseStatement) {
			PStatementNoShortIf statement = ((AIfElseStatement) getASTNode()).getStatementNoShortIf();
			System.out.println("Je cherche le LEFT dans " + statement + " of " + statement.getClass());
		}*/

		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		return null;
	}

}
