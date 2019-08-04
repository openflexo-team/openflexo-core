/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.cli.command.fml;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BooleanBinaryOperator;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AExprFmlCommand;

/**
 * Represents an expression in FML command-line interpreter
 * 
 * Usage: <expression>
 * 
 * @author sylvain
 * 
 */
@FMLCommandDeclaration(keyword = "", usage = "<expression>", description = "Execute expression", syntax = "<expression>")
public class FMLExpression extends FMLCommand {

	private static final Logger logger = Logger.getLogger(FMLExpression.class.getPackage().getName());

	private DataBinding<Object> expression;

	public FMLExpression(AExprFmlCommand node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer, null);
		Expression exp = commandSemanticsAnalyzer.getExpression(node.getExpr());
		expression = new DataBinding<>(exp.toString(), getCommandInterpreter(), Object.class, BindingDefinitionType.GET);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String invalidCommandReason() {
		return null;
	}

	@Override
	public void execute() {
		try {
			if (expression.getExpression() != null && expression.getExpression() instanceof BinaryOperatorExpression
					&& ((BinaryOperatorExpression) expression.getExpression()).getOperator() == BooleanBinaryOperator.EQUALS) {
				// Special case for an expression declared as EQUALS
				// Interpret it as an assignation
				BinaryOperatorExpression equalsExp = (BinaryOperatorExpression) expression.getExpression();
				Expression leftArg = equalsExp.getLeftArgument();
				Expression rightArg = equalsExp.getRightArgument();
				DataBinding<Object> left = new DataBinding<>(leftArg.toString(), getCommandInterpreter(), Object.class,
						BindingDefinitionType.GET);
				DataBinding<Object> right = new DataBinding<>(rightArg.toString(), getCommandInterpreter(), Object.class,
						BindingDefinitionType.GET);
				if (right.isValid()) {
					Object value = right.getBindingValue(getCommandInterpreter());
					if (left.isValid()) {
						if (left.isSettable()) {
							// if (leftArg instanceof BindingValue && ((BindingValue) leftArg).getBindingPath().size() == 0) {
							// getCommandInterpreter().declareVariable(variableName, assignation.getAnalyzedType(), value);
							// }
							// else {
							getOutStream().println("SET " + left + " = " + value);
							left.setBindingValue(value, getCommandInterpreter());
							// }
						}
					}
					else {
						// getOutStream().println("Ca va pas avec " + left + " of " + left.getClass());
						if (left.getExpression() instanceof BindingValue
								&& ((BindingValue) left.getExpression()).getParsedBindingPath().size() == 1) {
							String variableName = ((BindingValue) left.getExpression()).getParsedBindingPath().get(0)
									.getSerializationRepresentation();
							// getOutStream().println("variable=" + variableName);
							getOutStream().println("SET " + variableName + " = " + value);
							getCommandInterpreter().declareVariable(variableName, right.getAnalyzedType(), value);
						}
						else {
							getErrStream().println("Cannot execute " + left + " : " + left.invalidBindingReason());
						}
					}
				}
				else {
					getErrStream().println("Cannot execute " + right + " : " + right.invalidBindingReason());
				}
			}
			else {
				if (expression.isValid()) {
					Object value = expression.getBindingValue(getCommandInterpreter());
					getOutStream().println("Executed " + expression + " <- " + value);
				}
				else {
					getErrStream().println("Cannot execute " + expression + " : " + expression.invalidBindingReason());
				}
			}
		} catch (TypeMismatchException e) {
			getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
		} catch (NullReferenceException e) {
			getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
		} catch (InvocationTargetException e) {
			getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
		} catch (NotSettableContextException e) {
			getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
		}
	}
}
