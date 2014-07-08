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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.FlexoOntologyObjectImpl;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.ontology.OntologyUtils;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Model supporting browsing through models or metamodels conform to {@link FlexoOntology} API<br>
 * 
 * Developers note: this model is shared by many widgets. Please modify it with caution.
 * 
 * @see FIBClassSelector
 * @see FIBIndividualSelector
 * @see FIBPropertySelector
 * 
 * @author sguerin
 */
public class OntologyBrowserModel<TA extends TechnologyAdapter> extends Observable implements FlexoObserver {

	static final Logger logger = Logger.getLogger(OntologyBrowserModel.class.getPackage().getName());

	private IFlexoOntology<TA> context;
	private boolean hierarchicalMode = true;
	private boolean strictMode = false;
	private IFlexoOntologyClass<TA> rootClass;
	private boolean displayPropertiesInClasses = true;
	private IFlexoOntologyClass<TA> domain = null;
	private IFlexoOntologyClass<TA> range = null;
	private BuiltInDataType dataType = null;

	private boolean showObjectProperties = true;
	private boolean showDataProperties = true;
	private boolean showAnnotationProperties = true;
	private boolean showClasses = true;
	private boolean showIndividuals = true;

	private List<IFlexoOntologyObject<TA>> roots = null;
	private Map<FlexoOntologyObjectImpl<TA>, List<FlexoOntologyObjectImpl<TA>>> structure = null;

	public OntologyBrowserModel(IFlexoOntology<TA> context) {
		super();
		setContext(context);
	}

	public List<IFlexoOntologyObject<TA>> getRoots() {
		if (roots == null) {
			recomputeStructure();
		}
		return roots;
	}

	public List<FlexoOntologyObjectImpl<TA>> getChildren(FlexoOntologyObjectImpl<TA> father) {
		return structure.get(father);
	}

	private boolean isRecomputingStructure = false;

	public void recomputeStructure() {

		logger.fine("BEGIN recomputeStructure for " + getContext());

		isRecomputingStructure = true;
		if (getHierarchicalMode()) {
			computeHierarchicalStructure();
		} else {
			computeNonHierarchicalStructure();
		}
		isRecomputingStructure = false;
		setChanged();
		notifyObservers(new OntologyBrowserModelRecomputed());

		logger.fine("END recomputeStructure for " + getContext());
	}

	public static class OntologyBrowserModelRecomputed {

	}

	public void delete() {
		context = null;
	}

	public IFlexoOntology<TA> getContext() {
		return context;
	}

	public void setContext(IFlexoOntology<TA> context) {
		if (this.context != null) {
			((FlexoObservable) context).deleteObserver(this);
		}
		this.context = context;
		if (this.context != null) {
			((FlexoObservable) context).addObserver(this);
		}
	}

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {
		if (isRecomputingStructure) {
			return;
		}
		recomputeStructure();
	}

	public IFlexoOntologyClass<TA> getRootClass() {
		return rootClass;
	}

	public void setRootClass(IFlexoOntologyClass<TA> rootClass) {
		this.rootClass = rootClass;
	}

	public boolean getHierarchicalMode() {
		return hierarchicalMode;
	}

	public void setHierarchicalMode(boolean hierarchicalMode) {
		this.hierarchicalMode = hierarchicalMode;
	}

	public boolean getStrictMode() {
		return strictMode;
	}

	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

	public boolean getDisplayPropertiesInClasses() {
		return displayPropertiesInClasses;
	}

	public void setDisplayPropertiesInClasses(boolean displayPropertiesInClasses) {
		this.displayPropertiesInClasses = displayPropertiesInClasses;
	}

	public boolean getShowObjectProperties() {
		return showObjectProperties;
	}

	public void setShowObjectProperties(boolean showObjectProperties) {
		this.showObjectProperties = showObjectProperties;
	}

	public boolean getShowDataProperties() {
		return showDataProperties;
	}

