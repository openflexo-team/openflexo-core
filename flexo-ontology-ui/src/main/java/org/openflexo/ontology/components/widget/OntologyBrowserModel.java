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

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.annotations.NotificationUnsafe;
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
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;

/**
 * Model supporting browsing through models or metamodels conform to {@link IFlexoOntology} API<br>
 * 
 * Developers note: this model is shared by many widgets. Please modify it with caution.
 * 
 * @see FIBClassSelector
 * @see FIBIndividualSelector
 * @see FIBPropertySelector
 * 
 * @author sguerin
 */
public class OntologyBrowserModel<TA extends TechnologyAdapter<TA>> implements HasPropertyChangeSupport, PropertyChangeListener {

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

	private final PropertyChangeSupport pcSupport;

	public OntologyBrowserModel(IFlexoOntology<TA> context) {
		super();
		pcSupport = new PropertyChangeSupport(this);
		setContext(context);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public List<IFlexoOntologyObject<TA>> getRoots() {
		if (roots == null && !isRecomputingStructure) {
			recomputeStructure();
		}
		return roots;
	}

	@NotificationUnsafe
	public List<FlexoOntologyObjectImpl<TA>> getChildren(IFlexoOntologyObject<TA> father) {
		if (father == null) {
			return null;
		}
		if (structure != null) {
			return structure.get(father);
		}
		return null;
	}

	protected boolean autoUpdate = true;

	public void enableAutoUpdate() {
		autoUpdate = true;
	}

	public void disableAutoUpdate() {
		autoUpdate = false;
	}

	private boolean isRecomputingStructure = false;

	public final void recomputeStructure() {

		logger.info("BEGIN recomputeStructure for " + getContext());

		isRecomputingStructure = true;
		if (getContext() != null) {
			if (getHierarchicalMode()) {
				computeHierarchicalStructure();
			}
			else {
				computeNonHierarchicalStructure();
			}
		}
		// printHierarchy();

		getPropertyChangeSupport().firePropertyChange("roots", null, roots);

		isRecomputingStructure = false;

		logger.info("END recomputeStructure for " + getContext());
	}

	protected void printHierarchy() {
		if (roots != null) {
			for (IFlexoOntologyObject<TA> root : roots) {
				printHierarchy(root, 0);
			}
		}
	}

	private void printHierarchy(IFlexoOntologyObject<TA> node, int level) {
		System.out.println(StringUtils.buildWhiteSpaceIndentation(level) + " > " + node.getName());
		if (getChildren(node) != null) {
			for (IFlexoOntologyObject<TA> child : getChildren(node)) {
				printHierarchy(child, level + 2);
			}
		}
	}

	public void delete() {
		context = null;
	}

	public IFlexoOntology<TA> getContext() {
		return context;
	}

	public void setContext(IFlexoOntology<TA> context) {
		boolean changed = this.context != context;
		if (changed) {
			if (this.context != null) {
				this.context.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			this.context = context;
			if (context != null) {
				context.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			if (autoUpdate) {
				recomputeStructure();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
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
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getHierarchicalMode() {
		return hierarchicalMode;
	}

	public void setHierarchicalMode(boolean hierarchicalMode) {
		this.hierarchicalMode = hierarchicalMode;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getStrictMode() {
		return strictMode;
	}

	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getDisplayPropertiesInClasses() {
		return displayPropertiesInClasses;
	}

	public void setDisplayPropertiesInClasses(boolean displayPropertiesInClasses) {
		this.displayPropertiesInClasses = displayPropertiesInClasses;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getShowObjectProperties() {
		return showObjectProperties;
	}

	public void setShowObjectProperties(boolean showObjectProperties) {
		this.showObjectProperties = showObjectProperties;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getShowDataProperties() {
		return showDataProperties;
	}

	public void setShowDataProperties(boolean showDataProperties) {
		this.showDataProperties = showDataProperties;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getShowAnnotationProperties() {
		return showAnnotationProperties;
	}

	public void setShowAnnotationProperties(boolean showAnnotationProperties) {
		this.showAnnotationProperties = showAnnotationProperties;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getShowClasses() {
		return showClasses;
	}

	public void setShowClasses(boolean showClasses) {
		this.showClasses = showClasses;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public boolean getShowIndividuals() {
		return showIndividuals;
	}

	public void setShowIndividuals(boolean showIndividuals) {
		this.showIndividuals = showIndividuals;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public IFlexoOntologyClass<TA> getDomain() {
		return domain;
	}

	public void setDomain(IFlexoOntologyClass<TA> domain) {
		this.domain = domain;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public IFlexoOntologyClass<TA> getRange() {
		return range;
	}

	public void setRange(IFlexoOntologyClass<TA> range) {
		this.range = range;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	public BuiltInDataType getDataType() {
		return dataType;
	}

	public void setDataType(BuiltInDataType dataType) {
		this.dataType = dataType;
		if (autoUpdate) {
			recomputeStructure();
		}
	}

	/**
	 * Implements data property selection policy<br>
	 * Override when required
	 * 
	 * @param p
	 * @return
	 */
	protected boolean isDisplayableAsDataProperty(IFlexoOntologyStructuralProperty<?> p) {
		return p instanceof IFlexoOntologyDataProperty;
	}

	/**
	 * Implements object property selection policy<br>
	 * Override when required
	 * 
	 * @param p
	 * @return
	 */
	protected boolean isDisplayableAsObjectProperty(IFlexoOntologyStructuralProperty<?> p) {
		return p instanceof IFlexoOntologyObjectProperty;
	}

	/**
	 * Implements annotation property selection policy<br>
	 * Override when required
	 * 
	 * @param p
	 * @return
	 */
	protected boolean isDisplayableAsAnnotationProperty(IFlexoOntologyStructuralProperty<?> p) {
		return p.isAnnotationProperty();
	}

	/**
	 * Return boolean indicating if supplied object is visible in current browser, given current configuration
	 * 
	 * @param object
	 * @return
	 */
	public boolean isDisplayable(IFlexoOntologyObject<TA> object) {

		if (object instanceof IFlexoOntology) {
			return true;
		}

		boolean returned = false;

		if (object instanceof IFlexoOntologyClass) {
			if (getRootClass() != null && object instanceof IFlexoOntologyConcept) {
				returned = getRootClass().isSuperConceptOf((IFlexoOntologyConcept<TA>) object);
			}
			else {
				returned = true;
			}
		}

		if (object instanceof IFlexoOntologyIndividual && getShowIndividuals()) {
			if (getRootClass() != null && object instanceof IFlexoOntologyConcept) {
				returned = getRootClass().isSuperConceptOf((IFlexoOntologyConcept<TA>) object);
			}
			else {
				returned = true;
			}
		}
		if (object instanceof IFlexoOntologyStructuralProperty) {
			IFlexoOntologyStructuralProperty<?> p = (IFlexoOntologyStructuralProperty<?>) object;
			if (showObjectProperties && isDisplayableAsObjectProperty(p)) {
				returned = true;
			}
			if (showDataProperties && isDisplayableAsDataProperty(p)) {
				returned = true;
			}
			if (showAnnotationProperties && isDisplayableAsAnnotationProperty(p)) {
				returned = true;
			}
		}

		if (returned == false) {
			return false;
		}

		if (object instanceof IFlexoOntologyStructuralProperty && getRootClass() != null) {
			boolean foundAPreferredLocationAsSubClassOfRootClass = false;
			List<? extends IFlexoOntologyClass<TA>> preferredLocations = getPreferredStorageLocations(
					(IFlexoOntologyStructuralProperty<TA>) object, null);
			removeOriginalFromRedefinedObjects(preferredLocations);
			for (IFlexoOntologyClass<TA> pl : preferredLocations) {
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
				if (!p.getDomain().equals(getDomain()) && !(((IFlexoOntologyClass<TA>) p.getDomain()).isSuperClassOf(getDomain()))) {
					return false;
				}
			}
		}

		if (object instanceof IFlexoOntologyObjectProperty && getRange() != null) {
			IFlexoOntologyObjectProperty<TA> p = (IFlexoOntologyObjectProperty<TA>) object;
			if (p.getRange() instanceof IFlexoOntologyClass) {
				if (!((IFlexoOntologyClass<TA>) p.getRange()).isSuperClassOf(getRange())) {
					return false;
				}
			}
			else {
				return false;
			}
		}

		if (object instanceof IFlexoOntologyDataProperty && getDataType() != null) {
			IFlexoOntologyDataProperty<TA> p = (IFlexoOntologyDataProperty<TA>) object;
			if (p.getRange() != getDataType()) {
				return false;
			}
		}

		return true;
	}

	private void appendOntologyContents(IFlexoOntology<TA> o, IFlexoOntologyObject<TA> parent) {
		List<IFlexoOntologyStructuralProperty<TA>> properties = new ArrayListWithoutDuplicates();
		List<IFlexoOntologyIndividual<TA>> individuals = new ArrayListWithoutDuplicates();
		Hashtable<IFlexoOntologyStructuralProperty<TA>, List<? extends IFlexoOntologyClass<TA>>> storedProperties = new Hashtable<>();
		Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>> storedIndividuals = new Hashtable<>();
		List<IFlexoOntologyStructuralProperty<TA>> unstoredProperties = new ArrayListWithoutDuplicates();
		List<IFlexoOntologyIndividual<TA>> unstoredIndividuals = new ArrayListWithoutDuplicates();
		List<IFlexoOntologyClass<TA>> storageClasses = new ArrayListWithoutDuplicates();
		properties = retrieveDisplayableProperties(o);
		individuals = retrieveDisplayableIndividuals(o);

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : properties) {
				List<? extends IFlexoOntologyClass<TA>> preferredLocations = getPreferredStorageLocations(p, o);
				removeOriginalFromRedefinedObjects(preferredLocations);
				if (preferredLocations != null && preferredLocations.size() > 0) {
					storedProperties.put(p, preferredLocations);
					for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
						if (!storageClasses.contains(preferredLocation)) {
							storageClasses.add(preferredLocation);
						}
					}
				}
				else {
					unstoredProperties.add(p);
				}
			}
		}

		if (getShowIndividuals()) {
			for (IFlexoOntologyIndividual<TA> i : individuals) {
				IFlexoOntologyClass<TA> preferredLocation = getPreferredStorageLocation(i);
				if (preferredLocation != null) {
					storedIndividuals.put(i, preferredLocation);
					if (!storageClasses.contains(preferredLocation)) {
						storageClasses.add(preferredLocation);
					}
				}
				else {
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
		}
		else if (getDisplayPropertiesInClasses() || showIndividuals) {
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
			Map<IFlexoOntologyClass<TA>, List<IFlexoOntologyStructuralProperty<TA>>> propertiesForStorageClasses = new HashMap<>();
			for (IFlexoOntologyStructuralProperty<TA> p : storedProperties.keySet()) {
				List<? extends IFlexoOntologyClass<TA>> preferredLocations = storedProperties.get(p);
				for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
					List<IFlexoOntologyStructuralProperty<TA>> l = propertiesForStorageClasses.get(preferredLocation);
					if (l == null) {
						l = new ArrayListWithoutDuplicates();
						propertiesForStorageClasses.put(preferredLocation, l);
					}
					l.add(p);
				}
			}
			for (IFlexoOntologyClass<TA> storageClass : propertiesForStorageClasses.keySet()) {
				addPropertiesAsHierarchy(storageClass, propertiesForStorageClasses.get(storageClass));
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
		}
		else {
			roots = new ArrayListWithoutDuplicates();
		}
		if (structure != null) {
			structure.clear();
		}
		else {
			structure = new Hashtable<>();
		}

		if (getContext() == null) {
			return;
		}

		if (strictMode) {

			appendOntologyContents(getContext(), null);

		}
		else {
			// System.out.println("computeNonHierarchicalStructure()");
			// System.out.println("context=" + getContext());
			// System.out.println("imported ontologies: " + getContext().getImportedOntologies());
			// System.out.println("all imported ontologies: " + OntologyUtils.getAllImportedOntologies(getContext()));

			roots.add(getContext());
			appendOntologyContents(getContext(), getContext());
			for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
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
		}
		else {
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
		}
		else {
			for (IFlexoOntologyStructuralProperty<TA> sp : p.getSuperProperties()) {
				if (someProperties.contains(sp)) {
					return true;
				}
			}
			return false;
		}
	}

	@SuppressWarnings("serial")
	class ArrayListWithoutDuplicates<E> extends ArrayList<E> {

		@Override
		public boolean addAll(Collection<? extends E> collection) {
			for (E c : collection) {
				add(c);
			}
			return true;
		}

		@Override
		public boolean add(E c) {
			if (!contains(c)) {
				return super.add(c);
			}
			return false;
		}
	}

	private void addChildren(IFlexoOntologyObject<TA> parent, IFlexoOntologyObject<TA> child) {
		if (structure == null) {
			logger.warning("Unexpected null structure");
			return;
		}
		List<FlexoOntologyObjectImpl<TA>> v = structure.get(parent);
		if (v == null) {
			v = new ArrayListWithoutDuplicates();
			structure.put((FlexoOntologyObjectImpl<TA>) parent, v);
		}
		if (!v.contains(child)) {
			v.add((FlexoOntologyObjectImpl<TA>) child);
			removeOriginalFromRedefinedObjects(v);
		}
	}

	private void computeHierarchicalStructure() {

		logger.fine("computeHierarchicalStructure()");

		if (roots != null) {
			roots.clear();
		}
		else {
			roots = new ArrayListWithoutDuplicates();
		}
		if (structure != null) {
			structure.clear();
		}
		else {
			structure = new Hashtable<>();
		}

		List<IFlexoOntologyStructuralProperty<TA>> properties = new ArrayListWithoutDuplicates();
		Hashtable<IFlexoOntologyStructuralProperty<TA>, List<IFlexoOntologyClass<TA>>> storedProperties = new Hashtable<>();
		List<IFlexoOntologyStructuralProperty<TA>> unstoredProperties = new ArrayListWithoutDuplicates();
		List<IFlexoOntologyClass<TA>> storageClasses = new ArrayListWithoutDuplicates();

		List<IFlexoOntologyIndividual<TA>> individuals = new ArrayListWithoutDuplicates();
		Hashtable<IFlexoOntologyIndividual<TA>, IFlexoOntologyClass<TA>> storedIndividuals = new Hashtable<>();
		List<IFlexoOntologyIndividual<TA>> unstoredIndividuals = new ArrayListWithoutDuplicates();

		if (getContext() == null) {
			return;
		}

		properties = retrieveDisplayableProperties(getContext());
		individuals = retrieveDisplayableIndividuals(getContext());

		if (!strictMode) {
			for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
				properties.addAll(retrieveDisplayableProperties(o));
				individuals.addAll(retrieveDisplayableIndividuals(o));
			}
		}

		if (getDisplayPropertiesInClasses()) {
			for (IFlexoOntologyStructuralProperty<TA> p : properties) {
				List<IFlexoOntologyClass<TA>> preferredLocations = getPreferredStorageLocations(p, null);
				removeOriginalFromRedefinedObjects(preferredLocations);
				if (preferredLocations != null && preferredLocations.size() > 0) {
					if (storedProperties.get(p) != null) {
						List<IFlexoOntologyClass<TA>> existing = storedProperties.get(p);
						existing.addAll(preferredLocations);
					}
					else {
						storedProperties.put(p, preferredLocations);
					}
					for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
						if (!storageClasses.contains(preferredLocation)) {
							storageClasses.add(preferredLocation);
						}
					}
				}
				else {
					unstoredProperties.add(p);
				}
			}

		}

		if (getShowIndividuals()) {
			for (IFlexoOntologyIndividual<TA> i : individuals) {
				IFlexoOntologyClass<TA> preferredLocation = getPreferredStorageLocation(i);
				if (preferredLocation != null) {
					storedIndividuals.put(i, preferredLocation);
					if (!storageClasses.contains(preferredLocation)) {
						storageClasses.add(preferredLocation);
					}
				}
				else {
					unstoredIndividuals.add(i);
				}
			}
		}

		if (getShowClasses()) {
			List<IFlexoOntologyClass<TA>> classes = new ArrayListWithoutDuplicates<IFlexoOntologyClass<TA>>();
			if (strictMode) {
				classes = retrieveDisplayableClasses(getContext());
			}
			else {
				for (IFlexoOntology<TA> o : OntologyUtils.getAllImportedOntologies(getContext())) {
					classes.addAll(retrieveDisplayableClasses(o));
				}
				classes.addAll(getContext().getClasses());
			}
			if (getDisplayPropertiesInClasses() || getShowIndividuals()) {
				classes.addAll(storageClasses);
			}
			removeOriginalFromRedefinedObjects(classes);
			addClassesAsHierarchy(null, classes);
		}
		else if (getDisplayPropertiesInClasses() || getShowIndividuals()) {
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
			if (getShowClasses()) {
				addChildren(getContext().getRootConcept(), i);
			}
			else {
				roots.add(i);
			}
		}

		if (getDisplayPropertiesInClasses()) {
			Map<IFlexoOntologyClass<TA>, List<IFlexoOntologyStructuralProperty<TA>>> propertiesForStorageClasses = new HashMap<>();
			for (IFlexoOntologyStructuralProperty<TA> p : storedProperties.keySet()) {
				List<IFlexoOntologyClass<TA>> preferredLocations = storedProperties.get(p);
				for (IFlexoOntologyClass<TA> preferredLocation : preferredLocations) {
					List<IFlexoOntologyStructuralProperty<TA>> l = propertiesForStorageClasses.get(preferredLocation);
					if (l == null) {
						l = new ArrayListWithoutDuplicates();
						propertiesForStorageClasses.put(preferredLocation, l);
					}
					if (!l.contains(p)) {
						l.add(p);
					}
				}
			}
			for (IFlexoOntologyClass<TA> storageClass : propertiesForStorageClasses.keySet()) {
				addPropertiesAsHierarchy(storageClass, propertiesForStorageClasses.get(storageClass));
			}

			addPropertiesAsHierarchy(null, unstoredProperties);

		}
		else {
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
		List<IFlexoOntologyClass<TA>> potentialStorageClasses = new ArrayListWithoutDuplicates();

		// First we look if property has a defined domain
		if (p.getDomain() instanceof IFlexoOntologyClass) {
			// Return the most specialized definition
			IFlexoOntology<TA> ontology = searchedOntology != null ? searchedOntology : getContext();

			IFlexoOntologyClass<TA> c = ontology.getClass(((IFlexoOntologyClass<TA>) p.getDomain()).getURI());
			if (c == null) {
				c = (IFlexoOntologyClass<TA>) p.getDomain();
			}
			if (isDisplayable(c)) {
				if (c != null && (searchedOntology == null || c.getOntology() == searchedOntology)) {
					if (!potentialStorageClasses.contains(c)) {
						potentialStorageClasses.add(c);
					}
					return potentialStorageClasses;
				}
			}
			else {
				List<IFlexoOntologyClass<TA>> returned = new ArrayList<>(getFirstDisplayableParents(c));

				// Remove Thing references if list is non trivially the Thing singleton
				for (IFlexoOntologyClass<TA> c2 : new ArrayList<>(returned)) {
					if (c2 == null || (c2.isRootConcept() && returned.size() > 1)) {
						returned.remove(c2);
					}
				}

				return returned;
			}
		}

		return potentialStorageClasses;

	}

	protected List<? extends IFlexoOntologyClass<TA>> getFirstDisplayableParents(IFlexoOntologyClass<TA> c) {

		List<IFlexoOntologyClass<TA>> returned = new ArrayListWithoutDuplicates();
		for (IFlexoOntologyClass<TA> superClass : c.getSuperClasses()) {
			if (isDisplayable(superClass)) {
				if (!returned.contains(superClass)) {
					returned.add(superClass);
				}
			}
			else {
				returned.addAll(getFirstDisplayableParents(superClass));
			}
		}
		IFlexoOntology<TA> ontology = getContext();
		if (returned.size() == 0 && ontology != null && ontology.getRootConcept() != null) {
			// Thing is the only solution
			returned.add(ontology.getRootConcept());
		}

		return returned;
	}

	protected IFlexoOntologyClass<TA> getPreferredStorageLocation(IFlexoOntologyIndividual<TA> i) {

		// Return the first class which is not the Thing concept
		for (IFlexoOntologyClass<TA> c : i.getTypes()) {
			if (c.isNamedClass() && !c.isRootConcept()) {
				IFlexoOntologyClass<TA> returned = getContext().getClass(c.getURI());
				if (returned != null) {
					return returned;
				}
				else {
					return c;
				}
			}
		}
		return getContext().getRootConcept();
	}

	private void addClassesAsHierarchy(IFlexoOntologyObject<TA> parent, List<IFlexoOntologyClass<TA>> someClasses) {
		if (someClasses.contains(getContext().getRootConcept())) {
			appendClassInHierarchy(parent, getContext().getRootConcept(), someClasses);
		}
		else {
			List<IFlexoOntologyClass<TA>> listByExcludingRootClasses = new ArrayList<>(someClasses);
			List<IFlexoOntologyClass<TA>> localRootClasses = new ArrayListWithoutDuplicates();
			for (IFlexoOntologyClass<TA> c : someClasses) {
				if (!hasASuperClassDefinedInList(c, someClasses)) {
					localRootClasses.add(c);
					listByExcludingRootClasses.remove(c);
				}
			}

			for (IFlexoOntologyClass<TA> c : localRootClasses) {
				List<IFlexoOntologyClass<TA>> potentialChildren = new ArrayListWithoutDuplicates();
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

		List<IFlexoOntologyClass<TA>> listByExcludingCurrentClass = new ArrayList<>(someClasses);
		listByExcludingCurrentClass.remove(c);

		if (parent == null) {
			if (!roots.contains(c)) {
				roots.add(c);
			}
		}
		else {
			addChildren(parent, c);
		}
		if (listByExcludingCurrentClass.size() > 0) {
			addClassesAsHierarchy(c, listByExcludingCurrentClass);
		}
	}

	private boolean hasASuperClassDefinedInList(IFlexoOntologyClass<TA> c, List<IFlexoOntologyClass<TA>> someClasses) {
		if (c.getSuperClasses() == null) {
			return false;
		}
		else {
			for (IFlexoOntologyClass<TA> c2 : someClasses) {
				if (c2.isSuperConceptOf(c) /*&& c2 != c*/ && !c2.getURI().equals(c.getURI())) {
					return true;
				}
			}
			return false;
		}
	}

	/* Unused
	private List<IFlexoOntologyConcept<TA>> retrieveDisplayableObjects(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyConcept<TA>> returned = new ArrayListWithoutDuplicates();
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
	 */

	/**
	 * Remove originals from redefined classes<br>
	 * Special case: original Thing definition is kept and redefinitions are excluded
	 * 
	 * @param list
	 */
	protected void removeOriginalFromRedefinedObjects(List<? extends IFlexoOntologyObject<TA>> list) {
		for (IFlexoOntologyObject<TA> c : new ArrayList<>(list)) {
			if (c instanceof IFlexoOntologyClass && ((IFlexoOntologyClass<TA>) c).isRootConcept()
					&& ((IFlexoOntologyClass<TA>) c).getOntology() != getContext() && list.contains(getContext().getRootConcept())) {
				list.remove(c);
			}
		}
	}

	private void appendParentClassesToStorageClasses(List<IFlexoOntologyClass<TA>> someClasses) {
		// System.out.println("appendParentClassesToStorageClasses with " + someClasses);

		// First compute the list of all top-level classes
		List<IFlexoOntologyClass<TA>> topLevelClasses = new ArrayListWithoutDuplicates();
		for (IFlexoOntologyClass<TA> c : someClasses) {
			boolean requireAddInTopClasses = true;
			List<IFlexoOntologyClass<TA>> classesToRemove = new ArrayListWithoutDuplicates();
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

		List<IFlexoOntologyClass<TA>> classesToAdd = new ArrayListWithoutDuplicates();
		if (someClasses.size() > 1) {
			for (int i = 0; i < topLevelClasses.size(); i++) {
				for (int j = i + 1; j < topLevelClasses.size(); j++) {
					IFlexoOntologyClass<TA> c1 = topLevelClasses.get(i);
					IFlexoOntologyClass<TA> c2 = topLevelClasses.get(j);
					IFlexoOntologyClass<TA> ancestor = OntologyUtils.getFirstCommonAncestor(c1, c2);
					if (ancestor != null) {
						IFlexoOntologyClass<TA> ancestorSeenFromContextOntology = getContext().getClass(ancestor.getURI());
						if (ancestorSeenFromContextOntology != null) {
							if (!someClasses.contains(ancestorSeenFromContextOntology)
									&& !classesToAdd.contains(ancestorSeenFromContextOntology)) {
								classesToAdd.add(ancestorSeenFromContextOntology);
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

	private List<IFlexoOntologyClass<TA>> retrieveDisplayableClasses(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyClass<TA>> returned = new ArrayListWithoutDuplicates<>();
		for (IFlexoOntologyClass<TA> c : ontology.getClasses()) {
			if (isDisplayable(c)) {
				returned.add(c);
			}
		}
		removeOriginalFromRedefinedObjects(returned);
		return returned;
	}

	private List<IFlexoOntologyIndividual<TA>> retrieveDisplayableIndividuals(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyIndividual<TA>> returned = new ArrayListWithoutDuplicates();
		for (IFlexoOntologyIndividual<TA> c : ontology.getIndividuals()) {
			if (isDisplayable(c)) {
				returned.add(c);
			}
		}
		return returned;
	}

	private List<IFlexoOntologyStructuralProperty<TA>> retrieveDisplayableProperties(IFlexoOntology<TA> ontology) {
		ArrayList<IFlexoOntologyStructuralProperty<TA>> returned = new ArrayListWithoutDuplicates();

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

	public Font getFont(IFlexoOntologyObject<TA> object, Font baseFont) {
		if (object instanceof IFlexoOntologyConcept && baseFont != null
				&& ((IFlexoOntologyConcept<?>) object).getOntology() != getContext()) {
			return baseFont.deriveFont(Font.ITALIC);
		}
		return baseFont;
	}

}
