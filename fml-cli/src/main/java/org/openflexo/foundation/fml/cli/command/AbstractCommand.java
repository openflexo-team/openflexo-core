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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * Represents a command in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCommand {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCommand.class.getPackage().getName());

	private Node node;
	private CommandSemanticsAnalyzer commandSemanticsAnalyzer;

	public AbstractCommand(Node node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		this.node = node;
		this.commandSemanticsAnalyzer = commandSemanticsAnalyzer;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public abstract String toString();

	public CommandSemanticsAnalyzer getCommandSemanticsAnalyzer() {
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
	public Object execute() {
		if (!isValid()) {
			getErrStream().println(invalidCommandReason());
			return null;
		}
		getCommandInterpreter().willExecute(this);
		return null;
	}

	/**
	 * Return boolean indicating if this {@link AbstractCommand} is valid relatively to semantics analysis
	 * 
	 * @return
	 */
	public abstract boolean isValid();

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

		DataBinding<Object> returned = ExpressionFactory.makeDataBinding(expression, getCommandInterpreter(), bdType, type,
				getCommandSemanticsAnalyzer().getModelFactory(), getCommandSemanticsAnalyzer().getTypingSpace(),
				getCommandSemanticsAnalyzer().getFMLBindingFactory());

		// System.out.println(
		// "Build new binding: " + returned + " valid: " + returned.isValid() + " reason: " + returned.invalidBindingReason());

		return returned;
	}

}
