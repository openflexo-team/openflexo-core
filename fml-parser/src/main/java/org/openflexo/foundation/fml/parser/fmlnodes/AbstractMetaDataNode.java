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

import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * @author sylvain
 * 
 */
public abstract class AbstractMetaDataNode<N extends Node, T extends FMLMetaData, A extends FMLSemanticsAnalyzer>
		extends FMLObjectNode<N, T, A> {

	public AbstractMetaDataNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public AbstractMetaDataNode(T metaData, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(metaData, analyzer);
	}

	@Override
	public final AbstractMetaDataNode<N, T, A> deserialize() {
		if (getParent() instanceof FMLObjectNode) {
			// System.out.println("Adding to meta data for " + getParent().getModelObject() + " -> " + getModelObject().getKey() + "="
			// + getModelObject());
			((FMLObjectNode<?, ?, ?>) getParent()).getModelObject().addToMetaData(getModelObject());
		}
		if (getParent() instanceof ListMetaDataNode) {
			// System.out.println("Adding to meta data for " + getParent().getModelObject() + " -> " + getModelObject().getKey() + "="
			// + getModelObject());
			((ListMetaDataNode) getParent()).getModelObject().addToMetaDataList(getModelObject());
		}

		return this;
	}

}
