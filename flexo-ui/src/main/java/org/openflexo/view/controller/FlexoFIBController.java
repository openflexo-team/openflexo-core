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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.openflexo.ApplicationContext;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.action.ImportProject;
import org.openflexo.foundation.action.RemoveImportedProject;
import org.openflexo.foundation.action.copypaste.CopyAction;
import org.openflexo.foundation.action.copypaste.CopyAction.CopyActionType;
import org.openflexo.foundation.action.copypaste.CutAction;
import org.openflexo.foundation.action.copypaste.CutAction.CutActionType;
import org.openflexo.foundation.action.copypaste.PasteAction;
import org.openflexo.foundation.action.copypaste.PasteAction.PasteActionType;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.action.MoveVirtualModelToContainerVirtualModel;
import org.openflexo.foundation.fml.action.MoveVirtualModelToDirectory;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTValidationReport;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.project.FlexoProjectReference;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.controller.FIBSelectable;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBMouseEvent;
import org.openflexo.gina.swing.utils.FIBUtilsIconLibrary;
import org.openflexo.gina.view.FIBView;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.pamela.validation.ConsistencySuccessfullyChecked;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.InformationIssue;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.prefs.PresentationPreferences;
import org.openflexo.selection.SelectionManager;
import org.openflexo.toolbox.FileUtils;

/**
 * Represents the controller of a FIBComponent in Openflexo graphical context (at this time, Swing)<br>
 * Extends FIBController by supporting FlexoController and icon management for Openflexo objects
 * 
 * 
 * @author sylvain
 * 
 * @param <T>
 */
