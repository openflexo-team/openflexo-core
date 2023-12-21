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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.widget.FIBBrowser;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent.CustomComponentParameter;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.SelectionSynchronizedFIBView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to edit/view an ontology.<br>
 * 
 * @author sguerin
 * 
 * @param TA
 *            type of {@link TechnologyAdapter}
 */
@SuppressWarnings("serial")
public abstract class FIBOntologyEditor<TA extends TechnologyAdapter<TA>> extends SelectionSynchronizedFIBView {
	static final Logger logger = Logger.getLogger(FIBOntologyEditor.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/FIBOntologyEditor.fib");

	private boolean allowsSearch = true;
	private boolean displayOptions = true;

	protected OntologyBrowserModel<TA> model = null;
	private TA technologyAdapter;

	private boolean isSearching = false;
	private String filteredName;
	private final List<IFlexoOntologyConcept<TA>> matchingValues;
	private IFlexoOntologyConcept<TA> selectedValue;

	public FIBOntologyEditor(IFlexoOntology<TA> ontology, FlexoController controller, LocalizedDelegate locales) {
		this(ontology, controller, FIB_FILE, locales);
	}

	public FIBOntologyEditor(IFlexoOntology<TA> ontology, FlexoController controller, Resource fibFile, LocalizedDelegate locales) {
		super(null, controller, fibFile, locales);
		matchingValues = new ArrayList<>();
		model = makeBrowserModel(ontology);
		// setOntology(ontology);
		if (ontology != null) {
			setTechnologyAdapter(ontology.getTechnologyAdapter());
		}
		setDataObject(this);
	}

	public IFlexoOntology<TA> getOntology() {
		// return ontology;
		return getModel().getContext();
	}

	@CustomComponentParameter(name = "ontology", type = CustomComponentParameter.Type.MANDATORY)
	public void setOntology(IFlexoOntology<TA> context) {
		IFlexoOntology<TA> oldValue = getOntology();
		if (oldValue != context) {
			if (getOntology() != null && getOntology().getDeletedProperty() != null) {
				manager.removeListener(((HasPropertyChangeSupport) getOntology()).getDeletedProperty(), this, getOntology());
			}
			getModel().setContext(context);
			// this.ontology = context;
			if ((getOntology() != null) && getOntology().getDeletedProperty() != null) {
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

	public OntologyBrowserModel<TA> getModel() {
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

	public List<IFlexoOntologyConcept<TA>> getMatchingValues() {
		return matchingValues;
	}

	public void search() {
		if (StringUtils.isNotEmpty(getFilteredName())) {
			logger.info("Searching " + getFilteredName());
			matchingValues.clear();
			for (IFlexoOntologyConcept<TA> o : getAllSelectableValues()) {
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
		}
		return "search";
	}

	/**
	 * This method is used to retrieve all potential values when implementing completion<br>
	 * Completion will be performed on that selectable values<br>
	 * Default implementation is to iterate on all values of browser, please take care to infinite loops.<br>
	 * 
	 * Override when required
	 */
	@SuppressWarnings("unchecked")
	protected Vector<IFlexoOntologyConcept<TA>> getAllSelectableValues() {
		Vector<IFlexoOntologyConcept<TA>> returned = new Vector<>();
		JFIBBrowserWidget<?> browserWidget = retrieveFIBBrowserWidget();
		if (browserWidget == null) {
			return null;
		}
		Iterator<Object> it = browserWidget.getBrowserModel().recursivelyExploreModelToRetrieveContents();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof IFlexoOntologyConcept) {
				returned.add((IFlexoOntologyConcept<TA>) o);
			}
		}
		// System.out.println("Returned: (" + returned.size() + ") " + returned);
		return returned;
	}

	public Resource getFibForOntologyObject(IFlexoOntologyObject<TA> object) {
		if (object == null) {
			object = model.getContext();
			// return getFIBController().getFIBPanelForObject(model.getContext());
		}
		// No specific TechnologyAdapter, lookup in generic libraries
		return getFIBController().getFIBPanelForObject(object);
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

	@SuppressWarnings("rawtypes")
	private JFIBBrowserWidget<?> retrieveFIBBrowserWidget() {
		if (getFIBComponent() instanceof FIBContainer) {
			List<FIBComponent> listComponent = ((FIBContainer) getFIBComponent()).getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBBrowser) {
					return (JFIBBrowserWidget) getFIBController().viewForComponent(c);
				}
			}
		}
		return null;
	}

	public IFlexoOntologyConcept<TA> getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(IFlexoOntologyConcept<TA> selectedValue) {
		IFlexoOntologyConcept<TA> oldSelected = this.selectedValue;
		this.selectedValue = selectedValue;
		getPropertyChangeSupport().firePropertyChange("selectedValue", oldSelected, selectedValue);
	}

	public void selectValue(IFlexoOntologyConcept<TA> selectedValue) {
		getFIBController().selectionCleared();
		getFIBController().objectAddedToSelection(selectedValue);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getOntology()
				&& ((HasPropertyChangeSupport) getOntology()).getDeletedProperty().equals(evt.getPropertyName())) {
			deleteView();
		}
		super.propertyChange(evt);
	}

	public abstract ImageIcon getOntologyClassIcon();

	public abstract ImageIcon getOntologyIndividualIcon();

	public abstract ImageIcon getOntologyDataPropertyIcon();

	public abstract ImageIcon getOntologyObjectPropertyIcon();

	public abstract ImageIcon getOntologyAnnotationIcon();

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

}
