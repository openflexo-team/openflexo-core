package org.openflexo.components.doc.editorkit.element;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
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
public class ParagraphElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends BranchElement
		implements AbstractDocumentElement<FlexoDocParagraph<D, TA>, D, TA> {

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	private FlexoDocParagraph<D, TA> paragraph = null;

	public ParagraphElement(FlexoStyledDocument<D, TA> flexoStyledDocument, DocumentElement<D, TA> documentElement, AttributeSet a) {
		flexoStyledDocument.super(documentElement, a);
		this.flexoStyledDocument = flexoStyledDocument;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentElement<D, TA> getParent() {
		return (DocumentElement<D, TA>) super.getParent();
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
		int paragraphIndex = 0;
		if (getFlexoDocument() != null) {
			for (FlexoDocElement<D, TA> e : getFlexoDocument().getElements()) {
				if (e instanceof FlexoDocParagraph) {
					if (paragraphIndex == index) {
						paragraph = (FlexoDocParagraph<D, TA>) e;
						break;
					}
					paragraphIndex++;
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

	@Override
	public String toString() {
		return "ParagraphElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":"
				+ (getParagraph() != null ? getParagraph().getRawTextPreview() : "null");
	}
}
