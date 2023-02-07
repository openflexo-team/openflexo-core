/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.view.controller.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.project.InteractiveProjectLoader;
import org.openflexo.swing.layout.MultiSplitLayout.Node;
import org.openflexo.swing.layout.MultiSplitLayoutTypeAdapterFactory;
import org.openflexo.toolbox.ExtendedSet;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.view.controller.FlexoController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link ControllerModel} represents the edition logic used by the {@link FlexoController}
 * 
 * Navigation history is managed here
 * 
 * @author sylvain
 *
 */
public class ControllerModel extends ControllerModelObject implements PropertyChangeListener {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(ControllerModel.class.getPackage().getName());

	public static final String RIGHT_VIEW_VISIBLE = "rightViewVisible";
	public static final String LEFT_VIEW_VISIBLE = "leftViewVisible";
	public static final String LOCATIONS = "locations";
	public static final String PERSPECTIVES = "perspectives";
	public static final String EDITORS = "editors";

	public static final String CURRENT_LOCATION = "currentLocation";
	public static final String CURRENT_OBJECT = "currentObject";
	public static final String CURRENT_EDITOR = "currentEditor";
	public static final String CURRENT_PERSPECTIVE = "currentPerspective";

	private final Location NO_LOCATION;

	/** Gson instance used to serialize and deserialize layouts of the mainpane multisplitpane */
	private Gson gsonLayout;

	/** Flag that indicates the current state of the left view */
	private boolean leftViewVisible = true;

	/** Flag that indicates the current state of the right view */
	private boolean rightViewVisible = true;

	/** The list of all the available perspectives */
	private final List<FlexoPerspective> perspectives;

	/** List of objects that are represented. This list allows to track observed objects. */
	private final List<FlexoObject> objects;

	/** Set of locations that are opened. This set represents all the currently opened module views. */
	private final ExtendedSet<Location> locations;

	/** The stack of locations that were visited */
	private final Stack<Location> previousHistory;

	/**
	 * The stack of locations that are ahead of the current location. This is only used when the user goes back in history. Whenever the
	 * users bypasses history navigation, this stack is reset
	 */
	private final Stack<Location> nextHistory;

	/** The current location in the history */
	private Location currentLocation;

	/** Internal flag that indicates if we are currently moving forward in the history */
	private boolean isGoingForward = false;

	/** Internal flag that indicates if we are currently moving backward in the history */
	private boolean isGoingBackward = false;

	/** The application context of this controller model */
	private final ApplicationContext context;

	/** The module in which this controller model was created (mainly used to save module specific preferences) */
	private final FlexoModule<?> module;

	/** A property change listener registration manager that keeps track of all the registered {@link PropertyChangeListener} */
	private final PropertyChangeListenerRegistrationManager registrationManager;

	public ControllerModel(ApplicationContext context, FlexoModule<?> module) {
		this.context = context;
		this.module = module;
		registrationManager = new PropertyChangeListenerRegistrationManager();

		NO_LOCATION = new Location(null, null, null) {
			@Override
			public FlexoPerspective getPerspective() {
				if (module != null && module.getController() != null) {
					return module.getController().getDefaultPerspective();
				}
				return null;
			}
		};
		currentLocation = NO_LOCATION;

		if (context.getGeneralPreferences() != null) {
			leftViewVisible = context.getPresentationPreferences().getShowLeftView(module.getShortName());
			rightViewVisible = context.getPresentationPreferences().getShowRightView(module.getShortName());
		}

		registrationManager.new PropertyChangeListenerRegistration(InteractiveProjectLoader.PROJECT_OPENED, this,
				context.getProjectLoader());
		registrationManager.new PropertyChangeListenerRegistration(InteractiveProjectLoader.PROJECT_CLOSED, this,
				context.getProjectLoader());
		objects = new ArrayList<>();
		locations = new ExtendedSet<>();
		perspectives = new Vector<>();
		previousHistory = new Stack<>();
		nextHistory = new Stack<>();
	}

	public ModuleLoader getModuleLoader() {
		return context.getModuleLoader();
	}

