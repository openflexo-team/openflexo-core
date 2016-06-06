/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.widget;

import java.util.Collection;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a ViewPoint while browsing in ViewPoint library
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBViewPointSelector extends FIBFlexoObjectSelector<ViewPointResource> {

	static final Logger logger = Logger.getLogger(FIBViewPointSelector.class.getPackage().getName());

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/ViewPointSelector.fib");

	public FIBViewPointSelector(ViewPointResource editedObject) {
		super(editedObject);
	}

	@Override
	public void delete() {
		super.delete();
		viewPointLibrary = null;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<ViewPointResource> getRepresentedType() {
		return ViewPointResource.class;
	}

	@Override
	public String renderedString(ViewPointResource editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	private ViewPointLibrary viewPointLibrary;

	public ViewPointLibrary getViewPointLibrary() {
		return viewPointLibrary;
	}

	@CustomComponentParameter(name = "viewPointLibrary", type = CustomComponentParameter.Type.MANDATORY)
	public void setViewPointLibrary(ViewPointLibrary viewPointLibrary) {
		this.viewPointLibrary = viewPointLibrary;
	}

	/**
	 * This method must be implemented if we want to implement completion<br>
	 * Completion will be performed on that selectable values<br>
	 * Return all viewpoints of this library
	 */
	@Override
	protected Collection<ViewPointResource> getAllSelectableValues() {
		if (getViewPointLibrary() != null) {
			return getViewPointLibrary().getViewPoints();
		}
		return null;
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not insanciated in EDIT mode)
	/*public static void main(String[] args) {
	
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.INFO, null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		final ViewPointLibrary viewPointLibrary;
	
		final FlexoServiceManager serviceManager = new DefaultFlexoServiceManager() {
			@Override
			protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
				return null;
			}
	
			@Override
			protected FlexoEditor createApplicationEditor() {
				return null;
			}
		};
		viewPointLibrary = serviceManager.getViewPointLibrary();
	
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				FIBViewPointSelector selector = new FIBViewPointSelector(null);
				selector.setViewPointLibrary(viewPointLibrary);
				return makeArray(selector);
			}
	
			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}
	
			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController(component);
			}
		};
		editor.launch();
	}*/

}
