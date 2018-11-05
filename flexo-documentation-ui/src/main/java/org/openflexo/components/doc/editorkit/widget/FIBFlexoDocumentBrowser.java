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

package org.openflexo.components.doc.editorkit.widget;

import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.prefs.ApplicationFIBLibraryService;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBBrowserView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Browser allowing to browse a {@link FlexoDocument}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoDocumentBrowser extends FIBBrowserView<FlexoDocument<?, ?>> {
	static final Logger logger = Logger.getLogger(FIBFlexoDocumentBrowser.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBFlexoDocumentBrowser.fib");

	public FIBFlexoDocumentBrowser(FlexoDocument<?, ?> document, FlexoController controller) {
		super(document, controller, FIB_FILE, controller != null ? controller.getFlexoLocales() : null);
		if (getFIBController() instanceof FlexoDocumentBrowserFIBController) {
			((FlexoDocumentBrowserFIBController) getFIBController()).setBrowser(this);
		}
	}

	public FIBFlexoDocumentBrowser(FlexoDocument<?, ?> document, ApplicationFIBLibraryService appFIBLibraryService) {
		super(document, appFIBLibraryService, FIB_FILE, null, true);
		if (getFIBController() instanceof FlexoDocumentBrowserFIBController) {
			((FlexoDocumentBrowserFIBController) getFIBController()).setBrowser(this);
		}
	}

	private FlexoDocObject<?, ?> selectedElement;
	private boolean showRuns = false;

	public FlexoDocObject<?, ?> getSelectedDocumentElement() {
		return selectedElement;
	}

	public void setSelectedDocumentElement(FlexoDocObject<?, ?> selected) {
		selectedElement = selected;
	}

	public void singleClick(Object object) {
	}

	public boolean showRuns() {
		return showRuns;
	}

	public void setShowRuns(boolean showRuns) {
		if (showRuns != this.showRuns) {
			this.showRuns = showRuns;
			getPropertyChangeSupport().firePropertyChange("showRuns", !showRuns, showRuns);
		}
	}

	public static class FlexoDocumentBrowserFIBController extends FlexoFIBController {
		private FIBFlexoDocumentBrowser browser;

		public FlexoDocumentBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		private void setBrowser(FIBFlexoDocumentBrowser browser) {
			this.browser = browser;
		}

		public FlexoDocObject<?, ?> getSelectedDocumentElement() {
			if (browser != null) {
				return browser.getSelectedDocumentElement();
			}
			return null;
		}

		public void setSelectedDocumentElement(FlexoDocObject<?, ?> selected) {
			if (browser != null) {
				browser.setSelectedDocumentElement(selected);
			}
		}

		@Override
		public void singleClick(Object object) {
			super.singleClick(object);
			browser.singleClick(object);
		}

	}

}
