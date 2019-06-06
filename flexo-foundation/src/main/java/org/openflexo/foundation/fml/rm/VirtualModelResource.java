/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.rm;

import java.util.List;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * The resource storing a {@link VirtualModel}
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(VirtualModelResourceImpl.class)
public interface VirtualModelResource extends PamelaResource<VirtualModel, FMLModelFactory>, DirectoryContainerResource<VirtualModel>,
		TechnologyAdapterResource<VirtualModel, FMLTechnologyAdapter> {

	public static final String VIRTUAL_MODEL_LIBRARY = "virtualModelLibrary";
	public static final String CONTAINED_VMI = "containedVMI";

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Force the resource data to be loaded when unloaded
	 */
	public VirtualModel getVirtualModel();

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Do not force the resource data to be loaded
	 */
	public VirtualModel getLoadedVirtualModel();

	@Override
	public VirtualModelResource getContainer();

	public List<VirtualModelResource> getContainedVirtualModelResources();

	public VirtualModelResource getVirtualModelResource(String virtualModelNameOrURI);

	@Getter(value = VIRTUAL_MODEL_LIBRARY, ignoreType = true)
	public VirtualModelLibrary getVirtualModelLibrary();

	@Setter(VIRTUAL_MODEL_LIBRARY)
	public void setVirtualModelLibrary(VirtualModelLibrary virtualModelLibrary);

	/**
	 * Return {@link ModelSlot} classes used in this {@link VirtualModel} resource<br>
	 * Note that this information is extracted from metadata or from reading XML file before effective parsing<br>
	 * This information is used to determine which technology adapters have to be activated before {@link VirtualModel} is loaded
	 * 
	 * @return
	 */
	public List<Class<? extends ModelSlot<?>>> getUsedModelSlots();

	/**
	 * Return a string representation of used model slots classes as a comma-separated string with class names
	 * 
	 * @return
	 */
	public String getUsedModelSlotsAsString();

	/**
	 * Rebuild a new {@link FMLModelFactory} using supplied use declarations, and set this new factory as model factory to use for this
	 * resource<br>
	 * This call is required for example when a new technology is required for a {@link VirtualModel}
	 * 
	 * @param useDeclarations
	 */
	public void updateFMLModelFactory(List<Class<? extends ModelSlot<?>>> useDeclarations);

	/**
	 * Returns a list of VMI resources located by this resource<br>
	 * Those resources are generally used to represent container resource (the VirtualModel)
	 * 
	 * @return the list of VMI resources.
	 */
	@Getter(value = CONTAINED_VMI, cardinality = Cardinality.LIST)
	public List<FMLRTVirtualModelInstanceResource> getContainedVMI();

	/**
	 * Adds a VMI resource<br>
	 * Those resources are generally used to represent container resource (the VirtualModel)
	 * 
	 * @param resource
	 *            the resource to add
	 */
	@Adder(CONTAINED_VMI)
	public void addToContainedVMI(FMLRTVirtualModelInstanceResource resource);

	/**
	 * Removes a VMI resource<br>
	 * Those resources are generally used to represent container resource (the VirtualModel)
	 * 
	 * @param resource
	 *            the resource to remove
	 */
	@Remover(CONTAINED_VMI)
	public void removeFromContainedVMI(FMLRTVirtualModelInstanceResource resource);

}
