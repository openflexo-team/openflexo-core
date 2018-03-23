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

import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Node;

public abstract class IRFlexoPropertyNode<O extends FlexoProperty<?>, N extends Node> extends IRNode<O, N> {

	public IRFlexoPropertyNode(N node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
	}

	/*protected String getFMLAnnotation(FMLRepresentationContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append("@" + getImplementedInterface().getSimpleName(), context);
		if (isKey()) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append("@Key", context);
		}
		return out.toString();
	}*/

	/*@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append(getFMLAnnotation(context), context);
		out.append(StringUtils.LINE_SEPARATOR, context);
		if (detailedFMLSpecifications(context) == null) {
			out.append("public " + TypeUtils.simpleRepresentation(getResultingType()) + " " + getName() + ";", context);
		}
		else {
			out.append("public " + TypeUtils.simpleRepresentation(getResultingType()) + " " + getName() + " {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append(detailedFMLSpecifications(context), context, 1);
			// out.append(StringUtils.LINE_SEPARATOR, context);
			out.append("}", context);
		}
		return out.toString();
	}*/

	/*public String detailedFMLSpecifications(FMLPrettyPrintContext context) {
		return null;
	}*/

}
