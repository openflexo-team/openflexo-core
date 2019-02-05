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

import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.PrettyPrintContext;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.toolbox.StringUtils;

/**
 * Specification of the pretty-print of a child object
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class ChildContents<T extends FMLPrettyPrintable> extends PrettyPrintableContents<T> {

	private FMLObjectNode<?, T> childNode;

	public ChildContents(String prelude, FMLObjectNode<?, T> childNode, String postlude, int identationLevel) {
		super(prelude, postlude, identationLevel);
		this.childNode = childNode;
		setFragment(childNode.getLastParsedFragment());
	}

	public FMLObjectNode<?, T> getChildNode() {
		return childNode;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		String childPrettyPrint = childNode.getNormalizedFMLRepresentation(context.derive(getRelativeIndentation()));
		if (StringUtils.isNotEmpty(childPrettyPrint)) {
			if (StringUtils.isNotEmpty(getPrelude())) {
				sb.append(getPrelude());
			}
			sb.append(childPrettyPrint);
			if (StringUtils.isNotEmpty(getPostlude())) {
				sb.append(getPostlude());
			}
		}
		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {
		// System.out.println("> Pour ChildContents " + childNode.getFMLObject() + " c'est plus complique");
		// System.out.println("Et on calcule la nouvelle valeur:");
		// System.out.println(childNode.computeFMLRepresentation(context));
		derivedRawSource.replace(getFragment(), childNode.computeFMLRepresentation(context));
	}

}
