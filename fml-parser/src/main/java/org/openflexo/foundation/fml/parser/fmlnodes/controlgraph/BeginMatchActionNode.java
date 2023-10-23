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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.ABeginMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AFromClause;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFromClause;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * Represents a {@link InitiateMatching} in FML source code
 * 
 * <pre>
 *    {begin_match_action}     kw_begin kw_match [concept_name]:identifier from_clause where_clause?
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class BeginMatchActionNode extends AssignableActionNode<ABeginMatchActionFmlActionExp, InitiateMatching> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BeginMatchActionNode.class.getPackage().getName());

	public BeginMatchActionNode(ABeginMatchActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public BeginMatchActionNode(InitiateMatching action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public InitiateMatching buildModelObjectFromAST(ABeginMatchActionFmlActionExp astNode) {
		InitiateMatching returned = getFactory().newInitiateMatching();

		// System.out.println("---------> On cherche: " + astNode.getConceptName().getText());

		// FlexoConceptInstanceType matchedType = getTypeFactory().makeFlexoConceptType(astNode.getConceptName().getText(),
		// getFragment(astNode.getConceptName()));

		Type type = TypeFactory.makeType(astNode.getConceptName(), getSemanticsAnalyzer().getTypingSpace());
		if (type instanceof FlexoConceptInstanceType) {
			returned.setMatchedType((FlexoConceptInstanceType) type);
		}
		else {
			throwIssue("Unexpected matched type " + getText(astNode.getConceptName()), getConceptNameFragment());
		}

		// returned.setMatchedType(matchedType);

		if (astNode.getFromClause() instanceof AFromClause) {
			PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
			DataBinding<FlexoConceptInstance> container = (DataBinding) ExpressionFactory.makeDataBinding(fromExpression, returned,
					BindingDefinitionType.GET, FlexoConceptInstance.class, getSemanticsAnalyzer(), this);
			returned.setContainer(container);
		}

		return returned;

	}

	/*
	 * <pre>
	 * kw_select kw_unique? [selected_type_name]:composite_ident from_clause where_clause?
	 * </pre>
	 */

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("begin"), getBeginFragment());
		append(staticContents(SPACE, "match", ""), getMatchFragment());
		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getMatchedType())), getConceptNameFragment());
		append(staticContents(SPACE, "from", ""), getFromFragment());
		//append(staticContents(SPACE, "(", ""), getLParFromFragment());
		append(dynamicContents(SPACE, () -> getFromAsString()), getFromExpressionFragment());
		//append(staticContents(")"), getRParFromFragment());
		when(() -> requiresSemi()).thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	private String getFromAsString() {
		if (getModelObject() != null) {
			return getModelObject().getContainer().toString();
		}
		return null;
	}

	private RawSourceFragment getBeginFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwBegin());
		}
		return null;
	}

	private RawSourceFragment getMatchFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwMatch());
		}
		return null;
	}

	private RawSourceFragment getConceptNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getConceptName());
		}
		return null;
	}

	private RawSourceFragment getFromFragment() {
		if (getASTNode() != null) {
			PFromClause fromClause = getASTNode().getFromClause();
			if (fromClause instanceof AFromClause) {
				return getFragment(((AFromClause) fromClause).getKwFrom());
			}
		}
		return null;
	}

	/*private RawSourceFragment getLParFromFragment() {
		if (getASTNode() != null) {
			PFromClause fromClause = getASTNode().getFromClause();
			if (fromClause instanceof AFromClause) {
				return getFragment(((AFromClause) fromClause).getLPar());
			}
		}
		return null;
	}*/

	/*private RawSourceFragment getRParFromFragment() {
		if (getASTNode() != null) {
			PFromClause fromClause = getASTNode().getFromClause();
			if (fromClause instanceof AFromClause) {
				return getFragment(((AFromClause) fromClause).getRPar());
			}
		}
		return null;
	}*/

	private RawSourceFragment getFromExpressionFragment() {
		if (getASTNode() != null) {
			PFromClause fromClause = getASTNode().getFromClause();
			if (fromClause instanceof AFromClause) {
				return getFragment(((AFromClause) fromClause).getExpression());
			}
		}
		return null;
	}

}
