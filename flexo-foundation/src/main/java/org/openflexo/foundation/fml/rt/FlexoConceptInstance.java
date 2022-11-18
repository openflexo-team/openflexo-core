/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.rt;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.BindingPathChangeListener;
import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.IterationInvariant;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.fml.binding.SetValueBindingVariable;
import org.openflexo.foundation.fml.binding.SuperBindingVariable;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;
import org.openflexo.foundation.fml.utils.FMLMultipleParametersBindingEvaluator;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.DeserializationFinalizer;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.CompoundIssue;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoConceptInstance} is the run-time concept (instance) of an {@link FlexoConcept}.<br>
 * 
 * As such, a {@link FlexoConceptInstance} is instantiated inside a {@link FMLRTVirtualModelInstance} (only
 * {@link FMLRTVirtualModelInstance} objects might leave outside an other {@link FMLRTVirtualModelInstance}).<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoConceptInstance.FlexoConceptInstanceImpl.class)
@XMLElement
@Imports({ @Import(FlexoEventInstance.class) })
// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
// This inheritance should disappear
public interface FlexoConceptInstance extends VirtualModelInstanceObject, Bindable, RunTimeEvaluationContext {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String EMPTY_STRING = "<null>";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_URI_KEY = "flexoConceptURI";

	String FLEXO_CONCEPT_KEY = "flexoConcept";

	@PropertyIdentifier(type = FlexoConceptInstance.class)
	public static final String CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY = "containerFlexoConceptInstance";
	@PropertyIdentifier(type = List.class)
	public static final String EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY = "embeddedFlexoConceptInstances";

	@PropertyIdentifier(type = ActorReference.class, cardinality = Cardinality.LIST)
	public static final String ACTORS_KEY = "actors";

	// @PropertyIdentifier(type = List.class)
	// public static final String MODEL_SLOT_INSTANCES_KEY = "modelSlotInstances";

	@PropertyIdentifier(type = FMLRTVirtualModelInstance.class)
	public static final String OWNING_VIRTUAL_MODEL_INSTANCE_KEY = "owningVirtualModelInstance";

