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
import org.openflexo.foundation.fml.parser.DynamicContents;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.foundation.fml.parser.StaticContents;
import org.openflexo.foundation.fml.parser.node.AConceptDeclaration;

/**
 * @author sylvain
 * 
 */
public class FlexoConceptNode extends FMLObjectNode<AConceptDeclaration, FlexoConcept> {

	public FlexoConceptNode(AConceptDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FlexoConceptNode(FlexoConcept concept, FMLSemanticsAnalyzer analyser) {
		super(concept, analyser);
	}

	@Override
	public FlexoConcept buildFMLObjectFromAST(AConceptDeclaration astNode) {
		FlexoConcept returned = getFactory().newFlexoConcept();
		returned.setName(astNode.getIdentifier().getText());
		return returned;
	}

	@Override
	public FlexoConceptNode deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getFMLObject().addToFlexoConcepts(getFMLObject());
		}
		return this;
	}

	@Override
	protected void preparePrettyPrint() {

		RawSourceFragment nameFragment = getFragment(getASTNode().getIdentifier());
		RawSourceFragment conceptFragment = getFragment(getASTNode().getConcept());

		if (getASTNode().getVisibility() != null) {
			RawSourceFragment visibilityFragment = getFragment(getASTNode().getVisibility());
			appendToPrettyPrintContents(new DynamicContents<>(() -> getVisibilityAsString(), SPACE, visibilityFragment));
		}
		else {
			appendToPrettyPrintContents(new DynamicContents<>(() -> getVisibilityAsString(), SPACE, conceptFragment.getStartPosition()));
		}
		appendToPrettyPrintContents(new StaticContents<>("concept", SPACE, conceptFragment));
		appendToPrettyPrintContents(new DynamicContents<>(() -> getFMLObject().getName(), nameFragment));
		appendToPrettyPrintContents(new StaticContents<>("{", LINE_SEPARATOR, getFragment(getASTNode().getLBrc())));

		for (FlexoProperty<?> property : getFMLObject().getFlexoProperties()) {
			appendToChildPrettyPrintContents("", property, LINE_SEPARATOR, 1);
		}
		for (FlexoBehaviour behaviour : getFMLObject().getFlexoBehaviours()) {
			appendToChildPrettyPrintContents(LINE_SEPARATOR, behaviour, LINE_SEPARATOR, 1);
		}
		for (FlexoConcept concept : getFMLObject().getChildFlexoConcepts()) {
			appendToChildPrettyPrintContents(LINE_SEPARATOR, concept, LINE_SEPARATOR, 1);
		}
		appendToPrettyPrintContents(new StaticContents<>("}", LINE_SEPARATOR, getFragment(getASTNode().getRBrc())));
	}

}
