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

package org.openflexo.components.doc.editorkit.view;

import javax.swing.text.AbstractDocument;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * A factory to create a view of some portion of document subject. This is intended to enable customization of how views get mapped over a
 * document model.
 *
 * @author Stanislav Lapitsky
 */
// TODO: FD4SG why a Docx in flexo-documentation?
public class DocxViewFactory implements ViewFactory {

	/**
	 * Constructs new instance.
	 */
	public DocxViewFactory() {
	}

	/**
	 * Creates a view from the given structural element of a document.
	 *
	 * @param elem
	 *            the piece of the document to build a view of
	 * @return the view
	 * @see View
	 */
	@Override
	public View create(Element elem) {
		String kind = elem.getName();
		if (kind != null) {
			if (kind.equals(AbstractDocument.ContentElementName)) {
				return new LabelView(elem);
			}
			else if (kind.equals(AbstractDocument.ParagraphElementName)) {
				// return new NumberedParagraphView(elem);
				return new ParagraphView(elem);
			}
			else if (kind.equals(AbstractDocument.SectionElementName)) {
				return new SectionView(elem, View.Y_AXIS);
			}
			else if (kind.equals(StyleConstants.ComponentElementName)) {
				return new ComponentView(elem);
			}
			else if (kind.equals(StyleConstants.IconElementName)) {
				return new IconView(elem);
			}
			else if (kind.equals("table")) {
				return new TableView(elem);
			}
			else if (kind.equals("row")) {
				return new RowView(elem);
			}
			else if (kind.equals("cell")) {
				return new CellView(elem);
			}
		}
		// default to text display
		return new LabelView(elem);
	}
}
