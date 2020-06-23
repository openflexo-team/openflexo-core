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

package org.openflexo.foundation.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.ResourceLoadingListener;
import org.openflexo.logging.FlexoLogger;

/**
 * Implements a reference to a {@link FlexoObject}.<br>
 * 
 * This reference has a declared owner, receiving events regarding life-cycle of this object (see {@link ReferenceOwner}).<br>
 * 
 * This class manage serialization of a reference of a {@link FlexoObject} againts resource management, retrieving referenced object through
 * access to a resource.
 * 
 * This serialization is performed using a simple {@link String} decoded by a {@link FlexoObjectReferenceConverter}.
 * 
 * Serialization has the form:
 * 
 * <pre>
 *  resourceURI#userId_flexoId
 * </pre>
 * 
 * @author sylvain
 * 
 * @param <O>
 *            type of object being referenced by this reference
 */
public class FlexoObjectReference<O extends FlexoObject> implements ResourceLoadingListener, PropertyChangeListener {

	// metamodelElementReference="http://onsenfout/ExampleDiagram.diagram#SYL_3"

	private final static String SEPARATOR = "#";
	private final static String ID_SEPARATOR = "_";

	private static final Logger logger = FlexoLogger.getLogger(FlexoObjectReference.class.getPackage().getName());

	/**
	 * Implemented by all classes managing a {@link FlexoObjectReference}
	 * 
	 * @author sylvain
	 * 
	 */
	public interface ReferenceOwner {

		void notifyObjectLoaded(FlexoObjectReference<?> reference);

		void objectCantBeFound(FlexoObjectReference<?> reference);

		void objectDeleted(FlexoObjectReference<?> reference);

		void objectSerializationIdChanged(FlexoObjectReference<?> reference);

		FlexoServiceManager getServiceManager();

	}

	public enum ReferenceStatus {
		RESOLVED, UNRESOLVED, NOT_FOUND, RESOURCE_NOT_FOUND, DELETED
	}

	private String resourceIdentifier;
	private String userIdentifier;
	private String className;
	private String objectIdentifier;

	// private String enclosingProjectIdentifier;

	/** The project of the referring object. */
	// private FlexoProject referringProject;

	private ReferenceOwner owner;
	private O modelObject;
	private boolean serializeClassName = false;
	private ReferenceStatus status = ReferenceStatus.UNRESOLVED;
	private FlexoResource<?> resource;

	private boolean deleted = false;

	public FlexoObjectReference(O object) {

		setObject(object);

	}

	@Override
	public String toString() {
		return "FlexoObjectReference resource=" + resourceIdentifier + " modelObject=" + modelObject + " status=" + status + " owner="
				+ owner + " userIdentifier=" + userIdentifier + " className=" + className + " flexoID=" + objectIdentifier;
	}

	public FlexoObjectReference(String identifier, ReferenceOwner owner) {
		// System.out.println("On cree une reference pour " + identifier + " and " + owner);
		setOwner(owner);
		try {
			String modelObjectIdentifier = identifier;
			String[] s = modelObjectIdentifier.split(SEPARATOR);
			this.resourceIdentifier = s[0];
			this.userIdentifier = s[1].substring(0, s[1].lastIndexOf(ID_SEPARATOR));
			this.objectIdentifier = s[1].substring(s[1].lastIndexOf(ID_SEPARATOR) + ID_SEPARATOR.length());
			if (s.length == 3) {
				this.className = s[2];
				serializeClassName = true;
			}
		} catch (RuntimeException e) {
			logger.log(Level.WARNING, "Can't parse reference '" + identifier + "'.");
		}
	}

	public void delete() {
		delete(true);
	}

	public void delete(boolean notify) {
		if (!deleted) {
			deleted = true;
			if (modelObject != null) {
				modelObject.removeFromReferencers(this);
			}
			if (owner != null) {
				owner.objectDeleted(this);
			}
			owner = null;
			modelObject = null;
		}
	}

	public O getObject() {
		return getObject(false);
	}

