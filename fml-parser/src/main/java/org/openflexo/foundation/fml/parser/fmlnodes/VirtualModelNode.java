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

import java.util.ArrayList;

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.foundation.fml.parser.node.ASuperClause;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class VirtualModelNode extends AbstractFlexoConceptNode<AModelDecl, VirtualModel> {

	public VirtualModelNode(AModelDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public VirtualModelNode(VirtualModel virtualModel, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(virtualModel, analyzer);
	}

	@Override
	public VirtualModel buildModelObjectFromAST(AModelDecl astNode) {
		VirtualModel returned = getFactory().newVirtualModel();
		try {
			returned.setName(astNode.getUidentifier().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getUidentifier().getText());
		}
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		// getTypeFactory().setDeserializedVirtualModel(returned);
		buildParentConcepts(returned, astNode.getSuperClause());
		return returned;
	}

	@Override
	public VirtualModelNode deserialize() {
		if (getParent() instanceof FMLCompilationUnitNode) {
			((FMLCompilationUnitNode) getParent()).getModelObject().setVirtualModel(getModelObject());
		}
		for (FlexoConcept concept : new ArrayList<>(getModelObject().getFlexoConcepts())) {
			bindEmbeddedFlexoConcepts(concept);
		}

		getModelObject().getVersion();
		getModelObject().getURI();
		return this;
	}

	// TODO: remove getFlexoConcepts() property in VirtualModel
	@Deprecated
	private void bindEmbeddedFlexoConcepts(FlexoConcept concept) {
		for (FlexoConcept childConcept : concept.getEmbeddedFlexoConcepts()) {
			getModelObject().addToFlexoConcepts(childConcept);
			bindEmbeddedFlexoConcepts(childConcept);
		}
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent, FMLMetaData.class));
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(staticContents("", "model", SPACE), getModelFragment());
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());

		when(() -> getModelObject().getParentFlexoConcepts().size() > 0)
				.thenAppend(staticContents(SPACE, "extends", SPACE), getExtendsFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getParentFlexoConceptsDeclaration()), getSuperTypeListFragment());

		append(staticContents(SPACE, "{", LINE_SEPARATOR), getLBrcFragment());
		append(childrenContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent, FlexoProperty.class));
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class));
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getAllRootFlexoConcepts(), LINE_SEPARATOR, Indentation.Indent,
				FlexoConcept.class));
		append(staticContents("", "}", LINE_SEPARATOR), getRBrcFragment());
		// @formatter:on

		/*appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		
		appendStaticContents("model", SPACE, getModelFragment());
		appendDynamicContents(() -> getModelObject().getName(), getNameFragment());
		appendStaticContents(SPACE, "{", LINE_SEPARATOR, getLBrcFragment());
		
		appendToChildrenPrettyPrintContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent,
				FlexoProperty.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoConcepts(), LINE_SEPARATOR, Indentation.Indent,
				FlexoConcept.class);
		
		appendStaticContents("}", LINE_SEPARATOR, getRBrcFragment());*/
	}

	@Override
	protected RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getModelFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwModel());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getUidentifier());
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
