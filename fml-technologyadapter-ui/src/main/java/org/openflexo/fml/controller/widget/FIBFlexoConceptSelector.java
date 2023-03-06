/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.widget;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLUtils;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.ta.FlexoConceptType;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select an FlexoConcept
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoConceptSelector extends FIBFlexoObjectSelector<FlexoConcept> {

	static final Logger logger = Logger.getLogger(FIBFlexoConceptSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/FlexoConceptSelector.fib");

	public FIBFlexoConceptSelector(FlexoConcept editedObject) {
		super(editedObject);
		// defaultExpectedType = editedObject != null ? FMLType.retrieveFlexoConceptType(editedObject.getFlexoConcept())
		// : FMLType.UNDEFINED_FLEXO_CONCEPT_TYPE;
		defaultExpectedType = editedObject != null ? editedObject.getConceptType() : FlexoConceptType.UNDEFINED_FLEXO_CONCEPT_TYPE;
	}

	@Override
	public void delete() {
		super.delete();
		virtualModelLibrary = null;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@Override
	public Class<FlexoConcept> getRepresentedType() {
		return FlexoConcept.class;
	}

	@Override
	public String renderedString(FlexoConcept editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	private VirtualModelLibrary virtualModelLibrary;

	public VirtualModelLibrary getVirtualModelLibrary() {
		return virtualModelLibrary;
	}

	@CustomComponentParameter(name = "virtualModelLibrary", type = CustomComponentParameter.Type.MANDATORY)
	public void setVirtualModelLibrary(VirtualModelLibrary virtualModelLibrary) {
		if ((virtualModelLibrary == null && this.virtualModelLibrary != null)
				|| (virtualModelLibrary != null && !virtualModelLibrary.equals(this.virtualModelLibrary))) {
			VirtualModelLibrary oldValue = this.virtualModelLibrary;
			this.virtualModelLibrary = virtualModelLibrary;
			getPropertyChangeSupport().firePropertyChange("virtualModelLibrary", oldValue, virtualModelLibrary);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	private VirtualModel virtualModel;

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	@CustomComponentParameter(name = "virtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModel(VirtualModel virtualModel) {

		if (this.virtualModel != virtualModel) {
			FlexoObject oldRoot = getRootObject();
			this.virtualModel = virtualModel;
			getPropertyChangeSupport().firePropertyChange("rootObject", oldRoot, getRootObject());
		}
	}

	private VirtualModel inheritingContext = null;
	private boolean restrictToContext = false;

	/**
	 * When true, indicates that we want to select a {@link FlexoConcept} instantiable in supplied {@link #getInheritingContext()}
	 * 
	 * @return
	 */
	public boolean isRestrictToContext() {
		return restrictToContext;
	}

	/**
	 * When set to true, indicates that we want to select a {@link FlexoConcept} instantiable in supplied {@link #getInheritingContext()}
	 * 
	 * 
	 * @param restrictToContext
	 */
	@CustomComponentParameter(name = "restrictToContext", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRestrictToContext(boolean restrictToContext) {
		if (restrictToContext != this.restrictToContext) {
			this.restrictToContext = restrictToContext;
			getPropertyChangeSupport().firePropertyChange("restrictToContext", !restrictToContext, restrictToContext);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	/**
	 * Return inheriting context for an acceptable value of this selector<br>
	 * Default value is null
	 * 
	 * @return
	 */
	public VirtualModel getInheritingContext() {
		return inheritingContext;
	}

	/**
	 * Sets inheriting context for an acceptable value of this selector.
	 * 
	 * This means that an instance of selected {@link FlexoConcept} might be instanciated in an instance of supplied {@link VirtualModel}
	 * 
	 * If supplied inheritingContext is null (but {@link #isRestrictToContext()} flag to true), this means that selectable concept must be a
	 * {@link VirtualModel} at top-level
	 * 
	 * @param inheritingContext
	 */
	@CustomComponentParameter(name = "inheritingContext", type = CustomComponentParameter.Type.OPTIONAL)
	public void setInheritingContext(VirtualModel inheritingContext) {
		if (this.inheritingContext != inheritingContext) {
			VirtualModel oldValue = this.inheritingContext;
			this.inheritingContext = inheritingContext;
			getPropertyChangeSupport().firePropertyChange("inheritingContext", oldValue, inheritingContext);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	@Override
	public boolean isAcceptableValue(Object o) {
		boolean returned = super.isAcceptableValue(o);
		if (returned && isRestrictToContext()) {
			if (getInheritingContext() == null) {
				return true;
				// return o instanceof VirtualModel && ((VirtualModel) o).getContainerVirtualModel() == null;
			}
			else {
				if (o instanceof VirtualModel) {
					if (((VirtualModel) o).getContainerVirtualModel() == null) {
						return false;
					}
					return ((VirtualModel) o).getContainerVirtualModel().isAssignableFrom(getInheritingContext());
				}
				else {
					return ((FlexoConcept) o).getOwningVirtualModel() != null
							&& ((FlexoConcept) o).getOwningVirtualModel().isAssignableFrom(getInheritingContext());
				}
			}
		}
		return returned;
	}

	public FlexoObject getRootObject() {
		if (getExpectedFlexoConceptType() != null) {
			return getExpectedFlexoConceptType();
		}
		if (getExpectedType() instanceof FlexoConceptType) {
			Type t = ((FlexoConceptType) getExpectedType()).getType();
			if (t instanceof FlexoConceptInstanceType) {
				return ((FlexoConceptInstanceType) t).getFlexoConcept();
			}
			if (t instanceof WildcardType && ((WildcardType) t).getUpperBounds().length == 1) {
				Type t2 = ((WildcardType) t).getUpperBounds()[0];
				if (t2 instanceof FlexoConceptInstanceType) {
					return ((FlexoConceptInstanceType) t2).getFlexoConcept();
				}
			}
		}
		if (getInheritingContext() != null) {
			return getInheritingContextRoot();
		}
		if (getVirtualModel() != null) {
			return getVirtualModel();
		}
		else {
			return getVirtualModelLibrary();
		}
	}

	private FlexoObject getInheritingContextRoot() {
		List<VirtualModel> vmList = new ArrayList<>();
		appendInheritingVirtualModels(getInheritingContext(), vmList);
		FlexoObject returned = getInheritingContext();
		if (vmList.size() >= 1) {
			// returned = vmList.get(0);
			for (VirtualModel vm : vmList) {
				if (returned instanceof VirtualModel) {
					returned = FMLUtils.getMostSpecializedContainer((VirtualModel) returned, vm);
				}
				else {
					// TODO: check that vm is in returned
					logger.warning("TODO: check that " + vm + " is in " + returned);
				}
			}
		}
		if (returned == null) {
			return getInheritingContext().getVirtualModelLibrary();
		}
		else {
			return returned;
		}
	}

	private void appendInheritingVirtualModels(VirtualModel vm, List<VirtualModel> vmList) {
		if (vm != null) {
			for (FlexoConcept parent : vm.getParentFlexoConcepts()) {
				if (parent instanceof VirtualModel) {
					vmList.add((VirtualModel) parent);
					appendInheritingVirtualModels((VirtualModel) parent, vmList);
				}
			}
		}
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

	private Type expectedType;
	private FlexoConceptType defaultExpectedType;
	private FlexoConcept expectedFlexoConceptType = null;

	public Type getDefaultExpectedType() {
		return defaultExpectedType;
	}

	public Type getExpectedType() {
		if (expectedFlexoConceptType != null) {
			return expectedFlexoConceptType.getConceptType();
		}
		if (expectedType == null) {
			return getDefaultExpectedType();
		}
		return expectedType;
	}

	@CustomComponentParameter(name = "expectedType", type = CustomComponentParameter.Type.OPTIONAL)
	public void setExpectedType(Type expectedType) {
		if ((expectedType == null && this.expectedType != null) || (expectedType != null && !expectedType.equals(this.expectedType))) {
			Type oldValue = this.expectedType;
			this.expectedType = expectedType;
			getPropertyChangeSupport().firePropertyChange("expectedType", oldValue, expectedType);
		}
	}

	public FlexoConcept getExpectedFlexoConceptType() {
		return expectedFlexoConceptType;
	}

	@CustomComponentParameter(name = "expectedFlexoConceptType", type = CustomComponentParameter.Type.OPTIONAL)
	public void setExpectedFlexoConceptType(FlexoConcept expectedFlexoConceptType) {

		if ((expectedFlexoConceptType == null && this.expectedFlexoConceptType != null)
				|| (expectedFlexoConceptType != null && !expectedFlexoConceptType.equals(this.expectedFlexoConceptType))) {
			FlexoConcept oldValue = this.expectedFlexoConceptType;
			this.expectedFlexoConceptType = expectedFlexoConceptType;
			getPropertyChangeSupport().firePropertyChange("expectedFlexoConceptType", oldValue, expectedFlexoConceptType);
			getPropertyChangeSupport().firePropertyChange("expectedType", null, getExpectedType());
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	public String getExpectedFlexoConceptTypeURI() {
		if (expectedFlexoConceptType != null) {
			return expectedFlexoConceptType.getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "expectedFlexoConceptTypeURI", type = CustomComponentParameter.Type.OPTIONAL)
	public void setExpectedFlexoConceptTypeURI(String expectedFlexoConceptTypeURI) {

		if (getServiceManager() != null) {
			expectedFlexoConceptType = getServiceManager().getVirtualModelLibrary().getFlexoConcept(expectedFlexoConceptTypeURI);
			System.out.println("sets concept to : " + expectedFlexoConceptType);
			getPropertyChangeSupport().firePropertyChange("expectedFlexoConceptType", null, expectedFlexoConceptType);
			getPropertyChangeSupport().firePropertyChange("expectedType", null, getExpectedType());
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

}
