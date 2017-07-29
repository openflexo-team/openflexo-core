/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
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

/**
 * Super class for any object involved in Openflexo-Core (model layer)<br>
 * 
 * Provides a direct access to {@link FlexoServiceManager} for objects beeing part of a {@link ResourceData} accessed through a
 * FlexoResource
 * 
 * @author sguerin
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoObject.FlexoObjectImpl.class)
// TODO: remove ReferenceOwner declaration and create a new class
public interface FlexoObject extends AccessibleProxyObject, DeletableProxyObject, CloneableProxyObject, KeyValueCoding, Validable {

	@PropertyIdentifier(type = String.class)
	String USER_IDENTIFIER_KEY = "userIdentifier";
	@PropertyIdentifier(type = long.class)
	String FLEXO_ID_KEY = "flexoID";
	@PropertyIdentifier(type = Vector.class)
	String CUSTOM_PROPERTIES_KEY = "customProperties";

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

	/**
	 * Return hashCode() representation, in hexadecimal format
	 * 
	 * @return
	 */
	public String hash();

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

	// public boolean hasSpecificHelp(String key);

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
	 * Return FlexoConceptInstance matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
	 * <br>
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

	public FlexoServiceManager getServiceManager();

	/**
	 * Mark the current object to be in 'modified' status<br>
	 * If object is part of a {@link ResourceData}, mark the {@link ResourceData} to be modified, and thus, related resource to be modified.
	 * Also notify the {@link FlexoEditingContext}
	 */
	public void setIsModified();

	/**
	 * Return the locales attached to this {@link FlexoObject}.<br>
	 * This is the responsability of subclasses to implements a proper scheme
	 * 
	 * @return
	 */
	public LocalizedDelegate getLocales();

	default String getReferenceForSerialization(boolean serializeClassName) {
		if (this instanceof InnerResourceData) {
			ResourceData resourceData = ((InnerResourceData) this).getResourceData();
			if (resourceData != null && resourceData.getResource() != null) {
				FlexoResource resource = resourceData.getResource();
				return FlexoObjectReference.constructSerializationRepresentation(
						this instanceof FlexoProjectObject ? ((FlexoProjectObject) this).getProject().getURI() : null, resource.getURI(),
						getUserIdentifier(), Long.toString(getFlexoID()), serializeClassName ? getClass().getName() : null);
			}
		}
		return null;
	}

	public static abstract class FlexoObjectImpl extends FlexoObservable implements FlexoObject {

		private static final Logger logger = Logger.getLogger(FlexoObject.class.getPackage().getName());

		private boolean ignoreNotifications = false;
		private Date lastMemoryUpdate = null;

		private Object context;

		private List<FlexoProperty> customProperties;

		/**
		 * A map that stores the different declared actions for each class
		 */
		private static final Map<Class, List<FlexoActionType<?, ?, ?>>> _declaredActionsForClass = new LinkedHashMap<>();

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
		 * Implements access to service manager of objects being part of a {@link ResourceData} accessed through a FlexoResource
		 */
		@Override
		public FlexoServiceManager getServiceManager() {
			if (this instanceof InnerResourceData) {
				ResourceData<?> resdata = ((InnerResourceData<?>) this).getResourceData();
				// avoid NPE when deleting object
				if (resdata != null) {
					FlexoResource<?> resource = resdata.getResource();
					if (resource != null) {
						return resource.getServiceManager();
					}
				}
			}
			return null;
		}

		/**
		 * Test if changing a value from oldValue to newValue is significant
		 * 
		 * @param oldValue
		 * @param newValue
		 * @return
		 */
		protected <T> boolean requireChange(T oldValue, T newValue) {
			return oldValue == null && newValue != null || oldValue != null && newValue == null
					|| oldValue != null && newValue != null && !oldValue.equals(newValue);
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
			}
			else {
				clearIsModified();
			}
		}

		/**
		 * Mark the current object to be in 'modified' status<br>
		 * If object is part of a {@link ResourceData}, mark the {@link ResourceData} to be modified, and thus, related resource to be
		 * modified. Also notify the {@link FlexoEditingContext}
		 */
		@Override
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

		/**
		 * @deprecated should not be used anymore since this is a potential memory leak
		 * 
		 * @param actionType
		 * @param objectClass
		 */
		@Deprecated
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
			}
			else {
				logger.warning("Trying to declare null action !");
			}

			if (_actionListForClass != null) {
				List<Class> entriesToRemove = new ArrayList<Class>();
				for (Class<?> aClass : _actionListForClass.keySet()) {
					if (objectClass.isAssignableFrom(aClass)) {
						entriesToRemove.add(aClass);
					}
				}
				for (Class<?> aClass : entriesToRemove) {
					logger.info("Recompute actions list for " + aClass);
					_actionListForClass.remove(aClass);
				}
			}

		}

		@Deprecated
		public static <T1 extends FlexoObject, T extends T1> void removeActionFromClass(FlexoActionType<?, T1, ?> actionType,
				Class<T> objectClass) {

			List<FlexoActionType<?, ?, ?>> actions = _declaredActionsForClass.get(objectClass);
			if (actions.contains(actionType)) {
				actions.remove(actionType);
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
				for (FlexoActionType<?, ?, ?> a : newActionList) {
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

		public static LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
			if (serviceManager != null) {
				return serviceManager.getLocalizationService().getFlexoLocalizer();
			}
			return FlexoLocalization.getMainLocalizer();
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (this instanceof TechnologyObject && ((TechnologyObject<?>) this).getTechnologyAdapter() != null) {
				return ((TechnologyObject<?>) this).getTechnologyAdapter().getLocales();
			}
			return getLocales(getServiceManager());
		}

		/**
		 * @return
		 */
		public String getLocalizedClassName() {
			return getLocales().localizedForKey(getClass().getSimpleName());
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
			}
			else {
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
			}
			else {
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
			}
			else {
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

		/**
		 * This property is used by the hightlightUncommentedItem mode. The decoration meaning that the description is missing will only
		 * appears on object for wich this method return true. So this method has to be overridden in subclass.
		 * 
		 * @return
		 */
		public boolean isDescriptionImportant() {
			return false;
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
			PamelaResource resource = getPamelaResource();
			if (resource != null) {
				List<?> embeddedObjects = resource.getFactory().getEmbeddedObjects(this, EmbeddingType.CLOSURE);
				List<Validable> returned = new ArrayList<Validable>();
				for (Object e : embeddedObjects) {
					if (e instanceof Validable) {
						returned.add((Validable) e);
					}
				}
				return returned;

			}
			return null;
		}

		private PamelaResource getPamelaResource() {
			ResourceData data = null;
			if (this instanceof InnerResourceData) {
				data = ((InnerResourceData) this).getResourceData();
			}
			if (this instanceof ResourceData) {
				data = (ResourceData) this;
			}

			if (data != null && data.getResource() instanceof PamelaResource) {
				return (PamelaResource) data.getResource();
			}

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
			PamelaResource resource = getPamelaResource();
			if (resource != null) {
				flexoID = resource.getNewFlexoID();
				resource.register(this);
			}
			else {
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
				// FD : seems unused
				// long oldId = this.flexoID;
				this.flexoID = flexoID;
				PamelaResource resource = getPamelaResource();
				if (resource != null) {
					// TODO sets last id of resource ?
				}
			}
		}

		@Override
		public Class<?> getImplementedInterface() {
			if (this instanceof ResourceData && ((ResourceData<?>) this).getResource() instanceof PamelaResource) {
				ModelFactory f = ((PamelaResource) ((ResourceData<?>) this).getResource()).getFactory();
				return f.getModelEntityForInstance(this).getImplementedInterface();
			}
			if (this instanceof InnerResourceData && ((InnerResourceData<?>) this).getResourceData() != null
					&& ((InnerResourceData<?>) this).getResourceData().getResource() instanceof PamelaResource) {
				ModelFactory f = ((PamelaResource) ((InnerResourceData<?>) this).getResourceData().getResource()).getFactory();
				return f.getModelEntityForInstance(this).getImplementedInterface();
			}
			return getClass();
		}

		/**
		 * Return hashCode() representation, in hexadecimal format
		 * 
		 * @return
		 */
		@Override
		public String hash() {
			return Integer.toHexString(hashCode());
		}

		@Override
		public String toString() {
			return getImplementedInterface().getSimpleName() + "[ID=" + getFlexoID() + "]" + "@" + hash();
		}

	}

}
