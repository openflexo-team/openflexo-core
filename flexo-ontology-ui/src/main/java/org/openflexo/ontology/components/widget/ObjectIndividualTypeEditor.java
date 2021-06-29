/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.openflexo.ApplicationContext;
import org.openflexo.components.widget.DefaultCustomTypeEditorImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyTechnologyContextManager;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.StringUtils;

/**
 * An editor to edit a {@link IndividualOfClass} type
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/ObjectIndividualTypeEditor.fib")
public class ObjectIndividualTypeEditor<TA extends TechnologyAdapter<TA>, I extends IFlexoOntologyIndividual<TA>, C extends IFlexoOntologyClass<TA>, IC extends IndividualOfClass<TA, I, C>>
		extends DefaultCustomTypeEditorImpl<IC> {

	static final Logger logger = Logger.getLogger(ObjectIndividualTypeEditor.class.getPackage().getName());

	private C selectedClass = null;
	protected OntologyBrowserModel<TA> browserModel = null;
	private FlexoMetaModelResource<?, ?, TA> metaModelResource;
	private String filteredClassName = "";
	private List<C> matchingValues = new ArrayList<>();
	private String presentationName;
	private Class<TA> technologyAdapterClass;
	private Class<C> classClass;
	private Class<IC> typeClass;

	public ObjectIndividualTypeEditor(FlexoServiceManager serviceManager, Class<TA> technologyAdapterClass, Class<IC> typeClass,
			Class<C> classClass, String presentationName) {
		super(serviceManager);
		this.presentationName = presentationName;
		this.technologyAdapterClass = technologyAdapterClass;
		this.classClass = classClass;
		this.typeClass = typeClass;
	}

	@Override
	public String getPresentationName() {
		return presentationName;
	}

	@Override
	public Class<IC> getCustomType() {
		return typeClass;
	}

	public OntologyBrowserModel<TA> getBrowserModel() {
		return browserModel;
	}

	/*public void setBrowserModel(OntologyBrowserModel<TA> browserModel) {
		if ((browserModel == null && this.browserModel != null) || (browserModel != null && !browserModel.equals(this.browserModel))) {
			OntologyBrowserModel<TA> oldValue = this.browserModel;
			this.browserModel = browserModel;
			getPropertyChangeSupport().firePropertyChange("browserModel", oldValue, browserModel);
		}
	}*/

	public FlexoMetaModelResource<?, ?, TA> getMetaModelResource() {
		return metaModelResource;
	}

	public void setMetaModelResource(FlexoMetaModelResource<?, ?, TA> metaModelResource) {
		if ((metaModelResource == null && this.metaModelResource != null)
				|| (metaModelResource != null && !metaModelResource.equals(this.metaModelResource))) {
			FlexoMetaModelResource<?, ?, TA> oldValue = this.metaModelResource;
			this.metaModelResource = metaModelResource;
			getPropertyChangeSupport().firePropertyChange("modelResource", oldValue, metaModelResource);
			browserModel = makeBrowserModel(metaModelResource != null ? (IFlexoOntology<TA>) metaModelResource.getMetaModelData() : null);
			getPropertyChangeSupport().firePropertyChange("browserModel", null, getBrowserModel());
		}
	}

	public C getSelectedClass() {
		return selectedClass;
	}

	public void setSelectedClass(C selectedClass) {
		if ((selectedClass == null && this.selectedClass != null) || (selectedClass != null && !selectedClass.equals(this.selectedClass))) {
			C oldValue = this.selectedClass;
			this.selectedClass = selectedClass;
			getPropertyChangeSupport().firePropertyChange("selectedClass", oldValue, selectedClass);
			matchingValues.clear();
			getPropertyChangeSupport().firePropertyChange("matchingValues", null, matchingValues);
			getPropertyChangeSupport().firePropertyChange("searchLabel", null, getSearchLabel());
		}
	}

	@Override
	public IC getEditedType() {
		return (IC) ((FlexoOntologyTechnologyContextManager<TA>) getTechnologyAdapter().getTechnologyContextManager())
				.getIndividualOfClass(getSelectedClass());
	}

	public List<C> getMatchingValues() {
		return matchingValues;
	}

	public TA getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(technologyAdapterClass);
		}
		return null;
	}

	/*private EMFModel emfModel;
	
	public EMFModel getEMFModel() {
		return emfModel;
	}
	
	@CustomComponentParameter(name = "EMFModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setEMFModel(EMFModel emfModel) {
	
		if (this.emfModel != emfModel) {
			FlexoObject oldRoot = getRootObject();
			this.emfModel = emfModel;
			getPropertyChangeSupport().firePropertyChange("rootObject", oldRoot, getRootObject());
		}
	}*/

	/*public FlexoObject getRootObject() {
		if (getEMFModel() != null) {
			return getEMFModel();
		}
		else {
			// return getEMFTechnologyAdapter();
			return null;
		}
	}*/

	@Override
	public Resource getFIBComponentResource() {
		// TODO Auto-generated method stub
		Resource returned = super.getFIBComponentResource();
		System.out.println("Cool on retourne " + returned);
		return returned;
	}

	public ImageIcon getSearchIcon() {
		return UtilsIconLibrary.SEARCH_ICON;
	}

	/*public ImageIcon getDoneIcon() {
		return UtilsIconLibrary.CANCEL_ICON;
	}*/

	public String getSearchLabel() {
		if (matchingValues.size() >= 1) {
			return "Found " + matchingValues.size() + " classes";
		}
		if (StringUtils.isNotEmpty(getFilteredClassName())) {
			return "No matches";
		}
		return "You might use wildcards (* = any string) and press 'Search'";

	}

	/**
	 * Build browser model<br>
	 * Override this method when required
	 * 
	 * @return
	 */
	protected OntologyBrowserModel<TA> makeBrowserModel(IFlexoOntology<TA> ontology) {

		FlexoOntologyTechnologyAdapterController<TA> emfTAC = (FlexoOntologyTechnologyAdapterController<TA>) ((ApplicationContext) getServiceManager())
				.getTechnologyAdapterControllerService().getTechnologyAdapterController(getTechnologyAdapter());
		OntologyBrowserModel<TA> returned = emfTAC.makeOntologyBrowserModel(ontology);
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

	public String getFilteredClassName() {
		return filteredClassName;
	}

	public void setFilteredClassName(String filteredClassName) {
		if (filteredClassName == null || !filteredClassName.equals(this.filteredClassName)) {
			String oldValue = this.filteredClassName;
			this.filteredClassName = filteredClassName;
			updateMatchingClasses();
			getPropertyChangeSupport().firePropertyChange("filteredClassName", oldValue, filteredClassName);
			/*if (searchMode) {
				updateMatchingClasses();
				getPropertyChangeSupport().firePropertyChange("searchMode", !searchMode(), searchMode());
			}*/
		}
	}

	/*public void search() {
		// LOGGER.info("SEARCH " + filteredClassName);
		// isExplicitelySearching = true;
		// explicitelySearch();
		updateMatchingClasses();
		// isExplicitelySearching = false;
		if (getMatchingValues().size() != 1) {
			searchMode = true;
		}
		getPropertyChangeSupport().firePropertyChange("searchMode", !searchMode(), searchMode());
	}
	
	public void done() {
		logger.info("Done with SEARCH " + filteredClassName);
		searchMode = false;
		setFilteredClassName("");
		getPropertyChangeSupport().firePropertyChange("searchMode", !searchMode(), searchMode());
	}
	
	public boolean searchMode() {
		return getMatchingValues().size() != 1 && searchMode;
	}*/

	private void updateMatchingClasses() {

		logger.info("*************** updateMatchingClasses() for " + filteredClassName);

		final List<C> oldMatchingValues = new ArrayList<>(getMatchingValues());
		// System.out.println("updateMatchingValues() with " + getFilteredName());
		matchingValues.clear();

		if (StringUtils.isNotEmpty(getFilteredClassName())) {
			if (getAllSelectableValues() != null && getFilteredClassName() != null) {
				for (IFlexoOntologyClass<TA> next : getAllSelectableValues()) {
					if (classClass.isAssignableFrom(next.getClass())) {
						if (matches((C) next, getFilteredClassName())) {
							matchingValues.add((C) next);
						}
					}
				}
			}
		}
		logger.info("Objects matching with " + getFilteredClassName() + " found " + matchingValues.size() + " values");
		getPropertyChangeSupport().firePropertyChange("searchLabel", null, getSearchLabel());

		SwingUtilities.invokeLater(() -> {
			getPropertyChangeSupport().firePropertyChange("matchingValues", oldMatchingValues, getMatchingValues());
			if (matchingValues.size() == 1) {
				setSelectedClass(matchingValues.get(0));
			}
		});

	}

	public List<? extends IFlexoOntologyClass<TA>> getAllSelectableValues() {
		if (getMetaModelResource() != null) {
			return ((IFlexoOntology<TA>) getMetaModelResource().getMetaModelData()).getClasses();
		}
		return null;
	}

	protected boolean matches(C o, String filteredName) {
		return o != null && StringUtils.isNotEmpty(o.getName()) && (o.getName()).toUpperCase().indexOf(filteredName.toUpperCase()) > -1;
	}

}
