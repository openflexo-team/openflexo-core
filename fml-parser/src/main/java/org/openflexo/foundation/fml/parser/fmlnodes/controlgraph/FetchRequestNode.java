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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFromClause;
import org.openflexo.foundation.fml.parser.node.ASelectActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFromClause;
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

	public FetchRequestNode(ASelectActionFmlActionExp astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FetchRequestNode(FR action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	@Override
	public FR buildModelObjectFromAST(ASelectActionFmlActionExp astNode) {
		FR returned = null;

		// FlexoConceptInstanceType type = getTypeFactory().lookupConceptNamed(astNode.getSelectedTypeName());

		FlexoConceptInstanceType type = getTypeFactory().makeFlexoConceptType(astNode.getSelectedTypeName());

		// System.out.println("Found type: " + type);

		if (type instanceof VirtualModelInstanceType) {
			AbstractSelectVirtualModelInstance selectAction = null;
			if (astNode.getKwUnique() != null) {
				selectAction = getFactory().newSelectUniqueVirtualModelInstance();
			}
			else {
				selectAction = getFactory().newSelectVirtualModelInstance();
			}
			VirtualModel vm = ((VirtualModelInstanceType) type).getVirtualModel();
			if (vm != null && selectAction != null) {
				selectAction.setVirtualModelType(vm);
			}
			if (astNode.getFromClause() instanceof AFromClause) {
				PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
				DataBinding<?> container = ExpressionFactory.makeExpression(fromExpression, getAnalyser(), selectAction);
				selectAction.setContainer(container);
				selectAction.setReceiver(container);
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
			FlexoConcept concept = type.getFlexoConcept();
			if (concept != null && selectAction != null) {
				selectAction.setFlexoConceptType(concept);
			}
			if (astNode.getFromClause() instanceof AFromClause) {
				PExpression fromExpression = ((AFromClause) astNode.getFromClause()).getExpression();
				DataBinding<?> container = ExpressionFactory.makeExpression(fromExpression, getAnalyser(), selectAction);
				selectAction.setContainer(container);
				selectAction.setReceiver(container);
			}
			returned = (FR) selectAction;
		}
		else {
			// TODO...
			returned = null;
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

		append(staticContents("select"), getSelectFragment());

		when(() -> isUnique())
		.thenAppend(staticContents(SPACE,"unique",""), getUniqueFragment());

		append(dynamicContents(SPACE, () -> serializeType(getModelObject().getFetchedType())), getTypeFragment());

		append(staticContents(SPACE, "from",""), getFromFragment());
		append(staticContents(SPACE, "(",""), getLParFromFragment());
		append(dynamicContents(() -> getFromAsString()), getFromExpressionFragment());
		append(staticContents(")"), getRParFromFragment());
		// Append semi only when required
		when(() -> requiresSemi()).thenAppend(staticContents(";"), getSemiFragment());
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
			return getFragment(getASTNode().getSelectedTypeName());
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

	private RawSourceFragment getLParFromFragment() {
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
	}

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
