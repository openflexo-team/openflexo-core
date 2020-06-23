/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.fib;

import java.io.File;
import java.io.IOException;

import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FileUtils.CopyStrategy;

public class InstallDefaultPackagedResourceCenterDirectory {

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/InstallDefaultPackagedResourceCenterDirectory.fib");

	// private static final Resource ONTOLOGIES_DIR = ResourceLocator.locateResource("Ontologies");
	// private static final Resource VIEWPOINT_LIBRARY_DIR = ResourceLocator.locateResource("ViewPoints");

	private File resourceCenterDirectory;

	public File getResourceCenterDirectory() {
		if (resourceCenterDirectory == null) {
			File attempt = new File(FileUtils.getApplicationDataDirectory(), "FlexoResourceCenter");
			int id = 2;
			while (attempt.exists()) {
				attempt = new File(FileUtils.getApplicationDataDirectory(), "FlexoResourceCenter" + id);
				id++;
			}
			resourceCenterDirectory = attempt;
		}
		return resourceCenterDirectory;
	}

	public void setResourceCenterDirectory(File resourceCenterDirectory) {
		this.resourceCenterDirectory = resourceCenterDirectory;
	}

	public void installDefaultPackagedResourceCenter(FlexoResourceCenterService rcService) throws IOException {
		getResourceCenterDirectory().mkdirs();
		// copyViewPoints(VIEWPOINT_LIBRARY_DIR, getResourceCenterDirectory(), CopyStrategy.REPLACE_OLD_ONLY);
		// copyOntologies(ONTOLOGIES_DIR, getResourceCenterDirectory(), CopyStrategy.REPLACE_OLD_ONLY);
		DirectoryResourceCenter newRC = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(getResourceCenterDirectory(),
				rcService);
		rcService.addToResourceCenters(newRC);
		rcService.storeDirectoryResourceCenterLocations();
	}

	private static void copyViewPoints(Resource initialDirectory, File resourceCenterDirectory, CopyStrategy copyStrategy) {

		if (initialDirectory instanceof FileResourceImpl) {
			if (((FileResourceImpl) initialDirectory).getFile().getParentFile().equals(resourceCenterDirectory)) {
				return;
			}
		}
		/*
		try {
			FileUtils.copyResourceToDir(VIEWPOINT_LIBRARY_DIR, resourceCenterDirectory, copyStrategy);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	private static void copyOntologies(Resource initialDirectory, File resourceCenterDirectory, CopyStrategy copyStrategy) {

		if (initialDirectory instanceof FileResourceImpl) {
			if (((FileResourceImpl) initialDirectory).getFile().getParentFile().equals(resourceCenterDirectory)) {
				return;
			}
		}
		try {
			FileUtils.copyResourceToDir(initialDirectory, resourceCenterDirectory, copyStrategy);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
