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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.StructuralModification.FragmentStructure.RetainedParagraphElement;
import org.openflexo.components.doc.editorkit.StructuralModification.FragmentStructure.RetainedParagraphElement.RetainedRunElement;
import org.openflexo.components.doc.editorkit.element.ParagraphElement;
import org.openflexo.components.doc.editorkit.element.RunElement;
import org.openflexo.foundation.doc.FlexoDocElementContainer;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.StringUtils;

class StructuralModification<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> {

	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	FragmentStructure previous;
	FragmentStructure now;

	public StructuralModification(FlexoStyledDocument<D, TA> flexoStyledDocument, int startOffet, int length) {
		this.flexoStyledDocument = flexoStyledDocument;
		// System.out.println("On va changer la structure du document");
		previous = new FragmentStructure(startOffet, length);
		// System.out.println("WAS:");
		// System.out.println(previous.toString());
		// Thread.dumpStack();
	}

	void fireDocumentChanged(DocumentEvent e) {
		now = new FragmentStructure(e.getOffset(), e.getLength());

		System.out.println("OK, on a change des trucs");
		System.out.println("WAS:");
		System.out.println(previous.toString());
		System.out.println("NOW:");
		System.out.println(now.toString());
		// Thread.dumpStack();

		RetainedParagraphElement currentP = null;

		if (previous.paragraphElements.size() <= now.paragraphElements.size()) {
			for (int i = 0; i < previous.paragraphElements.size(); i++) {
				RetainedParagraphElement oldP = previous.paragraphElements.get(i);
				RetainedParagraphElement newP = now.paragraphElements.get(i);
				fireUpdateParagraph(oldP, newP);
				currentP = newP;
			}
			// Now handle new paragraphs
			for (int i = previous.paragraphElements.size(); i < now.paragraphElements.size(); i++) {
				RetainedParagraphElement newP = now.paragraphElements.get(i);
				fireNewParagraph(newP, currentP);
				currentP = newP;
			}
		}
		else {
			// Some paragraphs have been removed
			for (int i = now.paragraphElements.size(); i < previous.paragraphElements.size(); i++) {
				RetainedParagraphElement oldPToRemove = previous.paragraphElements.get(i);
				if (oldPToRemove.paragraph != null) {
					oldPToRemove.paragraph.getContainer().removeFromElements(oldPToRemove.paragraph);
				}
			}
			for (int i = 0; i < now.paragraphElements.size(); i++) {
				RetainedParagraphElement oldP = previous.paragraphElements.get(i);
				RetainedParagraphElement newP = now.paragraphElements.get(i);
				fireUpdateParagraph(oldP, newP);
				currentP = newP;
			}
		}

	}

	private void fireUpdateParagraph(FragmentStructure.RetainedParagraphElement oldP, FragmentStructure.RetainedParagraphElement newP) {

		if (newP.paragraph == null) {
			newP.paragraph = newP.parElement.lookupDocObject();
			if (newP.paragraph != null) {
				System.out.println("Tiens j'ai quand meme trouve le paragraph: " + newP.paragraph);
			}
		}

		RetainedRunElement currentR = null;

		if (oldP.runElements.size() <= newP.runElements.size()) {
			for (int i = 0; i < oldP.runElements.size(); i++) {
				RetainedRunElement oldR = oldP.runElements.get(i);
				RetainedRunElement newR = newP.runElements.get(i);
				fireUpdateRun(oldR, newR);
			}
			// Now handle new runs
			for (int i = oldP.runElements.size(); i < newP.runElements.size(); i++) {
				RetainedRunElement newR = newP.runElements.get(i);
				String newText = newR.getText();
				if (newText.endsWith("\n")) {
					newText = newText.substring(0, newText.length() - 1);
				}
				if (StringUtils.isNotEmpty(newText)) {
					fireNewRun(newP.paragraph, newR, newR.getText(), currentR);
					currentR = newR;
				}
			}

		}
	}

	private void fireNewParagraph(FragmentStructure.RetainedParagraphElement newP, FragmentStructure.RetainedParagraphElement previousP) {
		System.out.println("Creating new paragraph just after " + previousP.paragraph);

		FlexoDocElementContainer<D, TA> container = previousP.paragraph.getContainer();
		int index = previousP.paragraph.getIndex();
		FlexoDocParagraph<D, TA> newParagraph = this.flexoStyledDocument.flexoDocument.getFactory().makeParagraph();
		container.insertElementAtIndex(newParagraph, index + 1);
		newP.parElement.setParagraph(newParagraph);
		newP.paragraph = newParagraph;

		for (RetainedRunElement rre : newP.runElements) {
			String newText = rre.getText();
			if (newText.endsWith("\n")) {
				newText = newText.substring(0, newText.length() - 1);
			}
			System.out.println("Creating new run with [" + newText + "]");
			FlexoTextRun newRun = this.flexoStyledDocument.flexoDocument.getFactory().makeTextRun(newText);
			newParagraph.addToRuns(newRun);
		}

	}