public class FlexoFIBController extends FIBController implements GraphicalFlexoObserver, PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(FlexoFIBController.class.getPackage().getName());

	private FlexoController controller;

	public static final ImageIcon ARROW_DOWN = UtilsIconLibrary.ARROW_DOWN_2;
	public static final ImageIcon ARROW_UP = UtilsIconLibrary.ARROW_UP_2;
	public static final ImageIcon ARROW_BOTTOM = UtilsIconLibrary.ARROW_BOTTOM_2;
	public static final ImageIcon ARROW_TOP = UtilsIconLibrary.ARROW_TOP_2;

	public FlexoFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		// Default parent localizer is the main localizer
		setParentLocalizer(FlexoLocalization.getMainLocalizer());
	}

	public FlexoFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory);
		setFlexoController(controller);
	}

	@Override
	public void delete() {
		for (ValidationReport report : observedReports) {
			report.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		observedReports.clear();
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

	public ApplicationContext getServiceManager() {
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
			SwingUtilities.invokeLater(() -> update(o, dataModification));
			return;
		}

		FIBView<?, ?> rv = getRootView();
		if (rv != null) {
			// rv.updateDataObject(getDataObject());
			rv.update();
		}
	}

	public <TA extends TechnologyAdapter<TA>> TechnologyAdapterController<TA> getTechnologyAdapterController(TA technologyAdapter) {
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
		if (getFlexoController() != null) {
			getFlexoController().objectWasClicked(object);
		}
	}

	public void doubleClick(Object object) {
		if (getFlexoController() != null) {
			getFlexoController().objectWasDoubleClicked(object);
		}
	}

	public void rightClick(Object object, FIBMouseEvent e) {
		if (getFlexoController() != null) {
			getFlexoController().objectWasRightClicked(object, e);
		}
	}

	private Map<Object, ImageIcon> cachedIcons = new HashMap<>();

	@NotificationUnsafe
	public final ImageIcon iconForObject(Object object) {

		if (object instanceof FlexoResource && ((FlexoResource<?>) object).isLoaded()) {
			return iconForObject(((FlexoResource<?>) object).getLoadedResourceData());
		}

		ImageIcon returned = cachedIcons.get(object);
		if (returned == null) {
			returned = retrieveIconForObject(object);

			if (object instanceof Validable && hasValidationReport((Validable) object)) {
				if (hasErrors((Validable) object)) {
					returned = IconFactory.getImageIcon(returned, IconLibrary.ERROR);
				}
				else if (hasWarnings((Validable) object)) {
					returned = IconFactory.getImageIcon(returned, IconLibrary.WARNING);
				}
				cachedIcons.put(object, returned);
			}
		}
		return returned;
	}

	protected void clearCachedIcons() {
		cachedIcons.clear();
	}

	public boolean hasValidationReport(Validable object) {
		return getValidationReport(object) != null;
	}

	public boolean hasErrors(Validable object) {
		ValidationReport validationReport = getValidationReport(object);
		if (object instanceof FMLObject && validationReport instanceof FMLValidationReport) {
			return ((FMLValidationReport) validationReport).hasErrors((FMLObject) object);
		}
		if (object instanceof VirtualModelInstanceObject && validationReport instanceof FMLRTValidationReport) {
			return ((FMLRTValidationReport) validationReport).hasErrors((VirtualModelInstanceObject) object);
		}
		return false;
	}

	public boolean hasWarnings(Validable object) {
		ValidationReport validationReport = getValidationReport(object);
		if (object instanceof FMLObject && validationReport instanceof FMLValidationReport) {
			return ((FMLValidationReport) validationReport).hasWarnings((FMLObject) object);
		}
		if (object instanceof VirtualModelInstanceObject && validationReport instanceof FMLRTValidationReport) {
			return ((FMLRTValidationReport) validationReport).hasWarnings((VirtualModelInstanceObject) object);
		}
		return false;
	}

	/*public ValidationReport getValidationReport(Validable object) {
		if (getServiceManager() != null && object instanceof TechnologyObject) {
			TechnologyAdapter ta = ((TechnologyObject<?>) object).getTechnologyAdapter();
			TechnologyAdapterController<?> tac = getServiceManager().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			if (tac != null) {
				if (object instanceof ResourceData) {
					return tac.getValidationReport((ResourceData<?>) object);
				}
				if (object instanceof InnerResourceData) {
					//System.out.println("Pour l'objet: " + object);
					//System.out.println("On retourne le ValidationReport "
					//		+ tac.getValidationReport(((InnerResourceData<?>) object).getResourceData()));
					return tac.getValidationReport(((InnerResourceData<?>) object).getResourceData());
				}
			}
		}
		return null;
	}*/

	public ValidationReport getValidationReport(Validable object) {
		if (getServiceManager() != null) {
			TechnologyAdapter ta = null;
			if (object instanceof TechnologyObject) {
				ta = ((TechnologyObject<?>) object).getTechnologyAdapter();
			}
			else if (object instanceof VirtualModelInstanceObject) {
				ta = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			if (ta != null) {
				TechnologyAdapterController<?> tac = getServiceManager().getTechnologyAdapterControllerService()
						.getTechnologyAdapterController(ta);
				if (tac != null) {
					if (object instanceof ResourceData) {
						return tac.getValidationReport((ResourceData<?>) object, !SwingUtilities.isEventDispatchThread());
					}
					if (object instanceof InnerResourceData) {
						return tac.getValidationReport(((InnerResourceData<?>) object).getResourceData(),
								!SwingUtilities.isEventDispatchThread());
					}
				}
			}
		}
		return null;
	}

	public ValidationModel getValidationModel(Validable object) {
		if (object instanceof TechnologyObject) {
			TechnologyAdapter ta = ((TechnologyObject<?>) object).getTechnologyAdapter();
			TechnologyAdapterController<?> tac = getServiceManager().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			if (object instanceof ResourceData) {
				return tac.getValidationModel((Class) object.getClass());
			}
			if (object instanceof InnerResourceData) {
				return tac.getValidationModel((Class) ((InnerResourceData<?>) object).getResourceData().getClass());
			}
		}
		return null;
	}

	private final List<ValidationReport> observedReports = new ArrayList<>();

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof ValidationReport) {
			clearCachedIcons();
		}
	}

	protected ImageIcon retrieveIconForObject(Object object) {
		if (object instanceof ValidationError) {
			if (((ValidationError<?, ?>) object).isFixable()) {
				return FIBUtilsIconLibrary.FIXABLE_ERROR_ICON;
			}
			else {
				return FIBUtilsIconLibrary.UNFIXABLE_ERROR_ICON;
			}
		}
		else if (object instanceof ValidationWarning) {
			if (((ValidationWarning<?, ?>) object).isFixable()) {
				return FIBUtilsIconLibrary.FIXABLE_WARNING_ICON;
			}
			else {
				return FIBUtilsIconLibrary.UNFIXABLE_WARNING_ICON;
			}
		}
		else if (object instanceof ConsistencySuccessfullyChecked) {
			return IconLibrary.VALID_ICON;
		}
		else if (object instanceof InformationIssue) {
			return FIBUtilsIconLibrary.INFO_ISSUE_ICON;
		}
		else if (object instanceof FixProposal) {
			return FIBUtilsIconLibrary.FIX_PROPOSAL_ICON;
		}

		if (object instanceof Validable) {
			ValidationReport report = getValidationReport((Validable) object);
			if (report != null) {
				if (!observedReports.contains(report)) {
					report.getPropertyChangeSupport().addPropertyChangeListener(this);
					observedReports.add(report);
				}
			}
		}

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

	public void importProject(FlexoProject<?> project) {
		// TODO: reimplement this properly when project will be a FlexoProjectObject
		// ImportProject importProject = ImportProject.actionType.makeNewAction(project, null, getEditor());
		ImportProject importProject = ImportProject.actionType.makeNewAction(null, null, getEditor());
		importProject.doAction();
	}

	public void unimportProject(FlexoProject<?> project, List<FlexoProjectReference> references) {
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
		Vector<FlexoObject> globalSelection = new Vector<>();
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
		Vector<FlexoObject> globalSelection = new Vector<>();
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
		Vector<FlexoObject> globalSelection = new Vector<>();
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
		if (getFlexoController() != null && getFlexoController().getApplicationContext() != null) {
			if (!preferencesRegistered) {
				getFlexoController().getApplicationContext().getPresentationPreferences().getPropertyChangeSupport()
						.addPropertyChangeListener(evt -> {
							if (evt.getPropertyName().equals(PresentationPreferences.HIDE_EMPTY_FOLDERS)) {
								// FlexoFIBController.this.getPropertyChangeSupport().firePropertyChange("shouldBeDisplayed(RepositoryFolder)",
								// false, true);
								FlexoFIBController.this.getPropertyChangeSupport()
										.firePropertyChange("shouldBeDisplayed(RepositoryFolder<?,?>)", false, true);
							}
						});
				preferencesRegistered = true;
			}
		}
	}

	public boolean hideEmptyFolders() {
		listenToPresentationPreferences();
		FlexoController ctrl = getFlexoController();
		if (ctrl == null) {
			return false;
		}
		ApplicationContext applicationContext = ctrl.getApplicationContext();
		if (applicationContext == null)
			return false;

		PresentationPreferences presPref = applicationContext.getPresentationPreferences();
		if (presPref == null)
			return false;

		return presPref.hideEmptyFolders();
	}

	@NotificationUnsafe
	public boolean shouldBeDisplayed(RepositoryFolder<?, ?> folder) {
		// Folders representing a VirtualModel should not be displayed here
		if (folder.getName().endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
			return false;
		}
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
			for (RepositoryFolder<?, ?> childFolder : new ArrayList<>(folder.getChildren())) {
				if (shouldBeDisplayed(childFolder)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private SelectionManager localSelectionManager;
	private boolean isLocalSelectionManagerUpdating = false;

	/**
	 * Return a local {@link SelectionManager}
	 * 
	 * When not already existant, instantiate a new {@link SelectionManager} - not bound to the {@link FlexoController}' selection manager A
	 * local selection manager might be used to synchronize the selection of two widgets, inside a component (a view), independantly from
	 * the main selection manager
	 * 
	 * @return
	 */
	public SelectionManager getLocalSelectionManager() {
		if (localSelectionManager == null) {
			localSelectionManager = new SelectionManager(null) {

				@Override
				public FlexoObject getRootFocusedObject() {
					// not required here
					return null;
				}

				@Override
				public void addToSelected(FlexoObject object) {
					super.addToSelected(object);
					if (!isLocalSelectionManagerUpdating) {
						objectAddedToSelection(object);
					}
				}

				@Override
				public void removeFromSelected(FlexoObject object) {
					super.removeFromSelected(object);
					if (!isLocalSelectionManagerUpdating) {
						objectRemovedFromSelection(object);
					}
				}

				@Override
				public void resetSelection() {
					super.resetSelection();
					if (!isLocalSelectionManagerUpdating) {
						selectionCleared();
					}
				}

				@Override
				public void resetSelection(boolean temporary) {
					super.resetSelection(temporary);
					if (!isLocalSelectionManagerUpdating) {
						selectionCleared();
					}
				}
			};
		}
		return localSelectionManager;
	}

	// Notice that T should extends FlexoObject but as we do not want that gina depends on foundation, it is not done
	@Override
	public <T> void updateSelection(FIBSelectable<T> widget, List<T> oldSelection, List<T> newSelection) {
		super.updateSelection(widget, oldSelection, newSelection);
		if (localSelectionManager != null) {
			isLocalSelectionManagerUpdating = true;
			localSelectionManager.setSelectedObjects((List<FlexoObject>) newSelection);
			isLocalSelectionManagerUpdating = false;
		}
	}

	public boolean isVisibleInBrowser(FlexoResource<?> resource) {
		if (resource instanceof FlexoProjectResource && ((FlexoProjectResource<?>) resource).isStandAlone()) {
			return false;
		}
		return !resource.isDeleted();
	}

	public void moveVirtualModelInFolder(CompilationUnitResource vmResource, RepositoryFolder receiver) {
		MoveVirtualModelToDirectory action = MoveVirtualModelToDirectory.actionType
				.makeNewAction(vmResource.getCompilationUnit().getVirtualModel(), null, getEditor());
		action.setNewFolder(receiver);
		action.doAction();
	}

	public boolean canMoveVirtualModelInFolder(CompilationUnitResource vmResource, RepositoryFolder receiver) {
		RepositoryFolder currentFolder = vmResource.getResourceCenter().getRepositoryFolder(vmResource);
		if (currentFolder != null && currentFolder == receiver) {
			return false;
		}
		if (vmResource.getIODelegate() instanceof DirectoryBasedIODelegate && receiver.getSerializationArtefact() instanceof File) {
			File virtualModelDirectory = ((DirectoryBasedIODelegate) vmResource.getIODelegate()).getDirectory();
			if (FileUtils.isFileContainedIn((File) receiver.getSerializationArtefact(), virtualModelDirectory)) {
				return false;
			}
			return true;
		}

		return false;
	}

	public void moveVirtualModelInVirtualModel(CompilationUnitResource vmResource, CompilationUnitResource container) {
		MoveVirtualModelToContainerVirtualModel action = MoveVirtualModelToContainerVirtualModel.actionType
				.makeNewAction(vmResource.getCompilationUnit().getVirtualModel(), null, getEditor());
		action.setContainerResource(container);
		action.doAction();
	}

	public boolean canMoveVirtualModelInVirtualModel(CompilationUnitResource vmResource, CompilationUnitResource container) {
		if (vmResource.getContainer() == container) {
			return false;
		}
		if (vmResource.getIODelegate() instanceof DirectoryBasedIODelegate
				&& container.getIODelegate() instanceof DirectoryBasedIODelegate) {
			File virtualModelDirectory = ((DirectoryBasedIODelegate) vmResource.getIODelegate()).getDirectory();
			if (FileUtils.isFileContainedIn(((DirectoryBasedIODelegate) container.getIODelegate()).getDirectory(), virtualModelDirectory)) {
				return false;
			}
			return true;
		}

		return false;
	}

}
