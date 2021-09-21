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

import org.openflexo.foundation.fml.expr.FMLConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ArithmeticConstant;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.node.AFloatingPointLiteral;

/**
 * @author sylvain
 * 
 */
public class FloatingPointConstantNode extends ConstantNode<AFloatingPointLiteral, ArithmeticConstant<?>> {

	public FloatingPointConstantNode(AFloatingPointLiteral astNode, ExpressionFactory expressionFactory) {
		super(astNode, expressionFactory);
	}

	public FloatingPointConstantNode(ArithmeticConstant<Integer> constant, ExpressionFactory expressionFactory) {
		super(constant, expressionFactory);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ArithmeticConstant<?> buildModelObjectFromAST(AFloatingPointLiteral astNode) {
		Number value = null;
		String valueText = astNode.getLitFloat().getText();
		if (valueText.endsWith("F") || valueText.endsWith("f")) {
			valueText = valueText.substring(0, valueText.length() - 1);
			try {
				value = Float.parseFloat(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if (valueText.endsWith("D") || valueText.endsWith("d")) {
			valueText = valueText.substring(0, valueText.length() - 1);
			try {
				value = Double.parseDouble(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				value = Double.parseDouble(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return (ArithmeticConstant) FMLConstant.makeConstant(value);
	}

}
