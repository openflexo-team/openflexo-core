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

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a Model while browsing in Information Space<br>
 * You may select a meta-model selected model should conform.
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBModelSelector extends FIBFlexoObjectSelector<FlexoModelResource> {

	static final Logger logger = Logger.getLogger(FIBModelSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/ModelSelector.fib");

	private InformationSpace informationSpace;
	private TechnologyAdapter technologyAdapter;
	private FlexoResourceCenter resourceCenter;
	private FlexoMetaModelResource metaModelResource;

	public FIBModelSelector(FlexoModelResource editedObject) {
		super(editedObject);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@Override
	public Class<FlexoModelResource> getRepresentedType() {
		return FlexoModelResource.class;
	}

	@Override
	public String renderedString(FlexoModelResource editedObject) {
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
		updateCustomPanel(getEditedObject());
	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	@CustomComponentParameter(name = "technologyAdapter", type = CustomComponentParameter.Type.OPTIONAL)
	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
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

	public FlexoMetaModelResource getMetaModelResource() {
		return metaModelResource;
	}

	@CustomComponentParameter(name = "metaModelResource", type = CustomComponentParameter.Type.OPTIONAL)
	public void setMetaModelResource(FlexoMetaModelResource metaModelResource) {
		this.metaModelResource = metaModelResource;
	}

	@Override
	protected boolean isAcceptableValue(Object o) {
		if (getMetaModelResource() == null) {
			return super.isAcceptableValue(o);
		} else {
			if (super.isAcceptableValue(o)) {
				if (o instanceof FlexoModelResource) {
					return ((FlexoModelResource) o).getMetaModelResource().equals(getMetaModelResource());
				}
			}
			return false;
		}
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

		final ViewPointLibrary viewPointLibrary;

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
				FIBModelSelector selector = new FIBModelSelector(null);
				selector.setInformationSpace(informationSpace);
				//try {
				//	selector.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
				//			(Class<TechnologyAdapter>) Class.forName("org.openflexo.technologyadapter.emf.EMFTechnologyAdapter")));
				//} catch (ClassNotFoundException e) {
				//	e.printStackTrace();
				//}
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
