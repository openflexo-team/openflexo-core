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
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingValueChangeListener;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.binding.SettableBindingEvaluationContext;
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
public interface FlexoConceptInstance extends VirtualModelInstanceObject, Bindable, SettableBindingEvaluationContext {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String EMPTY_STRING = "<emtpy>";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_URI_KEY = "flexoConceptURI";
	@PropertyIdentifier(type = ActorReference.class, cardinality = Cardinality.LIST)
	public static final String ACTORS_KEY = "actors";

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

	public <T> T getFlexoActor(FlexoRole<T> patternRole);

	public <T> void setFlexoActor(T object, FlexoRole<T> patternRole);

	public <T> FlexoRole<T> getRoleForActor(T actor);

	public <T> void setObjectForFlexoRole(T object, FlexoRole<T> patternRole);

	public <T> void nullifyFlexoActor(FlexoRole<T> patternRole);

	public String getStringRepresentation();

	public static abstract class FlexoConceptInstanceImpl extends VirtualModelInstanceObjectImpl implements FlexoConceptInstance {

		private static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstance.class.getPackage().toString());

		protected FlexoConcept flexoConcept;
		protected String flexoConceptURI;
		// This HashMap stores ActorReference associated with role name as String
		private final HashMap<String, ActorReference<?>> actors;

		/**
		 * Default constructor
		 */
		public FlexoConceptInstanceImpl(/*VirtualModelInstance virtualModelInstance*/) {
			super();
			actors = new HashMap<String, ActorReference<?>>();
		}

		@Override
		public FlexoProject getProject() {
			if (getView() != null) {
				return getView().getProject();
			}
			return super.getProject();
		}

		@Override
		public <T> T getFlexoActor(FlexoRole<T> flexoRole) {
			if (flexoRole == null) {
				logger.warning("Unexpected null patternRole");
				return null;
			}
			// logger.info(">>>>>>>> FlexoConceptInstance "+Integer.toHexString(hashCode())+" getPatternActor() actors="+actors);
			ActorReference<T> actorReference = (ActorReference<T>) actors.get(flexoRole.getRoleName());

			if (actorReference != null) {
				return actorReference.getModellingElement();
			}
			// Pragmatic attempt to fix "inheritance issue...."
			else if (actorReference == null) {
				getParentActorReference(getFlexoConcept(), flexoRole);
			}
			return null;
		}

		private <T> ActorReference<T> getParentActorReference(FlexoConcept flexoConcept, FlexoRole<T> flexoRole) {
			ActorReference<T> actorReference;
			for (FlexoConcept parentFlexoConcept : this.getFlexoConcept().getParentFlexoConcepts()) {
				if (parentFlexoConcept != null) {
					FlexoRole ppFlexoRole = parentFlexoConcept.getFlexoRole(flexoRole.getName());
					if (ppFlexoRole == flexoRole) {
						flexoRole = (FlexoRole<T>) this.getFlexoConcept().getFlexoRole(ppFlexoRole.getName());
						actorReference = (ActorReference<T>) actors.get(flexoRole);
						if (actorReference != null) {
							return actorReference;
						}
					}
				}
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
			for (FlexoRole<?> role : getFlexoConcept().getFlexoRoles()) {
				if (getFlexoActor(role) == actor) {
					return (FlexoRole<T>) role;
				}
			}
			return null;
		}

		@Override
		public <T> void setObjectForFlexoRole(T object, FlexoRole<T> flexoRole) {

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(">>>>>>>>> setObjectForFlexoRole flexoRole: " + flexoRole + " set " + object + " was "
						+ getFlexoActor(flexoRole));
			}
			T oldObject = getFlexoActor(flexoRole);
			if (object != oldObject) {
				// Un-register last reference
				if (oldObject instanceof FlexoProjectObject) {
					((FlexoProjectObject) oldObject).unregisterFlexoConceptReference(this);
				}

				// Un-register last reference
				if (object instanceof FlexoProjectObject) {
					((FlexoProjectObject) object).registerFlexoConceptReference(this);
				}

				// We manage here the ActorReference according to old and new objects

				if (oldObject != null) {
					ActorReference<T> actorReference = (ActorReference<T>) actors.get(flexoRole.getRoleName());
					if (object == null) {
						removeFromActors(actorReference);
					} else {
						actorReference.setModellingElement(object);
					}
				} else /*if (object != null)*/{
					// We are sure object is not null, becase oldObject is null and object != oldObject
					ActorReference<T> actorReference = flexoRole.makeActorReference(object, this);
					addToActors(actorReference);
				}

				setChanged();
				notifyObservers(new FlexoActorChanged(this, flexoRole, oldObject, object));
				// System.out.println("FlexoConceptInstance "+Integer.toHexString(hashCode())+" setObjectForPatternRole() actors="+actors);

				getPropertyChangeSupport().firePropertyChange(flexoRole.getRoleName(), oldObject, object);
			}
		}

