/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Gina-swing, a component of the software infrastructure 
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
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.listener.FIBSelectionListener;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.selection.SelectionListener;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Implementation of a FIBJPanel synchronized with the selection
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of object beeing represented as 'data' in FIB
 */
@SuppressWarnings("serial")
public abstract class SelectionSynchronizedFIBJPanel<T> extends FIBJPanel<T> implements SelectionListener, FIBSelectionListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SelectionSynchronizedFIBJPanel.class.getPackage().getName());

	public SelectionSynchronizedFIBJPanel(FIBComponent component, T editedObject, LocalizedDelegate parentLocalizer) {
		super(component, editedObject, parentLocalizer);

		getController().addSelectionListener(this);
		if (getController() != null && getController().getSelectionManager() != null) {
			logger.info("Added selection manager for " + getClass().getSimpleName());
			getController().getSelectionManager().addToSelectionListeners(this);
		}

	}

	public SelectionSynchronizedFIBJPanel(Resource fibFileName, T editedObject, FIBLibrary fibLibrary, LocalizedDelegate parentLocalizer) {
		this(fibLibrary.retrieveFIBComponent(fibFileName, true), editedObject, parentLocalizer);
	}

	@Override
	public FlexoFIBController getController() {
		return (FlexoFIBController) super.getController();
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
		getController().objectAddedToSelection(getRelevantObject(object));
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
		getController().objectRemovedFromSelection(getRelevantObject(object));
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
		getController().selectionCleared();
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

	public SelectionManager getSelectionManager() {
		if (getController() != null) {
			return getController().getSelectionManager();
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

}
