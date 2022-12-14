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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.Resource;

@ModelEntity
@ImplementationClass(InJarIODelegate.InJarIODelegateImpl.class)
@XMLElement
@Imports({ @Import(DirectoryBasedJarIODelegate.class) })
public interface InJarIODelegate extends StreamIODelegate<InJarResourceImpl> {

	@PropertyIdentifier(type = InJarResourceImpl.class)
	public static final String IN_JAR_RESOURCE = "inJarResource";

	@Getter(value = IN_JAR_RESOURCE, ignoreType = true)
	public InJarResourceImpl getInJarResource();

	@Setter(IN_JAR_RESOURCE)
	public void setInJarResource(InJarResourceImpl inJarResource);

	public abstract class InJarIODelegateImpl extends StreamIODelegateImpl<InJarResourceImpl> implements InJarIODelegate {

		protected static final Logger logger = Logger.getLogger(InJarIODelegateImpl.class.getPackage().getName());

		public static InJarIODelegate makeInJarFlexoIODelegate(InJarResourceImpl inJarResource, PamelaModelFactory factory) {
			InJarIODelegate delegate = factory.newInstance(InJarIODelegate.class);
			delegate.setInJarResource(inJarResource);
			return delegate;
		}

		@Override
		public InJarResourceImpl getSerializationArtefact() {
			return getInJarResource();
		}

		@Override
		public Resource getSerializationArtefactAsResource() {
			return getInJarResource();
		}

		@Override
		public String stringRepresentation() {
			return getInJarResource().toString();
		}

		@Override
		public InputStream getInputStream() {
			return getInJarResource().openInputStream();
		}

		@Override
		public void setInputStream(InputStream inputStream) {
		}

		@Override
		public OutputStream getOutputStream() {
			if (getSaveToSourceResource() && getSourceResource() != null) {
				System.out.println("Saving as source resource instead of file resource");
				System.out.println("Was in jar");
				System.out.println("Using " + getSourceResource());
				try {
					return new FileOutputStream(getSourceResource().getFile());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			}
			// can't save if not in sources
			return null;
		}

		@Override
		public void setOutputStream(OutputStream outsputStream) {
			// can't write
		}

		@Override
		public synchronized boolean hasWritePermission() {
			return false;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public boolean delete() {
			// can't delete
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public String getParentPath() {
			return null;
		}

		@Override
		public void rename(String newName) throws CannotRenameException {
			throw new CannotRenameException(getFlexoResource());
		}

		@Override
		public void save(FlexoResource<?> resource) throws NotImplementedException {
			throw new NotImplementedException("In-jar resources cannot be saved");
		}

		@Override
		public RepositoryFolder<?, InJarResourceImpl> getRepositoryFolder(ResourceRepository<?, InJarResourceImpl> resourceRepository,
				boolean createWhenNonExistent) throws IOException {
			return resourceRepository.getRootFolder();
		}

		/*@Override
		public Resource getSerializationArtefactAsResource(InJarResourceImpl serializationArtefact) {
			return serializationArtefact;
		}*/

		/*@Override
		public InJarResourceImpl locateResourceRelativeToParentPath(String relativePathName) {
			InJarResourceImpl current = getSerializationArtefact().getContainer();
			StringTokenizer st = new StringTokenizer(relativePathName, "/\\");
			while (st.hasMoreElements()) {
				String pathElement = st.nextToken();
				if (pathElement.equals("..")) {
					current = current.getContainer();
				}
				else {
					boolean foundChild = false;
					for (InJarResourceImpl child : current.getContents()) {
						if (child.getName().equals(pathElement)) {
							current = child;
							foundChild = true;
							break;
						}
					}
					if (!foundChild) {
						logger.warning("Could not find contained path element " + pathElement + " for jar entry " + current);
						return null;
					}
				}
			}
			return current;
		}*/

	}

}
