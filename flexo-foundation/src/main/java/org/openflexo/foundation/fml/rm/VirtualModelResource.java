/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(VirtualModelResourceImpl.class)
@XMLElement
public interface VirtualModelResource extends AbstractVirtualModelResource<VirtualModel> {

	public static final String VIEW_POINT_LIBRARY = "viewPointLibrary";

	// public static final String DIRECTORY = "directory";

	/**
	 * Return virtual model stored by this resource<br>
	 * Load the resource data when unloaded
	 */
	@Override
	public VirtualModel getVirtualModel();

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Do not force the resource data to be loaded
	 */
	public VirtualModel getLoadedVirtualModel();

	/*@Getter(value = VIEW_POINT_LIBRARY, ignoreType = true)
	public ViewPointLibrary getViewPointLibrary();

	@Setter(VIEW_POINT_LIBRARY)
	public void setViewPointLibrary(ViewPointLibrary viewPointLibrary);*/

	/*@Getter(DIRECTORY)
	@XmlAttribute
	public File getDirectory();

	@Setter(DIRECTORY)
	public void setDirectory(File file);*/

	@Override
	public ViewPointResource getContainer();

}
