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
package org.openflexo.foundation.view;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.expr.NotSettableContextException;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.viewpoint.CloningScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.editionaction.DeleteAction;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
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
public interface FlexoConceptInstance extends VirtualModelInstanceObject, Bindable, BindingEvaluationContext {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String EMPTY_STRING = "<emtpy>";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_URI_KEY = "flexoConceptURI";
	@PropertyIdentifier(type = Vector.class)
	public static final String ACTOR_LIST_KEY = "actorList";

	public FlexoConcept getFlexoConcept();

	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = FLEXO_CONCEPT_URI_KEY)
	@XMLAttribute
	public String getFlexoConceptURI();

	@Setter(FLEXO_CONCEPT_URI_KEY)
	public void setFlexoConceptURI(String flexoConceptURI);

	@Getter(value = ACTOR_LIST_KEY, cardinality = Cardinality.LIST, inverse = ActorReference.FLEXO_CONCEPT_INSTANCE_KEY)
	@XMLElement
	public List<ActorReference<?>> getActorList();

	@Setter(ACTOR_LIST_KEY)
	public void setActorList(List<ActorReference<?>> actorList);

	@Adder(ACTOR_LIST_KEY)
	public void addToActorList(ActorReference<?> aActorList);

	@Remover(ACTOR_LIST_KEY)
	public void removeFromActorList(ActorReference<?> aActorList);

	/**
	 * Sets the {@link VirtualModelInstance} where this object is declared and living
	 * 
	 * @return
	 */
	@Override
	public void setVirtualModelInstance(VirtualModelInstance vmInstance);

	// Debug method
	public String debug();

	public <T> T getFlexoActor(FlexoRole<T> patternRole);

	public <T> void setFlexoActor(T object, FlexoRole<T> patternRole);

	public <T> FlexoRole<T> getRoleForActor(T actor);

	public <T> void setObjectForFlexoRole(T object, FlexoRole<T> patternRole);

	public <T> void nullifyFlexoActor(FlexoRole<T> patternRole);

	public String getStringRepresentation();

	public static abstract class FlexoConceptInstanceImpl extends VirtualModelInstanceObjectImpl implements FlexoConceptInstance {

		private static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstance.class.getPackage().toString());

		private FlexoConcept flexoConcept;
		private String flexoConceptURI;
		private final Hashtable<FlexoRole<?>, ActorReference<?>> actors;
		private VirtualModelInstance vmInstance;

		private Vector<ActorReference<?>> deserializedActorList;

		/**
		 * Default constructor
		 */
		public FlexoConceptInstanceImpl(/*VirtualModelInstance virtualModelInstance*/) {
			super();
			/*if (virtualModelInstance != null) {
				setProject(virtualModelInstance.getProject());
			}
			vmInstance = virtualModelInstance;*/
			actors = new Hashtable<FlexoRole<?>, ActorReference<?>>();
			// actorList = new Vector<ActorReference<?>>();
			// initializeDeserialization(builder);
		}

		/*public FlexoConceptInstanceImpl(FlexoConcept aPattern, VirtualModelInstance virtualModelInstance, FlexoProject project) {
			super();
			if (virtualModelInstance != null) {
				setProject(virtualModelInstance.getProject());
			}
			this.vmInstance = virtualModelInstance;
			this.flexoConcept = aPattern;
			actors = new Hashtable<FlexoRole<?>, ActorReference<?>>();
		}*/

		@Override
		public FlexoProject getProject() {
			if (getView() != null) {
				return getView().getProject();
			}
			return super.getProject();
		}

		@Override
		public <T> T getFlexoActor(FlexoRole<T> patternRole) {
			if (patternRole == null) {
				logger.warning("Unexpected null patternRole");
				return null;
			}
			// logger.info(">>>>>>>> FlexoConceptInstance "+Integer.toHexString(hashCode())+" getPatternActor() actors="+actors);
			ActorReference<T> actorReference = (ActorReference<T>) actors.get(patternRole);
			// Pragmatic attempt to fix "inheritance issue...."
			for (FlexoConcept parentEP : this.getFlexoConcept().getParentFlexoConcepts()) {
				while (actorReference == null && parentEP != null) {
					if (parentEP != null) {
						FlexoRole ppFlexoRole = parentEP.getFlexoRole(patternRole.getName());
						if (ppFlexoRole == patternRole) {
							patternRole = (FlexoRole<T>) this.getFlexoConcept().getFlexoRole(ppFlexoRole.getName());
							actorReference = (ActorReference<T>) actors.get(patternRole);
						}
					}
					if (actorReference != null) {
						break;
					}

				}
			}
			if (actorReference != null) {
				return actorReference.getModellingElement();
			}
			return null;
		}

		@Override
		public <T> void setFlexoActor(T object, FlexoRole<T> patternRole) {
			setObjectForFlexoRole(object, patternRole);
		}

		@Override
		public <T> void nullifyFlexoActor(FlexoRole<T> patternRole) {
			setObjectForFlexoRole(null, patternRole);
		}

		@Override
		public <T> FlexoRole<T> getRoleForActor(T actor) {
			for (FlexoRole<?> role : actors.keySet()) {
				if (getFlexoActor(role) == actor) {
					return (FlexoRole<T>) role;
				}
			}
			return null;
		}

		@Override
		public <T> void setObjectForFlexoRole(T object, FlexoRole<T> patternRole) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(">>>>>>>> For patternRole: " + patternRole + " set " + object + " was " + getFlexoActor(patternRole));
			}
			T oldObject = getFlexoActor(patternRole);
			if (object != oldObject) {
				// Un-register last reference
				if (oldObject instanceof FlexoProjectObject) {
					((FlexoProjectObject) oldObject).unregisterFlexoConceptReference(this);
				}

				// Un-register last reference
				if (object instanceof FlexoProjectObject) {
					((FlexoProjectObject) object).registerFlexoConceptReference(this);
				}

				if (object != null) {
					ActorReference<T> actorReference = patternRole.makeActorReference(object, this);
					actors.put(patternRole, actorReference);
				} else {
					actors.remove(patternRole);
				}
				setChanged();
				notifyObservers(new FlexoActorChanged(this, patternRole, oldObject, object));
				// System.out.println("FlexoConceptInstance "+Integer.toHexString(hashCode())+" setObjectForPatternRole() actors="+actors);
				getPropertyChangeSupport().firePropertyChange(patternRole.getRoleName(), oldObject, object);
			}
		}

		@Override
		public String debug() {
			StringBuffer sb = new StringBuffer();
			sb.append("FlexoConcept: " + (flexoConcept != null ? flexoConcept.getName() : getFlexoConceptURI() + "[NOT_FOUND]") + "\n");
			sb.append("Instance: " + getFlexoID() + " hash=" + Integer.toHexString(hashCode()) + "\n");
			for (FlexoRole<?> patternRole : actors.keySet()) {
				FlexoProjectObject object = actors.get(patternRole);
				sb.append("Role: " + patternRole + " : " + object + "\n");
			}
			return sb.toString();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getVirtualModelInstance() != null && flexoConcept == null && StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getVirtualModelInstance().getVirtualModel().getFlexoConcept(flexoConceptURI);
			}
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			if (this.flexoConcept != flexoConcept) {
				FlexoConcept oldFlexoConcept = this.flexoConcept;
				this.flexoConcept = flexoConcept;
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

		public Hashtable<FlexoRole<?>, ActorReference<?>> getActors() {
			return actors;
		}

		/*public void setActors(Hashtable<FlexoRole<?>, ActorReference<?>> actors) {
			this.actors = actors;
		}

		public void setActorForKey(ActorReference<?> o, FlexoRole<?> key) {
			actors.put(key, o);
			ActorReference removeThis = null;
			for (ActorReference ref : actorList) {
				if (ref.getPatternRole() == o.getPatternRole()) {
					removeThis = ref;
				}
			}
			if (removeThis != null) {
				actorList.remove(removeThis);
			}
		}

		public void removeActorWithKey(FlexoRole<?> key) {
			actors.remove(key);
			ActorReference removeThis = null;
			for (ActorReference ref : actorList) {
				if (ref.getPatternRole() == key) {
					removeThis = ref;
				}
			}
			if (removeThis != null) {
				actorList.remove(removeThis);
			}
		}*/

		/*public String getStringValue(String inspectorEntryKey)
		{
			return "GET string value for "+inspectorEntryKey;
		}

		public void setStringValue(String inspectorEntryKey, String value)
		{
			System.out.println("SET string value for "+inspectorEntryKey+" value: "+value);
		}*/

		// WARNING: do no use outside context of serialization/deserialization (performance issues)
		@Override
		public Vector<ActorReference<?>> getActorList() {
			return new Vector<ActorReference<?>>(actors.values());
		}

		// WARNING: do no use outside context of serialization/deserialization
		public void setActorList(Vector<ActorReference<?>> deserializedActors) {
			for (ActorReference<?> ar : deserializedActors) {
				addToActorList(ar);
			}
		}

		// WARNING: do no use outside context of serialization/deserialization
		@Override
		public void addToActorList(ActorReference actorReference) {
			actorReference.setFlexoConceptInstance(this);
			if (actorReference.getFlexoRole() != null) {
				actors.put(actorReference.getFlexoRole(), actorReference);
			} else {
				if (deserializedActorList == null) {
					deserializedActorList = new Vector<ActorReference<?>>();
				}
				deserializedActorList.add(actorReference);
			}
		}

		// WARNING: do no use outside context of serialization/deserialization
		@Override
		public void removeFromActorList(ActorReference actorReference) {
			actorReference.setFlexoConceptInstance(null);
			if (actorReference.getFlexoRole() != null) {
				actors.remove(actorReference.getFlexoRole());
			}
		}

		public void finalizeDeserialization() {
			finalizeActorsDeserialization();
		}

		private void finalizeActorsDeserialization() {
			if (getFlexoConcept() != null && deserializedActorList != null) {
				for (ActorReference actorRef : deserializedActorList) {
					// System.out.println("Actor: " + actorRef.getPatternRoleName() + " pattern role = " + actorRef.getPatternRole() +
					// " name="
					// + actorRef.getPatternRoleName() + " ep=" + getFlexoConcept());
					if (actorRef.getFlexoRole() != null) {
						actors.put(actorRef.getFlexoRole(), actorRef);
					}
				}
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
			} else {
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
			FlexoRole pr = getFlexoConcept().getFlexoRole(variable.getVariableName());
			if (pr != null) {
				return getFlexoActor(pr);
			}
			logger.warning("Unexpected " + variable);
			return null;
		}

		// private boolean deleted = false;

		/*public boolean deleted() {
			return deleted;
		}

		@Override
		public boolean isDeleted() {
			return deleted();
		}*/

		/**
		 * Delete this FlexoConcept instance using default DeletionScheme
		 */
		@Override
		public boolean delete() {
			// Also implement properly #getDeletedProperty()
			if (getFlexoConcept().getDefaultDeletionScheme() != null) {
				return delete(getFlexoConcept().getDefaultDeletionScheme());
			} else {
				// Generate on-the-fly default deletion scheme
				return delete(getFlexoConcept().generateDefaultDeletionScheme());
			}
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		public boolean delete(DeletionScheme deletionScheme) {
			if (isDeleted()) {
				return false;
			}
			VirtualModelInstance container = getVirtualModelInstance();
			if (container != null) {
				container.removeFromFlexoConceptInstancesList(this);
			}
			// logger.warning("FlexoConceptInstance deletion !");
			// deleted = true;
			/*if (getFlexoConcept().getPrimaryRepresentationRole() != null) {
				Object primaryPatternActor = getPatternActor(getFlexoConcept().getPrimaryRepresentationRole());
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
					logger.warning("Actor for role " + getFlexoConcept().getPrimaryRepresentationRole() + " is not a FlexoModelObject: is "
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
					.getPrimaryRepresentationRole()), null, null);
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
			for (FlexoRole<?> pr : getFlexoConcept().getFlexoRoles()) {
				if (pr.defaultBehaviourIsToBeDeleted() && getFlexoActor(pr) instanceof FlexoObject) {
					returned.add((FlexoObject) getFlexoActor(pr));
				}
			}
			return returned;
		}

		/**
		 * Delete this FlexoConcept instance using supplied DeletionScheme
		 */
		public List<FlexoObject> objectsThatWillBeDeleted(DeletionScheme deletionScheme) {
			Vector<FlexoObject> returned = new Vector<FlexoObject>();
			for (EditionAction editionAction : deletionScheme.getActions()) {
				if (editionAction instanceof DeleteAction) {
					DeleteAction deleteAction = (DeleteAction) editionAction;

					returned.add((FlexoObject) getFlexoActor(deleteAction.getFlexoRole()));
				}
			}
			return returned;
		}

		/*@Override
		public String getDeletedProperty() {
			// when delete will be implemented, a notification will need to be sent and this method should reflect the name of the
			// property of that notification
			return DELETED_PROPERTY;
		}*/

		// @Override
		// public String getDisplayableName() {
		/*for (GraphicalElementPatternRole pr : getFlexoConcept().getGraphicalElementPatternRoles()) {
			if (pr != null && pr.getLabel().isSet() && pr.getLabel().isValid()) {
				try {
					return (String) pr.getLabel().getBindingValue(this);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}*/
		// return getFlexoConcept().getName();
		// return getStringRepresentation();
		// }

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}

		/*@Override
		public String getFullyQualifiedName() {
			return getVirtualModelInstance().getFullyQualifiedName() + "." + getFlexoConcept().getURI() + "." + getFlexoID();
		}

		@Override
		public String getClassNameKey() {
			return "flexo_concept_instance";
		}*/

		@Override
		public VirtualModelInstance getResourceData() {
			return getVirtualModelInstance();
		}

		@Override
		public VirtualModelInstance getVirtualModelInstance() {
			return vmInstance;
		}

		@Override
		public void setVirtualModelInstance(VirtualModelInstance vmInstance) {
			this.vmInstance = vmInstance;
		}

		protected boolean hasValidRenderer() {
			return getFlexoConcept() != null && getFlexoConcept().getInspector() != null
					&& getFlexoConcept().getInspector().getRenderer() != null && getFlexoConcept().getInspector().getRenderer().isSet()
					&& getFlexoConcept().getInspector().getRenderer().isValid();
		}

		@Override
		public String getStringRepresentation() {
			if (hasValidRenderer()) {
				try {
					// System.out.println("Evaluating " + getFlexoConcept().getInspector().getRenderer() + " for " + this);
					Object obj = getFlexoConcept().getInspector().getRenderer().getBindingValue(new BindingEvaluationContext() {
						@Override
						public Object getValue(BindingVariable variable) {
							if (variable.getVariableName().equals("instance")) {
								return FlexoConceptInstanceImpl.this;
							}
							logger.warning("Unexpected variable " + variable);
							return null;
						}
					});
					if (obj instanceof String) {
						return (String) obj;
					} else {
						if (obj != null) {
							return obj.toString();
						} else
							return EMPTY_STRING;
					}
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return extendedStringRepresentation();
		}

		public String extendedStringRepresentation() {
			StringBuffer sb = new StringBuffer();
			sb.append(getFlexoConcept().getName() + ": ");
			boolean isFirst = true;
			for (ActorReference ref : actors.values()) {
				if (ref.getModellingElement() != null) {
					sb.append((isFirst ? "" : ", ") + ref.getRoleName() + "=" + ref.getModellingElement().toString());
				} else {
					sb.append((isFirst ? "" : ", ") + ref.getRoleName() + "=" + "No object found");
				}
				isFirst = false;
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ":" + (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "_"
					+ getFlexoID() + (hasValidRenderer() ? " [" + getStringRepresentation() + "]" : "");
		}

	}
}
