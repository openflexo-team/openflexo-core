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

import org.openflexo.foundation.fml.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AValueAnnotation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class MetaDataNode extends FMLObjectNode<AValueAnnotation, FMLMetaData, MainSemanticsAnalyzer> {

	public MetaDataNode(AValueAnnotation astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public MetaDataNode(FMLMetaData importDeclaration, MainSemanticsAnalyzer analyser) {
		super(importDeclaration, analyser);
	}

	@Override
	public MetaDataNode deserialize() {
		if (getParent() instanceof FMLObjectNode) {
			System.out.println("Adding to meta data for " + getParent().getModelObject() + " -> " + getModelObject().getKey() + "="
					+ getModelObject().getFMLValueRepresentation());
			((FMLObjectNode<?, ?, ?>) getParent()).getModelObject().addToMetaData(getModelObject());
		}

		return this;
	}

	@Override
	public FMLMetaData buildModelObjectFromAST(AValueAnnotation astNode) {
		String key = makeFullQualifiedIdentifier(astNode.getIdentifier(), astNode.getAdditionalIdentifiers());
		String value = getText(astNode.getExpression());
		return getFactory().newMetaData(key, value);
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("@"), getAtFragment());
		append(dynamicContents(() -> getModelObject().getKey()), getKeyFragment());
		append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> getModelObject().getFMLValueRepresentation()), getValueFragment());
		append(staticContents(")"), getRParFragment());
	}

	private RawSourceFragment getKeyFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier(), getASTNode().getAdditionalIdentifiers());
		}
		return null;
	}

	private RawSourceFragment getValueFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

	private RawSourceFragment getAtFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getAt());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRPar());
		}
		return null;
	}

}