	/**
	 * Return the {@link FMLRTVirtualModelInstance} where this FlexoConceptInstance is instantiated (result might be different from
	 * {@link #getVirtualModelInstance()}, which is The {@link VirtualModelInstanceObject} API)
	 * 
	 * @return
	 */
	@Getter(value = OWNING_VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract VirtualModelInstance<?, ?> getOwningVirtualModelInstance();

	@Setter(OWNING_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setOwningVirtualModelInstance(VirtualModelInstance<?, ?> virtualModelInstance);

	@Getter(FLEXO_CONCEPT_KEY)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = FLEXO_CONCEPT_URI_KEY)
	@XMLAttribute
	public String getFlexoConceptURI();

	@Setter(FLEXO_CONCEPT_URI_KEY)
	public void setFlexoConceptURI(String flexoConceptURI);

	@Getter(value = CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY, inverse = EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConceptInstance getContainerFlexoConceptInstance();

	@Setter(CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY)
	public void setContainerFlexoConceptInstance(FlexoConceptInstance container);

	/**
	 * Return a newly created list of all embedded {@link FlexoConceptInstance} conform to the supplied FlexoConcept
	 * 
	 * @param flexoConcept
	 * @return
	 */
	public List<FlexoConceptInstance> getEmbeddedFlexoConceptInstances(FlexoConcept flexoConcept);

	/**
	 * Return all {@link FlexoConcept} contained in this {@link FlexoConcept}
	 * 
	 * @return
	 */
	@Getter(value = EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY, cardinality = Cardinality.LIST, inverse = CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY)
	@XMLElement(context = "Embedded", primary = true)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoConceptInstance> getEmbeddedFlexoConceptInstances();

	@Setter(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY)
	public void setEmbeddedFlexoConceptInstances(List<FlexoConceptInstance> flexoConcepts);

	@Adder(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY)
	@PastingPoint(priority = 1)
	public void addToEmbeddedFlexoConceptInstances(FlexoConceptInstance aFlexoConcept);

	@Remover(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY)
	public void removeFromEmbeddedFlexoConceptInstances(FlexoConceptInstance aFlexoConcept);

	/**
	 * Return boolean indicating whether this concept instance has a FlexoConceptInstance for container (containment semantics)<br>
	 * 
	 * @return
	 */
	public boolean isRoot();

	@Getter(value = ACTORS_KEY, cardinality = Cardinality.LIST, inverse = ActorReference.FLEXO_CONCEPT_INSTANCE_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ActorReference<?>> getActors();

	@Setter(ACTORS_KEY)
	public void setActors(List<ActorReference<?>> actors);

	@Adder(ACTORS_KEY)
	public void addToActors(ActorReference<?> anActorReference);

	@Remover(ACTORS_KEY)
	public void removeFromActors(ActorReference<?> anActorReference);

	public List<ModelSlotInstance<?, ?>> getModelSlotInstances();

	// Debug method
	public String debug();

	/**
	 * Compute value associated with supplied property
	 * 
	 * @param flexoProperty
	 *            the property to lookup
	 */
	public <T> T getFlexoPropertyValue(FlexoProperty<T> flexoProperty);

	/**
	 * Sets value associated with supplied property
	 * 
	 * @param flexoProperty
	 *            the property to lookup
	 * @param value
	 *            the new value to set
	 */
	public <T> void setFlexoPropertyValue(FlexoProperty<T> flexoProperty, T value);

	/**
	 * Return property value associated with supplied property name
	 * 
	 * @param propertyName
	 *            the property to lookup
	 */
	public <T> T getFlexoPropertyValue(String propertyName);

	/**
	 * Sets value associated with supplied property
	 * 
	 * @param propertyName
	 *            name of property to use
	 * @param value
	 *            the new value to set
	 */
	public <T> void setFlexoPropertyValue(String propertyName, T value);

	/**
	 * Return actor associated with supplied role, asserting cardinality of supplied property is SINGLE.<br>
	 * If cardinality of supplied property is MULTIPLE, return first found value
	 * 
	 * @param flexoRole
	 *            the role to lookup
	 */
	public <T> T getFlexoActor(FlexoRole<T> flexoRole);

	/**
	 * Return actor associated with supplied role name, asserting cardinality of supplied property is SINGLE.<br>
	 * If cardinality of supplied property is MULTIPLE, return first found value
	 * 
	 * @param flexoPropertyName
	 *            the property to lookup
	 */
	public <T> T getFlexoActor(String flexoRoleName);

	/**
	 * Return actor list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
	 * If cardinality of supplied property is SINGLE, return a singleton list<br>
	 * If no value are defined for this property, return an empty list
	 * 
	 * @param flexoProperty
	 *            the property to lookup
	 */
	public <T> List<T> getFlexoActorList(FlexoRole<T> flexoRole);

	/**
	 * Return actor list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
	 * If cardinality of supplied property is SINGLE, return a singleton list<br>
	 * If no value are defined for this property, return an empty list
	 * 
	 * @param flexoPropertyName
	 *            the property to lookup
	 */
	public <T> List<T> getFlexoActorList(String flexoRoleName);

	/**
	 * Return actor associated with supplied property, asserting cardinality of supplied property is SINGLE.<br>
	 * If cardinality of supplied property is MULTIPLE, replace all existing value with supplied object. If no value is found, add supplied
	 * object.
	 * 
	 * @param object
	 *            the object to be registered as actor for supplied property
	 * @param flexoProperty
	 *            the property to be considered
	 */
	public <T> void setFlexoActor(T object, FlexoRole<T> flexoRole);

	/**
	 * Add actor to the list of reference associated with supplied role, asserting cardinality of supplied role is MULTIPLE.<br>
	 * If cardinality of supplied property is SINGLE, replace all existing value with supplied object.
	 * 
	 * @param object
	 *            the object to be registered as new actor for supplied property
	 * @param flexoProperty
	 *            the property to be considered
	 */
	public <T> void addToFlexoActors(T object, FlexoRole<T> flexoRole);

	/**
	 * Remove actor from the list of reference associated with supplied role, asserting cardinality of supplied role is MULTIPLE.<br>
	 * If cardinality of supplied property is SINGLE, remove existing matching value
	 * 
	 * @param object
	 *            the object to be removed from supplied property
	 * @param flexoProperty
	 *            the property to be considered
	 */
	public <T> void removeFromFlexoActors(T object, FlexoRole<T> flexoRole);

	/**
	 * Clear all actors associated with supplied property
	 * 
	 * @param flexoProperty
	 */
	public <T> void nullifyFlexoActor(FlexoRole<T> flexoRole);

	/**
	 * Return boolean indicating if supplied object is already registered as an actor for supplied {@link FlexoRole}
	 * 
	 * @param object
	 * @param flexoRole
	 * @return
	 */
	public <T> boolean hasFlexoActor(T object, FlexoRole<T> flexoRole);

	/**
	 * Return {@link ActorReference} referencing supplied object if this one is already registered as an actor for supplied
	 * {@link FlexoRole}<br>
	 * When not return null
	 * 
	 * @param object
	 * @param flexoRole
	 * @return
	 */
	public <T> ActorReference<T> getActorReference(T object, FlexoRole<T> flexoRole);

	public <T> FlexoProperty<T> getPropertyForActor(T actor);

	/**
	 * Return {@link ActorReference} object associated with supplied property, asserting cardinality of supplied property is SINGLE.<br>
	 * If cardinality of supplied property is MULTIPLE, return first found value
	 * 
	 * @param flexoProperty
	 *            the property to lookup
	 */
	public <T> ActorReference<T> getActorReference(FlexoRole<T> flexoRole);

	/**
	 * Return {@link ActorReference} list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
	 * If cardinality of supplied property is SINGLE, return a singleton list<br>
	 * If no value are defined for this property, return an empty list
	 * 
	 * @param flexoProperty
	 *            the property to lookup
	 */
	public <T> List<ActorReference<T>> getActorReferenceList(FlexoRole<T> flexoRole);

	public String getStringRepresentation();

	public String getStringRepresentationWithID();

	public boolean hasValidRenderer();

	@DeserializationInitializer
	public void initializeDeserialization(AbstractVirtualModelInstanceModelFactory<?> factory);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public boolean hasNature(FlexoConceptInstanceNature nature);

	/**
	 * Return {@link ModelSlotInstance} concretizing supplied modelSlot, asserting cardinality is single
	 * 
	 * @param modelSlot
	 *            a model slot with single cardinality
	 * @return
	 */
	@Deprecated
	public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
			MS modelSlot);

	/**
	 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name, asserting cardinality is single
	 * 
	 * @param modelSlotName
	 *            name of a model slot with single cardinality
	 * @return
	 */
	@Deprecated
	public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
			String modelSlotName);

	/**
	 * Return boolean indicating if type of this FlexoConceptInstance has the supplied name
	 * 
	 * @param conceptName
	 * @return
	 */
	public boolean isOf(String conceptName);

	/**
	 * Use the current FlexoConceptInstance as the run-time context of an expression supplied<br>
	 * as a String expressed in FML language, and execute this expression in that run-time context<br>
	 * Return the value computed from the expression in that run-time context.
	 * 
	 * Note that 'this' is implicitly used and replaces first path element when missing
	 * 
	 * TODO: add FlexoEditor parameter instead of using {@link #getEditor()}
	 * 
	 * @param expression
	 * @return
	 * @throws TypeMismatchException
	 * @throws NullReferenceException
	 * @throws InvocationTargetException
	 */
	public <T> T execute(String expression)
			throws TypeMismatchException, NullReferenceException, ReflectiveOperationException, InvalidBindingException;

	/**
	 * Use the current FlexoConceptInstance as the run-time context of an expression supplied<br>
	 * as a String expressed in FML language, and a set of arguments given in appearing order in the expression<br>
	 * Execute this expression in that run-time context and return the value computed from the expression in that run-time context.
	 * 
	 * Note that 'this' is implicitly used and replaces first path element when missing
	 * 
	 * Syntax is this:
	 * 
	 * <pre>
	 * {$variable1}+' '+{$variable2}+' !'"
	 * </pre>
	 * 
	 * for an expression with the two variables variable1 and variable2
	 * 
	 * TODO: add FlexoEditor parameter instead of using {@link #getEditor()}
	 * 
	 * @param expression
	 * @param parameters
	 * @return value as computed from expression
	 * @throws TypeMismatchException
	 * @throws NullReferenceException
	 * @throws InvocationTargetException
	 */
	public <T> T execute(String expression, Object... parameters)
			throws TypeMismatchException, NullReferenceException, ReflectiveOperationException, InvalidBindingException;

	/**
	 * Instantiate run-time-level object encoding reference to this {@link FlexoConceptInstance} object
	 * 
	 * @param role
	 *            the {@link FlexoConceptInstanceRole} defining access to supplied object
	 * @param fci
	 *            the {@link FlexoConceptInstance} where this reference should be built
	 * 
	 */
	public ActorReference<? extends FlexoConceptInstance> makeActorReference(FlexoConceptInstanceRole role, FlexoConceptInstance fci);

	/**
	 * Delete this {@link FlexoConceptInstance} while calling super delete and removing FCI from container and/or owning
	 * {@link VirtualModelInstance}
	 * 
	 * @return
	 */
	public boolean performCoreDeletion();

	/**
	 * Delete this FlexoConcept instance using supplied DeletionScheme
	 */
	public boolean deleteWithScheme(DeletionScheme deletionScheme, RunTimeEvaluationContext evaluationContext);

	/**
	 * Return a identifier for this FlexoConceptInstance under the form 'ConceptName'+ID
	 * 
	 * @return
	 */
	public String getUserFriendlyIdentifier();

	/**
	 * Clone this {@link FlexoConceptInstance} given supplied factory.<br>
	 * Clone is computed using roles, where property values are kept references<br>
	 * Embedded {@link FlexoConceptInstance} are cloned using same semantics
	 * 
	 * @param factory
	 * @return
	 */
	public FlexoConceptInstance cloneUsingRoles(AbstractVirtualModelInstanceModelFactory<?> factory);

	/**
	 * An {@link #equals(Object)} implementation for {@link FlexoConceptInstance}, focused on roles
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equalsUsingRoles(Object obj);

	/**
	 * A {@link #hashCode()} implementation for {@link FlexoConceptInstance}, focused on roles
	 * 
	 * @return
	 */
	public int hashCodeUsingRoles();

	/**
	 * Return applicable inspected object, which is the delegated object in related concept has delegated inspector, otherwise this
	 */
	public FlexoConceptInstance getInspectedObject();

	public static abstract class FlexoConceptInstanceImpl extends VirtualModelInstanceObjectImpl implements FlexoConceptInstance {

		private static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstance.class.getPackage().toString());

		protected FlexoConcept flexoConcept;
		protected String flexoConceptURI;
		// This HashMap stores List of ActorReference associated with property name
		// Take care that for roles which cardinality is single are also implemented with singleton list
		// We don't use here the FlexoProperty as key but a String because this causes some issues during deserialization
		// (when FlexoConcept is not yet known)
		private final HashMap<String, List<ActorReference<?>>> actors;

		private final HashMap<FlexoRole<?>, List<?>> actorLists;

		// Stores internal variables used during execution
		private final HashMap<String, Object> variables;

		/**
		 * Default constructor
		 */
		public FlexoConceptInstanceImpl(/*FMLRTVirtualModelInstance virtualModelInstance*/) {
			super();
			actors = new HashMap<>();
			actorLists = new HashMap<>();
			variables = new HashMap<>();
		}

		@Override
		public ExpressionEvaluator getEvaluator() {
			return new FMLExpressionEvaluator(this);
		}

		/**
		 * Implements {@link #getFocusedObject()} of {@link RunTimeEvaluationContext} : local evaluation context is the
		 * {@link FlexoConceptInstance} itself
		 */
		@Override
		public FlexoObject getFocusedObject() {
			return this;
		}

		// TODO: this is not a good idea, we should separate FlexoConceptInstance from RunTimeEvaluationContext
		private FlexoEditor getFlexoEditor() {
			if (getResourceCenter() != null && getResourceCenter() instanceof FlexoProject && getServiceManager() != null) {
				return getServiceManager().getProjectLoaderService().getEditorForProject((FlexoProject<?>) getResourceCenter());
			}
			else if (getResourceCenter() != null && getResourceCenter().getDelegatingProjectResource() != null) {
				return getServiceManager().getProjectLoaderService()
						.getEditorForProject(getResourceCenter().getDelegatingProjectResource().getFlexoProject());
			}
			return null;
		}

		@Override
		public FlexoEditor getEditor() {
			return getFlexoEditor();
		}

		@Override
		public FMLRunTimeEngine getFMLRunTimeEngine() {
			if (getEditor() != null) {
				return getEditor().getFMLRunTimeEngine();
			}
			return null;
		}

		@Override
		public boolean isRoot() {
			return getContainerFlexoConceptInstance() == null;
		}

		@Override
		public void setContainerFlexoConceptInstance(FlexoConceptInstance aConceptInstance) {
			boolean shouldRecompute = (getOwningVirtualModelInstance() != null && getContainerFlexoConceptInstance() != aConceptInstance);
			performSuperSetter(CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (shouldRecompute) {
				getOwningVirtualModelInstance().reindexAllConceptInstances();
				getOwningVirtualModelInstance().notifyAllRootFlexoConceptInstancesMayHaveChanged();
			}
		}

		@Override
		public void addToEmbeddedFlexoConceptInstances(FlexoConceptInstance aConceptInstance) {
			performSuperAdder(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().notifyAllRootFlexoConceptInstancesMayHaveChanged();
				// getOwningVirtualModelInstance().getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false,
				// true);
			}
		}

		@Override
		public void removeFromEmbeddedFlexoConceptInstances(FlexoConceptInstance aConceptInstance) {
			performSuperRemover(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().notifyAllRootFlexoConceptInstancesMayHaveChanged();
				// getOwningVirtualModelInstance().getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false,
				// true);
			}
		}

		/**
		 * Implements a local RunTimeEvaluationContext, in the context of this FlexoConceptInstance, but isolated. Variables beeing declared
		 * here won't be visible from outside scope of this LocalRunTimeEvaluationContext
		 * 
		 * @author sylvain
		 *
		 */
		public class LocalRunTimeEvaluationContext implements RunTimeEvaluationContext {

			// Stores internal variables used during execution of this isolated RunTimeEvaluationContext
			protected HashMap<String, Object> localVariables = new HashMap<>();

			@Override
			public ExpressionEvaluator getEvaluator() {
				return new FMLExpressionEvaluator(this);
			}

			@Override
			public FlexoEditor getEditor() {
				return getFlexoEditor();
			}

			@Override
			public FMLRunTimeEngine getFMLRunTimeEngine() {
				if (getEditor() != null) {
					return getEditor().getFMLRunTimeEngine();
				}
				return null;
			}

			@Override
			public FlexoObject getFocusedObject() {
				return getFlexoConceptInstance();
			}

			@Override
			public FlexoConceptInstance getFlexoConceptInstance() {
				return FlexoConceptInstanceImpl.this;
			}

			@Override
			public VirtualModelInstance<?, ?> getVirtualModelInstance() {
				return getFlexoConceptInstance().getOwningVirtualModelInstance();
			}

			/**
			 * Calling this method will register a new variable in the run-time context provided by this {@link FlexoConceptInstance} in the
			 * context of its implementation of {@link RunTimeEvaluationContext}.<br>
			 * Variable is initialized with supplied name and value
			 * 
			 * @param variableName
			 * @param value
			 */
			@Override
			public void declareVariable(String variableName, Object value) {
				localVariables.put(variableName, value);
			}

			/**
			 * Calling this method will dereference variable identified by supplied name
			 * 
			 * @param variableName
			 */
			@Override
			public void dereferenceVariable(String variableName) {
				localVariables.remove(variableName);
			}

			@Override
			public Object getValue(BindingVariable variable) {
				if (localVariables.containsKey(variable.getVariableName())) {
					return localVariables.get(variable.getVariableName());
				}
				return FlexoConceptInstanceImpl.this.getValue(variable);
			}

			@Override
			public void setValue(Object value, BindingVariable variable) {
				if (localVariables.containsKey(variable.getVariableName())) {
					localVariables.put(variable.getVariableName(), value);
				}
				else {
					FlexoConceptInstanceImpl.this.setValue(value, variable);
				}
			}

			@Override
			public void logOut(String message, LogLevel logLevel) {
				getFlexoConceptInstance().logOut(message, logLevel);
			}

			@Override
			public void logErr(String message, LogLevel logLevel) {
				getFlexoConceptInstance().logErr(message, logLevel);
			}

		}

		@Override
		public void logOut(String message, LogLevel logLevel) {
			if (getEditor() != null && getEditor().getFMLConsole() != null) {
				getEditor().getFMLConsole().log(message, logLevel, this, null);
			}
			else {
				System.out.println(message);
			}
		}

		@Override
		public void logErr(String message, LogLevel logLevel) {
			if (getEditor() != null && getEditor().getFMLConsole() != null) {
				getEditor().getFMLConsole().log(message, logLevel, this, null);
			}
			else {
				System.err.println(message);
			}
		}

		@Override
		public <T> T getFlexoPropertyValue(FlexoProperty<T> flexoProperty) {
			if (flexoProperty == null || flexoProperty.getFlexoConcept() == null) {
				logger.warning("Unexpected null value: " + flexoProperty);
				return null;
			}
			if (!flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
				// May be the property is to find in the embedding hierarchy
				// Attempt to recursively find it
				FlexoConceptInstance container = getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoProperty.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						return container.getFlexoPropertyValue(flexoProperty);
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}

			if (getOwningVirtualModelInstance() != null && getOwningVirtualModelInstance() != this
					&& flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept().getOwner())) {
				// In this case the property concerns the owning FMLRTVirtualModelInstance
				return getOwningVirtualModelInstance().getFlexoPropertyValue(flexoProperty);
			}
			else {
				if (flexoProperty instanceof FlexoRole) {
					// Take care that we don't manage here the multiple cardinality !!!
					// This is performed in both classes: FlexoPropertyPathElement and FlexoPropertyBindingVariable
					if (((FlexoRole<?>) flexoProperty).getCardinality().isMultipleCardinality()) {
						return (T) getFlexoActorList((FlexoRole) flexoProperty);
					}
					return getFlexoActor((FlexoRole<T>) flexoProperty);
				}
				else if (flexoProperty instanceof ExpressionProperty) {
					try {
						T returned = (T) ((ExpressionProperty<T>) flexoProperty).getExpression().getBindingValue(this);
						return returned;
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						logger.warning("Unexpected exception " + e + " while executing expression property=" + flexoProperty);
						return null;
					} catch (NullReferenceException e) {
						e.printStackTrace();
						logger.warning("Unexpected exception " + e + " while executing expression property=" + flexoProperty);
						return null;
					} catch (InvocationTargetException e) {
						e.getTargetException().printStackTrace();
						logger.warning(
								"Unexpected exception " + e.getTargetException() + " while executing expression property=" + flexoProperty);
						return null;
					} catch (ReflectiveOperationException e) {
						e.printStackTrace();
						logger.warning("Unexpected exception " + e + " while executing expression property=" + flexoProperty);
						return null;
					}
				}
				else if (flexoProperty instanceof GetProperty) {
					FMLControlGraph getControlGraph = ((GetProperty<T>) flexoProperty).getGetControlGraph();
					if (getControlGraph != null) {
						try {
							RunTimeEvaluationContext localEvaluationContext = new LocalRunTimeEvaluationContext();
							T returnedValue = null;
							try {
								getControlGraph.execute(localEvaluationContext);
							} catch (ReturnException e) {
								returnedValue = (T) e.getReturnedValue();
							}
							return returnedValue;
						} catch (FlexoException e) {
							e.printStackTrace();
							logger.warning("Unexpected exception " + e + " while executing get property=" + flexoProperty);
							return null;
						}
					}
				}
				else if (flexoProperty instanceof AbstractProperty) {
					FlexoProperty<T> specializedProperty = (FlexoProperty<T>) getFlexoConcept()
							.getAccessibleProperty(flexoProperty.getName());
					if (flexoProperty != specializedProperty) {
						return getFlexoPropertyValue(specializedProperty);
					}
					else {
						logger.warning("Cannot execute abstract property: " + flexoProperty);
						return null;
					}
				}
			}

			if (flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept().getOwner()) && getOwningVirtualModelInstance() == null) {
				// TODO: check this
				return null;
			}

			logger.warning("Not implemented: getValue() for " + this + " property=" + flexoProperty);

			return null;
		}

