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
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to select an IFlexoOntologyStructuralProperty.<br>
 * 
 * This widget provides many configuration options:
 * <ul>
 * <li>context: required, defines ontology context</li>
 * <li>strictMode: required, default is false, when true, indicates that properties are retrieved from declared context ontology only</li>
 * <li>hierarchicalMode: required, default is true, defines if properties are stored relative to a storage class, defined either as the
 * domain class, or the top-level class where this property is used as a restriction</li>
 * <li>rootClass: when set, defines top-level class used as storage location, available in hierarchical mode only</li>
 * <li>domain: when set, defines the domain class, properties not declared having a domain are excluded from this selector</li>
 * <li>range: when set, defines the range class for object properties, properties not declared having a range are excluded from this
 * selector</li>
 * <li>dataType: when set, defines the dataType for data properties, properties not declared having a datatype are excluded from this
 * selector</li>
 * <li>selectObjectProperties, indicated if object properties should be retrieved</li>
 * <li>selectDataProperties, indicated if data properties should be retrieved</li>
 * <li>selectAnnotationProperties, indicated if annotation properties should be retrieved</li>
 * </ul>
 * 
 * @author sylvain
 * 
 * @param TA
 *            type of {@link TechnologyAdapter}
 * 
 */
@SuppressWarnings("serial")
public class FIBPropertySelector<TA extends TechnologyAdapter<TA>> extends FIBFlexoObjectSelector<IFlexoOntologyStructuralProperty<TA>> {

	static final Logger logger = Logger.getLogger(FIBPropertySelector.class.getPackage().getName());

	public static final Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/FIBPropertySelector.fib");

	private ResourceManager resourceManager;

	protected OntologyBrowserModel<TA> model = null;

