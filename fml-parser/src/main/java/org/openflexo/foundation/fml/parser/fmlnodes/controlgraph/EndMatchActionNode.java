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

import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourArgument;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AComplexEndMatchActionClause;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.ANormalCompositeIdent;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.PMethodInvocation;
import org.openflexo.foundation.fml.parser.node.AEndMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AEndMatchUnmatchedClause;
import org.openflexo.foundation.fml.parser.node.AInClause;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.ASimpleEndMatchActionClause;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PEndMatchActionClause;
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
	// The 'unmatched:' clause of an end-match action, parsed in buildModelObjectFromAST and resolved
	// to a concrete FlexoBehaviour in finalizeDeserialization:
	// - 'unmatched: delete(args?)' -> deleteClause == true, resolved to the concept default DeletionScheme
	// - 'unmatched: someBehaviour(args)' -> behaviourName set, resolved to the matching behaviour
	private boolean deleteClause;
	private String behaviourName;
	private List<DataBinding<?>> behaviourArgs;

	public EndMatchActionNode(AEndMatchActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public EndMatchActionNode(FinalizeMatching action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
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
				getSemanticsAnalyzer(), this);

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
				// Resolve the behaviour of the 'unmatched:' clause. 'delete(...)' maps to the concept
				// default DeletionScheme; a named method maps to the matching behaviour. When nothing was
				// parsed (empty/unsupported clause) the behaviour is left null so that FinalizeMatching
				// generates no parameter (avoids the former spurious "binding required" validation error).
				if (deleteClause) {
					getModelObject().setFlexoBehaviour(flexoConceptType.getDefaultDeletionScheme());
				}
				else if (behaviourName != null) {
					FlexoBehaviour behaviour = flexoConceptType.getFlexoBehaviour(behaviourName);
					if (behaviour != null) {
						getModelObject().setFlexoBehaviour(behaviour);
					}
					else {
						throwIssue("No behaviour " + behaviourName + " in " + flexoConceptType.getName(), getConceptNameFragment());
					}
				}
				if (getModelObject().getFlexoBehaviour() != null && behaviourArgs != null) {
					int index = 0;
					for (FlexoBehaviourArgument flexoBehaviourParameter : getModelObject().getFlexoBehaviour().getParameters()) {
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

		Type type = TypeFactory.makeType(astNode.getConceptName(), getSemanticsAnalyzer().getTypingSpace());
		conceptType = TypeFactory.asFlexoConceptInstanceType(type, getSemanticsAnalyzer().getTypingSpace());
		if (conceptType != null) {
			returned.setMatchedType(conceptType);
		}
		if (conceptType == null || type instanceof UnresolvedType) {
			throwIssue("Unexpected matched type " + getText(astNode.getConceptName()), getConceptNameFragment());
		}

		// conceptType = getTypeFactory().makeFlexoConceptType(astNode.getConceptName().getText(), getFragment(astNode.getConceptName()));
		// returned.setMatchedType(conceptType);

		if (astNode.getInClause() instanceof AInClause) {
			PExpression inExpression = ((AInClause) astNode.getInClause()).getExpression();
			DataBinding<MatchingSet> matchingSet = (DataBinding) ExpressionFactory.makeDataBinding(inExpression, returned,
					BindingDefinitionType.GET, MatchingSet.class, getSemanticsAnalyzer(), this);

			returned.setMatchingSet(matchingSet);
		}

		PEndMatchActionClause clause = getEndMatchActionClause(astNode);
		if (clause instanceof ASimpleEndMatchActionClause) {
			// 'unmatched: delete(args?)' -> resolved to the concept default DeletionScheme in finalizeDeserialization
			deleteClause = true;
			handleArguments(((ASimpleEndMatchActionClause) clause).getArgumentList(), returned);
		}
		else if (clause instanceof AComplexEndMatchActionClause) {
			// 'unmatched: someBehaviour(args)': a behaviour of the matched concept, by its name, resolved in finalizeDeserialization
			// (CORE-D-2). Anything else ('a.b()', 'super()', 'T.m()') is not a behaviour of the concept: no behaviour runs.
			PMethodInvocation invocation = ((AComplexEndMatchActionClause) clause).getMethodInvocation();
			if (invocation instanceof APrimaryMethodInvocation && ((APrimaryMethodInvocation) invocation).getPrimary() instanceof AIdentifierPrimary
					&& ((AIdentifierPrimary) ((APrimaryMethodInvocation) invocation).getPrimary()).getCompositeIdent() instanceof ANormalCompositeIdent) {
				ANormalCompositeIdent ident = (ANormalCompositeIdent) ((AIdentifierPrimary) ((APrimaryMethodInvocation) invocation).getPrimary())
						.getCompositeIdent();
				if (ident.getPrefixes().isEmpty()) {
					behaviourName = ident.getIdentifier().getText();
					handleArguments(((APrimaryMethodInvocation) invocation).getArgumentList(), returned);
				}
			}
			if (behaviourName == null) {
				throwIssue("'unmatched:' expects delete(...) or a behaviour of " + getText(astNode.getConceptName()) + " by its name: "
						+ getText(clause), getFragment(clause));
			}
		}
		return returned;

	}

	/**
	 * <pre>
	 *    {match_action} kw_match [concept_name]:identifier in_clause from_clause where_clause create_clause
	 * </pre>
	 */
	@Override
	public void definePrettyPrint(boolean hasParsedVersion) {
		super.definePrettyPrint(hasParsedVersion);

		// @formatter:off

		append(staticContents("end"), getEndFragment());
		append(staticContents(SPACE, "match", ""), getMatchFragment());
		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getMatchedType())).keepingParsedTextWhileUnchanged(), getConceptNameFragment());
		append(staticContents(SPACE, "in", ""), getInFragment());
		//append(staticContents(SPACE, "(", ""), getLParInFragment());
		append(bindingContents(SPACE, () -> getInBinding()), getInExpressionFragment());
		//append(staticContents(")"), getRParInFragment());
		// 'unmatched: <clause>' is optional: without it, the instances left unmatched are left as they are (CORE-D-57)
		when(() -> hasUnmatchedClause()).thenAppend(staticContents(SPACE, FMLKeywords.Unmatched.getKeyword(), ""), getUnmatchedFragment())
		.thenAppend(staticContents(":"), getColonFragment())
		.thenAppend(textContents(SPACE, preserving -> getUnmatchedClauseAsString(preserving)), getUnmatchedClauseFragment());

		/*when(() -> isNormalAction()).thenAppend(staticContents(SPACE, "action", ""), getActionFragment())
				.thenAppend(staticContents("::"), getColonColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getFlexoBehaviour().getName()), getBehaviourNameFragment())
				.thenAppend(staticContents("("), getAbstractActionLParFragment())
				.thenAppend(textContents(preserving -> serializeArguments(getModelObject().getParameters(), preserving)).keepingParsedTextWhileUnchanged(),
						getAbstractActionArgumentsFragment())
				.thenAppend(staticContents(")"), getAbstractActionRParFragment());

		when(() -> isDeleteAction()).thenAppend(staticContents(SPACE, "delete", ""), getDeleteFragment())
				.thenAppend(staticContents("::"), getColonColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getFlexoBehaviour().getName()), getBehaviourNameFragment())
				.thenAppend(staticContents("("), getAbstractActionLParFragment())
				.thenAppend(textContents(preserving -> serializeArguments(getModelObject().getParameters(), preserving)).keepingParsedTextWhileUnchanged(),
						getAbstractActionArgumentsFragment())
				.thenAppend(staticContents(")"), getAbstractActionRParFragment());*/

		append(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	/*private boolean isNormalAction() {
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
	}*/

	/** The 'unmatched: ...' clause of the AST, null when there is none */
	private static AEndMatchUnmatchedClause getUnmatchedClause(AEndMatchActionFmlActionExp astNode) {
		return astNode != null && astNode.getEndMatchUnmatchedClause() instanceof AEndMatchUnmatchedClause
				? (AEndMatchUnmatchedClause) astNode.getEndMatchUnmatchedClause()
				: null;
	}

	private static PEndMatchActionClause getEndMatchActionClause(AEndMatchActionFmlActionExp astNode) {
		AEndMatchUnmatchedClause unmatched = getUnmatchedClause(astNode);
		return unmatched != null ? unmatched.getEndMatchActionClause() : null;
	}

	/**
	 * A behaviour runs on the instances left unmatched, or the parsed text has a clause. The text is looked at too because the behaviour
	 * is resolved after the pretty-print is initialized (finalizeDeserialization), and because a clause the parser does not support yet
	 * ('unmatched: someBehaviour(...)') leaves no behaviour.
	 */
	private boolean hasUnmatchedClause() {
		return getExplicitBehaviour() != null || getUnmatchedClause(getASTNode()) != null;
	}

	/**
	 * The behaviour set on the action, not the one {@link FinalizeMatching#getFlexoBehaviour()} falls back to when none is (the first
	 * action scheme of the concept, CORE-D-2): the text must say what the model holds
	 */
	private FlexoBehaviour getExplicitBehaviour() {
		return getModelObject() != null && getModelObject().getBehaviourReference() != null ? getModelObject().getFlexoBehaviour() : null;
	}

	/**
	 * The clause after 'unmatched:': 'delete(args)' for a deletion scheme, 'name(args)' for another behaviour, or the parsed text when no
	 * behaviour was resolved from it
	 */
	private String getUnmatchedClauseAsString(boolean preserving) {
		FlexoBehaviour behaviour = getExplicitBehaviour();
		// 'delete(...)' is the default deletion scheme of the concept; another one is called by its name
		if (behaviour instanceof DeletionScheme && getModelObject().getFlexoConceptType() != null
				&& behaviour == getModelObject().getFlexoConceptType().getDefaultDeletionScheme()) {
			return "delete(" + serializeArguments(getModelObject().getParameters(), preserving) + ")";
		}
		if (behaviour != null) {
			return behaviour.getName() + "(" + serializeArguments(getModelObject().getParameters(), preserving) + ")";
		}
		PEndMatchActionClause clause = getEndMatchActionClause(getASTNode());
		return clause != null ? getText(clause) : null;
	}

	private String getConceptName() {
		if (getASTNode() != null) {
			return getASTNode().getConceptName().getText();
		}
		return null;
	}

	private DataBinding<?> getInBinding() {
		if (getModelObject() != null) {
			return getModelObject().getMatchingSet();
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

	private RawSourceFragment getUnmatchedFragment() {
		AEndMatchUnmatchedClause unmatched = getUnmatchedClause(getASTNode());
		return unmatched != null ? getFragment(unmatched.getKwUnmatched()) : null;
	}

	private RawSourceFragment getUnmatchedClauseFragment() {
		PEndMatchActionClause clause = getEndMatchActionClause(getASTNode());
		return clause != null ? getFragment(clause) : null;
	}

	private RawSourceFragment getColonFragment() {
		AEndMatchUnmatchedClause unmatched = getUnmatchedClause(getASTNode());
		return unmatched != null ? getFragment(unmatched.getColon()) : null;
	}

	/*private AActionClause getActionClause() {
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
	}*/

	/*private RawSourceFragment getActionFragment() {
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
	}*/

	/*private RawSourceFragment getColonColonFragment() {
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
	}*/

	private String serializeArguments(List<ExecuteBehaviourParameter> arguments, boolean preserving) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			DataBinding<?> value = arguments.get(i).getValue();
			sb.append((i > 0 ? "," : "") + (value != null ? print(value, preserving) : "null"));
		}
		return sb.toString();
	}

}
