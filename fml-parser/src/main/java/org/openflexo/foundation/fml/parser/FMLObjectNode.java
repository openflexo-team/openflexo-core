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

package org.openflexo.foundation.fml.parser;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * Maintains consistency between the model (represented by an {@link FMLObject}) and source code represented in FML language
 * 
 * Works
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectNode<N extends Node, T extends FMLPrettyPrintable, A extends FMLSemanticsAnalyzer>
		extends ObjectNode<N, T, A> implements FMLPrettyPrintDelegate<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLObjectNode.class.getPackage().getName());

	public FMLObjectNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (modelObject != null) {
			modelObject.setPrettyPrintDelegate(this);
			modelObject.initializeDeserialization(getFactory());
		}
	}

	public FMLObjectNode(T aFMLObject, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(aFMLObject, analyzer);

		modelObject.setPrettyPrintDelegate(this);
		preparePrettyPrint(false);
	}

	@Override
	public FMLCompilationUnitSemanticsAnalyzer getSemanticsAnalyzer() {
		return (FMLCompilationUnitSemanticsAnalyzer) super.getSemanticsAnalyzer();
	}

	@Override
	public void setModelObject(T modelObject) {
		super.setModelObject(modelObject);
		modelObject.setPrettyPrintDelegate(this);
	}

}
