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

import org.openflexo.connie.expr.BinaryOperator;
import org.openflexo.foundation.fml.expr.FMLAssignOperator;
import org.openflexo.foundation.fml.expr.FMLBinaryOperatorExpression;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.node.AAssignAssignmentOperator;
import org.openflexo.foundation.fml.parser.node.AAssignmentExpression;
import org.openflexo.foundation.fml.parser.node.APlusAssignAssignmentOperator;

/**
 * @author sylvain
 * 
 */
public class AssignmentExpressionNode extends FMLBinaryOperatorExpressionNode<AAssignmentExpression> {

	public AssignmentExpressionNode(AAssignmentExpression astNode, ExpressionFactory expressionFactory) {
		super(astNode, expressionFactory);
	}

	public AssignmentExpressionNode(FMLBinaryOperatorExpression expression, ExpressionFactory expressionFactory) {
		super(expression, expressionFactory);
	}

	@Override
	public BinaryOperator getOperator() {
		if (getASTNode() != null) {
			// TODO: do for other assign operators
			if (getASTNode().getAssignmentOperator() instanceof AAssignAssignmentOperator) {
				return FMLAssignOperator.ASSIGN;
			}
			else if (getASTNode().getAssignmentOperator() instanceof APlusAssignAssignmentOperator) {
				return FMLAssignOperator.PLUS_ASSIGN;
			}
		}
		if (getModelObject() != null) {
			return getModelObject().getOperator();
		}
		return null;
	}

	@Override
	public AssignmentExpressionNode deserialize() {
		getModelObject().setLeftArgument(getExpressionFactory().getExpression(getASTNode().getLeft()));
		getModelObject().setRightArgument(getExpressionFactory().getExpression(getASTNode().getRight()));
		super.deserialize();
		return this;
	}

}
