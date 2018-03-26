/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.components.widget;

import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a Resource while browsing in Information Space<br>
 * You may select a resource kind.
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBRepositoryFolderSelector extends FIBFlexoObjectSelector<RepositoryFolder> {

	static final Logger logger = Logger.getLogger(FIBRepositoryFolderSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/RepositoryFolderSelector.fib");

	private ResourceManager resourceManager;
	private TechnologyAdapter<?> technologyAdapter;
	private FlexoResourceCenter<?> resourceCenter;
	private Class<? extends ResourceData<?>> resourceDataClass;

	public FIBRepositoryFolderSelector(RepositoryFolder editedObject) {
		super(editedObject);
		// logger.info(">>>>>>>>>> Create FIBResourceSelector: " + Integer.toHexString(hashCode()));
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<RepositoryFolder> getRepresentedType() {
		return RepositoryFolder.class;
	}

	@Override
	public String renderedString(RepositoryFolder editedObject) {
		if (editedObject != null) {
			// System.out.println("path relative to repo: " + editedObject + " value=" + editedObject.getPathRelativeToRepository());
			return editedObject.getPathRelativeToRepository();
		}
		return "";
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	@CustomComponentParameter(name = "resourceManager", type = CustomComponentParameter.Type.MANDATORY)
	public void setResourceManager(ResourceManager resourceManager) {

		if (this.resourceManager != resourceManager) {
			ResourceManager oldValue = this.resourceManager;
			this.resourceManager = resourceManager;
			getPropertyChangeSupport().firePropertyChange("resourceManager", oldValue, resourceManager);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
			updateCustomPanel(getEditedObject());
		}
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		return technologyAdapter;
	}

	@CustomComponentParameter(name = "technologyAdapter", type = CustomComponentParameter.Type.OPTIONAL)
	public void setTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
		if (this.technologyAdapter != technologyAdapter) {
			TechnologyAdapter<?> oldValue = this.technologyAdapter;
			this.technologyAdapter = technologyAdapter;
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
			getPropertyChangeSupport().firePropertyChange("rootObject", null, getRootObject());
			updateCustomPanel(getEditedObject());
		}
	}

	public Object getRootObject() {
		if (getTechnologyAdapter() != null) {
			return getTechnologyAdapter();
		}
		else {
			return getResourceManager();
		}
	}

	public FlexoResourceCenter<?> getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter<?> resourceCenter) {
		if (resourceCenter == null || !resourceCenter.equals(this.resourceCenter)) {
			FlexoResourceCenter<?> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
		}
	}

	// IMPORTANT: used in ResourceSelector.fib
	public <RD extends ResourceData<RD>> Class<RD> getTypedResourceDataClass() {
		return (Class<RD>) resourceDataClass;
	}

	public Class<? extends ResourceData<?>> getResourceDataClass() {
		return resourceDataClass;
	}

	@CustomComponentParameter(name = "resourceDataClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setResourceDataClass(Class<? extends ResourceData<?>> resourceDataClass) {
		// System.out.println("set resource data class with " + resourceDataClass);
		this.resourceDataClass = resourceDataClass;
		fireEditedObjectChanged();
	}

	//

	@Override
	protected boolean isAcceptableValue(Object o) {
		if (super.isAcceptableValue(o)) {
			if (o instanceof RepositoryFolder) {
				if (getTechnologyAdapter() != null) {
					if (((RepositoryFolder<?, ?>) o).getResourceRepository() instanceof TechnologyAdapterResourceRepository) {
						TechnologyAdapterResourceRepository<?, ?, ?, ?> repo = (TechnologyAdapterResourceRepository<?, ?, ?, ?>) ((RepositoryFolder<?, ?>) o)
								.getResourceRepository();
						return repo.getTechnologyAdapter() == getTechnologyAdapter();
					}
					else {
						return false;
					}
				}
				if (getResourceDataClass() != null) {
					return getResourceDataClass()
							.isAssignableFrom((((RepositoryFolder<?, ?>) o).getResourceRepository()).getResourceDataClass());
				}
				return true;
			}
		}
		return false;
	}
}
