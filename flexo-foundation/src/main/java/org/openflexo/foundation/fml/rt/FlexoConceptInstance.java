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
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
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
public interface FlexoConceptInstance extends VirtualModelInstanceObject, Bindable, RunTimeEvaluationContext {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String EMPTY_STRING = "<emtpy>";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_URI_KEY = "flexoConceptURI";
	@PropertyIdentifier(type = ActorReference.class, cardinality = Cardinality.LIST)
	public static final String ACTORS_KEY = "actors";

	@PropertyIdentifier(type = AbstractVirtualModelInstance.class)
	public static final String OWNING_VIRTUAL_MODEL_INSTANCE_KEY = "owningVirtualModelInstance";

	/**
	 * Return the {@link VirtualModelInstance} where this FlexoConceptInstance is instanciated (result might be different from
	 * {@link #getVirtualModelInstance()}, which is The {@link VirtualModelInstanceObject} API)
	 * 
	 * @return
	 */
	@Getter(value = OWNING_VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract AbstractVirtualModelInstance<?, ?> getOwningVirtualModelInstance();

	@Setter(OWNING_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setOwningVirtualModelInstance(AbstractVirtualModelInstance<?, ?> virtualModelInstance);

	public FlexoConcept getFlexoConcept();

	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = FLEXO_CONCEPT_URI_KEY)
	@XMLAttribute
	public String getFlexoConceptURI();

	@Setter(FLEXO_CONCEPT_URI_KEY)
	public void setFlexoConceptURI(String flexoConceptURI);

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
			actors = new HashMap<String, List<ActorReference<?>>>();
			actorLists = new HashMap<FlexoRole<?>, List<?>>();
			variables = new HashMap<String, Object>();
		}

		@Override
		public FlexoProject getProject() {
			if (getView() != null && getView() != this) {
				return getView().getProject();
			}
			return super.getProject();
		}

		@Override
		public void debug(String aLogString) {
			if (getFlexoEditor() != null) {
				getFlexoEditor().getFMLConsole().debug(aLogString);
			}
		}

		@Override
		public void log(String aLogString, LogLevel logLevel) {
			if (getFlexoEditor() != null) {
				getFlexoEditor().getFMLConsole().log(aLogString, logLevel);
			}
		}

