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

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.APwdDirective;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents #pwd directive in FML command-line interpreter
 * 
 * Usage: #pwd
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(PwdDirective.PwdDirectiveImpl.class)
@DirectiveDeclaration(keyword = "pwd", usage = "pwd", description = "Print working directory", syntax = "pwd")
public interface PwdDirective extends Directive<APwdDirective> {

	public static abstract class PwdDirectiveImpl extends DirectiveImpl<APwdDirective> implements PwdDirective {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PwdDirective.class.getPackage().getName());

		@Override
		public String toString() {
			return "pwd";
		}

		@Override
		public File execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();

			String cmdOutput = getCommandInterpreter().getWorkingDirectory().getAbsolutePath();

			output.add(cmdOutput);
			getOutStream().println(cmdOutput);
			return getCommandInterpreter().getWorkingDirectory();
		}
	}
}
