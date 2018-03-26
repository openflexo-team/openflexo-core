/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.components.widget;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceType;
import org.openflexo.foundation.resource.ITechnologySpecificFlexoResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a Resource while browsing in Information Space<br>
 * You may select a resource kind.
 * 
 * @author sguerin, vincent leildé
 * 
 */
@SuppressWarnings("serial")
public class FIBResourceSelector extends FIBFlexoObjectSelector<TechnologyAdapterResource> {

	static final Logger logger = Logger.getLogger(FIBResourceSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/ResourceSelector.fib");

	private ResourceManager resourceManager;
	private TechnologyAdapter<?> technologyAdapter;
	private FlexoResourceCenter<?> resourceCenter;
	private Class<? extends ResourceData<?>> resourceDataClass;
	private Type expectedType;
	private FlexoResourceType defaultExpectedType;

	public FIBResourceSelector(TechnologyAdapterResource editedObject) {
		super(editedObject);
		// logger.info(">>>>>>>>>> Create FIBResourceSelector: " + Integer.toHexString(hashCode()));
		if (editedObject != null && editedObject.getServiceManager() != null) {
			defaultExpectedType = FlexoResourceType.getFlexoResourceType(editedObject.getResourceDataClass(),
					editedObject.getServiceManager().getTechnologyAdapterService());
		}
		else {
			defaultExpectedType = FlexoResourceType.UNDEFINED_RESOURCE_TYPE;
		}
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<TechnologyAdapterResource> getRepresentedType() {
		return TechnologyAdapterResource.class;
	}

	@Override
	public String renderedString(TechnologyAdapterResource editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public ResourceManager getResourceManager() {
		if (resourceManager == null && getServiceManager() != null) {
			return getServiceManager().getResourceManager();
		}
		return resourceManager;
	}

	@CustomComponentParameter(name = "resourceManager", type = CustomComponentParameter.Type.MANDATORY)
	public void setResourceManager(ResourceManager resourceManager) {

		if (this.resourceManager != resourceManager) {
			ResourceManager oldValue = this.resourceManager;
			this.resourceManager = resourceManager;
			getPropertyChangeSupport().firePropertyChange("resourceManager", oldValue, resourceManager);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
			updateCustomPanel(getEditedObject());
		}
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		if (technologyAdapter == null && getServiceManager() != null && getExpectedType() instanceof FlexoResourceType) {
			ITechnologySpecificFlexoResourceFactory<?, ?, ?> resourceFactory = ((FlexoResourceType) getExpectedType()).getResourceFactory();
			if (resourceFactory != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(resourceFactory.getTechnologyAdapterClass());
			}
		}
		return technologyAdapter;
	}

	@CustomComponentParameter(name = "technologyAdapter", type = CustomComponentParameter.Type.OPTIONAL)
	public void setTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
		if (this.technologyAdapter != technologyAdapter) {
			TechnologyAdapter<?> oldValue = this.technologyAdapter;
			this.technologyAdapter = technologyAdapter;
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
			updateCustomPanel(getEditedObject());
		}
	}

	public Object getRootObject() {
		if (getTechnologyAdapter() != null) {
			return getTechnologyAdapter();
		}
		else {
			return getResourceManager();
		}
	}

	public FlexoResourceCenter<?> getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter<?> resourceCenter) {
		if (resourceCenter == null || !resourceCenter.equals(this.resourceCenter)) {
			FlexoResourceCenter<?> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
		}
	}

	// IMPORTANT: used in ResourceSelector.fib
	public <RD extends ResourceData<RD>> Class<RD> getTypedResourceDataClass() {
		return (Class<RD>) resourceDataClass;
	}

	public Class<? extends ResourceData<?>> getResourceDataClass() {
		if (resourceDataClass == null && getExpectedType() instanceof FlexoResourceType
				&& ((FlexoResourceType) getExpectedType()).getResourceFactory() != null) {
			return ((FlexoResourceType) getExpectedType()).getResourceFactory().getResourceDataClass();
		}
		return resourceDataClass;
	}

	@CustomComponentParameter(name = "resourceDataClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setResourceDataClass(Class<? extends ResourceData<?>> resourceDataClass) {
		// System.out.println("set resource data class with " + resourceDataClass);
		this.resourceDataClass = resourceDataClass;
		fireEditedObjectChanged();
	}

	public Type getExpectedType() {
		if (expectedType == null) {
			return defaultExpectedType;
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

	@Override
	protected boolean isAcceptableValue(Object o) {

		if (super.isAcceptableValue(o)) {
			if (o instanceof TechnologyAdapterResource) {
				if (getTechnologyAdapter() != null) {
					if (((TechnologyAdapterResource<?, ?>) o).getTechnologyAdapter() != getTechnologyAdapter()) {
						return false;
					}
				}
				if (getResourceDataClass() != null) {
					return getResourceDataClass().isAssignableFrom(((TechnologyAdapterResource<?, ?>) o).getResourceDataClass());
				}
				return true;
			}
		}
		return false;
	}

	public boolean isFolderVisible(RepositoryFolder<?, ?> folder) {
		if (!folder.containsResources()) {
			return false;
		}
		return true;
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not instantiated in EDIT mode)
	/*public static void main(String[] args) {
	
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.INFO, null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		// final VirtualModelLibrary viewPointLibrary;
	
		final FlexoServiceManager serviceManager = new TestFlexoServiceManager(new FileResource(
				"C:/Users/Vincent/git/openflexo/packaging/technologyadaptersintegration/src/test/resources/TestResourceCenter"));
	
		/*final FlexoServiceManager serviceManager = new DefaultFlexoServiceManager() {
			@Override
			protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
				return null;
			}
	
			@Override
			protected FlexoEditor createApplicationEditor() {
				return null;
			}
		};*/
	/*TechnologyAdapterControllerService tacService = DefaultTechnologyAdapterControllerService.getNewInstance();
	serviceManager.registerService(tacService);
	
	final InformationSpace informationSpace = serviceManager.getInformationSpace();
	
	FIBAbstractEditor editor = new FIBAbstractEditor() {
		@Override
		public Object[] getData() {
			FIBResourceSelector selector = new FIBResourceSelector(null);
			selector.setInformationSpace(informationSpace);
			try {
				selector.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
						(Class<TechnologyAdapter>) Class.forName("org.openflexo.technologyadapter.excel.ExcelTechnologyAdapter")));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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
