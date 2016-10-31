/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller.widget;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBProjectObjectSelector;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewType;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.ViewRepository;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a ViewPoint
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBViewSelector extends FIBProjectObjectSelector<View> {

	static final Logger logger = Logger.getLogger(FIBViewSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/ViewSelector.fib");

	private ViewLibrary viewLibrary;
	private Type expectedType;
	private ViewType defaultExpectedType;
	private ViewPoint viewPoint;

	public FIBViewSelector(View editedObject) {
		super(editedObject);
		defaultExpectedType = editedObject != null ? ViewType.getViewType(editedObject.getViewPoint()) : ViewType.UNDEFINED_VIEW_TYPE;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<View> getRepresentedType() {
		return View.class;
	}

	@Override
	public String renderedString(View editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public ViewLibrary getViewLibrary() {
		return viewLibrary;
	}

	@CustomComponentParameter(name = "viewLibrary", type = CustomComponentParameter.Type.OPTIONAL)
	public void setViewLibrary(ViewLibrary viewLibrary) {
		this.viewLibrary = viewLibrary;
	}

	/**
	 * Return virtual model which selected VirtualModelInstance should conform
	 * 
	 * @return
	 */
	public ViewPoint getViewPoint() {
		return viewPoint;
	}

	/**
	 * Sets virtual model which selected VirtualModelInstance should conform
	 * 
	 * @param virtualModel
	 */
	@CustomComponentParameter(name = "viewPoint", type = CustomComponentParameter.Type.OPTIONAL)
	public void setViewPoint(ViewPoint viewPoint) {
		this.viewPoint = viewPoint;
		defaultExpectedType = ViewType.getViewType(viewPoint);
	}

	public Type getExpectedType() {
		if (expectedType == null) {
			return defaultExpectedType;
		}
		return expectedType;
	}

	public void setExpectedType(Type expectedType) {

		if ((expectedType == null && this.expectedType != null) || (expectedType != null && !expectedType.equals(this.expectedType))) {
			Type oldValue = this.expectedType;
			this.expectedType = expectedType;
			getPropertyChangeSupport().firePropertyChange("expectedType", oldValue, expectedType);
		}
	}

	public Object getRootObject() {
		if (getViewLibrary() != null) {
			return getViewLibrary();
		}
		else if (getProject() != null) {
			return getProject();
		}
		else if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	public List<ViewResource> getViewResources(RepositoryFolder<?, ?> folder) {
		if (folder.getResourceRepository() instanceof ViewRepository) {
			return (List) folder.getResources();
		}
		return null;
	}

	@Override
	public boolean isAcceptableValue(Object o) {
		if (!super.isAcceptableValue(o)) {
			return false;
		}
		if (!(o instanceof View)) {
			return false;
		}
		if (!(getExpectedType() instanceof ViewType)) {
			return false;
		}
		View view = (View) o;
		ViewType viewType = (ViewType) getExpectedType();
		return (viewType.getViewPoint() == null) || (viewType.getViewPoint().isAssignableFrom(view.getVirtualModel()));

	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not insanciated in EDIT mode)
	/*public static void main(String[] args) {
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				FlexoEditor editor = ProjectDialogEDITOR.loadProject(new FileResource("Prj/TestVE.prj"));
				FlexoProject project = editor.getProject();
				FIBViewSelector selector = new FIBViewSelector(null);
				selector.setProject(project);
				return makeArray(selector);
			}
	
			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}
	
			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController<FIBViewSelector>(component);
			}
		};
		editor.launch();
	}*/

}
