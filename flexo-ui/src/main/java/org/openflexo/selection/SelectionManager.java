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

package org.openflexo.selection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.inspector.EmptySelection;
import org.openflexo.inspector.MultipleSelection;
import org.openflexo.inspector.UniqueSelection;
import org.openflexo.view.controller.FlexoController;

/**
 * Abstract selection manager: there is one SelectionManager for each FlexoModule. This manager is responsible of:
 * <UL>
 * <LI>Selection management: it holds a list of all currently selected objects, as FlexoObject instances</LI>
 * <LI>Synchonization of all components synchronized with itself objects, as FlexoObject instances</LI>
 * <LI>Focused and last selected objects management</LI>
 * <LI>Inspection management</LI>
 * <LI>Copy/Cut/Paste/SelectAll management by storing and managing a {@link org.openflexo.foundation.action.copypaste.FlexoClipboard}</LI>
 * </UL>
 * 
 * @author bmangez, sguerin
 */
public abstract class SelectionManager extends Observable {

	private static final Logger logger = Logger.getLogger(SelectionManager.class.getPackage().getName());

	// protected FlexoClipboard _clipboard;

	private FlexoObject _inspectedObject;

	protected FocusableView _focusedPanel;

	private FlexoObject lastSelectedObject;

	private final FlexoController _controller;

	protected ContextualMenuManager _contextualMenuManager;

	/**
	 * This represents the selection, as a Vector of FlexoObject
	 */
	private final Vector<FlexoObject> _selection;

	/**
	 * This represents all registered SelectionListener instances
	 */
	private final Vector<SelectionListener> _selectionListeners;

	private final Hashtable<String, Object> inspectionContext;

	public static final String MODULE_KEY = "MODULE";

	// ==========================================================================
	// ============================= Constructor
	// ================================
	// ==========================================================================

	public SelectionManager(FlexoController controller) {
		super();
		_controller = controller;
		_selection = new Vector<>();
		_selectionListeners = new Vector<>();

		inspectionContext = new Hashtable<>();
		if (getController() != null) {
			inspectionContext.put(MODULE_KEY, getController().getModule().getClass().getName());
		}
	}

	public FlexoController getController() {
		return _controller;
	}

	public FlexoEditor getEditor() {
		return _controller.getEditor();
	}

	@Override
	public void addObserver(Observer o) {
		super.addObserver(o);
		// (new Exception("ici")).printStackTrace();
	}

	// ==========================================================================
	// =================== Selection Management, public A.P.I
	// ===================
	// ==========================================================================

	public void addToSelectionListeners(SelectionListener listener) {
		if (!_selectionListeners.contains(listener)) {
			_selectionListeners.add(listener);
		}
	}

	public void removeFromSelectionListeners(SelectionListener listener) {
		_selectionListeners.remove(listener);
	}

	public void addToSelectionListeners(List<SelectionListener> listeners) {
		for (SelectionListener listener : listeners) {
			addToSelectionListeners(listener);
		}
	}

	public void removeFromSelectionListeners(List<SelectionListener> listeners) {
		_selectionListeners.removeAll(listeners);
	}

	public int getSelectionListenersCount() {
		return _selectionListeners.size();
	}

	public void debugSelectionListeners() {
		for (SelectionListener sl : _selectionListeners) {
			System.out.println(" * " + sl);
		}
	}

	public int getSelectionSize() {
		return getSelection().size();
	}

	/**
	 * Return the current selection, as a Vector of FlexoObject
	 * 
	 * @return a Vector of FlexoObject
	 */
	public Vector<FlexoObject> getSelection() {
		return _selection;
	}

	/**
	 * Returns boolean indicating if current selection contains supplied object
	 * 
	 * @param object
	 * @return a boolean
	 */
	public boolean selectionContains(FlexoObject object) {
		return _selection.contains(object);
	}

