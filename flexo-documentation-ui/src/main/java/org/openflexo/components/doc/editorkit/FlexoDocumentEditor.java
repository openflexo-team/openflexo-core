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
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.openflexo.components.doc.editorkit.element.AbstractDocumentElement;
import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A Wysiwyg editor for {@link FlexoDocument} API
 * 
 * @author Sylvain Guerin
 *
 */
@SuppressWarnings("serial")
public class FlexoDocumentEditor<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		implements HasPropertyChangeSupport, DocumentListener, CaretListener {

	private D flexoDocument;
	private DocumentFactory<D, TA> documentFactory;
	private Highlighter highlighter;
	private DefaultHighlightPainter highlighterPainter;

	private JEditorPane jEditorPane;
	private FlexoDocumentEditorPanel editorPanel;

	private PropertyChangeSupport pcSupport;

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
		highlighterPainter = new DefaultHighlightPainter(Color.GREEN);
	}

	/**
	 * Creates an FlexoDocumentEditor with a Document to be displayed and edited.
	 * 
	 * @param flexoDocument
	 *            the flexoDocument.
	 */
	public FlexoDocumentEditor(D flexoDocument) {
		this(flexoDocument.getFactory());
		setFlexoDocument(flexoDocument);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	public void highlight(int startIndex, int endIndex) {
		try {
			highlighter.addHighlight(startIndex, endIndex, highlighterPainter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getPropertyChangeSupport().firePropertyChange("flexoDocument", oldValue, flexoDocument);
		}
	}

	public DocumentFactory<D, TA> getDocumentFactory() {
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {

	}

	public class FlexoDocumentEditorPanel extends JPanel {

		private FlexoDocumentToolbar toolBar;

		private FlexoDocumentEditorPanel() {
			this.toolBar = new FlexoDocumentToolbar(FlexoDocumentEditor.this, null);
			// Let's create the buttons
			this.setLayout(new BorderLayout());
			this.add(toolBar, BorderLayout.NORTH);
			this.add(new JScrollPane(jEditorPane), BorderLayout.CENTER);
		}

	}

	public <O extends FlexoDocObject<D, TA>> AbstractDocumentElement<O, D, TA> getElement(O docObject) {
		System.out.println("On cherche " + docObject);
		AbstractDocumentElement<O, D, TA> returned = getStyledDocument().getRootElement().getElement(docObject);
		System.out.println("On trouve " + returned);
		return returned;
	}

	public void setSelectedElements(List<AbstractDocumentElement<?, D, TA>> elts) {
		// TODO Auto-generated method stub
	}

	public boolean scrollToElement(AbstractDocumentElement<?, ?, ?> element) {
		return scrollToElement(element, true);
	}

	public boolean scrollToElement(AbstractDocumentElement<?, ?, ?> element, boolean setCaretPosition) {
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

	public void clearHighligths() {
		highlighter.removeAllHighlights();
	}

	public void highlight(FlexoDocObject<D, TA> docObject) {
		clearHighligths();
		AbstractDocumentElement<?, D, TA> docElement = getElement(docObject);
		if (docElement != null) {
			try {
				highlighter.addHighlight(docElement.getStartOffset(), docElement.getEndOffset(), highlighterPainter);
				scrollToElement(docElement);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void caretUpdate(CaretEvent evt) {
		System.out.println("Caret changed with " + evt);
		int start = Math.min(evt.getDot(), evt.getMark());
		int end = Math.max(evt.getDot(), evt.getMark());
		System.out.println("Selection: " + start + ":" + end);
		System.out.println("CharacterElement: " + getStyledDocument().getCharacterElement(start));
		System.out.println("ParagraphElement: " + getStyledDocument().getParagraphElement(start));

		AttributeSet as = getStyledDocument().getCharacterElement(start).getAttributes();
		System.out.println("Les attributs: " + as);
		Enumeration en = as.getAttributeNames();
		while (en.hasMoreElements()) {
			Object attribute = en.nextElement();
			System.out.println(" > " + attribute + "=" + as.getAttribute(attribute));
		}
	}
}
