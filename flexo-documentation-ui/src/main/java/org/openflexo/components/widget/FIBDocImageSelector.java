/**
 * 
 * Copyright (c) 2014-2017, Openflexo
 * 
 * This file is part of Flexo-Documentation-UI, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Please not that some parts of that component are freely inspired from
 * Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.gina.view.widget.FIBCustomWidget;
import org.openflexo.rm.Resource;

/**
 * Widget allowing to select an {@link FlexoDrawingRun} inside a {@link FlexoDocument}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBDocImageSelector<T extends FlexoDrawingRun<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FIBFlexoObjectSelector<T> {
	static final Logger logger = Logger.getLogger(FIBDocImageSelector.class.getPackage().getName());

	private D document;

	public FIBDocImageSelector(T editedObject) {
		super(editedObject);
	}

	@Override
	public abstract Resource getFIBResource();

	@Override
	public abstract Class<T> getRepresentedType();

	public D getDocument() {
		return document;
	}

	public void setDocument(D document) {
		if ((document == null && this.document != null) || (document != null && !document.equals(this.document))) {
			D oldValue = this.document;
			this.document = document;
			getPropertyChangeSupport().firePropertyChange("document", oldValue, document);
		}
	}

	@Override
	protected ImageSelectorDetailsPanel makeCustomPanel(T editedObject) {
		return new ImageSelectorDetailsPanel(editedObject);
	}

	@Override
	protected ImageSelectorFIBController makeCustomFIBController(FIBComponent fibComponent) {
		return new ImageSelectorFIBController(fibComponent, this);
	}

	@Override
	protected ImageSelectorFIBController getController() {
		return (ImageSelectorFIBController) super.getController();
	}

	@Override
	public ImageSelectorDetailsPanel getCustomPanel() {
		return (ImageSelectorDetailsPanel) super.getCustomPanel();
	}

	@Override
	public void setSelectedObject(Object selectedObject) {
		// TODO Auto-generated method stub
		super.setSelectedObject(selectedObject);
		ImageSelectorDetailsPanel customPanel = (ImageSelectorDetailsPanel) getCustomPanel(false);
		if (customPanel != null) {
			selectImageInDocumentEditor((T) selectedObject, customPanel.getDocEditorWidget());
		}
	}

	public static class ImageSelectorFIBController extends SelectorFIBController {
		public ImageSelectorFIBController(final FIBComponent component, final FIBDocImageSelector selector) {
			super(component, selector);
		}

		public boolean containsDrawingRun(FlexoDocParagraph<?, ?> paragraph) {
			if (paragraph.getChildrenElements().size() > 0) {
				return true;
			}
			for (FlexoDocRun<?, ?> run : paragraph.getRuns()) {
				if (run instanceof FlexoDrawingRun) {
					return true;
				}
			}
			return false;
		}

		public boolean isSingleDrawingRun(FlexoDocParagraph<?, ?> paragraph) {
			return (paragraph.getDrawingRuns().size() == 1);
		}

	}

	public class ImageSelectorDetailsPanel extends SelectorDetailsPanel {

		private FIBCustomWidget<?, ?, ?> docEditorWidget = null;

		protected ImageSelectorDetailsPanel(T anObject) {
			super(anObject);
		}

		public FIBCustomWidget<?, ?, ?> getDocEditorWidget() {
			if (docEditorWidget == null) {
				docEditorWidget = retrieveDocEditorWidget();
			}
			return docEditorWidget;
		}

		private FIBCustomWidget<?, ?, ?> retrieveDocEditorWidget() {
			List<FIBComponent> listComponent = getFIBComponent().getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBCustom) {
					return (FIBCustomWidget) getController().viewForComponent(c);
				}
			}
			return null;
		}

		@Override
		public ImageSelectorFIBController getController() {
			return (ImageSelectorFIBController) super.getController();
		}

		@Override
		protected void selectValue(T value) {

			/*FIBBrowserWidget browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				// Force reselect value because tree may have been recomputed
				browserWidget.setSelected(value);
			}*/

			setSelectedValue(value);

			JFIBBrowserWidget browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				if (value == null) {
					browserWidget.clearSelection();
				}
				else {
					browserWidget.setSelected(value);
				}
			}

			selectImageInDocumentEditor(value, getDocEditorWidget());
		}

		@Override
		public void update() {
			// TODO Auto-generated method stub
			super.update();
		}

	}

	protected void selectImageInDocumentEditor(T drawingRun, FIBCustomWidget<?, ?, ?> documentEditorWidget) {

	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject == null) {
			return "";
		}
		return editedObject.getImageName();
	}
}
