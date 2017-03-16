/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
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

package org.openflexo.components.doc.editorkit;

import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.SelectionSynchronizedFIBView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Widget allowing to edit/view an {@link OWLOntology}.<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FlexoDocumentToolbar extends SelectionSynchronizedFIBView {
	static final Logger logger = Logger.getLogger(FlexoDocumentToolbar.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FlexoDocumentToolbar.fib");

	public FlexoDocumentToolbar(FlexoDocumentEditor<?, ?> documentEditor, FlexoController controller) {
		super(documentEditor, controller, FIB_FILE,
				documentEditor != null && documentEditor.getFlexoDocument() != null
						? documentEditor.getFlexoDocument().getTechnologyAdapter().getLocales()
						: (controller != null ? controller.getFlexoLocales() : FlexoLocalization.getMainLocalizer()));
		if (getFIBController() instanceof ToolbarFIBController) {
			((ToolbarFIBController) getFIBController()).setToolbar(this);
		}
	}

	@Override
	public FlexoDocumentEditor<?, ?> getDataObject() {
		return (FlexoDocumentEditor<?, ?>) super.getDataObject();
	}

	private FlexoDocObject<?, ?> selectedObject;

	public FlexoDocObject<?, ?> getSelectedDocObject() {
		return selectedObject;
	}

	public void setSelectedDocObject(FlexoDocObject<?, ?> selected) {
		selectedObject = selected;
	}

	public void singleClick(Object object) {
		System.out.println("Hop, singleClick with " + object);
	}

	public static class ToolbarFIBController extends FlexoFIBController {
		private FlexoDocumentToolbar toolbar;

		public ToolbarFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		private void setToolbar(FlexoDocumentToolbar toolbar) {
			this.toolbar = toolbar;
		}

		public FlexoDocObject<?, ?> getSelectedDocObject() {
			if (toolbar != null) {
				return toolbar.getSelectedDocObject();
			}
			return null;
		}

		public void setSelectedDocObject(FlexoDocObject<?, ?> selected) {
			if (toolbar != null) {
				toolbar.setSelectedDocObject(selected);
			}
		}

		@Override
		public void singleClick(Object object) {
			super.singleClick(object);
			toolbar.singleClick(object);
		}

	}

}
