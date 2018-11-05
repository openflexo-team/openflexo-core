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

package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * This class implements the semantics analyzer for a parsed FMLObject.<br>
 * Its main purpose is to structurally build a binding from a parsed AST.<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectSemanticsAnalyzer<N extends Node, T extends FMLObject> extends FMLSemanticsAnalyzer {

	private final N node;
	private final FMLSemanticsAnalyzer parentAnalyser;

	public FMLObjectSemanticsAnalyzer(N node, FMLSemanticsAnalyzer parentAnalyser, FlexoServiceManager serviceManager) {
		// System.out.println(">>>> node=" + node + " of " + node.getClass());
		super(parentAnalyser.getVirtualModel(), serviceManager);
		this.node = node;
		this.parentAnalyser = parentAnalyser;
	}

	public N getNode() {
		return node;
	}

	public abstract T makeFMLObject();

	@Override
	public VirtualModel getVirtualModel() {
		if (parentAnalyser != null) {
			System.out.println("Moi: " + this + " et lui " + parentAnalyser);
			return parentAnalyser.getVirtualModel();
		}
		return super.getVirtualModel();
	}
}
