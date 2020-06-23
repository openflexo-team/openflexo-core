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
import org.openflexo.toolbox.StringUtils;

public interface AbstractDocumentElement<E extends FlexoDocObject<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> {

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

	/**
	 * Retrieve {@link AbstractDocumentElement} encoding supplied docObject
	 * 
	 * @param docObject
	 * @return
	 */
	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject);

	/**
	 * Gets a child element.
	 *
	 * @param index
	 *            the child index, &gt;= 0 &amp;&amp; &lt; getElementCount()
	 * @return the child element, null if none
	 */
	public Element getElement(int index);

	/**
	 * Gets the number of children for the element.
	 *
	 * @return the number of children &gt;= 0
	 */
	public int getElementCount();

	public int getStartOffset();

	public int getEndOffset();

	@SuppressWarnings("unchecked")
	public static <O extends FlexoDocObject<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> AbstractDocumentElement<O, D, TA> retrieveElement(
			AbstractDocumentElement<O, D, TA> element, O docObject) {

		if (element.getDocObject() == null) {
			element.lookupDocObject();
		}
		if (docObject == element.getDocObject()) {
			return element;
		}

		// System.out.println("> On cherche " + docObject + " dans " + element);

		for (int i = 0; i < element.getElementCount(); i++) {
			Element e = element.getElement(i);
			// System.out.println("On cherche dans " + e);
			if (e instanceof AbstractDocumentElement) {
				AbstractDocumentElement<O, D, TA> docElement = (AbstractDocumentElement<O, D, TA>) e;
				if (docElement.getDocObject() == null) {
					// System.out.println("Lookup necessaire");
					docElement.lookupDocObject();
				}
				AbstractDocumentElement<O, D, TA> potentialResult = docElement.getElement(docObject);
				// System.out.println("En cherchant dans " + element + " je trouve: " + potentialResult);
				if (potentialResult != null) {
					return potentialResult;
				}
			}
		}
		return null;
	}

	// @SuppressWarnings("unchecked")
	public static String debugElement(AbstractDocumentElement<?, ?, ?> element) {
		return debugElement(element, 0);
	}

	// @SuppressWarnings("unchecked")
	public static String debugElement(AbstractDocumentElement<?, ?, ?> element, int indentLevel) {

		String indent = StringUtils.buildWhiteSpaceIndentation(indentLevel);
		StringBuffer sb = new StringBuffer();
		sb.append(indent + element.toString() + "\n");
		for (int i = 0; i < element.getElementCount(); i++) {
			Element e = element.getElement(i);
			// System.out.println("On cherche dans " + e);
			if (e instanceof AbstractDocumentElement) {
				AbstractDocumentElement<?, ?, ?> docElement = (AbstractDocumentElement<?, ?, ?>) e;
				sb.append(debugElement(docElement, indentLevel + 2));
			}
		}
		return sb.toString();
	}

}