		private FlexoEditor getFlexoEditor() {
			if (getProject() != null && getServiceManager() != null) {
				return getServiceManager().getProjectLoaderService().getEditorForProject(getProject());
			}
			return null;
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
			protected HashMap<String, Object> localVariables = new HashMap<String, Object>();

			@Override
			public FlexoConceptInstance getFlexoConceptInstance() {
				return FlexoConceptInstanceImpl.this;
			}

			@Override
			public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance() {
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
			public void debug(String aLogString) {
				getFlexoConceptInstance().debug(aLogString);
			}

			@Override
			public void log(String aLogString, LogLevel logLevel) {
				getFlexoConceptInstance().log(aLogString, logLevel);
			}
		}

		@Override
		public <T> T getFlexoPropertyValue(FlexoProperty<T> flexoProperty) {
			if (flexoProperty instanceof FlexoRole) {
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
			T oldValue = getFlexoPropertyValue(flexoProperty);
			if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
				if (flexoProperty instanceof FlexoRole) {
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
					FMLControlGraph setControlGraph = ((GetSetProperty<T>) flexoProperty).getSetControlGraph();
					try {
						// TODO: handle value beeing set, both in BindingModel AND he in LocalRunTimeEvaluationContext
						RunTimeEvaluationContext localEvaluationContext = new LocalRunTimeEvaluationContext();
						T returnedValue = null;
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

			if (flexoRole.getFlexoConcept() == getFlexoConcept().getOwningVirtualModel()) {
				// logger.warning("Should not we delegate this to owning VM ???");
				return getOwningVirtualModelInstance().getFlexoActor(flexoRole);
			}
			List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

			if (actorReferences != null && actorReferences.size() > 0) {
				return actorReferences.get(0).getModellingElement();
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

			List<ActorReference<T>> actorReferences = (List) actors.get(flexoRole.getRoleName());

			List<T> returned = (List) actorLists.get(flexoRole);
			if (returned == null) {
				List<T> existingList = new ArrayList<T>();
				if (actorReferences != null) {
					for (ActorReference<T> ref : actorReferences) {
						existingList.add(ref.getModellingElement());
					}
				}
				// CAUTION: tricky area
				// To be able to generically handle adding in a list of roles, we should override add method here
				returned = new ArrayList<T>(existingList) {
					@Override
					public boolean add(T e) {
						// System.out.println("Adding element "+e+" to list for actor " + flexoRole);
						addToFlexoActors(e, flexoRole);
						return super.add(e);
					}
				};
				actorLists.put(flexoRole, returned);
			}
			return returned;
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
		 * @param flexoProperty
		 *            the property to be considered
		 */
		@Override
		public <T> void setFlexoActor(T object, FlexoRole<T> flexoRole) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(
						">>>>>>>>> setObjectForFlexoRole flexoRole: " + flexoRole + " set " + object + " was " + getFlexoActor(flexoRole));
			}
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
						for (ActorReference<T> actorReference : new ArrayList<ActorReference<T>>(references)) {
							removeFromActors(actorReference);
						}
					}
				}

				if (object != null && !done) {
					ActorReference<T> actorReference = flexoRole.makeActorReference(object, this);
					addToActors(actorReference);
				}

				setIsModified();

				setChanged();
				notifyObservers(new FlexoActorChanged(this, flexoRole, oldObject, object));
				// System.out.println("FlexoConceptInstance "+Integer.toHexString(hashCode())+" setObjectForPatternProperty()
				// actors="+actors);

				getPropertyChangeSupport().firePropertyChange(flexoRole.getRoleName(), oldObject, object);

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

				for (ActorReference<T> actorReference : new ArrayList<ActorReference<T>>(references)) {
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
		public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance() {
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
				references = new ArrayList<ActorReference<T>>();
				actors.put(roleName, (List) references);
			}
			return references;
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
			DataBinding<Object> vpdb = new DataBinding<Object>(expression);
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
			DataBinding<Object> vpdb = new DataBinding<Object>(expression);
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
			else if (variable instanceof FlexoPropertyBindingVariable && getFlexoConcept() != null
					&& ((FlexoPropertyBindingVariable) variable).getFlexoProperty().getFlexoConcept() == getFlexoConcept()) {
				return ((FlexoPropertyBindingVariable) variable).getValue(this);
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY)) {
				return getFlexoConcept();
			}
			else if (variable.getVariableName().equals(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY)) {
				return this;
			}

			if (getOwningVirtualModelInstance() != null) {
				return getOwningVirtualModelInstance().getValue(variable);
			}

			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
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
				FlexoProperty property = getFlexoConcept().getAccessibleProperty(variable.getVariableName());
				logger.warning("Not implemented: setValue() for " + variable);
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
					return deleteWithScheme(getFlexoConcept().generateDefaultDeletionScheme());
				}
			}
			else {
				return super.delete(context);
			}
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		public boolean deleteWithScheme(DeletionScheme deletionScheme) {
			if (isDeleted()) {
				return false;
			}
			AbstractVirtualModelInstance<?, ?> container = getOwningVirtualModelInstance();
			if (container != null) {
				container.removeFromFlexoConceptInstances(this);
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
			return super.delete();
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
			Vector<FlexoObject> returned = new Vector<FlexoObject>();
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
			Vector<FlexoObject> returned = new Vector<FlexoObject>();
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
		public AbstractVirtualModelInstance<?, ?> getResourceData() {
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
			sb.append((getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + ": ");
			boolean isFirst = true;
			for (List<ActorReference<?>> refList : actors.values()) {
				for (ActorReference<?> ref : refList) {
					if (ref.getModellingElement() != null) {
						sb.append((isFirst ? "" : ", ") + ref.getRoleName() + "=" + ref.getModellingElement().toString());
					}
					else {
						sb.append((isFirst ? "" : ", ") + ref.getRoleName() + "=" + "No object found");
					}
					isFirst = false;
				}
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return getImplementedInterface().getSimpleName() + ":" + (getFlexoConcept() != null ? getFlexoConcept().getName() : "null")
					+ "[ID=" + getFlexoID() + "]" + (hasValidRenderer() ? " [" + getStringRepresentation() + "]" : "");
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

	}
}
