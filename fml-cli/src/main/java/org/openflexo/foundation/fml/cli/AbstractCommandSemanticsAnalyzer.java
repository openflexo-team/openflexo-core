/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.cli;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
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
import org.openflexo.foundation.fml.cli.command.fml.FMLAssignation;
import org.openflexo.foundation.fml.cli.command.fml.FMLContextCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLExpression;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ObjectNode;
import org.openflexo.foundation.fml.parser.node.AActivateTaDirective;
import org.openflexo.foundation.fml.parser.node.AAssignmentExpression;
import org.openflexo.foundation.fml.parser.node.ACdDirective;
import org.openflexo.foundation.fml.parser.node.AContextFmlCommand;
import org.openflexo.foundation.fml.parser.node.AEnterDirective;
import org.openflexo.foundation.fml.parser.node.AExitDirective;
import org.openflexo.foundation.fml.parser.node.AExprFmlCommand;
import org.openflexo.foundation.fml.parser.node.AHelpDirective;
import org.openflexo.foundation.fml.parser.node.AHistoryDirective;
import org.openflexo.foundation.fml.parser.node.ALoadDirective;
import org.openflexo.foundation.fml.parser.node.ALsDirective;
import org.openflexo.foundation.fml.parser.node.AMoreDirective;
import org.openflexo.foundation.fml.parser.node.AOpenDirective;
import org.openflexo.foundation.fml.parser.node.APwdDirective;
import org.openflexo.foundation.fml.parser.node.AQuitDirective;
import org.openflexo.foundation.fml.parser.node.AResourcesDirective;
import org.openflexo.foundation.fml.parser.node.AServiceDirective;
import org.openflexo.foundation.fml.parser.node.AServicesDirective;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * Base class implements the main semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCommandSemanticsAnalyzer extends FMLSemanticsAnalyzer {

	private static final Logger logger = Logger.getLogger(AbstractCommandSemanticsAnalyzer.class.getPackage().getName());

	private AbstractCommandInterpreter commandInterpreter;

	public AbstractCommandSemanticsAnalyzer(AbstractCommandInterpreter commandInterpreter, Start tree) {
		super(commandInterpreter.getModelFactory(), tree);
		this.commandInterpreter = commandInterpreter;
	}

	@Override
	public FMLCompilationUnitSemanticsAnalyzer getCompilationUnitAnalyzer() {
		return null;
	}

	public AbstractCommandInterpreter getCommandInterpreter() {
		return commandInterpreter;
	}

	public PrintStream getOutStream() {
		return getCommandInterpreter().getOutStream();
	}

	public PrintStream getErrStream() {
		return getCommandInterpreter().getErrStream();
	}

	@Override
	public FMLCompilationUnit getCompilationUnit() {
		return null;
	}

	@Override
	public Start getRootNode() {
		return (Start) super.getRootNode();
	}

	@Override
	public <N extends Node, FMLN extends ObjectNode<?, ?, ?>> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function) {
		return null;
	}

	@Override
	public void throwIssue(String errorMessage, RawSourceFragment fragment, RawSourcePosition startPosition) {
		getErrStream().println(errorMessage + " at " + startPosition);
	}

	@Override
	public List<SemanticAnalysisIssue> getSemanticAnalysisIssues() {
		return null;
	}

	protected abstract void registerCommand(Node n, AbstractCommand command);

	@Override
	public void outAPwdDirective(APwdDirective node) {
		super.outAPwdDirective(node);
		registerCommand(node, new PwdDirective(node, this));
	}

	@Override
	public void outALsDirective(ALsDirective node) {
		super.outALsDirective(node);
		registerCommand(node, new LsDirective(node, this));
	}

	@Override
	public void outACdDirective(ACdDirective node) {
		super.outACdDirective(node);
		registerCommand(node, new CdDirective(node, this));
	}

	@Override
	public void outAHistoryDirective(AHistoryDirective node) {
		super.outAHistoryDirective(node);
		registerCommand(node, new HistoryDirective(node, this));
	}

	@Override
	public void outAServicesDirective(AServicesDirective node) {
		super.outAServicesDirective(node);
		registerCommand(node, new ServicesDirective(node, this));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void outAServiceDirective(AServiceDirective node) {
		super.outAServiceDirective(node);
		registerCommand(node, new ServiceDirective(node, this));
	}

	@Override
	public void outAActivateTaDirective(AActivateTaDirective node) {
		super.outAActivateTaDirective(node);
		registerCommand(node, new ActivateTA(node, this));
	}

	@Override
	public void outAResourcesDirective(AResourcesDirective node) {
		super.outAResourcesDirective(node);
		registerCommand(node, new ResourcesDirective(node, this));
	}

	@Override
	public void outAOpenDirective(AOpenDirective node) {
		super.outAOpenDirective(node);
		registerCommand(node, new OpenProject(node, this));
	}

	@Override
	public void outALoadDirective(ALoadDirective node) {
		super.outALoadDirective(node);
		registerCommand(node, new LoadResource(node, this));
	}

	@Override
	public void outAMoreDirective(AMoreDirective node) {
		super.outAMoreDirective(node);
		registerCommand(node, new MoreDirective(node, this));
	}

	@Override
	public void outAEnterDirective(AEnterDirective node) {
		super.outAEnterDirective(node);
		registerCommand(node, new EnterDirective(node, this));
	}

	@Override
	public void outAExitDirective(AExitDirective node) {
		super.outAExitDirective(node);
		registerCommand(node, new ExitDirective(node, this));
	}

	@Override
	public void outAQuitDirective(AQuitDirective node) {
		super.outAQuitDirective(node);
		registerCommand(node, new QuitDirective(node, this));
	}

	@Override
	public void outAHelpDirective(AHelpDirective node) {
		super.outAHelpDirective(node);
		registerCommand(node, new HelpDirective(node, this));
	}

	// COMMANDS

	@Override
	public void outAContextFmlCommand(AContextFmlCommand node) {
		super.outAContextFmlCommand(node);
		registerCommand(node, new FMLContextCommand(node, this));
	}

	@Override
	public void outAExprFmlCommand(AExprFmlCommand node) {
		super.outAExprFmlCommand(node);

		// ASTDebugger.debug(node.getExpression());

		if (node.getExpression() instanceof AAssignmentExpression) {
			registerCommand(node, new FMLAssignation((AAssignmentExpression) node.getExpression(), this));
		}
		else {
			registerCommand(node, new FMLExpression(node, this));
		}
	}

}
