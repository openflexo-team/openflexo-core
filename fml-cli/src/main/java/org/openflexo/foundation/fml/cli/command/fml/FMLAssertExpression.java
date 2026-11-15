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

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AAssertFmlCommand;
import org.openflexo.foundation.fml.rt.error.FMLTypeError;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents an expression in FML command-line interpreter
 * 
 * Usage: <expression>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLAssertExpression.FMLAssertExpressionImpl.class)
@FMLCommandDeclaration(
		keyword = "",
		usage = "assert <expression>",
		description = "Execute expression and assert that the result is true",
		syntax = "assert <expression>")
public interface FMLAssertExpression extends FMLCommand<AAssertFmlCommand> {

	public static abstract class FMLAssertExpressionImpl extends FMLCommandImpl<AAssertFmlCommand> implements FMLAssertExpression {
		private static final Logger logger = Logger.getLogger(FMLAssertExpression.class.getPackage().getName());

		private DataBinding<?> expression;

		@Override
		public void create(AAssertFmlCommand node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			// Expression exp = commandSemanticsAnalyzer.getExpression(node.getExpression());
			// expression = new DataBinding<>(exp.toString(), getCommandInterpreter(), Object.class, BindingDefinitionType.GET);

			// System.out.println("----------------> On traite ASSERT " + node.getExpression());

			expression = retrieveExpression(node.getExpression());

		}

		@Override
		public String toString() {
			return "assert " + expression.toString();
		}

		@Override
		public boolean isSyntaxicallyValid() {
			// We provide dynamic typing
			return expression != null /*&& expression.isValid() && TypeUtils.isBoolean(expression.getAnalyzedType())*/;
		}

		@Override
		public String invalidCommandReason() {
			if (expression == null) {
				return "null expression";
			}
			if (!expression.isValid()) {
				return expression.invalidBindingReason();
			}
			if (!TypeUtils.isBoolean(expression.getAnalyzedType())) {
				return "expression cannot be evaluated as a boolean";
			}
			return null;
		}

		@Override
		public Object execute() throws FMLCommandExecutionException {

			super.execute();
			output.clear();
			String cmdOutput;

			// We provide dynamic typing
			// The expression may be valid now, so we revalidate
			// TODO: this should have been automatically handled by Connie with the observable scheme and should be no more necessary
			if (!expression.isValid()) {
				expression.revalidate();
			}

			if (isSyntaxicallyValid()) {
				Object value;
				try {
					value = expression.getBindingValue(getCommandInterpreter());
				} catch (Exception e) {
					// Not printed: an FML failure is the failure of the command (CORE-F-11)
					cmdOutput = "Cannot execute " + this;

					output.add(cmdOutput);
					throw evaluationFailure(cmdOutput, expression, e);
				}
				cmdOutput = "Executed " + expression + " <- " + value;

				output.add(cmdOutput);
				getOutStream().println(cmdOutput);
				if (Boolean.TRUE.equals(value)) {
					return true;
				}
				if (value != null && !(value instanceof Boolean)) {
					cmdOutput = "Cannot execute " + this + ": " + expression + " is not a boolean but " + value;

					output.add(cmdOutput);
					throw new FMLCommandExecutionException(cmdOutput,
							new FMLTypeError("Assertion " + expression + " evaluates to " + value + ", which is not a boolean"));
				}
				// An expression evaluating to null is a failed assertion, not an internal error
				throw new FMLAssertException(getLine(), expression, getCommandInterpreter());
			}
			throw invalidCommand("Cannot execute " + expression + " : " + expression.invalidBindingReason());
		}
	}
}
