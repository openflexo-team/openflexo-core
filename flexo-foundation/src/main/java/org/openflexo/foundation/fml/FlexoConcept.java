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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.openflexo.foundation.technologyadapter.ModelSlot;
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
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
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
@Imports({ @Import(FlexoEvent.class) })
public interface FlexoConcept extends FlexoConceptObject, VirtualModelObject {

	@PropertyIdentifier(type = VirtualModel.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String CONTAINER_FLEXO_CONCEPT_KEY = "containerFlexoConcept";
	@PropertyIdentifier(type = List.class)
	public static final String EMBEDDED_FLEXO_CONCEPT_KEY = "embeddedFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_BEHAVIOURS_KEY = "flexoBehaviours";
	@PropertyIdentifier(type = List.class)
	public static final String FLEXO_PROPERTIES_KEY = "flexoProperties";
	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String INSPECTOR_KEY = "inspector";
	@PropertyIdentifier(type = String.class)
	public static final String PARENT_FLEXO_CONCEPTS_LIST_KEY = "parentFlexoConceptsList";
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
	public VirtualModel getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(VirtualModel virtualModel);

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(value = CONTAINER_FLEXO_CONCEPT_KEY, inverse = EMBEDDED_FLEXO_CONCEPT_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getContainerFlexoConcept();

	@Setter(CONTAINER_FLEXO_CONCEPT_KEY)
	public void setContainerFlexoConcept(FlexoConcept name);

	/**
	 * Return all {@link FlexoConcept} contained in this {@link FlexoConcept}
	 * 
	 * @return
	 */
	@Getter(value = EMBEDDED_FLEXO_CONCEPT_KEY, cardinality = Cardinality.LIST, inverse = CONTAINER_FLEXO_CONCEPT_KEY)
	@XMLElement(context = "Embedded", primary = true)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoConcept> getEmbeddedFlexoConcepts();

	@Setter(EMBEDDED_FLEXO_CONCEPT_KEY)
	public void setEmbeddedFlexoConcepts(List<FlexoConcept> flexoConcepts);

	@Adder(EMBEDDED_FLEXO_CONCEPT_KEY)
	@PastingPoint
	public void addToEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept);

	@Remover(EMBEDDED_FLEXO_CONCEPT_KEY)
	public void removeFromEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept);

	public List<FlexoBehaviour> getDeclaredFlexoBehaviours();

	public List<FlexoBehaviour> getAccessibleFlexoBehaviours();

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

	/**
	 * Return first found declared FlexoBehaviour matching supplied name<br>
	 * Use this method with caution as it does not guarantee unicity of the result nor of returned type
	 * 
	 * @param behaviourName
	 * @return
	 */
	@Finder(collection = FLEXO_BEHAVIOURS_KEY, attribute = FlexoBehaviour.NAME_KEY)
	@Deprecated
	public FlexoBehaviour getFlexoBehaviour(String behaviourName);

	/**
	 * Return {@link FlexoBehaviour} matching supplied name and signature (expressed with types)<br>
	 * Search is perform in entire scope, and includes parent concept behaviours (inherited behaviours included)
	 * 
	 * @param behaviourName
	 * @param parameters
	 * @return
	 */
	public FlexoBehaviour getFlexoBehaviour(String behaviourName, Type... parameters);

	/**
	 * Return {@link FlexoBehaviour} matching supplied name and signature (expressed with types), which are declared for this concept.
	 * Result does not include inherited behaviours.
	 * 
	 * @param behaviourName
	 * @param parameters
	 * @return
	 */
	public FlexoBehaviour getDeclaredFlexoBehaviour(String behaviourName, Type... parameters);

