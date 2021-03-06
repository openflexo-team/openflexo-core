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

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;

public class FullInteractiveProjectLoadingHandler extends InteractiveProjectLoadingHandler {

	private boolean alwaysUpgradeResourceToLatestVersion = false;

	public FullInteractiveProjectLoadingHandler(File projectDirectory) {
		super();
	}

	@Override
	public boolean upgradeResourceToLatestVersion(FlexoResource<?> resource) throws ProjectLoadingCancelledException {
		if (isPerformingAutomaticConversion()) {
			return true;
		}

		if (alwaysUpgradeResourceToLatestVersion) {
			return true;
		}

		String CONVERT_ALL = FlexoLocalization.getMainLocalizer().localizedForKey("convert_all_resources");
		String CONVERT = FlexoLocalization.getMainLocalizer().localizedForKey("convert_this_resource");
		String DONT_CONVERT = FlexoLocalization.getMainLocalizer().localizedForKey("don't_convert_this_resource");
		String CANCEL = FlexoLocalization.getMainLocalizer().localizedForKey("cancel");
		int choice = FlexoController.selectOption("<html><center>" + IconLibrary.UNFIXABLE_WARNING_ICON.getHTMLImg() + "<b>&nbsp;"
				+ FlexoLocalization.getMainLocalizer().localizedForKey("warning") + "</b></center><br>"
				+ FlexoLocalization.getMainLocalizer().localizedForKey("resource") + " <b>" + resource.getURI() + "</b><br>"
				+ FlexoLocalization.getMainLocalizer().localizedForKey(
						"this_resource_has_been_serialized_with_an_older_version_than_the_one_declared_as_current_application_version")
				+ "<br>" + FlexoLocalization.getMainLocalizer()
						.localizedForKey("should_i_convert_this_resource_to_latest_version_(recommanded_choice)")
				+ "<br>"
				// + FlexoLocalization.localizedForKey("current_version") + " : <b>" + resource.getXmlVersion() + "</b><br>"
				// + FlexoLocalization.localizedForKey("will_be_converted_to_version") + " : <b>" + resource.latestVersion()
				+ "</b><br></html>", CONVERT_ALL, CONVERT_ALL, CONVERT, DONT_CONVERT, CANCEL);

		if (choice == 0) { // CONVERT_ALL
			alwaysUpgradeResourceToLatestVersion = true;
			return true;
		}
		else if (choice == 1) { // CONVERT
			return true;
		}
		else if (choice == 2) { // DONT_CONVERT
			return false;
		}
		else {
			throw new ProjectLoadingCancelledException();
		}
	}

	private Hashtable<FlexoResource<?>, Boolean> useOlderMappingHashtable;

	@Override
	public boolean useOlderMappingWhenLoadingFailure(FlexoResource<?> resource) throws ProjectLoadingCancelledException {
		if (useOlderMappingHashtable == null) {
			useOlderMappingHashtable = new Hashtable<>();
		}

		if (useOlderMappingHashtable.get(resource) != null) {
			return useOlderMappingHashtable.get(resource);
		}

		String TRY_TO_RECOVER = FlexoLocalization.getMainLocalizer().localizedForKey("try_to_recover_resource");
		String CANCEL = FlexoLocalization.getMainLocalizer().localizedForKey("cancel");
		int choice = FlexoController.selectOption("<html><center>" + IconLibrary.UNFIXABLE_WARNING_ICON.getHTMLImg() + "<b>&nbsp;"
				+ FlexoLocalization.getMainLocalizer().localizedForKey("warning") + "</b></center><br>"
				+ FlexoLocalization.getMainLocalizer().localizedForKey("resource") + " <b>" + resource.getURI() + "</b><br>"
				+ FlexoLocalization.getMainLocalizer().localizedForKey("this_resource_could_not_be_deserialized_with_declared_version")
				+ "<br>" + FlexoLocalization.getMainLocalizer().localizedForKey("should_i_try_to_recover_by_using_older_versions") + "<br>"
				+ "<i>" + FlexoLocalization.getMainLocalizer().localizedForKey("you_may_loose_some_informations") + "</i><br></html>",
				TRY_TO_RECOVER, TRY_TO_RECOVER, CANCEL);

		if (choice == 0) {
			useOlderMappingHashtable.put(resource, true);
			return true;
		}
		else {
			throw new ProjectLoadingCancelledException();
		}
	}