	public void setObject(O object) {
		if (modelObject != null) {
			modelObject.removeFromReferencers(this);
		}
		/*if (owner != null) {
			owner.objectDeleted(this);
		}*/

		this.modelObject = object;

		if (this.modelObject != null) {
			this.modelObject.addToReferencers(this);
		}
		this.status = ReferenceStatus.RESOLVED;

		FlexoResource<?> resource = null;
		ResourceData<?> rd = null;
		if (modelObject instanceof InnerResourceData) {
			if (((InnerResourceData<?>) modelObject).getResourceData() != null) {
				rd = ((InnerResourceData<?>) modelObject).getResourceData();
				resource = rd.getResource();
			}
			else {
				logger.warning("object " + modelObject + " has null resource data !");
			}
		}
		else if (modelObject instanceof ResourceData) {
			rd = ((ResourceData<?>) modelObject);
			resource = rd.getResource();
		}
		else {
			logger.warning("object " + modelObject + " has no resource data !");
		}

		/**
		 * We also initialize the string representation for the following reason: Let's say the user creates a reference to the given
		 * <code>object</code> and then later on, the user deletes that <code>object</code> but the reference owner does not remove its
		 * reference, we will have to serialize this without any data which will be a disaster (we should expect NullPointer,
		 * ArrayIndexOutOfBounds, etc...
		 */

		if (resource != null) {
			this.resourceIdentifier = resource.getURI();
			this.userIdentifier = resource.getUserIdentifier(modelObject);
			this.objectIdentifier = resource.getObjectIdentifier(modelObject);
			this.className = modelObject.getClass().getName();
			setOwner(resource);
		}

		/*if (modelObject instanceof InnerResourceData) {
			InnerResourceData<?> ird = (InnerResourceData<?>) modelObject;
			ResourceData<?> rd = ird.getResourceData();
			if (rd != null) {
				this.resourceIdentifier = rd.getResource().getURI();
			}
			else
				logger.warning("object " + modelObject + " has a resource data (" + rd + ") with null resource ");
		}
		else
			logger.warning("object " + modelObject + " has no resource data !");*/

		/*if (modelObject != null) {
			this.userIdentifier = modelObject.getUserIdentifier();
			this.objectIdentifier = Long.toString(modelObject.getFlexoID());
			this.className = modelObject.getClass().getName();
		}
		
		if (object instanceof InnerResourceData) {
			ResourceData<?> resourceData = ((InnerResourceData<?>) object).getResourceData();
			if (resourceData != null) {
				setOwner(resourceData.getResource());
			}
		}
		else {
			logger.warning("Could not find any Reference owner for " + object);
		}*/
	}

	public String getClassName() {
		return className;
	}

	public O getObject(boolean force) {
		if (modelObject == null) {

			modelObject = findObject(force);
			if (modelObject != null) {
				modelObject.addToReferencers(this);
			}
			if (owner != null) {
				if (modelObject != null) {
					status = ReferenceStatus.RESOLVED;
					owner.notifyObjectLoaded(this);
				}
				else if (getResource(force) == null || getResource(force).isLoaded()) {
					if (getResource(force) == null) {
						status = ReferenceStatus.RESOURCE_NOT_FOUND;
					}
					else {
						status = ReferenceStatus.NOT_FOUND;
					}
					if (force) {
						owner.objectCantBeFound(this);
					}
				}
			}
		}
		return modelObject;
	}

	private O findObjectInResource(FlexoResource<?> resource) {
		try {
			// Ensure the resource is loaded
			resource.getResourceData();
			return (O) resource.findObject(objectIdentifier, userIdentifier, className);
		} catch (RuntimeException | FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
			logger.log(Level.SEVERE, "Error while finding object in resource '" + resource.getURI() + "'", e);
		}
		logger.warning("Cannot find object " + userIdentifier + "_" + objectIdentifier + " in resource " + resource);
		return null;
	}

	private O findObject(boolean force) {
		FlexoResource<?> res = getResource(force);
		if (res == null) {
			return null;
		}
		return findObjectInResource(res);
	}

