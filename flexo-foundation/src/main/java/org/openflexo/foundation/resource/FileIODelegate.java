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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FileUtils;

/**
 * Represents an I/O delegate based on a File<br>
 * To be used when associated {@link FlexoResource} is serialized into a simple {@link File}
 * 
 * 
 * @author vincent,sylvain
 *
 */
@ModelEntity
@XMLElement
@Imports({ @Import(DirectoryBasedIODelegate.class), @Import(GitIODelegate.class) })
@ImplementationClass(FileIODelegate.FileIODelegateImpl.class)
public interface FileIODelegate extends StreamIODelegate<File> {

	public static final String FILE = "file";

	@Getter(FILE)
	@XMLAttribute
	public File getFile();

	@Setter(FILE)
	public void setFile(File file);

	// public boolean renameFileTo(String name) throws InvalidFileNameException,
	// IOException;

	public boolean delete(boolean deleteFile);

	public String getFileName();

	public File createTemporaryArtefact(String fileExtension) throws IOException;

	public abstract class FileIODelegateImpl extends StreamIODelegateImpl<File> implements FileIODelegate {

		private final Logger logger = Logger.getLogger(FileIODelegateImpl.class.getPackage().getName());

		private static final FileSystemResourceLocatorImpl FS_RESOURCE_LOCATOR = new FileSystemResourceLocatorImpl();

		public static FileIODelegate makeFileFlexoIODelegate(File file, PamelaModelFactory factory) {
			FileIODelegate fileIODelegate = factory.newInstance(FileIODelegate.class);
			fileIODelegate.setFile(file);
			return fileIODelegate;
		}

		@Override
		public File getSerializationArtefact() {
			return getFile();
		}

		@Override
		public Resource getSerializationArtefactAsResource() {
			return FS_RESOURCE_LOCATOR.retrieveResource(getSerializationArtefact());
		}

		@Override
		public RepositoryFolder<?, File> getRepositoryFolder(ResourceRepository<?, File> resourceRepository, boolean createWhenNonExistent)
				throws IOException {
			return resourceRepository.getParentRepositoryFolder(getFile(), true);
		}

		@Override
		public synchronized boolean hasWritePermission() {
			return getFile() == null || (!getFile().exists() || getFile().canWrite()) && getFile().getParentFile() != null
					&& (!getFile().getParentFile().exists() || getFile().getParentFile().canWrite());
		}

		private boolean renameFileTo(String name) throws IOException {
			if (name != null && getFile() != null) {
				File newFile = new File(getFile().getParentFile(), name);
				if (getFile().exists()) {
					FileUtils.rename(getFile(), newFile);
					if (getFile().exists()) {
						getFile().delete();
					}
					setFile(newFile);
					resetDiskLastModifiedDate();
				}
				return true;
			}
			return false;
		}

		/**
		 * Delete this resource by deleting the file
		 */
		@Override
		public boolean delete() {
			if (hasWritePermission()) {
				return delete(true);
			}
			logger.warning("Delete requested for READ-ONLY file resource " + this);
			return false;
		}

		@Override
		public FileWritingLock willWriteOnDisk() {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("willWriteOnDisk()");
			}
			_isSaving = true;

			if (!getFile().getParentFile().exists()) {
				getFile().getParentFile().mkdirs();
			}

			getFlexoResource().getServiceManager().notify(null, new WillWriteFileOnDiskNotification(getFile().getParentFile()));
			getFlexoResource().getServiceManager().notify(null, new WillWriteFileOnDiskNotification(getFile()));

			// This locking scheme was an attempt which seems to be unnecessary
			// Disactivated it. But kept for future needing if required
			// return new FileWritingLock();
			return null;
		}

		@Override
		public void hasWrittenOnDisk(FileWritingLock lock) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("hasWrittenOnDisk()");
			}

			getFlexoResource().getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(getFile().getParentFile()));
			getFlexoResource().getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(getFile()));

			if (lock != null) {
				lock.start();
			}
			else {
				notifyHasBeenWrittenOnDisk();
			}
		}

		/**
		 * Delete this resource. Delete file is flag deleteFile is true.
		 */
		@Override
		public boolean delete(boolean deleteFile) {
			if (hasWritePermission()) {
				if (getFile() != null && getFile().exists() && deleteFile) {
					getFlexoResource().getServiceManager().getResourceManager().addToFilesToDelete(getFile());
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Will delete file " + getFile().getAbsolutePath() + " upon next save of RM");
					}
				}
				return true;
			}
			logger.warning("Delete requested for READ-ONLY file resource " + this);
			return false;
		}

		@Override
		public boolean exists() {
			if (getFile() == null) {
				return false;
			}
			return getFile().exists();
		}

		@Override
		public String stringRepresentation() {
			return getFile().getAbsolutePath();
		}

		@Override
		public String toString() {
			if (getFile() != null) {
				return getFile().getAbsolutePath();
			}
			return "null";
		}

		@Override
		public InputStream getInputStream() {
			try {
				return new FileInputStream(getFile());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}

		}

		@Override
		public OutputStream getOutputStream() {
			try {
				if (getSaveToSourceResource() && getSourceResource() != null) {
					System.out.println("Saving as source resource instead of file resource");
					System.out.println("Was in " + getFile());
					System.out.println("Using " + getSourceResource());
					return new FileOutputStream(getSourceResource().getFile());
				}
				return new FileOutputStream(getFile());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public String getParentPath() {
			if (getFile().isDirectory()) {
				return getFile().getAbsolutePath();
			}
			return getFile().getParent();
		}

		@Override
		public void setOutputStream(OutputStream outsputStream) {
		}

		@Override
		public void setInputStream(InputStream inputStream) {
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public void rename(String newName) throws CannotRenameException {
			try {
				renameFileTo(newName);
			} catch (IOException e) {
				throw new CannotRenameException(getFlexoResource());
			}
		}

		@Override
		public String getFileName() {
			return getFlexoResource().getName();
		}

		@Override
		public void save(FlexoResource<?> resource) throws NotImplementedException {
		}

		/*
		 * @Override public FileResourceImpl
		 * locateResourceRelativeToParentPath(String relativePathName) { File
		 * currentFile = new File(getSerializationArtefact().getParentFile(),
		 * relativePathName); if (currentFile.exists()) { return
		 * FS_RESOURCE_LOCATOR.retrieveResource(currentFile); } return null; }
		 */

		@Override
		public File createTemporaryArtefact(String fileExtension) throws IOException {
			return File.createTempFile("temp", fileExtension, getSerializationArtefact().getParentFile());
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

	public static class FileHasBeenWrittenOnDiskNotification implements ServiceNotification {
		private final File file;

		public FileHasBeenWrittenOnDiskNotification(File file) {
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

}
