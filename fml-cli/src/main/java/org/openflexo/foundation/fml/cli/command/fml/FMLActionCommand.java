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

import java.util.Iterator;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.parser.node.AFmlActionFmlCommand;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents an EditionAction in FML command-line interpreter
 * 
 * Usage: <expression>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLActionCommand.FMLActionCommandImpl.class)
@FMLCommandDeclaration(keyword = "", usage = "<action>", description = "Execute FML action", syntax = "<action>")
public interface FMLActionCommand extends FMLCommand<AFmlActionFmlCommand> {

	public static abstract class FMLActionCommandImpl extends FMLCommandImpl<AFmlActionFmlCommand> implements FMLActionCommand {

		private static final Logger logger = Logger.getLogger(FMLActionCommand.class.getPackage().getName());

		private EditionAction editionAction;

		private boolean isSyntaxicallyValid = true;
		private String invalidCommandReason = "is valid";

		@Override
		public void create(AFmlActionFmlCommand node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			editionAction = retrieveEditionAction(node.getFmlActionExp());
			editionAction.setOwner(this);

		}

		@Override
		public void init() {
			super.init();
			checkValididity();
		}

		private void checkValididity() {
			try {
				ValidationReport validateReport = getValidationModel().validate(editionAction);
				Iterator<ValidationError<?, ? super EditionAction>> iterator = validateReport.errorIssuesRegarding(editionAction)
						.iterator();
				if (iterator.hasNext()) {
					ValidationError<?, ? super EditionAction> error = iterator.next();
					String localizedMessage = getValidationModel().localizedIssueMessage(error);
					String localizedMessageDetails = getValidationModel().localizedIssueDetailedInformations(error);
					isSyntaxicallyValid = false;
					invalidCommandReason = localizedMessage
							+ (StringUtils.isNotEmpty(localizedMessageDetails) ? " : " + localizedMessageDetails : "");
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return editionAction.getFMLPrettyPrint();
		}

		@Override
		public boolean isSyntaxicallyValid() {
			return isSyntaxicallyValid;
		}

		@Override
		public String invalidCommandReason() {
			return invalidCommandReason;
		}

		@Override
		public Object execute() throws FMLCommandExecutionException {

			super.execute();
			output.clear();

			try {
				return editionAction.execute(getCommandInterpreter());
			} catch (ReturnException e) {
				e.printStackTrace();
				throw new FMLCommandExecutionException(e);
			} catch (FlexoException e) {
				e.printStackTrace();
				throw new FMLCommandExecutionException(e);
			}

		}
	}
}
