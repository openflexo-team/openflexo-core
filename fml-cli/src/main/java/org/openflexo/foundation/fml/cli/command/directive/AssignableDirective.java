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

package org.openflexo.foundation.fml.cli.command.directive;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.ACommandAssign;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PCommandAssign;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents a {@link Directive} whose value can be assigned
 * 
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AssignableDirective.AssignableDirectiveImpl.class)
public interface AssignableDirective<N extends Node> extends Directive<N> {

	public void create(N node, PCommandAssign assignNode, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer);

	public static abstract class AssignableDirectiveImpl<N extends Node> extends DirectiveImpl<N> implements AssignableDirective<N> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(AssignableDirective.class.getPackage().getName());

		private DataBinding<?> assignation;

		// This is the local BindingModel augmented with new variable declaration if required
		private BindingModel bindingModel;
		private BindingVariable localDeclarationVariable;

		@Override
		public void create(N node, PCommandAssign assignNode, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			if (assignNode instanceof ACommandAssign) {
				ACommandAssign commandAssign = (ACommandAssign) assignNode;
				assignation = retrieveAssignation(commandAssign.getLeftHandSide());
				System.out.println("assignation=" + assignation);
			}
		}

		@Override
		public void init() {
			super.init();
			if (assignation != null) {
				System.out.println("On declare l'assignation de type : " + getAssignableType());
				assignation.setDeclaredType(getAssignableType());
				if (assignation.isNewVariableDeclaration()) {
					if (getScript() != null) {
						bindingModel = new BindingModel(getBindingModel());
						localDeclarationVariable = new BindingVariable(getAssignationVariable(), getAssignableType());
						bindingModel.addToBindingVariables(localDeclarationVariable);
					}
					// Too early
					/*else {
						localDeclarationVariable = getCommandInterpreter().declareVariable(getAssignationVariable(), getAssignableType());
					}*/
				}
			}
		}

		public String getAssignToString() {
			if (assignation != null) {
				return assignation.toString() + " = ";
			}
			return "";
		}

		public String getAssignationVariable() {
			if (assignation.isSimpleVariable()) {
				BindingPath bindingPath = (BindingPath) assignation.getExpression();
				return bindingPath.getBindingVariable().getVariableName();
			}
			return null;
		}

		@Override
		public BindingModel getInferedBindingModel() {
			if (bindingModel != null) {
				return bindingModel;
			}
			return super.getInferedBindingModel();
		}

		public abstract Type getAssignableType();

		protected abstract Object performExecute() throws FMLCommandExecutionException;

		@Override
		public final Object execute() throws FMLCommandExecutionException {
			super.execute();

			Object assignedValue = performExecute();

			if (assignation != null) {
				if (assignation.isValid()) {
					try {
						assignation.setBindingValue(assignedValue, getCommandInterpreter());
						getOutStream().println("Assigned " + assignedValue + " to " + assignation);
					} catch (TypeMismatchException e) {
						throw new FMLCommandExecutionException("Cannot execute " + assignation, e);
					} catch (NullReferenceException e) {
						throw new FMLCommandExecutionException("Cannot execute " + assignation, e);
					} catch (ReflectiveOperationException e) {
						throw new FMLCommandExecutionException("Cannot execute " + assignation, e);
					} catch (NotSettableContextException e) {
						throw new FMLCommandExecutionException("Cannot execute " + assignation, e);
					}
				}
				else if (assignation.isNewVariableDeclaration() || getParentCommand() == null) {
					if (localDeclarationVariable == null) {
						localDeclarationVariable = getCommandInterpreter().declareVariable(getAssignationVariable(), getAssignableType());
					}
					getCommandInterpreter().setVariableValue(localDeclarationVariable, assignedValue);
					getOutStream().println("Declared new variable " + localDeclarationVariable.getVariableName() + "=" + assignedValue);
				}
			}
			return assignedValue;

		}
	}
}
