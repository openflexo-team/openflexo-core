/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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
import java.util.logging.Logger;

import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FlexoVersion;

/**
 * Default implementation for a {@link FlexoResourceCenter} bound to a directory on the file system
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DirectoryResourceCenter.DirectoryResourceCenterImpl.class)
public interface DirectoryResourceCenter extends FileSystemBasedResourceCenter {

	public static DirectoryResourceCenter instanciateNewDirectoryResourceCenter(File resourceCenterDirectory,
			FlexoResourceCenterService rcService) throws IOException {
		DirectoryResourceCenterImpl.logger.info("Instanciate ResourceCenter from " + resourceCenterDirectory.getAbsolutePath());
		PamelaModelFactory factory;
		try {
			factory = new PamelaModelFactory(DirectoryResourceCenter.class);
			DirectoryResourceCenter directoryResourceCenter = factory.newInstance(DirectoryResourceCenter.class);
			directoryResourceCenter.setBaseArtefact(resourceCenterDirectory);
			directoryResourceCenter.setFlexoResourceCenterService(rcService);
			directoryResourceCenter.update();
			if (rcService.isDirectoryWatchingEnabled()) {
				directoryResourceCenter.startDirectoryWatching();
			}
			return directoryResourceCenter;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static abstract class DirectoryResourceCenterImpl extends FileSystemBasedResourceCenterImpl implements DirectoryResourceCenter {

		protected static final Logger logger = Logger.getLogger(DirectoryResourceCenter.class.getPackage().getName());

		/*public DirectoryResourceCenterImpl(File resourceCenterDirectory, FlexoResourceCenterService rcService) {
			super(resourceCenterDirectory, rcService);
		}
		
		public DirectoryResourceCenterImpl(File resourceCenterDirectory, String defaultBaseURI, FlexoResourceCenterService rcService) {
			super(resourceCenterDirectory, rcService);
			setDefaultBaseURI(defaultBaseURI);
		}*/

		/*public static DirectoryResourceCenter instanciateNewDirectoryResourceCenter(File resourceCenterDirectory,
				FlexoResourceCenterService rcService) {
			logger.info("Instanciate ResourceCenter from " + resourceCenterDirectory.getAbsolutePath());
			DirectoryResourceCenter directoryResourceCenter = new DirectoryResourceCenter(resourceCenterDirectory, rcService);
			try {
				directoryResourceCenter.update();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return directoryResourceCenter;
		}*/

		@Override
		public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion) throws Exception {
		}

		@Override
		public void update() throws IOException {
		}

		@Override
		public boolean isDeleted() {
			return false;
		}

		private DirectoryResourceCenterEntry entry;

		@Override
		public ResourceCenterEntry<?> getResourceCenterEntry() {
			if (entry == null) {
				try {
					PamelaModelFactory factory = new PamelaModelFactory(DirectoryResourceCenterEntry.class);
					entry = factory.newInstance(DirectoryResourceCenterEntry.class);
					entry.setDirectory(getDirectory());
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}
			}
			return entry;
		}

	}

	@ModelEntity
	@ImplementationClass(DirectoryResourceCenterEntry.DirectoryResourceCenterEntryImpl.class)
	@XMLElement(xmlTag = "FSBasedResourceCenterEntry")
	public static interface DirectoryResourceCenterEntry extends FSBasedResourceCenterEntry<DirectoryResourceCenter> {
		@Implementation
		public static abstract class DirectoryResourceCenterEntryImpl implements DirectoryResourceCenterEntry {

			@Override
			public DirectoryResourceCenter makeResourceCenter(FlexoResourceCenterService rcService) {
				try {
					DirectoryResourceCenterImpl returned = (DirectoryResourceCenterImpl) DirectoryResourceCenter
							.instanciateNewDirectoryResourceCenter(getDirectory(), rcService);
					returned.entry = this;
					return returned;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

		}

	}

}
