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

import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.IterationInvariant;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AIterationAssertDeclaration;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *     iteration_assert_declaration = kw_assert l_par type lidentifier colon expression r_par l_brc [inner_assert_declarations]:simple_assert_declaration* r_brc;
 *
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class IterationAssertNode extends AbstractAssertNode<AIterationAssertDeclaration, IterationInvariant> {

	public IterationAssertNode(AIterationAssertDeclaration astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public IterationAssertNode(IterationInvariant iterationInvariant, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(iterationInvariant, analyzer);
	}

	@Override
	public IterationInvariant buildModelObjectFromAST(AIterationAssertDeclaration astNode) {
		IterationInvariant returned = getFactory().newIterationInvariant();

		returned.setIteratorName(getIteratorName(astNode));

		DataBinding<List> constraint = ExpressionFactory.makeDataBinding(astNode.getExpression(), returned, BindingDefinitionType.GET,
				Boolean.class, getSemanticsAnalyzer(), this);
		returned.setIteration(constraint);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("", FMLKeywords.Assert.getKeyword(), SPACE), getAssertFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getIteratorType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getIteratorName()), getIteratorNameFragment());
		append(staticContents(SPACE, ":", SPACE), getColonFragment());
		append(dynamicContents(() -> getModelObject().getIteration().toString()), getIterationFragment());
		append(staticContents(")"), getRParFragment());

		append(staticContents(SPACE, "{", ""), getLBrcFragment());
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getSimpleInvariants(), LINE_SEPARATOR, Indentation.Indent,
				SimpleInvariant.class));
		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	private String getIteratorName(AIterationAssertDeclaration astNode) {
		return astNode.getLidentifier().getText();
	}

	protected RawSourceFragment getAssertFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwAssert());
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

	protected RawSourceFragment getIterationFragment() {
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
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	protected RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}

}
