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
package org.openflexo.components.widget;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;

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

	public static String FIB_FILE_NAME = "Fib/ResourceSelector.fib";

	private InformationSpace informationSpace;
	private TechnologyAdapter technologyAdapter;
	private FlexoResourceCenter resourceCenter;
	private Class<? extends ResourceData<?>> resourceDataClass;

	public FIBResourceSelector(TechnologyAdapterResource editedObject) {
		super(editedObject);
	}

	@Override
	public String getFIBFileName() {
		return FIB_FILE_NAME;
	}

	@Override
	public Class<TechnologyAdapterResource> getRepresentedType() {
		return TechnologyAdapterResource.class;
	}

	@Override
	public String renderedString(TechnologyAdapterResource editedObject) {
		if (editedObject != null) {
			return editedObject.getURI();
		}
		return "";
	}

	public InformationSpace getInformationSpace() {
		return informationSpace;
	}

	@CustomComponentParameter(name = "informationSpace", type = CustomComponentParameter.Type.MANDATORY)
	public void setInformationSpace(InformationSpace informationSpace) {

		this.informationSpace = informationSpace;
		getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		updateCustomPanel(getEditedObject());

	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	@CustomComponentParameter(name = "technologyAdapter", type = CustomComponentParameter.Type.OPTIONAL)
	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		// System.out.println(">>>>>>>>> SETS TechnologyAdapter with " + technologyAdapter);
		this.technologyAdapter = technologyAdapter;
		getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
		updateCustomPanel(getEditedObject());
	}

	public Object getRootObject() {
		if (getTechnologyAdapter() != null) {
			return getTechnologyAdapter();
		} else {
			return getInformationSpace();
		}
	}

	public FlexoResourceCenter getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter resourceCenter) {
		this.resourceCenter = resourceCenter;
	}

	// IMPORTANT: used in ResourceSelector.fib
	public <RD extends ResourceData<RD>> Class<RD> getTypedResourceDataClass() {
		return (Class<RD>) resourceDataClass;
	}

	public Class<? extends ResourceData<?>> getResourceDataClass() {
		return resourceDataClass;
	}

	@CustomComponentParameter(name = "resourceDataClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setResourceDataClass(Class<? extends ResourceData<?>> resourceDataClass) {
		System.out.println("set resource data class with " + resourceDataClass);
		this.resourceDataClass = resourceDataClass;
		fireEditedObjectChanged();
	}

	//

	@Override
	protected boolean isAcceptableValue(Object o) {

		if (super.isAcceptableValue(o)) {
			if (o instanceof TechnologyAdapterResource) {
				if (getTechnologyAdapter() != null) {
					if (((TechnologyAdapterResource) o).getTechnologyAdapter() != getTechnologyAdapter()) {
						return false;
					}
				}
				if (getResourceDataClass() != null) {
					return getResourceDataClass().isAssignableFrom(((TechnologyAdapterResource) o).getResourceDataClass());
				}
				return true;
			}
		}
		return false;
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not instantiated in EDIT mode)
	/*public static void main(String[] args) {

		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.INFO, null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// final ViewPointLibrary viewPointLibrary;

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