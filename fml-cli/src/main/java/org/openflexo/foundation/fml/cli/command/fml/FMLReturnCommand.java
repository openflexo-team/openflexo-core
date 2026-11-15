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

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AReturnFmlCommand;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A top-level {@code return} in FML command-line interpreter: it ends the script it belongs to, successfully, after the value, if any, is
 * printed. In the interactive interpreter it only prints the value (CORE-F-11)
 * 
 * Usage: return [expression]
 * 
 */
@ModelEntity
@ImplementationClass(FMLReturnCommand.FMLReturnCommandImpl.class)
@FMLCommandDeclaration(
		keyword = "return",
		usage = "return [<expression>]",
		description = "End the script, successfully, with the value of the expression, if any",
		syntax = "return [<expression>]")
public interface FMLReturnCommand extends FMLCommand<AReturnFmlCommand> {

	/** The expression of the returned value, null for a bare return */
	public DataBinding<?> getExpression();

	public static abstract class FMLReturnCommandImpl extends FMLCommandImpl<AReturnFmlCommand> implements FMLReturnCommand {

		private DataBinding<?> expression;

		@Override
		public void create(AReturnFmlCommand node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);
			if (node.getExpression() != null) {
				expression = retrieveExpression(node.getExpression());
			}
		}

		@Override
		public DataBinding<?> getExpression() {
			return expression;
		}

		@Override
		public String toString() {
			return expression != null ? "return " + expression : "return";
		}

		@Override
		public boolean isSyntaxicallyValid() {
			// We provide dynamic typing: the expression is checked again when executed
			return true;
		}

		@Override
		public String invalidCommandReason() {
			return null;
		}

		@Override
		public Object execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();
			if (expression == null) {
				output.add("Returned");
				getOutStream().println("Returned");
				return null;
			}
			if (!expression.isValid()) {
				expression.revalidate();
			}
			if (!expression.isValid()) {
				throw invalidCommand("Cannot execute " + this + " : " + expression.invalidBindingReason());
			}
			Object value;
			try {
				value = expression.getBindingValue(getCommandInterpreter());
			} catch (Exception e) {
				String cmdOutput = "Cannot execute " + this;
				output.add(cmdOutput);
				throw evaluationFailure(cmdOutput, expression, e);
			}
			String cmdOutput = "Returned " + value;
			output.add(cmdOutput);
			getOutStream().println(cmdOutput);
			return value;
		}
	}
}