	@Getter(value = FLEXO_PROPERTIES_KEY, cardinality = Cardinality.LIST, inverse = FlexoProperty.FLEXO_CONCEPT_KEY)
	@XMLElement(deprecatedContext = "ModelSlot_", primary = true)
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
	 * Such properties are those that are available in the most specialized context<br>
	 * Some properties may be shadowed by a more specialized property and are not retrieved here.
	 * 
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
	 * Build and return the list of all accessible {@link AbstractProperty} from this {@link FlexoConcept}
	 * 
	 * @return
	 */
	public List<AbstractProperty> getAccessibleAbstractProperties();

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

	// ModelSlot are also FlexoRole instances, but it's usefull to be able to access them
	public List<ModelSlot<?>> getModelSlots();

	public void addToModelSlots(ModelSlot<?> aModelSlot);

	public void removeFromModelSlots(ModelSlot<?> aModelSlot);

	public ModelSlot<?> getModelSlot(String modelSlotName);

	public <MS extends ModelSlot<?>> List<MS> getModelSlots(Class<MS> msType);

	@Getter(value = INSPECTOR_KEY, inverse = FlexoConceptInspector.FLEXO_CONCEPT_KEY)
	@XMLElement(xmlTag = "Inspector")
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public FlexoConceptInspector getInspector();

	@Setter(INSPECTOR_KEY)
	public void setInspector(FlexoConceptInspector inspector);

	// Used for serialization, do not use as API
	@Getter(PARENT_FLEXO_CONCEPTS_LIST_KEY)
	@XMLAttribute
	public String _getParentFlexoConceptsList();

	// Used for serialization, do not use as API
	@Setter(PARENT_FLEXO_CONCEPTS_LIST_KEY)
	public void _setParentFlexoConceptsList(String conceptsList);

	@Getter(value = PARENT_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = CHILD_FLEXO_CONCEPTS_KEY)
	public List<FlexoConcept> getParentFlexoConcepts();

