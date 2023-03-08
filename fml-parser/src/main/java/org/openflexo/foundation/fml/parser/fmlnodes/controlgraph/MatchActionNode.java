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
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.ACreateClause;
import org.openflexo.foundation.fml.parser.node.AFromClause;
import org.openflexo.foundation.fml.parser.node.AInClause;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.AManyQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.AMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.AOneQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.AQualifiedWhereClause;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PCreateClause;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFromClause;
import org.openflexo.foundation.fml.parser.node.PInClause;
import org.openflexo.foundation.fml.parser.node.PQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.PQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.PQualifiedWhereClause;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
import org.openflexo.foundation.fml.rt.editionaction.CreateFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * Represents a {@link MatchFlexoConceptInstance} in FML source code
 * 
 * <pre>
 *    {match_action}           kw_match [concept_name]:identifier in_clause from_clause where_clause create_clause
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class MatchActionNode extends AssignableActionNode<AMatchActionFmlActionExp, MatchFlexoConceptInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MatchActionNode.class.getPackage().getName());

	private FlexoConceptInstanceType conceptType;
	private String constructorName;
	private List<DataBinding<?>> constructorArgs;

	public MatchActionNode(AMatchActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public MatchActionNode(MatchFlexoConceptInstance action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	private void handleArguments(PArgumentList argumentList, MatchFlexoConceptInstance modelObject) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleArguments(l.getArgumentList(), modelObject);
			handleArgument(l.getExpression(), modelObject);
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleArgument(((AOneArgumentList) argumentList).getExpression(), modelObject);
		}
	}

	private void handleArgument(PExpression expression, MatchFlexoConceptInstance modelObject) {
		DataBinding<?> argValue = ExpressionFactory.makeDataBinding(expression, modelObject, BindingDefinitionType.GET, Object.class,
				getSemanticsAnalyzer(), this);

		if (constructorArgs == null) {
			constructorArgs = new ArrayList<>();
		}

		constructorArgs.add(argValue);
	}

	private void handleMatchingCriterias(PQualifiedArgumentList argumentList, MatchFlexoConceptInstance modelObject) {
		if (argumentList instanceof AManyQualifiedArgumentList) {
			AManyQualifiedArgumentList l = (AManyQualifiedArgumentList) argumentList;
			handleMatchingCriterias(l.getQualifiedArgumentList(), modelObject);
			handleMatchingCriteria(l.getQualifiedArgument(), modelObject);
		}
		else if (argumentList instanceof AOneQualifiedArgumentList) {
			handleMatchingCriteria(((AOneQualifiedArgumentList) argumentList).getQualifiedArgument(), modelObject);
		}
	}

	private void handleMatchingCriteria(PQualifiedArgument qualifiedArgument, MatchFlexoConceptInstance modelObject) {

		if (qualifiedArgument instanceof ASimpleQualifiedArgument) {
			String propertyName = ((ASimpleQualifiedArgument) qualifiedArgument).getArgName().getText();
			PExpression expression = ((ASimpleQualifiedArgument) qualifiedArgument).getExpression();
			DataBinding<?> argValue = ExpressionFactory.makeDataBinding(expression, modelObject, BindingDefinitionType.GET, Object.class,
					getSemanticsAnalyzer(), this);

			MatchingCriteria newMatchingCriteria = getFactory().newMatchingCriteria(null);
			newMatchingCriteria._setPatternRoleName(propertyName);
			newMatchingCriteria.setValue(argValue);
			modelObject.addToMatchingCriterias(newMatchingCriteria);
		}
		else {
			logger.warning("Unexpected qualified argument: " + qualifiedArgument);
		}
	}

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (conceptType != null && conceptType.isResolved()) {
			FlexoConcept flexoConceptType = conceptType.getFlexoConcept();
			if (flexoConceptType != null) {
				getModelObject().setFlexoConceptType(flexoConceptType);
				if (flexoConceptType.getCreationSchemes().size() == 0) {
					// No constructor: !! problem
					throwIssue("No constructor for concept " + flexoConceptType, getConceptNameFragment());
				}
				else if (flexoConceptType.getCreationSchemes().size() == 1) {
					getModelObject().setCreationScheme(flexoConceptType.getCreationSchemes().get(0));
				}
				else /* flexoConceptType.getCreationSchemes().size() > 1 */ {
					// TODO
					getModelObject().setCreationScheme((CreationScheme) flexoConceptType.getFlexoBehaviour(constructorName));
				}

				if (getModelObject().getCreationScheme() != null) {
					int index = 0;
					for (FlexoBehaviourParameter flexoBehaviourParameter : getModelObject().getCreationScheme().getParameters()) {
						CreateFlexoConceptInstanceParameter arg = getModelObject().getParameter(flexoBehaviourParameter);
						if (index < constructorArgs.size()) {
							arg.setValue(constructorArgs.get(index));
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
	public MatchFlexoConceptInstance buildModelObjectFromAST(AMatchActionFmlActionExp astNode) {
		MatchFlexoConceptInstance returned = getFactory().newMatchFlexoConceptInstance();

		Type type = TypeFactory.makeType(astNode.getConceptName(), getSemanticsAnalyzer().getTypingSpace());
		if (type instanceof FlexoConceptInstanceType) {
			conceptType = (FlexoConceptInstanceType) type;
			returned.setMatchedType((FlexoConceptInstanceType) type);
		}
		else {
			throwIssue("Unexpected matched type " + getText(astNode.getConceptName()), getConceptNameFragment());
		}

		// conceptType = getTypeFactory().makeFlexoConceptType(astNode.getConceptName().getText(), getFragment(astNode.getConceptName()));
		// returned.setMatchedType(conceptType);

		/*FlexoConcept concept = conceptType.getFlexoConcept();
		if (concept != null) {
			returned.setFlexoConceptType(concept);
		}*/
		if (astNode.getInClause() instanceof AInClause) {
			PExpression inExpression = ((AInClause) astNode.getInClause()).getExpression();
			DataBinding<MatchingSet> matchingSet = (DataBinding) ExpressionFactory.makeDataBinding(inExpression, returned,
					BindingDefinitionType.GET, MatchingSet.class, getSemanticsAnalyzer(), this);
			returned.setMatchingSet(matchingSet);
		}
		if (astNode.getFromClause() instanceof AFromClause) {
			PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
			DataBinding<FlexoConceptInstance> container = (DataBinding) ExpressionFactory.makeDataBinding(fromExpression, returned,
					BindingDefinitionType.GET, FlexoConceptInstance.class, getSemanticsAnalyzer(), this);
			returned.setContainer(container);
		}

		if (astNode.getQualifiedWhereClause() != null) {
			handleMatchingCriterias(((AQualifiedWhereClause) astNode.getQualifiedWhereClause()).getQualifiedArgumentList(), returned);
		}

		if (astNode.getCreateClause() instanceof ACreateClause) {
			constructorName = ((ACreateClause) astNode.getCreateClause()).getConstructorName().getText();
			handleArguments(((ACreateClause) astNode.getCreateClause()).getArgumentList(), returned);
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

		append(staticContents("match"), getMatchFragment());
		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getMatchedType())), getConceptNameFragment());
		when(() -> hasInClause()).thenAppend(staticContents(SPACE, "in", ""), getInFragment())
				//.thenAppend(staticContents(SPACE, "(", ""), getLParInFragment())
				.thenAppend(dynamicContents(SPACE, () -> getInAsString()), getInExpressionFragment());
				//.thenAppend(staticContents(")"), getRParInFragment());
		append(staticContents(SPACE, "from", ""), getFromFragment());
		//append(staticContents(SPACE, "(", ""), getLParFromFragment());
		append(dynamicContents(SPACE, () -> getFromAsString()), getFromExpressionFragment());
		//append(staticContents(")"), getRParFromFragment());
		when(() -> hasWhereClause()).thenAppend(staticContents(SPACE, FMLKeywords.Where.getKeyword(), ""), getWhereFragment())
				.thenAppend(staticContents(SPACE, "(", ""), getLParWhereFragment())
				.thenAppend(dynamicContents(() -> getWhereAsString()), getWhereCriteriasFragment())
				.thenAppend(staticContents(")"), getRParWhereFragment());
		append(staticContents(SPACE, "create", ""), getCreateFragment());
		append(staticContents("::"), getColonColonFragment());
		append(dynamicContents(() -> getModelObject().getCreationScheme() != null ? getModelObject().getCreationScheme().getName() : ""),
				getConstructorNameFragment());
		append(staticContents("("), getCreateLParFragment());
		append(dynamicContents(() -> serializeArguments(getModelObject().getParameters())), getCreateArgumentsFragment());
		append(staticContents(")"), getCreateRParFragment());
		// Append semi only when required
		// final to true is here a little hack to prevent semi to be removed at pretty-print
		// This is due to a wrong management of semi
		// TODO: refactor 'semi' management
		when(() -> requiresSemi(),true).thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	/*@Override
	protected boolean requiresSemi() {
		boolean returned = super.requiresSemi();
		if (!returned) {
			System.out.println("Zut alors pas de ; pour " + getModelObject().getFMLRepresentation());
			System.out.println("Parent: " + getParent().getModelObject());
		}
		return returned;
	}*/

	private boolean hasInClause() {
		if (getModelObject() != null) {
			return getModelObject().getMatchingSet().isSet();
		}
		else {
			return getASTNode() != null && getASTNode().getInClause() != null;
		}
	}

	private boolean hasWhereClause() {
		if (getModelObject() != null) {
			return getModelObject().getMatchingCriterias().size() > 0;
		}
		else {
			return getASTNode() != null && getASTNode().getQualifiedWhereClause() != null;
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

	private String getFromAsString() {
		if (getModelObject() != null) {
			if (getModelObject().getContainer().isSet()) {
				return getModelObject().getContainer().toString();
			}
			return getModelObject().getReceiver().toString();
		}
		return null;
	}

	private String getWhereAsString() {
		if (getModelObject() != null) {
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (MatchingCriteria matchingCriteria : getModelObject().getMatchingCriterias()) {
				if (matchingCriteria.getValue().isSet()) {
					sb.append((isFirst ? "" : ",")
							+ (matchingCriteria.getFlexoProperty() != null ? matchingCriteria.getFlexoProperty().getName() : "?") + "="
							+ matchingCriteria.getValue());
					isFirst = false;
				}
			}
			return sb.toString();
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
	}
	
	private RawSourceFragment getRParFromFragment() {
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

	private RawSourceFragment getInFragment() {
		if (getASTNode() != null) {
			PInClause inClause = getASTNode().getInClause();
			if (inClause instanceof AInClause) {
				return getFragment(((AInClause) inClause).getKwIn());
			}
		}
		return null;
	}

	/*	private RawSourceFragment getLParInFragment() {
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

	private RawSourceFragment getWhereFragment() {
		if (getASTNode() != null) {
			PQualifiedWhereClause whereClause = getASTNode().getQualifiedWhereClause();
			if (whereClause instanceof AQualifiedWhereClause) {
				return getFragment(((AQualifiedWhereClause) whereClause).getKwWhere());
			}
		}
		return null;
	}

	private RawSourceFragment getLParWhereFragment() {
		if (getASTNode() != null) {
			PQualifiedWhereClause whereClause = getASTNode().getQualifiedWhereClause();
			if (whereClause instanceof AQualifiedWhereClause) {
				return getFragment(((AQualifiedWhereClause) whereClause).getLPar());
			}
		}
		return null;
	}

	private RawSourceFragment getRParWhereFragment() {
		if (getASTNode() != null) {
			PQualifiedWhereClause whereClause = getASTNode().getQualifiedWhereClause();
			if (whereClause instanceof AQualifiedWhereClause) {
				return getFragment(((AQualifiedWhereClause) whereClause).getRPar());
			}
		}
		return null;
	}

	private RawSourceFragment getWhereCriteriasFragment() {
		if (getASTNode() != null) {
			PQualifiedWhereClause whereClause = getASTNode().getQualifiedWhereClause();
			if (whereClause instanceof AQualifiedWhereClause) {
				return getFragment(((AQualifiedWhereClause) whereClause).getQualifiedArgumentList());
			}
		}
		return null;
	}

	private RawSourceFragment getCreateFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getKwCreate());
			}
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getColonColon());
			}
		}
		return null;
	}

	private RawSourceFragment getConstructorNameFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getConstructorName());
			}
		}
		return null;
	}

	private RawSourceFragment getCreateLParFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getLPar());
			}
		}
		return null;
	}

	private RawSourceFragment getCreateArgumentsFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getArgumentList());
			}
		}
		return null;
	}

	private RawSourceFragment getCreateRParFragment() {
		if (getASTNode() != null) {
			PCreateClause createClause = getASTNode().getCreateClause();
			if (createClause instanceof ACreateClause) {
				return getFragment(((ACreateClause) createClause).getRPar());
			}
		}
		return null;
	}

	private String serializeArguments(List<CreateFlexoConceptInstanceParameter> arguments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			sb.append((i > 0 ? "," : "") + arguments.get(i).getValue().toString());
		}
		return sb.toString();
	}

}
