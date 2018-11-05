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

package org.openflexo.foundation.resource;

import javax.xml.bind.annotation.XmlAttribute;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.rm.Resource;

/**
 * A {@link DirectoryContainerResource} is a resource bound to a directory on file system<br>
 * This directory contains a main file (the file which is accessed through {@link FlexoFileResource} api) and some other resources (files)
 * contained in this directory.<br>
 * This resource also manage a RelativeFilePathConverter that allow to access resources inside this directory. Such resource is easyly
 * transportable (any resources contained in this directory are always retrievable whereever the parent directory is).
 * 
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity(isAbstract = true)
public interface DirectoryContainerResource<RD extends ResourceData<RD>> extends FlexoResource<RD> {

	public static final String DIRECTORY = "directory";
	public static final String RELATIVE_RESOURCE_PATH_CONVERTER = "relativePathResourceConverter";

	/**
	 * Return the directory (parent file)
	 * 
	 * @return
	 */
	@Deprecated
	@Getter(value = DIRECTORY, ignoreType = true)
	@XmlAttribute
	public Resource getDirectory();

	/**
	 * Sets the directory (parent file)
	 */
	@Setter(DIRECTORY)
	@Deprecated
	public void setDirectory(Resource resource);

	/**
	 * Return the relative file path converter associated with the directory location
	 * 
	 * @return
	 */
	@Getter(value = RELATIVE_RESOURCE_PATH_CONVERTER, ignoreType = true)
	public RelativePathResourceConverter getRelativePathResourceConverter();

	/**
	 * Sets the relative file path converter
	 */
	@Setter(RELATIVE_RESOURCE_PATH_CONVERTER)
	public void setRelativePathResourceConverter(RelativePathResourceConverter converter);

}
