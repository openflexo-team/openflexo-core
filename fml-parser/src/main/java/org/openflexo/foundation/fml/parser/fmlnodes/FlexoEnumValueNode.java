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

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.AEnumValue;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

public class FlexoEnumValueNode extends FMLObjectNode<AEnumValue, FlexoEnumValue, FMLCompilationUnitSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoEnumValueNode.class.getPackage().getName());

	public FlexoEnumValueNode(AEnumValue astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FlexoEnumValueNode(FlexoEnumValue modelObject, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(modelObject, analyzer);
	}

	@Override
	public FlexoEnumValueNode deserialize() {
		if (getParent() instanceof FlexoEnumNode) {
			FlexoEnum flexoEnum = ((FlexoEnumNode) getParent()).getModelObject();
			flexoEnum.addToValues(getModelObject());
		}
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlexoEnumValue buildModelObjectFromAST(AEnumValue astNode) {
		FlexoEnumValue returned = getFactory().newFlexoEnumValue();
		try {
			returned.setName(astNode.getCidentifier().getText());
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());
		// @formatter:on
	}

	protected RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getCidentifier());
		}
		return null;
	}

}
