/*
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.doc;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents a consecutive sequence of {@link FlexoDocElement} in a {@link FlexoDocument}
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocFragment.FlexoDocumentFragmentImpl.class)
@Imports({ @Import(TextSelection.class) })
public interface FlexoDocFragment<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String START_ELEMENT_KEY = "startElement";

	@PropertyIdentifier(type = String.class)
	public static final String END_ELEMENT_KEY = "endElement";

	/**
	 * Return start element in related {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(START_ELEMENT_KEY)
	public FlexoDocElement<D, TA> getStartElement();

	@Setter(START_ELEMENT_KEY)
	public void setStartElement(FlexoDocElement<D, TA> startElement);

	/**
	 * Return start element in related {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(END_ELEMENT_KEY)
	public FlexoDocElement<D, TA> getEndElement();

	@Setter(END_ELEMENT_KEY)
	public void setEndElement(FlexoDocElement<D, TA> endElement);

	/**
	 * Return the run as identified by runIdentifier, under the form: paraIndex.runIndex
	 * 
	 * @param runIdentifier
	 * @return
	 */
	public FlexoDocRun<?, ?> getRun(String runIdentifier);

	public void checkConsistency() throws FragmentConsistencyException;

	public class FragmentConsistencyException extends FlexoException {
		public FragmentConsistencyException(String message) {
			super(message);
		}
	}

	/**
	 * Return all elements this fragment is composed of
	 * 
	 * @return
	 */
	public List<? extends FlexoDocElement<D, TA>> getElements();

	public String getStringRepresentation();

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId, int startCharId,
			FlexoDocElement<D, TA> endElement, int endRunId, int endCharId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId,
			FlexoDocElement<D, TA> endElement, int endRunId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element, int startRunId, int startCharId, int endRunId,
			int endCharId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element, int startRunId, int endRunId)
			throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
			throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element) throws FragmentConsistencyException;

	public static abstract class FlexoDocumentFragmentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoDocFragment<D, TA> {

		private static final Logger logger = Logger.getLogger(FlexoDocumentFragmentImpl.class.getPackage().getName());

		@Override
		public List<? extends FlexoDocElement<D, TA>> getElements() {
			int startIndex = getStartElement().getContainer().getElements().indexOf(getStartElement());
			int endIndex = getStartElement().getContainer().getElements().indexOf(getEndElement());
			if (startIndex > -1 && endIndex >= startIndex) {
				return getStartElement().getContainer().getElements().subList(startIndex, endIndex + 1);
			}
			return Collections.emptyList();
		}

		/**
		 * Return the run as identified by runIdentifier, under the form: elementId.runIndex
		 * 
		 * @param runIdentifier
		 * @return
		 */
		@Override
		public FlexoDocRun<?, ?> getRun(String runIdentifier) {
			StringTokenizer st = new StringTokenizer(runIdentifier, ".");
			String elementId = null;
			if (st.hasMoreTokens()) {
				elementId = st.nextToken();
			}
			int runId = -1;
			if (st.hasMoreTokens()) {
				runId = Integer.parseInt(st.nextToken());
			}
			if (StringUtils.isNotEmpty(elementId)) {
				FlexoDocElement<?, ?> element = getFlexoDocument().getElementWithIdentifier(elementId);
				if (element instanceof FlexoDocParagraph) {
					FlexoDocParagraph<?, ?> para = (FlexoDocParagraph<?, ?>) element;
					if (runId > -1 && runId < para.getRuns().size()) {
						return para.getRuns().get(runId);
					}
				}
				else if (element != null) {
					logger.warning("!!! Not implemented: " + element.getClass());
				}
				else {
					logger.warning("!!! Cannot find element with id: " + elementId + " in " + getFlexoDocument());
					System.out.println(getFlexoDocument().debugStructuredContents());
				}
			}
			return null;
		}

		@Override
		public void checkConsistency() throws FragmentConsistencyException {
			if (getFlexoDocument() == null) {
				throw new FragmentConsistencyException("Undefined FlexoDocument");
			}
			if (getStartElement().getContainer() == null) {
				throw new FragmentConsistencyException("Undefined start element container");
			}
			if (getEndElement().getContainer() == null) {
				throw new FragmentConsistencyException("Undefined end element container");
			}
			if (getStartElement().getContainer() != getEndElement().getContainer()) {
				throw new FragmentConsistencyException("Inconsistent containers");
			}

			int startIndex = getStartElement().getContainer().getElements().indexOf(getStartElement());
			if (startIndex == -1) {
				throw new FragmentConsistencyException("Cannot find start index");
			}
			int endIndex = getStartElement().getContainer().getElements().indexOf(getEndElement());
			if (endIndex == -1) {
				throw new FragmentConsistencyException("Cannot find end index");
			}
			if (endIndex < startIndex) {
				throw new FragmentConsistencyException("Inconsistent fragment (reverse order)");
			}
			// Otherwise, that's ok
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FlexoDocFragment)) {
				return false;
			}
			FlexoDocFragment f2 = (FlexoDocFragment) obj;
			return getFlexoDocument().equals(f2.getFlexoDocument()) && getStartElement().equals(f2.getStartElement())
					&& getEndElement().equals(f2.getEndElement());
		}

		@Override
		public String getStringRepresentation() {
			return (getStartElement() instanceof FlexoDocParagraph ? ((FlexoDocParagraph) getStartElement()).getRawTextPreview()
					: (getStartElement() != null ? getStartElement().toString() : "?"))
					+ " : "
					+ (getStartElement() != getEndElement()
							? (getEndElement() instanceof FlexoDocParagraph ? ((FlexoDocParagraph) getEndElement()).getRawTextPreview()
									: (getEndElement() != null ? getEndElement().toString() : "?"))
							: "");
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId, int startCharId,
				FlexoDocElement<D, TA> endElement, int endRunId, int endCharId) throws FragmentConsistencyException {
			return getFlexoDocument().getFactory().makeTextSelection(this, startElement, startRunId, startCharId, endElement, endRunId,
					endCharId);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId,
				FlexoDocElement<D, TA> endElement, int endRunId) throws FragmentConsistencyException {
			return makeTextSelection(startElement, startRunId, -1, endElement, endRunId, -1);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element, int startRunId, int startCharId, int endRunId,
				int endCharId) throws FragmentConsistencyException {
			return makeTextSelection(element, startRunId, startCharId, element, endRunId, endCharId);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element, int startRunId, int endRunId)
				throws FragmentConsistencyException {
			return makeTextSelection(element, startRunId, -1, element, endRunId, -1);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
				throws FragmentConsistencyException {
			return makeTextSelection(startElement, -1, -1, endElement, -1, -1);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> element) throws FragmentConsistencyException {
			return makeTextSelection(element, element);
		}

	}

}
