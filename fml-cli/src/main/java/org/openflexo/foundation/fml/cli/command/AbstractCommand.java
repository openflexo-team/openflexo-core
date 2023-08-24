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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.ScriptSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExecuteDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.HelpDirective;
import org.openflexo.foundation.fml.cli.command.directive.HistoryDirective;
import org.openflexo.foundation.fml.cli.command.directive.LoadResource;
import org.openflexo.foundation.fml.cli.command.directive.LsDirective;
import org.openflexo.foundation.fml.cli.command.directive.MoreDirective;
import org.openflexo.foundation.fml.cli.command.directive.OpenProject;
import org.openflexo.foundation.fml.cli.command.directive.PwdDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.foundation.fml.cli.command.directive.ResourcesDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServiceDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServicesDirective;
import org.openflexo.foundation.fml.cli.command.fml.FMLActionCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertExpression;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssignation;
import org.openflexo.foundation.fml.cli.command.fml.FMLContextCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLExpression;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Represents a command in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractCommand.AbstractCommandImpl.class)
@Imports({ @Import(FlexoEvent.class), @Import(value = HelpDirective.class), @Import(HistoryDirective.class), @Import(CdDirective.class),
		@Import(PwdDirective.class), @Import(LsDirective.class), @Import(QuitDirective.class), @Import(ServicesDirective.class),
		@Import(ServiceDirective.class), @Import(ActivateTA.class), @Import(ResourcesDirective.class), @Import(OpenProject.class),
		@Import(LoadResource.class), @Import(MoreDirective.class), @Import(EnterDirective.class), @Import(ExitDirective.class),
		@Import(FMLContextCommand.class), @Import(FMLExpression.class), @Import(FMLAssignation.class), @Import(FMLAssertExpression.class),
		@Import(FMLActionCommand.class), @Import(ExecuteDirective.class) })
public interface AbstractCommand<N extends Node> extends Bindable, FMLControlGraphOwner {

	@PropertyIdentifier(type = Node.class)
	public static final String NODE_KEY = "node";
	@PropertyIdentifier(type = AbstractCommandSemanticsAnalyzer.class)
	public static final String COMMAND_SEMANTICS_ANALYZER_KEY = "commandSemanticsAnalyzer";
	@PropertyIdentifier(type = AbstractCommand.class)
	public static final String PARENT_COMMAND_KEY = "parentCommand";
	@PropertyIdentifier(type = FMLScript.class)
	public static final String SCRIPT_KEY = "script";
	@PropertyIdentifier(type = Integer.class)
	public static final String LINE_KEY = "line";

	List<String> output = new ArrayList<>();

	@Initializer
	void create(@Parameter(NODE_KEY) N node,
			@Parameter(COMMAND_SEMANTICS_ANALYZER_KEY) AbstractCommandSemanticsAnalyzer scriptSemanticsAnalyzer);

	public void init();

	@Getter(value = NODE_KEY, ignoreType = true)
	public N getNode();

	@Getter(value = COMMAND_SEMANTICS_ANALYZER_KEY, ignoreType = true)
	public AbstractCommandSemanticsAnalyzer getCommandSemanticsAnalyzer();

	@Getter(PARENT_COMMAND_KEY)
	public AbstractCommand<?> getParentCommand();

	@Setter(PARENT_COMMAND_KEY)
	public void setParentCommand(AbstractCommand<?> aCommand);

	@Getter(SCRIPT_KEY)
	public FMLScript getScript();

	@Setter(SCRIPT_KEY)
	public void setScript(FMLScript aScript);

	@Getter(value = LINE_KEY, defaultValue = "-1")
	public int getLine();

	@Setter(LINE_KEY)
	public void setLine(int line);

	public Object execute() throws FMLCommandExecutionException;

	public AbstractCommandInterpreter getCommandInterpreter();

	public BindingModel getInferedBindingModel();

	public String getOriginalCommandAsString();

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

	List<String> getOutput();

	public static abstract class AbstractCommandImpl<N extends Node> extends FlexoObjectImpl implements AbstractCommand<N> {

		@SuppressWarnings("unused")
		static final Logger logger = Logger.getLogger(AbstractCommand.class.getPackage().getName());

		private boolean wasInitialized = false;

		@Override
		public void init() {
			wasInitialized = true;
		}

		@Override
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
		@Override
		public Object execute() throws FMLCommandExecutionException {
			output.clear();

			if (!wasInitialized) {
				init();
			}
			if (!isValidInThatContext()) {
				String cmdOutput = invalidCommandReason();

				output.add(cmdOutput);
				throw new FMLCommandExecutionException(cmdOutput);
			}

			getCommandInterpreter().willExecute(this);
			return null;
		}

		@Override
		public String getOriginalCommandAsString() {
			if (getCommandSemanticsAnalyzer() instanceof ScriptSemanticsAnalyzer) {
				return getCommandSemanticsAnalyzer().getText(getNode());
			}
			return getNode().toString();
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
			return getCommandInterpreter().getBindingModel();
		}

		@Override
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

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		@Override
		public void reduce() {
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {
		}

		public List<String> getOutput(){
			return output;
		}
	}
}
