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

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBProjectObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;

/**
 * Widget allowing to select a VirtualModelInstance
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBVirtualModelInstanceSelector extends FIBProjectObjectSelector<VirtualModelInstanceResource> {

	static final Logger logger = Logger.getLogger(FIBVirtualModelInstanceSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/VirtualModelInstanceSelector.fib");

	private ViewLibrary viewLibrary;
	private View view;
	private VirtualModel virtualModel;

	public FIBVirtualModelInstanceSelector(VirtualModelInstanceResource editedObject) {
		super(editedObject);
	}

	@Override
	public void delete() {
		super.delete();
		viewLibrary = null;
		view = null;
		virtualModel = null;
	}


	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}
	
	@Override
	public Class<VirtualModelInstanceResource> getRepresentedType() {
		return VirtualModelInstanceResource.class;
	}

	@Override
	public String renderedString(VirtualModelInstanceResource editedObject) {
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

	public View getView() {
		return view;
	}

	@CustomComponentParameter(name = "view", type = CustomComponentParameter.Type.OPTIONAL)
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Return virtual model which selected VirtualModelInstance should conform
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	/**
	 * Sets virtual model which selected VirtualModelInstance should conform
	 * 
	 * @param virtualModel
	 */
	@CustomComponentParameter(name = "virtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModel(VirtualModel virtualModel) {
		this.virtualModel = virtualModel;
	}

	public FlexoObject getRootObject() {
		if (getView() != null) {
			return getView().getResource();
		} else if (getViewLibrary() != null) {
			return getViewLibrary();
		} else {
			return getProject();
		}
	}

	@Override
	protected boolean isAcceptableValue(Object o) {
		if (o instanceof VirtualModelInstanceResource) {
			VirtualModelInstance vmi = ((VirtualModelInstanceResource) o).getVirtualModelInstance();
			if (getVirtualModel() != null) {
				return vmi.getVirtualModel() == getVirtualModel();
			}
			return true;
		}
		return super.isAcceptableValue(o);
	}

	public boolean isConformedToVirtualModel(VirtualModelInstanceResource vmiResource){
		if (((VirtualModelInstanceResource) vmiResource).getVirtualModelResource() != null && getVirtualModel()!=null) {
			return ((VirtualModelInstanceResource) vmiResource).getVirtualModelResource().getVirtualModel() == getVirtualModel();
		}
		return false;
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
				FIBVirtualModelSelector selector = new FIBVirtualModelSelector(null);
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
