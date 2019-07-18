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
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.DisplayResource;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.HelpDirective;
import org.openflexo.foundation.fml.cli.command.directive.HistoryDirective;
import org.openflexo.foundation.fml.cli.command.directive.LoadResource;
import org.openflexo.foundation.fml.cli.command.directive.LsDirective;
import org.openflexo.foundation.fml.cli.command.directive.OpenProject;
import org.openflexo.foundation.fml.cli.command.directive.PwdDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.foundation.fml.cli.command.directive.ResourcesDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServiceDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServicesDirective;
import org.openflexo.foundation.fml.cli.parser.node.ABindingPath;
import org.openflexo.foundation.fml.cli.parser.node.ACall;
import org.openflexo.foundation.fml.cli.parser.node.ACallBinding;
import org.openflexo.foundation.fml.cli.parser.node.ADotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.AIdentifierBinding;
import org.openflexo.foundation.fml.cli.parser.node.APathPath;
import org.openflexo.foundation.fml.cli.parser.node.ATail1Binding;
import org.openflexo.foundation.fml.cli.parser.node.ATail2Binding;
import org.openflexo.foundation.fml.cli.parser.node.Node;
import org.openflexo.foundation.fml.cli.parser.node.PBinding;
import org.openflexo.foundation.fml.cli.parser.node.PPath;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents a directive in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
@DeclareDirectives({ @DeclareDirective(value = HelpDirective.class), @DeclareDirective(HistoryDirective.class),
		@DeclareDirective(CdDirective.class), @DeclareDirective(PwdDirective.class), @DeclareDirective(LsDirective.class),
		@DeclareDirective(QuitDirective.class), @DeclareDirective(ServicesDirective.class), @DeclareDirective(ServiceDirective.class),
		@DeclareDirective(ActivateTA.class), @DeclareDirective(ResourcesDirective.class), @DeclareDirective(OpenProject.class),
		@DeclareDirective(LoadResource.class), @DeclareDirective(DisplayResource.class), @DeclareDirective(EnterDirective.class),
		@DeclareDirective(ExitDirective.class) })
public abstract class Directive extends AbstractCommand {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Directive.class.getPackage().getName());

	public Directive(Node node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer);
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
		else if (path instanceof ABindingPath) {
			return retrievePathFromBinding(((ABindingPath) path).getBinding());
		}
		else if (path instanceof APathPath) {
			return retrievePathFromBinding(((APathPath) path).getBinding()) + File.separator + retrievePath(((APathPath) path).getPath());
		}
		return null;
	}

	private String retrievePathFromBinding(PBinding binding) {
		return binding.toString();
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

	protected Object evaluate(PBinding value, CommandTokenType tokenType) {
		System.out.println("On doit evaluer " + value);
		System.out.println("En tant que " + tokenType);

		switch (tokenType) {
			case Expression:
				return null;
			case LocalReference:
				String valueAsString = getText(value);
				System.out.println("Hop: " + valueAsString);
				File referencedFile = new File(getCommandInterpreter().getWorkingDirectory(), valueAsString);
				System.out.println("Fichier ? " + referencedFile);
				if (referencedFile != null && referencedFile.exists()) {
					System.out.println("existe ? " + (referencedFile != null ? referencedFile.exists() : "???"));
					ResourceManager rm = getCommandInterpreter().getServiceManager().getResourceManager();
					List<FlexoResource<?>> resources = rm.getResources(referencedFile);
					System.out.println("found: " + resources);
					if (resources.size() > 0) {
						try {
							return resources.get(0).getResourceData();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ResourceLoadingCancelledException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FlexoException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return null;
			case Path:
				return null;
			case Service:
				return null;
			case Operation:
				return null;
			case TA:
				return null;
			case RC:
				return null;
			case Resource:
				return null;
		}

		/*if (valueType.equals("<path>") && pDirectiveOption instanceof APathDirectiveOption) {
			return new File(getCommandInterpreter().getWorkingDirectory(),
					retrievePath(((APathDirectiveOption) pDirectiveOption).getPath()));
		}
		if (valueType.equals("<ta>") && pDirectiveOption instanceof APathDirectiveOption) {
			String taName = retrievePath(((APathDirectiveOption) pDirectiveOption).getPath());
			for (TechnologyAdapter ta : getCommandInterpreter().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (ta.getClass().getSimpleName().equals(taName)) {
					return ta;
				}
			}
		}
		return pDirectiveOption.toString();*/

		System.out.println("On retourne rien");
		return null;
	}

	public String getText(PBinding binding) {
		if (binding instanceof AIdentifierBinding) {
			return ((AIdentifierBinding) binding).getIdentifier().getText();
		}
		else if (binding instanceof ACallBinding) {
			return getText((ACall) ((ACallBinding) binding).getCall());
		}
		else if (binding instanceof ATail1Binding) {
			return ((ATail1Binding) binding).getIdentifier().getText() + "." + getText(((ATail1Binding) binding).getBinding());
		}
		else if (binding instanceof ATail2Binding) {
			return getText((ACall) ((ATail2Binding) binding).getCall()) + "." + getText(((ATail2Binding) binding).getBinding());
		}
		return null;
	}

	public String getText(ACall call) {
		StringBuffer sb = new StringBuffer();
		sb.append(call.getIdentifier());
		sb.append("(");
		sb.append("not_implemented_yet");
		sb.append(")");
		return sb.toString();
	}
}
