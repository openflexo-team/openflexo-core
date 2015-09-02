/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
 * developed at Openflexo.
 * 
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

package org.openflexo.foundation.doc;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.doc.FlexoDocumentFragment.FragmentConsistencyException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * This class represent a text selection in a document fragment.<br>
 * 
 * Because of the structure of a {@link FlexoDocument}, a selection might be seen as a consecutive list of runs to consider, with eventually
 * a begin index for the first run and a end index for the last run.<br>
 * A {@link TextSelection} might be defined in a single paragraph (start and end element are then the same), or in multiple consecutive
 * paragraphs.
 * 
 * More formally, a {@link TextSelection} is identified by:
 * <ul>
 * <li>start document element</li>
 * <li>start run index (in the first document element)</li>
 * <li>start character index (in the start run)</li>
 * <li>end document element</li>
 * <li>end run index (in the last document element)</li>
 * <li>end character index (in the start run)</li>
 * </ul>
 * A {@link TextSelection} MUST reference a valid {@link FlexoDocumentFragment} containing the text selection
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(TextSelection.TextSelectionImpl.class)
@XMLElement
public interface TextSelection<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoObject {

	@PropertyIdentifier(type = FlexoDocumentFragment.class)
	public static final String FRAGMENT_KEY = "fragment";

	@PropertyIdentifier(type = FlexoDocumentElement.class)
	public static final String START_ELEMENT_KEY = "startElement";
	@PropertyIdentifier(type = String.class)
	public static final String START_ELEMENT_IDENTIFIER_KEY = "startElementId";
	@PropertyIdentifier(type = FlexoDocumentElement.class)
	public static final String END_ELEMENT_KEY = "endElement";
	@PropertyIdentifier(type = String.class)
	public static final String END_ELEMENT_IDENTIFIER_KEY = "endElementId";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_RUN_INDEX_KEY = "startRunId";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_RUN_INDEX_KEY = "endRunId";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_CHARACTER_INDEX_KEY = "startCharId";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_CHARACTER_INDEX_KEY = "endCharId";

	/**
	 * Return the fragment in which this {@link TextSelection} is valid
	 * 
	 * @return
	 */
	@Getter(FRAGMENT_KEY)
	public FlexoDocumentFragment<D, TA> getFragment();

	@Setter(FRAGMENT_KEY)
	public void setFragment(FlexoDocumentFragment<D, TA> fragment);

	@Getter(START_ELEMENT_KEY)
	public FlexoDocumentElement<D, TA> getStartElement();

	@Setter(START_ELEMENT_KEY)
	public void setStartElement(FlexoDocumentElement<D, TA> startElement);

	@Getter(START_ELEMENT_IDENTIFIER_KEY)
	@XMLAttribute
	public String getStartElementIdentifier();

	@Setter(START_ELEMENT_IDENTIFIER_KEY)
	public void setStartElementIdentifier(String startElementIdentifier);

	@Getter(END_ELEMENT_KEY)
	public FlexoDocumentElement<D, TA> getEndElement();

	@Setter(END_ELEMENT_KEY)
	public void setEndElement(FlexoDocumentElement<D, TA> endElement);

	@Getter(END_ELEMENT_IDENTIFIER_KEY)
	@XMLAttribute
	public String getEndElementIdentifier();

	@Setter(END_ELEMENT_IDENTIFIER_KEY)
	public void setEndElementIdentifier(String endElementIdentifier);

