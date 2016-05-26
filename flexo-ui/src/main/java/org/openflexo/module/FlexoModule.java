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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.DataFlexoObserver;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.ResourceModified;
import org.openflexo.foundation.resource.ResourceRegistered;
import org.openflexo.foundation.resource.ResourceSaved;
import org.openflexo.foundation.resource.ResourceUnregistered;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.prefs.ModulePreferences;
import org.openflexo.project.InteractiveProjectLoader;
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
	private ModulePreferences<M> preferences = null;

	public FlexoModule(ApplicationContext applicationContext) {
		super();
		this.applicationContext = applicationContext;
	}

	public void initModule() {
		controller = createControllerForModule();
	}

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

	private InteractiveProjectLoader getProjectLoader() {
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
		return getModule().getLocalizedName();
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

	void setAsActiveModule() {

		// System.out.println("**************** setAsActiveModule()");

		isActive = true;
		int state = getFlexoFrame().getExtendedState();
		state &= ~Frame.ICONIFIED;
		getFlexoFrame().setExtendedState(state);
		getFlexoFrame().setVisible(true);
		/*
		 SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (controller != null) {
					getFlexoFrame().toFront();
				}
			}
		});
		 */
		if (getEditor() != null && getController().getCurrentDisplayedObjectAsModuleView() == null) {
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
				getFlexoController().setCurrentEditedObjectAsModuleView(defaultObjectToSelect);
			}
			else {
				if (getFlexoController().getSelectionManager().getFocusedObject() == null) {
					getFlexoController().setCurrentEditedObjectAsModuleView(null);
				}
			}
			getFlexoController().getSelectionManager().fireUpdateSelection();

		}

		if (getFlexoController().getCurrentModuleView() != null) {
			getFlexoController().getCurrentModuleView().willShow();
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
		Module<?> moiMeme = getModule();
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
		logger.fine("ModuleLoader service received notification " + notification + " from " + caller);
		if (notification instanceof ProjectClosed) {
			if (getEditor() != null && getEditor().getProject() == ((ProjectClosed) notification).getProject()) {
				logger.info("Closing projet " + getEditor().getProject() + " in module " + this);
				getController().getControllerModel().setCurrentEditor(null);
				// getController().getControllerModel().setCurrentLocation(new Location(null, null,
				// getController().getCurrentPerspective()));
			}
		}
		else if (notification instanceof ResourceModified || notification instanceof ResourceSaved
				|| notification instanceof ResourceRegistered || notification instanceof ResourceUnregistered) {
			if (getFlexoFrame() != null) {
				getFlexoFrame().updateWindowModified();
			}
		}

	}
}
