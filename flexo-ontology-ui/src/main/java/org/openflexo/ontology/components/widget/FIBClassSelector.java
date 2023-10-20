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

package org.openflexo.ontology.components.widget;

import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to select an IFlexoOntologyClass<br>
 * 
 * This widget provides many configuration options:
 * <ul>
 * <li>context: required, defines ontology context</li>
 * <li>strictMode: required, default is false, when true, indicates that properties are retrieved from declared context ontology only</li>
 * <li>hierarchicalMode: required, default is true, defines if properties are stored relative to a storage class, defined either as the
 * domain class, or the top-level class where this property is used as a restriction</li>
 * <li>rootClass: when set, defines top-level class used as storage location, available in hierarchical mode only</li>
 * </ul>
 * 
 * @author sylvain
 * 
 * @param TA
 *            type of {@link TechnologyAdapter}
 * 
 */
@SuppressWarnings("serial")
public class FIBClassSelector<TA extends TechnologyAdapter<TA>> extends FIBFlexoObjectSelector<IFlexoOntologyClass<TA>> {
	static final Logger logger = Logger.getLogger(FIBClassSelector.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/FIBClassSelector.fib");

	private ResourceManager resourceManager;

	protected OntologyBrowserModel<TA> model = null;
	private TA technologyAdapter;

	public FIBClassSelector(IFlexoOntologyClass<TA> editedObject) {
		super(editedObject);
		model = makeBrowserModel(editedObject != null ? editedObject.getOntology() : null);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<IFlexoOntologyClass<TA>> getRepresentedType() {
		return (Class) IFlexoOntologyClass.class;
	}

	public ResourceManager getResourceManager() {
		if (resourceManager == null && getServiceManager() != null) {
			resourceManager = getServiceManager().getResourceManager();
		}
		return resourceManager;
	}

	@CustomComponentParameter(name = "resourceManager", type = CustomComponentParameter.Type.MANDATORY)
	public void setResourceManager(ResourceManager resourceManager) {

		if (this.resourceManager != resourceManager) {
			ResourceManager oldValue = this.resourceManager;
			this.resourceManager = resourceManager;
			getPropertyChangeSupport().firePropertyChange("resourceManager", oldValue, resourceManager);
			updateCustomPanel(getEditedObject());
		}
	}

	@Override
	public String renderedString(IFlexoOntologyClass<TA> editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public String getContextOntologyURI() {
		if (getContext() != null) {
			return getContext().getURI();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@CustomComponentParameter(name = "contextOntologyURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setContextOntologyURI(String ontologyURI) {
		// logger.info("Sets ontology with " + ontologyURI);
		if (getResourceManager() != null) {
			FlexoModelResource<?, ?, ?, ?> modelResource = getResourceManager().getModelWithURI(ontologyURI);
			if (modelResource != null && modelResource.getModel() instanceof IFlexoOntology) {
				setContext((IFlexoOntology<TA>) modelResource.getModel());
			}
		}
	}

	public IFlexoOntology<TA> getContext() {
		return getModel().getContext();
	}

	@CustomComponentParameter(name = "context", type = CustomComponentParameter.Type.MANDATORY)
	public void setContext(IFlexoOntology<TA> context) {

		// TODO Fixe this !
		System.out.println("setContext in FIBClassSelector with" + context);
		getModel().setStrictMode(true);

		IFlexoOntology<TA> oldValue = getContext();
		if (oldValue != context) {
			getModel().setContext(context);
			update();
			getPropertyChangeSupport().firePropertyChange("context", oldValue, context);
			getPropertyChangeSupport().firePropertyChange("ontology", oldValue, context);
		}
	}

	public IFlexoOntology<TA> getOntology() {
		return getContext();
	}

	public void setOntology(IFlexoOntology<TA> ontology) {
		setContext(ontology);
	}

	public String getRootClassURI() {
		if (getRootClass() != null) {
			return getRootClass().getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "rootClassURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setRootClassURI(String aRootClassURI) {
		// logger.info("Sets rootClassURI with " + aRootClassURI + " context=" + getContext());
		if (getContext() != null) {
			IFlexoOntologyClass<TA> rootClass = getContext().getClass(aRootClassURI);
			if (rootClass != null) {
				setRootClass(rootClass);
			}
		}
	}

	public IFlexoOntologyClass<TA> getRootClass() {
		return getModel().getRootClass();
	}

	@CustomComponentParameter(name = "rootClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRootClass(IFlexoOntologyClass<TA> rootClass) {
		IFlexoOntologyClass<TA> oldValue = getRootClass();
		if (oldValue != rootClass) {
			model.setRootClass(rootClass);
			update();
			getPropertyChangeSupport().firePropertyChange("rootClass", oldValue, rootClass);
		}
	}

	public boolean getStrictMode() {
		return getModel().getStrictMode();
	}

	@CustomComponentParameter(name = "strictMode", type = CustomComponentParameter.Type.OPTIONAL)
	public void setStrictMode(boolean strictMode) {
		boolean oldValue = getStrictMode();
		if (oldValue != strictMode) {
			model.setStrictMode(strictMode);
			update();
			getPropertyChangeSupport().firePropertyChange("strictMode", oldValue, strictMode);
		}
	}

	public boolean getHierarchicalMode() {
		return getModel().getHierarchicalMode();
	}

	@CustomComponentParameter(name = "hierarchicalMode", type = CustomComponentParameter.Type.OPTIONAL)
	public void setHierarchicalMode(boolean hierarchicalMode) {
		boolean oldValue = getHierarchicalMode();
		if (oldValue != hierarchicalMode) {
			model.setHierarchicalMode(hierarchicalMode);
			update();
			getPropertyChangeSupport().firePropertyChange("hierarchicalMode", oldValue, hierarchicalMode);
		}
	}

	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TA technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	protected OntologyBrowserModel<TA> performBuildOntologyBrowserModel(IFlexoOntology<TA> ontology) {
		return new OntologyBrowserModel<>(ontology);
	}

	/**
	 * Build browser model<br>
	 * Override this method when required
	 * 
	 * @return
	 */
	protected OntologyBrowserModel<TA> makeBrowserModel(IFlexoOntology<TA> ontology) {
		OntologyBrowserModel<TA> returned = null;
		if (getTechnologyAdapter() != null) {
			// Use technology specific browser model
			TechnologyAdapterController<TA> technologyAdapterController = getTechnologyAdapter().getTechnologyAdapterService()
					.getServiceManager().getService(TechnologyAdapterControllerService.class)
					.getTechnologyAdapterController(technologyAdapter);
			if (technologyAdapterController instanceof FlexoOntologyTechnologyAdapterController) {
				returned = ((FlexoOntologyTechnologyAdapterController<TA>) technologyAdapterController).makeOntologyBrowserModel(ontology);
			}
		}
		if (returned == null) {
			// Use default
			returned = performBuildOntologyBrowserModel(ontology);
		}

		returned.disableAutoUpdate();
		returned.setStrictMode(false);
		returned.setHierarchicalMode(true);
		returned.setDisplayPropertiesInClasses(false);
		returned.setShowClasses(true);
		returned.setShowIndividuals(false);
		returned.setShowObjectProperties(false);
		returned.setShowDataProperties(false);
		returned.setShowAnnotationProperties(false);
		returned.enableAutoUpdate();
		returned.recomputeStructure();

		return returned;
	}

	public OntologyBrowserModel<TA> getModel() {
		return model;
	}

	public void update() {
		getPropertyChangeSupport().firePropertyChange("model", null, getModel());
	}
}
