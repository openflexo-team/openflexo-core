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
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.TLidentifier;
import org.openflexo.foundation.fml.parser.node.TUidentifier;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * Either a {@link TLidentifier} or a {@link TUidentifier}
 * 
 * @author sylvain
 * 
 */
public class SimplePathElementNode extends AbstractBindingPathElementNode<Token, SimplePathElement<?>> {

	private IBindingPathElement parent;

	public SimplePathElementNode(Token astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent, Bindable bindable) {
		super(astNode, analyzer, bindable);
		this.parent = parent;
		// buildModelObjectFromAST() was already called, but too early (parent not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public SimplePathElementNode(SimplePathElement<?> bindingPathElement, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(bindingPathElement, analyzer, bindable);
	}

	@Override
	public SimplePathElement<?> buildModelObjectFromAST(Token astNode) {
		if (parent != null && getBindable() != null) {
			SimplePathElement<?> pathElement = getBindingFactory().makeSimplePathElement(parent, astNode.getText(), getBindable());
			return pathElement;
		}
		return null;
		// return new NormalBindingPathElement(astNode.getText());
	}

}
