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

import org.openflexo.foundation.fml.FMLEnumPropertyValue;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AEnumQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.ALowerEnumValueArgument;
import org.openflexo.foundation.fml.parser.node.AUpperEnumValueArgument;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
    | {enum}           [arg_name]:lidentifier assign enum_value_argument

    	enum_value_argument =
		{lower} dollar lidentifier
	 |	{upper} dollar uidentifier
	 ;
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class FMLEnumPropertyValueNode<M extends FMLObject, E extends Enum<E>>
		extends AbstractFMLPropertyValueNode<AEnumQualifiedArgument, FMLEnumPropertyValue<M, E>, M, E> {

	private static final Logger logger = Logger.getLogger(FMLEnumPropertyValueNode.class.getPackage().getName());

	public FMLEnumPropertyValueNode(AEnumQualifiedArgument astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FMLEnumPropertyValueNode(FMLEnumPropertyValue<M, E> propertyValue, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(propertyValue, analyzer);
	}

	@Override
	public FMLEnumPropertyValueNode<M, E> deserialize() {

		String propertyName = getASTNode().getArgName().getText();
		FMLProperty fmlProperty = ((FMLObject) getParent().getModelObject()).getFMLProperty(propertyName, getFactory());

		if (fmlProperty == null) {
			getModelObject().setUnresolvedPropertyName(propertyName);
			logger.warning("Cannot find FML property " + propertyName + " in " + getParent().getModelObject());
			return (FMLEnumPropertyValueNode<M, E>) super.deserialize();
		}

		getModelObject().setProperty(fmlProperty);

		E enumValue = Enum.valueOf((Class<E>) fmlProperty.getType(), getEnumValueNode().getText());
		getModelObject().setEnumValue(enumValue);

		return (FMLEnumPropertyValueNode<M, E>) super.deserialize();
	}

	@Override
	public FMLEnumPropertyValue<M, E> buildModelObjectFromAST(AEnumQualifiedArgument astNode) {

		return (FMLEnumPropertyValue<M, E>) getFactory().newEnumPropertyValue();
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getPropertyName()), getArgNameFragment());
		append(staticContents("="), getAssignFragment());
		append(staticContents("$"), getDollarFragment());
		append(dynamicContents(() -> getModelObject().getEnumValue().name()), getEnumValueFragment());
	}

	private RawSourceFragment getArgNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getArgName());
		}
		return null;
	}

	private RawSourceFragment getAssignFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getAssign());
		}
		return null;
	}

	private RawSourceFragment getDollarFragment() {
		if (getASTNode() != null) {
			if (getASTNode().getEnumValueArgument() instanceof ALowerEnumValueArgument) {
				return getFragment(((ALowerEnumValueArgument) getASTNode().getEnumValueArgument()).getDollar());
			}
			if (getASTNode().getEnumValueArgument() instanceof AUpperEnumValueArgument) {
				return getFragment(((AUpperEnumValueArgument) getASTNode().getEnumValueArgument()).getDollar());
			}
		}
		return null;
	}

	private RawSourceFragment getEnumValueFragment() {
		if (getEnumValueNode() != null) {
			return getFragment(getEnumValueNode());
		}
		return null;
	}

	private Token getEnumValueNode() {
		if (getASTNode() != null) {
			if (getASTNode().getEnumValueArgument() instanceof ALowerEnumValueArgument) {
				return ((ALowerEnumValueArgument) getASTNode().getEnumValueArgument()).getLidentifier();
			}
			if (getASTNode().getEnumValueArgument() instanceof AUpperEnumValueArgument) {
				return ((AUpperEnumValueArgument) getASTNode().getEnumValueArgument()).getUidentifier();
			}
		}
		return null;
	}
}