	public void setShowDataProperties(boolean showDataProperties) {
		this.showDataProperties = showDataProperties;
	}

	public boolean getShowAnnotationProperties() {
		return showAnnotationProperties;
	}

	public void setShowAnnotationProperties(boolean showAnnotationProperties) {
		this.showAnnotationProperties = showAnnotationProperties;
	}

	public boolean getShowClasses() {
		return showClasses;
	}

	public void setShowClasses(boolean showClasses) {
		this.showClasses = showClasses;
	}

	public boolean getShowIndividuals() {
		return showIndividuals;
	}

	public void setShowIndividuals(boolean showIndividuals) {
		this.showIndividuals = showIndividuals;
	}

	public IFlexoOntologyClass<TA> getDomain() {
		return domain;
	}

	public void setDomain(IFlexoOntologyClass<TA> domain) {
		this.domain = domain;
	}

	public IFlexoOntologyClass<TA> getRange() {
		return range;
	}

	public void setRange(IFlexoOntologyClass<TA> range) {
		this.range = range;
	}

	public BuiltInDataType getDataType() {
		return dataType;
	}

	public void setDataType(BuiltInDataType dataType) {
		this.dataType = dataType;
	}

	public boolean isDisplayable(IFlexoOntologyObject<TA> object) {

		if (object instanceof IFlexoOntology) {
			return true;
		}

		boolean returned = false;

		if (object instanceof IFlexoOntologyClass && showClasses) {
			if (getRootClass() != null && object instanceof IFlexoOntologyConcept) {
				returned = getRootClass().isSuperConceptOf((IFlexoOntologyConcept<TA>) object);
			} else {
				returned = true;
			}
		}
		if (object instanceof IFlexoOntologyIndividual && showIndividuals) {
			if (getRootClass() != null && object instanceof IFlexoOntologyConcept) {
				returned = getRootClass().isSuperConceptOf((IFlexoOntologyConcept<TA>) object);
			} else {
				returned = true;
			}
		}
		if (object instanceof IFlexoOntologyObjectProperty && showObjectProperties) {
			returned = true;
		}
		if (object instanceof IFlexoOntologyDataProperty && showDataProperties) {
			returned = true;
		}
		if (object instanceof IFlexoOntologyStructuralProperty && ((IFlexoOntologyStructuralProperty<TA>) object).isAnnotationProperty()
				&& showAnnotationProperties) {
			returned = true;
		}

		if (returned == false) {
			return false;
		}

		if (object instanceof IFlexoOntologyStructuralProperty && getRootClass() != null) {
			boolean foundAPreferredLocationAsSubClassOfRootClass = false;
			List<IFlexoOntologyClass<TA>> preferredLocation = getPreferredStorageLocations((IFlexoOntologyStructuralProperty<TA>) object,
					null);
			for (IFlexoOntologyClass<TA> pl : preferredLocation) {
				if (rootClass.isSuperConceptOf(pl)) {
					foundAPreferredLocationAsSubClassOfRootClass = true;
					break;
				}
			}
			if (!foundAPreferredLocationAsSubClassOfRootClass) {
				return false;
			}
		}

		if (object instanceof IFlexoOntologyStructuralProperty && getDomain() != null) {
			IFlexoOntologyStructuralProperty<TA> p = (IFlexoOntologyStructuralProperty<TA>) object;
			if (p.getDomain() instanceof IFlexoOntologyClass) {
				if (!((IFlexoOntologyClass<TA>) p.getDomain()).equals(getDomain())
						&& !((IFlexoOntologyClass<TA>) p.getDomain()).isSuperClassOf(getDomain())) {
					// System.out.println("Dismiss " + object + " becasuse " + p.getDomain().getName() + " is not superclass of "
					// + getDomain().getName());
					return false;
				}
				/*if (!getDomain().isSuperClassOf(((IFlexoOntologyClass) p.getDomain()))) {
					return false;
				}*/
			} else {
				// System.out.println("Dismiss " + object + " becasuse domain=" + p.getDomain());
				return false;
			}
		}

		if (object instanceof IFlexoOntologyObjectProperty && getRange() != null) {
			IFlexoOntologyObjectProperty<TA> p = (IFlexoOntologyObjectProperty<TA>) object;
			if (p.getRange() instanceof IFlexoOntologyClass) {
				if (!((IFlexoOntologyClass<TA>) p.getRange()).isSuperClassOf(getRange())) {
					return false;
				}
				/*if (!getRange().isSuperClassOf(((IFlexoOntologyClass) p.getRange()))) {
					return false;
				}*/
			} else {
				return false;
			}
		}

		if (object instanceof IFlexoOntologyDataProperty && getDataType() != null) {
			IFlexoOntologyDataProperty<TA> p = (IFlexoOntologyDataProperty<TA>) object;
			if (p.getRange() != getDataType()) {
				// System.out.println("Dismiss " + object + " becasuse " + p.getDataType() + " is not  " + getDataType());
				return false;
			}
		}

		return true;
	}

