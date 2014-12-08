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
package org.openflexo.foundation.viewpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.viewpoint.action.CreateFlexoBehaviour;
import org.openflexo.foundation.viewpoint.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.viewpoint.editionaction.DeleteAction;
import org.openflexo.foundation.viewpoint.inspector.FlexoConceptInspector;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.StringUtils;

/**
 * An FlexoConcept aggregates modelling elements from different modelling element resources (models, metamodels, graphical representation,
 * GUI, etcâ¦). Each such element is associated with a {@link FlexoRole}.
 * 
 * A FlexoRole is an abstraction of the manipulation roles played in the {@link FlexoConcept} by modelling element potentially in different
 * metamodels.
 * 
 * An {@link FlexoConceptInstance} is an instance of an {@link FlexoConcept} .
 * 
 * Instances of modelling elements in an {@link FlexoConceptInstance} are called Pattern Actors. They play given Pattern Roles.
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoConcept.FlexoConceptImpl.class)
@XMLElement
public interface FlexoConcept extends FlexoConceptObject {

	@PropertyIdentifier(type = VirtualModel.class)
	public static final String VIRTUAL_MODEL_KEY = "virtualModel";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_BEHAVIOURS_KEY = "flexoBehaviours";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_ROLES_KEY = "flexoRoles";
	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String INSPECTOR_KEY = "inspector";
	@PropertyIdentifier(type = List.class)
	public static final String PARENT_FLEXO_CONCEPTS_KEY = "parentFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String CHILD_FLEXO_CONCEPTS_KEY = "childFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_CONCEPT_CONSTRAINTS_KEY = "flexoConceptConstraints";

	@Override
	@Getter(value = VIRTUAL_MODEL_KEY, inverse = VirtualModel.FLEXO_CONCEPTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public VirtualModel getVirtualModel();

	@Setter(VIRTUAL_MODEL_KEY)
	public void setVirtualModel(VirtualModel virtualModel);

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(value = FLEXO_BEHAVIOURS_KEY, cardinality = Cardinality.LIST, inverse = FlexoBehaviour.FLEXO_CONCEPT_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoBehaviour> getFlexoBehaviours();

	@Setter(FLEXO_BEHAVIOURS_KEY)
	public void setFlexoBehaviours(List<FlexoBehaviour> flexoBehaviours);

	@Adder(FLEXO_BEHAVIOURS_KEY)
	@PastingPoint
	public void addToFlexoBehaviours(FlexoBehaviour aFlexoBehaviour);

	@Remover(FLEXO_BEHAVIOURS_KEY)
	public void removeFromFlexoBehaviours(FlexoBehaviour aFlexoBehaviour);

	@Finder(collection = FLEXO_BEHAVIOURS_KEY, attribute = FlexoBehaviour.NAME_KEY)
	public FlexoBehaviour getFlexoBehaviour(String editionSchemeName);

	public FlexoBehaviour getFlexoBehaviourForURI(String uri);

	@Getter(value = FLEXO_ROLES_KEY, cardinality = Cardinality.LIST, inverse = FlexoRole.FLEXO_CONCEPT_KEY)
	@XMLElement
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoRole<?>> getFlexoRoles();

	@Setter(FLEXO_ROLES_KEY)
	public void setFlexoRoles(List<FlexoRole<?>> patternRoles);

	@Adder(FLEXO_ROLES_KEY)
	@PastingPoint
	public void addToFlexoRoles(FlexoRole<?> aPatternRole);

	@Remover(FLEXO_ROLES_KEY)
	public void removeFromFlexoRoles(FlexoRole<?> aPatternRole);

	/**
	 * Return declared roles for this {@link FlexoConcept}<br>
	 * Declared roles are those returned by getFlexoRoles() method
	 * 
	 * @return
	 */
	public List<FlexoRole<?>> getDeclaredRoles();

	/**
	 * Build and return all roles for this {@link FlexoConcept}<br>
	 * This returned {@link List} includes all declared roles for this FlexoConcept, augmented with all roles of parent {@link FlexoConcept}
	 * 
	 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
	 * 
	 * @return
	 */
	public List<FlexoRole<?>> getAllRoles();

	/**
	 * Return {@link FlexoRole} identified by supplied name
	 * 
	 * @param flexoRoleName
	 * @return
	 */
	@Finder(collection = FLEXO_ROLES_KEY, attribute = FlexoRole.NAME_KEY)
	public FlexoRole<?> getFlexoRole(String flexoRoleName);

	/**
	 * Build and return the list of {@link FlexoRole} with supplied type
	 * 
	 * @param type
	 * @return
	 */
	public <R> List<R> getFlexoRoles(Class<R> type);

	@Getter(value = INSPECTOR_KEY, inverse = FlexoConceptInspector.FLEXO_CONCEPT_KEY)
	@XMLElement(xmlTag = "Inspector")
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public FlexoConceptInspector getInspector();

	@Setter(INSPECTOR_KEY)
	public void setInspector(FlexoConceptInspector inspector);

	@Getter(value = PARENT_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = CHILD_FLEXO_CONCEPTS_KEY)
	@XMLElement(context = "Parent")
	public List<FlexoConcept> getParentFlexoConcepts();

	@Setter(PARENT_FLEXO_CONCEPTS_KEY)
	public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts);

	@Adder(PARENT_FLEXO_CONCEPTS_KEY)
	public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept);

	@Remover(PARENT_FLEXO_CONCEPTS_KEY)
	public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept);

	@Getter(value = CHILD_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = PARENT_FLEXO_CONCEPTS_KEY)
	// @XMLElement(context = "Child")
	public List<FlexoConcept> getChildFlexoConcepts();

	@Setter(CHILD_FLEXO_CONCEPTS_KEY)
	public void setChildFlexoConcepts(List<FlexoConcept> childFlexoConcepts);

	@Adder(CHILD_FLEXO_CONCEPTS_KEY)
	public void addToChildFlexoConcepts(FlexoConcept childFlexoConcept);

	@Remover(CHILD_FLEXO_CONCEPTS_KEY)
	public void removeFromChildFlexoConcepts(FlexoConcept childFlexoConcept);

	@Getter(value = FLEXO_CONCEPT_CONSTRAINTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoConceptConstraint.FLEXO_CONCEPT_KEY)
	@XMLElement
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoConceptConstraint> getFlexoConceptConstraints();

	@Setter(FLEXO_CONCEPT_CONSTRAINTS_KEY)
	public void setFlexoConceptConstraints(List<FlexoConceptConstraint> flexoConceptConstraints);

	@Adder(FLEXO_CONCEPT_CONSTRAINTS_KEY)
	@PastingPoint
	public void addToFlexoConceptConstraints(FlexoConceptConstraint aFlexoConceptConstraint);

	@Remover(FLEXO_CONCEPT_CONSTRAINTS_KEY)
	public void removeFromFlexoConceptConstraints(FlexoConceptConstraint aFlexoConceptConstraint);

	@DeserializationFinalizer
	public void finalizeFlexoConceptDeserialization();

	// public void updateBindingModel();

	public boolean isRoot();

	public <ES extends FlexoBehaviour> List<ES> getFlexoBehaviours(Class<ES> editionSchemeClass);

	public List<AbstractActionScheme> getAbstractActionSchemes();

	public List<ActionScheme> getActionSchemes();

	/**
	 * Only one synchronization scheme is allowed
	 * 
	 * @return
	 */
	public SynchronizationScheme getSynchronizationScheme();

	public List<DeletionScheme> getDeletionSchemes();

	public List<NavigationScheme> getNavigationSchemes();

	public List<AbstractCreationScheme> getAbstractCreationSchemes();

	public List<CreationScheme> getCreationSchemes();

	public boolean hasActionScheme();

	public boolean hasCreationScheme();

	public boolean hasDeletionScheme();

	public boolean hasSynchronizationScheme();

	public boolean hasNavigationScheme();

	public DeletionScheme getDefaultDeletionScheme();

	public DeletionScheme generateDefaultDeletionScheme();

	public List<IndividualRole> getIndividualRoles();

	public List<ClassRole> getClassRoles();

	public FlexoConceptInstanceType getInstanceType();

	public FlexoConceptStructuralFacet getStructuralFacet();

	public FlexoConceptBehaviouralFacet getBehaviouralFacet();

	public boolean isAssignableFrom(FlexoConcept flexoConcept);

	public boolean isSuperConceptOf(FlexoConcept flexoConcept);

	public String getAvailableRoleName(String baseName);

	public String getAvailableEditionSchemeName(String baseName);

	public boolean hasNature(FlexoConceptNature nature);

	@Override
	public FlexoConceptBindingModel getBindingModel();

	/**
	 * Return declared parent FlexoConcept for this {@link FlexoConcept}<br>
	 * Declared parent FlexoConcept are those returned by getParentFlexoConcepts() method
	 * 
	 * @return
	 */
	public List<FlexoConcept> getDeclaredParentFlexoConcepts();

	/**
	 * Build and return all parent FlexoConcept for this {@link FlexoConcept}<br>
	 * This returned {@link List} includes all declared parent concepts for this FlexoConcept, augmented with all parents of parent
	 * {@link FlexoConcept} (recursive)
	 * 
	 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllParentFlexoConcepts();

	public static abstract class FlexoConceptImpl extends FlexoConceptObjectImpl implements FlexoConcept {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoConcept.class.getPackage().getName());

		// private List<FlexoRole<?>> patternRoles;
		// private List<FlexoBehaviour> editionSchemes;
		// private List<FlexoConceptConstraint> flexoConceptConstraints;
		private FlexoConceptInspector inspector;

		// private OntologicObjectRole primaryConceptRole;
		// private GraphicalElementPatternRole primaryRepresentationRole;

		private VirtualModel virtualModel;

		private final FlexoConcept parentFlexoConcept = null;
		// private final Vector<FlexoConcept> childFlexoConcepts = new
		// Vector<FlexoConcept>();

		private FlexoConceptStructuralFacet structuralFacet;
		private FlexoConceptBehaviouralFacet behaviouralFacet;

		private final FlexoConceptInstanceType instanceType = new FlexoConceptInstanceType(this);

		/**
		 * Stores a chained collections of objects which are involved in validation
		 */
		private final ChainedCollection<ViewPointObject> validableObjects = null;

		private FlexoConceptBindingModel bindingModel;

		@Override
		public final boolean hasNature(FlexoConceptNature nature) {
			return nature.hasNature(this);
		}

		@Override
		public FlexoConceptInstanceType getInstanceType() {
			return instanceType;
		}

		@Override
		public FlexoConceptStructuralFacet getStructuralFacet() {
			if (structuralFacet == null && getVirtualModelFactory() != null) {
				structuralFacet = getVirtualModelFactory().newFlexoConceptStructuralFacet(this);
			}
			return structuralFacet;
		}

		@Override
		public FlexoConceptBehaviouralFacet getBehaviouralFacet() {
			if (behaviouralFacet == null && getVirtualModelFactory() != null) {
				behaviouralFacet = getVirtualModelFactory().newFlexoConceptBehaviouralFacet(this);
			}
			return behaviouralFacet;
		}

		@Override
		public FlexoConceptImpl getFlexoConcept() {
			return this;
		}

		@Override
		public boolean delete() {
			if (bindingModel != null) {
				bindingModel.delete();
			}
			if (getVirtualModel() != null) {
				getVirtualModel().removeFromFlexoConcepts(this);
			}
			super.delete();
			deleteObservers();
			return true;
		}

		@Override
		public String getStringRepresentation() {
			return (getVirtualModel() != null ? getVirtualModel().getStringRepresentation() : "null") + "#" + getName();
		}

		/**
		 * Return the URI of the {@link FlexoConcept}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name >#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getVirtualModel() != null) {
				return getVirtualModel().getURI() + "#" + getName();
			} else {
				return "null#" + getName();
			}
		}

		@Override
		public void setName(String name) {
			if (name != null) {
				// We prevent ',' so that we can use it as a delimiter in tags.
				super.setName(name.replace(",", ""));
			}
		}

		/**
		 * Return declared roles for this {@link FlexoConcept}<br>
		 * Declared roles are those returned by getFlexoRoles() method
		 * 
		 * @return
		 */
		@Override
		public List<FlexoRole<?>> getDeclaredRoles() {
			return getFlexoRoles();
		}

		/**
		 * Build and return all roles for this {@link FlexoConcept}<br>
		 * This returned {@link List} includes all declared roles for this FlexoConcept, augmented with all roles of parent
		 * {@link FlexoConcept}
		 * 
		 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoRole<?>> getAllRoles() {
			if (getParentFlexoConcepts().size() == 0) {
				return getDeclaredRoles();
			}

			List<FlexoRole<?>> returned = new ArrayList<FlexoRole<?>>();
			returned.addAll(getDeclaredRoles());
			for (FlexoConcept concept : getParentFlexoConcepts()) {
				returned.addAll(concept.getAllRoles());
			}

			return returned;
		}

		@Override
		public void setFlexoRoles(List<FlexoRole<?>> somePatternRole) {
			// patternRoles = somePatternRole;
			performSuperSetter(FLEXO_ROLES_KEY, somePatternRole);
			availablePatternRoleNames = null;
		}

		@Override
		public void addToFlexoRoles(FlexoRole<?> aPatternRole) {
			availablePatternRoleNames = null;
			performSuperAdder(FLEXO_ROLES_KEY, aPatternRole);
			/*if (_bindingModel != null) {
				updateBindingModel();
			}*/
		}

		@Override
		public void removeFromFlexoRoles(FlexoRole aFlexoRole) {
			availablePatternRoleNames = null;
			performSuperRemover(FLEXO_ROLES_KEY, aFlexoRole);
			/*if (_bindingModel != null) {
				updateBindingModel();
			}*/
		}

		@Override
		public <R> List<R> getFlexoRoles(Class<R> type) {
			List<R> returned = new ArrayList<R>();
			for (FlexoRole<?> r : getFlexoRoles()) {
				if (TypeUtils.isTypeAssignableFrom(type, r.getClass())) {
					returned.add((R) r);
				}
			}
			return returned;
		}

		@Override
		public List<IndividualRole> getIndividualRoles() {
			return getFlexoRoles(IndividualRole.class);
		}

		@Override
		public List<ClassRole> getClassRoles() {
			return getFlexoRoles(ClassRole.class);
		}

		/*
		 * public List<GraphicalElementPatternRole>
		 * getGraphicalElementPatternRoles() { return
		 * getPatternRoles(GraphicalElementPatternRole.class); }
		 * 
		 * public List<ShapePatternRole> getShapePatternRoles() { return
		 * getPatternRoles(ShapePatternRole.class); }
		 * 
		 * public List<ConnectorPatternRole> getConnectorPatternRoles() { return
		 * getPatternRoles(ConnectorPatternRole.class); }
		 */

		/*
		 * public ShapePatternRole getDefaultShapePatternRole() {
		 * List<ShapePatternRole> l = getShapePatternRoles(); if (l.size() > 0)
		 * { return l.get(0); } return null; }
		 * 
		 * public ConnectorPatternRole getDefaultConnectorPatternRole() {
		 * List<ConnectorPatternRole> l = getConnectorPatternRoles(); if
		 * (l.size() > 0) { return l.get(0); } return null; }
		 */

		private Vector<String> availablePatternRoleNames = null;

		public Vector<String> getAvailablePatternRoleNames() {
			if (availablePatternRoleNames == null) {
				availablePatternRoleNames = new Vector<String>();
				for (FlexoRole r : getFlexoRoles()) {
					availablePatternRoleNames.add(r.getName());
				}
			}
			return availablePatternRoleNames;
		}

		@Override
		public String getAvailableRoleName(String baseName) {
			String testName = baseName;
			int index = 2;
			while (getFlexoRole(testName) != null) {
				testName = baseName + index;
				index++;
			}
			return testName;
		}

		@Override
		public String getAvailableEditionSchemeName(String baseName) {
			String testName = baseName;
			int index = 2;
			while (getFlexoBehaviour(testName) != null) {
				testName = baseName + index;
				index++;
			}
			return testName;
		}

		@Override
		public FlexoBehaviour getFlexoBehaviourForURI(String uri) {

			if (uri != null && !uri.isEmpty() && getVirtualModel() != null) {
				if (uri.contains(getVirtualModel().getURI())) {
					String behaviourname = uri.replace(getVirtualModel().getURI(), "").substring(1);
					System.out.println("XTOF :: je récupère " + behaviourname);
					return getFlexoBehaviour(behaviourname);
				} else {
					logger.warning("Trying to retrieve a FlexoBehaviour (" + uri + ") that does not belong to current Concept " + getURI());
					return null;
				}
			}

			return null;

		}

		@Override
		@SuppressWarnings("unchecked")
		public <ES extends FlexoBehaviour> List<ES> getFlexoBehaviours(Class<ES> editionSchemeClass) {
			List<ES> returned = new ArrayList<ES>();
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (editionSchemeClass.isAssignableFrom(es.getClass())) {
					returned.add((ES) es);
				}
			}
			return returned;
		}

		@Override
		public List<AbstractActionScheme> getAbstractActionSchemes() {
			return getFlexoBehaviours(AbstractActionScheme.class);
		}

		@Override
		public List<ActionScheme> getActionSchemes() {
			return getFlexoBehaviours(ActionScheme.class);
		}

		/**
		 * Only one synchronization scheme is allowed
		 * 
		 * @return
		 */
		@Override
		public SynchronizationScheme getSynchronizationScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof SynchronizationScheme) {
					return (SynchronizationScheme) es;
				}
			}
			return null;
		}

		@Override
		public List<DeletionScheme> getDeletionSchemes() {
			return getFlexoBehaviours(DeletionScheme.class);
		}

		@Override
		public List<NavigationScheme> getNavigationSchemes() {
			return getFlexoBehaviours(NavigationScheme.class);
		}

		@Override
		public List<AbstractCreationScheme> getAbstractCreationSchemes() {
			return getFlexoBehaviours(AbstractCreationScheme.class);
		}

		@Override
		public List<CreationScheme> getCreationSchemes() {
			return getFlexoBehaviours(CreationScheme.class);
		}

		@Override
		public boolean hasActionScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof ActionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasCreationScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof CreationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasDeletionScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof DeletionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasSynchronizationScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof SynchronizationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasNavigationScheme() {
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (es instanceof NavigationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public DeletionScheme getDefaultDeletionScheme() {
			if (getDeletionSchemes().size() > 0) {
				return getDeletionSchemes().get(0);
			}
			return null;
		}

		@Override
		public DeletionScheme generateDefaultDeletionScheme() {
			DeletionScheme newDeletionScheme = getVirtualModelFactory().newDeletionScheme();
			newDeletionScheme.setName("deletion");
			Vector<FlexoRole> rolesToDelete = new Vector<FlexoRole>();
			for (FlexoRole pr : getFlexoRoles()) {
				if (pr.defaultBehaviourIsToBeDeleted()) {
					rolesToDelete.add(pr);
				}
			}
			Collections.sort(rolesToDelete, new Comparator<FlexoRole>() {
				@Override
				public int compare(FlexoRole o1, FlexoRole o2) {
					/*
					 * if (o1 instanceof ShapePatternRole && o2 instanceof
					 * ConnectorPatternRole) { return 1; } else if (o1
					 * instanceof ConnectorPatternRole && o2 instanceof
					 * ShapePatternRole) { return -1; }
					 */

					/*
					 * if (o1 instanceof ShapePatternRole) { if (o2 instanceof
					 * ShapePatternRole) { if (((ShapePatternRole)
					 * o1).isEmbeddedIn((ShapePatternRole) o2)) { return -1; }
					 * if (((ShapePatternRole)
					 * o2).isEmbeddedIn((ShapePatternRole) o1)) { return 1; }
					 * return 0; } }
					 */
					return 0;
				}

			});
			for (FlexoRole pr : rolesToDelete) {
				DeleteAction a = getVirtualModelFactory().newDeleteAction();
				a.setObject(new DataBinding<Object>(pr.getRoleName()));
				newDeletionScheme.addToActions(a);
			}
			addToFlexoBehaviours(newDeletionScheme);
			return newDeletionScheme;
		}

		@Override
		public FlexoConceptInspector getInspector() {
			if (inspector == null && getVirtualModelFactory() != null) {
				inspector = getVirtualModelFactory().newFlexoConceptInspector(this);
				inspector.setInspectorTitle(getName());
			}
			return inspector;
		}

		@Override
		public void setInspector(FlexoConceptInspector inspector) {
			if (inspector != null) {
				inspector.setFlexoConcept(this);
			}
			this.inspector = inspector;
		}

		@Override
		public VirtualModel getVirtualModel() {
			return virtualModel;
		}

		@Override
		public void setVirtualModel(VirtualModel virtualModel) {
			if (this.virtualModel != virtualModel) {
				VirtualModel oldVirtualModel = this.virtualModel;
				this.virtualModel = virtualModel;
				getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODEL_KEY, oldVirtualModel, virtualModel);
			}
		}

		@Override
		public String toString() {
			return "FlexoConcept:" + getName();
		}

		@Override
		public void finalizeFlexoConceptDeserialization() {
			// createBindingModel();
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				es.finalizeEditionSchemeDeserialization();
			}
			for (FlexoRole pr : getFlexoRoles()) {
				pr.finalizeFlexoRoleDeserialization();
			}
			// updateBindingModel();
		}

		/*
		 * public void finalizeParentFlexoConceptDeserialization() { if
		 * (StringUtils.isNotEmpty(parentFlexoConceptURI)) {
		 * setParentFlexoConcept(getParentFlexoConcept()); } }
		 */

		public void debug() {
			System.out.println(getStringRepresentation());
		}

		@Deprecated
		public void save() {
			try {
				getVirtualModel().getResource().save(null);
			} catch (SaveResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public FlexoConceptBindingModel getBindingModel() {
			if (bindingModel == null) {
				// createBindingModel();
				bindingModel = new FlexoConceptBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public boolean isRoot() {
			return getParentFlexoConcepts().size() == 0;
		}

		@Override
		public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) {
			performSuperSetter(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcepts);
		}

		@Override
		public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) {
			if (!isSuperConceptOf(parentFlexoConcept)) {
				performSuperAdder(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcept);
			} else {
				logger.warning("Could not add as parent FlexoConcept: " + parentFlexoConcept);
			}
		}

		@Override
		public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept) {
			performSuperRemover(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcept);
		}

		/**
		 * Return declared parent FlexoConcept for this {@link FlexoConcept}<br>
		 * Declared parent FlexoConcept are those returned by getParentFlexoConcepts() method
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getDeclaredParentFlexoConcepts() {
			return getParentFlexoConcepts();
		}

		/**
		 * Build and return all parent FlexoConcept for this {@link FlexoConcept}<br>
		 * This returned {@link List} includes all declared parent concepts for this FlexoConcept, augmented with all parents of parent
		 * {@link FlexoConcept}
		 * 
		 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllParentFlexoConcepts() {
			if (getParentFlexoConcepts().size() == 0) {
				return getDeclaredParentFlexoConcepts();
			}

			List<FlexoConcept> returned = new ArrayList<FlexoConcept>();
			returned.addAll(getDeclaredParentFlexoConcepts());
			for (FlexoConcept concept : getParentFlexoConcepts()) {
				returned.addAll(concept.getAllParentFlexoConcepts());
			}

			return returned;
		}

		@Override
		public boolean isSuperConceptOf(FlexoConcept flexoConcept) {
			return isAssignableFrom(flexoConcept);
		}

		@Override
		public boolean isAssignableFrom(FlexoConcept flexoConcept) {
			if (flexoConcept == this) {
				return true;
			}
			for (FlexoConcept parent : flexoConcept.getParentFlexoConcepts()) {
				if (isAssignableFrom(parent)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			// Voir du cote de GeneratorFormatter pour formatter tout ca

			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoConcept " + getName(), context);
			if (getParentFlexoConcepts().size() > 0) {
				out.append(" extends ", context);
				for (FlexoConcept parent : getParentFlexoConcepts()) {
					out.append(parent.getName() + ",", context);
				}

			}
			out.append(" {" + StringUtils.LINE_SEPARATOR, context);

			if (getFlexoRoles().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoRole pr : getFlexoRoles()) {
					out.append(pr.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			if (getFlexoBehaviours().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoBehaviour es : getFlexoBehaviours()) {
					out.append(es.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			out.append("}" + StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}

	}

	@DefineValidationRule
	public static class FlexoConceptShouldHaveRoles extends ValidationRule<FlexoConceptShouldHaveRoles, FlexoConcept> {
		public FlexoConceptShouldHaveRoles() {
			super(FlexoConcept.class, "flexo_concept_should_have_roles");
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveRoles, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!(flexoConcept instanceof VirtualModel) && flexoConcept.getFlexoRoles().size() == 0) {
				return new ValidationWarning<FlexoConceptShouldHaveRoles, FlexoConcept>(this, flexoConcept,
						"flexo_concept_role_has_no_role");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class FlexoConceptShouldHaveFlexoBehaviours extends ValidationRule<FlexoConceptShouldHaveFlexoBehaviours, FlexoConcept> {
		public FlexoConceptShouldHaveFlexoBehaviours() {
			super(FlexoConcept.class, "flexo_concept_should_have_edition_scheme");
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveFlexoBehaviours, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (flexoConcept.getFlexoBehaviours().size() == 0) {
				return new ValidationWarning<FlexoConceptShouldHaveFlexoBehaviours, FlexoConcept>(this, flexoConcept,
						"flexo_concept_has_no_edition_scheme");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class FlexoConceptShouldHaveDeletionScheme extends ValidationRule<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> {
		public FlexoConceptShouldHaveDeletionScheme() {
			super(FlexoConcept.class, "flexo_concept_should_have_deletion_scheme");
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (flexoConcept.getDeletionSchemes().size() == 0) {
				CreateDefaultDeletionScheme fixProposal = new CreateDefaultDeletionScheme(flexoConcept);
				return new ValidationWarning<FlexoConceptShouldHaveDeletionScheme, FlexoConcept>(this, flexoConcept,
						"flexo_concept_has_no_deletion_scheme", fixProposal);
			}
			return null;
		}

		protected static class CreateDefaultDeletionScheme extends FixProposal<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> {

			private final FlexoConcept flexoConcept;
			private DeletionScheme newDefaultDeletionScheme;

			public CreateDefaultDeletionScheme(FlexoConcept anFlexoConcept) {
				super("create_default_deletion_scheme");
				this.flexoConcept = anFlexoConcept;
			}

			public FlexoConcept getFlexoConcept() {
				return flexoConcept;
			}

			public DeletionScheme getDeletionScheme() {
				return newDefaultDeletionScheme;
			}

			@Override
			protected void fixAction() {
				CreateFlexoBehaviour action = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null);
				action.setFlexoBehaviourClass(DeletionScheme.class);
				action.doAction();
				// newDefaultDeletionScheme = flexoConcept.createDeletionScheme();
				// AddIndividual action = getObject();
				// action.setAssignation(new
				// ViewPointDataBinding(patternRole.getPatternRoleName()));
			}

		}

	}

}
