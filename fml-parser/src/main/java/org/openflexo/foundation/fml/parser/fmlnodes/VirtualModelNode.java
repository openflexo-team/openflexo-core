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
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class VirtualModelNode extends FMLObjectNode<AModelDecl, VirtualModel> {

	public VirtualModelNode(AModelDecl astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public VirtualModelNode(VirtualModel virtualModel, FMLSemanticsAnalyzer analyser) {
		super(virtualModel, analyser);
	}

	@Override
	public VirtualModel buildModelObjectFromAST(AModelDecl astNode) {
		VirtualModel returned = getFactory().newVirtualModel();
		returned.setName(astNode.getIdentifier().getText());
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		return returned;
	}

	@Override
	public VirtualModelNode deserialize() {
		if (getParent() instanceof FMLCompilationUnitNode) {
			((FMLCompilationUnitNode) getParent()).getModelObject().setVirtualModel(getModelObject());
		}
		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		if (hasParsedVersion && getVisibilityFragment() != null) {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		}
		else {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE);
		}

		if (hasParsedVersion) {
			appendStaticContents("model", SPACE, getModelFragment());
			appendDynamicContents(() -> getModelObject().getName(), getNameFragment());
			appendStaticContents(SPACE, "{", LINE_SEPARATOR, getLBrcFragment());
		}
		else {
			appendStaticContents("model" + SPACE);
			appendDynamicContents(() -> getModelObject().getName());
			appendStaticContents(SPACE, "{", LINE_SEPARATOR);
		}
		appendToChildrenPrettyPrintContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, 1, FlexoProperty.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, 1,
				FlexoBehaviour.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoConcepts(), LINE_SEPARATOR, 1,
				FlexoConcept.class);

		if (getASTNode() != null) {
			appendStaticContents("}", LINE_SEPARATOR, getRBrcFragment());
		}
		else {
			appendStaticContents("}", LINE_SEPARATOR);
		}
	}

	private RawSourceFragment getVisibilityFragment() {
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

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	private RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}
}