		/**
		 * Sets value associated with supplied property, if supplied value is not equals (java semantics) to actual value
		 * 
		 * @param flexoProperty
		 *            the property to lookup
		 * @param value
		 *            the new value to set
		 */
		@Override
		public <T> void setFlexoPropertyValue(FlexoProperty<T> flexoProperty, T value) {

			if (flexoProperty == null || flexoProperty.getFlexoConcept() == null) {
				logger.warning("Unexpected null value: " + flexoProperty);
				return;
			}

			if (!flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
				FlexoConceptInstance container = getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoProperty.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						container.setFlexoPropertyValue(flexoProperty, value);
						return;
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}
			else {

				T oldValue = getFlexoPropertyValue(flexoProperty);
				if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
					if (flexoProperty instanceof ModelSlot) {
						setModelSlotValue((ModelSlot) flexoProperty, value);
						setIsModified();
						getPropertyChangeSupport().firePropertyChange(flexoProperty.getPropertyName(), oldValue, value);
					}
					else if (flexoProperty instanceof FlexoRole) {
						setFlexoActor(value, (FlexoRole) flexoProperty);
					}
					else if (flexoProperty instanceof ExpressionProperty) {
						try {
							((ExpressionProperty<T>) flexoProperty).getExpression().setBindingValue(value, this);
						} catch (TypeMismatchException e) {
							e.printStackTrace();
						} catch (NullReferenceException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NotSettableContextException e) {
							e.printStackTrace();
						} catch (ReflectiveOperationException e) {
							e.printStackTrace();
						}
					}
					else if (flexoProperty instanceof GetSetProperty) {

						// System.out.println("On veut executer un SET sur la GetSetProperty " + flexoProperty + " avec la valeur " +
						// value);

						FMLControlGraph setControlGraph = ((GetSetProperty<T>) flexoProperty).getSetControlGraph();
						try {
							// TODO: handle value beeing set, both in BindingModel AND he in LocalRunTimeEvaluationContext
							RunTimeEvaluationContext localEvaluationContext = new LocalRunTimeEvaluationContext() {
								@Override
								public Object getValue(BindingVariable variable) {
									if (variable instanceof SetValueBindingVariable
											&& ((SetValueBindingVariable) variable).getProperty() == flexoProperty) {
										return value;
									}
									return super.getValue(variable);
								}
							};
							// FD unused T returnedValue = null;
							try {
								setControlGraph.execute(localEvaluationContext);
							} catch (ReturnException e) {
								// Ignore
							}
						} catch (FlexoException e) {
							e.printStackTrace();
						}
					}
					else if (flexoProperty instanceof AbstractProperty) {
						FlexoProperty<T> specializedProperty = (FlexoProperty<T>) getFlexoConcept()
								.getAccessibleProperty(flexoProperty.getName());
						if (flexoProperty != specializedProperty) {
							setFlexoPropertyValue(specializedProperty, value);
						}
						else {
							logger.warning("Cannot execute abstract property: " + flexoProperty);
						}
					}

					setIsModified();

					getPropertyChangeSupport().firePropertyChange(flexoProperty.getPropertyName(), oldValue, value);
				}
			}
		}

		/**
		 * Return property value associated with supplied property name
		 * 
		 * @param propertyName
		 *            the property to lookup
		 */
		@Override
		public final <T> T getFlexoPropertyValue(String propertyName) {
			FlexoProperty<T> property = (FlexoProperty<T>) getFlexoConcept().getAccessibleProperty(propertyName);
			if (property == null) {
				logger.warning("Cannot lookup property " + propertyName);
				return null;
			}
			return getFlexoPropertyValue(property);
		}

		/**
		 * Sets value associated with supplied property
		 * 
		 * @param propertyName
		 *            name of property to use
		 * @param value
		 *            the new value to set
		 */
		@Override
		public final <T> void setFlexoPropertyValue(String propertyName, T value) {
			FlexoProperty<T> property = (FlexoProperty<T>) getFlexoConcept().getAccessibleProperty(propertyName);
			if (property == null) {
				logger.warning("Cannot lookup property " + propertyName);
				return;
			}
			setFlexoPropertyValue(property, value);
		}

		/**
		 * Return actor associated with supplied property, asserting cardinality of supplied property is SINGLE.<br>
		 * If cardinality of supplied property is MULTIPLE, return first found value
		 * 
		 * @param flexoProperty
		 *            the property to lookup
		 */
		@Override
		public <T> T getFlexoActor(final FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}
			if (!flexoRole.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
				FlexoConceptInstance container = getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoRole.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						return container.getFlexoActor(flexoRole);
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}
			else {

				if (getOwningVirtualModelInstance() != null && flexoRole.getFlexoConcept() != null
						&& flexoRole.getFlexoConcept() == getFlexoConcept().getOwningVirtualModel()) {
					// logger.warning("Should not we delegate this to owning VM ???");
					return getOwningVirtualModelInstance().getFlexoActor(flexoRole);
				}
				List<ActorReference<T>> actorReferences = getActorReferenceList(flexoRole);

				if (actorReferences != null && actorReferences.size() > 0) {
					return actorReferences.get(0).getModellingElement();
				}
			}
			return null;
		}

		/**
		 * Return actor associated with supplied property name, asserting cardinality of supplied property is SINGLE.<br>
		 * If cardinality of supplied property is MULTIPLE, return first found value
		 * 
		 * @param flexoPropertyName
		 *            the property to lookup
		 */
		@Override
		public final <T> T getFlexoActor(String flexoRoleName) {
			FlexoRole<T> role = (FlexoRole<T>) getFlexoConcept().getAccessibleProperty(flexoRoleName);
			if (role == null) {
				logger.warning("Cannot lookup property " + flexoRoleName);
				return null;
			}
			return getFlexoActor(role);
		}

		/**
		 * Return actor list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
		 * If cardinality of supplied property is SINGLE, return a singleton list<br>
		 * If no value are defined for this property, return an empty list
		 * 
		 * @param flexoProperty
		 *            the property to lookup
		 */
		// TODO: optimize this method while caching those lists
		@Override
		public <T> List<T> getFlexoActorList(final FlexoRole<T> flexoRole) {

			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}

			if (!flexoRole.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
				FlexoConceptInstance container = getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoRole.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						return container.getFlexoActorList(flexoRole);
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}
			else {

				List<ActorReference<T>> actorReferences = getActorReferenceList(flexoRole);

				List<T> returned = (List) actorLists.get(flexoRole);
				if (returned == null) {
					List<T> existingList = new ArrayList<>();
					if (actorReferences != null) {
						for (ActorReference<T> ref : actorReferences) {
							existingList.add(ref.getModellingElement());
						}
					}
					// CAUTION: tricky area
					// To be able to generically handle adding and removing in a list of roles, we should override add and remove methods
					// here
					returned = new ArrayList<T>(existingList) {
						@Override
						public boolean add(T e) {
							// System.out.println("Adding element "+e+" to list for actor " + flexoRole);
							addToFlexoActors(e, flexoRole);
							return super.add(e);
						}

						@Override
						public boolean remove(Object o) {
							// System.out.println("Removing element "+o+" from list for actor " + flexoRole);
							removeFromFlexoActors((T) o, flexoRole);
							return super.remove(o);
						}
					};
					actorLists.put(flexoRole, returned);
				}
				return returned;
			}

			return null;
		}

		/**
		 * Return actor list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
		 * If cardinality of supplied property is SINGLE, return a singleton list<br>
		 * If no value are defined for this property, return an empty list
		 * 
		 * @param flexoPropertyName
		 *            the property to lookup
		 */
		@Override
		public final <T> List<T> getFlexoActorList(String flexoRoleName) {
			FlexoRole<T> role = (FlexoRole<T>) getFlexoConcept().getAccessibleProperty(flexoRoleName);
			if (role == null) {
				logger.warning("Cannot lookup role " + flexoRoleName);
				return null;
			}
			return getFlexoActorList(role);
		}

		/**
		 * Return {@link ActorReference} object associated with supplied property, asserting cardinality of supplied property is SINGLE.<br>
		 * If cardinality of supplied property is MULTIPLE, return first found value
		 * 
		 * @param flexoProperty
		 *            the property to lookup
		 */
		@Override
		public final <T> ActorReference<T> getActorReference(FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}
			List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

			if (actorReferences != null && actorReferences.size() > 0) {
				return actorReferences.get(0);
			}
			System.out.println(">>>>>>>>>> Tiens on pourrait peut-etre creer une ActorReference ??? pour " + flexoRole);
			return null;
		}

		/**
		 * Return {@link ActorReference} list associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
		 * If cardinality of supplied property is SINGLE, return a singleton list<br>
		 * If no value are defined for this property, return an empty list
		 * 
		 * @param flexoProperty
		 *            the property to lookup
		 */
		@Override
		public final <T> List<ActorReference<T>> getActorReferenceList(FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}

			List actorReferences = actors.get(flexoRole.getRoleName());

			if ((actorReferences == null || actorReferences.size() == 0) && selfInstantiatedActorReferences.get(flexoRole) == null
					&& flexoRole.supportSelfInstantiation()) {
				actorReferences = flexoRole.selfInstantiate(this);
				selfInstantiatedActorReferences.put(flexoRole, actorReferences);
			}

			return actorReferences;
		}

		private Map<FlexoRole<?>, List<ActorReference<?>>> selfInstantiatedActorReferences = new HashMap<>();

		/**
		 * Sets actor associated with supplied property, asserting cardinality of supplied property is SINGLE.<br>
		 * If cardinality of supplied property is MULTIPLE, replace all existing value with supplied object. If no value is found, add
		 * supplied object.
		 * 
		 * @param object
		 *            the object to be registered as actor for supplied property
		 * @param flexoRole
		 *            the role to be considered
		 */
		@Override
		public <T> void setFlexoActor(T object, FlexoRole<T> flexoRole) {

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(
						">>>>>>>>> setObjectForFlexoRole flexoRole: " + flexoRole + " set " + object + " was " + getFlexoActor(flexoRole));
			}

			if (!flexoRole.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
				FlexoConceptInstance container = getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoRole.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						container.setFlexoActor(object, flexoRole);
						return;
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}
			if (getOwningVirtualModelInstance() != this && flexoRole.getFlexoConcept().isAssignableFrom(getFlexoConcept().getOwner())) {
				// In this case the property concerns the owning FMLRTVirtualModelInstance
				getOwningVirtualModelInstance().setFlexoPropertyValue(flexoRole, object);
			}
			else {

				T oldObject = getFlexoActor(flexoRole);

				if (object != oldObject) {

					boolean done = false;

					List<ActorReference<T>> references = getReferences(flexoRole.getRoleName());

					if ((references.size() == 1) && (object != null)) {
						// Replace existing reference with new value
						ActorReference<T> ref = references.get(0);
						ref.setModellingElement(object);
						done = true;
					}
					else if (references.size() > 0) {
						// Remove all existing references
						for (ActorReference<T> actorReference : new ArrayList<>(references)) {
							removeFromActors(actorReference);
						}
					}

					if (object != null && !done) {
						ActorReference<? extends T> actorReference = flexoRole.makeActorReference(object, this);
						addToActors(actorReference);
					}

					if (getResourceData() != null) {
						getResourceData().setIsModified();
					}
					setIsModified();

					setChanged();
					notifyObservers(new FlexoActorChanged<>(this, flexoRole, oldObject, object));
					// System.out.println("FlexoConceptInstance "+Integer.toHexString(hashCode())+" setObjectForPatternProperty()
					// actors="+actors);

					getPropertyChangeSupport().firePropertyChange(flexoRole.getRoleName(), oldObject, object);

				}
			}
		}

		/**
		 * Add actor to the list of reference associated with supplied property, asserting cardinality of supplied property is MULTIPLE.<br>
		 * If cardinality of supplied property is SINGLE, replace all existing value with supplied object.
		 * 
		 * @param object
		 *            the object to be registered as actor for supplied property
		 * @param flexoProperty
		 *            the property to be considered
		 */
		@Override
		public final <T> void addToFlexoActors(T object, FlexoRole<T> flexoRole) {

			if (object != null) {
				ActorReference<? extends T> actorReference = flexoRole.makeActorReference(object, this);
				addToActors(actorReference);
				getPropertyChangeSupport().firePropertyChange(flexoRole.getPropertyName(), null, object);
			}

		}

		/**
		 * Return boolean indicating if supplied object is already registered as an actor for supplied {@link FlexoRole}
		 * 
		 * @param object
		 * @param flexoRole
		 * @return
		 */
		@Override
		public final <T> boolean hasFlexoActor(T object, FlexoRole<T> flexoRole) {
			return getActorReference(object, flexoRole) != null;
		}

		/**
		 * Return {@link ActorReference} referencing supplied object if this one is already registered as an actor for supplied
		 * {@link FlexoRole}<br>
		 * When not return null
		 * 
		 * @param object
		 * @param flexoRole
		 * @return
		 */
		@Override
		public final <T> ActorReference<T> getActorReference(T object, FlexoRole<T> flexoRole) {
			List<ActorReference<T>> actorReferenceList = getActorReferenceList(flexoRole);
			if (actorReferenceList != null) {
				for (ActorReference<T> ar : actorReferenceList) {
					if (ar.getModellingElement() == object) {
						return ar;
					}
				}
			}
			return null;
		}

		/**
		 * Remove actor from the list of reference associated with supplied property, asserting cardinality of supplied property is
		 * MULTIPLE.<br>
		 * If cardinality of supplied property is SINGLE, remove existing matching value
		 * 
		 * @param object
		 *            the object to be removed from supplied property
		 * @param flexoProperty
		 *            the property to be considered
		 */
		@Override
		public final <T> void removeFromFlexoActors(T object, FlexoRole<T> flexoRole) {

			if (object != null) {
				List<ActorReference<T>> references = getReferences(flexoRole.getRoleName());

				if (references == null) {
					// No values are defined, simply return
					return;
				}

				for (ActorReference<T> actorReference : new ArrayList<>(references)) {
					if (areSameValue(actorReference.getModellingElement(), object)) {
						removeFromActors(actorReference);
					}
				}

			}
		}

		/**
		 * Clear all actors associated with supplied property
		 * 
		 * @param flexoProperty
		 */
		@Override
		public final <T> void nullifyFlexoActor(FlexoRole<T> flexoRole) {
			setFlexoActor(null, flexoRole);
		}

		@Override
		public final <T> FlexoProperty<T> getPropertyForActor(T actor) {
			for (FlexoProperty<?> role : getFlexoConcept().getAccessibleProperties()) {
				List<ActorReference<?>> references = (List) getReferences(role.getPropertyName());
				for (ActorReference<?> actorReference : references) {
					if (areSameValue(actorReference.getModellingElement(), actor)) {
						return (FlexoProperty<T>) role;
					}
				}
			}
			return null;
		}

		/**
		 * Return {@link ModelSlotInstance} concretizing supplied modelSlot
		 * 
		 * @param modelSlot
		 * @return
		 */
		@Override
		public final <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
				MS modelSlot) {

			for (ActorReference<?> actorReference : getActors()) {
				if (actorReference instanceof ModelSlotInstance && ((ModelSlotInstance<?, ?>) actorReference).getModelSlot() == modelSlot) {
					return (ModelSlotInstance<MS, RD>) actorReference;
				}
			}

			if (getOwningVirtualModelInstance() != null) {
				ModelSlotInstance<MS, RD> returned = getOwningVirtualModelInstance().getModelSlotInstance(modelSlot);
				if (returned != null) {
					return returned;
				}
			}

			if (this instanceof VirtualModelInstance && ((VirtualModelInstance<?, ?>) this).getContainerVirtualModelInstance() != null) {
				ModelSlotInstance<MS, RD> returned = ((VirtualModelInstance<?, ?>) this).getContainerVirtualModelInstance()
						.getModelSlotInstance(modelSlot);
				if (returned != null) {
					return returned;
				}
			}

			// logger.warning("Cannot find ModelSlotInstance for ModelSlot " + modelSlot);
			// System.out.println("Je suis: " + getFlexoConcept().getFMLRepresentation());
			// System.out.println("Le model slot: " + modelSlot.getFlexoConcept().getFMLRepresentation());
			// Thread.dumpStack();
			return null;
		}

		/**
		 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name
		 * 
		 * @param modelSlot
		 * @return
		 */
		@Override
		public final <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
				String modelSlotName) {

			for (ActorReference<?> actorReference : getActors()) {
				if (actorReference instanceof ModelSlotInstance
						&& ((ModelSlotInstance<?, ?>) actorReference).getModelSlot().getName() == modelSlotName) {
					return (ModelSlotInstance<MS, RD>) actorReference;
				}
			}
			// Do not warn: the model slot may be null here
			// logger.warning("Cannot find ModelSlotInstance named " + modelSlotName);
			return null;
		}

		@Override
		public final List<ModelSlotInstance<?, ?>> getModelSlotInstances() {
			List<ModelSlotInstance<?, ?>> returned = new ArrayList<>();
			for (ActorReference<?> actorReference : getActors()) {
				if (actorReference instanceof ModelSlotInstance) {
					returned.add((ModelSlotInstance<?, ?>) actorReference);
				}
			}
			return returned;
		}

		@SuppressWarnings("unchecked")
		private <MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>> void setModelSlotValue(MS ms, Object value) {

			if (getFlexoConcept() != null && ms != null) {
				ModelSlotInstance<MS, RD> msi = getModelSlotInstance(ms.getName());
				if (msi == null) {
					msi = (ModelSlotInstance<MS, RD>) ms.makeActorReference((RD) value, this);
					addToActors(msi);
				}
				else {
					msi.setAccessedResourceData((RD) value);
				}
				/*if (value instanceof TechnologyAdapterResource) {
					msi.setResource((TechnologyAdapterResource<?, ?>) value);
				}
				if (value instanceof ResourceData) {
					msi.setAccessedResourceData((ResourceData<?>) value);
				}
				else {
					logger.warning("Unexpected resource data " + value + " for model slot " + ms);
				}*/
			}
		}

		@Override
		public String debug() {
			StringBuffer sb = new StringBuffer();
			sb.append("FlexoConcept: " + (flexoConcept != null ? flexoConcept.getName() : getFlexoConceptURI() + "[NOT_FOUND]") + "\n");
			sb.append("Instance: " + getFlexoID() + " hash=" + Integer.toHexString(hashCode()) + "\n");
			for (FlexoRole<?> role : getFlexoConcept().getDeclaredProperties(FlexoRole.class)) {
				// FlexoProjectObject object = actors.get(patternProperty);
				Object actor = getFlexoActor(role);
				sb.append("Property: " + role.getName() + " " + role.getResultingType() + " : [" + actor + "]\n");
			}
			return sb.toString();
		}

		@Override
		public VirtualModelInstance<?, ?> getVirtualModelInstance() {
			return getOwningVirtualModelInstance();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwningVirtualModelInstance() != null && getOwningVirtualModelInstance().getVirtualModel() != null && flexoConcept == null
					&& StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getOwningVirtualModelInstance().getVirtualModel().getFlexoConcept(flexoConceptURI);
				if (flexoConcept == null) {
					logger.warning("Could not find FlexoConcept with uri=" + flexoConceptURI + " (searched in "
							+ getOwningVirtualModelInstance().getVirtualModel() + ")");
				}
			}
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			if (this.flexoConcept != flexoConcept) {
				FlexoConcept oldFlexoConcept = this.flexoConcept;
				this.flexoConcept = flexoConcept;
				if (getOwningVirtualModelInstance() != null) {
					getOwningVirtualModelInstance().flexoConceptInstanceChangedFlexoConcept(this, oldFlexoConcept, flexoConcept);
				}
				getPropertyChangeSupport().firePropertyChange("FlexoConcept", oldFlexoConcept, flexoConcept);
			}
		}

		// Serialization/deserialization only, do not use
		@Override
		public String getFlexoConceptURI() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getURI();
			}
			return flexoConceptURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setFlexoConceptURI(String flexoConceptURI) {
			this.flexoConceptURI = flexoConceptURI;
		}

		private <T> List<ActorReference<T>> getReferences(String roleName) {
			List<ActorReference<T>> references = (List) actors.get(roleName);
			if (references == null) {
				references = new ArrayList<>();
				actors.put(roleName, (List) references);
			}
			return references;
		}

		@Override
		public final List<FlexoConceptInstance> getEmbeddedFlexoConceptInstances(FlexoConcept flexoConcept) {

			if (flexoConcept == null) {
				// logger.warning("Unexpected null FlexoConcept");
				return Collections.emptyList();
			}

			List<FlexoConceptInstance> returned = new ArrayList<>();

			for (FlexoConceptInstance fci : getEmbeddedFlexoConceptInstances()) {
				if (flexoConcept.isAssignableFrom(fci.getFlexoConcept())) {
					returned.add(fci);
				}
			}

			return returned;
		}

		@Override
		public final void addToActors(ActorReference<?> actorReference) {

			// System.out.println("***** addToActors " + actorReference);

			if (actorReference == null) {
				logger.warning("Could not register null ActorReference");
				Thread.dumpStack();
				return;
			}

			if (actorReference.getFlexoRole() != null) {
				// Remove the cache
				actorLists.remove(actorReference.getFlexoRole());
			}

			if (actorReference.getRoleName() == null) {
				logger.warning("Could not register ActorReference with null FlexoProperty: " + actorReference);
				return;
			}
			List<ActorReference<?>> references = (List) getReferences(actorReference.getRoleName());
			references.add(actorReference);
			// System.out.println("added " + actorReference + " for " + actorReference.getModellingElement());
			performSuperAdder(ACTORS_KEY, actorReference);
		}

		@Override
		public final void removeFromActors(ActorReference<?> actorReference) {

			if (actorReference.getFlexoRole() != null) {
				// Remove the cache
				actorLists.remove(actorReference.getFlexoRole());
			}

			if (actorReference.getRoleName() == null) {
				logger.warning("Could not unregister ActorReference with null FlexoProperty: " + actorReference);
				return;
			}
			List<ActorReference<?>> references = (List) getReferences(actorReference.getRoleName());

			actorReference.setFlexoConceptInstance(null);
			references.remove(actorReference);

			// If no more values are present, remove the empty list
			if (references.size() == 0) {
				actors.remove(actorReference.getRoleName());
			}
			performSuperRemover(ACTORS_KEY, actorReference);
		}

		public Object evaluate(String expression) {
			DataBinding<Object> vpdb = new DataBinding<>(expression, getFlexoConcept(), Object.class, BindingDefinitionType.GET);
			try {
				return vpdb.getBindingValue(this);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		public boolean setBindingValue(String expression, Object value) {
			DataBinding<Object> vpdb = new DataBinding<>(expression, getFlexoConcept(), Object.class, BindingDefinitionType.SET);
			if (vpdb.isValid()) {
				try {
					vpdb.setBindingValue(value, this);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NotSettableContextException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
				return true;
			}
			return false;
		}

		@Override
		public BindingFactory getBindingFactory() {
			if (getFlexoConcept() != null && getFlexoConcept().getInspector() != null) {
				return getFlexoConcept().getInspector().getBindingFactory();
			}
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFlexoConcept() != null && getFlexoConcept().getInspector() != null) {
				return getFlexoConcept().getInspector().getBindingModel();
			}
			return null;
		}

		public class SuperReference {

			private FlexoConcept superConcept;

			public SuperReference(FlexoConcept superConcept) {
				super();
				this.superConcept = superConcept;
			}

			public FlexoConcept getSuperConcept() {
				return superConcept;
			}

			public FlexoConceptInstance getInstance() {
				return FlexoConceptInstanceImpl.this;
			}
		}

		private Map<FlexoConcept, SuperReference> superReferences = new HashMap<>();

		private SuperReference getSuperReference(FlexoConcept superConcept) {
			SuperReference returned = superReferences.get(superConcept);
			if (returned == null) {
				returned = new SuperReference(superConcept);
				superReferences.put(superConcept, returned);
			}
			return returned;
		}

		@Override
		public Object getValue(BindingVariable variable) {

			if (variable == null) {
				return null;
			}
			if (variable.getVariableName().equals(FlexoConceptBindingModel.THIS_PROPERTY_NAME)) {
				return this;
			}
			else if (variable instanceof SuperBindingVariable) {
				return getSuperReference(((SuperBindingVariable) variable).getSuperConcept());
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.CONTAINER_PROPERTY_NAME) && getFlexoConcept() != null) {
				if (getFlexoConcept().getApplicableContainerFlexoConcept() != null) {
					return getContainerFlexoConceptInstance();
				}
				return getOwningVirtualModelInstance();
			}
			else if (variable.getVariableName().equals(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY)) {
				return this;
			}
			else if (variable instanceof FlexoPropertyBindingVariable && getFlexoConcept() != null) {
				FlexoProperty<?> flexoProperty = ((FlexoPropertyBindingVariable) variable).getFlexoProperty();
				if (!flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
					FlexoConceptInstance container = getContainerFlexoConceptInstance();
					while (container != null) {
						if (flexoProperty.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
							return ((FlexoPropertyBindingVariable) variable).getValue(container);
						}
						container = container.getContainerFlexoConceptInstance();
					}
				}
				else {
					return ((FlexoPropertyBindingVariable) variable).getValue(this);
				}
			}

			if (getOwningVirtualModelInstance() != null) {
				return getOwningVirtualModelInstance().getValue(variable);
			}

			if (variables.containsKey(variable.getVariableName())) {
				return variables.get(variable.getVariableName());
			}

			logger.warning("Cannot find BindingVariable " + variable + " for " + this);
			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
			// TODO here the code relies on switches, a dispatching approach will be safer (charlie)

			if (variable instanceof FlexoRoleBindingVariable && getFlexoConcept() != null) {
				FlexoRole role = ((FlexoRoleBindingVariable) variable).getFlexoRole();
				if (role != null) {
					if (role.getCardinality().isMultipleCardinality()) {
						if (value instanceof List) {
							// We handle here a multiple value assignation
							List<ActorReference<?>> arToRemove = new ArrayList<>();
							List<ActorReference<?>> existingARs = getActorReferenceList(role);
							if (existingARs != null) {
								arToRemove.addAll(existingARs);
							}
							for (Object o : (List) value) {
								ActorReference<?> existingAr = getActorReference(o, role);
								if (existingAr == null) {
									addToFlexoActors(o, role);
								}
								else {
									arToRemove.remove(existingAr);
								}
							}
							// Finally remove all extra references
							for (ActorReference<?> removedAR : arToRemove) {
								removeFromActors(removedAR);
							}
						}
						else {
							logger.warning("Unexpected value " + value + " for multiple cardinality role: " + role);
						}
					}
					else { // Simple cardinality
						setFlexoActor(value, role);
					}
				}
				else {
					logger.warning("Unexpected property " + variable);
				}
				return;
			}
			else if (variable instanceof FlexoPropertyBindingVariable && getFlexoConcept() != null) {
				FlexoPropertyBindingVariable bindingVariable = (FlexoPropertyBindingVariable) variable;
				if (!bindingVariable.isSettable()) {
					logger.warning("Can't set value " + value + " for read-only variable " + variable);
					return;
				}
				FlexoProperty<Object> property = (FlexoProperty<Object>) bindingVariable.getFlexoProperty();
				setFlexoPropertyValue(property, value);
				return;
			}
			/*else if (variable.getVariableName().equals(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				logger.warning("Forbidden write access " + FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			}*/
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.THIS_PROPERTY_NAME)) {
				logger.warning(
						"Forbidden write access " + FlexoConceptBindingModel.THIS_PROPERTY_NAME + " in " + this + " of " + getClass());
				return;
			}

			if (variables.containsKey(variable.getVariableName())) {
				variables.put(variable.getVariableName(), value);
				return;
			}

			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().setValue(value, variable);
				return;
			}

			logger.warning("Unexpected variable requested in settable context in FlexoConceptInstance: " + variable + " of "
					+ variable.getClass());

		}

		/**
		 * Delete this FlexoConcept instance using default DeletionScheme
		 */
		@Override
		public boolean delete(Object... context) {

			// Also implement properly #getDeletedProperty()
			if (getFlexoConcept() != null) {
				if (getFlexoConcept().getDefaultDeletionScheme() != null) {
					return deleteWithScheme(getFlexoConcept().getDefaultDeletionScheme(), new LocalRunTimeEvaluationContext());
				}
				// Generate on-the-fly default deletion scheme
				DeletionScheme ds = getFlexoConcept().generateDefaultDeletionScheme();
				return deleteWithScheme(ds, new LocalRunTimeEvaluationContext());
			}
			/*boolean returned = performSuperDelete(context);
			getPropertyChangeSupport().firePropertyChange(getDeletedProperty(), false, true);
			return returned;*/
			return performCoreDeletion();
		}

		/**
		 * Delete this {@link FlexoConceptInstance} while calling super delete and removing FCI from container and/or owning
		 * {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Override
		public boolean performCoreDeletion() {
			storedFactoryAfterDeletion = getFactory();
			FlexoConceptInstance container = getContainerFlexoConceptInstance();
			VirtualModelInstance<?, ?> vmi = getOwningVirtualModelInstance();
			if (container != null) {
				container.removeFromEmbeddedFlexoConceptInstances(this);
			}
			if (vmi != null) {
				vmi.removeFromFlexoConceptInstances(this);
			}
			boolean returned = performSuperDelete();
			getPropertyChangeSupport().firePropertyChange(getDeletedProperty(), false, true);
			return returned;
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		@Override
		public boolean deleteWithScheme(DeletionScheme deletionScheme, RunTimeEvaluationContext evaluationContext) {
			if (isDeleted()) {
				return false;
			}

			storedFactoryAfterDeletion = getFactory();
			FlexoConceptInstance container = getContainerFlexoConceptInstance();
			VirtualModelInstance<?, ?> vmi = getOwningVirtualModelInstance();

			if (deletionScheme != null && deletionScheme.getControlGraph() != null) {
				try {
					deletionScheme.getControlGraph().execute(evaluationContext);
				} catch (ReturnException e) {
					// TODO: think about that
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
			}

			if (container != null) {
				container.removeFromEmbeddedFlexoConceptInstances(this);
			}

			if (vmi != null) {
				vmi.removeFromFlexoConceptInstances(this);
			}
			// logger.warning("FlexoConceptInstance deletion !");
			// deleted = true;
			/*if (getFlexoConcept().getPrimaryRepresentationProperty() != null) {
				Object primaryPatternActor = getPatternActor(getFlexoConcept().getPrimaryRepresentationProperty());
				if (primaryPatternActor instanceof FlexoModelObject) {
					DeletionSchemeAction deletionSchemeAction = DeletionSchemeAction.actionType.makeNewAction(
							(FlexoModelObject) primaryPatternActor, null, null);
					deletionSchemeAction.setDeletionScheme(deletionScheme);
					deletionSchemeAction.setFlexoConceptInstanceToDelete(this);
					deletionSchemeAction.doAction();
					if (deletionSchemeAction.hasActionExecutionSucceeded()) {
						logger.info("Successfully performed delete FlexoConcept instance " + getFlexoConcept());
					}
				} else {
					logger.warning("Actor for property " + getFlexoConcept().getPrimaryRepresentationProperty() + " is not a FlexoModelObject: is "
							+ primaryPatternActor);
				}
			}*/
			boolean returned = performSuperDelete();
			getPropertyChangeSupport().firePropertyChange(getDeletedProperty(), false, true);
			return returned;
		}

		private AbstractVirtualModelInstanceModelFactory<?> storedFactoryAfterDeletion;

		@Override
		public AbstractVirtualModelInstanceModelFactory<?> getFactory() {
			if (isDeleted()) {
				return storedFactoryAfterDeletion;
			}
			return super.getFactory();
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		// TODO: not implemented yet
		public FlexoConceptInstanceImpl cloneFlexoConceptInstance(CloningScheme cloningScheme) {
			/*logger.warning("NEW FlexoConceptInstance deletion !");
			deleted = true;
			DeletionSchemeAction deletionSchemeAction = DeletionSchemeAction.actionType.makeNewAction(getPatternActor(getFlexoConcept()
					.getPrimaryRepresentationProperty()), null, null);
			deletionSchemeAction.setDeletionScheme(deletionScheme);
			deletionSchemeAction.setFlexoConceptInstanceToDelete(this);
			deletionSchemeAction.doAction();
			if (deletionSchemeAction.hasActionExecutionSucceeded()) {
				logger.info("Successfully performed delete FlexoConcept instance " + getFlexoConcept());
			}*/
			System.out.println("cloneFlexoConceptInstance() in FlexoConceptInstance with " + cloningScheme);
			return null;
		}

		/**
		 * Return the list of objects that will be deleted if default DeletionScheme is used
		 */
		public List<FlexoObject> objectsThatWillBeDeleted() {
			Vector<FlexoObject> returned = new Vector<>();
			for (FlexoRole<?> pr : getFlexoConcept().getDeclaredProperties(FlexoRole.class)) {
				if (pr.defaultBehaviourIsToBeDeleted() && getFlexoActor(pr) instanceof FlexoObject) {
					returned.add((FlexoObject) getFlexoActor(pr));
				}
			}
			return returned;
		}

		/**
		 * Return list of objects that will be deleted when using supplied DeletionScheme
		 */
		/*public List<FlexoObject> objectsThatWillBeDeleted(DeletionScheme deletionScheme) {
			Vector<FlexoObject> returned = new Vector<>();
			for (EditionAction editionAction : deletionScheme.getActions()) {
				if (editionAction instanceof DeleteAction) {
					DeleteAction deleteAction = (DeleteAction) editionAction;
					if (deleteAction.getAssignedFlexoProperty() instanceof FlexoRole) {
						returned.add((FlexoObject) getFlexoActor((FlexoRole<?>) deleteAction.getAssignedFlexoProperty()));
					}
				}
			}
			return returned;
		}*/

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}

		@Override
		public VirtualModelInstance<?, ?> getResourceData() {
			return getOwningVirtualModelInstance();
		}

		private boolean rendererWasForceRevalidated = false;

		@Override
		public boolean hasValidRenderer() {

			if (getFlexoConcept() != null && getFlexoConcept().getInspector() != null
					&& getFlexoConcept().getInspector().getRenderer() != null) {
				if (!getFlexoConcept().getInspector().getRenderer().isValid()) {
					// Quick and dirty hack to force revalidate
					if (!rendererWasForceRevalidated) {
						String invalidReason = getFlexoConcept().getInspector().getRenderer().invalidBindingReason();
						getFlexoConcept().getInspector().getRenderer().revalidate();
						rendererWasForceRevalidated = true;
						if (getFlexoConcept().getInspector().getRenderer().isValid()) {
							logger.warning("Please investigate: i was required to force revalidate renderer: "
									+ getFlexoConcept().getInspector().getRenderer() + " invalid reason=" + invalidReason);
						}
					}
				}
				// return getFlexoConcept().getInspector().getRenderer().isValid();
			}

			if (getFlexoConcept() != null) {
				return getFlexoConcept().getApplicableRenderer() != null;
			}

			return false;
		}

		private BindingPathChangeListener<String> rendererChangeListener = null;

		private boolean isComputingRenderer = false;

		@Override
		public String getStringRepresentationWithID() {
			return getStringRepresentation() + " [ID=" + getFlexoID() + "]";
		}

		@Override
		public String getStringRepresentation() {

			/*if (getFlexoConcept() != null && getFlexoConcept().getInspector() != null
					&& getFlexoConcept().getInspector().getRenderer() != null) {
				System.out.println("renderer=" + getFlexoConcept().getInspector().getRenderer());
				System.out.println("valid=" + getFlexoConcept().getInspector().getRenderer().isValid());
				System.out.println("reason=" + getFlexoConcept().getInspector().getRenderer().invalidBindingReason());
			}*/

			// We avoid here to enter in an infinite loop while protecting the computation of toString()
			// (Could happen while extensively logging)

			if (hasValidRenderer() && !isComputingRenderer) {
				try {
					isComputingRenderer = true;
					Object obj = getFlexoConcept().getApplicableRenderer().getBindingValue(this);

					if (rendererChangeListener == null) {
						rendererChangeListener = new BindingPathChangeListener<String>(getFlexoConcept().getApplicableRenderer(), this) {
							@Override
							public void bindingValueChanged(Object source, String newValue) {
								/*System.out.println(" bindingValueChanged() detected for string representation of "
										+ FlexoConceptInstanceImpl.this + " " + getFlexoConcept().getInspector().getRenderer()
										+ " with newValue=" + newValue + " source=" + source);*/
								if (!isDeleted()) {
									// We have here detected that the string representation of this concept instance has changed
									getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, newValue);
								}
							}
						};
					}

					isComputingRenderer = false;

					if (obj instanceof String) {
						return (String) obj;
					}
					if (obj != null) {
						return obj.toString();
					}
					return EMPTY_STRING;
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				} finally {
					isComputingRenderer = false;
				}

			}
			return extendedStringRepresentation();
		}

		public String extendedStringRepresentation() {
			StringBuffer sb = new StringBuffer();
			sb.append((getFlexoConcept() != null ? getFlexoConcept().getName() : "null"));
			sb.append("[ID=" + getFlexoID() + "]");
			boolean isFirst = true;
			for (List<ActorReference<?>> refList : actors.values()) {
				for (ActorReference<?> ref : refList) {
					if (ref.getModellingElement() != null) {
						sb.append((isFirst ? ": " : ", ") + ref.getRoleName() + "=" + ref.getModellingElement().toString());
					}
					else {
						sb.append((isFirst ? ": " : ", ") + ref.getRoleName() + "=" + "No object found");
					}
					isFirst = false;
				}
			}
			return sb.toString();
		}

		// Used to avoid loops while computing toString()
		private boolean toStringIsBuilding = false;

		@Override
		public String toString() {
			if (toStringIsBuilding) {
				return getImplementedInterface().getSimpleName() + ":" + (getFlexoConcept() != null ? getFlexoConcept().getName() : "null")
						+ "[ID=" + getFlexoID() + "]";
			}
			toStringIsBuilding = true;
			if (hasValidRenderer()) {
				String returned = getStringRepresentation();
				toStringIsBuilding = false;
				return returned;
			}
			String returned = getImplementedInterface().getSimpleName() + ":"
					+ (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "[ID=" + getFlexoID() + "]";
			toStringIsBuilding = false;
			return returned;
		}

		/**
		 * Return a identifier for this FlexoConceptInstance under the form 'ConceptName'+ID
		 * 
		 * @return
		 */
		@Override
		public String getUserFriendlyIdentifier() {
			return (getFlexoConcept() != null ? getFlexoConcept().getName() : "?FlexoConceptInstance?") + getFlexoID();
		}

		/**
		 * Calling this method will register a new variable in the run-time context provided by this {@link FlexoConceptInstance} in the
		 * context of its implementation of {@link RunTimeEvaluationContext}.<br>
		 * Variable is initialized with supplied name and value
		 * 
		 * @param variableName
		 * @param value
		 */
		@Override
		public void declareVariable(String variableName, Object value) {
			variables.put(variableName, value);
		}

		/**
		 * Calling this method will dereference variable identified by supplied name
		 * 
		 * @param variableName
		 */
		@Override
		public void dereferenceVariable(String variableName) {
			variables.remove(variableName);
		}

		@Override
		public FlexoConceptInstance getFlexoConceptInstance() {
			return this;
		}

		private AbstractVirtualModelInstanceModelFactory<?> deserializationFactory;

		@Override
		public void initializeDeserialization(AbstractVirtualModelInstanceModelFactory<?> factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			deserializationFactory = null;
		}

		public AbstractVirtualModelInstanceModelFactory<?> getDeserializationFactory() {
			return deserializationFactory;
		}

		@Override
		public final boolean hasNature(FlexoConceptInstanceNature nature) {
			return nature.hasNature(this);
		}

		@Override
		public <T> T execute(String expression)
				throws TypeMismatchException, NullReferenceException, ReflectiveOperationException, InvalidBindingException {
			DataBinding<T> db = new DataBinding<>(expression, this, Object.class, BindingDefinitionType.GET);
			if (!db.isValid()) {
				logger.warning("Invalid binding " + db + " reason: " + db.invalidBindingReason());
				throw new InvalidBindingException(db);
			}
			T returned = db.getBindingValue(this);
			db.delete();
			return returned;
		}

		@Override
		public <T> T execute(String expression, Object... parameters)
				throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {
			return (T) FMLMultipleParametersBindingEvaluator.evaluateBinding(expression, getBindingFactory(), this, parameters);
		}

		@Override
		public boolean isOf(String conceptName) {
			return getFlexoConcept().getName().equals(conceptName);
		}

		/**
		 * Instanciate run-time-level object encoding reference to this {@link FlexoConceptInstance} object
		 * 
		 * @param role
		 *            the {@link FlexoConceptInstanceRole} defining access to supplied object
		 * @param fci
		 *            the {@link FlexoConceptInstance} where this reference should be built
		 * 
		 */
		@Override
		public ActorReference<? extends FlexoConceptInstance> makeActorReference(FlexoConceptInstanceRole role, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = getFactory();
			if (factory == null) {
				return null;
			}
			ModelObjectActorReference<FlexoConceptInstance> returned = factory.newInstance(ModelObjectActorReference.class);
			returned.setFlexoRole(role);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(this);
			return returned;
		}

		@Override
		public String render() {
			StringBuffer sb = new StringBuffer();
			String line = StringUtils.buildString('-', 80) + "\n";
			sb.append(line);
			sb.append("FlexoConceptInstance : " + getUserFriendlyIdentifier() + "\n");
			if (hasValidRenderer()) {
				sb.append("Renderer             : " + getStringRepresentation() + "\n");
			}
			sb.append("FlexoConcept         : " + getFlexoConcept().getName() + "\n");
			sb.append(line);
			List<FlexoRole> roles = getFlexoConcept().getAccessibleProperties(FlexoRole.class);
			if (roles.size() > 0) {
				for (FlexoRole<?> role : roles) {
					if (role.getFlexoConcept().isAssignableFrom(getFlexoConcept())) {
						sb.append(role.getName() + " = " + getFlexoPropertyValue(role) + "\n");
					}
				}
			}
			else {
				sb.append("No values" + "\n");
			}
			sb.append(line);
			if (getEmbeddedFlexoConceptInstances().size() > 0) {
				for (FlexoConceptInstance child : getEmbeddedFlexoConceptInstances()) {
					appendFCI(child, sb, 0);
				}
			}
			else {
				sb.append("No contents" + "\n");
			}
			sb.append(line);
			return sb.toString();
		}

		protected void appendFCI(FlexoConceptInstance fci, StringBuffer sb, int indent) {
			sb.append(StringUtils.buildWhiteSpaceIndentation(indent * 2) + "> " + fci.getUserFriendlyIdentifier() + "\n");
			for (FlexoConceptInstance child : fci.getEmbeddedFlexoConceptInstances()) {
				appendFCI(child, sb, indent + 1);
			}
		}

		/**
		 * Clone this {@link FlexoConceptInstance} given supplied factory.<br>
		 * Clone is computed using roles, where property values are kept references<br>
		 * Embedded {@link FlexoConceptInstance} are cloned using same semantics
		 * 
		 * @param factory
		 * @return
		 */
		@Override
		public FlexoConceptInstance cloneUsingRoles(AbstractVirtualModelInstanceModelFactory<?> factory) {
			FlexoConceptInstance clone = factory.newInstance(FlexoConceptInstance.class);
			clone.setFlexoConcept(getFlexoConcept());
			clone.setLocalFactory(factory);
			for (FlexoRole flexoRole : getFlexoConcept().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				clone.setFlexoPropertyValue(flexoRole, value);
			}
			for (FlexoConceptInstance fci : getEmbeddedFlexoConceptInstances()) {
				FlexoConceptInstance clonedFCI = fci.cloneUsingRoles(factory);
				clone.addToEmbeddedFlexoConceptInstances(clonedFCI);
			}
			return clone;

		}

		/**
		 * An {@link #equals(Object)} implementation for {@link FlexoConceptInstance}, focused on roles
		 * 
		 * @param obj
		 * @return
		 */
		@Override
		public boolean equalsUsingRoles(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof FlexoConceptInstance)) {
				return false;
			}
			FlexoConceptInstance other = (FlexoConceptInstance) obj;
			if (getFlexoConcept() != other.getFlexoConcept()) {
				return false;
			}
			for (FlexoRole<?> flexoRole : getFlexoConcept().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				Object otherValue = other.getFlexoPropertyValue(flexoRole);
				if (value == null) {
					if (otherValue != null) {
						return false;
					}
				}
				else if (!value.equals(otherValue)) {
					return false;
				}
			}

			ListIterator<FlexoConceptInstance> e1 = getEmbeddedFlexoConceptInstances().listIterator();
			ListIterator<FlexoConceptInstance> e2 = other.getEmbeddedFlexoConceptInstances().listIterator();

			while (e1.hasNext() && e2.hasNext()) {
				FlexoConceptInstance o1 = e1.next();
				FlexoConceptInstance o2 = e2.next();
				if (!(o1 == null ? o2 == null : o1.equalsUsingRoles(o2))) {
					return false;
				}
			}
			if (e1.hasNext() || e2.hasNext()) {
				return false;
			}

			return true;
		}

		/**
		 * A {@link #hashCode()} implementation for {@link FlexoConceptInstance}, focused on roles
		 * 
		 * @return
		 */
		@Override
		public int hashCodeUsingRoles() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getFlexoConcept() == null) ? 0 : getFlexoConcept().hashCode());
			for (FlexoRole<?> flexoRole : getFlexoConcept().getAccessibleRoles()) {
				Object value = getFlexoPropertyValue(flexoRole);
				result = prime * result + ((value == null) ? 0 : value.hashCode());
			}
			for (FlexoConceptInstance flexoConceptInstance : getEmbeddedFlexoConceptInstances()) {
				result = prime * result + ((flexoConceptInstance == null) ? 0 : flexoConceptInstance.hashCodeUsingRoles());
			}
			return result;
		}

		/**
		 * Return applicable inspected object, which is the delegated object in related concept has delegated inspector, otherwise this
		 */
		@Override
		public FlexoConceptInstance getInspectedObject() {
			if (getFlexoConcept() != null && getFlexoConcept().hasDelegatedInspector()) {
				try {
					FlexoConceptInstance delegate = getFlexoConcept().getInspector().getDelegateConceptInstance().getBindingValue(this);
					return delegate;
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return this;
		}

	}

	@DefineValidationRule
	public static class FlexoConceptInstanceMustHaveType extends ValidationRule<FlexoConceptInstanceMustHaveType, FlexoConceptInstance> {
		public FlexoConceptInstanceMustHaveType() {
			super(FlexoConceptInstance.class, "flexo_concept_instance_must_have_a_type");
		}

		@Override
		public ValidationIssue<FlexoConceptInstanceMustHaveType, FlexoConceptInstance> applyValidation(
				FlexoConceptInstance flexoConceptInstance) {

			if (flexoConceptInstance.getFlexoConcept() == null) {
				DeleteThisInstance fixProposal = new DeleteThisInstance(flexoConceptInstance);
				return new ValidationError<>(this, flexoConceptInstance, "instance_has_no_type", fixProposal);
				// return new ValidationWarning<>(this, flexoConcept, "non_abstract_flexo_concept_($validable.name)_has_no_behaviours");
			}

			// return new CompoundIssue<>(flexoConceptInstance,
			// new ValidationError<>(this, flexoConceptInstance, "moi j'aime pas les frites"),
			// new ValidationWarning<>(this, flexoConceptInstance, "et pas trop les saucisses"));
			return null;
		}

		protected static class DeleteThisInstance extends FixProposal<FlexoConceptInstanceMustHaveType, FlexoConceptInstance> {

			private final FlexoConceptInstance flexoConceptInstance;

			public DeleteThisInstance(FlexoConceptInstance flexoConceptInstance) {
				super("create_default_deletion_scheme");
				this.flexoConceptInstance = flexoConceptInstance;
			}

			public FlexoConceptInstance getFlexoConceptInstance() {
				return flexoConceptInstance;
			}

			@Override
			protected void fixAction() {
				getFlexoConceptInstance().delete();
			}

		}

	}

	@DefineValidationRule
	public static class RoleCardinalitiesMustBeValid extends ValidationRule<RoleCardinalitiesMustBeValid, FlexoConceptInstance> {
		public RoleCardinalitiesMustBeValid() {
			super(FlexoConceptInstance.class, "role_cardinalities_must_be_valid");
		}

		@Override
		public ValidationIssue<RoleCardinalitiesMustBeValid, FlexoConceptInstance> applyValidation(
				FlexoConceptInstance flexoConceptInstance) {

			if (flexoConceptInstance.getFlexoConcept() != null) {

				List<ValidationIssue<RoleCardinalitiesMustBeValid, FlexoConceptInstance>> issues = new ArrayList<>();

				for (FlexoRole<?> flexoRole : flexoConceptInstance.getFlexoConcept().getAccessibleRoles()) {
					if (flexoRole.getFlexoConcept().isAssignableFrom(flexoConceptInstance.getFlexoConcept())) {
						List<?> actorReferenceList = flexoConceptInstance.getActorReferenceList(flexoRole);
						switch (flexoRole.getCardinality()) {
							case One:
								if (actorReferenceList == null || actorReferenceList.size() == 0) {
									issues.add(new InvalidCardinality(flexoRole, flexoConceptInstance,
											"missing_required_role_($role.name)_for_($flexoConceptInstance)"));
								}
								else if (actorReferenceList.size() > 1) {
									issues.add(new InvalidCardinality(flexoRole, flexoConceptInstance,
											"found_multiple_values_for_role_($role.name)_for_($flexoConceptInstance)"));
								}
								break;
							case ZeroOne:
								if (actorReferenceList != null && actorReferenceList.size() > 1) {
									issues.add(new InvalidCardinality(flexoRole, flexoConceptInstance,
											"found_multiple_values_for_role_($role.name)_for_($flexoConceptInstance)"));
								}
								break;
							case OneMany:
								System.out.println("actorReferenceList = " + actorReferenceList);
								if (actorReferenceList == null || actorReferenceList.size() == 0) {
									issues.add(new InvalidCardinality(flexoRole, flexoConceptInstance,
											"missing_required_roles_($role.name)_for_($flexoConceptInstance)"));
								}

							default:
								break;
						}
					}
					/*else {
						System.out.println("Do not check for " + flexoRole.getName() + " because it applies to "
								+ flexoRole.getFlexoConcept() + " and i am a " + flexoConceptInstance.getFlexoConcept());
					}*/
				}

				if (issues.size() > 0) {
					return new CompoundIssue<>(flexoConceptInstance, issues);
				}

			}

			return null;
		}

		public class InvalidCardinality extends ValidationError<RoleCardinalitiesMustBeValid, FlexoConceptInstance> {

			private FlexoRole<?> role;
			private FlexoConceptInstance flexoConceptInstance;

			public InvalidCardinality(FlexoRole<?> role, FlexoConceptInstance flexoConceptInstance, String message) {
				super(RoleCardinalitiesMustBeValid.this, flexoConceptInstance, message);
				this.role = role;
				this.flexoConceptInstance = flexoConceptInstance;
			}

			public FlexoRole<?> getRole() {
				return role;
			}

			public FlexoConceptInstance getFlexoConceptInstance() {
				return flexoConceptInstance;
			}
		}

	}

	@DefineValidationRule
	public static class ConstraintsShouldNotBeViolated extends ValidationRule<ConstraintsShouldNotBeViolated, FlexoConceptInstance> {
		public ConstraintsShouldNotBeViolated() {
			super(FlexoConceptInstance.class, "concept_constraints_should_not_be_violated");
		}

		@Override
		public ValidationIssue<ConstraintsShouldNotBeViolated, FlexoConceptInstance> applyValidation(
				FlexoConceptInstance flexoConceptInstance) {

			if (flexoConceptInstance.getFlexoConcept() != null) {
				List<ValidationIssue<ConstraintsShouldNotBeViolated, FlexoConceptInstance>> issues = new ArrayList<>();
				applyValidationToConcept(flexoConceptInstance, flexoConceptInstance.getFlexoConcept(), issues);
				if (issues.size() > 0) {
					return new CompoundIssue<>(flexoConceptInstance, issues);
				}
			}

			return null;
		}

		private void applyValidationToConcept(FlexoConceptInstance flexoConceptInstance, FlexoConcept concept,
				List<ValidationIssue<ConstraintsShouldNotBeViolated, FlexoConceptInstance>> issues) {

			for (SimpleInvariant simpleInvariant : concept.getSimpleInvariants()) {
				try {
					if (!simpleInvariant.getConstraint().getBindingValue(flexoConceptInstance)) {
						issues.add(new ViolatedInvariant(simpleInvariant, flexoConceptInstance));
					}
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			for (IterationInvariant iterationInvariant : concept.getIterationInvariants()) {

				try {
					List<?> iterationObjects = iterationInvariant.getIteration().getBindingValue(flexoConceptInstance);
					for (Object item : iterationObjects) {
						// System.out.println("invariant = " + invariant);
						// System.out.println("invariant.getConstraint() = " + invariant.getConstraint());
						// System.out.println("valid : " + invariant.getConstraint().isValid());
						// System.out.println("reason: " + invariant.getConstraint().invalidBindingReason());

						for (SimpleInvariant simpleInvariant : iterationInvariant.getSimpleInvariants()) {
							try {
								Boolean evaluateConstraint = simpleInvariant.getConstraint()
										.getBindingValue(new BindingEvaluationContext() {
											@Override
											public Object getValue(BindingVariable variable) {
												if (variable.getVariableName().equals(iterationInvariant.getIteratorName())) {
													return item;
												}
												return flexoConceptInstance.getValue(variable);
											}

											@Override
											public ExpressionEvaluator getEvaluator() {
												return new FMLExpressionEvaluator(this);
											}
										});
								// System.out.println("evaluateConstraint : " + evaluateConstraint);
								if (!evaluateConstraint) {
									issues.add(new ViolatedInvariant(simpleInvariant, flexoConceptInstance));
								}
							} catch (TypeMismatchException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NullReferenceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ReflectiveOperationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			for (FlexoConcept parentConcept : concept.getParentFlexoConcepts()) {
				applyValidationToConcept(flexoConceptInstance, parentConcept, issues);
			}
		}

		public class ViolatedInvariant extends ValidationError<ConstraintsShouldNotBeViolated, FlexoConceptInstance> {

			private SimpleInvariant invariant;
			private FlexoConceptInstance flexoConceptInstance;
			private Object item;

			public ViolatedInvariant(SimpleInvariant constraint, FlexoConceptInstance flexoConceptInstance) {
				super(ConstraintsShouldNotBeViolated.this, flexoConceptInstance, constraint.getName());
				this.invariant = constraint;
				this.flexoConceptInstance = flexoConceptInstance;
			}

			public ViolatedInvariant(SimpleInvariant constraint, FlexoConceptInstance flexoConceptInstance, Object item) {
				this(constraint, flexoConceptInstance);
				this.item = item;
			}

			public SimpleInvariant getInvariant() {
				return invariant;
			}

			public FlexoConceptInstance getFlexoConceptInstance() {
				return flexoConceptInstance;
			}

			public Object getItem() {
				return item;
			}

		}

	}

}