	public ProjectLoader getProjectLoader() {
		return context.getProjectLoader();
	}

	public FlexoModule<?> getModule() {
		return module;
	}

	@Override
	public void delete() {
		for (FlexoPerspective p : perspectives) {
			p.delete();
		}
		perspectives.clear();
		registrationManager.delete();
		super.delete();
		currentLocation = NO_LOCATION;
	}

	public FlexoController getController() {
		return module.getController();
	}

	/***************
	 * PERSPECTIVE *
	 ***************/

	/**
	 * Returns the current perspective.
	 * 
	 * @return the current perspective
	 */
	public FlexoPerspective getCurrentPerspective() {
		if (currentLocation != null) {
			return currentLocation.getPerspective();
		}
		else {
			return defaultPerspective;
		}
	}

	private FlexoPerspective defaultPerspective = null;

	public void setCurrentPerspective(FlexoPerspective currentPerspective) {

		logger.info("setCurrentPerspective with " + currentPerspective);

		// System.out.println("currentLocation=" + currentLocation);
		// System.out.println("currentPerspective=" + currentLocation.getPerspective());
		// System.out.println("currentObject=" + getCurrentObject());
		// System.out.println(">>>>>>>> SWITCHING to " + currentPerspective);

		if (currentPerspective != null) {
			defaultPerspective = currentPerspective;
		}

		FlexoObject object = getCurrentObject();
		if (currentPerspective != null) {
			currentPerspective.willShow();
			object = currentPerspective.getDefaultObject(object != null ? object : getCurrentProject());
		}
		setCurrentLocation(getCurrentEditor(), object, currentPerspective);
	}

	public List<FlexoPerspective> getPerspectives() {
		return perspectives;
	}

	public void addToPerspectives(FlexoPerspective perspective) {
		if (perspective == null) {
			return;
		}
		perspectives.add(perspective);
		getPropertyChangeSupport().firePropertyChange(PERSPECTIVES, null, perspective);
		if (getCurrentPerspective() == null) {
			setCurrentPerspective(perspective);
		}
	}

	public void removeFromPerspectives(FlexoPerspective perspective) {
		perspectives.remove(perspective);
		getPropertyChangeSupport().firePropertyChange(PERSPECTIVES, perspective, null);
	}

	/***********
	 * EDITORS *
	 ***********/

	public FlexoEditor getCurrentEditor() {
		// if (requiresProject()) {
		if (currentLocation.getEditor() != null) {
			return currentLocation.getEditor();
		}
		else {
			return context.getApplicationEditor();
		}
	}

	/*public boolean requiresProject() {
		return module.getModule().requireProject();
	}*/

	public void setCurrentEditor(FlexoEditor currentEditor) {
		if (currentEditor != getCurrentEditor()) {
			if (currentEditor == null || currentEditor.getProject() == null || isSelectableProject(currentEditor.getProject())) {
				Location location = getLastLocationForEditor(currentEditor, null);
				if (location != null && location != NO_LOCATION) {
					setCurrentLocation(location);
				}
				else {
					FlexoObject object = null;
					if (getCurrentEditor() == null && currentEditor != null && currentEditor.getProject() != null
							&& getCurrentPerspective() != null) {
						object = getCurrentPerspective().getDefaultObject(currentEditor.getProject());
					}
					setCurrentLocation(currentEditor, object, getCurrentPerspective());
				}
			}
		}
	}

	public FlexoProject<?> getCurrentProject() {
		return getCurrentEditor() != null ? getCurrentEditor().getProject() : null;
	}

	public void setCurrentProject(FlexoProject<?> project) {
		setCurrentEditor(project != null ? context.getProjectLoader().getEditorForProject(project) : null);
	}

	public boolean isSelectableProject(FlexoProject<?> project) {
		return context.getProjectLoader().getRootProjects().contains(project);
	}

	/**********************
	 * NAVIGATION HISTORY *
	 **********************/

	public FlexoObject getCurrentObject() {
		return currentLocation != null ? currentLocation.getObject() : null;
	}

