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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AHelpDirective;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents #exit directive in FML command-line interpreter
 * 
 * Usage: #exit
 * 
 * @author sylvain
 * 
 */
@DirectiveDeclaration(keyword = "help", usage = "help", description = "Display this help", syntax = "help")
public class HelpDirective extends Directive {

	private static final Logger logger = Logger.getLogger(HelpDirective.class.getPackage().getName());

	public HelpDirective(AHelpDirective node, CommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);
	}

	@Override
	public void execute() {
		for (Class<? extends Directive> directiveClass : getCommandInterpreter().getAvailableDirectives()) {
			String usage = directiveClass.getAnnotation(DirectiveDeclaration.class).usage();
			String description = directiveClass.getAnnotation(DirectiveDeclaration.class).description();
			displayDirectiveHelp(usage, description);
		}
		for (Class<? extends FMLCommand> fmlCommandClass : getCommandInterpreter().getAvailableCommands()) {
			String usage = fmlCommandClass.getAnnotation(FMLCommandDeclaration.class).usage();
			String description = fmlCommandClass.getAnnotation(FMLCommandDeclaration.class).description();
			displayFMLCommandHelp(usage, description);
		}
	}

	private void displayDirectiveHelp(String usage, String description) {
		System.out.println(usage + StringUtils.buildWhiteSpaceIndentation(40 - usage.length()) + ": " + description);
	}

	private void displayFMLCommandHelp(String usage, String description) {
		System.out.println(usage + StringUtils.buildWhiteSpaceIndentation(40 - usage.length()) + ": " + description);
	}
}
