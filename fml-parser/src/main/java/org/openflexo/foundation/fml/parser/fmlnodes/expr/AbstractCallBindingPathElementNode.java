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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.expr.BindingValue.AbstractBindingPathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;

/**
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCallBindingPathElementNode<N extends Node, BPE extends AbstractBindingPathElement>
		extends AbstractBindingPathElementNode<N, BPE> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCallBindingPathElementNode.class.getPackage().getName());

	private Bindable bindable;
	private List<Expression> args;

	public AbstractCallBindingPathElementNode(N astNode, MainSemanticsAnalyzer analyser, Bindable bindable) {
		super(astNode, analyser, bindable);
		this.bindable = bindable;
	}

	public AbstractCallBindingPathElementNode(BPE bindingPathElement, MainSemanticsAnalyzer analyser, Bindable bindable) {
		super(bindingPathElement, analyser, bindable);
		this.bindable = bindable;
	}

	public List<Expression> getArguments() {
		return args;
	}

	protected void handleArguments(PArgumentList argumentList) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleArguments(l.getArgumentList());
			handleArgument(l.getExpression());
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleArgument(((AOneArgumentList) argumentList).getExpression());
		}
	}

	protected void handleArgument(PExpression expression) {

		DataBindingNode dataBindingNode = getAnalyser().retrieveFMLNode(expression, n -> new DataBindingNode(n, getAnalyser()));
		addToChildren(dataBindingNode);

		Expression argValue = ExpressionFactory.makeExpression(expression, bindable, getAnalyser(), dataBindingNode);
		if (args == null) {
			args = new ArrayList<>();
		}

		args.add(argValue);

	}

	protected String serializeArguments(List<Expression> arguments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			sb.append((i > 0 ? "," : "") + arguments.get(i).toString());
		}
		return sb.toString();
	}

}
