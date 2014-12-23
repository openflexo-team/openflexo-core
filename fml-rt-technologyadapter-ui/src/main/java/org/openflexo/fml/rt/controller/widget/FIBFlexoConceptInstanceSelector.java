/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.fml.rt.controller.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBProjectObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;

/**
 * Widget allowing to select an FlexoConceptInstance.<br>
 * 
 * The scope of searched EPI is either:
 * <ul>
 * <li>the whole project, if {@link FlexoProject} has been set</li>
 * <li>a view, if {@link View} has been set</li>
 * <li>a virtual model instance, if {@link VirtualModelInstance} has been set</li>
 * </ul>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoConceptInstanceSelector extends FIBProjectObjectSelector<FlexoConceptInstance> {

	static final Logger logger = Logger.getLogger(FIBFlexoConceptInstanceSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/FlexoConceptInstanceSelector.fib");

	private ViewPointLibrary viewPointLibrary;
	private ViewPoint viewPoint;
	private VirtualModel virtualModel;
	private FlexoConcept flexoConcept;
	private View view;
	private VirtualModelInstance virtualModelInstance;

	public FIBFlexoConceptInstanceSelector(FlexoConceptInstance editedObject) {
		super(editedObject);
	}

	@Override
	public void delete() {
		super.delete();
		viewPointLibrary = null;
		viewPoint = null;
		virtualModel = null;
		flexoConcept = null;
		view = null;
		virtualModelInstance = null;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<FlexoConceptInstance> getRepresentedType() {
		return FlexoConceptInstance.class;
	}

	@Override
	public String renderedString(FlexoConceptInstance editedObject) {
		if (editedObject != null) {
			return editedObject.getStringRepresentation();
		}
		return "";
	}

	public ViewPointLibrary getViewPointLibrary() {
		return viewPointLibrary;
	}

	@CustomComponentParameter(name = "viewPointLibrary", type = CustomComponentParameter.Type.MANDATORY)
	public void setViewPointLibrary(ViewPointLibrary viewPointLibrary) {
		this.viewPointLibrary = viewPointLibrary;
	}

	public ViewPoint getViewPoint() {
		return viewPoint;
	}

	@CustomComponentParameter(name = "viewPoint", type = CustomComponentParameter.Type.OPTIONAL)
	public void setViewPoint(ViewPoint viewPoint) {
		this.viewPoint = viewPoint;
	}

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	@CustomComponentParameter(name = "virtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModel(VirtualModel virtualModel) {
		this.virtualModel = virtualModel;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@CustomComponentParameter(name = "flexoConcept", type = CustomComponentParameter.Type.OPTIONAL)
	public void setFlexoConcept(FlexoConcept flexoConcept) {
		System.out.println(">>>>>>>>> Sets FlexoConcept with " + flexoConcept);
		this.flexoConcept = flexoConcept;
	}

	public FlexoObject getRootObject() {
		if (getFlexoConcept() != null) {
			return getFlexoConcept();
		} else if (getVirtualModel() != null) {
			return getVirtualModel();
		} else if (getViewPoint() != null) {
			return getViewPoint();
		} else {
			return getViewPointLibrary();
		}
	}

	public List<FlexoConceptInstance> getEPInstances(FlexoConcept ep) {
		if (getVirtualModelInstance() != null) {
			return getVirtualModelInstance().getFlexoConceptInstances(ep);
		} else if (getView() != null) {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			for (VirtualModelInstance vmi : getView().getVirtualModelInstances()) {
				returned.addAll(vmi.getFlexoConceptInstances(ep));
			}
			return returned;
		} else if (getProject() != null) {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			for (ViewResource vr : getProject().getViewLibrary().getAllResources()) {
				for (VirtualModelInstance vmi : vr.getView().getVirtualModelInstances()) {
					returned.addAll(vmi.getFlexoConceptInstances(ep));
				}
			}
			return returned;
		}
		return null;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
		System.out.println(">>>>>>>>> Sets view with " + view);
	}

	public VirtualModelInstance getVirtualModelInstance() {
		return virtualModelInstance;
	}

	public void setVirtualModelInstance(VirtualModelInstance virtualModelInstance) {
		this.virtualModelInstance = virtualModelInstance;
		System.out.println(">>>>>>>>> Sets VirtualModelInstance with " + virtualModelInstance);
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not insanciated in EDIT mode)
	/*public static void main(String[] args) {
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				TestApplicationContext testApplicationContext = new TestApplicationContext(new FileResource(
						"src/test/resources/TestResourceCenter"));
				FIBFlexoConceptSelector selector = new FIBFlexoConceptSelector(null);
				selector.setViewPointLibrary(testApplicationContext.getViewPointLibrary());
				return makeArray(selector);
			}

			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}

			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController(component);
			}
		};
		editor.launch();
	}*/

}