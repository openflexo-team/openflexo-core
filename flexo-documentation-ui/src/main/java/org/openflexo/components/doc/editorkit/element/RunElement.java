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

import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Element (a {@link AbstractDocumentElement}) representing a {@link FlexoDocRun}
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@SuppressWarnings("serial")
public class RunElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends LeafElement
		implements AbstractDocumentElement<FlexoDocRun<D, TA>, D, TA> {

	/**
	 * 
	 */
	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	FlexoDocRun<D, TA> run;

	public RunElement(FlexoStyledDocument<D, TA> flexoStyledDocument, Element parent, AttributeSet a, int offs0, int offs1,
			FlexoTextRun<D, TA> run) {
		flexoStyledDocument.super(parent, a, offs0, offs1);
		this.flexoStyledDocument = flexoStyledDocument;
		this.run = run;
	}

	public FlexoDocRun<D, TA> getRun() {
		return getDocObject();
	}

	public void setRun(FlexoDocRun<D, TA> run) {
		this.run = run;
	}

	@Override
	public FlexoDocRun<D, TA> getDocObject() {
		/*if (run == null && getParentElement() instanceof ParagraphElement && currentModification != null) {
			int index = getParent().getIndex(this);
			int runIndex = 0;
			if (flexoDocument != null) {
				for (FlexoDocRun<D,TA> r : ((ParagraphElement) getParentElement()).getParagraph().getRuns()) {
					if (r instanceof FlexoTextRun) {
						if (runIndex == index) {
							run = (FlexoTextRun<D,TA>) r;
							break;
						}
						runIndex++;
					}
				}
			}
		}*/
		return run;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FlexoDocRun<D, TA> lookupDocObject() {
		int index = getParent().getIndex(this);
		int runIndex = 0;
		if (this.flexoStyledDocument.getFlexoDocument() != null && getParentElement() instanceof ParagraphElement
				&& ((ParagraphElement<D, TA>) getParentElement()).getParagraph() != null) {
			for (FlexoDocRun<D, TA> r : ((ParagraphElement<D, TA>) getParentElement()).getParagraph().getRuns()) {
				// if (r instanceof FlexoTextRun) {
				if (runIndex == index) {
					run = r;
					break;
				}
				runIndex++;
				// }
			}
		}
		return run;
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
		String text = "???";
		if (run instanceof FlexoTextRun) {
			try {
				text = this.flexoStyledDocument.getText(getStartOffset(), getEndOffset() - getStartOffset());
				if (text.length() > 20) {
					text = text.substring(0, 20);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		else if (run instanceof FlexoDrawingRun) {
			text = ((FlexoDrawingRun) run).getImageName();
		}
		return "RunElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":" + text;
	}

}
