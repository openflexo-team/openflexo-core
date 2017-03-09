/**
 * 
 */
package org.openflexo.components.doc;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocStyle;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.doc.Property;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class Editor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends JEditorPane {

	private FlexoDocument<D, TA> flexoDocument;
	private DocumentFactory<D, TA> documentFactory;
	private Translator<D, TA> translator;
	private Highlighter highlighter;
	private DefaultHighlightPainter highlighterPainter;

	/**
	 * Creates an empty Editor.
	 */
	public Editor(DocumentFactory<D, TA> documentFactory) {
		super("text/html", "");
		this.translator = new Translator<>();
		this.documentFactory = documentFactory;
		highlighter = getHighlighter();
		highlighterPainter = new DefaultHighlightPainter(Color.GREEN);
	}

	/**
	 * Creates an Editor with a Document to be displayed and edited.
	 * 
	 * @param flexoDocument
	 *            the flexoDocument.
	 */
	public Editor(FlexoDocument<D, TA> flexoDocument) {
		this(flexoDocument.getFactory());
		this.flexoDocument = flexoDocument;
		setDocumentModel(flexoDocument);

	}

	public void highlight(int startIndex, int endIndex) {
		try {
			highlighter.addHighlight(startIndex, endIndex, highlighterPainter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FlexoDocument<D, TA> getDocumentModel() {
		return flexoDocument;
	}

	public void setDocumentModel(FlexoDocument<D, TA> flexoDocument) {
		if (flexoDocument != this.flexoDocument) {
			FlexoDocument<D, TA> oldValue = this.flexoDocument;
			this.flexoDocument = flexoDocument;
			getDocument().addDocumentListener(new EditorListener(this));
			updateContents();
			// getPropertyChangeSupport().firePropertyChange("documentModel", oldValue, flexoDocument);
		}
	}

	public Translator<D, TA> getTranslator() {
		return this.translator;
	}

	public DocumentFactory<D, TA> getDocumentFactory() {
		return documentFactory;
	}

	protected void updateContents() {
		if (flexoDocument != null) {
			String content = translator.generateHTML(flexoDocument);
			System.out.println(content);
			setText(content);
		}
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
	public void setProperty(Property property, int start, int end) {
		int currentPosition = 0;
		// Let's go through the arborescence to find the runs that we want to change
		for (FlexoDocElement<D, TA> e : flexoDocument.getElements()) {
			// Paragraphs - Tables will be handled later
			System.out.println("Examen d'un element");
			if (e instanceof FlexoDocParagraph) {
				System.out.println("L'element est un paragraphe");
				FlexoDocParagraph<D, TA> p = (FlexoDocParagraph<D, TA>) e;
				currentPosition = setPropertyIntoParagraph(start, end, property, currentPosition, p);
				if (currentPosition == -1 || currentPosition == end)
					return;
				else if (currentPosition >= start) {
					start = currentPosition;
				}
			}
			else if (e instanceof FlexoDocTable) {
				for (FlexoDocTableRow<D, TA> r : ((FlexoDocTable<D, TA>) e).getTableRows()) {
					for (FlexoDocTableCell<D, TA> c : r.getTableCells()) {
						for (FlexoDocParagraph<D, TA> p : c.getParagraphs()) {
							System.out.println("Examen d'un paragraphe de la table");
							currentPosition = setPropertyIntoParagraph(start, end, property, currentPosition, p);
							if (currentPosition == -1 || currentPosition == end)
								return;
							else if (currentPosition >= start) {
								start = currentPosition;
							}
						}
					}
				}
			}
		}
		/*for (DocumentListener l : listeners) {
			l.contentUpdate();
		}*/
	}

	public void delete(int offset, int length) {
		int currentPosition = 0;
		// Let's go through the arborescence to find the runs that we want to change
		for (FlexoDocElement<D, TA> e : flexoDocument.getElements()) {
			// Paragraphs - Tables will be handled later
			System.out.println("Examen d'un element");
			if (e instanceof FlexoDocParagraph) {
				System.out.println("L'element est un paragraphe");
				FlexoDocParagraph<D, TA> p = (FlexoDocParagraph<D, TA>) e;
				currentPosition = deleteIntoParagraph(currentPosition, offset, length, p);
				if (currentPosition == -1 || currentPosition == offset + length)
					return;
				else if (currentPosition >= offset) {
					length -= currentPosition - offset;
					offset += currentPosition;
				}
			}
			// Table
			else if (e instanceof FlexoDocTable) {
				FlexoDocTable<D, TA> t = (FlexoDocTable<D, TA>) e;
				for (FlexoDocTableRow<D, TA> r : t.getTableRows()) {
					for (FlexoDocTableCell<D, TA> c : r.getTableCells()) {
						for (FlexoDocParagraph<D, TA> p : c.getParagraphs()) {
							currentPosition = deleteIntoParagraph(currentPosition, offset, length, p);
							if (currentPosition == -1 || currentPosition == offset + length)
								return;
							else if (currentPosition >= offset) {
								length -= currentPosition - offset;
								offset += currentPosition;
							}
						}
					}
				}
			}
		}
		/*for (DocumentListener l : listeners) {
			l.contentUpdate();
		}*/
	}

	// Private auxiliary methods

	/**
	 * Submethod of insert, avoids repetition of code. It is used to locate the paragraph and insert the text in it.
	 * 
	 * @param p
	 *            paragraph to be examined
	 * @param offset
	 *            same as insert
	 * @param currentPosition
	 *            current position of the search process
	 * @param text
	 *            same as insert
	 * @return the new value of currentPosition, or -1 if insertion has been achieved.
	 */
	private int insertIntoParagraph(FlexoDocParagraph<D, TA> p, int offset, int currentPosition, String text) {

		for (FlexoDocRun<D, TA> r : new ArrayList<>(p.getRuns())) {
			System.out.println("Examen d'un run");
			if (r instanceof FlexoTextRun) {
				System.out.println("Le run est un TextRun");
				FlexoTextRun<D, TA> tr = (FlexoTextRun<D, TA>) r;
				// Let's check if the offset is within this run.
				int l = tr.getText().length();
				currentPosition += l;
				System.out.println("Position courante : " + currentPosition + ", position recherchee : " + offset);
				// If it is, this is our guy
				if (currentPosition >= offset) {
					System.out.println("La position a ete trouvee");
					// Insertion position within the text
					int insertionPosition = l - currentPosition + offset;
					// Let's split the text around this position
					String start = tr.getText().substring(0, insertionPosition);
					String end = tr.getText().substring(insertionPosition);
					// And then let's recompose it with the inserted text.
					tr.setText(start + text + end);
					System.out.println(tr.getText());
					return -1;
				}
				// If not, let's look at the next run.
			}
			else {
				// This is an ImgRun. Its length is 1.
				currentPosition++;
				System.out.println("apres l'image, la position est de " + currentPosition);
				if (currentPosition >= offset) {
					FlexoTextRun<D, TA> newRun = getDocumentFactory().makeTextRun(text);// new TextRun(s, "");
					p.addToRuns(newRun);
					return -1;
				}
			}
		}
		// End of paragraph = \n, thus one more character
		currentPosition++;
		return currentPosition;
	}

	private int setPropertyIntoParagraph(int start, int end, Property property, int currentPosition, FlexoDocParagraph<D, TA> p) {
		for (FlexoDocRun<D, TA> r : new ArrayList<>(p.getRuns())) {
			// TextRuns - ImgRuns will be handled later
			System.out.println("Examen d'un run");
			if (r instanceof FlexoTextRun) {
				System.out.println("Le run est un TextRun");
				FlexoTextRun<D, TA> tr = (FlexoTextRun<D, TA>) r;
				// Let's check if the offset is within this run.
				int l = tr.getText().length();
				currentPosition += l;
				System.out.println("Position courante : " + currentPosition + ", position recherch�e : " + start);
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
					FlexoDocStyle<D, TA> s = documentFactory.makeStyle();
					s.setBasedOn(tr.getStyle());
					FlexoTextRun<D, TA> newRun = getDocumentFactory().makeTextRun("");// new TextRun(s, "");
					newRun.setStyle(s);
					// newRun.getStyle().addProperty(property);
					// Is the rest less, more, or exactly what we want?
					// If less
					if (textEnd.length() < end - start + 1) {
						System.out.println("Il y aura d'autres runs a explorer");
						// All the rest of the text goes into our new run
						newRun.setText(textEnd);
						// And we insert it after tr
						p.addToRuns(newRun);
						// The exploration of the arborescence will go on.
						// The next run to be explored will be newRun.
						// We don't want to change it, so let's change our start position
						start += textEnd.length();
						System.out.println("nouvelle position de dapart : " + start);
					}
					// If exactly
					else if (textEnd.length() == end - start) {
						System.out.println("La modification va pile jusqu'a la fin du run");
						// All the rest of the text goes into our new run
						newRun.setText(textEnd);
						// And we insert it after tr
						p.insertRunAtIndex(newRun, tr.getIndex() + 1);
						// We're done! No other run is needed.
						return -1;
					}
					// If more
					else {
						System.out.println("La modification ne va pas tout a fait jusqu'a la fin du run");
						// Let's split the remaining text into two parts:
						// What must change, and what must remain the same.
						String changingText = textEnd.substring(0, end - start);
						System.out.println("Changing text : " + changingText);
						textEnd = textEnd.substring(end - start);
						// What must change goes into our new run
						newRun.setText(changingText);
						p.insertRunAtIndex(newRun, tr.getIndex() + 1);
						// The rest goes into a run similar to tr
						FlexoTextRun<D, TA> lastRun = getDocumentFactory().makeTextRun(textEnd);// new TextRun(tr.getStyle(), textEnd);
						newRun.setStyle(tr.getStyle());
						p.insertRunAtIndex(lastRun, newRun.getIndex() + 1);
						// We're done!
						return -1;
					}
				}
				// If not, let's look at the next run.
			}
			else if (r instanceof FlexoDrawingRun) {
				System.out.println("Le run est un ImgRun");
				FlexoDrawingRun<D, TA> ir = (FlexoDrawingRun<D, TA>) r;
				currentPosition++;
				if (currentPosition == start) {
					// ir.getStyle().addProperty(property);
					if (end == start)
						return -1;
					start++;
				}
			}
		}
		// End of paragraph = \n, thus one more character
		currentPosition++;
		return currentPosition;
	}

	private int deleteIntoParagraph(int currentPosition, int offset, int length, FlexoDocParagraph<D, TA> p) {

		for (FlexoDocRun<D, TA> r : new ArrayList<>(p.getRuns())) {
			// TextRuns - ImgRuns will be handled later
			System.out.println("Examen d'un run");
			if (r instanceof FlexoTextRun) {
				System.out.println("Le run est un TextRun");
				FlexoTextRun<D, TA> tr = (FlexoTextRun<D, TA>) r;
				// Let's check if the offset is within this run.
				int l = tr.getText().length();
				currentPosition += l;
				System.out.println("Position courante : " + currentPosition + ", position recherch�e : " + offset);
				// If it is, this is our guy
				if (currentPosition >= offset) {
					System.out.println("La position de depart a ete trouvee");
					// Start position within the text
					int startPosition = l - currentPosition + offset;
					// Let's split the text around this position
					String textStart = tr.getText().substring(0, startPosition);
					String textEnd = tr.getText().substring(startPosition);
					// Is the rest less, more, or exactly what we want?
					// If less
					if (textEnd.length() < length) {
						length -= textEnd.length();
						System.out.println("Il y aura d'autres runs a explorer");
						// All the rest of the text is deleted
						// We just keep textStart
						tr.setText(textStart);
						// The exploration of the arborescence will go on.
						// The next run to be explored will be newRun.
						// We don't want to change it, so let's change our start position
						offset += textEnd.length();
					}
					// If exactly
					else if (textEnd.length() == length) {
						System.out.println("La modification va pile jusqu'a la fin du run");
						// All the rest of the text is deleted
						// We just keep textStart
						tr.setText(textStart);
						// We're done.
						return -1;
					}
					// If more
					else {
						System.out.println("La modification ne va pas tout � fait jusqu'� la fin du run");
						// Let's split the remaining text into two parts:
						// What must be deleted, and what must be re-attached.
						textEnd = textEnd.substring(length);
						tr.setText(textStart + textEnd);
						// We're done!
						return -1;
					}
				}
				// If not, let's look at the next run.
			}
		}
		// End of paragraph = \n, thus one more character
		currentPosition++;
		return currentPosition;
	}

	private int insertElementIntoParagraph(FlexoDocElement<D, TA> e, int position, int currentPosition, FlexoDocParagraph<D, TA> p) {
		System.out.println("insertion d'un element");

		int addPosition = p.getIndex() + 1;
		FlexoDocParagraph<D, TA> newParagraph = null; // getDocumentFactory().makeParagraph();// TODO : g�rer le style

		for (FlexoDocRun<D, TA> r : new ArrayList<>(p.getRuns())) {

			if (currentPosition == -1) {
				newParagraph.addToRuns(r);
				// i.remove();
				System.out.println("run retire");
			}
			else {
				if (r instanceof FlexoTextRun) {
					System.out.println("Le run est un TextRun");
					FlexoTextRun<D, TA> tr = (FlexoTextRun<D, TA>) r;
					// Let's check if the offset is within this run.
					int l = tr.getText().length();
					currentPosition += l;
					System.out.println("Position courante : " + currentPosition + ", position recherch�e : " + position);
					// If it is, this is our guy
					if (currentPosition >= position) {
						System.out.println("La position de depart a ete trouvee");
						// Start position within the text
						int startPosition = l - currentPosition + position;
						// Let's split the text around this position
						String textStart = tr.getText().substring(0, startPosition);
						String textEnd = tr.getText().substring(startPosition);
						// The current run will only keep the beginning of the text.
						tr.setText(textStart);
						// Ok, now let's create a run that has our new property, plus the properties of tr.
						FlexoDocStyle<D, TA> s = documentFactory.makeStyle();
						s.setBasedOn(tr.getStyle());
						FlexoTextRun<D, TA> newRun = getDocumentFactory().makeTextRun(textEnd);
						newRun.setStyle(s);
						newParagraph.addToRuns(newRun);
						System.out.println("Dans le nouveau paragraphe, j'ajoute " + textEnd);
						// We're done! No other run is needed.
						currentPosition = -1;
					}
					// If not, let's look at the next run.
				}
				else if (r instanceof FlexoDrawingRun) {
					System.out.println("Le run est un ImgRun");
					FlexoDrawingRun<D, TA> ir = (FlexoDrawingRun<D, TA>) r;
					currentPosition++;
					if (currentPosition == position) {
						currentPosition = -1;
					}
				}
				System.out.println("pos" + currentPosition);
			}
		}
		if (currentPosition == -1) {
			System.out.println("ajout de l'element");
			flexoDocument.insertElementAtIndex(newParagraph, addPosition);
			flexoDocument.insertElementAtIndex(e, addPosition + 1);
			return -1;
		}
		// End of paragraph = \n, thus one more character
		currentPosition++;
		return currentPosition;
	}

}

class EditorListener implements DocumentListener {

	private Editor e;

	public EditorListener(Editor e) {
		this.e = e;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		int length = arg0.getLength();
		System.out.println("Une insertion de " + length + "caracteres a ete effectuee en position " + offset);
		Element body = e.getDocument().getDefaultRootElement().getElement(1);
		Element paragraph = body.getElement(0);
		if (offset != 0) {
			String insertedText;
			try {
				insertedText = e.getText(offset, length);
				// e.document.insert(insertedText, offset - length);
				System.out.println("On doit inserer " + insertedText + " offset=" + offset + " length=" + length);
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {

	}

}
