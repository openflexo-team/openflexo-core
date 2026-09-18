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
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaXMLSerializableResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * The {@link FlexoResource} encoding a {@link FMLRTVirtualModelInstance}: a {@code Xxx.fml.rt/} directory holding one
 * {@code Xxx.fml.rt.xml} file, in which the instance is serialized in XML.
 * <p>
 * The resource knows the {@link CompilationUnitResource} of the VirtualModel the instance is conform to, by URI
 * ({@link #getVirtualModelURI()}). An instance may contain other instances, stored in its own directory
 * ({@link #getVirtualModelInstanceResources()}).
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLRTVirtualModelInstanceResourceImpl.class)
@XMLElement
public interface FMLRTVirtualModelInstanceResource
		extends PamelaXMLSerializableResource<FMLRTVirtualModelInstance, FMLRTVirtualModelInstanceModelFactory>,
		TechnologyAdapterResource<FMLRTVirtualModelInstance, FMLRTTechnologyAdapter>,
		DirectoryContainerResource<FMLRTVirtualModelInstance> {

	public static final String VIRTUAL_MODEL_RESOURCE = "virtualModelResource";

	@Getter(value = VIRTUAL_MODEL_RESOURCE, ignoreType = true)
	public CompilationUnitResource getVirtualModelResource();

	@Setter(VIRTUAL_MODEL_RESOURCE)
	public void setVirtualModelResource(CompilationUnitResource virtualModelResource);

	public FMLRTVirtualModelInstance getVirtualModelInstance();

	@Getter(value = CONTAINER, inverse = CONTENTS)
	@Override
	public FMLRTVirtualModelInstanceResource getContainer();

	/**
	 * Return the {@link VirtualModel} this {@link VirtualModelInstance} is conform to
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModel();

	/**
	 * Return class of {@link TechnologyAdapter} which handles this kind of resource
	 * 
	 * @return
	 */
	public Class<FMLRTTechnologyAdapter> getTechnologyAdapterClass();

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

	/**
	 * Return the list of all instance resources contained in this one
	 *
	 * @return the contained instance resources
	 */
	public List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources();

	/**
	 * Return the list of all instance resources contained in this one which are conform to supplied {@link VirtualModel}
	 *
	 * @param virtualModel
	 *            the VirtualModel the returned instances are conform to
	 * @return the contained instance resources of that VirtualModel
	 */
	public List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources(VirtualModel virtualModel);

}
