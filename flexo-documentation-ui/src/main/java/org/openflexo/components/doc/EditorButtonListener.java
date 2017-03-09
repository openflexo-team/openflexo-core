/**
 * 
 */
package org.openflexo.components.doc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.doc.InlineStyle;
import org.openflexo.foundation.doc.Property;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class EditorButtonListener implements ActionListener {

	private EditorPanel panel;
	private EditorButton button;

	/**
	 * 
	 */
	public EditorButtonListener(EditorPanel panel, EditorButton button) {
		this.panel = panel;
		this.button = button;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Editor editor = panel.getEditor();
		int start = editor.getSelectionStart() - 1;
		int end = editor.getSelectionEnd() - 1;
		setProperty(editor.getDocumentModel(), button.getProperty(), start, end);

		String newText = panel.getTranslator().generateHTML(editor.getDocumentModel());
		System.out.println("newText=" + newText);

		// editor.setText(newText);
	}

	/**
	 * Adds a property to a given portion of content.
	 * 
	 * @param property
	 *            the property to be added
	 * @param start
	 *            the beginning of the portion
	 * @param end
	 *            the end of the portion
	 */
	public <D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> void setProperty(FlexoDocument<D, TA> document, Property property,
			int start, int end) {

		System.out.println("On fait un setProperty pour " + property + " start=" + start + " end=" + end);

		int currentPosition = 0;
		// Let's go through the arborescence to find the runs that we want to change
		for (FlexoDocElement<D, TA> e : document.getElements()) {
			// Paragraphs - Tables will be handled later
			System.out.println("Examen d'un element");
			if (e instanceof FlexoDocParagraph) {
				System.out.println("L'element est un paragraphe");
				FlexoDocParagraph<D, TA> p = (FlexoDocParagraph<D, TA>) e;
				ListIterator<FlexoDocRun<D, TA>> i = (ListIterator) p.getRuns().iterator();
				while (i.hasNext()) {
					FlexoDocRun<D, TA> r = i.next();
					// TextRuns - ImgRuns will be handled later
					System.out.println("Examen d'un run");
					if (r instanceof FlexoTextRun) {
						System.out.println("Le run est un TextRun");
						FlexoTextRun<D, TA> tr = (FlexoTextRun<D, TA>) r;
						// Let's check if the offset is within this run.
						int l = tr.getText().length();
						currentPosition += l;
						System.out.println("Position courante : " + currentPosition + ", position recherchee : " + start);
						// If it is, this is our guy
						if (currentPosition >= start) {
							System.out.println("La position de depart a ete trouvee");
							// Start position within the text
							int startPosition = l - currentPosition + start;
							// Let's split the text around this position
							String textStart = tr.getText().substring(0, startPosition);
							String textEnd = tr.getText().substring(startPosition);
							// The current run will only keep the beginning of the text.
							tr.setText(textStart);
							// Ok, now let's create a run that has our new property, plus the properties of tr.
							InlineStyle s = new InlineStyle();
							s.setParent(tr.getStyle());
							FlexoTextRun<D, TA> newRun = document.getFactory().makeTextRun("");// new TextRun(s, "");
							newRun.setStyle(s);
							newRun.getStyle().addProperty(property);
							// Is the rest less, more, or exactly what we want?
							// If less
							if (textEnd.length() < end - start + 1) {
								System.out.println("Il y aura d'autres runs a explorer");
								// All the rest of the text goes into our new run
								newRun.setText(textEnd);
								// And we insert it after tr
								i.add(newRun);
								// The exploration of the arborescence will go on.
								// The next run to be explored will be newRun.
								// We don't want to change it, so let's change our start position
								start += textEnd.length();
							}
							// If exactly
							else if (textEnd.length() == end - start + 1) {
								System.out.println("La modification va pile jusqu'a la fin du run");
								// All the rest of the text goes into our new run
								newRun.setText(textEnd);
								// And we insert it after tr
								p.insertRunAtIndex(newRun, tr.getIndex() + 1);
								// p.insertRunAfter(newRun, tr);
								// We're done! No other run is needed.
								break;
							}
							// If more
							else {
								System.out.println("La modification ne va pas tout a fait jusqu'a la fin du run");
								// Let's split the remaining text into two parts:
								// What must change, and what must remain the same.
								String changingText = textEnd.substring(0, end - start);
								textEnd = textEnd.substring(end - start);
								// What must change goes into our new run
								newRun.setText(changingText);
								p.insertRunAtIndex(newRun, tr.getIndex() + 1);
								// p.insertRunAfter(newRun, tr);
								// The rest goes into a run similar to tr
								FlexoTextRun<D, TA> lastRun = document.getFactory().makeTextRun(textEnd);
								lastRun.setStyle(tr.getStyle());
								p.insertRunAtIndex(lastRun, tr.getIndex() + 1);
								// TextRun lastRun = new TextRun(tr.getStyle(), textEnd);
								// p.insertRunAfter(lastRun, newRun);
								// We're done!
								break;
							}
						}
						// If not, let's look at the next run.
					}
				}
				// End of paragraph = \n, thus one more character
				currentPosition++;
			}
		}

		// panel.updateDocument();

		// panel.getEditor().fire

		// panel.getEditor().getDocument().

		/*for (DocumentListener l : listeners) {
			l.contentUpdate();
		}*/
	}

}
