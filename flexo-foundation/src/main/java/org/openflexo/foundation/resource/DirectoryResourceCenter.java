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
import java.util.Collection;
import java.util.logging.Logger;

import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

/**
 * Default implementation for a {@link FlexoResourceCenter} bound to a directory on the file system
 * 
 * @author sylvain
 * 
 */
public class DirectoryResourceCenter extends FileSystemBasedResourceCenter {

	protected static final Logger logger = Logger.getLogger(DirectoryResourceCenter.class.getPackage().getName());

	public DirectoryResourceCenter(File resourceCenterDirectory, FlexoResourceCenterService rcService) {
		super(resourceCenterDirectory, rcService);
	}

	public DirectoryResourceCenter(File resourceCenterDirectory, String defaultBaseURI, FlexoResourceCenterService rcService) {
		super(resourceCenterDirectory, rcService);
		setDefaultBaseURI(defaultBaseURI);
	}

	public static DirectoryResourceCenter instanciateNewDirectoryResourceCenter(File resourceCenterDirectory,
			FlexoResourceCenterService rcService) {
		logger.info("Instanciate ResourceCenter from " + resourceCenterDirectory.getAbsolutePath());
		DirectoryResourceCenter directoryResourceCenter = new DirectoryResourceCenter(resourceCenterDirectory, rcService);
		try {
			directoryResourceCenter.update();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return directoryResourceCenter;
	}

	@Override
	public Collection<FlexoResource<?>> getAllResources(IProgress progress) {
		return getAllResources();
	}

	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void update() throws IOException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public String getDisplayableName() {
		return getDefaultBaseURI();
	}
}
