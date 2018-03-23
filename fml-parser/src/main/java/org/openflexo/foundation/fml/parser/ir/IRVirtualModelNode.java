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

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AVirtualModelDeclaration;
import org.openflexo.toolbox.StringUtils;

public class IRVirtualModelNode extends IRAbstractFlexoConceptNode<VirtualModel, AVirtualModelDeclaration> {

	public IRVirtualModelNode(AVirtualModelDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
	}

	@Override
	VirtualModel buildFMLObject() {
		VirtualModel vm = getSemanticsAnalyzer().getFactory().newVirtualModel();
		vm.initializeDeserialization(getSemanticsAnalyzer().getFactory());
		vm.setName(getNode().getIdentifier().getText());
		/*try {
			vm = VirtualModelImpl.newVirtualModel(getNode().getIdentifier().getText(), getViewPoint());
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}*/
		// System.out.println("******** Hop je cree un nouveau VirtualModel " + getNode().getIdentifier().getText());
		// getFragment().printFragment();

		for (IRNode<?, ?> childNode : getChildren()) {
			if (childNode instanceof IRFlexoConceptNode) {
				vm.addToFlexoConcepts(((IRFlexoConceptNode) childNode).getFMLObject());
			}
		}

		return vm;
	}

	@Override
	protected String getAnnotationHeader(FMLPrettyPrintContext context) {
		String returned = super.getAnnotationHeader(context);
		if (getFMLObject() instanceof VirtualModel) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(returned, context);
			out.append("@Version(\"" + getFMLObject().getVersion() + "\")" + StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}
		return returned;
	}

	protected String getFMLDeclaredConcepts(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);
		if (getFMLObject().getFlexoConcepts().size() > 0) {
			out.append(StringUtils.LINE_SEPARATOR, context);
			for (FlexoConcept ep : getFMLObject().getFlexoConcepts()) {
				IRNode<FlexoConcept, ?> conceptNode = getRootNode().getNode(getFMLObject());
				out.append(conceptNode.getFMLPrettyPrint(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
		}
		return out.toString();
	}

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);

		out.append(getAnnotationHeader(context), context);
		out.append(StringUtils.LINE_SEPARATOR, context);

		out.append("public model " + getFMLObject().getName() + getExtends(context), context);
		out.append(" {" + StringUtils.LINE_SEPARATOR, context);

		out.append(getFMLDeclaredProperties(context), context);

		out.append(getFMLDeclaredBehaviours(context), context);

		out.append(getFMLDeclaredConcepts(context), context);

		out.append("}" + StringUtils.LINE_SEPARATOR, context);

		return out.toString();
	}

}
