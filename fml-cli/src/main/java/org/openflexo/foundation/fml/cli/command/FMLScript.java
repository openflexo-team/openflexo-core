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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.ScriptSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a command in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLScript.FMLScriptImpl.class)
public interface FMLScript extends HasPropertyChangeSupport {

	@PropertyIdentifier(type = Node.class)
	public static final String NODE_KEY = "node";
	@PropertyIdentifier(type = ScriptSemanticsAnalyzer.class)
	public static final String SCRIPT_SEMANTICS_ANALYZER_KEY = "scriptSemanticsAnalyzer";
	@PropertyIdentifier(type = AbstractCommand.class, cardinality = Cardinality.LIST)
	public static final String COMMANDS_KEY = "commands";

	@Initializer
	void create(@Parameter(NODE_KEY) Node node, @Parameter(SCRIPT_SEMANTICS_ANALYZER_KEY) ScriptSemanticsAnalyzer scriptSemanticsAnalyzer);

	@Getter(value = NODE_KEY, ignoreType = true)
	public Node getNode();

	@Getter(value = SCRIPT_SEMANTICS_ANALYZER_KEY, ignoreType = true)
	public ScriptSemanticsAnalyzer getScriptSemanticsAnalyzer();

	@Getter(value = COMMANDS_KEY, cardinality = Cardinality.LIST, inverse = AbstractCommand.SCRIPT_KEY)
	public List<AbstractCommand<?>> getCommands();

	@Adder(COMMANDS_KEY)
	public void addToCommands(AbstractCommand<?> aCommand);

	@Remover(COMMANDS_KEY)
	public void removeFromCommands(AbstractCommand<?> aCommand);

	public void execute() throws FMLCommandExecutionException;

	public static abstract class FMLScriptImpl extends FlexoObjectImpl implements FMLScript {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(FMLScript.class.getPackage().getName());

		public AbstractCommandInterpreter getCommandInterpreter() {
			return getScriptSemanticsAnalyzer().getCommandInterpreter();
		}

		public PrintStream getOutStream() {
			return getCommandInterpreter().getOutStream();
		}

		public PrintStream getErrStream() {
			return getCommandInterpreter().getErrStream();
		}

		@Override
		public void addToCommands(AbstractCommand command) {
			AbstractCommand lastCommand = getCommands().size() > 0 ? getCommands().get(getCommands().size() - 1) : null;
			performSuperAdder(COMMANDS_KEY, command);
			if (lastCommand != null) {
				command.setParentCommand(lastCommand);
			}
			command.init();
		}

		/**
		 * Execute this {@link FMLScript}
		 * 
		 * @throws FMLCommandExecutionException
		 * 
		 */
		@Override
		public void execute() throws FMLCommandExecutionException {
			for (AbstractCommand command : getCommands()) {
				logger.info(">>> Execute " + command);
				getOutStream().println(getCommandInterpreter().getPrompt() + " > " + command);
				command.execute();
			}
		}

	}
}
