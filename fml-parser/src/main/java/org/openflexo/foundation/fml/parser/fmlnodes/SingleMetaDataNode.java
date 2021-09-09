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
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ASingleAnnotationAnnotation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class SingleMetaDataNode extends AbstractMetaDataNode<ASingleAnnotationAnnotation, SingleMetaData<?>, MainSemanticsAnalyzer> {

	public SingleMetaDataNode(ASingleAnnotationAnnotation astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public SingleMetaDataNode(SingleMetaData<?> metaData, MainSemanticsAnalyzer analyser) {
		super(metaData, analyser);
	}

	@Override
	public SingleMetaData<?> buildModelObjectFromAST(ASingleAnnotationAnnotation astNode) {
		String key = makeFullQualifiedIdentifier(astNode.getIdentifier());

		SingleMetaData<?> returned = getFactory().newSingleMetaData(key);

		DataBinding<?> valueExpression = ExpressionFactory.makeDataBinding(astNode.getConditionalExp(), returned, BindingDefinitionType.GET,
				Object.class, getAnalyser(), this);

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

		append(staticContents("@"), getAtFragment());
		append(dynamicContents(() -> getModelObject().getKey()), getKeyFragment());
		append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> getModelObject().getSerializationRepresentation()), getValueFragment());
		append(staticContents(")"), getRParFragment());
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
