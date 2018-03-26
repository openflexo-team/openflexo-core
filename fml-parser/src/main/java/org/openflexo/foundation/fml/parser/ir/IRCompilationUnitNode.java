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

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;
import org.openflexo.toolbox.StringUtils;

public class IRCompilationUnitNode extends IRNode<VirtualModel, Start> {

	private IRVirtualModelNode virtualModelNode;
	private final Map<FMLObject, IRNode<?, ?>> parsedFMLObjects;

	public IRCompilationUnitNode(Start node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);
		parsedFMLObjects = new HashMap<>();
	}

	public IRVirtualModelNode getVirtualModelNode() {
		return virtualModelNode;
	}

	public void setVirtualModelNode(IRVirtualModelNode virtualModelNode) {
		this.virtualModelNode = virtualModelNode;
	}

	@Override
	VirtualModel buildFMLObject() {
		// Done in the IRVirtualModelNode
		return null;
	}

	@Override
	public VirtualModel getFMLObject() {
		if (getVirtualModelNode() != null) {
			return getVirtualModelNode().getFMLObject();
		}
		return super.getFMLObject();
	}

	protected <O extends FMLObject> void registerFMLObject(O fmlObject, IRNode<O, ?> node) {
		parsedFMLObjects.put(fmlObject, node);
	}

	@SuppressWarnings("unchecked")
	public <O extends FMLObject> IRNode<O, ?> getNode(O fmlObject) {
		return (IRNode<O, ?>) parsedFMLObjects.get(fmlObject);
	}

	@Override
	public String getFMLPrettyPrint(FMLPrettyPrintContext context) {
		FMLRepresentationOutput out = new FMLRepresentationOutput(context);

		if (getFMLObject().getUseDeclarations().size() > 0) {
			for (UseModelSlotDeclaration msDecl : getFMLObject().getUseDeclarations()) {
				out.append("use " + msDecl.getModelSlotClass().getCanonicalName() + ";" + StringUtils.LINE_SEPARATOR, context);
			}
			out.append(StringUtils.LINE_SEPARATOR, context);
		}
		IRNode<VirtualModel, ?> vmNode = getRootNode().getNode(getFMLObject());
		out.append(vmNode.getFMLPrettyPrint(context), context);
		return out.toString();
	}
}
