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
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;

/**
 * Widget allowing to select an object managed by {@link FMLRTTechnologyAdapter}
 * 
 * The scope of searched object is either:
 * <ul>
 * <li>the whole environment (all is foundable in all resource centers), if {@link FlexoServiceManager} has been set</li>
 * <li>a resource center, if {@link FlexoResourceCenter} has been set</li>
 * <li>a view, if {@link VirtualModelInstance} has been set</li>
 * <li>a virtual model instance, if {@link FMLRTVirtualModelInstance} has been set</li>
 * </ul>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBAbstractFMLRTObjectSelector<T extends FlexoConceptInstance> extends FIBProjectObjectSelector<T> {

	static final Logger logger = Logger.getLogger(FIBAbstractFMLRTObjectSelector.class.getPackage().getName());

	private FlexoServiceManager serviceManager;
	private FlexoResourceCenter<?> resourceCenter;
	private VirtualModelInstance<?, ?> virtualModelInstance;
	private Type expectedType;
	private FlexoConceptInstanceType defaultExpectedType;
	private FlexoConcept expectedFlexoConceptType = null;

	public FIBAbstractFMLRTObjectSelector(T editedObject) {
		super(editedObject);
		defaultExpectedType = editedObject != null ? FlexoConceptInstanceType.getFlexoConceptInstanceType(editedObject.getFlexoConcept())
				: FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
	}

	@Override
	public void delete() {
		super.delete();
		virtualModelInstance = null;
		expectedType = null;
		defaultExpectedType = null;
	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject != null) {
			return editedObject.getStringRepresentation();
		}
		return "";
	}

	@Override
	public void setServiceManager(FlexoServiceManager serviceManager) {
		super.setServiceManager(serviceManager);
		getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
	}

	public Object getRootObject() {

		if (getVirtualModelInstance() != null) {
			return getVirtualModelInstance();
		}
		else if (getProject() != null) {
			FlexoServiceManager sm = getProject().getServiceManager();
			FMLRTTechnologyAdapter fmlRTTA = sm.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			return fmlRTTA.getVirtualModelInstanceRepository(getProject()).getRootFolder();
		}
		else if (getResourceCenter() != null) {
			FlexoServiceManager sm = getResourceCenter().getServiceManager();
			FMLRTTechnologyAdapter fmlRTTA = sm.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			return fmlRTTA.getVirtualModelInstanceRepository(getResourceCenter()).getRootFolder();
		}
		else if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
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

	public boolean isVisibleValue(Object o) {

		if (!(o instanceof FlexoConceptInstance)) {
			return false;
		}
		if (!(getExpectedType() instanceof FlexoConceptInstanceType)) {
			return false;
		}
		FlexoConceptInstanceType fciType = (FlexoConceptInstanceType) getExpectedType();
		FlexoConceptInstance fci = (FlexoConceptInstance) o;

		if (fci.getEmbeddedFlexoConceptInstances().size() > 0) {
			// We display this FlexoConceptInstance if this is a potential container of expected type
			boolean contained = false;
			FlexoConcept current = fciType.getFlexoConcept();
			while (current != null) {
				if (current.isAssignableFrom(fci.getFlexoConcept())) {
					contained = true;
				}
				current = current.getApplicableContainerFlexoConcept();
			}
			if (contained) {
				return true;
			}
		}

		return (fciType.getFlexoConcept() == null) || (fciType.getFlexoConcept().isAssignableFrom(fci.getFlexoConcept()));

	}

	public FlexoResourceCenter<?> getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter<?> resourceCenter) {
		if ((resourceCenter == null && this.resourceCenter != null)
				|| (resourceCenter != null && !resourceCenter.equals(this.resourceCenter))) {
			FlexoResourceCenter<?> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	public VirtualModelInstance<?, ?> getVirtualModelInstance() {
		return virtualModelInstance;
	}

	public void setVirtualModelInstance(VirtualModelInstance<?, ?> virtualModelInstance) {

		if ((virtualModelInstance == null && this.virtualModelInstance != null)
				|| (virtualModelInstance != null && !virtualModelInstance.equals(this.virtualModelInstance))) {
			VirtualModelInstance<?, ?> oldValue = this.virtualModelInstance;
			this.virtualModelInstance = virtualModelInstance;
			getPropertyChangeSupport().firePropertyChange("virtualModelInstance", oldValue, virtualModelInstance);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		}
	}

	public Type getDefaultExpectedType() {
		return defaultExpectedType;
	}

	public Type getExpectedType() {
		if (expectedFlexoConceptType != null) {
			return expectedFlexoConceptType.getInstanceType();
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

	public List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources(RepositoryFolder<?, ?> folder) {
		if (folder.getResourceRepository() instanceof FMLRTVirtualModelInstanceRepository) {
			return (List) folder.getResources();
		}
		return null;
	}

	public boolean isFolderVisible(RepositoryFolder<?, ?> folder) {
		if (!folder.containsResources()) {
			return false;
		}
		if (getExpectedType() instanceof FlexoConceptInstanceType) {
			for (FMLRTVirtualModelInstanceResource r : getVirtualModelInstanceResources(folder)) {
				if (r.isLoaded() && isVirtualModelInstanceVisible(r.getLoadedResourceData())) {
					return true;
				}
			}
			for (RepositoryFolder<?, ?> childFolder : folder.getChildren()) {
				if (isFolderVisible(childFolder)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public boolean isVirtualModelInstanceVisible(VirtualModelInstance<?, ?> virtualModelInstance) {
		if (virtualModelInstance.getVirtualModelInstances() != null && virtualModelInstance.getVirtualModelInstances().size() > 0) {
			for (VirtualModelInstance<?, ?> containedVMI : virtualModelInstance.getVirtualModelInstances()) {
				if (isVirtualModelInstanceVisible(containedVMI)) {
					return true;
				}
			}
		}
		if (getExpectedType() instanceof VirtualModelInstanceType) {
			// We are expecting a VMI of following type
			VirtualModel vmType = ((VirtualModelInstanceType) getExpectedType()).getVirtualModel();
			return (vmType == null) || (vmType.isAssignableFrom(virtualModelInstance.getVirtualModel()));

		}
		if (getExpectedType() instanceof FlexoConceptInstanceType) {
			// In this case, we should display VMI whose contains FCI
			FlexoConcept conceptType = ((FlexoConceptInstanceType) getExpectedType()).getFlexoConcept();
			if (conceptType == null) {
				return true;
			}
			VirtualModel vmType = conceptType.getOwningVirtualModel();
			boolean takeIt = false;
			VirtualModel current = vmType;
			while (current != null && !takeIt) {
				if (current.isAssignableFrom(virtualModelInstance.getVirtualModel())) {
					takeIt = true;
				}
				current = current.getContainerVirtualModel();
			}

			/*if (!takeIt) {
				System.out.println("Do not display vmi " + virtualModelInstance);
			}*/
			return takeIt;

		}
		return true;
	}

}
