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

package org.openflexo.components.doc.editorkit.widget;

import java.awt.BorderLayout;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.openflexo.components.doc.editorkit.FlexoDocumentFragmentEditor;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent;
import org.openflexo.swing.CustomPopup.ApplyCancelListener;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A widget presenting base editing features for FlexoDocumentation API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocFragmentEditorWidget<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends JPanel
		implements FIBCustomComponent<FlexoDocFragment<D, TA>>, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(FlexoDocFragmentEditorWidget.class.getPackage().getName());

	private FlexoDocumentFragmentEditor<D, TA> editor;
	protected FIBCustom component;
	protected FIBController controller;
	private FlexoDocFragment<D, TA> fragment;

	private final List<ApplyCancelListener> applyCancelListener = new ArrayList<>();

	private PropertyChangeSupport pcSupport;

	public FlexoDocFragmentEditorWidget(FlexoDocFragment<D, TA> fragment) {
		super(new BorderLayout());
		pcSupport = new PropertyChangeSupport(this);
		editor = new FlexoDocumentFragmentEditor<>(fragment);
		add(editor.getEditorPanel(), BorderLayout.CENTER);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public D getFlexoDocument() {
		return editor.getFlexoDocument();
	}

	public void setFlexoDocument(D document) {
		editor.setFlexoDocument(document);
	}

	@Override
	public FlexoDocFragment<D, TA> getEditedObject() {
		return fragment;
	}

	@Override
	public void setEditedObject(FlexoDocFragment<D, TA> fragment) {
		if ((fragment == null && this.fragment != null) || (fragment != null && !fragment.equals(this.fragment))) {
			this.fragment = fragment;
			System.out.println("OK, on sette le fragment " + fragment);
			if (fragment == null) {
				Thread.dumpStack();
			}
			editor.setFragment(fragment);

			// editor.getStyledDocument().getRootElement().setFilteredFragment(fragment);
			// editor.getStyledDocument().refresh();

			/*if (editorPanel != null) {
				remove(editorPanel);
			}
			
			editorView = createEditorView(document, getToolbarStates(), getObjectFactory());
			editorPanel = FxScriptUIHelper.getInstance().createEditorPanel(editorView);
			add(editorPanel, BorderLayout.CENTER);
			revalidate();
			repaint();*/
		}
	}

	private FlexoDocFragment<D, TA> revertValue;

	@Override
	public FlexoDocFragment<D, TA> getRevertValue() {
		return revertValue;
	}

	@Override
	public void setRevertValue(FlexoDocFragment<D, TA> revertValue) {
		this.revertValue = revertValue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<FlexoDocFragment<D, TA>> getRepresentedType() {
		return (Class) FlexoDocFragment.class;
	}

	public FlexoDocumentFragmentEditor<D, TA> getEditor() {
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
		return getEditor().getTextSelection();
	}

	private TextSelection<D, TA> highlightedTextSelection = null;

	public TextSelection<D, TA> getHighlightedTextSelection() {
		return highlightedTextSelection;
	}

	public void setHighlightedTextSelection(TextSelection<D, TA> highlightedTextSelection) {

		// System.out.println("Highligth " + highlightedTextSelection);

		if ((highlightedTextSelection == null && this.highlightedTextSelection != null)
				|| (highlightedTextSelection != null && !highlightedTextSelection.equals(this.highlightedTextSelection))) {

			getEditor().clearHighligths();

			// TextSelection<D, TA> oldValue = this.highlightedTextSelection;
			this.highlightedTextSelection = highlightedTextSelection;
			// getPropertyChangeSupport().firePropertyChange("highlightedTextSelection", oldValue, highlightedTextSelection);
			if (highlightedTextSelection != null) {
				getEditor().highlight(highlightedTextSelection);
			}
		}
	}

	public static class FlexoDocumentSelectionListener implements CaretListener {

		private final FlexoDocFragmentEditorWidget<?, ?> editor;

		public FlexoDocumentSelectionListener(FlexoDocFragmentEditorWidget<?, ?> editor) {
			this.editor = editor;
		}

		public FlexoDocFragmentEditorWidget<?, ?> getEditor() {
			return editor;
		}

		@Override
		public void caretUpdate(CaretEvent e) {
			// System.out.println("caretUpdate dot=" + e.getDot() + " mark=" + e.getMark());
		}
	}

}
