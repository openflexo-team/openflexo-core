/**
 * 
 * Copyright (c) 2022, Openflexo
 * 
 * This file is part of FML-CLI, a component of the software infrastructure 
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
import java.util.Map;

import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;

public class CdResourceCenter implements ServiceOperation<FlexoResourceCenterService> {

	@Override
	public String getOperationName() {
		return "cd_rc";
	}

	@Override
	public String usage(FlexoResourceCenterService service) {
		return "service " + service.getServiceName() + " cd_rc -rc <rc>";
	}

	@Override
	public String description() {
		return "change working directory to the path denoted by supplied resource center";
	}

	@Override
	public String getArgument() {
		return "<rc>";
	}

	/*@Override
	public String getArgumentOption() {
		return "-r";
	}*/

	@Override
	public String getSyntax(FlexoResourceCenterService service) {
		return "service " + service.getServiceName() + " " + getOperationName() + " -rc " + getArgument();
	}

	/*@Override
	public List<ServiceOperationOption> getOptions() {
		return null;
	}*/

	@Override
	public void execute(FlexoResourceCenterService service, PrintStream out, PrintStream err, Object argument, Map<String, ?> options) {
		System.out.println("Prout with " + argument);
		/*if (argument instanceof File) {
			File directory = (File) argument;
			out.println("Add ResourceCenter from directory " + directory);
			DirectoryResourceCenter newRC;
			try {
				newRC = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(directory, service);
				service.addToResourceCenters(newRC);
				out.println("ResourceCenter has been registered");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}

	@Override
	public String getStringRepresentation(Object argumentValue) {
		// return getOperationName() + " -d " + ((File) argumentValue).getAbsolutePath();
		return getOperationName() + "[\"" + ((FlexoResourceCenter<?>) argumentValue).getDefaultBaseURI() + "\"]";
	}

}
