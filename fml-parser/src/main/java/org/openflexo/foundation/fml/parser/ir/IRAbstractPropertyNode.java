/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.ir;

import java.lang.reflect.Type;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.SemanticsException;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyDeclaration;

public class IRAbstractPropertyNode extends IRFlexoPropertyNode<AbstractProperty<?>, AAbstractPropertyDeclaration> {

	public IRAbstractPropertyNode(AAbstractPropertyDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);

	}

	@Override
	AbstractProperty<?> buildFMLObject() {
		AbstractProperty<?> property = getSemanticsAnalyzer().getFactory().newAbstractProperty();
		property.setName(getNode().getIdentifier().getText());
		try {
			Type type = TypeAnalyzingUtils.makeType(getNode().getType(), getSemanticsAnalyzer());
			property.setType(type);
		} catch (SemanticsException e) {
			fireSemanticsException(e);
			property.setType(Object.class);
		}
		return property;
	}

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append("public abstract " + TypeUtils.simpleRepresentation(getFMLObject().getResultingType()) + " " + getFMLObject().getName()
				+ ";", context);
		return out.toString();
	}

}
