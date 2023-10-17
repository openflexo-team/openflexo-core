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
import org.openflexo.foundation.fml.controlgraph.ExpressionIterationAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AForBasicExpressionStatement;
import org.openflexo.foundation.fml.parser.node.AForBasicStatement;
import org.openflexo.foundation.fml.parser.node.AInitializerExpressionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationForInit;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PForInit;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.foundation.fml.parser.node.PStatementExpression;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *    | {for_basic}               kw_for l_par for_init? [semi1]:semi [semi2]:semi statement_expression? r_par statement
 *    | {for_basic_expression}    kw_for l_par for_init? [semi1]:semi expression [semi2]:semi statement_expression? r_par statement
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ExpressionIterationActionNode extends ControlGraphNode<PStatement, ExpressionIterationAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpressionIterationActionNode.class.getPackage().getName());

	public ExpressionIterationActionNode(AForBasicStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ExpressionIterationActionNode(AForBasicExpressionStatement astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ExpressionIterationActionNode(ExpressionIterationAction iteration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(iteration, analyzer);
	}

	@Override
	public ExpressionIterationAction buildModelObjectFromAST(PStatement astNode) {
		ExpressionIterationAction returned = getFactory().newExpressionIterationAction();

		// System.out.println("buildModelObjectFromAST() for " + astNode);
		// System.out.println("iteratorName=" + getIteratorName(astNode));
		// System.out.println("iteratorType=" + getIteratoType(astNode));
		// System.out.println("initExpression:" + getInitExpression(astNode));
		// System.out.println("conditionExpression:" + getConditionExpression(astNode));
		// System.out.println("statementExpression:" + getStatementExpression(astNode));

		returned.setIteratorName(getIteratorName(astNode));
		returned.setDeclaredType(TypeFactory.makeType(getIteratoType(astNode), getSemanticsAnalyzer().getTypingSpace()));

		DataBinding<?> initExpression = ExpressionFactory.makeDataBinding(getInitExpression(astNode), returned, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);
		DataBinding<Boolean> conditionExpression = ExpressionFactory.makeDataBinding(getConditionExpression(astNode), returned,
				BindingDefinitionType.GET, Boolean.class, getSemanticsAnalyzer(), this);

		ControlGraphNode<?, ?> statementExpressionCGNode = ControlGraphFactory.makeControlGraphNode(getStatementExpression(astNode),
				getSemanticsAnalyzer());

		// System.out.println("initExpression:" + initExpression);
		// System.out.println("conditionExpression:" + conditionExpression);
		// System.out.println("statementExpressionCGNode:" + statementExpressionCGNode + " of " + statementExpressionCGNode.getClass());

		returned.setInitExpression(initExpression);
		returned.setConditionExpression(conditionExpression);
		returned.setStatementExpression(statementExpressionCGNode.getModelObject());
		addToChildren(statementExpressionCGNode);

		ControlGraphNode<?, ?> iterationCGNode = ControlGraphFactory.makeControlGraphNode(getIterationStatement(astNode),
				getSemanticsAnalyzer());
		returned.setControlGraph(iterationCGNode.getModelObject());
		addToChildren(iterationCGNode);

		return returned;
	}

	private String getIteratorName(PStatement astNode) {
		PVariableDeclarator variableDeclarator = getVariableDeclarator(astNode);
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return ((AInitializerExpressionVariableDeclarator) variableDeclarator).getLidentifier().getText();
		}
		else {
			System.err.println("Unexpected variableDeclarator " + variableDeclarator);
			Thread.dumpStack();
		}
		return null;
	}

	private PExpression getInitExpression(PStatement astNode) {
		PVariableDeclarator variableDeclarator = getVariableDeclarator(astNode);
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return ((AInitializerExpressionVariableDeclarator) variableDeclarator).getExpression();
		}
		else {
			System.err.println("Unexpected variableDeclarator " + variableDeclarator);
			Thread.dumpStack();
		}
		return null;
	}

	private PType getIteratoType(PStatement astNode) {
		PForInit variableDeclarator = getForInit(astNode);
		if (variableDeclarator instanceof AVariableDeclarationForInit) {
			return ((AVariableDeclarationForInit) variableDeclarator).getType();
		}
		else {
			System.err.println("Unexpected variableDeclarator " + variableDeclarator);
			Thread.dumpStack();
		}
		return null;
	}

	private PForInit getForInit(PStatement astNode) {
		if (astNode instanceof AForBasicStatement) {
			return ((AForBasicStatement) astNode).getForInit();
		}
		if (astNode instanceof AForBasicExpressionStatement) {
			return ((AForBasicExpressionStatement) astNode).getForInit();
		}
		return null;
	}

	private AVariableDeclarationForInit getVariableDeclaration(PStatement astNode) {
		PForInit forInit = getForInit(astNode);
		if (forInit instanceof AVariableDeclarationForInit) {
			return (AVariableDeclarationForInit) forInit;
		}
		return null;
	}

	private PVariableDeclarator getVariableDeclarator(PStatement astNode) {
		AVariableDeclarationForInit variableDeclaration = getVariableDeclaration(astNode);
		if (variableDeclaration != null) {
			return variableDeclaration.getVariableDeclarator();
		}
		return null;
	}

	private PExpression getConditionExpression(PStatement astNode) {
		if (astNode instanceof AForBasicStatement) {
			return null;
		}
		if (astNode instanceof AForBasicExpressionStatement) {
			return ((AForBasicExpressionStatement) astNode).getExpression();
		}
		return null;
	}

	private PStatementExpression getStatementExpression(PStatement astNode) {
		if (astNode instanceof AForBasicStatement) {
			return ((AForBasicStatement) astNode).getStatementExpression();
		}
		if (astNode instanceof AForBasicExpressionStatement) {
			return ((AForBasicExpressionStatement) astNode).getStatementExpression();
		}
		return null;
	}

	private PStatement getIterationStatement(PStatement astNode) {
		if (astNode instanceof AForBasicStatement) {
			return ((AForBasicStatement) astNode).getStatement();
		}
		if (astNode instanceof AForBasicExpressionStatement) {
			return ((AForBasicExpressionStatement) astNode).getStatement();
		}
		return null;
	}

	/**
	 * <pre>
	 *    | {for_basic}               kw_for l_par for_init? [semi1]:semi [semi2]:semi statement_expression? r_par statement
	 *    | {for_basic_expression}    kw_for l_par for_init? [semi1]:semi expression [semi2]:semi statement_expression? r_par statement
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
		append(staticContents("="), getAssignFragment());
		append(dynamicContents(() -> getModelObject().getInitExpression().toString()), getInitExpressionFragment());
		append(staticContents(SPACE,";",SPACE), getSemi1Fragment());
		append(dynamicContents(() -> getModelObject().getConditionExpression().toString()), getConditionExpressionFragment());
		append(staticContents(SPACE,";",SPACE), getSemi2Fragment());
		append(childContents("", () -> getModelObject().getStatementExpression(), "", Indentation.DoNotIndent));
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	protected RawSourceFragment getTypeFragment() {
		PForInit variableDeclarator = getForInit(getASTNode());
		if (variableDeclarator instanceof AVariableDeclarationForInit) {
			return getFragment(((AVariableDeclarationForInit) variableDeclarator).getType());
		}
		return null;
	}

	protected RawSourceFragment getIteratorNameFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator(getASTNode());
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getLidentifier());
		}
		return null;
	}

	protected RawSourceFragment getForFragment() {
		if (getASTNode() instanceof AForBasicStatement) {
			return getFragment(((AForBasicStatement) getASTNode()).getKwFor());
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getKwFor());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AForBasicStatement) {
			return getFragment(((AForBasicStatement) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getAssignFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator(getASTNode());
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getAssign());
		}
		return null;
	}

	protected RawSourceFragment getInitExpressionFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator(getASTNode());
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getExpression());
		}
		return null;
	}

	protected RawSourceFragment getSemi1Fragment() {
		if (getASTNode() instanceof AForBasicStatement) {
			return getFragment(((AForBasicStatement) getASTNode()).getSemi1());
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getSemi1());
		}
		return null;
	}

	protected RawSourceFragment getConditionExpressionFragment() {
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getExpression());
		}
		return null;
	}

	protected RawSourceFragment getSemi2Fragment() {
		if (getASTNode() instanceof AForBasicStatement) {
			return getFragment(((AForBasicStatement) getASTNode()).getSemi2());
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getSemi2());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AForBasicStatement) {
			return getFragment(((AForBasicStatement) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return getFragment(((AForBasicExpressionStatement) getASTNode()).getRPar());
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
		if (getASTNode() instanceof AForBasicStatement) {
			return ((AForBasicStatement) getASTNode()).getStatement();
		}
		if (getASTNode() instanceof AForBasicExpressionStatement) {
			return ((AForBasicExpressionStatement) getASTNode()).getStatement();
		}
		return null;
	}

}
