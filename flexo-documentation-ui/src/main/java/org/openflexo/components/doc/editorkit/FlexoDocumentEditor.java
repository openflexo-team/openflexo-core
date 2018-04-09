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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import org.openflexo.FlexoCst;
import org.openflexo.components.doc.editorkit.element.AbstractDocumentElement;
import org.openflexo.components.doc.editorkit.element.ParagraphElement;
import org.openflexo.components.doc.editorkit.element.RunElement;
import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocFragment.FragmentConsistencyException;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.doc.TextSelection.TextMarker;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A Wysiwyg editor for {@link FlexoDocument} API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocumentEditor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		implements HasPropertyChangeSupport, DocumentListener, CaretListener {

	private static final Logger logger = Logger.getLogger(FlexoDocumentEditor.class.getPackage().getName());

	protected D flexoDocument;
	private DocumentFactory<D, TA> documentFactory;
	private Highlighter highlighter;
	private DefaultHighlightPainter highlighterPainter;

	protected JEditorPane jEditorPane;
	protected FlexoDocumentEditorPanel editorPanel;

	private PropertyChangeSupport pcSupport;

	private boolean showToolbar = true;

	public static final FlexoDocumentEditorKit FlexoDocumentEditorKit = new FlexoDocumentEditorKit();

	/**
	 * Creates an empty FlexoDocumentEditor.
	 */
	public FlexoDocumentEditor(DocumentFactory<D, TA> documentFactory) {
		super();
		pcSupport = new PropertyChangeSupport(this);
		jEditorPane = new JEditorPane("text/rtf", "");
		// FlexoDocumentEditorKit.install(jEditorPane);
		jEditorPane.setEditorKit(new FlexoDocumentEditorKit());
		editorPanel = new FlexoDocumentEditorPanel();
		this.documentFactory = documentFactory;
		highlighter = jEditorPane.getHighlighter();

		System.out.println("highlighter=" + highlighter);

		highlighterPainter = new DefaultHighlightPainter(Color.GREEN);
	}

	/**
	 * Creates an FlexoDocumentEditor with a Document to be displayed and edited.
	 * 
	 * @param flexoDocument
	 *            the flexoDocument.
	 */
	public FlexoDocumentEditor(D flexoDocument) {
		this(flexoDocument != null ? flexoDocument.getFactory() : null);
		setFlexoDocument(flexoDocument);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public D getFlexoDocument() {
		return flexoDocument;
	}

	public void setFlexoDocument(D flexoDocument) {
		if (flexoDocument != this.flexoDocument) {
			FlexoDocument<D, TA> oldValue = this.flexoDocument;
			this.flexoDocument = flexoDocument;
			FlexoDocumentEditorFactory<D, TA> reader;
			try {
				reader = new FlexoDocumentEditorFactory<>(flexoDocument);
				jEditorPane.setDocument(reader.getDocument());
				jEditorPane.getDocument().addDocumentListener(this);
				jEditorPane.addCaretListener(this);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			getPropertyChangeSupport().firePropertyChange("flexoDocument", oldValue, flexoDocument);
		}
	}

	public DocumentFactory<D, TA> getDocumentFactory() {
		if (getFlexoDocument() != null) {
			return getFlexoDocument().getFactory();
		}
		return documentFactory;
	}

	public JEditorPane getJEditorPane() {
		return jEditorPane;
	}

	public FlexoDocumentEditorPanel getEditorPanel() {
		return editorPanel;
	}

	@SuppressWarnings("unchecked")
	public FlexoStyledDocument<D, TA> getStyledDocument() {
		return (FlexoStyledDocument<D, TA>) jEditorPane.getDocument();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		int length = arg0.getLength();
		System.out.println("Une insertion de " + length + "caracteres a ete effectuee en position " + offset);
		// Element body = jEditorPane.getDocument().getDefaultRootElement().getElement(1);
		// Element paragraph = body.getElement(0);
		if (offset != 0) {
			String insertedText;
			try {
				insertedText = jEditorPane.getText(offset, length);
				// e.document.insert(insertedText, offset - length);
				System.out.println("On doit inserer " + insertedText + " offset=" + offset + " length=" + length);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {

	}

	public boolean getShowToolbar() {
		return showToolbar;
	}

	public void setShowToolbar(boolean showToolbar) {
		if (showToolbar != getShowToolbar()) {
			this.showToolbar = showToolbar;
			// getPropertyChangeSupport().firePropertyChange("showHeader", !showHeader, showHeader);
			getEditorPanel().toolBar.setVisible(showToolbar);
		}
	}

	public class FlexoDocumentEditorPanel extends JPanel {

		private FlexoDocumentToolbar toolBar;
		private JPanel infoBar;
		private JLabel writableLabel;
		private JLabel modeLabel;
		private JLabel textSelectionLabel;
		private JLabel textPositionLabel;

		private FlexoDocumentEditorPanel() {
			this.toolBar = new FlexoDocumentToolbar(FlexoDocumentEditor.this, null);
			// Let's create the buttons
			this.setLayout(new BorderLayout());
			this.add(toolBar, BorderLayout.NORTH);
			this.add(new JScrollPane(jEditorPane), BorderLayout.CENTER);
			infoBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			writableLabel = new JLabel("Writable");
			writableLabel.setFont(FlexoCst.SMALL_FONT);
			modeLabel = new JLabel("Smart Insert");
			modeLabel.setFont(FlexoCst.SMALL_FONT);
			textSelectionLabel = new JLabel();
			textSelectionLabel.setFont(FlexoCst.SMALL_FONT);
			textPositionLabel = new JLabel();
			textPositionLabel.setFont(FlexoCst.SMALL_FONT);
			infoBar.add(writableLabel);
			infoBar.add(new JLabel("|"));
			infoBar.add(modeLabel);
			infoBar.add(new JLabel("|"));
			infoBar.add(textPositionLabel);
			infoBar.add(textSelectionLabel);
			this.add(infoBar, BorderLayout.SOUTH);
			infoBar.setOpaque(false);

			toolBar.setVisible(getShowToolbar());
		}

	}

	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {
		// System.out.println("On cherche " + docObject);
		AbstractDocumentElement<O, D, TA> returned = getStyledDocument().getRootElement().getElement(docObject);
		// System.out.println("On trouve " + returned);
		return returned;
	}

	public <O extends FlexoDocObject<D, TA>> boolean scrollTo(O docObject, boolean setCaretPosition) {
		AbstractDocumentElement<O, D, TA> element = getStyledDocument().getRootElement().getElement(docObject);
		return scrollToElement(element, setCaretPosition);
	}

	public boolean scrollToElement(AbstractDocumentElement<?, ?, ?> element) {
		return scrollToElement(element, true);
	}

	public boolean scrollToElement(AbstractDocumentElement<?, ?, ?> element, boolean setCaretPosition) {
		if (element == null) {
			return false;
		}
		try {
			int pos = element.getStartOffset();
			Rectangle r = jEditorPane.modelToView(pos);
			if (r != null) {
				// the view is visible, scroll it to the
				// center of the current visible area.
				Rectangle vis = jEditorPane.getVisibleRect();
				// r.y -= (vis.height / 2);
				r.height = vis.height;

				if (vis.contains(new Point(r.x, r.y))) {
					// We are already inside
				}
				else {
					jEditorPane.scrollRectToVisible(r);
					if (setCaretPosition) {
						jEditorPane.setCaretPosition(pos);
					}
				}
				return true;
			}
			return false;
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return true;
		}
	}

	public void highlight(int startIndex, int endIndex) {
		try {
			highlighter.addHighlight(startIndex, endIndex, highlighterPainter);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void clearHighligths() {
		highlighter.removeAllHighlights();
	}

	public void highlight(FlexoDocObject<D, TA> docObject) {
		clearHighligths();
		AbstractDocumentElement<?, D, TA> docElement = getElement(docObject);
		if (docElement != null) {
			try {
				// System.out.println("Highlight " + docElement.getStartOffset() + " : " + docElement.getEndOffset());
				highlighter.addHighlight(docElement.getStartOffset(), docElement.getEndOffset(), highlighterPainter);
				scrollToElement(docElement);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		else {
			logger.warning("Could not find AbstractDocumentElement for " + docObject);
		}
	}

	public void highlightObjects(List<? extends FlexoDocObject<D, TA>> objects) {
		// System.out.println("Highlighting " + objects);
		clearHighligths();
		for (FlexoDocObject<D, TA> o : objects) {
			AbstractDocumentElement<?, D, TA> docElement = getElement(o);
			if (docElement != null) {
				try {
					highlighter.addHighlight(docElement.getStartOffset(), docElement.getEndOffset(), highlighterPainter);
					scrollToElement(docElement);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void highlight(TextSelection<D, TA> highlightedTextSelection) {
		clearHighligths();

		int startIndex = -1;
		int endIndex = -1;

		// System.out.println("Highlighting " + highlightedTextSelection);
		// System.out.println("startRun=" + highlightedTextSelection.getStartRun());
		// System.out.println("startElement=" + highlightedTextSelection.getStartElement());

		if (highlightedTextSelection.getStartRun() != null) {
			AbstractDocumentElement<?, D, TA> startRunElement = getElement(highlightedTextSelection.getStartRun());
			// System.out.println("startRunElement=" + startRunElement);
			// System.out.println("endCharIndex=" + highlightedTextSelection.getStartCharacterIndex());
			if (startRunElement != null) {
				if (highlightedTextSelection.getStartCharacterIndex() > -1) {
					startIndex = startRunElement.getStartOffset() + highlightedTextSelection.getStartCharacterIndex();
				}
				else {
					startIndex = startRunElement.getStartOffset();
				}
			}
		}
		else if (highlightedTextSelection.getStartElement() != null) {
			AbstractDocumentElement<?, D, TA> startElement = getElement(highlightedTextSelection.getStartElement());
			// System.out.println("startElement=" + startElement);
			if (startElement != null) {
				startIndex = startElement.getStartOffset();
			}
		}
		if (highlightedTextSelection.getEndRun() != null) {
			AbstractDocumentElement<?, D, TA> endRunElement = getElement(highlightedTextSelection.getEndRun());
			// System.out.println("endRunElement=" + endRunElement);
			// System.out.println("endCharIndex=" + highlightedTextSelection.getEndCharacterIndex());
			if (endRunElement != null) {
				if (highlightedTextSelection.getEndCharacterIndex() > -1) {
					endIndex = endRunElement.getStartOffset() + highlightedTextSelection.getEndCharacterIndex();
				}
				else {
					endIndex = endRunElement.getEndOffset();
				}
			}
		}
		else if (highlightedTextSelection.getEndElement() != null) {
			AbstractDocumentElement<?, D, TA> endElement = getElement(highlightedTextSelection.getEndElement());
			if (endElement != null) {
				endIndex = endElement.getEndOffset();
			}
		}

		// System.out.println("start=" + startIndex);
		// System.out.println("end=" + endIndex);
		if (startIndex > -1 && endIndex > -1) {
			highlight(startIndex, endIndex);
			scrollToElement(getElement(highlightedTextSelection.getStartElement()));
		}
	}

	@Override
	public void caretUpdate(CaretEvent evt) {

		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					caretUpdate(evt);
				}
			});
			return;
		}

		System.out.println("Caret changed with " + evt);
		int start = Math.min(evt.getDot(), evt.getMark());
		int end = Math.max(evt.getDot(), evt.getMark());
		System.out.println("Selection: " + start + ":" + end);
		System.out.println("CharacterElement: " + getStyledDocument().getCharacterElement(start));
		System.out.println("ParagraphElement: " + getStyledDocument().getParagraphElement(start));

		AttributeSet as = getStyledDocument().getCharacterElement(start).getAttributes();
		System.out.println("Les attributs: " + as);
		Enumeration<?> en = as.getAttributeNames();
		while (en.hasMoreElements()) {
			Object attribute = en.nextElement();
			System.out.println(" > " + attribute + "=" + as.getAttribute(attribute));
		}

		getEditorPanel().textPositionLabel.setText("" + start);

		if (start < end) {
			try {
				TextMarker startMarker = retrieveTextMarker(start);
				TextMarker endMarker = retrieveTextMarker(end);
				/*System.out.println("startMarker=" + startMarker);
				System.out.println("endMarker=" + endMarker);
				System.out.println("docFactory=" + getDocumentFactory());
				System.out.println("je suis " + this);
				System.out.println("getFlexoDocument()=" + getFlexoDocument());*/
				textSelection = getDocumentFactory().makeTextSelection(startMarker, endMarker);

				// System.out.println("TextSelection=" + textSelection);
				getEditorPanel().textSelectionLabel.setText(textSelection.toString());
				getPropertyChangeSupport().firePropertyChange("textSelection", null, textSelection);
			} catch (FragmentConsistencyException e1) {
				e1.printStackTrace();
			}
		}
		else {
			textSelection = null;
			getEditorPanel().textSelectionLabel.setText("");
			getPropertyChangeSupport().firePropertyChange("textSelection", true, false);
		}
	}

	private TextSelection<D, TA> textSelection;

	public TextSelection<D, TA> getTextSelection() {
		return textSelection;
	}

	protected TextMarker retrieveTextMarker(int pos) {

		Element paragraphElement = getStyledDocument().getParagraphElement(pos);
		Element charElement = getStyledDocument().getCharacterElement(pos);

		TextMarker returned = new TextMarker();

		System.out.println("RetrieveTextMarker for pos=" + pos);
		System.out.println("paragraphElement=" + paragraphElement);
		System.out.println("charElement=" + charElement);

		if (paragraphElement instanceof ParagraphElement) {
			returned.documentElement = ((ParagraphElement) paragraphElement).getParagraph();
		}

		if (charElement instanceof RunElement) {
			FlexoDocRun<D, TA> run = ((RunElement) charElement).getRun();
			if (run != null) {
				if (returned.documentElement == null) {
					returned.documentElement = run.getParagraph();
				}
				int runIndex = run.getIndex();
				int characterIndex = pos - charElement.getStartOffset();
				returned.runIndex = runIndex;
				returned.characterIndex = characterIndex;
				if (characterIndex == 0) {
					returned.firstChar = true;
				}
				if (pos == paragraphElement.getEndOffset() - 1) {
					returned.lastChar = true;
				}
				if (runIndex == 0) {
					returned.firstRun = true;
				}
				if (runIndex == run.getParagraph().getRuns().size() - 1) {
					returned.lastRun = true;
				}
			}
		}

		System.out.println("Returning " + returned);

		/*WordMLDocument doc = editorView.getDocument();
		
		TextMarker returned = new TextMarker();
		
		// System.out.println("pos=" + pos);
		Element characterElement = doc.getCharacterElement(pos);
		Element paragraphElement = doc.getParagraphElement(pos);
		// System.out.println("characterElement=" + characterElement);
		// System.out.println("paragraphElement=" + paragraphElement);
		if (characterElement instanceof DocumentElement) {
			ElementML elementML = ((DocumentElement) characterElement).getElementML();
			// System.out.println("elementML=" + elementML);
			Object docXObject = elementML.getDocxObject();
			// System.out.println("docXObject=" + docXObject);
			if (docXObject instanceof JAXBElement) {
				docXObject = ((JAXBElement) docXObject).getValue();
			}
			// System.out.println("startDocXObject=" + docXObject);
			if (docXObject instanceof P) {
				returned.documentElement = getDocXDocument().getParagraph((P) docXObject);
			}
			else if (docXObject instanceof R) {
				Object parent = ((R) docXObject).getParent();
				if (parent instanceof P) {
					returned.documentElement = getDocXDocument().getParagraph((P) parent);
				}
		
				if (returned.documentElement != null) {
					DocXRun docXRun = ((DocXParagraph) returned.documentElement).getRun((R) docXObject);
					int runIndex = docXRun.getIndex();
					int characterIndex = pos - paragraphElement.getStartOffset();
					returned.runIndex = runIndex;
					returned.characterIndex = characterIndex;
					if (characterIndex == 0) {
						returned.firstChar = true;
					}
					if (pos == paragraphElement.getEndOffset() - 1) {
						returned.lastChar = true;
					}
					if (runIndex == 0) {
						returned.firstRun = true;
					}
					if (runIndex == ((DocXParagraph) returned.documentElement).getRuns().size() - 1) {
						returned.lastRun = true;
					}
				}
		
			}
			else if (docXObject instanceof Tbl) {
				returned.documentElement = getDocXDocument().getTable((Tbl) docXObject);
			}
			else if (docXObject != null) { // Whatever it is go that way...
				R run = (R) ((Child) docXObject).getParent();
				// System.out.println("run=" + run);
				if (run.getParent() instanceof P) {
					P p = (P) run.getParent();
					returned.documentElement = getDocXDocument().getParagraph(p);
				}
		
				// XTOF: NPE protection
				if (returned.documentElement != null) {
					DocXRun docXRun = ((DocXParagraph) returned.documentElement).getRun(run);
					if (docXRun != null) {
						int runIndex = docXRun.getIndex();
						// System.out.println("runIndex=" + runIndex);
						int characterIndex = pos - paragraphElement.getStartOffset();
						// System.out.println("characterIndex=" + characterIndex);
		
						returned.runIndex = runIndex;
						returned.characterIndex = characterIndex;
						if (characterIndex == 0) {
							returned.firstChar = true;
						}
						if (pos == paragraphElement.getEndOffset() - 1) {
							returned.lastChar = true;
						}
						if (runIndex == 0) {
							returned.firstRun = true;
						}
						if (runIndex == ((DocXParagraph) returned.documentElement).getRuns().size() - 1) {
							returned.lastRun = true;
						}
					}
					else {
						logger.warning("Could not Retreive TextMarker @" + pos + " NO RUN FOUND");
					}
				}
			}
			// System.out.println("returned.documentElement=" + returned.documentElement);
		}*/

		return returned;

	}

}
