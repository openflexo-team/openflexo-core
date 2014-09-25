/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2013-2014 Openflexo
 *
 * This file is part of OpenFlexo Software Infrastructure.
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
package org.openflexo.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.action.AddFlexoProperty;
import org.openflexo.foundation.action.DeleteFlexoProperty;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoActionizer;
import org.openflexo.foundation.action.SortFlexoProperties;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.CloneableProxyObject;
import org.openflexo.model.factory.DeletableProxyObject;
import org.openflexo.model.factory.EmbeddingType;
import org.openflexo.model.factory.KeyValueCoding;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.validation.Validable;
import org.openflexo.toolbox.HTMLUtils;

/**
 * Super class for any object involved in Openflexo-Core (model layer)<br>
 * 
 * This abstract class represents an object. (a "model" in the model-view paradigm)<br>
 * 
 * This class provides default implementation for validation (see {@link Validable} interface). When relevant, just extends interface
 * {@link Validable} and implements both methods {@link Validable#getDefaultValidationModel()} and
 * {@link Validable#getAllEmbeddedValidableObjects()}
 * 
 * @author sguerin
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoObject.FlexoObjectImpl.class)
// TODO: remove ReferenceOwner declaration and create a new class
public abstract interface FlexoObject extends AccessibleProxyObject, DeletableProxyObject, CloneableProxyObject, KeyValueCoding, Validable {
	@PropertyIdentifier(type = String.class)
	public static final String USER_IDENTIFIER_KEY = "userIdentifier";
	@PropertyIdentifier(type = long.class)
	public static final String FLEXO_ID_KEY = "flexoID";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = boolean.class)
	public static final String HAS_SPECIFIC_DESCRIPTIONS_KEY = "hasSpecificDescriptions";
	@PropertyIdentifier(type = Map.class)
	public static final String SPECIFIC_DESCRIPTIONS_KEY = "specificDescriptions";
	@PropertyIdentifier(type = Vector.class)
	public static final String CUSTOM_PROPERTIES_KEY = "customProperties";

	@Getter(value = USER_IDENTIFIER_KEY)
	@XMLAttribute(xmlTag = "userID")
	public String getUserIdentifier();

	@Setter(USER_IDENTIFIER_KEY)
	public void setUserIdentifier(String userIdentifier);

	@Getter(value = FLEXO_ID_KEY, defaultValue = "0")
	@XMLAttribute
	// Here, we dont want to have the FlexoID duplicated (never a good idea for
	// an ID !!!)
	// We delegate here the computation of a new ID to the PamelaResource
	@CloningStrategy(value = StrategyType.FACTORY, factory = "obtainNewFlexoID()")
	public long getFlexoID();

	/**
	 * Obtain new flexoID by requesting it to the PamelaResource
	 * 
	 * @return
	 */
	public long obtainNewFlexoID();

	@Setter(FLEXO_ID_KEY)
	public void setFlexoID(long flexoID);

	@Getter(value = DESCRIPTION_KEY)
	@XMLAttribute
	public String getDescription();

	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	public boolean hasDescription();

	@Getter(value = HAS_SPECIFIC_DESCRIPTIONS_KEY, defaultValue = "false")
	@XMLAttribute(xmlTag = "useSpecificDescriptions")
	public boolean getHasSpecificDescriptions();

	@Setter(HAS_SPECIFIC_DESCRIPTIONS_KEY)
	public void setHasSpecificDescriptions(boolean hasSpecificDescriptions);

	public List<String> getSpecificDescriptionKeys();

	@Getter(value = SPECIFIC_DESCRIPTIONS_KEY, ignoreType = true)
	public Map<String, String> getSpecificDescriptions();

	@Setter(SPECIFIC_DESCRIPTIONS_KEY)
	public void setSpecificDescriptions(Map<String, String> specificDescriptions);

	public boolean hasSpecificDescriptionForKey(String key);

	public String getSpecificDescriptionForKey(String key);

	@Adder(SPECIFIC_DESCRIPTIONS_KEY)
	public void setSpecificDescriptionsForKey(String description, String key);

	@Remover(SPECIFIC_DESCRIPTIONS_KEY)
	public void removeSpecificDescriptionsWithKey(String key);

	@Getter(value = CUSTOM_PROPERTIES_KEY, cardinality = Cardinality.LIST, inverse = FlexoProperty.OWNER_KEY)
	@XMLElement
	public List<FlexoProperty> getCustomProperties();

	@Setter(CUSTOM_PROPERTIES_KEY)
	public void setCustomProperties(List<FlexoProperty> customProperties);

	@Adder(CUSTOM_PROPERTIES_KEY)
	public void addToCustomProperties(FlexoProperty aCustomPropertie);

	@Remover(CUSTOM_PROPERTIES_KEY)
	public void removeFromCustomProperties(FlexoProperty aCustomPropertie);

	@Finder(attribute = FlexoProperty.NAME_KEY, collection = CUSTOM_PROPERTIES_KEY)
	public FlexoProperty getPropertyNamed(String name);

	public List<FlexoActionType<?, ?, ?>> getActionList();

	public void addToReferencers(FlexoObjectReference<? extends FlexoObject> ref);

	public void removeFromReferencers(FlexoObjectReference<? extends FlexoObject> ref);

	public List<FlexoObjectReference<?>> getReferencers();

	public boolean hasSpecificHelp(String key);

	/**
	 * Return the list of all references to FlexoConceptInstance where this FlexoObject is involved in a FlexoRole
	 * 
	 * @return
	 */
	// public List<FlexoObjectReference<FlexoConceptInstance>>
	// getFlexoConceptReferences();

	// public void
	// setFlexoConceptReferences(List<FlexoModelObjectReference<FlexoConceptInstance>>
	// flexoConceptReferences);

	/*public void addToFlexoConceptReferences(final FlexoObjectReference<FlexoConceptInstance> ref);

	public void removeFromFlexoConceptReferences(FlexoObjectReference<FlexoConceptInstance> ref);
	*/

	/**
	 * Return the {@link FlexoConceptInstance}
	 * 
	 * @param flexoConceptId
	 * @param instanceId
	 * @return
	 */
	// public FlexoConceptInstance getFlexoConceptInstance(String
	// flexoConceptId, long instanceId);

	/**
	 * Return FlexoConceptInstance matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI<br>
	 * If many FlexoConceptInstance are declared in this FlexoProjectObject, return first one
	 * 
	 * @param flexoConceptId
	 * @return
	 */
	// public FlexoConceptInstance getFlexoConceptInstance(String
	// flexoConceptId);

	/**
	 * Return FlexoConceptInstance matching supplied FlexoConcept<br>
	 * If many FlexoConceptInstance are declared in this FlexoProjectObject, return first one
	 * 
	 * @param flexoConceptId
	 * @return
	 */
	// public FlexoConceptInstance getFlexoConceptInstance(FlexoConcept
	// flexoConcept);

	// public void registerFlexoConceptReference(FlexoConceptInstance
	// flexoConceptInstance);

	// public void unregisterFlexoConceptReference(FlexoConceptInstance
	// flexoConceptInstance);

	public Class<?> getImplementedInterface();

	@Deprecated
	public void setChanged();

	@Deprecated
	public void notifyObservers();

	@Deprecated
	public void notifyObservers(DataModification arg);

	public static abstract class FlexoObjectImpl extends FlexoObservable implements FlexoObject {

		private static final Logger logger = Logger.getLogger(FlexoObject.class.getPackage().getName());

		protected boolean isDeleted = false;
		private boolean ignoreNotifications = false;
		// private boolean isModified = false;
		private Date lastMemoryUpdate = null;

		private Object context;

		private String description;

		private Map<String, String> specificDescriptions;
		private List<FlexoProperty> customProperties;
		private boolean hasSpecificDescriptions = false;

		/**
		 * A map that stores the different declared actions for each class
		 */
		private static final Map<Class, List<FlexoActionType<?, ?, ?>>> _declaredActionsForClass = new Hashtable<Class, List<FlexoActionType<?, ?, ?>>>();

		/**
		 * A map that stores all the actions for each class (computed with the inheritance of each class)
		 */
		private static final Hashtable<Class, List<FlexoActionType<?, ?, ?>>> _actionListForClass = new Hashtable<Class, List<FlexoActionType<?, ?, ?>>>();

		public static FlexoActionizer<AddFlexoProperty, FlexoObject, FlexoObject> addFlexoPropertyActionizer;

		public static FlexoActionizer<DeleteFlexoProperty, FlexoProperty, FlexoProperty> deleteFlexoPropertyActionizer;

		public static FlexoActionizer<SortFlexoProperties, FlexoObject, FlexoObject> sortFlexoPropertiesActionizer;

		/**
		 * This list contains all EPI's by reference
		 */
		// TODO: merge with referencers
		// private List<FlexoObjectReference<FlexoConceptInstance>>
		// flexoConceptReferences;

		private final List<FlexoObjectReference<?>> referencers;

		/**
		 * Default constructor for {@link FlexoObject}
		 */
		public FlexoObjectImpl() {
			specificDescriptions = new TreeMap<String, String>();
			customProperties = new ArrayList<FlexoProperty>();
			// flexoConceptReferences = new
			// ArrayList<FlexoObjectReference<FlexoConceptInstance>>();
			referencers = new ArrayList<FlexoObjectReference<?>>();
		}

		@Override
		public final String getDeletedProperty() {
			return DELETED_PROPERTY;
		}

		/**
		 * Returns fully qualified name for this object NOTE: not used anymore, should disappear
		 * 
		 * @return
		 */
		// @Deprecated
		// public abstract String getFullyQualifiedName();

		/**
		 * Abstract implementation of delete<br>
		 * This method should be overriden.<br>
		 * At this level, only manage {@link #isDeleted()} feature
		 * 
		 * @return flag indicating if deletion has successfully been performed
		 */
		public boolean delete() {

			/*for (FlexoObjectReference<FlexoConceptInstance> ref : new ArrayList<FlexoObjectReference<FlexoConceptInstance>>(
					flexoConceptReferences)) {
				FlexoConceptInstance epi = ref.getObject();
				if (epi != null) {
					epi.nullifyFlexoActor(epi.getRoleForActor(this));
				}
			}

			flexoConceptReferences.clear();
			flexoConceptReferences = null;*/

			setChanged();
			notifyObservers(new ObjectDeleted(this));
			if (isDeleted()) {
				return false;
			}
			isDeleted = true;
			return true;
		}

		/**
		 * Return a flag indicating if this object was deleted
		 * 
		 * @return
		 */
		@Override
		public boolean isDeleted() {
			return isDeleted;
		}

		/**
		 * Test if changing a value from oldValue to newValue is significant
		 * 
		 * @param oldValue
		 * @param newValue
		 * @return
		 */
		protected <T> boolean requireChange(T oldValue, T newValue) {
			return oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null
					&& !oldValue.equals(newValue);
		}

		/**
		 * Test if changing a value from old to newString is significant
		 * 
		 * @param oldValue
		 * @param newValue
		 * @return
		 */
		// TODO: to be merged with requireChange() ???
		public static boolean stringHasChanged(String old, String newString) {
			return old == null && newString != null || old != null && !old.equals(newString);
		}

		@Override
		public void addToReferencers(FlexoObjectReference<? extends FlexoObject> ref) {
			if (referencers != null && !referencers.contains(ref)) {
				referencers.add(ref);
			}
		}

		@Override
		public void removeFromReferencers(FlexoObjectReference<? extends FlexoObject> ref) {
			if (referencers != null) {
				referencers.remove(ref);
			}
		}

		@Override
		public List<FlexoObjectReference<?>> getReferencers() {
			return referencers;
		}

		/*@Override
		public List<FlexoObjectReference<FlexoConceptInstance>> getFlexoConceptReferences() {
			return flexoConceptReferences;
		}*/

		/*public void setFlexoConceptReferences(List<FlexoModelObjectReference<FlexoConceptInstance>> flexoConceptReferences) {
			this.flexoConceptReferences = flexoConceptReferences;
		}*/

		/*@Override
		public void addToFlexoConceptReferences(final FlexoObjectReference<FlexoConceptInstance> ref) {
			// SwingUtilities.invokeLater(new Runnable() {
			// @Override
			// public void run() {
			// try {
			// ref.getObject();
			// } catch (ClassCastException e) {
			// System.out.println("Je tiens le coupable !!!");
			// }
			// }
			// });
			// logger.info("****************** addToFlexoConceptReferences() with " + ref);
			// ref.setOwner(this);
			flexoConceptReferences.add(ref);
			setChanged();
			notifyObservers(new DataModification("flexoConceptReferences", null, ref));
		}*/

		/*@Override
		public void removeFromFlexoConceptReferences(FlexoObjectReference<FlexoConceptInstance> ref) {
			// ref.setOwner(null);
			flexoConceptReferences.remove(ref);
			setChanged();
			notifyObservers(new DataModification("flexoConceptReferences", ref, null));
		}*/

		/*@Override
		public FlexoConceptInstance getFlexoConceptInstance(String flexoConceptId, long instanceId) {
			if (flexoConceptId == null) {
				return null;
			}
			if (flexoConceptReferences == null) {
				return null;
			}
			for (FlexoObjectReference<FlexoConceptInstance> r : flexoConceptReferences) {
				FlexoConceptInstance epi = r.getObject();
				if (epi.getFlexoConcept().getName().equals(flexoConceptId) && epi.getFlexoID() == instanceId) {
					return epi;
				}
			}
			return null;
		}*/

		/**
		 * Return FlexoConceptInstance matching supplied id represented as a string, which could be either the name of FlexoConcept, or its
		 * URI<br>
		 * If many FlexoConceptInstance are declared in this FlexoProjectObject, return first one
		 * 
		 * @param flexoConceptId
		 * @return
		 */
		/*@Override
		public FlexoConceptInstance getFlexoConceptInstance(String flexoConceptId) {
			if (flexoConceptId == null) {
				return null;
			}
			for (FlexoObjectReference<FlexoConceptInstance> ref : flexoConceptReferences) {
				FlexoConceptInstance epi = ref.getObject();
				if (epi.getFlexoConcept().getName().equals(flexoConceptId)) {
					return epi;
				}
				if (epi.getFlexoConcept().getURI().equals(flexoConceptId)) {
					return epi;
				}
			}
			return null;
		}*/

		/**
		 * Return FlexoConceptInstance matching supplied FlexoConcept<br>
		 * If many FlexoConceptInstance are declared in this FlexoProjectObject, return first one
		 * 
		 * @param flexoConceptId
		 * @return
		 */
		/*@Override
		public FlexoConceptInstance getFlexoConceptInstance(FlexoConcept flexoConcept) {
			if (flexoConcept == null) {
				return null;
			}
			for (FlexoObjectReference<FlexoConceptInstance> ref : flexoConceptReferences) {
				FlexoConceptInstance epi = ref.getObject();
				if (epi != null && epi.getFlexoConcept() == flexoConcept) {
					return epi;
				}
			}
			return null;
		}*/

		/*protected FlexoObjectReference<FlexoConceptInstance> getFlexoConceptReference(FlexoConceptInstance flexoConceptInstance) {
			for (FlexoObjectReference<FlexoConceptInstance> ref : flexoConceptReferences) {
				String was = ref.toString() + " serialized as " + ref.getStringRepresentation();
				try {
					FlexoConceptInstance epi = ref.getObject();
					if (epi == flexoConceptInstance) {
						return ref;
					}
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
			return null;
		}*/

		/*@Override
		public void registerFlexoConceptReference(FlexoConceptInstance flexoConceptInstance) {

			FlexoObjectReference<FlexoConceptInstance> existingReference = getFlexoConceptReference(flexoConceptInstance);

			if (existingReference == null) {
				addToFlexoConceptReferences(new FlexoObjectReference<FlexoConceptInstance>(flexoConceptInstance));
			}
		}*/

		/*@Override
		public void unregisterFlexoConceptReference(FlexoConceptInstance flexoConceptInstance) {
			FlexoObjectReference<FlexoConceptInstance> referenceToRemove = getFlexoConceptReference(flexoConceptInstance);
			if (referenceToRemove == null) {
				logger.warning("Called for unregister FlexoConceptReference for unexisting reference to flexo concept instance EP="
						+ flexoConceptInstance.getFlexoConcept().getName() + " id=" + flexoConceptInstance.getFlexoID());
			} else {
				removeFromFlexoConceptReferences(referenceToRemove);
			}
		}*/

		public boolean ignoreNotifications() {
			return ignoreNotifications;
		}

		/**
		 * Temporary method to prevent notifications. This should never be used by anyone except the screenshot generator
		 */
		public synchronized void setIgnoreNotifications() {
			ignoreNotifications = true;
		}

		/**
		 * Temporary method to reactivate notifications. This should never be used by anyone except the screenshot generator
		 */
		public synchronized void resetIgnoreNotifications() {
			ignoreNotifications = false;
		}

		/**
		 * Overrides default {@link #setModified(boolean)} method by providing extended support for modification propagation for resource
		 * embedding
		 */
		@Override
		public void setModified(boolean modified) {
			if (modified) {
				setIsModified();
			} else {
				clearIsModified();
			}
		}

		/**
		 * Mark the current object to be in 'modified' status<br>
		 * If object is part of a {@link ResourceData}, mark the {@link ResourceData} to be modified, and thus, related resource to be
		 * modified. Also notify the {@link FlexoEditingContext}
		 */
		public synchronized void setIsModified() {

			// If ignore notification flag set to true, just return
			if (ignoreNotifications) {
				return;
			}

			// Update last updated date
			lastMemoryUpdate = new Date();

			// If this object is part of a ResourceData, then call
			// setIsModified() on ResourceData
			if (this instanceof InnerResourceData && ((InnerResourceData<?>) this).getResourceData() != null
					&& ((InnerResourceData<?>) this).getResourceData() != this) {
				((InnerResourceData<?>) this).getResourceData().setIsModified();
			}

			// If object is already in 'modified' status, abort
			if (isModified()) {
				return;
			}

			// Call the super implementation (PAMELA framework)
			performSuperSetModified(true);

			// If this object is a ResourceData, notify FlexoEditingContext of
			// related resource changing 'modified' status
			if (this instanceof ResourceData) {
				FlexoResource<?> resource = ((ResourceData<?>) this).getResource();
				if (resource != null) {
					resource.notifyResourceModified();
				}
			}
		}

		/**
		 * Mark the current object not to be anymore in 'modified' status<br>
		 */
		public synchronized void clearIsModified() {
			clearIsModified(false);
		}

		/**
		 * Mark the current object not to be anymore in 'modified' status<br>
		 * Passed flag indicates if last memory update should be reset
		 */
		public synchronized void clearIsModified(boolean clearLastMemoryUpdate) {
			performSuperSetModified(false);
			// isModified = false;
			// GPO: I commented the line hereunder because I don't think that we
			// need to reset this date
			if (clearLastMemoryUpdate) {
				lastMemoryUpdate = null;
			}

			if (this instanceof ResourceData) {
				FlexoResource<?> resource = ((ResourceData<?>) this).getResource();
				if (resource != null) {
					resource.notifyResourceModified();
				}
			}

		}

		/**
		 * Return date of last update in memory
		 * 
		 * @return
		 */
		public Date lastMemoryUpdate() {
			return lastMemoryUpdate;
		}

		public Object getContext() {
			return context;
		}

		public void setContext(Object context) {
			this.context = context;
		}

		// ***************************************************
		// Action management
		// ***************************************************

		@Override
		public final List<FlexoActionType<?, ?, ?>> getActionList() {
			return getActionList(getClass());
		}

		public static <T extends FlexoObject> List<FlexoActionType<?, ?, ?>> getActionList(Class<T> aClass) {
			if (_actionListForClass.get(aClass) == null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("COMPUTE ACTION_LIST FOR " + aClass.getName());
				}
				List<FlexoActionType<?, ?, ?>> returned = updateActionListFor(aClass);
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("DONE. COMPUTE ACTION_LIST FOR " + aClass.getName() + ": " + returned.size() + " action(s) :");
					for (FlexoActionType<?, ?, ?> next : returned) {
						logger.fine(" " + next.getLocalizedName());
					}
					logger.fine(".");
				}
				return returned;
			}
			List<FlexoActionType<?, ?, ?>> returned = _actionListForClass.get(aClass);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("RETURN (NO COMPUTING) ACTION_LIST FOR " + aClass.getName() + ": " + returned.size() + " action(s) :");

				for (FlexoActionType<?, ?, ?> next : returned) {
					logger.fine(" " + next.getLocalizedName());
				}
				logger.fine(".");
			}
			return returned;
		}

		public static <T1 extends FlexoObject, T extends T1> void addActionForClass(FlexoActionType<?, T1, ?> actionType,
				Class<T> objectClass) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("addActionForClass: " + actionType + " for " + objectClass);
			}
			List<FlexoActionType<?, ?, ?>> actions = _declaredActionsForClass.get(objectClass);
			if (actions == null) {
				actions = new ArrayList<FlexoActionType<?, ?, ?>>();
				_declaredActionsForClass.put(objectClass, actions);
			}
			if (actionType != null) {
				if (!actions.contains(actionType)) {
					actions.add(actionType);
				}
			} else {
				logger.warning("Trying to declare null action !");
			}

			if (_actionListForClass != null) {
				List<Class> entriesToRemove = new ArrayList<Class>();
				for (Class aClass : _actionListForClass.keySet()) {
					if (objectClass.isAssignableFrom(aClass)) {
						entriesToRemove.add(aClass);
					}
				}
				for (Class aClass : entriesToRemove) {
					logger.info("Recompute actions list for " + aClass);
					_actionListForClass.remove(aClass);
				}
			}

		}

		private static <T extends FlexoObject> List<FlexoActionType<?, ?, ?>> updateActionListFor(Class<T> aClass) {
			List<FlexoActionType<?, ?, ?>> newActionList = new ArrayList<FlexoActionType<?, ?, ?>>();
			for (Map.Entry<Class, List<FlexoActionType<?, ?, ?>>> e : _declaredActionsForClass.entrySet()) {
				if (e.getKey().isAssignableFrom(aClass)) {
					newActionList.addAll(e.getValue());
				}
			}
			_actionListForClass.put(aClass, newActionList);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("updateActionListFor() class: " + aClass);
				for (FlexoActionType a : newActionList) {
					logger.finer(" > " + a);
				}
			}
			return newActionList;
		}

		public int getActionCount() {
			return getActionList().size();
		}

		public FlexoActionType<?, ?, ?> getActionTypeAt(int index) {
			return getActionList().get(index);
		}

		// ***************************************************
		// Documentation management
		// ***************************************************

		/**
		 * Returns wheter this object is imported or not. Object implementing FlexoImportableObject should override this method
		 * 
		 * @return true if this object is imported.
		 */
		public boolean isImported() {
			return false;
		}

		public boolean isDocEditable() {
			return !isImported();
		}

		@Override
		public String getDescription() {
			if (description != null && description.startsWith("<html>First")) {
				new Exception().printStackTrace();
			}
			return description;
		}

		@Override
		public void setDescription(String description) {
			if (!stringHasChanged(this.description, description)) {
				return;
			}
			String old = this.description;
			if (old == null && description == null) {
				return;
			}
			if (old == null || !old.equals(description)) {
				this.description = description;
				setChanged();
				notifyObservers(new DataModification("description", old, description));
			}
		}

		public String getFullDescriptionWithOnlyBodyContent(String specificDescriptionType) {
			StringBuilder sb = new StringBuilder();

			if (getDescription() != null) {
				String description = HTMLUtils.extractBodyContent(getDescription());
				sb.append(description != null ? description : getDescription());
			}

			if (getHasSpecificDescriptions() && specificDescriptionType != null
					&& getSpecificDescriptionForKey(specificDescriptionType) != null) {
				String specifDesc = HTMLUtils.extractBodyContent(getSpecificDescriptionForKey(specificDescriptionType));
				sb.append(specifDesc != null ? specifDesc : getSpecificDescriptionForKey(specificDescriptionType));
			}

			return sb.toString().trim();
		}

		@Override
		public boolean getHasSpecificDescriptions() {
			return hasSpecificDescriptions;
		}

		@Override
		public void setHasSpecificDescriptions(boolean hasSpecificDescription) {
			if (this.hasSpecificDescriptions == hasSpecificDescription) {
				return;
			}
			boolean old = this.hasSpecificDescriptions;
			this.hasSpecificDescriptions = hasSpecificDescription;
			setChanged();
			notifyObservers(new DataModification("hasSpecificDescriptions", old, hasSpecificDescription));
		}

		@Override
		public List<String> getSpecificDescriptionKeys() {
			if (!getHasSpecificDescriptions()) {
				return null;
			}
			return new ArrayList<String>(getSpecificDescriptions().keySet());
		}

		@Override
		public Map<String, String> getSpecificDescriptions() {
			return specificDescriptions;
		}

		@Override
		public void setSpecificDescriptions(Map<String, String> specificDescriptions) {
			if (this.specificDescriptions == null) {
				this.specificDescriptions = new TreeMap<String, String>();
			} else {
				this.specificDescriptions = specificDescriptions;
			}
		}

		/**
		 * @return
		 */
		public String getLocalizedClassName() {
			return FlexoLocalization.localizedForKey(getClass().getSimpleName());
		}

		@Override
		public List<FlexoProperty> getCustomProperties() {
			return customProperties;
		}

		@Override
		public void setCustomProperties(List<FlexoProperty> customProperties) {
			if (this.customProperties != null) {
				for (FlexoProperty property : this.customProperties) {
					property.setOwner(null);
				}
			}
			this.customProperties = customProperties;
			if (this.customProperties != null) {
				for (FlexoProperty property : new ArrayList<FlexoProperty>(this.customProperties)) {
					property.setOwner(this);
				}
			}

		}

		@Override
		public void addToCustomProperties(FlexoProperty property) {
			addToCustomProperties(property, false);
		}

		public void addToCustomProperties(FlexoProperty property, boolean insertSorted) {
			if (insertSorted && property.getName() != null) {
				int i = 0;
				for (FlexoProperty p : customProperties) {
					if (p.getName() != null && p.getName().compareTo(property.getName()) > 0) {
						break;
					}
					i++;
				}
				customProperties.set(i, property);
			} else {
				customProperties.add(property);
			}
			if (property != null) {
				property.setOwner(this);
			}
			setChanged();
			DataModification dm = new DataModification("customProperties", null, property);
			notifyObservers(dm);
		}

		@Override
		public void removeFromCustomProperties(FlexoProperty property) {
			customProperties.remove(property);
		}

		public boolean hasPropertyNamed(String name) {
			return getPropertyNamed(name) != null;
		}

		@Override
		public FlexoProperty getPropertyNamed(String name) {
			if (name == null) {
				for (FlexoProperty p : getCustomProperties()) {
					if (p.getName() == null) {
						return p;
					}
				}
			} else {
				for (FlexoProperty p : getCustomProperties()) {
					if (name.equals(p.getName())) {
						return p;
					}
				}
			}
			return null;
		}

		public List<FlexoProperty> getProperties(String name) {
			List<FlexoProperty> v = new ArrayList<FlexoProperty>();
			if (name == null) {
				for (FlexoProperty p : getCustomProperties()) {
					if (p.getName() == null) {
						v.add(p);
					}
				}
			} else {
				for (FlexoProperty p : getCustomProperties()) {
					if (name.equals(p.getName())) {
						v.add(p);
					}
				}
			}
			return v;
		}

		public void addProperty() {
			if (addFlexoPropertyActionizer != null) {
				addFlexoPropertyActionizer.run(this, null);
			}
		}

		public boolean canSortProperties() {
			return customProperties.size() > 1;
		}

		public void sortProperties() {
			if (sortFlexoPropertiesActionizer != null) {
				sortFlexoPropertiesActionizer.run(this, null);
			}
		}

		public void deleteProperty(FlexoProperty property) {
			if (deleteFlexoPropertyActionizer != null) {
				deleteFlexoPropertyActionizer.run(property, null);
			}
		}

		@Override
		public boolean hasDescription() {
			return getDescription() != null && getDescription().trim().length() > 0;
		}

		/**
		 * This property is used by the hightlightUncommentedItem mode. The decoration meaning that the description is missing will only
		 * appears on object for wich this method return true. So this method has to be overridden in subclass.
		 * 
		 * @return
		 */
		public boolean isDescriptionImportant() {
			return false;
		}

		@Override
		public boolean hasSpecificHelp(String key) {
			return getSpecificDescriptionForKey(key) != null && getSpecificDescriptionForKey(key).length() > 0;
		}

		@Override
		public boolean hasSpecificDescriptionForKey(String key) {
			return getSpecificDescriptionForKey(key) != null && getSpecificDescriptionForKey(key).trim().length() > 0;
		}

		@Override
		public String getSpecificDescriptionForKey(String key) {
			return specificDescriptions.get(key);
		}

		@Override
		public void setSpecificDescriptionsForKey(String description, String key) {
			specificDescriptions.put(key, description);
			setChanged();
			DataModification dm = new DataModification("specificDescriptions", null, description);
			notifyObservers(dm);
		}

		@Override
		public void removeSpecificDescriptionsWithKey(String key) {
			specificDescriptions.remove(key);
		}

		/**
		 * Return all embedded objects which need to be validated<br>
		 * This method is really generic and use PAMELA annotations. You might want to override this method to provide a more precise
		 * implementation
		 * 
		 */
		@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			// System.out.println("> Compute getEmbeddedValidableObjects() for " + this.getClass() + " : " + this);
			ResourceData<?> resourceData = null;
			if (this instanceof ResourceData) {
				resourceData = (ResourceData) this;
			} else if (this instanceof InnerResourceData) {
				resourceData = ((InnerResourceData) this).getResourceData();
			}
			if (resourceData != null && resourceData.getResource() instanceof PamelaResource) {
				List<?> embeddedObjects = ((PamelaResource) resourceData.getResource()).getFactory().getEmbeddedObjects(this,
						EmbeddingType.CLOSURE);
				List<Validable> returned = new ArrayList<Validable>();
				for (Object e : embeddedObjects) {
					if (e instanceof Validable) {
						returned.add((Validable) e);
					}
				}
				// System.out.println("Return " + returned);
				return returned;

			}

			// System.out.println("< return null;");
			return null;
		}

		private static HelpRetriever _helpRetriever = null;

		public static interface HelpRetriever {
			public String shortHelpForObject(FlexoObject object);

			public String longHelpForObject(FlexoObject object);
		}

		/**
		 * Return help text for supplied object, as defined in DocResourceManager as long version Note: return an HTML version, with
		 * embedding <html>...</html> tags.
		 */
		public String getHelpText() {
			if (_helpRetriever != null) {
				return _helpRetriever.longHelpForObject(this);
			}
			return null;
		}

		/**
		 * Return help text for supplied object, as defined in DocResourceManager as short version Note: return an HTML version, with
		 * embedding <html>...</html> tags.
		 */
		public String getShortHelpText() {
			if (_helpRetriever != null) {
				return _helpRetriever.shortHelpForObject(this);
			}
			return null;
		}

		public static HelpRetriever getHelpRetriever() {
			return _helpRetriever;
		}

		public static void setHelpRetriever(HelpRetriever retriever) {
			_helpRetriever = retriever;
		}

		private static String currentUserIdentifier;

		public static String getCurrentUserIdentifier() {
			if (currentUserIdentifier == null) {
				currentUserIdentifier = "FLX".intern();
			}
			return currentUserIdentifier;
		}

		public static void setCurrentUserIdentifier(String aUserIdentifier) {
			if (aUserIdentifier != null && aUserIdentifier.indexOf('#') > -1) {
				aUserIdentifier = aUserIdentifier.replace('#', '-');
				currentUserIdentifier = aUserIdentifier.intern();
			}
		}

		private String userIdentifier;

		@Override
		public String getUserIdentifier() {
			if (userIdentifier == null) {
				return getCurrentUserIdentifier();
			}
			return userIdentifier;
		}

		@Override
		public void setUserIdentifier(String aUserIdentifier) {
			if (aUserIdentifier != null && aUserIdentifier.indexOf('#') > -1) {
				aUserIdentifier = aUserIdentifier.replace('#', '-');
			}
			userIdentifier = aUserIdentifier != null ? aUserIdentifier.intern() : null;
		}

		private long flexoID = -1;

		/**
		 * Returns the flexoID.
		 * 
		 * @return
		 */
		@Override
		public long getFlexoID() {
			if (flexoID < 0) {
				obtainNewFlexoID();
			}
			return flexoID;
		}

		/**
		 * Obtain new flexoID by requesting it to the PamelaResource
		 * 
		 * @return
		 */
		@Override
		public long obtainNewFlexoID() {
			if (this instanceof InnerResourceData && ((InnerResourceData) this).getResourceData() != null
					&& ((InnerResourceData) this).getResourceData().getResource() instanceof PamelaResource) {
				flexoID = ((PamelaResource<?, ?>) ((InnerResourceData) this).getResourceData().getResource()).getNewFlexoID();
			} else {
				flexoID = -1;
			}
			return flexoID;
		}

		/**
		 * Sets the flexoID
		 * 
		 * @param flexoID
		 *            The flexoID to set.
		 */
		@Override
		public void setFlexoID(long flexoID) {
			if (flexoID > 0 && flexoID != this.flexoID) {
				long oldId = this.flexoID;
				this.flexoID = flexoID;
				if (this instanceof InnerResourceData && ((InnerResourceData) this).getResourceData() != null
						&& ((InnerResourceData) this).getResourceData().getResource() instanceof PamelaResource) {
					// TODO sets last id of resource ?
				}
			}
		}

		@Override
		public Class<?> getImplementedInterface() {
			if (this instanceof ResourceData && ((ResourceData) this).getResource() instanceof PamelaResource) {
				ModelFactory f = ((PamelaResource) ((ResourceData) this).getResource()).getFactory();
				return f.getModelEntityForInstance(this).getImplementedInterface();
			}
			if (this instanceof InnerResourceData && ((InnerResourceData) this).getResourceData() != null
					&& ((InnerResourceData) this).getResourceData().getResource() instanceof PamelaResource) {
				ModelFactory f = ((PamelaResource) ((InnerResourceData) this).getResourceData().getResource()).getFactory();
				return f.getModelEntityForInstance(this).getImplementedInterface();
			}
			return getClass();
		}

		@Override
		public String toString() {
			return getImplementedInterface().getSimpleName() + "[ID=" + getFlexoID() + "]" + "@" + Integer.toHexString(hashCode());
		}

	}

}
