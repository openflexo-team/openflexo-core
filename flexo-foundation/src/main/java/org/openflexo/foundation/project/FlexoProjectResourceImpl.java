/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.project;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaXMLSerializableResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.task.FlexoTask;

/**
 * Default implementation for {@link FlexoProjectResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class FlexoProjectResourceImpl<I> extends PamelaXMLSerializableResourceImpl<FlexoProject<I>, FlexoProjectFactory>
		implements FlexoProjectResource<I> {

	static final Logger logger = Logger.getLogger(FlexoProjectResourceImpl.class.getPackage().getName());

	public static final String BASE_PROJECT_URI = "http://www.openflexo.org/projects";

	/**
	 * Instantiation of a delegate RC if project is stand-alone (not contained in another RC)
	 */
	private FlexoResourceCenter<I> delegateResourceCenter = null;

	@Override
	public FlexoResourceCenter<I> getDelegateResourceCenter() {
		return delegateResourceCenter;
	}

	@Override
	public void setDelegateResourceCenter(FlexoResourceCenter<I> delegateResourceCenter) {
		if ((delegateResourceCenter == null && this.delegateResourceCenter != null)
				|| (delegateResourceCenter != null && !delegateResourceCenter.equals(this.delegateResourceCenter))) {
			FlexoResourceCenter<?> oldValue = this.delegateResourceCenter;
			this.delegateResourceCenter = delegateResourceCenter;
			delegateResourceCenter.setDefaultBaseURI(getURI());
			delegateResourceCenter.setDelegatingProjectResource(this);
			getPropertyChangeSupport().firePropertyChange("delegateResourceCenter", oldValue, delegateResourceCenter);
		}
	}

	private boolean isClosing = false;
	private boolean isClosed = false;

	@Override
	public void setClosing() {
		isClosing = true;
	}

	@Override
	public void setClosed() {
		isClosed = true;
	}

	@Override
	public FlexoProject<I> getFlexoProject() {
		if (isClosing) {
			return getLoadedResourceData();
		}
		if (isClosed) {
			return null;
		}
		try {
			return getResourceData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<FlexoProject<I>> getResourceDataClass() {
		return (Class) FlexoProject.class;
	}

	@Override
	public FlexoProject<I> loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		FlexoProject<I> returned = super.loadResourceData();
		returned.setLastUniqueID(0);

		// We add the newly created project as a ResourceCenter
		FlexoTask addResourceCenterTask = getServiceManager().resourceCenterAdded(returned);

		// If resource center adding is executing in a task, we have to wait the task to be finished
		if (addResourceCenterTask != null) {
			getServiceManager().getTaskManager().waitTask(addResourceCenterTask);
		}

		return returned;
	}

	public static String buildProjectURI(String baseName) {
		Calendar rightNow = Calendar.getInstance();
		String returned = BASE_PROJECT_URI + "/" + rightNow.get(Calendar.YEAR) + "/" + (rightNow.get(Calendar.MONTH) + 1) + "/" + baseName
				+ "_" + System.currentTimeMillis() + FlexoProjectResourceFactory.PROJECT_SUFFIX;
		return returned;
	}

	private String defaultURI = null;

	@Override
	public String computeDefaultURI() {
		if (defaultURI == null) {
			defaultURI = buildProjectURI(getName());
		}
		return defaultURI;
	}

	@Override
	public void setURI(String anURI) {
		performSuperSetter(URI, anURI);
		if (getDelegateResourceCenter() != null) {
			getDelegateResourceCenter().setDefaultBaseURI(anURI);
		}
	}

	/**
	 * When true, indicates that this {@link FlexoProject} has no parent {@link FlexoResourceCenter}
	 * 
	 * @return
	 */
	@Override
	public boolean isStandAlone() {
		return (getResourceCenter() == getDelegateResourceCenter());
	}

}
