/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Gina-core, a component of the software infrastructure 
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

import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.ScriptSemanticsAnalyzer;
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
import org.openflexo.foundation.fml.cli.command.fml.FMLActionCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertExpression;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssignation;
import org.openflexo.foundation.fml.cli.command.fml.FMLContextCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLExpression;
import org.openflexo.foundation.fml.parser.node.AActivateTaDirective;
import org.openflexo.foundation.fml.parser.node.AAssertFmlCommand;
import org.openflexo.foundation.fml.parser.node.AAssignmentExpression;
import org.openflexo.foundation.fml.parser.node.ACdDirective;
import org.openflexo.foundation.fml.parser.node.AContextFmlCommand;
import org.openflexo.foundation.fml.parser.node.AEnterDirective;
import org.openflexo.foundation.fml.parser.node.AExitDirective;
import org.openflexo.foundation.fml.parser.node.AExpressionFmlCommand;
import org.openflexo.foundation.fml.parser.node.AFmlActionFmlCommand;
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
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

public class FMLScriptModelFactory extends PamelaModelFactory {

	public FMLScriptModelFactory() throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(FMLScript.class));
	}

	public FMLScript newFMLScript(Node node, ScriptSemanticsAnalyzer scriptSemanticsAnalyzer) {
		return newInstance(FMLScript.class, node, scriptSemanticsAnalyzer);
	}

	public ActivateTA newActivateTA(AActivateTaDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(ActivateTA.class, node, semanticsAnalyzer);
	}

	public CdDirective newCdDirective(ACdDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(CdDirective.class, node, semanticsAnalyzer);
	}

	public EnterDirective newEnterDirective(AEnterDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(EnterDirective.class, node, semanticsAnalyzer);
	}

	public ExitDirective newExitDirective(AExitDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(ExitDirective.class, node, semanticsAnalyzer);
	}

	public HelpDirective newHelpDirective(AHelpDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(HelpDirective.class, node, semanticsAnalyzer);
	}

	public HistoryDirective newHistoryDirective(AHistoryDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(HistoryDirective.class, node, semanticsAnalyzer);
	}

	public LoadResource newLoadResource(ALoadDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(LoadResource.class, node, semanticsAnalyzer);
	}

	public LsDirective newLsDirective(ALsDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(LsDirective.class, node, semanticsAnalyzer);
	}

	public MoreDirective newMoreDirective(AMoreDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(MoreDirective.class, node, semanticsAnalyzer);
	}

	public OpenProject newOpenProject(AOpenDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(OpenProject.class, node, semanticsAnalyzer);
	}

	public PwdDirective newPwdDirective(APwdDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(PwdDirective.class, node, semanticsAnalyzer);
	}

	public QuitDirective newQuitDirective(AQuitDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(QuitDirective.class, node, semanticsAnalyzer);
	}

	public ResourcesDirective newResourcesDirective(AResourcesDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(ResourcesDirective.class, node, semanticsAnalyzer);
	}

	public ServiceDirective<?> newServiceDirective(AServiceDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(ServiceDirective.class, node, semanticsAnalyzer);
	}

	public ServicesDirective newServicesDirective(AServicesDirective node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(ServicesDirective.class, node, semanticsAnalyzer);
	}

	public FMLExpression newFMLExpression(AExpressionFmlCommand node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(FMLExpression.class, node, semanticsAnalyzer);
	}

	public FMLAssignation newFMLAssignation(AAssignmentExpression node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(FMLAssignation.class, node, semanticsAnalyzer);
	}

	public FMLAssertExpression newFMLAssertExpression(AAssertFmlCommand node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(FMLAssertExpression.class, node, semanticsAnalyzer);
	}

	public FMLContextCommand newFMLContextCommand(AContextFmlCommand node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(FMLContextCommand.class, node, semanticsAnalyzer);
	}

	public FMLActionCommand newFMLActionCommand(AFmlActionFmlCommand node, AbstractCommandSemanticsAnalyzer semanticsAnalyzer) {
		return newInstance(FMLActionCommand.class, node, semanticsAnalyzer);
	}

}
