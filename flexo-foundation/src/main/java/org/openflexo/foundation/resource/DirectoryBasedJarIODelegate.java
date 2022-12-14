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
import java.net.URL;
import java.util.logging.Logger;

import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.toolbox.JarClassLoader;

/**
 * Represents an I/O delegate based on a directory and a core file inside this directory<br>
 * The naming conventions expect the directory named as the resource name + directory extension, and the core file inside named as the
 * resource name + file extension. <br>
 * To be used when associated {@link FlexoResource} is serialized into a directory, inside a Jar
 * 
 * 
 * @author vincent,sylvain
 *
 */
@ModelEntity
@XMLElement
@ImplementationClass(DirectoryBasedJarIODelegate.DirectoryBasedJarIODelegateImpl.class)
public interface DirectoryBasedJarIODelegate extends InJarIODelegate {

	// public static final String FILE_EXTENSION = "fileExtension";
	// public static final String DIRECTORY_EXTENSION = "directoryExtension";

	/**
	 * Return directory where core file is stored
	 * 
	 * @return
	 */
	public InJarResourceImpl getDirectory();

	/**
	 * Sets directory where core file is stored
	 * 
	 * @param directory
	 */
	public void setDirectory(InJarResourceImpl directory);

	/**
	 * Return core file (defined as resource name + file extension)
	 * 
	 * @return
	 */
	@Override
	public InJarResourceImpl getInJarResource();

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

	@Implementation
	public abstract class DirectoryBasedJarIODelegateImpl extends InJarIODelegateImpl implements DirectoryBasedJarIODelegate {

		private final Logger logger = Logger.getLogger(DirectoryBasedJarIODelegateImpl.class.getPackage().getName());

		private InJarResourceImpl directory;

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

		public static DirectoryBasedJarIODelegate makeDirectoryBasedFlexoIODelegate(InJarResourceImpl directory, InJarResourceImpl file,
				PamelaModelFactory factory) {
			DirectoryBasedJarIODelegate fileIODelegate = factory.newInstance(DirectoryBasedJarIODelegate.class);
			// fileIODelegate.setDirectoryExtension(directoryExtension);
			// fileIODelegate.setFileExtension(fileExtension);

			// System.out.println("Building DirectoryBasedJarIODelegate");
			// System.out.println("containerDir=" + containerDir);

			// InJarResourceImpl directory = resourceCenter.getDirectory(baseName + directoryExtension, containerDir);
			// System.out.println("directory=" + directory);

			// InJarResourceImpl file = resourceCenter.getDirectory(baseName + fileExtension, directory);
			// System.out.println("file=" + file);

			fileIODelegate.setDirectory(directory);
			fileIODelegate.setInJarResource(file);

			return fileIODelegate;
		}

		@Override
		public InJarResourceImpl getDirectory() {
			return directory;
		}

		@Override
		public void setDirectory(InJarResourceImpl directory) {
			if ((directory == null && this.directory != null) || (directory != null && !directory.equals(this.directory))) {
				InJarResourceImpl oldValue = this.directory;
				this.directory = directory;
				/*if (!this.directory.exists()) {
					this.directory.mkdirs();
				}*/
				getPropertyChangeSupport().firePropertyChange("directory", oldValue, directory);
			}
		}

		@Override
		public void rename(String newName) throws CannotRenameException {
			// Not applicable
		}

		@Override
		public ClassLoader retrieveClassLoader() {
			URL jarResource = getDirectory().getJarResource().getURL();
			ClassLoader returned = new JarClassLoader(jarResource, getClass().getClassLoader());
			return returned;
		}

		@Override
		public String toString() {
			return "DirectoryBasedJarIODelegate " + super.toString();
		}

	}

}
