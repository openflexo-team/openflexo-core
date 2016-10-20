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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.action.CopyAction;
import org.openflexo.foundation.action.CopyAction.CopyActionType;
import org.openflexo.foundation.action.CutAction;
import org.openflexo.foundation.action.CutAction.CutActionType;
import org.openflexo.foundation.action.ImportProject;
import org.openflexo.foundation.action.PasteAction;
import org.openflexo.foundation.action.PasteAction.PasteActionType;
import org.openflexo.foundation.action.RemoveImportedProject;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBMouseEvent;
import org.openflexo.gina.view.FIBView;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.prefs.PresentationPreferences;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.FIBBrowserActionAdapter;

/**
 * Represents the controller of a FIBComponent in Openflexo graphical context (at this time, Swing)<br>
 * Extends FIBController by supporting FlexoController and icon management for Openflexo objects
 * 
 * 
 * @author sylvain
 * 
 * @param <T>
 */
public class FlexoFIBController extends FIBController implements GraphicalFlexoObserver {

	private static final Logger logger = Logger.getLogger(FlexoFIBController.class.getPackage().getName());

	private FlexoController controller;

	public static final ImageIcon ARROW_DOWN = UtilsIconLibrary.ARROW_DOWN_2;
	public static final ImageIcon ARROW_UP = UtilsIconLibrary.ARROW_UP_2;
	public static final ImageIcon ARROW_BOTTOM = UtilsIconLibrary.ARROW_BOTTOM_2;
	public static final ImageIcon ARROW_TOP = UtilsIconLibrary.ARROW_TOP_2;

	/**
	 * This factory is augmented with model entities defined in flexo layer
	 */
	public static FIBModelFactory FLEXO_FIB_FACTORY;

	static {
		try {
			FLEXO_FIB_FACTORY = new FIBModelFactory(FIBBrowserActionAdapter.class);
		} catch (ModelDefinitionException e1) {
			e1.printStackTrace();
		}
	}

