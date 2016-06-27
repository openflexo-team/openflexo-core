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
import java.io.InputStream;
import java.io.OutputStream;

import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.InJarResourceImpl;

@ModelEntity
@ImplementationClass(InJarFlexoIODelegate.InJarFlexoIODelegateImpl.class)
@XMLElement
public interface InJarFlexoIODelegate extends FlexoIOStreamDelegate<InJarResourceImpl> {

	@PropertyIdentifier(type = InJarResourceImpl.class)
	public static final String IN_JAR_RESOURCE = "inJarResource";

	@Getter(value = IN_JAR_RESOURCE, ignoreType = true)
	public InJarResourceImpl getInJarResource();

	@Setter(IN_JAR_RESOURCE)
	public void setInJarResource(InJarResourceImpl inJarResource);

	public abstract class InJarFlexoIODelegateImpl extends FlexoIOStreamDelegateImpl<InJarResourceImpl>implements InJarFlexoIODelegate {

		public static InJarFlexoIODelegate makeInJarFlexoIODelegate(InJarResourceImpl inJarResource, ModelFactory factory) {
			InJarFlexoIODelegate delegate = factory.newInstance(InJarFlexoIODelegate.class);
			delegate.setInJarResource(inJarResource);
			return delegate;
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
			// TODO Auto-generated method stub
		}

		@Override
		public OutputStream getOutputStream() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setOutputStream(OutputStream outsputStream) {
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public String getParentPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rename() throws CannotRenameException {
			throw new CannotRenameException(getFlexoResource());
		}

		@Override
		public void save(FlexoResource<?> resource) throws NotImplementedException {
			throw new NotImplementedException("In-jar resources cannot be saved");
		}

		@Override
		public RepositoryFolder<?> getRepositoryFolder(ResourceRepository<?> resourceRepository, boolean createWhenNonExistent)
				throws IOException {
			return resourceRepository.getRootFolder();
		}
	}

}
