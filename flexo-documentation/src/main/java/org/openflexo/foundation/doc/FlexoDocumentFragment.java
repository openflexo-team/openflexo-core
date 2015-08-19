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
 * Represents a consecutive sequence of {@link FlexoDocumentElement} in a {@link FlexoDocument}
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocumentFragment.FlexoDocumentFragmentImpl.class)
@Imports({ @Import(TextSelection.class) })
public interface FlexoDocumentFragment<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

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
	public FlexoDocumentElement<D, TA> getStartElement();

	@Setter(START_ELEMENT_KEY)
	public void setStartElement(FlexoDocumentElement<D, TA> startElement);

	/**
	 * Return start element in related {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(END_ELEMENT_KEY)
	public FlexoDocumentElement<D, TA> getEndElement();

	@Setter(END_ELEMENT_KEY)
	public void setEndElement(FlexoDocumentElement<D, TA> endElement);

	/**
	 * Return the run as identified by runIdentifier, under the form: paraIndex.runIndex
	 * 
	 * @param runIdentifier
	 * @return
	 */
	public FlexoRun<?, ?> getRun(String runIdentifier);

	public void checkConsistency() throws FragmentConsistencyException;

	public class FragmentConsistencyException extends FlexoException {
		public FragmentConsistencyException() {
		}
	}

	/**
	 * Return all elements this fragment is composed of
	 * 
	 * @return
	 */
	public List<? extends FlexoDocumentElement<D, TA>> getElements();

	public String getStringRepresentation();

	public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> startElement, int startRunId, int startCharId,
			FlexoDocumentElement<D, TA> endElement, int endRunId, int endCharId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> startElement, int startRunId,
			FlexoDocumentElement<D, TA> endElement, int endRunId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> element, int startRunId, int startCharId, int endRunId,
			int endCharId) throws FragmentConsistencyException;

	public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> element, int startRunId, int endRunId)
			throws FragmentConsistencyException;

	public static abstract class FlexoDocumentFragmentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoDocumentFragment<D, TA> {

		private static final Logger logger = Logger.getLogger(FlexoDocumentFragmentImpl.class.getPackage().getName());

		@Override
		public List<? extends FlexoDocumentElement<D, TA>> getElements() {
			int startIndex = getFlexoDocument().getElements().indexOf(getStartElement());
			int endIndex = getFlexoDocument().getElements().indexOf(getEndElement());
			return getFlexoDocument().getElements().subList(startIndex, endIndex + 1);
		}

		/**
		 * Return the run as identified by runIdentifier, under the form: elementId.runIndex
		 * 
		 * @param runIdentifier
		 * @return
		 */
		@Override
		public FlexoRun<?, ?> getRun(String runIdentifier) {
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
				FlexoDocumentElement<?, ?> element = getFlexoDocument().getElementWithIdentifier(elementId);
				if (element instanceof FlexoParagraph) {
					FlexoParagraph<?, ?> para = (FlexoParagraph<?, ?>) element;
					if (runId > -1 && runId < para.getRuns().size()) {
						return para.getRuns().get(runId);
					}
				} else if (element != null) {
					logger.warning("!!! Not implemented: " + element.getClass());
				} else {
					logger.warning("!!! Cannot find element with id: " + elementId + " in " + getFlexoDocument());
					System.out.println(getFlexoDocument().debugStructuredContents());
				}
			}
			return null;
		}

		@Override
		public void checkConsistency() throws FragmentConsistencyException {
			if (getFlexoDocument() == null) {
				throw new FragmentConsistencyException();
			}
			int startIndex = getFlexoDocument().getElements().indexOf(getStartElement());
			if (startIndex == -1) {
				throw new FragmentConsistencyException();
			}
			int endIndex = getFlexoDocument().getElements().indexOf(getEndElement());
			if (endIndex == -1) {
				throw new FragmentConsistencyException();
			}
			if (endIndex < startIndex) {
				throw new FragmentConsistencyException();
			}
			// Otherwise, that's ok
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FlexoDocumentFragment)) {
				return false;
			}
			FlexoDocumentFragment f2 = (FlexoDocumentFragment) obj;
			return getFlexoDocument().equals(f2.getFlexoDocument()) && getStartElement().equals(f2.getStartElement())
					&& getEndElement().equals(f2.getEndElement());
		}

		@Override
		public String getStringRepresentation() {
			return (getStartElement() instanceof FlexoParagraph ? ((FlexoParagraph) getStartElement()).getRawTextPreview()
					: (getStartElement() != null ? getStartElement().toString() : "?"))
					+ " : "
					+ (getStartElement() != getEndElement()
							? (getEndElement() instanceof FlexoParagraph ? ((FlexoParagraph) getEndElement()).getRawTextPreview()
									: (getEndElement() != null ? getEndElement().toString() : "?"))
							: "");
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> startElement, int startRunId, int startCharId,
				FlexoDocumentElement<D, TA> endElement, int endRunId, int endCharId) throws FragmentConsistencyException {
			return getFlexoDocument().getFactory().makeTextSelection(this, startElement, startRunId, startCharId, endElement, endRunId,
					endCharId);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> startElement, int startRunId,
				FlexoDocumentElement<D, TA> endElement, int endRunId) throws FragmentConsistencyException {
			return makeTextSelection(startElement, startRunId, -1, endElement, endRunId, -1);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> element, int startRunId, int startCharId, int endRunId,
				int endCharId) throws FragmentConsistencyException {
			return makeTextSelection(element, startRunId, startCharId, element, endRunId, endCharId);
		}

		@Override
		public TextSelection<D, TA> makeTextSelection(FlexoDocumentElement<D, TA> element, int startRunId, int endRunId)
				throws FragmentConsistencyException {
			return makeTextSelection(element, startRunId, -1, element, endRunId, -1);
		}

	}

}
