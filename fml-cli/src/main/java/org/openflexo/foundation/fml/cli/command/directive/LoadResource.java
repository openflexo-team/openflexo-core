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

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.ALoadDirective;
import org.openflexo.foundation.fml.parser.node.APathLoadDirective;
import org.openflexo.foundation.fml.parser.node.AResourceLoadDirective;
import org.openflexo.foundation.fml.parser.node.PLoadDirective;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents load resource directive in FML command-line interpreter
 * 
 * Usage: load <resource> where <resource> represents a resource
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(LoadResource.LoadResourceImpl.class)
@DirectiveDeclaration(
		keyword = "load",
		usage = "[declaration = ] load <file> | -r <resource>",
		description = "Load resource denoted by supplied resource uri",
		syntax = "load <path> | -r <resource>")
public interface LoadResource extends AssignableDirective<ALoadDirective> {

	public static abstract class LoadResourceImpl extends AssignableDirectiveImpl<ALoadDirective> implements LoadResource {
		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(LoadResource.class.getPackage().getName());

		private FlexoResource<?> resource;
		private String resourcePath;

		@Override
		public void create(ALoadDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			create(node, node.getCommandAssign(), commandSemanticsAnalyzer);

			PLoadDirective loadDirective = node.getLoadDirective();

			if (loadDirective instanceof AResourceLoadDirective) {
				resource = retrieveResource(((AResourceLoadDirective) loadDirective).getReferenceByUri());
			}
			else if (loadDirective instanceof APathLoadDirective) {
				resourcePath = retrievePath(((APathLoadDirective) loadDirective).getPath());
			}
		}

		@Override
		public boolean isSyntaxicallyValid() {
			return super.isSyntaxicallyValid();
		}

		@Override
		public Type getAssignableType() {
			if (getResultingResource() != null) {
				Class<?> rdClass = getResultingResource().getResourceDataClass();
				if (rdClass.equals(FMLCompilationUnit.class)) {
					return VirtualModel.class;
				}
				return rdClass;
			}
			return null;
		}

		@Override
		public String toString() {
			if (StringUtils.isNotEmpty(resourcePath)) {
				return getAssignToString() + "load " + resourcePath;
			}
			else if (resource != null) {
				return getAssignToString() + "load -r [\"" + resource.getURI() + "\"]";
			}
			return getAssignToString() + "load";
		}

		public FlexoResource<?> getResultingResource() {
			if (StringUtils.isNotEmpty(resourcePath)) {
				return retrieveResourceFromPath(resourcePath);
			}
			else if (resource != null) {
				return resource;
			}
			return null;
		}

		@Override
		public Object performExecute() throws FMLCommandExecutionException {
			output.clear();
			String cmdOutput;

			logger.info("Load resource " + getResultingResource() + " from currentPath=" + getCommandInterpreter().getWorkingDirectory());
			if (getResultingResource() == null) {
				cmdOutput =  "Cannot access resource, resource=" + resource + " resourcePath=" + resourcePath;

				output.add(cmdOutput);
				throw new FMLCommandExecutionException(cmdOutput);
			}

			if (getResultingResource().isLoaded()) {
				cmdOutput =  "Resource " + resource.getURI() + " already loaded";

				output.add(cmdOutput);
				getOutStream().println(cmdOutput);

				Object returned = getResultingResource().getLoadedResourceData();
				if (returned instanceof FMLCompilationUnit) {
					return ((FMLCompilationUnit) returned).getVirtualModel();
				}
				return returned;
			}
			else {
				try {
					Object returned = getResultingResource().getResourceData();
					cmdOutput 		= "Loaded " + getResultingResource().getURI();

					output.add(cmdOutput);
					getOutStream().println(cmdOutput);
					if (returned instanceof FMLCompilationUnit) {
						return ((FMLCompilationUnit) returned).getVirtualModel();
					}
					return returned;
				} catch (FileNotFoundException e) {
					cmdOutput = "Cannot find resource";

					output.add(cmdOutput);
					throw new FMLCommandExecutionException(cmdOutput, e);
				} catch (ResourceLoadingCancelledException e) {
					cmdOutput = "Operation cancelled";

					output.add(cmdOutput);
					throw new FMLCommandExecutionException(e);
				} catch (FlexoException e) {
					cmdOutput = "Cannot load resource";

					output.add(cmdOutput);
					throw new FMLCommandExecutionException(cmdOutput, e);
				}
			}
		}
	}
}
