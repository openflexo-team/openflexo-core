/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent;
import org.openflexo.swing.CustomPopup.ApplyCancelListener;

/**
 * A widget presenting base editing features for FlexoDocumentation API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocumentEditorWidget<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends JPanel
		implements FIBCustomComponent<D> {

	private static final Logger logger = Logger.getLogger(FlexoDocumentEditorWidget.class.getPackage().getName());

	private FlexoDocumentEditor<D, TA> editor;
	protected FIBCustom component;
	protected FIBController controller;

	private boolean showToolbar = true;

	private final List<ApplyCancelListener> applyCancelListener = new ArrayList<>();

	public FlexoDocumentEditorWidget(D document) {
		super(new BorderLayout());
		editor = new FlexoDocumentEditor<>(document);
		editor.setShowToolbar(getShowToolbar());
		add(editor.getEditorPanel(), BorderLayout.CENTER);
	}

	@Override
	public D getEditedObject() {
		return editor.getFlexoDocument();
	}

	@Override
	public void setEditedObject(D document) {
		editor.setFlexoDocument(document);
	}

	@Override
	public D getRevertValue() {
		return editor.getFlexoDocument();
	}

	@Override
	public void setRevertValue(D object) {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<D> getRepresentedType() {
		return (Class) FlexoDocument.class;
	}

	public FlexoDocumentEditor<D, TA> getEditor() {
		return editor;
	}

	public JEditorPane getJEditorPane() {
		return editor.getJEditorPane();
	}

	@Override
	public void addApplyCancelListener(ApplyCancelListener l) {
		applyCancelListener.add(l);
	}

	@Override
	public void removeApplyCancelListener(ApplyCancelListener l) {
		applyCancelListener.remove(l);
	}

	public void apply() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("apply()");
		}
		notifyApplyPerformed();
	}

	public void notifyApplyPerformed() {
		for (ApplyCancelListener l : applyCancelListener) {
			l.fireApplyPerformed();
		}
	}

	public void cancel() {
		for (ApplyCancelListener l : applyCancelListener) {
			l.fireCancelPerformed();
		}
	}

	@Override
	public void init(FIBCustom component, FIBController controller) {
		this.component = component;
		this.controller = controller;
	}

	@Override
	public void delete() {
	}

	public TextSelection<D, TA> getTextSelection() {
		return editor.getTextSelection();
	}

	public boolean getShowToolbar() {
		return showToolbar;
	}

	@CustomComponentParameter(name = "showToolbar", type = CustomComponentParameter.Type.OPTIONAL)
	public void setShowToolbar(boolean showToolbar) {
		if (showToolbar != getShowToolbar()) {
			this.showToolbar = showToolbar;
			// getPropertyChangeSupport().firePropertyChange("showHeader", !showHeader, showHeader);
			getEditor().setShowToolbar(showToolbar);
		}
	}

	public static class FlexoDocumentSelectionListener implements CaretListener {

		private final FlexoDocumentEditorWidget<?, ?> editor;

		public FlexoDocumentSelectionListener(FlexoDocumentEditorWidget<?, ?> editor) {
			this.editor = editor;
		}

		public FlexoDocumentEditorWidget<?, ?> getEditor() {
			return editor;
		}

		@Override
		public void caretUpdate(CaretEvent e) {
			// System.out.println("caretUpdate dot=" + e.getDot() + " mark=" + e.getMark());
		}
	}

}
