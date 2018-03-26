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

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * Implemented by all concepts which may contains some FlexoDocElement
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocElementContainer<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocObject<D, TA> {

	@PropertyIdentifier(type = FlexoDocElement.class, cardinality = Cardinality.LIST)
	public static final String ELEMENTS_KEY = "elements";

	/**
	 * Return the list of top-level elements of this document (elements like paragraphs or tables, sequentially composing the document)
	 * 
	 * @return
	 */
	@Getter(value = ELEMENTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocElement.CONTAINER_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocElement<D, TA>> getElements();

	@Setter(ELEMENTS_KEY)
	public void setElements(List<FlexoDocElement<D, TA>> someElements);

	/**
	 * Add element to this {@link FlexoDocument} (public API).<br>
	 * Element will be added to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	@Adder(ELEMENTS_KEY)
	@PastingPoint
	public void addToElements(FlexoDocElement<D, TA> anElement);

	/**
	 * Remove element from this {@link FlexoDocument} (public API).<br>
	 * Element will be removed to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	@Remover(ELEMENTS_KEY)
	public void removeFromElements(FlexoDocElement<D, TA> anElement);

	/**
	 * Insert element to this {@link FlexoDocument} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	public void insertElementAtIndex(FlexoDocElement<D, TA> anElement, int index);

	/**
	 * Moved element to this {@link FlexoDocument} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	public void moveElementToIndex(FlexoDocElement<D, TA> anElement, int index);

	/**
	 * Return element identified by identifier, asserting that this element exists in the table (eg a paragraph in a cell), or null if no
	 * such element exists
	 */
	public FlexoDocElement<D, TA> getElementWithIdentifier(String identifier);

}