	@Getter(value = START_RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getStartRunIndex();

	@Setter(START_RUN_INDEX_KEY)
	public void setStartRunIndex(int startRunIndex);

	@Getter(value = END_RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getEndRunIndex();

	@Setter(END_RUN_INDEX_KEY)
	public void setEndRunIndex(int endRunIndex);

	@Getter(value = START_CHARACTER_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getStartCharacterIndex();

	@Setter(START_CHARACTER_INDEX_KEY)
	public void setStartCharacterIndex(int startRunIndex);

	@Getter(value = END_CHARACTER_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getEndCharacterIndex();

	@Setter(END_CHARACTER_INDEX_KEY)
	public void setEndCharacterIndex(int endRunIndex);

	/**
	 * Return {@link FlexoRun} where first character of this {@link TextSelection} is defined
	 * 
	 * @return
	 */
	public FlexoRun<D, TA> getStartRun();

	/**
	 * Return {@link FlexoRun} where last character of this {@link TextSelection} is defined
	 * 
	 * @return
	 */
	public FlexoRun<D, TA> getEndRun();

	/**
	 * Return string raw representation of text beeing selected<br>
	 * Returned text is build accross document structure reflected in the fragment
	 * 
	 * @return
	 */
	public String getRawText();

	/**
	 * Indicates if this {@link TextSelection} concerns a single paragraph
	 * 
	 * @return
	 */
	public boolean isSingleParagraph();

	/**
	 * Indicates if this {@link TextSelection} concerns a single run in a single paragraph
	 * 
	 * @return
	 */
	public boolean isSingleRun();

	public static abstract class TextSelectionImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoObjectImpl
			implements TextSelection<D, TA> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TextSelection.class.getPackage().getName());

		private FlexoDocumentElement<D, TA> startElement = null;
		private String startElementIdentifier = null;
		private FlexoDocumentElement<D, TA> endElement = null;
		private String endElementIdentifier = null;

		@Override
		public String getStartElementIdentifier() {
			if (getStartElement() != null) {
				return getStartElement().getIdentifier();
			}
			return startElementIdentifier;
		}

		@Override
		public void setStartElementIdentifier(String startElementIdentifier) {

			if ((startElementIdentifier == null && this.startElementIdentifier != null)
					|| (startElementIdentifier != null && !startElementIdentifier.equals(this.startElementIdentifier))) {
				FlexoDocumentElement<D, TA> oldStartElement = getStartElement();
				String oldStartElementId = getStartElementIdentifier();
				this.startElementIdentifier = startElementIdentifier;
				this.startElement = null;
				getPropertyChangeSupport().firePropertyChange(START_ELEMENT_IDENTIFIER_KEY, oldStartElementId, startElementIdentifier);
				getPropertyChangeSupport().firePropertyChange(START_ELEMENT_KEY, oldStartElement, getStartElement());
			}
		}

		@Override
		public FlexoDocumentElement<D, TA> getStartElement() {
			if (startElement == null && startElementIdentifier != null && getFragment() != null) {
				startElement = getFragment().getFlexoDocument().getElementWithIdentifier(startElementIdentifier);
			}
			return startElement;
		}

		@Override
		public void setStartElement(FlexoDocumentElement<D, TA> startElement) {
			if (startElement != this.startElement) {
				FlexoDocumentElement<D, TA> oldValue = this.startElement;
				this.startElement = startElement;
				getPropertyChangeSupport().firePropertyChange(START_ELEMENT_KEY, oldValue, startElement);
				getPropertyChangeSupport().firePropertyChange(START_ELEMENT_IDENTIFIER_KEY,
						oldValue != null ? oldValue.getIdentifier() : null, getStartElementIdentifier());
			}
		}

		@Override
		public String getEndElementIdentifier() {
			if (getEndElement() != null) {
				return getEndElement().getIdentifier();
			}
			return endElementIdentifier;
		}

		@Override
		public void setEndElementIdentifier(String endElementIdentifier) {

			if ((endElementIdentifier == null && this.endElementIdentifier != null)
					|| (endElementIdentifier != null && !endElementIdentifier.equals(this.endElementIdentifier))) {
				FlexoDocumentElement<D, TA> oldEndElement = getEndElement();
				String oldEndElementId = getEndElementIdentifier();
				this.endElementIdentifier = endElementIdentifier;
				this.endElement = null;
				getPropertyChangeSupport().firePropertyChange(END_ELEMENT_IDENTIFIER_KEY, oldEndElementId, endElementIdentifier);
				getPropertyChangeSupport().firePropertyChange(END_ELEMENT_KEY, oldEndElement, getEndElement());
			}
		}

		@Override
		public FlexoDocumentElement<D, TA> getEndElement() {
			if (endElement == null && endElementIdentifier != null && getFragment() != null) {
				endElement = getFragment().getFlexoDocument().getElementWithIdentifier(endElementIdentifier);
			}
			return endElement;
		}

		@Override
		public void setEndElement(FlexoDocumentElement<D, TA> endElement) {
			if (endElement != this.endElement) {
				FlexoDocumentElement<D, TA> oldValue = this.endElement;
				this.endElement = endElement;
				getPropertyChangeSupport().firePropertyChange(END_ELEMENT_KEY, oldValue, endElement);
				getPropertyChangeSupport().firePropertyChange(END_ELEMENT_IDENTIFIER_KEY,
						oldValue != null ? oldValue.getIdentifier() : null, getEndElementIdentifier());
			}
		}

		/**
		 * Return {@link FlexoRun} where first character of this {@link TextSelection} is defined
		 * 
		 * @return
		 */
		@Override
		public FlexoRun<D, TA> getStartRun() {
			if (getStartElement() instanceof FlexoParagraph
					&& (getStartRunIndex() < ((FlexoParagraph<D, TA>) getStartElement()).getRuns().size())) {
				return ((FlexoParagraph<D, TA>) getStartElement()).getRuns().get(getStartRunIndex());
			}
			return null;
		}

		/**
		 * Return {@link FlexoRun} where last character of this {@link TextSelection} is defined
		 * 
		 * @return
		 */
		@Override
		public FlexoRun<D, TA> getEndRun() {
			if (getEndElement() instanceof FlexoParagraph
					&& (getEndRunIndex() < ((FlexoParagraph<D, TA>) getEndElement()).getRuns().size())) {
				return ((FlexoParagraph<D, TA>) getEndElement()).getRuns().get(getEndRunIndex());
			}
			return null;
		}

		/**
		 * Indicates if this {@link TextSelection} concerns a single paragraph
		 * 
		 * @return
		 */
		@Override
		public boolean isSingleParagraph() {
			return getStartElement() == getEndElement();
		}

		/**
		 * Indicates if this {@link TextSelection} concerns a single run in a single paragraph
		 * 
		 * @return
		 */
		@Override
		public boolean isSingleRun() {
			return isSingleParagraph() && (getStartRun() == getEndRun());
		}

		/**
		 * Return string raw representation of text beeing selected<br>
		 * Returned text is build accross document structure reflected in the fragment
		 * 
		 * @return
		 */
		@Override
		public String getRawText() {
			if (getStartElement() != null && getEndElement() != null) {
				try {
					StringBuffer sb = new StringBuffer();
					FlexoDocumentFragment<D, TA> f = getStartElement().getFlexoDocument().getFragment(startElement, endElement);
					for (FlexoDocumentElement<D, TA> element : f.getElements()) {
						if (element instanceof FlexoParagraph) {
							FlexoParagraph<D, TA> paragraph = (FlexoParagraph<D, TA>) element;
							boolean isFirst = (paragraph == getStartElement());
							boolean isLast = (paragraph == getEndElement());
							boolean isUnique = isFirst && isLast;
							if (!isFirst) {
								sb.append(StringUtils.LINE_SEPARATOR);
							}
							if (isUnique) { // Unique paragraph
								if (getStartRunIndex() == getEndRunIndex()) {
									if (getStartCharacterIndex() > -1 && getEndCharacterIndex() > -1) {
										sb.append(getStartRun().getText().substring(getStartCharacterIndex(), getEndCharacterIndex()));
									}
									else if (getStartCharacterIndex() > -1) {
										sb.append(getStartRun().getText().substring(getStartCharacterIndex()));
									}
									else if (getEndCharacterIndex() > -1) {
										sb.append(getStartRun().getText().substring(0, getEndCharacterIndex()));
									}
									else {
										sb.append(getStartRun().getText());
									}
								}
								else {
									for (int i = getStartRunIndex(); i <= getEndRunIndex(); i++) {
										FlexoRun<D, TA> run = paragraph.getRuns().get(i);
										if (i == getStartRunIndex() && getStartCharacterIndex() > -1) {
											sb.append(run.getText().substring(getStartCharacterIndex()));
										}
										else if (i == getEndRunIndex() && getEndCharacterIndex() > -1) {
											sb.append(run.getText().substring(0, getEndCharacterIndex()));
										}
										else {
											sb.append(run.getText());
										}
									}
								}
							}
							else if (isFirst) { // First paragraph
								for (int i = getStartRunIndex(); i < paragraph.getRuns().size(); i++) {
									FlexoRun<D, TA> run = paragraph.getRuns().get(i);
									if (i == getStartRunIndex() && getStartCharacterIndex() > -1) {
										sb.append(run.getText().substring(getStartCharacterIndex()));
									}
									else {
										sb.append(run.getText());
									}
								}
							}
							else if (isLast) { // Last paragraph
								for (int i = 0; i <= getEndRunIndex(); i++) {
									FlexoRun<D, TA> run = paragraph.getRuns().get(i);
									if (i == getEndRunIndex() && getEndCharacterIndex() > -1) {
										sb.append(run.getText().substring(0, getEndCharacterIndex()));
									}
									else {
										sb.append(run.getText());
									}
								}
							}
							else { // Normal paragraph, fully included in the selection
								for (int i = 0; i < paragraph.getRuns().size(); i++) {
									FlexoRun<D, TA> run = paragraph.getRuns().get(i);
									sb.append(run.getText());
								}
							}
						}
					}
					// System.out.println("Returning " + sb.toString());
					return sb.toString();
				} catch (FragmentConsistencyException e) {
					logger.warning(e.getMessage());
					e.printStackTrace();
				}

			}
			return null;
		}
	}
}
