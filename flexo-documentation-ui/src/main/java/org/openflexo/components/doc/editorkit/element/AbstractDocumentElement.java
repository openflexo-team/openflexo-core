package org.openflexo.components.doc.editorkit.element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public interface AbstractDocumentElement<E extends FlexoDocObject<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> {

	/**
	 * Return conceptual document beeing represented by this {@link FlexoStyledDocument}
	 * 
	 * @return
	 */
	D getFlexoDocument();

	/**
	 * Return internal representation of conceptual document
	 * 
	 * @return
	 */
	FlexoStyledDocument<D, TA> getFlexoStyledDocument();

	/**
	 * Return {@link FlexoDocObject} this element represents
	 * 
	 * @return
	 */
	E getDocObject();

	/**
	 * Recursive call to doc object looking up
	 * 
	 * @return
	 */
	E lookupDocObject();

	// public <O extends FlexoDocObject<D,TA>> AbstractDocumentElement<O> getElement(O docObject);

}
