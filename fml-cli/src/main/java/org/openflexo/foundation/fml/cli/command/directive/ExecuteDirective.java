/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * 
 * This file is part of openflexo-core, a component of the software infrastructure 
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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.FMLScriptParser;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.parser.node.ACdDirective;
import org.openflexo.foundation.fml.parser.node.AExecuteDirective;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.StringUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Represents #execute directive in FML command-line interpreter
 * 
 * Usage: #execute
 * 
 * @author fledoux
 * 
 */
@ModelEntity
@ImplementationClass(ExecuteDirective.ExecuteDirectiveImpl.class)
@DirectiveDeclaration(keyword = "execute", usage = "execute", description = "Execute a FMLscript in the context", syntax = "execute <fmlscript file path>")
public interface ExecuteDirective extends Directive<AExecuteDirective> {

	public static abstract class ExecuteDirectiveImpl extends DirectiveImpl<AExecuteDirective> implements ExecuteDirective {
		
		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ExecuteDirective.class.getPackage().getName());

		private String scriptPath;
		
		@Override
		public void create(AExecuteDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);
			scriptPath = retrievePath(node.getPath());
		}
		
		@Override
		public String toString() {
			return "execute "  + scriptPath;
		}
		
		public File getScriptFile() {
			File scriptFile;
			// TODO Unix only
			if (scriptPath.startsWith("/")) {
				scriptFile = new File(scriptPath);
			} else {
				scriptFile = new File(getCommandInterpreter().getWorkingDirectory(), scriptPath);
			}
			return scriptFile;
		}
		
		@Override
		public boolean isSyntaxicallyValid() {
			return StringUtils.isNotEmpty(scriptPath);
		}
		
		@Override
		public String invalidCommandReason() {
			if (scriptPath == null) {
				return "No directory";
			}
			// Check if it's a file			
			else if (getScriptFile().exists() && getScriptFile().isFile()) {
				return "Cannot find script file: " + getScriptFile().getName();
			}			
			return null;
		}

		@Override
		public Object execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();
			try {
				FMLScriptParser parser = new FMLScriptParser();
				FileInputStream scriptStream = new FileInputStream(getScriptFile());
				FMLScript script = parser.parse(scriptStream, getCommandInterpreter().getModelFactory(), getCommandInterpreter());
				script.execute();
			} catch (ParseException e) {
				getOutStream().println("Error during script parsing : " + e.getMessage());
			} catch (FileNotFoundException e) {
				getOutStream().println("Script file was not found : " + e.getMessage());
			} catch (IOException e) {
				getOutStream().println("Error during script execution : " + e.getMessage());
			} catch (ModelDefinitionException e) {
				getOutStream().println("Model definitnio error during script execution : " + e.getMessage());
			}

			return getCommandInterpreter().getWorkingDirectory();
		}
	}
}
