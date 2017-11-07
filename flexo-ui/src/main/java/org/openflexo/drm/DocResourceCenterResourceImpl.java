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

package org.openflexo.drm;

import java.util.logging.Logger;

import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.project.ProjectDataResource;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileIODelegate.FileIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.IProgress;

/**
 * Default implementation for {@link ProjectDataResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class DocResourceCenterResourceImpl extends PamelaResourceImpl<DocResourceCenter, DRMModelFactory>
		implements DocResourceCenterResource {

	static final Logger logger = Logger.getLogger(DocResourceCenterResourceImpl.class.getPackage().getName());

	public static DocResourceCenterResource retrieveDocResourceCenterResource(DocResourceManager docResourceManager,
			FlexoResourceCenter<?> resourceCenter) {
		try {
			System.out.println("retrieveDocResourceCenterResource for " + docResourceManager.getDRMFile());
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(FileIODelegate.class, DocResourceCenterResource.class));
			DocResourceCenterResourceImpl returned = (DocResourceCenterResourceImpl) factory.newInstance(DocResourceCenterResource.class);
			returned.setFactory(docResourceManager.getDRMModelFactory());
			returned.initName("DocResourceCenter");
			returned.setIODelegate(FileIODelegateImpl.makeFileFlexoIODelegate(docResourceManager.getDRMFile(), factory));
			returned.setURI("http://www.openflexo.org/DocResourceCenter");
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(docResourceManager.getServiceManager());
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public DocResourceCenter getDocResourceCenter() {
		return getLoadedResourceData();
	}

	@Override
	public Class<DocResourceCenter> getResourceDataClass() {
		return DocResourceCenter.class;
	}

	@Override
	public DocResourceCenter loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		// TODO
		/*DocResourceCenter returned = super.loadResourceData(progress);
		returned.clearIsModified();
		return returned;*/
		return null;
	}

}