	public void setCurrentObject(FlexoObject object) {
		// Little block to change the currentPerspective if the
		// current perspective can't handle this object
		FlexoPerspective perspective = getCurrentPerspective();

		if (object == null || perspective.getRepresentableMasterObject(object) == null) {
			for (FlexoPerspective p : getPerspectives()) {
				if (p == null) {
					continue;
				}
				if (p.getRepresentableMasterObject(object) != null) {
					perspective = p;
					break;
				}
			}
		}
		setCurrentLocation(getCurrentEditor(), object, perspective);
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location location) {
		if (location != null && location.equals(getCurrentLocation())) {
			return;
		}
		else if (location != null) {
			if (location == NO_LOCATION) {
				Location old = currentLocation;
				currentLocation = NO_LOCATION;
				notifyLocationChange(old, currentLocation);

				return;
			}
			else {
				setCurrentLocation(location.getEditor(), location.getObject(), location.getPerspective());
				defaultPerspective = location.getPerspective();

			}
		}
	}

	public Set<Location> getLocations() {
		return Collections.unmodifiableSet(locations);
	}

	/**
	 * Adds the specified location to the set of locations if it is not yet in the set. The method returns a location which is equal to the
	 * specified location and which is in the set (ie, if the location was not yet in the set, it returns the specified location, else it
	 * returns the locations that was previously contained).
	 * 
	 * @param location
	 *            the location to add.
	 * @return the location which is contained in the set.
	 */
	public Location addToLocations(Location location) {
		Location existing = locations.get(location);
		if (existing == null) {
			locations.add(existing = location);
			getPropertyChangeSupport().firePropertyChange(LOCATIONS, null, existing = location);
		}
		return existing;
	}

	public boolean removeFromLocations(Location location) {

		// System.out.println("On supprime la location pour " + location.getEditor().getProject() + " object=" + location.getObject()
		// + " perspective=" + location.getPerspective());

		boolean removed = locations.remove(location);
		if (removed) {
			if (location != null && location.equals(currentLocation)) {
				Location lastLocationForEditor = getLastLocationForEditor(getCurrentEditor(), getCurrentPerspective());
				// System.out.println("Et on passe a la location pour "
				// + (lastLocationForEditor.getEditor() != null ? lastLocationForEditor.getEditor().getProject() : null) + " object="
				// + lastLocationForEditor.getObject() + " perspective=" + lastLocationForEditor.getPerspective());
				setCurrentLocation(lastLocationForEditor);
			}
			getPropertyChangeSupport().firePropertyChange(LOCATIONS, location, null);
		}
		return removed;
	}

	public void setCurrentLocation(FlexoEditor editor, FlexoObject object, FlexoPerspective perspective) {
		if (isDeleted()) {
			return;
		}
		/*if (editor == null) {
			editor = getCurrentEditor();
		}*/
		if (perspective == null) {
			perspective = getCurrentPerspective();
		}
		if (!isGoingForward && !isGoingBackward) {
			if (currentLocation != null && currentLocation != NO_LOCATION) {
				previousHistory.push(currentLocation);
			}
			nextHistory.clear();
		}
		if (object != null) {
			if (getCurrentObject() != object) {
				if (!objects.contains(object)) {
					registrationManager.new PropertyChangeListenerRegistration(object.getDeletedProperty(), this, object);
					objects.add(object);
				}
			}
		}
		Location old = currentLocation;

		// System.out.println("******* Adding location editor=" + editor + " object=" + object + " perspective=" + perspective);
		currentLocation = addToLocations(new Location(editor, object, perspective));
		notifyLocationChange(old, currentLocation);
	}

	private void notifyLocationChange(Location old, Location newLocation) {

		if (old == null || old.getEditor() != currentLocation.getEditor()) {
			getPropertyChangeSupport().firePropertyChange(CURRENT_EDITOR, old != null ? old.getEditor() : null,
					currentLocation.getEditor());
		}
		if (old == null || old.getPerspective() != currentLocation.getPerspective()) {
			getPropertyChangeSupport().firePropertyChange(CURRENT_PERSPECTIVE, old != null ? old.getPerspective() : null,
					currentLocation.getPerspective());
		}
		if (old == null || old.getObject() != currentLocation.getObject()) {
			getPropertyChangeSupport().firePropertyChange(CURRENT_OBJECT, old != null ? old.getObject() : null,
					currentLocation.getObject());
		}
		getPropertyChangeSupport().firePropertyChange(CURRENT_LOCATION, old, currentLocation);
	}

