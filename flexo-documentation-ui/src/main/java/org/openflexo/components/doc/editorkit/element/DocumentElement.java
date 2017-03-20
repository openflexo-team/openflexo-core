package org.openflexo.components.doc.editorkit.element;

import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
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
public class DocumentElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FlexoStyledDocument<D, TA>.DocumentRootElement implements AbstractDocumentElement<FlexoDocument<D, TA>, D, TA> {

	/**
	 * 
	 */
	private final FlexoStyledDocument<D, TA> flexoStyledDocument;

	/**
	 * @param flexoStyledDocument
	 */
	public DocumentElement(FlexoStyledDocument<D, TA> flexoStyledDocument) {
		flexoStyledDocument.super();
		this.flexoStyledDocument = flexoStyledDocument;
	}

	@Override
	public FlexoDocument<D, TA> getDocObject() {
		return getFlexoDocument();
	}

	@Override
	public FlexoDocument<D, TA> lookupDocObject() {
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

	@Override
	public String toString() {
		return "DocumentElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n";
	}
}
