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

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFlexoBehaviourDeclarationConceptBodyDeclaration;

public class IRFlexoBehaviourNode extends IRNode<FlexoBehaviour, AFlexoBehaviourDeclarationConceptBodyDeclaration> {

	public IRFlexoBehaviourNode(AFlexoBehaviourDeclarationConceptBodyDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
	}

	@Override
	FlexoBehaviour buildFMLObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	/*protected String getFMLAnnotation(FMLPrettyPrintContext context) {
		return "@" + getImplementedInterface().getSimpleName();
	}
	
	@Override
	public String getFMLRepresentation(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		out.append(getFMLAnnotation(context), context);
		out.append(StringUtils.LINE_SEPARATOR, context);
		out.append(getVisibility().getFMLRepresentation() + TypeUtils.simpleRepresentation(getReturnType()) + " " + getName() + "("
				+ getParametersFMLRepresentation(context) + ") {", context);
		out.append(StringUtils.LINE_SEPARATOR, context);
		if (getControlGraph() != null) {
			out.append(getControlGraph().getFMLRepresentation(context), context, 1);
		}
		out.append(StringUtils.LINE_SEPARATOR, context);
		out.append("}", context);
		out.append(StringUtils.LINE_SEPARATOR, context);
		return out.toString();
	}
	
	protected String getParametersFMLRepresentation(FMLPrettyPrintContext context) {
		if (getParameters().size() > 0) {
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (FlexoBehaviourParameter p : getParameters()) {
				sb.append((isFirst ? "" : ", ") + TypeUtils.simpleRepresentation(p.getType()) + " " + p.getName());
				isFirst = false;
			}
			return sb.toString();
		}
		return "";
	}*/

}
