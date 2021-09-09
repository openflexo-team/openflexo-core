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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AActionClause;
import org.openflexo.foundation.fml.parser.node.ADeleteAbstractActionClause;
import org.openflexo.foundation.fml.parser.node.ADeleteClause;
import org.openflexo.foundation.fml.parser.node.AEndMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AInClause;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.ANormalAbstractActionClause;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.PAbstractActionClause;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PInClause;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteBehaviourParameter;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * Represents a {@link FinalizeMatching} in FML source code
 * 
 * <pre>
 *    {end_match_action}       kw_end kw_match [concept_name]:identifier in_clause abstract_action_clause
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class EndMatchActionNode extends ControlGraphNode<AEndMatchActionFmlActionExp, FinalizeMatching> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EndMatchActionNode.class.getPackage().getName());

	private FlexoConceptInstanceType conceptType;
	private String behaviourName;
	private List<DataBinding<?>> behaviourArgs;

	public EndMatchActionNode(AEndMatchActionFmlActionExp astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public EndMatchActionNode(FinalizeMatching action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	private void handleArguments(PArgumentList argumentList, FinalizeMatching modelObject) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleArguments(l.getArgumentList(), modelObject);
			handleArgument(l.getExpression(), modelObject);
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleArgument(((AOneArgumentList) argumentList).getExpression(), modelObject);
		}
	}

	private void handleArgument(PExpression expression, FinalizeMatching modelObject) {
		DataBinding<?> argValue = ExpressionFactory.makeDataBinding(expression, modelObject, BindingDefinitionType.GET, Object.class,
				getAnalyser(), this);

		if (behaviourArgs == null) {
			behaviourArgs = new ArrayList<>();
		}

		behaviourArgs.add(argValue);
	}

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (conceptType != null && conceptType.isResolved()) {
			FlexoConcept flexoConceptType = conceptType.getFlexoConcept();
			if (flexoConceptType != null) {
				getModelObject().setFlexoConceptType(flexoConceptType);
				getModelObject().setFlexoBehaviour(flexoConceptType.getFlexoBehaviour(behaviourName));
				if (getModelObject().getFlexoBehaviour() != null) {
					int index = 0;
					for (FlexoBehaviourParameter flexoBehaviourParameter : getModelObject().getFlexoBehaviour().getParameters()) {
						ExecuteBehaviourParameter arg = getModelObject().getParameter(flexoBehaviourParameter);
						if (index < behaviourArgs.size()) {
							arg.setValue(behaviourArgs.get(index));
							index++;
						}
						else {
							throwIssue("Missing argument value for parameter " + flexoBehaviourParameter, getConceptNameFragment());
							break;
						}
					}
				}
			}
			else {
				throwIssue("Unknown concept " + getConceptName(), getConceptNameFragment());
			}

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FinalizeMatching buildModelObjectFromAST(AEndMatchActionFmlActionExp astNode) {
		FinalizeMatching returned = getFactory().newFinalizeMatching();

		Type type = TypeFactory.makeType(astNode.getConceptName(), getAnalyser().getTypingSpace());
		if (type instanceof FlexoConceptInstanceType) {
			conceptType = (FlexoConceptInstanceType) type;
			returned.setMatchedType((FlexoConceptInstanceType) type);
		}
		else {
			throwIssue("Unexpected matched type " + getText(astNode.getConceptName()), getConceptNameFragment());
		}

		// conceptType = getTypeFactory().makeFlexoConceptType(astNode.getConceptName().getText(), getFragment(astNode.getConceptName()));
		// returned.setMatchedType(conceptType);

		if (astNode.getInClause() instanceof AInClause) {
			PExpression inExpression = ((AInClause) astNode.getInClause()).getExpression();
			DataBinding<MatchingSet> matchingSet = (DataBinding) ExpressionFactory.makeDataBinding(inExpression, returned,
					BindingDefinitionType.GET, MatchingSet.class, getAnalyser(), this);

			returned.setMatchingSet(matchingSet);
		}
		if (astNode.getAbstractActionClause() instanceof ANormalAbstractActionClause) {
			AActionClause actionClause = (AActionClause) ((ANormalAbstractActionClause) astNode.getAbstractActionClause())
					.getActionClause();
			behaviourName = actionClause.getActionName().getText();
			handleArguments(actionClause.getArgumentList(), returned);
		}
		if (astNode.getAbstractActionClause() instanceof ADeleteAbstractActionClause) {
			ADeleteClause actionClause = (ADeleteClause) ((ADeleteAbstractActionClause) astNode.getAbstractActionClause())
					.getDeleteClause();
			behaviourName = actionClause.getDestructorName().getText();
			handleArguments(actionClause.getArgumentList(), returned);
		}
		return returned;

	}

	/**
	 * <pre>
	 *    {match_action} kw_match [concept_name]:identifier in_clause from_clause where_clause create_clause
	 * </pre>
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("end"), getEndFragment());
		append(staticContents(SPACE, "match", ""), getMatchFragment());
		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getMatchedType())), getConceptNameFragment());
		append(staticContents(SPACE, "in", ""), getInFragment());
		append(staticContents(SPACE, "(", ""), getLParInFragment());
		append(dynamicContents(() -> getInAsString()), getInExpressionFragment());
		append(staticContents(")"), getRParInFragment());

		when(() -> isNormalAction()).thenAppend(staticContents(SPACE, "action", ""), getActionFragment())
				.thenAppend(staticContents("::"), getColonColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getFlexoBehaviour().getName()), getBehaviourNameFragment())
				.thenAppend(staticContents("("), getAbstractActionLParFragment())
				.thenAppend(dynamicContents(() -> serializeArguments(getModelObject().getParameters())),
						getAbstractActionArgumentsFragment())
				.thenAppend(staticContents(")"), getAbstractActionRParFragment());

		when(() -> isDeleteAction()).thenAppend(staticContents(SPACE, "delete", ""), getDeleteFragment())
				.thenAppend(staticContents("::"), getColonColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getFlexoBehaviour().getName()), getBehaviourNameFragment())
				.thenAppend(staticContents("("), getAbstractActionLParFragment())
				.thenAppend(dynamicContents(() -> serializeArguments(getModelObject().getParameters())),
						getAbstractActionArgumentsFragment())
				.thenAppend(staticContents(")"), getAbstractActionRParFragment());

		append(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	private boolean isNormalAction() {
		if (getModelObject() != null) {
			return getModelObject().getFlexoBehaviour() instanceof ActionScheme;
		}
		else {
			return getASTNode() != null && getASTNode().getAbstractActionClause() instanceof ANormalAbstractActionClause;
		}
	}

	private boolean isDeleteAction() {
		if (getModelObject() != null) {
			return getModelObject().getFlexoBehaviour() instanceof DeletionScheme;
		}
		else {
			return getASTNode() != null && getASTNode().getAbstractActionClause() instanceof ADeleteAbstractActionClause;
		}
	}

	private String getConceptName() {
		if (getASTNode() != null) {
			return getASTNode().getConceptName().getText();
		}
		return null;
	}

	private String getInAsString() {
		if (getModelObject() != null) {
			return getModelObject().getMatchingSet().toString();
		}
		return null;
	}

	private RawSourceFragment getEndFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwEnd());
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

	private RawSourceFragment getInFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getKwIn());
			}
		}
		return null;
	}

	private RawSourceFragment getLParInFragment() {
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
	}

	private RawSourceFragment getInExpressionFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getExpression());
			}
		}
		return null;
	}

	private AActionClause getActionClause() {
		if (getASTNode() != null) {
			PAbstractActionClause abstractActionClause = getASTNode().getAbstractActionClause();
			if (abstractActionClause instanceof ANormalAbstractActionClause) {
				return (AActionClause) ((ANormalAbstractActionClause) abstractActionClause).getActionClause();
			}
		}
		return null;
	}

	private ADeleteClause getDeleteClause() {
		if (getASTNode() != null) {
			PAbstractActionClause abstractActionClause = getASTNode().getAbstractActionClause();
			if (abstractActionClause instanceof ADeleteAbstractActionClause) {
				return (ADeleteClause) ((ADeleteAbstractActionClause) abstractActionClause).getDeleteClause();
			}
		}
		return null;
	}

	private RawSourceFragment getActionFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getKwAction());
		}
		return null;
	}

	private RawSourceFragment getDeleteFragment() {
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getKwDelete());
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getColonColon());
		}
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getColonColon());
		}
		return null;
	}

	private RawSourceFragment getBehaviourNameFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getActionName());
		}
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getDestructorName());
		}
		return null;
	}

	private RawSourceFragment getAbstractActionLParFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getLPar());
		}
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getLPar());
		}
		return null;
	}

	private RawSourceFragment getAbstractActionArgumentsFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getArgumentList());
		}
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getArgumentList());
		}
		return null;
	}

	private RawSourceFragment getAbstractActionRParFragment() {
		if (getActionClause() != null) {
			return getFragment(getActionClause().getRPar());
		}
		if (getDeleteClause() != null) {
			return getFragment(getDeleteClause().getRPar());
		}
		return null;
	}

	private String serializeArguments(List<ExecuteBehaviourParameter> arguments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			sb.append((i > 0 ? "," : "") + arguments.get(i).getValue().toString());
		}
		return sb.toString();
	}

}
