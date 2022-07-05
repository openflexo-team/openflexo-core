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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;

public class AddTempResourceCenter implements ServiceOperation<FlexoResourceCenterService> {

	@Override
	public String getOperationName() {
		return "add_temp_rc";
	}

	@Override
	public String usage(FlexoResourceCenterService service) {
		return "service " + service.getServiceName() + " add_temp_rc";
	}

	@Override
	public String description() {
		return "create a sandbox resource center, and change directory into it";
	}

	@Override
	public String getArgument() {
		return null;
	}

	@Override
	public String getSyntax(FlexoResourceCenterService service) {
		return "service " + service.getServiceName() + " " + getOperationName();
	}

	@Override
	public void execute(FlexoResourceCenterService service, PrintStream out, PrintStream err, Object argument, Map<String, ?> options) {
		/*AbstractCommandInterpreter commandInterpreter = (AbstractCommandInterpreter) options.get("commandInterpreter");
		if (argument instanceof DirectoryResourceCenter) {
			// System.out.println("On change pour [" + ((DirectoryResourceCenter) argument).getRootDirectory().getAbsolutePath() + "]");
			commandInterpreter.setWorkingDirectory(((DirectoryResourceCenter) argument).getRootDirectory());
			// System.out.println("Hop: [" + commandInterpreter.getWorkingDirectory().getAbsolutePath() + "]");
		}
		else {
			err.println("Cannot cd to " + argument);
		}*/
		System.out.println("Hop");

		try {
			File tempFile = File.createTempFile("Temp", "");
			File testResourceCenterDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "TestResourceCenter");
			tempFile.delete();
			testResourceCenterDirectory.mkdirs();

			out.println("Add ResourceCenter from directory " + testResourceCenterDirectory);
			DirectoryResourceCenter newRC;
			newRC = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(testResourceCenterDirectory, service);
			service.addToResourceCenters(newRC);
			out.println("ResourceCenter has been registered");

			AbstractCommandInterpreter commandInterpreter = (AbstractCommandInterpreter) options.get("commandInterpreter");
			commandInterpreter.setWorkingDirectory(testResourceCenterDirectory);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getStringRepresentation(Object argumentValue) {
		return getOperationName();
	}

}
