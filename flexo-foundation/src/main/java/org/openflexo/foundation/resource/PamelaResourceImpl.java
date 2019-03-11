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

package org.openflexo.foundation.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.undo.UndoableEdit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Parent;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.FlexoUndoManager.IgnoreHandler;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.kvc.AccessorInvocationException;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.AccessibleProxyObject;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.EmbeddingType;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.undo.AtomicEdit;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;

import com.google.common.base.Throwables;

/**
 * Default implementation for {@link PamelaResource} (a resource where underlying model is managed by PAMELA framework)
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author Sylvain
 * 
 */
public abstract class PamelaResourceImpl<RD extends ResourceData<RD> & AccessibleProxyObject, F extends ModelFactory & PamelaResourceModelFactory>
		extends FlexoResourceImpl<RD> implements PamelaResource<RD, F> {

	private static final Logger logger = Logger.getLogger(PamelaResourceImpl.class.getPackage().getName());

	@SuppressWarnings("unused")
	private boolean isLoading = false;

	// private boolean isConverting = false;
	// protected boolean performLoadWithPreviousVersion = true;

	/**
	 * Save the &quot;real&quot; resource data of this resource.
	 * 
	 * @throws SaveResourceException
	 */
	@Override
	public final void save() throws SaveResourceException {
		// progress.setProgress(getLocales().localizedForKey("saving") + " " + this.getName());
		if (!isLoaded()) {
			return;
		}
		if (!isDeleted()) {
			saveResourceData(true);
			resourceData.clearIsModified(false);
		}

	}

	private boolean isDeserializing = false;

	/**
	 * Internally used to notify factory that a deserialization process has started<br>
	 * This hook allows to handle FlexoID and ignore of edits raised during deserialization process
	 */
	@Override
	public void startDeserializing() {
		// Sometimes, multiple invokation of startDeserializing may raise,
		// ignore extra
		if (isDeserializing) {
			return;
		}
		isDeserializing = true;
		F factory = getFactory();
		if (factory != null) {
			factory.startDeserializing();
		}
		else {
			logger.warning("Trying to deserialize with a NULL factory!!!");
			System.err.println(Throwables.getStackTraceAsString(new Throwable()));
		}
	}

	/**
	 * Internally used to notify factory that a deserialization process has finished<br>
	 */
	@Override
	public void stopDeserializing() {
		if (!isDeserializing) {
			return;
		}
		isDeserializing = false;
		getFactory().stopDeserializing();
		if (getLoadedResourceData() != null) {
			getLoadedResourceData().clearIsModified();
		}
		else {
			logger.warning("Could not access loaded resource data");
		}
	}

	@Override
	public boolean isDeserializing() {
		return isDeserializing;
	}

	@Override
	public boolean isLoading() {
		return isLoading;
	}

	/**
	 * Load resource data by applying a special scheme handling XML versionning, ie to find right XML version of current resource file.<br>
	 * If version of stored file is not conform to latest declared version, convert resource file and update it to latest version.
	 * 
	 * @throws ProjectLoadingCancelledException
	 * @throws MalformedXMLException
	 * 
	 * @see org.openflexo.foundation.rm.FlexoResource#loadResourceData()
	 */
	@Override
	public RD loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException, InconsistentDataException,
			InvalidModelDefinitionException {
		if (resourceData != null) {
			// already loaded
			return resourceData;
		}

		startDeserializing();

		isLoading = true;
		// progress.setProgress(getLocales().localizedForKey("loading") + " " + this.getName());
		// progress.resetSecondaryProgress(4);
		// progress.setProgress(getLocales().localizedForKey("loading_from_disk"));

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Load resource data for " + this);
		}
		/*
		 * TODO if (!getFile().exists()) { recoverFile(); if
		 * (!getFile().exists()) { if (logger.isLoggable(Level.SEVERE)) {
		 * logger.severe("File " + getFile().getAbsolutePath() +
		 * " does not exist, throwing exception now!"); } throw new
		 * FlexoFileNotFoundException(this); } }
		 */

		/*
		 * EditingContext editingContext =
		 * getServiceManager().getEditingContext(); IgnoreLoadingEdits
		 * ignoreHandler = null; FlexoUndoManager undoManager = null;
		 * 
		 * if (editingContext != null && editingContext.getUndoManager()
		 * instanceof FlexoUndoManager) { undoManager = (FlexoUndoManager)
		 * editingContext.getUndoManager();
		 * undoManager.addToIgnoreHandlers(ignoreHandler = new
		 * IgnoreLoadingEdits(this)); //
		 * System.out.println("@@@@@@@@@@@@@@@@ START LOADING RESOURCE " +
		 * getURI()); }
		 */

		try {

			resourceData = performLoad();
			isLoading = false;
			resourceData.setResource(this);
			resourceData.clearIsModified();
			return resourceData;

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOFlexoException(e);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new InvalidXMLException(e);
		} catch (InvalidDataException e) {
			e.printStackTrace();
			throw new InconsistentDataException(e);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new InvalidModelDefinitionException(e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Unhandled Exception");
		} finally {
			/*
			 * if (ignoreHandler != null) {
			 * undoManager.removeFromIgnoreHandlers(ignoreHandler); //
			 * System.out.println("@@@@@@@@@@@@@@@@ END LOADING RESOURCE " +
			 * getURI()); }
			 */
			stopDeserializing();
			isLoading = false;

		}
		return null;

	}

	protected RD performLoad() throws IOException, Exception {
		// Retrieve the data from an input stream given by the FlexoIOStream
		// delegate of the resource
		return (RD) getFactory().deserialize(getFlexoIOStreamDelegate().getInputStream());
	}

	/**
	 * Delete (dereference) resource data if resource data is loaded<br>
	 * Also delete the resource data
	 */
	@Override
	public void unloadResourceData(boolean deleteResourceData) {
		if (isLoaded()) {

			isUnloading = true;

			if (deleteResourceData) {

				EditingContext editingContext = getServiceManager().getEditingContext();
				FlexoUndoManager undoManager = null;
				IgnoreHandler ignoreHandler = new IgnoreLoadingEdits(this);

				if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
					undoManager = (FlexoUndoManager) editingContext.getUndoManager();
					undoManager.addToIgnoreHandlers(ignoreHandler);
				}
				resourceData.delete();
				if (undoManager != null) {
					undoManager.removeFromIgnoreHandlers(ignoreHandler);
				}
			}
			resourceData = null;
			notifyResourceUnloaded();

			isUnloading = false;
		}
	}

	/**
	 * Return flag indicating if this resource support external update<br>
	 * {@link PamelaResource} supports it, so return true
	 * 
	 * @return
	 */
	@Override
	public boolean isUpdatable() {
		return true;
	}

	/**
	 * If this resource support external update (reloading), perform it now<br>
	 * Default implementation does nothing
	 * 
	 * @param updatedResourceData
	 * @see #isUpdatable()
	 */
	@Override
	public void updateResourceData() {
		System.out.println("OK on met a jour la resource avec sa resource data qu'on reloade");
		RD reloadedResourceData;
		try {

			System.out.println("On recharge la resource");
			// Reload resource data
			reloadedResourceData = performLoad();
			reloadedResourceData.setResource(this);

			System.out.println("On obtient " + getFactory().stringRepresentation(reloadedResourceData));

			System.out.println("Et ensuite on update");

			// Now perform PAMELA updating with reloaded resource data
			// Existing model will be updated and notified
			resourceData.updateWith(reloadedResourceData);

			System.out.println("Hop");

			System.out.println("Apres le updateWith " + getFactory().stringRepresentation(resourceData));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This should be removed from Pamela Resource class
	private File getFile() {
		return (File) getIODelegate().getSerializationArtefact();
	}

	/**
	 * Return a FlexoIOStreamDelegate associated to this flexo resource
	 * 
	 * @return
	 */
	public final StreamIODelegate<?> getFlexoIOStreamDelegate() {
		if (getIODelegate() instanceof StreamIODelegate) {
			return (StreamIODelegate<?>) getIODelegate();
		}
		return null;
	}

	public final InputStream getInputStream() {
		if (getFlexoIOStreamDelegate() != null) {
			return getFlexoIOStreamDelegate().getInputStream();
		}
		return null;
	}

	public final OutputStream getOutputStream() {
		if (getFlexoIOStreamDelegate() != null) {
			return getFlexoIOStreamDelegate().getOutputStream();
		}
		return null;
	}

	/**
	 * Save current resource data to current XML resource file.<br>
	 * Forces XML version to be the latest one.
	 * 
	 * @return
	 */
	protected final void saveResourceData(boolean clearIsModified) throws SaveResourceException, SaveResourcePermissionDeniedException {
		// System.out.println("PamelaResourceImpl Saving " + getFile());
		if (!getIODelegate().hasWritePermission()) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Permission denied : " + getIODelegate().toString());
			}
			throw new SaveResourcePermissionDeniedException(getIODelegate());
		}
		if (resourceData != null) {
			_saveResourceData(clearIsModified);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Succeeding to save Resource " + this + " : " + getFile().getName() + " version=" + getModelVersion()
						+ " with date " + FileUtils.getDiskLastModifiedDate(getFile()));
			}
		}
		if (clearIsModified) {
			try {
				getResourceData().clearIsModified(false);
				// No need to reset the last memory update since it is valid
				notifyResourceSaved();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void _saveResourceData(boolean clearIsModified) throws SaveResourceException {
		File temporaryFile = null;
		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			logger.info("Saving SOURCE resource " + this + " : " + getFlexoIOStreamDelegate().getSourceResource().getFile() + " version="
					+ getModelVersion());
		}
		else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Saving resource " + this + " : " + getFile() + " version=" + getModelVersion());
			}
		}
		try {
			/*
			 * File dir = getFile().getParentFile(); willWrite(dir); if
			 * (!dir.exists()) { dir.mkdirs(); } willWrite(getFile());
			 */
			// Make local copy
			makeLocalCopy();
			// Using temporary file

			temporaryFile = File.createTempFile("temp", ".xml", getFile().getParentFile());
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
			}
			performXMLSerialization(/* handler, */temporaryFile);
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Renaming temp file " + temporaryFile.getAbsolutePath() + " to " + getFile().getAbsolutePath());
			}
			// Renaming temporary file is done in post serialization
			postXMLSerialization(temporaryFile, lock, clearIsModified);
		} catch (IOException e) {
			e.printStackTrace();
			if (temporaryFile != null) {
				temporaryFile.delete();
			}
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Failed to save resource " + this + " with model version " + getModelVersion());
			}
			getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
			throw new SaveResourceException(getIODelegate(), e);
		} /*
			* finally { hasWritten(getFile());
			* hasWritten(getFile().getParentFile()); }
			*/
	}

	/**
	 * @param version
	 * @param temporaryFile
	 * @param lock
	 * @param clearIsModified
	 * @throws IOException
	 */
	private void postXMLSerialization(File temporaryFile, FileWritingLock lock, boolean clearIsModified) throws IOException {
		if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			FileUtils.rename(temporaryFile, getFlexoIOStreamDelegate().getSourceResource().getFile());
		}
		else {
			FileUtils.rename(temporaryFile, getFile());
		}
		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

	/**
	 * @param version
	 * @param handler
	 * @param temporaryFile
	 * @throws InvalidObjectSpecificationException
	 * @throws InvalidModelException
	 * @throws AccessorInvocationException
	 * @throws DuplicateSerializationIdentifierException
	 * @throws IOException
	 */
	private void performXMLSerialization(/* SerializationHandler handler, */File temporaryFile) throws IOException {
		try (FileOutputStream out = new FileOutputStream(temporaryFile)) {
			getFactory().serialize(resourceData, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	/*
	 * private StringEncoder STRING_ENCODER = null;
	 * 
	 * @Override public StringEncoder getStringEncoder() { if (STRING_ENCODER ==
	 * null) { if (this instanceof FlexoProjectResource) { STRING_ENCODER = new
	 * StringEncoder(super.getStringEncoder(), ((FlexoProjectResource)
	 * this).getProject() .getObjectReferenceConverter()); } else {
	 * STRING_ENCODER = super.getStringEncoder(); } } return STRING_ENCODER; }
	 */

	public void recoverFile() {
		if (getFile() == null) {
			return;
		}
		if (getFile().exists()) {
			return;
		}
		if (getFile().getParentFile().exists()) {
			File[] files = getFile().getParentFile().listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.getName().equalsIgnoreCase(getFile().getName())) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Found file " + file.getAbsolutePath() + ". Using it and repairing project as well!");
					}
					((FileIODelegate) getIODelegate()).setSerializationArtefact(file);
					break;
				}
			}
		}
	}

	/**
	 * Manually converts resource file from version v1 to version v2. This methods only warns and does nothing, and must be overriden in
	 * subclasses !
	 * 
	 * @param v1
	 * @param v2
	 * @return boolean indicating if conversion was sucessfull
	 */
	protected boolean convertResourceFileFromVersionToVersion(FlexoVersion v1, FlexoVersion v2) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Unable to find converter for resource " + this + " from version " + v1 + " to version " + v2);
		}
		return false;
	}

	private void makeLocalCopy() throws IOException {

		File fileToSave = getFile();
		if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			fileToSave = getFlexoIOStreamDelegate().getSourceResource().getFile();
		}

		if (fileToSave != null && fileToSave.exists()) {
			String localCopyName = fileToSave.getName() + "~";
			File localCopy = new File(fileToSave.getParentFile(), localCopyName);
			FileUtils.copyFileToFile(fileToSave, localCopy);
		}
	}

	@Override
	public FlexoVersion latestVersion() {
		return null;
	}

	private boolean lastUniqueIDHasBeenSet = false;
	private long lastID;

	public boolean lastUniqueIDHasBeenSet() {
		return lastUniqueIDHasBeenSet;
	}

	@Override
	public long getNewFlexoID() {
		if (lastID < 0) {
			return -1;
		}
		return ++lastID;
	}

	/**
	 * @return Returns the lastUniqueID.
	 */
	@Override
	public long getLastID() {
		if (lastUniqueIDHasBeenSet && lastID < 0) {
			lastID = 0;
		}
		return lastID;
	}

	/**
	 * @param lastUniqueID
	 *            The lastUniqueID to set.
	 */
	@Override
	public void setLastID(long lastUniqueID) {
		if (lastUniqueID > lastID) {
			lastID = lastUniqueID;
			lastUniqueIDHasBeenSet = true;
			logger.fine("Resource " + this + " lastID is now " + lastID);
		}
	}

	private Map<String, Map<Long, FlexoObject>> objects = new HashMap<>();
	private boolean indexed = false;
	private boolean isIndexing = false;

	@Override
	public boolean isIndexing() {
		return isIndexing;
	}

	@Override
	public void register(FlexoObject object) {
		Map<Long, FlexoObject> objectsForUserIdentifier = objects.get(object.getUserIdentifier());
		if (objectsForUserIdentifier == null) {
			objectsForUserIdentifier = new HashMap<>();
			objects.put(object.getUserIdentifier(), objectsForUserIdentifier);
		}
		objectsForUserIdentifier.put(object.getFlexoID(), object);
	}

	/**
	 * Generic method used to retrieve in this resource an object with supplied objectIdentifier, userIdentifier, and type identifier<br>
	 * 
	 * Note that for certain resources, some parameters might not be used (for example userIdentifier or typeIdentifier)
	 * 
	 * @param objectIdentifier
	 * @param userIdentifier
	 * @param typeIdentifier
	 * @return
	 */
	@Override
	public FlexoObject findObject(String objectIdentifier, String userIdentifier, String typeIdentifier) {
		return getFlexoObject(Long.parseLong(objectIdentifier), userIdentifier);
	}

	/**
	 * Used to compute identifier of an object asserting this object is the {@link ResourceData} itself, or a {@link InnerResourceData}
	 * object stored inside this resource
	 * 
	 * @param object
	 * @return a String identifying supplied object (semantics is composite key using userIdentifier and typeIdentifier)
	 */
	@Override
	public String getObjectIdentifier(Object object) {
		if (object instanceof FlexoObject) {
			return Long.toString(((FlexoObject) object).getFlexoID());
		}
		logger.warning("Object " + object + " is not a FlexoObject");
		return "???";
	}

	/**
	 * Used to compute user identifier of an object asserting this object is the {@link ResourceData} itself, or a {@link InnerResourceData}
	 * object stored inside this resource
	 * 
	 * @param object
	 * @return a String identifying author (user) of supplied object
	 */
	@Override
	public String getUserIdentifier(Object object) {
		if (object instanceof FlexoObject) {
			return ((FlexoObject) object).getUserIdentifier();
		}
		logger.warning("Object " + object + " is not a FlexoObject");
		return "FLX";
	}

	/**
	 * Internally called to index all objects found in resource
	 * 
	 */
	protected void indexResource() {

		isIndexing = true;
		// System.out.println("Indexing PamelaResource " + this);
		// objects = new HashMap<>();
		List<Object> allObjects = getFactory().getEmbeddedObjects(getLoadedResourceData(), EmbeddingType.CLOSURE);
		allObjects.add(getLoadedResourceData());
		for (Object temp : allObjects) {
			if (temp instanceof FlexoObject) {
				register((FlexoObject) temp);
			}
		}
		// System.out.println("Done indexing PamelaResource " + this);
		isIndexing = false;
	}

	/**
	 * Retrieve object with supplied flexoId and userIdentifier
	 * 
	 * @param flexoId
	 * @param userIdentifier
	 * @return
	 */
	@Override
	public FlexoObject getFlexoObject(Long flexoId, String userIdentifier) {

		if (flexoId == null) {
			return null;
		}

		if (!isLoaded()) {
			return null;
		}

		if (!indexed) {
			indexResource();
			indexed = true;
		}

		if (userIdentifier != null) {
			Map<Long, FlexoObject> objectsForUserIdentifier = objects.get(userIdentifier);
			if (objectsForUserIdentifier != null) {
				return objectsForUserIdentifier.get(flexoId);
			}
		}
		else {
			for (Map<Long, FlexoObject> objectMap : objects.values()) {
				if (objectMap.containsKey(flexoId)) {
					return objectMap.get(flexoId);
				}
			}
		}

		return null;
	}

	/**
	 * Read an XML input stream from File and return the parsed Document
	 */
	public static Document readXMLFile(File f) throws JDOMException, IOException {
		try (FileInputStream fio = new FileInputStream(f)) {
			return readXMLInputStream(fio);
		}
	}

	/**
	 * Read an XML input stream and return the parsed Document
	 */
	public static Document readXMLInputStream(InputStream inputStream) throws JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		Document reply = parser.build(inputStream);
		return reply;
	}

	public static Element getElement(Parent parent, String name) {
		Iterator<Element> it = parent.getDescendants(new ElementFilter(name));
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}

	/**
	 * This handler allows to ignore edits raised during deserialization
	 * 
	 * @author sylvain
	 * 
	 */
	public static class IgnoreLoadingEdits implements IgnoreHandler {

		private final PamelaResource<?, ?> resource;

		public IgnoreLoadingEdits(PamelaResource<?, ?> resource) {
			this.resource = resource;
		}

		@Override
		public boolean isIgnorable(UndoableEdit edit) {
			if (edit instanceof AtomicEdit) {
				// Unsused Object o =
				((AtomicEdit<?>) edit).getObject();
				if (((AtomicEdit<?>) edit).getModelFactory() == resource.getFactory()) {
					// System.out.println("PAMELA RESOURCE LOADING : Ignore edit
					// "
					// + edit);
					return true;
				}
			}
			return false;
		}
	}

}
