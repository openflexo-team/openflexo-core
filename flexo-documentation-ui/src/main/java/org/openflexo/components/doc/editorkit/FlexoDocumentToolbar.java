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

package org.openflexo.components.doc.editorkit;

import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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

	private FlexoDocumentEditor<?, ?> documentEditor;

	public FlexoDocumentToolbar(FlexoDocumentEditor<?, ?> documentEditor, FlexoController controller) {
		super(documentEditor, controller, FIB_FILE,
				documentEditor != null && documentEditor.getFlexoDocument() != null
						? documentEditor.getFlexoDocument().getTechnologyAdapter().getLocales()
						: (controller != null ? controller.getFlexoLocales() : FlexoLocalization.getMainLocalizer()));
		this.documentEditor = documentEditor;
		if (getFIBController() instanceof ToolbarFIBController) {
			((ToolbarFIBController) getFIBController()).setToolbar(this);
		}
		setOpaque(false);
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

		public void toogleBold() {

			int selectionStart = toolbar.documentEditor.getJEditorPane().getSelectionStart();
			int selectionEnd = toolbar.documentEditor.getJEditorPane().getSelectionEnd();
			String text = null;
			try {
				text = toolbar.documentEditor.getStyledDocument().getText(selectionStart, selectionEnd - selectionStart);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			System.out.println("OK on met en bold la selection " + selectionStart + "-" + selectionEnd + " soit: " + text);

			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setBold(sas, true);
			toolbar.documentEditor.getStyledDocument().setCharacterAttributes(selectionStart, selectionEnd - selectionStart, sas, false);
		}

		public void toogleItalic() {

			int selectionStart = toolbar.documentEditor.getJEditorPane().getSelectionStart();
			int selectionEnd = toolbar.documentEditor.getJEditorPane().getSelectionEnd();
			String text = null;
			try {
				text = toolbar.documentEditor.getStyledDocument().getText(selectionStart, selectionEnd - selectionStart);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			System.out.println("OK on met en italic la selection " + selectionStart + "-" + selectionEnd + " soit: " + text);

			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setItalic(sas, true);
			toolbar.documentEditor.getStyledDocument().setCharacterAttributes(selectionStart, selectionEnd - selectionStart, sas, false);
		}

		public void toogleUnderline() {

			int selectionStart = toolbar.documentEditor.getJEditorPane().getSelectionStart();
			int selectionEnd = toolbar.documentEditor.getJEditorPane().getSelectionEnd();
			String text = null;
			try {
				text = toolbar.documentEditor.getStyledDocument().getText(selectionStart, selectionEnd - selectionStart);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			System.out.println("OK on met en underline la selection " + selectionStart + "-" + selectionEnd + " soit: " + text);

			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setUnderline(sas, true);
			toolbar.documentEditor.getStyledDocument().setCharacterAttributes(selectionStart, selectionEnd - selectionStart, sas, false);
		}

		@Override
		public void singleClick(Object object) {
			super.singleClick(object);
			toolbar.singleClick(object);
		}

	}

}
