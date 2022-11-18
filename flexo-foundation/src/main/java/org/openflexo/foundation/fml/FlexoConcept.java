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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.FragmentContext;
import org.openflexo.foundation.fml.FlexoConceptBehaviouralFacet.FlexoConceptBehaviouralFacetImpl;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet.FlexoConceptStructuralFacetImpl;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.rm.BasicResourceImpl.LocatorNotFoundException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.swing.ImageUtils;
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
@Imports({ @Import(FlexoEvent.class), @Import(FlexoEnum.class) })
public interface FlexoConcept extends FlexoConceptObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = VirtualModel.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = Visibility.class)
	public static final String VISIBILITY_KEY = "visibility";
	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String CONTAINER_FLEXO_CONCEPT_KEY = "containerFlexoConcept";
	@PropertyIdentifier(type = List.class)
	public static final String EMBEDDED_FLEXO_CONCEPT_KEY = "embeddedFlexoConcepts";
	@PropertyIdentifier(type = FlexoBehaviour.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_BEHAVIOURS_KEY = "flexoBehaviours";
	@PropertyIdentifier(type = FlexoProperty.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_PROPERTIES_KEY = "flexoProperties";
	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String KEY_PROPERTIES_KEY = "keyProperties";
	@PropertyIdentifier(type = FlexoProperty.class, cardinality = Cardinality.LIST)
	public static final String INSPECTOR_KEY = "inspector";
	@PropertyIdentifier(type = String.class)
	public static final String PARENT_FLEXO_CONCEPTS_LIST_KEY = "parentFlexoConceptsList";
	@PropertyIdentifier(type = List.class)
	public static final String PARENT_FLEXO_CONCEPTS_KEY = "parentFlexoConcepts";
	@PropertyIdentifier(type = List.class)
	public static final String CHILD_FLEXO_CONCEPTS_KEY = "childFlexoConcepts";
	@PropertyIdentifier(type = AbstractInvariant.class, cardinality = Cardinality.LIST)
	public static final String INVARIANTS_KEY = "invariants";
	@PropertyIdentifier(type = Boolean.class)
	public static final String IS_ABSTRACT_KEY = "isAbstract";

	public static final String ACCESSIBLE_PROPERTIES_KEY = "accessibleProperties";
	public static final String ACCESSIBLE_BEHAVIOURS_KEY = "accessibleBehaviours";
	public static final String APPLICABLE_CONTAINER_FLEXO_CONCEPT_KEY = "applicableContainerFlexoConcept";

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
	public void setName(String name) throws InvalidNameException;

	/**
	 * Return the URI of this {@link FlexoConcept}<br>
	 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
	 * eg<br>
	 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
	 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
	 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept#AnInnerConcept.MyBehaviour
	 * 
	 * @return String representing unique URI of this object
	 */
	public String getURI();

	/**
	 * Return local URI of this {@link FlexoConcept} (URI in the context of owning {@link VirtualModel}
	 */
	public String getLocalURI();

	@Getter(value = VISIBILITY_KEY, defaultValue = "Default")
	@XMLAttribute
	public Visibility getVisibility();

	@Setter(VISIBILITY_KEY)
	public void setVisibility(Visibility visibility);

	/**
	 * Return container {@link FlexoConcept} relative to containment. This is the {@link FlexoConcept} in which lives this concept
	 * 
	 * @return
	 */
	@Getter(value = CONTAINER_FLEXO_CONCEPT_KEY, inverse = EMBEDDED_FLEXO_CONCEPT_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getContainerFlexoConcept();

	/**
	 * Sets container {@link FlexoConcept} (relative to containment).
	 * 
	 * Note that this is an explicit declaration. When unspecified (let to null), this containment is inherited from its parent concepts
	 * 
	 * @param name
	 */
	@Setter(CONTAINER_FLEXO_CONCEPT_KEY)
	public void setContainerFlexoConcept(FlexoConcept container);

	/**
	 * Return applicable container {@link FlexoConcept} (relative to containment). When no explicit declaration was defined for this
	 * concept, this containment is inherited from its parent concepts
	 */
	public FlexoConcept getApplicableContainerFlexoConcept();

	/**
	 * Sets applicable container {@link FlexoConcept} (relative to containment).
	 * 
	 * 
	 * @param name
	 */
	public void setApplicableContainerFlexoConcept(FlexoConcept concept);

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

	@Reindexer(EMBEDDED_FLEXO_CONCEPT_KEY)
	public void moveEmbeddedFlexoConceptToIndex(FlexoConcept aFlexoConcept, int index);

	@Finder(collection = EMBEDDED_FLEXO_CONCEPT_KEY, attribute = FlexoConcept.NAME_KEY)
	public FlexoConcept getEmbeddedFlexoConcept(String conceptName);

	/**
	 * Return FlexoConcept in the same {@link VirtualModel} declaring this concept as its container {@link FlexoConcept}
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer();

	/**
	 * Return all accessible embedded FlexoConcept (those which are declared, and those accessed through inheritance)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAccessibleEmbeddedFlexoConcepts();

	public List<FlexoBehaviour> getDeclaredFlexoBehaviours();

	public List<FlexoBehaviour> getAccessibleFlexoBehaviours(boolean considerContainment);

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

	@Reindexer(FLEXO_BEHAVIOURS_KEY)
	public void moveFlexoBehaviourToIndex(FlexoBehaviour aFlexoBehaviour, int index);

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
	 * @param arguments
	 * @return
	 */
	public FlexoBehaviour getFlexoBehaviour(String behaviourName, Type... arguments);

	/**
	 * Return {@link FlexoBehaviour} matching supplied signature (expressed with types), which are declared for this concept. Result does
	 * not include inherited behaviours.
	 * 
	 * @param behaviourName
	 * @param parameters
	 * @return
	 */
	public FlexoBehaviour getDeclaredFlexoBehaviour(String signature);

	/**
	 * Return {@link FlexoBehaviour} matching supplied name and signature (expressed with types), which are declared for this concept.
	 * Result does not include inherited behaviours.
	 * 
	 * @param behaviourName
	 * @param parameters
	 * @return
	 */
	public FlexoBehaviour getDeclaredFlexoBehaviour(String behaviourName, Type... parameters);

	/**
	 * Return {@link FlexoProperty}'s explicitly declared in this {@link FlexoConcept}
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_PROPERTIES_KEY, cardinality = Cardinality.LIST, inverse = FlexoProperty.FLEXO_CONCEPT_KEY)
	@XMLElement(deprecatedContext = "ModelSlot_", primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoProperty<?>> getFlexoProperties();

	/**
	 * Sets {@link FlexoProperty}'s explicitly declared in this {@link FlexoConcept}
	 * 
	 * @param properties
	 */
	@Setter(FLEXO_PROPERTIES_KEY)
	public void setFlexoProperties(List<FlexoProperty<?>> properties);

	/**
	 * Add to {@link FlexoProperty}'s explicitly declared in this {@link FlexoConcept}
	 * 
	 * @param aProperty
	 */
	@Adder(FLEXO_PROPERTIES_KEY)
	@PastingPoint
	public void addToFlexoProperties(FlexoProperty<?> aProperty);

	/**
	 * Remove from {@link FlexoProperty}'s explicitly declared in this {@link FlexoConcept}
	 * 
	 * @param aProperty
	 */
	@Remover(FLEXO_PROPERTIES_KEY)
	public void removeFromFlexoProperties(FlexoProperty<?> aProperty);

	@Reindexer(FLEXO_PROPERTIES_KEY)
	public void moveFlexoPropertyToIndex(FlexoProperty<?> aProperty, int index);

	/**
	 * Return properties used to uniquely identify an instance of this concept
	 * 
	 * @return
	 */
	@Getter(value = KEY_PROPERTIES_KEY, cardinality = Cardinality.LIST)
	@XMLElement(context = "Key_", primary = false)
	public List<FlexoProperty<?>> getKeyProperties();

	/**
	 * Sets properties used to uniquely identify an instance of this concept
	 * 
	 * @param properties
	 */
	@Setter(KEY_PROPERTIES_KEY)
	public void setKeyProperties(List<FlexoProperty<?>> properties);

	/**
	 * Add to properties used to uniquely identify an instance of this concept
	 * 
	 * @param aProperty
	 */
	@Adder(KEY_PROPERTIES_KEY)
	public void addToKeyProperties(FlexoProperty<?> aProperty);

	/**
	 * Remove from properties used to uniquely identify an instance of this concept
	 * 
	 * @param aProperty
	 */
	@Remover(KEY_PROPERTIES_KEY)
	public void removeFromKeyProperties(FlexoProperty<?> aProperty);

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
	 * This means that only leaf nodes of inheritance graph inferred by this {@link FlexoConcept} hierarchy will be returned.
	 * 
	 * Note that this method is not efficient (perf issue: the list is rebuilt for each call)
	 * 
	 * @return
	 */
	public List<FlexoProperty<?>> getAccessibleProperties();

	/**
	 * Build and return all end-properties for this {@link FlexoConcept}<br>
	 * Such properties are those that are available in the most specialized context<br>
	 * Some properties may be shadowed by a more specialized property and are not retrieved here.
	 * 
	 * This returned {@link List} includes all declared properties for this FlexoConcept, augmented with all properties of parent
	 * {@link FlexoConcept} which are not parent properties of this concept declared properties.<br>
	 * This means that only leaf nodes of inheritance graph inferred by this {@link FlexoConcept} hierarchy will be returned.
	 * 
	 * @return
	 */
	public List<FlexoProperty<?>> retrieveAccessibleProperties(boolean considerContainment);

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
	 * Build and return all accessible key-properties for this {@link FlexoConcept}<br>
	 * Same semantics as for {@link #getAccessibleProperties()}
	 * 
	 * @return
	 */
	public List<FlexoProperty<?>> getAccessibleKeyProperties();

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

	// ModelSlot are also FlexoRole instances, but it's useful to be able to access them
	public List<ModelSlot<?>> getModelSlots();

	public void addToModelSlots(ModelSlot<?> aModelSlot);

	public void removeFromModelSlots(ModelSlot<?> aModelSlot);

	public ModelSlot<?> getModelSlot(String modelSlotName);

	public <MS extends ModelSlot<?>> List<MS> getModelSlots(Class<MS> msType);

	@Getter(value = INSPECTOR_KEY, inverse = FlexoConceptInspector.FLEXO_CONCEPT_KEY, ignoreForEquality = true)
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

	@Getter(value = PARENT_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = CHILD_FLEXO_CONCEPTS_KEY, ignoreForEquality = true)
	public List<FlexoConcept> getParentFlexoConcepts();

	@Setter(PARENT_FLEXO_CONCEPTS_KEY)
	public void setParentFlexoConcepts(List<FlexoConcept> parentFlexoConcepts) throws InconsistentFlexoConceptHierarchyException;

	@Adder(PARENT_FLEXO_CONCEPTS_KEY)
	public void addToParentFlexoConcepts(FlexoConcept parentFlexoConcept) throws InconsistentFlexoConceptHierarchyException;

	@Remover(PARENT_FLEXO_CONCEPTS_KEY)
	public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept);

	@Reindexer(PARENT_FLEXO_CONCEPTS_KEY)
	public void moveParentFlexoConceptToIndex(FlexoConcept parentFlexoConcept, int index);

	// Used in FML pettry-print
	public String getParentFlexoConceptsDeclaration();

	@Getter(value = CHILD_FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, isDerived = true/*, inverse = PARENT_FLEXO_CONCEPTS_KEY*/)
	// @XMLElement(context = "Child")
	public List<FlexoConcept> getChildFlexoConcepts();

	@Setter(CHILD_FLEXO_CONCEPTS_KEY)
	public void setChildFlexoConcepts(List<FlexoConcept> childFlexoConcepts);

	@Adder(CHILD_FLEXO_CONCEPTS_KEY)
	public void addToChildFlexoConcepts(FlexoConcept childFlexoConcept);

	@Remover(CHILD_FLEXO_CONCEPTS_KEY)
	public void removeFromChildFlexoConcepts(FlexoConcept childFlexoConcept);

	@Getter(value = INVARIANTS_KEY, cardinality = Cardinality.LIST, inverse = AbstractInvariant.FLEXO_CONCEPT_KEY)
	@XMLElement
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<AbstractInvariant> getInvariants();

	@Setter(INVARIANTS_KEY)
	public void setInvariants(List<AbstractInvariant> invariants);

	@Adder(INVARIANTS_KEY)
	@PastingPoint
	public void addToInvariants(AbstractInvariant anInvariant);

	@Remover(INVARIANTS_KEY)
	public void removeFromInvariants(AbstractInvariant anInvariant);

	@Reindexer(INVARIANTS_KEY)
	public void moveInvariantToIndex(AbstractInvariant constraint, int index);

	public List<SimpleInvariant> getSimpleInvariants();

	public List<IterationInvariant> getIterationInvariants();

	/**
	 * Return boolean indicating whether this concept has a FlexoConcept for container (containment semantics)<br>
	 * 
	 * @return
	 */
	public boolean isRoot();

	/**
	 * Return boolean indicating whether this concept has no parent in this VirtualModel (inheritance semantics), or have parents
	 * exclusively outside container {@link VirtualModel}
	 * 
	 * @return
	 */
	public boolean isSuperConceptOfContainerVirtualModel();

	public <ES extends FlexoBehaviour> List<ES> getFlexoBehaviours(Class<ES> editionSchemeClass);

	public <ES extends FlexoBehaviour> List<ES> getAccessibleFlexoBehaviours(Class<ES> editionSchemeClass, boolean considerContainment);

	public List<AbstractActionScheme> getAbstractActionSchemes();

	public List<ActionScheme> getActionSchemes();

	public List<AbstractActionScheme> getAccessibleAbstractActionSchemes();

	public List<CreationScheme> getAccessibleCreationSchemes();

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

	public CreationScheme getDefaultCreationScheme();

	public DeletionScheme getDefaultDeletionScheme();

	public DeletionScheme generateDefaultDeletionScheme();

	/**
	 * Return type representing an instance of this concept
	 * 
	 * @return
	 */
	public FlexoConceptInstanceType getInstanceType();

	/**
	 * Return type representing this concept or a sub-concept of this concept
	 * 
	 * @return
	 */
	public FlexoConceptType getConceptType();

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

	public List<FlexoConcept> getTopLevelSuperConcepts();

	/**
	 * Search and return {@link FlexoConcept} with supplied local name, given the context of this {@link FlexoConcept}<br>
	 * 
	 * Lookup algorithm follows:
	 * <ul>
	 * <li>If name matches declared {@link FlexoConcept} return this {@link FlexoConcept}</li>
	 * <li>If name matches any container {@link VirtualModel} or {@link FlexoConcept} (recursively from current to container), return
	 * related {@link FlexoConcept}</li>
	 * <li>If name matches any parent {@link FlexoConcept} (inheritance semantics), return related {@link VirtualModel}</li>
	 * <li>If name matches any contained {@link FlexoConcept}, return related {@link FlexoConcept}</li>
	 * </ul>
	 * 
	 * @param conceptName
	 * @return
	 */
	public FlexoConcept lookupFlexoConceptWithName(String conceptName);

	@PropertyIdentifier(type = Resource.class)
	public static final String BIG_ICON_RESOURCE_KEY = "bigIconResource";
	@PropertyIdentifier(type = Resource.class)
	public static final String MEDIUM_ICON_RESOURCE_KEY = "mediumIconResource";
	@PropertyIdentifier(type = Resource.class)
	public static final String SMALL_ICON_RESOURCE_KEY = "smallIconResource";

	/**
	 * Icon for FlexoConcept, with 64x64 pixel format
	 * 
	 * @return
	 */
	@Getter(value = BIG_ICON_RESOURCE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Resource getBigIconResource();

	@Setter(BIG_ICON_RESOURCE_KEY)
	public void setBigIconResource(Resource imageFile);

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public File getBigIconFile();

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public void setBigIconFile(File file) throws MalformedURLException, LocatorNotFoundException;

	/**
	 * Icon for FlexoConcept, with 64x64 pixel format
	 * 
	 * @return
	 */
	public ImageIcon getBigIcon();

	/**
	 * Icon for FlexoConcept, with 32x32 pixel format
	 * 
	 * @return
	 */
	@Getter(value = MEDIUM_ICON_RESOURCE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Resource getMediumIconResource();

	@Setter(MEDIUM_ICON_RESOURCE_KEY)
	public void setMediumIconResource(Resource imageFile);

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public File getMediumIconFile();

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public void setMediumIconFile(File file) throws MalformedURLException, LocatorNotFoundException;

	/**
	 * Icon for FlexoConcept, with 32x32 pixel format
	 * 
	 * @return
	 */
	public ImageIcon getMediumIcon();

	/**
	 * Icon for FlexoConcept, with 16x16 pixel format
	 * 
	 * @return
	 */
	@Getter(value = SMALL_ICON_RESOURCE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Resource getSmallIconResource();

	@Setter(SMALL_ICON_RESOURCE_KEY)
	public void setSmallIconResource(Resource imageFile);

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public File getSmallIconFile();

	// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
	public void setSmallIconFile(File file) throws MalformedURLException, LocatorNotFoundException;

	/**
	 * Icon for FlexoConcept, with 16x16 pixel format
	 * 
	 * @return
	 */
	public ImageIcon getSmallIcon();

	/**
	 * Return boolean indicating if this concept defines a delegated inspector
	 * 
	 * @return
	 */
	public boolean hasDelegatedInspector();

	/**
	 * Return applicable inspector, while returning delegate inspector when relevant, or current concept inspector
	 * 
	 * @return
	 */
	public FlexoConceptInspector getApplicableInspector();

	/**
	 * Return applicable renderer, exploiting concept hierarchy and finding most specialized one
	 * 
	 * @return
	 */
	public DataBinding<String> getApplicableRenderer();

	public String getPresentationName();

	public static abstract class FlexoConceptImpl extends FlexoConceptObjectImpl implements FlexoConcept, PropertyChangeListener {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoConcept.class.getPackage().getName());

		private FlexoConceptInspector inspector;

		private FlexoConceptStructuralFacet structuralFacet;
		private FlexoConceptBehaviouralFacet behaviouralFacet;
		private InnerConceptsFacet innerConceptsFacet;

		private final FlexoConceptInstanceType instanceType = new FlexoConceptInstanceType(this);
		private final FlexoConceptType conceptType = new FlexoConceptType(this);

		private FlexoConceptBindingModel bindingModel;

		@Deprecated
		private String parentFlexoConceptList;

		/**
		 * Stores a cache for properties for all end-properties of this {@link FlexoConcept}
		 */
		private List<FlexoProperty<?>> accessibleProperties;
		/**
		 * Stores a cache for key properties for all end-properties of this {@link FlexoConcept}
		 */
		// Unused private List<FlexoProperty<?>> accessibleKeyProperties2;

		private ImageIcon bigIcon;
		private ImageIcon mediumIcon;
		private ImageIcon smallIcon;

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getOwner() != null) {
				return getOwner().getResourceData();
			}
			return null;
		}

		@Override
		public final boolean hasNature(FlexoConceptNature nature) {
			return nature.hasNature(this);
		}

		/**
		 * Return type representing an instance of this concept
		 * 
		 * @return
		 */
		@Override
		public FlexoConceptInstanceType getInstanceType() {
			return instanceType;
		}

		/**
		 * Return type representing this concept or a sub-concept of this concept
		 * 
		 * @return
		 */
		@Override
		public FlexoConceptType getConceptType() {
			return conceptType;
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
		 * Return the URI of this {@link FlexoConcept}<br>
		 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept#AnInnerConcept.MyBehaviour
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getContainerFlexoConcept() != null) {
				return getContainerFlexoConcept().getURI() + "#" + getName();
			}
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getURI() + "#" + getName();
			}
			return "null#" + getName();
		}

		/**
		 * Return local URI of this {@link FlexoConcept} (URI in the context of owning {@link VirtualModel}
		 */
		@Override
		public String getLocalURI() {
			if (getContainerFlexoConcept() != null) {
				return getContainerFlexoConcept().getLocalURI() + "#" + getName();
			}
			return getName();
		}

		@Override
		public void setName(String name) throws InvalidNameException {
			if (name != null) {
				// We prevent ',' so that we can use it as a delimiter in tags.
				super.setName(name.replace(",", ""));
			}
		}

		@Override
		public boolean isAbstract() {
			/*if (abstractRequired()) {
				return true;
			}*/
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
				accessibleProperties = retrieveAccessibleProperties(true);
			}
			return accessibleProperties;
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
		public List<FlexoProperty<?>> retrieveAccessibleProperties(boolean considerContainment) {

			List<FlexoProperty<?>> computedAccessibleProperties = new ArrayList<>();
			Map<String, FlexoProperty<?>> inheritedProperties = new HashMap<>();

			// First take declared properties
			computedAccessibleProperties.addAll(getDeclaredProperties());

			// Take inherited properties
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				for (FlexoProperty<?> p : parentConcept.retrieveAccessibleProperties(considerContainment)) {
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

			// Take properties obtained by containment
			if (getContainerFlexoConcept() != null && considerContainment) {
				for (FlexoProperty<?> p : getContainerFlexoConcept().retrieveAccessibleProperties(considerContainment)) {
					if (getDeclaredProperty(p.getPropertyName()) == null && inheritedProperties.get(p.getPropertyName()) == null) {
						computedAccessibleProperties.add(p);
					}
				}
			}

			VirtualModel owner = getOwner();
			if (owner != null && owner != this && considerContainment) {
				for (FlexoProperty<?> p : owner.getAccessibleProperties()) {
					if (getDeclaredProperty(p.getPropertyName()) == null) {
						// This property is inherited but not overridden
						// We check that we don't have this property yet
						if (inheritedProperties.get(p.getName()) == null) {
							inheritedProperties.put(p.getName(), p);
						}
					}
				}
			}

			computedAccessibleProperties.addAll(inheritedProperties.values());

			return computedAccessibleProperties;
		}

		/**
		 * Build and return all accessible key-properties for this {@link FlexoConcept}<br>
		 * Same semantics as for {@link #getAccessibleProperties()}
		 * 
		 * @return
		 */
		@Override
		public List<FlexoProperty<?>> getAccessibleKeyProperties() {

			// Implements a cache
			// Do not recompute accessible properties when not required

			List<FlexoProperty<?>> accessibleKeyProperties = new ArrayList<>();
			for (FlexoProperty<?> p : getAccessibleProperties()) {
				if (p.isKeyProperty()) {
					accessibleKeyProperties.add(p);
				}
			}
			return accessibleKeyProperties;
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
			aProperty.handleRequiredImports(getDeclaringCompilationUnit());
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
			if (propertyName == null) {
				return null;
			}
			for (FlexoProperty<?> p : getAccessibleProperties()) {
				if (propertyName.equals(p.getName())) {
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
			FlexoProperty<?> returned = getAccessibleProperty(modelSlotName);
			if (returned instanceof ModelSlot) {
				return (ModelSlot<?>) returned;
			}
			return null;
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

		/**
		 * Return all accessible embedded FlexoConcept (those which are declared, and those accessed through inheritance)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAccessibleEmbeddedFlexoConcepts() {

			// Implements a cache

			List<FlexoConcept> returned = new ArrayList<>();
			returned.addAll(getEmbeddedFlexoConcepts());
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				returned.addAll(parentConcept.getEmbeddedFlexoConcepts());
			}
			return returned;
		}

		/**
		 * Return FlexoConcept in the same {@link VirtualModel} declaring this concept as its container {@link FlexoConcept}
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer() {

			// Implements a cache

			List<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept flexoConcept : getDeclaringCompilationUnit().getVirtualModel().getFlexoConcepts()) {
				if (flexoConcept.getApplicableContainerFlexoConcept() == this) {
					returned.add(flexoConcept);
				}
			}
			return returned;
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
		public FlexoBehaviour getDeclaredFlexoBehaviour(String signature) {
			for (FlexoBehaviour b : getDeclaredFlexoBehaviours()) {
				if (b.getSignature().equals(signature)) {
					return b;
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
		public List<SimpleInvariant> getSimpleInvariants() {
			List<SimpleInvariant> returned = new ArrayList<>();
			for (AbstractInvariant invariant : getInvariants()) {
				if (invariant instanceof SimpleInvariant) {
					returned.add((SimpleInvariant) invariant);
				}
			}
			return returned;
		}

		@Override
		public List<IterationInvariant> getIterationInvariants() {
			List<IterationInvariant> returned = new ArrayList<>();
			for (AbstractInvariant invariant : getInvariants()) {
				if (invariant instanceof IterationInvariant) {
					returned.add((IterationInvariant) invariant);
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
		public List<FlexoBehaviour> getAccessibleFlexoBehaviours(boolean considerContainment) {

			if (getParentFlexoConcepts().size() == 0) {
				return getDeclaredFlexoBehaviours();
			}

			List<FlexoBehaviour> returned = new ArrayList<>();
			// List<FlexoBehaviour> inheritedBehaviours = new ArrayList<FlexoBehaviour>();

			returned.addAll(getDeclaredFlexoBehaviours());

			// Take behaviours obtained by containment
			if (considerContainment && getContainerFlexoConcept() != null) {
				returned.addAll(getContainerFlexoConcept().getAccessibleFlexoBehaviours(considerContainment));
			}

			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				for (FlexoBehaviour behaviour : parentConcept.getAccessibleFlexoBehaviours(considerContainment)) {
					if (!behaviour.isOverridenInContext(this)) {
						returned.add(behaviour);
					}

				}
			}

			return returned;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <ES extends FlexoBehaviour> List<ES> getAccessibleFlexoBehaviours(Class<ES> editionSchemeClass,
				boolean considerContainment) {
			List<ES> returned = new ArrayList<>();
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(considerContainment)) {
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
			return getAccessibleFlexoBehaviours(AbstractActionScheme.class, true);
		}

		@Override
		public List<CreationScheme> getAccessibleCreationSchemes() {
			return getAccessibleFlexoBehaviours(CreationScheme.class, false);
		}

		@Override
		public List<ActionScheme> getAccessibleActionSchemes() {
			return getAccessibleFlexoBehaviours(ActionScheme.class, true);
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
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(false)) {
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
			return getAccessibleFlexoBehaviours(DeletionScheme.class, false);
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
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(true)) {
				if (es instanceof ActionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasCreationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(false)) {
				if (es instanceof CreationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public CreationScheme getDefaultCreationScheme() {
			for (CreationScheme creationScheme : getCreationSchemes()) {
				if (creationScheme.isDefaultCreationScheme()) {
					return creationScheme;
				}
			}
			return null;
		}

		@Override
		public boolean hasDeletionScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(false)) {
				if (es instanceof DeletionScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasSynchronizationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(false)) {
				if (es instanceof SynchronizationScheme) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasNavigationScheme() {
			for (FlexoBehaviour es : getAccessibleFlexoBehaviours(false)) {
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
			// newDeletionScheme.setName("delete");
			newDeletionScheme.setAnonymous(true);
			newDeletionScheme.setControlGraph(getFMLModelFactory().newEmptyControlGraph());
			addToFlexoBehaviours(newDeletionScheme);

			List<FlexoProperty<?>> propertiesToDelete = new ArrayList<>();
			for (FlexoProperty<?> pr : getDeclaredProperties()) {
				if (pr.defaultBehaviourIsToBeDeleted()) {
					propertiesToDelete.add(pr);
				}
			}
			for (FlexoProperty<?> pr : propertiesToDelete) {
				if (pr instanceof PrimitiveRole) {
					AssignationAction<Object> nullifyStatement = getFMLModelFactory().newAssignationAction("null");
					nullifyStatement.setAssignation(new DataBinding<>(pr.getPropertyName()));
					newDeletionScheme.getControlGraph().sequentiallyAppend(nullifyStatement);
				}
				else {
					DeleteAction<?> deleteAction = getFMLModelFactory().newDeleteAction();
					deleteAction.setObject(new DataBinding<>(pr.getPropertyName()));
					newDeletionScheme.getControlGraph().sequentiallyAppend(deleteAction);
				}
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
			return "FlexoConcept:" + getName() + "[" + Integer.toHexString(hashCode()) + "]";
		}

		@Override
		public void finalizeDeserialization() {
			// createBindingModel();
			for (FlexoBehaviour es : getFlexoBehaviours()) {
				es.finalizeDeserialization();
			}
			for (FlexoProperty<?> pr : getDeclaredProperties()) {
				pr.finalizeDeserialization();
			}
			decodeParentFlexoConceptList(true);
			super.finalizeDeserialization();
		}

		public void debug() {
			System.out.println(getStringRepresentation());
		}

		/*@Deprecated
		public void save() {
			try {
				getOwningVirtualModel().getResource().save();
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
		}*/

		@Override
		public FlexoConceptBindingModel getBindingModel() {
			if (bindingModel == null) {
				// createBindingModel();
				bindingModel = new FlexoConceptBindingModel(this);
				getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, null, bindingModel);
			}
			return bindingModel;
		}

		@Override
		public boolean isSuperConceptOfContainerVirtualModel() {
			if (getParentFlexoConcepts().size() == 0) {
				return true;
			}
			for (FlexoConcept flexoConcept : getParentFlexoConcepts()) {
				if (flexoConcept.getOwner() == getOwner()) {
					return false;
				}
			}
			return true;
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
			FlexoConcept oldApplicableContainerFlexoConcept = getApplicableContainerFlexoConcept();
			performSuperSetter(CONTAINER_FLEXO_CONCEPT_KEY, aConcept);
			clearAccessiblePropertiesCache();
			VirtualModel owningVirtualModel = getOwningVirtualModel();
			if (owningVirtualModel != null) {
				owningVirtualModel.getInnerConceptsFacet().notifiedConceptsChanged();
				owningVirtualModel.getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						owningVirtualModel.getAllRootFlexoConcepts());
			}
			getPropertyChangeSupport().firePropertyChange(APPLICABLE_CONTAINER_FLEXO_CONCEPT_KEY, oldApplicableContainerFlexoConcept,
					getApplicableContainerFlexoConcept());
		}

		@Override
		public void addToEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperAdder(EMBEDDED_FLEXO_CONCEPT_KEY, aFlexoConcept);
			getInnerConceptsFacet().notifiedConceptsChanged();
			VirtualModel owningVirtualModel = getOwningVirtualModel();
			if (owningVirtualModel != null) {
				owningVirtualModel.getInnerConceptsFacet().notifiedConceptsChanged();
				owningVirtualModel.getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						owningVirtualModel.getAllRootFlexoConcepts());
			}
		}

		@Override
		public void removeFromEmbeddedFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperRemover(EMBEDDED_FLEXO_CONCEPT_KEY, aFlexoConcept);
			getInnerConceptsFacet().notifiedConceptsChanged();
			VirtualModel owningVirtualModel = getOwningVirtualModel();
			if (owningVirtualModel != null) {
				owningVirtualModel.getInnerConceptsFacet().notifiedConceptsChanged();
				owningVirtualModel.getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null,
						owningVirtualModel.getAllRootFlexoConcepts());
			}
		}

		// Used for serialization, do not use as API
		@Override
		@FMLMigration
		public String _getParentFlexoConceptsList() {
			if (parentFlexoConceptList == null && getParentFlexoConcepts().size() > 0) {
				parentFlexoConceptList = computeParentFlexoConceptList();
			}
			return parentFlexoConceptList;
		}

		@FMLMigration
		private boolean parentFlexoConceptListWasExplicitelySet = false;

		// Used for serialization, do not use as API
		@Override
		@FMLMigration
		public void _setParentFlexoConceptsList(String parentFlexoConceptList) {
			this.parentFlexoConceptList = parentFlexoConceptList;
			parentFlexoConceptListWasExplicitelySet = true;
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

		@Override
		public String getParentFlexoConceptsDeclaration() {
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (FlexoConcept parent : getParentFlexoConcepts()) {
				sb.append((isFirst ? "" : ",") + parent.getName());
				isFirst = false;
			}
			return sb.toString();
		}

		private boolean isDecodingParentFlexoConceptList = false;

		// Testing XML->FML migration
		@Deprecated
		@FMLMigration
		public static boolean PREVENT_PARENT_CONCEPTS_DECODING = false;

		@FMLMigration
		private void decodeParentFlexoConceptList(boolean loadWhenRequired) {
			if (parentFlexoConceptListWasExplicitelySet && getVirtualModelLibrary() != null && !isDecodingParentFlexoConceptList
					&& !PREVENT_PARENT_CONCEPTS_DECODING) {
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
							e.printStackTrace();
						}
					}
					parentFlexoConceptList = null;
				}
				else {
					// Some concepts are not decoded yet, we don't do anything
					System.out.println("Bizarre, on a pas reussi a decoder tous les concepts " + parentFlexoConceptList);
				}
				isDecodingParentFlexoConceptList = false;
				parentFlexoConceptListWasExplicitelySet = false;
			}
		}

		private List<FlexoConcept> parentFlexoConcepts = new ArrayList<>();

		@Override
		public List<FlexoConcept> getParentFlexoConcepts() {
			if (parentFlexoConceptListWasExplicitelySet && getVirtualModelLibrary() != null) {
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
				FlexoConcept oldApplicableContainerFlexoConcept = getApplicableContainerFlexoConcept();
				parentFlexoConcepts.add(parentFlexoConcept);
				parentFlexoConcept.addToChildFlexoConcepts(this);
				// Listen to the new parent FlexoConcept
				parentFlexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
				getPropertyChangeSupport().firePropertyChange("parentFlexoConcepts", null, parentFlexoConcept);
				notifyParentConceptsHaveChanged();
				getPropertyChangeSupport().firePropertyChange(APPLICABLE_CONTAINER_FLEXO_CONCEPT_KEY, oldApplicableContainerFlexoConcept,
						getApplicableContainerFlexoConcept());
			}
			else {
				throw new InconsistentFlexoConceptHierarchyException(
						"FlexoConcept " + this + " : Could not add as parent FlexoConcept: " + parentFlexoConcept);
			}
		}

		@Override
		public void removeFromParentFlexoConcepts(FlexoConcept parentFlexoConcept) {
			// Stop listen to new parent FlexoConcept
			FlexoConcept oldApplicableContainerFlexoConcept = getApplicableContainerFlexoConcept();
			parentFlexoConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
			parentFlexoConcepts.remove(parentFlexoConcept);
			parentFlexoConcept.removeFromChildFlexoConcepts(this);
			getPropertyChangeSupport().firePropertyChange("parentFlexoConcepts", parentFlexoConcept, null);
			notifyParentConceptsHaveChanged();
			getPropertyChangeSupport().firePropertyChange(APPLICABLE_CONTAINER_FLEXO_CONCEPT_KEY, oldApplicableContainerFlexoConcept,
					getApplicableContainerFlexoConcept());
		}

		// Called when the parent concept hierarchy has changed
		private void notifyParentConceptsHaveChanged() {
			parentFlexoConceptList = null;
			accessibleProperties = null;
			VirtualModel owningVirtualModel = getOwningVirtualModel();
			if (owningVirtualModel != null) {
				owningVirtualModel.getInnerConceptsFacet().notifiedConceptsChanged();
				owningVirtualModel.getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", null,
						owningVirtualModel.getAllSuperFlexoConcepts());
			}
			getPropertyChangeSupport().firePropertyChange(ACCESSIBLE_PROPERTIES_KEY, false, true);
			getPropertyChangeSupport().firePropertyChange(ACCESSIBLE_BEHAVIOURS_KEY, false, true);
			setIsModified();

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
		public List<FlexoConcept> getTopLevelSuperConcepts() {
			if (getParentFlexoConcepts().size() == 0) {
				return Collections.singletonList(this);
			}
			List<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				List<FlexoConcept> l = parentConcept.getTopLevelSuperConcepts();
				for (FlexoConcept concept : l) {
					if (!returned.contains(concept)) {
						returned.add(concept);
					}
				}
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

		/**
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link VirtualModel}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link VirtualModel} enclosing, we must provide this hook to give a chance
		 * to objects that rely on ViewPoint instantiation context to update their bindings (some bindings might becomes valid)<br>
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

		/**
		 * Search and return {@link FlexoConcept} with supplied local name, given the context of this {@link FlexoConcept}<br>
		 * 
		 * Lookup algorithm follows:
		 * <ul>
		 * <li>If name matches declared {@link FlexoConcept} return this {@link FlexoConcept}</li>
		 * <li>If name matches any container {@link VirtualModel} or {@link FlexoConcept} (recursively from current to container), return
		 * related {@link FlexoConcept}</li>
		 * <li>If name matches any parent {@link FlexoConcept} (inheritance semantics), return related {@link VirtualModel}</li>
		 * <li>If name matches any contained {@link FlexoConcept}, return related {@link FlexoConcept}/li>
		 * </ul>
		 * 
		 * @param conceptName
		 * @return
		 */
		@Override
		public final FlexoConcept lookupFlexoConceptWithName(String conceptName) {
			return lookupFlexoConceptWithName(conceptName, new HashSet<>());
		}

		protected FlexoConcept lookupFlexoConceptWithName(String conceptName, Set<FlexoConcept> visited) {
			if (visited.contains(this)) {
				return null;
			}
			visited.add(this);
			if (StringUtils.isEmpty(conceptName)) {
				return null;
			}
			if (getName().equals(conceptName)) {
				return this;
			}
			if (getOwner() != null) {
				FlexoConcept returned = ((FlexoConceptImpl) getOwner()).lookupFlexoConceptWithName(conceptName, visited);
				if (returned != null) {
					return returned;
				}
			}
			if (getContainerFlexoConcept() != null) {
				FlexoConcept returned = ((FlexoConceptImpl) getContainerFlexoConcept()).lookupFlexoConceptWithName(conceptName, visited);
				if (returned != null) {
					return returned;
				}
			}
			for (FlexoConcept parentConcept : getParentFlexoConcepts()) {
				FlexoConcept returned = ((FlexoConceptImpl) parentConcept).lookupFlexoConceptWithName(conceptName, visited);
				if (returned != null) {
					return returned;
				}
			}
			for (FlexoConcept embeddedConcept : getEmbeddedFlexoConcepts()) {
				FlexoConcept returned = ((FlexoConceptImpl) embeddedConcept).lookupFlexoConceptWithName(conceptName, visited);
				if (returned != null) {
					return returned;
				}
			}
			return null;
		}

		private List<Validable> embeddedValidable = null;

		@Override
		public Collection<Validable> getEmbeddedValidableObjects() {
			if (embeddedValidable == null) {
				embeddedValidable = new ArrayList<>();
				embeddedValidable.add(getStructuralFacet());
				embeddedValidable.add(getBehaviouralFacet());
				embeddedValidable.add(getInnerConceptsFacet());
				embeddedValidable.add(getInspector());
			}
			return embeddedValidable;
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public File getBigIconFile() {
			if (getBigIconResource() instanceof FileResourceImpl) {
				return ((FileResourceImpl) getBigIconResource()).getFile();
			}
			return null;
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public void setBigIconFile(File file) throws MalformedURLException, LocatorNotFoundException {

			this.setBigIconResource(new FileResourceImpl(file));
			bigIcon = null;
			getPropertyChangeSupport().firePropertyChange("bigIcon", null, getBigIcon());
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public File getMediumIconFile() {
			if (getMediumIconResource() instanceof FileResourceImpl) {
				return ((FileResourceImpl) getMediumIconResource()).getFile();
			}
			return null;
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public void setMediumIconFile(File file) throws MalformedURLException, LocatorNotFoundException {

			this.setMediumIconResource(new FileResourceImpl(file));
			mediumIcon = null;
			getPropertyChangeSupport().firePropertyChange("mediumIcon", null, getMediumIcon());
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public File getSmallIconFile() {
			if (getSmallIconResource() instanceof FileResourceImpl) {
				return ((FileResourceImpl) getSmallIconResource()).getFile();
			}
			return null;
		}

		// TODO : this is a Workaround for Fib File selector...It has to be fixed in a more efficient way
		@Override
		public void setSmallIconFile(File file) throws MalformedURLException, LocatorNotFoundException {

			this.setSmallIconResource(new FileResourceImpl(file));
			smallIcon = null;
			getPropertyChangeSupport().firePropertyChange("smallIcon", null, getSmallIcon());
		}

		@Override
		public ImageIcon getBigIcon() {
			if (bigIcon == null && getBigIconResource() != null && getBigIconResource().exists()) {
				bigIcon = new ImageIcon(ImageUtils.loadImageFromResource(getBigIconResource()));
			}
			return bigIcon;
		}

		@Override
		public ImageIcon getMediumIcon() {
			if (mediumIcon == null && getMediumIconResource() != null && getMediumIconResource().exists()) {
				mediumIcon = new ImageIcon(ImageUtils.loadImageFromResource(getMediumIconResource()));
			}
			return mediumIcon;
		}

		@Override
		public ImageIcon getSmallIcon() {
			if (smallIcon == null && getSmallIconResource() != null && getSmallIconResource().exists()) {
				smallIcon = new ImageIcon(ImageUtils.loadImageFromResource(getSmallIconResource()));
			}
			return smallIcon;
		}

		/**
		 * Return boolean indicating if this concept defines a delegated inspector
		 * 
		 * @return
		 */
		@Override
		public boolean hasDelegatedInspector() {
			if (getInspector() != null && getInspector().getDelegateConceptInstance() != null
					&& getInspector().getDelegateConceptInstance().isSet() && getInspector().getDelegateConceptInstance().isSet()) {
				return true;
			}
			return false;
		}

		/**
		 * Return applicable inspector, while returning delegate inspector when relevant, or current concept inspector
		 * 
		 * @return
		 */
		@Override
		public FlexoConceptInspector getApplicableInspector() {
			if (hasDelegatedInspector()) {
				Type analyzedType = getInspector().getDelegateConceptInstance().getAnalyzedType();
				if (analyzedType instanceof FlexoConceptInstanceType) {
					return ((FlexoConceptInstanceType) analyzedType).getFlexoConcept().getApplicableInspector();
				}
			}
			return getInspector();
		}

		/**
		 * Return applicable renderer, exploiting concept hierarchy and finding most specialized one
		 * 
		 * @return
		 */
		@Override
		public DataBinding<String> getApplicableRenderer() {
			if (getInspector() != null && getInspector().getRenderer() != null && getInspector().getRenderer().isSet()
					&& getInspector().getRenderer().isValid()) {
				return getInspector().getRenderer();
			}
			else if (getParentFlexoConcepts().size() > 0) {
				List<FlexoConcept> parentConceptsWithARenderer = new ArrayList<>();
				for (FlexoConcept parent : getParentFlexoConcepts()) {
					if (parent.getApplicableRenderer() != null) {
						parentConceptsWithARenderer.add(parent);
					}
				}
				if (parentConceptsWithARenderer.size() > 0) {
					return FMLUtils.getMostSpecializedConcept(parentConceptsWithARenderer).getApplicableRenderer();
				}
			}
			return null;
		}

		@Override
		public String getPresentationName() {
			if (getApplicableInspector() != null && StringUtils.isNotEmpty(getApplicableInspector().getInspectorTitle())) {
				return getApplicableInspector().getInspectorTitle();
			}
			else {
				return getName();
			}

		}

		/**
		 * Return applicable container {@link FlexoConcept} (relative to containment). When no explicit declaration was defined for this
		 * concept, this containment is inherited from its parent concepts
		 */
		@Override
		public FlexoConcept getApplicableContainerFlexoConcept() {
			if (getContainerFlexoConcept() != null) {
				return getContainerFlexoConcept();
			}
			return getApplicableContainerFlexoConceptInheritedFromParents();
		}

		private FlexoConcept getApplicableContainerFlexoConceptInheritedFromParents() {
			List<FlexoConcept> parentContainers = new ArrayList<>();
			for (FlexoConcept parent : getParentFlexoConcepts()) {
				if (parent.getApplicableContainerFlexoConcept() != null) {
					parentContainers.add(parent.getApplicableContainerFlexoConcept());
				}
			}
			if (parentContainers.size() > 0) {
				return FMLUtils.getMostSpecializedConcept(parentContainers);
			}
			return null;
		}

		/**
		 * Sets applicable container {@link FlexoConcept} (relative to containment).
		 * 
		 * 
		 * @param name
		 */
		@Override
		public void setApplicableContainerFlexoConcept(FlexoConcept concept) {
			if (getApplicableContainerFlexoConcept() != concept) {
				if (concept == getApplicableContainerFlexoConceptInheritedFromParents()) {
					// No need to redefine
					setContainerFlexoConcept(null);
				}
				else {
					setContainerFlexoConcept(concept);
				}
				notifyContainerChanges();
			}
		}

		private void notifyContainerChanges() {
			getPropertyChangeSupport().firePropertyChange("applicableContainerFlexoConcept", false, true);
			getPropertyChangeSupport().firePropertyChange("allEmbeddedFlexoConceptsDeclaringThisConceptAsContainer", false, true);
		}

		@Override
		public String getAuthor() {
			String returned = getSingleMetaData("Author", String.class);
			if (returned != null) {
				return returned;
			}
			if (getOwner() != null) {
				return getOwner().getAuthor();
			}
			return null;
		}

		@Override
		public void setAuthor(String author) {
			if (isDeserializing() || requireChange(getAuthor(), author)) {
				setSingleMetaData("Author", author, String.class);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.ACCESSIBLE_PROPERTIES_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.ACCESSIBLE_BEHAVIOURS_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.CONTAINER_FLEXO_CONCEPT_KEY)) {
				notifyParentConceptsHaveChanged();
			}
		}

	}

	@DefineValidationRule
	public static class ContainerFlexoConceptMustBeConsistentWithParents
			extends ValidationRule<ContainerFlexoConceptMustBeConsistentWithParents, FlexoConcept> {
		public ContainerFlexoConceptMustBeConsistentWithParents() {
			super(FlexoConcept.class, "container_flexo_concept_must_be_consistent_with_parents");
		}

		@Override
		public ValidationIssue<ContainerFlexoConceptMustBeConsistentWithParents, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (flexoConcept.getContainerFlexoConcept() != null && flexoConcept.getParentFlexoConcepts().size() > 0) {
				FlexoConcept applicableParent = ((FlexoConceptImpl) flexoConcept).getApplicableContainerFlexoConceptInheritedFromParents();
				if (applicableParent != null && !applicableParent.isSuperConceptOf(flexoConcept.getContainerFlexoConcept())) {
					return new ValidationError<>(this, flexoConcept, "container_declaration_is_not_compatible_with_parents_definition");
				}
			}
			return null;
		}

		/*@Override
		public void setAbstract(boolean isAbstract) {
			System.out.println("Hop, le concept " + getName() + " abstract=" + isAbstract);
			performSuperSetter(IS_ABSTRACT_KEY, isAbstract);
			System.out.println("FML: " + getFMLPrettyPrint());
		}
		
		@Override
		public void setVisibility(Visibility visibility) {
			System.out.println("Hop, le concept " + getName() + " visibility=" + visibility);
			performSuperSetter(VISIBILITY_KEY, visibility);
			System.out.println("FML: " + getFMLPrettyPrint());
		}*/
	}

	@DefineValidationRule
	public static class NonAbstractFlexoConceptMustImplementAllPropertiesAndbehaviours
			extends ValidationRule<NonAbstractFlexoConceptMustImplementAllPropertiesAndbehaviours, FlexoConcept> {
		public NonAbstractFlexoConceptMustImplementAllPropertiesAndbehaviours() {
			super(FlexoConcept.class, "non_abstract_flexo_concept_must_implement_all_properties_and_behaviours");
		}

		@Override
		public String getFragmentContext() {
			return FragmentContext.NAME.name();
		}

		@Override
		public ValidationIssue<NonAbstractFlexoConceptMustImplementAllPropertiesAndbehaviours, FlexoConcept> applyValidation(
				FlexoConcept flexoConcept) {
			if (!(flexoConcept.isAbstract()) && flexoConcept.abstractRequired()) {
				return new ValidationError<>(this, flexoConcept,
						"non_abstract_flexo_concept_does_not_implement_all_properties_and_behaviours");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class NonAbstractFlexoConceptShouldHaveProperties
			extends ValidationRule<NonAbstractFlexoConceptShouldHaveProperties, FlexoConcept> {
		public NonAbstractFlexoConceptShouldHaveProperties() {
			super(FlexoConcept.class, "non_abstract_flexo_concept_should_have_properties");
		}

		@Override
		public String getFragmentContext() {
			return FragmentContext.NAME.name();
		}

		@Override
		public ValidationIssue<NonAbstractFlexoConceptShouldHaveProperties, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!(flexoConcept.isAbstract()) && !(flexoConcept instanceof VirtualModel) && !(flexoConcept instanceof FlexoEvent)
					&& !(flexoConcept instanceof FlexoEnum) && !(flexoConcept instanceof FlexoEnumValue)
					&& flexoConcept.getDeclaredProperties().size() == 0) {
				return new ValidationWarning<>(this, flexoConcept, "non_abstract_flexo_concept_does_not_define_any_property");
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
		public String getFragmentContext() {
			return FragmentContext.NAME.name();
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveFlexoBehaviours, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!flexoConcept.isAbstract() && !(flexoConcept instanceof FlexoEnum) && flexoConcept.getFlexoBehaviours().size() == 0) {
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
		public String getFragmentContext() {
			return FragmentContext.NAME.name();
		}

		@Override
		public ValidationIssue<FlexoConceptShouldHaveDeletionScheme, FlexoConcept> applyValidation(FlexoConcept flexoConcept) {
			if (!flexoConcept.isAbstract() && !(flexoConcept instanceof FlexoEnum) && flexoConcept.getDeletionSchemes().size() == 0) {
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
				// TODO : FD4SG was the only call to a deprecated method (added the null third parameter)
				CreateFlexoBehaviour action = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, null);
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
