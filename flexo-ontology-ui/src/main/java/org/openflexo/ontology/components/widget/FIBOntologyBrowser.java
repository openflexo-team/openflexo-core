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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.fib.model.FIBBrowser;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBContainer;
import org.openflexo.fib.swing.FIBJPanel;
import org.openflexo.fib.view.widget.FIBBrowserWidget;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to browse an ontology.<br>
 * 
 * This widget provides many configuration options:
 * <ul>
 * <li>context: required, defines ontology to browse</li>
 * <li>strictMode: required, default is false, when true, indicates that properties are retrieved from declared context ontology only</li>
 * <li>hierarchicalMode: required, default is true, defines if properties are stored relative to a storage class, defined either as the
 * domain class, or the top-level class where this property is used as a restriction</li>
 * <li>rootClass: when set, defines top-level class used as storage location, available in hierarchical mode only</li>
 * </ul>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBOntologyBrowser extends FIBJPanel<FIBOntologyBrowser> implements PropertyChangeListener {

	static final Logger logger = Logger.getLogger(FIBOntologyBrowser.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/FIBOntologyBrowser.fib");

	protected PropertyChangeListenerRegistrationManager manager = new PropertyChangeListenerRegistrationManager();

	private boolean allowsSearch = true;
	private boolean displayOptions = true;

	protected OntologyBrowserModel model = null;
	private TechnologyAdapter technologyAdapter;

	private boolean isSearching = false;
	private String filteredName;
	private final List<IFlexoOntologyConcept> matchingValues;
	private IFlexoOntologyConcept selectedValue;

	public FIBOntologyBrowser(IFlexoOntology ontology) {
		this(FIB_FILE, ontology);
	}

	protected FIBOntologyBrowser(Resource fibFile, IFlexoOntology ontology) {
		super(fibFile, null, FlexoLocalization.getMainLocalizer());
		matchingValues = new ArrayList<IFlexoOntologyConcept>();
		model = makeBrowserModel(ontology);
		// setOntology(ontology);
		if (ontology != null) {
			setTechnologyAdapter(ontology.getTechnologyAdapter());
		}
		setEditedObject(this);
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

	public IFlexoOntology getOntology() {
		// return ontology;
		return getModel().getContext();
	}

	@CustomComponentParameter(name = "ontology", type = CustomComponentParameter.Type.MANDATORY)
	public void setOntology(IFlexoOntology context) {
		IFlexoOntology oldValue = getOntology();
		if (oldValue != context) {
			if (getOntology() instanceof HasPropertyChangeSupport
					&& ((HasPropertyChangeSupport) getOntology()).getDeletedProperty() != null) {
				manager.removeListener(((HasPropertyChangeSupport) getOntology()).getDeletedProperty(), this, getOntology());
			}
			getModel().setContext(context);
			// this.ontology = context;
			if ((getOntology() instanceof HasPropertyChangeSupport)
					&& ((HasPropertyChangeSupport) getOntology()).getDeletedProperty() != null) {
				manager.addListener(((HasPropertyChangeSupport) getOntology()).getDeletedProperty(), this, getOntology());
			}
			update();
			getPropertyChangeSupport().firePropertyChange("ontology", oldValue, context);
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

	public IFlexoOntologyClass getRootClass() {
		return getModel().getRootClass();
	}

	@CustomComponentParameter(name = "rootClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRootClass(IFlexoOntologyClass rootClass) {
		IFlexoOntologyClass oldValue = getRootClass();
		if (oldValue != rootClass) {
			model.setRootClass(rootClass);
			update();
			getPropertyChangeSupport().firePropertyChange("rootClass", oldValue, rootClass);
		}
	}

	public boolean getShowObjectProperties() {
		return getModel().getShowObjectProperties();
	}

	@CustomComponentParameter(name = "showObjectProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowObjectProperties(boolean showObjectProperties) {
		boolean oldValue = getShowObjectProperties();
		if (oldValue != showObjectProperties) {
			model.setShowObjectProperties(showObjectProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("showObjectProperties", oldValue, showObjectProperties);
		}
	}

	public boolean getShowDataProperties() {
		return getModel().getShowDataProperties();
	}

	@CustomComponentParameter(name = "showDataProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowDataProperties(boolean showDataProperties) {
		boolean oldValue = getShowDataProperties();
		if (oldValue != showDataProperties) {
			model.setShowDataProperties(showDataProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("showDataProperties", oldValue, showDataProperties);
		}
	}

	public boolean getShowAnnotationProperties() {
		return getModel().getShowAnnotationProperties();
	}

	@CustomComponentParameter(name = "showAnnotationProperties", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowAnnotationProperties(boolean showAnnotationProperties) {
		boolean oldValue = getShowAnnotationProperties();
		if (oldValue != showAnnotationProperties) {
			model.setShowAnnotationProperties(showAnnotationProperties);
			update();
			getPropertyChangeSupport().firePropertyChange("showAnnotationProperties", oldValue, showAnnotationProperties);
		}
	}

	public boolean getShowClasses() {
		return getModel().getShowClasses();
	}

	@CustomComponentParameter(name = "showClasses", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowClasses(boolean showClasses) {
		boolean oldValue = getShowClasses();
		if (oldValue != showClasses) {
			model.setShowClasses(showClasses);
			update();
			getPropertyChangeSupport().firePropertyChange("showClasses", oldValue, showClasses);
		}
	}

	public boolean getShowIndividuals() {
		return getModel().getShowIndividuals();
	}

	@CustomComponentParameter(name = "showIndividuals", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowIndividuals(boolean showIndividuals) {
		boolean oldValue = getShowIndividuals();
		if (oldValue != showIndividuals) {
			model.setShowIndividuals(showIndividuals);
			update();
			getPropertyChangeSupport().firePropertyChange("showIndividuals", oldValue, showIndividuals);
		}
	}

	public boolean getAllowsSearch() {
		return allowsSearch;
	}

	@CustomComponentParameter(name = "allowsSearch", type = CustomComponentParameter.Type.OPTIONAL)
	public void setAllowsSearch(boolean allowsSearch) {
		this.allowsSearch = allowsSearch;
	}

	public boolean getDisplayOptions() {
		return displayOptions;
	}

	@CustomComponentParameter(name = "displayOptions", type = CustomComponentParameter.Type.OPTIONAL)
	public void setDisplayOptions(boolean displayOptions) {
		this.displayOptions = displayOptions;
	}

	public IFlexoOntologyClass getDomain() {
		return getModel().getDomain();
	}

	@CustomComponentParameter(name = "domain", type = CustomComponentParameter.Type.OPTIONAL)
	public void setDomain(IFlexoOntologyClass domain) {
		IFlexoOntologyClass oldValue = getDomain();
		if (oldValue != domain) {
			model.setDomain(domain);
			update();
			getPropertyChangeSupport().firePropertyChange("domain", oldValue, domain);
		}
	}

	public IFlexoOntologyClass getRange() {
		return getModel().getRange();
	}

	@CustomComponentParameter(name = "range", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRange(IFlexoOntologyClass range) {
		IFlexoOntologyClass oldValue = getRange();
		if (oldValue != range) {
			model.setRange(range);
			update();
			getPropertyChangeSupport().firePropertyChange("range", oldValue, range);
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

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	protected OntologyBrowserModel performBuildOntologyBrowserModel(IFlexoOntology ontology) {
		return new OntologyBrowserModel(ontology);
	}

	/**
	 * Build browser model<br>
	 * Override this method when required
	 * 
	 * @return
	 */
	protected OntologyBrowserModel makeBrowserModel(IFlexoOntology ontology) {
		OntologyBrowserModel returned = null;
		if (getTechnologyAdapter() != null) {
			// Use technology specific browser model
			TechnologyAdapterController<?> technologyAdapterController = getTechnologyAdapter().getTechnologyAdapterService()
					.getServiceManager().getService(TechnologyAdapterControllerService.class)
					.getTechnologyAdapterController(technologyAdapter);
			if (technologyAdapterController instanceof FlexoOntologyTechnologyAdapterController) {
				returned = ((FlexoOntologyTechnologyAdapterController) technologyAdapterController).makeOntologyBrowserModel(ontology);
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
		returned.setShowClasses(true);
		returned.setShowIndividuals(true);
		returned.setShowObjectProperties(true);
		returned.setShowDataProperties(true);
		returned.setShowAnnotationProperties(true);

		returned.enableAutoUpdate();
		returned.recomputeStructure();

		return returned;
	}

	public OntologyBrowserModel<?> getModel() {
		return model;
	}

	public void update() {
		getPropertyChangeSupport().firePropertyChange("model", null, getModel());
	}

	public String getFilteredName() {
		return filteredName;
	}

	public void setFilteredName(String filteredName) {
		this.filteredName = filteredName;
	}

	public List<IFlexoOntologyConcept> getMatchingValues() {
		return matchingValues;
	}

	public void search() {
		if (StringUtils.isNotEmpty(getFilteredName())) {
			logger.info("Searching " + getFilteredName());
			matchingValues.clear();
			for (IFlexoOntologyConcept o : getAllSelectableValues()) {
				if (o.getName().indexOf(getFilteredName()) > -1) {
					if (!matchingValues.contains(o)) {
						matchingValues.add(o);
					}
				}
			}

			isSearching = true;
			getPropertyChangeSupport().firePropertyChange("isSearching", false, true);
			getPropertyChangeSupport().firePropertyChange("matchingValues", null, matchingValues);

			if (matchingValues.size() == 1) {
				selectValue(matchingValues.get(0));
			}
		}

	}

	public void dismissSearch() {
		logger.info("Dismiss search");

		isSearching = false;
		getPropertyChangeSupport().firePropertyChange("isSearching", true, false);
	}

	public boolean isSearching() {
		return isSearching;
	}

	public ImageIcon getCancelIcon() {
		return UtilsIconLibrary.CANCEL_ICON;
	}

	public ImageIcon getSearchIcon() {
		return UtilsIconLibrary.SEARCH_ICON;
	}

	public String getButtonText() {
		if (isSearching()) {
			return "done";
		} else {
			return "search";
		}
	}

	@Override
	public Class<FIBOntologyBrowser> getRepresentedType() {
		return FIBOntologyBrowser.class;
	}

	/**
	 * This method is used to retrieve all potential values when implementing completion<br>
	 * Completion will be performed on that selectable values<br>
	 * Default implementation is to iterate on all values of browser, please take care to infinite loops.<br>
	 * 
	 * Override when required
	 */
	protected Vector<IFlexoOntologyConcept> getAllSelectableValues() {
		Vector<IFlexoOntologyConcept> returned = new Vector<IFlexoOntologyConcept>();
		FIBBrowserWidget browserWidget = retrieveFIBBrowserWidget();
		if (browserWidget == null) {
			return null;
		}
		Iterator<Object> it = browserWidget.getBrowserModel().recursivelyExploreModelToRetrieveContents();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof IFlexoOntologyConcept) {
				returned.add((IFlexoOntologyConcept) o);
			}
		}
		return returned;
	}

	public FIBBrowser getFIBBrowser() {
		if (getFIBComponent() instanceof FIBContainer) {
			List<FIBComponent> listComponent = ((FIBContainer) getFIBComponent()).getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBBrowser) {
					return (FIBBrowser) c;
				}
			}
		}
		return null;
	}

	private FIBBrowserWidget retrieveFIBBrowserWidget() {
		if (getFIBComponent() instanceof FIBContainer) {
			List<FIBComponent> listComponent = ((FIBContainer) getFIBComponent()).getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBBrowser) {
					return (FIBBrowserWidget) getController().viewForComponent(c);
				}
			}
		}
		return null;
	}

	public IFlexoOntologyConcept getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(IFlexoOntologyConcept selectedValue) {
		IFlexoOntologyConcept oldSelected = this.selectedValue;
		this.selectedValue = selectedValue;
		getPropertyChangeSupport().firePropertyChange("selectedValue", oldSelected, selectedValue);
	}

	public void selectValue(IFlexoOntologyConcept selectedValue) {
		getController().selectionCleared();
		getController().objectAddedToSelection(selectedValue);
	}

	public abstract ImageIcon getOntologyClassIcon();

	public abstract ImageIcon getOntologyIndividualIcon();

	public abstract ImageIcon getOntologyDataPropertyIcon();

	public abstract ImageIcon getOntologyObjectPropertyIcon();

	public abstract ImageIcon getOntologyAnnotationPropertyIcon();

	public abstract boolean supportTechnologySpecificHiddenConcepts();

	public abstract String technologySpecificHiddenConceptsLabel();

	/**
	 * Override when required
	 * 
	 * @return
	 */
	public boolean showTechnologySpecificConcepts() {
		return false;
	}

	/**
	 * Override when required
	 * 
	 * @return
	 */
	public void setShowTechnologySpecificConcepts(boolean flag) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getOntology()
				&& ((HasPropertyChangeSupport) getOntology()).getDeletedProperty().equals(evt.getPropertyName())) {
			logger.warning("Detecting ontology deleted in FIBOntologyBrowser");
		}
	}

}
