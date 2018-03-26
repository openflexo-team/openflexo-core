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

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.element.DocFragmentElement;
import org.openflexo.components.doc.editorkit.element.ParagraphElement;
import org.openflexo.components.doc.editorkit.element.RunElement;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Internal representation of a fragment of a {@link FlexoDocument}
 * 
 * @author Sylvain Guerin
 * @see DefaultStyledDocument
 */
@SuppressWarnings("serial")
public class FlexoFragmentStyledDocument<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoStyledDocument<D, TA> {

	static final Logger logger = Logger.getLogger(FlexoFragmentStyledDocument.class.getPackage().getName());

	private FlexoDocFragment<D, TA> fragment;

	public FlexoFragmentStyledDocument(FlexoDocFragment<D, TA> fragment) {
		super(fragment != null ? fragment.getFlexoDocument() : null);
		this.fragment = fragment;
	}

	public FlexoDocFragment<D, TA> getFragment() {
		return fragment;
	}

	public void setFragment(FlexoDocFragment<D, TA> fragment) {
		if ((fragment == null && this.fragment != null) || (fragment != null && !fragment.equals(this.fragment))) {
			// FlexoDocFragment<D, TA> oldValue = this.fragment;
			this.fragment = fragment;
			// getPropertyChangeSupport().firePropertyChange("fragment", oldValue, fragment);
		}
	}

	@Override
	protected AbstractElement createDefaultRoot() {
		// grabs a write-lock for this initialization and
		// abandon it during initialization so in normal
		// operation we can detect an illegitimate attempt
		// to mutate attributes.
		writeLock();
		DocFragmentElement<D, TA> rootElement = new DocFragmentElement<>(this);
		ParagraphElement<D, TA> paragraph = new ParagraphElement<>(this, rootElement, null);

		RunElement<D, TA> brk = new RunElement<>(this, paragraph, null, 0, 1, null);
		Element[] buff = new Element[1];
		buff[0] = brk;
		paragraph.replace(0, 0, buff);

		buff[0] = paragraph;
		rootElement.replace(0, 0, buff);
		writeUnlock();
		return rootElement;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Element createBranchElement(Element parent, AttributeSet a) {
		if (parent instanceof DocFragmentElement) {
			return new ParagraphElement<>(this, (DocFragmentElement<D, TA>) parent, a);

		}
		return super.createBranchElement(parent, a);
	}

}
