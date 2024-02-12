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
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

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

	/**
	 * Lookup the most precised {@link FMLObject} represented by supplied position in actual FML pretty-print of this {@link FMLObjectNode}
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public FMLPrettyPrintable getFMLObjectAtLocation(int row, int col) {
		return getFMLObjectAtLocation(row, col, FMLPrettyPrintable.class);
	}

	/**
	 * Lookup the most precised object of supplied type represented by supplied position in actual FML pretty-print of this
	 * {@link FMLObjectNode}
	 * 
	 * @param <T2>
	 * @param row
	 * @param col
	 * @param objectClass
	 * @return
	 */
	public <T2 extends FMLPrettyPrintable> T2 getFMLObjectAtLocation(int row, int col, Class<T2> objectClass) {
		FMLObjectNode<?, T2, ?> node = getFMLObjectNodeAtLocation(row, col, objectClass);
		if (node != null) {
			return node.getModelObject();
		}
		return null;
	}

	/**
	 * Lookup the most precised object of supplied type represented by supplied position in actual FML pretty-print of this
	 * {@link FMLObjectNode}
	 * 
	 * @param <T2>
	 * @param row
	 * @param col
	 * @param objectClass
	 * @return
	 */
	public <T2 extends FMLPrettyPrintable> FMLObjectNode<?, T2, ?> getFMLObjectNodeAtLocation(int row, int col, Class<T2> objectClass) {
		if (getFragment() == null) {
			return null;
		}
		RawSourcePosition position = getRawSource().new RawSourcePosition(row, col);
		FMLObjectNode<?, T2, ?> nodeAtPosition = searchFMLObjectNodeAtPosition(position, objectClass);
		if (nodeAtPosition != null) {
			return nodeAtPosition;
		}
		return null;
	}

	/**
	 * Internally used to lookup objects
	 * 
	 * @param <T2>
	 * @param position
	 * @param objectClass
	 * @return
	 */
	private <T2 extends FMLPrettyPrintable> FMLObjectNode<?, T2, ?> searchFMLObjectNodeAtPosition(RawSourcePosition position,
			Class<T2> objectClass) {
		if (position.isInside(getFragment())) {
			FMLObjectNode<?, T2, ?> returned = null;
			if (objectClass.isAssignableFrom(getModelObject().getClass())) {
				returned = (FMLObjectNode<?, T2, ?>) this;
			}
			for (P2PPNode<?, ?> p2ppNode : getChildren()) {
				if (p2ppNode instanceof FMLObjectNode) {
					FMLObjectNode<?, ?, ?> child = (FMLObjectNode<?, ?, ?>) p2ppNode;
					FMLObjectNode<?, T2, ?> moreSpecialized = child.searchFMLObjectNodeAtPosition(position, objectClass);
					if (moreSpecialized != null) {
						return moreSpecialized;
					}
				}
			}
			return returned;
		}
		return null;

	}

}
