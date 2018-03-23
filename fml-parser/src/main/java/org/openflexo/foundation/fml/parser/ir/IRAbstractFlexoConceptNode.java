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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.toolbox.StringUtils;

public abstract class IRAbstractFlexoConceptNode<O extends FlexoConcept, N extends Node> extends IRAbstractAnnotableNode<O, N> {

	public IRAbstractFlexoConceptNode(N node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
	}

	protected String getFMLDeclaredProperties(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		if (getFMLObject().getDeclaredProperties().size() > 0) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			for (FlexoProperty<?> pr : getFMLObject().getDeclaredProperties()) {
				IRNode<FlexoProperty<?>, ?> propertyNode = getRootNode().getNode(pr);
				out.append(propertyNode.getFMLPrettyPrint(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
		}
		return out.toString();
	}

	protected String getFMLDeclaredBehaviours(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		if (getFMLObject().getFlexoBehaviours().size() > 0) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			for (FlexoBehaviour es : getFMLObject().getFlexoBehaviours()) {
				IRNode<FlexoBehaviour, ?> behaviourNode = getRootNode().getNode(es);
				out.append(behaviourNode.getFMLPrettyPrint(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
		}
		return out.toString();
	}

	protected String getFMLDeclaredInnerConcepts(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		if (getFMLObject().getEmbeddedFlexoConcepts().size() > 0) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			for (FlexoConcept ep : getFMLObject().getEmbeddedFlexoConcepts()) {
				IRNode<FlexoConcept, ?> conceptNode = getRootNode().getNode(ep);
				out.append(conceptNode.getFMLPrettyPrint(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
		}
		return out.toString();
	}

	protected String getExtends(FMLPrettyPrintContext context) {
		if (getFMLObject().getParentFlexoConcepts().size() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append(" extends ");
			boolean isFirst = true;
			for (FlexoConcept parent : getFMLObject().getParentFlexoConcepts()) {
				sb.append((isFirst ? "" : ",") + parent.getName());
				isFirst = false;
			}
			sb.append(" ");
			return sb.toString();
		}
		return "";
	}

}
