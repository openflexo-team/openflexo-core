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

package org.openflexo.module;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.NewProjectComponent;
import org.openflexo.components.OpenProjectComponent;
import org.openflexo.foundation.DataFlexoObserver;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.resource.ResourceModified;
import org.openflexo.foundation.resource.ResourceRegistered;
import org.openflexo.foundation.resource.ResourceSaved;
import org.openflexo.foundation.resource.ResourceUnregistered;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.prefs.ModulePreferences;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Abstract class defining a Flexo Module. A Flexo Module is an application component part of the Openflexo Diatomee and dedicated to a
 * particular purpose.
 * 
 * @author sguerin
 */
public abstract class FlexoModule<M extends FlexoModule<M>> implements DataFlexoObserver {

	static final Logger logger = Logger.getLogger(FlexoModule.class.getPackage().getName());

	private boolean isActive = false;

	private FlexoController controller;

	private final ApplicationContext applicationContext;
	// Unused private final ModulePreferences<M> preferences = null;

	public FlexoModule(ApplicationContext applicationContext) {
		super();
		this.applicationContext = applicationContext;
	}

	public void initModule() {
		locales = new LocalizedDelegateImpl(ResourceLocator.locateResource(getLocalizationDirectory()),
				getApplicationContext().getLocalizationService().getFlexoLocalizer(),
				getApplicationContext().getLocalizationService().getAutomaticSaving(), true);
		controller = createControllerForModule();
	}

	private LocalizedDelegate locales = null;

	/**
	 * Return the locales relative to this module
	 * 
	 * @return
	 */
	public LocalizedDelegate getLocales() {
		if (locales == null) {
			return FlexoLocalization.getMainLocalizer();
		}
		return locales;
	}

	public abstract String getLocalizationDirectory();

	public abstract Module<M> getModule();

