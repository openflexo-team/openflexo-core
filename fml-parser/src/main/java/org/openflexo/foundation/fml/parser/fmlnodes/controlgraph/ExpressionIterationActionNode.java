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
import org.openflexo.foundation.fml.parser.node.AEmptyForStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.AExpressionForStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.AForBasicExpressionStatement;
import org.openflexo.foundation.fml.parser.node.AForBasicStatement;
import org.openflexo.foundation.fml.parser.node.AInitializerExpressionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationForInit;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PForInit;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.foundation.fml.parser.node.PStatementExpression;
import org.openflexo.foundation.fml.parser.node.PStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.TKwFor;
import org.openflexo.foundation.fml.parser.node.TLPar;
import org.openflexo.foundation.fml.parser.node.TRPar;
import org.openflexo.foundation.fml.parser.node.TSemi;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * An {@link ExpressionIterationAction}, built from a classic {@code for} loop, in one of the forms
 * 
 * <pre>
 *    statement             | {for_basic}            kw_for l_par for_init? [semi1]:semi [semi2]:semi statement_expression? r_par statement
 *                          | {for_basic_expression} kw_for l_par for_init? [semi1]:semi expression [semi2]:semi statement_expression? r_par statement
 *    for_statement_no_short_if
 *                          | {empty}                kw_for l_par for_init? [semi1]:semi [semi2]:semi statement_expression? r_par statement_no_short_if
 *                          | {expression}           kw_for l_par for_init? [semi1]:semi expression [semi2]:semi statement_expression? r_par statement_no_short_if
 * </pre>
 * 
 * The last two are loops nested, unbraced, in the then arm of an if/else (CORE-D-46). The condition and the update are optional: a
 * missing condition is true, as in Java (CORE-D-9).
 */
