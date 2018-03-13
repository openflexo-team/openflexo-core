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

package org.openflexo.components.widget;

import org.openflexo.ApplicationContext;
import org.openflexo.connie.type.CustomType;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Default base implementation for a {@link CustomTypeEditor}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultCustomTypeEditorImpl<T extends CustomType> extends PropertyChangedSupportDefaultImplementation
		implements FlexoCustomTypeEditor<T> {

	private FlexoServiceManager serviceManager;

	public DefaultCustomTypeEditorImpl(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	private Resource fibComponentResource = null;

	@Override
	public Resource getFIBComponentResource() {
		if (fibComponentResource == null) {
			Class<?> current = getClass();
			while (fibComponentResource == null && current != null) {
				if (current.getAnnotation(FIBPanel.class) != null) {
					String fibPanelName = current.getAnnotation(FIBPanel.class).value();
					fibComponentResource = ResourceLocator.locateResource(fibPanelName);
				}
				current = current.getSuperclass();
			}
		}
		return fibComponentResource;
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	@Override
	public FlexoFIBController makeFIBController() {
		// System.out.println("makeFIBController() in DefaultCustomTypeEditorImpl");
		FIBComponent component = ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(getFIBComponentResource());
		FlexoController controller = ((ApplicationContext) serviceManager).getModuleLoader().getActiveModule().getController();

		return new SelectorFIBController(component, this, controller);
	}

	public static class SelectorFIBController extends FlexoFIBController {
		// Unused private DefaultCustomTypeEditorImpl<?> editor;

		public SelectorFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public SelectorFIBController(FIBComponent component, DefaultCustomTypeEditorImpl<?> editor, FlexoController controller) {
			super(component, SwingViewFactory.INSTANCE);
			// Unused this.editor = editor;
			setFlexoController(controller);
		}

		public void selectedObjectChanged() {
			// System.out.println("L'objet selectionné change");
		}

		public void apply() {
			System.out.println("apply");
		}

	}
}
