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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.parser.node.ALoadDirective;
import org.openflexo.foundation.fml.parser.node.APathLoadDirective;
import org.openflexo.foundation.fml.parser.node.AResourceLoadDirective;
import org.openflexo.foundation.fml.parser.node.PLoadDirective;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents load resource directive in FML command-line interpreter
 * 
 * Usage: load <resource> where <resource> represents a resource
 * 
 * @author sylvain
 * 
 */
@DirectiveDeclaration(
		keyword = "load",
		usage = "load <file> | -r <resource>",
		description = "Load resource denoted by supplied resource uri",
		syntax = "load <path> | -r <resource>")
public class LoadResource extends Directive {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LoadResource.class.getPackage().getName());

	private FlexoResource<?> resource;
	private String resourcePath;

	public LoadResource(ALoadDirective node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer);

		PLoadDirective loadDirective = node.getLoadDirective();

		if (loadDirective instanceof AResourceLoadDirective) {
			resource = retrieveResource(((AResourceLoadDirective) loadDirective).getReferenceByUri());
		}
		else if (loadDirective instanceof APathLoadDirective) {
			resourcePath = retrievePath(((APathLoadDirective) loadDirective).getPath());
		}

	}

	@Override
	public String toString() {
		if (StringUtils.isNotEmpty(resourcePath)) {
			return "load " + resourcePath;
		}
		else if (resource != null) {
			return "load -r [\"" + resource.getURI() + "\"]";
		}
		return "load";
	}

	public FlexoResource<?> getResultingResource() {
		if (StringUtils.isNotEmpty(resourcePath)) {
			File resourceFile;
			if (resourcePath.startsWith("/")) {
				resourceFile = new File(resourcePath);
			}
			else {
				resourceFile = new File(getCommandInterpreter().getWorkingDirectory(), resourcePath);
			}
			try {
				resourceFile = new File(resourceFile.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<FlexoResource<?>> resources = getCommandInterpreter().getServiceManager().getResourceManager().getResources(resourceFile);
			if (resources.size() == 0) {
				getErrStream().println("Cannot load as a resource " + resourceFile);
			}
			else if (resources.size() > 1) {
				getErrStream().println("Multiple resources for " + resourceFile);
				return resources.get(0);
			}
			else {
				return resources.get(0);
			}

		}
		else if (resource != null) {
			return resource;
		}
		return null;
	}

	@Override
	public void execute() {
		super.execute();
		if (getResultingResource().isLoaded()) {
			getOutStream().println("Resource " + resource.getURI() + " already loaded");
		}
		else {
			try {
				getResultingResource().loadResourceData();
				getOutStream().println("Loaded " + getResultingResource().getURI() + ".");
			} catch (FileNotFoundException e) {
				getErrStream().println("Cannot find resource");
			} catch (ResourceLoadingCancelledException e) {
			} catch (FlexoException e) {
				getErrStream().println("Cannot load resource : " + e.getMessage());
			}
		}
	}
}