	public ModulePreferences<M> getPreferences() {
		return getApplicationContext().getPreferencesService().getPreferences(getModule().getPreferencesClass());
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	private ModuleLoader getModuleLoader() {
		return getApplicationContext().getModuleLoader();
	}

	private ProjectLoader getProjectLoader() {
		return getApplicationContext().getProjectLoader();
	}

	protected abstract FlexoController createControllerForModule();

	public FlexoController getController() {
		return controller;
	}

	public FlexoController getFlexoController() {
		return controller;
	}

	public FlexoFrame getFlexoFrame() {
		if (controller != null) {
			return controller.getFlexoFrame();
		}
		else {
			return null;
		}
	}

	public final String getName() {
		return getModule().getName();
	}

	public final String getShortName() {
		return getModule().getShortName();
	}

	public final String getDescription() {
		return getModule().getLocalizedDescription();
	}

	public boolean isActive() {
		return isActive;
	}

	void setAsInactive() {
		isActive = false;
		if (getFlexoFrame() != null) {
			getFlexoFrame().setRelativeVisible(false);
		}
		if (getFlexoController() != null) {
			if (getFlexoController().getValidationWindow(false) != null) {
				getFlexoController().getValidationWindow(false).setVisible(false);
			}

			if (getFlexoController().getCurrentModuleView() != null) {
				getFlexoController().getCurrentModuleView().willHide();
			}
		}
	}

	private FlexoEditor getEditor() {
		return getController().getEditor();
	}

	@ModelEntity
	@ImplementationClass(WelcomePanel.WelcomePanelImpl.class)
	public static interface WelcomePanel<M extends FlexoModule<?>> extends FlexoObject {

		@Getter(value = "module", ignoreType = true)
		public M getModule();

		@Setter("module")
		public void setModule(M module);

		public void openProject();

		public void newProject();

		public static abstract class WelcomePanelImpl extends FlexoObjectImpl implements WelcomePanel {

			@Override
			public void openProject() {
				File projectDirectory = OpenProjectComponent.getProjectDirectory(getModule().getController().getApplicationContext());
				if (projectDirectory != null) {
					// try {
					try {
						getModule().getProjectLoader().loadProject(projectDirectory);
					} catch (ProjectLoadingCancelledException e) {
						// Nothing to do
					} catch (ProjectInitializerException e) {
						e.printStackTrace();
						FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_project_located_at")
								+ projectDirectory.getAbsolutePath());
					}
				}
			}

			@Override
			public void newProject() {
				File projectDirectory = NewProjectComponent.getProjectDirectory(getModule().getController().getApplicationContext());
				if (projectDirectory != null) {
					if (getModule().getModule() instanceof NatureSpecificModule) {
						Class<? extends ProjectNature> projectNatureClass = ((NatureSpecificModule) getModule().getModule())
								.getProjectNatureClass();
						try {
							getModule().getProjectLoader().newStandaloneProject(projectDirectory, projectNatureClass);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ProjectInitializerException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							getModule().getProjectLoader().newStandaloneProject(projectDirectory);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ProjectInitializerException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}
	}

	private WelcomePanel<M> makeWelcomePanel() {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(WelcomePanel.class);
			WelcomePanel<M> returned = factory.newInstance(WelcomePanel.class);
			returned.setModule((M) this);
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void selectDefaultObjectWhenAvailable() {

		if (getController().getCurrentDisplayedObjectAsModuleView() != null) {
			// An object is already selected, do not select another one
			return;
		}

		if (getEditor() != null && getEditor().getProject() != null && getController().getCurrentDisplayedObjectAsModuleView() == null) {
			boolean selectDefaultObject = false;
			FlexoObject defaultObjectToSelect = getController().getDefaultObjectToSelect(getEditor().getProject());
			if (defaultObjectToSelect != null && (getFlexoController().getCurrentDisplayedObjectAsModuleView() == null
					|| getFlexoController().getCurrentDisplayedObjectAsModuleView() == defaultObjectToSelect)) {
				if (getFlexoController().getSelectionManager().getFocusedObject() == null) {
					selectDefaultObject = true;
				}
			}
			else {
				selectDefaultObject = true;
			}
			if (selectDefaultObject) {
				if (defaultObjectToSelect == null) {
					defaultObjectToSelect = makeWelcomePanel();
				}
				getFlexoController().setCurrentEditedObject(defaultObjectToSelect);
			}
			else {
				if (getFlexoController().getSelectionManager().getFocusedObject() == null) {
					getFlexoController().setCurrentEditedObject(null);
				}
			}
			getFlexoController().getSelectionManager().fireUpdateSelection();

		}
		else if (getFlexoController().getEditor() == null || getFlexoController().getEditor().getProject() == null) {
			getFlexoController().setCurrentEditedObject(makeWelcomePanel());
		}

		if (getFlexoController().getCurrentModuleView() != null) {
			getFlexoController().getCurrentModuleView().willShow();
		}

	}

	void setAsActiveModule() {

		// System.out.println("**************** setAsActiveModule() for " + this);

		isActive = true;
		int state = getFlexoFrame().getExtendedState();
		state &= ~Frame.ICONIFIED;
		getFlexoFrame().setExtendedState(state);
		getFlexoFrame().setVisible(true);

		if (getFlexoController() != null) {
			if (getFlexoController().getValidationWindow(false) != null) {
				getFlexoController().getValidationWindow(false).setVisible(true);
			}

			if (getFlexoController().getCurrentModuleView() != null) {
				getFlexoController().getCurrentModuleView().willShow();
			}

			else {
				selectDefaultObjectWhenAvailableLater();
			}
		}

	}

	private boolean isRequestingDefaultObjectSelection = false;

	/**
	 * Instantiate a task to be executed after all already scheduled tasks which will select a default object when required<br>
	 * This design is better than SwingUtilities.invokeLater()
	 */
	private void selectDefaultObjectWhenAvailableLater() {
		if (isRequestingDefaultObjectSelection) {
			return;
		}
		isRequestingDefaultObjectSelection = true;
		SelectDefaultObjectWhenRequired task = new SelectDefaultObjectWhenRequired();
		for (FlexoTask sTask : getController().getApplicationContext().getTaskManager().getScheduledTasks()) {
			task.addToDependantTasks(sTask);
		}
		getController().getApplicationContext().getTaskManager().scheduleExecution(task);
	}

	private class SelectDefaultObjectWhenRequired extends FlexoTask {

		public SelectDefaultObjectWhenRequired() {
			super("SelectDefaultObject", getLocales().localizedForKey("select_default_object"));
		}

		@Override
		public void performTask() throws InterruptedException {
			selectDefaultObjectWhenAvailable();
			isRequestingDefaultObjectSelection = false;
		}

	}

	/**
	 * Close Module after asking confirmation Review unsaved and save Unload in module loader
	 * 
	 * @return boolean indicating if close was performed
	 */
	public boolean close()
	/*
	 * This method is used to request the closing of module (either because it has been hit in the menu or the red-cross of the current
	 * window has been pressed. To perform the real closing of the module, closeWithoutConfirmation() must be called. Once
	 * closeWithoutConfirmation has been called, there is no way back. Therefore any call to closeWithoutConfirmation() must be followed by
	 * a "return true"
	 */
	{
		boolean isLastModule = getModuleLoader().isLastLoadedModule(getModule());
		if (isLastModule) {
			try {
				getModuleLoader().quit(true);
				return true;
			} catch (OperationCancelledException e) {
				return false;
			}
		}
		else { // There are still other modules left
			closeWithoutConfirmation();// Unloads the module
			return true; // Since there is nothing to save and that Flexo
			// has other windows opened to access it, we
			// close the module and that's it!!!
			// }
		}
	}

	public void closeWithoutConfirmation() {
		closeWithoutConfirmation(true);
	}

	public void closeWithoutConfirmation(boolean quitIfNoModuleLeft) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Closing module " + getName());
		}
		moduleWillClose();
		setAsInactive();
		if (controller != null) {
			controller.dispose();
		}
		else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Called twice closeWithoutConfirmation on " + this);
		}
		controller = null;

		if (getModuleLoader().isLoaded(getModule())) {
			getModuleLoader().unloadModule(getModule());
		}
		// Is there some modules loaded ?
		Collection<Module<?>> leftModules = getModuleLoader().getLoadedModules();
		// Unused Module<?> moiMeme = getModule();
		leftModules.remove(getModule());
		if (leftModules.size() > 0) {
			try {
				getModuleLoader().switchToModule(leftModules.iterator().next());
			} catch (ModuleLoadingException e) {
				logger.severe("Module is loaded and so this exception CANNOT occur. Please investigate and FIX.");
				e.printStackTrace();
			}
		}
		else {
			if (quitIfNoModuleLeft) {
				try {
					getModuleLoader().quit(false);
				} catch (OperationCancelledException e) {
				}
			}
		}

	}

	public void moduleWillClose() {
	}

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {

	}

	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		// logger.fine("********************* ModuleLoader service received notification " + notification + " from " + caller);
		if (notification instanceof ProjectClosed) {
			if (getEditor() != null && getEditor().getProject() == ((ProjectClosed) notification).getProject()) {
				logger.info("Closing projet " + getEditor().getProject() + " in module " + this);
				getController().getControllerModel().setCurrentEditor(null);
				// getController().getControllerModel().setCurrentLocation(new Location(null, null,
				// getController().getCurrentPerspective()));
			}
		}
		else if (notification instanceof ProjectLoaded) {
			selectDefaultObjectWhenAvailableLater();
		}
		else if (notification instanceof ResourceModified || notification instanceof ResourceSaved
				|| notification instanceof ResourceRegistered || notification instanceof ResourceUnregistered) {
			if (getFlexoFrame() != null) {
				getFlexoFrame().updateWindowModified();
			}
		}

	}

	/**
	 * Hooks used to handle the fact that a module should activate or not advanced actions of a {@link TechnologyAdapter}<br>
	 * Overrides when required. Default behaviour returns null.
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public boolean activateAdvancedActions(TechnologyAdapter<?> technologyAdapter) {
		return false;
	}
}
