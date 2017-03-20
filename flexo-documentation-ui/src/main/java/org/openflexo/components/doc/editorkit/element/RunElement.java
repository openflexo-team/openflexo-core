package org.openflexo.components.doc.editorkit.element;

import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
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
public class RunElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends LeafElement
		implements AbstractDocumentElement<FlexoTextRun<D, TA>, D, TA> {

	/**
	 * 
	 */
	private final FlexoStyledDocument<D, TA> flexoStyledDocument;
	FlexoTextRun<D, TA> run;

	public RunElement(FlexoStyledDocument<D, TA> flexoStyledDocument, Element parent, AttributeSet a, int offs0, int offs1,
			FlexoTextRun<D, TA> run) {
		flexoStyledDocument.super(parent, a, offs0, offs1);
		this.flexoStyledDocument = flexoStyledDocument;
		this.run = run;
	}

	public FlexoTextRun<D, TA> getRun() {
		return getDocObject();
	}

	public void setRun(FlexoTextRun<D, TA> run) {
		this.run = run;
	}

	@Override
	public FlexoTextRun<D, TA> getDocObject() {
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
	public FlexoTextRun<D, TA> lookupDocObject() {
		int index = getParent().getIndex(this);
		int runIndex = 0;
		if (this.flexoStyledDocument.getFlexoDocument() != null && getParentElement() instanceof ParagraphElement
				&& ((ParagraphElement<D, TA>) getParentElement()).getParagraph() != null) {
			for (FlexoDocRun<D, TA> r : ((ParagraphElement<D, TA>) getParentElement()).getParagraph().getRuns()) {
				if (r instanceof FlexoTextRun) {
					if (runIndex == index) {
						run = (FlexoTextRun<D, TA>) r;
						break;
					}
					runIndex++;
				}
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

	@Override
	public String toString() {
		String text = "???";
		try {
			text = this.flexoStyledDocument.getText(getStartOffset(), getEndOffset() - getStartOffset());
			if (text.length() > 20) {
				text = text.substring(0, 20);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return "RunElement(" + Integer.toHexString(hashCode()) + ") " + getStartOffset() + "," + getEndOffset() + ":"
				+ /*run.getText()*/text;
	}

}
