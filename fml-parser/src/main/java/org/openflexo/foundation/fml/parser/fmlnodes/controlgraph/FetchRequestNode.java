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
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.UniqueFetchRequest;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AFmlSelectType;
import org.openflexo.foundation.fml.parser.node.AFromClause;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.ANormalSelectType;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.ASelectActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AWhereClause;
import org.openflexo.foundation.fml.parser.node.AWithClause;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFromClause;
import org.openflexo.foundation.fml.parser.node.PSelectType;
import org.openflexo.foundation.fml.parser.node.PWhereClause;
import org.openflexo.foundation.fml.parser.node.PWithClause;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AbstractSelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AbstractSelectVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueVirtualModelInstance;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class FetchRequestNode<FR extends AbstractFetchRequest<?, ?, ?, ?>> extends AssignableActionNode<ASelectActionFmlActionExp, FR> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FetchRequestNode.class.getPackage().getName());

	public FetchRequestNode(ASelectActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FetchRequestNode(FR action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@Override
	public FR buildModelObjectFromAST(ASelectActionFmlActionExp astNode) {
		FR returned = null;

		Type type = null;
		PSelectType selectedType = astNode.getSelectedType();
		if (selectedType instanceof ANormalSelectType) {
			type = TypeFactory.makeType(((ANormalSelectType) selectedType).getReferenceType(), getSemanticsAnalyzer().getTypingSpace());
		}
		else if (selectedType instanceof AFmlSelectType) {
			type = TypeFactory.makeType((((AFmlSelectType) selectedType).getTechnologySpecificType()),
					getSemanticsAnalyzer().getTypingSpace());
		}

		if (type instanceof VirtualModelInstanceType) {
			AbstractSelectVirtualModelInstance selectAction = null;
			if (astNode.getKwUnique() != null) {
				selectAction = getFactory().newSelectUniqueVirtualModelInstance();
			}
			else {
				selectAction = getFactory().newSelectVirtualModelInstance();
			}
			/*VirtualModel vm = ((VirtualModelInstanceType) type).getVirtualModel();
			if (vm != null && selectAction != null) {
				selectAction.setVirtualModelType(vm);
			}*/
			selectAction.setType((VirtualModelInstanceType) type);
			if (astNode.getFromClause() instanceof AFromClause) {
				PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
				DataBinding<FlexoConceptInstance> container = (DataBinding) ExpressionFactory.makeDataBinding(fromExpression, selectAction,
						BindingDefinitionType.GET, FlexoConceptInstance.class, getSemanticsAnalyzer(), this);
				selectAction.setContainer(container);
				// selectAction.setReceiver(container);
			}
			returned = (FR) selectAction;
		}
		else if (type instanceof FlexoConceptInstanceType) {
			AbstractSelectFlexoConceptInstance selectAction = null;
			if (astNode.getKwUnique() != null) {
				selectAction = getFactory().newSelectUniqueFlexoConceptInstance();
			}
			else {
				selectAction = getFactory().newSelectFlexoConceptInstance();
			}
			/*FlexoConcept concept = type.getFlexoConcept();
			if (concept != null && selectAction != null) {
				selectAction.setFlexoConceptType(concept);
			}*/
			selectAction.setType((FlexoConceptInstanceType) type);
			if (astNode.getFromClause() instanceof AFromClause) {
				PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
				DataBinding<FlexoConceptInstance> container = (DataBinding) ExpressionFactory.makeDataBinding(fromExpression, selectAction,
						BindingDefinitionType.GET, FlexoConceptInstance.class, getSemanticsAnalyzer(), this);
				selectAction.setContainer(container);
				// selectAction.setReceiver(container);

			}
			returned = (FR) selectAction;

		}
		else {
			AbstractFetchRequest<?, ?, ?, ?> selectAction = null;

			Class<? extends AbstractFetchRequest<?, ?, ?, ?>> frClass = getFetchRequestClass(type, astNode.getKwUnique() != null,
					astNode.getWithClause());

			if (frClass != null) {
				selectAction = getFactory().newInstance(frClass);
				//System.out.println("For " + selectAction + " setFetchedType with " + type);
				selectAction.setFetchedType(type);
				if (astNode.getFromClause() instanceof AFromClause) {
					PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
					Type resourceDataType = TypeUtils.getTypeArgument(frClass, AbstractFetchRequest.class, 1);
					DataBinding receiver = ExpressionFactory.makeDataBinding(fromExpression, selectAction, BindingDefinitionType.GET,
							resourceDataType, getSemanticsAnalyzer(), this);
					selectAction.setReceiver(receiver);

				}
				returned = (FR) selectAction;
			}

			if (selectAction == null) {
				throwIssue("Unexpected fetch request for type " + getText(astNode.getSelectedType()), getTypeFragment());
			}
		}

		if (astNode.getWhereClause() != null) {
			handleConditions(((AWhereClause) astNode.getWhereClause()).getArgumentList(), returned);
		}

		return returned;

	}

	private Class<? extends AbstractFetchRequest<?, ?, ?, ?>> getFetchRequestClass(Type type, boolean isUnique, PWithClause withClause) {
		String identifier = null;
		if (withClause instanceof AWithClause) {
			identifier = ((AWithClause) withClause).getUidentifier().getText();
		}

		Class<? extends AbstractFetchRequest<?, ?, ?, ?>> bestMatch = null;
		for (UseModelSlotDeclaration useDecl : getCompilationUnit().getUseDeclarations()) {
			// We iterate on all use declarations
			for (Class<? extends AbstractFetchRequest<?, ?, ?, ?>> frClass : getSemanticsAnalyzer().getServiceManager()
					.getTechnologyAdapterService().getAvailableAbstractFetchRequestActionTypes(useDecl.getModelSlotClass())) {
				if (AbstractFetchRequest.class.isAssignableFrom(frClass)) {
					if ((isUnique && UniqueFetchRequest.class.isAssignableFrom(frClass))
							|| ((!isUnique) && FetchRequest.class.isAssignableFrom(frClass))) {
						Type accessedType = TypeUtils.getTypeArgument(frClass, AbstractFetchRequest.class, 2);
						if (TypeUtils.isTypeAssignableFrom(accessedType, type)) {
							// System.out.println("Looked up " + frClass);
							if (identifier == null || identifier.equals(frClass.getSimpleName())) {
								return frClass;
							}
							else {
								// Type is right but identifier does not match
								bestMatch = frClass;
							}
						}
					}
				}
			}
		}
		if (identifier != null) {
			throwIssue("Unexpected fetch request " + identifier, getWithFragment());
			// Return best match
			return bestMatch;
		}
		return null;
	}

	private void handleConditions(PArgumentList argumentList, FR modelObject) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleConditions(l.getArgumentList(), modelObject);
			handleCondition(l.getExpression(), modelObject);
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleCondition(((AOneArgumentList) argumentList).getExpression(), modelObject);
		}
	}

	private void handleCondition(PExpression expression, FR modelObject) {

		DataBinding<Boolean> argValue = ExpressionFactory.makeDataBinding(expression, modelObject, BindingDefinitionType.GET, Boolean.class,
				getSemanticsAnalyzer(), this);

		FetchRequestCondition newCondition = getFactory().newFetchRequestCondition();
		newCondition.setCondition(argValue);
		modelObject.addToConditions(newCondition);
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

		append(staticContents("select"), getSelectFragment());

		when(() -> isUnique()).thenAppend(staticContents(SPACE, "unique", ""), getUniqueFragment());

		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getFetchedType())), getTypeFragment());

		append(staticContents(SPACE, "from", ""), getFromFragment());
		//append(staticContents(SPACE, "(", ""), getLParFromFragment());
		append(dynamicContents(SPACE, () -> getFromAsString()), getFromExpressionFragment());

		when(() -> hasWhereClause()).thenAppend(staticContents(SPACE, FMLKeywords.Where.getKeyword(), ""), getWhereFragment())
		.thenAppend(staticContents(SPACE, "(", ""), getLParWhereFragment())
		.thenAppend(dynamicContents(() -> getWhereAsString()), getWhereConditionsFragment())
		.thenAppend(staticContents(")"), getRParWhereFragment());

		//append(staticContents(")"), getRParFromFragment());
		// Append semi only when required
		// final to true is here a little hack to prevent semi to be removed at pretty-print
		// This is due to a wrong management of semi
		// TODO: refactor 'semi' management
		when(() -> requiresSemi(),true).thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	private String getFromAsString() {
		if (getModelObject() instanceof AbstractSelectFlexoConceptInstance) {
			if (((AbstractSelectFlexoConceptInstance) getModelObject()).getContainer().isSet()) {
				return ((AbstractSelectFlexoConceptInstance) getModelObject()).getContainer().toString();
			}
		}
		if (getModelObject() instanceof AbstractSelectVirtualModelInstance) {
			if (((AbstractSelectVirtualModelInstance) getModelObject()).getContainer().isSet()) {
				return ((AbstractSelectVirtualModelInstance) getModelObject()).getContainer().toString();
			}
		}
		if (getModelObject() != null) {
			return getModelObject().getReceiver().toString();
		}
		return null;
	}

	private String getWhereAsString() {
		if (getModelObject() != null) {
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (FetchRequestCondition condition : getModelObject().getConditions()) {
				if (condition.getCondition().isSet()) {
					sb.append((isFirst ? "" : ",") + condition.getCondition());
					isFirst = false;
				}
			}
			return sb.toString();
		}
		return null;
	}

	private boolean isUnique() {
		if (getASTNode() != null && getASTNode().getKwUnique() != null) {
			return true;
		}
		if (getModelObject() != null) {
			return (getModelObject() instanceof SelectUniqueFlexoConceptInstance)
					|| (getModelObject() instanceof SelectUniqueVirtualModelInstance);
		}
		return false;
	}

	private RawSourceFragment getSelectFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwSelect());
		}
		return null;
	}

	private RawSourceFragment getUniqueFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwUnique());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSelectedType());
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

	private boolean hasWhereClause() {
		if (getModelObject() != null) {
			return getModelObject().getConditions().size() > 0;
		}
		else {
			return getASTNode() != null && getASTNode().getWhereClause() != null;
		}
	}

	private RawSourceFragment getWithFragment() {
		if (getASTNode() != null) {
			PWithClause withClause = getASTNode().getWithClause();
			if (withClause != null) {
				return getFragment(withClause);
			}
		}
		return null;
	}

	private RawSourceFragment getWhereFragment() {
		if (getASTNode() != null) {
			PWhereClause whereClause = getASTNode().getWhereClause();
			if (whereClause instanceof AWhereClause) {
				return getFragment(((AWhereClause) whereClause).getKwWhere());
			}
		}
		return null;
	}

	private RawSourceFragment getLParWhereFragment() {
		if (getASTNode() != null) {
			PWhereClause whereClause = getASTNode().getWhereClause();
			if (whereClause instanceof AWhereClause) {
				return getFragment(((AWhereClause) whereClause).getLPar());
			}
		}
		return null;
	}

	private RawSourceFragment getRParWhereFragment() {
		if (getASTNode() != null) {
			PWhereClause whereClause = getASTNode().getWhereClause();
			if (whereClause instanceof AWhereClause) {
				return getFragment(((AWhereClause) whereClause).getRPar());
			}
		}
		return null;
	}

	private RawSourceFragment getWhereConditionsFragment() {
		if (getASTNode() != null) {
			PWhereClause whereClause = getASTNode().getWhereClause();
			if (whereClause instanceof AWhereClause) {
				return getFragment(((AWhereClause) whereClause).getArgumentList());
			}
		}
		return null;
	}

}
