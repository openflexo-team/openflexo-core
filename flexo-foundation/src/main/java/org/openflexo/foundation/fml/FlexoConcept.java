/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConceptBehaviouralFacet.FlexoConceptBehaviouralFacetImpl;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet.FlexoConceptStructuralFacetImpl;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
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
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
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
public interface FlexoConcept extends FlexoConceptObject, VirtualModelObject {

	@PropertyIdentifier(type = AbstractVirtualModel.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_BEHAVIOURS_KEY = "flexoBehaviours";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_PROPERTIES_KEY = "flexoProperties";
	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String INSPECTOR_KEY = "inspector";
	@PropertyIdentifier(type = List.class)
	public static final String PARENT_FLEXO_CONCEPTS_KEY = "parentFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String CHILD_FLEXO_CONCEPTS_KEY = "childFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_CONCEPT_CONSTRAINTS_KEY = "flexoConceptConstraints";
	@PropertyIdentifier(type = Boolean.class)
	public static final String IS_ABSTRACT_KEY = "isAbstract";

	// TODO: (SGU) i think we have to remove inverse property here
	@Getter(value = OWNER_KEY, inverse = VirtualModel.FLEXO_CONCEPTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public AbstractVirtualModel<?> getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(AbstractVirtualModel<?> virtualModel);

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

	@Getter(value = FLEXO_PROPERTIES_KEY, cardinality = Cardinality.LIST, inverse = FlexoProperty.FLEXO_CONCEPT_KEY)
	@XMLElement
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoProperty<?>> getFlexoProperties();

	@Setter(FLEXO_PROPERTIES_KEY)
	public void setFlexoProperties(List<FlexoProperty<?>> properties);

	@Adder(FLEXO_PROPERTIES_KEY)
	@PastingPoint
	public void addToFlexoProperties(FlexoProperty<?> aProperty);

	@Remover(FLEXO_PROPERTIES_KEY)
	public void removeFromFlexoProperties(FlexoProperty<?> aProperty);

	@Getter(value = IS_ABSTRACT_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean isAbstract();

	@Setter(IS_ABSTRACT_KEY)
	public void setAbstract(boolean isAbstract);

	/**
	 * Return boolean indicating if this FlexoConcept MUST be abstract (containing an {@link AbstractProperty})
	 * 
	 * @return
	 */
	public boolean abstractRequired();

	/**
	 * Return declared properties for this {@link FlexoConcept}<br>
	 * Declared properties are those returned by getFlexoProperties() method
	 * 
	 * @return
	 */
	public List<FlexoProperty<?>> getDeclaredProperties();

	/**
	 * Build and return all end-properties for this {@link FlexoConcept}<br>
	 * This returned {@link List} includes all declared properties for this FlexoConcept, augmented with all properties of parent
	 * {@link FlexoConcept} which are not parent properties of this concept declared properties.<br>
	 * This means that only leaf nodes of inheritance graph infered by this {@link FlexoConcept} hierarchy will be returned.
	 * 
	 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
	 * 
	 * @return
	 */
	public List<FlexoProperty<?>> getAccessibleProperties();

	/**
	 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all accessible properties<br>
	 * Note that returned property is not necessary one of declared property, but might be inherited.
	 * 
	 * @param propertyName
	 * @return
	 * @see #getAccessibleProperties()
	 */
	public FlexoProperty<?> getAccessibleProperty(String propertyName);

	/**
	 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all declared properties<br>
	 * 
	 * @param propertyName
	 * @return
	 * @see #getDeclaredProperties()
	 */
	public FlexoProperty<?> getDeclaredProperty(String propertyName);

	/**
	 * Build and return the list of all declared {@link FlexoProperty} with supplied type
	 * 
	 * @param type
	 * @return
	 */
	public <R> List<R> getDeclaredProperties(Class<R> type);

	/**
	 * Build and return the list of all declared {@link FlexoProperty} with supplied type
	 * 
	 * @param type
	 * @return
	 */
	public <R> List<R> getAccessibleProperties(Class<R> type);

	/**
	 * Build and return the list of all accessible roles from this {@link FlexoConcept}
	 * 
	 * @return
	 */
	public List<FlexoRole> getAccessibleRoles();

	/**
	 * Build and return the list of all declared roles from this {@link FlexoConcept}
	 * 
	 * @return
	 */
	public List<FlexoRole> getDeclaredRoles();

	/**
	 * Return {@link FlexoRole} identified by supplied name, which is to be retrieved in all accessible properties<br>
	 * Note that returned role is not necessary one of declared role, but might be inherited.
	 * 
	 * @param propertyName
	 * @return
	 * @see #getAccessibleRoles()
	 */
	public FlexoRole<?> getAccessibleRole(String roleName);

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
	public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) throws InconsistentFlexoConceptHierarchyException;

	@Adder(PARENT_FLEXO_CONCEPTS_KEY)
	public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) throws InconsistentFlexoConceptHierarchyException;

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

	public FlexoConceptInstanceType getInstanceType();

	public FlexoConceptStructuralFacet getStructuralFacet();

	public FlexoConceptBehaviouralFacet getBehaviouralFacet();

	public boolean isAssignableFrom(FlexoConcept flexoConcept);

	public boolean isSuperConceptOf(FlexoConcept flexoConcept);

	public String getAvailablePropertyName(String baseName);

	public String getAvailableFlexoBehaviourName(String baseName);

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

		private FlexoConceptInspector inspector;

		private FlexoConceptStructuralFacet structuralFacet;
		private FlexoConceptBehaviouralFacet behaviouralFacet;

		private final FlexoConceptInstanceType instanceType = new FlexoConceptInstanceType(this);

		private FlexoConceptBindingModel bindingModel;

		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			return getOwner();
		}

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
			FMLModelFactory factory = getFMLModelFactory();
			if (structuralFacet == null && factory != null) {
				CompoundEdit ce = null;
				if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					ce = factory.getEditingContext().getUndoManager().startRecording("CREATE_STRUCTURAL_FACET");
				}
				structuralFacet = factory.newFlexoConceptStructuralFacet(this);
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
			}
			return structuralFacet;
		}

		@Override
		public FlexoConceptBehaviouralFacet getBehaviouralFacet() {
			FMLModelFactory factory = getFMLModelFactory();
			if (behaviouralFacet == null && factory != null) {
				CompoundEdit ce = null;
				if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					ce = factory.getEditingContext().getUndoManager().startRecording("CREATE_BEHAVIOURAL_FACET");
				}
				behaviouralFacet = factory.newFlexoConceptBehaviouralFacet(this);
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
			}
			return behaviouralFacet;
		}

		@Override
		public FlexoConceptImpl getFlexoConcept() {
			return this;
		}

		@Override
		public boolean delete(Object... context) {
			Map<FlexoConcept, List<FlexoConcept>> oldParents = new HashMap<FlexoConcept, List<FlexoConcept>>();
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				oldParents.put(parentConcept, new ArrayList<FlexoConcept>(parentConcept.getChildFlexoConcepts()));
			}
			if (bindingModel != null) {
				bindingModel.delete();
			}
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().removeFromFlexoConcepts(this);
			}

			performSuperDelete(context);

			for (FlexoConcept parentConcept : oldParents.keySet()) {
				// Notify child changes
				parentConcept.getPropertyChangeSupport().firePropertyChange("childFlexoConcepts", oldParents.get(parentConcept),
						parentConcept.getChildFlexoConcepts());
			}
			deleteObservers();
			return true;
		}

		@Override
		public String getStringRepresentation() {
			return (getOwningVirtualModel() != null ? getOwningVirtualModel().getStringRepresentation() : "null") + "#" + getName();
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
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getURI() + "#" + getName();
			}
			else {
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

		@Override
		public boolean isAbstract() {
			if (abstractRequired()) {
				return true;
			}
			return (Boolean) performSuperGetter(IS_ABSTRACT_KEY);
		}

		@Override
		public boolean abstractRequired() {
			for (FlexoProperty<?> p : getAccessibleProperties()) {
				if (p.isAbstract()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Return declared properties for this {@link FlexoConcept}<br>
		 * Declared properties are those returned by getFlexoProperties() method
		 * 
		 * @return
		 */
		@Override
		public List<FlexoProperty<?>> getDeclaredProperties() {
			return getFlexoProperties();
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
		public List<FlexoProperty<?>> getAccessibleProperties() {
			if (getParentFlexoConcepts().size() == 0) {
				return getDeclaredProperties();
			}

			List<FlexoProperty<?>> returned = new ArrayList<FlexoProperty<?>>();
			List<FlexoProperty<?>> inheritedProperties = new ArrayList<FlexoProperty<?>>();
			returned.addAll(getDeclaredProperties());
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				for (FlexoProperty<?> p : parentConcept.getAccessibleProperties()) {
					if (getDeclaredProperty(p.getPropertyName()) == null) {
						// This property is inherited but not overriden
						// We check that we don't have this property yet
						if (!inheritedProperties.contains(p)) {
							inheritedProperties.add(p);
						}
					}
				}
			}
			// Now, we have to suppress all extra references
			List<FlexoProperty<?>> unnecessaryProperty = new ArrayList<FlexoProperty<?>>();
			for (FlexoProperty<?> p : inheritedProperties) {
				for (FlexoProperty<?> superP : p.getAllSuperProperties()) {
					if (inheritedProperties.contains(superP)) {
						unnecessaryProperty.add(superP);
					}
				}
			}

			for (FlexoProperty<?> removeThis : unnecessaryProperty) {
				inheritedProperties.remove(removeThis);
			}

			returned.addAll(inheritedProperties);
			return returned;
		}

		@Override
		public void setFlexoProperties(List<FlexoProperty<?>> someProperties) {
			// patternRoles = somePatternRole;
			performSuperSetter(FLEXO_PROPERTIES_KEY, someProperties);
			availablePropertiesNames = null;
		}

		@Override
		public void addToFlexoProperties(FlexoProperty<?> aProperty) {
			availablePropertiesNames = null;
			performSuperAdder(FLEXO_PROPERTIES_KEY, aProperty);
			notifiedPropertiesChanged(null, aProperty);
		}

		@Override
		public void removeFromFlexoProperties(FlexoProperty<?> aProperty) {
			availablePropertiesNames = null;
			performSuperRemover(FLEXO_PROPERTIES_KEY, aProperty);
			notifiedPropertiesChanged(aProperty, null);
		}

		protected void notifiedPropertiesChanged(FlexoProperty<?> oldValue, FlexoProperty<?> newValue) {
			if (getStructuralFacet() instanceof FlexoConceptStructuralFacetImpl) {
				((FlexoConceptStructuralFacetImpl) getStructuralFacet()).notifiedPropertiesChanged(oldValue, newValue);
			}
			getPropertyChangeSupport().firePropertyChange("isAbstract", !isAbstract(), isAbstract());
			getPropertyChangeSupport().firePropertyChange("abstractRequired", !abstractRequired(), abstractRequired());
		}

		@Override
		public <R> List<R> getDeclaredProperties(Class<R> type) {
			List<R> returned = new ArrayList<R>();
			for (FlexoProperty<?> r : getDeclaredProperties()) {
				if (TypeUtils.isTypeAssignableFrom(type, r.getClass())) {
					returned.add((R) r);
				}
			}
			return returned;
		}

		@Override
		public <R> List<R> getAccessibleProperties(Class<R> type) {
			List<R> returned = new ArrayList<R>();
			for (FlexoProperty<?> r : getAccessibleProperties()) {
				if (TypeUtils.isTypeAssignableFrom(type, r.getClass())) {
					returned.add((R) r);
				}
			}
			return returned;
		}

		/**
		 * Build and return the list of all accessible roles from this {@link FlexoConcept}
		 * 
		 * @return
		 */
		@Override
		public List<FlexoRole> getAccessibleRoles() {
			return getAccessibleProperties(FlexoRole.class);
		}

		/**
		 * Build and return the list of all declared roles from this {@link FlexoConcept}
		 * 
		 * @return
		 */
		@Override
		public List<FlexoRole> getDeclaredRoles() {
			return getDeclaredProperties(FlexoRole.class);
		}

		/**
		 * Return {@link FlexoRole} identified by supplied name, which is to be retrieved in all accessible properties<br>
		 * Note that returned role is not necessary one of declared role, but might be inherited.
		 * 
		 * @param propertyName
		 * @return
		 * @see #getAccessibleRoles()
		 */
		@Override
		public FlexoRole<?> getAccessibleRole(String roleName) {
			for (FlexoRole<?> p : getAccessibleRoles()) {
				if (p.getName().equals(roleName)) {
					return p;
				}
			}
			return null;
		}

		/**
		 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all accessible properties<br>
		 * Note that returned property is not necessary one of declared property, but might be inherited.
		 * 
		 * @param flexoPropertyName
		 * @return
		 * @see #getAccessibleProperties()
		 */
		@Override
		public FlexoProperty<?> getAccessibleProperty(String propertyName) {
			for (FlexoProperty<?> p : getAccessibleProperties()) {
				if (p.getName().equals(propertyName)) {
					return p;
				}
			}
			return null;
		}

		/**
		 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all declared properties<br>
		 * 
		 * @param propertyName
		 * @return
		 * @see #getDeclaredProperties()
		 */
		@Override
		public FlexoProperty<?> getDeclaredProperty(String propertyName) {
			for (FlexoProperty<?> p : getDeclaredProperties()) {
				if (p.getName().equals(propertyName)) {
					return p;
				}
			}
			return null;
		}

		private Vector<String> availablePropertiesNames = null;

		public Vector<String> getAvailablePropertyNames() {
			if (availablePropertiesNames == null) {
				availablePropertiesNames = new Vector<String>();
				for (FlexoProperty<?> r : getAccessibleProperties()) {
					availablePropertiesNames.add(r.getName());
				}
			}
			return availablePropertiesNames;
		}

		@Override
		public String getAvailablePropertyName(String baseName) {
			String testName = baseName;
			int index = 2;
			while (getAccessibleProperty(testName) != null) {
				testName = baseName + index;
				index++;
			}
			return testName;
		}

		@Override
		public String getAvailableFlexoBehaviourName(String baseName) {
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

			if (uri != null && !uri.isEmpty() && getOwningVirtualModel() != null) {
				if (uri.contains(getOwningVirtualModel().getURI())) {
					String behaviourname = uri.replace(getOwningVirtualModel().getURI(), "").substring(1);
					return getFlexoBehaviour(behaviourname);
				}
				else {
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
		public void addToFlexoBehaviours(FlexoBehaviour aFlexoBehaviour) {
			performSuperAdder(FLEXO_BEHAVIOURS_KEY, aFlexoBehaviour);
			notifiedBehavioursChanged(null, aFlexoBehaviour);
		}

		@Override
		public void removeFromFlexoBehaviours(FlexoBehaviour aFlexoBehaviour) {
			performSuperRemover(FLEXO_BEHAVIOURS_KEY, aFlexoBehaviour);
			notifiedBehavioursChanged(aFlexoBehaviour, null);
		}

		protected void notifiedBehavioursChanged(FlexoBehaviour oldValue, FlexoBehaviour newValue) {
			if (getBehaviouralFacet() instanceof FlexoConceptBehaviouralFacetImpl) {
				((FlexoConceptBehaviouralFacetImpl) getBehaviouralFacet()).notifiedBehavioursChanged(oldValue, newValue);
			}
			if (newValue instanceof CreationScheme) {
				getPropertyChangeSupport().firePropertyChange("creationSchemes", null, getCreationSchemes());
				getPropertyChangeSupport().firePropertyChange("abstractCreationSchemes", null, getAbstractCreationSchemes());
			}
			if (newValue instanceof ActionScheme) {
				getPropertyChangeSupport().firePropertyChange("actionSchemes", null, getActionSchemes());
			}
			if (newValue instanceof DeletionScheme) {
				getPropertyChangeSupport().firePropertyChange("deletionSchemes", null, getDeletionSchemes());
			}
			if (newValue instanceof NavigationScheme) {
				getPropertyChangeSupport().firePropertyChange("navigationSchemes", null, getNavigationSchemes());
			}
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
			DeletionScheme newDeletionScheme = getFMLModelFactory().newDeletionScheme();
			newDeletionScheme.setName("deletion");
			newDeletionScheme.setControlGraph(getFMLModelFactory().newEmptyControlGraph());
			addToFlexoBehaviours(newDeletionScheme);

			List<FlexoProperty<?>> propertiesToDelete = new ArrayList<FlexoProperty<?>>();
			for (FlexoProperty<?> pr : getDeclaredProperties()) {
				if (pr.defaultBehaviourIsToBeDeleted()) {
					propertiesToDelete.add(pr);
				}
			}
			for (FlexoProperty<?> pr : propertiesToDelete) {
				DeleteAction deleteAction = getFMLModelFactory().newDeleteAction();
				deleteAction.setObject(new DataBinding<Object>(pr.getPropertyName()));
				newDeletionScheme.getControlGraph().sequentiallyAppend(deleteAction);
			}
			return newDeletionScheme;
		}

		@Override
		public FlexoConceptInspector getInspector() {
			if (inspector == null && getFMLModelFactory() != null) {
				inspector = getFMLModelFactory().newFlexoConceptInspector(this);
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

		/*@Override
		public AbstractVirtualModel<?> getParentVirtualModel() {
			return virtualModel;
		}
		
		@Override
		public void setParentVirtualModel(AbstractVirtualModel<?> virtualModel) {
			if (this.virtualModel != virtualModel) {
				AbstractVirtualModel<?> oldVirtualModel = this.virtualModel;
				this.virtualModel = virtualModel;
				getPropertyChangeSupport().firePropertyChange(PARENT_VIRTUAL_MODEL_KEY, oldVirtualModel, virtualModel);
			}
		}*/

		@Override
		public String toString() {
			return "FlexoConcept:" + getName();
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			// createBindingModel();
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				es.finalizeDeserialization();
			}
			for (FlexoProperty<?> pr : getDeclaredProperties()) {
				pr.finalizeDeserialization();
			}
		}

		public void debug() {
			System.out.println(getStringRepresentation());
		}

		@Deprecated
		public void save() {
			try {
				getOwningVirtualModel().getResource().save(null);
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
		public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) throws InconsistentFlexoConceptHierarchyException {
			performSuperSetter(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcepts);
		}

		@Override
		public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) throws InconsistentFlexoConceptHierarchyException {
			if (!isSuperConceptOf(parentFlexoConcept)) {
				performSuperAdder(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcept);
				if (getOwningVirtualModel() != null) {
					getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
					getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
							getOwningVirtualModel().getAllRootFlexoConcepts());
				}
			}
			else {
				throw new InconsistentFlexoConceptHierarchyException(
						"FlexoConcept " + this + " : Could not add as parent FlexoConcept: " + parentFlexoConcept);
			}
		}

		@Override
		public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept) {
			performSuperRemover(PARENT_FLEXO_CONCEPTS_KEY, parentFlexoConcept);
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
				getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						getOwningVirtualModel().getAllRootFlexoConcepts());
			}
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
			if (flexoConcept == null) {
				return false;
			}
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

			if (getDeclaredProperties().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoProperty<?> pr : getDeclaredProperties()) {
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
	public static class NonAbstractFlexoConceptShouldHaveProperties
			extends ValidationRule<NonAbstractFlexoConceptShouldHaveProperties, FlexoConcept> {
		public NonAbstractFlexoConceptShouldHaveProperties() {
			super(FlexoConcept.class, "non_abstract_flexo_concept_should_have_properties");
		}

		@Override
		public ValidationIssue<NonAbstractFlexoConceptShouldHaveProperties, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!(flexoConcept instanceof AbstractVirtualModel) && flexoConcept.getDeclaredProperties().size() == 0) {
				return new ValidationWarning<NonAbstractFlexoConceptShouldHaveProperties, FlexoConcept>(this, flexoConcept,
						"non_abstract_flexo_concept_role_does_not_define_any_property");
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
