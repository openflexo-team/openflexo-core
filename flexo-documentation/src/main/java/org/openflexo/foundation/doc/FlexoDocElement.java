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
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * Generic abstract concept representing an object beeing part of a text-based document at root level<br>
 * A {@link FlexoDocument} is composed of a sequence of {@link FlexoDocElement}<br>
 * 
 * A FlexoDocElement has a unique identifier
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocElement.FlexoDocumentElementImpl.class)
public interface FlexoDocElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocObject<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String IDENTIFIER_KEY = "identifier";
	@PropertyIdentifier(type = String.class)
	public static final String BASE_IDENTIFIER_KEY = "baseIdentifier";
	@PropertyIdentifier(type = NamedDocStyle.class)
	public static final String NAMED_STYLE_KEY = "namedStyle";
	@PropertyIdentifier(type = FlexoParagraphStyle.class)
	public static final String PARAGRAPH_STYLE_KEY = "paragraphStyle";
	@PropertyIdentifier(type = FlexoDocElementContainer.class)
	public static final String CONTAINER_KEY = "container";

	public static final String CHILDREN_ELEMENTS_KEY = "childrenElements";

	/**
	 * Return identifier of the {@link FlexoDocElement} in the {@link FlexoDocument}<br>
	 * The identifier is here a {@link String} and MUST be unique regarding the whole {@link FlexoDocument}.<br>
	 * Please note that two different documents may have both a paragraph with same identifier
	 * 
	 * @return
	 */
	@Getter(IDENTIFIER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public String getIdentifier();

	@Setter(IDENTIFIER_KEY)
	public void setIdentifier(String identifier);

	/**
	 * Return identifier of the {@link FlexoDocElement} in the template {@link FlexoDocument} if this element<br>
	 * has been built according to template-based operation
	 * 
	 * @return
	 */
	@Getter(BASE_IDENTIFIER_KEY)
	public String getBaseIdentifier();

	@Setter(BASE_IDENTIFIER_KEY)
	public void setBaseIdentifier(String baseIdentifier);

	@Getter(value = NAMED_STYLE_KEY)
	public NamedDocStyle<D, TA> getNamedStyle();

	@Setter(NAMED_STYLE_KEY)
	public void setNamedStyle(NamedDocStyle<D, TA> style);

	@Getter(value = PARAGRAPH_STYLE_KEY, ignoreType = true)
	public FlexoParagraphStyle<D, TA> getParagraphStyle();

	@Setter(PARAGRAPH_STYLE_KEY)
	public void setParagraphStyle(FlexoParagraphStyle<D, TA> style);

	/**
	 * Return the list of children elements for this element, which are infered to be children of current element while interpreting the
	 * document as a structured document (see {@link FlexoDocument#getStructuringStyles()})
	 * 
	 * @return
	 */
	public List<FlexoDocElement<D, TA>> getChildrenElements();

	public void invalidateChildrenElements();

	public void notifyChildrenElementsChanged();

	/**
	 * Return container of this element in the document<br>
	 * This can be the document itself, if this element is declared as root element, or a cell of a table for example
	 * 
	 * @return
	 */
	@Getter(CONTAINER_KEY)
	public FlexoDocElementContainer<D, TA> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(FlexoDocElementContainer<D, TA> container);

	/**
	 * Return index of this element in container
	 * 
	 * @return
	 */
	public int getIndex();

	public static abstract class FlexoDocumentElementImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocObjectImpl<D, TA> implements FlexoDocElement<D, TA> {

		private List<FlexoDocElement<D, TA>> childrenElements = null;

		/**
		 * Return the list of children elements for this element, which are infered to be children of current element while interpreting the
		 * document as a structured document (see {@link FlexoDocument#getStructuringStyles()})
		 * 
		 * @return
		 */
		@Override
		public List<FlexoDocElement<D, TA>> getChildrenElements() {
			if (childrenElements == null) {
				childrenElements = computeChildrenElements();
			}
			return childrenElements;
		}

		protected List<FlexoDocElement<D, TA>> computeChildrenElements() {
			if (getFlexoDocument() == null) {
				return null;
			}
			return Collections.emptyList();
		}

		@Override
		public void invalidateChildrenElements() {
			if (childrenElements != null) {
				for (FlexoDocElement<D, TA> e : childrenElements) {
					e.invalidateChildrenElements();
				}
			}
			childrenElements = null;
		}

		@Override
		public void notifyChildrenElementsChanged() {
			// System.out.println("We notify " + CHILDREN_ELEMENTS_KEY + " for " + getChildrenElements());
			getPropertyChangeSupport().firePropertyChange(CHILDREN_ELEMENTS_KEY, null, getChildrenElements());
		}

		@Override
		public D getFlexoDocument() {
			if (getContainer() != null) {
				return getContainer().getFlexoDocument();
			}
			return null;
		}

		@Override
		public final int getIndex() {
			if (getContainer() != null && getContainer().getElements() != null) {
				return getContainer().getElements().indexOf(this);
			}
			return -1;
		}
	}

}
