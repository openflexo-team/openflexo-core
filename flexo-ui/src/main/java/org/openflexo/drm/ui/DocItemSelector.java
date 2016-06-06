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

package org.openflexo.drm.ui;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.drm.DocItem;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a DocItem while browsing the DocResourceCenter
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class DocItemSelector extends FIBFlexoObjectSelector<DocItem> {

	protected static final String EMPTY_STRING = "";

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/DocItemSelector.fib");

	public DocItemSelector(DocItem editedObject) {
		super(editedObject);
	}

	@Override
	public Class<DocItem> getRepresentedType() {
		return DocItem.class;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

}
