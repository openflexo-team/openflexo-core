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

package org.openflexo.view;

import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.listener.FIBSelectionListener;
import org.openflexo.gina.view.FIBView;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.selection.SelectionManager;
import org.openflexo.selection.SelectionSynchronizedComponent;
import org.openflexo.view.controller.FlexoController;

/**
 * Default implementation for a FIBViewImpl which is synchronized with a {@link SelectionManager}
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class SelectionSynchronizedFIBView extends FlexoFIBView
		implements SelectionSynchronizedComponent, GraphicalFlexoObserver, FIBSelectionListener {
	static final Logger logger = Logger.getLogger(SelectionSynchronizedFIBView.class.getPackage().getName());

	public SelectionSynchronizedFIBView(Object representedObject, FlexoController controller, Resource fibResource,
			LocalizedDelegate locales) {
		this(representedObject, controller, fibResource, locales, false);
		if (controller != null) {
			controller.willLoad(fibResource);
		}
	}

	public SelectionSynchronizedFIBView(Object representedObject, FlexoController controller, Resource fibResource,
			LocalizedDelegate locales, boolean addScrollBar) {
		this(representedObject, controller,
				(controller != null ? controller.getApplicationFIBLibraryService().retrieveFIBComponent(fibResource)
						: ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(fibResource)),
				locales, addScrollBar);
	}

	protected SelectionSynchronizedFIBView(Object representedObject, FlexoController controller, FIBComponent fibComponent,
			LocalizedDelegate locales, boolean addScrollBar) {
		super(representedObject, controller, fibComponent, locales, addScrollBar);
		getFIBView().getController().addSelectionListener(this);
		if (controller != null && controller.getSelectionManager() != null) {
			logger.fine("Added selection manager for " + getClass().getSimpleName());
			controller.getSelectionManager().addToSelectionListeners(this);
		}
	}

	@Override
	public void deleteView() {
		FIBView<?, ?> aFibView = getFIBView();
		FIBController aController = null;
		if (aFibView != null) {
			aController = aFibView.getController();
			if (aController != null) {
				aController.removeSelectionListener(this);
			}
		}
		getFlexoController().getSelectionManager().removeFromSelectionListeners(this);
		super.deleteView();
	}

	/**
	 * Adds specified object to selection
	 * 
	 * @param object
	 */
	@Override
	public void fireObjectSelected(FlexoObject object) {
		if (ignoreFiredSelectionEvents) {
			return;
		}
		// logger.info("SELECTED: "+object);
		getFIBView().getController().objectAddedToSelection(getRelevantObject(object));
	}

	/**
	 * Removes specified object from selection
	 * 
	 * @param object
	 */
	@Override
	public void fireObjectDeselected(FlexoObject object) {
		if (ignoreFiredSelectionEvents) {
			return;
		}
		// logger.info("DESELECTED: "+object);
		getFIBView().getController().objectRemovedFromSelection(getRelevantObject(object));
	}

	/**
	 * Clear selection
	 */
	@Override
	public void fireResetSelection() {
		if (ignoreFiredSelectionEvents) {
			return;
		}
		// logger.info("RESET SELECTION");
		getFIBView().getController().selectionCleared();
	}

	/**
	 * Notify that the selection manager is performing a multiple selection
	 */
	@Override
	public void fireBeginMultipleSelection() {
		if (ignoreFiredSelectionEvents) {
			return;
		}
	}

	/**
	 * Notify that the selection manager has finished to perform a multiple selection
	 */
	@Override
	public void fireEndMultipleSelection() {
		if (ignoreFiredSelectionEvents) {
			return;
		}
	}

	@Override
	public SelectionManager getSelectionManager() {
		if (getFlexoController() != null) {
			return getFlexoController().getSelectionManager();
		}
		return null;
	}

	@Override
	public void selectionChanged(List<Object> selection) {
		if (selection == null) {
			return;
		}
		Vector<FlexoObject> newSelection = new Vector<>();
		for (Object o : selection) {
			if (o instanceof FlexoObject) {
				newSelection.add(getRelevantObject((FlexoObject) o));
			}
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("FlexoFIBView now impose new selection : " + newSelection);
		}
		if (getSelectionManager() != null) {
			ignoreFiredSelectionEvents = true;
			getSelectionManager().setSelectedObjects(newSelection);
			ignoreFiredSelectionEvents = false;
		}
	}

	private boolean ignoreFiredSelectionEvents = false;

	/**
	 * We manage here an indirection with resources: resource data is used instead of resource if resource is loaded
	 * 
	 * @param object
	 * @return
	 */
	private static FlexoObject getRelevantObject(FlexoObject object) {
		if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
			try {
				return (FlexoObject) ((FlexoResource<?>) object).getResourceData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	@Override
	public Vector<FlexoObject> getSelection() {
		if (getSelectionManager() != null) {
			return getSelectionManager().getSelection();
		}
		return null;
	}

	@Override
	public void resetSelection() {
		if (getSelectionManager() != null) {
			getSelectionManager().resetSelection();
		}
		else {
			fireResetSelection();
		}
	}

	@Override
	public void addToSelected(FlexoObject object) {
		if (mayRepresents(object)) {
			if (getSelectionManager() != null) {
				getSelectionManager().addToSelected(object);
			}
			else {
				fireObjectSelected(object);
			}
		}
	}

	@Override
	public void removeFromSelected(FlexoObject object) {
		if (mayRepresents(object)) {
			if (getSelectionManager() != null) {
				getSelectionManager().removeFromSelected(object);
			}
			else {
				fireObjectDeselected(object);
			}
		}
	}

	@Override
	public void addToSelected(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().addToSelected(objects);
		}
		else {
			fireBeginMultipleSelection();
			for (Enumeration<?> en = objects.elements(); en.hasMoreElements();) {
				FlexoObject next = (FlexoObject) en.nextElement();
				fireObjectSelected(next);
			}
			fireEndMultipleSelection();
		}
	}

	@Override
	public void removeFromSelected(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().removeFromSelected(objects);
		}
		else {
			fireBeginMultipleSelection();
			for (Enumeration<?> en = objects.elements(); en.hasMoreElements();) {
				FlexoObject next = (FlexoObject) en.nextElement();
				fireObjectDeselected(next);
			}
			fireEndMultipleSelection();
		}
	}

	@Override
	public void setSelectedObjects(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().setSelectedObjects(objects);
		}
		else {
			resetSelection();
			addToSelected(objects);
		}
	}

	@Override
	public FlexoObject getFocusedObject() {
		if (getSelectionManager() != null) {
			return getSelectionManager().getFocusedObject();
		}
		return null;
	}

	@Override
	public boolean mayRepresents(FlexoObject anObject) {
		return true;
	}
}
