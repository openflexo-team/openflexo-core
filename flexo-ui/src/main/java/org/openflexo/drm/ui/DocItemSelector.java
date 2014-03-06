/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.drm.ui;

import java.io.File;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.drm.DocItem;
import org.openflexo.toolbox.ResourceLocator;

/**
 * Widget allowing to select a DocItem while browsing the DocResourceCenter
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class DocItemSelector extends FIBFlexoObjectSelector<DocItem> {

	protected static final String EMPTY_STRING = "";

	public static final String  FIB_FILE_NAME = "Fib/DocItemSelector.fib";

	public DocItemSelector(DocItem editedObject) {
		super(editedObject);
	}

	@Override
	public Class<DocItem> getRepresentedType() {
		return DocItem.class;
	}

	@Override
	public String getFIBFileName() {
		return FIB_FILE_NAME;
	}

}