	public Stack<Location> getNextHistory() {
		return nextHistory;
	}

	public Stack<Location> getPreviousHistory() {
		return previousHistory;
	}

	public boolean canGoForward() {
		return nextHistory.size() > 0;
	}

	public boolean canGoBack() {
		return previousHistory.size() > 0;
	}

	public boolean canGoUp() {
		return getCurrentObject() != null && getParent(getCurrentObject()) != null;
	}

	private static <E> List<E> union(final List<? extends E> list1, final List<? extends E> list2) {
		final ArrayList<E> result = new ArrayList<>(list1.size() + list2.size());
		result.addAll(list1);
		result.addAll(list2);
		return result;
	}

	private Location getLastLocationForEditor(FlexoEditor editor, FlexoPerspective perspective) {
		List<Location> allLocations = union(previousHistory, nextHistory);
		if (perspective != null) {
			// avoid empty perspective if there is one to select
			for (Location location : allLocations) {
				if (filterEditor(editor, location) && location.getPerspective() == perspective && isLocationAvailable(location)) {
					return location;
				}
			}
		}
		else {
			for (Location location : allLocations) {
				if (filterEditor(editor, location) && isLocationAvailable(location)) {
					return location;
				}
			}
		}
		/*if (editor != null) {
			System.out.println("Comme lastLocation, je retourne " + (editor != null ? editor.getProject() : null));
			return new Location(editor, null, perspective);
		}*/
		return NO_LOCATION;
	}

	private static boolean filterEditor(FlexoEditor editor, Location location) {
		return editor == null || location.getEditor() == editor;
	}

	private boolean isLocationAvailable(Location location) {
		return locations.contains(location);
	}

	private static FlexoObject getParent(FlexoObject object) {
		// Please reimplement this
		return null;
	}

