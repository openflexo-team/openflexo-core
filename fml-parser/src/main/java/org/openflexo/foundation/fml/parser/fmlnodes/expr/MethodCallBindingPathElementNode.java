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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.expr.BindingValue.MethodCallBindingPathElement;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * @author sylvain
 * 
 */
public class MethodCallBindingPathElementNode
		extends AbstractCallBindingPathElementNode<APrimaryMethodInvocation, MethodCallBindingPathElement> {

	private Node identifierNode;

	public MethodCallBindingPathElementNode(APrimaryMethodInvocation astNode, Node identifierNode, MainSemanticsAnalyzer analyser,
			Bindable bindable) {
		super(astNode, analyser, bindable);
		this.identifierNode = identifierNode;
	}

	public MethodCallBindingPathElementNode(MethodCallBindingPathElement bindingPathElement, MainSemanticsAnalyzer analyser,
			Bindable bindable) {
		super(bindingPathElement, analyser, bindable);
	}

	@Override
	public RawSourcePosition getStartLocation() {
		if (identifierNode != null) {
			RawSourceFragment fragment = getFragment(identifierNode);
			if (fragment != null) {
				return fragment.getStartPosition();
			}
			return null;

		}
		return super.getStartLocation();
	}

	@Override
	public MethodCallBindingPathElement buildModelObjectFromAST(APrimaryMethodInvocation astNode) {

		if (getBindable() != null) {
			handleArguments(astNode.getArgumentList());
			String identifier = getLastPathIdentifier(astNode.getPrimary());
			MethodCallBindingPathElement returned = new MethodCallBindingPathElement(identifier, getArguments());
			return returned;
		}
		return null;
	}

	@Override
	public MethodCallBindingPathElementNode deserialize() {
		return this;
	}

}
