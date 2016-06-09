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
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager.IgnoreHandler;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.kvc.AccessorInvocationException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EmbeddingType;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.undo.AtomicEdit;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

import com.google.common.base.Throwables;

/**
 * Default implementation for {@link PamelaResource} (a resource where underlying model is managed by PAMELA framework)
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author Sylvain
 * 
 */
public abstract class PamelaResourceImpl<RD extends ResourceData<RD>, F extends ModelFactory & PamelaResourceModelFactory>
		extends FlexoResourceImpl<RD>implements PamelaResource<RD, F> {

	private static final Logger logger = Logger.getLogger(PamelaResourceImpl.class.getPackage().getName());

	private boolean isLoading = false;

	// private boolean isConverting = false;
	// protected boolean performLoadWithPreviousVersion = true;

	/**
	 * Save the &quot;real&quot; resource data of this resource.
	 * 
	 * @throws SaveResourceException
	 */
	@Override
	public final void save(IProgress progress) throws SaveResourceException {
		if (progress != null) {
			progress.setProgress(FlexoLocalization.localizedForKey("saving") + " " + this.getName());
		}
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
	public RD loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		if (resourceData != null) {
			// already loaded
			return resourceData;
		}

		startDeserializing();

		isLoading = true;
		if (progress != null) {
			progress.setProgress(FlexoLocalization.localizedForKey("loading") + " " + this.getName());
			progress.resetSecondaryProgress(4);
			progress.setProgress(FlexoLocalization.localizedForKey("loading_from_disk"));
		}

		LoadResourceException exception = null;

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Load resource data for " + this);
		}
		/*TODO
		 * if (!getFile().exists()) {
			recoverFile();
			if (!getFile().exists()) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe("File " + getFile().getAbsolutePath() + " does not exist, throwing exception now!");
				}
				throw new FlexoFileNotFoundException(this);
			}
		}*/

		/*EditingContext editingContext = getServiceManager().getEditingContext();
		IgnoreLoadingEdits ignoreHandler = null;
		FlexoUndoManager undoManager = null;
		
		if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
			undoManager = (FlexoUndoManager) editingContext.getUndoManager();
			undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(this));
			// System.out.println("@@@@@@@@@@@@@@@@ START LOADING RESOURCE " + getURI());
		}*/

		try {

			resourceData = performLoad();
			isLoading = false;
			resourceData.setResource(this);
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
			isLoading = false;
			/*if (ignoreHandler != null) {
				undoManager.removeFromIgnoreHandlers(ignoreHandler);
				// System.out.println("@@@@@@@@@@@@@@@@ END LOADING RESOURCE " + getURI());
			}*/
			stopDeserializing();

		}
		return null;

	}

	protected RD performLoad() throws IOException, Exception {
		// Retrieve the data from an input stream given by the FlexoIOStream delegate of the resource
		return (RD) getFactory().deserialize(getFlexoIOStreamDelegate().getInputStream());
	}

	// This should be removed from Pamela Resource class
	private File getFile() {
		return (File) getFlexoIODelegate().getSerializationArtefact();
	}

	/**
	 * Return a FlexoIOStreamDelegate associated to this flexo resource
	 * 
	 * @return
	 */
	public FlexoIOStreamDelegate<?> getFlexoIOStreamDelegate() {
		return (FlexoIOStreamDelegate<?>) getFlexoIODelegate();
	}

	/**
	 * Save current resource data to current XML resource file.<br>
	 * Forces XML version to be the latest one.
	 * 
	 * @return
	 */
	protected final void saveResourceData(boolean clearIsModified) throws SaveResourceException, SaveResourcePermissionDeniedException {
		// System.out.println("PamelaResourceImpl Saving " + getFile());
		if (!getFlexoIODelegate().hasWritePermission()) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Permission denied : " + getFlexoIODelegate().toString());
			}
			throw new SaveResourcePermissionDeniedException(getFlexoIODelegate());
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
				getResourceData(null).clearIsModified(false);
				// No need to reset the last memory update since it is valid
				notifyResourceSaved();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class WillWriteFileOnDiskNotification implements ServiceNotification {
		private final File file;

		public WillWriteFileOnDiskNotification(File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}
	}

	public static class WillRenameFileOnDiskNotification implements ServiceNotification {
		private final File fromFile;
		private final File toFile;

		public WillRenameFileOnDiskNotification(File fromFile, File toFile) {
			this.fromFile = fromFile;
			this.toFile = toFile;
		}

		public File getFromFile() {
			return fromFile;
		}

		public File getToFile() {
			return toFile;
		}
	}

	public static class WillDeleteFileOnDiskNotification implements ServiceNotification {
		private final File file;

		public WillDeleteFileOnDiskNotification(File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}
	}

	public void willWrite(File file) {
		getServiceManager().notify(null, new WillWriteFileOnDiskNotification(file));
	}

	public void willRename(File fromFile, File toFile) {
		getServiceManager().notify(null, new WillRenameFileOnDiskNotification(fromFile, toFile));
	}

	public void willDelete(File file) {
		getServiceManager().notify(null, new WillDeleteFileOnDiskNotification(file));
	}

	protected void _saveResourceData(boolean clearIsModified) throws SaveResourceException {
		File temporaryFile = null;
		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (logger.isLoggable(Level.INFO)) {
			logger.info("Saving resource " + this + " : " + getFile() + " version=" + getModelVersion());
		}

		try {
			File dir = getFile().getParentFile();
			if (!dir.exists()) {
				willWrite(dir);
				dir.mkdirs();
			}
			willWrite(getFile());
			// Make local copy
			makeLocalCopy();
			// Using temporary file
			temporaryFile = File.createTempFile("temp", ".xml", dir);
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
			}
			performXMLSerialization(/*handler,*/temporaryFile);
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
			throw new SaveResourceException(getFlexoIODelegate(), e);
		}
	}

	/**
	 * @param version
	 * @param temporaryFile
	 * @param lock
	 * @param clearIsModified
	 * @throws IOException
	 */
	private void postXMLSerialization(File temporaryFile, FileWritingLock lock, boolean clearIsModified) throws IOException {
		FileUtils.rename(temporaryFile, getFile());
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
	private void performXMLSerialization(/*SerializationHandler handler,*/File temporaryFile) throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(temporaryFile);
			getFactory().serialize(resourceData, out);
			out.flush();
			out.close();
			out = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		} finally {
			if (out != null) {
				out.close();
			}
			out = null;
		}
	}

	/*private StringEncoder STRING_ENCODER = null;
	
	@Override
	public StringEncoder getStringEncoder() {
		if (STRING_ENCODER == null) {
			if (this instanceof FlexoProjectResource) {
				STRING_ENCODER = new StringEncoder(super.getStringEncoder(), ((FlexoProjectResource) this).getProject()
						.getObjectReferenceConverter());
			} else {
				STRING_ENCODER = super.getStringEncoder();
			}
		}
		return STRING_ENCODER;
	}*/

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
					((FileFlexoIODelegate) getFlexoIODelegate()).setSerializationArtefact(file);
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
		if (getFile() != null && getFile().exists()) {
			String localCopyName = getFile().getName() + "~";
			File localCopy = new File(getFile().getParentFile(), localCopyName);
			FileUtils.copyFileToFile(getFile(), localCopy);

		}
	}

	@Override
	public FlexoVersion latestVersion() {
		// TODO Auto-generated method stub
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

	private Map<String, Map<Long, FlexoObject>> objects;
	private boolean isIndexing = false;

	@Override
	public boolean isIndexing() {
		return isIndexing;
	}

	/**
	 * Internally called to index all objects found in resource
	 * 
	 */
	protected void indexResource() {

		isIndexing = true;
		//System.out.println("Indexing PamelaResource " + this);
		objects = new HashMap<>();
		List<Object> allObjects = getFactory().getEmbeddedObjects(getLoadedResourceData(), EmbeddingType.CLOSURE);
		allObjects.add(getLoadedResourceData());
		for (Object temp : allObjects) {
			if (temp instanceof FlexoObject) {
				FlexoObject o = (FlexoObject) temp;
				Map<Long, FlexoObject> objectsForUserIdentifier = objects.get(o.getUserIdentifier());
				if (objectsForUserIdentifier == null) {
					objectsForUserIdentifier = new HashMap<>();
					objects.put(o.getUserIdentifier(), objectsForUserIdentifier);
				}
				objectsForUserIdentifier.put(o.getFlexoID(), o);
			}
		}
		//System.out.println("Done indexing PamelaResource " + this);
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

		if (flexoId == null || userIdentifier == null) {
			return null;
		}

		if (!isLoaded()) {
			return null;
		}

		if (objects == null) {
			indexResource();
		}

		Map<Long, FlexoObject> objectsForUserIdentifier = objects.get(userIdentifier);
		if (objectsForUserIdentifier != null) {
			return objectsForUserIdentifier.get(flexoId);
		}

		return null;
	}

	/**
	 * Read an XML input stream from File and return the parsed Document
	 */
	public static Document readXMLFile(File f) throws JDOMException, IOException {
		FileInputStream fio = new FileInputStream(f);
		return readXMLInputStream(fio);
	}

	/**
	 * Read an XML input stream and return the parsed Document
	 */
	public static Document readXMLInputStream(InputStream inputStream) throws JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		Document reply = parser.build(inputStream);
		return reply;
	}

	public static Element getElement(Document document, String name) {
		Iterator it = document.getDescendants(new ElementFilter(name));
		if (it.hasNext()) {
			return (Element) it.next();
		}
		else {
			return null;
		}
	}

	public static Element getElement(Element from, String name) {
		Iterator it = from.getDescendants(new ElementFilter(name));
		if (it.hasNext()) {
			return (Element) it.next();
		}
		else {
			return null;
		}
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
				Object o = ((AtomicEdit) edit).getObject();
				if (((AtomicEdit) edit).getModelFactory() == resource.getFactory()) {
					// System.out.println("PAMELA RESOURCE LOADING : Ignore edit "
					// + edit);
					return true;
				}
			}
			return false;
		}
	}

}