	private static void fireUpdateRun(RetainedRunElement oldR, RetainedRunElement newR) {
		if (oldR.run == null) {
			FlexoStyledDocument.logger.warning("Unexpected null run");
			return;
		}
		if (newR.run == null) {
			newR.run = newR.runElement.lookupDocObject();
			if (newR.run != null) {
				System.out.println("Tiens j'ai quand meme trouve le run: " + newR.run);
			}
		}
		if (newR.run == null) {
			System.out.println("Tiens faut creer un nouveau run pour " + oldR.run.getParagraph());
		}
		else if (newR.run == oldR.run) {
			String newText = newR.getText();
			if (newText.endsWith("\n")) {
				newText = newText.substring(0, newText.length() - 1);
			}
			if (newR.run instanceof FlexoTextRun) {
				System.out.println("Mise a jour du run " + newR.run + " avec " + newText);
				((FlexoTextRun<?, ?>) newR.run).setText(newText);
			}
		}
	}

	private void fireNewRun(FlexoDocParagraph<D, TA> container, FragmentStructure.RetainedParagraphElement.RetainedRunElement newR,
			String text, RetainedRunElement previousR) {
		System.out.println("Creating new run just after " + previousR);

		int index = -1;
		if (previousR != null && previousR.run != null) {
			index = previousR.run.getIndex();
		}
		FlexoTextRun newRun = container.getFlexoDocument().getFactory().makeTextRun(text);
		if (index == -1) {
			container.addToRuns(newRun);
		}
		else {
			container.insertRunAtIndex(newRun, index + 1);
		}
		newR.runElement.setRun(newRun);
		newR.run = newRun;
	}

	class FragmentStructure {

		List<RetainedParagraphElement> paragraphElements;

		public FragmentStructure(int offset, int length) {
			ParagraphElement<D, TA> pElStart = (ParagraphElement<D, TA>) StructuralModification.this.flexoStyledDocument
					.getParagraphElement(offset);
			ParagraphElement<D, TA> pElEnd = (ParagraphElement<D, TA>) StructuralModification.this.flexoStyledDocument
					.getParagraphElement(offset + length);
			FlexoStyledDocument<D, TA>.DocumentRootElement<?> docElement = pElStart.getParent();
			int startId = -1;
			int endId = -1;
			for (int i = 0; i < docElement.getElementCount(); i++) {
				Element e = docElement.getElement(i);
				if (e == pElStart) {
					startId = i;
				}
				if (e == pElEnd) {
					endId = i;
				}
			}
			paragraphElements = new ArrayList<>();
			for (int i = startId; i <= endId; i++) {
				Element e = docElement.getElement(i);
				if (e instanceof ParagraphElement) {
					paragraphElements.add(new RetainedParagraphElement((ParagraphElement<D, TA>) e));
				}
			}
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Document structure:\n");
			for (RetainedParagraphElement rpe : paragraphElements) {
				sb.append(rpe);
			}
			return sb.toString();
		}

		class RetainedParagraphElement {
			ParagraphElement<D, TA> parElement;
			FlexoDocParagraph<D, TA> paragraph;
			List<RetainedRunElement> runElements;
			int startIndex;
			int endIndex;

			public RetainedParagraphElement(ParagraphElement<D, TA> pEl) {
				this.parElement = pEl;
				startIndex = pEl.getStartOffset();
				endIndex = pEl.getEndOffset();
				paragraph = pEl.getParagraph();
				runElements = new ArrayList<>();
				for (int i = 0; i < pEl.getElementCount(); i++) {
					Element child = pEl.getElement(i);
					if (child instanceof RunElement) {
						RetainedRunElement rEl = new RetainedRunElement((RunElement<D, TA>) child);
						runElements.add(rEl);
					}
				}
			}

			@Override
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("  > " + "[ParagraphElement:" + Integer.toHexString(parElement.hashCode()) + "] start=" + startIndex + " end="
						+ endIndex + " [" + (paragraph != null ? Integer.toHexString(paragraph.hashCode()) : "null") + "]\n");
				for (RetainedRunElement rre : runElements) {
					sb.append(rre + "\n");
				}
				return sb.toString();
			}

			class RetainedRunElement {
				RunElement<D, TA> runElement;
				FlexoDocRun<D, TA> run;
				int startIndex;
				int endIndex;

				public RetainedRunElement(RunElement<D, TA> rEl) {
					this.runElement = rEl;
					startIndex = rEl.getStartOffset();
					endIndex = rEl.getEndOffset();
					run = rEl.getRun();
				}

				public String getText() {
					try {
						return flexoStyledDocument.getText(startIndex, endIndex - startIndex);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public String toString() {
					return "    > " + "[RunElement:" + Integer.toHexString(runElement.hashCode()) + "] start=" + startIndex + " end="
							+ endIndex + " [" + (run != null ? Integer.toHexString(run.hashCode()) : "null") + "]";
				}
			}
		}
	}
}
