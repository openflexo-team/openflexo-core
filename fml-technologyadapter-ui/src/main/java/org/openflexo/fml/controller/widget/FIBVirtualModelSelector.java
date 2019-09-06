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

import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a VirtualModel
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBVirtualModelSelector extends FIBFlexoObjectSelector<CompilationUnitResource> {

	static final Logger logger = Logger.getLogger(FIBVirtualModelSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/VirtualModelSelector.fib");

	public FIBVirtualModelSelector(CompilationUnitResource editedObject) {
		super(editedObject);
	}

	@Override
	public void delete() {
		super.delete();
		virtualModelLibrary = null;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<CompilationUnitResource> getRepresentedType() {
		return CompilationUnitResource.class;
	}

	@Override
	public String renderedString(CompilationUnitResource editedObject) {
		if (editedObject != null) {
			return editedObject.getDisplayName();
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

	private CompilationUnitResource containedVirtualModel;

	public CompilationUnitResource getContainerVirtualModel() {
		return containedVirtualModel;
	}

	@CustomComponentParameter(name = "containerVirtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setContainerVirtualModel(CompilationUnitResource viewPoint) {
		if (this.containedVirtualModel != viewPoint) {
			FlexoObject oldRoot = getRootObject();
			this.containedVirtualModel = viewPoint;
			getPropertyChangeSupport().firePropertyChange("rootObject", oldRoot, getRootObject());
		}
	}

	public FlexoObject getRootObject() {
		if (getContainerVirtualModel() != null) {
			return getContainerVirtualModel();
		}
		else {
			return getVirtualModelLibrary();
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
