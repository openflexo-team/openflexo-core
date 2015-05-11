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

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * Generic abstract concept representing an object beeing part of a text-based document at root level<br>
 * A {@link FlexoDocument} is composed of a sequence of {@link FlexoDocumentElement}<br>
 * 
 * A FlexoDocumentElement has a unique identifier
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocumentElement.FlexoDocumentElementImpl.class)
public interface FlexoDocumentElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String IDENTIFIER_KEY = "identifier";

	/**
	 * Return identifier of the {@link FlexoParagraph} in the {@link FlexoDocument}<br>
	 * The identifier is here a {@link String} and MUST be unique regarding the whole {@link FlexoDocument}.<br>
	 * Please note that two different documents may have both a paragraph with same identifier
	 * 
	 * @return
	 */
	@Getter(IDENTIFIER_KEY)
	public String getIdentifier();

	@Setter(IDENTIFIER_KEY)
	public void setIdentifier(String identifier);

	/**
	 * Return the list of children elements for this element, which are infered to be children of current element while interpreting the
	 * document as a structured document (see {@link FlexoDocument#getStructuringStyles()})
	 * 
	 * @return
	 */
	public List<FlexoDocumentElement<D, TA>> getChildrenElements();

	public static abstract class FlexoDocumentElementImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			InnerFlexoDocumentImpl<D, TA> implements FlexoDocumentElement<D, TA> {

		private List<FlexoDocumentElement<D, TA>> childrenElements = null;

		/**
		 * Return the list of children elements for this element, which are infered to be children of current element while interpreting the
		 * document as a structured document (see {@link FlexoDocument#getStructuringStyles()})
		 * 
		 * @return
		 */
		@Override
		public List<FlexoDocumentElement<D, TA>> getChildrenElements() {
			if (childrenElements == null) {
				childrenElements = computeChildrenElements();
			}
			return childrenElements;
		}

		protected List<FlexoDocumentElement<D, TA>> computeChildrenElements() {
			if (getFlexoDocument() == null) {
				return null;
			}
			return Collections.emptyList();
		}
	}

}
