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

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceAction;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.parser.node.AServiceDirective;

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
public class ServiceDirective<S extends FlexoService> extends Directive {

	private static final Logger logger = Logger.getLogger(ServiceDirective.class.getPackage().getName());

	private S service;
	private ServiceAction<S> serviceAction;
	private String invalidCommandReason = null;

	@SuppressWarnings("unchecked")
	public ServiceDirective(AServiceDirective node, CommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);

		service = getCommandInterpreter().getServiceManager().getService(node.getServiceName().getText());

		if (service != null) {
			for (ServiceAction<?> action : service.getAvailableServiceActions()) {
				if (action.getActionName().equals(node.getAction().getText())) {
					serviceAction = (ServiceAction<S>) action;
					break;
				}
			}
			if (serviceAction == null) {
				invalidCommandReason = "Action " + node.getAction().getText() + " not found for service " + service.getServiceName();
			}

			/*System.out.println("Service: " + service);
			System.out.println("Action: " + serviceAction);
			for (PDirectiveOption pDirectiveOption : node.getOptions()) {
				System.out.println(" > " + pDirectiveOption);
			}*/

		}
		else {
			invalidCommandReason = "Service " + node.getServiceName().getText() + " not found";
		}
	}

	@Override
	public boolean isValid() {
		return service != null && serviceAction != null;
	}

	@Override
	public String invalidCommandReason() {
		return invalidCommandReason;
	}

	@Override
	public void execute() {
		serviceAction.execute(service);
	}
}
