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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AServiceDirective;
import org.openflexo.foundation.fml.cli.parser.node.PDirectiveOption;

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
@DirectiveDeclaration(
		keyword = "service",
		usage = "service <service> operation [options]",
		description = "Execute operation of a given service, type service <service_name> usage to get help",
		syntax = "service <service> <operation> <ta>")
public class ServiceDirective<S extends FlexoService> extends Directive {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ServiceDirective.class.getPackage().getName());

	private S service;
	private ServiceOperation<S> serviceOperation;
	private String invalidCommandReason = null;
	private List<?> options = new ArrayList<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ServiceDirective(AServiceDirective node, AbstractCommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);

		service = getCommandInterpreter().getServiceManager().getService(node.getServiceName().getText());

		if (service != null) {
			for (ServiceOperation<?> action : service.getAvailableServiceOperations()) {
				if (action.getOperationName().equals(node.getAction().getText())) {
					serviceOperation = (ServiceOperation<S>) action;
					break;
				}
			}
			if (serviceOperation == null) {
				invalidCommandReason = "Operation " + node.getAction().getText() + " not found for service " + service.getServiceName();
			}
			else {
				// options
				if (serviceOperation.getOptions() != null) {
					if (serviceOperation.getOptions().size() != node.getOptions().size()) {
						invalidCommandReason = "Operation " + node.getAction().getText() + " cannot be processed: wrong arguments number";
					}
					else {
						int index = 0;
						for (PDirectiveOption pDirectiveOption : node.getOptions()) {
							String optionType = serviceOperation.getOptions().get(index);
							Object optionValue = makeOption(pDirectiveOption, optionType);
							((List) options).add(optionValue);
							index++;
						}
					}
				}
			}

			/*getOutStream().println("Service: " + service);
			getOutStream().println("Action: " + serviceOperation);
			for (PDirectiveOption pDirectiveOption : node.getOptions()) {
				getOutStream().println(" > " + pDirectiveOption);
			}*/

		}
		else {
			invalidCommandReason = "Service " + node.getServiceName().getText() + " not found";
		}
	}

	@Override
	public boolean isValid() {
		return service != null && serviceOperation != null
				&& (serviceOperation.getOptions() == null || serviceOperation.getOptions().size() == options.size());
	}

	@Override
	public String invalidCommandReason() {
		return invalidCommandReason;
	}

	@Override
	public void execute() {
		serviceOperation.execute(service, options.toArray(new Object[options.size()]));
	}
}
