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

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.cli.CLIUtils;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AContextFmlCommand;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents context command in FML command-line interpreter<br>
 * Display current evaluation context
 * 
 * Usage: context
 * 
 * @author sylvain
 * 
 */
@FMLCommandDeclaration(keyword = "context", usage = "context", description = "Display current evaluation context", syntax = "context")
public class FMLContextCommand extends FMLCommand {

	private static final Logger logger = Logger.getLogger(FMLContextCommand.class.getPackage().getName());

	public FMLContextCommand(AContextFmlCommand node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer, null);
	}

	@Override
	public void execute() {
		int maxTypeCols = -1;
		int maxNameCols = -1;

		for (int i = 0; i < getCommandInterpreter().getBindingModel().getBindingVariablesCount(); i++) {
			BindingVariable bv = getCommandInterpreter().getBindingModel().getBindingVariableAt(i);
			String type = "[" + TypeUtils.simpleRepresentation(bv.getType()) + "]";
			String name = bv.getVariableName();
			if (type.length() > maxTypeCols) {
				maxTypeCols = type.length();
			}
			if (name.length() > maxNameCols) {
				maxNameCols = name.length();
			}
		}

		for (int i = 0; i < getCommandInterpreter().getBindingModel().getBindingVariablesCount(); i++) {
			BindingVariable bv = getCommandInterpreter().getBindingModel().getBindingVariableAt(i);
			String type = "[" + TypeUtils.simpleRepresentation(bv.getType()) + "]";
			String name = bv.getVariableName();

			getOutStream().println(type + StringUtils.buildWhiteSpaceIndentation(maxTypeCols - type.length()) + " "
					+ StringUtils.buildWhiteSpaceIndentation(maxNameCols - name.length()) + name + " = "
					+ CLIUtils.denoteObject(getCommandInterpreter().getValue(bv)));
		}
	}
}
