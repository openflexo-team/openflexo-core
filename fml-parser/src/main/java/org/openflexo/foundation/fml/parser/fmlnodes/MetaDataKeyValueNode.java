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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Constant;
import org.openflexo.foundation.fml.md.MetaDataKeyValue;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.AAnnotationKeyValuePair;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class MetaDataKeyValueNode extends FMLObjectNode<AAnnotationKeyValuePair, MetaDataKeyValue<?>, FMLCompilationUnitSemanticsAnalyzer> {

	public MetaDataKeyValueNode(AAnnotationKeyValuePair astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public MetaDataKeyValueNode(MetaDataKeyValue<?> metaData, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(metaData, analyzer);
	}

	@Override
	public MetaDataKeyValueNode deserialize() {
		if (getParent() instanceof MultiValuedMetaDataNode) {
			// System.out.println("Adding to meta data for " + getParent().getModelObject() + " -> " + getModelObject().getKey() + "="
			// + getModelObject());
			((MultiValuedMetaDataNode) getParent()).getModelObject().addToKeyValues(getModelObject());
		}

		return this;
	}

	@Override
	public MetaDataKeyValue<?> buildModelObjectFromAST(AAnnotationKeyValuePair astNode) {
		String key = makeFullQualifiedIdentifier(astNode.getIdentifier());

		MetaDataKeyValue<?> returned = getFactory().newMetaDataKeyValue(key);

		DataBinding<?> valueExpression = ExpressionFactory.makeDataBinding(astNode.getConditionalExp(), returned, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);

		if (valueExpression.getExpression() instanceof Constant) {
			returned.setSerializationRepresentation(getText(astNode.getConditionalExp()));
		}
		else {
			returned.setValueExpression((DataBinding) valueExpression);
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getKey()), getKeyFragment());
		append(staticContents("="), getAssignFragment());
		append(dynamicContents(() -> getModelObject().getSerializationRepresentation()), getValueFragment());
	}

	private RawSourceFragment getKeyFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getValueFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getConditionalExp());
		}
		return null;
	}

	private RawSourceFragment getAssignFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getAssign());
		}
		return null;
	}

}
