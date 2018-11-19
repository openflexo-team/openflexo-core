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

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select an FlexoConceptInstance.<br>
 * 
 * The scope of searched EPI is either:
 * <ul>
 * <li>the whole project, if {@link FlexoProject} has been set</li>
 * <li>a view, if {@link VirtualModelInstance} has been set</li>
 * <li>a virtual model instance, if {@link FMLRTVirtualModelInstance} has been set</li>
 * </ul>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoConceptInstanceSelector extends FIBAbstractFMLRTObjectSelector<FlexoConceptInstance> {

	static final Logger logger = Logger.getLogger(FIBFlexoConceptInstanceSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/FlexoConceptInstanceSelector.fib");

	/*private VirtualModelLibrary viewPointLibrary;
	private ViewPoint viewPoint;
	private VirtualModel virtualModel;
	private FlexoConcept flexoConcept;
	private View view;
	private FMLRTVirtualModelInstance virtualModelInstance;
	private Type expectedType;
	private FlexoConceptInstanceType defaultExpectedType;*/

	public FIBFlexoConceptInstanceSelector(FlexoConceptInstance editedObject) {
		super(editedObject);
		// defaultExpectedType = editedObject != null ? FlexoConceptInstanceType.getFlexoConceptInstanceType(editedObject.getFlexoConcept())
		// : FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
	}

	@Override
	public void delete() {
		super.delete();
		/*viewPointLibrary = null;
		viewPoint = null;
		virtualModel = null;
		flexoConcept = null;
		view = null;
		virtualModelInstance = null;*/
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<FlexoConceptInstance> getRepresentedType() {
		return FlexoConceptInstance.class;
	}

	/*@Override
	public String renderedString(FlexoConceptInstance editedObject) {
		if (editedObject != null) {
			return editedObject.getStringRepresentation();
		}
		return "";
	}*/

	/*public VirtualModelLibrary getViewPointLibrary() {
		return viewPointLibrary;
	}
	
	@CustomComponentParameter(name = "viewPointLibrary", type = CustomComponentParameter.Type.MANDATORY)
	public void setViewPointLibrary(VirtualModelLibrary viewPointLibrary) {
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
		// System.out.println(">>>>>>>>> Sets FlexoConcept with " + flexoConcept);
		this.flexoConcept = flexoConcept;
		defaultExpectedType = FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept);
	}
	
	public FlexoObject getRootObject() {
		if (getFlexoConcept() != null) {
			return getFlexoConcept();
		}
		else if (getVirtualModel() != null) {
			return getVirtualModel();
		}
		else if (getViewPoint() != null) {
			return getViewPoint();
		}
		else if (getView() != null) {
			return getView().getViewPoint();
		}
		else if (getVirtualModelInstance() != null) {
			return getVirtualModelInstance().getVirtualModel();
		}
		else if (getProject() != null) {
			return getProject().getViewLibrary();
		}
		else {
			return getViewPointLibrary();
		}
	}
	
	public List<FlexoConceptInstance> getEPInstances(FlexoConcept ep) {
	
		if (getVirtualModelInstance() != null) {
			if (getVirtualModelInstance().getVirtualModel() == ep) {
				return Collections.singletonList((FlexoConceptInstance) getVirtualModelInstance());
			}
			return getVirtualModelInstance().getFlexoConceptInstances(ep);
		}
		else if (getView() != null) {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			for (VirtualModelInstance<?, ?> vmi : getView().getVirtualModelInstances()) {
				returned.addAll(vmi.getFlexoConceptInstances(ep));
			}
			return returned;
		}
		else if (getProject() != null) {
			List<FlexoConceptInstance> returned = new ArrayList<FlexoConceptInstance>();
			for (ViewResource vr : getProject().getViewLibrary().getAllResources()) {
				for (VirtualModelInstance<?, ?> vmi : vr.getView().getVirtualModelInstances()) {
					returned.addAll(vmi.getFlexoConceptInstances(ep));
				}
			}
			return returned;
		}
		return null;
	}
	
	@Override
	public boolean isAcceptableValue(Object o) {
		if (!super.isAcceptableValue(o)) {
			return false;
		}
		if (!(o instanceof FlexoConceptInstance)) {
			return false;
		}
		if (!(getExpectedType() instanceof FlexoConceptInstanceType)) {
			return false;
		}
		FlexoConceptInstance fci = (FlexoConceptInstance) o;
		FlexoConceptInstanceType fciType = (FlexoConceptInstanceType) getExpectedType();
		return (fciType.getFlexoConcept() == null) || (fciType.getFlexoConcept().isAssignableFrom(fci.getFlexoConcept()));
	
	}
	
	public View getView() {
		return view;
	}
	
	public void setView(View view) {
		this.view = view;
		// System.out.println(">>>>>>>>> Sets view with " + view);
	}
	
	public FMLRTVirtualModelInstance getVirtualModelInstance() {
		return virtualModelInstance;
	}
	
	public void setVirtualModelInstance(FMLRTVirtualModelInstance virtualModelInstance) {
		this.virtualModelInstance = virtualModelInstance;
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
	}*/

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
