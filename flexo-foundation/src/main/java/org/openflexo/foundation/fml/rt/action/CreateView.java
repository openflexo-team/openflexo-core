/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;

/**
 * Abstract base implementation for an action which aims at creating a new {@link View}
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of container of View
 */
public abstract class CreateView<A extends CreateView<A, T>, T extends FlexoObject>
		extends AbstractCreateVirtualModelInstance<A, T, View, ViewPoint> {

	private static final Logger logger = Logger.getLogger(CreateView.class.getPackage().getName());

	// private View newView;

	// private boolean useViewPoint = true;
	// private String newViewName;
	// private String newViewTitle;
	// private ViewPointResource viewpointResource;
	// public boolean createVirtualModel = false;
	// public boolean createDiagram = false;

	// public boolean skipChoosePopup = false;

	protected CreateView(FlexoActionType<A, T, FlexoObject> actionType, T focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/*@Override
	protected void doAction(Object context) throws SaveResourceException {
		logger.info("Add view in folder " + getFolder());
	
		if (StringUtils.isNotEmpty(newViewTitle) && StringUtils.isEmpty(newViewName)) {
			newViewName = JavaUtils.getClassName(newViewTitle);
		}
	
		if (StringUtils.isNotEmpty(newViewName) && StringUtils.isEmpty(newViewTitle)) {
			newViewTitle = newViewName;
		}
	
		if (getFolder() == null) {
			throw new InvalidParameterException("folder is undefined");
		}
		if (StringUtils.isEmpty(newViewName)) {
			throw new InvalidParameterException("view name is undefined");
		}
	
		int index = 1;
		String baseName = newViewName;
		while (!getFolder().isValidResourceName(newViewName)) {
			newViewName = baseName + index;
			index++;
		}
	
		newView = ViewImpl.newView(newViewName, newViewTitle, viewpointResource.getViewPoint(), getFolder(), getProject());
	
		logger.info("Added view " + newView + " in folder " + getFolder() + " for project " + getProject());
	
		getViewLibrary().registerResource((ViewResource) getNewView().getResource(), getFocusedObject());
	
	}*/

	public abstract ViewLibrary getViewLibrary();

	@Override
	public FlexoProject getProject() {
		if (getViewLibrary() != null) {
			return getViewLibrary().getProject();
		}
		return null;
	}

	/*@Override
	public boolean isValid() {
	
		// System.out.println("viewpointResource=" + viewpointResource);
	
		if (getFolder() == null) {
			return false;
		}
		else if (viewpointResource == null) {
			return false;
		}
		if (StringUtils.isEmpty(newViewTitle)) {
			return false;
		}
	
		String viewName = newViewName;
		if (StringUtils.isNotEmpty(newViewTitle) && StringUtils.isEmpty(newViewName)) {
			viewName = JavaUtils.getClassName(newViewTitle);
		}
	
		if (getFocusedObject().getResourceWithName(viewName) != null) {
			return false;
		}
		return true;
	}*/

	public View getNewView() {
		return getNewVirtualModelInstance();
	}

	public String getNewViewName() {
		return getNewVirtualModelInstanceName();
	}

	public void setNewViewName(String newViewName) {
		setNewVirtualModelInstanceName(newViewName);
		getPropertyChangeSupport().firePropertyChange("newViewName", null, newViewName);
	}

	public String getNewViewTitle() {
		return getNewVirtualModelInstanceTitle();
	}

	public void setNewViewTitle(String newViewTitle) {
		setNewVirtualModelInstanceTitle(newViewTitle);
		getPropertyChangeSupport().firePropertyChange("newViewTitle", null, newViewTitle);
	}

	public ViewPoint getViewPoint() {
		return getVirtualModel();
	}

	public ViewPointResource getViewpointResource() {
		if (getVirtualModel() != null) {
			return (ViewPointResource) getVirtualModel().getResource();
		}
		return null;
	}

	public void setViewpointResource(ViewPointResource viewpointResource) {
		setVirtualModel(viewpointResource.getViewPoint());
		getPropertyChangeSupport().firePropertyChange("viewpointResource", null, viewpointResource);
	}
}
