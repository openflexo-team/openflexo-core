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

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

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
	public List<FlexoDocumentElement<D, TA>> getElements();

	public static abstract class FlexoDocumentFragmentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			InnerFlexoDocumentImpl<D, TA> implements FlexoDocumentFragment<D, TA> {

		@Override
		public List<FlexoDocumentElement<D, TA>> getElements() {
			int startIndex = getFlexoDocument().getElements().indexOf(getStartElement());
			int endIndex = getFlexoDocument().getElements().indexOf(getEndElement());
			return getFlexoDocument().getElements().subList(startIndex, endIndex + 1);
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
	}

}
