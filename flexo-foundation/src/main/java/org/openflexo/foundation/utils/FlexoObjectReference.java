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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.KVCFlexoObject;
import org.openflexo.foundation.fml.rt.rm.ViewResourceFactory;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
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
 * @author sylvain
 * 
 * @param <O>
 *            type of object being referenced by this reference
 */
public class FlexoObjectReference<O extends FlexoObject> extends KVCFlexoObject implements ResourceLoadingListener, PropertyChangeListener {

	private static final Logger logger = FlexoLogger.getLogger(FlexoObjectReference.class.getPackage().getName());

	/**
	 * Implemented by all classes managing a {@link FlexoObjectReference}
	 * 
	 * @author sylvain
	 * 
	 */
	public static interface ReferenceOwner {

		public void notifyObjectLoaded(FlexoObjectReference<?> reference);

		public void objectCantBeFound(FlexoObjectReference<?> reference);

		public void objectDeleted(FlexoObjectReference<?> reference);

		public void objectSerializationIdChanged(FlexoObjectReference<?> reference);

		public FlexoServiceManager getServiceManager();

	}

	public enum ReferenceStatus {
		RESOLVED, UNRESOLVED, NOT_FOUND, RESOURCE_NOT_FOUND, DELETED
	}

	private static final String SEPARATOR = "#";
	private static final String PROJECT_SEPARATOR = "|";
	private static final String ID_SEPARATOR = "_";

	/**
	 * @return
	 */
	public static String getSerializationRepresentationForObject(FlexoObject modelObject, boolean serializeClassName) {

		if (modelObject instanceof InnerResourceData) {

			if (((InnerResourceData) modelObject).getResourceData() != null
					&& ((InnerResourceData) modelObject).getResourceData().getResource() != null) {
				if (modelObject instanceof FlexoProjectObject) {
					return ((FlexoProjectObject) modelObject).getProject().getURI() + PROJECT_SEPARATOR
							+ ((InnerResourceData) modelObject).getResourceData().getResource().getURI() + SEPARATOR
							+ modelObject.getUserIdentifier() + ID_SEPARATOR + String.valueOf(modelObject.getFlexoID())
							+ (serializeClassName ? SEPARATOR + modelObject.getClass().getName() : "");
				}
				else {
					return ((InnerResourceData) modelObject).getResourceData().getResource().getURI() + SEPARATOR
							+ modelObject.getUserIdentifier() + ID_SEPARATOR + String.valueOf(modelObject.getFlexoID())
							+ (serializeClassName ? SEPARATOR + modelObject.getClass().getName() : "");
				}
			}
		}

		return null;
	}

	private String projectIdentifier;
	private String resourceIdentifier;
	private String userIdentifier;
	private String className;
	private long flexoID;

	// private String enclosingProjectIdentifier;

	/** The project of the referring object. */
	// private FlexoProject referringProject;

	private ReferenceOwner owner;
	private O modelObject;
	private boolean serializeClassName = false;
	private ReferenceStatus status = ReferenceStatus.UNRESOLVED;
	private FlexoResource<?> resource;

	private boolean deleted = false;
	private String modelObjectIdentifier;

	public FlexoObjectReference(O object) {
		this.modelObject = object;

		if (this.modelObject != null) {
			this.modelObject.addToReferencers(this);
		}
		this.status = ReferenceStatus.RESOLVED;

		/**
		 * We also initialize the string representation for the following reason: Let's say the user creates a reference to the given
		 * <code>object</code> and then later on, the user deletes that <code>object</code> but the reference owner does not remove its
		 * reference, we will have to serialize this without any data which will be a disaster (we should expect NullPointer,
		 * ArrayIndexOutOfBounds, etc...
		 */
		if (modelObject instanceof InnerResourceData && ((InnerResourceData) modelObject).getResourceData() != null) {
			if (((InnerResourceData) modelObject).getResourceData().getResource() != null) {
				this.resourceIdentifier = ((InnerResourceData) modelObject).getResourceData().getResource().getURI();
			}
			else {
				logger.warning("object " + modelObject + " has a resource data (" + ((InnerResourceData) modelObject).getResourceData()
						+ ") with null resource ");
			}
		}
		else {
			logger.warning("object " + modelObject + " has no resource data !");
		}
		if (modelObject != null) {
			this.userIdentifier = modelObject.getUserIdentifier();
			this.flexoID = modelObject.getFlexoID();
			this.className = modelObject.getClass().getName();
		}

		if (object instanceof FlexoProjectObject) {
			setOwner(((FlexoProjectObject) object).getProject());
		}
		else if (object instanceof InnerResourceData) {
			setOwner(((InnerResourceData) object).getResourceData().getResource());
		}
		else {
			logger.warning("Could not find any Reference owner for " + object);
		}
	}

