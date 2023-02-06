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

import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.WrappedFMLObject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * @author sylvain
 * 
 */
public abstract class AbstractFMLPropertyValueNode<N extends Node, P extends FMLPropertyValue<M, T>, M extends FMLObject, T>
		extends FMLObjectNode<N, P, FMLCompilationUnitSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractFMLPropertyValueNode.class.getPackage().getName());

	public AbstractFMLPropertyValueNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public AbstractFMLPropertyValueNode(P property, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(property, analyzer);
	}

	@Override
	public AbstractFMLPropertyValueNode<N, P, M, T> deserialize() {
		// System.out.println("J'arrive la avec " + getASTNode());
		// System.out.println("getParent().getModelObject()=" + getParent().getModelObject());
		// System.out.println("property:" + getModelObject().getProperty());
		// System.out.println("class: " + getModelObject().getImplementedInterface());

		if (getModelObject().getProperty() != null) {
			if (getParent().getModelObject() instanceof WrappedFMLObject) {
				WrappedFMLObject<M> wrappedObject = (WrappedFMLObject<M>) getParent().getModelObject();
				wrappedObject.getObject().addToFMLPropertyValues(getModelObject());
				getModelObject().setObject(wrappedObject.getObject());
				getModelObject().applyPropertyValueToModelObject();
			}
			else {
				((M) getParent().getModelObject()).addToFMLPropertyValues(getModelObject());
				getModelObject().setObject((M) getParent().getModelObject());
				getModelObject().applyPropertyValueToModelObject();
			}
		}
		else {
			logger.warning("Ignore property " + getASTNode() + " since it cannot be mapped to any FMLProperty");
			// We add it anyway to notify the developer
			((M) getParent().getModelObject()).addToFMLPropertyValues(getModelObject());
			getModelObject().setObject((M) getParent().getModelObject());
		}
		// System.out.println("Tiens faudrait appliquer la propriete " + getModelObject() + " a " + getParent().getModelObject());
		return this;
	}

}
