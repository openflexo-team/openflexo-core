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
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.ARcResourcesDirective;
import org.openflexo.foundation.fml.parser.node.AResourcesDirective;
import org.openflexo.foundation.fml.parser.node.ATaRcResourcesDirective;
import org.openflexo.foundation.fml.parser.node.ATaResourcesDirective;
import org.openflexo.foundation.fml.parser.node.PResourcesDirective;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterGlobalRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents resources directive in FML command-line interpreter
 * 
 * Allows to display list of resources for a particular technology adapter and/or resource center
 * 
 * Usage: resources [<technology_adapter>] [<resource_center>]
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
@ImplementationClass(ResourcesDirective.ResourcesDirectiveImpl.class)
@DirectiveDeclaration(
		keyword = "resources",
		usage = "resources <ta>|* [<rc>]",
		description = "Display list of resources for a particular technology adapter and/or resource center",
		syntax = "resources <ta> <rc>")
public interface ResourcesDirective extends Directive<AResourcesDirective> {

	public TechnologyAdapter<?> getTechnologyAdapter();

	public FlexoResourceCenter<?> getResourceCenter();

	public static abstract class ResourcesDirectiveImpl extends DirectiveImpl<AResourcesDirective> implements ResourcesDirective {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ResourcesDirective.class.getPackage().getName());

		private TechnologyAdapter<?> technologyAdapter;
		private FlexoResourceCenter<?> resourceCenter;

		@Override
		public void create(AResourcesDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			PResourcesDirective resourcesDirective = node.getResourcesDirective();

			if (resourcesDirective instanceof ATaRcResourcesDirective) {
				technologyAdapter = getTechnologyAdapter(getText(((ATaRcResourcesDirective) resourcesDirective).getTechnologyAdapter()));
				resourceCenter = retrieveResourceCenter(((ATaRcResourcesDirective) resourcesDirective).getResourceCenter());
			}
			else if (resourcesDirective instanceof ATaResourcesDirective) {
				technologyAdapter = getTechnologyAdapter(getText(((ATaResourcesDirective) resourcesDirective).getTechnologyAdapter()));
			}
			else if (resourcesDirective instanceof ARcResourcesDirective) {
				resourceCenter = retrieveResourceCenter(((ARcResourcesDirective) resourcesDirective).getResourceCenter());
			}
		}

		@Override
		public String toString() {
			if (technologyAdapter != null) {
				if (resourceCenter != null) {
					return "resources " + getTechnologyAdapter().getIdentifier() + " [\"" + resourceCenter.getDefaultBaseURI() + "\"]";
				}
				else {
					return "resources " + getTechnologyAdapter().getIdentifier();
				}
			}
			else {
				if (resourceCenter != null) {
					return "resources * [\"" + resourceCenter.getDefaultBaseURI() + "\"]";
				}
				else {
					return "resources";
				}
			}

		}

		@Override
		public TechnologyAdapter<?> getTechnologyAdapter() {
			return technologyAdapter;
		}

		@Override
		public FlexoResourceCenter<?> getResourceCenter() {
			return resourceCenter;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object execute() throws FMLCommandExecutionException {

			super.execute();
			output.clear();

			Collection<? extends FlexoResource<?>> resourcesToDisplay = null;

			if (getTechnologyAdapter() != null) {
				if (getResourceCenter() != null) {
					TechnologyAdapterGlobalRepository<?, ?> repository = getTechnologyAdapter().getGlobalRepository(getResourceCenter());
					resourcesToDisplay = repository.getAllResources();
				}
				else {
					resourcesToDisplay = new ArrayList<>();
					List<TechnologyAdapterGlobalRepository<?, ?>> globalRepositories = getTechnologyAdapter().getGlobalRepositories();
					for (TechnologyAdapterGlobalRepository<?, ?> repository : globalRepositories) {
						resourcesToDisplay.addAll(repository.getAllResources());
					}
				}
			}
			else {
				if (getResourceCenter() != null) {
					resourcesToDisplay = getResourceCenter().getAllResources();
				}
				else {
					resourcesToDisplay = new ArrayList<>();
					for (FlexoResourceCenter rc : getCommandInterpreter().getServiceManager().getResourceCenterService()
							.getResourceCenters()) {
						resourcesToDisplay.addAll(rc.getAllResources());
					}
				}
			}

			int nameMaxLength = 0;
			int typeMaxLength = 0;
			int taMaxLength = 0;

			for (FlexoResource<?> resource : resourcesToDisplay) {
				String name = resource.getDisplayName();
				String type = resource.getResourceDataClass().getSimpleName();
				if (type.equals("FMLRTVirtualModelInstance")) {
					type = "VirtualModelInstance";
				}
				String ta = "-";
				if (resource instanceof TechnologyAdapterResource) {
					ta = ((TechnologyAdapterResource) resource).getTechnologyAdapter().getIdentifier();
				}
				if (name.length() > nameMaxLength)
					nameMaxLength = name.length();
				if (type.length() > typeMaxLength)
					typeMaxLength = type.length();
				if (ta.length() > taMaxLength)
					taMaxLength = ta.length();
			}

			for (FlexoResource<?> resource : resourcesToDisplay) {
				String name = resource.getDisplayName();
				String type = resource.getResourceDataClass().getSimpleName();
				if (type.equals("FMLRTVirtualModelInstance")) {
					type = "VirtualModelInstance";
				}
				String ta = "-";
				String uri = "[\"" + resource.getURI() + "\"]";
				if (resource instanceof TechnologyAdapterResource) {
					ta = ((TechnologyAdapterResource) resource).getTechnologyAdapter().getIdentifier();
				}
				String cmdOutput = name + StringUtils.buildWhiteSpaceIndentation(nameMaxLength - name.length() + 1) + type
						+ StringUtils.buildWhiteSpaceIndentation(typeMaxLength - type.length() + 1) + ta
						+ StringUtils.buildWhiteSpaceIndentation(taMaxLength - ta.length() + 1)
						+ (resource.isLoaded() ? "[LOADED]   " : "[UNLOADED] ") + uri;

				output.add(cmdOutput);
				getOutStream().println(cmdOutput);
			}

			return null;

		}
	}
}