	public void historyBack() {
		if (canGoBack()) {
			isGoingBackward = true;
			try {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Back to " + previousHistory.peek());
				}
				nextHistory.push(currentLocation);
				Location nextLocation = previousHistory.pop();
				setCurrentLocation(nextLocation);
			} finally {
				isGoingBackward = false;
			}
		}
	}

	public void historyForward() {
		if (canGoForward()) {
			isGoingForward = true;
			try {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Forward to " + nextHistory.peek());
				}
				previousHistory.push(currentLocation);
				Location nextLocation = nextHistory.pop();
				setCurrentLocation(nextLocation);
			} finally {
				isGoingForward = false;
			}
		}
	}

	public void goUp() {
		if (canGoUp()) {
			setCurrentObject(getParent(getCurrentObject()));
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getProjectLoader()) {
			if (evt.getPropertyName().equals(InteractiveProjectLoader.PROJECT_OPENED)) {
				FlexoProject<?> project = (FlexoProject<?>) evt.getNewValue();
				if (getCurrentPerspective() != null) {
					FlexoObject object = getCurrentPerspective().getDefaultObject(project);
					logger.info("Displaying project " + project);
					logger.info("Object " + object);
					logger.info("Editor: " + getProjectLoader().getEditorForProject(project));
					setCurrentLocation(getProjectLoader().getEditorForProject(project), object, getCurrentPerspective());
				}
				else {
					setCurrentProject(project);
				}
			}
			else if (evt.getPropertyName().equals(InteractiveProjectLoader.PROJECT_CLOSED)) {
				handleProjectRemoval((FlexoProject<?>) evt.getOldValue());
				setCurrentProject(null);
			}
		}
		else if (evt.getOldValue() instanceof FlexoObject
				&& evt.getPropertyName().equals(((FlexoObject) evt.getOldValue()).getDeletedProperty())) {
			handleObjectDeletion((FlexoObject) evt.getOldValue());
		}
	}

	private void handleProjectRemoval(FlexoProject<?> removedProject) {
		updateHistoryForProjectRemoval(previousHistory, removedProject);
		updateHistoryForProjectRemoval(nextHistory, removedProject);
		for (Location location : new ArrayList<>(locations)) {
			if (location.getEditor() != null && location.getEditor().getProject() == removedProject) {
				removeFromLocations(location);
			}
		}
	}

	private static void updateHistoryForProjectRemoval(Stack<Location> history, FlexoProject<?> removedProject) {
		Iterator<Location> i = history.iterator();
		while (i.hasNext()) {
			Location hl = i.next();
			if (hl.getEditor() != null && hl.getEditor().getProject() == removedProject) {
				i.remove();
			}
		}
	}

	private void handleObjectDeletion(FlexoObject deletedObject) {
		while (objects.remove(deletedObject)) {
			;
		}
		updateHistoryForDeletedObject(previousHistory, deletedObject);
		updateHistoryForDeletedObject(nextHistory, deletedObject);
		for (Location location : new ArrayList<>(locations)) {
			if (location.getObject() == deletedObject) {
				removeFromLocations(location);
			}
		}
		if (currentLocation != null && currentLocation.getObject() == deletedObject) {
			if (canGoBack()) {
				historyBack();
			}
			else {
				setCurrentObject(null);
			}
		}
		registrationManager.removeListener(deletedObject.getDeletedProperty(), this, deletedObject);
	}

	private static void updateHistoryForDeletedObject(Stack<Location> history, FlexoObject deletedObject) {
		Iterator<Location> i = history.iterator();
		while (i.hasNext()) {
			Location hl = i.next();
			if (hl.getObject() == deletedObject) {
				i.remove();
			}
		}
	}

	/**********
	 * LAYOUT *
	 **********/

	public Node<?> getLayoutForPerspective(FlexoPerspective perspective) {

		String layout = null;
		if (context.getGeneralPreferences() != null) {
			layout = context.getPresentationPreferences().getLayoutFor(getModule().getShortName() + perspective.getName());
		}
		if (layout != null) {
			// System.out.println("Parsing layout: " + layout);
			return getLayoutFromString(layout);
		}
		else {
			return null;
		}
	}

	private Node<?> getLayoutFromString(String layout) {
		return getGsonLayout().fromJson(layout, Node.class);
	}

	public void setLayoutForPerspective(FlexoPerspective perspective, Node<?> layout) {
		context.getPresentationPreferences().setLayoutFor(getGsonLayout().toJson(layout),
				getModule().getShortName() + perspective.getName());
		context.getPreferencesService().savePreferences();
	}

	private Gson getGsonLayout() {
		if (gsonLayout == null) {
			GsonBuilder builder = new GsonBuilder().registerTypeAdapterFactory(new MultiSplitLayoutTypeAdapterFactory());
			gsonLayout = builder.create();
		}
		return gsonLayout;
	}

	/**************
	 * VISIBILITY *
	 **************/

	public boolean isLeftViewVisible() {
		return leftViewVisible;
	}

	public void setLeftViewVisible(boolean leftViewVisible) {
		this.leftViewVisible = leftViewVisible;
		context.getPresentationPreferences().setShowLeftView(getModule().getShortName(), leftViewVisible);
		context.getPreferencesService().savePreferences();
		getPropertyChangeSupport().firePropertyChange(LEFT_VIEW_VISIBLE, !leftViewVisible, leftViewVisible);
	}

	public boolean isRightViewVisible() {
		return rightViewVisible;
	}

	public void setRightViewVisible(boolean rightViewVisible) {
		this.rightViewVisible = rightViewVisible;
		context.getPresentationPreferences().setShowRightView(getModule().getShortName(), rightViewVisible);
		context.getPreferencesService().savePreferences();
		getPropertyChangeSupport().firePropertyChange(RIGHT_VIEW_VISIBLE, !rightViewVisible, rightViewVisible);
	}

}
