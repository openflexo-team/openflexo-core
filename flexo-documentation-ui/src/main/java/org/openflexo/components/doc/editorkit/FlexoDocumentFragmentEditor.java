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

import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * A Wysiwyg editor for {@link FlexoDocFragment} API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocumentFragmentEditor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoDocumentEditor<D, TA> {

	private static final Logger logger = Logger.getLogger(FlexoDocumentFragmentEditor.class.getPackage().getName());

	private FlexoDocFragment<D, TA> fragment;

	public FlexoDocumentFragmentEditor(FlexoDocFragment<D, TA> fragment) {
		super(fragment != null ? fragment.getFlexoDocument() : null);
		setFragment(fragment);
	}

	@Override
	public D getFlexoDocument() {
		if (fragment != null) {
			return fragment.getFlexoDocument();
		}
		return super.getFlexoDocument();
	}

	// Override setFlexoDocument by preventing reading the whole document
	// > will be done in setFragment()
	@Override
	public void setFlexoDocument(D flexoDocument) {
		if (flexoDocument != this.flexoDocument) {
			FlexoDocument<D, TA> oldValue = this.flexoDocument;
			this.flexoDocument = flexoDocument;
			getPropertyChangeSupport().firePropertyChange("flexoDocument", oldValue, flexoDocument);
		}
	}

	public FlexoDocFragment<D, TA> getFragment() {
		return fragment;
	}

	public void setFragment(FlexoDocFragment<D, TA> fragment) {
		if ((fragment == null && this.fragment != null) || (fragment != null && !fragment.equals(this.fragment))) {
			FlexoDocFragment<D, TA> oldValue = this.fragment;
			this.fragment = fragment;
			if (fragment != null) {
				FlexoDocFragmentEditorFactory<D, TA> reader;
				try {
					reader = new FlexoDocFragmentEditorFactory<>(fragment);
					jEditorPane.setDocument(reader.getDocument());
					jEditorPane.getDocument().addDocumentListener(this);
					jEditorPane.addCaretListener(this);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			getPropertyChangeSupport().firePropertyChange("fragment", oldValue, fragment);
		}
	}
}
