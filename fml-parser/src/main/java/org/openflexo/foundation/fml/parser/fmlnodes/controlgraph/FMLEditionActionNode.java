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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.editionaction.UnresolvedTechnologySpecificAction;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFromClause;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.AInClause;
import org.openflexo.foundation.fml.parser.node.ATaEditionActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.foundation.fml.parser.node.PFromClause;
import org.openflexo.foundation.fml.parser.node.PInClause;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * <pre>
 *    [ta_id]:identifier colon_colon [edition_action]:identifier fml_parameters in_clause? from_clause?
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class FMLEditionActionNode<EA extends TechnologySpecificAction<?, ?>>
		extends AssignableActionNode<ATaEditionActionFmlActionExp, EA> {

	private RawSourcePosition startPosition;
	private RawSourcePosition endPosition;

	public FMLEditionActionNode(ATaEditionActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
		startPosition = getRawSource().getStartPosition();
		endPosition = getRawSource().getEndPosition();
	}

	public FMLEditionActionNode(EA editionAction, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(editionAction, analyzer);
	}

	@Override
	public EA buildModelObjectFromAST(ATaEditionActionFmlActionExp astNode) {

		Class<EA> editionActionClass = null;
		editionActionClass = (Class<EA>) getFMLFactory().getEditionActionClass(astNode.getTaId(), astNode.getEditionAction());
		EA returned = getFactory().newInstance(editionActionClass);
		if (editionActionClass.equals(UnresolvedTechnologySpecificAction.class)) {
			UnresolvedTechnologySpecificAction unresolved = (UnresolvedTechnologySpecificAction) returned;
			unresolved.setTAId(astNode.getTaId().getText());
			unresolved.setEditionActionName(astNode.getEditionAction().getText());
		}

		if (astNode.getInClause() != null && returned instanceof TechnologySpecificActionDefiningReceiver) {
			AInClause inClause = (AInClause) astNode.getInClause();
			DataBinding<Object> receiver = ExpressionFactory.makeDataBinding(inClause.getExpression(), returned, BindingDefinitionType.GET,
					Object.class, getSemanticsAnalyzer(), this);
			((TechnologySpecificActionDefiningReceiver) returned).setReceiver(receiver);
		}

		// decodeFMLProperties(astNode.getFmlParameters(), returned);

		/*
		if (astNode instanceof AFmlInnerConceptDecl) {
			returned.setVisibility(getVisibility(((AFmlInnerConceptDecl) astNode).getVisibility()));
			returned.setName(((AFmlInnerConceptDecl) astNode).getIdentifier().getText());
			returned.setCardinality(getCardinality(((AFmlInnerConceptDecl) astNode).getCardinality()));
			CustomType type = (CustomType) getTypeFactory().makeType(((AFmlInnerConceptDecl) astNode).getType(), returned);
			returned.setType(type);
			decodeFMLProperties(((AFmlInnerConceptDecl) astNode).getFmlParameters(), returned);
		}
		if (astNode instanceof AFmlFullyQualifiedInnerConceptDecl) {
			returned.setVisibility(getVisibility(((AFmlFullyQualifiedInnerConceptDecl) astNode).getVisibility()));
			returned.setName(((AFmlFullyQualifiedInnerConceptDecl) astNode).getIdentifier().getText());
			returned.setCardinality(getCardinality(((AFmlFullyQualifiedInnerConceptDecl) astNode).getCardinality()));
			CustomType type = (CustomType) getTypeFactory().makeType(((AFmlFullyQualifiedInnerConceptDecl) astNode).getType(), returned);
			returned.setType(type);
			decodeFMLProperties(((AFmlFullyQualifiedInnerConceptDecl) astNode).getFmlParameters(), returned);
		}*/
		return returned;
	}

	/**
	 * <pre>
	 *    [ta_id]:identifier colon_colon [edition_action]:identifier fml_parameters in_clause? from_clause?
	 * </pre>
	 * 
	 * @author sylvain
	 * 
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		append(dynamicContents(() -> getFMLFactory().serializeTAId(getModelObject())), getTaIdFragment());
		append(staticContents("::"), getColonColonFragment());
		append(dynamicContents(() -> serializeEditionActionName(getModelObject())), getEditionActionFragment());
		append(staticContents("("), getFMLParametersLParFragment());
		when(() -> hasFMLProperties())
		.thenAppend(childrenContents("","", () -> getModelObject().getFMLPropertyValues(getFactory()), ",","", Indentation.DoNotIndent,
				FMLPropertyValue.class));
		append(staticContents(")"), getFMLParametersRParFragment());

		when(() -> hasInClause())
		.thenAppend(staticContents(SPACE, "in",""), getInFragment())
		//.thenAppend(staticContents(SPACE, "(",""), getLParInFragment())
		.thenAppend(dynamicContents(SPACE, () -> getInAsString()), getInExpressionFragment());
		//.thenAppend(staticContents(")"), getRParInFragment());

		when(() -> hasFromClause())
		.thenAppend(staticContents(SPACE, "from",""), getFromFragment())
		//.thenAppend(staticContents(SPACE, "(",""), getLParFromFragment())
		.thenAppend(dynamicContents(SPACE, () -> getFromAsString()), getFromExpressionFragment());
		//.thenAppend(staticContents(")"), getRParFromFragment());

		// Append semi only when required
		// final to true is here a little hack to prevent semi to be removed at pretty-print
		// This is due to a wrong management of semi
		// TODO: refactor 'semi' management
		when(() -> requiresSemi(),true).thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on	
	}

	protected String serializeEditionActionName(EA editionAction) {
		if (editionAction instanceof UnresolvedTechnologySpecificAction) {
			return ((UnresolvedTechnologySpecificAction) editionAction).getEditionActionName();
		}
		return editionAction.getFMLKeyword(getFactory());
	}

	protected boolean hasFMLProperties() {
		if (getFMLParameters() != null) {
			return true;
		}
		if (getModelObject() != null) {
			return getModelObject().hasFMLProperties(getFactory());
		}
		return false;
	}

	private boolean hasInClause() {
		if (getModelObject() instanceof TechnologySpecificActionDefiningReceiver) {
			return ((TechnologySpecificActionDefiningReceiver) getModelObject()).getReceiver().isSet();
		}
		else {
			return getASTNode() != null && getASTNode().getInClause() != null;
		}
	}

	private String getInAsString() {
		if (getModelObject() instanceof TechnologySpecificActionDefiningReceiver) {
			return ((TechnologySpecificActionDefiningReceiver) getModelObject()).getReceiver().toString();
		}
		return null;
	}

	private boolean hasFromClause() {
		return false;
	}

	private String getFromAsString() {
		return null;
	}

	protected RawSourceFragment getTaIdFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getTaId());
		}
		return null;
	}

	protected RawSourceFragment getColonColonFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getColonColon());
		}
		return null;
	}

	protected RawSourceFragment getEditionActionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getEditionAction());
		}
		return null;
	}

	protected PFmlParameters getFMLParameters() {
		if (getASTNode() != null) {
			return getASTNode().getFmlParameters();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersFragment() {
		if (getFMLParameters() != null) {
			return getFragment(getFMLParameters());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersLParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersRParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getRPar());
		}
		return null;
	}

	private RawSourceFragment getInFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getKwIn());
			}
		}
		return null;
	}

	/*private RawSourceFragment getLParInFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getLPar());
			}
		}
		return null;
	}
	
	private RawSourceFragment getRParInFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getRPar());
			}
		}
		return null;
	}*/

	private RawSourceFragment getInExpressionFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getExpression());
			}
		}
		return null;
	}

	private RawSourceFragment getFromFragment() {
		if (getASTNode() != null) {
			PFromClause inClause = getASTNode().getFromClause();
			if (inClause instanceof AFromClause) {
				return getFragment(((AFromClause) inClause).getKwFrom());
			}
		}
		return null;
	}

	/*private RawSourceFragment getLParFromFragment() {
		if (getASTNode() != null) {
			PFromClause inClause = getASTNode().getFromClause();
			if (inClause instanceof AFromClause) {
				return getFragment(((AFromClause) inClause).getLPar());
			}
		}
		return null;
	}
	
	private RawSourceFragment getRParFromFragment() {
		if (getASTNode() != null) {
			PFromClause inClause = getASTNode().getFromClause();
			if (inClause instanceof AFromClause) {
				return getFragment(((AFromClause) inClause).getRPar());
			}
		}
		return null;
	}*/

	private RawSourceFragment getFromExpressionFragment() {
		if (getASTNode() != null) {
			PFromClause inClause = getASTNode().getFromClause();
			if (inClause instanceof AFromClause) {
				return getFragment(((AFromClause) inClause).getExpression());
			}
		}
		return null;
	}

}
