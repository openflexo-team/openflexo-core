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

package org.openflexo.foundation.fml.cli.command;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.ScriptSemanticsAnalyzer;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Represents a command in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCommand extends PropertyChangedSupportDefaultImplementation implements Bindable {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCommand.class.getPackage().getName());

	private Node node;
	private AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer;
	// This variable references parent (previous) command if this command is part of a script, otherwise it is null
	private AbstractCommand parentCommand;

	private boolean wasInitialized = false;

	public AbstractCommand(Node node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		this.node = node;
		this.commandSemanticsAnalyzer = commandSemanticsAnalyzer;
	}

	public void init() {
		wasInitialized = true;
	}

	public Node getNode() {
		return node;
	}

	/**
	 * Return bindable which defines binding model.<br>
	 * If this command is part of a script, this is the previous command, otherwise this is the command interpreter itself
	 * 
	 * @return
	 */
	/*public Bindable getBindable() {
		if (getParentCommand() != null) {
			return getParentCommand();
		}
		return getCommandInterpreter();
	}*/

	public AbstractCommand getParentCommand() {
		return parentCommand;
	}

	public void setParentCommand(AbstractCommand parentCommand) {
		if ((parentCommand == null && this.parentCommand != null) || (parentCommand != null && !parentCommand.equals(this.parentCommand))) {
			AbstractCommand oldValue = this.parentCommand;
			this.parentCommand = parentCommand;
			getPropertyChangeSupport().firePropertyChange("parentCommand", oldValue, parentCommand);
		}
	}

	@Override
	public abstract String toString();

	public AbstractCommandSemanticsAnalyzer getCommandSemanticsAnalyzer() {
		return commandSemanticsAnalyzer;
	}

	public AbstractCommandInterpreter getCommandInterpreter() {
		return getCommandSemanticsAnalyzer().getCommandInterpreter();
	}

	public PrintStream getOutStream() {
		return getCommandInterpreter().getOutStream();
	}

	public PrintStream getErrStream() {
		return getCommandInterpreter().getErrStream();
	}

	/**
	 * Execute this {@link AbstractCommand}
	 * 
	 * @return (eventual) returned value after execution
	 */
	public Object execute() throws ExecutionException {
		if (!wasInitialized) {
			init();
		}
		if (!isValidInThatContext()) {
			getErrStream().println(invalidCommandReason());
			return null;
		}
		getCommandInterpreter().willExecute(this);
		return null;
	}

	public String getOriginalCommandAsString() {
		if (getCommandSemanticsAnalyzer() instanceof ScriptSemanticsAnalyzer) {
			return getCommandSemanticsAnalyzer().getText(getNode());
		}
		return getNode().toString();
	}

	/**
	 * Return boolean indicating if this {@link AbstractCommand} is syntaxically valid
	 * 
	 * @return
	 */
	public abstract boolean isSyntaxicallyValid();

	/**
	 * Return boolean indicating if this {@link AbstractCommand} is valid in that run-time context
	 * 
	 * @return
	 */
	public abstract boolean isValidInThatContext();

	/**
	 * Return String indicating why this {@link AbstractCommand} is not valid relatively to semantics analysis<br>
	 * Undetermined value if {@link AbstractCommand} is valid
	 * 
	 * @return
	 */
	public abstract String invalidCommandReason();

	public enum CommandTokenType {
		Expression {
			@Override
			public String syntaxKeyword() {
				return "<expression>";
			}
		},
		/*LocalReference {
			@Override
			public String syntaxKeyword() {
				return "<reference>";
			}
		},*/
		Path {
			@Override
			public String syntaxKeyword() {
				return "<path>";
			}
		},
		TA {
			@Override
			public String syntaxKeyword() {
				return "<ta>";
			}
		},
		RC {
			@Override
			public String syntaxKeyword() {
				return "<rc>";
			}
		},
		Resource {
			@Override
			public String syntaxKeyword() {
				return "<resource>";
			}
		},
		Service {
			@Override
			public String syntaxKeyword() {
				return "<service>";
			}
		},
		Operation {
			@Override
			public String syntaxKeyword() {
				return "<operation>";
			}
		};

		public abstract String syntaxKeyword();

		public static CommandTokenType getType(String syntaxKeyword) {
			for (CommandTokenType commandTokenType : values()) {
				if (commandTokenType.syntaxKeyword().equals(syntaxKeyword)) {
					return commandTokenType;
				}
			}
			logger.warning("Unexpected CommandTokenType: " + syntaxKeyword);
			return null;
		}
	}

	protected DataBinding<?> retrieveAssignation(Node expression) {
		return retrieveExpression(expression, Object.class, BindingDefinitionType.GET_SET);
	}

	protected DataBinding<?> retrieveExpression(Node expression) {
		return retrieveExpression(expression, Object.class, BindingDefinitionType.GET);
	}

	protected DataBinding<?> retrieveExpression(Node expression, Type type, BindingDefinitionType bdType) {

		DataBinding<Object> returned = ExpressionFactory.makeDataBinding(expression, this, bdType, type,
				getCommandSemanticsAnalyzer().getModelFactory(), getCommandSemanticsAnalyzer().getTypingSpace(),
				getCommandSemanticsAnalyzer().getFMLBindingFactory());

		// System.out.println(
		// "Build new binding: " + returned + " valid: " + returned.isValid() + " reason: " + returned.invalidBindingReason());

		return returned;
	}

	protected EditionAction retrieveEditionAction(PFmlActionExp fmlAction) {

		FMLCompilationUnitSemanticsAnalyzer analyzer = new FMLCompilationUnitSemanticsAnalyzer(
				getCommandSemanticsAnalyzer().getModelFactory(), getCommandSemanticsAnalyzer().getServiceManager(), fmlAction,
				getCommandSemanticsAnalyzer().getRawSource());

		ControlGraphNode<?, ?> controlGraphNode = ControlGraphFactory.makeControlGraphNode(fmlAction, analyzer);

		if (controlGraphNode != null && controlGraphNode.getModelObject() instanceof EditionAction) {
			return (EditionAction) controlGraphNode.getModelObject();
		}
		return null;

	}

	@Override
	public BindingModel getBindingModel() {
		if (getParentCommand() != null) {
			return getParentCommand().getInferedBindingModel();
		}
		/*
		System.out.println("Je suis " + this);
		if (getBindable() instanceof AbstractCommand) {
			System.out.println("Mon bindable c'est " + getBindable());
			System.out.println("Je retourne " + ((AbstractCommand) getBindable()).getInferedBindingModel());
			return ((AbstractCommand) getBindable()).getInferedBindingModel();
		}*/
		return getCommandInterpreter().getBindingModel();
	}

	public BindingModel getInferedBindingModel() {
		return getBindingModel();
	}

	@Override
	public BindingFactory getBindingFactory() {
		return getCommandInterpreter().getBindingFactory();
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
	}

	public static class ExecutionException extends Exception {

		public ExecutionException(String message) {
			super(message);
		}

		public ExecutionException(String message, Throwable cause) {
			super(message + " : " + cause.getMessage(), cause);
		}

		public ExecutionException(Throwable cause) {
			super("ExecutionException caused by " + cause.getMessage());
		}

	}

}
