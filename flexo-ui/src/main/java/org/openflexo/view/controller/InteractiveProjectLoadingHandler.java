/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;

public abstract class InteractiveProjectLoadingHandler implements ProjectLoadingHandler {

	private static final Logger logger = Logger.getLogger(InteractiveProjectLoadingHandler.class.getPackage().getName());

	private boolean performingAutomaticConversion = false;

	protected boolean isPerformingAutomaticConversion() {
		return performingAutomaticConversion;
	}

	protected Vector<ResourceToConvert> searchResourcesToConvert(FlexoProject<?> project) {
		Vector<ResourceToConvert> resourcesToConvert = new Vector<>();

		for (FlexoResource<?> resource : project.getAllResources()) {
			if (resource instanceof PamelaResource) {
				PamelaResource<?, ?> pamelaResource = (PamelaResource<?, ?>) resource;
				if (!pamelaResource.getModelVersion().equals(pamelaResource.latestVersion())) {
					resourcesToConvert.add(new ResourceToConvert(pamelaResource));
					logger.fine("Require conversion for " + pamelaResource + " from " + pamelaResource.getModelVersion() + " to "
							+ pamelaResource.latestVersion());
				}
			}
		}

		return resourcesToConvert;
	}

	protected void performConversion(FlexoProject<?> project, Vector<ResourceToConvert> resourcesToConvert) {
		List<PamelaResource<?, ?>> resources = new ArrayList<>();
		for (ResourceToConvert resourceToConvert : resourcesToConvert) {
			resources.add(resourceToConvert.getResource());
		}
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("converting_project"));
		// progress.resetSecondaryProgress(resourcesToConvert.size());
		performingAutomaticConversion = true;
		// DependencyAlgorithmScheme scheme = project.getDependancyScheme();
		// Pessimistic dependancy scheme is cheaper and optimistic is not intended for this situation
		// project.setDependancyScheme(DependencyAlgorithmScheme.Pessimistic);
		// FlexoResource.sortResourcesWithDependancies(resources);
		for (PamelaResource<?, ?> res : resources) {
			// progress.setSecondaryProgress(FlexoLocalization.getMainLocalizer().localizedForKey("converting") + " " + res.getURI() + " "
			// + FlexoLocalization.getMainLocalizer().localizedForKey("from") + " " + res.getModelVersion() + " "
			// + FlexoLocalization.getMainLocalizer().localizedForKey("to") + " " + res.latestVersion());
			if (!res.isDeleted()) {
				try {
					res.getResourceData();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				} // Converts the resource by loading it.
			}
		}
		// project.setDependancyScheme(scheme);
		performingAutomaticConversion = false;

	}

	@Override
	public void notifySevereLoadingFailure(FlexoResource<?> resource, Exception e) {
		if (resource.getIODelegate() instanceof FileIODelegate) {
			FileIODelegate r = (FileIODelegate) (resource.getIODelegate());
			if (e.getMessage().indexOf("JDOMParseException") > -1 && !GraphicsEnvironment.isHeadless()) {
				JOptionPane.showMessageDialog(null,
						"Could not load project: file '" + r.getFile().getAbsolutePath() + "' contains invalid XML!\n" + e.getMessage()
								.substring(e.getMessage().indexOf("JDOMParseException") + 20, e.getMessage().indexOf("StackTrace") - 1),
						"XML error", JOptionPane.ERROR_MESSAGE);

			}
			e.printStackTrace();
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Full exception message: " + e.getMessage());
			}
			if (!GraphicsEnvironment.isHeadless()) {
				JOptionPane.showMessageDialog(null,
						FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_resource_manager_file") + "\n"
								+ FlexoLocalization.getMainLocalizer().localizedForKey("to_avoid_damaging_the_project_flexo_will_exit")
								+ "\n" + FlexoLocalization.getMainLocalizer().localizedForKey("error_is_caused_by_file") + " : '"
								+ r.getFile().getAbsolutePath() + "'",
						FlexoLocalization.getMainLocalizer().localizedForKey("error_during_opening_project"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected class ResourceToConvert {

		private final PamelaResource<?, ?> resource;

		ResourceToConvert(PamelaResource<?, ?> resource) {
			this.resource = resource;
		}

		public Icon getIcon() {
			return IconLibrary.getIconForResource(resource);
		}

		public String getName() {
			return resource.getName();
		}

		public String getCurrentVersion() {
			return resource.getModelVersion().toString();
		}

		public String getLatestVersion() {
			return resource.latestVersion().toString();
		}

		public PamelaResource<?, ?> getResource() {
			return resource;
		}

		protected void convert() {
			try {
				resource.getResourceData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
		}
	}

}
