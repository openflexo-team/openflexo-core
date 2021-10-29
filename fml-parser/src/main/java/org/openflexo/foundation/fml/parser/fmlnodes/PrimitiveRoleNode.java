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

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AJavaInnerConceptDecl;

/**
 * @author sylvain
 * 
 */
public class PrimitiveRoleNode extends BasicPropertyNode<PrimitiveRole<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PrimitiveRoleNode.class.getPackage().getName());

	public PrimitiveRoleNode(AJavaInnerConceptDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public PrimitiveRoleNode(PrimitiveRole<?> property, FMLCompilationUnitSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
		append(dynamicContents(() -> serializeCardinality(getModelObject().getCardinality())), getCardinalityFragment());
		append(dynamicContents(SPACE, () -> getModelObject().getName()), getNameFragment());
		append(staticContents(";"), getSemiFragment());
	}

	@Override
	public PrimitiveRole<?> buildModelObjectFromAST(AJavaInnerConceptDecl astNode) {
		PrimitiveRole<?> returned = getFactory().newPrimitiveRole();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		try {
			returned.setName(getName(astNode.getVariableDeclarator()).getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + getName(astNode.getVariableDeclarator()).getText());
		}
		returned.setPrimitiveType(PrimitiveType.toPrimitiveType(TypeFactory.makeType(astNode.getType(), getAnalyser().getTypingSpace())));
		returned.setCardinality(getCardinality(astNode.getCardinality()));
		return returned;
	}

}
