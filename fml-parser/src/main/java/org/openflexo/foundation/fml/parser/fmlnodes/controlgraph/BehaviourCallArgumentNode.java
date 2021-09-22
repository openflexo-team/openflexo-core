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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.rt.editionaction.BehaviourCallArgument;

/**
 * @author sylvain
 * 
 */
public class BehaviourCallArgumentNode extends FMLObjectNode<PExpression, BehaviourCallArgument, MainSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BehaviourCallArgumentNode.class.getPackage().getName());

	public BehaviourCallArgumentNode(PExpression astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public BehaviourCallArgumentNode(BehaviourCallArgument modelObject, MainSemanticsAnalyzer analyser) {
		super(modelObject, analyser);
	}

	@Override
	public BehaviourCallArgumentNode deserialize() {
		/*if (getParent() instanceof AddFlexoConceptInstanceNode) {
			AddFlexoConceptInstance<?> action = ((AddFlexoConceptInstanceNode) getParent()).getModelObject();
			int currentIndex = action.getParameters().size();
			if (action.getCreationScheme() != null) {
				FlexoBehaviourParameter parameter = action.getCreationScheme().getParameters().get(currentIndex);
				getModelObject().setParam(parameter);
			}
			((AddFlexoConceptInstanceNode) getParent()).getModelObject()
					.addToParameters((AddFlexoConceptInstanceParameter) getModelObject());
		}
		if (getParent() instanceof AddVirtualModelInstanceNode) {
			AddVirtualModelInstance action = ((AddVirtualModelInstanceNode) getParent()).getModelObject();
			int currentIndex = action.getParameters().size();
			if (action.getCreationScheme() != null) {
				FlexoBehaviourParameter parameter = action.getCreationScheme().getParameters().get(currentIndex);
				getModelObject().setParam(parameter);
			}
			((AddVirtualModelInstanceNode) getParent()).getModelObject()
					.addToParameters((AddFlexoConceptInstanceParameter) getModelObject());
		}*/
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public BehaviourCallArgument buildModelObjectFromAST(PExpression astNode) {
		BehaviourCallArgument returned = getFactory().newAddFlexoConceptInstanceParameter(null);

		// TODO: faire plutot ca
		/*ControlGraphNode<?, ?> assignableActionNode = ControlGraphFactory.makeControlGraphNode(astNode, getAnalyser());
		
		if (assignableActionNode != null) {
			if (assignableActionNode.getModelObject() instanceof ExpressionAction) {
				returned.setValue(((ExpressionAction)assignableActionNode.getModelObject()).getExpression());
				returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
				addToChildren(assignableActionNode);
			}
			else {
				System.err.println("Unexpected " + assignableActionNode.getModelObject());
				Thread.dumpStack();
			}
		}*/

		DataBinding<?> value = ExpressionFactory.makeDataBinding(astNode, returned, BindingDefinitionType.GET, Object.class, getAnalyser(),
				this);
		returned.setValue(value);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getValue().toString()), getFragment());
	}

}
