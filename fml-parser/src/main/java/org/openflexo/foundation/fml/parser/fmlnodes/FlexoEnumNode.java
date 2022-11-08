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

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AEnumDecl;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class FlexoEnumNode extends AbstractFlexoConceptNode<AEnumDecl, FlexoEnum> {

	public FlexoEnumNode(AEnumDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FlexoEnumNode(FlexoEnum concept, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(concept, analyzer);
	}

	@Override
	public FlexoEnum buildModelObjectFromAST(AEnumDecl astNode) {
		FlexoEnum returned = getFactory().newFlexoEnum();
		try {
			returned.setName(astNode.getUidentifier().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getUidentifier().getText());
		}
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		return returned;
	}

	@Override
	public FlexoEnumNode deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoConcepts(getModelObject());
		}
		if (getParent() instanceof FlexoEnumNode) {
			((FlexoEnumNode) getParent()).getModelObject().addToEmbeddedFlexoConcepts(getModelObject());
		}
		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent, FMLMetaData.class));
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(staticContents("", FMLKeywords.Enum.getKeyword(), SPACE), getEnumFragment());
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());

		append(staticContents(SPACE, "{", LINE_SEPARATOR), getLBrcFragment());
		append(childrenContents("", "", () -> getModelObject().getValues(), ","+LINE_SEPARATOR, LINE_SEPARATOR, Indentation.Indent, FlexoEnumValue.class));
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

	private RawSourceFragment getEnumFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwEnum());
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
		// Not applicable
		return null;
	}

	@Override
	protected RawSourceFragment getExtendsFragment() {
		// Not applicable
		return null;
	}

	@Override
	protected RawSourceFragment getSuperTypeListFragment() {
		// Not applicable
		return null;
	}

}
