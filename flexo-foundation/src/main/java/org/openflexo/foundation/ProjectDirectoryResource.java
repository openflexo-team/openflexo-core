/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation;

import java.io.File;
import java.io.FileNotFoundException;

import org.openflexo.foundation.ProjectDirectoryResource.ProjectDirectoryResourceImpl;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.IProgress;

/**
 * A {@link ProjectDirectoryResource} is the resource denoting the {@link FlexoProject} directory on disk
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ProjectDirectoryResourceImpl.class)
public interface ProjectDirectoryResource extends FlexoProjectResource<FlexoProject>, FlexoResource<FlexoProject> {

	/**
	 * Default implementation for {@link ProjectDataResource}
	 * 
	 * 
	 * @author Sylvain
	 * 
	 */
	public static abstract class ProjectDirectoryResourceImpl extends FlexoResourceImpl<FlexoProject> implements ProjectDirectoryResource {

		public static ProjectDirectoryResource makeProjectDirectoryResource(FlexoProject project) {
			try {
				ModelFactory resourceFactory = new ModelFactory(
						ModelContextLibrary.getCompoundModelContext(FileIODelegate.class, ProjectDirectoryResource.class));
				ProjectDirectoryResource returned = resourceFactory.newInstance(ProjectDirectoryResource.class);
				returned.setProject(project);
				returned.initName(project.getProjectName());
				FileIODelegate fileIODelegate = resourceFactory.newInstance(FileIODelegate.class);
				returned.setIODelegate(fileIODelegate);
				fileIODelegate.setFile(project.getProjectDirectory());
				returned.setURI(project.getURI());
				returned.setResourceCenter(project);
				returned.setServiceManager(project.getServiceManager());
				if (!(fileIODelegate.getFile()).exists()) {
					fileIODelegate.getFile().mkdirs();
				}
				returned.setResourceData(project);
				// project.getViewLibrary();
				return returned;
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public FlexoProject getProject() {
			try {
				return getResourceData(null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void save(IProgress progress) throws SaveResourceException {
			if (!getFile().exists()) {
				getFile().mkdirs();
			}
		}

		@Override
		public Class<FlexoProject> getResourceDataClass() {
			return FlexoProject.class;
		}

		@Override
		public FlexoProject loadResourceData(IProgress progress)
				throws ResourceLoadingCancelledException, FileNotFoundException, FlexoException {
			// TODO Auto-generated method stub
			return null;
		}

		private File getFile() {
			return getFileFlexoIODelegate().getFile();
		}

		private FileIODelegate getFileFlexoIODelegate() {
			return (FileIODelegate) getIODelegate();
		}
	}

}
