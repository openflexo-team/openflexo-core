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

import java.util.logging.Logger;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoFragmentStyledDocument;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Element (a {@link AbstractDocumentElement}) representing a {@link FlexoDocParagraph}
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@SuppressWarnings("serial")
public class ParagraphElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends BranchElement
		implements AbstractDocumentElement<FlexoDocParagraph<D, TA>, D, TA> {

	static final Logger logger = Logger.getLogger(ParagraphElement.class.getPackage().getName());

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	private FlexoDocParagraph<D, TA> paragraph = null;

	public ParagraphElement(FlexoStyledDocument<D, TA> flexoStyledDocument, DocumentElement<D, TA> documentElement, AttributeSet a) {
		flexoStyledDocument.super(documentElement, a);
		this.flexoStyledDocument = flexoStyledDocument;
	}

	public ParagraphElement(FlexoFragmentStyledDocument<D, TA> flexoStyledDocument, DocFragmentElement<D, TA> docFragmentElement,
			AttributeSet a) {
		flexoStyledDocument.super(docFragmentElement, a);
		this.flexoStyledDocument = flexoStyledDocument;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FlexoStyledDocument<D, TA>.DocumentRootElement<?> getParent() {
		return (FlexoStyledDocument<D, TA>.DocumentRootElement<?>) super.getParent();
	}

	public FlexoDocParagraph<D, TA> getParagraph() {
		return getDocObject();
	}

	public void setParagraph(FlexoDocParagraph<D, TA> paragraph) {
		this.paragraph = paragraph;
	}

	@Override
	public FlexoDocParagraph<D, TA> getDocObject() {
		return paragraph;
	}

	@Override
	public FlexoDocParagraph<D, TA> lookupDocObject() {
		int index = getParent().getIndex(this);
		if (getParent() instanceof DocumentElement) {
			if (getFlexoDocument() != null) {
				int elementIndex = 0;
				for (FlexoDocElement<D, TA> e : getFlexoDocument().getElements()) {
					if (e instanceof FlexoDocParagraph) {
						if (elementIndex == index) {
							paragraph = (FlexoDocParagraph<D, TA>) e;
							break;
						}
					}
					if (e instanceof FlexoDocParagraph || e instanceof FlexoDocTable) {
						elementIndex++;
					}
				}
			}
		}
		else if (getParent() instanceof DocFragmentElement) {
			FlexoDocFragment<D, TA> fragment = ((DocFragmentElement) getParent()).getDocObject();
			int elementIndex = 0;
			for (FlexoDocElement<D, TA> e : fragment.getElements()) {
				if (e instanceof FlexoDocParagraph) {
					if (elementIndex == index) {
						paragraph = (FlexoDocParagraph<D, TA>) e;
						break;
					}
				}
				if (elementIndex > index) {
					logger.warning("Could not find FlexoDocParagraph for " + this);
					return null;
				}
				if (e instanceof FlexoDocParagraph || e instanceof FlexoDocTable) {
					elementIndex++;
				}
			}
		}
		for (int i = 0; i < getElementCount(); i++) {
			Element e = getElement(i);
			if (e instanceof AbstractDocumentElement) {
				((AbstractDocumentElement<?, ?, ?>) e).lookupDocObject();
			}
		}
		return paragraph;
	}

	@Override
	public int getStartOffset() {
		if (getElementCount() > 0) {
			return super.getStartOffset();
		}
		return -1;
	}

	@Override
	public int getEndOffset() {
		if (getElementCount() > 0) {
			return super.getEndOffset();
		}
		return -1;
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
		return "ParagraphElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":"
				+ (getParagraph() != null ? getParagraph().getRawTextPreview() : "null");
	}
}
