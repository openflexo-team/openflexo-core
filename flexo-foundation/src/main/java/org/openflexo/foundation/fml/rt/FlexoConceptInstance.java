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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.MultipleParametersBindingEvaluator;
import org.openflexo.connie.binding.BindingValueChangeListener;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.fml.binding.SetValueBindingVariable;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration.DefaultModelSlotInstanceConfigurationOption;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.DeserializationInitializer;
import org.openflexo.model.annotations.Embedded;
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
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoConceptInstance} is the run-time concept (instance) of an {@link FlexoConcept}.<br>
 * 
 * As such, a {@link FlexoConceptInstance} is instantiated inside a {@link VirtualModelInstance} (only {@link VirtualModelInstance} objects
 * might leave outside an other {@link VirtualModelInstance}).<br>
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
public interface FlexoConceptInstance extends FlexoObject, VirtualModelInstanceObject, Bindable, RunTimeEvaluationContext {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String EMPTY_STRING = "<empty>";

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

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String OWNING_VIRTUAL_MODEL_INSTANCE_KEY = "owningVirtualModelInstance";

	/**
	 * Return the {@link VirtualModelInstance} where this FlexoConceptInstance is instanciated (result might be different from
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
	@PastingPoint
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

	public boolean hasValidRenderer();

	@DeserializationInitializer
	public void initializeDeserialization(VirtualModelInstanceModelFactory<?> factory);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public boolean hasNature(FlexoConceptInstanceNature nature);

	/**
	 * Return {@link ModelSlotInstance} concretizing supplied modelSlot
	 * 
	 * @param modelSlot
	 * @return
	 */
	public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
			MS modelSlot);

	/**
	 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name
	 * 
	 * @param modelSlot
	 * @return
	 */
	public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(String modelSlotName);

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
	 * TODO: add FlexoEditor parameter instead of using {@link #getEditor()}
	 * 
	 * @param expression
	 * @return
	 * @throws TypeMismatchException
	 * @throws NullReferenceException
	 * @throws InvocationTargetException
	 */
	public <T> T execute(String expression) throws TypeMismatchException, NullReferenceException, InvocationTargetException;

	/**
	 * Use the current FlexoConceptInstance as the run-time context of an expression supplied<br>
	 * as a String expressed in FML language, and a set of arguments given in appearing order in the expression<br>
	 * Execute this expression in that run-time context and return the value computed from the expression in that run-time context.
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
			throws TypeMismatchException, NullReferenceException, InvocationTargetException;

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
		public FlexoConceptInstanceImpl(/*VirtualModelInstance virtualModelInstance*/) {
			super();
			actors = new HashMap<>();
			actorLists = new HashMap<>();
			variables = new HashMap<>();
		}

		@Override
		public FlexoResourceCenter<?> getResourceCenter() {
			View prout = getView();
			if (getView() != null && getView() != this) {
				return getView().getResourceCenter();
			}
			return super.getResourceCenter();
		}

		/*@Override
		public void debug(String aLogString, FlexoConceptInstance fci, FlexoBehaviour behaviour) {
			if (getFlexoEditor() != null) {
				getFlexoEditor().getFMLConsole().debug(aLogString, fci, behaviour);
			}
		}
		
		@Override
		public void log(String aLogString, FMLConsole.LogLevel logLevel, FlexoConceptInstance fci, FlexoBehaviour behaviour) {
			if (getFlexoEditor() != null) {
				getFlexoEditor().getFMLConsole().log(aLogString, logLevel, fci, behaviour);
			}
		}*/

		// TODO: this is not a good idea, we should separate FlexoConceptInstance from RunTimeEvaluationContext
		private FlexoEditor getFlexoEditor() {
			if (getResourceCenter() instanceof FlexoProject && getServiceManager() != null) {
				return getServiceManager().getProjectLoaderService().getEditorForProject((FlexoProject) getResourceCenter());
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
			performSuperSetter(CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false, true);
			}
		}

		@Override
		public void addToEmbeddedFlexoConceptInstances(FlexoConceptInstance aConceptInstance) {
			performSuperAdder(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false, true);
			}
		}

		@Override
		public void removeFromEmbeddedFlexoConceptInstances(FlexoConceptInstance aConceptInstance) {
			performSuperRemover(EMBEDDED_FLEXO_CONCEPT_INSTANCE_KEY, aConceptInstance);
			if (getOwningVirtualModelInstance() != null) {
				getOwningVirtualModelInstance().getPropertyChangeSupport().firePropertyChange("allRootFlexoConceptInstances", false, true);
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

			/*@Override
			public void debug(String aLogString, FlexoConceptInstance fci, FlexoBehaviour behaviour) {
				getFlexoConceptInstance().debug(aLogString, fci, behaviour);
			}
			
			@Override
			public void log(String aLogString, FMLConsole.LogLevel logLevel, FlexoConceptInstance fci, FlexoBehaviour behaviour) {
				getFlexoConceptInstance().log(aLogString, logLevel, fci, behaviour);
			}*/
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
			else if (flexoProperty.getFlexoConcept().isAssignableFrom(getFlexoConcept().getOwner())) {
				// In this case the property concerns the owner VirtualModelInstance
				return getVirtualModelInstance().getFlexoPropertyValue(flexoProperty);
			}
			else {
				if (flexoProperty instanceof ModelSlot) {
					ModelSlot ms = getFlexoConcept().getModelSlot(((ModelSlot) flexoProperty).getName());
					if (ms != null) {
						ModelSlotInstance<?, ?> modelSlotInstance = getModelSlotInstance(ms);
						if (modelSlotInstance != null) {
							return (T) modelSlotInstance.getAccessedResourceData();
						}
						else {
							// Do not warn: the model slot may be null here
							// logger.warning("Unexpected null model slot instance for " + ms + " in " + this);
							return null;
						}
					}
				}
				else if (flexoProperty instanceof FlexoRole) {
					// Take care that we don't manage here the multiple cardinality !!!
					// This is performed in both classes: FlexoConceptFlexoPropertyPathElement and FlexoPropertyBindingVariable
					return getFlexoActor((FlexoRole<T>) flexoProperty);
				}
				else if (flexoProperty instanceof ExpressionProperty) {
					try {
						T returned = (T) ((ExpressionProperty<T>) flexoProperty).getExpression().getBindingValue(this);
						return returned;
					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
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
						}
					}
				}
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
						setModelSlotValue((ModelSlot<?>) flexoProperty, value);
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
						}
					}
					else if (flexoProperty instanceof GetSetProperty) {

						System.out.println("On veut executer un SET sur la GetSetProperty " + flexoProperty + " avec la valeur " + value);

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
		public <T> T getFlexoPropertyValue(String propertyName) {
			FlexoProperty<T> property = (FlexoProperty<T>) getFlexoConcept().getAccessibleProperty(propertyName);
			if (property == null) {
				logger.warning("Cannot lookup property " + propertyName);
				return null;
			}
			return getFlexoPropertyValue(property);
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

				if (flexoRole.getFlexoConcept() == getFlexoConcept().getOwningVirtualModel()) {
					// logger.warning("Should not we delegate this to owning VM ???");
					return getOwningVirtualModelInstance().getFlexoActor(flexoRole);
				}
				List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

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
		public <T> T getFlexoActor(String flexoRoleName) {
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

				List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

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
		public <T> List<T> getFlexoActorList(String flexoRoleName) {
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
		public <T> ActorReference<T> getActorReference(FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}
			List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

			if (actorReferences != null && actorReferences.size() > 0) {
				return actorReferences.get(0);
			}

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
		public <T> List<ActorReference<T>> getActorReferenceList(FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null flexoProperty");
				return null;
			}

			return (List) actors.get(flexoRole.getRoleName());
		}

		/*private <T> ActorReference<T> getParentActorReference(FlexoConcept flexoConcept, FlexoProperty<T> flexoProperty) {
			ActorReference<T> actorReference;
			for (FlexoConcept parentFlexoConcept : this.getFlexoConcept().getParentFlexoConcepts()) {
				if (parentFlexoConcept != null) {
					FlexoProperty ppFlexoProperty = parentFlexoConcept.getFlexoProperty(flexoProperty.getName());
					if (ppFlexoProperty == flexoProperty) {
						flexoProperty = (FlexoProperty<T>) this.getFlexoConcept().getFlexoProperty(ppFlexoProperty.getName());
						actorReference = (ActorReference<T>) actors.get(flexoProperty);
						if (actorReference != null) {
							return actorReference;
						}
					}
				}
			}
			return null;
		}*/

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
			else {

				T oldObject = getFlexoActor(flexoRole);
				if (object != oldObject) {

					boolean done = false;

					if (oldObject != null) {

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
					}

					if (object != null && !done) {
						ActorReference<T> actorReference = flexoRole.makeActorReference(object, this);
						addToActors(actorReference);
					}

					getResourceData().setIsModified();
					setIsModified();

					setChanged();
					notifyObservers(new FlexoActorChanged(this, flexoRole, oldObject, object));
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
		public <T> void addToFlexoActors(T object, FlexoRole<T> flexoRole) {

			if (object != null) {
				ActorReference<T> actorReference = flexoRole.makeActorReference(object, this);
				addToActors(actorReference);
				getPropertyChangeSupport().firePropertyChange(flexoRole.getPropertyName(), null, object);
			}

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
		public <T> void removeFromFlexoActors(T object, FlexoRole<T> flexoRole) {

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
		public <T> void nullifyFlexoActor(FlexoRole<T> flexoRole) {
			setFlexoActor(null, flexoRole);
		}

		@Override
		public <T> FlexoProperty<T> getPropertyForActor(T actor) {
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
		public <RD extends ResourceData<RD> & TechnologyObject<?>, MS extends ModelSlot<? extends RD>> ModelSlotInstance<MS, RD> getModelSlotInstance(
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

			if (this instanceof VirtualModelInstance && ((VirtualModelInstance) this).getView() != null) {
				ModelSlotInstance<MS, RD> returned = ((VirtualModelInstance) this).getView().getModelSlotInstance(modelSlot);
				if (returned != null) {
					return returned;
				}
			}

			// logger.warning("Cannot find ModelSlotInstance for ModelSlot " + modelSlot);
			// System.out.println("Je suis: " + getFlexoConcept().getFMLRepresentation());
			// System.out.println("Le model slot: " + modelSlot.getFlexoConcept().getFMLRepresentation());
			// Thread.dumpStack();
			/*if (getFlexoConcept() != null && !getFlexoConcept().getModelSlots().contains(modelSlot)) {
				logger.warning("Worse than that, supplied ModelSlot is not part of concept " + getFlexoConcept());
			}*/
			return null;
		}

		/**
		 * Return {@link ModelSlotInstance} concretizing modelSlot identified by supplied name
		 * 
		 * @param modelSlot
		 * @return
		 */
		@Override
		public <RD extends ResourceData<RD> & TechnologyObject<?>> ModelSlotInstance<?, RD> getModelSlotInstance(String modelSlotName) {

			for (ActorReference<?> actorReference : getActors()) {
				if (actorReference instanceof ModelSlotInstance
						&& ((ModelSlotInstance<?, ?>) actorReference).getModelSlot().getName() == modelSlotName) {
					return (ModelSlotInstance<?, RD>) actorReference;
				}
			}
			// Do not warn: the model slot may be null here
			// logger.warning("Cannot find ModelSlotInstance named " + modelSlotName);
			return null;
		}

		@Override
		public List<ModelSlotInstance<?, ?>> getModelSlotInstances() {
			List<ModelSlotInstance<?, ?>> returned = new ArrayList<>();
			for (ActorReference<?> actorReference : getActors()) {
				if (actorReference instanceof ModelSlotInstance) {
					returned.add((ModelSlotInstance) actorReference);
				}
			}
			return returned;
		}

		/*	@Override
			public void setModelSlotInstances(List<ModelSlotInstance<?, ?>> instances) {
				this.modelSlotInstances = instances;
			}
		
			@Override
			public void addToModelSlotInstances(ModelSlotInstance<?, ?> instance) {
				if (!modelSlotInstances.contains(instance)) {
					instance.setVirtualModelInstance(this);
					modelSlotInstances.add(instance);
					setChanged();
					notifyObservers(new VEDataModification("modelSlotInstances", null, instance));
				}
			}
		
			@Override
			public void removeFromModelSlotInstance(ModelSlotInstance<?, ?> instance) {
				if (modelSlotInstances.contains(instance)) {
					instance.setVirtualModelInstance(null);
					modelSlotInstances.remove(instance);
					setChanged();
					notifyObservers(new VEDataModification("modelSlotInstances", instance, null));
				}
			}*/

		private void setModelSlotValue(ModelSlot<?> ms, Object value) {

			if (getFlexoConcept() != null && ms != null) {
				if (value instanceof TechnologyAdapterResource) {
					ModelSlotInstance msi = getModelSlotInstance(ms.getName());
					if (msi == null) {
						ModelSlotInstanceConfiguration<?, ?> msiConfiguration = ms.createConfiguration(this, getResourceCenter());
						msiConfiguration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingResource);
						msi = msiConfiguration.createModelSlotInstance(this, getView());
						msi.setFlexoConceptInstance(this);
						addToActors(msi);
					}
					msi.setResource((TechnologyAdapterResource) value);
				}
				if (value instanceof ResourceData) {
					ModelSlotInstance msi = getModelSlotInstance(ms.getName());
					if (msi == null) {
						ModelSlotInstanceConfiguration<?, ?> msiConfiguration = ms.createConfiguration(this, getResourceCenter());
						msiConfiguration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingResource);
						msi = msiConfiguration.createModelSlotInstance(this, getView());
						msi.setFlexoConceptInstance(this);
						addToActors(msi);
					}
					msi.setAccessedResourceData((ResourceData) value);
				}
				else {
					logger.warning("Unexpected resource data " + value + " for model slot " + ms);
				}
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
		public List<FlexoConceptInstance> getEmbeddedFlexoConceptInstances(FlexoConcept flexoConcept) {

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
		public void addToActors(ActorReference<?> actorReference) {

			// System.out.println("***** addToActors " + actorReference);

			if (actorReference == null) {
				logger.warning("Could not register null ActorReference");
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
			else {
				List<ActorReference<?>> references = (List) getReferences(actorReference.getRoleName());
				references.add(actorReference);
				// System.out.println("added " + actorReference + " for " + actorReference.getModellingElement());
				performSuperAdder(ACTORS_KEY, actorReference);
			}

		}

		@Override
		public void removeFromActors(ActorReference<?> actorReference) {

			if (actorReference.getFlexoRole() != null) {
				// Remove the cache
				actorLists.remove(actorReference.getFlexoRole());
			}

			if (actorReference.getRoleName() == null) {
				logger.warning("Could not unregister ActorReference with null FlexoProperty: " + actorReference);
				return;
			}
			else {
				List<ActorReference<?>> references = (List) getReferences(actorReference.getRoleName());

				actorReference.setFlexoConceptInstance(null);
				references.remove(actorReference);

				// If no more values are present, remove the empty list
				if (references.size() == 0) {
					actors.remove(actorReference.getRoleName());
				}

				performSuperRemover(ACTORS_KEY, actorReference);
			}

		}

		public Object evaluate(String expression) {
			DataBinding<Object> vpdb = new DataBinding<>(expression);
			vpdb.setOwner(getFlexoConcept());
			vpdb.setDeclaredType(Object.class);
			vpdb.setBindingDefinitionType(BindingDefinitionType.GET);
			try {
				return vpdb.getBindingValue(this);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		public boolean setBindingValue(String expression, Object value) {
			DataBinding<Object> vpdb = new DataBinding<>(expression);
			vpdb.setOwner(getFlexoConcept());
			vpdb.setDeclaredType(Object.class);
			vpdb.setBindingDefinitionType(BindingDefinitionType.SET);
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
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		public BindingFactory getBindingFactory() {
			return getFlexoConcept().getInspector().getBindingFactory();
		}

		@Override
		public BindingModel getBindingModel() {
			return getFlexoConcept().getInspector().getBindingModel();
		}

		@Override
		public Object getValue(BindingVariable variable) {

			if (variable.getVariableName().equals(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY)) {
				return this;
			}
			/*else if (variable instanceof FlexoRoleBindingVariable && getFlexoConcept() != null) {
				FlexoRole<?> role = ((FlexoRoleBindingVariable) variable).getFlexoRole();
				// Handle here case of FlexoRole relates to VirtualModelInstance container
				if (role.getFlexoConcept() == getFlexoConcept().getOwningVirtualModel()) {
					return getOwningVirtualModelInstance().getValue(variable);
				}
				if (role != null) {
					return getFlexoActor(role);
				}
				logger.warning("Unexpected " + variable);
				// return null;
			}*/
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
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getFlexoConcept();
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY)) {
				return this;
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.CONTAINER_PROPERTY)) {
				return getContainerFlexoConceptInstance();
			}

			if (getOwningVirtualModelInstance() != null) {
				return getOwningVirtualModelInstance().getValue(variable);
			}

			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
			// TODO here the code relies on switches, a dispatching approach will be safer (charlie)

			if (variable instanceof FlexoRoleBindingVariable && getFlexoConcept() != null) {
				FlexoRole role = ((FlexoRoleBindingVariable) variable).getFlexoRole();
				if (role != null) {
					setFlexoActor(value, role);
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
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				logger.warning("Forbidden write access " + FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY + " in " + this + " of "
						+ getClass());
				return;
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY)) {
				logger.warning("Forbidden write access " + FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY + " in " + this + " of "
						+ getClass());
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
					return deleteWithScheme(getFlexoConcept().getDefaultDeletionScheme());
				}
				else {
					// Generate on-the-fly default deletion scheme
					DeletionScheme ds = getFlexoConcept().generateDefaultDeletionScheme();
					return deleteWithScheme(ds);
				}
			}
			else {
				boolean returned = super.delete(context);
				getPropertyChangeSupport().firePropertyChange(getDeletedProperty(), false, true);
				return returned;
			}
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		public boolean deleteWithScheme(DeletionScheme deletionScheme) {
			if (isDeleted()) {
				return false;
			}

			FlexoConceptInstance container = getContainerFlexoConceptInstance();

			if (deletionScheme != null && deletionScheme.getControlGraph() != null) {
				try {
					deletionScheme.getControlGraph().execute(this);
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

			VirtualModelInstance<?, ?> vmi = getOwningVirtualModelInstance();
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
			boolean returned = super.delete();
			getPropertyChangeSupport().firePropertyChange(getDeletedProperty(), false, true);
			return returned;
		}

		/**
		 * Clone this FlexoConcept instance using default CloningScheme
		 */
		public FlexoConceptInstanceImpl cloneFlexoConceptInstance() {
			/*if (getFlexoConcept().getDefaultDeletionScheme() != null) {
				delete(getFlexoConcept().getDefaultDeletionScheme());
			} else {
				// Generate on-the-fly default deletion scheme
				delete(getFlexoConcept().generateDefaultDeletionScheme());
			}*/
			System.out.println("cloneFlexoConceptInstance() in FlexoConceptInstance");
			return null;
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
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
		public List<FlexoObject> objectsThatWillBeDeleted(DeletionScheme deletionScheme) {
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
		}

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

		@Override
		public boolean hasValidRenderer() {
			return getFlexoConcept() != null && getFlexoConcept().getInspector() != null
					&& getFlexoConcept().getInspector().getRenderer() != null && getFlexoConcept().getInspector().getRenderer().isSet()
					&& getFlexoConcept().getInspector().getRenderer().isValid();
		}

		private BindingValueChangeListener<String> rendererChangeListener = null;

		private boolean isComputingRenderer = false;

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
					Object obj = getFlexoConcept().getInspector().getRenderer().getBindingValue(this);

					if (rendererChangeListener == null) {
						rendererChangeListener = new BindingValueChangeListener<String>(getFlexoConcept().getInspector().getRenderer(),
								this) {
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
					else {
						if (obj != null) {
							return obj.toString();
						}
						else
							return EMPTY_STRING;
					}

				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
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
			else {
				String returned = getImplementedInterface().getSimpleName() + ":"
						+ (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "[ID=" + getFlexoID() + "]";
				toStringIsBuilding = false;
				return returned;
			}
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

		private VirtualModelInstanceModelFactory<?> deserializationFactory;

		@Override
		public void initializeDeserialization(VirtualModelInstanceModelFactory<?> factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			deserializationFactory = null;
		}

		public VirtualModelInstanceModelFactory<?> getDeserializationFactory() {
			return deserializationFactory;
		}

		@Override
		public final boolean hasNature(FlexoConceptInstanceNature nature) {
			return nature.hasNature(this);
		}

		@Override
		public <T> T execute(String expression) throws TypeMismatchException, NullReferenceException, InvocationTargetException {
			DataBinding<T> db = new DataBinding<>(expression, this, Object.class, BindingDefinitionType.GET);
			return db.getBindingValue(this);
		}

		@Override
		public <T> T execute(String expression, Object... parameters)
				throws TypeMismatchException, NullReferenceException, InvocationTargetException {
			return (T) MultipleParametersBindingEvaluator.evaluateBinding(expression, getBindingFactory(), this, parameters);
		}

		@Override
		public boolean isOf(String conceptName) {
			return getFlexoConcept().getName().equals(conceptName);
		}

	}
}
