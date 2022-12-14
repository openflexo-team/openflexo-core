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
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.JarInDirClassLoader;

/**
 * Represents an I/O delegate based on a directory and a core file inside this directory<br>
 * The naming conventions expect the directory named as the resource name + directory extension, and the core file inside named as the
 * resource name + file extension. <br>
 * To be used when associated {@link FlexoResource} is serialized into a directory
 * 
 * 
 * @author vincent,sylvain
 *
 */
@ModelEntity
@XMLElement
@ImplementationClass(DirectoryBasedIODelegate.DirectoryBasedIODelegateImpl.class)
public interface DirectoryBasedIODelegate extends FileIODelegate {

	// public static final String FILE_EXTENSION = "fileExtension";
	// public static final String DIRECTORY_EXTENSION = "directoryExtension";

	/**
	 * Return directory where core file is stored
	 * 
	 * @return
	 */
	public File getDirectory();

	/**
	 * Sets directory where core file is stored
	 * 
	 * @param directory
	 */
	public void setDirectory(File directory);

	/**
	 * Return core file (defined as resource name + file extension)
	 * 
	 * @return
	 */
	@Override
	public File getFile();

	public void moveToDirectory(File newDirectory);

	/*@Getter(FILE_EXTENSION)
	@XMLAttribute
	public String getFileExtension();
	
	@Setter(FILE_EXTENSION)
	public void setFileExtension(String extension);
	
	@Getter(DIRECTORY_EXTENSION)
	@XMLAttribute
	public String getDirectoryExtension();
	
	@Setter(DIRECTORY_EXTENSION)
	public void setDirectoryExtension(String extension);*/

	public abstract class DirectoryBasedIODelegateImpl extends FileIODelegateImpl implements DirectoryBasedIODelegate {

		private final Logger logger = Logger.getLogger(DirectoryBasedIODelegateImpl.class.getPackage().getName());

		private File directory;

		public static DirectoryBasedIODelegate makeDirectoryBasedFlexoIODelegate(File directory, File file, PamelaModelFactory factory) {
			DirectoryBasedIODelegate fileIODelegate = factory.newInstance(DirectoryBasedIODelegate.class);
			/*fileIODelegate.setDirectoryExtension(directoryExtension);
			fileIODelegate.setFileExtension(fileExtension);
			File directory = new File(containerDir, baseName + directoryExtension);
			File file = new File(directory, baseName + fileExtension);*/
			fileIODelegate.setDirectory(directory);
			fileIODelegate.setFile(file);
			return fileIODelegate;
		}

		@Override
		public File getDirectory() {
			return directory;
		}

		@Override
		public void setDirectory(File directory) {
			if ((directory == null && this.directory != null) || (directory != null && !directory.equals(this.directory))) {
				File oldValue = this.directory;
				this.directory = directory;
				getPropertyChangeSupport().firePropertyChange("directory", oldValue, directory);
			}
		}

		@Override
		public void rename(String newName) throws CannotRenameException {
			System.out.println("OK, c'est parti pour un renommage dans DirectoryBasedFlexoIODelegate");
			System.out.println("le nouveau nom c'est " + newName);
			Thread.dumpStack();

			String directoryExtension = getDirectoryName().substring(getDirectoryName().indexOf("."));
			String fileExtension = getFileName().substring(getFileName().indexOf("."));

			System.out.println("directoryExtension=" + directoryExtension);
			System.out.println("fileExtension=" + fileExtension);

			if (newName.endsWith(directoryExtension)) {
				newName = newName.substring(0, newName.lastIndexOf(directoryExtension));
			}
			if (newName.endsWith(fileExtension)) {
				newName = newName.substring(0, newName.lastIndexOf(fileExtension));
			}

			String newFileName = newName.endsWith(fileExtension) ? newName : newName + fileExtension;
			String newDirectoryName = newName.endsWith(directoryExtension) ? newName : newName + directoryExtension;

			System.out.println("newFileName=" + newFileName);
			System.out.println("newDirectoryName=" + newDirectoryName);

			File renamedFile = new File(getDirectory(), newFileName);
			if (getFile().exists()) {
				try {
					FileUtils.rename(getFile(), renamedFile);
				} catch (IOException e) {
					e.printStackTrace();
					throw new CannotRenameException(getFlexoResource());
				}
				if (getFile().exists()) {
					getFile().delete();
				}
			}
			File renamedDirectory = new File(getDirectory().getParentFile(), newDirectoryName);
			if (getDirectory().exists()) {
				try {
					FileUtils.rename(getDirectory(), renamedDirectory);
				} catch (IOException e) {
					e.printStackTrace();
					throw new CannotRenameException(getFlexoResource());
				}
			}

			System.out.println("renamedFile:" + renamedFile);
			System.out.println("renamedDirectory:" + renamedDirectory);

			setDirectory(renamedDirectory);
			setFile(new File(renamedDirectory, newFileName));

			System.out.println("file: " + getFile());

			resetDiskLastModifiedDate();

		}

		@Override
		public void moveToDirectory(File newDirectory) {
			String filename = getFileName();
			setDirectory(newDirectory);
			setFile(new File(newDirectory, filename));
			resetDiskLastModifiedDate();
		}

		@Override
		public String getFileName() {
			return getFile().getName();
		}

		public String getDirectoryName() {
			return getDirectory().getName();
		}

		/**
		 * Delete this resource. Delete file is flag deleteFile is true.
		 */
		@Override
		public boolean delete(boolean deleteFile) {
			boolean returned = super.delete(deleteFile);
			if (hasWritePermission()) {
				if (getDirectory() != null && getDirectory().exists() && deleteFile) {
					getFlexoResource().getServiceManager().getResourceManager().addToFilesToDelete(getDirectory());
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Will delete directory " + getDirectory().getAbsolutePath() + " upon next save of RM");
					}
				}
				return returned;
			}
			logger.warning("Delete requested for READ-ONLY file resource " + this);
			return false;
		}

		@Override
		public RepositoryFolder<?, File> getRepositoryFolder(ResourceRepository<?, File> resourceRepository, boolean createWhenNonExistent)
				throws IOException {
			return resourceRepository.getParentRepositoryFolder(getDirectory(), true);
		}

		@Override
		public ClassLoader retrieveClassLoader() {
			return new JarInDirClassLoader(Collections.singletonList(getDirectory()));
		}

		@Override
		public String getDisplayName() {
			return getDirectoryName();
		}

	}

}
