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

package org.openflexo.foundation.fml.cli.command;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.DisplayResource;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.HelpDirective;
import org.openflexo.foundation.fml.cli.command.directive.LoadResource;
import org.openflexo.foundation.fml.cli.command.directive.LsDirective;
import org.openflexo.foundation.fml.cli.command.directive.OpenProject;
import org.openflexo.foundation.fml.cli.command.directive.PwdDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.foundation.fml.cli.command.directive.ResourcesDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServiceDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServicesDirective;
import org.openflexo.foundation.fml.cli.parser.node.ADotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.AFileNamePath;
import org.openflexo.foundation.fml.cli.parser.node.AIdentifierPath;
import org.openflexo.foundation.fml.cli.parser.node.APathDirectiveOption;
import org.openflexo.foundation.fml.cli.parser.node.APathPath;
import org.openflexo.foundation.fml.cli.parser.node.Node;
import org.openflexo.foundation.fml.cli.parser.node.PDirectiveOption;
import org.openflexo.foundation.fml.cli.parser.node.PPath;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents a directive in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
@DeclareDirectives({ @DeclareDirective(value = HelpDirective.class), @DeclareDirective(CdDirective.class),
		@DeclareDirective(PwdDirective.class), @DeclareDirective(LsDirective.class), @DeclareDirective(QuitDirective.class),
		@DeclareDirective(ServicesDirective.class), @DeclareDirective(ServiceDirective.class), @DeclareDirective(ActivateTA.class),
		@DeclareDirective(ResourcesDirective.class), @DeclareDirective(OpenProject.class), @DeclareDirective(LoadResource.class),
		@DeclareDirective(DisplayResource.class), @DeclareDirective(EnterDirective.class), @DeclareDirective(ExitDirective.class) })
public abstract class Directive extends AbstractCommand {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Directive.class.getPackage().getName());

	public Directive(Node node, CommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);
	}

	/*protected String retrieveFileName(PFileName fileName) {
		if (fileName instanceof AIdentifierFileName) {
			return ((AIdentifierFileName) fileName).getIdentifier().getText();
		}
		else if (fileName instanceof ATailFileName) {
			return ((ATailFileName) fileName).getIdentifier().getText() + "." + retrieveFileName(((ATailFileName) fileName).getFileName());
		}
		return null;
	}*/

	protected String retrievePath(PPath path) {
		if (path instanceof ADoubleDotPath) {
			return "..";
		}
		else if (path instanceof ADoubleDotPathPath) {
			return ".." + File.separator + retrievePath(((ADoubleDotPathPath) path).getPath());
		}
		else if (path instanceof ADotPath) {
			return ".";
		}
		else if (path instanceof ADotPathPath) {
			return "." + File.separator + retrievePath(((ADotPathPath) path).getPath());
		}
		else if (path instanceof AFileNamePath) {
			return ((AFileNamePath) path).getFileName().getText();
		}
		else if (path instanceof AIdentifierPath) {
			return ((AIdentifierPath) path).getIdentifier().getText();
		}
		else if (path instanceof APathPath) {
			return ((APathPath) path).getFileName().getText() + File.separator + retrievePath(((APathPath) path).getPath());
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String invalidCommandReason() {
		return null;
	}

	protected String getFileName(File f) {
		if (f == null) {
			return null;
		}
		if (f.isDirectory() && !f.getName().endsWith(VirtualModelResourceFactory.FML_SUFFIX)
				&& !f.getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)
				&& !f.getName().endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX)) {
			return f.getName() + File.separator;
		}
		return f.getName();
	}

	protected TechnologyAdapter getTechnologyAdapter(String technologyAdapterName) {
		for (TechnologyAdapter ta : getCommandInterpreter().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			if (ta.getIdentifier().equals(technologyAdapterName)) {
				return ta;
			}
		}
		return null;
	}

	protected FlexoResourceCenter<?> getResourceCenter(String rcURI) {
		if (rcURI.startsWith("[")) {
			rcURI = rcURI.substring(1);
		}
		if (rcURI.endsWith("]")) {
			rcURI = rcURI.substring(0, rcURI.length() - 1);
		}
		for (FlexoResourceCenter<?> rc : getCommandInterpreter().getServiceManager().getResourceCenterService().getResourceCenters()) {
			if (rc.getDefaultBaseURI().equals(rcURI)) {
				return rc;
			}
		}
		return null;
	}

	protected FlexoResource<?> getResource(String resourceURI) {
		if (resourceURI.startsWith("[")) {
			resourceURI = resourceURI.substring(1);
		}
		if (resourceURI.endsWith("]")) {
			resourceURI = resourceURI.substring(0, resourceURI.length() - 1);
		}
		return getCommandInterpreter().getServiceManager().getResourceManager().getResource(resourceURI);
	}

	protected Object makeOption(PDirectiveOption pDirectiveOption, String optionType) {
		if (optionType.equals("<path>") && pDirectiveOption instanceof APathDirectiveOption) {
			return new File(getCommandInterpreter().getWorkingDirectory(),
					retrievePath(((APathDirectiveOption) pDirectiveOption).getPath()));
		}
		if (optionType.equals("<ta>") && pDirectiveOption instanceof APathDirectiveOption) {
			String taName = retrievePath(((APathDirectiveOption) pDirectiveOption).getPath());
			for (TechnologyAdapter ta : getCommandInterpreter().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (ta.getClass().getSimpleName().equals(taName)) {
					return ta;
				}
			}
		}
		return pDirectiveOption.toString();
	}

}