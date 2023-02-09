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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.CommandTokenType;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AServiceDirective;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents #service directive in FML command-line interpreter
 * 
 * Allows to perform actions on services
 * 
 * Usage: #service <service_name> action
 * 
 * where action can be:
 * <ul>
 * <li>status: display status of service</li>
 * <li>start: start the service</li>
 * <li>stop: stop the service</li>
 * <li>other action depending on adressed service</li>
 * <li>help: display all actions available on this service</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ServiceDirective.ServiceDirectiveImpl.class)
@DirectiveDeclaration(
		keyword = "service",
		usage = "service <service> operation [options]",
		description = "Execute operation of a given service, type service <service_name> usage to get help",
		syntax = "service <service> <operation>")
public interface ServiceDirective<S extends FlexoService> extends Directive<AServiceDirective> {

	public static abstract class ServiceDirectiveImpl<S extends FlexoService> extends DirectiveImpl<AServiceDirective>
			implements ServiceDirective<S> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ServiceDirective.class.getPackage().getName());

		private S service;
		private ServiceOperation<S> serviceOperation;
		private boolean isValid;
		private String invalidCommandReason = null;
		private Object argumentValue = null;
		private Map<String, Object> optionValues = new HashMap<>();

		@SuppressWarnings({ "unchecked" })
		@Override
		public void create(AServiceDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			service = getCommandInterpreter().getServiceManager().getService(getText(node.getServiceName()));

			if (service != null) {
				for (ServiceOperation<?> action : service.getAvailableServiceOperations()) {
					if (action.getOperationName().equals(getText(node.getAction()))) {
						serviceOperation = (ServiceOperation<S>) action;
						break;
					}
				}
				if (serviceOperation == null) {
					isValid = false;
					invalidCommandReason = "Operation " + getText(node.getAction()) + " not found for service " + service.getServiceName();
				}
				else {
					// argument
					if (serviceOperation.getArgument() != null) {
						// An argument is required
						if (node.getArgument() != null) {
							String argumentType = serviceOperation.getArgument();
							CommandTokenType tokenType = CommandTokenType.getType(argumentType);
							argumentValue = evaluateArgument(node.getArgument(), tokenType);
							if (argumentValue != null) {
								isValid = true;
							}
							else {
								isValid = false;
								invalidCommandReason = "Operation " + getText(node.getAction()) + " cannot be processed: null argument";
							}
						}
						else {
							isValid = false;
							invalidCommandReason = "Operation " + getText(node.getAction()) + " cannot be processed: missing argument";
						}
					}
					else {
						if (node.getArgument() != null) {
							isValid = false;
							invalidCommandReason = "Operation " + getText(node.getAction()) + " cannot be processed: unexpected argument";
						}
						else {
							isValid = true;
						}
					}

					// options
					// TODO
					/*if (serviceOperation.getOptions() != null) {
					// if (serviceOperation.getOptions().size() != node.getOptions().size()) {
					if (serviceOperation.getOptions().size() != node.getOptions().size()) {
						invalidCommandReason = "Operation " + node.getAction().getText() + " cannot be processed: wrong arguments number";
					}
					else {
						int index = 0;
						PDirectiveOption pDirectiveOption = node.getOptions();
						// for (PDirectiveOption pDirectiveOption : node.getOptions()) {
						String optionType = serviceOperation.getOptions().get(index);
						Object optionValue = makeOption(pDirectiveOption, optionType);
						((List) options).add(optionValue);
						index++;
						// }
					}
					}*/
				}

				/*getOutStream().println("Service: " + service);
				getOutStream().println("Action: " + serviceOperation);
				for (PDirectiveOption pDirectiveOption : node.getOptions()) {
				getOutStream().println(" > " + pDirectiveOption);
				}*/

			}
			else {
				isValid = false;
				invalidCommandReason = "Service " + getText(node.getServiceName()) + " not found";
			}
		}

		@Override
		public String toString() {
			return "service " + service.getServiceName() + " " + serviceOperation.getStringRepresentation(argumentValue);
		}

		@Override
		public boolean isSyntaxicallyValid() {
			return service != null && serviceOperation != null && isValid;
		}

		@Override
		public String invalidCommandReason() {
			return invalidCommandReason;
		}

		@Override
		public S execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();

			if (isSyntaxicallyValid()) {
				optionValues.put("commandInterpreter", getCommandInterpreter());
				serviceOperation.execute(service, getOutStream(), getErrStream(), argumentValue, optionValues);
				return service;
			}

			String cmdOutput = invalidCommandReason();

			output.add(cmdOutput);
			throw new FMLCommandExecutionException(cmdOutput);
		}
	}
}