public class ExpressionIterationActionNode extends ControlGraphNode<Node, ExpressionIterationAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpressionIterationActionNode.class.getPackage().getName());

	public ExpressionIterationActionNode(Node astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ExpressionIterationActionNode(ExpressionIterationAction iteration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(iteration, analyzer);
	}

	/**
	 * The parts of a for header, whatever the production it comes from
	 */
	private static class ForParts {
		TKwFor kwFor;
		TLPar lPar;
		PForInit forInit;
		TSemi semi1;
		PExpression condition;
		TSemi semi2;
		PStatementExpression update;
		TRPar rPar;
		PStatement body;
		PStatementNoShortIf bodyNoShortIf;
	}

	private static ForParts getParts(Node astNode) {
		ForParts parts = new ForParts();
		if (astNode instanceof AForBasicStatement) {
			AForBasicStatement n = (AForBasicStatement) astNode;
			parts.kwFor = n.getKwFor();
			parts.lPar = n.getLPar();
			parts.forInit = n.getForInit();
			parts.semi1 = n.getSemi1();
			parts.semi2 = n.getSemi2();
			parts.update = n.getStatementExpression();
			parts.rPar = n.getRPar();
			parts.body = n.getStatement();
		}
		else if (astNode instanceof AForBasicExpressionStatement) {
			AForBasicExpressionStatement n = (AForBasicExpressionStatement) astNode;
			parts.kwFor = n.getKwFor();
			parts.lPar = n.getLPar();
			parts.forInit = n.getForInit();
			parts.semi1 = n.getSemi1();
			parts.condition = n.getExpression();
			parts.semi2 = n.getSemi2();
			parts.update = n.getStatementExpression();
			parts.rPar = n.getRPar();
			parts.body = n.getStatement();
		}
		else if (astNode instanceof AEmptyForStatementNoShortIf) {
			AEmptyForStatementNoShortIf n = (AEmptyForStatementNoShortIf) astNode;
			parts.kwFor = n.getKwFor();
			parts.lPar = n.getLPar();
			parts.forInit = n.getForInit();
			parts.semi1 = n.getSemi1();
			parts.semi2 = n.getSemi2();
			parts.update = n.getStatementExpression();
			parts.rPar = n.getRPar();
			parts.bodyNoShortIf = n.getStatementNoShortIf();
		}
		else if (astNode instanceof AExpressionForStatementNoShortIf) {
			AExpressionForStatementNoShortIf n = (AExpressionForStatementNoShortIf) astNode;
			parts.kwFor = n.getKwFor();
			parts.lPar = n.getLPar();
			parts.forInit = n.getForInit();
			parts.semi1 = n.getSemi1();
			parts.condition = n.getExpression();
			parts.semi2 = n.getSemi2();
			parts.update = n.getStatementExpression();
			parts.rPar = n.getRPar();
			parts.bodyNoShortIf = n.getStatementNoShortIf();
		}
		return parts;
	}

	private ForParts getParts() {
		return getParts(getASTNode());
	}

	@Override
	public ExpressionIterationAction buildModelObjectFromAST(Node astNode) {
		ExpressionIterationAction returned = getFactory().newExpressionIterationAction();
		ForParts parts = getParts(astNode);

		AVariableDeclarationForInit declaration = parts.forInit instanceof AVariableDeclarationForInit
				? (AVariableDeclarationForInit) parts.forInit
				: null;
		PVariableDeclarator declarator = declaration != null ? declaration.getVariableDeclarator() : null;
		if (declarator instanceof AInitializerExpressionVariableDeclarator) {
			returned.setIteratorName(((AInitializerExpressionVariableDeclarator) declarator).getLidentifier().getText());
			returned.setDeclaredType(TypeFactory.makeType(declaration.getType(), getSemanticsAnalyzer().getTypingSpace()));
			returned.setInitExpression(ExpressionFactory.makeDataBinding(((AInitializerExpressionVariableDeclarator) declarator).getExpression(),
					returned, BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer(), this));
		}
		else {
			getSemanticsAnalyzer().throwIssue(returned, "A for loop must declare and initialize its iteration variable (for (int i = 0; ...))",
					getFragment(astNode), getStartPosition());
		}

		if (parts.condition != null) {
			DataBinding<Boolean> conditionExpression = ExpressionFactory.makeDataBinding(parts.condition, returned,
					BindingDefinitionType.GET, Boolean.class, getSemanticsAnalyzer(), this);
			returned.setConditionExpression(conditionExpression);
		}

		if (parts.update != null) {
			ControlGraphNode<?, ?> statementExpressionCGNode = ControlGraphFactory.makeControlGraphNode(parts.update,
					getSemanticsAnalyzer());
			returned.setStatementExpression(statementExpressionCGNode.getModelObject());
			addToChildren(statementExpressionCGNode);
		}

		ControlGraphNode<?, ?> iterationCGNode = parts.body != null
				? ControlGraphFactory.makeControlGraphNode(parts.body, getSemanticsAnalyzer())
				: ControlGraphFactory.makeControlGraphNode(parts.bodyNoShortIf, getSemanticsAnalyzer());
		returned.setControlGraph(iterationCGNode.getModelObject());
		addToChildren(iterationCGNode);

		return returned;
	}

	private boolean hasCondition() {
		return getModelObject() != null && getModelObject().getConditionExpression() != null
				&& getModelObject().getConditionExpression().isSet();
	}

	@Override
	public void definePrettyPrint(boolean hasParsedVersion) {
		super.definePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("for"), getForFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getItemType()), SPACE).keepingParsedTextWhileUnchanged(), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getIteratorName()), getIteratorNameFragment());
		append(staticContents("="), getAssignFragment());
		append(bindingContents(() -> getModelObject().getInitExpression()), getInitExpressionFragment());
		append(staticContents(SPACE,";",SPACE), getSemi1Fragment());
		when(() -> hasCondition())
				.thenAppend(bindingContents(() -> getModelObject().getConditionExpression()), getConditionExpressionFragment());
		append(staticContents(SPACE,";",SPACE), getSemi2Fragment());
		append(childContents("", () -> getModelObject().getStatementExpression(), "", Indentation.DoNotIndent));
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), "", Indentation.Indent));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	private PVariableDeclarator getVariableDeclarator() {
		PForInit forInit = getParts().forInit;
		return forInit instanceof AVariableDeclarationForInit ? ((AVariableDeclarationForInit) forInit).getVariableDeclarator() : null;
	}

	protected RawSourceFragment getTypeFragment() {
		PForInit forInit = getParts().forInit;
		if (forInit instanceof AVariableDeclarationForInit) {
			PType type = ((AVariableDeclarationForInit) forInit).getType();
			return getFragment(type);
		}
		return null;
	}

	protected RawSourceFragment getIteratorNameFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator();
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getLidentifier());
		}
		return null;
	}

	protected RawSourceFragment getForFragment() {
		return getParts().kwFor != null ? getFragment(getParts().kwFor) : null;
	}

	protected RawSourceFragment getLParFragment() {
		return getParts().lPar != null ? getFragment(getParts().lPar) : null;
	}

	protected RawSourceFragment getAssignFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator();
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getAssign());
		}
		return null;
	}

	protected RawSourceFragment getInitExpressionFragment() {
		PVariableDeclarator variableDeclarator = getVariableDeclarator();
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return getFragment(((AInitializerExpressionVariableDeclarator) variableDeclarator).getExpression());
		}
		return null;
	}

	protected RawSourceFragment getSemi1Fragment() {
		return getParts().semi1 != null ? getFragment(getParts().semi1) : null;
	}

	protected RawSourceFragment getConditionExpressionFragment() {
		return getParts().condition != null ? getFragment(getParts().condition) : null;
	}

	protected RawSourceFragment getSemi2Fragment() {
		return getParts().semi2 != null ? getFragment(getParts().semi2) : null;
	}

	protected RawSourceFragment getRParFragment() {
		return getParts().rPar != null ? getFragment(getParts().rPar) : null;
	}

	protected RawSourceFragment getLBrcFragment() {
		ForParts parts = getParts();
		if (parts.body != null) {
			return getFragment(getLBrc(parts.body));
		}
		if (parts.bodyNoShortIf != null) {
			return getFragment(getLBrc(parts.bodyNoShortIf));
		}
		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		ForParts parts = getParts();
		if (parts.body != null) {
			return getFragment(getRBrc(parts.body));
		}
		if (parts.bodyNoShortIf != null) {
			return getFragment(getRBrc(parts.bodyNoShortIf));
		}
		return null;
	}

}
