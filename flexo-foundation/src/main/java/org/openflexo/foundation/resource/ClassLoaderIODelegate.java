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

import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.rm.Resource;

/**
 * AN I/O delegate plugged on a ClassLoader
 * 
 * 
 * @author sylvain
 *
 */
@ModelEntity
@XMLElement
public interface ClassLoaderIODelegate extends FlexoIODelegate<ClassLoader> {

	@Implementation
	public abstract class ClassLoaderIODelegateImpl implements ClassLoaderIODelegate {

		private final Logger logger = Logger.getLogger(ClassLoaderIODelegateImpl.class.getPackage().getName());

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public boolean delete() {
			return false;
		}

		@Override
		public boolean exists() {
			return getSerializationArtefact() != null;
		}

		@Override
		public String stringRepresentation() {
			return "ClassLoaderIODelegate[" + getSerializationArtefact() + "]";
		}

		@Override
		public boolean hasWritePermission() {
			return false;
		}

		@Override
		public FileWritingLock willWriteOnDisk() {
			return null;
		}

		@Override
		public void hasWrittenOnDisk(FileWritingLock lock) {
		}

		@Override
		public void notifyHasBeenWrittenOnDisk() {
		}

		@Override
		public String getDeletedProperty() {
			return null;
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
			// Not applicable
		}

		@Override
		public RepositoryFolder<?, ClassLoader> getRepositoryFolder(ResourceRepository<?, ClassLoader> resourceRepository,
				boolean createWhenNonExistent) throws IOException {
			return null;
		}

		@Override
		public ClassLoader retrieveClassLoader() {
			return getSerializationArtefact();
		}

		@Override
		public Resource getSerializationArtefactAsResource() {
			return null;
		}

		@Override
		public String getSerializationArtefactName() {
			if (getFlexoResource() != null && getFlexoResource().getResourceCenter() != null) {
				return ((FlexoResourceCenter) getFlexoResource().getResourceCenter()).retrieveName(getSerializationArtefact());
			}
			return null;
		}

		@Override
		public String getDisplayName() {
			return getSerializationArtefactName();
		}

	}

}
