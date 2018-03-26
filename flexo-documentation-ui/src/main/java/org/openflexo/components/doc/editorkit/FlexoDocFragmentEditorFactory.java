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

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument.DocumentRootElement;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * A factory used to build {@link FlexoStyledDocument} from a given {@link FlexoDocument}
 *
 * Note that this class was originally inspired from Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
 * 
 * @author Stanislav Lapitsky
 * @author sylvain
 */
public class FlexoDocFragmentEditorFactory<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoDocumentEditorFactory<D, TA> {

	private FlexoDocFragment<D, TA> fragment;

	public FlexoDocFragmentEditorFactory(FlexoDocFragment<D, TA> fragment) throws BadLocationException {
		super();
		this.fragment = fragment;
		document = new FlexoFragmentStyledDocument<>(fragment);
		read(fragment, 0);
	}

	public FlexoDocFragment<D, TA> getFragment() {
		return fragment;
	}

	@Override
	public FlexoDocument<D, TA> getFlexoDocument() {
		return getFragment().getFlexoDocument();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void read(FlexoDocument<D, TA> flexoDocument, int offset) throws BadLocationException {
		// Do not read the full document here
	}

	protected FlexoFragmentStyledDocument<D, TA> makeStyledDocument(D flexoDocument) {
		return new FlexoFragmentStyledDocument<>(null);
	}

	/**
	 * Reads content of specified fragment to the document.
	 *
	 * @param in
	 *            stream.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	protected void read(FlexoDocFragment<D, TA> flexoFragment, int offset) throws BadLocationException {
		System.out.println("Starting reading fragment " + flexoFragment);

		document.setIsReadingDocument(true);
		iteratePart(flexoFragment.getElements());

		// System.out.println("Before lookup: ");
		// System.out.println(AbstractDocumentElement.debugElement(document.getRootElement()));

		for (Element e : document.getRootElements()) {
			if (e instanceof DocumentRootElement) {
				((FlexoStyledDocument<D, TA>.DocumentRootElement<?>) e).lookupDocObject();
			}
		}

		// System.out.println("After lookup: ");
		// System.out.println(AbstractDocumentElement.debugElement(document.getRootElement()));

		document.setIsReadingDocument(false);

		this.currentOffset = offset;

	}

}
