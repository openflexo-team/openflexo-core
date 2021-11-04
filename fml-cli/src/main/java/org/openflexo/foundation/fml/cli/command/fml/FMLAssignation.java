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
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.parser.node.AAssignmentExpression;

/**
 * Represents an assignation in FML command-line interpreter
 * 
 * Usage: variable:=<expression>
 * 
 * @author sylvain
 * 
 */
@FMLCommandDeclaration(
		keyword = "",
		usage = "variable=<expression>",
		description = "Assign an expression to a variable",
		syntax = "variable=<expression>")
public class FMLAssignation extends FMLCommand {

	private static final Logger logger = Logger.getLogger(FMLAssignation.class.getPackage().getName());

	private DataBinding<?> assignation;
	private DataBinding<?> expression;

	public FMLAssignation(AAssignmentExpression node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer, null);

		assignation = retrieveAssignation(node.getLeft());
		expression = retrieveExpression(node.getRight());

	}

	@Override
	public String toString() {
		return assignation + " = " + expression;
	}

	public DataBinding<?> getAssignation() {
		return assignation;
	}

	@Override
	public boolean isValid() {
		return assignation != null && (assignation.isValid() || assignation.isNewVariableDeclaration()) && expression != null
				&& expression.isValid();
	}

	@Override
	public String invalidCommandReason() {
		if (assignation == null) {
			return "null assignation";
		}
		if (expression == null) {
			return "null expression";
		}
		if (!assignation.isValid() && !assignation.isNewVariableDeclaration()) {
			return assignation.invalidBindingReason();
		}
		if (!expression.isValid()) {
			return expression.invalidBindingReason();
		}
		return null;
	}

	@Override
	public Object execute() {
		super.execute();

		/*if (!assignation.isValid() 
				&& assignation.isBindingValue() 
				&& ((BindingValue)assignation.getExpression()))*/

		Object assignedValue = null;

		if (expression.isValid()) {
			try {
				assignedValue = expression.getBindingValue(getCommandInterpreter());
			} catch (TypeMismatchException e) {
				getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
				return null;
			} catch (NullReferenceException e) {
				getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
				return null;
			} catch (ReflectiveOperationException e) {
				getErrStream().println("Cannot execute " + expression + " : " + e.getMessage());
				return null;
			}
		}
		else {
			getErrStream().println("Cannot execute " + expression + " : " + expression.invalidBindingReason());
		}

		if (assignation.isValid()) {
			try {
				assignation.setBindingValue(assignedValue, getCommandInterpreter());
			} catch (TypeMismatchException e) {
				getErrStream().println("Cannot execute " + assignation + " : " + e.getMessage());
				return assignedValue;
			} catch (NullReferenceException e) {
				getErrStream().println("Cannot execute " + assignation + " : " + e.getMessage());
				return assignedValue;
			} catch (ReflectiveOperationException e) {
				getErrStream().println("Cannot execute " + assignation + " : " + e.getMessage());
				return assignedValue;
			} catch (NotSettableContextException e) {
				getErrStream().println("Cannot execute " + assignation + " : not settable binding");
				return assignedValue;
			}
			getOutStream().println("Assigned " + assignedValue + " to " + assignation);
		}
		else if (assignation.isNewVariableDeclaration()) {
			BindingValue bindingPath = (BindingValue) assignation.getExpression();
			getCommandInterpreter().declareVariable(bindingPath.getBindingVariable().getVariableName(), expression.getAnalyzedType(),
					assignedValue);
			getOutStream().println("Declared new variable " + bindingPath.getBindingVariable().getVariableName() + "=" + assignedValue);
		}

		return assignedValue;

		// getOutStream().println("SET " + variableName + " = " + value);
		// getCommandInterpreter().declareVariable(variableName, assignation.getAnalyzedType(), value);

	}
}
