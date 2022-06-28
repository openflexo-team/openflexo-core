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

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
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

	// This is the local BindingModel augmented with new variable declaration if required
	private BindingModel bindingModel;
	private BindingVariable localDeclarationVariable;

	public FMLAssignation(AAssignmentExpression node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer);

		assignation = retrieveAssignation(node.getLeft());
		expression = retrieveExpression(node.getRight());

	}

	@Override
	public void init() {
		super.init();
		if (assignation.isNewVariableDeclaration()) {
			if (getParentCommand() != null) {
				bindingModel = new BindingModel(getBindingModel());
				localDeclarationVariable = new BindingVariable(getAssignationVariable(), expression.getAnalyzedType());
				bindingModel.addToBindingVariables(localDeclarationVariable);
			}
			else {
				localDeclarationVariable = getCommandInterpreter().declareVariable(getAssignationVariable(), expression.getAnalyzedType());
			}
		}
	}

	@Override
	public BindingModel getInferedBindingModel() {
		if (bindingModel != null) {
			return bindingModel;
		}
		return super.getInferedBindingModel();
	}

	@Override
	public String toString() {
		return assignation + " = " + expression;
	}

	public DataBinding<?> getAssignation() {
		return assignation;
	}

	@Override
	public boolean isSyntaxicallyValid() {
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
	public Object execute() throws ExecutionException {
		super.execute();

		Object assignedValue = null;

		if (expression.isValid()) {
			try {
				assignedValue = expression.getBindingValue(getCommandInterpreter());
			} catch (Exception e) {
				throw new ExecutionException("Cannot execute " + expression, e);
			}
		}
		else {
			throw new ExecutionException("Cannot execute " + expression + " : " + expression.invalidBindingReason());
		}

		if (assignation.isValid()) {
			try {
				assignation.setBindingValue(assignedValue, getCommandInterpreter());
				getOutStream().println("Assigned " + assignedValue + " to " + assignation);
			} catch (TypeMismatchException e) {
				throw new ExecutionException("Cannot execute " + assignation, e);
			} catch (NullReferenceException e) {
				throw new ExecutionException("Cannot execute " + assignation, e);
			} catch (ReflectiveOperationException e) {
				throw new ExecutionException("Cannot execute " + assignation, e);
			} catch (NotSettableContextException e) {
				throw new ExecutionException("Cannot execute " + assignation, e);
			}
		}
		else if (assignation.isNewVariableDeclaration() || getParentCommand() == null) {
			getCommandInterpreter().setVariableValue(localDeclarationVariable, assignedValue);
			getOutStream().println("Declared new variable " + localDeclarationVariable.getVariableName() + "=" + assignedValue);
		}

		return assignedValue;

	}

	public String getAssignationVariable() {
		if (assignation.isSimpleVariable()) {
			BindingValue bindingPath = (BindingValue) assignation.getExpression();
			return bindingPath.getBindingVariable().getVariableName();
		}
		return null;
	}

}