		@Override
		public String debug() {
			StringBuffer sb = new StringBuffer();
			sb.append("FlexoConcept: " + (flexoConcept != null ? flexoConcept.getName() : getFlexoConceptURI() + "[NOT_FOUND]") + "\n");
			sb.append("Instance: " + getFlexoID() + " hash=" + Integer.toHexString(hashCode()) + "\n");
			for (FlexoRole<?> patternRole : getFlexoConcept().getFlexoRoles()) {
				FlexoProjectObject object = actors.get(patternRole);
				sb.append("Role: " + patternRole + " : " + object + "\n");
			}
			return sb.toString();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().getVirtualModel() != null && flexoConcept == null
					&& StringUtils.isNotEmpty(flexoConceptURI)) {
				flexoConcept = getVirtualModelInstance().getVirtualModel().getFlexoConcept(flexoConceptURI);
				if (flexoConcept == null) {
					System.out.println("Could not find FlexoConcept with uri=" + flexoConceptURI);
				}
			}
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			if (this.flexoConcept != flexoConcept) {
				FlexoConcept oldFlexoConcept = this.flexoConcept;
				this.flexoConcept = flexoConcept;
				if (getVirtualModelInstance() != null) {
					getVirtualModelInstance().flexoConceptInstanceChangedFlexoConcept(this, oldFlexoConcept, flexoConcept);
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

		@Override
		public void addToActors(ActorReference<?> actorReference) {

			//System.out.println("***** addToActors " + actorReference);

			if (actorReference == null) {
				logger.warning("Could not register null ActorReference");
				return;
			}

			if (actorReference.getRoleName() == null) {
				logger.warning("Could not register ActorReference with null FlexoRole: " + actorReference);
				return;
			} else {
				actors.put(actorReference.getRoleName(), actorReference);
				performSuperAdder(ACTORS_KEY, actorReference);
			}

		}

		@Override
		public void removeFromActors(ActorReference<?> actorReference) {

			if (actorReference.getRoleName() == null) {
				logger.warning("Could not unregister ActorReference with null FlexoRole: " + actorReference);
				return;
			} else {
				actors.remove(actorReference.getRoleName());
				performSuperRemover(ACTORS_KEY, actorReference);
			}

			actorReference.setFlexoConceptInstance(null);
			if (actorReference.getFlexoRole() != null) {
				actors.remove(actorReference.getFlexoRole());
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
			if (variable.getVariableName().equals("instance")) {
				return FlexoConceptInstanceImpl.this;
			}
			logger.warning("Unexpected " + variable);
			return null;
		}

		@Override
		public void setValue(Object value, BindingVariable variable) {
			FlexoRole pr = getFlexoConcept().getFlexoRole(variable.getVariableName());
			if (pr != null) {
				setFlexoActor(value, pr);
			} else {
				logger.warning("Unexpected " + variable);
			}
		}

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
				container.removeFromFlexoConceptInstances(this);
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

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}

		@Override
		public VirtualModelInstance getResourceData() {
			return getVirtualModelInstance();
		}

		protected boolean hasValidRenderer() {
			return getFlexoConcept() != null && getFlexoConcept().getInspector() != null
					&& getFlexoConcept().getInspector().getRenderer() != null && getFlexoConcept().getInspector().getRenderer().isSet()
					&& getFlexoConcept().getInspector().getRenderer().isValid();
		}

		private BindingValueChangeListener<String> rendererChangeListener = null;

		@Override
		public String getStringRepresentation() {
			if (hasValidRenderer()) {
				try {
					Object obj = getFlexoConcept().getInspector().getRenderer().getBindingValue(this);

					/*if (obj == null) {
						System.out.println("J'ai deja un premier probleme la");
						System.out.println("Evaluating " + getFlexoConcept().getInspector().getRenderer());
						System.out.println("Getting " + obj);
						FlexoConceptInstance value = (FlexoConceptInstance) getValue(new BindingVariable("instance",
								FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConcept())));
						System.out.println("value="
								+ (value != null ? value.getClass() + "/" + Integer.toHexString(value.hashCode()) : null));
						System.out.println("this=" + Integer.toHexString(hashCode()));
						DataBinding<String> db = getFlexoConcept().getInspector().getRenderer();
						if (db.getExpression() instanceof BindingValue) {
							BindingValue bv = (BindingValue) db.getExpression();
							System.out.println("Binding: " + db);
							System.out.println("BindingValue: " + bv);
							System.out.println("BindingVariable: " + bv.getBindingVariable());
							System.out.println("BindingPath: " + bv.getBindingPath());
							if (bv.getBindingPath().get(0) instanceof FlexoConceptPatternRolePathElement) {
								FlexoConceptPatternRolePathElement pathElement = (FlexoConceptPatternRolePathElement) bv.getBindingPath()
										.get(0);
								System.out.println("PathElement: " + pathElement);
								System.out.println("FlexoRole: " + pathElement.getFlexoRole());
								System.out.println("On devrait trouver: " + pathElement.getBindingValue(value, this));
								System.out.println("Les actors: " + value.getActors());
								FlexoRole role = value.getFlexoConcept().getFlexoRole("name");
								System.out.println("role=" + role);
								System.out.println("hop: " + value.getFlexoActor(role));
							}
						}
					}*/
					if (rendererChangeListener == null) {
						rendererChangeListener = new BindingValueChangeListener<String>(getFlexoConcept().getInspector().getRenderer(),
								this) {
							@Override
							public void bindingValueChanged(Object source, String newValue) {
								/*System.out.println(" bindingValueChanged() detected for string representation of "
										+ FlexoConceptInstanceImpl.this + " " + getFlexoConcept().getInspector().getRenderer()
										+ " with newValue=" + newValue + " source=" + source);*/
								if (!isDeleted) {
									// We have here detected that the string representation of this concept instance has changed
									getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, newValue);
								}
							}
						};
					}
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
