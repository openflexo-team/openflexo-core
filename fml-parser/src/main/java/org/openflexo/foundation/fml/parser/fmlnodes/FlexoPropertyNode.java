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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * @author sylvain
 * 
 */
public abstract class FlexoPropertyNode<N extends Node, T extends FlexoProperty<?>> extends FMLObjectNode<N, T, MainSemanticsAnalyzer> {

	private static final Logger logger = Logger.getLogger(FlexoPropertyNode.class.getPackage().getName());

	public FlexoPropertyNode(N astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FlexoPropertyNode(T property, MainSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public FlexoPropertyNode<N, T> deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoProperties(getModelObject());
		}
		if (getParent() instanceof FlexoConceptNode) {
			((FlexoConceptNode) getParent()).getModelObject().addToFlexoProperties(getModelObject());
		}
		return this;
	}

	@Override
	protected FMLCompilationUnit getCompilationUnit() {
		return getAnalyser().getCompilationUnit();
	}

}
