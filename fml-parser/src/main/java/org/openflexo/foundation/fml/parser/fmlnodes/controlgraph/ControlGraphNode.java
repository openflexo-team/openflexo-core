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

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * @author sylvain
 * 
 */
public abstract class ControlGraphNode<N extends Node, T extends FMLControlGraph> extends FMLObjectNode<N, T, ControlGraphFactory> {

	private static final Logger logger = Logger.getLogger(ControlGraphNode.class.getPackage().getName());

	private ControlGraphFactory cgFactory;

	public ControlGraphNode(N astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);
	}

	public ControlGraphNode(T property, ControlGraphFactory cgFactory) {
		super(property, cgFactory);
	}

	@Override
	public ControlGraphNode<N, T> deserialize() {
		// System.out.println("deserialize for " + getParent() + " modelObject: " + getModelObject());
		if (getParent() instanceof SequenceNode) {
			// getAbstractAnalyser().sequentiallyAppend(getModelObject());

			/*if (((SequenceNode) getParent()).getModelObject().getControlGraph1() == null) {
				((SequenceNode) getParent()).getModelObject().setControlGraph1(getModelObject());
			}
			else if (((SequenceNode) getParent()).getModelObject().getControlGraph2() == null) {
				((SequenceNode) getParent()).getModelObject().setControlGraph2(getModelObject());
			}*/
			// System.out.println("Donc: " + ((SequenceNode) getParent()).getModelObject().getFMLRepresentation());
		}
		return this;
	}

}