	private void appendOntologyContents(IFlexoOntology<TA> o, IFlexoOntologyObject<TA> parent) {
		List<IFlexoOntologyStructuralProperty<TA>> properties = new ArrayList<IFlexoOntologyStructuralProperty<TA>>();
		List<IFlexoOntologyIndividual<TA>> individuals = new ArrayList<IFlexoOntologyIndividual<TA>>();
		Hashtable<IFlexoOntologyStructuralProperty<TA>, List<IFlexoOntologyClass<TA>>> storedProperties = new Hashtable<IFlexoOntologyStructuralProperty<TA>, List<IFlexoOntologyClass<TA>>>();
		Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>> storedIndividuals = new Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>>();
		List<IFlexoOntologyStructuralProperty<TA>> unstoredProperties = new ArrayList<IFlexoOntologyStructuralProperty<TA>>();
		List<IFlexoOntologyIndividual<TA>> unstoredIndividuals = new ArrayList<IFlexoOntologyIndividual<TA>>();
		List<IFlexoOntologyClass<TA>> storageClasses = new ArrayList<IFlexoOntologyClass<TA>>();
		properties = retrieveDisplayableProperties(o);
		individuals = retrieveDisplayableIndividuals(o);

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : properties) {
				List<IFlexoOntologyClass<TA>> preferredLocations = getPreferredStorageLocations(p, o);
				if (preferredLocations != null && preferredLocations.size() > 0) {
					storedProperties.put(p, preferredLocations);
					for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
						if (!storageClasses.contains(preferredLocation)) {
							storageClasses.add(preferredLocation);
						}
					}
				} else {
					unstoredProperties.add(p);
				}
			}
		}

		if (showIndividuals) {
			for (IFlexoOntologyIndividual<TA> i : individuals) {
				IFlexoOntologyClass<TA> preferredLocation = getPreferredStorageLocation(i);
				if (preferredLocation != null) {
					storedIndividuals.put(i, preferredLocation);
					if (!storageClasses.contains(preferredLocation)) {
						storageClasses.add(preferredLocation);
					}
				} else {
					unstoredIndividuals.add(i);
				}
			}
		}

		if (parent != null && parent != o) {
			addChildren(parent, o);
		}

		if (showClasses) {
			List<IFlexoOntologyClass<TA>> classes = retrieveDisplayableClasses(o);
			if (classes.size() > 0) {
				removeOriginalFromRedefinedObjects(classes);
				addClassesAsHierarchy(parent == null ? null : o, classes);
			}
		} else if (getDisplayPropertiesInClasses() || showIndividuals) {
			removeOriginalFromRedefinedObjects(storageClasses);
			appendParentClassesToStorageClasses(storageClasses);
			addClassesAsHierarchy(parent == null ? null : o, storageClasses);
		}

		for (IFlexoOntologyIndividual<TA> i : storedIndividuals.keySet()) {
			IFlexoOntologyClass<TA> preferredLocation = storedIndividuals.get(i);
			addChildren(preferredLocation, i);
		}

		for (IFlexoOntologyIndividual<TA> i : unstoredIndividuals) {
			addChildren(parent == null ? null : o, i);
		}

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : storedProperties.keySet()) {
				List<IFlexoOntologyClass<TA>> preferredLocations = storedProperties.get(p);
				for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
					addChildren(preferredLocation, p);
				}
			}

			addPropertiesAsHierarchy(parent == null ? null : o, unstoredProperties);
		}

		else {
			addPropertiesAsHierarchy(parent == null ? null : o, properties);
		}
	}

	private void computeNonHierarchicalStructure() {
		if (roots != null) {
			roots.clear();
		} else {
			roots = new ArrayList<IFlexoOntologyObject<TA>>();
		}
		if (structure != null) {
			structure.clear();
		} else {
			structure = new Hashtable<FlexoOntologyObjectImpl<TA>, List<FlexoOntologyObjectImpl<TA>>>();
		}

		if (getContext() == null) {
			return;
		}

		if (strictMode) {

			appendOntologyContents(getContext(), null);

		} else {
			// System.out.println("computeNonHierarchicalStructure()");
			// System.out.println("context=" + getContext());
			// System.out.println("imported ontologies: " + getContext().getImportedOntologies());
			// System.out.println("all imported ontologies: " + OntologyUtils.getAllImportedOntologies(getContext()));

			roots.add(getContext());
			appendOntologyContents(getContext(), getContext());
			for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
				// System.out.println("Hop " + o + " displayable: " + isDisplayable(o));
				if (o != getContext() && isDisplayable(o)) {
					appendOntologyContents(o, getContext());
				}
			}
		}
	}

	private void addPropertiesAsHierarchy(IFlexoOntologyObject<TA> parent, List<IFlexoOntologyStructuralProperty<TA>> someProperties) {
		for (IFlexoOntologyStructuralProperty<TA> p : someProperties) {
			if (!hasASuperPropertyDefinedInList(p, someProperties)) {
				appendPropertyInHierarchy(parent, p, someProperties);
			}
		}
	}

	private void appendPropertyInHierarchy(IFlexoOntologyObject<TA> parent, IFlexoOntologyStructuralProperty<TA> p,
			List<IFlexoOntologyStructuralProperty<TA>> someProperties) {
		if (parent == null) {
			roots.add(p);
		} else {
			addChildren(parent, p);
		}
		for (IFlexoOntologyStructuralProperty<TA> subProperty : p.getSubProperties(getContext())) {
			if (someProperties.contains(subProperty)) {
				appendPropertyInHierarchy(p, subProperty, someProperties);
			}
		}
	}

	private boolean hasASuperPropertyDefinedInList(IFlexoOntologyStructuralProperty<TA> p,
			List<IFlexoOntologyStructuralProperty<TA>> someProperties) {
		if (p.getSuperProperties() == null) {
			return false;
		} else {
			for (IFlexoOntologyStructuralProperty<TA> sp : p.getSuperProperties()) {
				if (someProperties.contains(sp)) {
					return true;
				}
			}
			return false;
		}
	}

	private void addChildren(IFlexoOntologyObject<TA> parent, IFlexoOntologyObject<TA> child) {
		List<FlexoOntologyObjectImpl<TA>> v = structure.get(parent);
		if (v == null) {
			v = new ArrayList<FlexoOntologyObjectImpl<TA>>();
			structure.put((FlexoOntologyObjectImpl<TA>) parent, v);
		}
		if (!v.contains(child)) {
			v.add((FlexoOntologyObjectImpl<TA>) child);
		}
	}

	private void computeHierarchicalStructure() {

		logger.fine("computeHierarchicalStructure()");

		if (roots != null) {
			roots.clear();
		} else {
			roots = new ArrayList<IFlexoOntologyObject<TA>>();
		}
		if (structure != null) {
			structure.clear();
		} else {
			structure = new Hashtable<FlexoOntologyObjectImpl<TA>, List<FlexoOntologyObjectImpl<TA>>>();
		}

		List<IFlexoOntologyStructuralProperty<TA>> properties = new ArrayList<IFlexoOntologyStructuralProperty<TA>>();
		Hashtable<IFlexoOntologyStructuralProperty<TA>, List<IFlexoOntologyClass<TA>>> storedProperties = new Hashtable<IFlexoOntologyStructuralProperty<TA>, List<IFlexoOntologyClass<TA>>>();
		List<IFlexoOntologyStructuralProperty<TA>> unstoredProperties = new ArrayList<IFlexoOntologyStructuralProperty<TA>>();
		List<IFlexoOntologyClass<TA>> storageClasses = new ArrayList<IFlexoOntologyClass<TA>>();

		List<IFlexoOntologyIndividual<TA>> individuals = new ArrayList<IFlexoOntologyIndividual<TA>>();
		Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>> storedIndividuals = new Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>>();
		List<IFlexoOntologyIndividual<TA>> unstoredIndividuals = new ArrayList<IFlexoOntologyIndividual<TA>>();

		if (getContext() == null) {
			return;
		}

		if (strictMode) {
			properties = retrieveDisplayableProperties(getContext());
			individuals = retrieveDisplayableIndividuals(getContext());
		} else {
			for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
				properties.addAll(retrieveDisplayableProperties(o));
				individuals.addAll(retrieveDisplayableIndividuals(o));
			}
		}

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : properties) {
				List<IFlexoOntologyClass<TA>> preferredLocations = getPreferredStorageLocations(p, null);
				if (preferredLocations != null && preferredLocations.size() > 0) {
					storedProperties.put(p, preferredLocations);
					for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
						if (!storageClasses.contains(preferredLocation)) {
							storageClasses.add(preferredLocation);
						}
					}
				} else {
					unstoredProperties.add(p);
				}
			}
		}

		if (showIndividuals) {
			for (IFlexoOntologyIndividual<TA> i : individuals) {
				IFlexoOntologyClass<TA> preferredLocation = getPreferredStorageLocation(i);
				if (preferredLocation != null) {
					storedIndividuals.put(i, preferredLocation);
					if (!storageClasses.contains(preferredLocation)) {
						storageClasses.add(preferredLocation);
					}
				} else {
					unstoredIndividuals.add(i);
				}
			}
		}

		if (getShowClasses()) {
			List<IFlexoOntologyClass<TA>> classes = new ArrayList<IFlexoOntologyClass<TA>>();
			if (strictMode) {
				classes = retrieveDisplayableClasses(getContext());
			} else {
				for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
					classes.addAll(retrieveDisplayableClasses(o));
				}
			}
			removeOriginalFromRedefinedObjects(classes);
			addClassesAsHierarchy(null, classes);
		} else if (getDisplayPropertiesInClasses() || showIndividuals) {
			removeOriginalFromRedefinedObjects(storageClasses);
			appendParentClassesToStorageClasses(storageClasses);
			removeOriginalFromRedefinedObjects(storageClasses);
			addClassesAsHierarchy(null, storageClasses);
		}

		for (IFlexoOntologyIndividual<TA> i : storedIndividuals.keySet()) {
			IFlexoOntologyClass<TA> preferredLocation = storedIndividuals.get(i);
			addChildren(preferredLocation, i);
		}

		for (IFlexoOntologyIndividual<TA> i : unstoredIndividuals) {
			addChildren(getContext().getRootConcept(), i);
		}

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : storedProperties.keySet()) {
				List<IFlexoOntologyClass<TA>> preferredLocations = storedProperties.get(p);
				for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
					addChildren(preferredLocation, p);
				}
			}
			addPropertiesAsHierarchy(null, unstoredProperties);
		} else {
			addPropertiesAsHierarchy(null, properties);
		}

	}

	/**
	 * Compute a list of preferred location for an ontology property to be displayed.<br>
	 * If searchedOntology is not null, restrict returned list to classes declared in supplied ontology
	 * 
	 * @param p
	 * @param searchedOntology
	 * @return
	 */
	protected List<IFlexoOntologyClass<TA>> getPreferredStorageLocations(IFlexoOntologyStructuralProperty<TA> p,
			IFlexoOntology<TA> searchedOntology) {
		List<IFlexoOntologyClass<TA>> potentialStorageClasses = new ArrayList<IFlexoOntologyClass<TA>>();

		// First we look if property has a defined domain
		if (p.getDomain() instanceof IFlexoOntologyClass) {
			// Return the most specialized definition
			IFlexoOntology<TA> ontology = searchedOntology != null ? searchedOntology : getContext();
			IFlexoOntologyClass<TA> c = ontology.getClass(((IFlexoOntologyClass<TA>) p.getDomain()).getURI());
			if (c == null) {
				c = (IFlexoOntologyClass<TA>) p.getDomain();
			}
			if (c != null && (searchedOntology == null || c.getOntology() == searchedOntology)) {
				potentialStorageClasses.add(c);
				return potentialStorageClasses;
			}
		}

		return potentialStorageClasses;

		/*if (potentialStorageClasses.size() > 0) {
			return potentialStorageClasses.get(0);
		}*/

		/*if (p.getStorageLocations().size() > 0) {
			return p.getStorageLocations().get(0);
		}*/
		// return null;
	}

	private IFlexoOntologyClass<TA> getPreferredStorageLocation(IFlexoOntologyIndividual<TA> i) {

		// Return the first class which is not the Thing concept
		for (IFlexoOntologyClass<TA> c : i.getTypes()) {
			if (c.isNamedClass() && !c.isRootConcept()) {
				IFlexoOntologyClass<TA> returned = getContext().getClass(c.getURI());
				if (returned != null) {
					return returned;
				} else {
					return c;
				}
			}
		}
		return getContext().getRootConcept();
	}

	private void addClassesAsHierarchy(IFlexoOntologyObject<TA> parent, List<IFlexoOntologyClass<TA>> someClasses) {
		if (someClasses.contains(getContext().getRootConcept())) {
			appendClassInHierarchy(parent, getContext().getRootConcept(), someClasses);
		} else {
			List<IFlexoOntologyClass<TA>> listByExcludingRootClasses = new ArrayList<IFlexoOntologyClass<TA>>(someClasses);
			List<IFlexoOntologyClass<TA>> localRootClasses = new ArrayList<IFlexoOntologyClass<TA>>();
			for (IFlexoOntologyClass<TA> c : someClasses) {
				if (!hasASuperClassDefinedInList(c, someClasses)) {
					localRootClasses.add(c);
					listByExcludingRootClasses.remove(c);
				}
			}
			for (IFlexoOntologyClass<TA> c : localRootClasses) {
				List<IFlexoOntologyClass<TA>> potentialChildren = new ArrayList<IFlexoOntologyClass<TA>>();
				for (IFlexoOntologyClass<TA> c2 : listByExcludingRootClasses) {
					if (c.isSuperConceptOf(c2)) {
						potentialChildren.add(c2);
					}
				}
				appendClassInHierarchy(parent, c, potentialChildren);
			}
		}
	}

	private void appendClassInHierarchy(IFlexoOntologyObject<TA> parent, IFlexoOntologyClass<TA> c,
			List<IFlexoOntologyClass<TA>> someClasses) {

		List<IFlexoOntologyClass<TA>> listByExcludingCurrentClass = new ArrayList<IFlexoOntologyClass<TA>>(someClasses);
		listByExcludingCurrentClass.remove(c);

		if (parent == null) {
			roots.add(c);
		} else {
			addChildren(parent, c);
		}
		if (listByExcludingCurrentClass.size() > 0) {
			addClassesAsHierarchy(c, listByExcludingCurrentClass);
		}
	}

	private boolean hasASuperClassDefinedInList(IFlexoOntologyClass<TA> c, List<IFlexoOntologyClass<TA>> someClasses) {
		if (c.getSuperClasses() == null) {
			return false;
		} else {
			for (IFlexoOntologyClass<TA> c2 : someClasses) {
				if (c2.isSuperConceptOf(c) /*&& c2 != c*/&& !c2.getURI().equals(c.getURI())) {
					return true;
				}
			}
			return false;
		}
	}

	private List<IFlexoOntologyConcept<TA>> retrieveDisplayableObjects(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyConcept<TA>> returned = new ArrayList<IFlexoOntologyConcept<TA>>();
		for (IFlexoOntologyClass<TA> c : ontology.getClasses()) {
			if (isDisplayable(c)) {
				returned.add(c);
			}
		}
		for (IFlexoOntologyIndividual<TA> i : ontology.getIndividuals()) {
			if (isDisplayable(i)) {
				returned.add(i);
			}
		}
		for (IFlexoOntologyStructuralProperty<TA> p : ontology.getObjectProperties()) {
			if (isDisplayable(p)) {
				returned.add(p);
			}
		}
		for (IFlexoOntologyStructuralProperty<TA> p : ontology.getDataProperties()) {
			if (isDisplayable(p)) {
				returned.add(p);
			}
		}
		return returned;
	}

	/**
	 * Remove originals from redefined classes<br>
	 * Special case: original Thing definition is kept and redefinitions are excluded
	 * 
	 * @param list
	 */
	protected void removeOriginalFromRedefinedObjects(List<? extends IFlexoOntologyConcept<TA>> list) {
		for (IFlexoOntologyConcept<TA> c : new ArrayList<IFlexoOntologyConcept<TA>>(list)) {
			if (c instanceof IFlexoOntologyClass && ((IFlexoOntologyClass<TA>) c).isRootConcept() && c.getOntology() != getContext()
					&& list.contains(getContext().getRootConcept())) {
				list.remove(c);
			}
		}
	}

	private void appendParentClassesToStorageClasses(List<IFlexoOntologyClass<TA>> someClasses) {
		// System.out.println("appendParentClassesToStorageClasses with " + someClasses);

		// First compute the list of all top-level classes
		List<IFlexoOntologyClass<TA>> topLevelClasses = new ArrayList<IFlexoOntologyClass<TA>>();
		for (IFlexoOntologyClass<TA> c : someClasses) {
			boolean requireAddInTopClasses = true;
			List<IFlexoOntologyClass<TA>> classesToRemove = new ArrayList<IFlexoOntologyClass<TA>>();
			for (IFlexoOntologyClass<TA> tpC : topLevelClasses) {
				if (tpC.isSuperClassOf(c)) {
					requireAddInTopClasses = false;
				}
				if (c.isSuperClassOf(tpC)) {
					classesToRemove.add(tpC);
				}
			}
			if (requireAddInTopClasses) {
				topLevelClasses.add(c);
				for (IFlexoOntologyClass<TA> c2r : classesToRemove) {
					topLevelClasses.remove(c2r);
				}
			}
		}

		List<IFlexoOntologyClass<TA>> classesToAdd = new ArrayList<IFlexoOntologyClass<TA>>();
		if (someClasses.size() > 1) {
			for (int i = 0; i < topLevelClasses.size(); i++) {
				for (int j = i + 1; j < topLevelClasses.size(); j++) {
					// System.out.println("i=" + i + " j=" + j + " someClasses.size()=" + someClasses.size());
					// System.out.println("i=" + i + " j=" + j + " someClasses.size()=" + someClasses.size() + " someClasses=" +
					// someClasses);
					IFlexoOntologyClass<TA> c1 = topLevelClasses.get(i);
					IFlexoOntologyClass<TA> c2 = topLevelClasses.get(j);
					IFlexoOntologyClass<TA> ancestor = OntologyUtils.getFirstCommonAncestor(c1, c2);
					// System.out.println("Ancestor of " + c1 + " and " + c2 + " is " + ancestor);
					if (ancestor != null /*&& !ancestor.isThing()*/) {
						IFlexoOntologyClass<TA> ancestorSeenFromContextOntology = getContext().getClass(ancestor.getURI());
						if (ancestorSeenFromContextOntology != null) {
							if (!someClasses.contains(ancestorSeenFromContextOntology)
									&& !classesToAdd.contains(ancestorSeenFromContextOntology)) {
								classesToAdd.add(ancestorSeenFromContextOntology);
								// System.out.println("Add parent " + ancestorSeenFromContextOntology + " because of c1=" + c1.getName()
								// + " and c2=" + c2.getName());
							}
						}
					}
				}
			}
			if (classesToAdd.size() > 0) {
				for (IFlexoOntologyClass<TA> c : classesToAdd) {
					someClasses.add(c);
				}

				// Do it again whenever there are classes to add
				appendParentClassesToStorageClasses(someClasses);
			}
		}
	}

	/*private void removeOriginalFromRedefinedClasses(List<IFlexoOntologyClass> list) {
		// Remove originals from redefined classes
		for (IFlexoOntologyClass c : new ArrayList<IFlexoOntologyClass>(list)) {
			if (c.redefinesOriginalDefinition()) {
				list.remove(c.getOriginalDefinition());
			}
		}
	}*/

	private List<IFlexoOntologyClass<TA>> retrieveDisplayableClasses(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyClass<TA>> returned = new ArrayList<IFlexoOntologyClass<TA>>();
		for (IFlexoOntologyClass<TA> c : ontology.getClasses()) {
			if (isDisplayable(c)) {
				returned.add(c);
			}
		}
		/*if (!ontology.getURI().equals(OntologyLibrary.RDF_ONTOLOGY_URI) && !ontology.getURI().equals(OntologyLibrary.RDFS_ONTOLOGY_URI)) {
			System.out.println("Thing " + ontology.getRootClass() + " refines " + ontology.getRootClass().getOriginalDefinition());
		}*/
		removeOriginalFromRedefinedObjects(returned);
		return returned;
	}

	private List<IFlexoOntologyIndividual<TA>> retrieveDisplayableIndividuals(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyIndividual<TA>> returned = new ArrayList<IFlexoOntologyIndividual<TA>>();
		for (IFlexoOntologyIndividual<TA> c : ontology.getIndividuals()) {
			if (isDisplayable(c)) {
				returned.add(c);
			}
		}
		return returned;
	}

	private List<IFlexoOntologyStructuralProperty<TA>> retrieveDisplayableProperties(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyStructuralProperty<TA>> returned = new ArrayList<IFlexoOntologyStructuralProperty<TA>>();
		for (IFlexoOntologyStructuralProperty<TA> p : ontology.getObjectProperties()) {
			if (isDisplayable(p)) {
				returned.add(p);
			}
		}
		for (IFlexoOntologyStructuralProperty<TA> p : ontology.getDataProperties()) {
			if (isDisplayable(p)) {
				returned.add(p);
			}
		}
		return returned;
	}

	public Font getFont(IFlexoOntologyConcept<TA> object, Font baseFont) {
		if (baseFont != null && object.getOntology() != getContext()) {
			return baseFont.deriveFont(Font.ITALIC);
		}
		return baseFont;
	}

}