	/**
	 * Set supplied object to be the current selection
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	public void setSelectedObject(FlexoObject object) {
		if (getSelectionSize() == 1 && getSelection().get(0) == object) {
			// No need, return
			return;
		}
		// System.out.println("Switch from " + getSelection() + " to " + object);
		resetSelection();
		addToSelected(object);
	}

	/**
	 * Reset selection
	 */
	public void resetSelection() {
		resetSelection(false);
	}

	/**
	 * Reset selection<br>
	 * When temporary set to true, skip inspector management
	 */
	public void resetSelection(boolean temporary) {
		_selection.clear();
		for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
			e.nextElement().fireResetSelection();
		}
		setLastSelectedObject(null);
		fireSelectionBecomesEmpty();
		if (!temporary) {
			updateInspectorManagement();
		}
	}

	/**
	 * Add supplied object to current selection
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	public void addToSelected(FlexoObject object) {
		// logger.info(">>>>>>> addToSelected: " + object);
		internallyAddToSelected(object, true);
		updateInspectorManagement();
	}

	/**
	 * Remove supplied object from current selection
	 * 
	 * @param object
	 *            : the object to remove from selection
	 */
	public void removeFromSelected(FlexoObject object) {
		// logger.info(">>>>>>>> removeFromSelected: " + object);
		internallyRemoveFromSelected(object);
		updateInspectorManagement();
	}

	/**
	 * Add supplied object to current selection
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	public void updateSelectionForMaster(SelectionSynchronizedComponent master) {
		for (Enumeration<FlexoObject> en = getSelection().elements(); en.hasMoreElements();) {
			FlexoObject obj = en.nextElement();
			if (!master.mayRepresents(obj)) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Removing " + obj + " because master view is not able to show it");
				}
				internallyRemoveFromSelected(obj);
			}
			else {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Keeping " + obj + " because master view is able to show it");
				}
			}
		}
		updateInspectorManagement();
	}

	/**
	 * Add supplied objects to current selection
	 * 
	 * @param objects
	 *            : objects to add to selection, as a Vector of FlexoObject
	 */
	public void addToSelected(List<? extends FlexoObject> objects) {
		if (objects == null || objects.isEmpty()) {
			return;
		}
		for (SelectionListener sl : _selectionListeners) {
			sl.fireBeginMultipleSelection();
		}
		for (FlexoObject nextObj : objects) {
			internallyAddToSelected(nextObj, objects.size() == 1);
		}
		for (SelectionListener sl : _selectionListeners) {
			sl.fireEndMultipleSelection();
		}
		updateInspectorManagement();
	}

	/**
	 * Remove supplied objects from current selection
	 * 
	 * @param objects
	 *            : objects to remove from selection, as a Vector of FlexoObject
	 */
	public void removeFromSelected(Vector<? extends FlexoObject> objects) {
		if (objects == null || objects.isEmpty()) {
			return;
		}
		for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
			SelectionListener sl = e.nextElement();
			sl.fireBeginMultipleSelection();
		}
		internallyRemoveFromSelected(objects);
		for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
			SelectionListener sl = e.nextElement();
			sl.fireEndMultipleSelection();
		}
		updateInspectorManagement();
	}

	/**
	 * Sets supplied vector of FlexoObjects to be the current Selection
	 * 
	 * @param objects
	 *            : the object to set for current selection, as a Vector of FlexoObject
	 */
	public void setSelectedObjects(List<? extends FlexoObject> objects) {
		resetSelection();
		addToSelected(objects);
	}

	private FlexoObject _focusedObject;

	/**
	 * Return currently focused object
	 */
	public FlexoObject getFocusedObject() {
		return _focusedObject;
	}

	/**
	 * Sets currently focused object
	 */
	protected void setFocusedObject(FlexoObject focusedObject) {
		_focusedObject = focusedObject;
	}

	public FlexoObject getLastSelectedObject() {
		return lastSelectedObject;
	}

	public void setLastSelectedObject(FlexoObject lastSelectedObject) {
		this.lastSelectedObject = lastSelectedObject;
	}

	/**
	 * Request to all registered SelectionListener of this SelectionManager to be back-synchronized with current selection
	 */
	public void fireUpdateSelection() {
		for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
			SelectionListener sl = e.nextElement();
			fireUpdateSelection(sl);
		}
	}

	/**
	 * Request to supplied SelectionListener of this SelectionManager to be back-synchronized with current selection
	 */
	public void fireUpdateSelection(SelectionListener selectionListenerToSynchronize) {
		selectionListenerToSynchronize.fireResetSelection();
		for (Enumeration<FlexoObject> e = getSelection().elements(); e.hasMoreElements();) {
			FlexoObject o = e.nextElement();
			if (!o.isDeleted()) {
				selectionListenerToSynchronize.fireObjectSelected(o);
			}
		}

	}

	// ==========================================================================
	// ================ Selection Management, protected A.P.I
	// ===================
	// ==========================================================================

	/**
	 * Provides a hook called when selection becomes empty
	 * 
	 */
	protected void fireSelectionBecomesEmpty() {
		// _clipboard.setCopyEnabled(false);
		// _clipboard.setCutEnabled(false);
	}

	/**
	 * Provides a hook called when selection is no more empty
	 * 
	 */
	protected void fireSelectionIsNoMoreEmpty() {
		// _clipboard.setCopyEnabled(true);
		// _clipboard.setCutEnabled(true);
	}

	/**
	 * Add supplied object to current selection
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	protected void internallyAddToSelected(FlexoObject object) {
		internallyAddToSelected(object, true);
	}

	/**
	 * Add supplied object to current selection
	 * 
	 * @param isNewFocusedObject
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	protected void internallyAddToSelected(FlexoObject object, boolean isNewFocusedObject) {
		if (_controller == null) {
			logger.warning("Null controller");
			return;
		}
		object = _controller.getRelevantObject(object);
		if (!isSelectable(object)) {
			return;
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("internallyAddToSelected with " + object);
		}
		boolean selectionWasEmpty = getSelectionSize() == 0;
		if (!selectionContains(object)) {
			_selection.add(object);
			for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
				SelectionListener sl = e.nextElement();
				sl.fireObjectSelected(object);
			}
			if (selectionWasEmpty && getSelectionSize() > 0) {
				fireSelectionIsNoMoreEmpty();
			}
		}
		if (isNewFocusedObject) {
			setLastSelectedObject(object);
			setFocusedObject(object);
		}
	}

	/**
	 * Remove supplied object from current selection
	 * 
	 * @param object
	 *            : the object to remove from selection
	 */
	protected void internallyRemoveFromSelected(FlexoObject object) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("internallyRemoveFromSelected with " + object);
		}
		if (_focusedObject == object) {
			_focusedObject = null;
		}
		if (_inspectedObject == object) {
			_inspectedObject = null;
		}
		if (lastSelectedObject == object) {
			lastSelectedObject = null;
		}
		if (selectionContains(object)) {
			_selection.remove(object);
			for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
				SelectionListener sl = e.nextElement();
				sl.fireObjectDeselected(object);
			}
			if (getSelectionSize() == 0) {
				fireSelectionBecomesEmpty();
			}
		}
		fireUpdateSelection();
	}

	/**
	 * Remove supplied objects from current selection
	 * 
	 * @param objects
	 *            : the objects to remove from selection
	 */
	protected void internallyRemoveFromSelected(Vector<? extends FlexoObject> objects) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("internallyRemoveFromSelected with " + objects);
		}
		for (FlexoObject object : objects) {
			if (_focusedObject == object) {
				_focusedObject = null;
			}
			if (_inspectedObject == object) {
				_inspectedObject = null;
			}
			if (lastSelectedObject == object) {
				lastSelectedObject = null;
			}
			if (selectionContains(object)) {
				_selection.remove(object);
				for (Enumeration<SelectionListener> e = _selectionListeners.elements(); e.hasMoreElements();) {
					SelectionListener sl = e.nextElement();
					sl.fireObjectDeselected(object);
				}
			}
		}
		if (getSelectionSize() == 0) {
			fireSelectionBecomesEmpty();
		}
		fireUpdateSelection();
	}

	@Override
	public String toString() {
		String returned = super.toString() + "\n";
		for (Enumeration<FlexoObject> en = _selection.elements(); en.hasMoreElements();) {
			FlexoObject o = en.nextElement();
			StringTokenizer st = new StringTokenizer(o.getClass().getName(), ".");
			String className = "";
			while (st.hasMoreTokens()) {
				className = st.nextToken();
			}
			returned += "> " + className + ":" + o + "\n";
		}
		return returned;
	}

	// ============================================================
	// ======================= Inspector ==========================
	// ============================================================

	@Deprecated
	private Hashtable<String, Object> getInspectionContext() {
		return inspectionContext;
	}

	private void setCurrentInspectedObject(FlexoObject inspectable) {
		if (!isInspectable(inspectable)) {
			return;
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("setCurrentInspectedObject: " + countObservers() + " observers");
		}
		if (_inspectedObject != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Inspected object was: " + _inspectedObject.getClass().getName());
			}
		}
		if (inspectable != null) {
			if (_inspectedObject == null || !_inspectedObject.equals(inspectable)) {
				_inspectedObject = inspectable;
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Inspected object is now: " + _inspectedObject.getClass().getName());
				}
				setChanged();
				// Component focusOwner =
				// KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				notifyObservers(new UniqueSelection(_inspectedObject, getInspectionContext()));
			}
		}
		if (inspectable instanceof FlexoObject) {
			setFocusedObject(inspectable);
		}
	}

	private void setCurrentInspectedObjectToNone() {
		_inspectedObject = null;
		setChanged();
		notifyObservers(new EmptySelection());
		// InspectorController._inspectorWindow.toFront();
	}

	private void setCurrentInspectedObjectToMultiple() {
		_inspectedObject = null;
		setChanged();
		notifyObservers(new MultipleSelection());
		// InspectorController._inspectorWindow.toFront();
	}

	// Note that the updateInspectorManagement method is quite expensive in CPU
	private void updateInspectorManagement() {
		if (getSelectionSize() == 0) {
			setCurrentInspectedObjectToNone();
		}
		else if (getSelectionSize() == 1) {
			FlexoObject selection = getSelection().firstElement();
			setCurrentInspectedObject(selection);
		}
		else if (getSelectionSize() > 1) {
			setCurrentInspectedObjectToMultiple();
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Current selection is now: " + toString());
		}
	}

	// ==========================================================================
	// ============================= Deletion
	// ===================================
	// ==========================================================================

	/**
	 * Returns the root object that can be currently edited
	 * 
	 * @return FlexoObject
	 */
	public abstract FlexoObject getRootFocusedObject();

	protected boolean isInspectable(FlexoObject object) {
		if (!(object instanceof FlexoObject)) {
			return true;
		}
		/*FlexoObject obj = object;
		if (obj.getContext() != null) {
			if (obj.getContext() instanceof PalettePanel) {
				return ((PalettePanel) obj.getContext()).isEdited();
			}
		}*/
		return true;
	}

	protected boolean isSelectable(FlexoObject object) {
		if (object == null) {
			return false;
		}
		if (object.isDeleted()) {
			return false;
		}
		/*if (object.getContext() != null) {
			if (object.getContext() instanceof PalettePanel) {
				return ((PalettePanel) object.getContext()).isEdited();
			}
		}*/
		return true;
	}

	public ContextualMenuManager getContextualMenuManager() {
		return _contextualMenuManager;
	}

}
