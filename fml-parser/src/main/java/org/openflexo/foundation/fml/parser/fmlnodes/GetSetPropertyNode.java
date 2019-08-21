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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AGetDeclaration;
import org.openflexo.foundation.fml.parser.node.AGetSetPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.ASetDeclaration;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 * 		get_set_property_declaration =
 *              visibility? type identifier l_brc get_declaration set_declaration ? r_brc semi;
 *
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class GetSetPropertyNode extends FlexoPropertyNode<AGetSetPropertyDeclaration, GetProperty<?>> {

	private static final Logger logger = Logger.getLogger(GetSetPropertyNode.class.getPackage().getName());

	public GetSetPropertyNode(AGetSetPropertyDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public GetSetPropertyNode(GetProperty<?> property, FMLSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public GetProperty<?> buildModelObjectFromAST(AGetSetPropertyDeclaration astNode) {

		GetProperty<?> returned;

		if (astNode.getSetDeclaration() == null) {
			// This is a Get property
			returned = getFactory().newGetProperty();
		}
		else {
			returned = getFactory().newGetSetProperty();
		}

		returned.setVisibility(getVisibility(astNode.getVisibility()));
		returned.setName(astNode.getIdentifier().getText());
		returned.setDeclaredType(getTypeFactory().makeType(astNode.getType()));

		AGetDeclaration getDeclaration = (AGetDeclaration) astNode.getGetDeclaration();
		returned.setGetControlGraph(makeControlGraph(getDeclaration.getFlexoBehaviourBody()));

		if (astNode.getSetDeclaration() != null) {
			ASetDeclaration setDeclaration = (ASetDeclaration) astNode.getSetDeclaration();
			((GetSetProperty<?>) returned).setSetControlGraph(makeControlGraph(setDeclaration.getFlexoBehaviourBody()));
		}

		return returned;
	}

	protected FMLControlGraph makeControlGraph(PFlexoBehaviourBody body) {
		return getFactory().newEmptyControlGraph();
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
			appendDynamicContents(() -> serializeType(getModelObject().getType()), SPACE, getTypeFragment());
			appendDynamicContents(() -> getModelObject().getName(), SPACE, getNameFragment());
			appendStaticContents("{}", SPACE);
			// appendDynamicContents(() -> getModelObject().getExpression().toString(), getExpressionFragment());
			appendStaticContents(";", getSemiFragment());
		}
		else {
			appendDynamicContents(() -> serializeType(getModelObject().getType()), SPACE);
			appendDynamicContents(() -> getModelObject().getName(), SPACE);
			appendStaticContents("{}", SPACE);
			// appendDynamicContents(() -> getModelObject().getExpression().toString());
			appendStaticContents(";");
		}
	}

	private RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

}
