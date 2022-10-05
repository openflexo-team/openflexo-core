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
import org.openflexo.foundation.fml.parser.node.AForEnhancedFmlActionStatement;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *      | {for_enhanced_expression} kw_for l_par type lidentifier colon expression r_par statement
 *      | {for_enhanced_fml_action} kw_for l_par type lidentifier colon fml_action_exp r_par statement
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class IterationActionNode extends ControlGraphNode<PStatement, IterationAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IterationActionNode.class.getPackage().getName());

	public IterationActionNode(AForEnhancedExpressionStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public IterationActionNode(AForEnhancedFmlActionStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public IterationActionNode(IterationAction iteration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(iteration, analyzer);
	}

	@Override
	public IterationAction buildModelObjectFromAST(PStatement astNode) {
		IterationAction returned = getFactory().newIterationAction();

		returned.setIteratorName(getIteratorName(astNode));

		ControlGraphNode<?, ?> iterationActionCGNode = getIterationActionCGNode(astNode);
		if (iterationActionCGNode.getModelObject() instanceof AssignableAction) {
			returned.setIterationAction((AssignableAction) iterationActionCGNode.getModelObject());
			addToChildren(iterationActionCGNode);
		}
		else {
			if (astNode instanceof AForEnhancedExpressionStatement) {
				throwIssue("Cannot iterate on " + iterationActionCGNode.getModelObject(),
						getFragment(((AForEnhancedExpressionStatement) astNode).getExpression()));
			}
			if (astNode instanceof AForEnhancedFmlActionStatement) {
				throwIssue("Cannot iterate on " + iterationActionCGNode.getModelObject(),
						getFragment(((AForEnhancedFmlActionStatement) astNode).getFmlActionExp()));
			}
			throwIssue("Cannot iterate on " + iterationActionCGNode.getModelObject(), getFragment(astNode));
		}

		ControlGraphNode<?, ?> iterationCGNode = ControlGraphFactory.makeControlGraphNode(getIterationStatement(astNode),
				getSemanticsAnalyzer());
		returned.setControlGraph(iterationCGNode.getModelObject());
		addToChildren(iterationCGNode);

		return returned;
	}

	private String getIteratorName(PStatement astNode) {
		if (astNode instanceof AForEnhancedExpressionStatement) {
			return ((AForEnhancedExpressionStatement) astNode).getLidentifier().getText();
		}
		if (astNode instanceof AForEnhancedFmlActionStatement) {
			return ((AForEnhancedFmlActionStatement) astNode).getLidentifier().getText();
		}
		return null;
	}

	private PStatement getIterationStatement(PStatement astNode) {
		if (astNode instanceof AForEnhancedExpressionStatement) {
			return ((AForEnhancedExpressionStatement) astNode).getStatement();
		}
		if (astNode instanceof AForEnhancedFmlActionStatement) {
			return ((AForEnhancedFmlActionStatement) astNode).getStatement();
		}
		return null;
	}

	private ControlGraphNode<?, ?> getIterationActionCGNode(PStatement astNode) {
		if (astNode instanceof AForEnhancedExpressionStatement) {
			return ControlGraphFactory.makeControlGraphNode(((AForEnhancedExpressionStatement) astNode).getExpression(),
					getSemanticsAnalyzer());
		}
		if (astNode instanceof AForEnhancedFmlActionStatement) {
			return ControlGraphFactory.makeControlGraphNode(((AForEnhancedFmlActionStatement) astNode).getFmlActionExp(),
					getSemanticsAnalyzer());
		}
		return null;
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
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	protected RawSourceFragment getForFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getKwFor());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getKwFor());
		}
		return null;
	}

	protected RawSourceFragment getColonFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getColon());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getColon());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getTypeFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getType());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getType());
		}
		return null;
	}

	protected RawSourceFragment getIteratorNameFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getLidentifier());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getLidentifier());
		}
		return null;
	}

	protected RawSourceFragment getIterationFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getExpression());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getFmlActionExp());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return getFragment(((AForEnhancedExpressionStatement) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return getFragment(((AForEnhancedFmlActionStatement) getASTNode()).getRPar());
		}
		return null;
	}

	protected RawSourceFragment getLBrcFragment() {
		return getFragment(getLBrc(getStatement()));
	}

	protected RawSourceFragment getRBrcFragment() {
		return getFragment(getRBrc(getStatement()));
	}

	protected PStatement getStatement() {
		if (getASTNode() instanceof AForEnhancedExpressionStatement) {
			return ((AForEnhancedExpressionStatement) getASTNode()).getStatement();
		}
		if (getASTNode() instanceof AForEnhancedFmlActionStatement) {
			return ((AForEnhancedFmlActionStatement) getASTNode()).getStatement();
		}
		return null;
	}

}
