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

import java.util.logging.Logger;

import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a MetaModel while browsing in Information Space
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBMetaModelSelector extends FIBFlexoObjectSelector<FlexoMetaModelResource> {

	static final Logger logger = Logger.getLogger(FIBMetaModelSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/MetaModelSelector.fib");

	private ResourceManager resourceManager;
	private TechnologyAdapter technologyAdapter;

	public FIBMetaModelSelector(FlexoMetaModelResource editedObject) {
		super(editedObject);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@Override
	public Class<FlexoMetaModelResource> getRepresentedType() {
		return FlexoMetaModelResource.class;
	}

	@Override
	public String renderedString(FlexoMetaModelResource editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public ResourceManager getResourceManager() {
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

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	@CustomComponentParameter(name = "technologyAdapter", type = CustomComponentParameter.Type.OPTIONAL)
	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		if (this.technologyAdapter != technologyAdapter) {
			TechnologyAdapter oldValue = this.technologyAdapter;
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
	
		final VirtualModelLibrary viewPointLibrary;
	
		final FlexoServiceManager serviceManager = new DefaultFlexoServiceManager() {
			@Override
			protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
				return null;
			}
	
			@Override
			protected FlexoEditor createApplicationEditor() {
				return null;
			}
		};
		TechnologyAdapterControllerService tacService = DefaultTechnologyAdapterControllerService.getNewInstance();
		serviceManager.registerService(tacService);
		final InformationSpace informationSpace = serviceManager.getInformationSpace();
	
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				FIBMetaModelSelector selector = new FIBMetaModelSelector(null);
				selector.setInformationSpace(informationSpace);
				try {
					selector.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
							(Class<TechnologyAdapter>) Class.forName("org.openflexo.technologyadapter.emf.EMFTechnologyAdapter")));
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
