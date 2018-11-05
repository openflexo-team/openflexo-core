/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import java.util.List;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.factory.AccessibleProxyObject;

/**
 * This service implements access policy to resource centers in the context of a {@link FlexoServiceManager} (which, in interactive mode, is
 * an ApplicationContext)
 * 
 * One {@link UserResourceCenter} is declared to be the user resource center
 * 
 * @author sylvain
 * 
 */
@ModelEntity
public interface FlexoResourceCenterService extends FlexoService, AccessibleProxyObject {
	public static final String RESOURCE_CENTERS = "resourceCenters";

	public FlexoResourceCenter<?> getFlexoResourceCenter(String baseURI);

	@Getter(value = RESOURCE_CENTERS, cardinality = Cardinality.LIST, ignoreType = true)
	public List<FlexoResourceCenter<?>> getResourceCenters();

	@Setter(RESOURCE_CENTERS)
	public void setResourceCenters(List<FlexoResourceCenter<?>> resourceCenters);

	@Adder(RESOURCE_CENTERS)
	public void addToResourceCenters(FlexoResourceCenter<?> resourceCenter);

	@Remover(RESOURCE_CENTERS)
	public void removeFromResourceCenters(FlexoResourceCenter<?> resourceCenter);

	/**
	 * Save all locations for registered resource centers on disk
	 */
	public void storeDirectoryResourceCenterLocations();

	public boolean isDevMode();

	public void setDevMode(boolean devMode);

	public <I> FlexoResourceCenter<I> getResourceCenterContaining(I serializationArtefact);

	/**
	 * Return the {@link FlexoProjectResourceFactory}
	 * 
	 * @return
	 */
	public FlexoProjectResourceFactory<?> getFlexoProjectResourceFactory();
}