	@Setter(PARENT_FLEXO_CONCEPTS_KEY)
	public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) throws InconsistentFlexoConceptHierarchyException;

	@Adder(PARENT_FLEXO_CONCEPTS_KEY)
	public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) throws InconsistentFlexoConceptHierarchyException;

	@Remover(PARENT_FLEXO_CONCEPTS_KEY)
	public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept);

	@Getter(value = CHILD_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST/*, inverse = PARENT_FLEXO_CONCEPTS_KEY*/)
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

	/**
	 * Return boolean indicating whether this concept has a FlexoConcept for container (containment semantics)<br>
	 * 
	 * @return
	 */
	public boolean isRoot();

	/**
	 * Return boolean indicating whether this concept has no parent in this VirtualModel (inheritance semantics)
	 * 
	 * @return
	 */
	public boolean isSuperConcept();

	public <ES extends FlexoBehaviour> List<ES> getFlexoBehaviours(Class<ES> editionSchemeClass);

	public <ES extends FlexoBehaviour> List<ES> getAccessibleFlexoBehaviours(Class<ES> editionSchemeClass);

	public List<AbstractActionScheme> getAbstractActionSchemes();

	public List<ActionScheme> getActionSchemes();

	public List<AbstractActionScheme> getAccessibleAbstractActionSchemes();

	public List<ActionScheme> getAccessibleActionSchemes();

	public List<DeletionScheme> getAccessibleDeletionSchemes();

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

	public InnerConceptsFacet getInnerConceptsFacet();

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
		private InnerConceptsFacet innerConceptsFacet;

		private final FlexoConceptInstanceType instanceType = new FlexoConceptInstanceType(this);

		private FlexoConceptBindingModel bindingModel;

		private String parentFlexoConceptList;

		/**
		 * Stores a cache for properties for all end-properties of this {@link FlexoConcept}
		 */
		private List<FlexoProperty<?>> accessibleProperties;

		@Override
		public VirtualModel getVirtualModel() {
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
		public InnerConceptsFacet getInnerConceptsFacet() {
			FMLModelFactory factory = getFMLModelFactory();
			if (innerConceptsFacet == null && factory != null) {
				CompoundEdit ce = null;
				if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					ce = factory.getEditingContext().getUndoManager().startRecording("CREATE_INNER_CONCEPTS_FACET");
				}
				innerConceptsFacet = factory.newInnerConceptsFacet(this);
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
			}
			return innerConceptsFacet;
		}

		@Override
		public FlexoConceptImpl getFlexoConcept() {
			return this;
		}

		@Override
		public boolean delete(Object... context) {
			Map<FlexoConcept, List<FlexoConcept>> oldParents = new HashMap<>();
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				oldParents.put(parentConcept, new ArrayList<>(parentConcept.getChildFlexoConcepts()));
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
		 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
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
			if (getAccessibleProperties() != null) {
				for (FlexoProperty<?> p : getAccessibleProperties()) {
					if (p.isAbstract()) {
						return true;
					}
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
		 * Build and return all end-properties for this {@link FlexoConcept}<br>
		 * Such properties are those that are available in the most specialized context<br>
		 * Some properties may be shadowed by a more specialized property and are not retrieved here.
		 * 
		 * This returned {@link List} includes all declared properties for this FlexoConcept, augmented with all properties of parent
		 * {@link FlexoConcept} which are not parent properties of this concept declared properties.<br>
		 * This means that only leaf nodes of inheritance graph infered by this {@link FlexoConcept} hierarchy will be returned.
		 * 
		 * 
		 * @return
		 */
		@Override
		public List<FlexoProperty<?>> getAccessibleProperties() {

			// Implements a cache
			// Do not recompute accessible properties when not required

			if (accessibleProperties == null) {

				List<FlexoProperty<?>> computedAccessibleProperties = new ArrayList<>();
				Map<String, FlexoProperty<?>> inheritedProperties = new HashMap<>();

				// First take declared properties
				computedAccessibleProperties.addAll(getDeclaredProperties());

				// Take properties obtained by containment
				if (getContainerFlexoConcept() != null) {
					computedAccessibleProperties.addAll(getContainerFlexoConcept().getAccessibleProperties());
				}

				// Take inherited properties
				for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
					for (FlexoProperty<?> p : parentConcept.getAccessibleProperties()) {
						if (getDeclaredProperty(p.getPropertyName()) == null) {
							// This property is inherited but not overriden
							// We check that we don't have this property yet
							if (inheritedProperties.get(p.getName()) == null) {
								inheritedProperties.put(p.getName(), p);
							}
							else if (inheritedProperties.get(p.getName()).isSuperPropertyOf(p)) {
								inheritedProperties.put(p.getName(), p);
							}
						}
					}
				}

				if (getVirtualModel() != null && getVirtualModel() != this) {
					for (FlexoProperty<?> p : getVirtualModel().getAccessibleProperties()) {
						if (getDeclaredProperty(p.getPropertyName()) == null) {
							// This property is inherited but not overriden
							// We check that we don't have this property yet
							if (inheritedProperties.get(p.getName()) == null) {
								inheritedProperties.put(p.getName(), p);
							}
						}
					}
				}

				// Now, we have to suppress all extra references
				List<FlexoProperty<?>> unnecessaryProperty = new ArrayList<>();
				for (FlexoProperty<?> p : inheritedProperties.values()) {
					for (FlexoProperty<?> superP : p.getAllSuperProperties()) {
						if (inheritedProperties.get(superP.getName()) != null) {
							unnecessaryProperty.add(superP);
						}
					}
				}

				for (FlexoProperty<?> removeThis : unnecessaryProperty) {
					inheritedProperties.remove(removeThis);
				}

				try {
					if (computedAccessibleProperties != null) {
						computedAccessibleProperties.addAll(inheritedProperties.values());
					}
				} catch (NullPointerException e) {
					logger.warning("Something wrong in getAccessibleProperty() evaluation for " + this);
				}

				accessibleProperties = computedAccessibleProperties;
			}

			return accessibleProperties;
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
			clearAccessiblePropertiesCache();
		}

		public void clearAccessiblePropertiesCache() {
			// Reset accessible properties
			accessibleProperties = null;
			for (FlexoConcept embedded : getEmbeddedFlexoConcepts()) {
				((FlexoConceptImpl) embedded).clearAccessiblePropertiesCache();
			}
			for (FlexoConcept child : getChildFlexoConcepts()) {
				((FlexoConceptImpl) child).clearAccessiblePropertiesCache();
			}

		}

		@Override
		public <R> List<R> getDeclaredProperties(Class<R> type) {
			List<R> returned = new ArrayList<>();
			for (FlexoProperty<?> r : getDeclaredProperties()) {
				if (TypeUtils.isTypeAssignableFrom(type, r.getClass())) {
					returned.add((R) r);
				}
			}
			return returned;
		}

		@Override
		public <R> List<R> getAccessibleProperties(Class<R> type) {
			List<R> returned = new ArrayList<>();
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
		public List<AbstractProperty> getAccessibleAbstractProperties() {
			return getAccessibleProperties(AbstractProperty.class);
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
			List<FlexoProperty<?>> pList = new ArrayList<>();
			for (FlexoProperty<?> p : getAccessibleProperties()) {
				if (p.getName().equals(propertyName)) {
					pList.add(p);
				}
			}
			if (pList.size() == 0) {
				return null;
			}
			else if (pList.size() == 1) {
				return pList.get(0);
			}
			else {
				logger.warning("More thant one matching property: " + pList);
				return pList.get(0);
			}
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
				availablePropertiesNames = new Vector<>();
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
		public List<ModelSlot<?>> getModelSlots() {
			return (List) getAccessibleProperties(ModelSlot.class);
		}

		@Override
		public void addToModelSlots(ModelSlot<?> aModelSlot) {
			addToFlexoProperties(aModelSlot);
		}

		@Override
		public void removeFromModelSlots(ModelSlot<?> aModelSlot) {
			removeFromFlexoProperties(aModelSlot);
		}

		@Override
		public ModelSlot<?> getModelSlot(String modelSlotName) {
			return (ModelSlot<?>) getAccessibleProperty(modelSlotName);
		}

		@Override
		public <MS extends ModelSlot<?>> List<MS> getModelSlots(Class<MS> msType) {
			return getAccessibleProperties(msType);
		}

		public List<ModelSlot<?>> getRequiredModelSlots() {
			List<ModelSlot<?>> requiredModelSlots = new ArrayList<>();
			for (ModelSlot<?> modelSlot : getModelSlots()) {
				if (modelSlot.getIsRequired()) {
					requiredModelSlots.add(modelSlot);
				}
			}
			return requiredModelSlots;
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
		public FlexoBehaviour getFlexoBehaviour(String behaviourName, Type... parameters) {
			FlexoBehaviour returned = getDeclaredFlexoBehaviour(behaviourName, parameters);
			if (returned != null) {
				return returned;
			}
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				returned = parentConcept.getFlexoBehaviour(behaviourName, parameters);
				if (returned != null) {
					return returned;
				}
			}
			return null;
		}

		@Override
		public FlexoBehaviour getDeclaredFlexoBehaviour(String behaviourName, Type... parameters) {
			for (FlexoBehaviour b : getDeclaredFlexoBehaviours()) {
				if (b.getName().equals(behaviourName)) {
					if (b.getParameters().size() == parameters.length) {
						boolean allParametersMatch = true;
						for (int i = 0; i < b.getParameters().size(); i++) {
							if (!TypeUtils.isTypeAssignableFrom(b.getParameters().get(i).getType(), parameters[i], true)) {
								allParametersMatch = false;
								break;
							}
						}
						if (allParametersMatch) {
							return b;
						}
					}
				}
			}
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <ES extends FlexoBehaviour> List<ES> getFlexoBehaviours(Class<ES> editionSchemeClass) {
			List<ES> returned = new ArrayList<>();
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				if (editionSchemeClass.isAssignableFrom(es.getClass())) {
					returned.add((ES) es);
				}
			}
			return returned;
		}

		@Override
		public List<FlexoBehaviour> getDeclaredFlexoBehaviours() {
			return getFlexoBehaviours();
		}

		/**
		 * Return behaviours that are accessible from this FlexoConcept<br>
		 * This method manages inheritance, and shadowed behaviours are discarded
		 * 
		 * @return
		 */
		@Override
		public List<FlexoBehaviour> getAccessibleFlexoBehaviours() {

			if (getParentFlexoConcepts().size() == 0) {
				return getDeclaredFlexoBehaviours();
			}

			List<FlexoBehaviour> returned = new ArrayList<>();
			// List<FlexoBehaviour> inheritedBehaviours = new ArrayList<FlexoBehaviour>();

			returned.addAll(getDeclaredFlexoBehaviours());

			// Take behaviours obtained by containment
			if (getContainerFlexoConcept() != null) {
				returned.addAll(getContainerFlexoConcept().getAccessibleFlexoBehaviours());
			}

			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				for (FlexoBehaviour behaviour : parentConcept.getAccessibleFlexoBehaviours()) {

					if (!behaviour.isOverridenInContext(this)) {
						returned.add(behaviour);
					}

				}
			}

			return returned;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <ES extends FlexoBehaviour> List<ES> getAccessibleFlexoBehaviours(Class<ES> editionSchemeClass) {
			List<ES> returned = new ArrayList<>();
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
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
		public List<AbstractActionScheme> getAccessibleAbstractActionSchemes() {
			return getAccessibleFlexoBehaviours(AbstractActionScheme.class);
		}

		@Override
		public List<ActionScheme> getAccessibleActionSchemes() {
			return getAccessibleFlexoBehaviours(ActionScheme.class);
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
		public List<DeletionScheme> getAccessibleDeletionSchemes() {
			return getAccessibleFlexoBehaviours(DeletionScheme.class);
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
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
				if (es instanceof ActionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasCreationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
				if (es instanceof CreationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasDeletionScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
				if (es instanceof DeletionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasSynchronizationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
				if (es instanceof SynchronizationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasNavigationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours()) {
				if (es instanceof NavigationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public DeletionScheme getDefaultDeletionScheme() {
			if (getAccessibleDeletionSchemes().size() > 0) {
				return getAccessibleDeletionSchemes().get(0);
			}
			return null;
		}

		@Override
		public DeletionScheme generateDefaultDeletionScheme() {
			if (getFMLModelFactory() == null) {
				return null;
			}
			DeletionScheme newDeletionScheme = getFMLModelFactory().newDeletionScheme();
			newDeletionScheme.setName("delete");
			newDeletionScheme.setControlGraph(getFMLModelFactory().newEmptyControlGraph());
			addToFlexoBehaviours(newDeletionScheme);

			List<FlexoProperty<?>> propertiesToDelete = new ArrayList<>();
			for (FlexoProperty<?> pr : getDeclaredProperties()) {
				if (!(pr instanceof PrimitiveRole) && pr.defaultBehaviourIsToBeDeleted()) {
					propertiesToDelete.add(pr);
				}
			}
			for (FlexoProperty<?> pr : propertiesToDelete) {
				DeleteAction deleteAction = getFMLModelFactory().newDeleteAction();
				deleteAction.setObject(new DataBinding<>(pr.getPropertyName()));
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
			decodeParentFlexoConceptList(true);
		}

		public void debug() {
			System.out.println(getStringRepresentation());
		}

		@Deprecated
		public void save() {
			try {
				getOwningVirtualModel().getResource().save(null);
			} catch (SaveResourceException e) {
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
		public boolean isSuperConcept() {
			return getParentFlexoConcepts().size() == 0;
		}

		@Override
		public boolean isRoot() {
			return getContainerFlexoConcept() == null;
		}

		@Override
		public void setOwner(VirtualModel virtualModel) {
			performSuperSetter(OWNER_KEY, virtualModel);
			clearAccessiblePropertiesCache();
		}

		@Override
		public void setChildFlexoConcepts(List<FlexoConcept> childFlexoConcepts) {
			performSuperSetter(CHILD_FLEXO_CONCEPTS_KEY, childFlexoConcepts);
			clearAccessiblePropertiesCache();
		}

		@Override
		public void addToChildFlexoConcepts(FlexoConcept childFlexoConcept) {
			performSuperAdder(CHILD_FLEXO_CONCEPTS_KEY, childFlexoConcept);
			clearAccessiblePropertiesCache();
		}

		@Override
		public void removeFromChildFlexoConcepts(FlexoConcept childFlexoConcept) {
			performSuperRemover(CHILD_FLEXO_CONCEPTS_KEY, childFlexoConcept);
			clearAccessiblePropertiesCache();
		}

		@Override
		public void setContainerFlexoConcept(FlexoConcept aConcept) {
			performSuperSetter(CONTAINER_FLEXO_CONCEPT_KEY, aConcept);
			clearAccessiblePropertiesCache();
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
				getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						getOwningVirtualModel().getAllRootFlexoConcepts());
			}
		}

		@Override
		public void addToEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperAdder(EMBEDDED_FLEXO_CONCEPT_KEY, aFlexoConcept);
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
				getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						getOwningVirtualModel().getAllRootFlexoConcepts());
			}
		}

		@Override
		public void removeFromEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperRemover(EMBEDDED_FLEXO_CONCEPT_KEY, aFlexoConcept);
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
				getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						getOwningVirtualModel().getAllRootFlexoConcepts());
			}
		}

		// Used for serialization, do not use as API
		@Override
		public String _getParentFlexoConceptsList() {
			if (parentFlexoConceptList == null && getParentFlexoConcepts().size() > 0) {
				parentFlexoConceptList = computeParentFlexoConceptList();
			}
			return parentFlexoConceptList;
		}

		// Used for serialization, do not use as API
		@Override
		public void _setParentFlexoConceptsList(String parentFlexoConceptList) {
			this.parentFlexoConceptList = parentFlexoConceptList;
		}

		private String computeParentFlexoConceptList() {

			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (FlexoConcept parent : getParentFlexoConcepts()) {
				sb.append((isFirst ? "" : ",") + parent.getURI());
				isFirst = false;
			}
			return sb.toString();
		}

		private boolean isDecodingParentFlexoConceptList = false;

		private void decodeParentFlexoConceptList(boolean loadWhenRequired) {
			if (parentFlexoConceptList != null && getVirtualModelLibrary() != null && !isDecodingParentFlexoConceptList) {
				isDecodingParentFlexoConceptList = true;
				StringTokenizer st = new StringTokenizer(parentFlexoConceptList, ",");
				List<FlexoConcept> parentConcepts = new ArrayList<>();
				boolean someConceptsWereNotDecoded = false;
				while (st.hasMoreTokens()) {
					String conceptURI = st.nextToken();
					FlexoConcept concept = getVirtualModelLibrary().getFlexoConcept(conceptURI, loadWhenRequired);
					if (concept != null) {
						parentConcepts.add(concept);
					}
					else {
						someConceptsWereNotDecoded = true;
					}
				}
				if (!someConceptsWereNotDecoded) {
					// OK, all concepts were decoded, fill in parent concepts
					parentFlexoConcepts.clear();
					for (FlexoConcept parent : parentConcepts) {
						try {
							addToParentFlexoConcepts(parent);
						} catch (InconsistentFlexoConceptHierarchyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					parentFlexoConceptList = null;
				}
				else {
					// Some concepts are not decoded yet, we don't do anything
				}
				isDecodingParentFlexoConceptList = false;
			}
		}

		private List<FlexoConcept> parentFlexoConcepts = new ArrayList<>();

		@Override
		public List<FlexoConcept> getParentFlexoConcepts() {
			if (parentFlexoConceptList != null && getVirtualModelLibrary() != null) {
				decodeParentFlexoConceptList(!isDeserializing());
			}
			return parentFlexoConcepts;
		}

		@Override
		public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) throws InconsistentFlexoConceptHierarchyException {
			this.parentFlexoConcepts.clear();
			if (parentFlexoConcepts != null) {
				this.parentFlexoConcepts.addAll(parentFlexoConcepts);
				getPropertyChangeSupport().firePropertyChange("parentFlexoConcepts", null, parentFlexoConcepts);
			}
		}

		@Override
		public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) throws InconsistentFlexoConceptHierarchyException {
			if (!isSuperConceptOf(parentFlexoConcept)) {
				parentFlexoConcepts.add(parentFlexoConcept);
				parentFlexoConcept.addToChildFlexoConcepts(this);
				getPropertyChangeSupport().firePropertyChange("parentFlexoConcepts", null, parentFlexoConcept);
				parentFlexoConceptList = null;
				accessibleProperties = null;
				if (getOwningVirtualModel() != null) {
					getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
					getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", null,
							getOwningVirtualModel().getAllSuperFlexoConcepts());
				}
				setIsModified();
			}
			else {
				throw new InconsistentFlexoConceptHierarchyException(
						"FlexoConcept " + this + " : Could not add as parent FlexoConcept: " + parentFlexoConcept);
			}
		}

		@Override
		public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept) {
			parentFlexoConcepts.remove(parentFlexoConcept);
			parentFlexoConcept.removeFromChildFlexoConcepts(this);
			getPropertyChangeSupport().firePropertyChange("parentFlexoConcepts", parentFlexoConcept, null);
			parentFlexoConceptList = null;
			accessibleProperties = null;
			if (getOwningVirtualModel() != null) {
				getOwningVirtualModel().getInnerConceptsFacet().notifiedConceptsChanged();
				getOwningVirtualModel().getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", null,
						getOwningVirtualModel().getAllSuperFlexoConcepts());
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

			List<FlexoConcept> returned = new ArrayList<>();
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
				boolean isFirst = true;
				for (FlexoConcept parent : getParentFlexoConcepts()) {
					out.append((isFirst ? "" : ",") + parent.getName(), context);
					isFirst = false;
				}

			}
			out.append(" {", context);

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

		/**
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link ViewPoint}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link ViewPoint} enclosing, we must provide this hook to give a chance to
		 * objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
		 * 
		 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...<br>
		 * 
		 */
		@Override
		public void notifiedScopeChanged() {
			super.notifiedScopeChanged();
			for (FlexoBehaviour behaviour : getFlexoBehaviours()) {
				behaviour.notifiedScopeChanged();
			}
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
			if (!(flexoConcept instanceof VirtualModel) && flexoConcept.getDeclaredProperties().size() == 0) {
				return new ValidationWarning<>(this, flexoConcept, "non_abstract_flexo_concept_role_does_not_define_any_property");
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
			if (!flexoConcept.isAbstract() && flexoConcept.getFlexoBehaviours().size() == 0) {
				return new ValidationWarning<>(this, flexoConcept, "non_abstract_flexo_concept_($validable.name)_has_no_behaviours");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class FlexoConceptShouldHaveDeletionScheme extends ValidationRule<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> {
		public FlexoConceptShouldHaveDeletionScheme() {
			super(FlexoConcept.class, "non_abstract_flexo_concept_should_have_deletion_scheme");
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!flexoConcept.isAbstract() && flexoConcept.getDeletionSchemes().size() == 0) {
				CreateDefaultDeletionScheme fixProposal = new CreateDefaultDeletionScheme(flexoConcept);
				return new ValidationWarning<>(this, flexoConcept, "non_abstract_flexo_concept_($validable.name)_has_no_deletion_scheme",
						fixProposal);
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
