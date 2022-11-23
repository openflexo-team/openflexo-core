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

package org.openflexo.foundation.fml.parser.fmlnodes;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.controlgraph.ControlStructureAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.ABasicSimpleAssertDeclaration;
import org.openflexo.foundation.fml.parser.node.AFailureClause;
import org.openflexo.foundation.fml.parser.node.AProtectedSimpleAssertDeclaration;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PSimpleAssertDeclaration;
import org.openflexo.foundation.fml.parser.node.TKwAssert;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *      simple_assert_declaration = 
 *        {basic}     kw_assert expression semi
 *      | {protected} kw_assert expression failure_clause;
 *
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class SimpleAssertNode extends AbstractAssertNode<PSimpleAssertDeclaration, SimpleInvariant> {

	public SimpleAssertNode(PSimpleAssertDeclaration astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public SimpleAssertNode(SimpleInvariant simpleInvariant, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(simpleInvariant, analyzer);
	}

	@Override
	public SimpleInvariant buildModelObjectFromAST(PSimpleAssertDeclaration astNode) {
		SimpleInvariant returned = getFactory().newSimpleInvariant();

		PExpression pExp = null;
		if (astNode instanceof ABasicSimpleAssertDeclaration) {
			pExp = ((ABasicSimpleAssertDeclaration) astNode).getExpression();
		}
		else if (astNode instanceof AProtectedSimpleAssertDeclaration) {
			pExp = ((AProtectedSimpleAssertDeclaration) astNode).getExpression();
		}
		DataBinding<Boolean> constraint = ExpressionFactory.makeDataBinding(pExp, returned, BindingDefinitionType.GET, Boolean.class,
				getSemanticsAnalyzer(), this);
		returned.setConstraint(constraint);

		if (astNode instanceof AProtectedSimpleAssertDeclaration) {
			AFailureClause failureClause = (AFailureClause) ((AProtectedSimpleAssertDeclaration) astNode).getFailureClause();
			ControlGraphNode<?, ?> violationCGNode = ControlGraphFactory.makeControlGraphNode(failureClause.getStatement(),
					getSemanticsAnalyzer());
			returned.setViolationControlGraph(violationCGNode.getModelObject());
			addToChildren(violationCGNode);
		}

		return returned;
	}

	@Override
	public SimpleAssertNode deserialize() {
		if (getParent() instanceof IterationAssertNode) {
			((IterationAssertNode) getParent()).getModelObject().addToSimpleInvariants(getModelObject());
		}
		return (SimpleAssertNode) super.deserialize();
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(staticContents("", FMLKeywords.Assert.getKeyword(), SPACE), getAssertFragment());
		append(dynamicContents(() -> getModelObject().getConstraint().toString()), getExpressionFragment());

		// With onfailure clause with single line violation control graph
		when(() -> getModelObject().hasFailureClause() && isSingleLineControlGraph(getModelObject().getViolationControlGraph()))
			.thenAppend(staticContents(SPACE,FMLKeywords.OnFailure.getKeyword(),""), getOnFailureFragment())
			.thenAppend(staticContents("",":",SPACE), getColonFragment())
			.thenAppend(childContents("",() -> getModelObject().getViolationControlGraph(),"", Indentation.DoNotIndent));
		// With onfailure clause with violation control graph embraced with braces {...}
		when(() -> getModelObject().hasFailureClause() && !isSingleLineControlGraph(getModelObject().getViolationControlGraph()))
			.thenAppend(staticContents(SPACE,FMLKeywords.OnFailure.getKeyword(),""), getOnFailureFragment())
			.thenAppend(staticContents("",":",SPACE), getColonFragment())
			.thenAppend(staticContents("","{",LINE_SEPARATOR), getLBrcFragment())
			.thenAppend(childContents("",() -> getModelObject().getViolationControlGraph(),"", Indentation.Indent))
			.thenAppend(staticContents(LINE_SEPARATOR,"}",""), getRBrcFragment());
		// Without onfailure clause
		when(() -> !getModelObject().hasFailureClause())
			.thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on

	}

	private boolean isSingleLineControlGraph(FMLControlGraph cg) {
		if (cg instanceof ControlStructureAction || cg instanceof Sequence) {
			return false;
		}
		return true;
	}

	private TKwAssert getAssertKeyword() {
		if (getASTNode() instanceof ABasicSimpleAssertDeclaration) {
			return ((ABasicSimpleAssertDeclaration) getASTNode()).getKwAssert();
		}
		if (getASTNode() instanceof AProtectedSimpleAssertDeclaration) {
			return ((AProtectedSimpleAssertDeclaration) getASTNode()).getKwAssert();
		}
		return null;
	}

	private PExpression getPExpression() {
		if (getASTNode() instanceof ABasicSimpleAssertDeclaration) {
			return ((ABasicSimpleAssertDeclaration) getASTNode()).getExpression();
		}
		if (getASTNode() instanceof AProtectedSimpleAssertDeclaration) {
			return ((AProtectedSimpleAssertDeclaration) getASTNode()).getExpression();
		}
		return null;
	}

	private RawSourceFragment getAssertFragment() {
		if (getAssertKeyword() != null) {
			return getFragment(getAssertKeyword());
		}
		return null;
	}

	private RawSourceFragment getExpressionFragment() {
		if (getPExpression() != null) {
			return getFragment(getPExpression());
		}
		return null;
	}

	private AFailureClause getFailureClause() {
		if (getASTNode() instanceof AProtectedSimpleAssertDeclaration) {
			return (AFailureClause) ((AProtectedSimpleAssertDeclaration) getASTNode()).getFailureClause();
		}
		return null;
	}

	private RawSourceFragment getOnFailureFragment() {
		if (getFailureClause() != null) {
			return getFragment(getFailureClause().getKwOnfailure());
		}
		return null;
	}

	private RawSourceFragment getColonFragment() {
		if (getFailureClause() != null) {
			return getFragment(getFailureClause().getColon());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof ABasicSimpleAssertDeclaration) {
			return getFragment(((ABasicSimpleAssertDeclaration) getASTNode()).getSemi());
		}
		return null;
	}

	protected RawSourceFragment getLBrcFragment() {
		if (getFailureClause() != null) {
			return getFragment(getLBrc(getFailureClause().getStatement()));
		}
		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		if (getFailureClause() != null) {
			return getFragment(getRBrc(getFailureClause().getStatement()));
		}
		return null;
	}

}
