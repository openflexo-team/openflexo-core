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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLUtils;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
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
		this.virtualModelLibrary = virtualModelLibrary;
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

	private VirtualModel inheritingContext;

	public VirtualModel getInheritingContext() {
		return inheritingContext;
	}

	public void setInheritingContext(VirtualModel inheritingContext) {
		if (this.inheritingContext != inheritingContext) {
			VirtualModel oldValue = this.inheritingContext;
			this.inheritingContext = inheritingContext;
			getPropertyChangeSupport().firePropertyChange("inheritingContext", oldValue, inheritingContext);
		}
	}

	@Override
	protected boolean isAcceptableValue(Object o) {
		return super.isAcceptableValue(o);
	}

	public FlexoObject getRootObject() {
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
		VirtualModel returned = getInheritingContext();
		for (VirtualModel vm : vmList) {
			returned = (VirtualModel) FMLUtils.getMostSpecializedAncestor(returned, vm);
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
			vmList.add(vm);
			for (FlexoConcept parent : vm.getParentFlexoConcepts()) {
				if (parent instanceof VirtualModel) {
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

}
