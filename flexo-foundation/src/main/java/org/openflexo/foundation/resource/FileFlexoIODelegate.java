package org.openflexo.foundation.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;

@ModelEntity
@XMLElement
public interface FileFlexoIODelegate extends FlexoIOStreamDelegate<File> {

	public static final String FILE = "file";

	@Getter(FILE)
	@XMLAttribute
	public File getFile();

	@Setter(FILE)
	public void setFile(File file);

	public boolean renameFileTo(String name) throws InvalidFileNameException, IOException;

	public boolean delete(boolean deleteFile);

	@Implementation
	public abstract class FileFlexoIODelegateImpl extends FlexoIOStreamDelegateImpl<File> implements FileFlexoIODelegate {

		private final Logger logger = Logger.getLogger(FileFlexoIODelegateImpl.class.getPackage().getName());

		public static FileFlexoIODelegate makeFileFlexoIODelegate(File file, ModelFactory factory) {
			FileFlexoIODelegate fileIODelegate = factory.newInstance(FileFlexoIODelegate.class);
			fileIODelegate.setFile(file);
			return fileIODelegate;
		}

		@Override
		public File getSerializationArtefact() {
			return getFile();
		}

		@Override
		public synchronized boolean hasWritePermission() {
			return getFile() == null || (!getFile().exists() || getFile().canWrite()) && getFile().getParentFile() != null
					&& (!getFile().getParentFile().exists() || getFile().getParentFile().canWrite());
		}

		@Override
		public boolean renameFileTo(String name) throws InvalidFileNameException, IOException {
			File newFile = new File(getFile().getParentFile(), name);
			if (getFile().exists()) {
				FileUtils.rename(getFile(), newFile);
				if (getFile().exists()) {
					getFile().delete();
				}
				resetDiskLastModifiedDate();
			}
			return true;
		}

		/**
		 * Delete this resource by deleting the file
		 */
		@Override
		public boolean delete() {
			if (hasWritePermission()) {
				return delete(true);
			} else {
				logger.warning("Delete requested for READ-ONLY file resource " + this);
				return false;
			}
		}

		@Override
		public FileWritingLock willWriteOnDisk() {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("willWriteOnDisk()");
			}
			_isSaving = true;
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
			if (lock != null) {
				lock.start();
			} else {
				notifyHasBeenWrittenOnDisk();
			}
		}

		/**
		 * Delete this resource. Delete file is flag deleteFile is true.
		 */
		@Override
		public boolean delete(boolean deleteFile) {
			if (hasWritePermission()) {
				// if (getFlexoResource().delete()) {
				if (getFile() != null && getFile().exists() && deleteFile) {
					getFlexoResource().getServiceManager().getResourceManager().addToFilesToDelete(getFile());
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Will delete file " + getFile().getAbsolutePath() + " upon next save of RM");
					}
				}
				return true;
				// }
				// return false;
			} else {
				logger.warning("Delete requested for READ-ONLY file resource " + this);
				return false;
			}
		}

		@Override
		public boolean exists() {
			if (getFile() == null) {
				return false;
			}
			return getFile().exists();
		}

		@Override
		public String toString() {
			return getFile().getAbsolutePath();
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
		public String getParentPath(){
			if(getFile().isDirectory()){
				return getFile().getAbsolutePath();
			}else{
				return getFile().getParent();
			}
		}

	}

}
