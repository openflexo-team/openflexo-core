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

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.expr.FMLCastExpression;
import org.openflexo.foundation.fml.parser.AbstractExpressionFactory;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AAsRelationalExp;
import org.openflexo.foundation.fml.parser.node.ACastFmlUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ACastPrimitiveUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ACastReferenceUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * A cast. The grammar has three forms (CORE-F-5): a primitive type {@code (int) x}, a technology-specific type {@code (Foo(a=1)) x}, and
 * a parenthesed expression {@code (Foo) x}, whose expression must be a (qualified) name of a type: that is checked here. The form
 * {@code x as T} takes any type, generic ones included
 * 
 * @author sylvain
 */
public class FMLCastExpressionNode extends ExpressionNode<Node, FMLCastExpression> {

	public FMLCastExpressionNode(Node astNode, AbstractExpressionFactory expressionFactory) {
		super(astNode, expressionFactory);
	}

	public FMLCastExpressionNode(FMLCastExpression expression, AbstractExpressionFactory expressionFactory) {
		super(expression, expressionFactory);
	}

	@Override
	public FMLCastExpression buildModelObjectFromAST(Node astNode) {
		return new FMLCastExpression(null, null);
	}

	@Override
	public FMLCastExpressionNode deserialize() {
		Node astNode = getASTNode();
		Type type = null;
		Node operand = null;
		if (astNode instanceof ACastPrimitiveUnaryExpNotPlusMinus) {
			type = TypeFactory.makeType(((ACastPrimitiveUnaryExpNotPlusMinus) astNode).getPrimitiveType(),
					getExpressionFactory().getTypingSpace());
			operand = ((ACastPrimitiveUnaryExpNotPlusMinus) astNode).getUnaryExp();
		}
		else if (astNode instanceof ACastReferenceUnaryExpNotPlusMinus) {
			ACastReferenceUnaryExpNotPlusMinus castNode = (ACastReferenceUnaryExpNotPlusMinus) astNode;
			type = TypeFactory.makeTypeFromExpression(castNode.getExpression(), getExpressionFactory().getTypingSpace());
			if (type == null) {
				throwIssue("Not a type: " + getText(castNode.getExpression()), getFragment(castNode.getExpression()));
			}
			operand = castNode.getUnaryExpNotPlusMinus();
		}
		else if (astNode instanceof AAsRelationalExp) {
			type = TypeFactory.makeType(((AAsRelationalExp) astNode).getType(), getExpressionFactory().getTypingSpace());
			operand = ((AAsRelationalExp) astNode).getShiftExp();
		}
		else if (astNode instanceof ACastFmlUnaryExpNotPlusMinus) {
			type = TypeFactory.makeTechnologySpecificType((ACastFmlUnaryExpNotPlusMinus) astNode, getExpressionFactory().getTypingSpace());
			operand = ((ACastFmlUnaryExpNotPlusMinus) astNode).getUnaryExpNotPlusMinus();
		}
		getModelObject().setCastType(type);
		getModelObject().setArgument(getExpressionFactory().getExpression(operand));
		super.deserialize();
		return this;
	}

	private RawSourceFragment getTypeFragment() {
		Node astNode = getASTNode();
		if (astNode instanceof ACastPrimitiveUnaryExpNotPlusMinus) {
			return getFragment(((ACastPrimitiveUnaryExpNotPlusMinus) astNode).getPrimitiveType());
		}
		if (astNode instanceof ACastReferenceUnaryExpNotPlusMinus) {
			return getFragment(((ACastReferenceUnaryExpNotPlusMinus) astNode).getExpression());
		}
		if (astNode instanceof AAsRelationalExp) {
			return getFragment(((AAsRelationalExp) astNode).getType());
		}
		if (astNode instanceof ACastFmlUnaryExpNotPlusMinus) {
			ACastFmlUnaryExpNotPlusMinus castNode = (ACastFmlUnaryExpNotPlusMinus) astNode;
			return getFragment(castNode.getType(), castNode.getRpar1());
		}
		return null;
	}

	@Override
	protected void prepareParsedPrettyPrint() {
		// The type keeps the text written by the user (a qualified name, for instance) while it does not change
		if (getASTNode() instanceof AAsRelationalExp) {
			appendOperand(() -> getModelObject().getArgument());
			appendLeaf(() -> serializeType(getModelObject().getCastType()), getTypeFragment());
		}
		else {
			appendLeaf(() -> serializeType(getModelObject().getCastType()), getTypeFragment());
			appendOperand(() -> getModelObject().getArgument());
		}
	}

}