	public FIBPropertySelector(IFlexoOntologyStructuralProperty<TA> editedObject) {
		super(editedObject);
		model = makeBrowserModel(editedObject != null ? editedObject.getOntology() : null);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<IFlexoOntologyStructuralProperty<TA>> getRepresentedType() {
		return (Class) IFlexoOntologyStructuralProperty.class;
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
	public String renderedString(IFlexoOntologyStructuralProperty<TA> editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public IFlexoOntology<TA> getContext() {
		return getModel().getContext();
	}

	@CustomComponentParameter(name = "context", type = CustomComponentParameter.Type.MANDATORY)
	public void setContext(IFlexoOntology<TA> context) {
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

	public IFlexoOntologyClass<TA> getDomain() {
		return getModel().getDomain();
	}

	@CustomComponentParameter(name = "domain", type = CustomComponentParameter.Type.OPTIONAL)
	public void setDomain(IFlexoOntologyClass<TA> domain) {
		IFlexoOntologyClass<TA> oldValue = getDomain();
		if (oldValue != domain) {
			model.setDomain(domain);
			update();
			getPropertyChangeSupport().firePropertyChange("domain", oldValue, domain);
		}
	}

	public String getDomainClassURI() {
		if (getDomain() != null) {
			return getDomain().getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "domainClassURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setDomainClassURI(String aDomainClassURI) {
		// logger.info("Sets domainClassURI with " + aDomainClassURI + " context=" + getContext());
		if (getContext() != null) {
			IFlexoOntologyClass<TA> rootClass = getContext().getClass(aDomainClassURI);
			if (rootClass != null) {
				setDomain(rootClass);
			}
		}
	}

	public IFlexoOntologyClass<TA> getRange() {
		return getModel().getRange();
	}

	@CustomComponentParameter(name = "range", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRange(IFlexoOntologyClass<TA> range) {
		IFlexoOntologyClass<TA> oldValue = getRange();
		if (oldValue != range) {
			model.setRange(range);
			update();
			getPropertyChangeSupport().firePropertyChange("range", oldValue, range);
		}
	}

	public String getRangeClassURI() {
		if (getRange() != null) {
			return getRange().getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "rangeClassURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setRangeClassURI(String aRangeClassURI) {
		// logger.info("Sets rangeClassURI with " + aRangeClassURI + " context=" + getContext());
		if (getContext() != null) {
			IFlexoOntologyClass<TA> rootClass = getContext().getClass(aRangeClassURI);
			if (rootClass != null) {
				setRange(rootClass);
			}
		}
	}

	public BuiltInDataType getDataType() {
		return getModel().getDataType();
	}

	@CustomComponentParameter(name = "dataType", type = CustomComponentParameter.Type.OPTIONAL)
	public void setDataType(BuiltInDataType dataType) {
		BuiltInDataType oldValue = getDataType();
		if (oldValue != dataType) {
			model.setDataType(dataType);
			update();
			getPropertyChangeSupport().firePropertyChange("dataType", oldValue, dataType);
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

	public boolean getSelectObjectProperties() {
		return getModel().getShowObjectProperties();
	}

	@CustomComponentParameter(name = "selectObjectProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setSelectObjectProperties(boolean selectObjectProperties) {
		boolean oldValue = getSelectObjectProperties();
		if (oldValue != selectObjectProperties) {
			model.setShowObjectProperties(selectObjectProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("selectObjectProperties", oldValue, selectObjectProperties);
		}
	}

	public boolean getSelectDataProperties() {
		return getModel().getShowDataProperties();
	}

	@CustomComponentParameter(name = "selectDataProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setSelectDataProperties(boolean selectDataProperties) {
		boolean oldValue = getSelectDataProperties();
		if (oldValue != selectDataProperties) {
			model.setShowDataProperties(selectDataProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("selectDataProperties", oldValue, selectDataProperties);
		}
	}

	public boolean getSelectAnnotationProperties() {
		return getModel().getShowAnnotationProperties();
	}

	@CustomComponentParameter(name = "selectAnnotationProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setSelectAnnotationProperties(boolean selectAnnotationProperties) {
		boolean oldValue = getSelectAnnotationProperties();
		if (oldValue != selectAnnotationProperties) {
			model.setShowAnnotationProperties(selectAnnotationProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("selectAnnotationProperties", oldValue, selectAnnotationProperties);
		}
	}

	public boolean getDisplayPropertiesInClasses() {
		return getModel().getDisplayPropertiesInClasses();
	}

	@CustomComponentParameter(name = "displayPropertiesInClasses", type = CustomComponentParameter.Type.OPTIONAL)
	public void setDisplayPropertiesInClasses(boolean displayPropertiesInClasses) {
		boolean oldValue = getDisplayPropertiesInClasses();
		if (oldValue != displayPropertiesInClasses) {
			model.setDisplayPropertiesInClasses(displayPropertiesInClasses);
			update();
			getPropertyChangeSupport().firePropertyChange("displayPropertiesInClasses", oldValue, displayPropertiesInClasses);
		}
	}

	private TA technologyAdapter;

	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TA technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	private ModelSlot<?> modelSlot;

	public ModelSlot<?> getModelSlot() {
		return modelSlot;
	}

	public void setModelSlot(ModelSlot<?> modelSlot) {
		this.modelSlot = modelSlot;
	}

	/**
	 * Return a metamodel adressed by a model slot
	 * 
	 * @return
	 */
	public FlexoMetaModel<?> getAdressedFlexoMetaModel() {
		if (modelSlot instanceof TypeAwareModelSlot) {
			TypeAwareModelSlot<?, ?> typeAwareModelSlot = (TypeAwareModelSlot<?, ?>) modelSlot;
			return typeAwareModelSlot.getMetaModelResource().getMetaModelData();
		}
		return null;
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
		returned.setDisplayPropertiesInClasses(true);
		returned.setShowClasses(false);
		returned.setShowIndividuals(false);
		returned.setShowObjectProperties(true);
		returned.setShowDataProperties(true);
		returned.setShowAnnotationProperties(true);
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
