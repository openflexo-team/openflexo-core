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
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBProjectObjectSelector;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a FMLRTVirtualModelInstance
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBVirtualModelInstanceResourceSelector extends FIBProjectObjectSelector<FMLRTVirtualModelInstanceResource> {

	static final Logger logger = Logger.getLogger(FIBVirtualModelInstanceResourceSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/VirtualModelInstanceResourceSelector.fib");

	private FMLRTVirtualModelInstanceRepository<?> vmiRepository;
	private FMLRTVirtualModelInstance containerVirtualModelInstance;
	private VirtualModel virtualModel;
	private Type expectedType;
	private VirtualModelInstanceType defaultExpectedType;

	public FIBVirtualModelInstanceResourceSelector(FMLRTVirtualModelInstanceResource editedObject) {
		super(editedObject);
		defaultExpectedType = editedObject != null
				? VirtualModelInstanceType.getVirtualModelInstanceType(editedObject.getVirtualModelResource().getCompilationUnit().getVirtualModel())
				: VirtualModelInstanceType.UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;
	}

	@Override
	public void delete() {
		super.delete();
		vmiRepository = null;
		containerVirtualModelInstance = null;
		virtualModel = null;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<FMLRTVirtualModelInstanceResource> getRepresentedType() {
		return FMLRTVirtualModelInstanceResource.class;
	}

	@Override
	public String renderedString(FMLRTVirtualModelInstanceResource editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public FMLRTVirtualModelInstanceRepository<?> getVirtualModelInstanceRepository() {
		return vmiRepository;
	}

	@CustomComponentParameter(name = "virtualModelInstanceRepository", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModelInstanceRepository(FMLRTVirtualModelInstanceRepository<?> vmiRepository) {
		this.vmiRepository = vmiRepository;
	}

	public FMLRTVirtualModelInstance getContainerVirtualModelInstance() {
		return containerVirtualModelInstance;
	}

	@CustomComponentParameter(name = "containerVirtualModelInstance", type = CustomComponentParameter.Type.OPTIONAL)
	public void setContainerVirtualModelInstance(FMLRTVirtualModelInstance containerVirtualModelInstance) {
		this.containerVirtualModelInstance = containerVirtualModelInstance;
	}

	/**
	 * Return virtual model which selected FMLRTVirtualModelInstance should conform
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	/**
	 * Sets virtual model which selected FMLRTVirtualModelInstance should conform
	 * 
	 * @param virtualModel
	 */
	@CustomComponentParameter(name = "virtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModel(VirtualModel virtualModel) {
		this.virtualModel = virtualModel;
		defaultExpectedType = VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel);
	}

	public Object getRootObject() {
		if (getContainerVirtualModelInstance() != null) {
			return getContainerVirtualModelInstance().getResource();
		}
		else if (getVirtualModelInstanceRepository() != null) {
			return getVirtualModelInstanceRepository();
		}
		else if (getProject() != null) {
			FlexoServiceManager sm = getProject().getServiceManager();
			FMLRTTechnologyAdapter fmlRTTA = sm.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			return fmlRTTA.getVirtualModelInstanceRepository(getProject()).getRootFolder();
		}
		else if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	/*@Override
	protected boolean isAcceptableValue(Object o) {
		if (o instanceof VirtualModelInstanceResource) {
			FMLRTVirtualModelInstance vmi = ((VirtualModelInstanceResource) o).getVirtualModelInstance();
			if (getVirtualModel() != null) {
				return vmi.getVirtualModel() == getVirtualModel();
			}
			return true;
		}
		return super.isAcceptableValue(o);
	}*/

	@Override
	public boolean isAcceptableValue(Object o) {
		if (!super.isAcceptableValue(o)) {
			return false;
		}
		if (!(o instanceof FMLRTVirtualModelInstanceResource)) {
			return false;
		}
		if (!(getExpectedType() instanceof VirtualModelInstanceType)) {
			return false;
		}
		FMLRTVirtualModelInstance vmi = ((FMLRTVirtualModelInstanceResource) o).getVirtualModelInstance();
		VirtualModelInstanceType vmiType = (VirtualModelInstanceType) getExpectedType();
		return (vmiType.getVirtualModel() == null) || (vmiType.getVirtualModel().isAssignableFrom(vmi.getVirtualModel()));

	}

	/*public boolean isConformedToVirtualModel(VirtualModelInstanceResource vmiResource) {
		if (vmiResource.getVirtualModelResource() != null && getVirtualModel() != null) {
			return vmiResource.getVirtualModelResource().getVirtualModel() == getVirtualModel();
		}
		return false;
	}*/

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
