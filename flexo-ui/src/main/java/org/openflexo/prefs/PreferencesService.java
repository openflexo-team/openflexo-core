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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager.ServiceRegistered;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.prefs.FlexoPreferencesResource.FlexoPreferencesResourceImpl;
import org.openflexo.toolbox.HasPropertyChangeSupport;
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

	public <P extends PreferencesContainer> P managePreferences(Class<P> prefClass, PreferencesContainer container) {
		P returned = getPreferences(prefClass);
		if (returned == null) {
			returned = getPreferencesFactory().newInstance(prefClass);
			initPreferences(returned);
			container.addToContents(returned);
		}
		returned.setPreferencesService(this);
		return returned;
	}

	private void initPreferences(PreferencesContainer p) {
	}

	@Override
	public void initialize() {
		resource = FlexoPreferencesResourceImpl.makePreferencesResource(getServiceManager());
		getFlexoPreferences().setPreferencesService(this);
		managePreferences(GeneralPreferences.class, getFlexoPreferences());
		managePreferences(PresentationPreferences.class, getFlexoPreferences());
		managePreferences(AdvancedPrefs.class, getFlexoPreferences());

		for (FlexoService service : getServiceManager().getRegisteredServices()) {
			initializePreferencesForService(service);
		}

		if (getServiceManager().getModuleLoader() != null) {
			for (Module<?> m : getServiceManager().getModuleLoader().getKnownModules()) {
				initializePreferencesForModule(m);
			}
		}

		managePreferences(LoggingPreferences.class, getFlexoPreferences());
		managePreferences(BugReportPreferences.class, getFlexoPreferences());

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <S extends FlexoService> Class<? extends ServicePreferences<S>> getServicePreferencesClass(S service) {
		if (service instanceof FlexoResourceCenterService) {
			return (Class) ResourceCenterPreferences.class;
		}
		else if (service instanceof TechnologyAdapterService) {
			return (Class) TechnologyAdapterPreferences.class;
		}
		else if (service instanceof ModuleLoader) {
			return (Class) ModuleLoaderPreferences.class;
		}
		return null;
	}

	private <S extends FlexoService> ServicePreferences<S> initializePreferencesForService(S service) {
		Class<? extends ServicePreferences<S>> preferencesClass = getServicePreferencesClass(service);
		if (preferencesClass != null) {
			ServicePreferences<S> preferences = managePreferences(preferencesClass, getFlexoPreferences());
			if (preferences != null) {
				preferences.setService(service);
			}
			return preferences;
		}
		return null;
	}

	private ModulePreferences<?> initializePreferencesForModule(Module<?> module) {
		ModulePreferences<?> preferences = managePreferences(module.getPreferencesClass(), getModuleLoaderPreferences());
		if (preferences != null) {
			preferences.setModule((Module) module);
		}
		return preferences;
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
		if (notification instanceof ServiceRegistered) {
			initializePreferencesForService(caller);
		}
	}

	public FlexoPreferencesFactory makePreferencesFactory(FlexoPreferencesResource resource, ApplicationContext applicationContext)
			throws ModelDefinitionException {
		List<Class<?>> classes = buildClassesListForPreferenceFactory(applicationContext);
		return new FlexoPreferencesFactory(resource,
				ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes.size()])));
	}

	protected List<Class<?>> buildClassesListForPreferenceFactory(ApplicationContext applicationContext) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(FlexoPreferences.class);
		classes.add(GeneralPreferences.class);
		classes.add(PresentationPreferences.class);
		classes.add(AdvancedPrefs.class);
		classes.add(ResourceCenterPreferences.class);
		classes.add(TechnologyAdapterPreferences.class);
		classes.add(ModuleLoaderPreferences.class);
		for (FlexoService service : getServiceManager().getRegisteredServices()) {
			Class<?> servicePreferenceClass = getServicePreferencesClass(service);
			if (servicePreferenceClass != null) {
				classes.add(servicePreferenceClass);
			}
		}
		if (applicationContext.getModuleLoader() != null) {
			for (Module<?> m : applicationContext.getModuleLoader().getKnownModules()) {
				classes.add(m.getPreferencesClass());
			}
		}

		classes.add(LoggingPreferences.class);
		classes.add(BugReportPreferences.class);

		return classes;
	}

	public void applyPreferences() {
		System.out.println("applyPreferences() not implemented yet");
	}

	public void savePreferences() {
		try {
			resource.save(null);
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
	}

	public void revertToSaved() {
		System.out.println("revertToSaved() not implemented yet");
	}

	public FlexoPreferencesFactory getPreferencesFactory() {
		return resource.getFactory();
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

	public PresentationPreferences getPresentationPreferences() {
		return managePreferences(PresentationPreferences.class, getFlexoPreferences());
	}

	public AdvancedPrefs getAdvancedPrefs() {
		return managePreferences(AdvancedPrefs.class, getFlexoPreferences());
	}

	public LoggingPreferences getLoggingPreferences() {
		return managePreferences(LoggingPreferences.class, getFlexoPreferences());
	}

	public BugReportPreferences getBugReportPreferences() {
		return managePreferences(BugReportPreferences.class, getFlexoPreferences());
	}

	public ResourceCenterPreferences getResourceCenterPreferences() {
		return managePreferences(ResourceCenterPreferences.class, getFlexoPreferences());
	}

	public <S extends FlexoService> ServicePreferences<S> getServicePreferences(S service) {
		Class<? extends ServicePreferences<S>> preferencesClass = getServicePreferencesClass(service);
		// System.out.println("preferencesClass for " + service + " is " + preferencesClass);
		if (preferencesClass != null) {
			ServicePreferences<S> preferences = managePreferences(preferencesClass, getFlexoPreferences());
			if (preferences != null) {
				preferences.setService(service);
			}
			return preferences;
		}
		return null;

	}

	public TechnologyAdapterPreferences getTechnologyAdapterPreferences() {
		return (TechnologyAdapterPreferences) getServicePreferences(getServiceManager().getTechnologyAdapterService());
	}

	public ModuleLoaderPreferences getModuleLoaderPreferences() {
		return (ModuleLoaderPreferences) getServicePreferences(getServiceManager().getModuleLoader());
	}

	public boolean readOnly() {
		return false;
	}
}
