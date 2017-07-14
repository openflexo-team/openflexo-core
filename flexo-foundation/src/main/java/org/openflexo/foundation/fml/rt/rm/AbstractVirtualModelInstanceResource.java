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

package org.openflexo.foundation.fml.rt.rm;

import java.util.List;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;

/**
 * Base API for a {@link FlexoResource} encoding a {@link AbstractVirtualModelInstance}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractVirtualModelInstanceResourceImpl.class)
public interface AbstractVirtualModelInstanceResource<VMI extends AbstractVirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
		extends PamelaResource<VMI, AbstractVirtualModelInstanceModelFactory<?>>, TechnologyAdapterResource<VMI, TA>,
		DirectoryContainerResource<VMI> {

	public static final String VIRTUAL_MODEL_RESOURCE = "virtualModelResource";

	@Getter(value = VIRTUAL_MODEL_RESOURCE, ignoreType = true)
	public VirtualModelResource getVirtualModelResource();

	@Setter(VIRTUAL_MODEL_RESOURCE)
	public void setVirtualModelResource(VirtualModelResource virtualModelResource);

	public VMI getVirtualModelInstance();

	@Getter(value = CONTAINER, inverse = CONTENTS)
	@Override
	public AbstractVirtualModelInstanceResource<?, ?> getContainer();

	/**
	 * Return the {@link VirtualModel} this {@link AbstractVirtualModelInstance} is conform to
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModel();

	/*public static final String VIEW_LIBRARY = "viewLibrary";
	
	@Getter(value = VIEW_LIBRARY, ignoreType = true)
	public ViewLibrary getViewLibrary();
	
	@Setter(VIEW_LIBRARY)
	public void setViewLibrary(ViewLibrary viewLibrary);*/

	/**
	 * Return the list of all {@link VirtualModelInstanceResource} defined in this {@link ViewResource}
	 * 
	 * @return
	 */
	public List<? extends AbstractVirtualModelInstanceResource<?, TA>> getVirtualModelInstanceResources();

	/**
	 * Return the list of all {@link VirtualModelInstanceResource} defined in this {@link ViewResource} conform to supplied
	 * {@link VirtualModel}
	 * 
	 * @return
	 */
	public List<? extends AbstractVirtualModelInstanceResource<?, TA>> getVirtualModelInstanceResources(VirtualModel virtualModel);

	/**
	 * Return class of {@link TechnologyAdapter} which handles this kind of resource
	 * 
	 * @return
	 */
	public Class<TA> getTechnologyAdapterClass();

	/**
	 * Return URI of {@link VirtualModel} which this instance is conform to
	 * 
	 * @return
	 */
	public String getVirtualModelURI();

	/**
	 * Sets URI of {@link VirtualModel} which this instance is conform to
	 * 
	 * @param virtualModelURI
	 */
	public void setVirtualModelURI(String virtualModelURI);

}
