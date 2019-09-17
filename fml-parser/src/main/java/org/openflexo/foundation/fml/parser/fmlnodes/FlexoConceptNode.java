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

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AConceptDecl;
import org.openflexo.foundation.fml.parser.node.ASuperClause;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class FlexoConceptNode extends AbstractFlexoConceptNode<AConceptDecl, FlexoConcept> {

	public FlexoConceptNode(AConceptDecl astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FlexoConceptNode(FlexoConcept concept, MainSemanticsAnalyzer analyser) {
		super(concept, analyser);
	}

	@Override
	public FlexoConcept buildModelObjectFromAST(AConceptDecl astNode) {
		FlexoConcept returned = getFactory().newFlexoConcept();
		returned.setName(astNode.getIdentifier().getText());
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		buildParentConcepts(returned, astNode.getSuperClause());
		return returned;
	}

	@Override
	public FlexoConceptNode deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoConcepts(getModelObject());
		}
		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(staticContents("","concept",SPACE), getConceptFragment());
		append(dynamicContents(() -> getModelObject().getName()),getNameFragment());

		when(() -> getModelObject().getParentFlexoConcepts().size()>0)
		.thenAppend(staticContents(SPACE,"extends",SPACE), getExtendsFragment())
		.thenAppend(dynamicContents(() -> getModelObject().getParentFlexoConceptsDeclaration()),getSuperTypeListFragment())
		.elseAppend(staticContents(""), getSuperClauseFragment());

		append(staticContents(SPACE, "{", LINE_SEPARATOR), getLBrcFragment());
		append(childrenContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent,
				FlexoProperty.class));
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class));
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getEmbeddedFlexoConcepts(), LINE_SEPARATOR, Indentation.Indent,
				FlexoConcept.class));
		append(staticContents("", "}", LINE_SEPARATOR), getRBrcFragment());
		// @formatter:on

		/*appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		
		appendStaticContents("concept", SPACE, getConceptFragment());
		appendDynamicContents(() -> getModelObject().getName(), getNameFragment());
		appendStaticContents(SPACE, "{", LINE_SEPARATOR, getLBrcFragment());
		
		appendToChildrenPrettyPrintContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent,
				FlexoProperty.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getChildFlexoConcepts(), LINE_SEPARATOR,
				Indentation.Indent, FlexoConcept.class);
		
		appendStaticContents("}", LINE_SEPARATOR, getRBrcFragment());*/
	}

	@Override
	protected RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getConceptFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwConcept());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getSuperClauseFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			return getFragment(getASTNode().getSuperClause());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getExtendsFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			ASuperClause superClause = (ASuperClause) getASTNode().getSuperClause();
			return getFragment(superClause.getKwExtends());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getSuperTypeListFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			ASuperClause superClause = (ASuperClause) getASTNode().getSuperClause();
			return getFragment(superClause.getSuperTypeList());
		}
		return null;
	}

}
