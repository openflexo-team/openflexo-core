/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.prefs;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.AdvancedPrefs;
import org.openflexo.ApplicationContext;
import org.openflexo.GeneralPreferences;
import org.openflexo.ResourceCenterPreferences;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.module.Module;
import org.openflexo.prefs.FlexoPreferencesResource.FlexoPreferencesResourceImpl;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.FlexoFrame;

/**
 * This service manages preferences<br>
 * 
 * Preferences are organized into logical {@link FlexoPreferences} units identified by a String identifier.
 * 
 * 
 * @author sguerin
 */
public class PreferencesService extends FlexoServiceImpl implements FlexoService, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(PreferencesService.class.getPackage().getName());

	private FlexoPreferencesResource resource;

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public FlexoPreferences getFlexoPreferences() {
		return resource.getFlexoPreferences();
	}

	private <P extends PreferencesContainer> P managePreferences(Class<P> prefClass, PreferencesContainer container) {
		P returned = getPreferences(prefClass);
		if (returned == null) {
			returned = getPreferencesFactory().newInstance(prefClass);
			initPreferences(returned);
			container.addToContents(returned);
		}
		return returned;
	}

	private void initPreferences(PreferencesContainer p) {

	}

	@Override
	public void initialize() {
		resource = FlexoPreferencesResourceImpl.makePreferencesResource(getServiceManager());
		managePreferences(GeneralPreferences.class, getFlexoPreferences());
		managePreferences(AdvancedPrefs.class, getFlexoPreferences());
		for (Module<?> m : getServiceManager().getModuleLoader().getKnownModules()) {
			managePreferences(m.getPreferencesClass(), getFlexoPreferences());
		}
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine("PreferencesService received notification " + notification + " from " + caller);
		if (caller instanceof FlexoResourceCenterService) {
			if (notification instanceof ResourceCenterAdded && !readOnly()) {
				FlexoResourceCenter addedFlexoResourceCenter = ((ResourceCenterAdded) notification).getAddedResourceCenter();
				if (!(addedFlexoResourceCenter instanceof FlexoProject)) {
					getResourceCenterPreferences().ensureResourceEntryIsPresent(addedFlexoResourceCenter.getResourceCenterEntry());
				}
				savePreferences();
			}
			else if (notification instanceof ResourceCenterRemoved && !readOnly()) {
				getResourceCenterPreferences().ensureResourceEntryIsNoMorePresent(
						((ResourceCenterRemoved) notification).getRemovedResourceCenter().getResourceCenterEntry());
				savePreferences();
			}
		}
	}

	public void savePreferences() {
		try {
			resource.save(null);
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
	}

	public void revertToSaved() {
		// TODO: reimplement this
	}

	public FlexoPreferencesFactory getPreferencesFactory() {
		return resource.getFactory();
	}

	public File getLogDirectory() {
		File outputDir = new File(System.getProperty("user.home") + "/Library/Logs/OpenFlexo");
		if (ToolBox.getPLATFORM() == ToolBox.WINDOWS) {
			boolean ok = false;
			String appData = System.getenv("LOCALAPPDATA");
			if (appData != null) {
				File f = new File(appData);
				if (f.isDirectory() && f.canWrite()) {
					outputDir = new File(f, "OpenFlexo/Logs");
					ok = true;
				}
				if (!ok) {
					appData = System.getenv("APPDATA");
					if (appData != null) {
						f = new File(appData);
						if (f.isDirectory() && f.canWrite()) {
							outputDir = new File(f, "OpenFlexo/Logs");
							ok = true;
						}
					}
				}
			}
		}
		else if (ToolBox.getPLATFORM() == ToolBox.LINUX) {
			outputDir = new File(System.getProperty("user.home"), ".openflexo/logs");
		}
		return outputDir;
	}

	public <P extends PreferencesContainer> P getPreferences(Class<P> containerType) {
		return getFlexoPreferences().getPreferences(containerType);
	}

	public void showPreferences() {
		getPreferencesDialog().setVisible(true);
	}

	public PreferencesDialog getPreferencesDialog() {
		return PreferencesDialog.getPreferencesDialog(getServiceManager(), FlexoFrame.getActiveFrame());
	}

	public GeneralPreferences getGeneralPreferences() {
		return managePreferences(GeneralPreferences.class, getFlexoPreferences());
	}

	public AdvancedPrefs getAdvancedPrefs() {
		return managePreferences(AdvancedPrefs.class, getFlexoPreferences());
	}

	public ResourceCenterPreferences getResourceCenterPreferences() {
		return managePreferences(ResourceCenterPreferences.class, getFlexoPreferences());
	}

	public boolean readOnly() {
		return false;
	}
}