	public FlexoFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		// Default parent localizer is the main localizer
		setParentLocalizer(FlexoLocalization.getMainLocalizer());
	}

	public FlexoFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory);
		this.controller = controller;
	}

	@Override
	public void delete() {
		/*if (getDataObject() instanceof FlexoObservable) {
			((FlexoObservable) getDataObject()).deleteObserver(this);
		}
		if (getDataObject() instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) getDataObject()).getPropertyChangeSupport().removePropertyChangeListener(this);
		}*/
		super.delete();
	}

	public FlexoController getFlexoController() {
		return controller;
	}

	public void setFlexoController(FlexoController aController) {
		if (aController != controller) {
			FlexoController oldValue = controller;
			controller = aController;
			getPropertyChangeSupport().firePropertyChange("flexoController", oldValue, aController);
		}
	}

	public FlexoEditor getEditor() {
		if (getFlexoController() != null) {
			return getFlexoController().getEditor();
		}
		return null;
	}

	public SelectionManager getSelectionManager() {
		if (getFlexoController() != null) {
			return getFlexoController().getSelectionManager();
		}
		return null;
	}

	public FlexoServiceManager getServiceManager() {
		if (getFlexoController() != null) {
			return getFlexoController().getApplicationContext();
		}
		return null;
	}

	@Override
	public void update(final FlexoObservable o, final DataModification dataModification) {
		if (isDeleted()) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(o, dataModification);
				}
			});
			return;
		}

		FIBView rv = getRootView();
		if (rv != null) {
			// rv.updateDataObject(getDataObject());
			rv.update();
		}
	}

	public TechnologyAdapterController<?> getTechnologyAdapterController(TechnologyAdapter technologyAdapter) {
		if (getFlexoController() != null) {
			return getFlexoController().getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(technologyAdapter);
		}
		return null;
	}

	@Override
	public void setDataObject(Object anObject) {
		if (anObject != getDataObject()) {
			if (getDataObject() instanceof FlexoObservable) {
				((FlexoObservable) getDataObject()).deleteObserver(this);
			}
			super.setDataObject(anObject);
			if (anObject instanceof FlexoObservable) {
				((FlexoObservable) anObject).addObserver(this);
			}
		}

		logger.fine("Set DataObject with " + anObject);
		super.setDataObject(anObject);
	}

	public void singleClick(Object object) {
		// System.out.println("singleClick with " + object);
		// System.out.println("getFlexoController()=" + getFlexoController());
		if (getFlexoController() != null) {
			getFlexoController().objectWasClicked(object);
		}
	}

	public void doubleClick(Object object) {
		System.out.println("doubleClick with " + object);
		System.out.println("getFlexoController()=" + getFlexoController());
		if (getFlexoController() != null) {
			getFlexoController().objectWasDoubleClicked(object);
		}
	}

	public void rightClick(Object object, FIBMouseEvent e) {
		System.out.println("rightClick with " + object);
		System.out.println("getFlexoController()=" + getFlexoController());
		if (getFlexoController() != null) {
			getFlexoController().objectWasRightClicked(object, e);
		}
	}

	public ImageIcon iconForObject(Object object) {
		if (controller != null) {
			return controller.iconForObject(object);
		}
		else {
			return FlexoController.statelessIconForObject(object);
		}
	}

	/**
	 * Called when a throwable has been raised during model code invocation.
	 * 
	 * @param t
	 * @return true is exception was correctely handled
	 */
	@Override
	public boolean handleException(Throwable t) {
		if (t instanceof InvalidNameException) {
			FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("invalid_name") + " : "
					+ ((InvalidNameException) t).getExplanation());
			return true;
		}
		return super.handleException(t);
	}

	public ImageIcon getArrowDown() {
		return ARROW_DOWN;
	}

	public ImageIcon getArrowUp() {
		return ARROW_UP;
	}

	public ImageIcon getArrowTop() {
		return ARROW_TOP;
	}

	public ImageIcon getArrowBottom() {
		return ARROW_BOTTOM;
	}

	public void importProject(FlexoProject project) {
		// TODO: reimplement this properly when project will be a FlexoProjectObject
		// ImportProject importProject = ImportProject.actionType.makeNewAction(project, null, getEditor());
		ImportProject importProject = ImportProject.actionType.makeNewAction(null, null, getEditor());
		importProject.doAction();
	}

	public void unimportProject(FlexoProject project, List<FlexoProjectReference> references) {
		for (FlexoProjectReference ref : references) {
			// TODO: reimplement this properly when project will be a FlexoProjectObject
			// RemoveImportedProject removeProject = RemoveImportedProject.actionType.makeNewAction(project, null, getEditor());
			RemoveImportedProject removeProject = RemoveImportedProject.actionType.makeNewAction(null, null, getEditor());
			removeProject.setProjectToRemoveURI(ref.getURI());
			removeProject.doAction();
		}
	}

	@Override
	public void performCopyAction(Object focused, List<?> selection) {
		CopyActionType copyActionType = getEditor().getServiceManager().getEditingContext().getCopyActionType();
		FlexoObject focusedObject = null;
		if (focused instanceof FlexoObject) {
			focusedObject = (FlexoObject) focused;
		}
		Vector<FlexoObject> globalSelection = new Vector<FlexoObject>();
		for (Object o : selection) {
			if (o instanceof FlexoObject) {
				globalSelection.add((FlexoObject) o);
				if (focusedObject == null) {
					focusedObject = (FlexoObject) o;
				}
			}
		}
		if (copyActionType.isEnabled(focusedObject, globalSelection)) {
			CopyAction action = copyActionType.makeNewAction(focusedObject, globalSelection, getEditor());
			action.doAction();
		}
	}

	@Override
	public void performCutAction(Object focused, List<?> selection) {
		CutActionType cutActionType = getEditor().getServiceManager().getEditingContext().getCutActionType();
		FlexoObject focusedObject = null;
		if (focused instanceof FlexoObject) {
			focusedObject = (FlexoObject) focused;
		}
		Vector<FlexoObject> globalSelection = new Vector<FlexoObject>();
		for (Object o : selection) {
			if (o instanceof FlexoObject) {
				globalSelection.add((FlexoObject) o);
				if (focusedObject == null) {
					focusedObject = (FlexoObject) o;
				}
			}
		}
		if (cutActionType.isEnabled(focusedObject, globalSelection)) {
			CutAction action = cutActionType.makeNewAction(focusedObject, globalSelection, getEditor());
			action.doAction();
		}
	}

	@Override
	public void performPasteAction(Object focused, List<?> selection) {
		PasteActionType pasteActionType = getEditor().getServiceManager().getEditingContext().getPasteActionType();
		FlexoObject focusedObject = null;
		if (focused instanceof FlexoObject) {
			focusedObject = (FlexoObject) focused;
		}
		Vector<FlexoObject> globalSelection = new Vector<FlexoObject>();
		for (Object o : selection) {
			if (o instanceof FlexoObject) {
				globalSelection.add((FlexoObject) o);
				if (focusedObject == null) {
					focusedObject = (FlexoObject) o;
				}
			}
		}
		if (pasteActionType.isEnabled(focusedObject, globalSelection)) {
			PasteAction action = pasteActionType.makeNewAction(focusedObject, globalSelection, getEditor());
			action.doAction();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " FlexoController=" + getFlexoController();
	}

	private boolean preferencesRegistered = false;

	protected void listenToPresentationPreferences() {
		if (!preferencesRegistered) {
			getFlexoController().getApplicationContext().getPresentationPreferences().getPropertyChangeSupport()
					.addPropertyChangeListener(new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getPropertyName().equals(PresentationPreferences.HIDE_EMPTY_FOLDERS)) {
								// FlexoFIBController.this.getPropertyChangeSupport().firePropertyChange("shouldBeDisplayed(RepositoryFolder)",
								// false, true);
								FlexoFIBController.this.getPropertyChangeSupport()
										.firePropertyChange("shouldBeDisplayed(RepositoryFolder<?,?>)", false, true);
							}
						}
					});
			preferencesRegistered = true;
		}
	}

	public boolean hideEmptyFolders() {
		listenToPresentationPreferences();
		if (getFlexoController() == null) {
			return false;
		}
		return getFlexoController().getApplicationContext().getPresentationPreferences().hideEmptyFolders();
		// return true;
	}

	public boolean shouldBeDisplayed(RepositoryFolder<?, ?> folder) {
		if (folder.isRootFolder()) {
			return true;
		}
		if (!hideEmptyFolders()) {
			return true;
		}
		if (folder.getResources().size() == 0) {
			if (folder.getChildren().size() == 0) {
				return false;
			}
			for (RepositoryFolder<?, ?> childFolder : folder.getChildren()) {
				if (shouldBeDisplayed(childFolder)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
}