	public FlexoResource<?> getResource(boolean force) {
		if (resource == null && getOwner() != null && getOwner().getServiceManager() != null) {
			resource = getOwner().getServiceManager().getResourceManager().getResource(resourceIdentifier);
		}
		return resource;
	}

	public String getResourceIdentifier() {
		if (resource != null) {
			return resource.getURI();
		}
		return resourceIdentifier;
	}

	public String getObjectIdentifier() {
		return objectIdentifier;
	}

	public FlexoProject getReferringProject(boolean force) {
		if (modelObject instanceof FlexoProjectObject) {
			return ((FlexoProjectObject<?>) modelObject).getProject();
		}
		// TODO: lookup project using projectIdentifier
		return null;
	}

	public ReferenceOwner getOwner() {
		return owner;
	}

	public void setOwner(ReferenceOwner owner) {
		if (this.owner != owner) {
			if (this.owner instanceof FlexoProject) {
				if (this.owner != null) {
					((FlexoProject<?>) this.owner).removeObjectReferences(this);
				}
				this.owner = owner;
				if (this.owner != null) {
					((FlexoProject<?>) this.owner).addToObjectReferences(this);
				}
				else {
					if (owner != null) {
						if (logger.isLoggable(Level.WARNING)) {
							logger.warning("No project found for " + owner + " " + getStringRepresentation());
						}
					}
				}
			}
			else {
				this.owner = owner;
			}
		}
	}

	public String getStringRepresentation() {
		/*if (modelObject != null) {
			return modelObject.getReferenceForSerialization(serializeClassName);
		}*/
		return constructSerializationRepresentation();
	}

	public void notifyObjectDeletion() {
		status = ReferenceStatus.DELETED;
		try {
			if (getOwner() != null) {
				getOwner().objectDeleted(this);
			}
		} finally {
			modelObject = null;// If this is not done, we may end-up with memory leaks.
		}
	}

	public void notifySerializationIdHasChanged() {
		try {
			if (getOwner() != null) {
				getOwner().objectSerializationIdChanged(this);
			}
		} finally {
			modelObject = null;// If this is not done, we may end-up with memory leaks.
		}
	}

	@Override
	public void notifyResourceHasBeenLoaded(FlexoResource<?> resource) {
		findObject(false);
	}

	@Override
	public void notifyResourceWillBeLoaded(FlexoResource<?> resource) {
		// Nothing to do
	}

	/**
	 * Tells wheter the class name of the referenced model object should be serialized or not.
	 * 
	 * @return true if the class name of the referenced model object should be serialized, false otherwise.
	 */
	public boolean getSerializeClassName() {
		return serializeClassName;
	}

	/**
	 * Sets wheter the class name of the referenced model object should be serialized or not.
	 */
	public void setSerializeClassName(boolean serializeClassName) {
		this.serializeClassName = serializeClassName;
	}

	public ReferenceStatus getStatus() {
		if (getResource(false) == null && (status == ReferenceStatus.RESOLVED || status == ReferenceStatus.UNRESOLVED)) {
			status = ReferenceStatus.RESOURCE_NOT_FOUND;
		}
		return status;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == resource && "name".equals(evt.getPropertyName())) {
			resourceIdentifier = resource.getURI();
			if (getOwner() != null) {
				getOwner().objectSerializationIdChanged(this);
			}
		}
	}

	public String constructSerializationRepresentation() {
		StringBuilder result = new StringBuilder();
		result.append(resourceIdentifier);
		result.append(SEPARATOR);
		result.append(userIdentifier);
		result.append(ID_SEPARATOR);
		result.append(objectIdentifier);
		if (serializeClassName) {
			result.append(SEPARATOR);
			result.append(className);
		}
		return result.toString();
	}

	public static String constructSerializationRepresentation(String resourceURI, String userIdentifier, String objectId,
			String className) {
		StringBuilder result = new StringBuilder();
		result.append(resourceURI);
		result.append(SEPARATOR);
		result.append(userIdentifier);
		result.append(ID_SEPARATOR);
		result.append(objectId);
		if (className != null) {
			result.append(SEPARATOR);
			result.append(className);
		}
		return result.toString();
	}

}