	@Override
	public String toString() {
		return "FlexoModelObjectReference resource=" + resourceIdentifier + " modelObject=" + modelObject + " status=" + status + " owner="
				+ owner + " userIdentifier=" + userIdentifier + " className=" + className + " flexoID=" + flexoID;
	}

	public FlexoObjectReference(String modelObjectIdentifier, ReferenceOwner owner) {
		// this.referringProject = project;
		this.modelObjectIdentifier = modelObjectIdentifier;
		/*if (referringProject != null) {
			referringProject.addToObjectReferences(this);
			
		}*/
		setOwner(owner);
		try {
			int indexOf = modelObjectIdentifier.indexOf(PROJECT_SEPARATOR);
			if (indexOf > 0) {
				projectIdentifier = modelObjectIdentifier.substring(0, indexOf);
				modelObjectIdentifier = modelObjectIdentifier.substring(indexOf + PROJECT_SEPARATOR.length());
			}
			String[] s = modelObjectIdentifier.split(SEPARATOR);
			this.resourceIdentifier = s[0];
			this.userIdentifier = s[1].substring(0, s[1].lastIndexOf(ID_SEPARATOR));
			this.flexoID = Long.valueOf(s[1].substring(s[1].lastIndexOf(ID_SEPARATOR) + ID_SEPARATOR.length()));
			if (s.length == 3) {
				this.className = s[2];
				serializeClassName = true;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void delete() {
		delete(true);
	}

	public void delete(boolean notify) {
		if (!deleted) {
			deleted = true;
			/*if (getReferringProject(true) != null) {
				getReferringProject(true).removeObjectReferences(this);
			}*/
			// TODO: OLD FlexoResource scheme
			/*if (getResource(false) instanceof FlexoXMLStorageResource) {
				((FlexoXMLStorageResource) getResource(false)).removeResourceLoadingListener(this);
				((FlexoXMLStorageResource) getResource(false)).getPropertyChangeSupport().removePropertyChangeListener("name", this);
			}*/
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

	public String getClassName() {
		return className;
	}

	public Class<O> getKlass() {
		if (getClassName() != null) {
			try {
				return (Class<O>) Class.forName(getClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("There seems to be a problem in the code. Attempt to retrieve " + getClassName()
							+ " but was something else (see stacktrace)");
				}
			}
		}
		return null;
	}

	public O getObject(boolean force) {

		// System.out.println("modelObject=" + modelObject);
		// System.out.println("owner=" + owner);
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
				else if (getResource(force) == null || getResource(force).isLoaded()
				// TODO: OLD FlexoResource scheme
				/*&& (!(getResource(force) instanceof FlexoXMLStorageResource) || !((FlexoXMLStorageResource) getResource(force))
						.getIsLoading())*/) {
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
			ResourceData<?> resourceData = resource.getResourceData(null);

			if (resource instanceof PamelaResource) {
				return (O) ((PamelaResource<?, ?>) resource).getFlexoObject(flexoID, userIdentifier);
				/*List<Object> allObjects = ((PamelaResource<?, ?>) resource).getFactory().getEmbeddedObjects(resourceData,
						EmbeddingType.CLOSURE);
				allObjects.add(resourceData);
				for (Object temp : allObjects) {
					if (temp instanceof FlexoObject) {
						FlexoObject o = (FlexoObject) temp;
						if (o.getFlexoID() == flexoID && o.getUserIdentifier().equals(userIdentifier)) {
							return (O) temp;
						}
					}
				}*/
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FlexoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.warning("Cannot find object " + userIdentifier + "_" + flexoID + " in resource " + resource);
		return null;
	}

	private O findObject(boolean force) {
		/*System.out.println("findObject()");
		System.out.println("projectIdentifier=" + projectIdentifier);
		System.out.println("resourceIdentifier=" + resourceIdentifier);
		System.out.println("userIdentifier=" + userIdentifier);
		System.out.println("flexoID=" + flexoID);
		System.out.println("className=" + className);*/

		FlexoResource<?> res = getResource(force);
		if (res == null) {
			return null;
		}
		else {
			// System.out.println("Found resource");
			return findObjectInResource(res);
		}
	}

	public FlexoResource<?> getResource(boolean force) {
		/*if (getReferringProject(force) != null) {
			// We are locating a resource located in a project
			if (resource == null) {
				FlexoProject enclosingProject = getReferringProject(force);
				if (enclosingProject != null) {
					resource = enclosingProject.getServiceManager().getResourceManager().getResource(resourceIdentifier);
				}
			}
			return resource;
		} else {*/
		if (resource == null && getOwner() != null && getOwner().getServiceManager() != null) {
			resource = getOwner().getServiceManager().getResourceManager().getResource(resourceIdentifier);
			if (resource == null) {
				// Temporary hack to maintain 1.7.x projects
				return attemptToFindResourceIdentifiedBy(resourceIdentifier);
			}
		}
		return resource;
		// }
	}

	/**
	 * Temporary ensure compatibility with previous versions of Openflexo
	 * 
	 * @param resourceIdentifier
	 * @return
	 */
	@Deprecated
	private FlexoResource<?> attemptToFindResourceIdentifiedBy(String resourceIdentifier) {
		List<String> alternateURIs = new ArrayList<>();
		if (resourceIdentifier.lastIndexOf("/") > -1) {
			String s1 = resourceIdentifier.substring(0, resourceIdentifier.lastIndexOf("/"));
			String s2 = resourceIdentifier.substring(resourceIdentifier.lastIndexOf("/"));
			alternateURIs
					.add(s1 + ViewResourceFactory.VIEW_SUFFIX + s2 + VirtualModelInstanceResourceFactory.VIRTUAL_MODEL_INSTANCE_SUFFIX);
			alternateURIs.add(s1 + ViewResourceFactory.VIEW_SUFFIX + s2);
			alternateURIs.add(s1 + s2 + VirtualModelInstanceResourceFactory.VIRTUAL_MODEL_INSTANCE_SUFFIX);
		}
		for (String alternateURI : alternateURIs) {
			FlexoResource<?> resource = getOwner().getServiceManager().getResourceManager().getResource(alternateURI);
			if (resource != null) {
				logger.warning("Found alternate resource uri " + alternateURI + " instead of " + resourceIdentifier);
				return resource;
			}
		}
		logger.warning("Cannot find resource " + resourceIdentifier);
		return null;
	}

	public String getResourceIdentifier() {
		if (resource != null) {
			return resource.getURI();
		}
		else {
			return resourceIdentifier;
		}
	}

	public long getFlexoID() {
		return flexoID;
	}

	public FlexoProject getReferringProject(boolean force) {
		if (modelObject instanceof FlexoProjectObject) {
			return ((FlexoProjectObject) modelObject).getProject();
		}
		else {
			// TODO: lookup project using projectIdentifier
			return null;
		}
	}

	public ReferenceOwner getOwner() {
		return owner;
	}

	public void setOwner(ReferenceOwner owner) {
		if (this.owner != owner) {
			if (this.owner instanceof FlexoProject) {
				if (this.owner != null) {
					((FlexoProject) this.owner).removeObjectReferences(this);
				}
				this.owner = owner;
				if (this.owner != null) {
					((FlexoProject) this.owner).addToObjectReferences(this);
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
		if (modelObject != null) {
			return getSerializationRepresentationForObject(modelObject, serializeClassName);
		}
		else {
			return resourceIdentifier + SEPARATOR + userIdentifier + ID_SEPARATOR + flexoID
					+ (serializeClassName ? SEPARATOR + className : "");
		}
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

	/*public void _setEnclosingProjectIdentifier(String uri) {
		if (enclosingProjectIdentifier == null) {
			enclosingProjectIdentifier = uri;
			if (getOwner() != null) {
				getOwner().objectSerializationIdChanged(this);
			}
		}
	}*/
}
