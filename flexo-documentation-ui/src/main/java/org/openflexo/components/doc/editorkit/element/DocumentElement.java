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

package org.openflexo.components.doc.editorkit.element;

import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Element (a {@link AbstractDocumentElement}) representing the whole document
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@SuppressWarnings("serial")
public class DocumentElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoStyledDocument<D, TA>.DocumentRootElement<D> {

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;

	/**
	 * @param flexoStyledDocument
	 */
	public DocumentElement(FlexoStyledDocument<D, TA> flexoStyledDocument) {
		flexoStyledDocument.super();
		this.flexoStyledDocument = flexoStyledDocument;
	}

	@Override
	public D getDocObject() {
		return getFlexoDocument();
	}

	@Override
	public D lookupDocObject() {
		for (int i = 0; i < getElementCount(); i++) {
			Element e = getElement(i);
			if (e instanceof AbstractDocumentElement) {
				((AbstractDocumentElement<?, ?, ?>) e).lookupDocObject();
			}
		}

		return this.flexoStyledDocument.getFlexoDocument();
	}

	@Override
	public FlexoStyledDocument<D, TA> getFlexoStyledDocument() {
		return flexoStyledDocument;
	}

	@Override
	public D getFlexoDocument() {
		return getFlexoStyledDocument().getFlexoDocument();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {

		return AbstractDocumentElement.retrieveElement((AbstractDocumentElement<O, D, TA>) this, docObject);
	}

	@Override
	public String toString() {
		return "DocumentElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset();
	}

}
