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
 * Generic abstract concept representing a table cell in a table of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoTableCell<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends InnerFlexoDocument<D, TA>, FlexoDocumentElementContainer<D, TA> {

	@PropertyIdentifier(type = FlexoTableRow.class)
	public static final String ROW_KEY = "row";
	@PropertyIdentifier(type = FlexoParagraph.class, cardinality = Cardinality.LIST)
	public static final String PARAGRAPHS_KEY = "paragraphs";

	@Getter(ROW_KEY)
	public FlexoTableRow<D, TA> getRow();

	@Setter(ROW_KEY)
	public void setRow(FlexoTableRow<D, TA> table);

	/**
	 * Return the list of paragraphs in this cell
	 * 
	 * @return
	 */
	@Getter(value = PARAGRAPHS_KEY, cardinality = Cardinality.LIST, inverse = FlexoParagraph.CONTAINER_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoParagraph<D, TA>> getParagraphs();

	@Setter(PARAGRAPHS_KEY)
	public void setParagraphs(List<FlexoParagraph<D, TA>> someParagraphs);

	/**
	 * Add paragraph to this {@link FlexoTableCell} (public API).<br>
	 * Paragraph will be added to underlying technology-specific document model and {@link FlexoTableCell} will be updated accordingly
	 */
	@Adder(PARAGRAPHS_KEY)
	@PastingPoint
	public void addToParagraphs(FlexoParagraph<D, TA> anParagraph);

	/**
	 * Remove paragraph from this {@link FlexoTableCell} (public API).<br>
	 * Paragraph will be removed to underlying technology-specific document model and {@link FlexoTableCell} will be updated accordingly
	 */
	@Remover(PARAGRAPHS_KEY)
	public void removeFromParagraphs(FlexoParagraph<D, TA> anParagraph);

	/**
	 * Insert paragraph to this {@link FlexoTableCell} at supplied index (public API).<br>
	 * Paragraph will be inserted to underlying technology-specific document model and {@link FlexoTableCell} will be updated accordingly
	 */
	public void insertParagraphAtIndex(FlexoParagraph<D, TA> anParagraph, int index);

	/**
	 * Moved paragraph to this {@link FlexoTableCell} at supplied index (public API).<br>
	 * Paragraph will be moved inside underlying technology-specific document model and {@link FlexoTableCell} will be updated accordingly
	 */
	public void moveParagraphToIndex(FlexoParagraph<D, TA> anParagraph, int index);

	/**
	 * Return paragraph identified by identifier, or null if no such paragraph exists
	 */
	public FlexoParagraph<D, TA> getParagraphWithIdentifier(String identifier);

	/**
	 * Return index of the cell<br>
	 * Index of a run is the cell occurence in the row
	 * 
	 * @return
	 */
	public int getIndex();

	/**
	 * Return identifier of the cell
	 * 
	 * @return
	 */
	public String getIdentifier();

	/**
	 * Return a string representation (plain text) of contents of the cell
	 * 
	 * @return
	 */
	public String getRawText();

	/**
	 * Return element identified by identifier, asserting that this element exists in the table (eg a paragraph in a cell), or null if no
	 * such element exists
	 */
	@Override
	public FlexoDocumentElement<D, TA> getElementWithIdentifier(String identifier);

	public static abstract class FlexoTableCellImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoTableCell<D, TA> {

		@Override
		public int getIndex() {
			if (getRow() != null) {
				return getRow().getTableCells().indexOf(this);
			}
			return -1;
		}

		@Override
		public String toString() {
			return "Cell" + getIndex();
		}

	}

}