	@Override
	public boolean loadAndConvertAllOldResourcesToLatestVersion(FlexoProject project) throws ProjectLoadingCancelledException {
		Vector<ResourceToConvert> resourcesToConvert = searchResourcesToConvert(project);
		if (alwaysUpgradeResourceToLatestVersion) {
			performConversion(project, resourcesToConvert);
			return true;
		}
		if (resourcesToConvert.size() > 0) {
			// TODO: reimplement this
			/*ProjectConversionDialog dialog = new ProjectConversionDialog(project, resourcesToConvert);
			if (dialog.getStatus() == ProjectConversionDialog.ReturnedStatus.CANCEL) {
				throw new ProjectLoadingCancelledException();
			} else if (dialog.getStatus() == ProjectConversionDialog.ReturnedStatus.SKIP_CONVERSION) {
				return false;
			} else if (dialog.getStatus() == ProjectConversionDialog.ReturnedStatus.CONVERT) {
				alwaysUpgradeResourceToLatestVersion = true;
				performConversion(project, resourcesToConvert, progress);
				return true;
			}*/
		}

		return false;
	}

	// TODO: reimplement this

	/*private static class ProjectConversionDialog extends FlexoDialog {
	
		private enum ReturnedStatus {
			CANCEL, CONVERT, SKIP_CONVERSION
		};
	
		ReturnedStatus status;
	
		ProjectConversionDialog(FlexoProject project, Vector<ResourceToConvert> resourcesToConvert) {
			super((JFrame) null, FlexoLocalization.localizedForKey("project_conversion"), true);
			LabelParameter infoLabel = new LabelParameter("info", "info", "<html><center>"
					+ IconLibrary.UNFIXABLE_WARNING_ICON.getHTMLImg() + "<b>&nbsp;" + FlexoLocalization.localizedForKey("warning")
					+ "</b></center><br>" + "<center>" + project.getProjectDirectory().getName() + "</center><br>"
					+ FlexoLocalization.localizedForKey("this_project_seems_to_have_been_created_with_an_older_version_of_flexo") + "<br>"
					+ FlexoLocalization.localizedForKey("would_you_like_to_convert_entire_project_to_new_version_of_flexo_(recommanded)")
					+ "<br>" + "<i>" + FlexoLocalization.localizedForKey("following_resources_will_be_converted") + "</i>" + "</html>",
					false);
			final PropertyListParameter<ResourceToConvert> resourcesParam = new PropertyListParameter<ResourceToConvert>("resources",
					"resources_to_convert", resourcesToConvert, 20, 10);
			resourcesParam.addIconColumn("icon", "", 30, false);
			resourcesParam.addReadOnlyTextFieldColumn("resourceType", "type", 150, true);
			resourcesParam.addReadOnlyTextFieldColumn("name", "name", 150, true);
			resourcesParam.addReadOnlyTextFieldColumn("currentVersion", "old_version", 100, true);
			resourcesParam.addReadOnlyTextFieldColumn("latestVersion", "new_version", 100, true);
	
			AskParametersPanel panel = new AskParametersPanel(project, infoLabel, resourcesParam);
	
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panel, BorderLayout.CENTER);
			JPanel controlPanel = new JPanel(new FlowLayout());
			JButton cancelButton = new JButton();
			cancelButton.setText(FlexoLocalization.localizedForKey("cancel", cancelButton));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					status = ReturnedStatus.CANCEL;
					dispose();
				}
			});
			controlPanel.add(cancelButton);
			JButton skipConversionButton = new JButton();
			skipConversionButton.setText(FlexoLocalization.localizedForKey("skip_conversion", skipConversionButton));
			skipConversionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					status = ReturnedStatus.SKIP_CONVERSION;
					dispose();
				}
			});
			controlPanel.add(skipConversionButton);
			JButton convertAllResources = new JButton();
			convertAllResources.setText(FlexoLocalization.localizedForKey("convert_all_resources", convertAllResources));
			convertAllResources.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					status = ReturnedStatus.CONVERT;
					dispose();
				}
			});
			if (ToolBox.getPLATFORM() == ToolBox.MACOS) {
				convertAllResources.setSelected(true);
			}
			controlPanel.add(convertAllResources);
			getRootPane().setDefaultButton(convertAllResources);
			getContentPane().add(controlPanel, BorderLayout.SOUTH);
			validate();
			pack();
			setVisible(true);
	
		}
	
		protected ReturnedStatus getStatus() {
			return status;
		}
	}*/

}
